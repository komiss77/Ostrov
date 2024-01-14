package ru.komiss77.modules.player.profile;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.StatFlag;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;




public class JustPlayMenu implements InventoryProvider {
    
    private static final ClickableItem rail = ClickableItem.empty(new ItemBuilder(Material.ACTIVATOR_RAIL).name("§8.").build());
    private static final ClickableItem bubble = ClickableItem.empty(new ItemBuilder(Material.GLOW_LICHEN).name("§8.").build());
    private static final ClickableItem stone = ClickableItem.empty(new ItemBuilder(Material.LODESTONE).name("§8.").build());
    private static final ClickableItem slab = ClickableItem.empty(new ItemBuilder(Material.SMOOTH_STONE_SLAB).name("§8.").build());

    
    
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

        
        
        
        if (op.hasFlag(StatFlag.MiniGameMenuMode)) {
            
            int slot;

            for (final Game game : Game.values()) {
                if (game.type != ServerType.ARENAS && game.type != ServerType.LOBBY) {
                     continue;
                }
                final GameInfo gi = GM.getGameInfo(game);
                if (gi==null) continue;

                switch (game) {
                    case HS -> slot=40;
                    case LOBBY -> slot=49;
                    default -> {    
                        slot=game.menuSlot;
                    }
                }

                if (slot>0) {
                    content.set(slot, ClickableItem.of( gi.getIcon(op), e-> {
                            p.performCommand("server "+game.serverName);
                        }
                    ));
                }

                content.set(22, ClickableItem.of( new ItemBuilder(Material.RECOVERY_COMPASS)
                    .name("§e§lБОЛЬШИЕ СЕРВЕРА")
                    .addLore("")
                    .addLore("§e§lКреатив")
                    .addLore("§a§lВыживание")
                    .addLore("§d§lКланы")
                    .addLore("§c§lХардкор")
                    .addLore("§b§lРазвитие")
                    .addLore("§аи другие...")
                    .build(), e-> {
                        op.setFlag(StatFlag.MiniGameMenuMode, false);
                        reopen(p, content);
                    }
                ));


            }  
            
            
            
            
        } else {

            int slot;

            for (final Game game : Game.values()) {
                if (game.type != ServerType.ONE_GAME && game.type != ServerType.LOBBY) {
                     continue;
                }

                final GameInfo gi = GM.getGameInfo(game);
                if (gi==null) continue;

                switch (game) {
                    case DA -> slot=3;
                    case SK -> slot=5;
                    case AR -> slot=11;
                    case SE -> slot=15;
                    //
                    case OB -> slot=29;
                    case PA -> slot=33;
                    case SD -> slot=39;
                    case MI -> slot=41;
                    case LOBBY -> slot=49;
                    default -> slot=0;
                }

                if (slot>0) {
                    content.set(slot, ClickableItem.of( gi.getIcon(op), e-> {
                            p.performCommand("server "+game.serverName);
                        }
                    ));
                }

                content.set(22, ClickableItem.of( new ItemBuilder(Material.RECOVERY_COMPASS)
                    .name("§a§lМ§d§lИ§c§lН§e§lИ§9§lИ§5§lГ§4§lР§b§lЫ")
                    .addLore("")
                    .addLore("§e§lБедВарс")
                    .addLore("§4§lГолодные Игры")
                    .addLore("§5§lСкайВарс")
                    .addLore("§a§lБитва Строителей")
                    .addLore("§5§lКонтра")
                    .addLore("§3§lПрятки")
                    .addLore("§b§lКит-ПВП")
                    .addLore("§аи другие...")
                    .build(), e-> {
                        op.setFlag(StatFlag.MiniGameMenuMode, true);
                        reopen(p, content);
                    }
                ));


            }
        }

        

    }
    
    
    
    
    
    
    
    
    
    
}
