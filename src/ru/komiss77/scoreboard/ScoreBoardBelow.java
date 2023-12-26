package ru.komiss77.scoreboard;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;

import ru.komiss77.utils.TCUtils;

import org.bukkit.scoreboard.Objective;





public class ScoreBoardBelow
{
    private final Objective obj;
    private final List<Score> scores;
    
    public ScoreBoardBelow(final CustomScore scoreBoard, final String text, final int score) {
        scores = new ArrayList<>();
        obj = scoreBoard.getScoreboard().registerNewObjective("below", Criteria.DUMMY, TCUtils.format(text));
        obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        obj.displayName(TCUtils.format(text));
        
        final Score sc = obj.getScore(text);
        sc.setScore(score);
        scores.add(sc);
    }
    
    public List<Score> getScores() {
        return scores;
    }
    
    public Objective getObjective() {
        return obj;
    }
    
    public void update(final String below_line, final int value) {
        obj.getScore(below_line).setScore(value);
    }
}
