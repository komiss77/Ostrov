package ru.komiss77.utils;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;


   
    
    
    
 public class DonatEffect {
     
    //как в worldborder при откидывании
    public static void showWBEffect(final Location loc) { 
        final World world = loc.getWorld();
        world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
        world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
        world.playEffect(loc, Effect.SMOKE, 4);
        world.playEffect(loc, Effect.SMOKE, 4);
        world.playEffect(loc, Effect.SMOKE, 4);
        world.playEffect(loc, Effect.GHAST_SHOOT, 0);
    }
	
    //не менять название! ссылаются плагины
    public static void display(final Location location) {
        
        new BukkitRunnable() {
            int count = 20;
            @Override
            public void run() {
                displayColorTube(location.clone().add(0, count/2, 0));
                count--;
                if (count==0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Ostrov.instance, 1, 10);
    } 
     
    
    
    public static void displayHelix(final Location loc) {
        
        new BukkitRunnable() {
            int count = 10;
            double phi = 0;
            @Override
            public void run() {
                phi += Math.PI/16;
                double x; double y; double z;
                for(double t = 0; t<= 1.75*Math.PI; t += Math.PI/16) {
                    for(double i = 0; i< 2; i+=1) {
                        x = Math.cos(t + phi + i*Math.PI);
                        y = 0.5*t;
                        z = Math.sin(t + phi + i*Math.PI);
                        loc.add(x,y,z);
                        //loc.getWorld().spawnParticle(Particle.BARRIER, loc, 1);
                        loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, 1, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());//display(particle, location);
                        loc.subtract(x,y,z);
                    }
                }
                count--;
                if (count==0) {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 0, 10);
        //}.runTaskAsynchronously(Ostrov.instance);
        
        
    }     
    
    //не исп.
    public static void displayTornado(final Location loc, final boolean in) {
        new BukkitRunnable() {
            double radius = in ? 2.043476540885901 : 0.1; //нисходящая спираль
            double y = in ? 4 : 0; //нисходящая спираль
            @Override
            public void run() {

                for (int t= 0; t <= 40; t++) {
                    y= in ? y-0.002 : y+0.002;
                    radius= in ? radius/1.0015 : radius*1.0015;
                    double x = radius * Math.cos(Math.pow(y, 2)*10);
                    double z = radius * Math.sin(Math.pow(y, 2)*10);
                    loc.add(x,y,z);
                    loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0);
                    loc.subtract(x,y,z);
                }
                if ( (in && y<=0) || y>=4) {
                    this.cancel();
                }           
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 0, 1);
       /* new BukkitRunnable() {
            
            double radius = 2.043476540885901; //нисходящая спираль
            double y = 4; //нисходящая спираль

            //double radius = 0.1; // восходящая спираль
            //double y = 0; // восходящая спираль
            
            @Override
            public void run() {
                
                 //восходящая спираль
              /*  for (int t= 0; t <= 60; t++) {
                    y+=0.002;
                    radius*=1.0015;
                    double x = radius * Math.cos(Math.pow(y, 2)*10);
                    double z = radius * Math.sin(Math.pow(y, 2)*10);
                    loc.add(x,y,z);
                    loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0);
                    loc.subtract(x,y,z);
                }
                
                if (y>=4) {
                    //System.out.println("radius="+radius);
                    this.cancel();
                }/
                
                //нисходящая спираль
                for (int t= 0; t <= 40; t++) {
                    y-=0.002;
                    radius/=1.0015;
                    double x = radius * Math.cos(Math.pow(y, 2)*10);
                    double z = radius * Math.sin(Math.pow(y, 2)*10);
                    loc.add(x,y,z);
                    loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0);
                    loc.subtract(x,y,z);
                }
                
                if (y<=0) { //нисходящая спираль
                //if (y>=4) {  // восходящая спираль
                    this.cancel();
                }           
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 0, 1);*/

        //for (double y = 0; y <= 4; y += 0.002) {
       /* double radius = 0.1;
        for (double y = 0; y <= 4; y += 0.002) {
            double x = (radius*=1.0015) * Math.cos(Math.pow(y, 2)*10);
            double z = radius * Math.sin(Math.pow(y, 2)*10);
            //particle.sendToLocation(loc.clone().add(x, y, z));
            loc.add(x,y,z);
            loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0);
            //loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, 1, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());//display(particle, location);
            loc.subtract(x,y,z);
        }*/
    }

    
    
    public static void displayGalaxy(final Location loc) {
        //Particle particle = Particle.FLAME;
        int strands = 8;
        int particles = 80;
        float radius = 10;
        float curve = 10;
        double rotation = Math.PI / 4;
        //int period = 10;
        //int iterations = 8;
        
        new BukkitRunnable() {
            int count = 20;
            @Override
            public void run() {
                
                for (int i = 1; i <= strands; i++) {
                    for (int j = 1; j <= particles; j++) {
                        float ratio = (float) j / particles;
                        double angle = curve * ratio * 2 * Math.PI / strands + (2 * Math.PI * i / strands) + rotation;
                        double x = Math.cos(angle) * ratio * radius;
                        double z = Math.sin(angle) * ratio * radius;
                        loc.add(x, 0, z);
                        //loc.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0, 0);//display(particle, location);
                        loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, 1, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());//display(particle, location);
                        loc.subtract(x, 0, z);
                    }
                }
                count--;
                if (count==0) {
                    this.cancel();
                }
                
            }
        }.runTaskTimer(Ostrov.instance, 1, 10);
       
        
    }  


    
    
    private static void displayColorTube (final Location loc) {
        int circles = 36;
        int particlesCircle = 5;
        float radiusDonut = 2;
        float radiusTube = .5f;
        double xRotation=0, yRotation=0, zRotation = 0;
        
        Vector v = new Vector();
        for (int i = 0; i < circles; i++) {
            double theta = 2 * Math.PI * i / circles;
            for (int j = 0; j < particlesCircle; j++) {
                double phi = 2 * Math.PI * j / particlesCircle;
                double cosPhi = Math.cos(phi);
                v.setX((radiusDonut + radiusTube * cosPhi) * Math.cos(theta));
                v.setZ((radiusDonut + radiusTube * cosPhi) * Math.sin(theta));
                //v.setY((radiusDonut + radiusTube * cosPhi) * Math.sin(theta));
                v.setY(radiusTube * Math.sin(phi));
                //v.setZ(radiusTube * Math.sin(phi));

                rotateVector(v, xRotation, yRotation, zRotation);
                
                loc.add(v);
                //loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0);//display(particle, location);
                loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, 1, 0, 0, 0, Material.matchMaterial(TCUtils.randomDyeColor().toString()+"_WOOL").createBlockData());
                //.spawnParticle(Particle.FLAME, currentLocation, 1, 0, 0, 0);
                loc.subtract(v);
            }
        }
    }
    
    private static Vector rotateAroundAxisX(Vector v, double angle) {
        double y, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }

    private static Vector rotateAroundAxisY(Vector v, double angle) {
        double x, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    private static Vector rotateAroundAxisZ(Vector v, double angle) {
        double x, y, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }

    private static Vector rotateVector(Vector v, double angleX, double angleY, double angleZ) {
        rotateAroundAxisX(v, angleX);
        rotateAroundAxisY(v, angleY);
        rotateAroundAxisZ(v, angleZ);
        return v;
    } 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static void spawnRandomFirework(final Location loc) {
        final Firework firework = loc.getWorld().spawn(loc, Firework.class);
        final FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(FireworkEffect.builder().flicker(ApiOstrov.randBoolean()).withColor(Color.fromBGR(ApiOstrov.randInt(0,255), ApiOstrov.randInt(0,255), ApiOstrov.randInt(0,255))).withFade(Color.fromBGR(ApiOstrov.randInt(0,255), ApiOstrov.randInt(0,255), ApiOstrov.randInt(0,255))).with(FireworkEffect.Type.BALL).trail(ApiOstrov.randBoolean()).build());
        fireworkMeta.setPower(0);
        firework.setFireworkMeta(fireworkMeta);
    }
    
}   
    
