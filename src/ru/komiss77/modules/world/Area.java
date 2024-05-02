package ru.komiss77.modules.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.World;
import com.mojang.datafixers.util.Pair;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.Schematic.Rotate;
import ru.komiss77.notes.Slow;
import ru.komiss77.notes.ThreadSafe;
import ru.komiss77.version.Nms;


public class Area extends Cuboid {
	
	public static int MAX_STEP = 1000;
	public static int MAX_DST = 4;
	
//	public static final Map<String, Area> areas = new HashMap<>();
	
	public final World w;
	private final Map<Integer, SPos> walkable;
	public boolean isMapped;
	public boolean remap;
	
	public Area(final String id, final XYZ min, final XYZ max, final World w) {
		super(min, max);
		this.w = w;
		isMapped = true;
		remap = false;
		walkable = new HashMap<>();
		displayName = id;
	}
	
	@ThreadSafe
	@SuppressWarnings("unchecked")
	public void loadPos() {
		walkable.clear();
		if (!isMapped) {
			remap = true;
			return;
		}
		isMapped = false;
		long time = System.currentTimeMillis();
		Ostrov.log_ok("Mapping area " + displayName);
		
		final Iterator<XYZ> it = iteratorXYZ(Rotate.r0);
		while (it.hasNext()) {
			final XYZ lc = it.next();
			/* ``isSolid`` checks if the block is considered motion blocking for AI navigation. 
			 * what mojang considers "solid" is ``isBuildable``, which is what for example signs use to check if they can be attached to a block. 
			 * ``isPassable`` checks if the block has no collider.
			 * In practice this will always be the same except for webs, they have no collider (passable=true), 
			 * but blocks can be placed on them (isSolid=true)*/
			if (Nms.getFastMat(w, lc.x, lc.y, lc.z).isCollidable() &&
				!Nms.getFastMat(w, lc.x, lc.y + 1, lc.z).isCollidable() &&
				!Nms.getFastMat(w, lc.x, lc.y + 2, lc.z).isCollidable()) {
				final SPos sp = new SPos(new XYZ("", lc.x, lc.y + 1, lc.z), this);
				walkable.put(lc.getSLoc(), sp);
			}
		}
		Ostrov.log_ok("walks-" + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
		
		final Iterator<SPos> sit = walkable.values().iterator();
		while (sit.hasNext()) {
			final SPos sp = sit.next();
			final Map<SPos, Boolean> link = new HashMap<>();
			final XYZ lc = sp.toWXYZ(this);
			updateLinks(lc, link, 1, 0);
			updateLinks(lc, link, -1, 0);
			updateLinks(lc, link, 0, 1);
			updateLinks(lc, link, 0, -1);
//			Bukkit.getConsoleSender().sendMessage("l-" + lc.toString() + ", f-" + link.size());
			if (link.size() == 0) sit.remove();
			else {
				sp.linked = (Pair<SPos, Boolean>[]) new Pair<?, ?>[link.size()];
				int i = 0;
				for (final Entry<SPos, Boolean> en : link.entrySet()) {
					sp.linked[i] = new Pair<SPos, Boolean>(en.getKey(), en.getValue());
					i++;
				}
			}
		}
		
		Ostrov.log_ok("links-" + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
		
		for (final SPos org : walkable.values()) {
			final ArrayList<SPos> step = new ArrayList<>();
			org.steps.put(org, 0);
			step.add(org);
			
			for (int dst = 0; dst < MAX_STEP; dst++) {
				final HashSet<SPos> nst = new HashSet<>();
				final Iterator<SPos> sti = step.iterator();
				boolean none = true;
				while (sti.hasNext()) {
					final SPos stp = sti.next();
					for (final Pair<SPos, Boolean> ls : stp.linked) {
						if (!org.steps.containsKey(ls.getFirst())) {
							none = false;
							nst.add(ls.getFirst());
						}
					}
					org.steps.put(stp, dst);
					sti.remove();
				}
				
				if (none) break;
				step.addAll(nst);
			}
		}
		
		Ostrov.log_ok("dists-" + (System.currentTimeMillis() - time));
		Ostrov.log_ok("Area finished mapping!");
		isMapped = true;
		
		if (remap) {
			remap = false;
			loadPos();
		}
	}

	private void updateLinks(final XYZ sp, final Map<SPos, Boolean> link, final int dx, final int dz) {
		SPos nxt = walkable.get(new XYZ("", sp.x + dx, sp.y, sp.z + dz).getSLoc());
		if (nxt == null) nxt = walkable.get(new XYZ("", sp.x + dx, sp.y + 1, sp.z + dz).getSLoc());
		if (nxt == null) nxt = walkable.get(new XYZ("", sp.x + dx, sp.y - 1, sp.z + dz).getSLoc());
			
		if (nxt == null) {
			for (int i = 1; i != 4; i++) {
				for (int d = -2; d != (i == 1 ? 8 : 2); d++) {
					if (Nms.getFastMat(w, dx * i + sp.x, sp.y - d, dz * i + sp.z).isCollidable()) {
						if ((d == 0 || d == 1 || d == 2) || (i == 1 && d > 2)) {
							nxt = walkable.get(new XYZ("", dx * i + sp.x, sp.y - d + 1, dz * i + sp.z).getSLoc());
							if (nxt != null) link.put(nxt, true);
							return;
						}
					}
				}
			}
		} else link.put(nxt, false);
	}
	
	public Map<Integer, SPos> getWalkable() {
		return walkable;
	}
	
	@Slow(priority = 2)
	public SPos getCloseTo(final XYZ to) {
		final HashSet<XYZ> last = new HashSet<>();
		last.add(to);
		final SPos fs = walkable.get(to.getSLoc());
		if (fs != null) return fs;
		
		for (int dst = 0; dst < MAX_DST; dst++) {
			final ArrayList<XYZ> step = new ArrayList<>();
			for (final XYZ lc : last) {
				step.add(lc.clone().add(0, -1, 0));
				step.add(lc.clone().add(0, 1, 0));
				step.add(lc.clone().add(1, 0, 0));
				step.add(lc.clone().add(0, 0, 1));
				step.add(lc.clone().add(-1, 0, 0));
				step.add(lc.clone().add(0, 0, -1));
			}
			
			for (final XYZ lc : step) {
				if (last.add(lc)) {
					final SPos ns = walkable.get(lc.getSLoc());
					if (ns != null) return ns;
				}
			}
		}
		
		return null;
	}
	
	public Location getSPLoc(final SPos ps) {
		return new Location(w, minX() + ps.dx + 0.5d, minY() + ps.dy + 0.1d, minZ() + ps.dz + 0.5d);
	}
	
	public class SPos {
		
		public final short dx, dy, dz;
		protected final Map<SPos, Integer> steps;
		
		protected Pair<SPos, Boolean>[] linked;
		
		@SuppressWarnings("unchecked")
		private SPos(final XYZ loc, final Area ar) {
			dx = (short) (loc.x - ar.minX());
			dy = (short) (loc.y - ar.minY());
			dz = (short) (loc.z - ar.minZ());
			linked = (Pair<SPos, Boolean>[]) new Pair<?,?>[0];
			steps = new HashMap<>();
		}
		
		@Nullable
		public Integer distTo(final SPos to) {
			return steps.get(to);
		}
		
		public WXYZ toWXYZ(final Area of) {
			return new WXYZ(of.w, dx + of.minX(), dy + of.minY(), dz + of.minZ());
		}
		
		public int cLoc() {
	        return (dy&511)<<24 | dx << 12 | dz;
		}
		
		@Override
		public boolean equals(final Object o) {
			return o instanceof SPos && ((SPos) o).cLoc() == cLoc();
		}
		
		@Override
		public int hashCode() {
			return cLoc();
		}
		
		@Override
		public String toString() {
			return "x-" + dx + ", y-" + dy + ", z-" + dz;
		}
	}
}
