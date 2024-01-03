package ru.komiss77.scoreboard;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.komiss77.modules.player.PM;





public class CustomScore {
    
    private final String name;
    private final Scoreboard board;
    //private ScoreBoardTeam team;
    private ScoreBoardBelow below;
    private final SideBar sideBar;
    private final Team team;
    
    public boolean hideNameTags;
    
    
    public CustomScore(final Player player) {
        name = player.getName();
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        sideBar = new SideBar(player, this, name);
        team = board.registerNewTeam("_"+name);
        
        player.setScoreboard(board);
    }
    
    public Scoreboard getScoreboard() {
        return board;
    }
    
    public SideBar getSideBar() {
        return sideBar;
    }
    

    public Team getTeam() {
        return team;
    }

    public void hideNameTags() {
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        team.addEntries(PM.getOplayersNames());
        hideNameTags = true;
    }
    
    public void showNameTags() {
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        team.removeEntries(PM.getOplayersNames());
        hideNameTags = false;
    }
    
    
    public ScoreBoardBelow getBelow() {
        return below;
    }
    
    public void showBelow(final String below_line, final int value) {
        below = new ScoreBoardBelow(this, below_line, value);
    }
    
    public void removeBelow() {
        if (below != null) {
            final Iterator<Score> iterator = below.getScores().iterator();
            while (iterator.hasNext()) {
                board.resetScores(iterator.next().getEntry());
            }
            below.getObjective().unregister();
            below = null;
        }
    }
    

    
    
    
    
    
    
    public void remove() {
       // removeTeam();
        removeBelow();
        final Iterator<Objective> iterator = board.getObjectives().iterator();
        while (iterator.hasNext()) {
            iterator.next().unregister();
        }
        final Iterator<Team> iterator2 = board.getTeams().iterator();
        while (iterator2.hasNext()) {
            iterator2.next().unregister();
        }
    }

    
    /*
    private void setField(PacketPlayOutScoreboardTeam packet, String path, Object newValue) {
        try {
            Field field = packet.getClass().getDeclaredField(path);
            field.setAccessible(true);
            field.set(packet, newValue);
            field.setAccessible(false);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        
    }

        private static void setField(Object packet, Field field, Object value) {
        field.setAccessible(true);

        try {
            field.set(packet, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            //e.printStackTrace();
        }

        field.setAccessible(!field.isAccessible());
    }

    private static Field getField(Class classs, String fieldname) {
        try {
            return classs.getDeclaredField(fieldname);
        } catch (SecurityException | NoSuchFieldException e) {
            //e.printStackTrace();
            return null;
        }
    }
    */    
    
}
