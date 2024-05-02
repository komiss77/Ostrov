package ru.komiss77.modules.player;


class AkkauntInfo__ {
    
    final int userId;
    final String name;
    final String pass;
    final String phone;
    final String email;
    final String lastIp;
    final boolean ipProtect;
    final int reg;
    final int logout;
    final int banTo;
    final String banReason;
    final int banIpTo;
    final String banIpReason;


    AkkauntInfo__(final int userId, 
            final String name, 
            final String pass, 
            final String phone, 
            final String email, 
            final String lastIp, 
            final boolean ipProtect,
            final int reg,
            final int logout, 
            final int banTo,
            final String banReason, 
            final int banIpTo,
            final String banIpReason
    ) {
        this.userId = userId;
        this.name = name;
        this.pass = pass;
        this.phone = phone;
        this.email = email;
        this.lastIp = lastIp;
        this.ipProtect = ipProtect;
        this.reg = reg;
        this.logout = logout;
        this.banTo = banTo;
        this.banReason = banReason;
        this.banIpTo = banIpTo;
        this.banIpReason = banIpReason;
    }
    
}
