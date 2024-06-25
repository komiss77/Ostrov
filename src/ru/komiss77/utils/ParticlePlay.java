package ru.komiss77.utils;

import java.util.List;
import java.util.Set;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.modules.world.XYZ;

//https://www.spigotmc.org/resources/playerparticles.40261/
public class ParticlePlay {

    public Particle particleEffect;
    private final Location location;

    public ParticlePlay(final Particle particleeffect, final Location location) {
        this.particleEffect = particleeffect;
        this.location = location;

    }

    public enum particle {
        HEART("Сердечки", Material.REDSTONE_BLOCK, Particle.HEART),
        NOTE("Ноты", Material.NOTE_BLOCK, Particle.NOTE),
        VILLAGER_HAPPY("Изумруды", Material.EMERALD, Particle.VILLAGER_HAPPY),
        FLAME("Огонь", Material.FIRE, Particle.FLAME),
        CRIT_MAGIC("Удар магии", Material.DIAMOND_AXE, Particle.CRIT_MAGIC),
        CRIT("Удар", Material.ENCHANTED_BOOK, Particle.CRIT),
        VILLAGER_ANGRY("Злой Житель", Material.ARROW, Particle.VILLAGER_ANGRY),
        PORTAL("Портал", Material.OBSIDIAN, Particle.PORTAL),
        REDSTONE("Редстоун", Material.REDSTONE, Particle.REDSTONE),
        CLOUD("Дымок", Material.TNT, Particle.CLOUD),
        LAVA("Лава", Material.LAVA_BUCKET, Particle.LAVA),
        ENCHANTMENT_TABLE("Магия", Material.ENCHANTING_TABLE, Particle.ENCHANTMENT_TABLE),
        CAMPFIRE_SIGNAL_SMOKE("Сигнальный дым", Material.CAMPFIRE, Particle.CAMPFIRE_SIGNAL_SMOKE),
        END_ROD("Стержень энда", Material.END_ROD, Particle.END_ROD),
        SCULK_CHARGE_POP("Скалк", Material.SCULK_SENSOR, Particle.SCULK_CHARGE_POP),
        BUBBLE_COLUMN_UP("Пузыри", Material.SCULK_SHRIEKER, Particle.BUBBLE_COLUMN_UP),
        SPELL_WITCH("Ведьма", Material.POTION, Particle.SPELL_WITCH),
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
                                pl.spawnParticle(Particle.FIREWORKS_SPARK, particleLoc, 0);
                            } else {
                                pl.spawnParticle(Particle.VILLAGER_HAPPY, particleLoc, 0);
                            }
                        }
                );
                ApiOstrov.sendTitleDirect(pl, "", "§7Шифт - остановить показ", 0, 30, 0);
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 10, 25);

        if (tpToCenter && !cuboid.contains(p.getLocation())) {
            final Location center = cuboid.getCenter(p.getLocation());
            p.teleport(center);
        }
    }


    public void display() {
        if (particleEffect.getDataType() == Void.class) {

            location.getWorld().spawnParticle(particleEffect, location, 6, 1, 1, 1);

        } else if (particleEffect.getDataType() == Particle.DustOptions.class) {

            location.getWorld().spawnParticle(particleEffect, location, 6, 1, 1, 1, new Particle.DustOptions(TCUtils.randomCol(), 1));

        } else if (particleEffect.getDataType() == BlockData.class) {


        }
        //location.getWorld().playEffect(location, particleEffect, 0, 24 );
        //this.particleEffect.display(0.2F, 0.2F, 0.2F, 0.0F, 5, location, 64);
    }

    public void display(final List<Player> player_list) {
        for (Player p : player_list) {
            if (particleEffect.getDataType() == Void.class) {
                p.spawnParticle(particleEffect, location, 6, 1, 1, 1);
            } else if (particleEffect.getDataType() == Particle.DustOptions.class) {
                p.spawnParticle(particleEffect, location, 6, 1, 1, 1, new Particle.DustOptions(TCUtils.randomCol(), 1));
            }

        }
    }

/*
    public static void openParticleMenu(final Player p) {
        final Inventory particle_menu = Bukkit.createInventory(null, 27, Component.text("§1Выбор частиц"));
        particle_menu.setItem(10, new ItemBuilder(Material.REDSTONE_BLOCK).name("Сердечки").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(11, new ItemBuilder(Material.NOTE_BLOCK).name("Ноты").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(12, new ItemBuilder(Material.EMERALD).name("Изумруды").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(13, new ItemBuilder(Material.FIRE).name("Огонь").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(14, new ItemBuilder(Material.DIAMOND_AXE).name("Удар магии").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(15, new ItemBuilder(Material.ENCHANTED_BOOK).name("Удар").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(16, new ItemBuilder(Material.ARROW).name("Злой Житель").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(19, new ItemBuilder(Material.OBSIDIAN).name("Портал").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(20, new ItemBuilder(Material.REDSTONE).name("Редстоун").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(21, new ItemBuilder(Material.TNT).name("Дымок").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(22, new ItemBuilder(Material.LAVA_BUCKET).name("Лава").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(23, new ItemBuilder(Material.ENCHANTING_TABLE).name("Магия").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());

        p.openInventory(particle_menu);
    }


    public static Particle effectFromItemName(final String item_name) {
        switch (item_name) {
            case "Сердечки":
                return Particle.HEART;
            case "Ноты":
                return Particle.NOTE;
            case "Изумруды":
                return Particle.VILLAGER_HAPPY;
            case "Огонь":
                return Particle.FLAME;
            case "Удар магии":
                return Particle.CRIT_MAGIC;
            case "Удар":
                return Particle.CRIT;
            case "Злой Житель":
                return Particle.VILLAGER_ANGRY;
            case "Портал":
                return Particle.PORTAL;
            case "Редстоун":
                return Particle.REDSTONE;
            case "Дымок":
                return Particle.CLOUD;
            case "Лава":
                return Particle.LAVA;
            case "Магия":
                return Particle.ENCHANTMENT_TABLE;
        }
        return null;
    }*/


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
            final Firework firework = (Firework) loc.getWorld().spawn(loc, (Class) Firework.class);
            final FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.addEffect(FireworkEffect.builder().flicker(ApiOstrov.randBoolean()).withColor(Color.RED).withFade(Color.MAROON).with(FireworkEffect.Type.BURST).trail(ApiOstrov.randBoolean()).build());
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
