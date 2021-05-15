package ru.komiss77.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;







public class DelayTeleport {
    
    private static final HashMap<String,BukkitTask> tpData = new HashMap<>();
    private static final List<String> check = new ArrayList<>();
    
    
    public static void tp(final Player p, final Location toLoc, final int delay) {
        tp(p, toLoc, delay, "", false, false, null);
    }
    
    
    
    public static void tp(final Player p, final Location toLoc, final int delay, final String doneMsg, final boolean fromEffect, final boolean toEffect, final DyeColor color) {
        if (tpData.containsKey(p.getName()) && tpData.get(p.getName())!=null && !tpData.get(p.getName()).isCancelled()) {
            tpData.get(p.getName()).cancel();
        }
        if (fromEffect) playTpEffect(p.getLocation(), color);
        if (delay<=0) {
            ApiOstrov.teleportSave(p, toLoc, true);
            if (toEffect) playTpEffect(toLoc, color);
            return;
        }
        //if (delay>30) delay=30;
        final int x = p.getLocation().getBlockX();
        final int y = p.getLocation().getBlockY();
        final int z = p.getLocation().getBlockZ();

        tpData.put( p.getName(), new BukkitRunnable() {
                int sec = delay>30 ? 30 : delay;
                final String name = p.getName();
                @Override
                public void run() {
                    //final Player pl = Bukkit.getPlayer(name);
                    if (p==null || !p.isOnline() || p.isDead()) {
                        this.cancel();
                        tpData.remove(name);
                        return;
                    }
                    if (p.getLocation().getBlockX()!=x || p.getLocation().getBlockY()!=y || p.getLocation().getBlockZ()!=z) {
                        this.cancel();
                        ApiOstrov.sendActionBarDirect(p, "§cТП отменяется!");
                        tpData.remove(name);
                        return;
                    }
                    sec --;
                    if (sec==0) {
                        this.cancel();
                        ApiOstrov.teleportSave(p, toLoc, true);
                        if (toEffect) playTpEffect(toLoc, color);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2, 2), true);
                        //pl.setVelocity(pl.getVelocity().setY(0.2));
                        if (!doneMsg.isEmpty()) p.sendMessage(doneMsg);
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 5);
                        tpData.remove(name);
                    } else {
                        ApiOstrov.sendActionBarDirect(p, "§eСохраняйте неподвижность : §b"+sec);
                    }
                }
            }.runTaskTimer(Ostrov.instance, 0, 20)
        );
        
        /*check.clear();
        check.addAll(tpData.keySet());
        for (final String name:check) {
            if (Bukkit.getPlayer(name)==null) {
                if (tpData.containsKey(p.getName()) && tpData.get(p.getName())!=null && !tpData.get(p.getName()).isCancelled()) {
                    tpData.get(p.getName()).cancel();
                }
                tpData.remove(name);
            }
        }*/
        
    }

    
    
    private static void playTpEffect(final Location effectLoc, DyeColor color) {
        
            if (color==null) color = DyeColor.LIME;
            int circleElements = 20;
            double radius = 1.0;
            //double height2 = 1.0;
            //double circles = 15.0;
            //double fulltime = (double) WarpPowder.this.fullTeleportingTime;
            //double teleportingTime = WarpPowder.this.teleportingTime;

            //double perThrough = (Math.ceil((height/circles)*((fulltime*20)/circles))/20);

            Location loc =effectLoc.clone().add(0, 2.2, 0);

            //double y = (height2/circles)*through;
            
            for(int i = 0; i < 20; i++) {
                double alpha = (360.0/circleElements)*i;
                double x = radius * Math.sin(Math.toRadians(alpha));
                double z = radius * Math.cos(Math.toRadians(alpha));

                //Location particleFrom = new Location(loc.getWorld(), loc.getX()+x, loc.getY()+y, loc.getZ()+z);
                Location particleFrom = new Location(loc.getWorld(), loc.getX()+x, loc.getY(), loc.getZ()+z);
                particleFrom.getWorld().spawnParticle(Particle.FALLING_DUST, particleFrom, 1, 0, 0, 0, Material.valueOf(String.valueOf(color)+"_WOOL").createBlockData());
                //Location particleTo = new Location(tLoc.getWorld(), tLoc.getX()+x, tLoc.getY()+y, tLoc.getZ()+z);
                //Utils.createParticleInGame(game, "fireworksSpark", particleTo);
            }
    }

   /* private static void teleportSave(final Player p, final Location loc) {
        if (ApiOstrov.isLocationSave(p, loc)) {
            p.teleport(loc);
//System.out.println("1 loc="+loc);
        } else {
            Location loc2 = ApiOstrov.findNearestSaveLocation(loc);
            if (ApiOstrov.isLocationSave(p, loc2)) {
                p.teleport(loc);
//System.out.println("2 loc="+loc);
            } else {
                loc2 = loc.getWorld().getSpawnLocation();
                if (ApiOstrov.isLocationSave(p, loc2)) {
                    p.teleport(loc2);
//System.out.println("3 loc="+loc2);
                } else {
                    loc2.getBlock().getRelative(BlockFace.DOWN).setType(Material.GLASS);
                    loc2.getBlock().setType(Material.AIR);
                    loc2.getBlock().getRelative(BlockFace.UP).setType(Material.AIR);
//System.out.println("4 loc="+loc2);
                    p.teleport(loc2);
                }
            }
        }    
    }*/
    
    
    
    
}
