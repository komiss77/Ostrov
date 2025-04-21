package ru.komiss77.builder;

import java.util.Set;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.Ostrov;
import ru.komiss77.builder.menu.BuilderMain;
import ru.komiss77.builder.menu.EntityWorldMenu;
import ru.komiss77.events.BuilderMenuEvent;
import ru.komiss77.modules.world.*;
import ru.komiss77.modules.world.Schematic.Rotate;
import ru.komiss77.utils.BlockUtil;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.Nms;



public class SetupMode {

    public final GameMode before; //гм строителя до начала режима
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
    @Deprecated
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
            final Schematic copy = new Schematic(null, p.getName() + "_rotate", "", cuboid, p.getWorld(), false);
            clearArea();
            cuboid.rotate(rotate);
            copy.paste(p, BVec.of(cuboid.getSpawnLocation(cuboidWorld)), rotate, true);
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
            if (!Nms.fastType(cuboidWorld, b.getX(), b.getY(), b.getZ()).isAir()) {
                b.setBlockData(BlockUtil.air, false);
            }
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
        if (min != null && max != null
            && min.getWorld().getName().equals(max.getWorld().getName())
            && p.getWorld().getName().equals(min.getWorld().getName())) {
            if (spawnPoint != null && cuboid != null && !cuboid.contains(spawnPoint)) {
                spawnPoint = null;
            }
            cuboid = new Cuboid(BVec.of(min), BVec.of(max), spawnPoint);
            cuboidWorld = min.getWorld();
            genBorder(p);
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
            @Deprecated
            final Set<XYZ> border = cuboid.getBorder();
            final Location particleLoc = new Location(p.getWorld(), 0, 0, 0);

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
                            p.spawnParticle(Particle.FIREWORK, particleLoc, 0);
                        } else {
                            p.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 0);
                            //p.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0);
                        }
                        //p.spawnParticle(Particle.VILLAGER_HAPPY, particleLoc, 0);
                    }
                );
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 10, 25);
    }


    public void openSetupMenu(final Player p) {
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
        SmartInventory.builder()
            .id("Builder" + p.getName())
            .provider(new BuilderMain())
            .size(6, 9)
            .title("§2Меню Строителя")
            .build().open(p);
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


}
