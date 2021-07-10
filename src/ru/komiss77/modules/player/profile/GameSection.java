package ru.komiss77.modules.player.profile;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.PM;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class GameSection implements InventoryProvider {
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("§8.").build());
    

    
    public GameSection() {
    }
     
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
        PM.getOplayer(p).menu.game = null;
    }

    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        
        final Oplayer op = PM.getOplayer(p);
        final ProfileManager pm = op.menu;
        
        
        
        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        
        
        
        //final Pagination pagination = content.pagination();
        //final ArrayList<ClickableItem> menuEntry = new ArrayList<>();        
        
        
        
        pm.game = null;
        
        for (Game game : Game.values()) {
            
            final GameInfo gi = GM.getGameInfo(game);
            if (gi==null) {
                content.set(game.menuSlot, ClickableItem.empty(new ItemBuilder(Material.matchMaterial(game.mat))
                        .setName(game.displayName)
                        .lore("")
                        .lore("§cИгра недоступна")
                        .lore("")
                        .build() 
                    )
                );
                continue;
            }
            
            final boolean hasLevel =  op.getStat(Stat.LEVEL)>=game.level;
            final boolean hasReputation =  op.getDataInt(Data.REPUTATION)>=game.reputation;


                    
            switch (game.type) {
                
                case ONE_GAME:
                    content.set(game.menuSlot, ClickableItem.of(new ItemBuilder(gi.mat)
                        .setName(game.displayName)
                        //.addFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore("")
                        .lore (gi.getState().displayColor+gi.getState().name())
                        .lore( gi.getOnline()>=0 ? "§7Играют: "+gi.getOnline() : "§4Сервер выключен" )
                        .lore("")
                        .lore( hasLevel && hasReputation ? (gi.getOnline() >=0 ? "§a⊳ Клик - перейти на сервер" : "") : "§eНедоступен !")
                        .lore(  hasLevel ? "§7Требуемый уровень : §6" +game.level : "§cБудет доступны с уровня §e"+game.level)
                        .lore(  hasReputation ? "§7Требуемый репутация : §a+" +game.reputation : "§cДоступны при репутации §a+"+game.reputation)
                        .lore("")
                        .addLore(game.description)
                        .build()
                            , e-> {
                                if (hasLevel && hasReputation) {
                                    final ArenaInfo ai = gi.arenas.get(0);
                                    if (ai!=null) {
                                        if (ai.server.equals(GM.this_server_name)) {
                                            p.sendMessage("§6Вы и так уже на этом сервере!");
                                            return;
                                        }
                                        ApiOstrov.sendToServer(p, ai.server, ai.arenaName);
                                    }
                                } 
                            }
                        )
                    );
                    break;
                    
                    
                case LOBBY:
                    content.set(game.menuSlot, ClickableItem.of(new ItemBuilder(gi.mat)
                        .setName(game.displayName)
                        //.addFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore("")
                        .lore (gi.getState().displayColor+gi.getState().name())
                        //.lore( gi.getOnline()>=0 ? "§7Играют: "+gi.getOnline() : "§4Лобби выключено" )
                        .lore("")
                        //.addLore(game.description)
                        //.lore( hasLevel && hasReputation ? (gi.getOnline() >=0 ? "§a⊳ Клик - перейти на сервер" : "") : "§eНедоступен !")
                        //.lore(  hasLevel ? "§7Требуемый уровень : §6" +game.level : "§cБудет доступны с уровня §e"+game.level)
                        //.lore(  hasReputation ? "§7Требуемый репутация : §a+" +game.reputation : "§cДоступны при репутации §a+"+game.reputation)
                        .build()
                            , e-> {
                                if (hasLevel && hasReputation) {
final ArenaInfo ai = gi.arenas.get(0);
ApiOstrov.sendToServer(p, ai.server, ai.arenaName);
                                } 
                            }
                        )
                    );
                    break;
                    
                    
                case ARENAS:
                    content.set(game.menuSlot, ClickableItem.of(new ItemBuilder(gi.mat)
                        .setName(game.displayName)
                        //.addFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore("")
                        .lore (gi.getState().displayColor+gi.getState().name())
                        .lore( gi.getOnline()>=0 ? "§7Играют: "+gi.getOnline() : "" )
                        .lore("")
                        .lore( hasLevel && hasReputation ? (gi.getOnline() >=0 ? "§a⊳ Клик - выбрать арену" : "") : "§eНедоступен !")
                        .lore(  hasLevel ? "§7Требуемый уровень : §6" +game.level : "§cБудет доступны с уровня §e"+game.level)
                        .lore(  hasReputation ? "§7Требуемая репутация : §a+" +game.reputation : "§cДоступны при репутации §a+"+game.reputation)
                        .lore("")
                        .addLore(game.description)
                        .build()
                            , e-> {
                                if (hasLevel && hasReputation) {
                                    pm.game = game; //для динамической обновы
final ArenaInfo ai = gi.arenas.get(0);
ApiOstrov.sendToServer(p, ai.server, ai.arenaName);                                } 
                            }
                        )
                    );
                    /*
                    if (ApiOstrov.getWarpManager()!=null && ApiOstrov.getWarpManager().exist(game.name())) {
                        lore.set(1,"§a  Лев.клик - к табличкам");
                        lore.set(2,"§a⊳ Прав.клик - выбрать арену"); //line 1-4+разделитель
                    } else {
                        lore.set(2,"§a⊳ Клик - выбрать арену");
                    }
                    
                                                if (ApiOstrov.getWarpManager().exist(gameInfo.game.name())) {
                                p.closeInventory();
                                ApiOstrov.teleportSave(p, ApiOstrov.getWarpManager().getWarp(gameInfo.game.name()).loc, false);
                                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 2);
                            } else {
//System.out.println("openInventory "+si.server);
                                Ostrov.sync( () -> p.openInventory(gameInfo.arena_inv), 2 );
                                //p.openInventory(si.arena_inv);
                                p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1, 2);
                            }
                            
                        } else {
                            
                            p.openInventory(gameInfo.arena_inv);
                            p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1, 2);
                            
                        }
                    
                    */
                    break;
                    
                
            }
            

            
        }                
        

                
                
                









        
        
        
        
        
        
        
        
              
           /* 
        
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(36);    
        

        if (!pagination.isLast()) {
            content.set(4, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> {
                content.getHost().open(p, pagination.next().getPage()) ;
            }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(4, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> {
                content.getHost().open(p, pagination.previous().getPage()) ;
               })
            );
        }
        
        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));
*/



        

        
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        


        

    }


    
    
    
    
    
    
    
    
    
}
