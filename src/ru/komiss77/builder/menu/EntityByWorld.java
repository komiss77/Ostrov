package ru.komiss77.builder.menu;


import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.IEntityGroup.EntityGroup;
import ru.komiss77.version.VM;




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
                group=VM.getNmsEntitygroup().getEntytyType(e);
                if (count.containsKey(group)) {
                    count.put(group, count.get(group)+1);
                } else {
                    count.put(group, 1);
                }
            } 

        } else {

            for (final Entity e : world.getEntities()) {
                if (e.getType()==EntityType.PLAYER) continue;
                group=VM.getNmsEntitygroup().getEntytyType(e);
                if (count.containsKey(group)) {
                    count.put(group, count.get(group)+1);
                } else {
                    count.put(group, 1);
                }
            } 

        }
            
            


        contents.set(0, 2, ClickableItem.of( new ItemBuilder(Material.SUNFLOWER)
            .name("§eПКМ - показать все миры")
            .lore("")
            .lore(ApiOstrov.isLocalBuilder(p, false) ? "§eПКМ - показать все миры" : "§eВключите режим билдера!")
            .lore("")
            .build(), e -> {
                if (e.isLeftClick()) {
                    if (ApiOstrov.isLocalBuilder(p, true)) {
                        p.performCommand("entity --server");
                    }
                }
            }));  
        
        
        contents.set(0, 4, new InputButton(new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
            .name("§7Сущности в мире §a"+world.getName()+ (radius>0 ? " §7в радиусе §a"+radius : "") )
            .lore("§7")
            .lore("§fЛКМ - §bуказать радиус")
            .lore("§7(0 - весь мир)")
            //.lore(ApiOstrov.isLocalBuilder(p, false) ? "§eПКМ - показать все миры" : "")
            .lore("§7")
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


        
        contents.set(0, 6, ClickableItem.of( new ItemBuilder(Material.REDSTONE)
            .name("§cУдалить всех найденных")
            .lore("")
            .lore(ApiOstrov.isLocalBuilder(p, false) ? "§fШифт+ЛКМ - §судалить" : "§eВключите режим билдера!")
            .lore("")
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
        
        

        
        
        
        
            
            
            
            




        contents.set(1, 1, ClickableItem.of( new ItemBuilder(Material.ZOMBIE_HEAD)
            .name(EntityGroup.MONSTER.displayName)
            .lore("§7")
            .lore("§f"+  (count.containsKey(EntityGroup.MONSTER) ? "§e"+count.get(EntityGroup.MONSTER) : "не найдено") )
            .lore("§7")
            .lore("§7Лимит в настройках мира: §b" + (world.getMonsterSpawnLimit()>0 ? world.getMonsterSpawnLimit() : "--"))
            .lore("§7")
            .lore("§7ЛКМ - группу подробно")
            .lore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .lore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.MONSTER)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.MONSTER.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (VM.getNmsEntitygroup().getEntytyGroup(entity.getType())==EntityGroup.MONSTER) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  




        contents.set(1, 2, ClickableItem.of( new ItemBuilder(Material.LEATHER_HORSE_ARMOR)
            .name(EntityGroup.CREATURE.displayName)
            .lore("§7")
            .lore("§f"+  (count.containsKey(EntityGroup.CREATURE) ? "§e"+count.get(EntityGroup.CREATURE) : "не найдено") )
            .lore("§7")
            .lore("§7Лимит в настройках мира: §b" + (world.getAnimalSpawnLimit()>0 ? world.getAnimalSpawnLimit() : "--"))
            .lore("§7")
            .lore("§7ЛКМ - группу подробно")
            .lore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .lore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.CREATURE)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.CREATURE.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (VM.getNmsEntitygroup().getEntytyGroup(entity.getType())==EntityGroup.CREATURE) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  


        contents.set(1, 3, ClickableItem.of( new ItemBuilder(Material.NAUTILUS_SHELL)
            .name(EntityGroup.WATER_CREATURE.displayName)
            .lore("§7")
            .lore("§f"+  (count.containsKey(EntityGroup.WATER_CREATURE) ? "§e"+count.get(EntityGroup.WATER_CREATURE) : "не найдено") )
            .lore("§7")
            .lore("§7Лимит в настройках мира: §b" + (world.getWaterAnimalSpawnLimit()>0 ? world.getWaterAnimalSpawnLimit() : "--"))
            .lore("§7")
            .lore("§7ЛКМ - группу подробно")
            .lore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .lore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.WATER_CREATURE)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.WATER_CREATURE.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (VM.getNmsEntitygroup().getEntytyGroup(entity.getType())==EntityGroup.WATER_CREATURE) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  




        contents.set(1, 4, ClickableItem.of( new ItemBuilder(Material.COAL)
            .name(EntityGroup.AMBIENT.displayName)
            .lore("§7")
            .lore("§f"+  (count.containsKey(EntityGroup.AMBIENT) ? "§e"+count.get(EntityGroup.AMBIENT) : "не найдено") )
            .lore("§7")
            .lore("§7Лимит в настройках мира: §b" + (world.getAmbientSpawnLimit()>0 ? world.getAmbientSpawnLimit() : "--"))
            .lore("§7")
            .lore("§7ЛКМ - группу подробно")
            .lore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .lore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.AMBIENT)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.AMBIENT.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (VM.getNmsEntitygroup().getEntytyGroup(entity.getType())==EntityGroup.AMBIENT) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  


        contents.set(1, 5, ClickableItem.of( new ItemBuilder(Material.TROPICAL_FISH)
            .name(EntityGroup.WATER_AMBIENT.displayName)
            .lore("§7")
            .lore("§f"+  (count.containsKey(EntityGroup.WATER_AMBIENT) ? "§e"+count.get(EntityGroup.WATER_AMBIENT) : "не найдено") )
            .lore("§7")
            .lore("§7Лимит в настройках мира: §b" + (world.getAmbientSpawnLimit()>0 ? world.getAmbientSpawnLimit() : "--"))
            .lore("§7")
            .lore("§7ЛКМ - группу подробно")
            .lore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .lore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.WATER_AMBIENT)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.WATER_AMBIENT.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (VM.getNmsEntitygroup().getEntytyGroup(entity.getType())==EntityGroup.WATER_AMBIENT) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  


        contents.set(1, 7, ClickableItem.of( new ItemBuilder(Material.ARMOR_STAND)
            .name(EntityGroup.UNDEFINED.displayName)
            .lore("§7")
            .lore("§f"+  (count.containsKey(EntityGroup.UNDEFINED) ? "§e"+count.get(EntityGroup.UNDEFINED) : "не найдено") )
            .lore("§7")
            .lore("§7ЛКМ - группу подробно")
            .lore(ApiOstrov.isLocalBuilder(p, false) ? "§7Шифт+ПКМ - удалить всех мобов этой группы" : "")
            .lore("§7")
            .build(), e -> {
                if (e.isLeftClick()) {
                    SmartInventory.builder().id("EntityByGroup"+p.getName()). provider(new EntityByGroup(world, radius, EntityGroup.UNDEFINED)). size(6, 9). title("§2"+world.getName()+" "+EntityGroup.UNDEFINED.displayName+" §1r="+radius).build() .open(p);
                } else if (e.getClick()==ClickType.SHIFT_RIGHT && ApiOstrov.isLocalBuilder(p, false)) {
                    for (final Entity entity : world.getEntities()) {
                        if (VM.getNmsEntitygroup().getEntytyGroup(entity.getType())==EntityGroup.UNDEFINED) {
                            entity.remove();
                        }
                    }
                    reopen(p, contents);
                }
            }));  



        
            
            
        
        

        
        
        
        
        

    }
    
    
    
    
    
    
    
    
    
    
}
