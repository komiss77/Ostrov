package ru.komiss77.Enums;




public enum Action {
    
    //НЕ КОМЕНТИТЬ!!! НЕ ПЕРЕСТАВЛЯТЬ!!! НЕ УДАЛЯТЬ!!! ТОЛЬКО ДОБАВЛЯТЬ!!!
    
    NONE,//(0),
    //Auth
    AUTH_NOTYFY_MODER,//(1),
    AUTH_PLAYER_DATA,//(2),
    
    //Ostrov
    OSTROV_PLAYER_RAW_DATA,//(3),
    OSTROV_UPDATE_DATA,//(4),
    OSTROV_SET_BUNGEE_DATA,//(5),
    OSTROV_BUNGEE_CMD,//(6),
    OSTROV_BUNGEE_MONEY_CHANGE,//(7),
    OSTROV_PANDORA_CHECK,//(8),
    OSTROV_PANDORA_RESULT,//(9),
    OSTROV_PANDORA_EXECUTE,//(10),
    OSTROV_TELEPORT,//(11),
    GET_BUNGEE_ONLINE,//(12),
    OSTROV_RESEND_PLAYER_RAW_DATA,//(13),
    OSTROV_RUN_SPIGOT_CMD,//(14),
    BUNGEE_ONLINE,//(15),
    OSTROV_PASSPORT,//(16),
    OSTROV_STAT_DATA,//(17),
    OSTROV_SET_STAT_DATA,//(18),
    OSTROV_SEND_TO_ARENA,//(19),
    OSTROV_REWARD,//(20), 
    
    GKICK,//(21, 
    GBAN,//(22), 
    GBANIP,//(23), 
    
    PF_FRIENDS_ONLINE,//24(40),
    PF_FRIENDS_OFFLINE,//25(41), 
    PF_FRIEND_SETTINGS,//26(42), 
    PF_PARTY_MEMBER,//27(43),
    PF_PARTY_SETTINGS,//28(44), 
    PF_CALLBACK_RUN,//29(45), 
    
    //30, 31 не передвигать - совместимость со старым енум!
    ARENA_INFO_FROM_GAME,//30(30), //рассылается серверам по списку лобби, состояние арен, получает и отправляет плагин Bsign
    ARENA_INFO_TO_LISTENER,//31(31),
    

    ;
    
    
    //public int tag;

    //private Action(int tag){
    //    this.tag = tag;
    //}
    
    public static Action byTag(final String tag_as_string){
        final int order = getInteger(tag_as_string);
        if (order<0 || order>=Action.values().length) return NONE;
        return Action.values()[order];
        //for(Action set: Action.values()){
        //        if(String.valueOf(set.tag).equals(tag_as_string)){
        //                return set;
        //       }
        //}
        //return NONE;
    }


    private static int getInteger(final String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return Integer.MIN_VALUE;
        }
    }

    /*
    public static boolean exist(final int tag){
        for(Action s_: Action.values()){
            if (s_.tag==tag) return true;
        }
        return false;
    }
    public static boolean exist(final String as_string){
        for(Action s_: Action.values()){
            if (s_.toString().equals(as_string)) return true;
        }
        return false;
    }*/
    
}
