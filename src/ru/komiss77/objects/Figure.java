package ru.komiss77.objects;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import ru.komiss77.enums.Game;
import ru.komiss77.events.FigureActivateEntityEvent;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;


public class Figure {

    public final int figureId;
    public String name = "§8Безымянная";
    public String tag = "";
    public String lastName;

    public final EntityType entityType;
    public FigureType type = FigureType.COMMAND;
    public Game game = null;
    public String leftclickcommand = "@c say @p левый клик";
    public String rightclickcommand = "@c say @p правый клик";

    public String worldName;
    public int x, y, z, yaw, pitch;

    public int spawn_try = 0;
    public Entity entity;
    public int lastCheck = 0;
    public Location spawnLoc;
    public boolean allowTp = false; //для ручного тп фигуры


    public Figure(final Entity entity) { //новая без типа! стойка точно в порядке, сохранение после выбора типа.
        this.figureId = generateId();
        entityType = entity.getType();
        worldName = entity.getWorld().getName();
        this.entity = entity;
        x = entity.getLocation().getBlockX();
        y = entity.getLocation().getBlockY();
        z = entity.getLocation().getBlockZ();
        yaw = (int) entity.getLocation().getYaw();
        pitch = (int) entity.getLocation().getPitch();
        spawnLoc = entity.getLocation();
        spawnLoc.setYaw(entity.getLocation().getYaw());
        spawnLoc.setPitch(entity.getLocation().getPitch());
        Figure.this.setName(name);
        //Bukkit.getPluginManager().callEvent(new FigureActivateEntityEvent(this)); //тут рановато, нет тэга, типа и имени
    }


    public Figure(final int figureId, final EntityType entityType, final FigureType type, final Game game, final String locString) {
//System.out.println("********* setType "+type+" : "+game);final Location location) { //загрузка
        this.figureId = figureId;
        this.entityType = entityType;
        this.type = type;
        this.game = game;

        String[] split = locString.split(locString.contains(":") ? ":" : "<>");
        worldName = split[0];
        x = NumUtil.intOf(split[1], 0);
        y = NumUtil.intOf(split[2], 0);
        z = NumUtil.intOf(split[3], 0);
        yaw = NumUtil.intOf(split[4], 0);
        pitch = NumUtil.intOf(split[5], 0);
        spawnLoc = LocUtil.stringToLoc(locString, false, false).toCenterLocation();
    }

    public static int generateId() {
        final String createStamp = String.valueOf(System.currentTimeMillis());
        return Integer.parseInt(createStamp.substring(createStamp.length() - 8));  //15868 94042329
    }

    //@Override
    public void setName(final String name) {
        this.name = name;
        setDisplayName(name);
    }

    public void setDisplayName(final String name) {
//Bukkit.broadcastMessage("setDisplayName  "+name+" entity==null?"+(entity==null));        
        if (entity == null || entity.isDead()) {
            lastName = name;
            return;
        }
        if (name.isEmpty()) {
            entity.setCustomNameVisible(false);
        } else {
            entity.customName(TCUtil.form(name));
            entity.setCustomNameVisible(true);
        }
    }


    public boolean isInSameBlock(final Location loc) {
        return loc.getBlockX() == x && (loc.getBlockY() == y || loc.getBlockY() == y + 1) && loc.getBlockZ() == z;
    }


    public void setType(final FigureType type, final Game game) {
//System.out.println("********* setType "+type+" : "+game);
        this.type = type;
        this.game = game;
//System.out.println("********* setType "+type+" : "+game);
        if (type == FigureType.SERVER && game != null) {
            name = game.displayName;
        }
        Bukkit.getPluginManager().callEvent(new FigureActivateEntityEvent(this));
    }


    public Location getSpawnLocation() {
        if (spawnLoc == null) {
            final World w = Bukkit.getWorld(worldName);
            if (w != null) {
                spawnLoc = new Location(w, x + 0.5, y, z + 0.5);
                spawnLoc.setYaw(yaw);
                spawnLoc.setPitch(pitch);
            }
        }

        final Block b = spawnLoc.getBlock();
        if (b.isPassable()) spawnLoc.setY(spawnLoc.getBlockY());
        else spawnLoc.setY(spawnLoc.getBlockY() + b.getBoundingBox().getHeight());
        return spawnLoc;
    }


    public FigureType getType() {
        return type;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public Entity getEntity() {
        return entity;
    }

    public int getId() {
        return figureId;
    }

    public void setTag(final String tag) {
        this.tag = tag;
        Bukkit.getPluginManager().callEvent(new FigureActivateEntityEvent(this));
    }

    public void teleport(final Location to) {
//System.out.println("figure teleport entity="+entity+" to="+to);
        if (entity != null) {
            allowTp = true;
            entity.teleport(to);
            allowTp = false;
        }
    }

    public void name(Component string) {
    }


    public static enum FigureType {
        COMMAND,// ("command"), 
        COMMAND_CONFIRM,// ("command"), 
        EVENT,// ("event"), 
        SERVER,// ("server"),
        ;

        public static FigureType fromString(final String s) {
            if (s == null || s.isEmpty()) return COMMAND;
            for (FigureType t : values()) {
                if (s.equalsIgnoreCase(t.name())) return t;
            }
            return COMMAND;
        }


    }
}

