package ru.komiss77.ProfileMenu;


public enum E_Pass {
    //!!!!!!!!!!!!!  ДОЛНО Быть такое же название в DATA !!!!!!!!!!!!
    //!!! slot должен быть уникальный (используется как tag!)
    NAME        (1, "PAINTING", "Ник", "", "", false),
    ИМЯ_ФАМИЛИЯ (2, "PAPER", "Имя, Фамилия", "", "не указано", true),
    ПОЛ         (3, "PAPER", "Пол", "Ваш пол", "бесполое", true),
    РОДИЛСЯ     (4, "PAPER", "Дата Рождения", "", "01.01.1970", true),
    СТРАНА      (5, "PAPER", "Страна", "", "не указано", true),
    ГОРОД       (6, "PAPER", "Город", "", "не указано", true),
    
    ПАРА        (11, "PAINTING", "Супруг(а) на Острове", "", "холост/не замужем", false),
    USER_GROUPS (12, "PAINTING", "Группы", "", "нет групп", false),
    REG_TIME    (13, "PAINTING", "Дата Регистрации", "", "0", false),
    PLAY_TIME   (14, "PAINTING", "Игровое время", "", "0", false),
    
    
    УРОВЕНЬ     (20, "PAINTING", "Уровень Островитянина", "", "0", false),
    ОПЫТ        (21, "PAINTING", "Опыт", "", "0", false),
    РЕПУТАЦИЯ   (22, "PAINTING", "Репутация на сервере", "", "0", false),
    КАРМА       (23, "PAINTING", "Карма", "", "0", false),
    
    ТЕЛЕФОН     (28, "PAPER", "Телефон", "", "(XXX)XXX-XXXX", true),
    МЫЛО        (29, "PAPER", "эл.почта", "", "не указано", true),
    СКАЙП       (30, "PAPER", "Скайп", "", "не указано", true),
    ВКОНТАКТЕ   (31, "PAPER", "ВК", "", "не указано", true),
    ЮТУБ        (32, "PAPER", "Ютуб", "", "не указано", true),
    ТВИЧ        (33, "PAPER", "Твич", "", "не указано", true),
    О_СЕБЕ      (34, "PAPER", "О себе", "", "нечего сказать", true),
    
    
    
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
        for (E_Pass current:E_Pass.values()) {
            if (current.toString().equals(name)) return true;
        }
        return false;
    }

    
    
    
    
    
}
