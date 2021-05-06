package ru.komiss77.scoreboard;

import java.util.List;
import java.util.ArrayList;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Objective;





public class ScoreBoardBelow
{
    private final Objective obj;
    private final List<Score> scores;
    
    //public ScoreBoardBelow(final CustomScore scoreBoard, final String score_name, final int value, final List<Player>show_to) {
    public ScoreBoardBelow(final CustomScore scoreBoard, final String below_line, final int value) {
        scores = new ArrayList<>();
        obj = scoreBoard.getScoreboard().registerNewObjective("below", "dummy");
        obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        //obj.setDisplayName("\u9280");
        obj.setDisplayName(below_line);
        
            final Score score = obj.getScore(below_line);
            score.setScore(value);
            scores.add(score);
       // show_to.forEach((p) -> {
       //     final Score score = obj.getScore(p.getName());
       //     score.setScore(value);
       //     scores.add(score);
      //  });
        // for (final Player player2 : game.getTeamB().getPlayers()) {
        //    final Score score2 = this.obj.getScore(player2.getName());
        //    score2.setScore((int)(player2.getHealth() / player2.getMaxHealth() * 100.0));
        //    this.scores.add(score2);
        //}
    }
    
    public List<Score> getScores() {
        return scores;
    }
    
    public Objective getObjective() {
        return obj;
    }
    
    public void update(final String below_line, final int value) {
        //obj.getScore(player.getName()).setScore((int)(player.getHealth() / player.getMaxHealth() * 100.0));
        obj.getScore(below_line).setScore(value);
    }
}
