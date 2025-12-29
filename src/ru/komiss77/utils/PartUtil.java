package ru.komiss77.utils;

import java.util.Set;
import io.papermc.paper.math.FinePosition;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.modules.world.XYZ;

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
    }public enum particle {
        HEART("Сердечки", Material.REDSTONE_BLOCK, Particle.HEART),
        NOTE("Ноты", Material.NOTE_BLOCK, Particle.NOTE),
        HAPPY_VILLAGER("Изумруды", Material.EMERALD, Particle.HAPPY_VILLAGER),
        FLAME("Огонь", Material.BLAZE_POWDER, Particle.FLAME),
        ENCHANTED_HIT("Удар магии", Material.DIAMOND_AXE, Particle.ENCHANTED_HIT),
        CRIT("Удар", Material.ENCHANTED_BOOK, Particle.CRIT),
        ANGRY_VILLAGER("Злой Житель", Material.ARROW, Particle.ANGRY_VILLAGER),
        PORTAL("Портал", Material.OBSIDIAN, Particle.PORTAL),
        DUST("Редстоун", Material.REDSTONE, Particle.DUST),
        CLOUD("Дымок", Material.TNT, Particle.CLOUD),
        LAVA("Лава", Material.LAVA_BUCKET, Particle.LAVA),
        ENCHANT("Магия", Material.ENCHANTING_TABLE, Particle.ENCHANT),
        CAMPFIRE_SIGNAL_SMOKE("Сигнальный дым", Material.CAMPFIRE, Particle.CAMPFIRE_SIGNAL_SMOKE),
        END_ROD("Стержень энда", Material.END_ROD, Particle.END_ROD),
        SCULK_CHARGE_POP("Скалк", Material.SCULK_SENSOR, Particle.SCULK_CHARGE_POP),
        BUBBLE_COLUMN_UP("Пузыри", Material.SCULK_SHRIEKER, Particle.BUBBLE_COLUMN_UP),
        WITCH("Ведьма", Material.POTION, Particle.WITCH),
        ;

        public final String displayName;
        public final Material mat;
        public final Particle particle;


        private particle(final String displayName, final Material mat, final Particle particle) {
            this.displayName = displayName;
            this.mat = mat;
            this.particle = particle;
        }

    }


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
                displayColorTube(location.clone().add(0, count / 2, 0));
                count--;
                if (count == 0) {
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
                phi += Math.PI / 16;
                double x;
                double y;
                double z;
                for (double t = 0; t <= 1.75 * Math.PI; t += Math.PI / 16) {
                    for (double i = 0; i < 2; i += 1) {
                        x = Math.cos(t + phi + i * Math.PI);
                        y = 0.5 * t;
                        z = Math.sin(t + phi + i * Math.PI);
                        loc.add(x, y, z);
                        //loc.getWorld().spawnParticle(Particle.BARRIER, loc, 1);
                        loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, 1, 0, 0, 0, Material.REDSTONE_BLOCK.createBlockData());//display(particle, location);
                        loc.subtract(x, y, z);
                    }
                }
                count--;
                if (count == 0) {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 0, 10);
    }

    //не исп.
    public static void displayTornado(final Location loc, final boolean in) {
        new BukkitRunnable() {
            double radius = in ? 2.043476540885901 : 0.1; //нисходящая спираль
            double y = in ? 4 : 0; //нисходящая спираль

            @Override
            public void run() {
                for (int t = 0; t <= 40; t++) {
                    y = in ? y - 0.002 : y + 0.002;
                    radius = in ? radius / 1.0015 : radius * 1.0015;
                    double x = radius * Math.cos(Math.pow(y, 2) * 10);
                    double z = radius * Math.sin(Math.pow(y, 2) * 10);
                    loc.add(x, y, z);
                    loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0, 0, 0);
                    loc.subtract(x, y, z);
                }
                if ((in && y <= 0) || y >= 4) {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 0, 1);
    }


    public static void displayGalaxy(final Location loc) {
        int strands = 8;
        int particles = 80;
        float radius = 10;
        float curve = 10;
        double rotation = Math.PI / 4;
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
                if (count == 0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Ostrov.instance, 1, 10);
    }


    private static void displayColorTube(final Location loc) {
        int circles = 36;
        int particlesCircle = 5;
        float radiusDonut = 2;
        float radiusTube = .5f;
        double xRotation = 0, yRotation = 0, zRotation = 0;
        Vector v = new Vector();
        for (int i = 0; i < circles; i++) {
            double theta = 2 * Math.PI * i / circles;
            for (int j = 0; j < particlesCircle; j++) {
                double phi = 2 * Math.PI * j / particlesCircle;
                double cosPhi = Math.cos(phi);
                v.setX((radiusDonut + radiusTube * cosPhi) * Math.cos(theta));
                Vector vector = v.setZ((radiusDonut + radiusTube * cosPhi) * Math.sin(theta));
                v.setY(radiusTube * Math.sin(phi));
                rotateVector(v, xRotation, yRotation, zRotation);
                loc.add(v);
                loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc, 1, 0, 0, 0, Material.matchMaterial(TCUtil.randomDyeColor().toString() + "_WOOL").createBlockData());
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
        fireworkMeta.addEffect(
            FireworkEffect.builder()
                .flicker(NumUtil.rndBool())
                .withColor(Color.fromBGR(NumUtil.randInt(0, 255), NumUtil.randInt(0, 255), NumUtil.randInt(0, 255)))
                .withFade(Color.fromBGR(NumUtil.randInt(0, 255), NumUtil.randInt(0, 255), NumUtil.randInt(0, 255)))
                .with(FireworkEffect.Type.values()[NumUtil.randInt(0, FireworkEffect.Type.values().length - 1)])//.with(FireworkEffect.Type.BALL)
                .trail(NumUtil.rndBool())
                .build()
        );
        fireworkMeta.setPower(0);
        firework.setFireworkMeta(fireworkMeta);
    }

    @Deprecated
    public static void BorderDisplay(final Player p, final XYZ minPoint, final XYZ maxPoint, final boolean tpToCenter) {
        final Oplayer op = PM.getOplayer(p);
        final Cuboid cuboid = new Cuboid(minPoint, maxPoint);

        if (op.displayCube != null && !op.displayCube.isCancelled()) {
            op.displayCube.cancel();
        }

        op.displayCube = new BukkitRunnable() {
            final Set<XYZ> border = cuboid.getBorder();
            final Location particleLoc = new Location(p.getWorld(), 0, 0, 0);
            final String name = p.getName();
            Player pl;

            @Override
            public void run() {
                pl = Bukkit.getPlayerExact(name);
                if (pl == null || !pl.isOnline()) {
                    this.cancel();
                    return;
                }
                if (pl.isDead() || pl.isSneaking() || !pl.getWorld().equals(particleLoc.getWorld())) {
                    pl.resetTitle();
                    this.cancel();
                    return;
                }
                border.stream().forEach(
                    (xyz) -> {
                        particleLoc.set(xyz.x, xyz.y, xyz.z);
                        if (xyz.pitch >= 5) { //стенки
                            pl.spawnParticle(Particle.FIREWORK, particleLoc, 0);
                        } else {
                            pl.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 0);
                        }
                    }
                );
                ScreenUtil.sendTitleDirect(pl, "", "§7Шифт - остановить показ", 0, 30, 0);
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 10, 25);
    }


    public static void deathEffect(final Player player, final boolean epic) {

        final Location loc = player.getLocation().clone().add(0, 0.5, 0);
        final BlockData bd = Material.OBSIDIAN.createBlockData();
        int circleElements = 8;
        double radius = 0.4;

        for (int i = 0; i < 20; i++) {
            double alpha = (360.0 / circleElements) * i;
            double x = radius * Math.sin(Math.toRadians(alpha));
            double z = radius * Math.cos(Math.toRadians(alpha));
            Location particleFrom = new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z);
            particleFrom.getWorld().spawnParticle(Particle.FALLING_DUST, particleFrom, 1, 0, 0, 0, bd);
            particleFrom.getWorld().spawnParticle(Particle.FALLING_DUST, particleFrom.clone().add(0, 0.5, 0), 1, 0, 0, 0, bd);
            particleFrom.getWorld().spawnParticle(Particle.FALLING_DUST, particleFrom.clone().add(0, 1, 0), 1, 0, 0, 0, bd);
        }

        loc.getWorld().spawnParticle(Particle.FALLING_DUST, loc.clone().add(0, 2, 0), 1, 0, 0, 0, bd);
        loc.getWorld().spawnParticle(Particle.FALLING_DUST, getLeftRaisedHandLocation(player), 1, 0, 0, 0, bd);
        loc.getWorld().spawnParticle(Particle.FALLING_DUST, getLeftLoweredHandLocation(player), 1, 0, 0, 0, bd);
        loc.getWorld().spawnParticle(Particle.FALLING_DUST, getLeftRaisedHandLocation(player), 1, 0, 0, 0, bd);
        loc.getWorld().spawnParticle(Particle.FALLING_DUST, getRightLoweredHandLocation(player), 1, 0, 0, 0, bd);

        if (epic) {
            player.getWorld().playSound(player.getLocation(), "quake.random.meat", 1, 1);
            final Firework firework = loc.getWorld().spawn(loc, Firework.class);
            final FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.addEffect(FireworkEffect.builder().flicker(NumUtil.rndBool()).withColor(Color.RED).withFade(Color.MAROON).with(FireworkEffect.Type.BURST).trail(NumUtil.rndBool()).build());
            fireworkMeta.setPower(0);
            firework.setFireworkMeta(fireworkMeta);
        }
    }


    public static Location getRightLoweredHandLocation(final Player player) {
        return getRightSide(player.getEyeLocation(), 0.45).subtract(0, .6, 0); // right hand
    }

    public static Location getRightRaisedHandLocation(final Player player) {
        final Location loc = player.getLocation().clone();

        double a = loc.getYaw() / 180D * Math.PI + Math.PI / 2;
        double l = Math.sqrt(0.8D * 0.8D + 0.4D * 0.4D);

        loc.setX(loc.getX() + l * Math.cos(a) - 0.2D * Math.sin(a));
        loc.setY(loc.getY() + player.getEyeHeight() - 0.4D);
        loc.setZ(loc.getZ() + l * Math.sin(a) + 0.2D * Math.cos(a));
        //if (player.isSneaking()) {
        //     loc.subtract(0.0, 0.03, 0.0);
        //}
        return loc;
    }

    public static Location getLeftLoweredHandLocation(final Player player) {
        return getLeftSide(player.getEyeLocation(), 0.45).subtract(0, .6, 0); // right hand
    }

    public static Location getLeftRaisedHandLocation(final Player player) {
        final Location loc = player.getLocation().clone();

        double a = loc.getYaw() / 180D * Math.PI + Math.PI / 2;
        double l = Math.sqrt(0.8D * 0.8D + 0.4D * 0.4D);

        loc.setX(loc.getX() + l * Math.cos(a) + 0.2D * Math.sin(a));
        loc.setY(loc.getY() + player.getEyeHeight() - 0.4D);
        loc.setZ(loc.getZ() + l * Math.sin(a) - 0.2D * Math.cos(a));

        return loc;
    }

    public static Location getRightSide(Location location, double distance) {
        float angle = location.getYaw() / 60;
        return location.clone().subtract(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
    }

    public static Location getLeftSide(Location location, double distance) {
        float angle = location.getYaw() / 60;
        return location.clone().add(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance));
    }

}
