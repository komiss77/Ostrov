package ru.komiss77.Enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




public enum Data {
    
    // НАЧИНАЧТЬ с 100 !!! не более 299!! (определяется по длинне ==3 и значение 100-299)
    //часть названия используются в  E_Pass!!
    
    //              tag  mysql colunm    def.value    int?     таблица     отправлять   нужно сохранять в БД
    //                                                                    на остров?    если было изменение?
    AUTH_CAUSE      (101,    "",             "",     false,  Table.NONE,         true,    false),  //удаляется перед сохранением
    DAILY_RAW       (102,    "raw",           "",    false,  Table.DAILY,         false,   false), //все дневные достижения - массив
    
    USER_ID         (103,     "userId",     "-1",    true,   Table.USER,         false,   false),//важное неизменное
    NAME            (104,     "name",         "",    false,  Table.USER,         false,   false), //удаляется перед сохранением
    IP_PROTECT      (105,     "ipProtect",   "0",    true,   Table.USER,         true,    true),//удаляется перед сохранением, если менялся-сохраняется отдельно
    PASSWORD        (106,     "pw",           "",    false,  Table.USER,         true,    true),//удаляется перед сохранением, если менялся-сохраняется отдельно
    IP              (107,     "ip",           "",    false,  Table.USER,         true,    true),//приходит новый с авторизации
    REG_TIME        (108,     "reg",         "0",    true,   Table.USER,         true,    false),//удаляется перед сохранением
    LOGOUT_TIME     (109,     "logout",      "0",    true,   Table.USER,         false,   true),//подставляется новое перед сохранением
    SERVER          (110,     "server",       "",    false,  Table.USER,         false,   true),//обновляется после смены сервера
    MONEY           (111,     "loni",   "10000",    true,   Table.USER,         true,    true),//обновляется с острова по мере надобности
    MONEY_REAL      (112,     "ril",       "0",    true,   Table.USER,         true,    true),//
    PREFIX          (113,     "prefix",       "",    false,  Table.USER,         true,    true),//
    SUFFIX          (114,     "suffix",       "",    false,  Table.USER,         true,    true),//
    ИМЯ_ФАМИЛИЯ     (115,     "family",       "",    false,  Table.USER,         true,    true),//
    ПОЛ             (116,     "sex",          "",    false,  Table.USER,         true,    true),//
    РОДИЛСЯ         (117,     "birth",        "",    false,  Table.USER,         true,    true),//
    СТРАНА          (118,     "land",         "",    false,  Table.USER,         true,    true),//
    ГОРОД           (119,     "city",         "",    false,  Table.USER,         true,    true),//
    О_СЕБЕ          (120,     "about",        "",    false,  Table.USER,         true,    true),//
    DISCORD         (121,     "discord",      "",    false,  Table.USER,         true,    true),//
    ВКОНТАКТЕ       (122,     "vk",           "",    false,  Table.USER,         true,    true),//
    ПАРА            (123,     "marry",        "",    false,  Table.USER,         true,    true),//
    РЕПУТАЦИЯ_БАЗА  (124,     "reputation",  "0",    true,   Table.USER,         true,    true),//
    КАРМА           (125,     "karma",       "0",    true,   Table.USER,         true,    true),//
    ЮТУБ            (126,     "youtube",      "",    false,  Table.USER,         true,    true),//
    ТВИЧ            (127,     "twich",        "",    false,  Table.USER,         true,    true),//
    ТЕЛЕФОН         (128,     "phone",        "",    false,  Table.USER,         true,    true),//
    МЫЛО            (129,     "email",        "",    false,  Table.USER,         true,    true),//
    
    
    
    USER_PERMS      (150,    "",             "",     false,  Table.PEX_USER_PERMS,true,    false),//удаляется перед сохранением
    
    BAN_TO          (160,    "ban",          "-1",   true,   Table.JUDGEMENT,    false,    false),//сохраняется командой ban
    BAN_BY          (161,    "banby",        "",     false,  Table.JUDGEMENT,    false,    false),//сохраняется командой ban
    BAN_REAS        (162,    "banreas",      "",     false,  Table.JUDGEMENT,    false,    false),//сохраняется командой ban
    MUTE_TO         (163,    "mute",         "-1",   true,   Table.JUDGEMENT,    false,    false),//сохранять в saveUser не надо - сохраняется командой mute
    MUTE_BY         (164,    "muteby",       "",     false,  Table.JUDGEMENT,    false,    false),//сохранять в saveUser не надо - сохраняется командой mute
    MUTE_REAS       (165,    "mutereas",     "",     false,  Table.JUDGEMENT,    false,    false),//сохранять в saveUser не надо - сохраняется командой mute

    //PANDORA_USED    (171,    "open",         "0",    true,   Table.DAILY,        true,    false), //сохраняется saveDaily


    FRIEND_F_SETTINGS (180,  "settings",     "",     false,  Table.FRIEND_SETTINGS, false,    false), //удаляется перед сохранением или друзьями
    FRIEND_P_SETTINGS (181,  "pset",         "",     false,  Table.FRIEND_SETTINGS, false,    false),//удаляется перед сохранением или друзьями
    FRIEND_FRIENDS  (182,    "",             "",     false,  Table.FRIEND_FRIENDS,  false,    false),//удаляется перед сохранением или друзьями
    
    РЕПУТАЦИЯ_РАСЧЁТ(200,    "",             "0",    true,   Table.NONE,         true,    false),//расчитывается из наполнения паспорта и игрового времени
    РЕПУТАЦИЯ       (201,    "",             "0",    true,   Table.NONE,         true,    false),//
    RESOURCE_PACK_HASH (202, "",             "",     false,  Table.NONE,         true,    false),  //удаляется перед сохранением
    USER_GROUPS     (203,    "",             "",     false,  Table.NONE,         true,    false),//удаляется перед сохранением
    WANT_ARENA_JOIN (204,    "",             "",     false,  Table.NONE,         true,    false),//удаляется перед сохранением
    PARTY_MEBRERS   (205,    "",             "",     false,  Table.NONE,         true,    false),//удаляется перед сохранением

    
    
    
;

     
    public final int tag;
    public final String column;
    public final String def_value;
    public final boolean is_integer;
    public final Table table;
    public final boolean send_to_ostrov;
    public final boolean saveToDb;
    
    private Data(final int tag, final String column, final String def_value, final boolean is_integer, final Table table, final boolean send_to_ostrov, final boolean saveToDb){
        this.tag = tag;
        this.column = column;
        this.def_value = def_value;
        this.is_integer = is_integer;
        this.table = table;
        this.send_to_ostrov = send_to_ostrov;
        this.saveToDb = saveToDb;
    }
    
    
    private static final Map<Integer,Data> intMap;
    private static final Map<String,Data> stringMap;
    private static final Map<String,Data> columnMap;
    static {
        Map<Integer,Data> im = new ConcurrentHashMap<>();
        Map<String,Data> sm = new ConcurrentHashMap<>();
        Map<String,Data> cm = new ConcurrentHashMap<>();
        for (Data d : Data.values()) {
            im.put(d.tag,d);
            sm.put(d.name(),d);
            cm.put(d.column,d);
        }
        intMap = Collections.unmodifiableMap(im);
        stringMap = Collections.unmodifiableMap(sm);
        columnMap = Collections.unmodifiableMap(cm);
    }
    
    public static Data fromName(String asString) {
        return stringMap.get(asString);//stringMap.containsKey(asString) ? stringMap.get(asString) : EMPTY;
    }

    public static Data byTag(final int tag){
        return intMap.get(tag);//intMap.containsKey(tag) ? intMap.get(tag) : EMPTY;
    }

   
   
    public static Data byColumn(final String column){
        return columnMap.get(column);//for(Data set: Data.values()){
            //    if(!set.column.isEmpty() && set.column.equals(column)){
         //               return set;
       //         }
      //  }
      //  return null;
    }

  /*  public static boolean exist(final int tag){
        for(Data s_: Data.values()){
            if (s_.tag==tag) return true;
        }
        return false;
    }*/

  //  public static boolean exist(final Table table, final String column ){
   //     final Data d = byColumn(column);
   //     return d!=null && !d.column.isEmpty() && d.table==table;
        //for(Data s_: Data.values()){
        //    if (table==s_.table && !s_.column.isEmpty() && s_.column.equals(column)) return true;
        //}
        //return false;
  //  }



}
