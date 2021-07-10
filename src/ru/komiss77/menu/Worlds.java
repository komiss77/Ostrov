package ru.komiss77.menu;


import ru.komiss77.builder.menu.WorldSettings;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.wordBorder.WorldFillTask;
import ru.komiss77.modules.wordBorder.WorldTrimTask;
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
                int maxSize;
                
                for (final World world : Bukkit.getWorlds()) {
                    
                    maxSize = ((int) world.getWorldBorder().getSize() < Bukkit.getServer().getMaxWorldSize() ? ((int) world.getWorldBorder().getSize()) : Bukkit.getServer().getMaxWorldSize());
                    
                    menuEntry.add(ClickableItem.of(new ItemBuilder(getWorldMat(world))
                        .name(world.getName())
                        .lore("")
                        .lore("§7Игроки: "+world.getPlayers().size())
                        .lore("§7провайдер: §e"+world.getEnvironment().toString())
                        .lore("§7генератор: §e"+( world.getGenerator()==null ? "null" :    (world.getGenerator().getClass().getName().contains(".") ? world.getGenerator().getClass().getName().substring(world.getGenerator().getClass().getName().lastIndexOf(".")+1) : world.getGenerator().getClass().getName() )   )  )
                        .lore("§7чанков загружено: §e"+world.getLoadedChunks().length)
                        .lore("§7мобов загружено: §e"+world.getLivingEntities().size())
                        .lore("")
                        .lore("§7ЛКМ - ТП на точку спавна мира")
                        .lore("§7ПКМ - настройки мира")
                        .lore("§4клав.Q - §cвыгрузить мир")
                        .lore("§5===============================")
                        .lore("Центр границы мира: "+world.getWorldBorder().getCenter().getBlockX()+", "+world.getWorldBorder().getCenter().getBlockY()+", "+world.getWorldBorder().getCenter().getBlockZ())
                        .lore("Размер границы мира: §6"+world.getWorldBorder().getSize() )
                        .lore("§7*(установка границы в меню настроек)")
                        .lore("Макс.размер в server.properties: §6"+Bukkit.getServer().getMaxWorldSize())
                        .lore("Эффективный размер: §e"+maxSize)
                        .lore("(x от "+(world.getWorldBorder().getCenter().getBlockX()-maxSize/2)+" до "+(world.getWorldBorder().getCenter().getBlockX()+maxSize/2)+")")
                        .lore("(z от "+(world.getWorldBorder().getCenter().getBlockZ()-maxSize/2)+" до "+(world.getWorldBorder().getCenter().getBlockZ()+maxSize/2)+")")
                        .lore("")
                        .lore(WorldManager.fillTask!=null && WorldManager.fillTask.valid() ? (WorldManager.fillTask.isPaused()?"§6Предгенерация на паузе" : "§aИдёт предгенерация : §b"+WorldManager.fillTask.getPercentageCompleted()+"%") : (WorldManager.trimTask!=null && WorldManager.trimTask.valid()? "§cИдёт обрезка мира" : ""))
                        .lore(WorldManager.fillTask==null ? "§7Шифт+ЛКМ - начать предгенерацию" : (WorldManager.fillTask.isPaused() ? "§7Шифт+ЛКМ - продолжить предгенерацию" : "§7Шифт+ЛКМ - пауза предгенерации"))
                        .lore(WorldManager.fillTask==null ? "§7Шифт+ПКМ - обрезать мир по границе" : "§7Шифт+ПКМ - прекратить предгенерацию" )
                        .lore("§5===============================")
                        .build(), e-> {
                            switch (e.getClick()) {
                                
                                case LEFT:
                                    ApiOstrov.teleportSave(p, world.getSpawnLocation(), true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                    break;
                                
                                case DROP:
                                    //if (world != null) {
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
                                        WorldManager.fillTask = new WorldFillTask(p.getWorld().getName());
                                        if (WorldManager.fillTask.valid()) {
                                            int fillFrequency = 20;
                                            int ticks = 1, repeats = 1;
                                            if (fillFrequency > 20) {
                                                repeats = fillFrequency / 20;
                                            } else {
                                                ticks = 20 / fillFrequency;
                                            }
                                            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Ostrov.instance, WorldManager.fillTask, ticks, ticks);
                                            WorldManager.fillTask.setTaskID(task);
                                            p.sendMessage("§7Начата предгенерация чанков для мира §6"+p.getWorld().getName());
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
                                            WorldManager.trimTask = new WorldTrimTask(p.getWorld().getName());
                                            if (WorldManager.trimTask.valid()) {
                                                int trimFrequency = 5000;
                                                int ticks = 1, repeats = 1;
                                                if (trimFrequency > 20) {
                                                    repeats = trimFrequency / 20;
                                                } else {
                                                    ticks = 20 / trimFrequency;
                                                }
                                                    int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Ostrov.instance, WorldManager.trimTask, ticks, ticks);
                                                    WorldManager.trimTask.setTaskID(task);
                                                    p.sendMessage("§7Начато удаление чанков за границей мира для §6" + p.getWorld().getName());
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
