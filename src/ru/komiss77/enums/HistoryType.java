package ru.komiss77.enums;


public enum HistoryType {

    NONE("", "AIR"),

    BAN_SET("Бан", "INK_SAC"),
    UNBAN("Разбан", "GLOW_INK_SAC"),
    BANIP_SET("Бан по IP", "ENDER_PEARL"),
    UNBANIP("Разбан IP", "ENDER_EYE"),
    MUTE_SET("Молчанка", "BEETROOT_SOUP"),
    UNMUTE("Снятие Молчанки", "BOWL"),
    KICK("Пинок", "BLAZE_POWDER"),

    GROUP_ADD("Добавление Группы", "RESIN_BRICK"),
    GROUP_TIME_ADD("Продление Группы", "COPPER_INGOT"),
    GROUP_EXPIRIED("Снятие Группы", "RESIN_CLUMP"),
    PERMS_ADD("Добавление Права", "IRON_INGOT"),
    PERMS_TIME_ADD("Продление Права", "RAW_IRON"),
    PERMS_EXPIRIED("Удаление Права", "IRON_NUGGET"),
    STAFF_ADD("Получение Должности", "MAGMA_CREAM"),
    STAFF_DEL("Снятие с Должности", "SLIME_BALL"),

    MONEY_REAL_USE("Затрата Рил", "RAW_GOLD"),
    MONEY_REAL_ADD("Получение Рил", "GOLD_NUGGET"),
    MONEY_REAL_WITHDRAW("Заявка на Вывод", "GOLD_INGOT"),

    SESSION_INFO("Сессия", "HONEYCOMB"),
    OS_LVL_UP("Повышение Уровня", "EXPERIENCE_BOTTLE"),
    PASS_CHANGE("Смена Пароля", "GLISTERING_MELON_SLICE"),
    MISSION("Выполнение Миссии", "GUSTER_BANNER_PATTERN"),
    GAMEMODE("Смена Режима", "TURTLE_HELMET"),

    FRIEND_ADD("Принятие Друга", "GLOWSTONE_DUST"),
    FRIEND_DEL("Удаление Друга", "REDSTONE"),
    PARTY_ADD("Добавление в Комманду", "FERMENTED_SPIDER_EYE"),
    PARTY_DEL("Удаление из Комманды", "SPIDER_EYE"),
    ;

    public final String for_chat;
    public final String displayMat;

    HistoryType(final String for_chat, final String type) {
        this.for_chat = for_chat;
        this.displayMat = type;
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
