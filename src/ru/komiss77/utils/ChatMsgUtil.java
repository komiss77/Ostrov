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
/*
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
*/




    
    
    
    
}
