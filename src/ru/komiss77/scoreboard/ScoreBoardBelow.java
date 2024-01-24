package ru.komiss77.scoreboard;

import java.util.List;
import java.util.ArrayList;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Objective;
import ru.komiss77.utils.TCUtils;

@Deprecated
public class ScoreBoardBelow {
    private final Objective obj;
    protected final List<Score> scores;
    
@Deprecated
    public ScoreBoardBelow(final CustomScore scoreBoard, final String text, final int score) {
        scores = new ArrayList<>();
        obj = scoreBoard.getScoreboard().registerNewObjective("below", Criteria.DUMMY, TCUtils.format(text));
        obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        obj.displayName(TCUtils.format(text));
        ScoreBoardBelow.this.update(text, score);
        //final Score sc = obj.getScore(text);
        //sc.setScore(score);
        //scores.add(sc);
    }
    
@Deprecated
    public List<Score> getScores() {
        return scores;
    }
    
@Deprecated
    public Objective getObjective() {
        return obj;
    }
    
@Deprecated
    public void update(final String below_line, final int value) {
        final Score sc = obj.getScore(below_line);
        sc.setScore(value);
        if (!scores.contains(sc)){
            scores.add(sc);
        }
        //obj.getScore(below_line).setScore(value);
    }
}
