package ru.komiss77.utils;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.apache.commons.lang.WordUtils;
import org.bukkit.World;

public class StringUtil {

    private static final String PATTERN_ENG = "[A-Za-z_]";
    private static final String PATTERN_ENG_NUM = "\\w"; //[A-Za-z0-9_]";
    private static final String PATTERN_ENG_RUS = "[A-Za-zА-Яа-я_]";
    private static final String PATTERN_ENG_NUM_RUS = "[A-Za-z0-9А-Яа-я_]";
    public static final char CHAR_0 = '¦';
    public static final String SPLIT_0 = "»" + CHAR_0 + "«";
    public static final char CHAR_1 = '↕';
    public static final String SPLIT_1 = "" + CHAR_1;
    public static final char CHAR_2 = '÷';
    public static final String SPLIT_2 = "" + CHAR_2;
    public static final char CHAR_NA = '○';
    public static final String NA = String.valueOf(CHAR_NA);

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
        for (int i = ss.length - 1; i != 0; i--) {
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

    private static final Set<String> NA_SET = Set.of(NA, "na", "null", "NULL", "n/a", "N/A", "none", "NONE");
    public static boolean isNA(final String str) {
        return NA_SET.contains(str);
    }

    private static final Set<String> LOBBY_SET = Set.of("lobby", "world", "arenas", "hub", "main");
    public static boolean isLobby(final World w) {
        return LOBBY_SET.contains(w.getName());
    }

  public static String sha256(final String s) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      final byte[] hash = digest.digest(s.getBytes("UTF-8"));
      final StringBuilder hexString = new StringBuilder();
      for (int i = 0; i < hash.length; i++) {
        final String hex = Integer.toHexString(0xff & hash[i]);
        if (hex.length() == 1)
          hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    //try {
    //    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    //    byte[] hash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
    //    return new String(hash);//Base64.getEncoder().encodeToString(hash);
    //} catch (NoSuchAlgorithmException ex) {
    //    return "";
    //}
  }
    /*public static String multiReplace(final String str, final Map<String, String> places) {
        final int len = str.length(), ksl = len >> 8;
        final IntHashMap<List<String>> keys = new IntHashMap<>();
        for (final String k : places.keySet()) {
            if (k.isEmpty()) continue;
            final char kc = k.charAt(0);
            final List<String> kls = keys.get(kc);
            if (kls == null) {
                final ArrayList<String> nks = new ArrayList<>(ksl);
                keys.put(kc, nks);
            } else kls.add(k);
        }

        final StringBuilder sb = new StringBuilder(len);
        final char[] strArr = str.toCharArray();
        for (int i = 0; i != len; i++) {
            final List<String> kls = keys.get(strArr[i]);
            if (kls == null) {
                sb.append(strArr[i]);
                continue;
            }

            eq : for (final String k : kls) {
                final int kl = k.length() - 1;
                if (i + kl >= len) continue;
                for (int j = 1; j != kl; j++) {
                    if (k.charAt(j) != strArr[i + j]) continue eq;
                }

                final String sub = places.get(k);
                if (sub == null) continue;
                sb.append(sub);
                i += kl + 1;
                break;
            }
        }
        return sb.toString();
    }*/
}
