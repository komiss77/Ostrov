package ru.komiss77.Enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.DyeColor;





public enum GameState {
    
    РАБОТАЕТ (1, DyeColor.GREEN, false),
    ОЖИДАНИЕ (2, DyeColor.GREEN, true),
    СТАРТ (3, DyeColor.YELLOW, true),
    ЭКИПИРОВКА (4, DyeColor.ORANGE, true),
    ИГРА (5, DyeColor.RED, true),
    ПОЕДИНОК (6, DyeColor.RED, true),
    ГОЛОСОВАНИЕ (7, DyeColor.CYAN, true),
    ФИНИШ (8, DyeColor.MAGENTA, true),
    СКАНИРОВАНИЕ (9, DyeColor.BROWN, false),
    РЕГЕНЕРАЦИЯ (10, DyeColor.BROWN, false),
    ВЫКЛЮЧЕНА (11, DyeColor.BLACK, false),
    ПЕРЕЗАПУСК (12, DyeColor.BLUE, false),
    НЕОПРЕДЕЛЕНО (13, DyeColor.WHITE, false),
    ОЧЕРЕДЬ (14, DyeColor.PINK, false),
    ТУРНИР (15, DyeColor.BLUE, false),
    ;
    
    public final int tag;
    public final DyeColor attachedColor;
    public final boolean subscribeInteress;
    private static final Map<Integer,GameState> tagMap;
   
    private GameState (final int tag, final DyeColor attachedColor, final boolean subscribeInteress ) {
        this.tag = tag;
        this.attachedColor = attachedColor;
        this.subscribeInteress = subscribeInteress;
    }
    
    static {
        Map<Integer,GameState> im = new ConcurrentHashMap<>();
        for (GameState d : GameState.values()) {
            im.put(d.tag,d);
        }
        tagMap = Collections.unmodifiableMap(im);
    }
    
    public static GameState byTag(final int tag){
        return tagMap.get(tag);//intMap.containsKey(tag) ? intMap.get(tag) : EMPTY;
    }
    
    public static GameState fromString(final String as_string){
        if (as_string==null) return НЕОПРЕДЕЛЕНО;
        for(GameState s_: GameState.values()){
            if (s_.toString().equalsIgnoreCase(as_string)) return s_;
        }
        return НЕОПРЕДЕЛЕНО;
    }



    
}
