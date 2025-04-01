package ru.komiss77.modules.player.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Timer;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.TCUtil;


public class Mission {

    int id = -1;
    ItemType mat = ItemType.SPYGLASS;
    protected String nameColor = "";
    protected String name = "Новая миссия";
    protected Component displayName;
    int level = 0;
    int reputation = -77;
    CaseInsensitiveMap<Integer> request = new CaseInsensitiveMap<>(); //макс 8 требований!!
    int reward = 5;
    int canComplete = 10;
    int activeFrom = Timer.secTime() + 60 * 60; //через час
    int validTo = Timer.secTime() + 25 * 60 * 60; //через сутки и час
    int doing = 0;
    boolean changed = false;


    public int getRecordID(final String name) {
        return name.hashCode() ^ id;
    }

    public Component displayName() {
        if (displayName == null) {
            displayName = TCUtil.form(nameColor + "§o" + name);
        }
        return displayName;//TCUtils.(name, nameColor).decoration(TextDecoration.ITALIC, false);//TCUtils.toChat(nameColor)+name;
    }


    protected static List<Component> getRequest(final Player p, final Mission mi) {
        Stat stat;
        final List<Component> lore = new ArrayList<>();
        //lore.add("§7Требования:");
        lore.add(Component.text("§7Требования : " + (mi.request.isEmpty() ? "§cне указаны!" : "§b" + mi.request.size())));
        for (final Map.Entry<String, Integer> e : mi.request.entrySet()) {
            stat = Stat.fromName(e.getKey());
            if (stat == null) {
                if (MissionManager.customStatsDisplayNames.containsKey(e.getKey())) {
                    lore.add(Component.text(MissionManager.customStatsDisplayNames.get(e.getKey())
                        + (MissionManager.customStatsShowAmmount.get(e.getKey()) ? " §7: §d" + e.getValue() : ""))
                    );
                } else {
                    lore.add(Component.text("§b" + e.getKey() + " §7: §d" + e.getValue()));
                }
            } else {
                lore.add(Component.text(Lang.t(p, stat.game.displayName) + "§7, " + Lang.t(p, stat.desc) + " §d" + e.getValue()));
            }
        }
        return lore;
    }

    protected static String getRequestString(final Mission mi) {
        StringBuilder request = new StringBuilder();
        for (final Map.Entry<String, Integer> e : mi.request.entrySet()) {
            request.append("∫").append(e.getKey()).append(":").append(e.getValue());
        }
        return request.toString().replaceFirst("∫", "");
    }

}
