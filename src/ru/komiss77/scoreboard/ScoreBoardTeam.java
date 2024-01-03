package ru.komiss77.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import ru.komiss77.utils.TCUtils;




public class ScoreBoardTeam {
    
    private Scoreboard board;
    //private List<Team> teams;
    private Team team;
    
    public ScoreBoardTeam( final String name, final Scoreboard board) {
        
        team = board.registerNewTeam(name);
        team.prefix(TCUtils.format(""));
        team.suffix(TCUtils.format(""));
        team.addEntry(name);
       
        
     /* this.teams = new ArrayList<>();
        this.board = board;
        for (final Player player : Main.getManager().getTeam(game, GameTeam.Role.TERRORIST).getPlayers()) {
            final Team registerNewTeam = board.registerNewTeam(player.getName());
            final PlayerData playerStatus = GameManager.players_data.get(player.getName());//game.getStats().get(player.getUniqueId());
            registerNewTeam.setPrefix("§8[§4\u9291§8] " + (game.getSpectators().contains(player) ? "§7§o" : "§4"));
            registerNewTeam.setSuffix(" §8[§e" + playerStatus.getKills() + "-" + playerStatus.getDeaths() + "§8]");
            Main.getVersionInterface().hideNameTag(registerNewTeam);
            registerNewTeam.addEntry(player.getName());
            this.teams.add(registerNewTeam);
        }
        for (final Player player2 : Main.getManager().getTeam(game, GameTeam.Role.COUNTERTERRORIST).getPlayers()) {
            final Team registerNewTeam2 = board.registerNewTeam(player2.getName());
            final PlayerData playerStatus2 = GameManager.players_data.get(player2.getName());//game.getStats().get(player2.getUniqueId());
            registerNewTeam2.setPrefix("§8[§3\u9290§8] " + (game.getSpectators().contains(player2) ? "§7§o" : "§3"));
            registerNewTeam2.setSuffix(" §8[§e" + playerStatus2.getKills() + "-" + playerStatus2.getDeaths() + "§8]");
            Main.getVersionInterface().hideNameTag(registerNewTeam2);
            registerNewTeam2.addEntry(player2.getName());
            this.teams.add(registerNewTeam2);
        }*/
    }
    
    public void add(final Player player) {
       /* final Team registerNewTeam = this.board.registerNewTeam(player.getName());
        final PlayerData playerStatus = GameManager.players_data.get(player.getName());//game.getStats().get(player.getUniqueId());
        final boolean contains = game.getSpectators().contains(player);
        registerNewTeam.setPrefix((Main.getManager().getTeam(game, player) == GameTeam.Role.TERRORIST) ? ("§8[§4\u9291§8] " + (contains ? "§7§o" : "§4")) : ("§8[§3\u9290§8] " + (contains ? "§7§o" : "§3")));
        registerNewTeam.setSuffix(" §8[§e" + playerStatus.getKills() + "-" + playerStatus.getDeaths() + "§8]");
        Main.getVersionInterface().hideNameTag(registerNewTeam);
        registerNewTeam.addEntry(player.getName());
        this.teams.add(registerNewTeam);*/
    }
    
    public void remove(final Player player) {
        final Team team = this.board.getTeam(player.getName());
        team.unregister();
        //this.teams.remove(team);
    }
    
    //public List<Team> getTeams() {
    //    return this.teams;
    //}
    
    public void update(final Player player) {
       /* final Team team = this.board.getTeam(player.getName());
        final boolean contains = game.getSpectators().contains(player);
        final PlayerData playerStatus = GameManager.players_data.get(player.getName());//game.getStats().get(player.getUniqueId());
        team.setPrefix((Main.getManager().getTeam(game, player) == GameTeam.Role.TERRORIST) ? ("§8[§4\u9291§8] " + (contains ? "§7§o" : "§4")) : ("§8[§3\u9290§8] §3" + (contains ? "§7§o" : "§3")));
        team.setSuffix(" §8[§e" + playerStatus.getKills() + "-" + playerStatus.getDeaths() + "§8]");
        Main.getVersionInterface().hideNameTag(team);
    */}

    public void setPrefix(final String prefix) {
        team.prefix(TCUtils.format(prefix));
    }

    public void setSuffix(final String suffix) {
        team.suffix(TCUtils.format(suffix));
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
