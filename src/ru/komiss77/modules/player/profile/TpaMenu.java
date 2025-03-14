package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Timer;
import ru.komiss77.commands.IOO5OOCmd;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;


public class TpaMenu implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДРУЗЬЯ.glassMat).name("§8.").build());

/*
    public static void onTpaCmd(final Player p, final Oplayer op, final String targetName) {
        if (targetName == null) {
            op.menu.openTPAsection(p);
            return;
        }

        if (p.getName().equals(targetName)) {
            p.sendMessage("§cВы не можете ТП к самому себе");
            return;
        }

        final boolean moder = p.hasPermission("ostrov.tpo");

//System.out.println("tpa1 moder?"+moder+" tpa_command="+Config.tpa_command);        
        if (Cfg.tpa_command_delay < 0 && !moder) {
            p.sendMessage("§cКоманда отключена");
            return;
        }

        //задержка даётся вызывающему
        if (Timer.has(p, "tpa_command")) { //для модеров никогда не сработает - не добавляет в таймер
            p.sendMessage("§cТелепортер перезаряжается. §7Осталось " + Timer.getLeft(p, "tpa_command") + " сек.!");
            return;
        }

        if (op.isBlackListed(targetName)) {
            p.sendMessage("§c" + targetName + " у вас в чёрном списке!");
            return;
        }

        final Player target = Bukkit.getPlayerExact(targetName);
        final Oplayer targetOp = PM.getOplayer(targetName);

        if (target == null || targetOp == null) {
            p.sendMessage("§cНет на этом сервере!");
            return;
        }

        if (targetOp.isBlackListed(p.getName())) {
            p.sendMessage("§cВы в чёрном списке у " + targetName + "!");
            return;
        }

        if (Timer.has(target, "tp_request_from_" + p.getName())) {
            p.sendMessage("§cЗапрос уже отправлен!");
            return;
        }
        Timer.add(target, "tp_request_from_" + p.getName(), 15);
        targetOp.tpRequestFrom = targetName;

        target.sendMessage(TCUtil.form("§f<obf>111<!obf>§f Запрос на телепорт от §a" + p.getName() + "§f<obf>111<!obf> §2>§aпринять§2<")
                .hoverEvent(HoverEvent.showText(Component.text("§5Клик - принять")))
                .clickEvent(ClickEvent.runCommand("/tpaccept " + p.getName()))
                .append(Component.text(" §4>§cв игнор§4<")
                        .hoverEvent(HoverEvent.showText(Component.text("§4Отправить " + p.getName() + " в игнор-лист.")))
                        .clickEvent(ClickEvent.runCommand("/ignore add " + p.getName()))));

        p.sendMessage("§6Запрос на телепорт " + target.getName() + " отправлен, действетт 15сек.");
    }
*/

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


        Oplayer findOp;
        boolean found = false;

        //final TreeSet <String> sort = new TreeSet(PM.getOplayersNames());
        //for (Player p : Bukkit.getOnlinePlayers()) {
        //    sort.addAll(p.getName());
        //}
        //sort.remove(p.getName());
        final boolean moder = p.hasPermission("ostrov.tpo");

//System.out.println("tpa1 moder?"+moder+" tpa_command="+Config.tpa_command);        
        if (Cfg.tpa_command_delay < 0 && !moder) {

            content.set(13, ClickableItem.empty(new ItemBuilder(Material.BARRIER)
                    .name("§cКоманда отключена")
                    .build()));
            return;

        }

        //задержка даётся вызывающему
        if (Timer.has(p, "tpa_command")) { //для модеров никогда не сработает - не добавляет в таймер

            content.set(13, ClickableItem.of(new ItemBuilder(Material.BARRIER)
                    .name("§cТелепортер перезаряжается!")
                    .lore("")
                    .lore("§7Осталось " + Timer.getLeft(p, "tpa_command") + " сек.!")
                    .lore("§7ЛКМ - обновить")
                    .lore("")
                    .build(), e -> {
                reopen(p, content);
            }));
            return;

        }


        final Pagination pagination = content.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


        for (final Player find : Bukkit.getOnlinePlayers()) {

            if (find.getName().equals(p.getName())) continue;

            findOp = PM.getOplayer(find);
            if (findOp == null) continue;
            found = true;
            int price;


            if (moder) {

                final ItemStack friend_item = new ItemBuilder(Material.PLAYER_HEAD)
                        .name(find.getName())
                        .lore("")
                        .lore("§7В мире: §f" + find.getWorld().getName())
                        .lore("§7Координаты: §f" + find.getLocation().getBlockX() + ":" + find.getLocation().getBlockY() + ":" + find.getLocation().getBlockZ() + ":")
                        .lore("")
                        .lore("§b*Телепорт по клику")
                        .lore("§8(право модератора)")
                        .build();

                menuEntry.add(ClickableItem.of(friend_item
                                , e -> {
                                    if (find.isOnline()) {
                                        p.closeInventory();
                                        ApiOstrov.teleportSave(p, find.getLocation(), false);
                                    } else {
                                        p.sendMessage("§c" + find.getName() + " уже оффлайн");
                                    }
                                }
                        )
                );

            } else if (op.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(Material.WITHER_SKELETON_SKULL)
                        .name(find.getName())
                        .lore("")
                        .lore("§cВ игноре")
                        .lore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(Material.WITHER_SKELETON_SKULL)
                        .name(find.getName())
                        .lore("")
                        .lore("§cВы занесены в игнор")
                        .lore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (Timer.has(find, "tp_request_from_" + p.getName())) {

                final ItemStack friend_item = new ItemBuilder(Material.CREEPER_HEAD)
                        .name(find.getName())
                        .lore("")
                        .lore("§6Запрос уже")
                        .lore("§6отправлен.")
                        .lore("")
                        .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else {

              price = IOO5OOCmd.getTpPrice(p, find.getLocation());

                final ItemStack friend_item = new ItemBuilder(Material.PLAYER_HEAD)
                        .name(find.getName())
                        .lore("")
                        .lore("§aОтправить запрос")
                        .lore("")
                        .lore("§fСтоимость телепорта: ")
                        .lore(price == 0 ? "§2бесплатно" : price + " лони")
                        .lore("(Оплата после выполнения)")
                        .lore("")
                        .build();

                menuEntry.add(ClickableItem.of(friend_item
                                , e -> {
                                    if (find.isOnline()) {
                                        p.closeInventory();
                                        Timer.add(find, "tp_request_from_" + p.getName(), 15);
                                        PM.getOplayer(find).tpRequestFrom = p.getUniqueId();
                                        find.sendMessage(TCUtil.form("§f<obf>11<!obf>§f Телепорт от §a" + p.getName() + "§f<obf>11<!obf> §2[>§aпринять§2<]")
                                                .hoverEvent(HoverEvent.showText(Component.text("§5Клик - принять")))
                                                //.clickEvent(ClickEvent.runCommand("/tpaccept " + p.getName()))
                                                .clickEvent(ClickEvent.runCommand("/tpaccept"))
                                                .append(Component.text(" §4[>§cв игнор§4<]")
                                                        .hoverEvent(HoverEvent.showText(Component.text("§4Отправить " + p.getName() + " в игнор-лист.")))
                                                        .clickEvent(ClickEvent.runCommand("/ignore add " + p.getName()))));

                                        p.sendMessage("§6Запрос на телепорт " + find.getName() + " отправлен, действетт 15сек.");
                                    } else {
                                        p.sendMessage("§c" + find.getName() + " уже оффлайн");
                                        reopen(p, content);
                                    }
                                }
                        )
                );

            }


        }

//System.out.println("tpa2 found?"+found);        
        if (!found) {

            final ItemStack notFound = new ItemBuilder(Material.GLASS_BOTTLE)
                    .name("§7Никого не смогли найти..")
                    .lore("")
                    .lore("§7ЛКМ - обновить")
                    .lore("")
                    .build();

            content.set(13, ClickableItem.of(notFound, e -> {
                reopen(p, content);
            }));

        }


        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(36);


        if (!pagination.isLast()) {
            content.set(4, 8, ClickableItem.of(ItemUtil.nextPage, e
                            -> {
                        content.getHost().open(p, pagination.next().getPage());
                    }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(4, 0, ClickableItem.of(ItemUtil.previosPage, e
                            -> {
                        content.getHost().open(p, pagination.previous().getPage());
                    })
            );
        }

        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

        
              /* if (op.friends.isEmpty()) {

                    content.set( SlotPos.of(1, 4), ClickableItem.of(new ItemBuilder(Material.GLASS_BOTTLE)
                                .setName("§7Ой!")
                                .addLore("У Вас пока нет друзей!")
                                .addLore("")
                                .addLore("Чтобы добавить друга/подругу,")
                                .addLore("§7встаньте рядом и")
                                .addLore("§7клик на бутылочку.")
                                .addLore("")
                                .build(), e-> {

                                }
                        ) 
                    );

                } else {*/

        



        

        
        /*
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        */


    }


}
