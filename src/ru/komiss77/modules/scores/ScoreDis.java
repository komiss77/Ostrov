package ru.komiss77.modules.scores;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Entity;
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
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;


public class ScoreDis {

    protected UUID disp;

    private final String score;
    private final WXYZ loc;
    private final int length;
    private final boolean isAsc;
    private final HashMap<String, Integer> stats = new HashMap<>();
    private final ArrayList<String> ranks = new ArrayList<>();

    private static final double MARGIN = 1d;
    public ScoreDis(final String name, final WXYZ loc, final int length, final boolean isAsc) {
        this.score = name;
        this.loc = loc;
        this.length = length;
        this.isAsc = isAsc;
        final Location lc = loc.getCenterLoc();
        lc.getWorld().getChunkAtAsync(lc).thenAccept(ch -> {
            final TextDisplay near = LocUtil.getClsChEnt(lc, MARGIN, TextDisplay.class, null);
            final TextDisplay td = near == null ? loc.w.spawn(lc, TextDisplay.class) : near;
            ScoreManager.lists.put(disp = modify(td), this);
            reanimate(td);
            ch.setForceLoaded(true);
        });
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

    public void reanimate(final TextDisplay dis) {
        final StringBuilder sb = new StringBuilder();
        sb.append(score).append("\n\n");
        for (int i = 0; i < length; i++) {
            final String nm = ranks.size() > i ? ranks.get(i) : null;
            final String clr = switch (i) {
                case 0 -> "§e";
                case 1 -> "§м";
                case 2 -> "§я";
                default -> "§7";
            };
            if (nm == null) {
                sb.append("§8").append(i + 1).append(") ").append(clr).append("=-=-=-=§б: --\n");
            } else {
                sb.append("§8").append(i + 1).append(") ").append(clr).append(nm)
                    .append("§б: ").append(toDisplay(stats.get(nm))).append("\n");
            }
        }
        final String text = sb.toString();
        dis.text(TCUtil.form(score));
        new BukkitRunnable() {
            TextDisplay etd = dis;
            int i = score.length() + 1;

            @Override
            public void run() {
                if (etd == null || !etd.isValid()) {
                    if (loc.w.getEntity(disp) instanceof final TextDisplay td) etd = td;
                    return;
                }

                if ((i++) == text.length()) {
                    etd.text(TCUtil.form(text));
                    cancel();
                    return;
                }

                etd.text(TCUtil.form(text.substring(0, i)));
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
                stats.remove(ranks.removeLast());
            }
        }

        if (plc == 1) {
            new ScoreWorldRecordEvent(name, amt, this).callEvent();
        }
        reanimate(display());
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
        Ostrov.sync(() -> reanimate(display()));
    }

    public static String toDisplay(final Integer amt) {
        return amt == null ? "--" : String.valueOf((int) amt);
    }

    public boolean isPlaced(final String name, final int place) {
        return place <= ranks.size() && ranks.get(Math.max(1, place) - 1).equals(name);
    }

    public @Nullable Integer amount(final String name) {
        return stats.get(name);
    }

    public @Nullable TextDisplay display() {
        return loc.w.getEntity(disp) instanceof final TextDisplay td ? td : null;
    }

    public void remove() {
        final Entity ent = display();
        if (ent != null) ent.remove();
        ScoreManager.lists.remove(disp);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        return o instanceof ScoreDis && ((ScoreDis) o).score.equals(score);
    }

    @Override
    public int hashCode() {
        return score.hashCode();
    }
}
