package ru.komiss77.utils;




public class TransLiter {
    
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
      //  StringBuilder sb = new StringBuilder(withCirilyc.length()*3);
      //  for(char ch: withCirilyc.toCharArray()){
      //          sb.append(cyr2lat(ch));
      //  }
     //   return sb.toString().toLowerCase();
    }

    
  /*  public static String cyr2lat(char ch){
            switch (ch){
                    case 'А': return ".A";
                    case 'Б': return ".B";
                    case 'В': return ".V";
                    case 'Г': return ".G";
                    case 'Д': return ".D";
                    case 'Е': return ".E";
                    case 'Ё': return ".JE";
                    case 'Ж': return ".ZH";
                    case 'З': return ".Z";
                    case 'И': return ".I";
                    case 'Й': return ".Y";
                    case 'К': return ".K";
                    case 'Л': return ".L";
                    case 'М': return ".M";
                    case 'Н': return ".N";
                    case 'О': return ".O";
                    case 'П': return ".P";
                    case 'Р': return ".R";
                    case 'С': return ".S";
                    case 'Т': return ".T";
                    case 'У': return ".U";
                    case 'Ф': return ".F";
                    case 'Х': return ".KH";
                    case 'Ц': return ".C";
                    case 'Ч': return ".CH";
                    case 'Ш': return ".SH";
                    case 'Щ': return ".SE";
                    case 'Ъ': return ".HH";
                    case 'Ы': return ".IH";
                    case 'Ь': return ".JH";
                    case 'Э': return ".EH";
                    case 'Ю': return ".JU";
                    case 'Я': return ".JA";
                    default: return String.valueOf(ch);
            }
    }*/

    
    
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
