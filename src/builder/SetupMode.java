package builder;

import builder.menu.BuilderMain;
import builder.menu.SchemEditor;
import builder.menu.SchemMain;
import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Commands.Builder;
import ru.komiss77.Managers.Cuboid;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.SmartInventory;





//builders.get(player.getName()).setPosition(player, pos1, pos2);




public class SetupMode implements Listener{

    //public static ItemStack openBuildMenu = new ItemBuilder(Material.MAP).name("§aМеню настройки SkyBlock").build();;
    //public static SetupManager mapbuilderListener;
    public boolean canRaset=false;
    private LastEdit lastEdit = LastEdit.Main;
    private final String name;

    public String schemName="";
    private Cuboid cuboid;
    private World cuboidWorld;
    public Location pos1;
    public Location pos2;
    
    
    public SetupMode(final Player p) {
        this.name = p.getName();
        
    }

    public void setCuboid(final Player p, final Cuboid cuboid) {
        this.cuboid = cuboid;
        cuboidWorld = p.getWorld();
        pos1 = cuboid.getLowerLocation(cuboidWorld);
        pos2 = cuboid.getHightesLocation(cuboidWorld);
        checkPosition(p);
    }

    public void reset() {
        if ( cuboid==null || cuboidWorld==null) return;
        final Iterator <Block> it = cuboid.iterator(cuboidWorld);
        while (it.hasNext()) {
            it.next().setType(Material.AIR);
        }
    }   

    public void resetCuboid() {
        cuboid = null;
        cuboidWorld = null;
        pos1=null;
        pos2=null;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }



    private enum LastEdit {
        Main, SchemMain, SchemEdit, 
        ;
    }

    

    public void checkPosition(final Player p) {
//System.out.println("setPosition "+p.getName());
        if (pos1!=null && pos2!=null
                && pos1.getWorld().getName().equals(pos2.getWorld().getName())
                && p.getWorld().getName().equals(pos1.getWorld().getName()) ) {
            cuboid = new Cuboid(pos1, pos2);
            cuboidWorld = pos1.getWorld();
//System.out.println("new Cuboid ");
        } else {
            cuboid = null;
            cuboidWorld = null;
        }
    }

    public void updateAsync(final Player p) {
//System.out.println("updateAsync "+p.getName()+" points: "+ (cuboid==null ? "null" : cuboid.getSize()) );
        if ( cuboid!=null && cuboidWorld!=null) {
                //&& pos1!=null && pos2!=null
                //&& pos1.getWorld().getName().equals(pos2.getWorld().getName())
                //&& p.getWorld().getName().equals(pos1.getWorld().getName()) ) {
            //проверить размер выделения!
            //final Particle.DustOptions option = new Particle.DustOptions(Color.RED, 1);
            
            
            //final Vector3d playerVector = new Vector3d(p.getLocation().toVector());
            //final Vector3d origin = (this.type != SelectionType.CLIPBOARD) ? Vector3d.ZERO : location.subtract(selectionPoints.origin()).floor();
            //Location loc; = p.getLocation().clone();
            final Iterator <Location> it = cuboid.borderIterator(cuboidWorld);
            while (it.hasNext()) {
                //p.getWorld().spawnParticle(Particle.REDSTONE, it.next(), 1, 0, 0, 0, option);
                p.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, it.next(), 0);
                if (it.hasNext()) it.next(); //через одну!
            }
            
            
            
           // for (final Vector3d vector : selections) {
            //    final double x = vector.getX() + origin.getX();
            //    final double y = vector.getY() + origin.getY();
             //   final double z = vector.getZ() + origin.getZ();
             //   loc.setX(x);
              //  loc.setY(y);
              //  loc.setZ(z);
                ////if (playerVector.distanceSquared(x, y, z) > 50) {
                  //  continue;
               // }
//System.out.println("x="+loc.getBlockX()+" y="+loc.getBlockY()+" z="+loc.getBlockZ());
                //p.getWorld().spawnParticle(Particle.REDSTONE, p.getEyeLocation().clone().add(dx, dy, dz), 1, 0, 0, 0, option);
                //p.getWorld().spawnParticle(Particle.REDSTONE, x, y, z, 1, 0, 0, 0, option);
               // p.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, option);
                //p.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0);
                //FastParticle.spawnParticle(player, particleData.getType(), x, y, z, 1, 0.0, 0.0, 0.0, 0.0, particleData.getData());
           // }
        }
    }
    
    /*public static void SetupManager(final Player player) {
        //if (mapbuilderListener!=null) {
        //    HandlerList.unregisterAll(mapbuilderListener);
        //}
        lastEdit = LastEdit.Main;
        //mapbuilderListener = new SetupListener();
        Bukkit.getPluginManager().registerEvents(mapbuilderListener, SW.plugin);
        //player.performCommand(UniversalListener.leaveCommad);
        //player.teleport(Bukkit.getWorld(arena.worldName).getSpawnLocation());
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setGameMode(GameMode.CREATIVE);
                player.setAllowFlight(true);
                player.setFlying(true);
                //player.getInventory().clear();
                player.getInventory().setItem(0, openBuildMenu.clone());
                player.updateInventory();
            }
        }.runTaskLater(SW.plugin, 10);

    }
    
    
    public static void end(final Player player) {
        //HandlerList.unregisterAll(mapbuilderListener);
        player.closeInventory();
        ItemUtils.substractAllItems(player, SetupListener.openBuildMenu.getType());
        SW.tpLobby(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage("§eРежим настройки закончен.");
    }
    
    

    
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL || e.getItem()==null ) return;
        if (ItemUtils.compareItem(e.getItem(), openBuildMenu, false)) {
            e.setCancelled(true);
            if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction()==Action.RIGHT_CLICK_BLOCK) openSetupMenu(e.getPlayer());
        }
    }   
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        final ItemStack item = e.getItemDrop().getItemStack();
        if (ItemUtils.compareItem(item, openBuildMenu, false) ) {
            e.setCancelled(true);
        }
    }
    

        
    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWorldChange (final PlayerChangedWorldEvent e) {
        ItemUtils.substractAllItems(e.getPlayer(), openBuildMenu.getType());
    }    
    */
    
    
    
    
    public void openSetupMenu(final Player player) {
//System.out.println("openSetupMenu lastEdit="+lastEdit+" ");        
        switch (lastEdit) {
            case SchemEdit :
                openSchemEditMenu(player, schemName);
                break;
                
            case SchemMain :
                openSchemMainMenu(player);
                break;

            case Main:
            default:
                openMainSetupMenu(player);
                break;
        }
        
        
        
    }
    
    
    
    
    public void openMainSetupMenu(final Player p) {
        lastEdit = LastEdit.Main;
        SmartInventory.builder()
            .id("Builder"+p.getName())
            .provider(new BuilderMain())
            .size(6, 9)
            .title("§2Меню Строителя")
            .build().open(p);   
    }
    
    public void openSchemMainMenu(final Player p) {
        lastEdit = LastEdit.SchemMain;
        SmartInventory.builder()
                .id("SchemMain"+p.getName())
                .provider(new SchemMain())
                .size(6, 9)
                .title("§9Редактор схематиков")
                .build().open(p);
    }

    public void openSchemEditMenu(final Player p, final String schemName) {
        if (schemName.isEmpty()) {
            this.schemName=schemName;
            openSchemMainMenu(p);
            return;
        }
        lastEdit = LastEdit.SchemEdit;
        this.schemName = schemName;
        SmartInventory.builder()
            .id("SchemEditor"+p.getName())
            .provider(new SchemEditor())
            .size(6, 9)
            .title("§9Cхематик "+schemName)
            .build().open(p);
    }

    
/*
    public static void hideAS(final Arena arena) {
        for (Entity entity : arena.arenaWorld.getEntities()) {
            if (entity.getType()==EntityType.ARMOR_STAND && entity.getCustomName()!=null && entity.getCustomName().startsWith("§f>> ")) {
                entity.remove();
            }
        }
    }
    
    public static void showAS(final Arena arena) {
        hideAS(arena);
        Entity entity;
        for (Location loc : arena.powerups) {
            entity = loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            entity.setCustomName("§f>> §7Спавн §1 §f<<");
            entity.setCustomNameVisible(true);
        }
        

        
    }
  
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
//System.out.println("!!! ArmorStanddamage !"+e.getEntity().getCustomName()); 
    
        if ( (e.getEntity().getType()==EntityType.ARMOR_STAND) && e.getDamager().getType()==EntityType.PLAYER && ApiOstrov.isLocalBuilder((Player) e.getDamager(), false) ){
//System.out.println("!!! ArmorStanddamage 2"); 
            if (e.getEntity().getCustomName()!=null && e.getEntity().getCustomName().startsWith("§f>> ") ) {
//System.out.println("!!! ArmorStanddamage 3"); 
               e.setCancelled(true);
                final Player player = (Player) e.getDamager();
                Arena arena = GameManager.getArenabyWorld(player.getWorld().getName());

                if (arena == null) {
                   player.sendMessage("§cНе найдено арены в этом мире");
                   return;
                } else {
                    Location loc;
//System.out.println("!!! ArmorStanddamage 4"); 
                    
                    Iterator <Location> it = arena.spawns_blue.iterator();
                    while (it.hasNext()) {
                        loc = it.next();
                        if (e.getEntity().getLocation().getBlockX()==loc.getBlockX() && e.getEntity().getLocation().getBlockY()==loc.getBlockY() && e.getEntity().getLocation().getBlockZ()==loc.getBlockZ()) {
                            player.sendMessage("§cВы удалили точку спавна синих!");
                            it.remove();
                            e.getEntity().remove();
                            return;
                        }
                    }
                    it = arena.spawns_red.iterator();
                    while (it.hasNext()) {
                        loc = it.next();
                        if (e.getEntity().getLocation().getBlockX()==loc.getBlockX() && e.getEntity().getLocation().getBlockY()==loc.getBlockY() && e.getEntity().getLocation().getBlockZ()==loc.getBlockZ()) {
                            player.sendMessage("§cВы удалили точку спавна красных!");
                            it.remove();
                            e.getEntity().remove();
                            return;
                        }
                    }
                    
                    Stock stock;
                    Iterator <Stock> its = arena.items.iterator();
                    while (its.hasNext()) {
                        stock = its.next();
                        if (e.getEntity().getLocation().getBlockX()==stock.spawn_loc.getBlockX() && e.getEntity().getLocation().getBlockY()==stock.spawn_loc.getBlockY() && e.getEntity().getLocation().getBlockZ()==stock.spawn_loc.getBlockZ()) {
                            player.sendMessage("§cВы удалили точку спавна ресурса "+stock.type.displayName);
                            it.remove();
                            e.getEntity().remove();
                            return;
                        }
                    }
                }
            }
//System.err.println("!!! ArmorStanddamage !"+e.getEntity().getCustomName()); 

        }    
    }
    */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(final PlayerQuitEvent e) {
        if(e.getPlayer().getName().equals(name))  Builder.end(name);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(final PlayerKickEvent e) {
        if(e.getPlayer().getName().equals(name))  Builder.end(name);
    }

    
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if(!e.getPlayer().getName().equals(name)) return;
        if (e.getAction() == Action.PHYSICAL || e.getItem()==null ) return;
        if (ItemUtils.compareItem(e.getItem(), Builder.openBuildMenu, false)) {
            e.setUseItemInHand(Event.Result.DENY);
            if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction()==Action.RIGHT_CLICK_BLOCK) {
                openSetupMenu(e.getPlayer());
            }
        }
    }   
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if(!e.getPlayer().getName().equals(name)) return;
        final ItemStack item = e.getItemDrop().getItemStack();
        if (ItemUtils.compareItem(item, Builder.openBuildMenu, false) ) {
            //e.setCancelled(true);
            e.getItemDrop().remove();
        }
    }
    

        
    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWorldChange (final PlayerChangedWorldEvent e) {
       //? if(e.getPlayer().getName().equals(name))  Builder.end(name);
        //if (builders.get(e.getPlayer().getName()).canRaset) end(e.getPlayer()); //(e.getPlayer()); смена мира проиходит через 10тик после начала и сразу вырубит, так нельзя!
        //ItemUtils.substractAllItems(e.getPlayer(), openBuildMenu.getType());
    }    
    
 
}
