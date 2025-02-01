package ru.komiss77.utils;

import java.util.HashMap;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import ru.komiss77.objects.CaseInsensitiveMap;


public class CustomTextColor implements TextColor {

    public static final CaseInsensitiveMap<CustomTextColor> VALUES = new CaseInsensitiveMap<>();
    public static final HashMap<Integer, CustomTextColor> intClr = new HashMap<>();

    public static final CustomTextColor AMBER = new CustomTextColor("amber", 0xBB6622, "gold");//Янтарный
    public static final CustomTextColor APPLE = new CustomTextColor("apple", 0x88BB22, "green");//Салатовый
    public static final CustomTextColor BEIGE = new CustomTextColor("beige", 0xDDCCAA, "yellow");//Бежевый
    public static final CustomTextColor CARDINAL = new CustomTextColor("cardinal", 0xAA2266, "red");//Кардинный
    public static final CustomTextColor INDIGO = new CustomTextColor("indigo", 0x8866DD, "blue");//Сиреневый
    public static final CustomTextColor OLIVE = new CustomTextColor("olive", 0xAAAA22, "green");//Оливковый
    public static final CustomTextColor PINK = new CustomTextColor("pink", 0xEE88BB, "light_purple");//Малиновый
    public static final CustomTextColor SKY = new CustomTextColor("sky", 0x99DDDD, "aqua");//Небесный
    public static final CustomTextColor STALE = new CustomTextColor("stale", 0x557766, "dark_green");//Черствый
    public static final CustomTextColor MITHRIL = new CustomTextColor("mithril", 0xAABBDD, "gray");//Мифриловый

    private final String name;
    private final int val;
    private final HSVLike hsv;
    private final String like;

    private CustomTextColor(final String name, final int val, final String like) {
        this.name = name;
        this.val = val;
        this.like = like;
        this.hsv = HSVLike.fromRGB(red(), green(), blue());
        //Bukkit.getConsoleSender().sendMessage("reg-" + red() + ", " + green() + ", " + blue());
        VALUES.put(name, CustomTextColor.this);
        intClr.put(val, CustomTextColor.this);
    }

    @Override
    public int value() {
        return val;
    }

    @Override
    public HSVLike asHSV() {
        return hsv;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String like() {
        return this.like;
    }

    public static void registerColor(final String name, final int val, final String like) {
        final CustomTextColor ctc = new CustomTextColor(name, val, like);
        VALUES.put(name, ctc);
    }

    public static CustomTextColor[] values() {
        return VALUES.values().toArray(new CustomTextColor[0]);
    }
}
