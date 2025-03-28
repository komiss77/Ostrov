package ru.komiss77.modules.player.profile;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.ServerType;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class GameMenu implements InventoryProvider {

    private static final ClickableItem rail = ClickableItem.empty(new ItemBuilder(ItemType.ACTIVATOR_RAIL).name("§0.").build());
    private static final ClickableItem bubble = ClickableItem.empty(new ItemBuilder(ItemType.GLOW_LICHEN).name("§0.").build());
    private static final ClickableItem stone = ClickableItem.empty(new ItemBuilder(ItemType.LODESTONE).name("§0.").build());
    private static final ClickableItem slab = ClickableItem.empty(new ItemBuilder(ItemType.SMOOTH_STONE_SLAB).name("§0.").build());
    public static final String nameEn = "         " + Section.РЕЖИМЫ.item_nameEn;

    private boolean mini;

    public GameMenu(final boolean mini) {
        this.mini = mini;
    }

    @Override
    public void init(final Player p, final InventoryContent content) {

        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1, 1);

        content.fill(bubble);
        content.fillColumn(0, rail);
        content.fillColumn(8, rail);
        content.set(45, stone);
        content.set(53, stone);
        content.set(46, slab);
        content.set(52, slab);

        final Oplayer op = PM.getOplayer(p);

        if (mini) {

            for (final Game game : Game.values()) {
                if (game.type != ServerType.ARENAS && game.type != ServerType.LOBBY) {
                    continue;
                }
                final GameInfo gi = GM.getGameInfo(game);
                if (gi == null) continue;

                if (game.menuSlot > 0) {
                    content.set(game.menuSlot, ClickableItem.of(gi.getIcon(p, op), e -> {
                        if (e.isLeftClick()) {
                            GM.randomPlay(p, game, null);
                        } else if (e.isRightClick()) {
                            op.menu.openArenaMenu(p, game);
                        }
                    }));
                }
            }

            content.set(22, ClickableItem.of(new ItemBuilder(ItemType.RECOVERY_COMPASS)
                    .name("<gradient:apple:dark_aqua><b>БОЛЬШИЕ РЕЖИМЫ")
                    .lore("")
                    .lore("§a§lВыживание")
                    .lore("§9§lКреатив")
                    .lore("§c§lХардкор")
                    .lore("§b§lСкайБлок")
                    .lore("§eи другие...")
                    .build(), e -> {
                    mini = false;
                    reopen(p, content);
                }
            ));


        } else {

            for (final Game game : Game.values()) {
                if (game.type != ServerType.ONE_GAME && game.type != ServerType.LOBBY) {
                    continue;
                }

                final GameInfo gi = GM.getGameInfo(game);
                if (gi == null) continue;

                if (game.menuSlot > 0) {
                    content.set(game.menuSlot, ClickableItem.of(gi.getIcon(p, op), e -> {
                        if (gi.count() == 0) return;
                        final ArenaInfo ai = gi.arenas().stream().findAny().get();//gi.arenas.get(0);
                        if (ai.server.equals(Ostrov.MOT_D)) {//(game == GM.GAME) {
                            p.sendMessage("§6Вы и так уже на этом сервере!");
                            return;
                        }
                        p.performCommand("server " + ai.server);
                    }));
                }
            }

            content.set(22, ClickableItem.of(new ItemBuilder(ItemType.RECOVERY_COMPASS)
                    .name("§a§lМ§d§lИ§c§lН§e§lИ §9§lИ§5§lГ§4§lР§b§lЫ")
                    .lore("")
                    .lore("§e§lБедВарс")
                    .lore("§4§lГолодные Игры")
                    .lore("§5§lСкайВарс")
                    .lore("§a§lБитва Строителей")
                    .lore("§5§lКонтра")
                    .lore("§3§lПрятки")
                    .lore("§b§lКит-ПВП")
                    .lore("§аи другие...")
                    .build(), e -> {
                    mini = true;
                    reopen(p, content);
                }
            ));
        }

        content.set(37, Section.getMenuItem(Section.ВОЗМОЖНОСТИ, op));

        content.set(43, Section.getMenuItem(Section.ПРОФИЛЬ, op));


    }


}
