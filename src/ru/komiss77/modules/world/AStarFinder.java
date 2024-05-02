package ru.komiss77.modules.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.Ostrov;
import ru.komiss77.notes.Slow;
import ru.komiss77.notes.ThreadSafe;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.version.Nms;

import java.util.*;

public class AStarFinder {//idea of UnAlike

//	private static final Map<UUID, Map<Integer, XYZ[]>> worldNodes = new HashMap<>();
    public static final BlockData bd = Material.GOLD_BLOCK.createBlockData();
    private static final int MAX_DST = 4;
    private static final LinkedList<Node> EMPTY = new LinkedList<>();

    @ThreadSafe
    protected static LinkedList<Node> findPath(final WXYZ from, final WXYZ to, final int maxNodes, final boolean jump) {
        return getPath(getClsWlk(from), getClsWlk(to), maxNodes, jump);
    }

    @ThreadSafe
    private static LinkedList<Node> getPath(final WXYZ from, final WXYZ to, final int maxNodes, final boolean jump) {
        if (from == null || to == null) {
//          Ostrov.log_warn("Didnt find A* " + from + " or " + to); долбит консоль если просто взлететь вверх - да ладно, рили? не знал xD
          return EMPTY;//new LinkedList<>();
        }
        final Node bgn = new Node(from);
        final Node end = new Node(to);
        bgn.set(0, from.distAbs(to));
        end.set(0, 0);

        final int fsl = end.getSLoc();
//		final SortedList<Node> sls = new SortedList<>();
        final IntHashMap<Node> open = new IntHashMap<>();
        final HashSet<Integer> clsd = new HashSet<>();
        open.put(bgn.getSLoc(), bgn);

        int ci = 0;
        Node min = null;
        Node curr = null;
        while (true) {
            ci++;
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
                  ci = maxNodes;//конец
                  curr = min;
                }
            }

            final int csl = curr.getSLoc();
            if (min == null || curr.yaw < min.yaw) {
              min = curr;
            }
            open.remove(csl);
            clsd.add(csl);

//			curr = open.pollFirst();
//			final Location loc = curr.getCenterLoc(from.w);
//			Bukkit.getOnlinePlayers().forEach(p -> p.sendBlockChange(loc, Material.RED_CARPET.createBlockData()));
//			if (ci < 40) Bukkit.broadcast(Component.text(curr.toString() + " " + curr.pitch + " " + curr.yaw + " " + curr.cost));

            if (csl == fsl || ci == maxNodes) {
                final LinkedList<Node> path = new LinkedList<>();
                path.add(curr);
                int dff = 0;
                while (true) {
                    final Node crp = curr.prnt;
                    if (crp.getSLoc() == curr.getSLoc()) {
                        path.addFirst(curr);
                        break;
                    }

                    if (curr.distAbs(crp) == 1) {
//						Bukkit.getConsoleSender().sendMessage("c-" + curr.toString() + ", p-" + crp.toString() + ", d-" + curr.distAbs(crp));
                        final int d = ((curr.x - crp.x) << 1) + curr.z - crp.z;
                        if (d == dff) {
                            curr = crp;
                            continue;
                        }
                        dff = d;
                    } else {
                        dff = 0;
                        curr.jump = true;
                    }

                    path.addFirst(curr);
                    curr = crp;
                }
                return path;
            }

            Node nxt = null;
            for (final XYZ near : getNear(from.w, curr, jump)) {
              final int slc = near.getSLoc();
              if (clsd.contains(slc)) {
                  continue;
              }
              final Node nghNode = open.get(slc);
              final int hDst = curr.pitch + curr.distAbs(near);
              if (nghNode == null) {
                  final Node nwNode = new Node(near).set(hDst, end.distAbs(near));
                  nwNode.prnt = curr;
                  open.put(slc, nwNode);
                  if (nxt == null) {
                      nxt = nwNode.cost < curr.cost ? nwNode : null;
                  } else {
                      nxt = nwNode.cost < nxt.cost ? nwNode : null;
                  }
              } else if (nghNode.pitch > hDst) {
                  nghNode.set(hDst, nghNode.yaw);
                  nghNode.prnt = curr;
                  if (nxt == null) {
                      nxt = nghNode.cost < curr.cost ? nghNode : null;
                  } else {
                      nxt = nghNode.cost < nxt.cost ? nghNode : null;
                  }
              }
            }
            curr = nxt;
        }
    }

    private static XYZ[] getNear(final World w, final Node nd, final boolean jump) {//, final Map<Integer, XYZ[]> nodes
      final Set<XYZ> nds = new HashSet<>();
      lookNear(nd, nds, w, 1, 0, jump);
      lookNear(nd, nds, w, -1, 0, jump);
      lookNear(nd, nds, w, 0, 1, jump);
      lookNear(nd, nds, w, 0, -1, jump);
      final Iterator<XYZ> ndi = nds.iterator();
      BoundingBox bb = null;
      while (ndi.hasNext()) {
        final XYZ lc = ndi.next();
        switch (Nms.getFastMat(w, lc.x, lc.y - 1, lc.z)) {
          case ACACIA_FENCE, ACACIA_FENCE_GATE, BAMBOO_FENCE, BAMBOO_FENCE_GATE, BIRCH_FENCE,
            BIRCH_FENCE_GATE, CHERRY_FENCE, CHERRY_FENCE_GATE, CRIMSON_FENCE, CRIMSON_FENCE_GATE,
            DARK_OAK_FENCE, DARK_OAK_FENCE_GATE, JUNGLE_FENCE, JUNGLE_FENCE_GATE, MANGROVE_FENCE,
            MANGROVE_FENCE_GATE, NETHER_BRICK_FENCE, OAK_FENCE, OAK_FENCE_GATE, SPRUCE_FENCE,
            SPRUCE_FENCE_GATE, WARPED_FENCE, WARPED_FENCE_GATE, ANDESITE_WALL, BLACKSTONE_WALL,
            BRICK_WALL, COBBLED_DEEPSLATE_WALL, COBBLESTONE_WALL, DEEPSLATE_BRICK_WALL,
            DEEPSLATE_TILE_WALL, STONE_BRICK_WALL, RED_SANDSTONE_WALL, RED_NETHER_BRICK_WALL,
            POLISHED_DEEPSLATE_WALL, POLISHED_BLACKSTONE_WALL, POLISHED_BLACKSTONE_BRICK_WALL,
            NETHER_BRICK_WALL, MOSSY_COBBLESTONE_WALL, MOSSY_STONE_BRICK_WALL,
            GRANITE_WALL, END_STONE_BRICK_WALL, DIORITE_WALL:
            ndi.remove();
            break;
          default:
            if (lc.y > nd.y) {
              if (bb == null) bb = w.getBlockAt(nd.x, nd.y - 1, nd.z).getBoundingBox();
              final double h = bb.getHeight();
              if (h < 1d && w.getBlockAt(lc.x, lc.y - 1, lc.z)
                .getBoundingBox().getHeight() - h > 0.2d) {
                ndi.remove();
              }
            }
            break;
        }
      }
      //		nodes.put(nd.getSLoc(), ns);
      return nds.toArray(XYZ[]::new);
    }

    private static void lookNear(final XYZ sp, final Set<XYZ> nds, final World w, final int dx, final int dz, final boolean jump) {
        final WXYZ nxt = new WXYZ(w, sp.x + dx, sp.y, sp.z + dz);
        if (Nms.getFastMat(w, nxt.x, nxt.y + 1, nxt.z).isCollidable()) {
            return;//?|? B ?
        }
        if (Nms.getFastMat(nxt).isCollidable()) {//?|B 0 ?
            if (Nms.getFastMat(w, nxt.x, nxt.y + 2, nxt.z).isCollidable()) {
                return;//?|B 0 B
            }
            if (Nms.getFastMat(w, sp.x, sp.y + 2, sp.z).isCollidable()) {
                return;//?|B 0 v
            }
            nds.add(nxt.clone().add(0, 1, 0));//?|B 0 0
        } else {//?|0 0 ?
            if (Nms.getFastMat(w, nxt.x, nxt.y - 1, nxt.z).isCollidable()) {//B|0 0 ?
                nds.add(nxt.clone());//B|0 0 ?
            } else {//0|0 0 ?
                for (int d = 2; d != 11; d++) {//? 6<-? 0|0 0 ?
                    if (Nms.getFastMat(w, nxt.x, nxt.y - d, nxt.z).isCollidable()) {
                        nds.add(nxt.clone().add(0, 1 - d, 0));//B 9<-.. 0|0 0 ?
                        break;
                    }
                }

                if (Nms.getFastMat(w, nxt.x, nxt.y + 2, nxt.z).isCollidable()) {
                    return;//.. 9<-.. 0|0 0 B
                }
                if (Nms.getFastMat(w, sp.x, sp.y + 2, sp.z).isCollidable() || !jump) {
                    return;//.. 9<-.. 0|0 0 v
                }				//>0 block jump
                WXYZ jmp;
                for (int i = 2; i != 5; i++) {
                    for (int d = -2; d != 3; d++) {
                        if (Nms.getFastMat(w, dx * i + sp.x, sp.y - d, dz * i + sp.z).isCollidable()) {
                            switch (d) {
                                case 0, 1, 2:
                                    jmp = getIfWalk(new WXYZ(w, dx * i + sp.x, sp.y - d + 1, dz * i + sp.z));
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

    private static @Nullable
    WXYZ getIfWalk(final WXYZ lc) {
        return isWalk(lc) ? lc : null;
    }

    private static boolean isWalk(final WXYZ lc) {
        return Nms.getFastMat(lc.w, lc.x, lc.y - 1, lc.z).isCollidable()
          && !Nms.getFastMat(lc.w, lc.x, lc.y, lc.z).isCollidable()
          && !Nms.getFastMat(lc.w, lc.x, lc.y + 1, lc.z).isCollidable();
    }

    @Slow(priority = 2)
    public static WXYZ getClsWlk(final WXYZ to) {
        if (isWalk(to)) {
            return to;
        }
        final HashSet<WXYZ> last = new HashSet<>();
        last.add(to);

        for (int dst = 0; dst < MAX_DST; dst++) {
            final ArrayList<WXYZ> step = new ArrayList<>();
            for (final WXYZ lc : last) {
                step.add(lc.clone().add(0, 1, 0));
                step.add(lc.clone().add(0, -1, 0));
                step.add(lc.clone().add(1, 0, 0));
                step.add(lc.clone().add(0, 0, 1));
                step.add(lc.clone().add(-1, 0, 0));
                step.add(lc.clone().add(0, 0, -1));
            }

            for (final WXYZ lc : step) {
                if (last.add(lc)) {
                    if (isWalk(lc)) {
                        return lc;
                    }
                }
            }
        }

        return null;
    }

    protected static class Node extends XYZ /*implements Comparable<Node> */ {

        private int cost;
        private Node prnt;
        protected boolean jump;

        private Node(final XYZ lc) {
            x = lc.x;
            y = lc.y;
            z = lc.z;
            pitch = lc.pitch;
            yaw = lc.yaw;
            cost = pitch + yaw;
            prnt = this;
            jump = false;
        }

        private Node set(final int home, final int far) {
            cost = (pitch = home) + (yaw = far);
            return this;
        }

        /*@Override
		public int compareTo(@NotNull final Node n) {
			return cost - n.cost;
		}*/
        @Override
        public boolean equals(final Object o) {
            return o instanceof XYZ && ((XYZ) o).getSLoc() == getSLoc();
        }

        @Override
        public int hashCode() {
            return getSLoc();
        }
    }

}
