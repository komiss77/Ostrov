package ru.komiss77.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public enum RewardType {

    //NONE(false),

    LONI(1, true),

    PERM(2, false),
    GROUP(3, false),

    EXP(4, true),
    //LEVEL(true), геморойно
    REP(5, true),
    KARMA(6, true),
    RIL(7, true),

    ;


    public final boolean is_integer;
    public final int tag;
    private static final Map<Integer, RewardType> tagMap;

    private RewardType(final int tag, final boolean is_integer) {
        this.is_integer = is_integer;
        this.tag = tag;
    }

    static {
        Map<Integer, RewardType> im = new ConcurrentHashMap<>();
        for (RewardType d : RewardType.values()) {
            im.put(d.tag, d);
        }
        tagMap = Collections.unmodifiableMap(im);
    }

    public static RewardType byTag(final int tag) {
        return tagMap.get(tag);//intMap.containsKey(tag) ? intMap.get(tag) : EMPTY;
    }


    public static RewardType fromString(final String as_string) {
        if (as_string.equalsIgnoreCase("permission")) {
            return RewardType.PERM; //багфикс - PERMISSION переименовали в PERM, посыпались награды в неожиданных местах
        }
        for (RewardType type : RewardType.values()) {
            if (type.name().equalsIgnoreCase(as_string)) return type;
        }
        return null;
    }

    public static String possibleValues() {
        String possible = "";
        for (RewardType t : RewardType.values()) {
            possible = possible + ", " + t.toString().toLowerCase();
        }
        possible = possible.replaceFirst(", ", "").trim();
        return possible;
    }

}

