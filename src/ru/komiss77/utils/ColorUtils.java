package ru.komiss77.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;





public class ColorUtils {
    
    
   public static Color getColor(String s) {
        Color color = Color.BLACK;

        if (s.equalsIgnoreCase("BLACK")) {
            return color;
        } else {
            if (s.equalsIgnoreCase("AQUA")) {
                color = Color.AQUA;
            } else if (s.equalsIgnoreCase("BLUE")) {
                color = Color.BLUE;
            } else if (s.equalsIgnoreCase("FUCHSIA")) {
                color = Color.FUCHSIA;
            } else if (s.equalsIgnoreCase("GRAY")) {
                color = Color.GRAY;
            } else if (s.equalsIgnoreCase("GREEN")) {
                color = Color.GREEN;
            } else if (s.equalsIgnoreCase("LIME")) {
                color = Color.LIME;
            } else if (s.equalsIgnoreCase("MAROON")) {
                color = Color.MAROON;
            } else if (s.equalsIgnoreCase("NAVY")) {
                color = Color.NAVY;
            } else if (s.equalsIgnoreCase("OLIVE")) {
                color = Color.OLIVE;
            } else if (s.equalsIgnoreCase("ORANGE")) {
                color = Color.ORANGE;
            } else if (s.equalsIgnoreCase("PURPLE")) {
                color = Color.PURPLE;
            } else if (s.equalsIgnoreCase("RED")) {
                color = Color.RED;
            } else if (s.equalsIgnoreCase("SILVER")) {
                color = Color.SILVER;
            } else if (s.equalsIgnoreCase("TEAL")) {
                color = Color.TEAL;
            } else if (s.equalsIgnoreCase("WHITE")) {
                color = Color.WHITE;
            } else if (s.equalsIgnoreCase("YELLOW")) {
                color = Color.YELLOW;
            }

            return color;
        }
    }

   
   
       public static ItemStack changeColor (ItemStack source, short new_color) {
        DyeColor dc = DyeColor.WHITE;
        
        switch (new_color) {
            case 0: dc = DyeColor.BLACK; break;
            case 1: dc = DyeColor.BLUE; break;
            case 2: dc = DyeColor.GREEN; break;
            case 3: dc = DyeColor.ORANGE; break;
            case 4: dc = DyeColor.RED; break;
            case 5: dc = DyeColor.PURPLE; break;
            case 6: dc = DyeColor.BROWN; break;
            case 7: dc = DyeColor.LIGHT_GRAY; break;
            case 8: dc = DyeColor.GRAY; break;
            case 9: dc = DyeColor.LIGHT_BLUE; break;
            case 10: dc = DyeColor.LIME; break;
            case 11: dc = DyeColor.CYAN; break;
            case 12: dc = DyeColor.PINK; break;
            case 13: dc = DyeColor.MAGENTA; break;
            case 14: dc = DyeColor.YELLOW; break;
            case 15: dc = DyeColor.WHITE; break;
        }
        
        return changeColor(source, dc);
    }
       
    
    public static ItemStack changeColor (ItemStack source, final DyeColor new_color) {
        try {
            String base_mat_name = source.getType().toString();
            base_mat_name = ApiOstrov.getItemNameBaseWithOutColor(base_mat_name);
            if(base_mat_name.isEmpty()) return source;
            source.setType( Material.matchMaterial(new_color.toString()+"_"+base_mat_name) );
        } catch (Exception ex) { 
            Ostrov.log_err("ItemUtils.changeColor - "+source.toString()+", "+new_color.toString()+" : "+ ex.getMessage());
        }
        return source;
    }
    
    public static Material changeColor (Material source, final DyeColor new_color) {
        try {
            String base_mat_name = source.toString();
            base_mat_name = ApiOstrov.getItemNameBaseWithOutColor(base_mat_name);
            if(base_mat_name.isEmpty()) return source;
            return Material.matchMaterial(new_color.toString()+"_"+base_mat_name);
        } catch (Exception ex) { 
            Ostrov.log_err("ItemUtils.changeColor - "+source.toString()+", "+new_color.toString()+" : "+ ex.getMessage());
        }
        return source;
    }
    
    
    
    public static boolean canChangeColor (Material check) {
        final String type = check.toString();
        return type.startsWith("BLACK_") || type.startsWith("BLUE_") || type.startsWith("BROWN_") || type.startsWith("CYAN_") || type.startsWith("GRAY_")
                 || type.startsWith("GREEN_") || type.startsWith("LIGHT_BLUE_") || type.startsWith("LIGHT_GRAY_") || type.startsWith("LIME_") || type.startsWith("MAGENTA_")
                 || type.startsWith("ORANGE_") || type.startsWith("PINK_") || type.startsWith("PURPLE_") || type.startsWith("RED_") || type.startsWith("WHITE_")
                 || type.startsWith("YELLOW_");
    }

    public static String getItemNameBaseWithOutColor (String source_type) {
        if (source_type.startsWith("BLACK_")) return source_type.replaceFirst("BLACK_", "");
        else if (source_type.startsWith("BLUE_")) return source_type.replaceFirst("BLUE_", "");
        else if (source_type.startsWith("BROWN_")) return source_type.replaceFirst("BROWN_", "");
        else if (source_type.startsWith("CYAN_")) return source_type.replaceFirst("CYAN_", "");
        else if (source_type.startsWith("GRAY_")) return source_type.replaceFirst("GRAY_", "");
        else if (source_type.startsWith("GREEN_")) return source_type.replaceFirst("GREEN_", "");
        else if (source_type.startsWith("LIGHT_BLUE_")) return source_type.replaceFirst("LIGHT_BLUE_", "");
        else if (source_type.startsWith("LIGHT_GRAY_")) return source_type.replaceFirst("LIGHT_GRAY_", "");
        else if (source_type.startsWith("LIME_")) return source_type.replaceFirst("LIME_", "");
        else if (source_type.startsWith("MAGENTA_")) return source_type.replaceFirst("MAGENTA_", "");
        else if (source_type.startsWith("ORANGE_")) return source_type.replaceFirst("ORANGE_", "");
        else if (source_type.startsWith("PINK_")) return source_type.replaceFirst("PINK_", "");
        else if (source_type.startsWith("PURPLE_")) return source_type.replaceFirst("PURPLE_", "");
        else if (source_type.startsWith("RED_")) return source_type.replaceFirst("RED_", "");
        else if (source_type.startsWith("WHITE_")) return source_type.replaceFirst("WHITE_", "");
        else if (source_type.startsWith("YELLOW_")) return source_type.replaceFirst("YELLOW_", "");
        else return source_type;
    }
    
    public static ChatColor randomColor() {
        return ChatColorfromInt(ApiOstrov.randInt(0, 15));
    }
    public static ChatColor ChatColorfromInt(final int i) {
        switch (i) {
            case 0: return ChatColor.AQUA;
            case 1: return ChatColor.BLACK;
            case 2: return ChatColor.BLUE;
            case 3: return ChatColor.DARK_AQUA;
            case 4: return ChatColor.DARK_BLUE;
            case 5: return ChatColor.DARK_GRAY;
            case 6: return ChatColor.DARK_GREEN;
            case 7: return ChatColor.DARK_PURPLE;
            case 8: return ChatColor.DARK_RED;
            case 9: return ChatColor.GOLD;
            case 10: return ChatColor.GRAY;
            case 11: return ChatColor.GREEN;
            case 12: return ChatColor.LIGHT_PURPLE;
            case 13: return ChatColor.RED;
            case 14: return ChatColor.YELLOW;
            default: return ChatColor.WHITE;
        }
    }
    
    public static ChatColor chatColorFromString(String s) {
        if (s.startsWith("§0")) return ChatColor.BLACK;
        else if (s.startsWith("§1")) return ChatColor.DARK_BLUE;
        else if (s.startsWith("§2")) return ChatColor.DARK_GREEN;
        else if (s.startsWith("§3")) return ChatColor.DARK_AQUA;
        else if (s.startsWith("§4")) return ChatColor.DARK_RED;
        else if (s.startsWith("§5")) return ChatColor.DARK_PURPLE;
        else if (s.startsWith("§6")) return ChatColor.GOLD;
        else if (s.startsWith("§7")) return ChatColor.GRAY;
        else if (s.startsWith("§8")) return ChatColor.DARK_GRAY;
        else if (s.startsWith("§9")) return ChatColor.BLUE;
        else if (s.startsWith("§a")) return ChatColor.GREEN;
        else if (s.startsWith("§b")) return ChatColor.AQUA;
        else if (s.startsWith("§c")) return ChatColor.RED;
        else if (s.startsWith("§d")) return ChatColor.LIGHT_PURPLE;
        else if (s.startsWith("§e")) return ChatColor.YELLOW;
        return ChatColor.WHITE;
    }
    public static int colorFromString(String s) {
        if (s.startsWith("§0")) return 0;
        else if (s.startsWith("§1")) return 1;
        else if (s.startsWith("§2")) return 2;
        else if (s.startsWith("§3")) return 3;
        else if (s.startsWith("§4")) return 4;
        else if (s.startsWith("§5")) return 5;
        else if (s.startsWith("§6")) return 6;
        else if (s.startsWith("§7")) return 7;
        else if (s.startsWith("§8")) return 8;
        else if (s.startsWith("§9")) return 9;
        else if (s.startsWith("§a")) return 10;
        else if (s.startsWith("§b")) return 11;
        else if (s.startsWith("§c")) return 12;
        else if (s.startsWith("§d")) return 13;
        else if (s.startsWith("§e")) return 14;
        return 15;
    }
    
    public static int IntFromChatColor(final ChatColor color) {
        switch (color) {
            case AQUA: return 0;
            case BLACK: return 1;
            case BLUE: return 2;
            case DARK_AQUA: return 3;
            case DARK_BLUE: return 4;
            case DARK_GRAY: return 5;
            case DARK_GREEN: return 6;
            case DARK_PURPLE: return 7;
            case DARK_RED: return 8;
            case GOLD: return 9;
            case GRAY: return 10;
            case GREEN: return 11;
            case LIGHT_PURPLE: return 12;
            case RED: return 13;
            case YELLOW: return 14;
            default: return 15;
        }
    }

    public static DyeColor randomDyeColor() {
        switch (ApiOstrov.randInt(0, 16)) {
            case 0: return DyeColor.BLACK;
            case 1: return DyeColor.BLUE;
            case 2: return DyeColor.BROWN;
            case 3: return DyeColor.CYAN;
            case 4: return DyeColor.GRAY;
            case 5: return DyeColor.GREEN;
            case 6: return DyeColor.LIGHT_BLUE;
            case 7: return DyeColor.LIGHT_GRAY;
            case 8: return DyeColor.LIME;
            case 9: return DyeColor.MAGENTA;
            case 10: return DyeColor.ORANGE;
            case 11: return DyeColor.PINK;
            case 12: return DyeColor.PURPLE;
            case 13: return DyeColor.RED;
            case 14: return DyeColor.YELLOW;
            default: return DyeColor.WHITE;
        }
    }


    public static Color randomCol() {
        switch (ApiOstrov.randInt(0, 16)) {
            case 0: return Color.AQUA;
            case 1: return Color.BLACK;
            case 2: return Color.BLUE;
            case 3: return Color.FUCHSIA;
            case 4: return Color.GRAY;
            case 5: return Color.GREEN;
            case 6: return Color.LIME;
            case 7: return Color.MAROON;
            case 8: return Color.NAVY;
            case 9: return Color.OLIVE;
            case 10: return Color.ORANGE;
            case 11: return Color.PURPLE;
            case 12: return Color.RED;
            case 13: return Color.SILVER;
            case 14: return Color.TEAL;
            case 15: return Color.YELLOW;
            default: return Color.WHITE;
        }
    }

    

    public static String DyeToString ( final DyeColor dyecolor ) {
    
        
     switch (dyecolor) {
        case WHITE: return "§fБелый";    //+++бел
        case ORANGE: return "§6Оранжевый";   
        case PURPLE: return "§dПурпурный";   
        case LIGHT_BLUE: return "§9Голубой";   
        case YELLOW: return "§eЖелтый";   
        case LIME: return "§aЛаймовый";   
        case PINK: return "§cРозовый";   
        case GRAY: return "§8Пепельный";   
        case LIGHT_GRAY: return "§7Серый";   
        case CYAN: return "§3Аквамарин";   
        case MAGENTA: return "§dФиолетовый";   
        case BLUE: return "§1Синий";   
        case BROWN: return "§6Коричневый";   
        case GREEN: return "§2Зелёный";   
        case RED: return "§4Бардовый";   
        case BLACK: return "§0Чёрный";   
        }
         return "";
    } 

    
    
        public static ChatColor ChatColorfromDyeColor(final DyeColor dyecolor) {
            switch (dyecolor) {
               case WHITE: return ChatColor.WHITE;    //+++бел
               case ORANGE: return ChatColor.GOLD;   
               case PURPLE: return ChatColor.LIGHT_PURPLE;   
               case LIGHT_BLUE: return ChatColor.BLUE;   
               case YELLOW: return ChatColor.YELLOW;   
               case LIME: return ChatColor.GREEN;   
               case PINK: return ChatColor.RED;   
               case GRAY: return ChatColor.DARK_GRAY;   
               case LIGHT_GRAY: return ChatColor.GRAY;   
               case CYAN: return ChatColor.AQUA;   
               case MAGENTA: return ChatColor.DARK_PURPLE;   
               case BLUE: return ChatColor.DARK_BLUE;   
               case BROWN: return ChatColor.DARK_AQUA;   
               case GREEN: return ChatColor.DARK_GREEN;   
               case RED: return ChatColor.DARK_RED;   
               case BLACK: return ChatColor.BLACK;   
            }
            return ChatColor.RESET;
        }
        
        public static DyeColor DyeColorfromChatColor(final ChatColor chatcolor) {
            switch (chatcolor) {
               case WHITE: return DyeColor.WHITE;    //+++бел
               case GOLD: return DyeColor.ORANGE;   
               case LIGHT_PURPLE: return DyeColor.PURPLE;   
               case BLUE: return DyeColor.LIGHT_BLUE;   
               case YELLOW: return DyeColor.YELLOW;   
               case GREEN: return DyeColor.LIME;   
               case RED: return DyeColor.PINK;   
               case DARK_GRAY: return DyeColor.GRAY;   
               case GRAY: return DyeColor.LIGHT_GRAY;   
               case AQUA: return DyeColor.CYAN;   
               case DARK_PURPLE: return DyeColor.MAGENTA;   
               case DARK_BLUE: return DyeColor.BLUE;   
               case DARK_AQUA: return DyeColor.BROWN;   
               case DARK_GREEN: return DyeColor.GREEN;   
               case DARK_RED: return DyeColor.RED;   
               case BLACK: return DyeColor.BLACK;   
            }
            return DyeColor.WHITE;
        }
        
        
        
}
