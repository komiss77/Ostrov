package ru.komiss77.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;

public class CustomScore extends SubTeam {
  //private static final CaseInsensitiveMap<CustomScore> boards; //для удобства перебора
  protected final String ownerName;
  private Scoreboard ownerBoard;
  private final SideBar sideBar;
    
    
  public CustomScore(final Player p) {
    super(p.getUniqueId().toString());
    include(p.getName());
    tagVis(Team.OptionStatus.NEVER);
    seeInvis(false);

    ownerName = p.getName();
    ownerBoard = Bukkit.getScoreboardManager().getNewScoreboard();
    sideBar = new SideBar(this, ownerName);
    p.setScoreboard(ownerBoard);

    for (final World w : Bukkit.getWorlds()) send(w);
    for (final Oplayer op : PM.getOplayers()) op.score.send(p);
  }
    

   // private void create() { //при входе на серв не бота
        //ownerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER); //кого добавить в эту тиму, его ник скроется
        //ownerTeam.setCanSeeFriendlyInvisibles(false);
        //всосать данные с других
        //if (!botBoard) {
            //for (CustomScore otherScore : boards.values()) {
            //    updTeam(otherScore.ownerName, otherScore.ownerTeam); // закинуть данные с борд других
            //}
        //}
    //    boards.put(ownerName, this); //добавлять после перебора других борд!
  //  }


    
    public void remove() {
      if (ownerBoard==null) {
          return;
      }
      for (final Objective ob : ownerBoard.getObjectives()) {
          ob.unregister();
      }
      for (final Team tm : ownerBoard.getTeams()) {
          tm.unregister();
      }
      ownerBoard = null;
      for (final World w : Bukkit.getWorlds()) {
        super.remove(w);
      }
    }

    /*@Deprecated
    //владелец борды увидел target (только игрока, не бота!)
    public void startTrack(final Player tracker, final String target) {
      startTrack(tracker, target, NamedTextColor.WHITE);
    }

    //владелец борды увидел target (только игрока, не бота!)
    public void startTrack(final Player tracker, final String target, final NamedTextColor color) {
      final ChatFormatting c = ChatFormatting.getByName(color.toString().toUpperCase());
      nmsTeam.setColor(c == null ? ChatFormatting.WHITE : c);
      Nms.sendPackets(tracker,
        ClientboundSetPlayerTeamPacket.createPlayerPacket(nmsTeam, target, ClientboundSetPlayerTeamPacket.Action.ADD),
        ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, false)
      );
    }

    //владелец борды больше не видит target (только игрока, не бота!)
    public void stopTrack(final Player tracker, final String target) {
      Nms.sendPackets(tracker,
        ClientboundSetPlayerTeamPacket.createPlayerPacket(nmsTeam, target, ClientboundSetPlayerTeamPacket.Action.REMOVE),
        ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, false)
      );
    }

    @Deprecated
    public static void allStartTrack(final String name) {
      allStartTrack(name, NamedTextColor.WHITE);
    }

    public static void trackBot(final String target, final NamedTextColor color) {
      final ChatFormatting c = ChatFormatting.getByName(color.toString().toUpperCase());
      nmsTeam.setColor(c == null ? ChatFormatting.WHITE : c);
      final ClientboundBundlePacket cbp = new ClientboundBundlePacket(List.of(
  //          ClientboundSetPlayerTeamPacket.createRemovePacket(nmsTeam),
        ClientboundSetPlayerTeamPacket.createPlayerPacket(nmsTeam, target, ClientboundSetPlayerTeamPacket.Action.ADD),
        ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, false)));
      for (final World w : Bukkit.getWorlds()) {
        Nms.sendWorldPacket(w, cbp);
      }
    }

    public static void allStopTrack(final String name) {
      final ClientboundBundlePacket cbp = new ClientboundBundlePacket(List.of(
        ClientboundSetPlayerTeamPacket.createPlayerPacket(nmsTeam, name, ClientboundSetPlayerTeamPacket.Action.REMOVE),
        ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, false)));
      for (final World w : Bukkit.getWorlds()) {
        Nms.sendWorldPacket(w, cbp);
      }
    }*/
    
    public Scoreboard getScoreboard() {
        return ownerBoard;
    }
    
    public SideBar getSideBar() {
        return sideBar;
    }

    @Deprecated
    public Team getTeam() {
        return null; //ownerTeam;
    }







    
}
