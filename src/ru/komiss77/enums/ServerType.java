package ru.komiss77.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




public enum ServerType {

    NONE,
    REG_OLD,
    REG_NEW,
    LOBBY,
    ONE_GAME,
    ARENAS,
    
    ;
    
    private static final Map<String,ServerType> nameMap;
    static {
        Map<String,ServerType> sm = new ConcurrentHashMap<>();
        for (ServerType d : ServerType.values()) {
            sm.put(d.name().toUpperCase(),d);
        }
        nameMap = Collections.unmodifiableMap(sm);
    }  
    
    public static ServerType fromString(String as_string) { //araim daaria bw01 bb01 sg02
        return nameMap.containsKey(as_string) ? nameMap.get(as_string.toUpperCase()) : NONE;
    }

    
}

