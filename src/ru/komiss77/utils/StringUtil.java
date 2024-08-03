package ru.komiss77.utils;

import org.apache.commons.lang.WordUtils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.StreamSupport;

public class StringUtil {

    private static final String PATTERN_ENG = "[A-Za-z_]";
    private static final String PATTERN_ENG_NUM = "\\w"; //[A-Za-z0-9_]";
    private static final String PATTERN_ENG_RUS = "[A-Za-zА-Яа-я_]";
    private static final String PATTERN_ENG_NUM_RUS = "[A-Za-z0-9А-Яа-я_]";


    public static String[] wrap(final String msg, final int length, final String newLine) {
        if (msg.length() < 2) return new String[]{msg};
        final char split = '\n';
        final String line = split + newLine;
        return WordUtils.wrap(msg, length, line, false).substring(1).split(line);
    }

    public static boolean checkString(String message, final boolean allowNumbers, final boolean allowRussian) {
        return checkString(message, false, allowNumbers, allowRussian);
    }

    public static boolean checkString(String message, final boolean allowSpace, final boolean allowNumbers, final boolean allowRussian) {
        if (allowNumbers && allowRussian) {
            message = message.replaceAll(PATTERN_ENG_NUM_RUS, "");
        } else if (allowNumbers) {
            message = message.replaceAll(PATTERN_ENG_NUM, "");
        } else if (allowRussian) {
            message = message.replaceAll(PATTERN_ENG_RUS, "");
        } else {
            message = message.replaceAll(PATTERN_ENG, "");
        }
        return allowSpace ? message.isBlank() : message.isEmpty();
    }

    public static String toSigFigs(final double n, final byte sf) {
        final String nm = String.valueOf(n);
        return nm.indexOf('.') + sf + 1 < nm.length() ? nm.substring(0, nm.indexOf('.') + sf + 1) : nm;
    }

    public static String listToString(final Iterable<?> array, final String splitter) {
        if (array == null) return "";
       /* StringBuilder sb=new StringBuilder();
        array.forEach( (s) -> {
            sb.append(s).append(splitter);
        });
        return sb.toString();*/
        return StreamSupport.stream(array.spliterator(), true)
                .map(Object::toString)
                .reduce((t, u) -> t + "," + u)
                .orElse("");
    }

    public static <E> String toString(final Collection<E> array, final String separator) {
        if (array == null || array.isEmpty()) return "";
        return array.stream()
                .map(E::toString)
                .reduce((t, u) -> t + separator + u)
                .orElse("");
    }

    public static String enumSetToString(final Set<?> enumSet) {
        StringBuilder sb = new StringBuilder();
        enumSet.forEach(eNum -> sb.append(eNum.toString()).append(","));
        return sb.toString();//allowRole;
    }


    public static String nrmlzStr(final String s) {
        final char[] ss = s.toLowerCase().toCharArray();
        ss[0] = Character.toUpperCase(ss[0]);
        for (byte i = (byte) (ss.length - 1); i > 0; i--) {
            switch (ss[i]) {
                case '_':
                    ss[i] = ' ';
                case ' ':
                    ss[i + 1] = Character.toUpperCase(ss[i + 1]);
                    break;
                default:
                    break;
            }
        }
        return String.valueOf(ss);
    }


    public static String getPercentBar(final int max, final int current, final boolean withPercent) {
        if (current < 0 || current > max) return "§8||||||||||||||||||||||||| ";
        final double percent = (double) current / max * 100;
        int p10 = (int) (percent * 10);
        final double percent1d = ((double) p10 / 10); //чтобы не показывало 100
        int pos = p10 / 40;
        if (pos < 2) pos = 2;
        else if (pos > 26) pos = 26;
        if (withPercent) {
            return new StringBuilder("§a||||||||||||||||||||||||| ").insert(pos, "§8").append("§f").append(percent1d).append("%").toString();
        } else {
            return new StringBuilder("§a||||||||||||||||||||||||| ").insert(pos, "§8").toString();
        }
    }
}
