package ru.komiss77.Enums;




public enum HistoryType {
    
    NONE(""),
    
    BAN_SET("бан"),
    UNBAN("разбан"),
    BANIP_SET("бан по IP"),
    UNBANIP("разбан IP"),
    //BAN_OFFLINE_SET(""),
    
    MUTE_SET("молчанка"),
    UNMUTE("снятие молчанки"),
    
    KICK("пинок"),
    
    GROUP_ADD("добавление группы"),
    GROUP_TIME_ADD("добавление срока группы"),
    GROUP_EXPIRIED("удаление группы"),
    PERMS_ADD("добавление права"),
    PERMS_TIME_ADD("добавление срока права"),
    PERMS_EXPIRIED("удаление права"),
    STAFF_ADD("назначение на должность"),
    STAFF_DEL("снятие с должности"),
    
    MONEY_REAL_USE("расходование средств"),
    MONEY_REAL_ADD("пополнение счёта"),

    SESSION_INFO("информация о сессии"),
    PASS_CHANGE("смена пароля"),
    ;
    
    
    public String for_chat;
    
    private HistoryType(String for_chat){
        this.for_chat = for_chat;
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
        }
        return NONE;
    }
    
}
