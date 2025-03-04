package ru.komiss77.modules.world;

import javax.annotation.Nullable;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import ru.komiss77.Ostrov;
import ru.komiss77.notes.Slow;
import ru.komiss77.notes.ThreadSafe;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.version.Nms;

public class AStarFinder {//idea of UnAlike

    //	private static final Map<UUID, Map<Integer, XYZ[]>> worldNodes = new HashMap<>();
    public static final BlockData bd = Material.GOLD_BLOCK.createBlockData();
    private static final int MAX_DST = 4;
    private static final LinkedList<Node> EMPTY = new LinkedList<>();

    @ThreadSafe
    public static LinkedList<? extends BVec> xyzPath(final BVec from, final BVec to, final int maxNodes, final boolean jump) {
        return getPath(getClsWlk(from), getClsWlk(to), maxNodes, jump);
    }

    @ThreadSafe
    protected static LinkedList<Node> findPath(final BVec from, final BVec to, final int maxNodes, final boolean jump) {
        return getPath(getClsWlk(from), getClsWlk(to), maxNodes, jump);
    }

    @ThreadSafe
    private static LinkedList<Node> getPath(final BVec from, final BVec to, final int maxNodes, final boolean jump) {
        if (from == null || to == null) return EMPTY;
        final World w = from.w();
        if (w == null) return EMPTY;
        final Node bgn = new Node(from);
        final Node end = new Node(to);
        bgn.set(0, from.distAbs(to));
        end.set(0, 0);

        final int finSLoc = end.thin();
//		final SortedList<Node> sls = new SortedList<>();
        final IntHashMap<Node> open = new IntHashMap<>();
        final HashSet<Integer> closed = new HashSet<>();
        open.put(bgn.thin(), bgn);

        int count = 0;
        Node min = null;
        Node curr = null;
        while (true) {
            count++;
            if (curr == null) {
                for (final Node nd : open.values()) {
                    if (curr == null || nd.cost < curr.cost) {
                        curr = nd;
                    }
                }

                if (curr == null) {
                    if (min == null) {
                        Ostrov.log_warn("No A* values found");
                        return new LinkedList<>();
                    }
                    count = maxNodes;//конец
                    curr = min;
                }
            }

            final int currSLoc = curr.thin();
            if (min == null || curr.far < min.far) {
                min = curr;
            }
            open.remove(currSLoc);
            closed.add(currSLoc);

//			curr = open.pollFirst();
//			final Location loc = curr.getCenterLoc(from.w);
//			Bukkit.getOnlinePlayers().forEach(p -> p.sendBlockChange(loc, Material.RED_CARPET.createBlockData()));
//			if (ci < 40) Bukkit.broadcast(Component.text(curr.toString() + " " + curr.pitch + " " + curr.yaw + " " + curr.cost));

            if (currSLoc == finSLoc || count == maxNodes) {
                final LinkedList<Node> path = new LinkedList<>();
                if (curr.far > min.far) curr = min;
                path.add(curr);
                int dff = 0;
                while (true) {
                    final Node currParent = curr.prnt;
                    if (currParent.thin() == curr.thin()) {
                        path.addFirst(curr);
                        break;
                    }

                    if (curr.distAbs(currParent) == 1) {
//						Bukkit.getConsoleSender().sendMessage("c-" + curr.toString() + ", p-" + crp.toString() + ", d-" + curr.distAbs(crp));
                        final int encDst = ((curr.x - currParent.x) << 1) + curr.z - currParent.z;
                        if (encDst == dff) {
                            curr = currParent;
                            continue;
                        }
                        dff = encDst;
                    } else {
                        dff = 0;
                        curr.jump = true;
                    }

                    path.addFirst(curr);
                    curr = currParent;
                }
                return path;
            }

            Node next = null;
            for (final BVec near : getNear(w, curr, jump)) {
                final int nearSLoc = near.thin();
                if (closed.contains(nearSLoc)) continue;

                final Node nghNode = open.get(nearSLoc);
                final int homeDist = curr.home + curr.distAbs(near);
                if (nghNode == null) {
                    final Node newNode = new Node(near).set(homeDist, end.distAbs(near));
                    newNode.prnt = curr;
                    open.put(nearSLoc, newNode);
                    if (next == null) {
                        next = newNode.cost < curr.cost ? newNode : null;
                    } else {
                        next = newNode.cost < next.cost ? newNode : null;
                    }
                } else if (nghNode.home > homeDist) {
                    nghNode.set(homeDist, nghNode.far);
                    nghNode.prnt = curr;
                    if (next == null) {
                        next = nghNode.cost < curr.cost ? nghNode : null;
                    } else {
                        next = nghNode.cost < next.cost ? nghNode : null;
                    }
                }
            }
            curr = next;
        }
    }

    private static final Set<BlockType> NO_STEP = Set.of(BlockType.ACACIA_FENCE, BlockType.ACACIA_FENCE_GATE, BlockType.BAMBOO_FENCE, BlockType.BAMBOO_FENCE_GATE,
        BlockType.BIRCH_FENCE, BlockType.BIRCH_FENCE_GATE, BlockType.CHERRY_FENCE, BlockType.CHERRY_FENCE_GATE, BlockType.CRIMSON_FENCE, BlockType.CRIMSON_FENCE_GATE,
        BlockType.DARK_OAK_FENCE, BlockType.DARK_OAK_FENCE_GATE, BlockType.JUNGLE_FENCE, BlockType.JUNGLE_FENCE_GATE, BlockType.MANGROVE_FENCE, BlockType.MANGROVE_FENCE_GATE,
        BlockType.NETHER_BRICK_FENCE, BlockType.OAK_FENCE, BlockType.OAK_FENCE_GATE, BlockType.SPRUCE_FENCE, BlockType.SPRUCE_FENCE_GATE, BlockType.WARPED_FENCE,
        BlockType.PALE_OAK_FENCE, BlockType.PALE_OAK_FENCE_GATE, BlockType.WARPED_FENCE_GATE, BlockType.ANDESITE_WALL, BlockType.BLACKSTONE_WALL, BlockType.BRICK_WALL,
        BlockType.COBBLED_DEEPSLATE_WALL, BlockType.COBBLESTONE_WALL, BlockType.DEEPSLATE_BRICK_WALL, BlockType.DEEPSLATE_TILE_WALL, BlockType.STONE_BRICK_WALL,
        BlockType.RED_SANDSTONE_WALL, BlockType.RED_NETHER_BRICK_WALL, BlockType.POLISHED_DEEPSLATE_WALL, BlockType.POLISHED_BLACKSTONE_WALL, BlockType.PRISMARINE_WALL,
        BlockType.POLISHED_BLACKSTONE_BRICK_WALL, BlockType.NETHER_BRICK_WALL, BlockType.MOSSY_COBBLESTONE_WALL, BlockType.MOSSY_STONE_BRICK_WALL,
        BlockType.GRANITE_WALL, BlockType.END_STONE_BRICK_WALL, BlockType.DIORITE_WALL, BlockType.MUD_BRICK_WALL, BlockType.RESIN_BRICK_WALL);
    private static BVec[] getNear(final World w, final Node nd, final boolean jump) {//, final Map<Integer, XYZ[]> nodes
        final Set<BVec> nds = new HashSet<>();
        lookNear(nd, nds, w, 1, 0, jump);
        lookNear(nd, nds, w, -1, 0, jump);
        lookNear(nd, nds, w, 0, 1, jump);
        lookNear(nd, nds, w, 0, -1, jump);
        final Iterator<BVec> ndi = nds.iterator();
        BoundingBox bb = null;
        while (ndi.hasNext()) {
            final BVec lc = ndi.next();
            if (NO_STEP.contains(Nms.fastType(w, lc.x, lc.y - 1, lc.z))) {
                ndi.remove(); continue;
            }
            if (lc.y <= nd.y) continue;
            if (bb == null) bb = w.getBlockAt(nd.x, nd.y - 1, nd.z).getBoundingBox();
            final double h = bb.getHeight();
            if (h < 1d && w.getBlockAt(lc.x, lc.y - 1, lc.z)
                .getBoundingBox().getHeight() - h > 0.2d) {
                ndi.remove();
            }
        }
        return nds.toArray(BVec[]::new);
    }

    private static void lookNear(final BVec sp, final Set<BVec> nds, final World w, final int dx, final int dz, final boolean jump) {
        final BVec nxt = BVec.of(sp.x + dx, sp.y, sp.z + dz);
        if (Nms.fastType(w, nxt.x, nxt.y + 1, nxt.z).hasCollision()) {
            return;//?|? B ?
        }
        if (Nms.fastType(w, nxt).hasCollision()) {//?|B 0 ?
            if (Nms.fastType(w, nxt.x, nxt.y + 2, nxt.z).hasCollision()) {
                return;//?|B 0 B
            }
            if (Nms.fastType(w, sp.x, sp.y + 2, sp.z).hasCollision()) {
                return;//?|B 0 v
            }
            nds.add(nxt.add(0, 1, 0));//?|B 0 0
        } else {//?|0 0 ?
            if (Nms.fastType(w, nxt.x, nxt.y - 1, nxt.z).hasCollision()) {//B|0 0 ?
                nds.add(nxt);//B|0 0 ?
            } else {//0|0 0 ?
                for (int d = 2; d != 11; d++) {//? 6<-? 0|0 0 ?
                    if (Nms.fastType(w, nxt.x, nxt.y - d, nxt.z).hasCollision()) {
                        nds.add(nxt.add(0, 1 - d, 0));//B 9<-.. 0|0 0 ?
                        break;
                    }
                }

                if (Nms.fastType(w, nxt.x, nxt.y + 2, nxt.z).hasCollision()) {
                    return;//.. 9<-.. 0|0 0 B
                }
                if (Nms.fastType(w, sp.x, sp.y + 2, sp.z).hasCollision() || !jump) {
                    return;//.. 9<-.. 0|0 0 v
                }                //>0 block jump
                BVec jmp;
                for (int i = 2; i != 5; i++) {
                    for (int d = -2; d != 3; d++) {
                        if (Nms.fastType(w, dx * i + sp.x, sp.y - d, dz * i + sp.z).hasCollision()) {
                            switch (d) {
                                case 0, 1, 2:
                                    jmp = getIfWalk(w, BVec.of(dx * i + sp.x, sp.y - d + 1, dz * i + sp.z));
                                    if (jmp != null) {
                                        nds.add(jmp);
                                    }
                                    break;
                                default:
                                    break;
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    private static @Nullable BVec getIfWalk(final World w, final BVec lc) {
        return isWalk(w, lc) ? lc : null;
    }

    private static boolean isWalk(final World w, final BVec lc) {
        return Nms.fastType(w, lc.x, lc.y - 1, lc.z).hasCollision()
            && !Nms.fastType(w, lc.x, lc.y, lc.z).hasCollision()
            && !Nms.fastType(w, lc.x, lc.y + 1, lc.z).hasCollision();
    }

    @Slow(priority = 2)
    public static @Nullable BVec getClsWlk(final BVec to) {
        final World w = to.w();
        if (w == null) return null;
        if (isWalk(w, to)) {
            return to;
        }
        final HashSet<BVec> last = new HashSet<>();
        last.add(to);

        for (int dst = 0; dst < MAX_DST; dst++) {
            final ArrayList<BVec> step = new ArrayList<>();
            for (final BVec lc : last) {
                step.add(lc.add(0, 1, 0));
                step.add(lc.add(0, -1, 0));
                step.add(lc.add(1, 0, 0));
                step.add(lc.add(0, 0, 1));
                step.add(lc.add(-1, 0, 0));
                step.add(lc.add(0, 0, -1));
            }

            for (final BVec lc : step) {
                if (last.add(lc) && isWalk(w, lc)) {
                    return lc;
                }
            }
        }

        return null;
    }

    protected static class Node extends BVec /*implements Comparable<Node> */ {

        private int cost;
        private int home;
        private int far;
        private Node prnt;
        protected boolean jump;

        private Node(final BVec lc) {
            super(lc.x, lc.y, lc.z);
            prnt = this;
            jump = false;
        }

        private Node set(final int home, final int far) {
            cost = (this.home = home) + (this.far = far);
            return this;
        }
    }

}
