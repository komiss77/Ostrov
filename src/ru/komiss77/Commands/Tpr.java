package ru.komiss77.Commands;

import java.util.Random;
import java.util.HashSet;
import java.util.Set;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import ru.komiss77.ApiOstrov;

import ru.komiss77.Ostrov;
import ru.komiss77.utils.WGutils;
import ru.komiss77.version.VM;



public class Tpr {
	
    public static Set playerlock;
    public static Random random;
    //static int minRange;
    //static int maxRange;

    public static void Init() {
        playerlock = new HashSet();
        random = new Random();
        //minRange = 100;
        //maxRange = ((DedicatedServer)((CraftWorld)world).getHandle().getMinecraftServer()).propertyManager.getInt("max-world-size", 500);
        //maxRange = (int) ((CraftWorld)world).getWorldBorder().getSize();
        //if (minRange>maxRange) minRange=maxRange;
    }
    
    
    
    public static void runCommand(final Player player){
            
            if ( !player.hasPermission("ostrov.tpr.free")) {
                if (ApiOstrov.moneyGetBalance(player.getName())<100) {
                    player.sendMessage("§cНедостаточно денег для перемещения! Стоимость: 100 лони");
                    return;
                }
            }
        
        
            if (player.getWorld().getEnvironment() == World.Environment.NETHER ) {
                player.sendMessage( "§cТелепорт не работает в аду!");
            } else if (playerlock.contains(player.getName())) {
                player.sendMessage( "§cДля Вас уже ищется место для телепорта!");
            }  else {
                playerlock.add(player.getName());
                player.sendMessage("§bТелепортер ищет безопасное место для Вас...");

                final int center_x=player.getWorld().getWorldBorder().getCenter().getBlockX();
                final int center_z=player.getWorld().getWorldBorder().getCenter().getBlockZ();
            
                final int max_size = VM.getNmsServer().getMaxWorldSize(player.getWorld());//propertyManager.getInt("max-world-size", 500);
                final int max_wb = (int) player.getWorld().getWorldBorder().getSize();
                final int maxRange;
                if (max_size<max_wb) maxRange=max_size;
                else maxRange = max_wb;
                
                
                final int minRange;
                if (maxRange<100) minRange=maxRange;
                else minRange = 100;
//System.out.println("maxRange="+maxRange);
        
                new BukkitRunnable() {
                    int find_try=0;
                    int find_x, find_z;
                    Location find_loc;
                    @Override
                    public void run() {

//System.out.println("1111 find "+find_try);
                        if(player==null || !player.isOnline() || player.isDead()) {
                            playerlock.remove(player.getName());
//System.out.println("2222 player==null?"+player==null+"   ");
                            this.cancel();
                        }

                        find_try++;
                        if (find_try>=100) {
                            player.sendMessage("§bТелепортер не смог найти безопасное место! Попробуйте позже..");
                            playerlock.remove(player.getName());
                            this.cancel();
                        }

                        find_x=minRange+random.nextInt(maxRange - minRange);
                        find_z=minRange+random.nextInt(maxRange - minRange);
                        if (random.nextBoolean()) find_x=0-find_x;
                        if (random.nextBoolean()) find_z=0-find_z;
                        find_x=find_x+center_x;
                        find_z=find_z+center_z;
System.out.println("-TPR find "+find_try+"  maxRange="+maxRange+"  find_x="+find_x+"   find_z="+find_z);

                        find_loc=new Location(player.getWorld(), find_x, 1, find_z);
                        if (!find_loc.getChunk().isLoaded()) find_loc.getChunk().load();
                        find_loc.setY(player.getWorld().getHighestBlockYAt(find_loc));
                        //highest.getType() == Material.AIR || highest.getType() == Material.WATER || highest.getType() == Material.STATIONARY_WATER || highest.getType() == Material.STATIONARY_LAVA || highest.getType() == Material.WEB || highest.getType() == Material.LAVA || highest.getType() == Material.CACTUS || highest.getType() == Material.ENDER_PORTAL || highest.getType() == Material.PORTAL
                        if (find_loc.clone().subtract(0,1,0).getBlock().getType()==Material.WATER ) find_loc=null;
                        if ( find_loc!=null && !ApiOstrov.isLocationSave(player, find_loc) ) find_loc=null;
                        //if ( find_loc!=null && Ostrov.getWorldGuard()!=null && !Ostrov.worldguard.canBuild(player, find_loc) ) find_loc=null;
                        if ( find_loc!=null && Ostrov.getWorldGuard()!=null) {
                            if (!WGutils.canBuild(player, find_loc)) find_loc=null;
                        } 
                        if (find_loc!=null) {
                            this.cancel();
                            player.teleport(find_loc.clone().add(0.5,1,0.5), PlayerTeleportEvent.TeleportCause.COMMAND);
                            player.sendMessage("§fВы переместились в точку §6" + find_loc.getBlockX() + " " + find_loc.getBlockY() + " " + find_loc.getBlockZ() + " !");
                            ApiOstrov.moneyChange(player.getName(), -100, "случайный телепорт");
                            playerlock.remove(player.getName());
                        }
                    }
                }.runTaskTimer(Ostrov.instance, 1, 1);

            }

/*

                            
                            int xold;
                            int chunksum;
                            int chunksumold;

                            

                                int zold = 0;

                                xold = 0;
                                chunksum = 0;
                                chunksumold = 0;

                                for (int chunkcount = 0; chunkcount < 10 && chunksum < 81; ++chunkcount) {
                                    int count = 0;

                                    int z;
                                    int x;
                                    int j;
                                    int xcheck;

                                    
                                    int find_x, find_z;
                                    Location find_loc;
                                    
                                    for ( int find=0; find<100; find++) {
                                    
                                        find_x=minRange+random.nextInt(maxRange - minRange);
                                        find_z=minRange+random.nextInt(maxRange - minRange);
                                        if (random.nextBoolean()) find_x=0-find_x;
                                        if (random.nextBoolean()) find_z=0-find_z;
                                        find_loc=new Location(player.getWorld(), find_x, 0, find_z);
                                        if (!find_loc.getChunk().isLoaded()) find_loc.getChunk().load();
                                        find_loc.setY(player.getWorld().getHighestBlockYAt(find_loc));
                                        
                                        if ( !Ostrov.Teleport_check_location(find_loc) ) find_loc=null;
                                        if ( find_loc!=null && Ostrov.worldguard && !WGBukkit.getPlugin().canBuild(player, find_loc) ) find_loc=null;
                                    } 
                                    
                                    
                                    
                                    do {
                                        ++count;
                                        Random i = new Random();

                                        j = minRange + i.nextInt(maxRange - minRange);
                                        xcheck = minRange + i.nextInt(maxRange - minRange);
                                        
                                        if (i.nextBoolean()) {
                                            j = 0 - j;
                                        }

                                        if (i.nextBoolean()) {
                                            xcheck = 0 - xcheck;
                                        }

                                        x = (int) player.getLocation().getX() + j;
                                        z = (int) player.getLocation().getZ() + xcheck;
                                        
                                        if (count == 100) {
                                            player.sendMessage("§bТелепортер не смог найти безопасное место! Попробуйте позже..");

                                            int n = count - 1;
                                            int k = count - 1;
                                            int l = checkstat[count - 1];

                                            checkstat[k] = checkstat[count - 1] + 1;
                                            checkstat[n] = l;
                                            playerlock.remove(player.getName());
                                            return;
                                        }
                                        
                                    } while (!teleportCheck(player, x, z, forceBlocks, forceRegions));
                                    

                                    ++checkstat[count - 1];
                                    
                                    if (chunkcount == 0) {
                                        xold = x;
                                        zold = z;
                                    }

                                    if (!Ostrov.worldguard) break;

                                    chunksum = 0;

                                    for (int i1 = -4; i1 <= 4; ++i1) {
                                        for (j = -4; j <= 4; ++j) {
                                            xcheck = x + i1 * 16;
                                            int zcheck = z + j * 16;
                                            Location location = new Location(player.getWorld(), (double) xcheck, (double) player.getWorld().getHighestBlockYAt(xcheck, zcheck)+1, (double) zcheck);

                                            if ( checkforRegion(player, location, false)) {
                                                ++chunksum;
                                            }
                                        }
                                    }

                                  //  this.getLogger().log(Level.FINE, "RandomTeleport ({0}. try) found {1} unprotected chunks around the location {2}/{3}", new Object[]{chunkcount, chunksum, x, z});
                                    if (chunksum > chunksumold) {
                                        xold = x;
                                        zold = z;
                                        chunksumold = chunksum;
                                    }
                                }

                                teleportPlayer(player, xold, zold); 
                                
                                playerlock.remove(player.getName());
                        }    */                    
	}
        
        
        
        
        
        
   /*   
    
    private static void teleportPlayer(Player player, int x, int z) {

        if (player != null && player.isOnline() ) {
            int yTp = player.getWorld().getHighestBlockYAt(x, z);

            player.teleport(new Location(player.getWorld(), (double) x + 0.5D, (double) yTp + 0.5D, (double) z + 0.5D), PlayerTeleportEvent.TeleportCause.COMMAND);
            player.sendMessage("§fВы переместились в точку §6" + (int)x + " " + (int)yTp + " " + (int)z + " !");
        }
    }

     
    
    
    private static boolean teleportCheck(Player player, int x, int z, boolean forceBlocks, boolean forceRegions) {
        //int y = player.getWorld().getHighestBlockYAt(x, z);
        Block highest = player.getWorld().getBlockAt(x, player.getWorld().getHighestBlockYAt(x, z) - 1, z);

        if (!forceBlocks) {
            switch (player.getWorld().getEnvironment()) {
            case NETHER:
                return false;

            case THE_END:
                if (highest.getType() == Material.AIR || highest.getType() == Material.WATER || highest.getType() == Material.STATIONARY_WATER || highest.getType() == Material.STATIONARY_LAVA || highest.getType() == Material.WEB || highest.getType() == Material.LAVA || highest.getType() == Material.CACTUS || highest.getType() == Material.ENDER_PORTAL || highest.getType() == Material.PORTAL) {
                    return false;
                }

            case NORMAL:
            default:
                if (highest.getType() != Material.SAND && highest.getType() != Material.GRAVEL && highest.getType() != Material.DIRT && highest.getType() != Material.GRASS) {
                    return false;
                }
            }
            
        } else if (highest.getType() == Material.AIR || highest.getType() == Material.WATER || highest.getType() == Material.STATIONARY_WATER || highest.getType() == Material.STATIONARY_LAVA || highest.getType() == Material.WEB || highest.getType() == Material.LAVA || highest.getType() == Material.CACTUS || highest.getType() == Material.ENDER_PORTAL || highest.getType() == Material.PORTAL) {
            return false;
        }

        return checkforRegion(player, highest.getLocation(), forceRegions);
    }

   
    
    
    
private static boolean checkforRegion(Player player, Location location, Boolean forceRegions) {
    
    if (!Ostrov.worldguard) return true;
    
        if (forceRegions ) {
            return true;
        } else {
            //Block block = location.getWorld().getBlockAt(location);
            return WGBukkit.getPlugin().canBuild(player, location.getWorld().getBlockAt(location));
        }
    }







    public static boolean isNumeric(String str) {
        char[] arr$ = str.toCharArray();
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            char c = arr$[i$];

            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    
   
   
    private static class SyntheticClass_1 {

         final static int[] $SwitchMap$org$bukkit$World$Environment = new int[World.Environment.values().length];

         static {
            try {
                SyntheticClass_1.$SwitchMap$org$bukkit$World$Environment[World.Environment.NETHER.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
            }

            try {
                SyntheticClass_1.$SwitchMap$org$bukkit$World$Environment[World.Environment.THE_END.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
            }

            try {
                SyntheticClass_1.$SwitchMap$org$bukkit$World$Environment[World.Environment.NORMAL.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror2) {
            }

        }
    }       
   */     
        

}
