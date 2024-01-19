package ru.komiss77.utils;

import java.util.HashMap;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import ru.komiss77.objects.CaseInsensitiveMap;

public class CustomTextColor implements TextColor {

    public static final CaseInsensitiveMap<CustomTextColor> VALUES = new CaseInsensitiveMap<>();
    public static final HashMap<Integer, CustomTextColor> intClr = new HashMap<>();

    public static final CustomTextColor AMBER = new CustomTextColor("amber", 0xCC8822);//Янтарный
    public static final CustomTextColor APPLE = new CustomTextColor("apple", 0x88BB44);//Салатовый
    public static final CustomTextColor BEIGE = new CustomTextColor("beige", 0xDDCCAA);//Бежевый
    public static final CustomTextColor CARDINAL = new CustomTextColor("cardinal", 0xBB2244);//Кардинный
    public static final CustomTextColor INDIGO = new CustomTextColor("indigo", 0xAAAADD);//Сиреневый
    public static final CustomTextColor OLIVE = new CustomTextColor("olive", 0xBBDDAA);//Оливковый
    public static final CustomTextColor ORCHID = new CustomTextColor("orchid", 0xDDAABB);//Малиновый
    public static final CustomTextColor SKYBLUE = new CustomTextColor("skyblue", 0xAADDDD);//Небесный
    public static final CustomTextColor STALE = new CustomTextColor("stale", 0x446666);//Черствый
    public static final CustomTextColor MITHRIL = new CustomTextColor("mithril", 0xB0C0C0);//Мифриловый

    private final String name;
    private final int val;
    private final HSVLike hsv;

    private CustomTextColor(final String name, final int val) {
        this.name = name;
        this.val = val;
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

    public static void registerColor(final String name, final int val) {
        final CustomTextColor ctc = new CustomTextColor(name, val);
        VALUES.put(name, ctc);
    }

    public static CustomTextColor[] values() {
        return VALUES.values().toArray(new CustomTextColor[0]);
    }
}
