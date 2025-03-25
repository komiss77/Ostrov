package ru.komiss77.enums;


import org.bukkit.inventory.ItemType;

public enum HistoryType {

    NONE("", ItemType.AIR),

    BAN_SET("Бан", ItemType.INK_SAC),
    UNBAN("Разбан", ItemType.GLOW_INK_SAC),
    BANIP_SET("Бан по IP", ItemType.ENDER_PEARL),
    UNBANIP("Разбан IP", ItemType.ENDER_EYE),
    MUTE_SET("Молчанка", ItemType.BEETROOT_SOUP),
    UNMUTE("Снятие Молчанки", ItemType.BOWL),
    KICK("Пинок", ItemType.BLAZE_POWDER),

    GROUP_ADD("Добавление Группы", ItemType.RESIN_BRICK),
    GROUP_TIME_ADD("Продление Группы", ItemType.COPPER_INGOT),
    GROUP_EXPIRIED("Снятие Группы", ItemType.RESIN_CLUMP),
    PERMS_ADD("Добавление Права", ItemType.IRON_INGOT),
    PERMS_TIME_ADD("Продление Права", ItemType.RAW_IRON),
    PERMS_EXPIRIED("Удаление Права", ItemType.IRON_NUGGET),
    STAFF_ADD("Получение Должности", ItemType.MAGMA_CREAM),
    STAFF_DEL("Снятие с Должности", ItemType.SLIME_BALL),

    MONEY_REAL_USE("Затрата Рил", ItemType.RAW_GOLD),
    MONEY_REAL_ADD("Получение Рил", ItemType.GOLD_NUGGET),
    MONEY_REAL_WITHDRAW("Заявка на Вывод", ItemType.GOLD_INGOT),

    SESSION_INFO("Сессия", ItemType.HONEYCOMB),
    OS_LVL_UP("Повышение Уровня", ItemType.EXPERIENCE_BOTTLE),
    PASS_CHANGE("Смена Пароля", ItemType.GLISTERING_MELON_SLICE),
    MISSION("Выполнение Миссии", ItemType.GUSTER_BANNER_PATTERN),
    GAMEMODE("Смена Режима", ItemType.TURTLE_HELMET),

    FRIEND_ADD("Принятие Друга", ItemType.GLOWSTONE_DUST),
    FRIEND_DEL("Удаление Друга", ItemType.REDSTONE),
    PARTY_ADD("Добавление в Комманду", ItemType.FERMENTED_SPIDER_EYE),
    PARTY_DEL("Удаление из Комманды", ItemType.SPIDER_EYE),
    ;


    public final String for_chat;
    @Deprecated
    public final String displayMat;
    public final ItemType type;

    HistoryType(final String for_chat, final ItemType type) {
        this.for_chat = for_chat;
        this.type = type;
        this.displayMat = type.key()
            .asMinimalString().toUpperCase();
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
