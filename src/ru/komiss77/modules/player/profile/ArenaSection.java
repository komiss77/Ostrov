package ru.komiss77.modules.player.profile;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.PM;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;


public class ArenaSection implements InventoryProvider {
    
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
        
        
        if (pm.game == null) return;
        final GameInfo gi = GM.getGameInfo(pm.game);
        if (gi == null) return;
        
        final Pagination pagination = content.pagination();
        final ClickableItem[] ci = new ClickableItem[gi.count()];
        
        
        for (final ArenaInfo ai : gi.arenas()) {
            
            ci[ai.slot] = ClickableItem.of( ai.getIcon(op)
                    , e-> {
                        
                        final boolean hasLevel =  op.getStat(Stat.LEVEL)>=ai.level;
                        final boolean hasReputation =  op.reputationCalc>=ai.reputation;
                        if (hasLevel && hasReputation) {
                            p.performCommand("server "+ai.server+" "+ai.arenaName);
                        } else {
                            PM.soundDeny(p);
                        }
                        
                    }
            );
                    

            

            
        }                
        

                
                

        
        //final Pagination pagination = content.pagination();
        pagination.setItems(ci);// pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(36);    
        

        pagination.page(pm.arenaPage);
        
        if (!pagination.isLast()) {
            content.set(4, 8, ClickableItem.of(new ItemBuilder(ItemUtils.nextPage).name(Game.getGamePageTitle(pm.arenaPage+1)).build(), e 
                    -> {
                pm.arenaPage = pagination.next().getPage();
                content.getHost().open(p, pm.arenaPage);
            }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(4, 0, ClickableItem.of(new ItemBuilder(ItemUtils.previosPage).name(Game.getGamePageTitle(pm.arenaPage-1)).build(), e 
                    -> {
                pm.arenaPage = pagination.previous().getPage();
                content.getHost().open(p, pm.arenaPage) ;
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
