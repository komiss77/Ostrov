package ru.komiss77.builder.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;




public class EntityByType implements InventoryProvider {
    
    
    private static final ItemStack fill = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build();;
    private final World world;
    private int radius;
    private final EntityType type;

    
    public EntityByType(final World world, final int radius, final EntityType type) {
        this.world = world;
        this.radius = radius;
        this.type = type;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, ClickableItem.empty(EntityByType.fill));
        
        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        
        final Map <Entity, Integer> entitys = new HashMap<>() ;
        double d;
        
        if (radius>0) {

            for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e.getType()==type) {
                    d = p.getLocation().distance(e.getLocation());
                    //entitys.put(e, LocationUtil.getDistance(p.getLocation(), e.getLocation()));
                    entitys.put(e,(int)Math.floor(d));
                }
            } 

        } else {
            
            for (final Entity e : world.getEntities()) {
                if (e.getType()==type) {
                    if (p.getWorld().getName().equals(e.getWorld().getName())) {
                        d = p.getLocation().distance(e.getLocation());
                        entitys.put(e,(int)Math.floor(d));
                    } else {
                        entitys.put(e, -1);
                    }
                    
                }
            } 

        }
          
        
        
        
        final SortedSet <Integer> distances = new TreeSet<>(entitys.values());
           
        
        
        for (final int dist : distances ) {
            for (final Entity entity : entitys.keySet()) {
                if (entitys.get(entity)==dist) {
                    
                    menuEntry.add( ClickableItem.of( new ItemBuilder(Material.ENDER_EYE)
                        .name("§f"+ entity.getLocation().getBlockX()+" §7: §f"+entity.getLocation().getBlockY()+" §7: §f"+entity.getLocation().getBlockZ() )
                        .addLore("§7Дистанция: "+( entitys.get(entity)==-1 ? "§8другой мир" : "§b"+entitys.get(entity)) )
                        .addLore("§7")
                        .addLore("§7ЛКМ - ТП к сущности")
                        .addLore("§7ПКМ - изменить характеристики")
                        .addLore("§7Шифт+ЛКМ - призвать")
                        .addLore("§7Шифт+ПКМ - удалить")
                        .addLore("§7")
                        .build(), e -> {
//Ostrov.log("CLICK="+e.getClick());
                            if (!ApiOstrov.isLocalBuilder(p, true)) return;
                            switch (e.getClick()) {
                                case LEFT -> p.teleport(entity);
                                case RIGHT -> {
                                   SmartInventory.builder()
                                    . provider(new EntitySetup(entity))
                                    . size(6, 9)
                                    . title("§2Характеристики сущности").build()
                                    .open(p);
                                }
                                case SHIFT_LEFT -> entity.teleport(p);
                                case SHIFT_RIGHT -> entity.remove();
                                    
                            }

                            if (e.getClick()!=ClickType.RIGHT) reopen(p, contents);
                        }));  
                }
            }
        }
        
        
        
        
        
        

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(36);    
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        contents.set(5, 2, new InputButton(InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
            .name("§fРадиус: §e"+ (radius>0?radius:" весь мир") )
            .addLore("§7")
            .addLore("§7ЛКМ - изменить радиус")
            .addLore("§7(0 - весь мир)")
            .addLore("§7")
            .build(), ""+radius, imput -> {

                if (!ApiOstrov.isInteger(imput)) {
                    p.sendMessage("§cДолжно быть число!");
                    return;
                }
                final int r = Integer.parseInt(imput);
                if (r<0 || r>100000) {
                    p.sendMessage("§cот 0 до 100000!");
                    return;
                }
                radius=r;
                reopen(p, contents);
            }));





            
            
       
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> 
            SmartInventory.builder()
                    .id("EntityByGroup"+p.getName())
                    . provider(new EntityByGroup(world, radius, EntityUtil.group(type)))
                    . size(6, 9)
                    . title("§2"+world.getName()+" "+type+" §1r="+radius).build() .open(p)
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
