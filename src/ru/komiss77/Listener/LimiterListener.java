
package ru.komiss77.Listener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Managers.Timer;
import ru.komiss77.Objects.CaseInsensitiveMap;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.version.IEntityGroup.EntityGroup;
import ru.komiss77.version.VM;




public final class LimiterListener extends Initiable implements Listener {
    
    
    private static LimiterListener limiter;
    private static List<String> ml_reasons;
    private static Map<EntityGroup,Integer> limit_groups;
    private static Map<String,Integer> limit_type;
    
    private final CaseInsensitiveMap<VehicleInfo> vehicleInfo;
    private static OstrovConfig spawn_limiter;
    
    private static boolean ml_check_chunk_onload,
            ml_check_chunk_unload,
            ml_watch_creature_spawns,
            ml_block_all_spawn,
            ml_notify_players,
            block_dispenser_egg,
            oneMinecartPerPlayer,
            oneBoatPerPlayer
            =false;

    
    
    public LimiterListener(final Ostrov plugin) {
        limiter = this;
        ml_reasons = new ArrayList<>();
        limit_groups = new HashMap<>();
        limit_type = new HashMap<>();
        vehicleInfo = new CaseInsensitiveMap<>();
        reload();
    }


 

    @Override
    public void reload() {
        loadConfig();
        
        try {
            
            if (limiter!=null) {
                HandlerList.unregisterAll(limiter);
            }
            if (!spawn_limiter.getBoolean("mob_limiter.enable")) {
                Ostrov.log_ok ("§eМоб-лмитер выключен.");
                return;
            }
            
            spawn_limiter.getConfigurationSection("mob_limiter.limits.blockstates").getKeys(false).stream().forEach((s) -> {
//System.out.println("+++++++++++++++++++Load() s="+s+"  limit="+Conf.spawn_limiter.getInt("mob_limiter.limits.blockstates."+s));
                LimitType.setLimit (s, spawn_limiter.getInt("mob_limiter.limits.blockstates."+s));
            });
            
            

            ml_check_chunk_onload=spawn_limiter.getBoolean("mob_limiter.check_chunk_onload");
            ml_check_chunk_unload=spawn_limiter.getBoolean("mob_limiter.check_chunk_unload");
            ml_watch_creature_spawns=spawn_limiter.getBoolean("mob_limiter.watch_creature_spawns");
            ml_block_all_spawn=spawn_limiter.getBoolean("mob_limiter.block_all_spawn");
            ml_notify_players=spawn_limiter.getBoolean("mob_limiter.notify_players");
            block_dispenser_egg=spawn_limiter.getBoolean("mob_limiter.block_dispenser_egg",false);
            
            oneMinecartPerPlayer=spawn_limiter.getBoolean("mob_limiter.oneMinecartPerPlayer",false);
            oneBoatPerPlayer=spawn_limiter.getBoolean("mob_limiter.oneBoatPerPlayer",false);

            ml_reasons.clear();
            if (spawn_limiter.getConfigurationSection("mob_limiter.spawn_reasons")!=null) {
                spawn_limiter.getConfigurationSection("mob_limiter.spawn_reasons").getKeys(false).stream().forEach((s) -> {
                    if ( spawn_limiter.getBoolean("mob_limiter.spawn_reasons."+s) ) ml_reasons.add (s);
                });
            }

            limit_groups.clear();
            if (spawn_limiter.getConfigurationSection("mob_limiter.limits.groups")!=null) {
                spawn_limiter.getConfigurationSection("mob_limiter.limits.groups").getKeys(false).stream().forEach((s) -> {
                    if ( spawn_limiter.getInt("mob_limiter.limits.groups."+s) >= 0) {
                        //if (MobMeta.isMeta(s)) {
                            limit_groups.put ( VM.getNmsEntitygroup().byTag(s), spawn_limiter.getInt("mob_limiter.limits.groups."+s) );
                       //}
                    }
                });
            }

            limit_type.clear();
            if (spawn_limiter.getConfigurationSection("mob_limiter.limits.type")!=null) {
                spawn_limiter.getConfigurationSection("mob_limiter.limits.type").getKeys(false).stream().forEach((s) -> {
                    if ( spawn_limiter.getInt("mob_limiter.limits.type."+s) >= 0) limit_type.put ( s,spawn_limiter.getInt("mob_limiter.limits.type."+s) );
                });
            }
            
            Bukkit.getPluginManager().registerEvents(limiter, Ostrov.GetInstance());

            Ostrov.log_ok ("§2Моб-лмитер активен!");
            // System.err.println(">>>> "+ml_reasons);
            // System.err.println(">>>> "+limit_groups);
            //System.err.println(">>>> "+limit_type);
        } catch (Exception ex) {
            Ostrov.log_err("Не удалось инициализировать LimiterListener: "+ex.getMessage());
        }
    }      
    
    
    
  

    
    
    public void loadConfig () {
        spawn_limiter = Cfg.manager.getNewConfig("spawn_limiter.yml", new String[]{"", "Ostrov77 spawn_limiter file", ""} );

        spawn_limiter.addDefault("mob_limiter.enable", false);
        spawn_limiter.addDefault("mob_limiter.check_chunk_onload", false);
        spawn_limiter.addDefault("mob_limiter.check_chunk_unload", false);
        spawn_limiter.addDefault("mob_limiter.block_all_spawn", false);
        spawn_limiter.addDefault("mob_limiter.watch_creature_spawns", false);
        spawn_limiter.addDefault("mob_limiter.notify_players", false);
        spawn_limiter.addDefault("mob_limiter.block_dispenser_egg", false);

        spawn_limiter.addDefault("mob_limiter.oneMinecartPerPlayer", false);
        spawn_limiter.addDefault("mob_limiter.oneBoatPerPlayer", false);

        spawn_limiter.addDefault("mob_limiter.limits.groups.CREATURE", -1);
        spawn_limiter.addDefault("mob_limiter.limits.groups.MONSTER", -1);
        spawn_limiter.addDefault("mob_limiter.limits.groups.WATER_CREATURE", -1);
        spawn_limiter.addDefault("mob_limiter.limits.groups.AMBIENT", -1);
        //spawn_limiter.addDefault("mob_limiter.limits.groups.NPC", 10);
        spawn_limiter.addDefault("mob_limiter.limits.groups.UNDEFINED", -1);

        for (LimitType lt: LimitType.values()) {
            if (lt!=LimitType.НЕТ) spawn_limiter.addDefault("mob_limiter.limits.blockstates."+lt.toString(), lt.limit);
        }
        
        
        for (SpawnReason s: SpawnReason.values()) {
            spawn_limiter.addDefault("mob_limiter.spawn_reasons."+s, true);
        }

        for (EntityType t: EntityType.values()) {
            spawn_limiter.addDefault("mob_limiter.limits.type."+t, -1);
        }
        spawn_limiter.saveConfig();

    }    
    
    
    
    
    
 //   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
//    public void onEntitySpawnEvent(EntitySpawnEvent e) {
//System.out.println("-- Limiter onEntitySpawnEvent() "+e.getEntityType()+" ");
      //  e.getVehicle()
       //     if (  e.getCurrentItem().getType().toString().endsWith("_EGG")) {
       //         e.setCancelled(true);
      //          e.getWhoClicked().sendMessage("§cНа данном сервере запрещён спавн мобов через раздатчик!");
      //      }
//    }

    
//    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
//    public void onItemSpawnEvent(ItemSpawnEvent e) { //расширение для EntitySpawnEvent
//System.out.println("-- Limiter onItemSpawnEvent() "+e.getEntityType()+" ");
      //  e.getVehicle()
       //     if (  e.getCurrentItem().getType().toString().endsWith("_EGG")) {
       //         e.setCancelled(true);
      //          e.getWhoClicked().sendMessage("§cНа данном сервере запрещён спавн мобов через раздатчик!");
      //      }
//    }    
    
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisconnect(PlayerQuitEvent e) {
        if (vehicleInfo.containsKey(e.getPlayer().getName())) {
            //final VehicleInfo vi = getVehicleInfo(e.getPlayer());
            //vehicleInfo.remove(e.getPlayer().getName());
        }
    }    
    
    
    
    
    
    
    
     
   // @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   // public void onVehicleCreate(VehicleCreateEvent e) {
        
//System.out.println("-- Limiter onVehicleCreate() "+e.getVehicle()+" loc="+e.getVehicle().getLocation());


        /*if (e.getVehicle().getType().toString().contains("MINECART")) {
            final String coord = e.getVehicle().getLocation().getBlockX()+":"+e.getVehicle().getLocation().getBlockY()+":"+e.getVehicle().getLocation().getBlockZ();
            final VehicleInfo vi = findByMinecartCoord(coord);
            if (vi==null) {
System.out.println("-- Minecart vi==null");
            } else {
System.out.println("-- Minecart owner="+vi.name);
                vi.placeMinecartCoord = "";  //забыть коорд, кто-то может ставить на то же место
            }
            
        } else  if (e.getVehicle().getType().toString().endsWith("_BOAT")) {
            final String coord = e.getVehicle().getLocation().getBlockX()+":"+e.getVehicle().getLocation().getBlockY()+":"+e.getVehicle().getLocation().getBlockZ();
            final VehicleInfo vi = findByBoatCoord(coord);
            if (vi==null) {
System.out.println("-- Minecart vi==null");
            } else {
System.out.println("-- Boat owner="+vi.name);
                vi.placeBoatCoord = ""; //забыть коорд, кто-то может ставить на то же место
            }
            
        }*/
      //  e.getVehicle()
       //     if (  e.getCurrentItem().getType().toString().endsWith("_EGG")) {
       //         e.setCancelled(true);
      //          e.getWhoClicked().sendMessage("§cНа данном сервере запрещён спавн мобов через раздатчик!");
      //      }
   // }
   
    
    
    
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction()==Action.PHYSICAL || e.getItem()==null) return;
        
        if (e.getItem().getType().toString().contains("FIREWORK")) {
            
            if (Timer.CD_has(e.getPlayer().getName(), "firework")) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§eЧуть помедленнее!");
            } else {
                Timer.CD_add(e.getPlayer().getName(), "firework", 1);
            }
            
        }
        
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK
                && ( (e.getItem().getType().toString().contains("MINECART") && oneMinecartPerPlayer) || (e.getItem().getType().toString().endsWith("_BOAT") && oneBoatPerPlayer) )  ){
            
            //if ( e.getItem().getType().toString().contains("MINECART") && !oneMinecartPerPlayer) return;
            //if ( e.getItem().getType().toString().endsWith("_BOAT") && !oneBoatPerPlayer) return;
            
            //if ( !e.getItem().getType().toString().contains("MINECART") && !e.getItem().getType().toString().endsWith("_BOAT") ) return;
            final Player p = e.getPlayer();
            
            if (ApiOstrov.isLocalBuilder(p, false)) return;
            
            if (Timer.CD_has(p.getName(), "vehicle")) {
                ApiOstrov.sendActionBarDirect(p, "§eПодождите "+Timer.CD_left(p.getName(), "vehicle" )+" сек.!");
            }
            Timer.CD_add(p.getName(), "vehicle", 3);
            
            Entity entity=null;
            
            switch (e.getItem().getType()) {
                case MINECART:
                    e.setCancelled(true);
                    if (e.getClickedBlock().getType().toString().contains("RAIL")) entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), Minecart.class);
                    break;
                case CHEST_MINECART:
                    e.setCancelled(true);
                    if (e.getClickedBlock().getType().toString().contains("RAIL")) entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), StorageMinecart.class);
                    break;
                case FURNACE_MINECART:
                    e.setCancelled(true);
                    if (e.getClickedBlock().getType().toString().contains("RAIL")) entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), PoweredMinecart.class);
                    break;
                case TNT_MINECART:
                    e.setCancelled(true);
                    if (e.getClickedBlock().getType().toString().contains("RAIL")) entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), ExplosiveMinecart.class);
                    break;
                case HOPPER_MINECART:
                    e.setCancelled(true);
                    if (e.getClickedBlock().getType().toString().contains("RAIL")) entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), HopperMinecart.class);
                    break;
                case COMMAND_BLOCK_MINECART:
                    e.setCancelled(true);
                    //if (e.getClickedBlock().getType().toString().contains("RAIL")) entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), Minecart.class);
                    break;
                
                case OAK_BOAT:
                case ACACIA_BOAT:
                case BIRCH_BOAT:
                case SPRUCE_BOAT:
                case JUNGLE_BOAT:
                case DARK_OAK_BOAT:
                    //if (e.getClickedBlock().getType()==Material.WATER) {
                        entity = p.getWorld().spawn(e.getClickedBlock().getRelative(BlockFace.UP).getLocation(), Boat.class);
                        switch (e.getItem().getType()) {
                            case OAK_BOAT:
                                ((Boat)entity).setWoodType(TreeSpecies.GENERIC);
                                break;
                            case DARK_OAK_BOAT:
                                ((Boat)entity).setWoodType(TreeSpecies.DARK_OAK);
                                break;
                            case ACACIA_BOAT:
                                ((Boat)entity).setWoodType(TreeSpecies.ACACIA);
                                break;
                            case BIRCH_BOAT:
                                ((Boat)entity).setWoodType(TreeSpecies.BIRCH);
                                break;
                            case SPRUCE_BOAT:
                                ((Boat)entity).setWoodType(TreeSpecies.REDWOOD);
                                break;
                            case JUNGLE_BOAT:
                                ((Boat)entity).setWoodType(TreeSpecies.JUNGLE);
                                break;
                        }
                        //((Boat)entity).setWoodType( e.getItem().getType()==Material.OAK_BOAT ? TreeSpecies.GENERIC : TreeSpecies.valueOf(e.getItem().getType().toString().split("_")[0]));
                    //}
                    break;
                
            }
            
//System.out.println("-- Limiter minecart="+entity);

            if (entity!=null) {
//System.out.println("-- valid?"+entity.isValid());
                //e.setCancelled(true);
                if (p.getInventory().getItemInMainHand().getType()==e.getItem().getType()) {
                    p.getInventory().setItemInMainHand(null);
                } else {
                    p.getInventory().setItemInOffHand(null);
                }
                final VehicleInfo vi = getVehicleInfo(e.getPlayer());
                
                if (entity.getType().toString().contains("MINECART")) {
                    
                    if (vi.minecart!= null && vi.minecart.get()!=null) {
                        vi.minecart.get().remove();
                        vi.minecart.clear();
                    }
                    vi.minecart = new WeakReference (entity);
                    
                    
                } else if (entity.getType()==EntityType.BOAT) {
//System.out.println("-- Limiter vi.boat="+vi.boat);
                    
                    if (vi.boat!= null && vi.boat.get()!=null) {
                        vi.boat.get().remove();
                        vi.boat.clear();
                    }
                    vi.boat = new WeakReference (entity);
                    
                }
                
            }
             
             
           // if (e.getItem().getType().toString().contains("MINECART")) {
             //   e.setCancelled(true);
             //   final Entity entity = e.getPlayer().getWorld().spawn(e.getClickedBlock().getLocation(), Minecart.class);
//System.out.println("-- Limiter onInteract() place MINECART loc="+e.getClickedBlock().getLocation());
                //final VehicleInfo vi = getVehicleInfo(e.getPlayer());
            //    getVehicleInfo(e.getPlayer()).placeMinecartCoord = e.getClickedBlock().getLocation().getBlockX()+":"+(e.getClickedBlock().getLocation().getBlockY()+1)+":"+e.getClickedBlock().getLocation().getBlockZ();
//System.out.println("-- Limiter onInteract() place MINECART block="+getVehicleInfo(e.getPlayer()).placeMinecartCoord);


           // } else  if (e.getItem().getType().toString().endsWith("_BOAT")) {

//System.out.println("-- Limiter onInteract() place _BOAT loc="+e.getClickedBlock().getLocation());
                //final VehicleInfo vi = getVehicleInfo(e.getPlayer());
              //  getVehicleInfo(e.getPlayer()).placeBoatCoord = e.getClickedBlock().getLocation().getBlockX()+":"+(e.getClickedBlock().getLocation().getBlockY()+1)+":"+e.getClickedBlock().getLocation().getBlockZ();
//System.out.println("-- Limiter onInteract() place _BOAT block="+getVehicleInfo(e.getPlayer()).placeBoatCoord);


           // }
            
        }
            
        
        
    }
    
    
    
    
    
    
    
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (block_dispenser_egg && e.getInventory().getType()==InventoryType.DISPENSER && e.getCurrentItem()!=null) {
            if (  e.getCurrentItem().getType().toString().endsWith("_EGG")) {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage("§cНа данном сервере запрещён спавн мобов через раздатчик!");
            }
        }
    }






    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent e) {
       // if (e.getItem()==null || e.getBlock()==null) return;
//System.out.println("nDispense item="+e.getItem().getType()+" block="+e.getBlock());
        if (e.getItem().getType().toString().contains("FIREWORK")) {
            if (Timer.CD_has("x"+e.getBlock().getLocation().getBlockX()+"y"+e.getBlock().getLocation().getBlockY()+"z"+e.getBlock().getLocation().getBlockZ(), "firework")) {
                e.setCancelled(true);
                e.getItem().setType(Material.AIR);
//System.out.println("Dispense cancel!!!");
            } else {
                Timer.CD_add("x"+e.getBlock().getLocation().getBlockX()+"y"+e.getBlock().getLocation().getBlockY()+"z"+e.getBlock().getLocation().getBlockZ(), "firework", 1);
            }
        } else if (block_dispenser_egg && e.getItem().getType().toString().endsWith("_EGG")) {
            e.setCancelled(true);
        } else if (oneMinecartPerPlayer && e.getItem().getType().toString().contains("MINECART")) {
            e.setCancelled(true);
        } else if (oneBoatPerPlayer && e.getItem().getType().toString().endsWith("_BOAT")) {
            e.setCancelled(true);
        }
    }
    

    
    
    
    
    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
//System.out.println("CreatureSpawnEvent "+e.getEntity().getType()+" reason="+e.getSpawnReason().toString());        
        //if (!use) return;
//System.out.println("222");        

        if (ml_block_all_spawn && e.getSpawnReason()!=SpawnReason.CUSTOM) {      //полная блокировка
//System.out.println("333");        
            e.setCancelled(true);
            return;
        }
//System.out.println("----spawn watch?"+ml_watch_creature_spawns+"  reason="+e.getSpawnReason().toString()+" contains?"+ml_reasons.contains(e.getSpawnReason().toString()) );        
        
        if ( !ml_watch_creature_spawns || !ml_reasons.contains(e.getSpawnReason().toString()) ) return;    
                                                //если откл. отслеживание спавна, возврат//если этот вид спавна не отслеживается, возврат
        
           boolean cancel=FastCheck(e.getEntity(), e.getSpawnReason());
                    
                    if(cancel) {
                        e.setCancelled(true);
                        //e.getEntity().remove();
                    }
                    
                   /* if (ml_check_surrounding_chunks > 0) {
                        Chunk c = e.getEntity().getLocation().getChunk();
                        
                        for (int x = c.getX() + ml_check_surrounding_chunks; x >= c.getX() - ml_check_surrounding_chunks; --x) {
                            for (int z = c.getZ() + ml_check_surrounding_chunks; z >= c.getZ() - ml_check_surrounding_chunks; --z) {
                                FastCheck(e.getLocation().getWorld().getChunkAt(x, z));
                            }
                        }
                    }*/

        }
    
    
    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent e) {
        if (ml_check_chunk_onload)   {
            CheckEntyty(e.getChunk());
            checkBlockStates(e.getChunk());
        }
    }


    
    
    
    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent e) {
        if (ml_check_chunk_unload) CheckEntyty(e.getChunk());
    }




    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getBlock().getState()!=null) {
            //if (max_banner<=0 || max_skull<=0 || max_frame<=0) return;
            if (ApiOstrov.isLocalBuilder(e.getPlayer(), false) || e.getPlayer().hasPermission("ostrov.limiter.ignore") ) return;
            //e.setCancelled( checkBlockStates(e.getPlayer(), e.getBlock()) );
            final LimitType limittype = LimitType.getType(e.getBlock().getType());
//System.out.println("onBlockPlace1 limittype="+limittype+" limit="+limittype.limit);                
            
            if (limittype!=LimitType.НЕТ && limittype.limit>0) {
                int count=0;
//System.out.println("onBlockPlace2 !!");                
                for (final BlockState blockState : e.getBlock().getChunk().getTileEntities()) {
//System.out.println("onBlockPlace3 count="+count+" mat="+blockState.getType().toString()+" type="+LimitType.getType(blockState.getType()).toString());                
                    if (LimitType.getType(blockState.getType())==limittype) {
                        count++;
//System.out.println("onBlockPlace3 count++="+count);                
                        if (count>=limittype.limit) {
                            e.setCancelled(true);
                            blockState.getBlock().setType(Material.AIR);
                            e.getPlayer().sendMessage("§eЛимит "+limittype.toString()+" в чанке: "+limittype.limit);
                            Ostrov.log_ok("§eBlockPlaceEvent cancel p="+e.getPlayer().getName()+" size="+e.getBlock().getChunk().getTileEntities().length+" mat="+blockState.getType().toString()+" type="+limittype.toString()+" count="+count );                        
                        }
                    }
                }
                
            }
        }
    }




    private static void checkBlockStates(final Chunk chunk) {
        
        final Map <LimitType,Integer> counter = new HashMap<>();
        
        LimitType limittype;
        
            for (final BlockState blockState : chunk.getTileEntities()) {
                
                limittype = LimitType.getType(blockState.getType());
                
                if (limittype!=LimitType.НЕТ && limittype.limit>0) {
                    if (counter.containsKey(limittype)) {
                        if (counter.get(limittype)>=limittype.limit) {
                            blockState.getBlock().setType(Material.AIR);
                            //if (p!=null) p.sendMessage("§cЛимит "+limittype.toString()+" в чанке: "+limittype.limit);
                            Ostrov.log_ok("§eremove blockState on ChunkLoad! size"+chunk.getTileEntities().length+" mat="+blockState.getType().toString()+" type="+limittype.toString()+" count="+counter.get(limittype) );                        
                        } else {
                            counter.put(limittype, counter.get(limittype)+1);
                        }
                    } else {
                        counter.put(limittype, 1);
                    }
                }
                
            }
        
        counter.clear();
        
    } 


    
    
    
    


    
    
    private void CheckEntyty(final Chunk chunk) {


        final HashMap<EntityGroup, Integer> groups = new HashMap<>();
        final HashMap<String, Integer> types = new HashMap<>();

        String eType;
        EntityGroup eGroup;
        int count;
        int removed=0;

            for (Entity entity: chunk.getEntities()) {

                //eGroup = Type.valueOf(entity.getType().toString()).getMeta();
                eGroup = VM.getNmsEntitygroup().getEntytyType(entity);
                eType = entity.getType().toString();
    //System.err.println("vvvvvvv CheckChunk group="+eGroup+"  type="+eType+" limit_by_group="+limit_groups.get(eGroup)+" limit_by_type="+limit_type.get(eType));

                if(limit_groups.containsKey(eGroup)) {
                    if (groups.containsKey(eGroup)) count=groups.get(eGroup); else count=0;
                    count++;
    //System.out.println("1 group count="+count+" removed="+removed);                    
                    if (count>=limit_groups.get(eGroup)) {
    //System.err.println("Удаляем по группе "+eGroup+" limit_by_group="+limit_groups.get(eGroup));
                        entity.remove();
                        removed++;
                    }
                    groups.put(eGroup, count);

                } else if (limit_type.containsKey(eType))  {
                        if (types.containsKey(eType)) count=types.get(eType); else count=0;
                         count++;
    //System.out.println("1 type count="+count+" removed="+removed);                    
                         if (count>=limit_type.get(eType)) {
    //System.err.println("Удаляем по типу "+eType+" limit_by_type="+limit_type.get(eType));
                            entity.remove();
                            removed++;
                         }
                         types.put(eType, count);
                    }

            }

            if (removed>0) Ostrov.log_ok("§5В чанке "+chunk.getX()+":"+chunk.getZ()+" удалено "+removed+" сущностей.");
            groups.clear();
            types.clear();
    }    





    
    
    
    
    private boolean FastCheck(final Entity e, final SpawnReason reason) {

        final EntityGroup group = VM.getNmsEntitygroup().getEntytyType(e);
        final String type = e.getType().toString();
//System.out.println("----FastCheck group="+group+"  type="+type );        
        if (!limit_groups.containsKey(group) && !limit_type.containsKey(type)) return false;
        
        int group_count=0;                                  //счётчик по группе
        int type_count=0;                                  //счётчик по типу
        List <Player>master=null;   //список игроков для оповещения
        
        //например,стойки для брони SpawnReason=DEFAULT
        if (reason==SpawnReason.SPAWNER_EGG || reason==SpawnReason.EGG || reason==SpawnReason.DISPENSE_EGG || reason==SpawnReason.DEFAULT ) {
            master=new ArrayList<>();
        }

            for ( Entity en: e.getLocation().getChunk().getEntities()) {

                if (en instanceof Player)  {
                    
                    if (master!=null) master.add((Player) en);
                    
                } else {
                    
                    if (limit_groups.containsKey(group)) {
                        group_count++;
                        if (group_count>=limit_groups.get(group)) {
                            if (ml_notify_players && master!=null && !master.isEmpty()) {
                                for (Player p: master ) {
                                    p.sendMessage("§eПревышен лимит сущности для чанка по группе §a"+group.toString()+"§e, лимит: §c"+limit_groups.get(group));
                                }
                            }
                            return true;
                        }
                    }
                    
                    if (limit_type.containsKey(type)) {
                        type_count++;
                        if (type_count>=limit_type.get(type)) {
                            if (ml_notify_players && master!=null && !master.isEmpty()) {
                                for (Player p: master ) {
                                    p.sendMessage("§eПревышен лимит сущности для чанка по типу §a"+type+"§e, лимит: §c"+limit_type.get(type));
                                }
                            }
                            return true;
                        }
                    }
                }
            }
//System.err.println("xxx: cancel="+cancel+" group:"+g_c+"/"+g_l+"   type:"+t_c+"/"+t_l);                    
        return false;
    
    

    }    

    private VehicleInfo getVehicleInfo(final Player p) {
        if (vehicleInfo.containsKey(p.getName())) {
            return vehicleInfo.get(p.getName());
        } else {
            final VehicleInfo vi = new VehicleInfo(p);
            vehicleInfo.put(p.getName(), vi);
            return vi;
        }
    }
/*
    private VehicleInfo findByMinecartCoord(final String coord) {
        for (final VehicleInfo vi : vehicleInfo.values()) {
            if ( !vi.placeMinecartCoord.isEmpty() && vi.placeMinecartCoord.equals(coord) ) return vi;
        }
        return null;
    }
    
    private VehicleInfo findByBoatCoord(final String coord) {
        for (final VehicleInfo vi : vehicleInfo.values()) {
            if ( !vi.placeBoatCoord.isEmpty() && vi.placeBoatCoord.equals(coord)) return vi;
        }
        return null;
    }*/
    


    
    
    
    
    
    
    

    public static enum LimitType { 
        
        НЕТ(0),
        ГОЛОВЫ(36),
        БАННЕРЫ(36),
        РАМКИ(48),
        ВОРОНКИ(16),
        РАЗДАТЧИКИ(8),
        СУНДУКИ(36),
        ;

        private static void setLimit(final String type, int limit) {
            if (limit>1024||limit<0) limit=0;
            for (LimitType lt:values()) {
                if (lt.toString().equals(type)) {
                    lt.limit = limit;
                    break;
                }
            }
        }
        
        public int limit;

        private LimitType(int limit){
            this.limit = limit;
        }
        
        public static LimitType getType(final Material mat) {
            
            final String type = mat.toString();
            
            if (type.contains("BANNER")) return БАННЕРЫ;
            else if (type.contains("SKULL")) return ГОЛОВЫ;
            else if (type.contains("HOPPER")) return ВОРОНКИ;
            else if (type.contains("CHEST")) return СУНДУКИ;
            else if (type.equals("DISPENSER")) return РАЗДАТЧИКИ;
            else if (type.equals("DROPPER")) return РАЗДАТЧИКИ;
            else if (type.equals("ITEM_FRAME")) return РАМКИ;
            
            return НЕТ;
            /*switch (mat) {
                case ITEM_FRAME:
                    return РАМКИ;
                case DISPENSER:
                case DROPPER:
                    return РАЗДАТЧИКИ;
                    
                default:
                    return НЕТ;
            }*/
        }
        
    }   

    
    
    
    
    
    
    
    
    
    
    
    
    private static class VehicleInfo {
        
        private final String name;
        //public String placeMinecartCoord="";
        //private String placeBoatCoord="";
        private WeakReference<Entity> minecart;
        private WeakReference<Entity> boat;

        public VehicleInfo(final Player p) {
            this.name = p.getName();
        }
    }
    

    
    
    
    

    
    
    
    




}

