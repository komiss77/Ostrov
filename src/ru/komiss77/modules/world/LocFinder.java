package ru.komiss77.modules.world;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import ru.komiss77.Ostrov;
import ru.komiss77.notes.ThreadSafe;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.version.Nms;

public final class LocFinder {

    public static final Check[] DEFAULT_CHECKS = {
        (TypeCheck) (bt, y) -> bt.isSolid(),
        (TypeCheck) (bt, y) -> LocUtil.isPassable(bt),
        (TypeCheck) (bt, y) -> LocUtil.isPassable(bt)
    };

    private final int minY;
    private final int maxY;
    private final Check[] checks;
    private BlockType[] mats = new BlockType[0];
    private BlockData[] datas = new BlockData[0];
    private BVec bVec;

    /*@Deprecated
    public static WXYZ findInArea(final WXYZ from, final int radius, final int offset,
        final int near, final Check[] checks, final int yDst) {
        final int ofs2 = offset << 1;
        final WXYZ in = new WXYZ(from.w(), NumUtil.rndCircPos(from, radius)).add(Ostrov.random.nextInt(ofs2) - offset,
            Ostrov.random.nextInt(ofs2) - offset, Ostrov.random.nextInt(ofs2) - offset);
        return new LocFinder(in, checks).find(DYrect.BOTH, near, yDst);
    }

    @Deprecated
    public static void onAsyncFind(final WXYZ loc, final Check[] checks,
        final DYrect dir, final int near, final int offsetY, final Consumer<WXYZ> onFind) {
        Ostrov.async(() -> {
            final WXYZ fin = new LocFinder(loc, checks).find(dir, near, offsetY);
            if (fin != null) Ostrov.sync(() -> onFind.accept(fin));
        });
    }*/

    public static BVec findInArea(final BVec from, final int radius, final int offset,
        final int near, final Check[] checks, final int yDst) {
        final int ofs2 = offset << 1;
        final BVec in = NumUtil.rndCircPos(from, radius).w(from.w()).add(Ostrov.random.nextInt(ofs2) - offset,
            Ostrov.random.nextInt(ofs2) - offset, Ostrov.random.nextInt(ofs2) - offset);
        return new LocFinder(in, checks).find(DYrect.BOTH, near, yDst);
    }

    public static void onAsyncFind(final BVec loc, final Check[] checks,
        final DYrect dir, final int near, final int offsetY, final Consumer<BVec> onFind) {
        Ostrov.async(() -> {
            final BVec fin = new LocFinder(loc, checks).find(dir, near, offsetY);
            if (fin != null) Ostrov.sync(() -> onFind.accept(fin));
        });
    }


    @Deprecated
    public LocFinder(final WXYZ loc) {
        this(loc, DEFAULT_CHECKS);
    }

    @Deprecated
    public LocFinder(final WXYZ loc, final Check[] checks) {
        this(BVec.of(loc.w(), loc.x, loc.y, loc.z), checks);
    }

    public LocFinder(final BVec loc) {
        this(loc, DEFAULT_CHECKS);
    }

    public LocFinder(final BVec loc, final Check[] checks) {
        this.checks = checks;
        this.minY = loc.w().getMinHeight();
        this.maxY = loc.w().getMaxHeight();
        if (loc.y < minY) loc.y = minY;
        if (loc.y > maxY) loc.y = maxY;
        this.bVec = loc;
    }

    /*@Deprecated
    @ThreadSafe
    public @Nullable WXYZ find(final DYrect dir, final int near, final int offsetY) {
        if (checks.length == 0)
            return bloc;
        final WXYZ lc = bloc;
        WXYZ fin = testLoc(dir);
//Bukkit.broadcast(TCUtil.form(fin + "-f1"));
        if (fin != null) {
            fin.y += offsetY;
            return fin;
        }
        for (int d = 1; d <= near; d++) {
            int fd = -d - 1;
            for (int dx = d; dx != fd; dx--) {
                for (int dz = d; dz != fd; dz--) {
                    if (dx == d || dz == d || dx == -d || dz == -d) {
                        bloc = lc.clone().add(dx * NumUtil.abs(dx),
                            0, dz * NumUtil.abs(dz));
                        fin = testLoc(dir);
                      //Bukkit.broadcast(TCUtil.form(fin + "-f2"));
                        if (fin != null) {
                            fin.y += offsetY;
                            return fin;
                        }
                    }
                }
            }
        }
        return null;
    }*/

    @ThreadSafe
    public @Nullable BVec find(final DYrect dir, final int near, final int offsetY) {
        if (checks.length == 0) return bVec;
        final BVec lc = bVec;
        final World w = lc.w();
        if (w == null) return null;
        BVec fin = testLoc(dir, w);
//Bukkit.broadcast(TCUtil.form(fin + "-f1"));
        if (fin != null) {
            fin.y += offsetY;
            return fin;
        }
        for (int d = 1; d <= near; d++) {
            int fd = -d - 1;
            for (int dx = d; dx != fd; dx--) {
                for (int dz = d; dz != fd; dz--) {
                    if (dx == d || dz == d || dx == -d || dz == -d) {
                        bVec = lc.add(dx * NumUtil.abs(dx),
                            0, dz * NumUtil.abs(dz));
                        fin = testLoc(dir, w);
                        //Bukkit.broadcast(TCUtil.form(fin + "-f2"));
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
    private @Nullable BVec testLoc(final DYrect dir, final World w) {
        if (bVec == null) return null;
        final Location lc = bVec.center(w);
        if (!lc.isChunkLoaded()) Ostrov.sync(() -> lc.getChunk().load());

        mats = new BlockType[maxY - minY];
        datas = new BlockData[maxY - minY];
        int topY = bVec.y + checks.length - 1, botY = bVec.y - checks.length + 1;
        boolean topCnt = topY < maxY && dir.top, botCnt = botY > minY && dir.bot;
        while (topCnt || botCnt) {
            if (topCnt) {
                boolean miss = false;
                for (int i = 0; i != checks.length; i++) {
                    final int finY = topY + i - checks.length;
                    if (finY < minY) {
                        miss = true;
                        break;
                    }
                    if (switch (checks[i]) {
                        case final TypeCheck tc -> tc.check(getType(finY, w), finY);
                        case final DataCheck dc -> dc.check(getData(finY, w), finY);
                        default -> false;
                    }) continue;
                    miss = true;
                    break;
                }
                if (!miss) return BVec.of(w, bVec.x, topY - checks.length, bVec.z);
                topY++; topCnt = topY < maxY;
            }

            if (botCnt) {
                boolean miss = false;
                for (int i = 0; i != checks.length; i++) {
                    final int finY = botY + i;
                    if (finY > maxY) {
                        miss = true;
                        break;
                    }
                    if (switch (checks[i]) {
                        case final TypeCheck tc -> tc.check(getType(finY, w), finY);
                        case final DataCheck dc -> dc.check(getData(finY, w), finY);
                        default -> false;
                    }) continue;
                    miss = true;
                    break;
                }
                if (!miss) return BVec.of(w, bVec.x, botY, bVec.z);
                botY--; botCnt = botY > minY;
            }
        }
        return null;
    }

    private BlockType getType(final int y, final World w) {
        final int slot = y - minY;
        if (slot >= mats.length || slot < 0)
            return BlockType.AIR;

        final BlockType mt = mats[slot];
        if (mt == null) {
            final BlockData bd = datas[slot];
            if (bd != null) {
                final BlockType nbt = bd.getMaterial().asBlockType();
                mats[slot] = nbt;
                return nbt;
            }
            final BlockType nbt = Nms.fastType(w, bVec.x, y, bVec.z);
            mats[slot] = nbt;
            return nbt;
        }
        return mt;
    }

    private static final BlockData AIR = BlockType.AIR.createBlockData();
    private BlockData getData(final int y, final World w) {
        final int slot = y - minY;
        if (slot >= mats.length || slot < 0)
            return AIR;

        final BlockData bd = datas[slot];
        if (bd == null) {
            final BlockData nbd = Nms.fastData(w, bVec.x, y, bVec.z);
            datas[slot] = nbd;
            return nbd;
        }
        return bd;
    }

    public interface Check {}
    public interface TypeCheck extends Check {
        boolean check(final BlockType tp, final int y);
    }
    public interface DataCheck extends Check {
        boolean check(final BlockData tp, final int y);
    }

    public enum DYrect {

        UP(true, false), DOWN(false, true), BOTH(true, true);

        public final boolean top, bot;

        DYrect(final boolean top, final boolean bot) {
            this.top = top;
            this.bot = bot;
        }
    }
}
