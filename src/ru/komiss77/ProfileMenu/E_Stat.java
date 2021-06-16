package ru.komiss77.ProfileMenu;

//import ru.komiss77.Objects.CaseInsensitiveSet;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.komiss77.Objects.CaseInsensitiveLinkedTreeSet;


public enum E_Stat {
    
    // НАЧИНАЧТЬ с 300 !!! не более 499!! (определяется по длинне ==3 и значение 300-499)
    
    //300-599 сохраняемая стата
    //600-899 дневная стата
    
    //добавить время для каждой игры
    PLAY_TIME (300, E_Stat_menu.GLOBAL,  "§3Игровое время §f: ",  0, false ), //промежуточных сохранений нет, только при выходе (расчёт на входе+текущая сессия)
    EXP (301, E_Stat_menu.GLOBAL,  "§3Опыт §f: ",  0, false ),
    LEVEL (302, E_Stat_menu.GLOBAL,  "§3Уровень островитянина §f: ",  0, false ),
    FLAGS (303, E_Stat_menu.GLOBAL,  "§3флаги",  0, false ),
    REPORT_C (304, E_Stat_menu.GLOBAL,  "§cЗамечания от консоли",  0, false ),
    REPORT_P (305, E_Stat_menu.GLOBAL,  "§cЗамечания от игроков",  0, false ),
    
    BW_game     (310, E_Stat_menu.BW,  "§3Раунды §f: ",  3, true ),
    BW_win      (311, E_Stat_menu.BW,  "§aПобеды §f: ",  10, true ),
    BW_loose    (312, E_Stat_menu.BW,  "§cПоражения §f: ",  -8, false ),
    BW_kill     (313, E_Stat_menu.BW,  "§bДуши §f: ",  1, true ),
    BW_death    (314, E_Stat_menu.BW,  "§cПогиб §f: ",  -1, false ),
    BW_bed      (315, E_Stat_menu.BW,  "§3Кровати §f: ",  2, true ),
   
    SG_game     (320, E_Stat_menu.SG,  "§3Раунды §f: ",  4, true ),
    SG_win      (321, E_Stat_menu.SG,  "§aПобеды §f: ",  10, true ),
    SG_loose    (322, E_Stat_menu.SG,  "§cПоражения §f: ",  -6, false ),
    SG_kill     (323, E_Stat_menu.SG,  "§bДуши §f: ",  1, true ),
    
    BB_game     (324, E_Stat_menu.BB,  "§3Раунды §f: ",  4, true ),
    BB_win      (325, E_Stat_menu.BB,  "§a1 место §f: ",  10, true ),
    BB_loose    (326, E_Stat_menu.BB,  "§cХудший результат §f: ",  -6, false ),
    BB_vote     (327, E_Stat_menu.BB,  "§eГолоса §f: ",  0, true ),
    BB_block    (328, E_Stat_menu.BB,  "§dБлоки §f: ",  0, true ),
    
    GR_game     (329, E_Stat_menu.GR,  "§3Раунды §f: ",  4, true ),
    GR_win      (330, E_Stat_menu.GR,  "§aПобеды §f: ",  10, true ),
    GR_loose    (331, E_Stat_menu.GR,  "§cПоражения §f: ",  -6, false ),
    GR_kill     (332, E_Stat_menu.GR,  "§bДуши §f: ",  1, true ),
    GR_death    (333, E_Stat_menu.GR,  "§cПогиб §f: ",  -1, false ),
    GR_gold     (334, E_Stat_menu.GR,  "§eСлитки §f: ",  1, true ),
    GR_pz       (335, E_Stat_menu.GR,  "§eПигЗомби §f: ",  0, true ),

    HS_game     (336, E_Stat_menu.HS,  "§3Раунды §f: ",  4, true ),
    HS_win      (337, E_Stat_menu.HS,  "§aПобеды §f: ",  10, true ),
    HS_loose    (338, E_Stat_menu.HS,  "§cПоражения §f: ",  -6, false ),
    HS_skill    (339, E_Stat_menu.HS,  "§bДуши Охотников§f: ",  1, true ),
    HS_hkill    (340, E_Stat_menu.HS,  "§bДуши Прячущихся§f: ",  1, true ),
    HS_fw       (341, E_Stat_menu.HS,  "§eСалютики§f: ",  1, true ),

    SW_game     (342, E_Stat_menu.SW,  "§3Раунды §f: ",  4, true ),
    SW_win      (343, E_Stat_menu.SW,  "§aПобеды §f: ",  10, true ),
    SW_loose    (344, E_Stat_menu.SW,  "§cПоражения §f: ",  -6, false ),
    SW_kill     (345, E_Stat_menu.SW,  "§bДуши §f: ",  1, true ),
    SW_death    (346, E_Stat_menu.SW,  "§cПогиб §f: ",  -1, false ),
    
    CS_game     (347, E_Stat_menu.CS,  "§3Раунды §f: ",  4, true ),
    CS_win      (348, E_Stat_menu.CS,  "§aПобеды §f: ",  10, true ),
    CS_loose    (349, E_Stat_menu.CS,  "§cПоражения §f: ",  -6, false ),
    CS_kill     (350, E_Stat_menu.CS,  "§bДуши §f: ",  1, true ),
    CS_death    (351, E_Stat_menu.CS, "§cПогиб §f: ",  -1, false ),
    CS_hshot    (352, E_Stat_menu.CS, "§eТочные выстрелы §f: ",  1, true ),
    CS_bomb     (353, E_Stat_menu.CS, "§eБомб поставлено §f: ",  1, true ),
    
    TW_game     (354, E_Stat_menu.TW,  "§3Раунды §f: ",  4, true ),
    TW_win      (355, E_Stat_menu.TW,  "§aПобеды §f: ",  10, true ),
    TW_loose    (356, E_Stat_menu.TW,  "§cПоражения §f: ",  -6, false ),
    TW_gold     (357, E_Stat_menu.TW,  "§eСобрано монет §f: ",  1, true ),
    
    SN_game     (358, E_Stat_menu.SN,  "§3Раунды §f: ",  4, true ),
    SN_win      (359, E_Stat_menu.SN,  "§aПобеды §f: ",  10, true ),
    SN_loose    (360, E_Stat_menu.SN,  "§cПоражения §f: ",  -6, false ),
    SN_gold     (361, E_Stat_menu.SN,  "§eСобрано слитков §f: ",  1, true ),
    
    QU_game     (362, E_Stat_menu.QU,  "§3Раунды §f: ",  4, true ),
    QU_win      (363, E_Stat_menu.QU,  "§aПобеды §f: ",  10, true ),
    QU_twin     (364, E_Stat_menu.QU,  "§aТИМ-Победы §f: ",  10, true ),
    QU_loose    (365, E_Stat_menu.QU,  "§cПоражения §f: ",  -6, false ),
    QU_kill     (366, E_Stat_menu.QU,  "§bДуши §f: ",  1, true ),
    QU_death    (367, E_Stat_menu.QU,  "§cПогиб §f: ",  -1, false ),
    
    ZH_game     (368, E_Stat_menu.ZH,  "§3Раунды §f: ",  4, true ),
    ZH_win      (369, E_Stat_menu.ZH,  "§aПобеды §f: ",  10, true ),
    ZH_loose    (370, E_Stat_menu.ZH,  "§cПоражения §f: ",  -6, false ),
    
    //TR_game     (371, E_Stat_menu.BW,  "§3Раунды §f: ",  4, true ),
    //TR_win      (372, E_Stat_menu.BW,  "§aПобеды §f: ",  10, true ),
    //TR_loose    (373, E_Stat_menu.BW,  "§cПоражения §f: ",  -6, false ),
    
    
    KB_twin      (374, E_Stat_menu.KB,  "§aПобеды в Турнире§f: ",  10, true ),
    KB_cwin      (375, E_Stat_menu.KB,  "§aПобеды в Испытании§f: ",  10, true ),
    KB_loose    (376, E_Stat_menu.KB,  "§cПоражения §f: ",  -1, false ),
    KB_kill     (377, E_Stat_menu.KB,  "§bДуши §f: ",  1, true ),
    KB_death    (378, E_Stat_menu.KB,  "§cПогиб §f: ",  -1, false ),
    KB_proj      (379, E_Stat_menu.KB, "§dВыстрелы §f: ",  1, true ),
    KB_abil     (380, E_Stat_menu.KB,  "§3Использование особенностей §f: ",  1, true ),
    KB_soup      (381, E_Stat_menu.KB, "§dСъедено супчиков §f: ",  1, true ),
    
    
    
    
    
    ;
    
    public final int tag;
    public final E_Stat_menu game;
    public final String desc;
    public final int exp_per_point;
    public final boolean is_achiv;
    
    public static final int diff = 300; //разница в тэге между постоянной и дневной статой
    
    private E_Stat(final int tag, final E_Stat_menu game, final String desc, final int exp_per_point, final boolean is_achiv){
        this.tag = tag;
        this.game = game;
        this.desc = desc;
        this.exp_per_point = exp_per_point;
        this.is_achiv = is_achiv;
    }
    
    
    
    
    private static final Map<Integer,E_Stat> intMap;
    private static final Map<String,E_Stat> stringMap;
    
    static {
        Map<Integer,E_Stat> im = new ConcurrentHashMap<>();
        Map<String,E_Stat> sm = new ConcurrentHashMap<>();
        for (E_Stat d : E_Stat.values()) {
            im.put(d.tag,d);
            sm.put(d.name(),d);
        }
        intMap = Collections.unmodifiableMap(im);
        stringMap = Collections.unmodifiableMap(sm);
    }
    
    public static E_Stat fromName(String asString) {
        return stringMap.get(asString);//stringMap.containsKey(asString) ? stringMap.get(asString) : EMPTY;
    }

    public static E_Stat byTag(final int tag){
        return intMap.get(tag);//intMap.containsKey(tag) ? intMap.get(tag) : EMPTY;
    }


/*
    public static E_Stat byColumn(final String as_string){
        for(E_Stat value: E_Stat.values()){
            if(value.toString().equals(as_string))return value;
        }
        return null;
    }*/

    
    
    public static String gameNameFromStat (final E_Stat stat) {
       //if (stat.toString().length()>=2) {
           //final String type=stat.toString().substring(0, 2);
           for (E_Stat_menu it:E_Stat_menu.values()) {
               if (stat.toString().startsWith(it.toString()+"_")) return it.game_name;
           }
           return "";
       //}
   }
    
    public static CaseInsensitiveLinkedTreeSet getGameType() {
        CaseInsensitiveLinkedTreeSet res = new CaseInsensitiveLinkedTreeSet();
        //CaseInsensitiveSet res = new CaseInsensitiveSet();
        for (E_Stat es:values()) {
            res.add(es.toString().substring(0, 2));
        }
        return res;
    }

}
