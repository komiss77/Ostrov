package ru.komiss77.utils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.notes.Slow;
import ru.komiss77.objects.Duo;
import ru.komiss77.version.Nms;


//не переименовывать! юзают все плагины!
public class LocUtil {

    private static final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private static final BlockFace[] radial = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7].getOppositeFace();
        }
        return axis[Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }


    public static Location stringToLoc(final String loc_string, final boolean autoLoadWorld, final boolean addHalfBlock) {
        if (loc_string == null || loc_string.isEmpty()) {
            return null; //|| loc_string.isEmpty() не надо, сработает ниже
        }
        final String separator;
        //if (loc_string.contains(".")) {
        // return stringToLoc(loc_string, autoLoadWorld, true);
        //} else 
        if (loc_string.contains("<>")) {
            separator = "<>";
        } else if (loc_string.contains(":")) {
            separator = ":";
        } else {
            return null; //stringToLoc(loc_string, autoLoadWorld, true);
        }

        final String[] split = loc_string.split(separator);
        if (split.length < 4 || split.length > 6) {
            Ostrov.log_warn("Декодер локации : длинна массива должна быть от 4 до 6 : " + loc_string);
            return null;
        }

        //if (!Ostrov.isInteger(split[1]) || !Ostrov.isInteger(split[2]) || !Ostrov.isInteger(split[3]) ) {
        //    Ostrov.log_err("Декодер локации : X, Y или Z - не числа : "+loc_string);
        //    return null;
        //}
        try {

            World world = Bukkit.getWorld(split[0]);
            if (world == null) {
                if (!autoLoadWorld) {
                    Ostrov.log_warn("Декодер локации : мир не найден! (autoLoadWorld=false) " + loc_string);
                    return null;
                }
                WorldManager.load(Bukkit.getConsoleSender(), split[0], World.Environment.NORMAL, WorldManager.Generator.Empty);
                world = Bukkit.getWorld(split[0]);
            }
            if (world == null) {
                WorldManager.create(Bukkit.getConsoleSender(), split[0], World.Environment.NORMAL, WorldManager.Generator.Empty, true);
                world = Bukkit.getWorld(split[0]);
            }
            if (world == null) {
                Ostrov.log_err("Декодер локации : Не удалось найти, загрузить или создать мир " + split[0] + " : " + loc_string);
                return null;
            }

            final Location loc = addHalfBlock ? new Location(world, Double.parseDouble(split[1]) + 0.5,
                Double.parseDouble(split[2]) + 0.5, Double.parseDouble(split[3]) + 0.5) :
                new Location(world, Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));

            if (split.length >= 5) {
                try {
                    //if (ApiOstrov.isInteger(split[4]) ) {
                    loc.setYaw(Integer.parseInt(split[4]));
                    //} else {
                    //    Ostrov.log_warn("Декодер локации : yaw - не числo : >"+loc_string+"<");
                    //}
                    if (split.length == 6) {
                        //if (ApiOstrov.isInteger(split[5]) ) {
                        loc.setPitch(Integer.parseInt(split[5]));
                        //} else {
                        //    Ostrov.log_warn("Декодер локации : pitch - не числo : >"+loc_string+"<");
                        // }
                    }
                } catch (NumberFormatException ex) {
                    Ostrov.log_warn("Декодер локации : yaw или pitch - не числo : >" + loc_string + "<");
                }
            }

            return loc;

        } catch (NumberFormatException | NullPointerException ex) {

            Ostrov.log_err("Декодер локации " + loc_string + " -> " + ex.getMessage());
            return null;

        }
    }

    public static String toString(final Location loc) {
        if (loc == null) {
            return ":::";
        }
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public static String toDirString(final Location loc) {
        if (loc == null) {
            return ":::::";
        }
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ() + ":" + (int) loc.getYaw() + ":" + (int) loc.getPitch();
    }

    public static int getDistance(final Location loc1, final Location loc2) {
        return (int) Math.sqrt(getDistanceSquared(loc1, loc2));
    }

    public static int getDistanceSquared(final Location loc1, final Location loc2) {
        if (loc1 == null || loc2 == null || !loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
            return Integer.MAX_VALUE;
        }
        //return ( (int) (Math.pow( loc1.getBlockX()-loc2.getBlockX(), 2) +
        //                    Math.pow( loc1.getBlockY()- loc2.getBlockY(), 2) +
        //                        Math.pow( loc1.getBlockZ()- loc2.getBlockZ(), 2)) );
        return square(loc1.getBlockX() - loc2.getBlockX()) + square(loc1.getBlockY() - loc2.getBlockY()) + square(loc1.getBlockZ() - loc2.getBlockZ());
    }

    private static int square(final int num) {
        return num * num;
    }

    public static Location getNearestPlayer(final Player p) {
        Location loc = p.getLocation();
        int minDistance = Integer.MAX_VALUE;
        for (final Player pl : p.getWorld().getPlayers()) {
            final int dst = getDistance(p.getLocation(), pl.getLocation());
            if (p.getEntityId() != pl.getEntityId() && dst < minDistance) {
                loc = pl.getLocation();
                minDistance = dst;
            }
        }
        return loc;
    }

    public static Player getNearestPlayer(final Player p, final int maxDist) {
        Player find = null;
        int minDistance = Integer.MAX_VALUE;
        for (final Player pl : p.getWorld().getPlayers()) {
            final int dst = getDistance(p.getLocation(), pl.getLocation());
            if (p.getEntityId() != pl.getEntityId() && dst < minDistance && dst < maxDist) {
                find = pl;
                minDistance = dst;
            }
        }
        return find;
    }

    public static Player getNearestPlayer(final Location loc, final int maxDist) {
        Player find = null;
        int minDistance = Integer.MAX_VALUE;
        for (final Player pl : loc.getWorld().getPlayers()) {
            final int dst = getDistance(loc, pl.getLocation());
            if (dst < minDistance && dst < maxDist) {
                find = pl;
                minDistance = dst;
            }
        }
        return find;
    }

    public static Biome biomeFromString(final String biomename) {
        for (Biome b : Ostrov.registries.BIOMES) {
            if (b.key().value().equalsIgnoreCase(biomename)) {
                return b;
            }
        }
        Ostrov.log_warn("biomeFromString - нет биома " + biomename + ", возвращаем PLAINS");
        return Biome.PLAINS;
        //return Registry.BIOME.get(NamespacedKey.minecraft(biomename.toLowerCase())); Invalid key. Must be [a-z0-9/._-]:
    }

    //с методом ниже не кроссить - там плодит локации!
    @Nullable
    public static Location getHighestLoc(final Location loc) {

        //в аду или при генерации как в аду (определяем потолок из бедрока)
        //if (underFeetLoc.getWorld().getEnvironment()==World.Environment.NETHER || (highY>0 && underFeetLoc.getBlock().getType()==Material.BEDROCK)) {
        final int minY = loc.getWorld().getMinHeight();
        //int find_y = highY;
        //find_y-=2; //понижаем на 2
        final int x = loc.getBlockX();
        final int z = loc.getBlockZ();
        int find_y = loc.getWorld().getMaxHeight() - 2;//underFeetLoc.getWorld().getHighestBlockYAt(underFeetLoc.getBlockX(), underFeetLoc.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES);
        Material feetMat;

        for (; find_y > minY; find_y--) {
            feetMat = Nms.getFastMat(loc.getWorld(), x, find_y, z);

            //в аду или при генерации как в аду (определяем потолок из бедрока)
            if ((loc.getWorld().getEnvironment() == World.Environment.NETHER || find_y > 0) && feetMat == Material.BEDROCK) {
                continue;
            }

            //2 блока воды подряд - поверхность моря, не подходит
            if (feetMat == Material.WATER && Nms.getFastMat(loc.getWorld(), x, find_y - 1, z) == Material.WATER) {
                //break;
                return null;
            }

            loc.setY(find_y);
            if (MoveUtil.isSafeLocation(loc)) { //тут будет найден блок в ногах, в конце убавить 1 !!!!
                break;
            }

        }

        if (find_y <= minY) { //в предыдущем цикле блок не нашелся - полная пустота??
            //find_y = loc.getWorld().getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES)+1;
            return null;//underFeetLoc.setY(highY); //ставим высшую точку обратно
        }

        loc.setY(find_y - 1); //ставим найденную точку
//Ostrov.log("getHighestLoc Y="+(find_y-1) );
        return loc;
    }

    @Nullable
    public static Location getHighestLoc(final World w, final int x, final int z) {
        return getHighestLoc(new Location(w, x + 0.5, 0, z + 0.5));
    }

    //годится ли блок для головы?
    @Deprecated
    public static boolean isPassable(final Material mat) {
        return isPassable(mat.asBlockType());
    }

    private static final Set<BlockType> PASS = Set.of(BlockType.BARRIER, BlockType.STRUCTURE_VOID,
        BlockType.CHORUS_PLANT, BlockType.SWEET_BERRY_BUSH, BlockType.BAMBOO, BlockType.VINE,
        BlockType.WEEPING_VINES, BlockType.TWISTING_VINES, BlockType.LADDER, BlockType.LILY_PAD,
        BlockType.CHORUS_FLOWER);
    //годится ли блок для головы?
    public static boolean isPassable(final BlockType tp) {
        if (tp == null) return false;
        if (tp.isAir()) return true;
        if (BlockType.LAVA.equals(tp)) return false;
        else if (PASS.contains(tp)) return true;
        else return !tp.hasCollision();
    }

    @Deprecated
    public static boolean isFeetAllow(Material mat) {
        return isPassable(mat);
    }

    @Deprecated
    public static boolean canStand(Material mat) {
        return canStand(mat.asBlockType());
    }

    @Deprecated
    public static boolean canStand(final BlockType tp) {
        return tp != null && tp.isSolid();
//        return tp != null && !BlockType.LAVA.equals(tp) && tp.isSolid(); хз зач лава, она всегда false на isSolid()
    }

    @Slow(priority = 1)
    public static <G extends Entity> Collection<G> getChEnts(final Location loc, final double dst, final Class<G> ent, final Predicate<G> which) {
        final HashMap<Integer, G> hs = new HashMap<>();
        final double X = loc.getX(), Y = loc.getY(), Z = loc.getZ(), dS = dst * dst;
        final int mnX = (int) (X + dst) >> 4, mnZ = (int) (Z + dst) >> 4;
        final World w = loc.getWorld();
        for (int cx = (int) (X - dst) >> 4; cx <= mnX; cx++) {
            for (int cz = (int) (Z - dst) >> 4; cz <= mnZ; cz++) {
                for (final Entity e : w.getChunkAt(cx, cz).getEntities()) {
                    if (ent.isAssignableFrom(e.getClass())) {
                        final Location el = e.getLocation();
                        if (Math.pow(el.getX() - X, 2d) + Math.pow(el.getY() - Y, 2d) + Math.pow(el.getZ() - Z, 2d) < dS) {
                            final G ge = ent.cast(e);
                            if (which == null || which.test(ge)) {
                                hs.put(e.getEntityId(), ge);
                            }
                        }
                    }
                }
            }
        }
        return hs.values();
    }

    @Slow(priority = 2)
    public static <G extends Entity> G getClsChEnt(final Location loc, final double dst, final Class<G> ent, final Predicate<G> which) {
        final double X = loc.getX(), Y = loc.getY(), Z = loc.getZ();
        final int mnX = (int) (X + dst) >> 4, mnZ = (int) (Z + dst) >> 4;
        final World w = loc.getWorld();

        double dS = dst * dst;
        G fin = null;
        for (int cx = (int) (X - dst) >> 4; cx <= mnX; cx++) {
            for (int cz = (int) (Z - dst) >> 4; cz <= mnZ; cz++) {
                for (final Entity e : w.getChunkAt(cx, cz).getEntities()) {
                    if (ent.isAssignableFrom(e.getClass())) {
                        final Location el = e.getLocation();
                        final double d = Math.pow(el.getX() - X, 2d) + Math.pow(el.getY() - Y, 2d) + Math.pow(el.getZ() - Z, 2d);
                        if (d < dS) {
                            final G ge = ent.cast(e);
                            if (which == null || which.test(ge)) {
                                dS = d;
                                fin = ge;
                            }
                        }
                    }
                }
            }
        }
        return fin;
    }

    @Slow(priority = 1)
    public static <G extends Entity> Collection<G> getChEnts(final WXYZ loc, final int dst, final Class<G> ent, final Predicate<G> which) {
        final HashMap<Integer, G> hs = new HashMap<>();
        final int X = loc.x, Y = loc.y, Z = loc.z, dS = dst * dst;
        final int mnX = (X + dst) >> 4, mnZ = (Z + dst) >> 4;
        for (int cx = (X - dst) >> 4; cx <= mnX; cx++) {
            for (int cz = (Z - dst) >> 4; cz <= mnZ; cz++) {
                for (final Entity e : loc.w.getChunkAt(cx, cz).getEntities()) {
                    if (ent.isAssignableFrom(e.getClass())) {
                        final Location el = e.getLocation();
                        final int dx = el.getBlockX() - X, dy = el.getBlockY() - Y, dz = el.getBlockZ() - Z;
                        if (dx * dx + dy * dy + dz * dz < dS) {
                            final G ge = ent.cast(e);
                            if (which == null || which.test(ge)) {
                                hs.put(e.getEntityId(), ge);
                            }
                        }
                    }
                }
            }
        }
        return hs.values();
    }

    @Slow(priority = 2)
    public static <G extends Entity> G getClsChEnt(final WXYZ loc, final int dst, final Class<G> ent, final Predicate<G> which) {
        final int X = loc.x, Y = loc.y, Z = loc.z;
        final int mnX = (X + dst) >> 4, mnZ = (Z + dst) >> 4;

        int dS = dst * dst;
        G fin = null;
        for (int cx = (X - dst) >> 4; cx <= mnX; cx++) {
            for (int cz = (Z - dst) >> 4; cz <= mnZ; cz++) {
                for (final Entity e : loc.w.getChunkAt(cx, cz).getEntities()) {
                    if (ent.isAssignableFrom(e.getClass())) {
                        final Location el = e.getLocation();
                        final int d = NumUtil.square(el.getBlockX() - X)
                            + NumUtil.square(el.getBlockY() - Y) + NumUtil.square(el.getBlockZ() - Z);
                        if (d < dS) {
                            final G ge = ent.cast(e);
                            if (which == null || which.test(ge)) {
                                dS = d;
                                fin = ge;
                            }
                        }
                    }
                }
            }
        }
        return fin;
    }

    public static TraceResult trace(final Location org, final Vector dir, final double dst, final OnPosData done) {
        dir.normalize();
        final int finX = (int) (dir.getX() * dst + org.getX()), finY = (int) (dir.getY() * dst + org.getY()), finZ = (int) (dir.getZ() * dst + org.getZ());
        int mapX = (int) Math.floor(org.getX()), mapY = (int) Math.floor(org.getY()), mapZ = (int) Math.floor(org.getZ());
        final double deltaDistX = Math.abs(1.0F / dir.getX()), deltaDistY = Math.abs(1.0F / dir.getY()), deltaDistZ = Math.abs(1.0F / dir.getZ());

        final int stepX, stepY, stepZ;
        double sideDistX, sideDistY, sideDistZ;
        if (dir.getX() < 0.0F) {
            stepX = -1;
            sideDistX = (org.getX() - mapX) * deltaDistX;
        } else {
            stepX = 1;
            sideDistX = (1.0F - org.getX() + mapX) * deltaDistX;
        }
        if (dir.getY() < 0.0F) {
            stepY = -1;
            sideDistY = (org.getY() - mapY) * deltaDistY;
        } else {
            stepY = 1;
            sideDistY = (1.0F - org.getY() + mapY) * deltaDistY;
        }
        if (dir.getZ() < 0.0F) {
            stepZ = -1;
            sideDistZ = (org.getZ() - mapZ) * deltaDistZ;
        } else {
            stepZ = 1;
            sideDistZ = (1.0F - org.getZ() + mapZ) * deltaDistZ;
        }

        if (Double.isNaN(sideDistX)) sideDistX = Double.POSITIVE_INFINITY;
        if (Double.isNaN(sideDistY)) sideDistY = Double.POSITIVE_INFINITY;
        if (Double.isNaN(sideDistZ)) sideDistZ = Double.POSITIVE_INFINITY;

        final World w = org.getWorld();
        final List<Duo<BlockPosition, BlockData>> info =
            new ArrayList<>(NumUtil.abs(finX - mapX) + NumUtil.abs(finY - mapY) + NumUtil.abs(finZ - mapZ));
        while (true) {
            if (sideDistZ < sideDistX && sideDistZ < sideDistY) {
                sideDistZ += deltaDistZ;
                mapZ += stepZ;
            } else if (sideDistX < sideDistY) {
                sideDistX += deltaDistX;
                mapX += stepX;
            } else {
                sideDistY += deltaDistY;
                mapY += stepY;
            }

//            w.spawnParticle(Particle.FLAME, org.clone().add(sideDistX, sideDistY, sideDistZ), 2, 0d, 0d, 0d, 0d);
            final BlockData bd = Nms.fastData(w, mapX, mapY, mapZ);
            final BlockPosition bp = Position.block(mapX, mapY, mapZ);
            if (done.test(bp, bd)) {
                return new TraceResult(info, new WXYZ(w, mapX, mapY, mapZ), false);
            }

            info.add(new Duo<>(bp, bd));
            if ((mapX - finX) * stepX > 0 || (mapY - finY) * stepY > 0 || (mapZ - finZ) * stepZ > 0) {
                final BlockPosition lbp = info.getLast().key();
                return new TraceResult(info, new WXYZ(w, lbp.blockX(), lbp.blockY(), lbp.blockZ()), true);
            }
        }
    }

    public interface OnPosData {
        boolean test(final BlockPosition pos, final BlockData data);
    }

    public record TraceResult(List<Duo<BlockPosition, BlockData>> posData, WXYZ last, boolean endDst) {
        @Slow(priority = 2)
        public List<Block> blocks() {
            final World w = last.w;
            return posData.stream().map(bs -> w.getBlockAt(bs.key().toLocation(w))).toList();
        }

        @Slow(priority = 1)
        public boolean has(final BlockPosition pos) {
            for (final Duo<BlockPosition, BlockData> pd : posData)
                if (pd.key().equals(pos)) return true;
            return false;
        }
    }

    public static boolean rayThruAir(final Location org, final Vector to, final double inc) {
        final Vector ch = org.toVector().subtract(to);
        if (ch.lengthSquared() < inc) {
            return true;
        }
        final Vector vec = ch.normalize().multiply(inc);
        final World w = org.getWorld();
        while (true) {
            to.add(vec);
            final BlockType mt = Nms.fastType(w, to.getBlockX(), to.getBlockY(), to.getBlockZ());
            if (BlockType.POWDER_SNOW.equals(mt)) {
                return false;
            }

            if (mt.hasCollision() && mt.isOccluding()) {
                if (w.getBlockAt(to.getBlockX(), to.getBlockY(), to.getBlockZ()).getBoundingBox().contains(to)) {
                    return false;
                }
            }

            if (Math.abs(to.getX() - org.getX()) < inc && Math.abs(to.getY() - org.getY()) < inc && Math.abs(to.getZ() - org.getZ()) < inc) {
                return true;
            }
        }
    }

    @Deprecated
    public static boolean rayThruSoft(final Location org, final Vector to, final double inc) {
        //final Vector tt = to.clone();
        final Vector ch = org.toVector().subtract(to);
        if (ch.lengthSquared() < inc) {
            return true;
        }
        final Vector vec = ch.normalize().multiply(inc);
        final World w = org.getWorld();
        while (true) {
            to.add(vec);
            switch (Nms.getFastMat(w, to.getBlockX(), to.getBlockY(), to.getBlockZ())) {
                default:
                    if (w.getBlockAt(to.getBlockX(), to.getBlockY(), to.getBlockZ()).getBoundingBox().contains(to)) {
                        return false;
                    }
                case OAK_LEAVES, ACACIA_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES,
                     SPRUCE_LEAVES, DARK_OAK_LEAVES, MANGROVE_LEAVES, AZALEA_LEAVES,
                     FLOWERING_AZALEA_LEAVES,

                     GLASS, WHITE_STAINED_GLASS, GLASS_PANE,
                     WHITE_STAINED_GLASS_PANE, DIAMOND_ORE,
                     COAL_ORE, IRON_ORE, EMERALD_ORE,

                     ACACIA_SLAB, BIRCH_SLAB, CRIMSON_SLAB, SPRUCE_SLAB, WARPED_SLAB,
                     DARK_OAK_SLAB, OAK_SLAB, JUNGLE_SLAB, PETRIFIED_OAK_SLAB, MANGROVE_SLAB,

                     ACACIA_STAIRS, BIRCH_STAIRS, CRIMSON_STAIRS, SPRUCE_STAIRS,
                     WARPED_STAIRS, DARK_OAK_STAIRS, OAK_STAIRS, JUNGLE_STAIRS, MANGROVE_STAIRS,

                     ACACIA_PLANKS, BIRCH_PLANKS, CRIMSON_PLANKS, SPRUCE_PLANKS,
                     WARPED_PLANKS, DARK_OAK_PLANKS, OAK_PLANKS, JUNGLE_PLANKS, MANGROVE_PLANKS,

                     ACACIA_TRAPDOOR, BIRCH_TRAPDOOR, CRIMSON_TRAPDOOR, DARK_OAK_TRAPDOOR,
                     JUNGLE_TRAPDOOR, MANGROVE_TRAPDOOR, OAK_TRAPDOOR, SPRUCE_TRAPDOOR, WARPED_TRAPDOOR,

                     ACACIA_WOOD, BIRCH_WOOD, CRIMSON_HYPHAE, SPRUCE_WOOD,
                     WARPED_HYPHAE, DARK_OAK_WOOD, OAK_WOOD, JUNGLE_WOOD, MANGROVE_WOOD,

                     ACACIA_LOG, BIRCH_LOG, CRIMSON_STEM, SPRUCE_LOG,
                     WARPED_STEM, DARK_OAK_LOG, OAK_LOG, JUNGLE_LOG, MANGROVE_LOG,

                     ACACIA_SIGN, ACACIA_WALL_SIGN, BIRCH_SIGN, BIRCH_WALL_SIGN, CRIMSON_SIGN,
                     CRIMSON_WALL_SIGN, SPRUCE_SIGN, SPRUCE_WALL_SIGN, WARPED_SIGN,
                     WARPED_WALL_SIGN, DARK_OAK_SIGN, DARK_OAK_WALL_SIGN, OAK_SIGN,
                     OAK_WALL_SIGN, JUNGLE_SIGN, JUNGLE_WALL_SIGN, MANGROVE_SIGN, MANGROVE_WALL_SIGN,

                     STRIPPED_ACACIA_WOOD, STRIPPED_BIRCH_WOOD, STRIPPED_CRIMSON_HYPHAE, STRIPPED_SPRUCE_WOOD,
                     STRIPPED_WARPED_HYPHAE, STRIPPED_DARK_OAK_WOOD, STRIPPED_OAK_WOOD, STRIPPED_JUNGLE_WOOD,
                     STRIPPED_MANGROVE_WOOD,

                     STRIPPED_ACACIA_LOG, STRIPPED_BIRCH_LOG, STRIPPED_CRIMSON_STEM, STRIPPED_SPRUCE_LOG,
                     STRIPPED_WARPED_STEM, STRIPPED_DARK_OAK_LOG, STRIPPED_OAK_LOG, STRIPPED_JUNGLE_LOG,
                     STRIPPED_MANGROVE_LOG,

                     ACACIA_FENCE, BIRCH_FENCE, CRIMSON_FENCE, SPRUCE_FENCE, WARPED_FENCE, DARK_OAK_FENCE,
                     OAK_FENCE, JUNGLE_FENCE, MANGROVE_FENCE, ACACIA_FENCE_GATE, BIRCH_FENCE_GATE, CRIMSON_FENCE_GATE,
                     SPRUCE_FENCE_GATE, WARPED_FENCE_GATE, DARK_OAK_FENCE_GATE, OAK_FENCE_GATE, JUNGLE_FENCE_GATE,
                     MANGROVE_FENCE_GATE,

                     OAK_DOOR, ACACIA_DOOR, BIRCH_DOOR, CRIMSON_DOOR, DARK_OAK_DOOR,
                     JUNGLE_DOOR, MANGROVE_DOOR, WARPED_DOOR, SPRUCE_DOOR,

                     BARREL, BEEHIVE, BEE_NEST, NOTE_BLOCK, JUKEBOX, CRAFTING_TABLE,

                     AIR, CAVE_AIR, VOID_AIR,

                     SEAGRASS, TALL_SEAGRASS, WEEPING_VINES, TWISTING_VINES,

                     BLACK_CARPET, BLUE_CARPET, BROWN_CARPET, CYAN_CARPET, GRAY_CARPET,
                     GREEN_CARPET, LIGHT_BLUE_CARPET, LIGHT_GRAY_CARPET, LIME_CARPET,
                     MAGENTA_CARPET, MOSS_CARPET, ORANGE_CARPET, PINK_CARPET,
                     PURPLE_CARPET, RED_CARPET, WHITE_CARPET, YELLOW_CARPET,

                     WATER, IRON_BARS, CHAIN, STRUCTURE_VOID, COBWEB, SNOW,
                     POWDER_SNOW, BARRIER, TRIPWIRE, LADDER, RAIL, POWERED_RAIL,
                     DETECTOR_RAIL, ACTIVATOR_RAIL, CAMPFIRE, SOUL_CAMPFIRE:
                    break;
            }

            if (Math.abs(to.getX() - org.getX()) < inc && Math.abs(to.getY() - org.getY()) < inc && Math.abs(to.getZ() - org.getZ()) < inc) {
                /*while (true) {
					tt.add(vec);
					final Block b = w.getBlockAt(tt.getBlockX(), tt.getBlockY(), tt.getBlockZ());
					if (b.getType().isAir()) b.setType(Material.OAK_WOOD, false);
					if (Math.abs(tt.getX() - org.getX()) < inc && Math.abs(tt.getZ() - org.getZ()) < inc) {
						break;
					}
				}*/
                return true;
            }
        }
    }


    //координата в long взято из BlockPosition
    public static long asLong(final Location loc) {
        return (((long) loc.getBlockX() & (long) 67108863) << 38) | (((long) loc.getBlockY() & (long) 4095)) | (((long) loc.getBlockZ() & (long) 67108863) << 12);
    }

    public static int getX(long packedPos) {
        return (int) (packedPos >> 38); // Paper - simplify/inline
    }

    public static int getY(long packedPos) {
        return (int) ((packedPos << 52) >> 52); // Paper - simplify/inline
    }

    public static int getZ(long packedPos) {
        return (int) ((packedPos << 26) >> 38);  // Paper - simplify/inline
    }


    //координата чанка запакованная в int
    public static int cLoc(final Chunk chunk) {
        return cLoc(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static int cLoc(final XYZ xyz) {
        return cLoc(xyz.worldName, xyz.x >> 4, xyz.z >> 4);
    }

    public static int cLoc(final Location loc) {
        return cLoc(loc.getWorld().getName(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public static int cLoc(final String worldName, final int cX, final int cZ) {
        //return worldName.length()<<26 | (cX+4096)<<13 | (cZ+4096);
        return (cX + 4096) << 13 | (cZ + 4096);
    }

    public static Chunk getChunk(final String worldName, final int cLoc) {
        return Bukkit.getWorld(worldName).getChunkAt(getChunkX(cLoc), getChunkZ(cLoc));
    }

    public static int getChunkX(int cLoc) { //len<<26 | (x+4096)<<13 | (z+4096);
        return ((cLoc >> 13 & 0x1FFF) - 4096); //8191 = 1FFF = 0b00000000_00000000_00011111_11111111
    }

    public static int getChunkZ(int cLoc) { //len<<26 | (x+4096)<<13 | (z+4096);
        return ((cLoc & 0x1FFF) - 4096); //8191 = 1FFF = 0b00000000_00000000_00011111_11111111
    }

    public static Set<String> worldNames() {
        final Set<String> list = new HashSet<>();
        for (World w : Bukkit.getWorlds()) {
            list.add(w.getName());
        }
        return list;
    }

}
