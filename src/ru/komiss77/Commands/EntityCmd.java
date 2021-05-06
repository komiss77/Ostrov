package ru.komiss77.Commands;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.menu.EntityByWorld;
import ru.komiss77.menu.EntityByWorlds;
import ru.komiss77.utils.inventory.SmartInventory;




public class EntityCmd {

    
    
    
    public static boolean execute (final Player p, final String[] arg) {
        
        if ( !p.hasPermission("ostrov.entity") && !ApiOstrov.isLocalBuilder(p, true)) return false;
        
        
        
        
        
        
                
                
                if (arg.length==0) {
                    SmartInventory.builder().id("EntityMain"+p.getName()). provider(new EntityByWorld(p.getWorld(), -1)). size(3, 9). title("§2Сущности "+p.getWorld().getName()).build() .open(p);
                    return true;
                }



                if ( arg.length==1) {
                    
                    if ( ApiOstrov.isInteger(arg[0])) {
                        
                        int r = Integer.valueOf(arg[0]);
                        SmartInventory.builder().id("EntityMain"+p.getName()). provider(new EntityByWorld(p.getWorld(), r)). size(3, 9). title("§2Сущности "+p.getWorld().getName()+" §1r="+r).build() .open(p);
                        return true;
                        
                    } else if (arg[0].equalsIgnoreCase("--server")) {
                        
                        if (!ApiOstrov.isLocalBuilder(p, false)) return false;
                        final StringBuilder sb = new StringBuilder();
                        sb.append("entity");
                        for (final World w : Bukkit.getWorlds()) {
//System.out.println("entity --server world="+w);
                            sb.append(" ");
                            sb.append(w.getName());
                        }
                        p.performCommand(sb.toString());
                        return true;
                    }
                    
                }
                
                
                final Set<World> worlds = new HashSet<>();
                for (int i=0; i<arg.length; i++) {
                    final World world = Bukkit.getWorld(arg[i]);
//System.out.println("++ i="+i+" arg="+arg[i]);
                    if (world!=null) {
                        worlds.add(world);
                    }
                    SmartInventory.builder().id("EntityWorlds"+p.getName()). provider(new EntityByWorlds(worlds)). size(6, 9). title("§2Сущности миров").build() .open(p);
                }

                    
               /* case 2:
                    if ( CMD.isNumber(arg[0]) ) {
                        EntityGroup group=null;
                        //if (NMSUtils.MobMeta.isMeta(arg[1].toUpperCase())) group=NMSUtils.MobMeta.valueOf(arg[1].toUpperCase());
                        if (VM.getNmsEntitygroup().isGroup(arg[1].toUpperCase())) group=VM.getNmsEntitygroup().byTag(arg[1]);
                        else {
                            //p.sendMessage("§cГруппы: CREATURE,MONSTER,AMBIENT,WATER_CREATURE,OTHER");
                            p.sendMessage("§cГруппы: "+EntityGroup.values());
                            return true;
                        }
                        
                        int r = Integer.valueOf(arg[0]);
                        HashMap <EntityType,Integer>count=new HashMap<>();
                        for (Entity e: p.getNearbyEntities(r, r, r)) {
                            if (group!=null) {
                                if (VM.getNmsEntitygroup().getEntytyType(e)==group) {
                                    if (count.containsKey(e.getType())) {
                                        count.put (e.getType(), count.get(e.getType())+1);
                                    } else count.put (e.getType(), 1);
                                }
                            } else {
                                if (count.containsKey(e.getType())) {
                                    count.put (e.getType(), count.get(e.getType())+1);
                                } else count.put (e.getType(), 1);
                            }
                        }
                        p.sendMessage( "§eВ радиусе §b"+r+ group==null?"":" §eдля группы §b"+group.toString()+" §eнайдены: §f"+count.toString() );
                        return true;
                    } else p.sendMessage( "§cрадиус должен быть числом!");
                    break;*/
        return true;
    }

}
