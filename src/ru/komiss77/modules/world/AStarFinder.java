package ru.komiss77.modules.world;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import ru.komiss77.notes.Slow;
import ru.komiss77.notes.ThreadSafe;
import ru.komiss77.version.IServer;
import ru.komiss77.version.VM;

import java.util.*;

public class AStarFinder {//idea of UnAlike
	
//	private static final Map<UUID, Map<Integer, XYZ[]>> worldNodes = new HashMap<>();
	public static final BlockData bd = Material.GOLD_BLOCK.createBlockData();
	private static final int MAX_DST = 4;
	
	@ThreadSafe
	protected static LinkedList<Node> findPath(final WXYZ from, final WXYZ to, final int maxNodes, final boolean jump) {
		return getPath(getClsWlk(from), getClsWlk(to), maxNodes, jump);
	}
	
	@ThreadSafe
	private static LinkedList<Node> getPath(final WXYZ from, final WXYZ to, final int maxNodes, final boolean jump) {
		if (from == null || to == null) return new LinkedList<>();
		final Node bgn = new Node(from);
		final Node end = new Node(to);
		bgn.set(0, from.distAbs(to));
		end.set(0, 0);
		
		final int fsl = end.getSLoc();
		final HashMap<Integer, Node> open = new HashMap<>();
		final HashSet<Integer> clsd = new HashSet<>();
		open.put(bgn.getSLoc(), bgn);
		
//		final Map<Integer, XYZ[]> nodes;
//		if (worldNodes.containsKey(from.w.getUID())) {
//			nodes = worldNodes.get(from.w.getUID());
//		} else {
//			nodes = new HashMap<>();
//			worldNodes.put(from.w.getUID(), nodes);
//		}
		
		Node curr;
		int ci = 0;
		while (true) {
			curr = null;
			for (final Node nd : open.values()) {
				if (curr == null || (curr.cost - nd.cost) >> 31 == 0)
					curr = nd;
			}
			
			if (curr == null) return new LinkedList<>();
			final int csl = curr.getSLoc();
			open.remove(csl);
			clsd.add(csl);
			
//			curr = open.pollFirst();
//			final Location loc = curr.getCenterLoc(from.w);
//			Bukkit.getOnlinePlayers().forEach(p -> p.sendBlockChange(loc, Material.RED_CARPET.createBlockData()));
//			if (ci < 40) Bukkit.broadcast(Component.text(curr.toString() + " " + curr.pitch + " " + curr.yaw + " " + curr.cost));
			ci++;
			
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
						final int d = ((curr.x - crp.x) << 1) + curr.z - crp.z;
						if (d == dff) {
							curr = crp;
							continue;
						} else dff = d;
					} else {
						dff = 0;
						curr.jump = true;
					}
					
					path.addFirst(curr);
					curr = crp;
				}
				return path;
			}
			
//			final XYZ[] nbs = nodes.get(curr.getSLoc());
//			for (final XYZ near : nbs == null ? getNear(from.w, curr, nodes) : nbs) {
			for (final XYZ near : getNear(from.w, curr, jump)) {
//				Bukkit.getOnlinePlayers().forEach(p -> p.sendBlockChange(near.getCenterLoc(p.getWorld()), Material.BLUE_CARPET.createBlockData()));
				if (clsd.contains(near.getSLoc())) continue;
				final Node nghNode = open.get(near.getSLoc());
				final int hDst = curr.pitch + curr.distAbs(near);
				if (nghNode == null) {
					final Node nwNode = new Node(near).set(hDst, end.distAbs(near));
					nwNode.prnt = curr;
					open.put(nwNode.getSLoc(), nwNode);
				} else if (nghNode.pitch > hDst) {
					nghNode.set(hDst, nghNode.yaw);
					nghNode.prnt = curr;
				}
			}
		}
	}
	
	private static XYZ[] getNear(final World w, final Node nd, final boolean jump) {//, final Map<Integer, XYZ[]> nodes
		final Set<XYZ> nds = new HashSet<>();
		final IServer is = VM.getNmsServer();
		lookNear(nd, nds, w, 1, 0, jump, is);
		lookNear(nd, nds, w, -1, 0, jump, is);
		lookNear(nd, nds, w, 0, 1, jump, is);
		lookNear(nd, nds, w, 0, -1, jump, is);
		final Iterator<XYZ> ndi = nds.iterator();
		while (ndi.hasNext()) {
			final XYZ lc = ndi.next();
			switch (is.getFastMat(w, lc.x, lc.y - 1, lc.z)) {
			case ACACIA_FENCE, ACACIA_FENCE_GATE, BAMBOO_FENCE, BAMBOO_FENCE_GATE, 
			BIRCH_FENCE, BIRCH_FENCE_GATE, CHERRY_FENCE, CHERRY_FENCE_GATE, 
			CRIMSON_FENCE, CRIMSON_FENCE_GATE, DARK_OAK_FENCE, DARK_OAK_FENCE_GATE, 
			JUNGLE_FENCE, JUNGLE_FENCE_GATE, MANGROVE_FENCE, MANGROVE_FENCE_GATE, 
			NETHER_BRICK_FENCE, OAK_FENCE, OAK_FENCE_GATE, 
			SPRUCE_FENCE, SPRUCE_FENCE_GATE, WARPED_FENCE, WARPED_FENCE_GATE,
			
			ANDESITE_WALL, BLACKSTONE_WALL, BRICK_WALL, COBBLED_DEEPSLATE_WALL, 
			COBBLESTONE_WALL, DEEPSLATE_BRICK_WALL, DEEPSLATE_TILE_WALL, 
			STONE_BRICK_WALL, RED_SANDSTONE_WALL, RED_NETHER_BRICK_WALL, 
			POLISHED_DEEPSLATE_WALL, POLISHED_BLACKSTONE_WALL, POLISHED_BLACKSTONE_BRICK_WALL, 
			NETHER_BRICK_WALL, MOSSY_COBBLESTONE_WALL, MOSSY_STONE_BRICK_WALL, 
			GRANITE_WALL, END_STONE_BRICK_WALL, DIORITE_WALL:
				ndi.remove();
				break;
			default:
				break;
			}
		}
        //		nodes.put(nd.getSLoc(), ns);
		return nds.toArray(new XYZ[0]);
	}

	private static void lookNear(final XYZ sp, final Set<XYZ> nds, final World w, final int dx, final int dz, final boolean jump, final IServer is) {
		WXYZ nxt = getIfWalk(new WXYZ(w, sp.x + dx, sp.y, sp.z + dz), is);
		if (nxt == null) nxt = getIfWalk(new WXYZ(w, sp.x + dx, sp.y + 1, sp.z + dz), is);
		if (nxt == null) nxt = getIfWalk(new WXYZ(w, sp.x + dx, sp.y - 1, sp.z + dz), is);
			
		if (nxt == null) {
			for (int d = -2; d != 8; d++) {
				if (is.getFastMat(w, dx + sp.x, sp.y - d, dz + sp.z).isCollidable()) {
					if (d > 2) {
						nds.add(new XYZ("", dx + sp.x, sp.y - d + 1, dz + sp.z));
						break;
					} else return;
				}
			}

			if (jump) {
				for (int i = 2; i != 5; i++) {
					for (int d = -2; d != 3; d++) {
//						for (final Player p : w.getPlayers()) p.sendBlockChange(new WXYZ(w, dx * i + sp.x, sp.y - d + 1, dz * i + sp.z).getCenterLoc(), bd);
						if (is.getFastMat(w, dx * i + sp.x, sp.y - d, dz * i + sp.z).isCollidable()) {
							switch (d) {
							case 0, 1, 2:
								nxt = getIfWalk(new WXYZ(w, dx * i + sp.x, sp.y - d + 1, dz * i + sp.z), is);
								if (nxt != null) nds.add(nxt);
								break;
							default:
								break;
							}
							return;
						}
					}
				}
			}
		} else nds.add(nxt);
	}
	
	private static WXYZ getIfWalk(final WXYZ lc, final IServer is) {
		return isWalk(lc, is) ? lc : null;
	}
	
	private static boolean isWalk(final WXYZ lc, final IServer is) {
		return is.getFastMat(lc.w, lc.x, lc.y - 1, lc.z).isCollidable() && 
			!is.getFastMat(lc.w, lc.x, lc.y, lc.z).isCollidable() && 
			!is.getFastMat(lc.w, lc.x, lc.y + 1, lc.z).isCollidable();
	}
	
	
	
	@Slow(priority = 2)
	public static WXYZ getClsWlk(final WXYZ to) {
		final IServer is = VM.getNmsServer();
		if (isWalk(to, is)) return to;
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
					if (isWalk(lc, is)) return lc;
				}
			}
		}
		
		return null;
	}
	
	/*@ThreadSafe
	public static boolean clearNodes(final World in) {
		return worldNodes.remove(in.getUID()) != null;
	}
	
	@ThreadSafe
	public static boolean clearNode(final WXYZ lc) {
		final Map<Integer, XYZ[]> nodes = worldNodes.get(lc.w.getUID());
		return nodes.remove(lc.getSLoc()) != null;
	}*/
	
	protected static class Node extends XYZ {
		
		private int cost;
		private Node prnt;
		protected boolean jump;
		
		private Node(final XYZ lc) {
			x = lc.x; y = lc.y; z = lc.z;
			pitch = lc.pitch; yaw = lc.yaw;
			cost = pitch + yaw; prnt = this;
			jump = false;
		}
		
		private Node set(final int home, final int far) {
			cost = (pitch = home) + (yaw = far);
			return this;
		}
		
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
