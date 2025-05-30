package ru.komiss77.enums;

import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public enum GameState {

    РАБОТАЕТ(1, "§2", DyeColor.GREEN, Material.TURTLE_SCUTE, false),
    ОЖИДАНИЕ(2, "§2", DyeColor.GREEN, Material.BUCKET, true),
    СТАРТ(3, "§6", DyeColor.YELLOW, Material.CLOCK, true),
    ЭКИПИРОВКА(4, "§6", DyeColor.ORANGE, Material.ENDER_CHEST, true),
    ИГРА(5, "§4", DyeColor.RED, Material.SPYGLASS, true),
    //ПОЕДИНОК (6, "§4", DyeColor.RED, true),
    ГОЛОСОВАНИЕ(7, "§5", DyeColor.CYAN, Material.TROPICAL_FISH_BUCKET, true),
    ФИНИШ(8, "§1", DyeColor.MAGENTA, Material.EXPERIENCE_BOTTLE, true),
    СКАНИРОВАНИЕ(9, "§7", DyeColor.BROWN, Material.SCULK_SHRIEKER, false),
    РЕГЕНЕРАЦИЯ(10, "§7", DyeColor.BROWN, Material.SCULK_SENSOR, false),
    ВЫКЛЮЧЕНА(11, "§c", DyeColor.BLACK, Material.GRAY_DYE, false),
    ПЕРЕЗАПУСК(12, "§c", DyeColor.BLUE, Material.BLUE_CONCRETE, false),
    НЕОПРЕДЕЛЕНО(13, "§8", DyeColor.WHITE, Material.BLUE_CONCRETE, false),
    ОЧЕРЕДЬ(14, "§7", DyeColor.PINK, Material.PINK_CONCRETE, false),
    ТУРНИР(15, "§b", DyeColor.BLUE, Material.BLUE_CONCRETE, false),
    РАУНД(16, "§4", DyeColor.RED, Material.RED_CONCRETE, false),
    ;

    public final int tag;
    public final String displayColor;
    public final DyeColor attachedColor;
    public final Material iconMat;
    public final boolean subscribeInteress;
    private static final Map<Integer, GameState> tagMap;
    private static final Map<String, GameState> nameMap;

    private GameState(final int tag, final String displayColor, final DyeColor attachedColor, final Material iconMat, final boolean subscribeInteress) {
        this.tag = tag;
        this.displayColor = displayColor;
        this.attachedColor = attachedColor;
        this.subscribeInteress = subscribeInteress;
        this.iconMat = iconMat;
    }

    static {
        Map<Integer, GameState> im = new ConcurrentHashMap<>();
        Map<String, GameState> nm = new ConcurrentHashMap<>();
        for (GameState d : GameState.values()) {
            im.put(d.tag, d);
            nm.put(d.name().toUpperCase(), d);
        }
        tagMap = Collections.unmodifiableMap(im);
        nameMap = Collections.unmodifiableMap(nm);
    }

    public static GameState byTag(final int tag) {
        return tagMap.get(tag);//intMap.containsKey(tag) ? intMap.get(tag) : EMPTY;
    }

    public static GameState fromString(final String as_string) {
        if (as_string == null) return НЕОПРЕДЕЛЕНО;
        GameState st = nameMap.get(as_string.toUpperCase());
        return st == null ? НЕОПРЕДЕЛЕНО : st;
        //for(GameState s_: GameState.values()){
        //    if (s_.toString().equalsIgnoreCase(as_string)) return s_;
        // }
        // return НЕОПРЕДЕЛЕНО;
    }


}
