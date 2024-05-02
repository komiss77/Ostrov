package ru.komiss77.utils;


public class DateUtil {
    
    
    public static String dayOfWeekName(final int dayOfWeekNumber) {
        switch (dayOfWeekNumber) {
            case 1: return "вс";
            case 2: return "пн";
            case 3: return "вт";
            case 4: return "ср";
            case 5: return "чт";
            case 6: return "пт";
            default: return "сб";
        }
    }
    
}
