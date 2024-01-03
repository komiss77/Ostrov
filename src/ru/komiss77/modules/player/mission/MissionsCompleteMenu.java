package ru.komiss77.modules.player.mission;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class MissionsCompleteMenu implements InventoryProvider {
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.BROWN_STAINED_GLASS_PANE).name("§8.").build());
    //private final List<Integer>ids;
    private int index = 0;
    
    public MissionsCompleteMenu() {
        //ids = new ArrayList<>();
    }
    

    
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 5, 5);
        content.set(0,fill);
        content.set(1,fill);
        content.set(3,fill);
        content.set(4,fill);
//System.out.println("completed="+completed.toString());        
        
        final Oplayer op = PM.getOplayer(p);
        
        
            

        if (MissionManager.missions.isEmpty()) {
            
            content.set(2, ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7Миссия невыполнима")
                .addLore("")
                .addLore("§5Нет активных миссий" )
                .addLore("")
                .build()
            ));
            return;

        } 
        
        if (op.missionIds.isEmpty()) {
            
            content.set(2, ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7Миссия невыполнима")
                .addLore("")
                .addLore("§5Нет миссий на выполнении" )
                .addLore("")
                .build()
            ));

            return;
        }
       
            
        //final List <ClickableItem> buttons = new ArrayList<>();
        final List<Integer>ids = new ArrayList<>(op.missionIds);
        //ids.addAll(op.missionIds);

        
        final int missionId = ids.get(index);
        final Mission mi = MissionManager.missions.get(missionId);
        //Mission mi;
      //  for (final int missionId : op.missionIds) {
            
            //mi = MissionManager.missions.get(missionId);
            if (mi==null) {
                
                content.set(2, ClickableItem.empty(new ItemBuilder( Material.MUSIC_DISC_11)
                    .name("§7ID: §3"+missionId)
                    .addLore("§cМиссия неактивна")
                    .build()
                ));
                
            } else {
                
                content.set(2,ClickableItem.of(new ItemBuilder(mi.mat)
                    .name(mi.displayName())
                    .addLore("§7Награда: §e"+mi.reward+" рил")
                    .build(), e-> {
                        p.closeInventory();
                        p.performCommand("mission complete "+missionId);
                    }
                ));
                
            }

            
       // }*/


        
               
        
        

        
        
        
        
        
        
        //final Pagination pagination = content.pagination();

        //pagination.setItems(buttons.toArray(new ClickableItem[buttons.size()]));
        //pagination.setItemsPerPage(1);    


        if (index<ids.size()-1) {
            content.set(4, ClickableItem.of(ItemUtils.nextPage, e 
                    -> {
                index++;
                reopen(p, content);
            }
            ));
        }

        if (index>0) {
            content.set(0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> {
                index--;
                reopen(p, content);
               })
            );
        }

        //pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(2)).allowOverride(false));

        

    }


    
    
    
    
    
    
    
    
    
}
