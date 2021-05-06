package ru.komiss77.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;


   
    
    
    
 public class DonatEffect {


    
    public static void display(final Location location) {
        
        new BukkitRunnable() {
            int count = 20;
            @Override
            public void run() {
                dislay(location.clone().add(0, count/2, 0));
                count--;
                if (count==0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Ostrov.instance, 1, 10);
    } 
     
     
    private static void dislay (final Location location) {
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
                
                location.add(v);
                location.getWorld().spawnParticle(Particle.FALLING_DUST, location, 1, 0, 0, 0, Material.matchMaterial(ColorUtils.randomDyeColor().toString()+"_WOOL").createBlockData());
                //.spawnParticle(Particle.FLAME, currentLocation, 1, 0, 0, 0);
                location.subtract(v);
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
    
    
    
}   
    
