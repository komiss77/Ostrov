package ru.komiss77.builder.menu;

import java.util.*;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.ValueSortedMap;
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

public class EntityServerMenu implements InventoryProvider {

    private static final ItemStack fill = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build();;
    private final List<World> worlds;

    
    public EntityServerMenu(final List<World> worlds) {
        this.worlds = worlds;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, ClickableItem.empty(EntityServerMenu.fill));
        
        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();


      final ValueSortedMap<EntityGroup,Integer> count=new ValueSortedMap<>(true);

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

          int tile =  world.getTileEntityCount();
          int tickingTile =  world.getTickableTileEntityCount();
          if (tile>0) count.put(EntityGroup.TILE, tile);
          if (tickingTile>0) count.put(EntityGroup.TICKABLE_TILE, tickingTile);

          final List<Component> lore=new ArrayList<>();
          lore.add(Component.text("§7Найдено всего: §e"+ total));
          for (final Map.Entry <EntityGroup, Integer> en : count.entrySet()) {
            lore.add(Component.text(en.getKey().displayName+" §7: §6"+en.getValue()));
          }
            
            
            menuEntry.add( ClickableItem.of( new ItemBuilder(Material.HEART_OF_THE_SEA)
                .name("§f"+world.getName() )
                .setLore(lore)
                .addLore("§7")
                .addLore("§7ЛКМ - подробно по миру")
                .addLore("§7")
                .build(), e -> {
                    if (e.isLeftClick()) {
                      PM.getOplayer(p).setup.openEntityWorldMenu(p, world, -1);
                    }
                }));
            
            
            count.clear();
            total = 0;

         
        }        
        
        
        
        

        
    pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(36);    
        

       
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
          PM.getOplayer(p).setup.openMainSetupMenu(p)
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
