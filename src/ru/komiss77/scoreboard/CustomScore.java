package ru.komiss77.scoreboard;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;





public class CustomScore {
    
    private final String name;
    private final Scoreboard board;
    // private ScoreBoardTeam team;
    private ScoreBoardBelow below;
    private final SideBar sideBar;
    //private final Team team;
    
    public CustomScore(final Player player) {
        name = player.getName();
//System.out.println("1");
        board = Bukkit.getScoreboardManager().getNewScoreboard();
//System.out.println("2");
        sideBar = new SideBar(player, this);
        //team = new ScoreBoardTeam (name, board);
//System.out.println("3");
      //  team = board.registerNewTeam("0"+name);
//System.out.println("4");
      //  team.addEntry(name);
       // team.setDisplayName("0"+name);
       
//System.out.println("5 team.members = "+team.getEntries());
        
        player.setScoreboard(board);
        
     //   ProtocolLibrary.getProtocolManager().addPacketListener(new TeamPacketListener());
    }
    
    public Scoreboard getScoreboard() {
        return board;
    }
    
    public SideBar getSideBar() {
        return sideBar;
    }
    

    

    
    
   /*
      public static final int TEAM_CREATED = 0;
      public static final int TEAM_REMOVED = 1;
      public static final int TEAM_UPDATED = 2;
      public static final int PLAYERS_ADDED = 3;
      public static final int PLAYERS_REMOVED = 4;
 
        
        String name = UUID.randomUUID().toString().substring(0, 16);
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
        Class clas = packet.getClass();
        Field team_name = getField(clas, "a"); //team_name
        Field display_name = getField(clas, "b"); //display_name
        Field prefix1 = getField(clas, "c"); //prefix1
        Field suffix1 = getField(clas, "d"); //suffix1
        Field members = getField(clas, "h"); //members
        Field param_int = getField(clas, "i");
        Field pack_option = getField(clas, "j");  //allowFriendlyFire

        setField(packet, team_name, name);
        setField(packet, display_name, nik);
        setField(packet, prefix1, prefix);
        setField(packet, suffix1, suffix);
        setField(packet, members, Arrays.asList(new String[] { nik}));
        setField(packet, param_int, 0);
        setField(packet, pack_option, 1);
        
        Bukkit.getOnlinePlayers().stream().forEach( (i) -> {
            ((CraftPlayer) i).getHandle().playerConnection.sendPacket(packet);
        });

    
    */ 
    
   // public void setPrefix(final String prefix) {
//System.out.println("setPrefix "+prefix+"members="+team.getEntries());
       // team.setPrefix(prefix);
      
     //   final ScoreboardTeam NmsTeam = ((CraftScoreboard)board).getHandle().getTeam("0"+name);
        //NmsTeam.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS);
        
//System.out.println("NmsTeam name="+NmsTeam.getName()+" member="+NmsTeam.getPlayerNameSet()+" tDisplayName="+NmsTeam.getDisplayName().getText()+" prefix="+NmsTeam.getPrefix().getText());

        //PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(NmsTeam, 0); - выкидывает
    //    PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(NmsTeam, 0);
        //setField (packet, "h", Arrays.asList(new String[] {name}) );
    //    setField (packet, "j", 1);
       
    //    SendPacket(packet);
      
     //   packet = new PacketPlayOutScoreboardTeam(NmsTeam, 3);
     //   setField (packet, "h", Arrays.asList(new String[] {name}) );
     //   SendPacket(packet);
  //  }

  // public void setSuffix(final String suffix) {
    //  team.setSuffix(suffix);
  // }
    
    
    
  // private void SendPacket (final PacketPlayOutScoreboardTeam packet) {
  //     for (Player p : Bukkit.getOnlinePlayers() ) {
//System.out.println("SendPacket "+p.getName());
         //  ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
   //    }
  // } 
    //public ScoreBoardTeam getTeams() {
    //    return team;
    //}
    
    
 //   public void showTeams() {
        //team = new ScoreBoardTeam( party, board);
        //Utils.sendInvisibility(this, party);
        
        /*for (final Player player : game.getTeamA().getPlayers()) {
            if (!game.getSpectators().contains(player)) {
                final ScoreboardTeam team = ((CraftScoreboard)scoreBoard.getScoreboard()).getHandle().getTeam(player.getName());
                try {
                    final Field declaredField = team.getClass().getDeclaredField("i");
                    declaredField.setAccessible(true);
                    declaredField.set(team, ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS);
                }
                catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
                sendListPacket(game.getTeamA(), (Packet<?>)new PacketPlayOutScoreboardTeam(team, 2));
            }
        }
        for (final Player player2 : game.getTeamB().getPlayers()) {
            if (!game.getSpectators().contains(player2)) {
                final ScoreboardTeam team2 = ((CraftScoreboard)scoreBoard.getScoreboard()).getHandle().getTeam(player2.getName());
                try {
                    final Field declaredField2 = team2.getClass().getDeclaredField("i");
                    declaredField2.setAccessible(true);
                    declaredField2.set(team2, ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS);
                }
                catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex2) {
                    ex2.printStackTrace();
                }
                sendListPacket(game.getTeamB(), (Packet<?>)new PacketPlayOutScoreboardTeam(team2, 2));
            }
        }*/
    

 //   }
    
  //  public void removeTeam() {
        /*if (team != null) {
            final Iterator<Team> iterator = team.getTeams().iterator();
            while (iterator.hasNext()) {
                iterator.next().unregister();
            }
            team = null;
        }*/
  //  }
    

    
    
 
    
    
    
    
    
    
    
    
    
    
    
    
    
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
