package ru.komiss77.Enums;

import org.bukkit.DyeColor;





public enum UniversalArenaState {
    
    РАБОТАЕТ (DyeColor.GREEN, false),
    ОЖИДАНИЕ (DyeColor.GREEN, true),
    СТАРТ (DyeColor.YELLOW, true),
    ЭКИПИРОВКА (DyeColor.ORANGE, true),
    ИГРА (DyeColor.RED, true),
    ПОЕДИНОК (DyeColor.RED, true),
    ГОЛОСОВАНИЕ (DyeColor.CYAN, true),
    ФИНИШ (DyeColor.MAGENTA, true),
    СКАНИРОВАНИЕ (DyeColor.BROWN, false),
    РЕГЕНЕРАЦИЯ (DyeColor.BROWN, false),
    ВЫКЛЮЧЕНА (DyeColor.BLACK, false),
    ПЕРЕЗАПУСК (DyeColor.BLUE, false),
    НЕОПРЕДЕЛЕНО (DyeColor.WHITE, false),
    ОЧЕРЕДЬ (DyeColor.PINK, false),
    ТУРНИР (DyeColor.BLUE, false),
    ;
    
    public DyeColor attachedColor;
    public boolean subscribeInteress;
    
    private UniversalArenaState (final DyeColor attachedColor, final boolean subscribeInteress ) {
        this.attachedColor = attachedColor;
        this.subscribeInteress = subscribeInteress;
    }
    
    public static UniversalArenaState fromString(final String as_string){
        if (as_string==null) return НЕОПРЕДЕЛЕНО;
        for(UniversalArenaState s_: UniversalArenaState.values()){
            if (s_.toString().equalsIgnoreCase(as_string)) return s_;
        }
        return НЕОПРЕДЕЛЕНО;
    }



    
}
