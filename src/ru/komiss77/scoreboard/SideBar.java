package ru.komiss77.scoreboard;

import java.util.HashMap;

import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.entity.Player;




public class SideBar {
    
    private Player p;
    private Objective obj;
    private CustomScore board;
    private HashMap <Integer, Line> entries;
    
    public SideBar(final Player p, final CustomScore board) {
        entries = new HashMap<>();
        this.p = p;
        this.board = board;
        (obj = board.getScoreboard().registerNewObjective("status", "dummy")).setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    
    public Player getPlayer() {
        return p;
    }
    
    public Objective getObjective() {
        return obj;
    }
    
    public void setTitle(final String displayName) {
        obj.setDisplayName(displayName);
    }
    
    public void reset() {
        entries.values().forEach( (scoreBoardLine) -> {
            scoreBoardLine.unregister();
            board.getScoreboard().resetScores(scoreBoardLine.getScore().getEntry());
        });
        entries.clear();
    }
    
    public void updateLine(int line, String value) {
        if (line<1 || line>15) {
            line=1;
            value = "§cline от 0 до 15";
        }
        if (entries.get(line) != null)  {
            entries.get(line).update(value);
        } else  {
            entries.put(line, new Line(board, value, line));
        }
    }
}
