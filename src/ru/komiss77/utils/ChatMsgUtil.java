package ru.komiss77.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;

import ru.komiss77.Commands.CMD;
import ru.komiss77.Managers.Warps;
import ru.komiss77.Ostrov;



public class ChatMsgUtil {
 
    
  
 


    public static void Help (Player p, int page ) {

        for (int i=0; i<20; i++) {
            p.sendMessage("");
        }

        int limit = CMD.ostrov_commands.size();  

        int from = page*15;
        if (from > limit) {
            p.sendMessage("§cСтраниц всего "+(int)limit/15);
            return;
        }
        int to = from+15;
        if (to> limit) to = limit;

        TextComponent msg;

            if (page == 0) {
                p.sendMessage( "§2Помощь по командам Острова." );
            } else {
                msg = new TextComponent( "§eПредыдущая страница - клик сюда" );
                //msg.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§fПобеды: §e"+d.wins).append(" §fФраги: §e"+d.kills).append(" §fИгры: §e"+d.gamesPlayed).append(" §fПроигрыши: §e"+d.deaths).create() ) );
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help "+(page-1) ) );
                //p.spigot().sendMessage(ChatMessageType.CHAT, msg);
                p.spigot().sendMessage( msg);
            }


                String cmd;

                for (int i=from; i<to; i++) {
                    cmd = CMD.ostrov_commands.get(i);
                    msg = new TextComponent( 
                            "§a§l"+cmd+" §f- "+Ostrov.instance.getDescription().getCommands().get(cmd).get("description").toString()
                                    .replaceFirst("<vip>", "§3(привилегия)")
                                    .replaceFirst("<moder>", "§3(модерская)")+" §8<- клик - набрать" );
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/"+cmd+" " ) );
                    //p.spigot().sendMessage(ChatMessageType.CHAT, msg);
                    p.spigot().sendMessage( msg);
                }

            //p.sendMessage("§b* §3- требуют привилегии, §b** §3- модераторские");
            if (to<limit) {
                msg = new TextComponent( "§eСледующая страница - клик сюда" );
                //msg.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§fПобеды: §e"+d.wins).append(" §fФраги: §e"+d.kills).append(" §fИгры: §e"+d.gamesPlayed).append(" §fПроигрыши: §e"+d.deaths).create() ) );
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help "+(page+1) ) );
                //p.spigot().sendMessage(ChatMessageType.CHAT, msg);
                p.spigot().sendMessage( msg);
            }










                //if (Conf.block_commands) p.sendMessage("§aРазрешенные команды: "+Conf.block_commands_except.toString().replaceAll("\\[|\\]|\\s", " §5") );
              /*  p.sendMessage("§aВаши команды: "+ServerListener.block_commands_except.toString().replaceAll("\\[|\\]|\\s", " §5") );

                p.sendMessage("§5/menu §7- открыть главне меню");
                p.sendMessage("§5/money §7- ваш баланс");
                p.sendMessage("§5/money <ник>§7- баланс игрока <ник>");
                p.sendMessage("§5/money send <ник> <сумма> §7- переслать деньги");
                p.sendMessage("§5/prefix <строка> §7- установить префикс");

                //if ( p.isOp() || p.hasPermission("ostrov.money.give") ) p.sendMessage("§5/money give <ник> <сумма> §7- выдать деньги");
                if ( p.isOp() || p.hasPermission("ostrov.pinfo") ) p.sendMessage("§5/pinfo <ник>§7- информация об игроке");

                if (p.isOp()) {
                    p.sendMessage("§5/oreload §7- reload config");
                    p.sendMessage("§5/okill <radius> §7- kill entity in radius");
                    }
    */
    }

    public static void Send_TextComponent_onclick_run (Player target, String text, String hover, String click) {
        TextComponent msg = new TextComponent( text );
        msg.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create() ) );
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, click ) );
        target.spigot().sendMessage(msg);
                                
    }

    public static void Send_TextComponent_onclick_suggest (Player target, String text, String hover, String click) {
        TextComponent msg = new TextComponent( text );
        msg.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create() ) );
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, click ) );
        target.spigot().sendMessage(msg);
                                
    }

    public static void Warp_servers_send( Player p ) {

                TextComponent swarps = new TextComponent("");
                Warps.Get_swarps().stream().forEach((name) -> {
                    TextComponent msg = new TextComponent( "§a"+name+"    " );
                    HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( 
                        (Warps.Is_open(name)? (Warps.Get_cost(name)==0? "§2✔ Бесплатный":"§4☝ Плата за посещение: "+Warps.Get_cost(name)+" p.") : "§7✖ На ремонте" )+ 
                                    "\n§5Владелец: §b"+Warps.Get_owner(name)+
                                    "\n§5Описание: §6"+Warps.Get_desc(name)+
                                    "\n§5Посещений: §7"+Warps.Get_counter(name)+
                                    "\n§5Создан: §7"+ApiOstrov.dateFromStamp(Warps.Get_createStamp(name))+
                                    " \n ").create());
                    msg.setHoverEvent( he );
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp "+name ) );
                    swarps.addExtra(msg);

                });
                    p.spigot().sendMessage( swarps);
        }

    public static void Warp_vip_send( Player p ) {

               TextComponent pwarps = new TextComponent("");
               Warps.Get_pwarps().stream().forEach((name) -> {
                   TextComponent msg = new TextComponent( "§3"+name+"    " );
                   HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( 
                       (Warps.Is_open(name)? "§2✔ Открыт" : "§6✖ На ремонте" )+ 
                                   "\n§5Владелец: §b"+Warps.Get_owner(name)+
                                   "\n§5Описание: §6"+Warps.Get_desc(name)+
                                   "\n§5Посещений: §7"+Warps.Get_counter(name)+
                                   "\n§5Создан: §7"+ApiOstrov.dateFromStamp(Warps.Get_createStamp(name))+
                                   " \n ").create());
                   msg.setHoverEvent( he );
                   msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp "+name ) );
                   pwarps.addExtra(msg);
               });

                   p.spigot().sendMessage( pwarps);
    }
    




    
    
    
    
    
     /*
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


 
 
 
 
 
 
 
 
 
 
  
 /*
 
@Deprecated    
    public static void sendActionBar(Player p, String msg) {
            //CraftPlayer player = (CraftPlayer) p;
            IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', msg) + "\"}");
            PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, ChatMessageType.GAME_INFO);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }

 
@Deprecated  
    public static void sendTitle(Player p, String title, String subtitle, int fadein, int stay, int fadeout) {
        
        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
        PacketPlayOutTitle packetPlayOutTimes = new PacketPlayOutTitle(EnumTitleAction.TIMES, (IChatBaseComponent) null, fadein, stay, fadeout );
        connection.sendPacket(packetPlayOutTimes);
        IChatBaseComponent titleMain;
        PacketPlayOutTitle packetPlayOutTitle;
//System.out.println(">>>>>>>>>>>> sendTitle "+title+"   "+subtitle);
        if (subtitle != null && !subtitle.isEmpty()) {
            titleMain = ChatSerializer.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', subtitle )  + "\"}");
            packetPlayOutTitle = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, titleMain);
            connection.sendPacket(packetPlayOutTitle);
        }

        if (title != null && !title.isEmpty()) {
            titleMain = ChatSerializer.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', title )  + "\"}");
            packetPlayOutTitle = new PacketPlayOutTitle(EnumTitleAction.TITLE, titleMain);
            connection.sendPacket(packetPlayOutTitle);
        }

    }

    */
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
/*
    public static int getPlayerPing(Player p) {
        return ((CraftPlayer) p).getHandle().ping;
    }

    }

    
    
    //FIX!!
    public static void sendNameTag(final String nik, final String prefix, final String suffix) {
        
        String name = UUID.randomUUID().toString().substring(0, 16);
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
        Class clas = packet.getClass();
        
        Field team_name = getField(clas, "a");
        Field display_name = getField(clas, "b"); //IChatBaseComponent
        Field prefix1 = getField(clas, "c"); //IChatBaseComponent
        Field suffix1 = getField(clas, "d"); //IChatBaseComponent
        Field members = getField(clas, "h"); //Collection h
        Field param_int = getField(clas, "i"); //int i
        Field pack_option = getField(clas, "j"); //int j

        setField(packet, team_name, name);
        //setField(packet, display_name, nik);
        //setField(packet, prefix1, prefix);
        //setField(packet, suffix1, suffix);
        setField(packet, display_name, ChatSerializer.a("{\"text\": \"" + nik + "\"}"));
        setField(packet, prefix1, ChatSerializer.a("{\"text\": \"" + prefix + "\"}"));
        setField(packet, suffix1, ChatSerializer.a("{\"text\": \"" + suffix + "\"}"));
        setField(packet, members, Arrays.asList(new String[] { nik}));
        setField(packet, param_int, 0);
        setField(packet, pack_option, 1);
        
        Bukkit.getOnlinePlayers().stream().forEach( (i) -> {
            ((CraftPlayer) i).getHandle().playerConnection.sendPacket(packet);
        });


    
    public static void sendBelowName(Player p, String belowname, int score) {
        final Scoreboard scoreboard = new Scoreboard();
        final ScoreboardObjective sobjective = getScoreboardObj(scoreboard, belowname);
        //ScoreboardScore sscore = getScoreboardScore(scoreboard, sobjective, p.getName(), score); 1.12
        //PacketPlayOutScoreboardScore scorepacket = new PacketPlayOutScoreboardScore(sscore); 1.12
        PacketPlayOutScoreboardScore scorepacket = new PacketPlayOutScoreboardScore(Action.CHANGE, belowname, p.getName(), score);
        PacketPlayOutScoreboardObjective objpacket = new PacketPlayOutScoreboardObjective(sobjective, 0);
        PacketPlayOutScoreboardDisplayObjective dobjpacket = new PacketPlayOutScoreboardDisplayObjective(2, sobjective);
        
        Bukkit.getOnlinePlayers().stream().forEach((i) -> {
            ((CraftPlayer)i).getHandle().playerConnection.sendPacket(objpacket);
            ((CraftPlayer)i).getHandle().playerConnection.sendPacket(dobjpacket);
            ((CraftPlayer)i).getHandle().playerConnection.sendPacket(scorepacket);
        });

        scoreboard.unregisterObjective(sobjective);
        //scoreboard=null;
    }

    
    public static void clearBelowName() {
        Scoreboard scoreboard = new Scoreboard();
        String name = UUID.randomUUID().toString().substring(0, 16);
        ScoreboardObjective sobjective = getScoreboardObj(scoreboard, name);
        
        PacketPlayOutScoreboardDisplayObjective dobjpacket = new PacketPlayOutScoreboardDisplayObjective(2, sobjective);
        Bukkit.getOnlinePlayers().stream().forEach((player) -> {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(dobjpacket);
        });
        
        scoreboard.unregisterObjective(sobjective);
        scoreboard=null;
    }


    private static ScoreboardObjective getScoreboardObj(Scoreboard scoreboard, String displayname) {
        ScoreboardObjective objective = new ScoreboardObjective(scoreboard, subString(displayname), IScoreboardCriteria.DUMMY, null, null); //1.12
        //ScoreboardObjective objective = scoreboard.registerObjective("obj", IScoreboardCriteria.DUMMY, null, null);
        //objective.setDisplayName(displayname); 1.12
        IChatBaseComponent text = new ChatMessage(displayname);
        objective.setDisplayName(text);
        scoreboard.a(displayname, objective);
        return objective;
    }

    private static ScoreboardScore getScoreboardScore(Scoreboard scoreboard, ScoreboardObjective obj, String playername, int score) {
        ScoreboardScore sscore = new ScoreboardScore(scoreboard, obj, playername);
        sscore.setScore(score);
        return sscore;
    }

    private static String subString(String message) {
        if (message.length() > 16) message = message.substring(0, 16);
        return message;
    }
    
    
    
    

    public static void sendPingTab(Player p, int score) {
        Scoreboard scoreboard = new Scoreboard();
        ScoreboardObjective sobjective = getScoreboardObj(scoreboard, "_pingtab");
        ScoreboardScore sscore = getScoreboardScore(scoreboard, sobjective, p.getName(), score);
        
        //PacketPlayOutScoreboardScore scorepacket = new PacketPlayOutScoreboardScore(sscore); 1.12
        PacketPlayOutScoreboardScore scorepacket = new PacketPlayOutScoreboardScore(Action.CHANGE, sobjective.getName(), p.getName(), score);
        PacketPlayOutScoreboardObjective objpacket = new PacketPlayOutScoreboardObjective(sobjective, 0);
        PacketPlayOutScoreboardDisplayObjective dobjpacket = new PacketPlayOutScoreboardDisplayObjective(0, sobjective);
        
        Bukkit.getOnlinePlayers().stream().forEach((i) -> {
            ((CraftPlayer)i).getHandle().playerConnection.sendPacket(objpacket);
            ((CraftPlayer)i).getHandle().playerConnection.sendPacket(dobjpacket);
            ((CraftPlayer)i).getHandle().playerConnection.sendPacket(scorepacket);
        });

        scoreboard.unregisterObjective(sobjective);
        scoreboard=null;
    }

    
    public static void clearPingTab() {
        Scoreboard scoreboard = new Scoreboard();
        String name = UUID.randomUUID().toString().substring(0, 16);
        ScoreboardObjective sobjective = getScoreboardObj(scoreboard, name);
        
        PacketPlayOutScoreboardDisplayObjective dobjpacket = new PacketPlayOutScoreboardDisplayObjective(0, sobjective);
        
        Bukkit.getOnlinePlayers().stream().forEach((player) -> {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(dobjpacket);
        });

        scoreboard.unregisterObjective(sobjective);
        scoreboard=null;
    }
*/

    
    
    
    
    
}
