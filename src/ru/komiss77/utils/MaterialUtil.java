package ru.komiss77.utils;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import org.bukkit.Material;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.objects.IntHashMap;


public class MaterialUtil {
    
    private static final OstrovConfig config;
    private static final IntHashMap<Material> byInt;
    private static final EnumMap<Material, Integer> byMat;
    
    
    
    static {
        byInt = new IntHashMap<>();
        byMat = new EnumMap<>(Material.class);
        config = Config.manager.getNewConfig("matToChar.yml", new String[]{"", "Material to character config", ""} );
        //config.addDefault("use", false);
        config.saveConfig(); 
        
        Material mat;
        int code=0;
        
        if (config.getConfigurationSection("charMap") != null) {
            for (String key : config.getConfigurationSection("charMap").getKeys(false)) {
    //System.out.println("+++++++++++++++++++Load() s="+s+"  limit="+Conf.spawn_limiter.getInt("mob_limiter.limits.blockstates."+s));
                code = key.charAt(0);
                mat = Material.matchMaterial(config.getString("charMap." + key));
//Ostrov.log("char="+s.charAt(0)+" key="+key+" mat="+mat);
                if (mat==null) {
                    Ostrov.log_warn("§6MaterialUtil - такого материала больше нет : §e"+key);
                } else {
                    byInt.put(code, mat);
                    byMat.put(mat, code);
                }
            }
        }
        
        int add = 0;
        for (Material m : Material.values()) {
            if(!byMat.containsKey(m)) {
                add++;
                while (!Character.isLetter((char)code)) {
                    code++;
                }
//Ostrov.log("key="+key+" char="+(char)key);
                config.set("charMap."+(char)code, m.name());
                byInt.put(code, m);
                byMat.put(m, code);
                code++;
            }
        }
        
        if (add>0) {
            config.saveConfig();
            Ostrov.log_ok("§aMaterialUtil - добавлено новых значений : §b"+add);
        }
        
        
        //тест на составление общей строки        
        //StringBuilder sb = new StringBuilder();
        //for (int i : byInt.keySet()) {
        //    sb.append( ((char)i));
        //}
        //config.set("test", sb.toString());
        //config.saveConfig();

        //тест на раскодировку общей строки
        //String all = config.getString("test");
        //for (char c : all.toCharArray()) {
        //    if (byInt.containsKey(c)) {
        //        Ostrov.log(""+c+"="+byInt.get(c));
        //    } else {
        //        Ostrov.log("--------------------- "+c);
        //    }
        //}
        
    }

    
    public static @Nonnull char toChar (final Material mat) {
        int key = byMat.getOrDefault(mat, 65);//A=AIR
        return (char)key;
    }
    
    public static @Nonnull String toString (final Collection<Material> mats) {
        final StringBuilder sb = new StringBuilder();
        for (Material m : mats) {
            sb.append(toChar(m));
        }
        return sb.toString();
    }
    
    public static @Nonnull Material toMat (final char c) {
        final Material mat = byInt.get(c);
        return mat == null ? Material.AIR : mat;
    }
    
    public static @Nonnull EnumSet<Material> toMat (final String s) {
        final EnumSet<Material> set = EnumSet.noneOf(Material.class);
        for (char c : s.toCharArray()) {
            set.add(toMat(c));
        }
        return set;
    }
    
}
