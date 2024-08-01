package ru.komiss77.listener;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.OCommand;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.TCUtils;

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
import java.util.List;
import java.util.UUID;

//https://emn178.github.io/online-tools/sha1_checksum.html

//не переименовывать!
public final class ResourcePacksLst implements Initiable, OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("resource").executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            if (!use) {
                pl.sendMessage("§cДанный сервер не требует пакета ресурсов!");
                return 0;
            }
            if (!PM.getOplayer(pl.getName()).resourcepack_locked) {
                pl.sendMessage("§aУ вас уже установлен пакет ресурсов!");
                return 0;
            }
            pl.addResourcePack(packUuid, link, hash, "§eУстанови этот пакет ресурсов для игры!", false);

            return Command.SINGLE_SUCCESS;
        }).build();
    }

    @Override
    public List<String> aliases() {
        return List.of("ресурс");
    }

    @Override
    public String description() {
        return "Загрузка ресурс-пака";
    }

    private static final Listener rpLst, inventoryLst, interactLst;
    public static boolean use = false;
    public static final ItemStack lock, key, lobby;
    private static String link;
    private static byte[] hash;
    private static UUID packUuid;


    static {
        key = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta im = key.getItemMeta();
        im.displayName(TCUtils.form("§bНажмите на ключик"));
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        im.setCustomModelData(1);
        key.setItemMeta(im);

        lock = new ItemStack(Material.GOLDEN_SWORD);
        im = lock.getItemMeta();
        im.displayName(TCUtils.form("§bНажмите на ключик"));
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        im.setCustomModelData(2);
        lock.setItemMeta(im);

        lobby = new ItemBuilder(Material.CRIMSON_DOOR)
            .lore("§eВернуться в лобби")
            .build();
        rpLst = new rpLst();
        interactLst = new interactLst();
        inventoryLst = new inventoryLst();
    }


    public ResourcePacksLst() { //или пытается грузить дважды, в RegisterCommands и как модуль
        reload();
        //Ostrov.getInstance().getCommand("rp").setExecutor(this);
    }

    public static void preDisconnect(final Player p) {
        if (use) {
            p.removeResourcePack(packUuid);
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
        HandlerList.unregisterAll(rpLst);
        HandlerList.unregisterAll(interactLst);
        HandlerList.unregisterAll(inventoryLst);

        final OstrovConfig packsConfig = Config.manager.getNewConfig("resoucepacks.yml", new String[]{"", "Ostrov77 resoucepacks", ""});

        packsConfig.addDefault("use", false);
        packsConfig.addDefault("block_interact", false);
        packsConfig.addDefault("block_menu", false);

        if (packsConfig.getString("default") != null) {
            link = packsConfig.getString("default");
            packsConfig.addDefault("link", link);//"http://site.ostrov77.ru/uploads/resourcepacks/ostrov77.zip");
        } else {
            packsConfig.addDefault("link", "http://site.ostrov77.ru/uploads/resourcepacks/ostrov77.zip");
        }

        packsConfig.removeKey("default");//, "http://site.ostrov77.ru/uploads/resourcepacks/none.zip");
        packsConfig.removeKey("per_world");//, "http://site.ostrov77.ru/uploads/resourcepacks/ostrov77.zip");
        packsConfig.removeKey("separate_world");
        packsConfig.saveConfig();

        //packs = new HashMap<>();
        link = packsConfig.getString("link");

        if (!packsConfig.getBoolean("use")) { //если офф в конфиге
            if (use) { //и перед этим был включен
                Ostrov.log_warn("Менеджер пакетов текстур - выгружен");
                return;
            }
            return;
        }
//Ostrov.log("--------------- link="+link);
        if (link == null || link.isEmpty()) {
            Ostrov.log_err("Менеджер пакетов текстур выгружен - URL не указан");
            return;
        }

        final boolean block_interact = packsConfig.getBoolean("block_interact");
        final boolean block_menu = packsConfig.getBoolean("block_menu");

        Ostrov.async(() -> {

            final String fileName = link.substring(link.lastIndexOf('/') + 1);
            try {
                final URL url = URI.create(link).toURL();

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
                    hash = digest.digest();
                    packUuid = UUID.randomUUID();
                    use = true;

                    Ostrov.sync(() -> {
                        Bukkit.getPluginManager().registerEvents(rpLst, Ostrov.getInstance());
                        if (block_interact) {
                            Bukkit.getPluginManager().registerEvents(interactLst, Ostrov.getInstance());
                        }
                        if (block_menu) {
                            Bukkit.getPluginManager().registerEvents(inventoryLst, Ostrov.getInstance());
                        }
                    }, 0);

                    Ostrov.log_ok("§2Пакет ресурсов " + fileName + ", hash=" + byteArray2Hex(hash));


                } catch (NoSuchAlgorithmException ex) {
                    Ostrov.log_err("Не удалось вычислить SHA1 для файла " + fileName + ": " + ex.getMessage());
                }

            } catch (IOException ex) {
                Ostrov.log_err("Не удалось загрузить пакет ресурсов : " + ex.getMessage());
            }

        }, 5);

    }


    static class rpLst implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public static void onLoad(final LocalDataLoadEvent e) {
            e.getPlayer().performCommand("resourse");
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public static void onPlayerResourcePackStatusEvent(PlayerResourcePackStatusEvent e) {
            final Player p = e.getPlayer();
            final Oplayer op = PM.getOplayer(p);
            switch (e.getStatus()) {

                case ACCEPTED -> {
                    op.resourcepack_locked = false;
                }

                case SUCCESSFULLY_LOADED -> {
                    pack_ok(e.getPlayer());
                }

                case DECLINED -> {
                    op.resourcepack_locked = true;
                    p.sendMessage(TCUtils.form("""
                            §e*******************************************************************
                            §4Твой клиент отверг пакет ресурсов. §eСкорее всего, проблема в настройках!
                            §2>>> §aКлик сюда для решения. §2<<<
                            §e*******************************************************************
                            """)
                        .hoverEvent(HoverEvent.showText(TCUtils.form("§5§oНажми для перехода")))
                        .clickEvent(ClickEvent.openUrl("https://youtu.be/dWou50o-aDQ")));
                }

                case FAILED_DOWNLOAD -> {
                    op.resourcepack_locked = true;
                    p.sendMessage(TCUtils.form("""
                            §e*******************************************************************
                            §4Твой клиент не загрузил пакет ресурсов. §eСкорее всего, проблема в настройках!
                            §2>>> §aКлик сюда для ручной загрузки. §2<<<
                            §e*******************************************************************
                            """)
                        .hoverEvent(HoverEvent.showText(TCUtils.form("§5§oНажми для загрузки")))
                        .clickEvent(ClickEvent.openUrl(link)));
                }

            }
        }
    }


    static class interactLst implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public static void onInteract(PlayerInteractEvent e) {
            final Oplayer op = PM.getOplayer(e.getPlayer().getName());
            //if (op==null) return;
            if (op.resourcepack_locked) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    e.setCancelled(true);
                    pack_err((Player) e.getPlayer());
                }
            }
        }
    }


    static class inventoryLst implements Listener {
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public static void onInventoryOpen(InventoryOpenEvent e) {
            //if (Ostrov.isCitizen(e.getPlayer())) return;
            //if ( !use || !block_menu) return;
            final Oplayer op = PM.getOplayer(e.getPlayer().getName());
            //if (op==null) return;
            if (TCUtils.deform(e.getView().title()).equals("§4Проверка Ресурс-пака") || op.menu.isProfileInventory(TCUtils.deform(e.getView().title()))) {
                return;
            }
            if (op.resourcepack_locked) {
                e.setCancelled(true);
                Ostrov.sync(() -> openCheckMenu((Player) e.getPlayer()), 1);
            }
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
        public static void onInvClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player)) return;
            if (e.getInventory().getType() != InventoryType.CHEST) return;
            if (e.getSlot() < 0 || e.getSlot() > 44 || e.getCurrentItem() == null || e.getCurrentItem().getType().isAir())
                return;

            if (TCUtils.deform(e.getView().title()).equals("§4Проверка Ресурс-пака")) {
                e.setCancelled(true);
                final Player p = (Player) e.getWhoClicked();
                final Oplayer op = PM.getOplayer(p);
                //if (op==null) return;
                if (e.getCurrentItem().getType() == lobby.getType()) {
                    ApiOstrov.sendToServer(p, "lobby0", "");
                    return;
                }

                if (ItemUtils.compareItem(e.getCurrentItem(), key, true)) {//клик на замок обрабатывать не надо, сработает при InventoryCloseEvent
                    if (e.getCurrentItem().getItemMeta().hasCustomModelData() && e.getCurrentItem().getItemMeta().getCustomModelData() == key.getItemMeta().getCustomModelData()) {
                        pack_ok(p);
                    }
                }
                p.closeInventory();
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public static void onInvClose(InventoryCloseEvent e) {
            final Oplayer op = PM.getOplayer(e.getPlayer().getName());
            if (op == null) return;
            if (e.getInventory().getType() != InventoryType.CHEST) return;
            if (TCUtils.deform(e.getView().title()).equals("§4Проверка Ресурс-пака")) {
                if (op.resourcepack_locked) {
                    pack_err((Player) e.getPlayer());
                }
            }
        }

    }


    private static void pack_ok(final Player p) {
        final Oplayer op = PM.getOplayer(p.getName());
        op.resourcepack_locked = false;
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
        p.sendMessage("§2Пакет ресурсов установлен!");
    }


    private static void pack_err(final Player p) {
        p.sendMessage("");
        p.sendMessage(TCUtils.form("§cВы не сможете играть на этом сервере без пакета ресурсов!\n§eЧто делать?:")
            .append(TCUtils.form("§aВариант 1: Попытаться еще раз. §5§o>Клик сюда для установки<")
                .hoverEvent(HoverEvent.showText(TCUtils.form("§b§oНажми для установки")))
                .clickEvent(ClickEvent.runCommand("/rp")))
            .append(TCUtils.form("§aВариант 2: Установить вручную. §5§o>Клик сюда для загрузки пакета<")
                .hoverEvent(HoverEvent.showText(TCUtils.form("§b§oНажми для установки")))
                .clickEvent(ClickEvent.openUrl(link)))
            .append(TCUtils.form("§aВариант 3: Исправить настройки. §5§o>Клик сюда для перехода<")
                .hoverEvent(HoverEvent.showText(TCUtils.form("§b§oНажми для перехода")))
                .clickEvent(ClickEvent.openUrl("https://youtu.be/dWou50o-aDQ"))));
        p.sendMessage("");
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, 1);
    }


    public static void openCheckMenu(final Player p) {
        if (!use) return; //не открывать менюшку, а то берутся предметы
        final Inventory rp_check = Bukkit.createInventory(null, 45, TCUtils.form("§4Проверка Ресурс-пака"));
        for (int i = 0; i < 44; i++) {
            rp_check.addItem(lock);
        }
        rp_check.setItem(ApiOstrov.randInt(0, 43), key);
        rp_check.setItem(44, lobby);
        p.openInventory(rp_check);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
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
