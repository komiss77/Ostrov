package ru.komiss77.enums;

//import ru.komiss77.Objects.CaseInsensitiveSet;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public enum Stat {
    
    // НАЧИНАЧТЬ с 300 !!! не более 499!! (определяется по длинне ==3 и значение 300-499)
    
    //300-599 сохраняемая стата
    //600-899 дневная стата
    
    //добавить время для каждой игры
    PLAY_TIME (300, Game.GLOBAL,  "§fИгровое время : §a",  0, null ), //промежуточных сохранений нет, только при выходе (расчёт на входе+текущая сессия)
    EXP (301, Game.GLOBAL,  "§fОпыт : §e",  0, null ),
    LEVEL (302, Game.GLOBAL,  "§fТекущий уровень : §b",  0, null ),
    FLAGS (303, Game.GLOBAL,  "§8флаги : ",  0, null ),
    //REPORT_C (304, Game.GLOBAL,  "§cЗамечания от консоли §7: ",  0, null ),
    //REPORT_P (305, Game.GLOBAL,  "§cЖалоб от игроков §7: ",  0, null ),
    

    BW_game     (310, Game.BW,  "§3Раунды §f: ",  3, new int[] {10, 100, 300, 500, 1000} ),
    BW_win      (311, Game.BW,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    BW_loose    (312, Game.BW,  "§cПоражения §f: ",  -8, null ),
    BW_kill     (313, Game.BW,  "§bДуши §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    BW_death    (314, Game.BW,  "§cПогиб §f: ",  -1, null ),
    BW_bed      (315, Game.BW,  "§3Кровати §f: ",  2, new int[] {10, 100, 300, 500, 1000} ),
   
    SG_game     (320, Game.SG,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    SG_win      (321, Game.SG,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    SG_loose    (322, Game.SG,  "§cПоражения §f: ",  -6, null ),
    SG_kill     (323, Game.SG,  "§bДуши §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    
    BB_game     (324, Game.BB,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    BB_win      (325, Game.BB,  "§a1 место §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    BB_loose    (326, Game.BB,  "§cХудший результат §f: ",  -6, null ),
    BB_vote     (327, Game.BB,  "§eГолоса §f: ",  0, new int[] {10, 100, 300, 500, 1000} ),
    BB_block    (328, Game.BB,  "§dБлоки §f: ",  0, new int[] {10, 100, 300, 500, 1000} ),
    
    GR_game     (329, Game.GR,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    GR_win      (330, Game.GR,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    GR_loose    (331, Game.GR,  "§cПоражения §f: ",  -6, null ),
    GR_kill     (332, Game.GR,  "§bДуши §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    GR_death    (333, Game.GR,  "§cПогиб §f: ",  -1, null ),
    GR_gold     (334, Game.GR,  "§eСлитки §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    GR_pz       (335, Game.GR,  "§eПигЗомби §f: ",  0, new int[] {10, 100, 300, 500, 1000} ),

    HS_game     (336, Game.HS,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    HS_win      (337, Game.HS,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    HS_loose    (338, Game.HS,  "§cПоражения §f: ",  -6, null ),
    HS_skill    (339, Game.HS,  "§bДуши Охотников§f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    HS_hkill    (340, Game.HS,  "§bДуши Прячущихся§f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    HS_fw       (341, Game.HS,  "§eСалютики§f: ",  1, new int[] {10, 100, 300, 500, 1000} ),

    SW_game     (342, Game.SW,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    SW_win      (343, Game.SW,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    SW_loose    (344, Game.SW,  "§cПоражения §f: ",  -6, null ),
    SW_kill     (345, Game.SW,  "§bДуши §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    SW_death    (346, Game.SW,  "§cПогиб §f: ",  -1, null ),
    
    CS_game     (347, Game.CS,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    CS_win      (348, Game.CS,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    CS_loose    (349, Game.CS,  "§cПоражения §f: ",  -6, null ),
    CS_kill     (350, Game.CS,  "§bДуши §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    CS_death    (351, Game.CS, "§cПогиб §f: ",  -1, null ),
    CS_hshot    (352, Game.CS, "§eТочные выстрелы §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    CS_bomb     (353, Game.CS, "§eБомб поставлено §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    
    TW_game     (354, Game.TW,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    TW_win      (355, Game.TW,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    TW_loose    (356, Game.TW,  "§cПоражения §f: ",  -6, null ),
    TW_gold     (357, Game.TW,  "§eСобрано монет §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    
    SN_game     (358, Game.SN,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    SN_win      (359, Game.SN,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    SN_loose    (360, Game.SN,  "§cПоражения §f: ",  -6, null ),
    SN_gold     (361, Game.SN,  "§eСобрано слитков §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    
    QU_game     (362, Game.QU,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    QU_win      (363, Game.QU,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    QU_twin     (364, Game.QU,  "§aТИМ-Победы §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    QU_loose    (365, Game.QU,  "§cПоражения §f: ",  -6, null ),
    QU_kill     (366, Game.QU,  "§bДуши §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    QU_death    (367, Game.QU,  "§cПогиб §f: ",  -1, null ),
    
    ZH_game     (368, Game.ZH,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    ZH_win      (369, Game.ZH,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    ZH_loose    (370, Game.ZH,  "§cПоражения §f: ",  -6, null ),
    
    //TR_game     (371, E_Stat_menu.BW,  "§3Раунды §f: ",  4, new int[] {10, 100, 300, 500, 1000} ),
    //TR_win      (372, E_Stat_menu.BW,  "§aПобеды §f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    //TR_loose    (373, E_Stat_menu.BW,  "§cПоражения §f: ",  -6, null ),
    
    
    KB_twin      (374, Game.KB,  "§aПобеды в Турнире§f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    KB_cwin      (375, Game.KB,  "§aПобеды в Испытании§f: ",  10, new int[] {10, 100, 300, 500, 1000} ),
    KB_loose    (376, Game.KB,  "§cПоражения §f: ",  -1, null ),
    KB_kill     (377, Game.KB,  "§bДуши §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    KB_death    (378, Game.KB,  "§cПогиб §f: ",  -1, null ),
    KB_proj      (379, Game.KB, "§dВыстрелы §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    KB_abil     (380, Game.KB,  "§3Использование особенностей §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    KB_soup      (381, Game.KB, "§dСъедено супчиков §f: ",  1, new int[] {10, 100, 300, 500, 1000} ),
    
    
    DA_dungee   (390, Game.DA,  "§3Данжей найдено §f: ",  5, new int[] {10, 100, 300, 500, 1000} ),
    MI_kraz   (392, Game.MI,  "§3Крац добавлено в казну §f: ",  0, new int[] {10, 100, 300, 500, 1000} ),
    PA_done   (393, Game.PA,  "§3паркуров пройдено §f: ",  0, new int[] {10, 100, 300, 500, 1000} ),
    
    
    
    
    ;
    
    public final int tag;
    public final Game game;
    public final String desc;
    public final int exp_per_point;
    public final int[] achiv;
    
    public static final int diff = 300; //разница в тэге между постоянной и дневной статой
    
    private Stat(final int tag, final Game game, final String desc, final int exp_per_point, final int[] achiv){
        this.tag = tag;
        this.game = game;
        this.desc = desc;
        this.exp_per_point = exp_per_point;
        this.achiv = achiv;
    }
    
    
    
    
    private static final Map<Integer,Stat> tagMap;
    private static final Map<String,Stat> nameMap;
    
    static {
        Map<Integer,Stat> im = new ConcurrentHashMap<>();
        Map<String,Stat> sm = new ConcurrentHashMap<>();
        for (Stat d : Stat.values()) {
            im.put(d.tag,d);
            sm.put(d.name(),d);
        }
        tagMap = Collections.unmodifiableMap(im);
        nameMap = Collections.unmodifiableMap(sm);
    }
    
    public static Stat fromName(String asString) {
        return nameMap.get(asString);//stringMap.containsKey(asString) ? stringMap.get(asString) : EMPTY;
    }

    public static Stat byTag(final int tag){
        return tagMap.get(tag);//intMap.containsKey(tag) ? intMap.get(tag) : EMPTY;
    }


/*
    public static E_Stat byColumn(final String as_string){
        for(E_Stat value: E_Stat.values()){
            if(value.toString().equals(as_string))return value;
        }
        return null;
    }*/

    
    
   // public static String gameFromStat (final Stat stat) {
       //if (stat.toString().length()>=2) {
           //final String type=stat.toString().substring(0, 2);
     //      for (Game g:Game.values()) {
      //         if (stat.toString().startsWith(g.toString()+"_")) return it.game_name;
       //    }
       //    return "";
       //}
 //  }
    
  //  public static CaseInsensitiveLinkedTreeSet getGameType() {
  //      CaseInsensitiveLinkedTreeSet res = new CaseInsensitiveLinkedTreeSet();
        //CaseInsensitiveSet res = new CaseInsensitiveSet();
   //     for (Stat es:values()) {
    //        res.add(es.toString().substring(0, 2));
    //    }
   //     return res;
   // }

}
