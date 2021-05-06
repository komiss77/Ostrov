package ru.komiss77.Enums;




public enum RewardType {

    NONE(false),
    
    MONEY(true),
    
    PERMISSION(false),
    GROUP(false),
    
    EXP(true),
    //LEVEL(true), геморойно
    REPUTATION(true),
    KARMA(true),
    
    ;
    
    
    
    
    public boolean is_integer;
    private RewardType (boolean is_integer) {
        this.is_integer = is_integer;
    }
    
    
    public static RewardType fromString(final String as_string){
        for(RewardType s_: RewardType.values()){
            if (s_.toString().equalsIgnoreCase(as_string)) return s_;
        }
        return NONE;
    }
    
    public static String possibleValues () {
        String possible="";
        for (RewardType t:RewardType.values()) {
            if (t!=RewardType.NONE) possible = ", "+possible+t.toString().toLowerCase();
        }
        possible=possible.replaceFirst(",", "").trim();
        return possible;
    }
    
}

