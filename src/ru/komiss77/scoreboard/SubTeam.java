package ru.komiss77.scoreboard;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.Craft;
import ru.komiss77.version.Nms;

import java.util.List;

public class SubTeam {

  protected final PlayerTeam nmsTeam;

  public SubTeam(final Team tm) {
    nmsTeam = new PlayerTeam(Craft.toNMS().getScoreboard(), tm.getName());
    nmsTeam.getPlayers().addAll(tm.getEntries());
    color(TCUtils.toChar(tm.color()));
    prefix(tm.prefix()); suffix(tm.suffix());
    collide(tm.getOption(Team.Option.COLLISION_RULE));
    tagVis(tm.getOption(Team.Option.NAME_TAG_VISIBILITY));
    seeInvis(tm.canSeeFriendlyInvisibles());
    attackTeam(tm.allowFriendlyFire());
  }

  public SubTeam(final String name) {
    nmsTeam = new PlayerTeam(Craft.toNMS().getScoreboard(), name);
  }

  public SubTeam include(final String line) {
    nmsTeam.getPlayers().add(line);
    return this;
  }

  public SubTeam include(final Entity ent) {
    nmsTeam.getPlayers().add(ent.getUniqueId().toString());
    return this;
  }

  public SubTeam empty() {
    nmsTeam.getPlayers().clear();
    return this;
  }

  public SubTeam color(final char color) {
    final ChatFormatting c = ChatFormatting.getByCode(color);
    nmsTeam.setColor(c == null ? ChatFormatting.WHITE : c);
    return this;
  }

  public SubTeam color(final NamedTextColor color) {
    final ChatFormatting c = ChatFormatting.getByName(color.toString().toUpperCase());
    nmsTeam.setColor(c == null ? ChatFormatting.WHITE : c);
    return this;
  }

  public SubTeam prefix(final String prefix) {
    return prefix(TCUtils.format(prefix));
  }

  public SubTeam prefix(final Component prefix) {
    nmsTeam.setPlayerPrefix(PaperAdventure.asVanilla(prefix));
    return this;
  }

  public SubTeam suffix(final String suffix) {
    return suffix(TCUtils.format(suffix));
  }

  public SubTeam suffix(final Component suffix) {
    nmsTeam.setPlayerSuffix(PaperAdventure.asVanilla(suffix));
    return this;
  }

  public SubTeam collide(final Team.OptionStatus status) {
    nmsTeam.setCollisionRule(switch (status) {
      case ALWAYS -> net.minecraft.world.scores.Team.CollisionRule.ALWAYS;
      case NEVER -> net.minecraft.world.scores.Team.CollisionRule.NEVER;
      case FOR_OTHER_TEAMS -> net.minecraft.world.scores.Team.CollisionRule.PUSH_OTHER_TEAMS;
      case FOR_OWN_TEAM -> net.minecraft.world.scores.Team.CollisionRule.PUSH_OWN_TEAM;
    });
    return this;
  }

  public SubTeam tagVis(final Team.OptionStatus status) {
    nmsTeam.setNameTagVisibility(switch (status) {
      case ALWAYS -> net.minecraft.world.scores.Team.Visibility.ALWAYS;
      case NEVER -> net.minecraft.world.scores.Team.Visibility.NEVER;
      case FOR_OTHER_TEAMS -> net.minecraft.world.scores.Team.Visibility.HIDE_FOR_OTHER_TEAMS;
      case FOR_OWN_TEAM -> net.minecraft.world.scores.Team.Visibility.HIDE_FOR_OWN_TEAM;
    });
    return this;
  }

  public SubTeam attackTeam(final boolean allow) {
    nmsTeam.setAllowFriendlyFire(allow);
    return this;
  }

  public SubTeam seeInvis(final boolean allow) {
    nmsTeam.setSeeFriendlyInvisibles(allow);
    return this;
  }

  public void send(final Player to) {
    Nms.sendPacket(to, new ClientboundBundlePacket(List.of(ClientboundSetPlayerTeamPacket.createRemovePacket(nmsTeam),
      ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, true))));
  }

  public void send(final World to) {
    Nms.sendWorldPacket(to, new ClientboundBundlePacket(List.of(ClientboundSetPlayerTeamPacket.createRemovePacket(nmsTeam),
      ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(nmsTeam, true))));
  }

  public void remove(final Player to) {
    Nms.sendPacket(to, ClientboundSetPlayerTeamPacket.createRemovePacket(nmsTeam));
  }

  public void remove(final World to) {
    Nms.sendWorldPacket(to, ClientboundSetPlayerTeamPacket.createRemovePacket(nmsTeam));
  }
}
