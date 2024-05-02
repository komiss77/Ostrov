package ru.komiss77.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




public enum Operation {
    
    
    NONE (0),
    
    //с Auth на банжи
    AUTH_PLAYER_DATA (1), //присылается с авторизации в банжи
    //AUTH_STAFF (2), //на авторизацию зашел стафф для обслуживания, данные не грузить!
    //AUTH_BEGIN (2), //запуск авторизации, если подключился bp==null
    
    
    //на банжи
    NOTIFY_MODER (10), //уведомление модерам
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
    ADD_CUSTOM_STAT (26),
    
    
    
    //на спигот
    OSTROV_RAW_DATA (30),
    SET_OSTROV_DATA (31), //в основном для даты, но можно изменить отдельную стату (например, E_Stat.FLAGS) минуя addStat
    ADD_OSTROV_STAT (32),
    EXECUTE_OSTROV_CMD (33),
    GONLINE (34),
    PLAYER_DATA_REQUEST_RESULT(35), //ответ на REQUEST_PLAYER_DATA  //переделать локально??
    TELEPORT_EVENT (36),
    ADD_EXP (37), //для пересчёта уровня, чтобы не грузить банжи
    GAME_INFO_TO_OSTROV (38),
    RESET_DAYLY_STAT (39),
    ADD_IGNORE_OSTROV (40), //для пересчёты репутации, чтобы не грузить банжи
    REMOVE_IGNORE_OSTROV (41), //для пересчёты репутации, чтобы не грузить банжи

    
    
    
    //Друзья      FRIEND_CONNECT не нужен, при переходе с авторизации разошлёт по onServerSwitch
    PARTY_INFO (70), //не исп?
    PARTY_MEMBER_SERVER_SWITCH (71), //c банжи на остров - при переходе лидера
    FRIEND_ADD (72), //c острова на банжи - только обновить Bplayer.friends, остальное делается на острове
    FRIEND_DELETE (73), //c острова на банжи - только обновить Bplayer.friends, остальное делается на острове
    GET_FRIENDS_INFO (74), //c острова на банжи - запрос из меню друзей
    FRIENDS_INFO_RESULT (75),  //с банжи по запросу с острова по GET_FRIENDS_INFO
    //PF_CALLBACK_RUN (76), 
    
    

    ;
    
    
    public final int tag;

    private Operation(int tag){
        this.tag = tag;
    }



    
    private static final Map<Integer,Operation> intMap;
    private static final Map<String,Operation> stringMap;
    static {
        Map<Integer,Operation> im = new ConcurrentHashMap<>();
        Map<String,Operation> sm = new ConcurrentHashMap<>();
        for (Operation d : Operation.values()) {
            im.put(d.tag,d);
            sm.put(d.name(),d);
        }
        intMap = Collections.unmodifiableMap(im);
        stringMap = Collections.unmodifiableMap(sm);
    }
    
    public static Operation fromName(String asString) {
        return stringMap.containsKey(asString) ? stringMap.get(asString) : NONE;
    }

    public static Operation byTag(final int tag){
        return intMap.containsKey(tag) ? intMap.get(tag) : NONE;
    }
    
}
