package ru.komiss77.modules.translate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;


//              НЕ ПЕРЕМЕЩАТЬ, НЕ импортировать остров! Использует прокси!!

//для обновы файликов поиском искать в папке ассетс клиента по строке "language.code": "ru_ru"

public enum EnumLang {

    EN_US("en_us", "en", new HashMap<>()),
    RU_RU("en_us", "ru", new HashMap<>()),
    ;

    //private static final Map<String, EnumLang> lookup = new HashMap<>();

    static {
        for (EnumLang lang : EnumSet.allOf(EnumLang.class)) {
            //lookup.put(lang.getLocale(), lang);
            //InputStreamReader in = new InputStreamReader(EnumLang.class.getResourceAsStream("/lang/" + lang.locale + ".json"), Charset.forName("UTF-8"));
            final InputStreamReader in = new InputStreamReader(EnumLang.class.getResourceAsStream("/ru/komiss77/modules/translate/"+lang.locale+".json"), Charset.forName("UTF-8"));
            final BufferedReader b = new BufferedReader(in);
            try {
                readFile(lang, b);
                //Ostrov.log_ok("§bLanguageHelper : поддержка перевода для "+lang.getLocale());
            } catch (Exception e) {
                //Ostrov.log_err("LanguageHelper : поддержка перевода для "+lang.getLocale()+" : "+e.getMessage());
                e.printStackTrace();
            }
                
        }
        
    }

    private final String locale;
    public final String targetLanguageCode; //для яндекса
    private final Map<String, String> map;


    EnumLang(String locale, String targetLanguageCode, Map<String, String> map) {
        this.locale = locale;
        this.targetLanguageCode = targetLanguageCode;
        this.map = map;
    }


    public static EnumLang get(final String locale) {
        //EnumLang result = lookup.get(locale.toLowerCase(Locale.ENGLISH));
        //return result == null ? EN_US : result;
        return locale.equalsIgnoreCase("ru_ru") ? RU_RU : EN_US;
    }

    public static void clean() {
        for (EnumLang enumLang : EnumLang.values()) {
            enumLang.getMap().clear();
        }
    }

    public static void readFile(EnumLang enumLang, BufferedReader reader) throws IOException {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> map = gson.fromJson(reader, type);
            enumLang.map.putAll(map);
        } finally {
            reader.close();
        }
    }

    public String getLocale() {
        return locale;
    }

    public Map<String, String> getMap() {
        return map;
    }
}



























    /*AF_ZA("af_za", new HashMap<>()),
    AR_SA("ar_sa", new HashMap<>()),
    AST_ES("ast_es", new HashMap<>()),
    AZ_AZ("az_az", new HashMap<>()),
    BAR("bar", new HashMap<>()),
    BE_BY("be_by", new HashMap<>()),
    BG_BG("bg_bg", new HashMap<>()),
    BR_FR("br_fr", new HashMap<>()),
    BRB("brb", new HashMap<>()),
    BS_BA("bs_ba", new HashMap<>()),
    CA_ES("ca_es", new HashMap<>()),
    CS_CZ("cs_cz", new HashMap<>()),
    CY_GB("cy_gb", new HashMap<>()),
    DA_DK("da_dk", new HashMap<>()),
    DE_AT("de_at", new HashMap<>()),
    DE_CH("de_ch", new HashMap<>()),
    DE_DE("de_de", new HashMap<>()),
    EL_GR("el_gr", new HashMap<>()),
    EN_AU("en_au", new HashMap<>()),
    EN_CA("en_ca", new HashMap<>()),
    EN_GB("en_gb", new HashMap<>()),
    EN_NZ("en_nz", new HashMap<>()),
    EN_PT("en_pt", new HashMap<>()),
    EN_UD("en_ud", new HashMap<>()),*/
/*ENP("enp", new HashMap<>()),
    ENWS("enws", new HashMap<>()),
    EO_UY("eo_uy", new HashMap<>()),
    ES_AR("es_ar", new HashMap<>()),
    ES_CL("es_cl", new HashMap<>()),
    ES_ES("es_es", new HashMap<>()),
    ES_MX("es_mx", new HashMap<>()),
    ES_UY("es_uy", new HashMap<>()),
    ES_VE("es_ve", new HashMap<>()),
    ET_EE("et_ee", new HashMap<>()),
    EU_ES("eu_es", new HashMap<>()),
    FA_IR("fa_ir", new HashMap<>()),
    FI_FI("fi_fi", new HashMap<>()),
    FIL_PH("fil_ph", new HashMap<>()),
    FO_FO("fo_fo", new HashMap<>()),
    FR_CA("fr_ca", new HashMap<>()),
    FR_FR("fr_fr", new HashMap<>()),
    FRA_DE("fra_de", new HashMap<>()),
    FY_NL("fy_nl", new HashMap<>()),
    GA_IE("ga_ie", new HashMap<>()),
    GD_GB("gd_gb", new HashMap<>()),
    GL_ES("gl_es", new HashMap<>()),
    GV_IM("gv_im", new HashMap<>()),
    HAW_US("haw_us", new HashMap<>()),
    HE_IL("he_il", new HashMap<>()),
    HI_IN("hi_in", new HashMap<>()),
    HR_HR("hr_hr", new HashMap<>()),
    HU_HU("hu_hu", new HashMap<>()),
    HY_AM("hy_am", new HashMap<>()),
    ID_ID("id_id", new HashMap<>()),
    IG_NG("ig_ng", new HashMap<>()),
    IO_EN("io_en", new HashMap<>()),
    IS_IS("is_is", new HashMap<>()),
    IT_IT("it_it", new HashMap<>()),
    JA_JP("ja_jp", new HashMap<>()),
    JBO_EN("jbo_en", new HashMap<>()),
    KA_GE("ka_ge", new HashMap<>()),
    KAB_KAB("kab_kab", new HashMap<>()),
    KK_KZ("kk_kz", new HashMap<>()),
    KN_IN("kn_in", new HashMap<>()),
    KO_KR("ko_kr", new HashMap<>()),
    KSH("ksh", new HashMap<>()),
    KW_GB("kw_gb", new HashMap<>()),
    LA_LA("la_la", new HashMap<>()),
    LB_LU("lb_lu", new HashMap<>()),
    LI_LI("li_li", new HashMap<>()),
    LOL_US("lol_us", new HashMap<>()),
    LT_LT("lt_lt", new HashMap<>()),
    LV_LV("lv_lv", new HashMap<>()),
    MI_NZ("mi_nz", new HashMap<>()),
    MK_MK("mk_mk", new HashMap<>()),
    MN_MN("mn_mn", new HashMap<>()),
    MOH_CA("moh_ca", new HashMap<>()),
    MS_MY("ms_my", new HashMap<>()),
    MT_MT("mt_mt", new HashMap<>()),
    NDS_DE("nds_de", new HashMap<>()),
    NL_BE("nl_be", new HashMap<>()),
    NL_NL("nl_nl", new HashMap<>()),
    NN_NO("nn_no", new HashMap<>()),
    NO_NO("no_no", new HashMap<>()),
    NUK("nuk", new HashMap<>()),
    OC_FR("oc_fr", new HashMap<>()),
    OJ_CA("oj_ca", new HashMap<>()),
    OVD("ovd", new HashMap<>()),
    PL_PL("pl_pl", new HashMap<>()),
    PT_BR("pt_br", new HashMap<>()),
    PT_PT("pt_pt", new HashMap<>()),
    QYA_AA("qya_aa", new HashMap<>()),
    RO_RO("ro_ro", new HashMap<>()),*/
    /*SE_NO("se_no", new HashMap<>()),
    SK_SK("sk_sk", new HashMap<>()),
    SL_SI("sl_si", new HashMap<>()),
    SO_SO("so_so", new HashMap<>()),
    SQ_AL("sq_al", new HashMap<>()),
    SR_SP("sr_sp", new HashMap<>()),
    SV_SE("sv_se", new HashMap<>()),
    SWG("swg", new HashMap<>()),
    SXU("sxu", new HashMap<>()),
    SZL("szl", new HashMap<>()),
    TA_IN("ta_in", new HashMap<>()),
    TH_TH("th_th", new HashMap<>()),
    TLH_AA("tlh_aa", new HashMap<>()),
    TR_TR("tr_tr", new HashMap<>()),
    TT_RU("tt_ru", new HashMap<>()),
    TZL_TZL("tzl_tzl", new HashMap<>()),
    UK_UA("uk_ua", new HashMap<>()),
    VAL_ES("val_es", new HashMap<>()),
    VEC_IT("vec_it", new HashMap<>()),
    VI_VN("vi_vn", new HashMap<>()),
    YO_NG("yo_ng", new HashMap<>()),
    ZH_CN("zh_cn", new HashMap<>()),
    ZH_TW("zh_tw", new HashMap<>());*/
