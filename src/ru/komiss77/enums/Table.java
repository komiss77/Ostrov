package ru.komiss77.enums;




public enum Table {
    
    NONE("", ""),
    BUNGEE_SERVERS("`bungeeServers`", ""),
    ARENAS("`arenasInfo`", ""),
    
    USER("`userData`", "`userId`"), //при входе ищет по `name` таблица обязательно  utf8_general_ci      ci — case insensitive. 
    STAT("`stats`", "`userId`"),
    DAILY("`dailyStats`", "`userId`"),
    JUDGEMENT("`judgement`", "`name`"), //таблица обязательно  utf8_general_ci      ci — case insensitive. 
    HISTORY("`history`", "`name`"), //просто кидает по имени
    
    PEX_BUNGEE_STAFF("`bungeestaff`", "`name`"),  //таблица администрации, загружаются в auth_bungee
    PEX_BUNGEE_PERMS("`bungeeperms`", ""), //права групп для банжи, загружаются в auth_bungee
    PEX_GROUPS("`groups`", ""),  //группы с описанием 
    PEX_GROUP_PERMS("`groupperms`", ""),  //права групп для спигота, загружаются в острове
    PEX_USER_GROUPS("`usergroups`", "`name`"),  //группы игроков, загружаются в auth_spigot
    PEX_USER_PERMS("`userperms`", "`name`"),  //личные права игроков, загружаются в auth_spigot
    
    FRIEND_FRIENDS("`fr_friends`", "`name`"),
    FRIEND_SETTINGS("`fr_settings`", "`name`"),
    
    PAYMENTS("`payments`", "`name`"),
    ;
    
    
    public String table_name;
    public String id_column;
    
    private Table (String table_name, String id_column) {
        this.table_name=table_name;
        this.id_column=id_column;
    }
}
