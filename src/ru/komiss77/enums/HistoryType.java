package ru.komiss77.enums;




public enum HistoryType {
    
    NONE("", "AIR"),
    
    BAN_SET("бан", "BLAZE_POWDER"),
    UNBAN("разбан", "GHAST_TEAR"),
    BANIP_SET("бан по IP", "BLAZE_POWDER"),
    UNBANIP("разбан IP", "GHAST_TEAR"),
    //BAN_OFFLINE_SET(""),
    
    MUTE_SET("молчанка", "BLAZE_POWDER"),
    UNMUTE("снятие молчанки", "GHAST_TEAR"),
    
    KICK("пинок", "BLAZE_POWDER"),
    
    GROUP_ADD("добавление группы", "EMERALD"),
    GROUP_TIME_ADD("добавление срока группы", "EMERALD"),
    GROUP_EXPIRIED("удаление группы", "BUCKET"),
    PERMS_ADD("добавление права", "EMERALD"),
    PERMS_TIME_ADD("добавление срока права", "EMERALD"),
    PERMS_EXPIRIED("удаление права", "BUCKET"),
    STAFF_ADD("назначение на должность", "EMERALD"),
    STAFF_DEL("снятие с должности", "BUCKET"),
    
    MONEY_REAL_USE("расходование средств", "GOLD_INGOT"),
    MONEY_REAL_ADD("пополнение счёта", "GOLD_INGOT"),

    SESSION_INFO("сессия", "GRAY_DYE"),
    PASS_CHANGE("смена пароля", "MAGENTA_GLAZED_TERRACOTTA"),
    ;
    
    
    public String for_chat;
    public String displayMat;
    
    private HistoryType(final String for_chat, final String displayMat){
        this.for_chat = for_chat;
        this.displayMat = displayMat;
    }


    public static HistoryType by_action(final String as_string) {
        for(HistoryType s_: HistoryType.values()){
            if (s_.toString().equals(as_string)) return s_;
        }
        return NONE;
    }
    
    
    public static boolean exist(final String as_string){
        for(HistoryType s_: HistoryType.values()){
            if (s_.toString().equals(as_string)) return true;
        }
        return false;
    }

    public HistoryType getSourceType(final HistoryType cmd) {
        switch (cmd) {
            case BAN_SET: return UNBAN;
            case MUTE_SET: return UNMUTE;
            case BANIP_SET: return UNBANIP;
            default: return NONE;
        }
    }
    
}
