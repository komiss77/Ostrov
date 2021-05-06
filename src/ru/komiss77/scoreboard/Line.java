package ru.komiss77.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;







public class Line
{
    private Team team;
    private Score score;
    private String name;
    
    public Line(final CustomScore scoreBoard, final String s, final int score) {
        final String string = ChatColor.values()[score - 1] + "§r";
        this.team = scoreBoard.getScoreboard().registerNewTeam(string);
        (this.score = scoreBoard.getSideBar().getObjective().getScore(string)).setScore(score);
        this.team.addEntry(string);
        this.update(s);
    }
    
    public void unregister() {
        this.team.unregister();
        this.team = null;
    }
    
    public Score getScore() {
        return this.score;
    }
    
    public void update(final String content) {
//System.out.println("update 111 line="+this.name+"  content="+content );
        if (!content.equals(this.name)) {
            this.name = content;
//System.out.println("update 222 line="+this.name );
            String substring = (name.length() >= 16) ? name.substring(0, 16) : name;
            String s = "";
            if (substring.length() > 0 && substring.charAt(substring.length() - 1) == '§') {
                substring = substring.substring(0, substring.length() - 1);
                s = "§";
            }
            this.team.setPrefix(substring);
            if (name.length() > 16) {
                final String string = String.valueOf(ChatColor.getLastColors(substring)) + s + name.substring(s.equals("§") ? 15 : 16, name.length());
                if (string.length() <= 16) {
                    this.team.setSuffix(string);
                }
                else {
                    this.team.setSuffix(string.substring(0, 16));
                }
            }
            else {
                this.team.setSuffix("");
            }
        }
    }
}
