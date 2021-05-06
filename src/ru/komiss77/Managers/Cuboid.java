package ru.komiss77.Managers;

import org.bukkit.block.Block;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.World;
import org.bukkit.Location;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.BlockUtils;

public class Cuboid {
    
    //public Location loc1=null;
    //public Location loc2=null;
    //private final String worldName;
    private int x1=0;
    private int y1=0; //не меняем! чтобы стирало с самого низа
    private int z1=0;
    private int x2=0;
    private int y2=0;
    private int z2=0;
    

    
    public Cuboid(final Location loc, final int sizeX, final int sizeY, final int sizeZ) {
//System.out.println(" +++++ Cuboidloc="+loc+" X="+sizeX+" Y="+sizeY+" Z="+sizeZ);
        this.x1 = loc.getBlockX();
        this.y1 = loc.getBlockY();
        this.z1 = loc.getBlockZ();
        this.x2 = x1+sizeX;
        this.y2 = y1+sizeY;
        this.z2 = z1+sizeZ;
//System.out.println(" +++++ Cuboid xyz+size = "+toString()+" size="+getSize());
    }

    
    public Cuboid(final Location location, final Location location2) {
        // = location.getWorld().getName();
        x1 = Math.min(location.getBlockX(), location2.getBlockX());
        y1 = Math.min(location.getBlockY(), location2.getBlockY());
        z1 = Math.min(location.getBlockZ(), location2.getBlockZ());
        x2 = Math.max(location.getBlockX(), location2.getBlockX());
        y2 = Math.max(location.getBlockY(), location2.getBlockY());
        z2 = Math.max(location.getBlockZ(), location2.getBlockZ());
//System.out.println(" +++++ Cuboid loc,loc = "+toString()+" size="+getSize());
    }
    
   /* public Cuboid(final String fromString) {
        final String[] split = fromString.split(", ");
        if (split.length!=7) return;
        
        worldName = split[0];
        x1 = Integer.parseInt(split[1]);
        y1 = Integer.parseInt(split[2]);
        z1 = Integer.parseInt(split[3]);
        x2 = Integer.parseInt(split[4]);
        y2 = Integer.parseInt(split[5]);
        z2 = Integer.parseInt(split[6]);
        
        //если были сохранены нули - точки не были установлены
        if (x1!=0 && z1!=0) loc1 = new Location(Bukkit.getWorld(worldName), x1, y1, z1);
        if (x2!=0 && z2!=0) loc2 = new Location(Bukkit.getWorld(worldName), x2, y2, z2);
//System.out.println(" +++++ Cuboid loc1="+loc1+" loc2="+loc2);
    }
    

   */  
    
    
    
    public boolean contains(final Location location) {
//System.out.println("Cuboid contains "+location);
        //return location.getWorld().getName().equals(worldName) && location.getBlockX() >= x1 && location.getBlockX() <= x2 && location.getBlockY() > y1 && location.getBlockY() < y2 && location.getBlockZ() >= z1 && location.getBlockZ() <= z2;
        return location.getBlockX() >= x1 && location.getBlockX() <= x2 && location.getBlockY() > y1 && location.getBlockY() < y2 && location.getBlockZ() >= z1 && location.getBlockZ() <= z2;
    }
    
    public boolean contains(final int x, final int y, final int z) {
//System.out.println("Cuboid contains "+location);
        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }
    
    
    
    
    
    
    
    
    
    public int getLowerY() {
        return y1;
    }
    
    public int getLowerX() {
        return x1;
    }
    
    public int getLowerZ() {
        return z1;
    }
    
    public int getHightesX() {
        return x2;
    }
    
    public int getHightesY() {
        return y2;
    }
    
    public int getHightesZ() {
        return z2;
    }
    
    public int getSizeX() {
        //return Math.abs(x2)-Math.abs(x1);
        return x2-x1;
    }
    
    public int getSizeY() {
        // Math.abs(y2)-Math.abs(y1);
        return y2-y1;
    }
    
    public int getSizeZ() {
        //return Math.abs(z2)-Math.abs(z1);
        return z2-z1;
    }
    
    
    
    
    
    
    
    
    
    public int getSize() {
        return (x2 - x1 + 1) * (y2 - y1 + 1) * (z2 - z1 + 1);
    }
    
    public Location getCenter(final Location current) {
        int x = Math.round((x1 + x2)/2);
        int z = Math.round ((z1 + z2)/2);
        if (x>0) x+=1;
        if (z>0) z+=1;
        int y = Math.round ((y1+ y2)/2);
        final int yTop = BlockUtils.getHighestBlock(current.getWorld(), x, z).getY();
        if (y < yTop) y = yTop;
        if (y < current.getBlockY()) y =  current.getBlockY();
//System.out.println("getCenter x="+x+" y="+y+" z="+z);
        return new Location ( current.getWorld(), x+0.5, y, z+0.5 );
    }
    
    public Location getRandomLocation(final World world) {
        //final World world = Bukkit.getWorld(worldName);
        //final Random random = new Random();
        final Location location = new Location(world, (double)(x1 + Ostrov.random.nextInt(x2 - x1 + 1)), (double)(y1 + Ostrov.random.nextInt(y2 - y1 + 1)), (double)(z1 + Ostrov.random.nextInt(z2 - z1 + 1)));
        return location.getBlock().getType().isAir() ? location : world.getHighestBlockAt(location).getLocation();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    public Iterator<Block> iterator(final World world) {
        return new CuboidIterator(world, x1, y1, z1, x2, y2, z2);
    }
    
    public Iterator<Location> borderIterator(final World world) {
        return new BorderIterator(world, x1, y1, z1, x2, y2, z2);
    }
    
    
    @Override
    public String toString() {
        return x1 + ", " + y1 + ", " + z1 + ", " + x2 + ", " + y2 + ", " + z2;
    }


    
    
    
    public class CuboidIterator implements Iterator<Block> {
        private final World w;
        private final int baseX;
        private final int baseY;
        private final int baseZ;
        private int x;
        private int y;
        private int z;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        
        public CuboidIterator(final World w, final int baseX, final int baseY, final int baseZ, final int sizeX, final int sizeY, final int sizeZ) {
            this.w = w;
            this.baseX = baseX;
            this.baseY = baseY;
            this.baseZ = baseZ;
            this.sizeX = sizeX - baseX + 1;
            this.sizeY = sizeY - baseY + 1;
            this.sizeZ = sizeZ - baseZ + 1;
            final int size = 0;
            z = size;
            y = size;
            x = size;
        }
        
        @Override
        public boolean hasNext() {
            return x < sizeX && y < sizeY && z < sizeZ;
        }
        
        @Override
        public Block next() {
            final Block block = w.getBlockAt(baseX + x, baseY + y, baseZ + z);
            this.update();
            return block;
        }
        
        public void update() {
            if (++x >= sizeX) {
                x = 0;
                if (++z >= sizeZ) {
                    z = 0;
                    ++y;
                }
            }
        }
        
        @Override
        public void remove() {
        }
    }
    
    
    public class BorderIterator implements Iterator<Location> {
        private final World w;
        private final int baseX;
        private final int baseY;
        private final int baseZ;
        private int x;
        private int y;
        private int z;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        private int stage; //0-дно, 1,2,3,4 - стенки, 5-крышка
        
        public BorderIterator(final World w, final int baseX, final int baseY, final int baseZ, final int sizeX, final int sizeY, final int sizeZ) {
            this.w = w;
            this.baseX = baseX;
            this.baseY = baseY;
            this.baseZ = baseZ;
            this.sizeX = sizeX - baseX + 1;
            this.sizeY = sizeY - baseY + 1;
            this.sizeZ = sizeZ - baseZ + 1;
            z = 0;
            y = 0;
            x = 0;
        }
        
        @Override
        public boolean hasNext() {
//System.out.println("hasNext ?"+(x < sizeX && y < sizeY && z < sizeZ));
            return !(x >= sizeX && y >= sizeY && z >= sizeZ);
        }
        
        @Override
        public Location next() {
            final Location loc = w.getBlockAt(baseX + x, baseY + y, baseZ + z).getLocation();
            this.update();
            return loc;
        }
        
        public void update() {
//System.out.println("stage="+stage+" x="+x+" y="+y+" z="+z);    

            switch (stage) {
                
                case 0: //поддон
                    if (++x >= sizeX) {
                        x = 0;
                        if (++z >= sizeZ) {
                            z = 0;
                            x = 0;
                            ++y;
                            stage=1;
                        }
                    }   
                    break;
                    
                case 1:
                    x++;
                    if (x>=sizeX) stage=2;
                    break;
                    
                case 2:
                    z++;
                    if (z>=sizeZ) stage=3;
                    break;
                    
                case 3:
                    x--;
                    if (x<=0) stage=4;
                    break;
                    
                case 4:
                    z--;
                    if (z<=0) {
                        x=0;
                        z=0;
                        y++;
                        if (y>=sizeY) {
                            stage=5;
                        } else {
                            stage=1;
                        }
                    }    
                    break;
                    
                case 5: //крышка
                    if (++x >= sizeX) {
                        x = 0;
                        if (++z >= sizeZ) {
                            x = sizeX;
                            z = sizeZ;
                            y=sizeY;
                        }
                    }  
                    break;
                    
                default:
                    break;
            }
           
        }
        
        @Override
        public void remove() {
        }
    }
    
    
}
