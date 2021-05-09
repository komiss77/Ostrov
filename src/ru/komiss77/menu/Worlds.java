package ru.komiss77.menu;


import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;





public class Worlds implements InventoryProvider {
    
    
    
    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();;
    

    
    public Worlds() {
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRect(0,0,  2,8, ClickableItem.empty(fill));
        
        
        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        
        
        

            //p.teleport( Bukkit.getWorld(itemname).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        
        
            if (ApiOstrov.isLocalBuilder(p, false)) {
                
                for (final World world : Bukkit.getWorlds()) {
                    
                    menuEntry.add(ClickableItem.of(new ItemBuilder(getWorldMat(world))
                        .name(world.getName())
                        .lore("")
                        .lore("§7Игроки: "+world.getPlayers().size())
                        .lore("§7провайдер: §e"+world.getEnvironment().toString())
                        .lore("§7генератор: §e"+( world.getGenerator()==null ? "null" :    (world.getGenerator().getClass().getName().contains(".") ? world.getGenerator().getClass().getName().substring(world.getGenerator().getClass().getName().lastIndexOf(".")+1) : world.getGenerator().getClass().getName() )   )  )
                        .lore("§7размер мира : §e"+world.getWorldBorder().getSize())
                        .lore("§7чанков загружено: §e"+world.getLoadedChunks().length)
                        .lore("§7мобов загружено: §e"+world.getLivingEntities().size())
                        .lore("")
                        .lore("")
                        .lore("")
                        .lore("")
                        .lore("")
                        .lore("§7ЛКМ - ТП на точку спавна мира")
                        .lore("§7ПКМ - настройки мира")
                        .lore("§4клав.Q - §cвыгрузить мир")
                        .lore("")
                        .build(), e-> {
                            switch (e.getClick()) {
                                
                                case LEFT:
                                    ApiOstrov.teleportSave(p, world.getSpawnLocation(), true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                    break;
                                
                                case DROP:
                                    if (world != null) {
                                        if (!world.getPlayers().isEmpty()) {
                                            p.sendMessage(Ostrov.prefix+"Все игроки должны покинуть мир перед удалением!");
                                            world.getPlayers().stream().forEach((p1) -> {
                                                p.sendMessage(Ostrov.prefix+"- " + p1.getName());
                                            });
                                            Ostrov.soundDeny(p);
                                            return;
                                        } else {
                                            Bukkit.unloadWorld(world, true);
                                            p.sendMessage(Ostrov.prefix+" мир "+world.getName()+" выгружен!");
                                            reopen(p, contents);
                                        }
                                    } else {
                                        p.sendMessage(Ostrov.prefix+"Загруженный мир с таким названием не найден!");
                                    }
                                    break;
                                    
                                case RIGHT:
                                    SmartInventory.builder()
                                    .id("WorldSettings"+p.getName())
                                    .provider(new WorldSettings(world))
                                    .size(6, 9)
                                    .title("§bНастройки мира "+world.getName())
                                    .build().open(p);
                                    break;
                            }
                        }));

                }
                
            } else {
                
                for (final World world : Bukkit.getWorlds()) {
                    
                    menuEntry.add(ClickableItem.of(new ItemBuilder(getWorldMat(world))
                        .name(world.getName())
                        .lore("")
                        .lore("§7ЛКМ - ТП на точку спавна мира")
                        .lore("")
                        .build(), e-> {
                            if (e.isLeftClick()) {
                                ApiOstrov.teleportSave(p, world.getSpawnLocation(), true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            }
                        }));
                }
                
            }
                




                
                
            
            
        
        
        
        
        
        
        
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(9);
        











        

        
        
        contents.set( 2, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> 
            p.closeInventory()
        ));
        

        
        if (!pagination.isLast()) {
            contents.set(2, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> contents.getHost().open(p, pagination.next().getPage()) )
            );
        }

        if (!pagination.isFirst()) {
            contents.set(2, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> contents.getHost().open(p, pagination.previous().getPage()) )
            );
        }
        
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0)).allowOverride(false));
        

        
        

    }

    private Material getWorldMat(final World w) {
        switch (w.getEnvironment()) {
            case NORMAL:
                return Material.GRASS;
            case NETHER:
                return Material.NETHERRACK;
            case THE_END:
                return Material.END_STONE;
            default:
                return Material.WHITE_GLAZED_TERRACOTTA;
        }
    }
    
    
    
    
    
    
    
    
    
    
}
