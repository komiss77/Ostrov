package ru.komiss77.builder.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;




public class EntityByWorlds implements InventoryProvider {
    
    
    
    private static final ItemStack fill = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build();;
    private final Set<World> worlds;

    
    public EntityByWorlds(final Set<World> worlds) {
        this.worlds = worlds;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, ClickableItem.empty(EntityByWorlds.fill));
        
        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        
        
        
        
        
        final Map<EntityGroup,Integer>count=new HashMap<>();
        int total=0;
        
        
        
        for (final World world : worlds) {
            
            
            EntityGroup group;
            for (final Entity e : world.getEntities()) {
                if (e.getType()==EntityType.PLAYER) continue;
                group=EntityUtil.group(e);
                if (count.containsKey(group)) {
                    count.put(group, count.get(group)+1);
                } else {
                    count.put(group, 1);
                }
                total++;
            } 
            
            
            
            
            
            menuEntry.add( ClickableItem.of( new ItemBuilder(Material.HEART_OF_THE_SEA)
                .name("§f"+world.getName() )
                .addLore("§7")
                .addLore("§7Найдено всего: §e"+ total )
                .addLore(EntityGroup.MONSTER.displayName+" §7: §6"+ (count.containsKey(EntityGroup.MONSTER) ? "§e"+count.get(EntityGroup.MONSTER) : "не найдено") )
                .addLore(EntityGroup.CREATURE.displayName+" §7: §6"+ (count.containsKey(EntityGroup.CREATURE) ? "§e"+count.get(EntityGroup.CREATURE) : "не найдено") )
                .addLore(EntityGroup.WATER_CREATURE.displayName+" §7: §6"+ (count.containsKey(EntityGroup.WATER_CREATURE) ? "§e"+count.get(EntityGroup.WATER_CREATURE) : "не найдено") )
                .addLore(EntityGroup.AMBIENT.displayName+" §7: §6"+ (count.containsKey(EntityGroup.AMBIENT) ? "§e"+count.get(EntityGroup.AMBIENT) : "не найдено") )
                .addLore(EntityGroup.WATER_AMBIENT.displayName+" §7: §6"+ (count.containsKey(EntityGroup.WATER_AMBIENT) ? "§e"+count.get(EntityGroup.WATER_AMBIENT) : "не найдено") )
                .addLore(EntityGroup.UNDEFINED.displayName+" §7: §6"+ (count.containsKey(EntityGroup.UNDEFINED) ? "§e"+count.get(EntityGroup.UNDEFINED) : "не найдено") )
                .addLore("§7")
                .addLore("§7ЛКМ - подробно по миру")
                .addLore("§7")
                .build(), e -> {
                    if (e.isLeftClick()) {
                        SmartInventory.builder().id("EntityMain"+p.getName()). provider(new EntityByWorld(world, -1)). size(3, 9). title("§2Сущности "+world.getName()).build() .open(p);
                    }
                }));
            
            
            count.clear();
            total = 0;

         
        }        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(36);    
        
        
        
        
        
        
        
        
        
        
        
        

    


            
            
       
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> 
            p.closeInventory()
        ));
        

        
        if (!pagination.isLast()) {
            contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> contents.getHost().open(p, pagination.next().getPage()) )
            );
        }

        if (!pagination.isFirst()) {
            contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> contents.getHost().open(p, pagination.previous().getPage()) )
            );
        }
        
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));
        

        

        
        
        
        
        

    }
    
    
    
    
    
    
    
    
    
    
}
