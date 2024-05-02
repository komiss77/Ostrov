package ru.komiss77.builder.menu;

import org.bukkit.*;
import java.util.ArrayList;
import java.util.TreeSet;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.wordBorder.WorldFillTask;
import ru.komiss77.modules.wordBorder.WorldTrimTask;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;


public class WorldSetupMenu implements InventoryProvider {

    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("§8.").build();
    

    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

        final Oplayer op = PM.getOplayer(p);
        
        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        

            int maxSize;

            final TreeSet<String> worldNames = new TreeSet<String>();

            for (final World world : Bukkit.getWorlds()) {
                worldNames.add(world.getName());
            }

            for (final String worldName : worldNames) {
                final World world = Bukkit.getWorld(worldName);

                maxSize = ((int) world.getWorldBorder().getSize() < Bukkit.getServer().getMaxWorldSize() ? ((int) world.getWorldBorder().getSize()) : Bukkit.getServer().getMaxWorldSize());
                final String gen =  world.getGenerator()==null ? "none" :    (world.getGenerator().getClass().getName().contains(".") ? world.getGenerator().getClass().getName().substring(world.getGenerator().getClass().getName().lastIndexOf(".")+1) : world.getGenerator().getClass().getName() );


                menuEntry.add(ClickableItem.of(new ItemBuilder(getWorldMat(world))
                    .name(world.getName())
                    .addLore("§b"+world.getEnvironment().name()+"§7, generator: §b"+gen)
                    .addLore("§fChunks §7Loaded:§d"+world.getLoadedChunks().length+" §7Ticking:§d"+world.getChunkCount())
                    .addLore("§fEntity§7:§6"+world.getEntityCount()+"§7, Living:§6"+world.getLivingEntities().size()+"§7, Players:§6"+world.getPlayers().size())
                    .addLore("§fTileEntity§7:§3"+world.getTileEntityCount()+"§7, Tickable:§3"+world.getTickableTileEntityCount())
                    .addLore("")
                    .addLore("§fЛКМ §7- ТП на точку спавна мира")
                    .addLore("§fПКМ §7- энтити")
                    .addLore("§fКолёсико §7- настройки мира")
                    .addLore("§4клав.Q - §cвыгрузить мир")
                    .addLore("§5===============================")
                    .addLore("Центр границы мира: "+world.getWorldBorder().getCenter().getBlockX()+", "+world.getWorldBorder().getCenter().getBlockY()+", "+world.getWorldBorder().getCenter().getBlockZ())
                    .addLore("Размер границы мира: §6"+world.getWorldBorder().getSize() )
                    .addLore("§7*(установка границы в меню настроек)")
                    .addLore("Макс.размер в server.properties: §6"+Bukkit.getServer().getMaxWorldSize())
                    .addLore("Эффективный размер: §e"+maxSize)
                    .addLore("(x от "+(world.getWorldBorder().getCenter().getBlockX()-maxSize/2)+" до "+(world.getWorldBorder().getCenter().getBlockX()+maxSize/2)+")")
                    .addLore("(z от "+(world.getWorldBorder().getCenter().getBlockZ()-maxSize/2)+" до "+(world.getWorldBorder().getCenter().getBlockZ()+maxSize/2)+")")

                    .addLore(WorldManager.fillTask!=null && WorldManager.fillTask.valid() ?
                            (WorldManager.fillTask.isPaused()?"§6Предгенерация на паузе" : "§aИдёт предгенерация §e"+WorldManager.fillTask.worldName()+" §7: §b"+WorldManager.fillTask.getPercentageCompleted()+"%")
                            : (WorldManager.trimTask!=null && WorldManager.trimTask.valid()? "§cИдёт обрезка мира §e"+WorldManager.trimTask.worldName() : ""))

                    .addLore(WorldManager.fillTask==null ? "§fШифт+ЛКМ §7- начать предгенерацию" : (WorldManager.fillTask.isPaused() ?
                            "§fШифт+ЛКМ §7- продолжить предгенерацию §e"+WorldManager.fillTask.worldName()
                            : "§fШифт+ЛКМ §7- пауза предгенерации §e"+WorldManager.fillTask.worldName()))

                    .addLore(WorldManager.fillTask==null ?
                            "§fШифт+ПКМ §7- обрезать мир по границе"
                            : "§fШифт+ПКМ §7- прекратить предгенерацию §e"+WorldManager.fillTask.worldName() )

                    .addLore("§5===============================")
                    .build(), e-> {

                        switch (e.getClick()) {

                            case LEFT:
                                ApiOstrov.teleportSave(p, world.getSpawnLocation(), true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                break;

                            case DROP:
                                if (!world.getPlayers().isEmpty()) {
                                    p.sendMessage(Ostrov.PREFIX+"Все игроки должны покинуть мир перед выгрузкой!");
                                    world.getPlayers().stream().forEach((p1) -> {
                                        p.sendMessage(Ostrov.PREFIX+"- " + p1.getName());
                                    });
                                    PM.soundDeny(p);
                                    reopen(p, contents);
                                } else {
                                    Bukkit.unloadWorld(world, true);
                                    p.sendMessage(Ostrov.PREFIX+" мир "+world.getName()+" выгружен!");
                                    reopen(p, contents);
                                }
                                break;

                          case RIGHT:
                            p.performCommand("entity");
                            break;

                          case MIDDLE:
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

            pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
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


        
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));


    }

    private Material getWorldMat(final World w) {
        return switch (w.getEnvironment()) {
            case NORMAL -> Material.SHORT_GRASS;
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> Material.WHITE_GLAZED_TERRACOTTA;
        };
    }
    
    
    
    
    
    
    
    
    
    
}
