package ru.komiss77.modules.games;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.enums.Game;
import ru.komiss77.enums.ServerType;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class GameSignEditor implements InventoryProvider {
    
    
    private final Sign sign;
//    private final SignSide side;
    
    private Game game;
    
    public GameSignEditor(final Sign sign) {
        this.sign = sign;
//        this.side = sign.getSide(Side.FRONT);//---
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_LODESTONE_COMPASS_LOCK, 15, 1);
        

        

        
        
        
        if (game==null) {
            
            for (final String serverName : GM.allBungeeServersName) {//(Game g : Game.values()) {
                final Game g = Game.fromServerName(serverName);
                if (g==Game.GLOBAL) continue;
                
                final ItemStack is = new ItemBuilder(Material.matchMaterial(g.mat))
                    .name(g.displayName)
                    .addLore("")
                    .addLore("§6Сервер: §b"+serverName)
                    .addLore("")
                    .addLore("§7Выбрать этот сервер")
                    .addLore("§7для таблички")
                    .addLore("")
                    .build();
                
                    contents.add(ClickableItem.of(is, e -> {
                        game = g;
                        if (g.type == ServerType.ONE_GAME) {
                            GM.addGameSign(p, sign, serverName, "");
                        } else {
                            reopen(p, contents);
                        }
                    }));
            }
            
        } else {
            
            final GameInfo gi = GM.getGameInfo(game);
            if (gi==null) {
                contents.set(1,4, ClickableItem.empty(new ItemBuilder( Material.GLASS_BOTTLE)
                    .name("§7GameInfo отсутствует!")
                    .build()));
                return;
            }
            
            for (ArenaInfo ai : gi.arenas.values()) {
                final ItemStack is = new ItemBuilder(ai.mat)
                    .name(game.displayName)
                    .addLore("")
                    .addLore("§6Сервер: §b"+ai.server)
                    .addLore("§eАрена: §a"+ai.arenaName)
                    .addLore("")
                    .addLore("§7Выбрать этот арену")
                    .addLore("§7для таблички")
                    .addLore("")
                    .build();

                contents.add(ClickableItem.of(is, e -> {
                    GM.addGameSign(p, sign, ai.server, ai.arenaName);
                }));            
            }
            
        }
        
        

    }

    
    
    

    
    
    
    
    
    
    
    
    
    
}
