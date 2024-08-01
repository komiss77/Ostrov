package ru.komiss77.scoreboard;

import javax.annotation.Nullable;

import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.komiss77.utils.TCUtils;


//чтобы строчки были с цветом, они дрбавляются как тимы
@Deprecated
public class Line {

    private final Scoreboard board;
    private final Score score;

    private Team team = null;
    //PlayerTeam nmsTeam;
    private String fakeValue;

    @Deprecated
    public Line(final CustomScore sb, final String value, final int line) {
        //final Player p = Bukkit.getPlayerExact(sb.ownerName);
        //if (p==null) return;
        fakeValue = TCUtils.getColor(line) + "§r"; //невидимое значение - цветовой код + сброс цвета
        // nmsTeam =  new PlayerTeam(((CraftScoreboard)sb.getScoreboard()).getHandle(), fakeValue);
        //Nms.sendPackets(p,
        //   ClientboundSetPlayerTeamPacket.createRemovePacket(nmsTeam), //подчистить старую, если была
        //  ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, true),
        //ClientboundSetPlayerTeamPacket.createPlayerPacket(nmsTeam, e.getUniqueId().toString(), ClientboundSetPlayerTeamPacket.Action.ADD),
        //  ClientboundSetPlayerTeamPacket.createPlayerPacket(nmsTeam, fakeValue, ClientboundSetPlayerTeamPacket.Action.ADD),
        //  ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, false)
        //);
        board = sb.getScoreboard();
        team = board.registerNewTeam(fakeValue);
        score = sb.getSideBar().getObjective().getScore(fakeValue);
        score.setScore(line);
        team.addEntry(fakeValue);
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
        if (team != null) {
            score.resetScore();
            team.removeEntry(fakeValue);
            team.unregister();
        }
        team = null;
    }

    public Score getScore() {
        return score;
    }

    public void update(final @Nullable String content) {
        if (content == null) {
            unregister();
            return;
        }

        if (team == null) {
            final String name = score.getEntry();
            team = board.getTeam(name);
            if (team == null) {
                team = board.registerNewTeam(name);
                team.addEntry(name);
            }
        }

        team.prefix(TCUtils.form(content));
    }
}
