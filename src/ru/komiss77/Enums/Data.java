package ru.komiss77.Enums;



public enum Data {
    //              tag  mysql colunm    def.value  int?     таблица     отправлять на остров?
    ID              (0,     "id",           "",     true,   Table.USER,         false),//важное неизменное
    NAME            (1,     "name",         "",     false,  Table.USER,         false), //удаляется перед сохранением
    PASS_MD5        (2,     "pwmd5",        "",     false,  Table.USER,         false),//удаляется перед сохранением, если менялся-сохраняется отдельно
    PASS_STRIP      (3,     "pw",           "",     false,  Table.USER,         false),//удаляется перед сохранением, если менялся-сохраняется отдельно
    IP              (4,     "ip",           "",     false,  Table.USER,         true),//приходит новый с авторизации
    REG_TIME        (5,     "reg",          "0",    true,   Table.USER,         true),//удаляется перед сохранением
    LOGOUT_TIME     (6,     "logout",       "0",    true,   Table.USER,         false),//подставляется новое перед сохранением
    PLAY_TIME       (7,     "playtime",     "0",    true,   Table.USER,         true),//обновляется в Oplayer.updateData()
    SERVER          (8,     "server",       "",     false,  Table.USER,         false),//обновляется после смены сервера
    MONEY           (9,     "money",        "10000",true,   Table.USER,         true),//обновляется с острова по мере надобности
    MONEY_REAL      (27,    "rubli",        "0",    true,   Table.USER,         true),//
    PREFIX          (10,    "prefix",       "",     false,  Table.USER,         true),//
    SUFFIX          (11,    "suffix",       "",     false,  Table.USER,         true),//
    
    BAN_TO          (12,    "ban",          "-1",   true,   Table.JUDGEMENT,    false),//сохраняется командой ban
    BAN_BY          (13,    "banby",        "",     false,  Table.JUDGEMENT,    false),//сохраняется командой ban
    BAN_REAS        (14,    "banreas",      "",     false,  Table.JUDGEMENT,    false),//сохраняется командой ban
    MUTE_TO         (15,    "mute",         "-1",   true,   Table.JUDGEMENT,    false),//сохранять в saveUser не надо - сохраняется командой mute
    MUTE_BY         (16,    "muteby",       "",     false,  Table.JUDGEMENT,    false),//сохранять в saveUser не надо - сохраняется командой mute
    MUTE_REAS       (17,    "mutereas",     "",     false,  Table.JUDGEMENT,    false),//сохранять в saveUser не надо - сохраняется командой mute

    DAY_PLAY_TIME   (18,    "day_play_time","0",    true,   Table.DAILY,        true), //сохраняется saveDaily
    PANDORA_USED    (19,    "open",         "0",    true,   Table.DAILY,        true),//сохраняется saveDaily

    USER_GROUPS     (20,    "",             "",     false,  Table.NONE,         true),//удаляется перед сохранением

    FRIEND_F_SETTINGS (21,  "settings",     "",     false,  Table.FRIEND_SETTINGS, false), //удаляется перед сохранением или друзьями
    FRIEND_P_SETTINGS (22,  "pset",         "",     false,  Table.FRIEND_SETTINGS, false),//удаляется перед сохранением или друзьями
    FRIEND_FRIENDS  (23,    "",             "",     false,  Table.FRIEND_FRIENDS,  false),//удаляется перед сохранением или друзьями
    
    AUTH_CAUSE      (24,    "",             "",     false,  Table.NONE,         true),  //удаляется перед сохранением
    RESOURCE_PACK_HASH (26, "",             "",     false,  Table.NONE,         true),  //удаляется перед сохранением
    
    ИМЯ_ФАМИЛИЯ     (28,    "family",       "",     false,  Table.USER,         true),//
    ПОЛ             (29,    "sex",          "",     false,  Table.USER,         true),//
    РОДИЛСЯ         (30,    "birth",        "",     false,  Table.USER,         true),//
    СТРАНА          (31,    "land",         "",     false,  Table.USER,         true),//
    ГОРОД           (32,    "city",         "",     false,  Table.USER,         true),//
    О_СЕБЕ          (33,    "about",        "",     false,  Table.USER,         true),//
    СКАЙП           (34,    "skype",        "",     false,  Table.USER,         true),//
    ВКОНТАКТЕ       (35,    "vk",           "",     false,  Table.USER,         true),//
    ПАРА            (36,    "marry",        "",     false,  Table.USER,         true),//
    
    РЕПУТАЦИЯ_БАЗА  (37,    "reputation",   "0",    true,   Table.USER,         true),//
    КАРМА           (38,    "karma",        "0",    true,   Table.USER,         true),//
    ЮТУБ            (39,    "youtube",       "",    false,  Table.USER,         true),//
    ТВИЧ            (40,    "twich",         "",    false,  Table.USER,         true),//
    ТЕЛЕФОН         (41,    "phone",         "",    false,  Table.USER,         true),//
    МЫЛО            (42,    "email",         "",    false,  Table.USER,         true),//
    УРОВЕНЬ         (43,    "level",        "0",    true,   Table.USER,         true),//
    ОПЫТ            (44,    "exp",          "0",    true,   Table.USER,         true),//
    РЕПУТАЦИЯ_РАСЧЁТ(45,    "",             "0",    true,   Table.NONE,         true),//расчитывается из наполнения паспорта и игрового времени
    РЕПУТАЦИЯ       (46,    "",             "0",    true,   Table.NONE,         true),//
    
    ДОСТИЖЕНИЯ      (47,    "achiv",         "",    false,  Table.USER,         true),//
    
    WANT_ARENA_JOIN (48,    "",              "",    false,  Table.NONE,         true),//удаляется перед сохранением
    PARTY_MEBRERS   (49,    "",              "",    false,  Table.NONE,         true),//удаляется перед сохранением

    USER_PERMS      (50,    "",             "",     false,  Table.PEX_USER_PERMS,true),//удаляется перед сохранением
;

     
    public int tag;
    public String column;
    public String def_value;
    public boolean is_integer;
    public Table table;
    public boolean send_to_ostrov;
    
    private Data(int tag, String column, String def_value, boolean is_integer, Table table, boolean send_to_ostrov){
        this.tag = tag;
        this.column = column;
        this.def_value = def_value;
        this.is_integer = is_integer;
        this.table = table;
        this.send_to_ostrov = send_to_ostrov;
    }
    
    public static Data fromName(String asString) {
        for(Data set: Data.values()){
                if(set.toString().equalsIgnoreCase(asString)){
                        return set;
                }
        }
        return ID;
    }

   public static Data byTag(final int tag){
        for(Data set: Data.values()){
                if(set.tag==tag){
                        return set;
                }
        }
        return null;
    }

    public static Data byColumn(final String column){
        for(Data set: Data.values()){
                if(!set.column.isEmpty() && set.column.equals(column)){
                        return set;
                }
        }
        return null;
    }

    public static boolean exist(final int tag){
        for(Data s_: Data.values()){
            if (s_.tag==tag) return true;
        }
        return false;
    }

    public static boolean exist(final Table table, final String column ){
        for(Data s_: Data.values()){
            if (table==s_.table && !s_.column.isEmpty() && s_.column.equals(column)) return true;
        }
        return false;
    }



}
