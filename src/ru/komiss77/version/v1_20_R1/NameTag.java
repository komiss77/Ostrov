package ru.komiss77.version.v1_20_R1;

import java.util.Collection;
import java.util.function.Predicate;

import org.bukkit.entity.Player;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase.EnumNameTagVisibility;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.INameTag;
import ru.komiss77.version.VM;

//https://www.spigotmc.org/resources/nametagedit.3836/           https://github.com/sgtcaze/NametagEdit/issues
//https://github.com/sgtcaze/NametagEdit/tree/master/src/main/java/com/nametagedit/plugin
//https://github.com/jiangdashao/VirtualTag/blob/master/src/main/kotlin/me/rerere/virtualtag/tag/VirtualTeam.kt    

/*
    PARAM_INT = getNMS(currentVersion.getParamInt());
    TEAM_NAME = getNMS(currentVersion.getTeamName());
    MEMBERS = getNMS(currentVersion.getMembers());
    PARAMS = getNMS(currentVersion.getParams());

    PREFIX = getParamNMS(currentVersion.getPrefix());
    SUFFIX = getParamNMS(currentVersion.getSuffix());
    PACK_OPTION = getParamNMS(currentVersion.getPackOption());
    DISPLAY_NAME = getParamNMS(currentVersion.getDisplayName());
    TEAM_COLOR = getParamNMS(currentVersion.getColor());
    PUSH = getParamNMS(currentVersion.getPush());
    VISIBILITY = getParamNMS(currentVersion.getVisibility());
    
    v1_17("j", "b", "c", "i", "h", "g", "a", "f", "e", "d", "k");
    v1_18("j", "b", "c", "i", "h", "g", "a", "f", "e", "d", "k")

    private final String members; "j"
    private final String prefix; "b"
    private final String suffix; "c"
    private final String teamName; "i"
    private final String paramInt; "h"
    private final String packOption; "g"
    private final String displayName; "a"
    private final String color; "f"
    private final String push; "e"
    private final String visibility; "d"
    // 1.17+
    private final String params; "k"
    
    EnumChatFormat {
    a,      b,          c,          d,           e,         f,      g,       h,     i,      j,      k,   l,    m,       n,          o,     p,       q,        r,        s,           t,        u,    v;
    BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE, OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET;
 */
public class NameTag implements INameTag {

    @Override
    public void updateTag(final Oplayer op, final Player to) {
        updateTag(op.nik, op.tagPrefix().append(TCUtils.format(op.nameColor())),
                op.tagSuffix(), op.nameColor().charAt(1), to, p -> op.isTagVis(p));
    }

    @Override
    public void updateTag(final Oplayer op, final Collection<? extends Player> toPlayers) {
        updateTag(op.nik, op.tagPrefix().append(TCUtils.format(op.nameColor())),
                op.tagSuffix(), op.nameColor().charAt(1), toPlayers, p -> op.isTagVis(p));
    }

    @Override
    public void updateTag(final String name, final String pfx, final String sfx,
            final char clr, final Player to, final Predicate<Player> canSee) {
        updateTag(name, TCUtils.format(pfx), TCUtils.format(sfx), clr, to, canSee);
    }

    @Override
    public void updateTag(final String name, final String pfx, final String sfx,
            final char clr, final Collection<? extends Player> toPlayers, final Predicate<Player> canSee) {
        updateTag(name, TCUtils.format(pfx), TCUtils.format(sfx), clr, toPlayers, canSee);
    }

    private void updateTag(final String name, final Component pfx, final Component sfx,
            final char clr, final Player to, final Predicate<Player> canSee) {
      /*  final Scoreboard board = VM.getNmsServer().toNMS().aF();
        final ScoreboardTeam team = board.g(name);
        team.b(PaperAdventure.asVanilla(pfx));
        team.c(PaperAdventure.asVanilla(sfx));
        final EnumChatFormat ecf = EnumChatFormat.a(clr);
        team.a(ecf == null ? EnumChatFormat.p : ecf);
//        team.a(PaperAdventure.asVanilla(TCUtils.format(op.nameColor()
//                + ("Гость_" + op.nik))));
//        team.d(PaperAdventure.asVanilla(TCUtils.format(op.nameColor()
//                + ("Гость_" + op.nik))));
        final PacketPlayOutScoreboardTeam teamPacket = PacketPlayOutScoreboardTeam.a(team);
        final PacketPlayOutScoreboardTeam teamCreatePacket = PacketPlayOutScoreboardTeam.a(team, true);
        final PacketPlayOutScoreboardTeam teamAddPacket = PacketPlayOutScoreboardTeam.a(team, name, PacketPlayOutScoreboardTeam.a.a);
        final PacketPlayOutScoreboardTeam teamModifySame = PacketPlayOutScoreboardTeam.a(team, false);
        team.a(EnumNameTagVisibility.b);
        final PacketPlayOutScoreboardTeam teamModifyDiff = PacketPlayOutScoreboardTeam.a(team, false);
        board.d(team);

        final PlayerConnection pc = VM.getNmsServer().toNMS(to).c;
        pc.a(teamPacket);
        pc.a(teamCreatePacket);
        pc.a(teamAddPacket);
        pc.a(canSee.test(to) ? teamModifySame : teamModifyDiff);*/
    }

    private void updateTag(final String name, final Component pfx, final Component sfx,
            final char clr, final Collection<? extends Player> toPlayers, final Predicate<Player> canSee) {
    /*    final Scoreboard board = VM.getNmsServer().toNMS().aF();
        final ScoreboardTeam team = board.g(name);
        team.b(PaperAdventure.asVanilla(pfx));
        team.c(PaperAdventure.asVanilla(sfx));
        final EnumChatFormat ecf = EnumChatFormat.a(clr);
        team.a(ecf == null ? EnumChatFormat.p : ecf);
        final PacketPlayOutScoreboardTeam teamPacket = PacketPlayOutScoreboardTeam.a(team);
        final PacketPlayOutScoreboardTeam teamCreatePacket = PacketPlayOutScoreboardTeam.a(team, true);
        final PacketPlayOutScoreboardTeam teamAddPacket = PacketPlayOutScoreboardTeam.a(team, name, PacketPlayOutScoreboardTeam.a.a);
        final PacketPlayOutScoreboardTeam teamModifySame = PacketPlayOutScoreboardTeam.a(team, false);
        team.a(EnumNameTagVisibility.b);
        final PacketPlayOutScoreboardTeam teamModifyDiff = PacketPlayOutScoreboardTeam.a(team, false);
        board.d(team);

        for (final Player to : toPlayers) {
            final PlayerConnection pc = VM.getNmsServer().toNMS(to).c;
            pc.a(teamPacket);
            pc.a(teamCreatePacket);
            pc.a(teamAddPacket);
            pc.a(canSee.test(to) ? teamModifySame : teamModifyDiff);
        }*/
    }

//    private static final HashMap<String, ScoreboardTeam> TEAMS = new HashMap<>();
//    private static final HashMap<String, ScoreboardTeam> CACHED_FAKE_TEAMS = new HashMap<>();
    //создает тиму final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, true);  paramInt=0
    //добавляет игрока final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, p.getName(), a.a);  paramInt=3
    //модифицирует final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, false);  paramInt=2
    //убирает игрока final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st, p.getName(), a.b);  paramInt=4
    //удаляет тиму final PacketPlayOutScoreboardTeam pt = PacketPlayOutScoreboardTeam.a(st);  paramInt=1
    /*@Override
    public void setNametag(final Player p, String prefix, String suffix) {
        ScoreboardTeam previous = CACHED_FAKE_TEAMS.get(p.getName());

        //1.17 if (previous != null && previous.getPrefix().getString().equals(prefix) && previous.getSuffix().getString().equals(suffix)) {
        if (previous != null && previous.e().getString().equals(prefix) && previous.f().getString().equals(suffix)) {
//Ostrov.log_warn(p.getName() + " already belongs to a similar team (" + previous.getName() + ")");
            return;
        }

        resetNametag(p);

        ScoreboardTeam fakeTeam = findFakeTeam(prefix, suffix); //ищем подходящую тиму с таким же префиксом
        if (fakeTeam != null) {
            
            //1.17 fakeTeam.getPlayerNameSet().add(p.getName()); 
            fakeTeam.g().add(p.getName());
            
            final PacketPlayOutScoreboardTeam addPlayer = PacketPlayOutScoreboardTeam.a(fakeTeam, p.getName(), PacketPlayOutScoreboardTeam.a.a);
            broadcastPacket(addPlayer);//addPlayerToTeamPackets(fakeTeam, p.getName());
//Ostrov.log_warn("Using existing team for " + p.getName());
            
        } else {
            
            String teamName = generateUUID();
            if (teamName.length() > 256) teamName = teamName.substring(0, 256);
            
            //final EntityPlayer ep = ((CraftPlayer)p).getHandle();
            final Scoreboard nmsSb = ((CraftPlayer)p).getScoreboard().getHandle();//ep.getMinecraftServer().getScoreboard();
            
            fakeTeam = nmsSb.g(teamName);//1.17 fakeTeam = sb.createTeam(teamName);
            fakeTeam.b(IChatBaseComponent.a(prefix));//1.17 fakeTeam.setPrefix(IChatBaseComponent.a(prefix));
            fakeTeam.c(IChatBaseComponent.a(suffix));//1.17 fakeTeam.setSuffix(IChatBaseComponent.a(suffix));
            fakeTeam.a(EnumChatFormat.c);//1.17 fakeTeam.setColor(EnumChatFormat.c);
            if (!fakeTeam.g().contains(p.getName())) fakeTeam.g().add(p.getName());
            nmsSb.d(fakeTeam);//1.17 sb.removeTeam(fakeTeam);
           
            TEAMS.put(teamName, fakeTeam);
            final PacketPlayOutScoreboardTeam createTeam = PacketPlayOutScoreboardTeam.a(fakeTeam, true);
            broadcastPacket(createTeam);//addTeamPackets(joining);
            
//Ostrov.log_warn("Created FakeTeam " + fakeTeam.getName() + ". Size: " + TEAMS.size());
            
        }
        CACHED_FAKE_TEAMS.put(p.getName(), fakeTeam);//cache(p.getName(), fakeTeam);

//Ostrov.log_warn(p.getName() + " has been added to team " + fakeTeam.getName());
    }
    
    @Override
    public void resetNametag(final Player p) {
        final ScoreboardTeam fakeTeam = CACHED_FAKE_TEAMS.remove(p.getName());
        if (fakeTeam != null && fakeTeam.g().remove(p.getName())) {

            final PacketPlayOutScoreboardTeam delPlayer = PacketPlayOutScoreboardTeam.a(fakeTeam, p.getName(), PacketPlayOutScoreboardTeam.a.b);
            broadcastPacket(delPlayer);// removePlayerFromTeamPackets(fakeTeam, p.getName());;

//Ostrov.log_warn(p.getName() + " was removed from " + fakeTeam.getName());
            if (fakeTeam.g().isEmpty()) {
                removeTeamPackets(fakeTeam);
                TEAMS.remove(fakeTeam.b());//1.17 TEAMS.remove(fakeTeam.getName());
//Ostrov.log_warn("FakeTeam " + fakeTeam.getName() + " has been deleted. Size: " + TEAMS.size());
            }
        }

        //return fakeTeam;
    }
    
   
    @Override
    public void sendTeams(final Player p) {
        for (ScoreboardTeam fakeTeam : TEAMS.values()) {
            final PacketPlayOutScoreboardTeam createTeam = PacketPlayOutScoreboardTeam.a(fakeTeam, true);
            ((CraftPlayer)p).getHandle().c.a(createTeam);//((CraftPlayer)p).getHandle().b.sendPacket(createTeam);//send(player,packet);
        }
    }
  
    
    private void removeTeamPackets(ScoreboardTeam fakeTeam) {
        final PacketPlayOutScoreboardTeam resetTeam = PacketPlayOutScoreboardTeam.a(fakeTeam);
        broadcastPacket(resetTeam);
    }

    private void broadcastPacket(final Packet<?> packet) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer)p).getHandle().c.a(packet);//((CraftPlayer)player).getHandle().b.sendPacket(packet);//send(player,packet);
        }
    }
    
   // private void send(final Player player, final Packet packet) {
   //     ((CraftPlayer)player).getHandle().b.sendPacket(packet);
  //  }

   
    
    private ScoreboardTeam findFakeTeam(String prefix, String suffix) {
        for (ScoreboardTeam fakeTeam : TEAMS.values()) {
            if (fakeTeam.e().getString().equals(prefix) && fakeTeam.f().getString().equals(suffix)) {
                return fakeTeam;
            }
        }
        return null;
    }
    
    
    
    public static String generateUUID() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return builder.toString();
    }*/
}
