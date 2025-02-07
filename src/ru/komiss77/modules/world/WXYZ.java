package ru.komiss77.modules.world;

import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.NumUtil;

public class WXYZ extends XYZ {

    @Deprecated
    @SuppressWarnings("не хранить мир - после отгрузки остаётся ghost!!! use world()")
    public final World w; //не хранить мир - после отгрузки остаётся ghost!!! use world()

    //private WeakReference<World> world; //не хранить мир - после отгрузки остаётся ghost!!! use world()
    //зачем такие навороты? лишний объект, проверки.. Всегда проще сделать Bukkit.getWorld и получить самый актуальный мир из ядра.
    //посмотри исходник, там всего одно действие - берётся мир по имени из мапы (кст,именно по этому нужно юзать название а не uuid)
    //к тому же WeakReference хоть и слабая, но ссылка, т.е. до прохода GC она всё равно выдаст старый мир который уже мог быть отгружен
    public @Nullable World w() {
        return Bukkit.getWorld(worldName);
        //final World wl = this.world.get();
        //if (wl != null) return wl;
        //final World nw = Bukkit.getWorld(worldName);
        //if (nw != null) world = new WeakReference<>(nw);
        //return nw;
    }

    public @Nullable World world() { //не успел моргнуть, а этот метод куда-то пропал, плагин выдал ошибку. всё, НИЧЕГО не переименовываем,не удаляем, не перемещаем!!!!!
        return Bukkit.getWorld(worldName);
    }
    public void w(final World w) {
        //this.world = new WeakReference<>(w);
        this.worldName = w.getName();
    }

    public WXYZ(final Block b) {
        this.x = b.getX();
        this.y = b.getY();
        this.z = b.getZ();
        this.w = b.getWorld();
        //this.world = new WeakReference<>(b.getWorld());
        this.pitch = 0;
        this.yaw = 0;
        this.worldName = w.getName();
    }

    public WXYZ(final Block b, final int pt) {
        this.x = b.getX();
        this.y = b.getY();
        this.z = b.getZ();
        this.w = b.getWorld();
        //this.world = new WeakReference<>(b.getWorld());
        this.worldName = w.getName();
        this.pitch = pt;
        this.yaw = 0;
    }

    public WXYZ(final Block b, final int pt, final int yw) {
        this.x = b.getX();
        this.y = b.getY();
        this.z = b.getZ();
        this.w = b.getWorld();
        //this.world = new WeakReference<>(b.getWorld());
        this.worldName = w.getName();
        this.pitch = pt;
        this.yaw = yw;
    }

    public WXYZ(final Location loc) {
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.w = loc.getWorld();
        //this.world = new WeakReference<>(loc.getWorld());
        this.worldName = w.getName();
        this.pitch = 0;
        this.yaw = 0;
    }

    public WXYZ(final Location loc, final boolean dir) {
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.w = loc.getWorld();
        //this.world = new WeakReference<>(loc.getWorld());
        this.worldName = w.getName();
        this.pitch = dir ? (int) loc.getPitch() : 0;
        this.yaw = dir ? (int) loc.getYaw() : 0;
    }

    public WXYZ(final World w, final Vector loc) {
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.w = w;
        //this.world = new WeakReference<>(w);
        this.worldName = w.getName();
        this.pitch = 0;
        this.yaw = 0;
    }

    public WXYZ(final XYZ p) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
        this.w = Bukkit.getWorld(p.worldName);
        //this.world = new WeakReference<>(Bukkit.getWorld(p.worldName));
        this.worldName = p.worldName;
        this.pitch = p.pitch;
        this.yaw = p.yaw;
    }

    public WXYZ(final World w, final XYZ p) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
        this.w = w;
        //this.world = new WeakReference<>(w);
        this.worldName = w.getName();
        this.pitch = p.pitch;
        this.yaw = p.yaw;
    }

    public WXYZ(final World w, final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        //this.world = new WeakReference<>(w);
        this.worldName = w.getName();
        this.pitch = 0;
        this.yaw = 0;
    }

    public WXYZ(final World w, final int x, final int y, final int z, final int pt) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        //this.world = new WeakReference<>(w);
        this.worldName = w.getName();
        this.pitch = pt;
        this.yaw = 0;
    }

    public WXYZ(final World w, final int x, final int y, final int z, final int pt, final int yw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        //this.world = new WeakReference<>(w);
        this.worldName = w.getName();
        this.pitch = pt;
        this.yaw = yw;
    }

    public Block getBlock() {
        return this.w.getBlockAt(x, y, z);
    }

    @Override
    public Location getCenterLoc() {
        return getCenterLoc(w);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof final WXYZ comp)) return false;
        return ClassUtil.equal(comp.w(), w(), World::getUID)
            && comp.x == x && comp.y == y && comp.z == z;
    }

    @Override
    public WXYZ add(final int x, final int y, final int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    @Override
    public WXYZ add(final XYZ val) {
        return add(val.x, val.y, val.z);
    }

    @Override
    public WXYZ times(final int m) {
        this.x *= m;
        this.y *= m;
        this.z *= m;
        return this;
    }

    @Override
    public WXYZ clone() {
        final XYZ c = super.clone();
        return new WXYZ(w, c);
    }

    public int dist2DSq(final WXYZ at) {
        return NumUtil.square(at.x - x) + NumUtil.square(at.z - z);
    }
}
