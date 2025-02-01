package ru.komiss77.modules.translate;

import java.util.HashMap;
import java.util.Map;
import ru.komiss77.notes.Slow;

//не перемещать, юзает прокси!!
//для перекодировки названия мира с кирилицы
public class TransLiter {

    public static final Map<Character, String> LETTERS;


    private static final int CRL_MAX = 0x44F; // Unicode offset for Cyrillic characters
    private static final int CRL_OFFSET = 0x400; // Unicode offset for Cyrillic characters
    private static final int LTN_RANGE = 128; // Latin ASCII range
    private static final char[] MAPPING = new char[LTN_RANGE + CRL_MAX - CRL_OFFSET + 1];

    static {
        // Initialize array with identity mapping
        for (int i = 0; i != LTN_RANGE; i++) MAPPING[i] = (char) i;
        for (int i = LTN_RANGE; i != MAPPING.length; i++) {
            MAPPING[i] = (char) (i + CRL_OFFSET - LTN_RANGE);
        }

        // Mapping based on a standard Russian keyboard layout
        char[][] mappings = {
            {'q', 'й'}, {'w', 'ц'}, {'e', 'у'}, {'r', 'к'}, {'t', 'е'}, {'y', 'н'},
            {'u', 'г'}, {'i', 'ш'}, {'o', 'щ'}, {'p', 'з'}, {'[', 'х'}, {']', 'ъ'},
            {'a', 'ф'}, {'s', 'ы'}, {'d', 'в'}, {'f', 'а'}, {'g', 'п'}, {'h', 'р'},
            {'j', 'о'}, {'k', 'л'}, {'l', 'д'}, {';', 'ж'}, {'\'', 'э'},
            {'z', 'я'}, {'x', 'ч'}, {'c', 'с'}, {'v', 'м'}, {'b', 'и'}, {'n', 'т'},
            {'m', 'ь'}, {',', 'б'}, {'.', 'ю'},
            {'Q', 'Й'}, {'W', 'Ц'}, {'E', 'У'}, {'R', 'К'}, {'T', 'Е'}, {'Y', 'Н'},
            {'U', 'Г'}, {'I', 'Ш'}, {'O', 'Щ'}, {'P', 'З'}, {'{', 'Х'}, {'}', 'Ъ'},
            {'A', 'Ф'}, {'S', 'Ы'}, {'D', 'В'}, {'F', 'А'}, {'G', 'П'}, {'H', 'Р'},
            {'J', 'О'}, {'K', 'Л'}, {'L', 'Д'}, {':', 'Ж'}, {'"', 'Э'},
            {'Z', 'Я'}, {'X', 'Ч'}, {'C', 'С'}, {'V', 'М'}, {'B', 'И'}, {'N', 'Т'},
            {'M', 'Ь'}, {'<', 'Б'}, {'>', 'Ю'}
        };

        for (final char[] pair : mappings) {
            MAPPING[pair[0]] = pair[1];
            final int cyrillicIndex = pair[1] - CRL_OFFSET + LTN_RANGE;
            if (cyrillicIndex < 0 || cyrillicIndex >= MAPPING.length) continue;
            MAPPING[cyrillicIndex] = pair[0]; // Safe mapping for Cyrillic characters
        }

        LETTERS = new HashMap<>();
        LETTERS.put('А', "A");
        LETTERS.put('Б', "B");
        LETTERS.put('В', "V");
        LETTERS.put('Г', "G");
        LETTERS.put('Д', "D");
        LETTERS.put('Е', "E");
        LETTERS.put('Ё', "YO");
        LETTERS.put('Ж', "ZH");
        LETTERS.put('З', "Z");
        LETTERS.put('И', "I");
        LETTERS.put('Й', "Y");
        LETTERS.put('К', "K");
        LETTERS.put('Л', "L");
        LETTERS.put('М', "M");
        LETTERS.put('Н', "N");
        LETTERS.put('О', "O");
        LETTERS.put('П', "P");
        LETTERS.put('Р', "R");
        LETTERS.put('С', "S");
        LETTERS.put('Т', "T");
        LETTERS.put('У', "U");
        LETTERS.put('Ф', "F");
        LETTERS.put('Х', "H");
        LETTERS.put('Ц', "C");
        LETTERS.put('Ч', "CH");
        LETTERS.put('Ш', "SH");
        LETTERS.put('Щ', "SE");
        LETTERS.put('Ъ', "HH");
        LETTERS.put('Ы', "IH");
        LETTERS.put('Ь', "JH");
        LETTERS.put('Э', "EH");
        LETTERS.put('Ю', "YU");
        LETTERS.put('Я', "YA");
        LETTERS.put('а', "a");
        LETTERS.put('б', "b");
        LETTERS.put('в', "v");
        LETTERS.put('г', "g");
        LETTERS.put('д', "d");
        LETTERS.put('е', "e");
        LETTERS.put('ё', "yo");
        LETTERS.put('ж', "zh");
        LETTERS.put('з', "z");
        LETTERS.put('и', "i");
        LETTERS.put('й', "y");
        LETTERS.put('к', "k");
        LETTERS.put('л', "l");
        LETTERS.put('м', "m");
        LETTERS.put('н', "n");
        LETTERS.put('о', "o");
        LETTERS.put('п', "p");
        LETTERS.put('р', "r");
        LETTERS.put('с', "s");
        LETTERS.put('т', "t");
        LETTERS.put('у', "u");
        LETTERS.put('ф', "f");
        LETTERS.put('х', "h");
        LETTERS.put('ц', "c");
        LETTERS.put('ч', "ch");
        LETTERS.put('ш', "sh");
        LETTERS.put('щ', "se");
        LETTERS.put('ъ', "hh");
        LETTERS.put('ы', "ih");
        LETTERS.put('ь', "jh");
        LETTERS.put('э', "eh");
        LETTERS.put('ю', "yu");
        LETTERS.put('я', "ya");
    }


    public static String cyr2lat(String withCirilyc) {
        withCirilyc = withCirilyc.toUpperCase();
        withCirilyc = withCirilyc
            .replaceAll("А", ".A")
            .replaceAll("Б", ".B")
            .replaceAll("В", ".V")
            .replaceAll("Г", ".G")
            .replaceAll("Д", ".D")
            .replaceAll("Е", ".E")
            .replaceAll("Ё", ".JE")
            .replaceAll("Ж", ".ZH")
            .replaceAll("З", ".Z")
            .replaceAll("И", ".I")
            .replaceAll("Й", ".Y")
            .replaceAll("К", ".K")
            .replaceAll("Л", ".L")
            .replaceAll("М", ".M")
            .replaceAll("Н", ".N")
            .replaceAll("О", ".O")
            .replaceAll("П", ".P")
            .replaceAll("Р", ".R")
            .replaceAll("С", ".S")
            .replaceAll("Т", ".T")
            .replaceAll("У", ".U")
            .replaceAll("Ф", ".F")
            .replaceAll("Х", ".KH")
            .replaceAll("Ц", ".C")
            .replaceAll("Ч", ".CH")
            .replaceAll("Ш", ".SH")
            .replaceAll("Щ", ".SE")
            .replaceAll("Ъ", ".HH")
            .replaceAll("Ы", ".IH")
            .replaceAll("Ь", ".JH")
            .replaceAll("Э", ".EH")
            .replaceAll("Ю", ".JU")
            .replaceAll("Я", ".JA")
        ;

        return withCirilyc.toLowerCase();
    }

    public static String cyr2latDirect(String withCirilyc) {
        StringBuilder sb = new StringBuilder();
        for (char ch : withCirilyc.toCharArray()) {
            if (LETTERS.containsKey(ch)) {
                sb.append(LETTERS.get(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String lat2cyr(String withTranslit) {
        withTranslit = withTranslit.toUpperCase();

        withTranslit = withTranslit
            .replaceAll(".A", "А")
            .replaceAll(".B", "Б")
            .replaceAll(".V", "В")
            .replaceAll(".G", "Г")
            .replaceAll(".D", "Д")
            .replaceAll(".E", "Е")
            .replaceAll(".JE", "Ё")
            .replaceAll(".ZH", "Ж")
            .replaceAll(".Z", "З")
            .replaceAll(".I", "И")
            .replaceAll(".Y", "Й")
            .replaceAll(".K", "К")
            .replaceAll(".L", "Л")
            .replaceAll(".M", "М")
            .replaceAll(".N", "Н")
            .replaceAll(".O", "О")
            .replaceAll(".P", "П")
            .replaceAll(".R", "Р")
            .replaceAll(".S", "С")
            .replaceAll(".T", "Т")
            .replaceAll(".U", "У")
            .replaceAll(".F", "Ф")
            .replaceAll(".KH", "Х")
            .replaceAll(".C", "Ц")
            .replaceAll(".CH", "Ч")
            .replaceAll(".SH", "Ш")
            .replaceAll(".SE", "Щ")
            .replaceAll(".HH", "Ъ")
            .replaceAll(".IH", "Ы")
            .replaceAll(".JH", "Ь")
            .replaceAll(".EH", "Э")
            .replaceAll(".JU", "Ю")
            .replaceAll(".JA", "Я")
        ;

        return withTranslit.toLowerCase();
    }

    public static char reLayOut(final char ch) {
        if (ch < LTN_RANGE) return MAPPING[ch];
        if (ch < CRL_OFFSET || ch > CRL_MAX) return ch;
        return MAPPING[ch - CRL_OFFSET + LTN_RANGE];
    }

    @Slow(priority = 1)
    public static String reLayOut(String input) {
        final StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) result.append(reLayOut(c));
        return result.toString();
    }
}
