package ru.komiss77.utils;

import java.util.*;
import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import ru.komiss77.Cfg;
import ru.komiss77.OConfig;
import ru.komiss77.Ostrov;
import ru.komiss77.objects.IntHashMap;


public class MaterialUtil {

    private static final OConfig config;
    private static final IntHashMap<Material> matByInt;
    private static final EnumMap<Material, Integer> matMap;

    private static final IntHashMap<Biome> biomeByInt;
    private static final Map<Biome, Integer> biomeMap;


    static {
        matByInt = new IntHashMap<>();
        matMap = new EnumMap<>(Material.class);

        biomeByInt = new IntHashMap<>();
        biomeMap = new HashMap<>();//EnumMap<>(Biome.class);

        config = Cfg.manager.getNewConfig("matToChar.yml", new String[]{"", "Enum to character config", ""});
        config.saveConfig();

        Material mat;
        int code = 0;
        int i = 0;
        boolean save = false;

        if (config.getConfigurationSection("charMap") != null) {
            for (String key : config.getConfigurationSection("charMap").getKeys(false)) {
                //System.out.println("+++++++++++++++++++Load() s="+s+"  limit="+Conf.spawn_limiter.getInt("mob_limiter.limits.blockstates."+s));
                code = key.charAt(0);
                mat = Material.matchMaterial(config.getString("charMap." + key));
//Ostrov.log("char="+s.charAt(0)+" key="+key+" mat="+mat);
                if (mat == null) {
                    //Ostrov.log_warn("§6MaterialUtil - такого материала больше нет : §e" + key);
                    //config.removeKey("charMap." + (char) code);
                    // i++;
                } else {
                    matByInt.put(code, mat);
                    matMap.put(mat, code);
                }
            }
        }
        //if (i > 0) { ничего не удалять, при переходе версии туда-сюда всё время меняется
        //    save = true;
        //     Ostrov.log_ok("§aMaterialUtil - удалено старых значений : §b" + i);
        //}

        i = 0;
        for (Material m : Material.values()) {
            if (!matMap.containsKey(m)) {
                i++;
                while (!Character.isLetter((char) code)) {
                    code++;
                }
//Ostrov.log("key="+key+" char="+(char)key);
                config.set("charMap." + (char) code, m.name());
                matByInt.put(code, m);
                matMap.put(m, code);
                code++;
            }
        }

        if (i > 0) {
            save = true;
            Ostrov.log_ok("§aMaterialUtil - добавлено новых материалов : §b" + i);
        }


        Biome biome;
        i = 0;
        String name;
        if (config.getConfigurationSection("biomeMap") != null) {
            for (String key : config.getConfigurationSection("biomeMap").getKeys(false)) {
                //System.out.println("+++++++++++++++++++Load() s="+s+"  limit="+Conf.spawn_limiter.getInt("mob_limiter.limits.blockstates."+s));
                name = config.getString("biomeMap." + key).toUpperCase();
                code = key.charAt(0);
                biome = null;
                for (Biome b : Ostrov.registries.BIOMES) {
                    if (b.key().value().equalsIgnoreCase(name)) {
                        biome = b;
                        break;
                    }
                }
//Ostrov.log("char="+s.charAt(0)+" key="+key+" mat="+mat);
                if (biome == null) {
                    //Ostrov.log_warn("§6MaterialUtil - такого биома больше нет : §e" + key);
                    //config.removeKey("biomeMap." + (char) code);
                    //i++;
                } else {
                    biomeByInt.put(code, biome);
                    biomeMap.put(biome, code);
                }
            }
        }
        //if (i > 0) {
        //    save = true;
        //    Ostrov.log_ok("§aMaterialUtil - удалено старых биомов : §b" + i);
        //}

        i = 0;
        for (Biome b : Ostrov.registries.BIOMES) {
            if (!biomeMap.containsKey(b)) {
                i++;
                while (!Character.isLetter((char) code)) {
                    code++;
                }
//Ostrov.log("key="+key+" char="+(char)key);
                config.set("biomeMap." + (char) code, b.key().value());
                biomeByInt.put(code, b);
                biomeMap.put(b, code);
                code++;
                i++;
            }
        }
        if (i > 0) {
            save = true;
            Ostrov.log_ok("§aMaterialUtil - добавлено новых биомов : §b" + i);
        }


        if (save) {
            config.saveConfig();
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


    public static @Nonnull char toChar(final Material mat) {
        int key = matMap.getOrDefault(mat, 65);//A=AIR
        return (char) key;
    }

    public static @Nonnull String toString(final Collection<Material> mats) {
        final StringBuilder sb = new StringBuilder();
        for (Material m : mats) {
            sb.append(toChar(m));
        }
        return sb.toString();
    }

    public static @Nonnull Material toMat(final char c) {
        final Material mat = matByInt.get(c);
        return mat == null ? Material.AIR : mat;
    }

    public static @Nonnull EnumSet<Material> toMat(final String s) {
        final EnumSet<Material> set = EnumSet.noneOf(Material.class);
        for (char c : s.toCharArray()) {
            set.add(toMat(c));
        }
        return set;
    }


    public static @Nonnull char toChar(final Biome b) {
        int key = biomeMap.getOrDefault(b, 0);//PLAINS
        return (char) key;
    }

    public static @Nonnull String biomesToString(final Collection<Biome> biomes) {
        final StringBuilder sb = new StringBuilder();
        for (Biome b : biomes) {
            sb.append(toChar(b));
        }
        return sb.toString();
    }

    public static @Nonnull Biome toBiome(final char c) {
        final Biome b = biomeByInt.get(c);
        return b == null ? Biome.PLAINS : b;
    }

    public static @Nonnull Set<Biome> toBiomes(final String s) {
        final Set<Biome> set = new HashSet<>();
        for (char c : s.toCharArray()) {
            set.add(toBiome(c));
        }
        return set;
    }






}
