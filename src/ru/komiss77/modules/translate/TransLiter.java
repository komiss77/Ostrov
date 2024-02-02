package ru.komiss77.modules.translate;

import java.util.HashMap;
import java.util.Map;

//не перемещать, юзает прокси!!
//для перекодировки названия мира с кирилицы
public class TransLiter {
    
    public static final Map<Character, String> LETTERS;
    static {
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
        LETTERS.put('Ъ', "JH");
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
        LETTERS.put('ъ', "jh");
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
        for (char ch: withCirilyc.toCharArray()) {
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


    
    
}
