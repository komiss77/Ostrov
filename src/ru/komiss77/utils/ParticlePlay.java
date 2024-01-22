package ru.komiss77.utils;

import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import net.kyori.adventure.text.Component;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.modules.world.XYZ;

public class ParticlePlay {

    public Particle particleEffect;
    private final Location location;
    
    public ParticlePlay(final Particle particleeffect, final Location location) {
        this.particleEffect = particleeffect;
        this.location = location;

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

            @Override
            public void run() {
                if (p == null || !p.isOnline()) {
                    this.cancel();
                    return;
                }
                if (p.isDead() || p.isSneaking()) {
                    p.resetTitle();
                    this.cancel();
                    return;
                }
                border.stream().forEach(
                        (xyz) -> {
                            particleLoc.set(xyz.x, xyz.y, xyz.z);
                            if (xyz.pitch >= 5) { //стенки
                                p.spawnParticle(Particle.FIREWORKS_SPARK, particleLoc, 0);
                            } else {
                                p.spawnParticle(Particle.VILLAGER_HAPPY, particleLoc, 0);
                            }
                        }
                );
                ApiOstrov.sendTitle(p, "", "§7Шифт - остановить показ", 0, 30, 0);
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
            
        } else if ( particleEffect.getDataType()== BlockData.class ) {
            
            
            
        }
        //location.getWorld().playEffect(location, particleEffect, 0, 24 );
        //this.particleEffect.display(0.2F, 0.2F, 0.2F, 0.0F, 5, location, 64);
    }

    public void display(final List <Player> player_list) {
        for (Player p:player_list) {
            if (particleEffect.getDataType() == Void.class) {
                p.spawnParticle(particleEffect, location, 6, 1, 1, 1);
            } else if (particleEffect.getDataType() == Particle.DustOptions.class) {
                p.spawnParticle(particleEffect, location, 6, 1, 1, 1, new Particle.DustOptions(TCUtils.randomCol(), 1));
            }
            
        }
    }




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
            case "Сердечки": return Particle.HEART;
            case "Ноты": return Particle.NOTE;
            case "Изумруды": return Particle.VILLAGER_HAPPY;
            case "Огонь": return Particle.FLAME;
            case "Удар магии": return Particle.CRIT_MAGIC;
            case "Удар": return Particle.CRIT;
            case "Злой Житель": return Particle.VILLAGER_ANGRY;
            case "Портал": return Particle.PORTAL;
            case "Редстоун": return Particle.REDSTONE;
            case "Дымок": return Particle.CLOUD;
            case "Лава": return Particle.LAVA;
            case "Магия": return Particle.ENCHANTMENT_TABLE;
        }
        return null;
    }





}
