package ru.komiss77.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Managers.Warps;
import ru.komiss77.utils.ChatMsgUtil;


public class Warp implements CommandExecutor {


    public Warp() {}

    
    
    @Override
    public boolean onCommand ( CommandSender se, Command comandd, String cmd, String[] a) {
        
        
        if ( se instanceof ConsoleCommandSender ) {
            
            if (a.length==2) {
                if (Warps.Warp_exist(a[0])) {
                    final Player target = Bukkit.getPlayer(a[1]);
                    if (target!=null) {
                        target.sendMessage( "§6Перемещаемся на "+a[0]+"..." );
                        ApiOstrov.teleportSave(target, Warps.Get_loc(a[0]));
                        return true;
                    } else {
                        se.sendMessage( "§cНе найден игрок "+a[1] );
                    }
                 } else {
                    se.sendMessage( "§cТакого варпа не существует!" );
                }
            }
            
            se.sendMessage( "§cКоманда не выполнена: §e"+cmd );
            return true;
        }
        
        
        
        if ( !(se instanceof Player) ) { 
            se.sendMessage("§4Консоль или игрок!"); 
            return false; 
        }
        
        
        Player p= (Player) se;
                
        if (Warps.сonsoleOnlyUse &&  !ApiOstrov.isLocalBuilder(p, true) ) { 
            se.sendMessage("§4Варпы командой отключены для игроков на этом сервере!");
            return true;
        }
        
        
           // if ( !p.isOp() && !p.hasPermission("ostrov.warp")) { p.sendMessage("§cУ Вас нет пава ostrov.warp !"); return false; } - выключать в настроках если что
            
            
           
           
           
           
           
           
           
           
    switch (a.length) {
            
            
            
            
            case 0:
                p.sendMessage( "" );
                p.sendMessage( "" );
                p.sendMessage( "" );
                
                ChatMsgUtil.Send_TextComponent_onclick_suggest(p, "§e *•.¸¸.•*´¨`*•.¸¸.•*´¨`*•.¸¸.•* Серверные варпы *•.¸¸.•*´¨`*•.¸¸.•*´¨`*•.¸¸.•* ", "", "/warp " );
//TextComponent temp = new TextComponent("§e *•.¸¸.•*´¨`*•.¸¸.•*´¨`*•.¸¸.•* Серверные варпы *•.¸¸.•*´¨`*•.¸¸.•*´¨`*•.¸¸.•* " );
    //head.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_ITEM, new ComponentBuilder("stone").create() ) );
    //temp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/warp ") );
    //if (!Ostrov.v1710) p.spigot().sendMessage(ChatMessageType.CHAT, temp); else p.spigot().sendMessage(temp);
   //p.spigot().sendMessage( temp);    
    
    
    
        if (!Warps.Get_swarps().isEmpty())        {
             ChatMsgUtil.Warp_servers_send(p);
            
         /*   TextComponent swarps = new TextComponent("");
            Warps.Get_swarps().stream().forEach((name) -> {
                TextComponent msg = new TextComponent( "§a"+name+"    " );
                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( 
                    (Warps.Is_open(name)? (Warps.Get_cost(name)==0? "§2✔ Бесплатный":"§4☝ Плата за посещение: "+Warps.Get_cost(name)+" p.") : "§7✖ На ремонте" )+ 
                                "\n§5Владелец: §b"+Warps.Get_owner(name)+
                                "\n§5Описание: §6"+Warps.Get_desc(name)+
                                "\n§5Посещений: §7"+Warps.Get_counter(name)+
                                "\n§5Создан: §7"+Warps.Get_createtime(name)+
                                " \n ").create());
                msg.setHoverEvent( he );
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp "+name ) );
                swarps.addExtra(msg);
                
            });
                p.spigot().sendMessage( swarps);*/
                //if (!Ostrov.v1710) p.spigot().sendMessage(ChatMessageType.CHAT, swarps); else p.spigot().sendMessage(swarps);
                //p.sendMessage( "   §e☝ §f§k0§b клик на название для телепорта §f§k0§e ☝" );
                p.sendMessage( "" );
        }
        
        
        
        
        if (!Warps.Get_pwarps().isEmpty())        {  
            
            
         ChatMsgUtil.Send_TextComponent_onclick_suggest(p, "§e *•.¸¸.•*´¨`*•.¸¸.•*´¨`*•.¸¸.•* Варпы игроков *•.¸¸.•*´¨`*•.¸¸.•*´¨`*•.¸¸.•* ", "", "/warp " );


            //temp = new TextComponent("§e *•.¸¸.•*´¨`*•.¸¸.•*´¨`*•.¸¸.•* Варпы игроков *•.¸¸.•*´¨`*•.¸¸.•*´¨`*•.¸¸.•* " );
            //temp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/warp ") );
            //p.spigot().sendMessage(temp);
            //if (!Ostrov.v1710) p.spigot().sendMessage(ChatMessageType.CHAT, temp); else p.spigot().sendMessage(temp);
             ChatMsgUtil.Warp_vip_send(p);
            
         /*   TextComponent pwarps = new TextComponent("");
                
            Warps.Get_pwarps().stream().forEach((name) -> {
                TextComponent msg = new TextComponent( "§3"+name+"    " );
                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( 
                    (Warps.Is_open(name)? "§2✔ Открыт" : "§6✖ На ремонте" )+ 
                                "\n§5Владелец: §b"+Warps.Get_owner(name)+
                                "\n§5Описание: §6"+Warps.Get_desc(name)+
                                "\n§5Посещений: §7"+Warps.Get_counter(name)+
                                "\n§5Создан: §7"+Warps.Get_createtime(name)+
                                " \n ").create());
                msg.setHoverEvent( he );
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp "+name ) );
                pwarps.addExtra(msg);
            });
            
                p.spigot().sendMessage( pwarps);*/
        }
    
                p.sendMessage( "" );
                p.sendMessage( "   §e ۩ ۩ ۩ §f§k0§b клик на название для телепорта §f§k0§e  ۩ ۩ ۩" );
                p.sendMessage( "" );
                
                p.sendMessage( "§7/warp <название> [on/off/del] §7-быстрый переход, on/off/del - включить/выключить/удалить варп." );
                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.setswarp")) p.sendMessage( "§7/warp <название> cost <сумма> §7- Установить плату для серверного варпа." );
                p.sendMessage( "" );
                break;
              
                
                
                
                
                
                
                
                
            case 1:
                if (Warps.Warp_exist(a[0])) {
                    if (Warps.Is_open(a[0])) {
                        if (!Warps.Need_perm(a[0])) {
                            int cost = Warps.Get_cost(a[0]);
                            if(Warps.Get_loc(a[0])!=null && Warps.Get_loc(a[0]).getWorld()!=null) {
                                if (ApiOstrov.moneyGetBalance(p.getName())>=cost) {
                                    p.sendMessage( "§6Перемещаемся на "+a[0]+"..." );
                                    if (cost>0) ApiOstrov.moneyChange(p, -cost, "warp");
                                    Warps.Add_count(p, a[0]);
                                    ApiOstrov.teleportSave(p, Warps.Get_loc(a[0]));
                                } else p.sendMessage( "§cУ Вас недостаточно денег для посещения! Нужно: "+cost+" p." );
                            } else p.sendMessage( "§cЛокация варпа недоступна: "+a[0] );
                        } else p.sendMessage( "§cДля посещения данного варпа требуется право ostrov.warp."+a[0] );
                    } else p.sendMessage( "§cВарп закрыт на ремонт!" );
                } else p.sendMessage( "§cТакого варпа не существует!" );
                break;
                
                
                
                
                
                
                
                
                
            case 2:
                if (Warps.Warp_exist(a[0])) {
                    if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.setswarp") || Warps.Get_owner(a[0]).equals(p.getName()) ) {
                        switch (a[1]) {
                            case "on":
                            case "off":
                                boolean on = true;
                                if (a[1].equals("off")) on = false;
                                
                                Warps.Set_open (p, a[0], on);
                                if (on) p.sendMessage( "§aВы открыли доступ к варпу!" );
                                else p.sendMessage( "§4Вы закрыли доступ к варпу!" );
                                return true;
                            case "del":
                                Warps.Del_warp(p, a[0]);
                                p.sendMessage( "§aВы удалили варп "+a[0] );
                                return true;
                            default:
                                p.sendMessage( "§con - открыт, off - заблокировать." );
                                break;
                        }
                    } else p.sendMessage( "§cВы не владелец данного варпа!" );
                } else p.sendMessage( "§cТакого варпа не существует!" );
                break;
              
                
                
                
            case 3:
                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.setswarp")) { 
                    if (Warps.Warp_exist(a[0])) {
                        if ( Warps.Get_type(a[0]).equals("server") ) {
                            if (a[1].equals("cost") ) {
                                int cost = 0;
                                if ( CMD.isNumber(a[2]) ) cost = Integer.valueOf( a[2]);
                                if (cost <0 || cost > 100000) {  p.sendMessage( "§cЦена от 0 до 100000" ); return false; }

                                Warps.Set_cost (p, a[0], cost);
                                p.sendMessage( "§aДля варпа "+a[0]+" Вы установили плату за посещение "+cost );

                            } else p.sendMessage( "§c/warp <название> cost сумма - установить плату за посещение!" );
                        } else p.sendMessage( "§cЦену можно установить только для серверных варпов!" );
                    } else p.sendMessage( "§cТакого варпа не существует!" );
                } else p.sendMessage("§cУ Вас нет права управлять серверными варпами!");
                break;
                
                
        }
            
             

                
        return true;
    }
    
    
    
    
    
    
  
    
    
    
    
    
    
    
    
}
