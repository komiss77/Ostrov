package ru.komiss77.builder.menu;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.Perm;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.Group;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class ViewGroups implements InventoryProvider {
    
    
    
    

    
    public ViewGroups() {
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);


        


        Material mat;
        
        int staffSlot = 28;
        int donatSlot = 10; 
        
        for (Group g : Perm.getGroups()) {
            mat = Material.matchMaterial(g.mat);
            if (mat==null) mat = Material.BEDROCK;
            
            final List<String>lore = new ArrayList<>();
            lore.add("§7Системное название: §6"+g.name);
            lore.add("§2Пермишены загружены:");
            
            for (String perm:g.permissions) {
                lore.add("§7"+perm);
            }
                    
            if (g.isStaff()) {
                contents.set(staffSlot,ClickableItem.empty(new ItemBuilder(mat)
                    .name(g.chat_name )
                    .addLore(lore)
                    .build()));        
                staffSlot++;
            } else {
                contents.set(donatSlot, ClickableItem.empty(new ItemBuilder(mat)
                    .name(g.chat_name )
                    .addLore(lore)
                    .build()));         
                donatSlot++;
            }     
            
        }
        
        //TreeMap <String,Boolean>perm = new TreeMap();

        
     
            
            
        
        
        

        
        
            
            
        
        


        
        
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> 
            PM.getOplayer(p).setup.openMainSetupMenu(p)
        ));
        


        

    }
    
    
    
    

}
