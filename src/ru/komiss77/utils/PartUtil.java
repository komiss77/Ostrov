package ru.komiss77.utils;

import io.papermc.paper.math.FinePosition;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;

public class PartUtil {

    public static void trail(final Location loc, final Vector dir, final int amount, final FinePosition offset, final Color color, final int time) {
//        Bukkit.getConsoleSender().sendMessage("dir-" + dir.toString());
        final int tmD4 = time >> 2;
        final double X = loc.getX(), Y = loc.getY(), Z = loc.getZ();
        double ox = randUns(offset.x()), oy = randUns(offset.y()), oz = randUns(offset.z());
        for (int i = 0; i != amount; i++) {
            switch (i & 3) {case 1 -> ox = randUns(offset.x()); case 2 -> oy = randUns(offset.y()); default -> oz = randUns(offset.z());}
            final Location start = new Location(loc.getWorld(), X + ox, Y + oy, Z + oz);
            start.getWorld().spawnParticle(Particle.TRAIL, start, 1, new Particle.Trail(new Location(loc.getWorld(), start.x() + dir.getX(),
                start.y() + dir.getY(), start.z() + dir.getZ()), color, time + (Ostrov.random.nextInt(3) - 1) * tmD4));
        }
    }

    private static double randUns(final double n) {
        return Ostrov.random.nextDouble(n * 2d) - n;
    }

}
