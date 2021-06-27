package ru.komiss77.Enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




public enum Action {
    
    
    NONE (0),
    
    //с Auth на банжи
    AUTH_PLAYER_DATA (1), //присылается с авторизации в банжи
    
    
    //на банжи
    NOTYFY_MODER (10), //уведомление модерам
    RESEND_RAW_DATA (11),  //Action_Sender_String
    GKICK (12), //Action_Sender_String
    GMUTE (13), 
    GBAN (14), 
    GBANIP (15), 
    SET_BUNGEE_DATA (16), //в основном для даты, но можно изменить отдельную стату (например, E_Stat.FLAGS) минуя addStat
    ADD_BUNGEE_STAT (17),
    EXECUTE_BUNGEE_CMD (18),
    GET_ONLINE (19),
    REQUEST_PLAYER_DATA (20), //переделать локально ??
    REWARD (21),
    GAME_INFO_TO_BUNGEE (22),
    SEND_TO_ARENA (23),
    REPORT_SERVER (24),
    REPORT_PLAYER (25),
    
    
    
    //на спигот
    OSTROV_RAW_DATA (30),
    SET_OSTROV_DATA (31), //в основном для даты, но можно изменить отдельную стату (например, E_Stat.FLAGS) минуя addStat
    ADD_OSTROV_STAT (32),
    EXECUTE_OSTROV_CMD (33),
    GONLINE (34),
    PLAYER_DATA_REQUEST_RESULT(35), //ответ на REQUEST_PLAYER_DATA  //переделать локально??
    ADD_REPUTATION (36), //для пересчёты репутации, чтобы не грузить банжи
    ADD_EXP (37), //для пересчёта уровня, чтобы не грузить банжи
    GAME_INFO_TO_OSTROV (38),
    RESET_DAYLY_STAT (39),
    
    
    
    
    //Друзья
    PF_FRIENDS_ONLINE (70),
    PF_FRIENDS_OFFLINE (71), 
    PF_FRIEND_SETTINGS (72), 
    PF_PARTY_MEMBER (73),
    PF_PARTY_SETTINGS (74), 
    PF_CALLBACK_RUN (75), 
    
    

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
