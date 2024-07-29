package ru.komiss77.modules.world;

import org.bukkit.Location;
import org.bukkit.Material;
import ru.komiss77.Ostrov;
import ru.komiss77.notes.ThreadSafe;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.version.Nms;

import java.util.function.Consumer;

public final class LocFinder {

    public static final MatCheck[] DEFAULT_CHECKS = {
            (mat, y) -> LocationUtil.canStand(mat),
            (mat, y) -> LocationUtil.isPassable(mat),
            (mat, y) -> LocationUtil.isPassable(mat)
    };

    private final int minY;
    private final int maxY;
    private final MatCheck[] checks;
    private Material[] mats = new Material[0];
    private WXYZ bloc;

    public static WXYZ findInArea(final WXYZ from, final int radius, final int offset,
                                  final int near, final LocFinder.MatCheck[] checks, final int yDst) {
        final int ofs2 = offset << 1;
        final WXYZ in = new WXYZ(from.w, FastMath.rndCircPos(from, radius)).add(Ostrov.random.nextInt(ofs2) - offset,
                Ostrov.random.nextInt(ofs2) - offset, Ostrov.random.nextInt(ofs2) - offset);
        return new LocFinder(in, checks).find(false, near, yDst);
    }

    public static void onAsyncFind(final WXYZ loc, final MatCheck[] checks,
                                   final boolean down, final int near, final int offsetY, final Consumer<WXYZ> onFind) {
        Ostrov.async(() -> {
            final WXYZ fin = new LocFinder(loc, checks).find(down, near, offsetY);
            if (fin != null) Ostrov.sync(() -> onFind.accept(fin));
        });
    }

    public LocFinder(final WXYZ loc, final MatCheck[] checks) {
        this.checks = checks;
        this.minY = loc.w.getMinHeight();
        this.maxY = loc.w.getMaxHeight();
        this.bloc = loc;
    }

    @ThreadSafe
    public WXYZ find(final boolean down, final int near, final int offsetY) {
        if (checks.length == 0)
            return bloc;
        final WXYZ lc = bloc;
        WXYZ fin = testLoc(down);
        if (fin != null) {
            fin.y += offsetY;
            return fin;
        }
        for (int d = 1; d <= near; d++) {
            int fd = -d - 1;
            for (int dx = d; dx != fd; dx--) {
                for (int dz = d; dz != fd; dz--) {
                    if (dx == d || dz == d || dx == -d || dz == -d) {
                        bloc = lc.clone().add(dx * FastMath.absInt(dx), 0,
                                dz * FastMath.absInt(dz));
                        fin = testLoc(down);
                        if (fin != null) {
                            fin.y += offsetY;
                            return fin;
                        }
                    }
                }
            }
        }
        return null;
    }

    @ThreadSafe
    private WXYZ testLoc(final boolean down) {
        if (bloc == null) return null;
        final Location lc = bloc.getCenterLoc();
        if (!lc.isChunkLoaded()) Ostrov.sync(() -> lc.getChunk().load());

        mats = new Material[maxY - minY];
        if (down) {
            for (int y = bloc.y; y > minY; y--) {
                boolean miss = false;
                for (int i = 0; i != checks.length; i++) {
                    final int finY = y + i;
                    if (checks[i].check(getMat(finY), finY)) continue;
                    miss = true;
                    break;
                }
                if (miss) continue;
                return new WXYZ(bloc.w, bloc.x, y, bloc.z);
            }
            return null;
        } else {
            for (int y = bloc.y; y < maxY; y++) {
                boolean miss = false;
                for (int i = 0; i != checks.length; i++) {
                    final int finY = y + i - checks.length;
                    if (checks[i].check(getMat(finY), finY)) continue;
                    miss = true;
                    break;
                }
                if (miss) continue;
                return new WXYZ(bloc.w, bloc.x, y - checks.length, bloc.z);
            }
            return testLoc(true);
        }
    }

    private Material getMat(final int y) {
        final int slot = y - minY;
        if (slot >= mats.length || slot < 0)
            return Material.AIR;

        final Material mt = mats[slot];
        if (mt == null) {
            mats[slot] = Nms.getFastMat(bloc.w, bloc.x, y, bloc.z);
            return mats[slot];
        }
        return mt;
    }

    public interface MatCheck {
        boolean check(final Material mat, final int y);
    }
}
