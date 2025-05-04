package ru.komiss77.enums;


public enum HistoryType {

  NONE("", "AIR", 0),

  BAN_SET("Бан", "INK_SAC", -5),
  UNBAN("Разбан", "GLOW_INK_SAC", 4),
  BANIP_SET("Бан по IP", "ENDER_PEARL", -10),
  UNBANIP("Разбан IP", "ENDER_EYE", 9),
  MUTE_SET("Молчанка", "BEETROOT_SOUP", -1),
  UNMUTE("Снятие Молчанки", "BOWL", 0),
  KICK("Пинок", "BLAZE_POWDER", -1),

  GROUP_ADD("Добавление Группы", "RESIN_BRICK", 1),
  GROUP_TIME_ADD("Продление Группы", "COPPER_INGOT", 1),
  GROUP_EXPIRIED("Снятие Группы", "RESIN_CLUMP", 0),
  PERMS_ADD("Добавление Права", "IRON_INGOT", 0),
  PERMS_TIME_ADD("Продление Права", "RAW_IRON", 1),
  PERMS_EXPIRIED("Удаление Права", "IRON_NUGGET", 0),
  STAFF_ADD("Получение Должности", "MAGMA_CREAM", 1),
  STAFF_DEL("Снятие с Должности", "SLIME_BALL", -1),

  MONEY_REAL_USE("Затрата Рил", "RAW_GOLD", 1),
  MONEY_REAL_ADD("Получение Рил", "GOLD_NUGGET", 1),
  MONEY_REAL_WITHDRAW("Заявка на Вывод", "GOLD_INGOT", 1),

  SESSION_INFO("Сессия", "HONEYCOMB", 0),
  OS_LVL_UP("Повышение Уровня", "EXPERIENCE_BOTTLE", 1),
  PASS_CHANGE("Смена Пароля", "GLISTERING_MELON_SLICE", 0),
  MISSION("Выполнение Миссии", "GUSTER_BANNER_PATTERN", 1),
  GAMEMODE("Смена Режима", "TURTLE_HELMET", 0),

  FRIEND_ADD("Принятие Друга", "GLOWSTONE_DUST", 1),
  FRIEND_DEL("Удаление Друга", "REDSTONE", -1),
  PARTY_ADD("Добавление в Комманду", "FERMENTED_SPIDER_EYE", 0),
  PARTY_DEL("Удаление из Комманды", "SPIDER_EYE", 0),
    ;

    public final String for_chat;
    public final String displayMat;
  public final int repChange;

  HistoryType(final String for_chat, final String type, final int repChange) {
        this.for_chat = for_chat;
        this.displayMat = type;
    this.repChange = repChange;
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
