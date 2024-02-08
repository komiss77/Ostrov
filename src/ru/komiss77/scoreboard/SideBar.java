package ru.komiss77.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import ru.komiss77.Ostrov;
import ru.komiss77.objects.Duo;
import ru.komiss77.utils.TCUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;


public class SideBar {

    public final String ext = " ";
    
    private final CustomScore board;
    private final String name;
    private final Objective obj;
    private final HashMap<Integer, Line> entries;
    private final HashMap<String, Line> lines;
    private final LinkedList<Duo<String, String>> toAdd;

    private int nextLine = 0;
    //private final HashMap <Integer, String> entries;
    
    public SideBar(final Player p, final CustomScore board, final String title) {
        toAdd = new LinkedList<>();
        entries = new HashMap<>();
        lines = new HashMap<>();
        name = p.getName();
        this.board = board;
        obj = board.getScoreboard().registerNewObjective("status", Criteria.DUMMY, TCUtils.format(title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.displayName(Component.empty());
    }
    
    @Deprecated
    public Player getPlayer() {
        return Bukkit.getPlayerExact(name);
    }
    
    public Objective getObjective() {
        return obj;
    }

    @Deprecated
    public SideBar setTitle(final String displayName) {
        return title(displayName);
    }
    
    public SideBar title(final String name) {
        obj.displayName(TCUtils.format(name));
        return this;
    }
    
    public SideBar reset() {
        entries.values().forEach(line -> {
            line.unregister();
            board.getScoreboard().resetScores(line.getScore().getEntry());
        });
        entries.clear();

        lines.values().forEach(line -> {
            line.unregister();
            board.getScoreboard().resetScores(line.getScore().getEntry());
        });
        lines.clear();
        toAdd.clear();
        nextLine = 0;
        return this;
    }

    @Deprecated
    public void updateLine(int line, String value) {
        if (line<0 || line>20) {
            line=1;
            value = "§cline от 0 до 20";
        }

        final Line l = entries.get(line);
        if (l==null){
            entries.put(line, new Line(board, value, line));
        } else l.update(value);
    }

    public SideBar add(final String name) {
        return add(name, null);
    }

    public SideBar add(final String name, final @Nullable String value) {
        toAdd.addFirst(new Duo<>(name, value));
        return this;
    }

    public SideBar update(final String name, final @Nullable String value) {
        final Line l = lines.get(name);
        if (l == null) {
            Ostrov.log_warn("Tried updating null line " + name);
            return this;
        }
        else l.update(value);
        return this;
    }

    public SideBar build() {
        for (final Duo<String, String> pr : toAdd)
            putLine(pr.key, pr.val);
        return this;
    }

    private void putLine(final String name, final String value) {
        final Line l = lines.get(name);
        if (l==null) {
            final Line nl = value == null ? new Line(board, name)
                    : new Line(board, TCUtils.getColor(nextLine) + "§r", value);
            nl.getScore().setScore(nextLine++);
            lines.put(name, nl);
            return;
        }

        if (value == null) {
            putLine(name + ext, null);
        } else l.update(value);
    }
}
