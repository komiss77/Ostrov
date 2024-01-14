package ru.komiss77.builder.menu;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.utils.inventory.InputButton.InputType;




public class EntityByWorld implements InventoryProvider {
    
    
    
    private static final ItemStack fill = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build();;
    private final World world;
    private int radius;

    
    public EntityByWorld(final World world, final int radius) {
        this.world = world;
        this.radius = radius;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillBorders(ClickableItem.empty(EntityByWorld.fill));
        
        

        final Map<EntityGroup,Integer>count=new HashMap<>();

        EntityGroup group;

        if (radius>0) {

            for (final Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e.getType()==EntityType.PLAYER) continue;
                group = EntityUtil.group(e);//.VM.getNmsEntitygroup().getEntityType(e);
                if (count.containsKey(group)) {
                    count.put(group, count.get(group)+1);
                } else {
                    count.put(group, 1);
                }
            } 

        } else {

            for (final Entity e : world.getEntities()) {
                if (e.getType()==EntityType.PLAYER) continue;
                group = EntityUtil.group(e);//group=VM.getNmsEntitygroup().getEntityType(e);
                if (count.containsKey(group)) {
                    count.put(group, count.get(group)+1);
                } else {
                    count.put(group, 1);
                }
            } 

        }
            
            


        contents.set(0, 2, ClickableItem.of( new ItemBuilder(Material.SUNFLOWER)
            .name("§eПКМ - показать все миры")
            .addLore("")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§eПКМ - показать все миры" : "§eВключите режим билдера!")
            .addLore("")
            .build(), e -> {
                if (e.isLeftClick()) {
                    if (ApiOstrov.isLocalBuilder(p, true)) {
                        p.performCommand("entity --server");
                    }
                }
            }));  
        
        
        contents.set(0, 4, new InputButton(InputType.ANVILL, new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
            .name("§7Сущности в мире §a"+world.getName()+ (radius>0 ? " §7в радиусе §a"+radius : "") )
            .addLore("§7")
            .addLore("§fЛКМ - §bуказать радиус")
            .addLore("§7(0 - весь мир)")
            //.addLore(ApiOstrov.isLocalBuilder(p, false) ? "§eПКМ - показать все миры" : "")
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


        
        contents.set(0, 6, ClickableItem.of( new ItemBuilder(Material.REDSTONE)
            .name("§cУдалить всех найденных")
            .addLore("")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§fШифт+ЛКМ - §судалить" : "§eВключите режим билдера!")
            .addLore("")
            .build(), e -> {
                if (e.isShiftClick()) {
                    if (ApiOstrov.isLocalBuilder(p, true)) {
                        if (radius>0) {

                            for (final Entity entity : p.getNearbyEntities(radius, radius, radius)) {
                                if (entity.getType()!=EntityType.PLAYER) {
                                    entity.remove();
                                }
                            } 

                        } else {

                            for (final Entity entity : p.getWorld().getEntities()) {
                                if (entity.getType()!=EntityType.PLAYER) {
                                    entity.remove();
                                }
                            } 

                        }
                    }
                    reopen(p, contents);
                }
            }));  
        
        

        
        
        
        
            
            
            
            




        contents.set(1, 1, ClickableItem.of(new ItemBuilder(Material.ZOMBIE_HEAD)
            .name(EntityGroup.MONSTER.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.MONSTER) ? "§e"+count.get(EntityGroup.MONSTER) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.MONSTER)>0 ? world.getSpawnLimit(SpawnCategory.MONSTER) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder()
                            .id("EntityByGroup"+p.getName())
                            . provider(new EntityByGroup(world, radius, EntityGroup.MONSTER))
                            . size(6, 9)
                            . title("§2"+world.getName()+" "+EntityGroup.MONSTER.displayName+" §1r="+radius).build()
                            .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.MONSTER) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  




        contents.set(1, 2, ClickableItem.of(new ItemBuilder(Material.LEATHER_HORSE_ARMOR)
            .name(EntityGroup.CREATURE.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.CREATURE) ? "§e"+count.get(EntityGroup.CREATURE) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.ANIMAL)>0 ? world.getSpawnLimit(SpawnCategory.ANIMAL) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.CREATURE)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.CREATURE.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.CREATURE) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  


        contents.set(1, 3, ClickableItem.of(new ItemBuilder(Material.NAUTILUS_SHELL)
            .name(EntityGroup.WATER_CREATURE.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.WATER_CREATURE) ? "§e"+count.get(EntityGroup.WATER_CREATURE) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.WATER_ANIMAL)>0 ? world.getSpawnLimit(SpawnCategory.WATER_ANIMAL) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.WATER_CREATURE)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.WATER_CREATURE.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.WATER_CREATURE) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  




        contents.set(1, 4, ClickableItem.of(new ItemBuilder(Material.COAL)
            .name(EntityGroup.AMBIENT.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.AMBIENT) ? "§e"+count.get(EntityGroup.AMBIENT) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.AMBIENT)>0 ? world.getSpawnLimit(SpawnCategory.AMBIENT) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.AMBIENT)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.AMBIENT.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.AMBIENT) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  


        contents.set(1, 5, ClickableItem.of(new ItemBuilder(Material.TROPICAL_FISH)
            .name(EntityGroup.WATER_AMBIENT.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.WATER_AMBIENT) ? "§e"+count.get(EntityGroup.WATER_AMBIENT) : "не найдено") )
            .addLore("§7")
            .addLore("§7Лимит в настройках мира: §b" + (world.getSpawnLimit(SpawnCategory.WATER_AMBIENT)>0 ? world.getSpawnLimit(SpawnCategory.WATER_AMBIENT) : "--"))
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.WATER_AMBIENT)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.WATER_AMBIENT.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.WATER_AMBIENT) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  


        contents.set(1, 7, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
            .name(EntityGroup.UNDEFINED.displayName)
            .addLore("§7")
            .addLore("§f"+  (count.containsKey(EntityGroup.UNDEFINED) ? "§e"+count.get(EntityGroup.UNDEFINED) : "не найдено") )
            .addLore("§7")
            .addLore("§7ЛКМ - группу подробно")
            .addLore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .addLore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.UNDEFINED)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.UNDEFINED.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (EntityUtil.group(entity)==EntityGroup.UNDEFINED) {
                            if (entity.getType()!=EntityType.PLAYER) {
                                entity.remove();
                            }
                        }
                    }
                    reopen(p, contents);
                }
            }));  



        
            
            
        
        

        
        
        
        
        

    }
    
    
    
    
    
    
    
    
    
    
}
