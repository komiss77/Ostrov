package ru.komiss77.utils;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ParticlePlay {

    public Particle particleEffect;
    private final Location location;
    
    public ParticlePlay(final Particle particleeffect, final Location location) {
        this.particleEffect = particleeffect;
        this.location = location;

    }
    
    
/*
spawnParticle​(Particle particle, Location location, int count) 
    
Spawns the particle (the number of times specified by count) at the target location.
void 	spawnParticle​(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ)
    
Spawns the particle (the number of times specified by count) at the target location.
void 	spawnParticle​(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) 
    
Spawns the particle (the number of times specified by count) at the target location.
<T> void 	spawnParticle​(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) 	
    
Spawns the particle (the number of times specified by count) at the target location.
<T> void 	spawnParticle​(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) 
    
Spawns the particle (the number of times specified by count) at the target location.
<T> void 	spawnParticle​(Particle particle, Location location, int count, T data)
    
    ПРИМЕРЫ
                            колл-во   разброс от loc                    цвет                              размер частиц
    Particle.REDSTONE, loc,    3,          1, 0, 1,     new Particle.DustOptions(Color.fromBGR(255, 50, 50),    5
    
*/
    
    
    public void display() {
        if (particleEffect.getDataType() == Void.class) {
            
            location.getWorld().spawnParticle(particleEffect, location, 6, 1, 1, 1);
            
        } else if (particleEffect.getDataType() == Particle.DustOptions.class) {
            
            location.getWorld().spawnParticle(particleEffect, location, 6, 1, 1, 1, new Particle.DustOptions(ColorUtils.randomCol(), 1));
            
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
                p.spawnParticle(particleEffect, location, 6, 1, 1, 1, new Particle.DustOptions(ColorUtils.randomCol(), 1));
            }
            
            //BlockData.class
            //ItemStack.class
            //p.playEffect(location, particleEffect, 0, 24);
        }
        //this.particleEffect.display(0.2F, 0.2F, 0.2F, 0.0F, 5, location, player_list);
    }




    public static void openParticleMenu(final Player p) {
        final Inventory particle_menu = Bukkit.createInventory(null, 27, "§1Выбор частиц");
        particle_menu.setItem(10, new ItemBuilder(Material.REDSTONE_BLOCK, (short)0).setName("Сердечки").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(11, new ItemBuilder(Material.NOTE_BLOCK, (short)0).setName("Ноты").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(12, new ItemBuilder(Material.EMERALD, (short)0).setName("Изумруды").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(13, new ItemBuilder(Material.FIRE, (short)0).setName("Огонь").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(14, new ItemBuilder(Material.DIAMOND_AXE, (short)0).setName("Удар магии").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(15, new ItemBuilder(Material.ENCHANTED_BOOK, (short)0).setName("Удар").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(16, new ItemBuilder(Material.ARROW, (short)0).setName("Злой Житель").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(19, new ItemBuilder(Material.OBSIDIAN, (short)0).setName("Портал").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(20, new ItemBuilder(Material.REDSTONE, (short)0).setName("Редстоун").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(21, new ItemBuilder(Material.TNT, (short)0).setName("Дымок").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(22, new ItemBuilder(Material.LAVA_BUCKET, (short)0).setName("Лава").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());
        particle_menu.setItem(23, new ItemBuilder(Material.ENCHANTING_TABLE, (short)0).setName("Магия").setLore("ЛКМ - получить установщик<br>ПКМ - убрать эти частицы", "§7").build());

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
