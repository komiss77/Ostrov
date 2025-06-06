package ru.komiss77.modules.world;

import java.util.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.Schematic.Rotate;
import ru.komiss77.utils.BlockUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.version.Nms;

//https://gist.github.com/ursinn/871525236408e33d4cbee607f7eff8ae
public class Cuboid {

    public int minX = 0;
    public int minY = 0; //не меняем! чтобы стирало с самого низа
    public int minZ = 0;
    public int maxX = 0;
    public int maxY = 0;
    public int maxZ = 0;
    public int spawnAddX, spawnAddY, spawnAddZ, spawnYaw, spawnPitch; //смещения от угла min, типа точка спавна

    //для лобби и кланов
    public int id;
    public String name;
    public String displayName;
    public Location spawnPoint;
    public Set<String> playerNames = new HashSet<>();
    //private final CuboidInfo info;

    public Cuboid(final String fromString) { // x1 + ", " + y1 + ", " + z1 + ", " + x2 + ", " + y2 + ", " + z2;
        String[] data = fromString.split(", ");
        if (data.length >= 6) {
            minX = NumUtil.intOf(data[0], 0);
            minY = NumUtil.intOf(data[1], 0);
            minZ = NumUtil.intOf(data[2], 0);
            maxX = NumUtil.intOf(data[3], 0);
            maxY = NumUtil.intOf(data[4], 0);
            maxZ = NumUtil.intOf(data[5], 0);
        }
        if (data.length >= 11) {
            spawnAddX = NumUtil.intOf(data[6], 0);
            spawnAddY = NumUtil.intOf(data[7], 0);
            spawnAddZ = NumUtil.intOf(data[8], 0);
            spawnYaw = NumUtil.intOf(data[9], 0);
            spawnPitch = NumUtil.intOf(data[10], 0);
        }
    }

    @Deprecated
    public Cuboid(final XYZ min, final int sizeX, final int sizeY, final int sizeZ) { //кубоид по локации и размерам
        minX = min.x;
        minY = min.y;
        minZ = min.z;
        maxX = minX + sizeX;
        maxY = minY + sizeY;
        maxZ = minZ + sizeZ;
    }
    public Cuboid(final BVec min, final int sizeX, final int sizeY, final int sizeZ) { //кубоид по локации и размерам
        minX = min.x;
        minY = min.y;
        minZ = min.z;
        maxX = minX + sizeX;
        maxY = minY + sizeY;
        maxZ = minZ + sizeZ;
    }

    public Cuboid(final Location min, final int sizeX, final int sizeY, final int sizeZ) { //кубоид по локации и размерам
        minX = min.getBlockX();
        minY = min.getBlockY();
        minZ = min.getBlockZ();
        maxX = minX + sizeX;
        maxY = minY + sizeY;
        maxZ = minZ + sizeZ;
    }

    public Cuboid(final int sizeX, final int sizeY, final int sizeZ) { //создание кубоида, не привязанного к локации (от нуля по размерам)
        //minX,minY,minZ будут нули, спавн ставится в центре
        maxX = sizeX;
        maxY = sizeY;
        maxZ = sizeZ;
        spawnAddX = maxX / 2;
        spawnAddY = maxY / 2;
        spawnAddZ = maxZ / 2;
    }

    //кубоид между двумя локациями
    public Cuboid(final Location pos1, final Location pos2) {
        minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    //кубоид между двумя XYZ
    @Deprecated
    public Cuboid(final XYZ min, final XYZ max) {
        this(min, max, null);
    }

    //кубоид между двумя локациями + спавн
    @Deprecated
    public Cuboid(final XYZ min, final XYZ max, final Location spawn) {
        minX = Math.min(min.x, max.x);
        minY = Math.min(min.y, max.y);
        minZ = Math.min(min.z, max.z);
        maxX = Math.max(min.x, max.x);
        maxY = Math.max(min.y, max.y);
        maxZ = Math.max(min.z, max.z);
        if (spawn == null) return;
        spawnAddX = Math.abs(spawn.getBlockX() - minX);
        spawnAddY = Math.abs(spawn.getBlockY() - minY);
        spawnAddZ = Math.abs(spawn.getBlockZ() - minZ);
        spawnYaw = (int) spawn.getYaw();
        spawnPitch = (int) spawn.getPitch();
        if (spawnAddX < 0 || spawnAddX > (maxX - minX)) {
            spawnAddX = 0;
        }
        if (spawnAddY < 0 || spawnAddY > (maxY - minY)) {
            spawnAddY = 0;
        }
        if (spawnAddZ < 0 || spawnAddZ > (maxZ - minZ)) {
            spawnAddZ = 0;
        }
    }

    public Cuboid(final BVec min, final BVec max) {
        this(min, max, null);
    }

    public Cuboid(final BVec min, final BVec max, final Location spawn) {
        minX = Math.min(min.x, max.x);
        minY = Math.min(min.y, max.y);
        minZ = Math.min(min.z, max.z);
        maxX = Math.max(min.x, max.x);
        maxY = Math.max(min.y, max.y);
        maxZ = Math.max(min.z, max.z);
        if (spawn == null) return;
        spawnAddX = Math.abs(spawn.getBlockX() - minX);
        spawnAddY = Math.abs(spawn.getBlockY() - minY);
        spawnAddZ = Math.abs(spawn.getBlockZ() - minZ);
        spawnYaw = (int) spawn.getYaw();
        spawnPitch = (int) spawn.getPitch();
        if (spawnAddX < 0 || spawnAddX > (maxX - minX)) {
            spawnAddX = 0;
        }
        if (spawnAddY < 0 || spawnAddY > (maxY - minY)) {
            spawnAddY = 0;
        }
        if (spawnAddZ < 0 || spawnAddZ > (maxZ - minZ)) {
            spawnAddZ = 0;
        }
    }

    //создать кубоид по схематику
    public Cuboid(final Schematic schem) {
        //minX,minY,minZ будут нули
        maxX = schem.getSizeX() - 1;
        maxY = schem.getSizeY() - 1;
        maxZ = schem.getSizeZ() - 1;
        spawnAddX = schem.getSpawnAddX();
        spawnAddY = schem.getSpawnAddY();
        spawnAddZ = schem.getSpawnAddZ();
        spawnYaw = schem.getSpawnYaw();
        spawnPitch = schem.getSpawnPitch();
    }

    //переместить pos1 на локацию с сохранением размеров
    @Deprecated
    public Cuboid setMinPos(final XYZ min) {
        final int dX = maxX - minX;
        final int dY = maxY - minY;
        final int dZ = maxZ - minZ;
        minX = min.x;
        minY = min.y;
        minZ = min.z;
        maxX = minX + dX;
        maxY = minY + dY;
        maxZ = minZ + dZ;
        return this;
    }

    public Cuboid minPos(final BVec min) {
        final int dX = maxX - minX;
        final int dY = maxY - minY;
        final int dZ = maxZ - minZ;
        minX = min.x;
        minY = min.y;
        minZ = min.z;
        maxX = minX + dX;
        maxY = minY + dY;
        maxZ = minZ + dZ;
        return this;
    }

    @Deprecated
    public Cuboid setMaxPos(final XYZ max) { //переместить кубоид с совмещением точки спавна
        final int spX = minX + spawnAddX;
        final int spY = minY + spawnAddY;
        final int spZ = minZ + spawnAddZ;
        minX = Math.min(minX, max.x);
        minY = Math.min(minY, max.y);
        minZ = Math.min(minZ, max.z);
        maxX = Math.max(minX, max.x);
        maxY = Math.max(minY, max.y);
        maxZ = Math.max(minZ, max.z);

        if (spX > maxX) {
            spawnAddX = maxX - minX;
        }
        if (spY > maxY) {
            spawnAddY = maxY - minY;
        }
        if (spZ > maxZ) {
            spawnAddZ = maxZ - minZ;
        }
        return this;
    }

    public Cuboid maxPos(final BVec max) {
        final int spX = minX + spawnAddX;
        final int spY = minY + spawnAddY;
        final int spZ = minZ + spawnAddZ;
        minX = Math.min(minX, max.x);
        minY = Math.min(minY, max.y);
        minZ = Math.min(minZ, max.z);
        maxX = Math.max(minX, max.x);
        maxY = Math.max(minY, max.y);
        maxZ = Math.max(minZ, max.z);

        if (spX > maxX) {
            spawnAddX = maxX - minX;
        }
        if (spY > maxY) {
            spawnAddY = maxY - minY;
        }
        if (spZ > maxZ) {
            spawnAddZ = maxZ - minZ;
        }
        return this;
    }

    @Deprecated
    public Cuboid allign(final XYZ spawn) { //переместить кубоид с совмещением точки спавна
        final int dX = maxX - minX;
        final int dY = maxY - minY;
        final int dZ = maxZ - minZ;
        //вычисляем расстояния от угла до точки спавна
        minX = spawn.x - spawnAddX;
        minY = spawn.y - spawnAddY;
        minZ = spawn.z - spawnAddZ;
        maxX = minX + dX;
        maxY = minY + dY;
        maxZ = minZ + dZ;
        return this;
    }

    public Cuboid allign(final BVec spawn) { //переместить кубоид с совмещением точки спавна
        final int dX = maxX - minX;
        final int dY = maxY - minY;
        final int dZ = maxZ - minZ;
        //вычисляем расстояния от угла до точки спавна
        minX = spawn.x - spawnAddX;
        minY = spawn.y - spawnAddY;
        minZ = spawn.z - spawnAddZ;
        maxX = minX + dX;
        maxY = minY + dY;
        maxZ = minZ + dZ;
        return this;
    }

    public void allign(final Location spawn) { //переместить кубоид с совмещением точки спавна
        final int dX = maxX - minX;
        final int dY = maxY - minY;
        final int dZ = maxZ - minZ;
        //вычисляем расстояния от угла до точки спавна
        minX = spawn.getBlockX() - spawnAddX;
        minY = spawn.getBlockY() - spawnAddY;
        minZ = spawn.getBlockZ() - spawnAddZ;
        maxX = minX + dX;
        maxY = minY + dY;
        maxZ = minZ + dZ;
    }

    public Cuboid sizeX(final int sizeX) {
        if (spawnAddX > sizeX) {
            spawnAddX = sizeX;
        }
        maxX = minX + sizeX;
        return this;
    }

    public Cuboid sizeY(final int sizeY) {
        if (spawnAddY > sizeY) {
            spawnAddY = sizeY;
        }
        maxY = minY + sizeY;
        return this;
    }

    public Cuboid sizeZ(final int sizeZ) {
        if (spawnAddZ > sizeZ) {
            spawnAddZ = sizeZ;
        }
        maxZ = minZ + sizeZ;
        return this;
    }

    @Deprecated
    public Cuboid setSpawn(final WXYZ spawn, final boolean relative) {
        if (relative) {
            spawnAddX = Math.max(0, Math.min(maxX - minX, spawn.x));
            spawnAddY = Math.max(0, Math.min(maxY - minY, spawn.y));
            spawnAddZ = Math.max(0, Math.min(maxZ - minZ, spawn.z));
        } else {
            spawnAddX = Math.max(minX, Math.min(maxX, spawn.x));
            spawnAddY = Math.max(minY, Math.min(maxY, spawn.y));
            spawnAddZ = Math.max(minZ, Math.min(maxZ, spawn.z));
        }
        spawnPoint = new Location(spawn.w, minX + spawnAddX,
            minY + spawnAddY, minZ + spawnAddZ, spawnYaw, spawnPitch);
        return this;
    }

    public Cuboid spawn(final BVec spawn, final boolean relative) {
        if (relative) {
            spawnAddX = Math.max(0, Math.min(maxX - minX, spawn.x));
            spawnAddY = Math.max(0, Math.min(maxY - minY, spawn.y));
            spawnAddZ = Math.max(0, Math.min(maxZ - minZ, spawn.z));
        } else {
            spawnAddX = Math.max(minX, Math.min(maxX, spawn.x));
            spawnAddY = Math.max(minY, Math.min(maxY, spawn.y));
            spawnAddZ = Math.max(minZ, Math.min(maxZ, spawn.z));
        }
        spawnPoint = new Location(spawn.w(), minX + spawnAddX,
            minY + spawnAddY, minZ + spawnAddZ, spawnYaw, spawnPitch);
        return this;
    }

    public Cuboid setSpawn(final Location spawn, final boolean relative) {
        if (relative) {
            spawnAddX = Math.max(0, Math.min(maxX - minX, spawn.getBlockX()));
            spawnAddY = Math.max(0, Math.min(maxY - minY, spawn.getBlockY()));
            spawnAddZ = Math.max(0, Math.min(maxZ - minZ, spawn.getBlockZ()));
        } else {
            spawnAddX = Math.max(0, Math.min(maxX, spawn.getBlockX()) - minX);
            spawnAddY = Math.max(0, Math.min(maxY, spawn.getBlockY()) - minY);
            spawnAddZ = Math.max(0, Math.min(maxZ, spawn.getBlockZ()) - minZ);
        }
        spawnYaw = (int) spawn.getYaw();
        spawnPitch = (int) spawn.getPitch();
        spawnPoint = new Location(spawn.getWorld(), minX + spawnAddX,
            minY + spawnAddY, minZ + spawnAddZ, spawnYaw, spawnPitch);
        return this;
    }

    @Deprecated
    public boolean contains(final XYZ loc) {
        return contains(loc.x, loc.y, loc.z);
    }

    public boolean contains(final BVec loc) {
        return contains(loc.x, loc.y, loc.z);
    }

    public boolean contains(final Location loc) {
        return contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean contains(final int x, final int y, final int z) {
//Ostrov.log("Cuboid contains "+location);
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean contains(final Cuboid other) {
        return contains(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    private boolean contains(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
        return this.minX <= minX && this.maxX >= maxX && this.minY <= minY && this.maxY >= maxY && this.minZ <= minZ && this.maxZ >= maxZ;
    }

    public boolean overlaps(final Cuboid other) {
        //overlaps(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
        return overlaps(other.minX(), other.minY(), other.minZ(), other.maxX(), other.maxY(), other.maxZ());
    }

    private boolean overlaps(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
        //return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
    }

    public int minY() {
        return minY;
    }

    public int minX() {
        return minX;
    }

    public int minZ() {
        return minZ;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }

    public int maxZ() {
        return maxZ;
    }

    public int sizeX() {
        return maxX - minX + 1;
    }

    public int sizeY() {
        return maxY - minY + 1;
    }

    public int sizeZ() {
        return maxZ - minZ + 1;
    }

    @Deprecated
    public double size() {
        return sizeX() * sizeY() * sizeZ();
    }

    public int volume() {
        return sizeX() * sizeY() * sizeZ();
    }

    public Location getCenter(final Location current) {//??????? че за метод
        int x = (minX + maxX) >> 1;
        int z = (minZ + maxZ) >> 1;
        if (x > 0) {
            x += 1;
        }
        if (z > 0) {
            z += 1;
        }
        int y = (minY + maxY) >> 1;
        final int yTop = BlockUtil.getHighestBlock(current.getWorld(), x, z).getY();
        if (y < yTop) {
            y = yTop;
        }
        if (y < current.getBlockY()) {
            y = current.getBlockY();
        }
        return new Location(current.getWorld(), x + 0.5, y, z + 0.5);
    }

    public Location getRandomLocation(final World world) {
        final Location location = new Location(world, minX + Ostrov.random.nextInt(maxX - minX + 1), minY + Ostrov.random.nextInt(maxY - minY + 1), minZ + Ostrov.random.nextInt(maxZ - minZ + 1));
        return location.getBlock().getType().isAir() ? location : world.getHighestBlockAt(location).getLocation();
    }

    public Location getLowerLocation(final World world) {
        return new Location(world, minX + .5, minY, minZ + .5);//return world.getBlockAt( minX, minY, minZ).getLocation();
    }

    public Location getHightesLocation(final World world) {
        return new Location(world, maxX + .5, maxY, maxZ + .5);//return world.getBlockAt( maxX, maxY, maxZ).getLocation();
    }

    public Location getSpawnLocation(final World world) { //спавн должен быть в блоке,
//Ostrov.log("---getSpawnLocation minY="+minY+" spawnAddY="+spawnAddY);
        //Ostrov.log_warn(toString());
        return new Location(world, minX + spawnAddX, minY + spawnAddY, minZ + spawnAddZ);//world.getBlockAt(minX + spawnAddX, minY + spawnAddY, minZ + spawnAddZ).getLocation();
    }

    public void setBiome(final World world, final Biome biome) {
        for (int x = minX; x <= maxX; x += 4) {
            for (int y = minY; y <= maxY; y += 4) {
                for (int z = minZ; z <= maxZ; z += 4) {
                    world.getBlockAt(x, y, z).setBiome(biome);
                }
            }
        }
        //XYZ xyz;
        //final Iterator <XYZ> it = iteratorXYZ(Schematic.Rotate.r0);
        //while (it.hasNext()) {
        //    xyz = it.next();
        //    if (xyz.x%4==0 && xyz.y%4==0 && xyz.z%4==0) {
        //        world.getBlockAt(xyz.x, xyz.y, xyz.z).setBiome(biome);
        //    }
        //}
        final Set<Chunk> chunks = getChunks(world);
        for (Player p : getPlayers(world)) {
            if (contains(p.getLocation())) {
                for (Chunk c : chunks) {
                    Nms.sendChunkChange(p, c);
                }
            }
        }

    }

    public Set<Chunk> getChunks(final World world) {
        final Set<Chunk> res = new HashSet<>();
        int x1 = minX() & ~0xf;
        int x2 = maxX() & ~0xf;
        int z1 = minZ() & ~0xf;
        int z2 = maxZ() & ~0xf;
        int cx, cz;
        for (int x = x1; x <= x2; x += 16) {
            for (int z = z1; z <= z2; z += 16) {
                cx = x >> 4;
                cz = z >> 4;
                //fix [Paper Watchdog Thread/ERROR] Chunk wait task info below:  [ChunkTaskScheduler] Chunk wait: [( 60,63) in 'world2']
                // NewChunkHolder{ currentChunkStatus=INACCESSIBLE, pendingChunkStatus=INACCESSIBLE, is_unload_safe=ticket_level, killed=false
                if (world.isChunkLoaded(cx, cz)) { // ? world.isChunkGenerated(cx, cz)
                    res.add(world.getChunkAt(cx, cz));
                }
            }
        }
        return res;
    }

    public List<Player> getPlayers(final World world) {
        final List<Player> list = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (contains(p.getLocation())) {
                list.add(p);
            }
        }
        return list;
    }

    public Cuboid rotate(final Rotate rotate) { //повернуть вокруг точки спавна
        if (rotate == Rotate.r0) {
            return this;
        }

        final int spawnX = minX + spawnAddX; //точки спавна - должны остаться те же
        final int spawnZ = minZ + spawnAddZ;
        final int dX = maxX - minX;//final int sizeX = getSizeX();
        final int dZ = maxZ - minZ;//final int sizeZ = getSizeZ();
        final int sX = spawnAddX;
        final int sZ = spawnAddZ;
//Ostrov.log("--rotate1 "+rotate+" min="+minX+","+minZ+" max="+maxX+","+maxZ+" spawn="+spawnX+","+spawnZ+" dX="+dX+"*"+dZ+" spawnAdd="+sX+","+sZ);     

        //не переделывать! синхронизировано с iteratorXYZ!
        //            z+
        //            |
        //            |
        //            |
        // x+ --------|-------- x-
        //            |
        //            |
        //            |
        //            z-
        switch (rotate) {
            case r90 -> {
                maxX = spawnX + spawnAddZ;// - sizeZ;
                minX = maxX - dZ;
                minZ = spawnZ - spawnAddX;
                maxZ = minZ + dX;
                spawnAddX = dZ - sZ;
                spawnAddZ = sX;
                //spawnYaw = spawnYaw+90>180 ? spawnYaw+90 : 180-spawnYaw;
            }
            case r180 -> {
                minX = spawnX - (dX - spawnAddX);
                maxX = minX + dX;
                minZ = spawnZ - (dZ - spawnAddZ);
                maxZ = minZ + dZ;
                spawnAddX = dX - sX;
                spawnAddZ = dZ - sZ;
            }
            case r270 -> {
                minX = spawnX - spawnAddZ;
                maxX = minX + dZ;
                maxZ = spawnZ + spawnAddX;// - sizeX;
                minZ = maxZ - dX;
                spawnAddX = sZ;
                spawnAddZ = dX - sX;
            }
        }
        spawnYaw += rotate.degree;
        if (spawnYaw > 180) {
            spawnYaw -= 360;
        }
        return this;
    }

    @Deprecated
    public Iterator<XYZ> iteratorXYZ(final Rotate rotate) {
        return new CuboidIteratorXYZ(rotate);
    }

    @Deprecated
    public Iterator<XYZ> schematicIterator(final Schematic schematic, final Rotate rotate) {
        return new CuboidIteratorXYZ(schematic, rotate);
    }

    //делать стенки
    @Deprecated
    public Set<XYZ> getBorder() {
        final Set<XYZ> border = new HashSet<>();
        //вертикальные линии
        for (int y = minY; y <= maxY + 1; y++) {
            //углы по оси y
            border.add(xyz(minX, y, minZ, 0, 1));
            border.add(xyz(maxX + 1, y, minZ, 0, 2));
            border.add(xyz(minX, y, maxZ + 1, 0, 3));
            border.add(xyz(maxX + 1, y, maxZ + 1, 0, 4));
            //стеночки с интервалом
            if (y % 3 == 0 && y > minY && y < maxY) {
                for (int x = minX + 3; x < maxX; x += 3) {
                    border.add(xyz(x, y, minZ, 0, 5));
                    border.add(xyz(x, y, maxZ + 1, 0, 6));
                }
                for (int z = minZ + 3; z < maxZ; z += 3) {
                    border.add(xyz(minX, y, z, 0, 7));
                    border.add(xyz(maxX + 1, y, z, 0, 8));
                }
            }

        }
        //линии по оси Х
        for (int x = minX; x <= maxX + 1; x += 3) {
            border.add(xyz(x, minY, minZ, 1, 0));
            border.add(xyz(x, maxY + 1, minZ, 1, 1));
            border.add(xyz(x, minY, maxZ + 1, 1, 2));
            border.add(xyz(x, maxY + 1, maxZ + 1, 1, 3));
        }
        //линии по оси Z
        for (int z = minZ; z <= maxZ + 1; z += 3) {
            border.add(xyz(minX, minY, z, 2, 0));
            border.add(xyz(minX, maxY + 1, z, 2, 1));
            border.add(xyz(maxX + 1, minY, z, 2, 2));
            border.add(xyz(maxX + 1, maxY + 1, z, 2, 3));
        }
        return border;
    }

    @Deprecated
    private XYZ xyz(final int x, final int y, final int z, final int yaw, final int pitch) {
        final XYZ xyz = new XYZ(null, x, y, z);
        xyz.yaw = yaw;
        xyz.pitch = pitch;
        return xyz;
    }

    //отдаст последовательность XYZ с учётом поворота, не меняя оригинал
    @Deprecated
    public class CuboidIteratorXYZ implements Iterator<XYZ> {

        public final XYZ xyz = new XYZ();
        private final Rotate rotate;
        private final int x_min = minX, y_min = minY, z_min = minZ; //начальная позиция
        private final int x_max = maxX, z_max = maxZ; //начальная позиция
        private final int itX, itY, itZ; //размеры по осям +1 блок (типа включительно)
        private int a, b, c; //счетчики по осям, от 0 до размера по оси
        private int count; //счётчик блоков для отпеделения hasNext()
        private final int size;

        public CuboidIteratorXYZ(final Rotate rotate) {
            this.rotate = rotate;
            itX = sizeX();
            itY = sizeY();
            itZ = sizeZ();
            size = itX * itY * itZ;
        }

        public CuboidIteratorXYZ(final Schematic schematic, final Rotate rotate) {
            //отдать последоватьельность блоков такую же, как в оригинальном схематике без поворотов
            this.rotate = rotate;
            itX = schematic.getSizeX();
            itY = schematic.getSizeY();
            itZ = schematic.getSizeZ();
            size = itX * itY * itZ;
        }

        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public XYZ next() {

            //не переделывать! синхронизировано с rotate!
            //            z+
            //            |
            //            |
            //            |
            // x+ --------|-------- x-
            //            |
            //            |
            //            |
            //            z-
            switch (rotate) {

                case r90 -> {
                    xyz.x = x_max - c;
                    xyz.z = z_min + a;//xyz.x = z_min + x;
                }

                case r180 -> {
                    xyz.x = x_max - a;
                    xyz.z = z_max - c;
                }

                case r270 -> {
                    xyz.x = x_min + c;
                    xyz.z = z_max - a;
                }

                default -> {
                    xyz.x = x_min + a;
                    xyz.z = z_min + c;
                }

            }

            xyz.y = y_min + b;

            //(x & 0xF) << 20 | (z & 0xF) << 16 | (y + 16384);
            //xyz.yaw = x<<19 | y<<11 | z; //тут отдаём коорд.блока в кубоиде, всегда начиная с 0,0,0 для одинаковой переборки
            //x = 00xxxxxx xxxx0000 00000000 00000000   лимит 1024
            //y = 00000000 0000yyyy yyyyyy00 00000000   лимит 1024
            //z = 00000000 00000000 000000zz zzzzzzzz   лимит 1024
            xyz.yaw = a << 20 | b << 10 | c; //тут отдаём коорд.блока в кубоиде, всегда начиная с 0,0,0 для одинаковой переборки
            //внимание! используются счётчики a,b,c, не из xyz!!

            if (++a >= itX) {
                a = 0;
                if (++c >= itZ) {
                    c = 0;
                    ++b;
                }
            }

            count++;
            return xyz;
        }

        @Override
        public void remove() {
        }
    }

    public Set<Block> getBlocks(final World world) {
        final Set<Block> set = new HashSet<>();
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    set.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return set;
    }

    public Iterator<Block> iterator(final World world) {
        return new CuboidIterator(world);
    }

    public class CuboidIterator implements Iterator<Block> {

        private final World world;
        private final int baseX = minX;
        private final int baseY = minY;
        private final int baseZ = minZ;
        private final int itX = sizeX();
        private final int itY = sizeY();
        private final int itZ = sizeZ();
        private final int size = itX * itY * itZ;
        private int x, y, z;
        private int count;

        public CuboidIterator(final World world) {
            this.world = world;
        }

        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public Block next() {

            final Block block = world.getBlockAt(baseX + x, baseY + y, baseZ + z);

            if (++x >= itX) {
                x = 0;
                if (++z >= itZ) {
                    z = 0;
                    ++y;
                }
            }

            count++;
            return block;
        }

        @Override
        public void remove() {
        }
    }

    public Iterator<Location> borderIterator(final World world) {
        return new BorderIterator(world);
    }

    public class BorderIterator implements Iterator<Location> {

        private final World world;
        private final int baseX = minX;
        private final int baseY = minY;
        private final int baseZ = minZ;
        private final int itX = sizeX();
        private final int itY = sizeY();
        private final int itZ = sizeZ();
        private final int size = itX * itY * itZ;
        private int x, y, z;
        private int count;
        private int stage; //0-дно, 1,2,3,4 - стенки, 5-крышка

        //public BorderIterator(final World w, final int baseX, final int baseY, final int baseZ, final int sizeX, final int sizeY, final int sizeZ) {
        public BorderIterator(final World world) {
            this.world = world;
        }

        @Override
        public boolean hasNext() {
//Ostrov.log("hasNext ?"+(x < sizeX && y < sizeY && z < sizeZ));
            return count < size;//return !(x >= itX && y >= itY && z >= itZ);
        }

        @Override
        public Location next() {

            final Location loc = new Location(world, baseX + x, baseY + y, baseZ + z);//world.getBlockAt(baseX + x, baseY + y, baseZ + z).getLocation();

            switch (stage) {

                case 0 -> { //поддон
                    if (++x >= itX) {
                        x = 0;
                        if (++z >= itZ) {
                            z = 0;
                            x = 0;
                            ++y;
                            stage = 1;
                        }
                    }
                }

                case 1 -> {
                    x++;
                    if (x >= itX) {
                        stage = 2;
                    }
                }

                case 2 -> {
                    z++;
                    if (z >= itZ) {
                        stage = 3;
                    }
                }

                case 3 -> {
                    x--;
                    if (x <= 0) {
                        stage = 4;
                    }
                }

                case 4 -> {
                    z--;
                    if (z <= 0) {
                        x = 0;
                        z = 0;
                        y++;
                        if (y >= itY) {
                            stage = 5;
                        } else {
                            stage = 1;
                        }
                    }
                }

                case 5 -> { //крышка
                    if (++x >= itX) {
                        x = 0;
                        if (++z >= itZ) {
                            x = itX;
                            z = itZ;
                            y = itY;
                        }
                    }
                }

            }

            count++;

            return loc;
        }

        @Override
        public void remove() {
        }
    }

    //@Override
    public Cuboid copy() {
        final Cuboid c = new Cuboid(0, 0, 0);
        c.minX = minX;
        c.maxX = maxX;
        c.minY = minY;
        c.maxY = maxY;
        c.minZ = minZ;
        c.maxZ = maxZ;
        c.spawnAddX = spawnAddX;
        c.spawnAddY = spawnAddY;
        c.spawnAddZ = spawnAddZ;
        c.spawnYaw = spawnYaw;
        c.spawnPitch = spawnPitch;
        return c;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(minX).append(", ")
            .append(minY).append(", ")
            .append(minZ).append(", ")
            .append(maxX).append(", ")
            .append(maxY).append(", ")
            .append(maxZ).append(", ")
            .append(spawnAddX).append(", ")
            .append(spawnAddY).append(", ")
            .append(spawnAddZ).append(", ")
            .append(spawnYaw).append(", ")
            .append(spawnPitch);
        return sb.toString();//minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof final Cuboid other)) {
            return false;
        } else {
            //return maxX) != Double.doubleToLongBits(other.maxX) ? false : (maxY) != Double.doubleToLongBits(other.maxY) ? false : (maxZ) != Double.doubleToLongBits(other.maxZ) ? false : (minX) != Double.doubleToLongBits(other.minX) ? false : (minY) != Double.doubleToLongBits(other.minY) ? false : minZ) == Double.doubleToLongBits(other.minZ)))));
            return maxX == other.maxX && (maxY == other.maxY && (maxZ == other.maxZ && (minX == other.minX && (minY == other.minY && minZ == other.minZ))));
        }
    }

    @Override
    public int hashCode() {
        int result = 1;
        long temp = maxX;
        result = 31 * result + Long.hashCode(temp);
        temp = maxY;
        result = 31 * result + Long.hashCode(temp);
        temp = maxZ;
        result = 31 * result + Long.hashCode(temp);
        temp = minX;
        result = 31 * result + Long.hashCode(temp);
        temp = minY;
        result = 31 * result + Long.hashCode(temp);
        temp = minZ;
        result = 31 * result + Long.hashCode(temp);
        return result;
    }

}
