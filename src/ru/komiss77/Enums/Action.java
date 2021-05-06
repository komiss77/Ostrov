package ru.komiss77.Enums;




public enum Action {
    NONE(0),
    //Auth
    AUTH_NOTYFY_MODER(1),
    AUTH_PLAYER_DATA(2),
    
    //Ostrov
    OSTROV_PLAYER_RAW_DATA(3),
    OSTROV_UPDATE_DATA(4),
    OSTROV_SET_BUNGEE_DATA(5),
    OSTROV_BUNGEE_CMD(6),
    OSTROV_BUNGEE_MONEY_CHANGE(7),
    OSTROV_PANDORA_CHECK(8),
    OSTROV_PANDORA_RESULT(9),
    OSTROV_PANDORA_EXECUTE(10),
    OSTROV_TELEPORT(11),
    OSTROV_AAC(12),
    GET_BUNGEE_ONLINE(13),
    OSTROV_RESEND_PLAYER_RAW_DATA(14),
    OSTROV_RUN_SPIGOT_CMD(15),
    BUNGEE_ONLINE(16),
    OSTROV_PASSPORT(17),
    OSTROV_STAT_DATA(18),
    OSTROV_SET_STAT_DATA(19),
    OSTROV_SEND_TO_ARENA(20),
    OSTROV_REWARD(21), 
    
    ARENA_INFO_FROM_GAME(30), //рассылается серверам по списку лобби, состояние арен, получает и отправляет плагин Bsign
    ARENA_INFO_TO_LISTENER(31),
    
    PF_FRIENDS_ONLINE(40),
    PF_FRIENDS_OFFLINE(41), 
    PF_FRIEND_SETTINGS(42), 
    PF_PARTY_MEMBER(43),
    PF_PARTY_SETTINGS(44), 
    PF_CALLBACK_RUN(45), 

    ;
    
    
    public int tag;

    private Action(int tag){
        this.tag = tag;
    }
    
    public static Action byTag(final String tag_as_string){
        for(Action set: Action.values()){
                if(String.valueOf(set.tag).equals(tag_as_string)){
                        return set;
                }
        }
        return NONE;
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
