package ru.komiss77.commands;

import java.util.ArrayList;
import com.mojang.brigadier.Command;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.inventory.*;


public class SpyCmd {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    public SpyCmd() { //новое
        final String name = "name";
        new OCmdBuilder("spy", "/spy [игрок]").run(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player p)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            final Oplayer op = PM.getOplayer(p);
            if (!op.isStaff && !ApiOstrov.canBeBuilder(p)) {
                p.sendMessage("§cДоступно только персоналу!");
                return 0;
            }

            SpyMenu.open(p);
            return Command.SINGLE_SUCCESS;
        }).then(Resolver.player(name)).run(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player p)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            final Oplayer op = PM.getOplayer(p);
            if (!op.isStaff) {
                p.sendMessage("§cДоступно только персоналу!");
                return 0;
            }

            final Player tgt = Resolver.player(cntx, name);
            if (tgt == null) {
                p.sendMessage(Ostrov.PREFIX + "§cТакой игрок не онлайн!");
                return 0;
            }
            if (op.spyOrigin != null) {
                cs.sendMessage("§cСначала закончи текущее наблюдение!");
                return 0;
            }
            if (p.getEntityId() == tgt.getEntityId()) {
                cs.sendMessage("§cЗа собой следить не получится!");
                return 0;
            }
            if (tgt.getGameMode() == GameMode.SPECTATOR) {
                cs.sendMessage("§c" + tgt.getName() + " в режиме зрителя!");
                return 0;
            }

            final Location loc = p.getLocation().clone();
            op.spyOldGm = p.getGameMode(); //!! до setGameMode
            p.setGameMode(GameMode.SPECTATOR);
            tgt.hidePlayer(Ostrov.instance, p);
            p.teleport(tgt);
            Ostrov.sync(() -> {
                p.setSpectatorTarget(tgt);
                op.spyOrigin = loc; //после вселения в цель, или не даст ТП в PlayerTeleportEvent
                ScreenUtil.sendActionBarDirect(p, "§bПрисесть - закончить наблюдение");
            }, 5);
            tgt.sendMessage(Ostrov.PREFIX + "§7За тобой наблюдает Персонал.");
            //это как вору прошептать: за тобой следят через камеру - да, сори Паша, но мы честный сервер
            op.tag.visible(false);

            return Command.SINGLE_SUCCESS;
        }).description("Слежка за игроками").register(Ostrov.mgr);
    }

    public static class SpyMenu implements InventoryProvider {

        private final ItemStack fill = new ItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).name("§8.").build();

        public static void open(Player p) {
            SmartInventory.builder()
                .id("SpyMenu" + p.getName())
                .provider(new SpyMenu())
                .size(6, 9)
                .title("§5За кем следим?")
                .build().open(p);
        }

        @Override
        public void init(final Player p, final InventoryContent contents) {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, .5f, 5);
            contents.fillBorders(ClickableItem.empty(fill));

            final Oplayer op = PM.getOplayer(p.getUniqueId());

            if (op.spyOrigin != null) {
                contents.set(2, 4, ClickableItem.of(new ItemBuilder(ItemType.REDSTONE)
                    .name("§6Закончить текущее наблюдение")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        p.setGameMode(GameMode.CREATIVE); //гм не совпадает - таймер отменит наблюдение сам
                        Ostrov.sync(() -> reopen(p, contents), 2); //откроет для выбора новой цели
                    }
                }));
                return;
            }

            final Pagination pagination = contents.pagination();
            final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

            for (final Player pl : Bukkit.getOnlinePlayers()) {

                if (pl.getEntityId() == p.getEntityId()
                    || pl.getGameMode() == GameMode.SPECTATOR)
                    continue;

                final ItemStack icon = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name("§7Слежка за §e" + pl.getName() + " <dark_gray>(ЛКМ)")
                    .skullOf(pl)
                    .build();

                menuEntry.add(ClickableItem.of(icon, e -> {
                    if (e.isLeftClick()) {
                        p.closeInventory();
                        p.performCommand("spy " + pl.getName());
                    } else {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                    }
                }));
            }

            pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
            pagination.setItemsPerPage(21);

            if (!pagination.isLast()) {
                contents.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
                    -> contents.getHost().open(p, pagination.next().getPage()))
                );
            }

            if (!pagination.isFirst()) {
                contents.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> contents.getHost().open(p, pagination.previous().getPage()))
                );
            }

            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL,
                SlotPos.of(1, 1)).allowOverride(false));
        }
    }
}
    
    
 
