package ru.komiss77.Enums;




public enum ServerType {

    NONE,
    REG_OLD,
    REG_NEW,
    LOBBY,
    DIAG,
    
    ;
    
    public static ServerType fromString(final String as_string){
        for(ServerType s_: ServerType.values()){
            if (s_.toString().equals(as_string)) return s_;
        }
        return NONE;
    }
    
}

