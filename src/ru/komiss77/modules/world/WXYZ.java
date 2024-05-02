package ru.komiss77.modules.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import ru.komiss77.utils.FastMath;

public class WXYZ extends XYZ {

	public final World w;
	   
	public WXYZ(final Block b) {
		this.x = b.getX(); this.y = b.getY(); this.z = b.getZ(); 
		this.w = b.getWorld(); this.pitch = 0; this.yaw = 0;
    this.worldName = w.getName();
	}

	public WXYZ(final Block b, final int pt) {
		this.x = b.getX(); this.y = b.getY(); this.z = b.getZ(); 
		this.w = b.getWorld(); this.pitch = pt; this.yaw = 0;
    this.worldName = w.getName();
	}
	   
	public WXYZ(final Block b, final int pt, final int yw) {
		this.x = b.getX(); this.y = b.getY(); this.z = b.getZ(); 
		this.w = b.getWorld(); this.pitch = pt; this.yaw = yw;
    this.worldName = w.getName();
	}
	   
	public WXYZ(final Location loc) {
		this.x = loc.getBlockX(); this.y = loc.getBlockY(); 
		this.z = loc.getBlockZ(); this.w = loc.getWorld();
    this.worldName = w.getName();
		this.pitch = 0;
		this.yaw = 0;
	}
	   
	public WXYZ(final Location loc, final boolean dir) {
		this.x = loc.getBlockX(); this.y = loc.getBlockY(); 
		this.z = loc.getBlockZ(); this.w = loc.getWorld(); 
		this.pitch = dir ? (int) loc.getPitch() : 0; 
		this.yaw = dir ? (int) loc.getYaw() : 0;
    this.worldName = w.getName();
	}
	   
	public WXYZ(final World w, final Vector loc) {
		this.x = loc.getBlockX(); this.y = loc.getBlockY(); this.z = loc.getBlockZ(); 
		this.w = w; this.pitch = 0; this.yaw = 0;
    this.worldName = w.getName();
	}
	   
	public WXYZ(final XYZ p) {
		this.x = p.x; this.y = p.y; this.z = p.z;
    this.worldName = p.worldName;
		this.w = Bukkit.getWorld(p.worldName);
    this.pitch = p.pitch; this.yaw = p.yaw;
	}
	   
	public WXYZ(final World w, final XYZ p) {
		this.x = p.x; this.y = p.y; this.z = p.z; 
		this.w = w; this.pitch = p.pitch; this.yaw = p.yaw;
    this.worldName = w.getName();
	}
	
	public WXYZ(final World w, final int x, final int y, final int z) {
		this.x = x; this.y = y; this.z = z; 
		this.w = w; this.pitch = 0; this.yaw = 0;
    this.worldName = w.getName();
	}
	
	public WXYZ(final World w, final int x, final int y, final int z, final int pt) {
		this.x = x; this.y = y; this.z = z; 
		this.w = w; this.pitch = pt; this.yaw = 0;
    this.worldName = w.getName();
	}
	
	public WXYZ(final World w, final int x, final int y, final int z, final int pt, final int yw) {
		this.x = x; this.y = y; this.z = z; 
		this.w = w; this.pitch = pt; this.yaw = yw;
    this.worldName = w.getName();
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
      return comp.w.getUID().equals(w.getUID()) && comp.x==x && comp.y==y && comp.z==z;
	}

  @Override
	public WXYZ add(final int x, final int y, final int z) {
      this.x += x; this.y += y; this.z += z;
      return this;
	}
	
	@Override
	public WXYZ add(final XYZ val) {
      return add(val.x, val.y, val.z);
	}
	
	@Override
	public WXYZ times(final int m) {
      this.x *= m; this.y *= m; this.z *= m;
      return this;
	}
    
  @Override
  public WXYZ clone() {
      final XYZ c = super.clone();
      return new WXYZ(w, c);
  }

	public int dist2DSq(final WXYZ at) {
		return FastMath.square(at.x - x) + FastMath.square(at.z - z);
	}
}
