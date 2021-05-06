package ru.komiss77.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.Warps;


public class Setwarp implements CommandExecutor {



    public Setwarp() {}

    
    
@Override                                                                                                                                               //setswarp name desc perm cost     
    public boolean onCommand ( CommandSender se, Command comandd, String cmd, String[] a) {
                
        if (!Warps.use) { se.sendMessage("§4Варпы отключены на этом сервере!"); return true; }
        
        if ( !(se instanceof Player) ) { se.sendMessage("§4Это не консольная команда!"); return false; }
        
        Player p= (Player) se;
        
            if ( !ApiOstrov.isLocalBuilder(p, true) && !p.hasPermission("ostrov.setwarp")) { p.sendMessage("§cУ Вас нет пава ostrov.setwarp !"); return false; }
            
            if (a.length !=2 ) {
                    p.sendMessage("§cФормат: /setswarp <название> <описание>");
                    return false; 
            }
            
           
            
            if ( a[0].length()<3 ||  a[0].length()>16) {  p.sendMessage("§cНазвание от 4 до 16 символов! Вместо пробелов используйте _ (он сам заменится на пробел)"); return false; }
            if (!Warps.checkString(a[0])) {  p.sendMessage( "§cВ названии есть недопустимые символы! §5Допускаются: §e 0-9 A-Z a-z А-Я а-я _ " ); return false; }
            
             if (Warps.Warp_exist(a[0]))  {  p.sendMessage( "§cТакой варп уже существует! " ); return false; }
             
             int  limit = 0;
            if (!PM.getOplayer(p.getName()).hasAnyGroup()) {                             //вычисление лимита
                if (Warps.warps_per_group.containsKey("default")) limit = Warps.warps_per_group.get("default");
            } else {
                for ( String gr : PM.getOplayer(p.getName()).getGroups()) {
                   if ( Warps.warps_per_group.containsKey(gr) && Warps.warps_per_group.get(gr)>limit ) limit = Warps.warps_per_group.get(gr); 
                }
            } 
//System.out.println("Лимит "+limit+" Сколько_варпов"+Warps.Сколько_варпов(p.getName()));            
            if ( Warps.Warp_ammount(p.getName()) >=limit ) { p.sendMessage( "§cВы не можете добавить еще один варп! Лимит Вашей группы: "+limit);return false; }
                
            if ( a[1].length()<8 ||  a[0].length()>32) {  p.sendMessage("§cОписание от 8 до 32 символов!"); return false; }
            if (!Warps.checkString(a[1])) {  p.sendMessage( "§cВ описании есть недопустимые символы! §5Допускаются: §e 0-9 A-Z a-z А-Я а-я _ " ); return false; }
             
            
           //int cost = 0;
           //if ( CMD.isNumber(a[3]) ) cost = Integer.valueOf( a[3]);
           //if (cost <0 || cost > 100000) {  p.sendMessage( "§cЦена от 0 до 100000" ); return false; }
                
           p.sendMessage("§aВы создали §eличный §aварп §b"+a[0]+" §aс описанием §7"+a[1].replaceAll("_", " ") );
           p.sendMessage("§aВы можете открывать/закрывть его командой §e/warp "+a[0]+" on/off" );
           Warps.Create_warp(p, a[0], "player", a[1].replaceAll("_", " "), false, 0);
 
                
        return true;
    }
    
    
    
}
