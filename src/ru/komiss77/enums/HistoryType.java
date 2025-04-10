package ru.komiss77.enums;

//тут нельзя импортить что-то от бакит (енум общий с прокси)!!

public enum HistoryType {

    NONE("", "AIR"),

    BAN_SET("бан", "INK_SAC"),
    UNBAN("разбан", "GLOW_INK_SAC"),
    BANIP_SET("бан по IP", "ENDER_PEARL"),
    UNBANIP("разбан IP", "ENDER_EYE"),
    //BAN_OFFLINE_SET(""),

    MUTE_SET("молчанка", "BEETROOT_SOUP"),
    UNMUTE("снятие молчанки", "BOWL"),

    KICK("пинок", "BLAZE_POWDER"),

    GROUP_ADD("добавление группы", "RESIN_BRICK"),
    GROUP_TIME_ADD("добавление срока группы", "COPPER_INGOT"),
    GROUP_EXPIRIED("удаление группы", "RESIN_CLUMP"),
    PERMS_ADD("добавление права", "IRON_INGOT"),
    PERMS_TIME_ADD("добавление срока права", "RAW_IRON"),
    PERMS_EXPIRIED("удаление права", "IRON_NUGGET"),
    STAFF_ADD("назначение на должность", "MAGMA_CREAM"),
    STAFF_DEL("снятие с должности", "SLIME_BALL"),

    MONEY_REAL_USE("расходование средств", "RAW_GOLD"),
    MONEY_REAL_ADD("пополнение счёта", "GOLD_NUGGET"),
    MONEY_REAL_WITHDRAW("заявка на вывод", "GOLD_INGOT"),

    SESSION_INFO("сессия", "HONEYCOMB"),
    PASS_CHANGE("смена пароля", "GLISTERING_MELON_SLICE"),

    OS_LVL_UP("Повышение Уровня", "EXPERIENCE_BOTTLE"),
    MISSION("Выполнение Миссии", "GUSTER_BANNER_PATTERN"),
    GAMEMODE("Смена Режима", "TURTLE_HELMET"),

    FRIEND_ADD("Принятие Друга", "GLOWSTONE_DUST"),
    FRIEND_DEL("Удаление Друга", "REDSTONE"),
    PARTY_ADD("Добавление в Комманду", "FERMENTED_SPIDER_EYE"),
    PARTY_DEL("Удаление из Комманды", "SPIDER_EYE"),
    ;


    public final String for_chat;
    public final String displayMat;

    HistoryType(final String for_chat, final String displayMat) {
        this.for_chat = for_chat;
        this.displayMat = displayMat;
        //this.displayMat = type.key().asMinimalString().toUpperCase();
    }


    public static HistoryType by_action(final String as_string) {
        for (HistoryType s_ : HistoryType.values()) {
            if (s_.toString().equals(as_string)) return s_;
        }
        return NONE;
    }


    public static boolean exist(final String as_string) {
        for (HistoryType s_ : HistoryType.values()) {
            if (s_.toString().equals(as_string)) return true;
        }
        return false;
    }

    public HistoryType getSourceType(final HistoryType cmd) {
        return switch (cmd) {
            case BAN_SET -> UNBAN;
            case MUTE_SET -> UNMUTE;
            case BANIP_SET -> UNBANIP;
            default -> NONE;
        };
    }

}
