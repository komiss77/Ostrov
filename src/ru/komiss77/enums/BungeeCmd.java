package ru.komiss77.enums;






public enum BungeeCmd {
    
    
    //MENU("menu", cmdGroup.основные, "ru.ostrov77.auth.bungee.commands.Money"),
    
    MONEY("money", cmdGroup.основные, "ru.ostrov77.auth.bungee.commands.Money"),
    GROUP("group", cmdGroup.основные, "ru.ostrov77.auth.bungee.commands.GroupCommand"),
    STAFF("staff", cmdGroup.основные, "ru.ostrov77.auth.bungee.commands.Staff"),
    JOURNAL("journal", cmdGroup.основные, "ru.ostrov77.auth.bungee.commands.Journal"),
    
    PREFIX("prefix", cmdGroup.привилегии, "ru.ostrov77.auth.bungee.commands.Prefix"),
    SUFFIX("suffix", cmdGroup.привилегии, "ru.ostrov77.auth.bungee.commands.Suffix"),
    
    //SEEN("seen", cmdGroup.основные, "ru.ostrov77.auth.bungee.commands.Seen"),
    //PINFO("suffix", cmdGroup.настройки, "ru.ostrov77.auth.bungee.commands.Pinfo"),
    //SETPASS("suffix", cmdGroup.настройки, "ru.ostrov77.auth.bungee.commands.SetPass"),
    
    BAN("ban", cmdGroup.модераторские, "ru.ostrov77.auth.bungee.commands.Ban"),
    KICK("kick", cmdGroup.модераторские, "ru.ostrov77.auth.bungee.commands.Kick"),
    BANIP("banip", cmdGroup.модераторские, "ru.ostrov77.auth.bungee.commands.BanIp"),
    MUTE("mute", cmdGroup.модераторские, "ru.ostrov77.auth.bungee.commands.Mute"),
    UNBAN("unban", cmdGroup.модераторские, "ru.ostrov77.auth.bungee.commands.UnBan"),
    UNBANIP("unbanip", cmdGroup.модераторские, "ru.ostrov77.auth.bungee.commands.UnBanIp"),
    UNMUTE("unmute", cmdGroup.модераторские, "ru.ostrov77.auth.bungee.commands.UnMute"),
    //KICK("kick", cmdGroup.модераторские, "ru.ostrov77.auth.bungee.commands.Kick"),
    
    HELP("banip", cmdGroup.скрытые, "ru.ostrov77.auth.bungee.commands.Help"),
    CLEAN("clean", cmdGroup.скрытые, "ru.ostrov77.auth.bungee.commands.Clean"),
    RELOAD("bareload", cmdGroup.скрытые, "ru.ostrov77.auth.bungee.commands.Reload"),
    //DIAG("diag", cmdGroup.скрытые, "ru.ostrov77.auth.bungee.commands.Diag"),
    ;
    
    public String execute;
    public cmdGroup group;
    public String clazz;
    
    private BungeeCmd(String execute, cmdGroup group, String clazz){
        this.execute = execute;
        this.group = group;
        this.clazz = clazz;
    }
    












    public enum cmdGroup {
        основные, модераторские, привилегии, настройки, скрытые
        ;
    }

    
    
}
