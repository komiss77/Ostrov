package ru.komiss77.Commands;

import java.util.HashMap;
import org.bukkit.Bukkit;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;

import ru.komiss77.Ostrov;
import ru.komiss77.utils.WGutils;
import ru.ostrov77.factions.ApiFactions;



public class Tpr {
	
    private static final HashMap<String,BukkitTask> tpData = new HashMap<>();
    
    //private final int center_x,center_z;
    //public static Random random;
    //static int minRange;
    //static int maxRange;
    //public Tpr() {
  //      center_x
   // }

   // public static void Init() {
        //playerlock = new HashSet();
        //random = new Random();
        //minRange = 100;
        //maxRange = ((DedicatedServer)((CraftWorld)world).getHandle().getMinecraftServer()).propertyManager.getInt("max-world-size", 500);
        //maxRange = (int) ((CraftWorld)world).getWorldBorder().getSize();
        //if (minRange>maxRange) minRange=maxRange;
   // }
    
    
    
    public static void runCommand(final Player p){

        //if ( !p.hasPermission("ostrov.tpr.free")) {
        //    if (ApiOstrov.moneyGetBalance(p.getName())<100) {
        //        p.sendMessage("§cНедостаточно денег для перемещения! Стоимость: 100 лони");
        //        return;
        //    }
        //}


        if (p.getWorld().getEnvironment() != World.Environment.NORMAL ) {
            p.sendMessage( "§cТелепорт работает только в обычном мире!");
            return;
        } 
        if (tpData.containsKey(p.getName())) {
            p.sendMessage( "§cДля Вас уже ищется место для телепорта!");
        }

        //p.sendMessage("§bТелепортер ищет безопасное место для Вас...");


/*
    Это устанавливает максимально возможный размер в блоках, выраженный в радиусе, который может получить мировая граница.
        Установка большей границы мира приводит к успешному выполнению команд, 
        но фактическая граница не выходит за пределы этого ограничения блока.
        Установка max-world-size выше значения по умолчанию, похоже, ничего не дает.
     Установка max-world-size на 1000 позволяет игроку иметь границу мира 2000 × 2000.
     Установка max-world-size на 4000 дает игроку границу мира 8000 × 8000. 
        https://minecraft.fandom.com/wiki/World_border
        */

        //вычисляем максимум +/- для x,z - РАДИУС!!! 
        
        //для каждой команды все параметры внутри, или могут запускать в разных мирах и подменятся параметры!
        
        tpData.put( p.getName(), new BukkitRunnable() {
            
            final int center_x=p.getWorld().getWorldBorder().getCenter().getBlockX();
            final int center_z=p.getWorld().getWorldBorder().getCenter().getBlockZ();
            
            final int worldDiameter = (int) p.getWorld().getWorldBorder().getSize() < Bukkit.getServer().getMaxWorldSize() ? ((int) p.getWorld().getWorldBorder().getSize()) : Bukkit.getServer().getMaxWorldSize() ;//VM.getNmsServer().getMaxWorldSize(p.getWorld());//propertyManager.getInt("max-world-size", 500);
            //final int max_wb = (int) p.getWorld().getWorldBorder().getSize();
//System.out.println("getWorldBorder="+ p.getWorld().getWorldBorder().getSize()+ " getMaxWorldSize="+Bukkit.getServer().getMaxWorldSize());        
            //final int maxRange;
            //if (max_size<max_wb) maxRange=max_size;
            //else maxRange = max_wb;

            //вычисляем минимум +/- для x,z
            final int minFindRadius = (worldDiameter/2)/50; //при мире 5к даст для поиска - дельтф будет +/- ( рандом от min до max)100, при 500 даст 10
            final int maxFindRadius = worldDiameter/2 - minFindRadius;  // - minFindRadius чтобы нге прижимало к границе

//System.out.println("worldDiameter="+ worldDiameter+" minFindRadius="+ minFindRadius+" maxFindRadius="+ maxFindRadius);        

            final int xMax = center_x+maxFindRadius;
            final int xMin = center_x-maxFindRadius;
            final int zMax = center_z+maxFindRadius;
            final int zMin = center_z-maxFindRadius;

//System.out.println("xMax="+ xMax+" xMin="+ xMin+" zMax="+ zMax+" zMin="+ zMin);        
            final int x = p.getLocation().getBlockX();
            final int y = p.getLocation().getBlockY();
            final int z = p.getLocation().getBlockZ();
            
            final String name = p.getName();
            int find_try=100; //5 секунд
            int lps = 10; //10 локаций в тик
            int find_x, find_z;
            Location loc;
            
            
                @Override
                public void run() {
     System.out.println("");                
//System.out.println("getWorldBorder="+ p.getWorld().getWorldBorder().getSize()+ " getMaxWorldSize="+Bukkit.getServer().getMaxWorldSize());        
//System.out.println("worldDiameter="+ worldDiameter+" minFindRadius="+ minFindRadius+" maxFindRadius="+ maxFindRadius);        
//System.out.println("center_x="+ center_x+" center_z="+ center_z); 
//System.out.println("xMax="+ xMax+" xMin="+ xMin+" zMax="+ zMax+" zMin="+ zMin); 
    
                    if (p==null || !p.isOnline() || p.isDead()) {
                        this.cancel();
                        tpData.remove(name);
                        return;
                    }
                    
                    if (find_try==0) {
                        p.sendMessage("§bТелепортер не смог найти подходящее место! Попробуйте позже..");
                        this.cancel();
                        tpData.remove(name);
                    }
                    
                    if (p.getLocation().getBlockX()!=x || p.getLocation().getBlockY()!=y || p.getLocation().getBlockZ()!=z) {
                        ApiOstrov.sendActionBarDirect(p, "§cТП отменяется!");
                        this.cancel();
                        tpData.remove(name);
                        return;
                    }
                    
                    
                    lps=10;
                    while(lps>0) {
                        
                        //find_x = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_x+minRange, center_x+maxRange) : - ApiOstrov.randInt(center_x+minRange, maxRange-center_x);
                        //find_z = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_z+minRange, center_z+maxRange) : - ApiOstrov.randInt(center_z+minRange, maxRange-center_z);
                        find_x = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_x+minFindRadius, xMax) : ApiOstrov.randInt(xMin, center_x-minFindRadius );
                        find_z = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_z+minFindRadius, zMax) : ApiOstrov.randInt(zMin, center_z-minFindRadius );
//System.out.println("-TPR find "+find_try+" lps="+lps+"  maxFindRadius="+maxFindRadius+"  find_x="+find_x+"   find_z="+find_z);
                        
                        //loc=p.getWorld().getBlockAt(find_x, 65, find_z).getLocation();
                        
                        
                        
                        //if (Ostrov.random.nextBoolean()) find_x=0-find_x;
                        //if (Ostrov.random.nextBoolean()) find_z=0-find_z;
                        //find_x=find_x+center_x;
                        //find_z=find_z+center_z;

                        loc=p.getWorld().getBlockAt(find_x, 65, find_z).getLocation();
                        
                        if (Ostrov.getWorldGuard()!=null) {
                            if (!WGutils.canBuild(p, loc)) {
                                lps--;
                                continue;
                            }
                        }
                        
                        if (Ostrov.apiFactions!=null) {
                            if (ApiFactions.geFaction(loc)!=null) {
                                lps--;
                                continue;
                            }
                        }
                        
                        
                        switch (loc.getBlock().getBiome()) {
                            case OCEAN:
                            case FROZEN_OCEAN:
                            case DEEP_OCEAN:
                            case WARM_OCEAN:
                            case LUKEWARM_OCEAN:
                            case COLD_OCEAN:
                            case DEEP_WARM_OCEAN:
                            case DEEP_LUKEWARM_OCEAN:
                            case DEEP_COLD_OCEAN:
                            case DEEP_FROZEN_OCEAN:
                                lps--;
                                continue;
                            default:
                                break;
                        }
                        
                        loc.setY(p.getWorld().getHighestBlockYAt(loc));

                        if (!loc.getChunk().isLoaded()) loc.getChunk().load();
                        
                        this.cancel();
                        tpData.remove(name);
                        ApiOstrov.teleportSave(p, loc, true);
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 5);
                        break;
                    }
                    
                    ApiOstrov.sendActionBarDirect(p, "§eСохраняйте неподвижность, ищем! §b"+100);

                }
            }.runTaskTimer(Ostrov.instance, 1, 1)
        );










          /*      new BukkitRunnable() {
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
                
                */
                
                
                
                
                
                
                
                
                
                

            

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
