package ru.komiss77.Enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




public enum Action {
    
    
    NONE (0),
    
    //с Auth на банжи
    AUTH_PLAYER_DATA (1), //присылается с авторизации в банжи
    
    
    //на банжи
    AUTH_NOTYFY_MODER (10), //уведомление модерам
    RESEND_RAW_DATA (11),
    GKICK (12), 
    GBAN (13), 
    GBANIP (14), 
    SET_DATA_TO_BUNGEE (15), //в основном для даты, но можно изменить отдельную стату (например, E_Stat.FLAGS) минуя addStat
    ADD_STAT_TO_BUNGEE (16),
    EXECUTE_BUNGEE_CMD (17),
    GET_BUNGEE_ONLINE (18),
    //REPORT(19),  //переделать локально
    REQUEST_PLAYER_DATA (20), //переделать локально ??
    
    //на спигот
    RAW_DATA_TO_OSTROV (30),
    SET_DATA_TO_OSTROV (31), //в основном для даты, но можно изменить отдельную стату (например, E_Stat.FLAGS) минуя addStat
    ADD_STAT_TO_OSTROV (32),
    EXECUTE_SPIGOT_CMD (33),
    //REWARD (34), //переделать локально
    TELEPORT (35),
    BUNGEE_ONLINE (36),
    REQUEST_PLAYER_DATA_RESULT(37), //ответ на REQUEST_PLAYER_DATA  //переделать локально??
    SEND_TO_ARENA (38),
    
    
    //PANDORA_CHECK (50), //остров-банжи  //переделать локально
    //PANDORA_CHECK_RESULT (51), //банжи-остров
    //PANDORA_RUN (52), //остров-банжи
    //PANDORA_RUN_RESULT (53), //банжи-остров
    
    
    
    
    //Друзья
    PF_FRIENDS_ONLINE (70),
    PF_FRIENDS_OFFLINE (71), 
    PF_FRIEND_SETTINGS (72), 
    PF_PARTY_MEMBER (73),
    PF_PARTY_SETTINGS (74), 
    PF_CALLBACK_RUN (75), 
    
    ARENA_INFO_FROM_GAME (90), //рассылается серверам по списку лобби, состояние арен, получает и отправляет плагин Bsign
    ARENA_INFO_TO_LISTENER (91),
    

    ;
    
    
    public final int tag;

    private Action(int tag){
        this.tag = tag;
    }



    
    private static final Map<Integer,Action> intMap;
    private static final Map<String,Action> stringMap;
    static {
        Map<Integer,Action> im = new ConcurrentHashMap<>();
        Map<String,Action> sm = new ConcurrentHashMap<>();
        for (Action d : Action.values()) {
            im.put(d.tag,d);
            sm.put(d.name(),d);
        }
        intMap = Collections.unmodifiableMap(im);
        stringMap = Collections.unmodifiableMap(sm);
    }
    
    public static Action fromName(String asString) {
        return stringMap.containsKey(asString) ? stringMap.get(asString) : NONE;
    }

    public static Action byTag(final int tag){
        return intMap.containsKey(tag) ? intMap.get(tag) : NONE;
    }








    /*
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
    }*/


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
