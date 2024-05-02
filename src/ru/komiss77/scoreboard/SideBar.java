package ru.komiss77.scoreboard;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import ru.komiss77.Ostrov;
import ru.komiss77.objects.Duo;
import ru.komiss77.utils.TCUtils;




public class SideBar {

  public final String ext = "§r";

  private final Objective obj;
  private final HashMap<Integer, String> data;
  private final HashMap<String, Integer> lines;
  private final LinkedList<Duo<String, String>> toAdd;

  private int nextLine = 0;
  //private final HashMap <Integer, String> entries;

  public SideBar(final CustomScore board, final String title) {
    toAdd = new LinkedList<>();
    data = new HashMap<>();
    lines = new HashMap<>();
    obj = board.getScoreboard().registerNewObjective("status", Criteria.DUMMY, TCUtils.format(title));
    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    obj.displayName(Component.empty());
    obj.numberFormat(NumberFormat.blank());
  }

  @Deprecated
  public SideBar(final Player p, final CustomScore board, final String title) {
    toAdd = new LinkedList<>();
    data = new HashMap<>();
    lines = new HashMap<>();
    obj = board.getScoreboard().registerNewObjective("status", Criteria.DUMMY, TCUtils.format(title));
    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    obj.displayName(Component.empty());
    obj.numberFormat(NumberFormat.blank());
  }

  public Objective getObjective() {
    return obj;
  }

  //@Deprecated
  public SideBar setTitle(final String displayName) {
    return title(displayName);
  }

  public SideBar title(final String name) {
    obj.displayName(TCUtils.format(name));
    return this;
  }

  public SideBar reset() {
    data.forEach( (key, value) -> obj.getScore( "§"+(char)('a' + key)+ext+value).resetScore());
    data.clear();

    toAdd.clear();
    lines.clear();
    nextLine = 0;
    return this;
  }

  @Deprecated
  public void updateLine(final int line, final String value) {
    update(line, value);
  }

  public SideBar update(final int line, final String value) {
    /*if (line<0 || line>14) {можно 26 и если > 26 будет то тогда подумаем
      line=1;
      value = "§cline от 0 до 14";
    }*/
    final String old = data.get(line);
    if (old!=null) {
      if (old.equals(value)) {//нет изменения - ничего не делаем
        return this;
      }
      obj.getScore("§"+(char)('a' + line)+ext+old).resetScore(); //строка изменилась - удалить старую
    }
    obj.getScore("§"+(char)('a' + line)+ext+value).setScore(line);
    data.put(line, value);
    return this;
  }

  public SideBar update(final String key, final String value) {
    final Integer line = lines.get(key);
    if (line == null) {
      Ostrov.log_warn("Tried updating null score " + key);
      return this;
    }
    return update(line, value);
  }

  public SideBar add(final String key) {
    return add(key, null);
  }

  //проба
  public void remove(final String key) {
    final Integer line = lines.remove(key);
    if (line != null) {
      final String old = data.get(line);
      if (old!=null) {
        obj.getScore("§"+(char)('a' + line)+ext+old).resetScore();
      }
    }
  }

  public SideBar add(final String key, final @Nullable String value) {
    toAdd.addFirst(new Duo<>(key, value));
    return this;
  }

  public SideBar build() {
    for (final Duo<String, String> pr : toAdd) {
      if (pr.val() == null) {
        update(nextLine, pr.key());
        nextLine++; continue;
      }
      update(nextLine, pr.val());
      lines.put(pr.key(), nextLine);
      nextLine++;
    }
    return this;
  }

  /*public SideBar update(final String name, final @Nullable String value) {
    final Line l = lines.get(name);
    if (l == null) {
      Ostrov.log_warn("Tried updating null line " + name);
      return this;
    }
    else l.update(value);
    return this;
  }*/

  /*private void putLine(final String name, final String value) {
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
  }*/
}
