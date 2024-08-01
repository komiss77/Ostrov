package ru.komiss77.modules.player.profile;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class Passport implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ПРОФИЛЬ.glassMat).name("§8.").build());


    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));

        final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;

        //линия - разделитель
        content.fillRow(4, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }


        String current;
        InputType type;
        for (final Entry<Data, Integer> e : PM.textEdit.entrySet()) {

            current = op.getDataString(e.getKey());

            if (e.getKey() == Data.ABOUT) {
                type = InputButton.InputType.SIGN;
            } else {
                type = InputButton.InputType.ANVILL;
            }
            content.add(new InputButton(type, new ItemBuilder(current.isEmpty() ? Material.FIREWORK_STAR : Material.LIME_DYE)
                    .name(e.getKey().desc)
                    .lore("")
                    .lore("§7сейчас: ")
                    .lore(current)
                    .lore("")
                    .lore("§7ЛКМ - §eизменить")
                    .lore("")
                    .build(), current.isEmpty() ? e.getKey().desc : current, msg -> {

                    if (msg.length() > e.getValue()) {
                        msg = msg.substring(0, e.getValue());
                        p.sendMessage("§eСтрока обрезана до " + e.getKey() + " символа.");
                        //Ostrov.soundDeny(p);
                        //return;
                    }
                    // final int amount = Integer.valueOf(msg);
                    //  if (amount<1 || amount>1_000_000) {
                    //      p.sendMessage("§cот 1 до 1000000");
                    //      Ostrov.soundDeny(p);
                    //      return;
                    //   }
                    op.setData(e.getKey(), msg);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                    reopen(p, content);
                    //return;
                }
                )
            );


        }


        content.add(new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name(Data.BIRTH.desc)
                .lore("")
                .lore("§7сейчас: ")
                .lore(op.getDataString(Data.BIRTH))
                .lore("")
                .lore("§7ЛКМ - §eизменить")
                .lore("")
                .build(), op.getDataString(Data.BIRTH), msg -> {

                if (msg.equals(op.getDataString(Data.BIRTH))) {
                    p.sendMessage(Ostrov.PREFIX + "§сВы ничего не изменили");
                    return;
                }
                if (!parseDate(msg)) {
                    p.sendMessage(Ostrov.PREFIX + "§сФормат ДД.ММ.ГГГГ");
                    return;// "Формат ДД.ММ.ГГГГ";
                }
                op.setData(Data.BIRTH, msg);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                reopen(p, content);
            }
            )
        );


        content.add(new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name(Data.VK.desc)
                .lore("")
                .lore("§7сейчас: ")
                .lore(op.getDataString(Data.VK))
                .lore("")
                .lore("§7ЛКМ - §eизменить")
                .lore("")
                .build(), op.getDataString(Data.VK), msg -> {
                if (msg.equals(op.getDataString(Data.VK))) {
                    p.sendMessage(Ostrov.PREFIX + "§сВы ничего не изменили");
                    return;
                }
                if (!parseURL(msg)) {
                    p.sendMessage(Ostrov.PREFIX + "§сэто не ссылка! Надо что-то вроде http://ostrov77.ru/donate.html");
                    return;// "это не URL ссылка";
                }
                op.setData(Data.VK, msg);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                reopen(p, content);
            }
            )
        );

        content.add(new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name(Data.YOUTUBE.desc)
                .lore("")
                .lore("§7сейчас: ")
                .lore(op.getDataString(Data.YOUTUBE))
                .lore("")
                .lore("§7ЛКМ - §eизменить")
                .lore("")
                .build(), op.getDataString(Data.YOUTUBE), msg -> {
                if (msg.equals(op.getDataString(Data.YOUTUBE))) {
                    p.sendMessage(Ostrov.PREFIX + "§сВы ничего не изменили");
                    return;
                }
                if (!parseURL(msg)) {
                    p.sendMessage(Ostrov.PREFIX + "§сэто не ссылка! Надо что-то вроде http://ostrov77.ru/donate.html");
                    return;// "это не URL ссылка";
                }
                op.setData(Data.YOUTUBE, msg);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                reopen(p, content);
            }
            )
        );


        content.add(new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name(Data.PHONE.desc)
                .lore("")
                .lore("§7сейчас: ")
                .lore(op.getDataString(Data.PHONE))
                .lore("")
                .lore("§7ЛКМ - §eизменить")
                .lore("")
                .build(), op.getDataString(Data.PHONE), msg -> {
                if (msg.equals(op.getDataString(Data.PHONE))) {
                    p.sendMessage(Ostrov.PREFIX + "§сВы ничего не изменили");
                    return;
                }
                final String onlyDidgits = msg.replaceAll("\\D+", "");
                if (onlyDidgits.length() != 10) {
                    p.sendMessage(Ostrov.PREFIX + "§сНомер телефона должен быть похож на (911)777-7777");
                    return;// "пример: (911)777-7777";
                } else {
                    msg = "(" + onlyDidgits.substring(0, 3) + ") " + onlyDidgits.substring(3, 6) + "-" + onlyDidgits.substring(6, 10);
                }
                op.setData(Data.PHONE, msg);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                reopen(p, content);
            }
            )
        );


        content.add(new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name(Data.EMAIL.desc)
                .lore("")
                .lore("§7сейчас: ")
                .lore(op.getDataString(Data.EMAIL))
                .lore("")
                .lore("§7ЛКМ - §eизменить")
                .lore("")
                .build(), op.getDataString(Data.EMAIL), msg -> {
                if (msg.equals(op.getDataString(Data.EMAIL))) {
                    p.sendMessage(Ostrov.PREFIX + "§сВы ничего не изменили");
                    return;
                }
                if (!parseMail(msg)) {
                    p.sendMessage(Ostrov.PREFIX + "§сЭлектронная почта должна иметь вид " + p.getName() + "@ostrov77.ru");
                    return;// "пример: "+player.getName()+"@ostrov77.ru";
                }
                op.setData(Data.EMAIL, msg);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                reopen(p, content);
            }
            )
        );


        content.add(ClickableItem.of(new ItemBuilder(Material.AMETHYST_SHARD)
                    .name(Data.GENDER.desc)
                    .lore("")
                    .lore("§7сейчас: ")
                    .lore(op.getDataString(Data.GENDER).isEmpty() ? "бесполое" : op.getDataString(Data.GENDER))
                    .lore("")
                    .lore("§7ЛКМ - §eизменить")
                    .lore("")
                    .build(), e -> {
                    switch (op.gender) {
                        case NEUTRAL -> {
                            op.setData(Data.GENDER, "мальчик");
                            op.gender = PM.Gender.MALE;
                        }
                        case MALE -> {
                            op.setData(Data.GENDER, "девочка");
                            op.gender = PM.Gender.FEMALE;
                        }
                        case FEMALE -> {
                            op.setData(Data.GENDER, "");
                            op.gender = PM.Gender.NEUTRAL;
                        }
                    }
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                    reopen(p, content);
                }
            )
        );


        if (op.getDataString(Data.FAMILY).isEmpty()
            || op.getDataString(Data.BIRTH).isEmpty()
            || op.getDataString(Data.CITY).isEmpty()
            || op.getDataString(Data.PHONE).isEmpty()
            || op.getDataString(Data.EMAIL).isEmpty()) {

            content.add(ClickableItem.empty(new ItemBuilder(Material.WOODEN_AXE)
                    .name(Data.IPPROTECT.desc)
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .lore("")
                    .lore("§7сейчас: ")
                    .lore(op.getDataInt(Data.IPPROTECT) == 0 ? "§aвыключена" : "§cВключена")
                    .lore("")
                    .lore("§6Для включения защиты")
                    .lore("§6аккаунта по IP-адресу")
                    .lore("§6должны быть указаны:")
                    .lore(!op.getDataString(Data.FAMILY).isEmpty() ? "§a✔ §8Фамилия, Имя" : "§6Фамилия, Имя")
                    .lore(!op.getDataString(Data.BIRTH).isEmpty() ? "§a✔ §8Дата рождения" : "§6Дата рождения")
                    .lore(!op.getDataString(Data.CITY).isEmpty() ? "§a✔ §8Город" : "§6Город")
                    .lore(!op.getDataString(Data.PHONE).isEmpty() ? "§a✔ §8Телефон" : "§6Телефон")
                    .lore(!op.getDataString(Data.EMAIL).isEmpty() ? "§a✔ §8Эл.почта" : "§6Эл.почта")
                    .lore("")
                    .build()
                )
            );

        } else {

            content.add(ClickableItem.of(new ItemBuilder(op.getDataInt(Data.IPPROTECT) == 0 ? Material.WOODEN_AXE : Material.IRON_AXE)
                        .name(Data.IPPROTECT.desc)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore("")
                        .lore("§7сейчас: ")
                        .lore(op.getDataInt(Data.IPPROTECT) == 0 ? "§aвыключена" : "§cВключена")
                        .lore("")
                        .lore("§7ЛКМ - §eизменить")
                        .lore("")
                        .lore("§eОсторожно!")
                        .lore("§eЕсли у Вас не статичестий")
                        .lore("§eIP адрес, то после его смены")
                        .lore("§eне сможете подключиться!")
                        .lore("")
                        .build()
                    , e -> {
                        if (op.getDataInt(Data.IPPROTECT) == 0) {
                            op.setData(Data.IPPROTECT, 1);
                        } else {
                            op.setData(Data.IPPROTECT, 0);
                        }
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                        reopen(p, content);
                    }
                )
            );
        }


        content.add(new InputButton(InputButton.InputType.SIGN, new ItemBuilder(Material.OAK_SIGN)
                .name(Data.PASS.desc)
                .lore("")
                .lore("")
                .lore("§7ЛКМ - §eизменить")
                .lore("")
                .lore("§7Откроется табличка,")
                .lore("§7наберите на ней:")
                .lore("§71 строка: §6текущий пароль")
                .lore("§72 строка: §bновый пароль")
                .lore("§73 строка: §bповтор нового пароля")
                .lore("")
                .lore("§eПосле смены пароля")
                .lore("§eВы будете отключены от сервера,")
                .lore("§eдля перезаходя с новым паролем.")
                .lore("")
                .build(), "пароль", msg -> {
                final String[] split = msg.split(" ");
                if (split.length < 3) {
                    p.sendMessage(Ostrov.PREFIX + "§спервая строка - старый пароль, вторая и треться строка - новый пароль");
                    return;
                }
                p.closeInventory();
                ApiOstrov.executeBungeeCmd(p, "setpass " + split[0] + " " + split[1] + " " + split[2]);
            }
            )
        );


        current = op.getDataString(Data.NOTES);
        content.add(new InputButton(InputButton.InputType.SIGN, new ItemBuilder(current.isEmpty() ? Material.FIREWORK_STAR : Material.LIME_DYE)
                .name(Data.NOTES.desc)
                .lore("")
                .lore("§7сейчас: ")
                .lore(current)
                .lore("")
                .lore("§7ЛКМ - §eизменить")
                .lore("")
                .build(), current, msg -> {

                if (msg.equals(op.getDataString(Data.NOTES))) {
                    p.sendMessage(Ostrov.PREFIX + "§сВы ничего не изменили");
                    return;
                }
                op.setData(Data.NOTES, msg);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1);
                reopen(p, content);
            }
            )
        );


        content.add(ClickableItem.of(new ItemBuilder(Material.FLETCHING_TABLE)
                    .name("§7Срок хранения данных")
                    .lore("с учётом привилегий")
                    .lore("§7сейчас: " + ApiOstrov.dateFromStamp(op.getDataInt(Data.VALID)))
                    .lore("")
                    .lore("§7ЛКМ - §eизменить")
                    .lore("")
                    .build()
                , e -> {
                    op.menu.openDonate(op);
                }
            )
        );
    }


    private static boolean parseDate(final String input) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateFormat.setLenient(false);
        try {
            Date test_date = dateFormat.parse(input);
            return dateFormat.format(test_date).equals(input);
        } catch (ParseException ex) {
            return false;
        }
    }

    public static boolean parseURL(final String url) {
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }
        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }


    public static boolean parseMail(final String email) {
        //Matcher matcher = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE) .matcher(email);
        Matcher matcher = Pattern.compile("^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$", Pattern.CASE_INSENSITIVE).matcher(email);
        return matcher.find();
    }


}
