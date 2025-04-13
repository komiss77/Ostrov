package ru.komiss77.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Random;
import java.util.UUID;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.OConfig;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.TCUtil;


public final class ResourcePacksLst implements Initiable {

    public static boolean use, onlySuggest;
    private static ResourcePackInfo pack = null;
    private static ResourcePackRequest request = null;

    public ResourcePacksLst() { //не переносить сюда обработчик команды, или пытается грузить дважды, в RegisterCommands и как модуль
        reload();
    }

    public static void execute(final Player p) {
        if (!use || request == null) {
            p.sendMessage("§cДанный сервер не требует пакета ресурсов!");
            if (p.hasResourcePack()) {
                p.clearResourcePacks();
            }
            return;
        }
        if (p.hasResourcePack()) {
            p.sendMessage("§aУ вас уже установлен пакет ресурсов!");
            return;
        }
        p.sendResourcePacks(request);
//            pl.setResourcePack(packUuid, link, hash, TCUtil.form("§eУстанови этот пакет ресурсов для игры!"), true);
    }

    public static void preDisconnect(final Player p) {
        if (use) {
            p.removeResourcePacks(request);
        }
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void onDisable() {
    }


    @Override
    public void reload() {
//Ostrov.log_warn("============== RP reload");
        final OConfig packsConfig = Cfg.manager.config("resoucepacks.yml", new String[]{"", "Ostrov77 resoucepacks", ""});

        packsConfig.addDefault("use", false);
        packsConfig.addDefault("link", "http://site.ostrov77.ru/uploads/resourcepacks/ostrov77.zip");
        packsConfig.addDefault("only_suggest", false);

        //packsConfig.removeKey("block_interact");//, "http://site.ostrov77.ru/uploads/resourcepacks/none.zip");
        //packsConfig.removeKey("block_menu");//, "http://site.ostrov77.ru/uploads/resourcepacks/ostrov77.zip");

        packsConfig.saveConfig();


        if (!packsConfig.getBoolean("use")) { //если офф в конфиге
            if (use) { //и перед этим был включен
                Ostrov.log_warn("Менеджер пакетов текстур - выгружен");
                return;
            }
            return;
        }

        final String rpLink = packsConfig.getString("link");
        if (rpLink == null || rpLink.isEmpty()) {
            Ostrov.log_err("Менеджер пакетов текстур выгружен - URL не указан");
            return;
        }

        onlySuggest = packsConfig.getBoolean("only_suggest");

        Ostrov.async(() -> {

            final String fileName = rpLink.substring(rpLink.lastIndexOf('/') + 1);
            try {
                final URL url = URI.create(rpLink).toURL();

                final File rp_file = new File(Ostrov.instance.getDataFolder(), "resourcepacks/" + fileName);
                if (!rp_file.exists()) {
                    rp_file.getParentFile().mkdirs();
                }
                Files.copy(url.openStream(), rp_file.toPath(), StandardCopyOption.REPLACE_EXISTING);

                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-1");
                    InputStream fis = new FileInputStream(rp_file);
                    int n = 0;
                    byte[] buffer = new byte[8192];
                    while (n != -1) {
                        n = fis.read(buffer);
                        if (n > 0) {
                            digest.update(buffer, 0, n);
                        }
                    }
                    fis.close();
                    final String hash = byteArray2Hex(digest.digest());
                    pack = ResourcePackInfo.resourcePackInfo(randUUID(fileName), URI.create(rpLink), hash);

                    final Component prompt;
                    if (onlySuggest) {
                        prompt = TCUtil.form("<yellow>Для полноценной игры рекомендуем установить пакет ресурсов!");
                    } else {
                        prompt = TCUtil.form("<yellow>Установи этот пакет ресурсов для игры!");
                    }

                    request = ResourcePackRequest.resourcePackRequest()
                        .packs(pack)
                        .required(!onlySuggest)
                        .replace(!onlySuggest)
                        .prompt(prompt)
                        .callback((id, status, aud) -> {
                            if (!(aud instanceof final Player p)) return;

                            switch (status) {

                                case ACCEPTED, DOWNLOADED, FAILED_RELOAD -> {
                                }

                                case INVALID_URL -> Ostrov.log_err("ResourcePackRequest INVALID_URL : " + rpLink);

                                case SUCCESSFULLY_LOADED -> {
                                    final Oplayer op = PM.getOplayer(p.getName());
                                    op.resourcepack_locked = false;
                                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                                    p.sendMessage("§2Пакет ресурсов установлен!");
                                }

                                case DISCARDED, DECLINED -> //op.resourcepack_locked = true;
                                    p.sendMessage(TCUtil.form("""
                                    §e*******************************************************************
                                    §4Твой клиент отверг пакет ресурсов. §eСкорее всего, проблема в настройках!
                                    §2>>> §aКлик для решения. §2<<<
                                    §e*******************************************************************
                                    """)
                                        .hoverEvent(HoverEvent.showText(TCUtil.form("§5§oНажми для перехода")))
                                        .clickEvent(ClickEvent.openUrl("https://youtu.be/dWou50o-aDQ")));

                                case FAILED_DOWNLOAD -> {
//                                    op.resourcepack_locked = true;
                                    p.sendMessage(TCUtil.form("""
                                          §e*******************************************************************
                                          §4Твой клиент не загрузил пакет ресурсов. §eСкорее всего, проблема в настройках!
                                          §2>>> §aКлик сюда для ручной загрузки. §2<<<
                                          §e*******************************************************************
                                          """)
                                        .hoverEvent(HoverEvent.showText(TCUtil.form("§5§oНажми для загрузки")))
                                        .clickEvent(ClickEvent.openUrl(rpLink)));
                                }
                            }
                        }).build();
                    use = true;

                    Ostrov.log_ok("§2Пакет ресурсов " + fileName + ", hash=" + hash);

                } catch (NoSuchAlgorithmException ex) {
                    Ostrov.log_err("Не удалось вычислить SHA1 для файла " + fileName + ": " + ex.getMessage());
                }

            } catch (IOException ex) {
                Ostrov.log_err("Не удалось загрузить пакет ресурсов : " + ex.getMessage());
            }

        }, 5);

    }

    public static UUID randUUID(final String of) {
        final Random ng = new Random(of.hashCode());
        final byte[] rbs = new byte[16];
        ng.nextBytes(rbs);
        rbs[6]  &= 0x0f;  /* clear version        */
        rbs[6]  |= 0x40;  /* set to version 4     */
        rbs[8]  &= 0x3f;  /* clear variant        */
        rbs[8]  |= (byte) 0x80;  /* set to IETF variant  */
        long msb = 0;
        long lsb = 0;
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (rbs[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (rbs[i] & 0xff);
        return new UUID(msb, lsb);
    }


    private static String byteArray2Hex(final byte[] hash) {
        final Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        final String frm = formatter.toString();
        formatter.close();
        return frm;
    }
}

//https://emn178.github.io/online-tools/sha1_checksum.html