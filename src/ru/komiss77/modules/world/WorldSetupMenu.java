package ru.komiss77.modules.world;

import ru.komiss77.builder.menu.WorldSettings;
import java.util.ArrayList;
import java.util.TreeSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.wordBorder.WorldFillTask;
import ru.komiss77.modules.wordBorder.WorldTrimTask;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;




public class WorldSetupMenu implements InventoryProvider {
    
    
    
    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();;
    

    
    public WorldSetupMenu() {
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(0,0,  2,8, ClickableItem.empty(fill));
        
        final Oplayer op = PM.getOplayer(p);
        
        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        
        
        

            //p.teleport( Bukkit.getWorld(itemname).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        
        
            if (ApiOstrov.isLocalBuilder(p, false)) {
                int maxSize;
                
                final TreeSet<String> worldNames = new TreeSet<String>();
                
                for (final World world : Bukkit.getWorlds()) {
                    worldNames.add(world.getName());
                }
                
                for (final String worldName : worldNames) {
                    final World world = Bukkit.getWorld(worldName);
                    
                    maxSize = ((int) world.getWorldBorder().getSize() < Bukkit.getServer().getMaxWorldSize() ? ((int) world.getWorldBorder().getSize()) : Bukkit.getServer().getMaxWorldSize());
                    
                    menuEntry.add(ClickableItem.of(new ItemBuilder(getWorldMat(world))
                        .name(world.getName())
                        .addLore("")
                        .addLore("§7Игроки: "+world.getPlayers().size())
                        .addLore("§7провайдер: §e"+world.getEnvironment().toString())
                        .addLore("§7генератор: §e"+( world.getGenerator()==null ? "null" :    (world.getGenerator().getClass().getName().contains(".") ? world.getGenerator().getClass().getName().substring(world.getGenerator().getClass().getName().lastIndexOf(".")+1) : world.getGenerator().getClass().getName() )   )  )
                        .addLore("§7чанков загружено: §e"+world.getLoadedChunks().length)
                        .addLore("§7мобов загружено: §e"+world.getLivingEntities().size())
                        .addLore("")
                        .addLore("§7ЛКМ - ТП на точку спавна мира")
                        .addLore("§7ПКМ - настройки мира")
                        .addLore("§4клав.Q - §cвыгрузить мир")
                        .addLore("§5===============================")
                        .addLore("Центр границы мира: "+world.getWorldBorder().getCenter().getBlockX()+", "+world.getWorldBorder().getCenter().getBlockY()+", "+world.getWorldBorder().getCenter().getBlockZ())
                        .addLore("Размер границы мира: §6"+world.getWorldBorder().getSize() )
                        .addLore("§7*(установка границы в меню настроек)")
                        .addLore("Макс.размер в server.properties: §6"+Bukkit.getServer().getMaxWorldSize())
                        .addLore("Эффективный размер: §e"+maxSize)
                        .addLore("(x от "+(world.getWorldBorder().getCenter().getBlockX()-maxSize/2)+" до "+(world.getWorldBorder().getCenter().getBlockX()+maxSize/2)+")")
                        .addLore("(z от "+(world.getWorldBorder().getCenter().getBlockZ()-maxSize/2)+" до "+(world.getWorldBorder().getCenter().getBlockZ()+maxSize/2)+")")
                        .addLore("")
                            
                        .addLore(WorldManager.fillTask!=null && WorldManager.fillTask.valid() ? 
                                (WorldManager.fillTask.isPaused()?"§6Предгенерация на паузе" : "§aИдёт предгенерация §e"+WorldManager.fillTask.worldName()+" §7: §b"+WorldManager.fillTask.getPercentageCompleted()+"%") 
                                : (WorldManager.trimTask!=null && WorldManager.trimTask.valid()? "§cИдёт обрезка мира §e"+WorldManager.trimTask.worldName() : ""))
                        
                        .addLore(WorldManager.fillTask==null ? "§7Шифт+ЛКМ - начать предгенерацию" : (WorldManager.fillTask.isPaused() ? 
                                "§7Шифт+ЛКМ - продолжить предгенерацию §e"+WorldManager.fillTask.worldName() 
                                : "§7Шифт+ЛКМ - пауза предгенерации §e"+WorldManager.fillTask.worldName()))

                        .addLore(WorldManager.fillTask==null ?
                                "§7Шифт+ПКМ - обрезать мир по границе" 
                                : "§7Шифт+ПКМ - прекратить предгенерацию §e"+WorldManager.fillTask.worldName() )

                        .addLore("§5===============================")
                        .build(), e-> {
                            switch (e.getClick()) {
                                
                                case LEFT:
                                    ApiOstrov.teleportSave(p, world.getSpawnLocation(), true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                    break;
                                
                                case DROP:
                                    //if (world != null) {
                                    if (!world.getPlayers().isEmpty()) {
                                        p.sendMessage(Ostrov.PREFIX+"Все игроки должны покинуть мир перед удалением!");
                                        world.getPlayers().stream().forEach((p1) -> {
                                            p.sendMessage(Ostrov.PREFIX+"- " + p1.getName());
                                        });
                                        PM.soundDeny(p);
                                        return;
                                    } else {
                                        Bukkit.unloadWorld(world, true);
                                        p.sendMessage(Ostrov.PREFIX+" мир "+world.getName()+" выгружен!");
                                        reopen(p, contents);
                                    }
                                   // } else {
                                   //     p.sendMessage(Ostrov.prefix+"Загруженный мир с таким названием не найден!");
                                  //  }
                                    break;
                                    
                                case RIGHT:
                                    SmartInventory.builder()
                                        .id("WorldSettings"+p.getName())
                                        .provider(new WorldSettings(world))
                                        .size(6, 9)
                                        .title("§bНастройки мира "+world.getName())
                                        .build()
                                        .open(p);
                                    break;
                                    
                                    
                                case SHIFT_LEFT:
                                    if (WorldManager.trimTask!=null && WorldManager.trimTask.valid()) {
                                        p.sendMessage("§cИдёт обрезка мира, подождите..");
                                    } else if (WorldManager.fillTask!=null && WorldManager.fillTask.valid()) {
                                        if (WorldManager.fillTask.isPaused()) {
                                            WorldManager.fillTask.pause();
                                            p.sendMessage("§eпредгенерация продолжена");
                                        } else {
                                            WorldManager.fillTask.pause();
                                            p.sendMessage("§eпредгенерация приостановлена");
                                        }
                                    } else {
                                        WorldManager.fillTask = new WorldFillTask(world.getName());
                                        if (WorldManager.fillTask.valid()) {
                                            int fillFrequency = 20;
                                            int ticks = 1;
                                            if (fillFrequency <= 20) {
                                                ticks = 20 / fillFrequency;
                                            }
                                            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Ostrov.instance, WorldManager.fillTask, ticks, ticks);
                                            WorldManager.fillTask.setTaskID(task);
                                            p.sendMessage("§7Начата предгенерация чанков для мира §6"+world.getName());
                                        } else {
                                            p.sendMessage("§cОшибка начала предгенерации.");
                                        }
                                    }
                                    reopen(p, contents);
                                    break;
                                    
                                case SHIFT_RIGHT:
                                    if (WorldManager.fillTask==null) {
                                        if (WorldManager.trimTask!=null && WorldManager.trimTask.valid()) {
                                            p.sendMessage("§eОбрезка лишних чанков уже запущена!");
                                        } else {
                                            WorldManager.trimTask = new WorldTrimTask(world.getName());
                                            if (WorldManager.trimTask.valid()) {
                                                int trimFrequency = 5000;
                                                int ticks = 1;
                                                if (trimFrequency <= 20) {
                                                    ticks = 20 / trimFrequency;
                                                }
                                                    int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Ostrov.instance, WorldManager.trimTask, ticks, ticks);
                                                    WorldManager.trimTask.setTaskID(task);
                                                    p.sendMessage("§7Начато удаление чанков за границей мира для §6" + world.getName());
                                            } else {
                                                p.sendMessage("§cудаление чанков за границей мира не начато");
                                            }
                                        }
                                    } else {
                                        WorldManager.fillTask.cancel();
                                        p.sendMessage("§eпредгенерация отменена.");
                                    }
                                    reopen(p, contents);
                                    break;
								default:
									break;
                                    
                            }
                        }));

                }
                
                pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
                pagination.setItemsPerPage(45);
        
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
                
            } else {
                
                contents.fillRect(0,0,  2,8, ClickableItem.empty(fill));
                
                for (final World world : Bukkit.getWorlds()) {
                    
                    menuEntry.add(ClickableItem.of(new ItemBuilder(getWorldMat(world))
                        .name(world.getName())
                        .addLore(op.world_positions.containsKey(world.getName())? "§7ЛКМ - ТП на точку выхода" : "")
                        .addLore("§7ПКМ - ТП на точку спавна мира")
                        .addLore("")
                        .build(), e-> {
                            if (e.isLeftClick() && op.world_positions.containsKey(world.getName())) {
                                final Location exit = LocationUtil.LocFromString(op.world_positions.get(world.getName()));
//Ostrov.log("----- "+op.world_positions.get(world.getName()));
                                ApiOstrov.teleportSave(p, exit, true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            } else {
                                ApiOstrov.teleportSave(p, world.getSpawnLocation(), true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            }
                        }));
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
            }
                




                
                
            
            
        
        
        
        
        
        
        
        //pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        //pagination.setItemsPerPage(9);
        











        

        
        
        
        
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));
        

        
        

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
