package ru.komiss77.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//тут нельзя импортить что-то от бакит (енум общий с прокси)!!

public enum Data {

    // НАЧИНАЧТЬ с 100 !!! не более 299!! (определяется по длинне ==3 и значение 100-299)
    //часть названия используются в  E_Pass!!

    //              tag            mysql colunm    def.value    int?     таблица      отправлять    нужно сохранять в БД
    //                                                                               на остров?    если было изменение?
    AUTH_CAUSE(101, "", "", false, Table.NONE, true, false, -1),  //удаляется перед сохранением
    DAILY_RAW(102, "raw", "", false, Table.DAILY, false, false, -1), //все дневные достижения - массив

    //не переименовывать, или не записывает в БД!!
    USERID(103, "ид", "-1", true, Table.USER, false, false, -1),//важное неизменное
    NAME(104, "имя", "", false, Table.USER, false, false, -1), //удаляется перед сохранением
    IPPROTECT(105, "защита ip", "0", true, Table.USER, true, true, -1),//удаляется перед сохранением, если менялся-сохраняется отдельно
    PASS(106, "пароль", "", false, Table.USER, true, true, -1),//удаляется перед сохранением, если менялся-сохраняется отдельно
    IP(107, "ip", "", false, Table.USER, true, true, -1),//приходит новый с авторизации
    SIENCE(108, "Дата Регистрации", "0", true, Table.USER, true, false, -1),//удаляется перед сохранением
    LOGOUT(109, "logout", "0", true, Table.USER, false, true, -1),//подставляется новое перед сохранением
    SERVER(110, "server", "", false, Table.USER, false, true, -1),//обновляется после смены сервера
    LONI(111, "лони", "100", true, Table.USER, true, true, -1),//обновляется с острова по мере надобности
    RIL(112, "рил", "0", true, Table.USER, true, true, -1),//
    PREFIX(113, "prefix", "", false, Table.USER, true, true, -1),//
    SUFFIX(114, "suffix", "", false, Table.USER, true, true, -1),//
    REPUTATION(115, "reputation", "0", true, Table.USER, true, true, -1),//
    KARMA(116, "karma", "0", true, Table.USER, true, true, -1),//
    REPORT_C(117, "§cРепорты консоли §7: ", "0", true, Table.USER, true, true, -1),//
    REPORT_P(118, "§cРепорты игроков §7: ", "0", true, Table.USER, true, true, -1),//
    REPORT_STAGE(119, "Стадия репортов", "0", true, Table.USER, true, true, -1),//

    //Имя, Фамилия | Дата Рождения | Пол | Город | Страна | Телефон | Эл. Почта | ВКонтакте | Discord | YouTube | О Себе | Примечания | Данные хрнятся до
    PHONE(120, "Телефон", "", false, Table.USER, true, true, 5),//
    EMAIL(121, "Эл. почта", "", false, Table.USER, true, true, 6),//
    FAMILY(122, "Имя, Фамилия", "", false, Table.USER, true, true, 0),//
    GENDER(123, "Пол", "", false, Table.USER, true, true, 2),//
    BIRTH(124, "Дата рождения", "", false, Table.USER, true, true, 1),//
    LAND(125, "Страна", "", false, Table.USER, true, true, 4),//
    CITY(126, "Город", "", false, Table.USER, true, true, 3),//
    ABOUT(127, "О Себе", "", false, Table.USER, true, true, 11),//
    DISCORD(128, "Discord", "", false, Table.USER, true, true, 8),//
    VK(129, "ВКонтакте", "", false, Table.USER, true, true, 7),//
    MARRY(130, "Супруг(а) на Острове", "", false, Table.USER, true, true, 9),//
    YOUTUBE(131, "Канал YouTube", "", false, Table.USER, true, true, 10),//
    SETTINGS(132, "Settings", "0", true, Table.USER, true, true, -1),//
    NOTES(133, "Примечания", "", false, Table.USER, true, true, 12),//
    VALID(134, "Данные хранятся до", "", true, Table.USER, true, true, -1),//
    TEXTDATA(135, "Разные данные", "", false, Table.USER, true, true, -1),//
    LANG(136, "Язык", "0", true, Table.USER, true, true, -1),//
    JAILED(137, "Время в чистилище", "0", true, Table.USER, true, true, -1),//сохраняется командой ban

    USER_PERMS(150, "", "", false, Table.PEX_USER_PERMS, true, false, -1),//удаляется перед сохранением

    @Deprecated BAN_T0(160, "ban", "-1", true, Table.JUDGEMENT, true, false, -1),//сохраняется командой ban
    BAN_BY(161, "banby", "", false, Table.JUDGEMENT, true, false, -1),//сохраняется командой ban
    BAN_REAS(162, "banreas", "", false, Table.JUDGEMENT, true, false, -1),//сохраняется командой ban
    MUTE_TO(163, "mute", "-1", true, Table.JUDGEMENT, true, false, -1),//сохранять в saveUser не надо - сохраняется командой mute
    MUTE_BY(164, "muteby", "", false, Table.JUDGEMENT, true, false, -1),//сохранять в saveUser не надо - сохраняется командой mute
    MUTE_REAS(165, "mutereas", "", false, Table.JUDGEMENT, true, false, -1),//сохранять в saveUser не надо - сохраняется командой mute

    FRIENDS(180, "", "", false, Table.NONE, true, false, -1), //приходит с авторизации, переваривается в хашМап и удаляется
    FRIENDS_MSG_OFFLINE(181, "", "0", true, Table.NONE, true, false, -1),//приходит с авторизации - сколько есть оффлайн-сообщений
    FRIENDS_INFO(182, "", "", false, Table.NONE, true, false, -1), //с банжи на сервер, карта друг:сервер:настройки
    FRIEND_JUMP_INFO(183, "", "", false, Table.NONE, false, false, -1),//инфо от сервера - можно ли ТП к игроку

    MISSIONS(201, "", "", false, Table.NONE, true, false, -1),//миссии-приходят с авторизации только на банжик
    RESOURCE_PACK_HASH(202, "", "", false, Table.NONE, true, false, -1),  //удаляется перед сохранением
    USER_GROUPS(203, "", "", false, Table.NONE, true, false, -1),//удаляется перед сохранением
    WANT_ARENA_JOIN(204, "", "", false, Table.NONE, true, false, -1),//удаляется перед сохранением
    PARTY_MEBRERS(205, "", "", false, Table.NONE, true, false, -1),//удаляется перед сохранением
    BLACK_LIST(206, "", "", false, Table.NONE, true, false, -1),//удаляется перед сохранением
    ;

    public final int tag;
    public final String desc;
    public final String def_value;
    public final boolean is_integer;
    public final Table table;
    public final boolean send_to_ostrov;
    public final boolean saveToDb;
    public final byte passSeq; //слот в паспорте

    Data(final int tag, final String desc, final String defValue, final boolean isInt, final Table table, final boolean sendToProxy, final boolean saveToDb, final int passSeq) {
        this.tag = tag;
        this.desc = desc;
        this.def_value = defValue;
        this.is_integer = isInt;
        this.table = table;
        this.send_to_ostrov = sendToProxy;
        this.saveToDb = saveToDb;
        this.passSeq = (byte) passSeq;
    }

    private static final Map<Integer, Data> tagMap;
    private static final Map<String, Data> nameMap;

    //private static final Map<String,Data> columnMap;
    static {
        Map<Integer, Data> im = new ConcurrentHashMap<>();
        Map<String, Data> sm = new ConcurrentHashMap<>();
        //Map<String,Data> cm = new ConcurrentHashMap<>();
        for (Data d : Data.values()) {
            im.put(d.tag, d);
            sm.put(d.name().toLowerCase(), d);
            //cm.put(d.column,d);
        }
        tagMap = Collections.unmodifiableMap(im);
        nameMap = Collections.unmodifiableMap(sm);
        //columnMap = Collections.unmodifiableMap(cm);
    }

    public static Data fromName(final String nameAtLowerCase) {
        return nameAtLowerCase == null ? null : nameMap.get(nameAtLowerCase);//stringMap.containsKey(asString) ? stringMap.get(asString) : EMPTY;
    }

    public static Data byTag(final int tag) {
        return tagMap.get(tag);//intMap.containsKey(tag) ? intMap.get(tag) : EMPTY;
    }


    // public static Data byColumn(final String column){
    //      return columnMap.get(column);//for(Data set: Data.values()){
    //    if(!set.column.isEmpty() && set.column.equals(column)){
    //               return set;
    //         }
    //  }
    //  return null;
    // }

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

    public static String getColumn(final Data d) {
        return d.name().toLowerCase();
    }


}
