package ru.komiss77.enums;



public enum AuthCause {
    
    ГОСТЬ,
    СЕССИЯ,
    ПАРОЛЬ_ПРИНЯТ,
    НОВЫЙ_АККАУНТ
    ;
    
    
    public static boolean exist(final String as_string){
        for(AuthCause s_: AuthCause.values()){
            if (s_.toString().equals(as_string)) return true;
        }
        return false;
    }

    public static AuthCause of(final String as_string) {
        try {
            return AuthCause.valueOf(as_string);
        } catch (IllegalArgumentException ex) {
//System.out.println("AuthCause of : no enum "+as_string);
            return null;
        }
    }
    
}
