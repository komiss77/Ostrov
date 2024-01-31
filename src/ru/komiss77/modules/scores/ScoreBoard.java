package ru.komiss77.modules.scores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import ru.komiss77.Ostrov;
import ru.komiss77.events.ScoreWorldRecordEvent;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.ThreadSafe;
import ru.komiss77.utils.TCUtils;

public class ScoreBoard {

    protected UUID disp;

    private final String score;
    private final WXYZ loc;
    private final int length;
    private final boolean isAsc;
    private final HashMap<String, Integer> stats = new HashMap<>();
    private final ArrayList<String> ranks = new ArrayList<>();

    public ScoreBoard(final String name, final WXYZ loc, final int length, final boolean isAsc) {
        this.score = name;
        this.loc = loc;
        this.length = length;
        this.isAsc = isAsc;
        final Location lc = loc.getCenterLoc();
        lc.getChunk().load();
        lc.getChunk().setForceLoaded(true);
        final TextDisplay td = loc.w.spawn(lc, TextDisplay.class);
        ScoreManager.scores.put(disp = modify(td), this);
        reanimate(td);
    }

    private UUID modify(final TextDisplay td) {
        td.setLineWidth(160);
        td.setPersistent(true);
        td.setShadowed(true);
        td.setBillboard(Billboard.VERTICAL);
        td.setAlignment(TextAlignment.CENTER);
        final Transformation tr = td.getTransformation();
        td.setTransformation(new Transformation(tr.getTranslation(),
                tr.getLeftRotation(), new Vector3f(1.6f, 1.6f, 1.6f), tr.getRightRotation()));
        td.getPersistentDataContainer().set(ScoreManager.key, PersistentDataType.BOOLEAN, true);
        return td.getUniqueId();
    }

    public void reanimate(@Nullable final Entity dis) {
        final TextDisplay td;
        if (dis == null || dis.getType() != EntityType.TEXT_DISPLAY) {
            td = loc.w.spawn(loc.getCenterLoc(), TextDisplay.class);
            ScoreManager.scores.remove(disp);
            ScoreManager.scores.put(disp = modify(td), this);
        } else {
            td = (TextDisplay) dis;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(score).append("\n\n");
        for (int i = 0; i < length; i++) {
            final String nm = ranks.size() > i ? ranks.get(i) : null;
            final String clr = switch (i) {
                case 0 ->
                    "§e";
                case 1 ->
                    "§м";
                case 2 ->
                    "§я";
                default ->
                    "§7";
            };
            if (nm == null) {
                sb.append("§8").append(i + 1).append(") ").append(clr).append("=-=-=-=§б: --\n");
            } else {
                sb.append("§8").append(i + 1).append(") ").append(clr).append(nm)
                    .append("§б: ").append(toDisplay(stats.get(nm))).append("\n");
            }
        }
        final String text = sb.toString();
        td.text(TCUtils.format(score));
        new BukkitRunnable() {
            Entity etd = td;
            int i = score.length() + 1;

            @Override
            public void run() {
                if (etd == null || !etd.isValid()) {
                    etd = loc.w.getEntity(disp);
                    return;
                }

                if ((i++) == text.length()) {
                    ((TextDisplay) etd).text(TCUtils.format(text));
                    cancel();
                    return;
                }

                td.text(TCUtils.format(text.substring(0, i)));
            }
        }.runTaskTimer(Ostrov.instance, 2, 2);
    }

    public boolean tryAdd(final String name, final int amt) {
        final Integer scr = stats.get(name);
        if (scr != null) {
            if (isAsc ? scr < amt : scr > amt) {
                stats.remove(name);
                ranks.remove(name);
            } else {
                return false;
            }
        }

        int plc = 1;
        for (final String nm : ranks) {
            final Integer sc = stats.get(nm);
            if (sc == null || isAsc ? sc >= amt : sc <= amt) {
                plc++;
            } else {
                break;
            }
        }

        if (plc > length) {
            return false;
        }
        stats.put(name, amt);
        if (plc > ranks.size()) {
            ranks.add(name);
        } else {
            ranks.add(plc - 1, name);
            if (ranks.size() > length) {
                stats.remove(ranks.remove(ranks.size() - 1));
            }
        }

        if (plc == 1) {
            new ScoreWorldRecordEvent(name, amt, this).callEvent();
        }
        reanimate(getEntity());
        return true;
    }

    @ThreadSafe
    public void populate(final HashMap<String, Integer> data) {
        stats.putAll(data);
        ranks.clear();
        String chs;
        int sc;
        final HashMap<String, Integer> cpd = new HashMap<>(stats);
        for (int i = 0; i < length; i++) {
            chs = "";
            sc = isAsc ? 0 : Integer.MAX_VALUE;
            for (final Entry<String, Integer> en : cpd.entrySet()) {
                if (isAsc ? en.getValue() > sc : en.getValue() < sc) {
                    sc = en.getValue();
                    chs = en.getKey();
                }
            }

            if (!chs.isEmpty()) {
                ranks.add(chs);
                cpd.remove(chs);
            }
        }

        for (final String nm : cpd.keySet()) {
            stats.remove(nm);
        }
        Ostrov.sync(() -> reanimate(loc.w.getEntity(disp)));
    }

    public String toDisplay(final Integer amt) {
        return amt == null ? "--" : String.valueOf((int) amt);
    }

    public boolean isPlaced(final String name, final int place) {
        return place <= ranks.size() && ranks.get(Math.max(1, place) - 1).equals(name);
    }

    public @Nullable
    Integer getAmt(final String name) {
        return stats.get(name);
    }

    public @Nullable
    Entity getEntity() {
        return loc.w.getEntity(disp);
    }

    public void remove() {
        final Entity ent = getEntity();
        if (ent != null) {
            ent.remove();
        }
        ScoreManager.scores.remove(disp);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof ScoreBoard && ((ScoreBoard) o).score.equals(score);
    }

    @Override
    public int hashCode() {
        return score.hashCode();
    }
}
