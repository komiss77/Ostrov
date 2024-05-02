package ru.komiss77.enums;

//import ru.komiss77.Objects.CaseInsensitiveSet;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static ru.komiss77.enums.Stat.KarmaChange.NONE;
import static ru.komiss77.enums.Stat.KarmaChange.ADD;
import static ru.komiss77.enums.Stat.KarmaChange.SUB;


public enum Stat {
    
    // НАЧИНАЧТЬ с 300 !!! не более 499!! (определяется по длинне ==3 и значение 300-499)
    
    //300-599 сохраняемая стата
    //600-899 дневная стата
    
    //добавить время для каждой игры
    PLAY_TIME (300, Game.GLOBAL,  "§fИгровое время : §a",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять! //промежуточных сохранений нет, только при выходе (расчёт на входе+текущая сессия)
    EXP (301, Game.GLOBAL,  "§fОпыт : §e",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    LEVEL (302, Game.GLOBAL,  "§fТекущий уровень : §b",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    FLAGS (303, Game.GLOBAL,  "§8флаги : ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    REPUTATION (304, Game.GLOBAL,  "§8репутация : ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    KARMA (304, Game.GLOBAL,  "§8карма : ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    WD_c (305, Game.GLOBAL,  "§8Заказы выводы : ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    WD_a (306, Game.GLOBAL,  "§8Сумма выводов : ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!

    BW_game     (310, Game.BW,  "§eРаунды§7:§f ",  2, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    BW_win      (311, Game.BW,  "§2Победы§7:§f ",  12, new int[] {1, 10, 100, 500, 1000}, ADD ),
    BW_loose    (312, Game.BW,  "§cПоражения§7:§f ",  0, null, SUB ),
    BW_kill     (313, Game.BW,  "§bДуши§7:§f ",  0, new int[] {10, 100, 666, 1000, 10000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    BW_death    (314, Game.BW,  "§cПогиб§7:§f ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    BW_bed      (315, Game.BW,  "§3Кровати§7:§f ",  2, new int[] {1, 10, 50, 250, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
   
    SG_game     (320, Game.SG,  "§eРаунды§7:§f ",  2, new int[] {10, 50, 100, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    SG_win      (321, Game.SG,  "§2Победы§7:§f ",  7, new int[] {10, 50, 100, 500, 1000}, ADD ),
    SG_loose    (322, Game.SG,  "§cПоражения§7:§f ",  0, null, SUB ),
    SG_kill     (323, Game.SG,  "§bДуши§7:§f ",  1, new int[] {10, 50, 100, 666, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    
    BB_game     (324, Game.BB,  "§eРаунды§7:§f ",  3, new int[] {10, 50, 100, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    BB_win      (325, Game.BB,  "§a1 место§7:§f ",  16, new int[] {1, 10, 100, 666, 1000}, ADD ),
    BB_loose    (326, Game.BB,  "§cХудший результат§7:§f ",  0, null, SUB ),
    BB_vote     (327, Game.BB,  "§eГолоса§7:§f ",  0, new int[] {10, 50, 100, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    BB_block    (328, Game.BB,  "§dБлоки§7:§f ",  0, new int[] {1000, 10000, 50000, 100000, 1000000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    
    GR_game     (329, Game.GR,  "§eРаунды§7:§f ",  3, new int[] {10, 50, 100, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    GR_win      (330, Game.GR,  "§2Победы§7:§f ",  12, new int[] {1, 10, 100, 500, 1000}, ADD ),
    GR_loose    (331, Game.GR,  "§cПоражения§7:§f ",  0, null, SUB ),
    GR_kill     (332, Game.GR,  "§bДуши§7:§f ",  0, new int[] {10, 100, 666, 1000, 10000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    GR_death    (333, Game.GR,  "§cПогиб§7:§f ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    GR_gold     (334, Game.GR,  "§6Слитки§7:§f ",  1, new int[] {10, 50, 100, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    GR_pz       (335, Game.GR,  "§bПигЗомби§7:§f ",  0, new int[] {50, 100, 500, 1000, 10000}, NONE ), //new int[] должно быть 5 штук, не удалять!

    HS_game     (336, Game.HS,  "§eРаунды§7:§f ",  2, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    HS_win      (337, Game.HS,  "§2Победы§7:§f ",  7, new int[] {10, 100, 250, 500, 1000}, ADD ),
    HS_loose    (338, Game.HS,  "§cПоражения§7:§f ",  0, null, SUB ),
    HS_skill    (339, Game.HS,  "§bДуши Охотников§7:§f ",  2, new int[] {10, 100, 300, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    HS_hkill    (340, Game.HS,  "§dДуши Прячущихся§7:§f ",  1, new int[] {10, 100, 300, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    HS_fw       (341, Game.HS,  "§5Салютики§7:§f ",  0, new int[] {1, 10, 100, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!

    SW_game     (342, Game.SW,  "§eРаунды§7:§f ",  1, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    SW_win      (343, Game.SW,  "§2Победы§7:§f ",  8, new int[] {10, 100, 250, 500, 1000}, ADD ),
    SW_loose    (344, Game.SW,  "§cПоражения§7:§f ",  0, null, SUB ),
    SW_kill     (345, Game.SW,  "§bДуши§7:§f ",  1, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    SW_death    (346, Game.SW,  "§cПогиб§7:§f ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    
    CS_game     (347, Game.CS,  "§eРаунды§7:§f ",  3, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    CS_win      (348, Game.CS,  "§2Победы§7:§f ",  10, new int[] {10, 100, 250, 500, 1000}, ADD ),
    CS_loose    (349, Game.CS,  "§cПоражения§7:§f ",  0, null, SUB ),
    CS_kill     (350, Game.CS,  "§bДуши§7:§f ",  0, new int[] {50, 100, 666, 1000, 10000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    CS_death    (351, Game.CS, "§cПогиб§7:§f ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    CS_hshot    (352, Game.CS, "§aТочные выстрелы§7:§f ",  0, new int[] {10, 100, 500, 1000, 10000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    CS_bomb     (353, Game.CS, "§dБомб поставлено§7:§f ",  1, new int[] {10, 100, 300, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    CS_mobs     (390, Game.CS, "§eУбито Монстров§7:§f ",  1, new int[] {100, 300, 500, 1000, 5000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    CS_spnrs    (391, Game.CS, "§dОбезврежено Спавнеров§7:§f ",  1, new int[] {10, 100, 300, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!  
    
    TW_game     (354, Game.TW,  "§eРаунды§7:§f ",  1, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    TW_win      (355, Game.TW,  "§2Победы§7:§f ",  8, new int[] {10, 100, 250, 500, 1000}, ADD ),
    TW_loose    (356, Game.TW,  "§cПоражения§7:§f ",  0, null, SUB ),
    TW_gold     (357, Game.TW,  "§6Собрано монет§7:§f ",  0, new int[] {50, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    
    SN_game     (358, Game.SN,  "§eРаунды§7:§f ",  1, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    SN_win      (359, Game.SN,  "§2Победы§7:§f ",  10, new int[] {10, 100, 250, 500, 1000}, ADD ),
    SN_loose    (360, Game.SN,  "§cПоражения§7:§f ",  0, null, SUB ),
    SN_gold     (361, Game.SN,  "§6Собрано слитков§7:§f ",  0, new int[] {50, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    
    QU_game     (362, Game.QU,  "§eРаунды§7:§f ",  6, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    QU_win      (363, Game.QU,  "§2Победы§7:§f ",  25, new int[] {10, 100, 250, 500, 1000}, ADD ),
    QU_twin     (364, Game.QU,  "§2Командные-Победы§7:§f ",  18, new int[] {10, 100, 300, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    QU_loose    (365, Game.QU,  "§cПоражения§7:§f ",  0, null, SUB ),
    QU_kill     (366, Game.QU,  "§bДуши§7:§f ",  0, new int[] {50, 100, 500, 1000, 10000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    QU_death    (367, Game.QU,  "§cПогиб§7:§f ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    
    ZH_game     (368, Game.ZH,  "§eРаунды§7:§f ",  2, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    ZH_win      (369, Game.ZH,  "§2Победы§7:§f ",  6, new int[] {10, 100, 250, 500, 1000}, ADD ),
    ZH_loose    (370, Game.ZH,  "§cПоражения§7:§f ",  0, null, SUB ),
    ZH_zklls    (371, Game.ZH,  "§cУбийства за Зомби§7:§f ",  1, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    ZH_pdths    (372, Game.ZH,  "§aСмерти за Выжившего§7:§f ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
	
    WZ_game     (373, Game.WZ,  "§3Раунды§7:§f ",  5, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    WZ_klls     (374, Game.WZ,  "§2Убийства§7:§f ",  1, new int[] {50, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    WZ_mbs      (375, Game.WZ,  "§eУбитые Мобы§7:§f ", 0, new int[] {10, 100, 500, 1000, 10000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    WZ_dths     (376, Game.WZ,  "§4Смерти§7:§f ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    WZ_win      (377, Game.WZ,  "§aПобеды§7:§f ",  25, new int[] {1, 10, 100, 500, 1000}, ADD ),
    WZ_loose    (378, Game.WZ,  "§cПоражения§7:§f ",  -5, null, SUB ),
	
    PA_done     (379, Game.PA,  "§eПройдено паркуров§7:§f ",  15, new int[] {1, 50, 100, 666, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    PA_chpt     (380, Game.PA,  "§2ЧекПоинты§7:§f ",  2, new int[] {50, 100, 666, 1000, 10000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    PA_falls    (381, Game.PA,  "§cПадения§7:§f ",  0,  null, NONE ), //new int[] должно быть 5 штук, не удалять!

    
    KB_twin     (382, Game.KB,  "§2Победы в Турнире§7:§f ",  5, new int[] {10, 100, 300, 500, 1000}, ADD ),
    KB_cwin     (383, Game.KB,  "§2Победы в Испытании§7:§f ",  5, new int[] {10, 100, 300, 500, 1000}, ADD ),
    KB_loose    (384, Game.KB,  "§cПоражения§7:§f ",  0, null, SUB ),
    KB_kill     (385, Game.KB,  "§bДуши§7:§f ",  0, new int[] {10, 100, 300, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    KB_death    (386, Game.KB,  "§cПогиб§7:§f ",  0, null, NONE ), //new int[] должно быть 5 штук, не удалять!
    KB_proj     (387, Game.KB, "§dВыстрелы §7:§f ",  0, new int[] {50, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    KB_abil     (388, Game.KB,  "§3Использование особенностей§7:§f ",  0, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    KB_soup     (389, Game.KB, "§dСъедено супчиков§7:§f ",  0, new int[] {50, 100, 500, 1000, 10000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    //CS_mobs     (390, Game.CS, "§dCS_mobs§7:§f ",  1, new int[] {10, 100, 300, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    //CS_spnrs    (391, Game.CS, "§dCS_spnrs§7:§f ",  1, new int[] {10, 100, 300, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    
    DA_dungee   (400, Game.DA,  "§3Данжей найдено§7:§f ",  15, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    
    //ApiOstrov.addCustomStat(p, "f_create", 1);
    MI_lvl      (410, Game.MI,  "§3Поднято уровней клана§7:§f ",  0, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    MI_turr     (411, Game.MI,  "§3Построено турелей§7:§f ",  0, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    //кастомные: fCreate создать клан, fJoin вступить в клан, fClaimXX захвачено терриконов, fJob Взять подработку, 
    //fPrivateKraz возжечь личных крац, fSubst(reach) накопить субстанции для клана, структуры fStrConverter fStrFerma fStrFactory fStrMine fStrAvp fStrProt fStrTp
    //fAlly союзник, fWar война
    
    SK_size     (420, Game.SK,  "§3Увеличение островка§7:§f ",  0, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    SK_ch       (421, Game.SK,  "§3Выполнение заданий§7:§f ",  0, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    SK_chu      (422, Game.SK,  "§3Уникальных заданий§7:§f ",  0, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    SK_emer     (423, Game.SK,  "§3Получено изумрудов§7:§f ",  0, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    SK_biome    (424, Game.SK,  "§3Открыто биомов§7:§f ",  0, new int[] {10, 100, 250, 500, 1000}, NONE ), //new int[] должно быть 5 штук, не удалять!
    //scCreate  scLevel(reach) scSizeWorld(reach) scTrade сделка у торговца
    //Люк Скайблокер  Дарт Скайблокер
    
    //аркаим создать приват, добавить друзей, маскировка, поставить 999 блоков
    //GM.thisServerGame.name()+"_region" - при создании
    //GM.thisServerGame.name()+"_member" - при добавлении юзера
    ;
    
    public static final int diff = 300; //разница в тэге между постоянной и дневной статой
    
    public final int tag;
    public final Game game;
    public final String desc;
    public final int exp_per_point;
    public final int[] achiv;
    public final KarmaChange karmaChange;
    
    
    private Stat(final int tag, final Game game, final String desc, final int exp_per_point, final int[] achiv, final KarmaChange karmaChange){
        this.tag = tag;
        this.game = game;
        this.desc = desc;
        this.exp_per_point = exp_per_point;
        this.achiv = achiv;
        this.karmaChange = karmaChange;
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


    public enum KarmaChange {
        ADD, SUB, NONE;
    }

}
