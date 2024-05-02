package ru.komiss77.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import ru.komiss77.ApiOstrov;




public class BlockstateCmd {

     public static boolean execute(Player p, String[] arg) {
        if ( !ApiOstrov.isLocalBuilder(p, true)) return false;
        
            switch (arg.length) {
                
                
                case 1:
                    if ( ApiOstrov.isInteger(arg[0]) ) {
                        int r = Integer.valueOf(arg[0]);
                        final Map<String,Integer>count=new HashMap<>();
                        
                        for (Chunk chunk: p.getWorld().getLoadedChunks()) {
                            for (BlockState bs:chunk.getTileEntities()) {
                                if (count.containsKey(bs.getType().toString())) {
                                    count.put(bs.getType().toString(), count.get(bs.getType().toString())+1);
                                } else count.put(bs.getType().toString(), 1);
                            }
                        }
                        
                        
                        if (count.isEmpty()) {
                            p.sendMessage( "§eВ радиусе §b"+r+" §eничего не найдено!" );
                            return true;
                        }
                        
                        
                        p.sendMessage( "§eВ радиусе §b"+r+" §eнайдено: §b"+entriesSortedByValues(count));
                        return true;
                    } else p.sendMessage( "радиус должен быть числом!");
                    break;
                    
                    
                case 2:
                    if ( ApiOstrov.isInteger(arg[0]) ) {
                        
                        Material mat = Material.matchMaterial(arg[1]);
                        if (mat==null) {
                            p.sendMessage("§cнет такого материала!");
                            return true;
                        }
                        
                        int r = Integer.valueOf(arg[0]);
                        final Map <String,Integer>count=new HashMap<>();
                        String chunk_coord;
                        Location max_loc=null;
                        int max=0;
                        
                        for (Chunk chunk: p.getWorld().getLoadedChunks()) {
                            for (BlockState bs:chunk.getTileEntities()) {
                                if (bs.getType()==mat) {
                                    chunk_coord = chunk.getX()+"x"+chunk.getZ();
                                    if (count.containsKey(chunk_coord)) {
                                        count.put(chunk_coord, count.get(chunk_coord)+1);
                                        if (count.get(chunk_coord)>max) {
                                            max_loc=p.getWorld().getHighestBlockAt(chunk.getBlock(7, 10, 7).getLocation()).getLocation();
                                            max=count.get(chunk_coord);
                                        }
                                    } else {
                                        count.put(chunk_coord, 1);
                                    }
                                }
                            }
                        }
                        
                        if (count.isEmpty()) {
                            p.sendMessage( "§eВ радиусе §b"+r+" §eдля типа §b"+mat.toString()+" §eничего не найдено!" );
                            return true;
                        }
                        
                        
                        p.sendMessage( "§eВ радиусе §b"+r+" §eдля типа §b"+mat.toString()+" §eнайдено: §f"+entriesSortedByValues(count) );
                        if (max_loc==null) {
                            p.sendMessage( "§cкоордината не определена!");
                        } else {
                            p.sendMessage(Component.text("§eНаибольшее колл-во в чанке "+max_loc.getChunk().getX()+"*"+max_loc.getChunk().getZ()+", клик-ТП")
                            	.clickEvent(ClickEvent.runCommand("/tp "+p.getName()+" "+max_loc.getBlockX()+" "+max_loc.getBlockY()+" "+max_loc.getBlockZ())));
                        }
                        return true;
                    } else {
                        p.sendMessage( "§cрадиус должен быть числом!");
                    }
                    break;
                    
                    
                default:
                    p.sendMessage( "§centity <радиус> [группа]");
                    break;
                    
                    
            }
        return false;
    }
     
     
     
     
     
    private static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<>(
            (Map.Entry<K,V> e1, Map.Entry<K,V> e2) -> {
                int res = e1.getValue().compareTo(e2.getValue());
                return res != 0 ? res : 1;
        });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }     
     
     
     
     
     
     
    
}
