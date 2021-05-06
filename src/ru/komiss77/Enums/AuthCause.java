package ru.komiss77.Enums;




public enum AuthCause {
    
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
    
}
