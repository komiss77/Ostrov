package ru.komiss77.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import ru.komiss77.Ostrov;

//не импортировать ничего от Paper! (юзает прокси)
public class TimeUtil {

    private static final Date date;
    private static final SimpleDateFormat ddMMyy_HHmm;  //
    private static final SimpleDateFormat ddMMyy;

    static {
        ddMMyy_HHmm = new java.text.SimpleDateFormat("dd.MM.yy HH:mm");
        ddMMyy = new java.text.SimpleDateFormat("dd.MM.yy");
        date = new java.util.Date(System.currentTimeMillis());
    }

    public static boolean isNewDay() { //после рестарта определить, настал ли новый день
        return Ostrov.newDay;
    }

    public static String dateFromStamp(final int unixTime) {
        date.setTime(unixTime * 1000L);
        return ddMMyy_HHmm.format(date);
    }

    public static String ddMMyy(final int unixTime) {
        date.setTime(unixTime * 1000L);
        return ddMMyy.format(date);
    }
    public static String getCurrentHourMin() {
        return Ostrov.getCurrentHourMin();
    }

    public static String secondToTime(int second) { //c днями и нед!
        if (second < 0) return "---";
        final int year = second / 30_758_400; //356*24*60*60
        second -= year * 30_758_400; //от секунд отнимаем годы
        final int month = second / 2_678_400; //31*24*60*60
        second -= month * 2_678_400; //от секунд отнимаем месяцы

        final int week = second / 604_800; //7*24*60*60
        if (year == 0)
            second -= week * 604_800; //от секунд отнимаем недели. недели не показываем и не отнимаем, если счёт на года

        final int day = second / 86_400; //24*60*60
        second -= day * 86_400; //от секунд отнимаем дни
        final int hour = second / 3600; //60*60
        second -= hour * 3600;  //от секунд отнимаем часы
        final int min = second / 60;
        second -= min * 60; //от секунд отнимаем минуты

        StringBuilder sb = new StringBuilder();
        if (year > 0) sb.append(year).append("г. ");
        if (month > 0) sb.append(month).append("мес. ");
        if (week > 0 && year == 0) sb.append(week).append("нед. ");
        if (day > 0) sb.append(day).append("д. ");
        if (year > 0) return sb.toString(); //счёт на года - достаточно до дней
        if (hour > 0) sb.append(hour).append("ч. ");
        if (month > 0 || week > 0) return sb.toString(); //счёт на месяца - достаточно до часов
        if (min > 0) sb.append(min).append("мин. ");
        if (second > 0) sb.append(second).append("сек. ");
        return sb.toString();
    }

    public static String dayOfWeekName(final int dayOfWeekNumber) {
        switch (dayOfWeekNumber) {
            case 1:
                return "вс";
            case 2:
                return "пн";
            case 3:
                return "вт";
            case 4:
                return "ср";
            case 5:
                return "чт";
            case 6:
                return "пт";
            default:
                return "сб";
        }
    }

}
