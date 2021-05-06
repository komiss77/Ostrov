package ru.komiss77.ProfileMenu;

//import ru.komiss77.Objects.CaseInsensitiveSet;


import ru.komiss77.Objects.CaseInsensitiveLinkedTreeSet;


public enum E_Stat {
    
//добавить время для каждой игры
    
    BW_game     (10, true,  "§3Раунды §f: ", "0", 3, true ),
    BW_win      (11, true,  "§aПобеды §f: ", "0", 10, true ),
    BW_loose    (12, true,  "§cПоражения §f: ", "0", -8, false ),
    BW_kill     (13, true,  "§bДуши §f: ", "0", 1, true ),
    BW_death    (14, true,  "§cПогиб §f: ", "0", -1, true ),
    BW_bed      (15, true,  "§3Кровати §f: ", "0", 2, true ),
   
    SG_game     (20, true,  "§3Раунды §f: ", "0", 4, true ),
    SG_win      (21, true,  "§aПобеды §f: ", "0", 10, true ),
    SG_loose    (22, true,  "§cПоражения §f: ", "0", -6, false ),
    SG_kill     (23, true,  "§bДуши §f: ", "0", 1, true ),
    //SG_kits     (24, false, "§3Наборы §f: ", "", 1, true ),
    
    BB_game     (30, true,  "§3Раунды §f: ", "0", 4, true ),
    BB_win      (31, true,  "§a1 место §f: ", "0", 10, true ),
    BB_loose    (32, true,  "§cХудший результат §f: ", "0", -6, false ),
    BB_vote     (33, true,  "§eГолоса §f: ", "0", 0, true ),
    BB_block    (34, true,  "§dБлоки §f: ", "0", 0, true ),
    
    GR_game     (40, true,  "§3Раунды §f: ", "0", 4, true ),
    GR_win      (41, true,  "§aПобеды §f: ", "0", 10, true ),
    GR_loose    (42, true,  "§cПоражения §f: ", "0", -6, false ),
    GR_kill     (43, true,  "§bДуши §f: ", "0", 1, true ),
    GR_death    (44, true,  "§cПогиб §f: ", "0", -1, false ),
    GR_gold     (45, true,  "§eСлитки §f: ", "0", 1, true ),
    GR_pz       (46, true,  "§eПигЗомби §f: ", "0", 0, true ),

    HS_game     (50, true,  "§3Раунды §f: ", "0", 4, true ),
    HS_win      (51, true,  "§aПобеды §f: ", "0", 10, true ),
    HS_loose    (52, true,  "§cПоражения §f: ", "0", -6, false ),
    HS_skill    (53, true,  "§bДуши Охотников§f: ", "0", 1, true ),
    HS_hkill    (54, true,  "§bДуши Прячущихся§f: ", "0", 1, true ),
    HS_fw       (55, true,  "§eСалютики§f: ", "0", 1, true ),
    //HS_hchance  (56, true,  "§dШанс искать §f: ", "0", 0, false ),
    //HS_block    (57, false, "§dМаскировки §f: ", "", 0, false ),
    //HS_perk     (58, false, "§dВозможности §f: ", "", 0, false ),

    SW_game     (60, true,  "§3Раунды §f: ", "0", 4, true ),
    SW_win      (61, true,  "§aПобеды §f: ", "0", 10, true ),
    SW_loose    (62, true,  "§cПоражения §f: ", "0", -6, false ),
    SW_kill     (63, true,  "§bДуши §f: ", "0", 1, true ),
    SW_death    (64, true,  "§cПогиб §f: ", "0", -1, false ),
    //SW_inv      (65, false, "§dКуплено §f: ", "", 0, false ),
    //SW_sel      (66, false, "§dВыбрано §f: ", "", 0, false ),
    
    CS_game     (16, true,  "§3Раунды §f: ", "0", 4, true ),
    CS_win      (17, true,  "§aПобеды §f: ", "0", 10, true ),
    CS_loose    (18, true,  "§cПоражения §f: ", "0", -6, false ),
    CS_kill     (19, true,  "§bДуши §f: ", "0", 1, true ),
    CS_death    (25, true, "§cПогиб §f: ", "0", -1, false ),
    CS_hshot    (26, true, "§eТочные выстрелы §f: ", "0", 1, true ),
    CS_bomb     (27, true, "§eБомб поставлено §f: ", "0", 1, true ),
    
    TW_game     (28, true,  "§3Раунды §f: ", "0", 4, true ),
    TW_win      (29, true,  "§aПобеды §f: ", "0", 10, true ),
    TW_loose    (35, true,  "§cПоражения §f: ", "0", -6, false ),
    TW_gold     (36, true,  "§eСобрано монет §f: ", "0", 1, true ),
    
    SN_game     (37, true,  "§3Раунды §f: ", "0", 4, true ),
    SN_win      (38, true,  "§aПобеды §f: ", "0", 10, true ),
    SN_loose    (39, true,  "§cПоражения §f: ", "0", -6, false ),
    SN_gold     (47, true,  "§eСобрано слитков §f: ", "0", 1, true ),
    
    QU_game     (48, true,  "§3Раунды §f: ", "0", 4, true ),
    QU_win      (49, true,  "§aПобеды §f: ", "0", 10, true ),
    QU_twin     (59, true,  "§aТИМ-Победы §f: ", "0", 10, true ),
    QU_loose    (70, true,  "§cПоражения §f: ", "0", -6, false ),
    QU_kill     (71, true,  "§bДуши §f: ", "0", 1, true ),
    QU_death    (72, true,  "§cПогиб §f: ", "0", -1, false ),
    //QU_sel      (65, false, "§dВыбрано §f: ", "", 0, false ),
    
    SP_game     (73, true,  "§3Раунды §f: ", "0", 4, true ),
    SP_win      (74, true,  "§aПобеды §f: ", "0", 10, true ),
    SP_loose    (75, true,  "§cПоражения §f: ", "0", -6, false ),
    
    TR_game     (76, true,  "§3Раунды §f: ", "0", 4, true ),
    TR_win      (77, true,  "§aПобеды §f: ", "0", 10, true ),
    TR_loose    (78, true,  "§cПоражения §f: ", "0", -6, false ),
    
    
    KB_twin      (79, true,  "§aПобеды в Турнире§f: ", "0", 10, true ),
    KB_cwin      (80, true,  "§aПобеды в Испытании§f: ", "0", 10, true ),
    KB_loose    (81, true,  "§cПоражения §f: ", "0", -1, false ),
    KB_kill     (82, true,  "§bДуши §f: ", "0", 1, true ),
    KB_death    (83, true,  "§cПогиб §f: ", "0", -1, false ),
    KB_proj      (84, true, "§dВыстрелы §f: ", "0", 1, true ),
    KB_abil     (85, true,  "§3Использование особенностей §f: ", "0", 1, true ),
    KB_soup      (86, true, "§dСъедено супчиков §f: ", "0", 1, true ),
    
    
    
    
    
    ;
    
    public int tag;
    public boolean is_integer;
    public String as_string;
    public String def_value;
    public int exp_per_point;
    public boolean is_achiv;
    
    
    private E_Stat(int tag, boolean is_integer, String as_string, String def_value, int exp_per_point, boolean is_achiv){
        this.tag = tag;
        this.is_integer = is_integer;
        this.as_string = as_string;
        this.def_value = def_value;
        this.exp_per_point = exp_per_point;
        this.is_achiv = is_achiv;
    }
    
    /*
    public static boolean exist(final int tag) {
        for (E_Stat current:E_Stat.values()) {
            if (current.tag==tag) return true;
        }
        return false;
    }*/
    public static E_Stat byColumn(final String as_string){
        for(E_Stat value: E_Stat.values()){
            if(value.toString().equals(as_string))return value;
        }
        return null;
    }

    public static E_Stat byTag(final int tag) {
        for(E_Stat value: E_Stat.values()){
           if (value.tag==tag) return value;
        }
        return null;
    }

    
    
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
    
    public static E_Stat fromString(final String as_string){
        for(E_Stat s_: E_Stat.values()){
            if (s_.toString().equalsIgnoreCase(as_string)) return s_;
        }
        return null;
    }
    
}
