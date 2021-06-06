package builder;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Managers.Cuboid;
import ru.komiss77.Managers.WE;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtils;


public class PasteJob  implements Runnable {

    public final CommandSender cs;
    private final Schematic schem;//public final String schemName;
    public final Location loc;
    //private final boolean deleteFile;
    
    private Cuboid cuboid;
    private Iterator <Block> cuboidIterator;
    //private final HashMap<String, Material> blockMap;
    private double cuboidSize = -1;
    private final BukkitTask task;   
    public boolean pause;
    
    private int checked;
    private int current;
    private int ticks;
    public int percent;
    
    private Block block;
    private int xyz;
    
    public final boolean pasteAir;

    public PasteJob(final CommandSender cs, final Location loc, final Schematic schem, final boolean pasteAir) {
        
        this.cs = cs;
        this.loc = loc;
        this.schem = schem;
        this.pasteAir = pasteAir;
       // this.deleteFile = deleteFile;
       // this.onDone = onDone;
        
        //cuboid = new Cuboid ( loc, schem.sizeX, schem.sizeY, schem.sizeZ );
        //cuboidIterator = cuboid.iterator(loc.getWorld()); //в кубоиде отсчёт по местности
        //blockMap = schem.blockMap;
        //cuboidSize = (double)cuboid.getSize();
//System.out.println("builder.PasteJob.<init>()"+"sh="+this.schem.name+" block="+this.schem.blockMap.size());        
        task = Bukkit.getScheduler().runTaskTimer(Ostrov.instance, this, 1, 1);
    }
    
    
    
    
    
    
    
    
    
    @Override
    public void run() {
        
        if (pause || WE.wait(task.getTaskId())) return;
        
        if (!schem.ready) return;
        
        if (cuboidSize==-1) {
            cuboid = new Cuboid ( loc, schem.sizeX, schem.sizeY, schem.sizeZ );
            cuboidIterator = cuboid.iterator(loc.getWorld());
            cuboidSize = cuboid.getSize();
        }
        
        WE.currentTask = task.getTaskId();

        //for( ; cuboidIterator.hasNext() && current < WE.getBlockPerTick(); ++current) {
        while( cuboidIterator.hasNext() && current < WE.getBlockPerTick()) {
            block = cuboidIterator.next();

            //создаём относительную строку
            xyz = (block.getLocation().getBlockX()-loc.getBlockX())<<19 | (block.getLocation().getBlockY()-loc.getBlockY())<<11 | (block.getLocation().getBlockZ()-loc.getBlockZ());
//System.out.println("xyz="+xyz);        

            if (schem.blocks.containsKey(xyz)) {
                if (schem.blockDatas.containsKey(xyz)) { //есть блокдата
                    if (schem.blocks.get(xyz) == block.getType()) { //тип такой же - обновить блокдату?
                        if (schem.blockDatas.get(xyz)!=block.getBlockData()) { //сравнить блокдату??
                            block.setBlockData(schem.blockDatas.get(xyz), false); 
                            current++;
                        }
                    } else { //тип разный - поставить тип и дату
                        block.setType(schem.blocks.get(xyz), false);
                        block.setBlockData(schem.blockDatas.get(xyz), false);
                        current++;
                    }
                } else if (schem.blocks.get(xyz) != block.getType()) { //блокдатф не запомнено - заменить если не совпадает тип
                    block.setType(schem.blocks.get(xyz), false);
                    current++;
                }
                
                if (schem.blockStates.containsKey(xyz)) {
                    setBlockState(block, schem.blockStates.get(xyz));
                    current++;
                }
                
                //if (schem.blocks.get(xyz) == block.getType() && !schem.blockDatas.containsKey(xyz)) {
                    //материал слвпадает, блокдаты нет - пропускаем??
                //} else {
                    
                //}
                //if (schem.blocks.get(xyz) != block.getType()) {
                //    block.setType(schem.blocks.get(xyz), false);
                //}
                //schem.blockMap.remove(xyz); //облегчаем массив
            } else if (block.getType() != Material.AIR) { //нет с сохрвнении значит тут воздух
                if (pasteAir) {
                    block.setType(Material.AIR);
                    current++;
                }
            }
        }

        checked += current;
        current = 0;
        ++ticks;
        if (ticks%5 == 0 && cs!=null) {
            percent = (int)((double)checked / cuboidSize * 100.0D);
            if ( cs instanceof Player) {
                ApiOstrov.sendActionBarDirect(Bukkit.getPlayer(cs.getName()), "§eвставка "+schem.getName()+": §f"+percent+"%");
            } else {
                if (ticks%20 == 0) cs.sendMessage( "§eвставка "+schem.getName()+": §f"+percent+"%");
            }
            //ticks = 0;
        }

        if (!cuboidIterator.hasNext()) {
            
            cancel();

            for (Entity e : loc.getWorld().getEntities()) {
              //if ( cuboid.contains(e.getLocation()) && (e instanceof LivingEntity || e.getType()==EntityType.DROPPED_ITEM) && e.getType()!=EntityType.PLAYER) e.remove();
                if ( cuboid.contains(e.getLocation()) && e.getType()!=EntityType.PLAYER) e.remove();
            }
            
           // if (deleteFile) {
          //      final File regionDataFile = new File( (folderPath==null || folderPath.isEmpty()) ? Ostrov.instance.getDataFolder() + "/schematics" : folderPath, name + ((extension==null || extension.isEmpty())? ".schem" : extension));

          //  }
            
            if (cs!=null) {
               // if ( Bukkit.getPlayer(cs.getName())!=null ) {
               //     ApiOstrov.sendActionBarDirect(Bukkit.getPlayer(cs.getName()), "§eВыполнено: "+100+"%");
                //} else {
                    cs.sendMessage("§aВставка закончена, сущности очищены");
                //}
            }

           // if (onDone!=null) onDone.run();
        }
    }

    
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
//System.out.println("paste setBlockState "+bsString);
        BlockState bs =  b.getState();
        if (bsString.startsWith("Inventory=") && bs instanceof InventoryHolder) {
            //final Inventory inv = ((InventoryHolder)bs).getInventory();
            bsString = bsString.replaceFirst("Inventory=", "");
            final Container ch = (Container) b.getState();
            //final int size = ch.getInventory().getSize();
            
            ItemStack[] content = new ItemStack[ch.getInventory().getSize()];
            String[] split = bsString.split(",");
            for (int i=0; i<split.length; i++) {
                if (i>content.length) break;
                if (split[i].equals("null")) {
                    //inv.setItem(i, new ItemStack(Material.AIR));
                } else {
                    content[i] = ItemUtils.getItemStackFromString(split[i], ";");
                }
//System.out.println("paste i="+i+" s="+split[i]+" is="+content[i]);
            }
            ch.getInventory().setContents(content);
//System.out.println("paste i=");
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
    
}
