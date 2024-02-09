package ru.komiss77.scoreboard;

import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.komiss77.utils.TCUtils;

import javax.annotation.Nullable;


//чтобы строчки были с цветом, они дрбавляются как тимы
public class Line {

  private final Scoreboard board;
  private final Score score;

  private Team team = null;
  private int hash = 0; //String content;

  @Deprecated
  public Line(final CustomScore scoreBoard, final String value, final int line) {
    final String name = TCUtils.getColor(line) + "§r";
    board = scoreBoard.getScoreboard();
    team = board.registerNewTeam(name);
    score = scoreBoard.getSideBar().getObjective().getScore(name);
    score.setScore(line);
    team.addEntry(name);
    update(value);
  }

  public Line(final CustomScore board, final String name) {
    this.board = board.getScoreboard();
    score = board.getSideBar().getObjective().getScore(name);
  }

  public Line(final CustomScore board, final String name, final String value) {
    this.board = board.getScoreboard();
    score = board.getSideBar().getObjective().getScore(name);
    update(value);
  }

  public void unregister() {
    team.unregister();
    team = null;
  }

  public Score getScore() {
    return score;
  }

  public void update(final @Nullable String content) {
    if (content == null) {
      if (team != null) unregister();
      return;
    }

    if (team == null) {
      final String name = score.getEntry();
      team = board.getTeam(name);
      if (team == null) {
        team = board.registerNewTeam(name);
      }
    }

    if (hash!=content.hashCode()) {
      hash = content.hashCode();
      team.prefix(TCUtils.format(content));
    }
  }
}
