package ru.komiss77.modules.world;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.AStarFinder.Node;
import ru.komiss77.notes.Slow;
import ru.komiss77.utils.NumUtil;

public class AStarPath {

    //	public static final HashMap<Integer, AreaPath> paths = new HashMap<>();
//	public static final BlockData bd = Material.DIAMOND_BLOCK.createBlockData();
    public static final double WALK_JUMP_DST = 2.5d;
    public static final double MAX_JUMP_DST = 3.2d;
    public static final float MIN_JMP_SPD = 0.8f;
    public static final float MAX_JMP_SPD = 1.2f;
    public static final double dY = 0.44d;
    public static final double MAX_dY = dY * 2d;
    public static final int STUCK_KD = 20;
    public static final int JUMP_KD = 2;

    private static final Node[] non = new Node[0];

    private final WeakReference<Mob> mrf;
    private final Pathfinder pth;
    private final int maxNodes;
    private final boolean jump;
    private final double jmpSpd;

    //	private final int id;

    private BVec tgt;
    private Node[] steps;
    private int next;

    private int jumpKd;
    private boolean isJump;

    private int lastDst;
    private int stuckCnt;

    private NextState nxs;
    private Boolean done;

    //    private int tgtSet;
//    private boolean move;

    public AStarPath(final Mob mb, final int maxNodes, final boolean jump) {
        mrf = new WeakReference<>(mb);
        this.maxNodes = maxNodes;
        this.jump = jump;
        this.jmpSpd = jump ? (MAX_JMP_SPD + MIN_JMP_SPD) * 0.5d : 0d;
        pth = mb.getPathfinder();
        steps = non;
        nxs = null;
        tgt = null;
        done = null;
//        tgtSet = 0;
        next = 0;
        jumpKd = 0;
        lastDst = 0;
        stuckCnt = 0;
    }

    public AStarPath(final Mob mb, final int maxNodes, final float jmpSpd) {
        mrf = new WeakReference<>(mb);
        this.maxNodes = maxNodes;
        this.jump = true;
        this.jmpSpd = jmpSpd;
        pth = mb.getPathfinder();
        steps = non;
        nxs = null;
        tgt = null;
        done = null;
        next = 0;
        jumpKd = 0;

    }

    @Nullable
    public Boolean tickGo(final double speed) {
        if (tgt == null) return null;
//        if (!move) return done = false;

        final Mob mb = mrf.get();
        if (mb == null || !mb.isValid()) {
            return done = null;
        }

        if (isJump) {
            if (!mb.isOnGround()) {
//				mb.setRotation(NumUtil.getYaw(mb.getVelocity()), 0f);
                return done = false;
            }

            pth.stopPathfinding();
            isJump = false;
        }

        if (steps.length == next) {
            final PathResult pr = pth.findPath(tgt.center(mb.getWorld()));
            if (pr != null) pth.moveTo(pr, speed);
            delTgt();
            return done = true;
        }

        final Node crr = steps[next];
        final Location lc = mb.getLocation();

        final int dst = crr.distSq(lc);
        if (dst == lastDst) {
            if (stuckCnt++ == STUCK_KD) {
                setTgt(steps[steps.length - 1].w(mb.getWorld()));
                stuckCnt = 0;
                return done = false;
            }
        } else {
            lastDst = dst;
            stuckCnt = 0;
        }

        if (dst < 4) {
            if (steps.length == next + 1) {
                next(NextState.WALK);
//				return false;
            } else {
                final Node nxt = steps[next + 1];
                if (crr.y - nxt.y > 1) {
                    if (dst < 2) next(NextState.FALL);
                } else {
                    if (nxt.jump && jump) {
                        if (dst == 0 || NumUtil.abs(crr.y - lc.getBlockY()) == dst)
                            next(NextState.JUMP);
                    } else {
                        next(crr.distAbs(nxt) < 3 ? NextState.WALK : NextState.FAST);
                    }
                }
            }
        }

        switch (nxs) {
            case FALL:
                pth.stopPathfinding();
                mb.setVelocity(mb.getVelocity().add(new Vector(crr.x + 0.5d - lc.getX(),
                    1d, crr.z + 0.5d - lc.getZ()).multiply(0.1d)));
                return done = false;
            case JUMP, FAST:
                if (dst > 2) {
                    if (jumpKd == 0) {
                        if (crr.x - lc.getBlockX() +
                            crr.z - lc.getBlockZ() == 0) break;
                        jump(mb, lc, crr);
                    } else jumpKd--;
                }
                break;
            default:
                break;
        }

		/*for (final Player p : tgt.w.getPlayers()) {
			p.sendBlockChange(crr.getCenterLoc(tgt.w), bd);
		}
		Ostrov.sync(() -> {
			for (final Player p : tgt.w.getPlayers()) {
				p.sendBlockChange(crr.getCenterLoc(tgt.w), tgt.w.getBlockData(crr.getCenterLoc(tgt.w)));
			}
		}, 10);*/
        final PathResult pr = pth.findPath(crr.center(mb.getWorld()));
        if (pr != null) pth.moveTo(pr, speed);

        return done = false;
    }

    private void next(final NextState nx) {
        pth.stopPathfinding();
        next++;
        nxs = nx;
    }

    @Deprecated
    public boolean isDone() {
        return done != null && done;
    }

    @Nullable
    @Deprecated
    public Location getNextLoc() {
        final BVec nxt = next < steps.length ? steps[next] : tgt;
        if (nxt == null) return null;
        final World w = tgt.w();
        return w == null ? null : nxt.center(w);
    }

    @Nullable
    public Location getNextLoc(final World w) {
        final BVec nxt = next < steps.length ? steps[next] : tgt;
        return nxt == null ? null : w == null ? null : nxt.center(w);
    }

    @Slow(priority = 2)
    public void setTgt(final BVec to) {
        pth.stopPathfinding();
        nxs = NextState.WALK;
        tgt = to;
        final Mob mb = mrf.get();
        if (mb == null) return;
        final LinkedList<Node> stps = AStarFinder.findPath(BVec.of(mb.getLocation()), to, maxNodes, jump);
        steps = stps.toArray(non);
        next = 0;
    }

    public void delTgt() {
        steps = non;
        next = 0;
        tgt = null;
    }

    public boolean hasTgt() {
        return tgt != null;
    }

    private void jump(final LivingEntity rplc, final Location from, final BVec curr) {
        if (rplc.isOnGround() && jump) {
            pth.stopPathfinding();
            isJump = true;
            jumpKd = (JUMP_KD >> 1) + Ostrov.random.nextInt(JUMP_KD);
            final Vector to = new Vector(curr.x + 0.5d, from.getWorld()
                .getBlockAt(curr.x, curr.y - 1, curr.z).getBoundingBox().getMaxY(), curr.z + 0.5d);
            final Vector drv = to.subtract(from.toVector());
            final double max = nxs == NextState.JUMP ? MAX_JUMP_DST : WALK_JUMP_DST;
            final double abx = Math.abs(drv.getX());
            if (abx > max) drv.setX(drv.getX() / abx * max);
            final double abz = Math.abs(drv.getZ());
            if (abz > max) drv.setZ(drv.getZ() / abz * max);
            final double cft = jmpSpd * 0.175d;
            rplc.setVelocity(new Vector(drv.getX() * cft, Math.min(MAX_dY,
                dY / jmpSpd * (drv.lengthSquared() * 0.01d + 1d)), drv.getZ() * cft));
        }
    }

    private enum NextState {
        WALK, FAST, JUMP, FALL
    }
}
