package ru.komiss77.builder.menu;


import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.ValueSortedMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.EntityUtil.EntityGroup;
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




public class EntityByGroup implements InventoryProvider {
    
    
    private static final ItemStack fill = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build();
    private final World world;
    private int radius;
    private final EntityGroup group;

    
    public EntityByGroup(final World world, final int radius, final EntityGroup group) {
        this.world = world;
        this.radius = radius;
        this.group = group;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, ClickableItem.empty(EntityByGroup.fill));
        
        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        final ValueSortedMap<EntityType,Integer>count=new ValueSortedMap<>();

        if (radius>0) {

            for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e.getType()==EntityType.PLAYER) continue;
                if (EntityUtil.group(e.getType())==group) {
                    if (count.containsKey(e.getType())) {
                        count.put(e.getType(), count.get(e.getType())+1);
                    } else {
                        count.put(e.getType(), 1);
                    }
                }
            } 

        } else {

            for (final Entity e : world.getEntities()) {
                if (e.getType()==EntityType.PLAYER) continue;
                if (EntityUtil.group(e.getType())==group) {
                    if (count.containsKey(e.getType())) {
                        count.put(e.getType(), count.get(e.getType())+1);
                    } else {
                        count.put(e.getType(), 1);
                    }
                }
            } 

        }
            
            

        
        
        
        
        
        
        
        int find;
        for (final EntityType type : count.keySet()) {
            
            find = count.get(type);
            menuEntry.add(ClickableItem.of(ItemUtils.buildEntityIcon(type)
                //.name("§f"+ (Translate.getEntityName(type, EnumLang.RU_RU) ) )
                .name(Lang.t(type, p))
                .setAmount(find>64 ? 1 : find)
                .addLore("§7")
                .addLore("§7Найдено: §e"+ find )
                .addLore("§7")
                .addLore("§7ЛКМ - подробно по типу")
                .addLore("§7")
                .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этого типа" : "")
                .build(), e -> {
                    if (e.isLeftClick()) {
                        SmartInventory.builder()
                                .id("EntityByType"+p.getName())
                                .provider(new EntityByType(world, radius, type))
                                .size(6, 9)
                                .title("§2"+world.getName()+", §6"+type+", §1r="+radius).build()
                                .open(p);
                    } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                        for (final Entity entity : world.getEntities()) {
                            if (entity.getType()==type) {
                                entity.remove();
                            }
                        }
                        reopen(p, contents);
                    }
                }));  

         
        }        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
    pagination.setItemsPerPage(36);    
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        contents.set(5, 2, new InputButton(InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
            .name("§7Группа"+group.toString()+" в мире §a"+world.getName()+ (radius>0 ? " §7r=§a"+radius : "") )
            .addLore("§7")
            .addLore("§7ЛКМ - изменить радиус")
            .addLore("§7(0 - весь мир)")
            .addLore("§7")
            .build(), ""+radius, imput -> {

                if (!ApiOstrov.isInteger(imput)) {
                    p.sendMessage("§cДолжно быть число!");
                    return;
                }
                final int r = Integer.valueOf(imput);
                if (r<0 || r>100000) {
                    p.sendMessage("§cот 0 до 100000!");
                    return;
                }
                radius=r;
                reopen(p, contents);
            }));





            
            
       
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> 
            SmartInventory.builder().id("EntityMain"+p.getName()). provider(new EntityByWorld(world, radius)). size(3, 9). title("§2Сущности "+world.getName()+" §1r="+radius).build() .open(p)
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
