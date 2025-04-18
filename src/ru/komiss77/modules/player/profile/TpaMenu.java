package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Cfg;
import ru.komiss77.Timer;
import ru.komiss77.commands.IOO5OOCmd;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.MoveUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;


public class TpaMenu implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДРУЗЬЯ.glassMat).name("§8.").build());

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
        content.fillRow(1, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
          content.set(2, section.column, Section.getMenuItem(section, op));
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

            content.set(13, ClickableItem.empty(new ItemBuilder(ItemType.BARRIER)
                .name("§cКоманда отключена")
                .build()));
            return;

        }

        //задержка даётся вызывающему
        if (Timer.has(p, "tpa_command")) { //для модеров никогда не сработает - не добавляет в таймер

            content.set(13, ClickableItem.of(new ItemBuilder(ItemType.BARRIER)
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

                final ItemStack friend_item = new ItemBuilder(ItemType.PLAYER_HEAD)
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
                                MoveUtil.safeTP(p, find.getLocation());
                            } else {
                                p.sendMessage("§c" + find.getName() + " уже оффлайн");
                            }
                        }
                    )
                );

            } else if (op.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.WITHER_SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cВ игноре")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.WITHER_SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cВы занесены в игнор")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (Timer.has(find, "tp_request_from_" + p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.CREEPER_HEAD)
                    .name(find.getName())
                    .lore("")
                    .lore("§6Запрос уже")
                    .lore("§6отправлен.")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else {

                price = IOO5OOCmd.getTpPrice(p, find.getLocation());

                final ItemStack friend_item = new ItemBuilder(ItemType.PLAYER_HEAD)
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
        if (!found) {

            final ItemStack notFound = new ItemBuilder(ItemType.GLASS_BOTTLE)
                .name("§7Никого не смогли найти..")
                .lore("")
                .lore("§7ЛКМ - обновить")
                .lore("")
                .build();

            content.set(4, ClickableItem.of(notFound, e -> {
                reopen(p, content);
            }));

        }


        pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(9);


        if (!pagination.isLast()) {
            content.set(1, 8, ClickableItem.of(ItemUtil.nextPage, e
                    -> {
                    content.getHost().open(p, pagination.next().getPage());
                }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(1, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> {
                    content.getHost().open(p, pagination.previous().getPage());
                })
            );
        }

        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));
    }


}
