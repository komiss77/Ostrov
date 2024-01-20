package ru.komiss77.modules.world;

import java.util.Iterator;
import java.util.Set;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.Schematic.Rotate;
import ru.komiss77.utils.ItemUtils;


class PasteJob  implements Runnable {

    protected final CommandSender cs;
    private final Schematic schem;
    private final BukkitTask task; 
    private final boolean pasteAir;
    private final Rotate rotate;
    private final Iterator <XYZ> it;
    private XYZ xyz;
    private final double cuboidSize;
    protected boolean pause = false;
    private int checked; //сколько всего поставлено блоков
    private int current; //счётчик блоков между тиками вставки
    private int ticks = 0; //тики вставки
    protected int percent;
    
    private Block block;
    private final World world;
    final long l = System.currentTimeMillis() + 50; //начнёт через два тика
    
    public PasteJob(final CommandSender cs, final World world, final Cuboid cuboid, final Schematic schem, final Rotate rotate, final boolean pasteAir) {
        this.cs = cs;
        this.schem = schem;
        this.pasteAir = pasteAir;
        this.rotate = rotate;//cuboid = new Cuboid(schem);
        it = cuboid.schematicIterator(schem, rotate);//schem.schematicIterator(cuboid, rotate);//cuboid.iteratorXYZ(rotate);
        this.world = world;
        cuboidSize = cuboid.volume();
        
        task = Bukkit.getScheduler().runTaskTimer(Ostrov.instance, PasteJob.this, 2, 1);
        
        WE.getChunks(cuboid.getLowerLocation(world), cuboid.getHightesLocation(world)).stream().forEach( (chunk) -> {
            for (Entity e : chunk.getEntities()) {
                if ( cuboid.contains(e.getLocation()) && e.getType()!=EntityType.PLAYER && (e instanceof LivingEntity)) {
                    e.remove();
                }
            }
        });
//Ostrov.log(" +++++++++++++++++PasteJob  cuboid paste = "+cuboid);
    }
    
    
    
    
    
    
    
    
    
    @Override
    public void run() {
//Ostrov.log("***paste run it.hasNext()?"+it.hasNext());
        if (pause || WE.wait(task.getTaskId())) return;
        
        ++ticks;
        
        if (schem==null) {
            if (ticks>=60) {
                Ostrov.log_err("PasteJob schem==null больше 60тик");
                this.cancel();
            }
            return;
        }
        
        if (!schem.ready) {
            if (ticks>=60) {
                Ostrov.log_err("PasteJob schem ready=false больше 60тик");
                this.cancel();
            }
            return;
        }
        
        WE.currentTask = task.getTaskId();

        Material mat;
        //BlockData blockData;
        String blockDataAsString;
        String blockState;
        
        while( it.hasNext() && current < WE.getBlockPerTick()) {
            xyz = it.next();

            block = world.getBlockAt(xyz.x, xyz.y, xyz.z); //тут уже готовые координаты блока!
//Ostrov.log("***paste xz="+xyz.x+","+xyz.z+" sLoc="+xyz.yaw+" mat="+schem.blocks.get(xyz.yaw));

            int sLoc = xyz.yaw; //берём соответствующий адрес блока
            
            mat = schem.blocks.get(sLoc);
            if (mat!=null) {//(schem.blocks.containsKey(xyz.yaw)) {
                
                blockDataAsString = schem.blockDatas.get(sLoc);
                if (blockDataAsString!=null) {//if (schem.blockDatas.containsKey(sLoc)) { //есть блокдата\
//Ostrov.log("bd="+blockDataAsString);

                    //200124 серануло java.lang.IllegalArgumentException: Could not parse data: CraftBlockData{minecraft:bamboo_slab[type=bottom,waterlogged=false]}
                    BlockData bd;
                    try {
                        bd = Bukkit.createBlockData(blockDataAsString);
                        if (rotate!=Rotate.r0) {
                            rotateData(bd, rotate);
                        }
                    } catch (IllegalArgumentException ex) {
                        bd = mat.createBlockData();
                        Ostrov.log_err("parse BlockData "+blockDataAsString+" : "+ex.getMessage());
                    }
                    
                    
                    if (mat == block.getType()) {//if (schem.blocks.get(sLoc) == block.getType()) { //тип такой же - обновить блокдату?
                        
                        if (!bd.equals(block.getBlockData())) {//if (schem.blockDatas.get(sLoc)!=block.getBlockData()) { //сравнить блокдату??
                            block.setBlockData(bd, false);//setBlockData(block, blockData);//block.setBlockData(blockData, false); //block.setBlockData(schem.blockDatas.get(sLoc), false); 
                            current++;
                        }
                        
                    } else { //тип разный - поставить тип и дату
                        
                        block.setType(mat, false);// block.setType(schem.blocks.get(sLoc), false);
                        block.setBlockData(bd, false);//setBlockData(block, blockData);//block.setBlockData(blockData, false);//block.setBlockData(schem.blockDatas.get(sLoc), false);
                        current++;
                        
                    }
                    
                } else if (mat != block.getType()) {//} else if (schem.blocks.get(sLoc) != block.getType()) { //блокдатф не запомнено - заменить если не совпадает тип
                    
                    block.setType(mat, false);//block.setType(schem.blocks.get(sLoc), false);
                    current++;
                    
                }
                blockState = schem.blockStates.get(sLoc);
                if (blockState!=null) {//if (schem.blockStates.containsKey(sLoc)) {
                    setBlockState(block, schem.blockStates.get(sLoc));
                    current++;
                }
                
            } else if (block.getType() != Material.AIR) { //нет с сохрвнении значит тут воздух
                if (pasteAir) {
                    block.setType(Material.AIR);
                    current++;
                }
            }
            //блоки которые не изменяются не считаются - они не дают нагрузку
        }

        checked += current;
        current = 0;
        if (ticks%5 == 0 && cs!=null) {
            percent = (int)((double)checked / cuboidSize * 100.0D);
            if ( cs instanceof Player) {
                ApiOstrov.sendActionBarDirect(Bukkit.getPlayer(cs.getName()), "§eвставка "+schem.getName()+": §f"+percent+"%");
            } else {
                if (ticks%20 == 0) cs.sendMessage( "§eвставка "+schem.getName()+": §f"+percent+"%");
            }
        }

        if (!it.hasNext()) {
            cancel();
            if (cs!=null) {
                cs.sendMessage("§aВставка "+schem.getName()+" закончена, сущности очищены. Bремя: §5"+(System.currentTimeMillis()-l)+" мс.");
            }
        }
       
    }

    private static void rotateData(final BlockData bd, final Rotate rt) {
        //if (bd == null) {
        //    return null;
        //}
//Ostrov.log("rotateData=");

        if (bd instanceof Rotatable rotatable) {
            rotatable.setRotation(rotateFace(rotatable.getRotation(), rt));
        }

        if (bd instanceof MultipleFacing multipleFacing) {
            final MultipleFacing mf = multipleFacing;
            final Set<BlockFace> bfs = mf.getFaces();
            for (final BlockFace bf : bfs) {
                mf.setFace(bf, false);
            }
            for (final BlockFace bf : bfs) {
                mf.setFace(rotateFace(bf, rt), true);
            }
        }

        if (bd instanceof Orientable orientable) {
            orientable.setAxis(rotateAxis(orientable.getAxis(), rt));
        }

        if (bd instanceof Directional directional) {
            directional.setFacing(rotateFace(directional.getFacing(), rt));
        }

        //return bd;
    }

    private static Axis rotateAxis(final Axis ax, final Rotate rotate) {
        switch (ax) {
            case Y:
            default:
                return ax;
            case X:
                switch (rotate) {
                    case r0:
                    default:
                        return ax;
                    case r90:
                        return Axis.Z;
                    case r180:
                        return Axis.X;
                    case r270:
                        return Axis.Z;
                }
            case Z:
                switch (rotate) {
                    case r0:
                    default:
                        return ax;
                    case r90:
                        return Axis.X;
                    case r180:
                        return Axis.Z;
                    case r270:
                        return Axis.X;
                }
        }
    }

    private static BlockFace rotateFace(final BlockFace bf, final Rotate rotate) {
        switch (bf) {
            case EAST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.SOUTH;
                    case r180:
                        return BlockFace.WEST;
                    case r270:
                        return BlockFace.NORTH;
                }
            }
            case EAST_NORTH_EAST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.SOUTH_SOUTH_EAST;
                    case r180:
                        return BlockFace.WEST_NORTH_WEST;
                    case r270:
                        return BlockFace.NORTH_NORTH_WEST;
                }
            }
            case EAST_SOUTH_EAST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.SOUTH_SOUTH_WEST;
                    case r180:
                        return BlockFace.WEST_NORTH_WEST;
                    case r270:
                        return BlockFace.NORTH_NORTH_EAST;
                }
            }
            case NORTH -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.EAST;
                    case r180:
                        return BlockFace.SOUTH;
                    case r270:
                        return BlockFace.WEST;
                }
            }
            case NORTH_EAST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.SOUTH_EAST;
                    case r180:
                        return BlockFace.SOUTH_WEST;
                    case r270:
                        return BlockFace.NORTH_WEST;
                }
            }
            case NORTH_NORTH_EAST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.EAST_SOUTH_EAST;
                    case r180:
                        return BlockFace.SOUTH_SOUTH_WEST;
                    case r270:
                        return BlockFace.WEST_NORTH_WEST;
                }
            }
            case NORTH_NORTH_WEST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.EAST_NORTH_EAST;
                    case r180:
                        return BlockFace.SOUTH_SOUTH_EAST;
                    case r270:
                        return BlockFace.WEST_SOUTH_WEST;
                }
            }
            case NORTH_WEST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.NORTH_EAST;
                    case r180:
                        return BlockFace.SOUTH_EAST;
                    case r270:
                        return BlockFace.SOUTH_WEST;
                }
            }
            case SOUTH -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.WEST;
                    case r180:
                        return BlockFace.NORTH;
                    case r270:
                        return BlockFace.EAST;
                }
            }
            case SOUTH_EAST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.SOUTH_WEST;
                    case r180:
                        return BlockFace.NORTH_WEST;
                    case r270:
                        return BlockFace.NORTH_EAST;
                }
            }
            case SOUTH_SOUTH_EAST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.WEST_SOUTH_WEST;
                    case r180:
                        return BlockFace.NORTH_NORTH_WEST;
                    case r270:
                        return BlockFace.EAST_NORTH_EAST;
                }
            }
            case SOUTH_SOUTH_WEST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.WEST_NORTH_WEST;
                    case r180:
                        return BlockFace.NORTH_NORTH_EAST;
                    case r270:
                        return BlockFace.EAST_SOUTH_EAST;
                }
            }
            case SOUTH_WEST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.NORTH_WEST;
                    case r180:
                        return BlockFace.NORTH_EAST;
                    case r270:
                        return BlockFace.SOUTH_EAST;
                }
            }
            case WEST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.NORTH;
                    case r180:
                        return BlockFace.EAST;
                    case r270:
                        return BlockFace.SOUTH;
                }
            }
            case WEST_NORTH_WEST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.NORTH_NORTH_EAST;
                    case r180:
                        return BlockFace.EAST_SOUTH_EAST;
                    case r270:
                        return BlockFace.SOUTH_SOUTH_WEST;
                }
            }
            case WEST_SOUTH_WEST -> {
                switch (rotate) {
                    case r0:
                    default:
                        return bf;
                    case r90:
                        return BlockFace.NORTH_NORTH_WEST;
                    case r180:
                        return BlockFace.EAST_NORTH_EAST;
                    case r270:
                        return BlockFace.SOUTH_SOUTH_EAST;
                }
            }
            default -> {
                return bf;
            }
        }
    }
    
   /* 
    private void setBlockData (final Block block, final BlockData bd) {
//Ostrov.log("BD="+bd.getAsString());
        
        if (rotate!=Rotate.r0) {
            
            if (bd instanceof Directional) {
//Ostrov.log("BD="+bd.getAsString());
                Directional dir = (Directional) bd;
                ((Directional)bd).setFacing(Rotate.rotateBlockFace(dir.getFacing(), rotate));
            }
            
            if (bd instanceof Rotatable) {
                Rotatable rtt = (Rotatable) bd;
                ((Rotatable)bd).setRotation(Rotate.rotateBlockFace(rtt.getRotation(), rotate));
            }
            
            if (bd instanceof Orientable) {
                Orientable ort = (Orientable) bd;
            }
            
          /*  if (rotate==Rotate.r180) {
                if (block.getType()==Material.CHEST) {
                    final Chest chest = (Chest) bd;
                    if (chest.getType()==Chest.Type.LEFT) {
                        ((Chest) bd).setType(Chest.Type.RIGHT);
                    } else if (chest.getType()==Chest.Type.RIGHT) {
                        ((Chest) bd).setType(Chest.Type.LEFT);
                    }
                    //block.setBlockData(chest, false);
                }
            }/
            
//Ostrov.log("result="+bd.getAsString());
            
        }
        
        
        block.setBlockData(bd, false); 
       // switch (rotate) {
            
           // case r90 -> {
            //    if (bd instanceof Directional) {
             //       ((Directional)bd).setFacing(Rotate.rotateBlockFace(((Directional)bd).getFacing(), rotate));

             //   }
                //block.setBlockData(bd, false);
            //}
            
            //case r180 -> {

           // }
            
           // case r270 -> {
                
                //block.setBlockData(bd, false);
          //  }
            
           // default -> {
                //block.setBlockData(bd, false);
            //}
       // }
        
       // block.setBlockData(bd, false);
    }    
    */
    
    
    
    public void cancel() {
        task.cancel();
        WE.endPaste(task.getTaskId());
    }
    
    public int getId() {
        return task.getTaskId();
    }

    public boolean isCanceled() {
        return task.isCancelled();
    }

    public String getSchemName() {
        return schem.getName();
    }
    
    
        private void setBlockState(final Block b, String bsString) {
//Ostrov.log("paste setBlockState "+bsString);
        BlockState bs =  b.getState();
        //if (bsString.startsWith("Inventory=") && bs instanceof InventoryHolder) {
        if (bsString.startsWith("Inventory=") && bs instanceof Container) {
            //final Inventory inv = ((InventoryHolder)bs).getInventory();
            bsString = bsString.replaceFirst("Inventory=", "");
            final Container ch = (Container)bs;
            
            final int inventorySize = ch.getInventory().getSize();
            final ItemStack[] content = new ItemStack[inventorySize];
            final String[] split = bsString.split(",");
            int ttl = 0;
            final int stackLimit = inventorySize>split.length ? split.length : inventorySize; //при rotate сундук становился меньше, и кидало ArrayIndexOutOfBoundsException
            for (int i=0; i<stackLimit; i++) {
                if (split[i].equals("null")) {
                    ttl += 10;//inv.setItem(i, new ItemStack(Material.AIR));
                } else {
                    final ItemStack it = ItemUtils.parseItem(split[i], ";");
                    content[i] = it;
                    ttl += it.getAmount();
                }
//Ostrov.log("paste i="+i+" s="+split[i]+" is="+content[i]);
            }
            
            if (split.length > content.length && split[content.length].equals("RANDOM")) {
            	final ItemStack[] weight = new ItemStack[content.length];
            	for (int i = weight.length - 1; i>=0; i--) {
                    int val = Ostrov.random.nextInt(ttl);
                    for (final ItemStack ii : content) {
                    	if (ii == null || ii.getType() == Material.AIR) {
                    		if ((val -= 10) < 0) {
                                weight[i] = null;
                                break;
                    		}
                    	} else if ((val -= ii.getAmount()) < 0) {
                    		weight[i] = ii.asQuantity(Ostrov.random.nextInt((ii.getAmount() >> 4) + 1) + 1);
                            break;
						}
                    }
            	}
                ch.getInventory().setContents(weight);
            } else {
                ch.getInventory().setContents(content);
            }
            
//Ostrov.log("paste i=");
            //bs.update();
            
        } else if (bsString.startsWith("CreatureSpawner=") && bs instanceof CreatureSpawner) {
            final CreatureSpawner crs = (CreatureSpawner) b.getState();
                crs.setSpawnedType(EntityType.valueOf(bsString.replaceFirst("CreatureSpawner=", "")));
                crs.setSpawnCount(2);
                crs.setSpawnRange(10);
                crs.setMinSpawnDelay(100);
                crs.setMaxSpawnDelay(400);
                crs.setRequiredPlayerRange(40);
                crs.setMaxNearbyEntities(8);
                crs.update();
        }
        
    }
    

    public XYZ getCurrentXYZ() {
        return xyz;
    }  





    
}
