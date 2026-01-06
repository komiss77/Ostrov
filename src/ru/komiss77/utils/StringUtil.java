package ru.komiss77.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.World;
import ru.komiss77.Ostrov;

public class StringUtil {

    private static final String PATTERN_ENG = "[A-Za-z_]";
    private static final String PATTERN_ENG_NUM = "\\w"; //[A-Za-z0-9_]";
    private static final String PATTERN_ENG_RUS = "[A-Za-zА-Яа-я_]";
    private static final String PATTERN_ENG_NUM_RUS = "[A-Za-z0-9А-Яа-я_]";    @Deprecated
    public static final char CHAR_0 = '¦';
    @Deprecated
    public static final String SPLIT_0 = "»" + CHAR_0 + "«";
    @Deprecated
    public static final char CHAR_1 = '↕';
    @Deprecated
    public static final String SPLIT_1 = "" + CHAR_1;
    @Deprecated
    public static final char CHAR_2 = '÷';
    @Deprecated
    public static final String SPLIT_2 = "" + CHAR_2;

    public static final char CHAR_NA = '○';
    public static final String NA = String.valueOf(CHAR_NA);

    public static final String UPPERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWERS = "abcdefghijklmnopqrstuvwxyz";
    public static final String NUMBERS = "0123456789";

    public enum Split {
        LARGE("<!>", "‹¦›", "«︙»", "»¦«"),
        MEDIUM("=", '∬', '‼', '᠃', '⧥', '↕'),
        SMALL(":", '÷', '∫', '¡', '±', '।');

        private final int len;
        private final String reject;
        private final String[] chars;
        private final Pattern pat;

        Split(final String reject, final String... chars) {
            this.len = chars[0].length();
            this.chars = chars;
            this.reject = reject;
            final StringBuilder sb = new StringBuilder();
            for (final String ch : chars) {
                if (ch.length() != len) continue;
                sb.append("|").append(ch);
            }
            pat = Pattern.compile(sb.substring(1));
        }

        Split(final String reject, final char... chars) {
            this.reject = reject;
            this.len = 1;
            this.chars = new String[chars.length];
            for (int i = 0; i != chars.length; i++) this.chars[i] = String.valueOf(chars[i]);
            pat = Pattern.compile("[" + new String(chars) + "]");
        }

        public String[] split(final String str) {
            return split(str, false);
        }

        public String[] split(final String str, final boolean once) {
            if (once) {
                final int ix = index(str);
                return ix < 0 ? new String[]{str}
                    : new String[]{str.substring(0, ix), str.substring(ix + len)};
            }
            return pat.split(str);
        }

        public int index(final String str) {
            if (len == 1) {
                for (final String ch : chars) {
                    final int ix = str.indexOf(ch.charAt(0));
                    if (ix < 0) continue; return ix;
                }
                return -1;
            }
            for (final String ch : chars) {
                final int ix = str.indexOf(ch);
                if (ix < 0) continue; return ix;
            }
            return -1;
        }

        public String join(final String s1, final String s2) {
            final String spl = get();
            return s1.replace(spl, reject) + spl
                + s2.replace(spl, reject);
        }

        public String join(final String... seq) {
            if (seq.length == 0) return "";
            final StringBuilder sb = new StringBuilder();
            final String spl = get();
            for (final String cs : seq) sb.append(spl).append(cs.replace(spl, reject));
            return sb.substring(spl.length());
        }

        public String join(final Collection<String> seq) {
            if (seq.size() == 0) return "";
            final StringBuilder sb = new StringBuilder();
            final String spl = get();
            for (final String cs : seq) sb.append(spl).append(cs.replace(spl, reject));
            return sb.substring(spl.length());
        }

        public int len() {return len;}
        public String get() {return chars[0];}
    }

    public static void suggester(final SuggestionsBuilder sb, final String starting, final Collection<String> variants) {
        variants.stream().filter(name -> name.regionMatches(true, 0, starting, 0, starting.length()))
            .limit(10).forEach(s -> sb.suggest(s));
    }

    public static String[] wrap(final String msg, final int length, final String newLine) {
        if (msg.length() < 2) return new String[]{msg};
        final char split = '\n';
        final String line = split + newLine;
        return crap(msg, length, line, " ").substring(1).split(line);
    }

    public static String crap(final String str, int length, String newLine, String split) {
        if (str == null) return null;
        if (newLine == null) newLine = System.lineSeparator();
        if (length < 1) length = 1;
        if (StringUtils.isBlank(split)) split = " ";
        final Pattern patternToWrapOn = Pattern.compile(split);
        final int inputLineLength = str.length();
        int offset = 0;
        final StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

        Matcher matcher;
        while (offset < inputLineLength) {
            int spaceToWrapAt = -1;
            matcher = patternToWrapOn.matcher(str.substring(offset,
                Math.min((int) Math.min(Integer.MAX_VALUE, offset + length + 1L), inputLineLength)));
            if (matcher.find()) {
                if (matcher.start() == 0) {
                    offset += matcher.end();
                    continue;
                }
                spaceToWrapAt = matcher.start() + offset;
            }

            // only last line without leading spaces is left
            if (inputLineLength - offset <= length) break;
            while (matcher.find()) spaceToWrapAt = matcher.start() + offset;
            if (spaceToWrapAt >= offset) {
                // normal case
                wrappedLine.append(str, offset, spaceToWrapAt);
                wrappedLine.append(newLine);
                offset = spaceToWrapAt + 1;
                continue;
            }
            // really long word or URL
            // do not wrap really long word, just extend beyond limit
            matcher = patternToWrapOn.matcher(str.substring(offset + length));
            if (matcher.find()) spaceToWrapAt = matcher.start() + offset + length;
            if (spaceToWrapAt < 0) {
                wrappedLine.append(str, offset, str.length());
                offset = inputLineLength;
                continue;
            }
            wrappedLine.append(str, offset, spaceToWrapAt);
            wrappedLine.append(newLine);
            offset = spaceToWrapAt + 1;
        }

        // Whatever is left in line is short enough to just pass through
        wrappedLine.append(str, offset, str.length());
        return wrappedLine.toString();
    }

    public static char rndChar(final String str) {
        return str == null || str.isEmpty() ? ' '
            : str.charAt(Ostrov.random.nextInt(str.length()));
    }

    public static boolean checkString(String message, final boolean allowNumbers, final boolean allowRussian) {
        return checkString(message, false, allowNumbers, allowRussian);
    }

    public static boolean checkString(String message, final boolean allowSpace, final boolean allowNumbers, final boolean allowRussian) {
        if (allowNumbers && allowRussian) message = message.replaceAll(PATTERN_ENG_NUM_RUS, "");
        else if (allowNumbers) message = message.replaceAll(PATTERN_ENG_NUM, "");
        else if (allowRussian) message = message.replaceAll(PATTERN_ENG_RUS, "");
        else message = message.replaceAll(PATTERN_ENG, "");
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

  public static Collection fromString(final String array, String splitter) {
    if (array == null || array.isBlank()) return Collections.EMPTY_LIST;
    if (splitter == null || splitter.isBlank()) splitter = ",";
    final String[] split = array.split(splitter);
    return Arrays.stream(split).collect(Collectors.toList());
  }

    public static String nrmlzStr(final String s) {
        final char[] ss = s.toLowerCase().toCharArray();
        ss[0] = Character.toUpperCase(ss[0]);
        for (int i = ss.length - 1; i != 0; i--)
            switch (ss[i]) {
                case '_':
                    ss[i] = ' ';
                case ' ':
                    ss[i + 1] = Character.toUpperCase(ss[i + 1]);
                    break;
                default:
                    break;
            }
        return String.valueOf(ss);
    }


    public static String getPercentBar(final int max, final int current, final boolean withPercent) {
        if (current < 0 || current > max) return "§8||||||||||||||||||||||||| ";
        final double percent = (double) current / max * 100;
        int p10 = (int) (percent * 10);
        final double percent1d = ((double) p10 / 10); //чтобы не показывало 100
        int pos = p10 / 37;
        if (pos < 2) pos = 2;
        else if (pos > 26) pos = 26;
        if (withPercent)
            return new StringBuilder("§a||||||||||||||||||||||||| ").insert(pos, "§8").append("§f").append(percent1d).append("%").toString();
        else return new StringBuilder("§a||||||||||||||||||||||||| ").insert(pos, "§8").toString();
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
            final byte[] hash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
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
