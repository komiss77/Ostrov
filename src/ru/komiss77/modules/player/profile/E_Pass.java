package ru.komiss77.modules.player.profile;


public enum E_Pass {
    //!!!!!!!!!!!!!  ДОЛНО Быть такое же название в DATA !!!!!!!!!!!!
    //!!! slot должен быть уникальный (используется как tag!)
    
    //data
    NAME        (1, "PAINTING", "Ник", "", "", false),
    FAMILY (2, "PAPER", "Имя, Фамилия", "", "не указано", true),
    GENDER         (3, "PAPER", "Пол", "Ваш пол", "бесполое", true),
    BIRTH     (4, "PAPER", "Дата Рождения", "", "01.01.1970", true),
    LAND      (5, "PAPER", "Страна", "", "не указано", true),
    CITY       (6, "PAPER", "Город", "", "не указано", true),
    PHONE     (28, "PAPER", "Телефон", "", "(XXX)XXX-XXXX", true),
    EMAIL        (29, "PAPER", "эл.почта", "", "не указано", true),
    DISCORD       (30, "PAPER", "Скайп", "", "не указано", true),
    VK   (31, "PAPER", "ВКонтакте", "", "не указано", true),
    YOUTUBE        (32, "PAPER", "Канал Ютуб", "", "не указано", true),
    ABOUT      (34, "PAPER", "О себе", "", "нечего сказать", true),
    MARRY        (11, "PAINTING", "Супруг(а) на Острове", "", "холост/не замужем", false),
    SIENCE    (13, "PAINTING", "Дата Регистрации", "", "0", false),
    REPUTATION   (22, "PAINTING", "Репутация", "", "0", false),
    KARMA       (23, "PAINTING", "Карма", "", "0", false),
    IP       (24, "PAINTING", "Текущий IP", "", "0", false),
    IPPROTECT       (25, "PAINTING", "Защита по IP", "", "0", false),
    //KARMA       (23, "PAINTING", "Карма", "", "0", false),
    
    //из статы
    PLAY_TIME   (14, "PAINTING", "Игровое время", "", "0", false),
    LEVEL     (20, "PAINTING", "Уровень", "", "0", false),
    EXP        (21, "PAINTING", "Опыт", "", "0", false),
    
    //
    USER_GROUPS (12, "PAINTING", "Группы", "", "нет групп", false),
    
    
    
    
    
    ;
    
    public int slot;
    public String mat;
    public String item_name;
    public String lore;
    public String default_value;
    public boolean editable;
    
    
    private E_Pass(int slot, String mat, String item_name, String lore, String default_value, boolean editable ){
        this.slot = slot;
        this.mat = mat;
        this.item_name = item_name;
        this.lore = lore;
        this.default_value = default_value;
        this.editable = editable;
    }
    
    
    public static boolean exist(final String name) {
//System.out.println("exist? name="+name);
        for (E_Pass current:E_Pass.values()) {
            if (current.name().equalsIgnoreCase(name)) return true;
        }
        return false;
    }
    
    public static E_Pass fromStrind(final String name) {
//System.out.println("exist? name="+name);
        for (E_Pass current:E_Pass.values()) {
            if (current.name().equalsIgnoreCase(name)) return current;
        }
        return null;
    }

    
    
    
    
    
}
