package ru.komiss77.scoreboard;

import java.util.HashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import ru.komiss77.utils.TCUtils;
import org.bukkit.entity.Player;


public class SideBar {
    
    private final CustomScore board;
    private final String name;
    private final Objective obj;
    private final HashMap <Integer, Line> entries;
    //private final HashMap <Integer, String> entries;
    
    public SideBar(final Player p, final CustomScore board, final String title) {
        entries = new HashMap<>();
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
    
    public void setTitle(final String displayName) {
        obj.displayName(TCUtils.format(displayName));
    }
    
    public void reset() {
        entries.values().forEach( (scoreBoardLine) -> {
            //obj.getScore(scoreBoardLine).resetScore();
            scoreBoardLine.unregister();
            board.getScoreboard().resetScores(scoreBoardLine.getScore().getEntry());
        });
        entries.clear();
    }
    
    public void updateLine(int line, String value) {
        if (line<0 || line>20) {
            line=1;
            value = "§cline от 0 до 20";
        }        
        /*String current = entries.remove(line); - так не сделать одинаковые строки!
        if (current!=null && !current.equals(value)) {
            obj.getScore(value).resetScore();
        }
        obj.getScore(value).setScore(line);
        entries.put(line, value);*/

        Line l = entries.get(line) ;//!= null)  {
        if (l!=null){
            l.update(value);
            //entries.get(line).update(value);
//if (line==14) System.out.println("--updateLine "+line+" "+value);
        } else  {
            entries.put(line, new Line(board, value, line));
//if (line==14) System.out.println("--newLine "+line+" "+value);
        }
    }
    
    
}
