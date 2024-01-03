package ru.komiss77.modules.player.profile;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;


public class GameSection implements InventoryProvider {
       
   private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.РЕЖИМЫ.glassMat).name("§8.").build());
    
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

        
        
        //final ArrayList<ClickableItem> menuEntry = new ArrayList<>(Game.MAX_SLOT+1);        
 //System.out.println("GameSection.init() MAX_SLOT="+Game.MAX_SLOT);        
        
        final Pagination pagination = content.pagination();
        final ClickableItem[] ci = new ClickableItem[Game.MAX_SLOT+1];
        
        
        //pm.game = null;
        
        
        for (Game game : Game.values()) {
            
            final GameInfo gi = GM.getGameInfo(game);
            
//System.out.println("game="+game+" gi==null?"+gi==null);                    
            if (gi==null) {
                if (game==Game.GLOBAL) {
                    continue;
                } 
                if (game==Game.LOBBY) {
                    ci[36*game.menuPage+game.menuSlot] = ClickableItem.of(new ItemBuilder(Material.matchMaterial(game.mat))
                            .name(Lang.t(p, game.displayName))
                            .addLore("")
                            .addLore("§6Вернуться в лобби")
                            .addLore("")
                            .build(), e -> {
                                p.performCommand("server lobby0");
                            }
                    );
                } else {
                    ci[36*game.menuPage+game.menuSlot] = ClickableItem.of(new ItemBuilder(Material.matchMaterial(game.mat))
                            .name(Lang.t(p, game.displayName))
                            .addLore("")
                            .addLore("§8Состояние неизвестно")
                            .addLore("")
                            .build(), e -> {
                                p.performCommand("server "+game.name()+"01"); //пытаемся отправить, бывает плохо прогружены
                            }
                    );
                }

                continue;
            }
            
            


            ci[36*game.menuPage+game.menuSlot] = ClickableItem.of( gi.getIcon(op)
                    , e-> {
                        
                        switch (game.type) {

                            case ONE_GAME -> {
                                final ArenaInfo ai = gi.arenas.get(0);
                                if (ai!=null) {
                                    if (ai.server.equals(Ostrov.MOT_D)) {
                                        p.sendMessage("§6Вы и так уже на этом сервере!");
                                        return;
                                    }
                                    p.performCommand("server "+ai.server);
                                }
                            }

                            case LOBBY, ARENAS -> pm.openArenaMenu(p, game);

                        }
                        
                    }
                    
            );
                        
        }                
        

                
                
                









        
        
        
        
        
        
        
        
              
            
        
        //final Pagination pagination = content.pagination();
        pagination.setItems(ci);// pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(36);    
        

        pagination.page(pm.gamePage);
        
        if (!pagination.isLast()) {
            content.set(4, 8, ClickableItem.of(new ItemBuilder(ItemUtils.nextPage).name(Game.getGamePageTitle(pm.gamePage+1)).build(), e 
                    -> {
                pm.gamePage = pagination.next().getPage();
                content.getHost().open(p, pm.gamePage);
            }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(4, 0, ClickableItem.of(new ItemBuilder(ItemUtils.previosPage).name(Game.getGamePageTitle(pm.gamePage-1)).build(), e 
                    -> {
                pm.gamePage = pagination.previous().getPage();
                content.getHost().open(p, pm.gamePage) ;
               })
            );
        }
        
        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));




        

        
        
       // content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
      //  {
      //      p.closeInventory();
      //  }
      //  ));
        


        

    }


    
    
    
    
    
    
    
    
    
}
