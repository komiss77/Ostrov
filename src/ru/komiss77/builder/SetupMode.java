package ru.komiss77.builder;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.Ostrov;
import ru.komiss77.builder.menu.BuilderMain;
import ru.komiss77.builder.menu.EntityWorldMenu;
import ru.komiss77.events.BuilderMenuEvent;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.modules.world.SchemEditorMenu;
import ru.komiss77.modules.world.SchemMainMenu;
import ru.komiss77.modules.world.Schematic;
import ru.komiss77.modules.world.Schematic.Rotate;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.Nms;


public class SetupMode implements Listener {

    public final GameMode before; //гм строителя до начала режима
    //public boolean canRaset = false; //хз что это
    public String lastEdit = ""; //режим последнего открытого меню
    public final String builderName; //ник строителя

    public String schemName = ""; //название создаваемого схематика
    public String param = ""; //параметры кубоида
    public Cuboid cuboid; //кубоид, выделенный в билдере
    private World cuboidWorld; //мир кубоида
    public Location min; //локация кубоида 1
    public Location max; //локация кубоида 2
    public Location spawnPoint; //не переименовывать! юзают другие плагины! локация axis

    //Доп.поля для внешних билдеров
    public String extra1 = "";
    public String extra2 = "";
    public Object arena; //арена для игрового билдера
    public Object loacalEditMode; //режим последнего открытого локального меню

    public Schematic undo; //для отмены последней вставки
    public WXYZ undoLoc; //локация последней вставки
    public BukkitTask displayCube;

    public SetupMode(final Player p) {
        this.builderName = p.getName();
        before = p.getGameMode();
    }


    public void rotate(final Player p, Rotate rotate, boolean withContent) {
        p.closeInventory();
        //делаем снимок неповёрнутой местности
        if (withContent) {
            final Schematic copy = new Schematic(p, p.getName() + "_rotate", "", cuboid, p.getWorld(), false);
            clearArea();
            cuboid.rotate(rotate);
            copy.paste(p, new WXYZ(cuboid.getSpawnLocation(cuboidWorld)), rotate, true);
        } else {
            cuboid.rotate(rotate);
        }
        //получаем новые координаты
        //spawnPoint не меняется, поворот вокруг неё
        min = cuboid.getLowerLocation(cuboidWorld);
        max = cuboid.getHightesLocation(cuboidWorld);
//Ostrov.log("-- rotate withContent?"+withContent+" schem="+schem);
        genBorder(p);//checkPosition(p); //- не делать, или делает новый кубоид
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 5);
    }

    //не убирать! использует лоббиОстров
    public void setCuboid(final Player p, final Cuboid cuboid) {
        this.cuboid = cuboid;
        cuboidWorld = p.getWorld();
        min = cuboid.getLowerLocation(cuboidWorld);
        max = cuboid.getHightesLocation(cuboidWorld);
        spawnPoint = cuboid.getSpawnLocation(cuboidWorld);
        genBorder(p);//checkPosition(p);
    }

    public void clearArea() {
        if (cuboid == null || cuboidWorld == null) return;
        cuboid.getBlocks(cuboidWorld).forEach((b) -> {
            if (Nms.getFastMat(cuboidWorld, b.getX(), b.getY(), b.getZ()) != Material.AIR) {
                b.setType(Material.AIR);
            }
            //if (!b.getType().isAir()) b.setType(Material.AIR);
        });
    }

    //не убирать! использует лоббиОстров, скилс
    public void resetCuboid() {
        cuboid = null;
        cuboidWorld = null;
        min = null;
        max = null;
        spawnPoint = null;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public void setSpawn(final Player p) {
        if (cuboid.contains(p.getLocation())) {
            spawnPoint = p.getLocation();
            spawnPoint.setYaw(p.getLocation().getYaw());
            spawnPoint.setPitch(p.getLocation().getPitch());
        } else {
            spawnPoint = null;
        }
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 5);
        checkPosition(p);

    }


    public void checkPosition(final Player p) {
//System.out.println("setPosition "+p.getName());
        if (min != null && max != null
                && min.getWorld().getName().equals(max.getWorld().getName())
                && p.getWorld().getName().equals(min.getWorld().getName())) {
            if (spawnPoint != null && cuboid != null && !cuboid.contains(spawnPoint)) {
                spawnPoint = null;
            }
            cuboid = new Cuboid(new XYZ(min), new XYZ(max), spawnPoint);
            cuboidWorld = min.getWorld();
            genBorder(p);
//System.out.println("new Cuboid ");
        } else {
            cuboid = null;
            cuboidWorld = null;
            if (displayCube != null && !displayCube.isCancelled()) displayCube.cancel();
        }
    }

    private void genBorder(final Player p) {
        //VM.getNmsServer().BorderDisplay(p, pos1, pos2, false);
        if (displayCube != null && !displayCube.isCancelled()) displayCube.cancel();

        displayCube = new BukkitRunnable() {
            final Set<XYZ> border = cuboid.getBorder();
            Location particleLoc = new Location(p.getWorld(), 0, 0, 0);

            @Override
            public void run() {
                if (p == null || !p.isOnline() || p.isDead()) {
                    this.cancel();
                    return;
                }
                border.stream().forEach(
                        (xyz) -> {
                            particleLoc.set(xyz.x, xyz.y, xyz.z);
                            //p.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, particleLoc, 0);
                            if (xyz.pitch >= 5) { //стенки
                                p.spawnParticle(Particle.FIREWORKS_SPARK, particleLoc, 0);
                            } else {
                                p.spawnParticle(Particle.VILLAGER_HAPPY, particleLoc, 0);
                                //p.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0);
                            }
                            //p.spawnParticle(Particle.VILLAGER_HAPPY, particleLoc, 0);
                        }
                );
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 10, 25);
      /*  final Set<XYZ>border = cuboid.getBorder();//new HashSet<>();
        /*for (int y = cuboid.getLowerY(); y<=cuboid.getHightesY()+1; y++) {
            border.add( new XYZ(null, cuboid.getLowerX(), y, cuboid.getLowerZ()) );
            border.add( new XYZ(null, cuboid.getHightesX()+1, y, cuboid.getLowerZ()) );
            border.add( new XYZ(null, cuboid.getLowerX(), y, cuboid.getHightesZ()+1) );
            border.add( new XYZ(null, cuboid.getHightesX()+1, y, cuboid.getHightesZ()+1) );
        }
        for (int x = cuboid.getLowerX(); x<=cuboid.getHightesX()+1; x++) {
            border.add( new XYZ(null, x, cuboid.getLowerY(), cuboid.getLowerZ()) );
            border.add( new XYZ(null, x, cuboid.getHightesY()+1, cuboid.getLowerZ()) );
            border.add( new XYZ(null, x, cuboid.getLowerY(), cuboid.getHightesZ()+1) );
            border.add( new XYZ(null, x, cuboid.getHightesY()+1, cuboid.getHightesZ()+1) );
        }
        for (int z = cuboid.getLowerZ(); z<=cuboid.getHightesZ()+1; z++) {
            border.add( new XYZ(null, cuboid.getLowerX(), cuboid.getLowerY(), z) );
            border.add( new XYZ(null, cuboid.getLowerX(), cuboid.getHightesY()+1, z) );
            border.add( new XYZ(null, cuboid.getHightesX()+1, cuboid.getLowerY(), z) );
            border.add( new XYZ(null, cuboid.getHightesX()+1, cuboid.getHightesY()+1, z) );
        }/
//border.stream().forEach( xyz->Ostrov.log(xyz.toString()) );
        if (op.displayCube!=null && !op.displayCube.isCancelled()) op.displayCube.cancel();
        
        op.displayCube = new BukkitRunnable() {
            final Player p = Bukkit.getPlayerExact(builderName);
            Location particleLoc = new Location(cuboidWorld, 0, 0, 0);
            @Override
            public void run() {
                if (p==null || !p.isOnline()) {
                    this.cancel();
                    return;
                }
                border.stream().forEach(
                    (xyz)->{
                        particleLoc.set(xyz.x, xyz.y, xyz.z);
                        p.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, particleLoc, 0);
                    }
                );
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 10, 20);*/

    }


    public void openSetupMenu(final Player p) {
//System.out.println("openSetupMenu lastEdit="+lastEdit+" ");        
        switch (lastEdit) {

            case "SchemEdit" -> openSchemEditMenu(p, schemName);

            case "SchemMain" -> openSchemMainMenu(p);

            case "", "Main" -> openMainSetupMenu(p);

            case "entity" -> openEntityWorldMenu(p, null, -1);

            default -> openLocalGameMenu(p);
        }
        //case "LocalGame" :
        //openLocalGameMenu(player);
        //break;


    }


    public void openEntityWorldMenu(final Player p, World world, final int radius) {
        lastEdit = "entity";
        if (world == null) {
            world = p.getWorld();
        }
        SmartInventory.builder()
                .id("EntityMain" + p.getName())
                .provider(new EntityWorldMenu(world, radius))
                .size(6, 9)
                .title("§2Сущности " + world.getName())
                .build()
                .open(p);
    }

    public void openMainSetupMenu(final Player p) {
        lastEdit = "";
        //if (p.getName().equals("komiss77") || p.getName().equals("semen")) {
        SmartInventory.builder()
                .id("Builder" + p.getName())
                .provider(new BuilderMain())
                .size(6, 9)
                .title("§2Меню Строителя")
                .build().open(p);
      /*  } else {
            SmartInventory.builder()
                    .id("Build " + p.getName())
                    .provider(new BuilderInv())
                    .size(3, 9)
                    .title("§eМеню Строителя")
                    .build().open((Player) p);
        }*/


    }

    public void openLocalGameMenu(final Player p) {
        Bukkit.getPluginManager().callEvent(new BuilderMenuEvent(p, this)); //event
    }

    public void openSchemMainMenu(final Player p) {
        lastEdit = "SchemMain";
        SmartInventory.builder()
                .id("SchemMain" + p.getName())
                .provider(new SchemMainMenu())
                .size(6, 9)
                .title("§9Редактор схематиков")
                .build().open(p);
    }

    public void openSchemEditMenu(final Player p, final String schemName) {
        if (schemName.isEmpty()) {
            this.schemName = schemName;
            openSchemMainMenu(p);
            return;
        }
        lastEdit = "SchemEdit";
        this.schemName = schemName;
        SmartInventory.builder()
                .id("SchemEditor" + p.getName())
                .provider(new SchemEditorMenu())
                .size(6, 9)
                .title("§9Cхематик " + schemName)
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
        if (e.getPlayer().getName().equals(builderName)) {
            BuilderCmd.end(builderName);
        }
    }
    
     /* @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(final PlayerKickEvent e) {
        if(e.getPlayer().getName().equals(builderName))  {
            BuilderCmd.end(builderName);
        }
    }


    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if(!e.getPlayer().getName().equals(builderName)) return;
        if (e.getAction() == Action.PHYSICAL || e.getItem()==null ) return;
        if (ItemUtils.compareItem(e.getItem(), BuilderCmd.openBuildMenu, false)) {
            e.setUseItemInHand(Event.Result.DENY);
            if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction()==Action.RIGHT_CLICK_BLOCK) {
                openSetupMenu(e.getPlayer());
            }
        }
    }   
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if(!e.getPlayer().getName().equals(builderName)) return;
        final ItemStack item = e.getItemDrop().getItemStack();
        if (ItemUtils.compareItem(item, BuilderCmd.openBuildMenu, false) ) {
            //e.setCancelled(true);
            e.getItemDrop().remove();
        }
    }*/


    // @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    // public void onWorldChange (final PlayerChangedWorldEvent e) {
    //? if(e.getPlayer().getName().equals(name))  Builder.end(name);
    //if (builders.get(e.getPlayer().getName()).canRaset) end(e.getPlayer()); //(e.getPlayer()); смена мира проиходит через 10тик после начала и сразу вырубит, так нельзя!
    //ItemUtils.substractAllItems(e.getPlayer(), openBuildMenu.getType());
    //}    


}
