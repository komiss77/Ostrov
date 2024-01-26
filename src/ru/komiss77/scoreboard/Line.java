package ru.komiss77.scoreboard;

import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import net.kyori.adventure.text.Component;
import ru.komiss77.utils.TCUtils;


//чтобы строчки были с цветом, они дрбавляются как тимы
public class Line {
    
    private Team team;
    private final Score score;
    private int hash; //String content;
    
    public Line(final CustomScore scoreBoard, final String s, final int line) {
        final String string = TCUtils.getColor(line) + "§r";
        team = scoreBoard.getScoreboard().registerNewTeam(string);
        score = scoreBoard.getSideBar().getObjective().getScore(string);
        score.setScore(line);
        team.addEntry(string);
        Line.this.update(s);
    }
    
    public void unregister() {
        team.unregister();
        team = null;
    }
    
    public Score getScore() {
        return score;
    }
    
    public void update(final String content) {
        if (hash!=content.hashCode()) {//(!newContent.equals(content)) {
            hash = content.hashCode();
            team.prefix(TCUtils.format(content));
            /*if (content.length() < 16) {
                team.prefix(TCUtils.format(content));
                team.suffix(Component.empty());
            } else { //тут точно больше 16 символов
                if (content.charAt(15)== '§') { //в конце цвет
                    team.prefix(TCUtils.format(content.substring(0, 14)));
                    if (content.length() <= 31) {
                        team.suffix(TCUtils.format(content.substring(15)));
                    } else {
                        team.suffix(TCUtils.format(content.substring(15, 31)));
                    }
                    
                } else {
                    team.prefix(TCUtils.format(content.substring(0, 15)));
                    if (content.length() <= 32) {
                        team.suffix(TCUtils.format(content.substring(16)));
                    } else {
                        team.suffix(TCUtils.format(content.substring(16, 32)));
                    }
                }
            }*/
            
        }
    }
}
