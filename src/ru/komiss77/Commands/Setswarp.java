package ru.komiss77.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Managers.Warps;


public class Setswarp implements CommandExecutor {



    public Setswarp() {}

    
    
@Override                                                                                                                                               //setswarp name desc perm cost     
    public boolean onCommand ( CommandSender se, Command comandd, String cmd, String[] a) {
                
        if (!Warps.use) { se.sendMessage("§4Варпы отключены на этом сервере!"); return true; }
        
        if ( !(se instanceof Player) ) { se.sendMessage("§4Это не консольная команда!"); return false; }
        
        Player p= (Player) se;
        
            if ( !ApiOstrov.isLocalBuilder(p, true) && !p.hasPermission("ostrov.setswarp")) { p.sendMessage("§cУ Вас нет права ostrov.setswarp !"); return false; }
            
            if (a.length !=4 || a.length <4 ) {
                    p.sendMessage("§cФормат: /setswarp <название> <описание> <право> <оплата>");
                    p.sendMessage("§e<право> - 0 - доступно всем, 1-для посещения требуется право ostrow.warp.<название>"); 
                    p.sendMessage("§e<оплата> - стоимость посещения, от 0 до 100000"); 
                    return false;
            }
            
           
            
            if ( a[0].length()<3 ||  a[0].length()>16) {  p.sendMessage("§cНазвание от 4 до 16 символов!"); return false; }
            if (!Warps.checkString(a[0])) {  p.sendMessage( "§cВ названии есть недопустимые символы! §5Допускаются: §e 0-9 A-Z a-z А-Я а-я _ " ); return false; }
            
             if (Warps.Warp_exist(a[0]))  {  p.sendMessage( "§cТакой варп уже существует! " ); return false; }
                
            if ( a[1].length()<8 ||  a[0].length()>32) {  p.sendMessage("§cОписание от 8 до 32 символов! Вместо пробелов используйте _ (он сам заменится на пробел)"); return false; }
            if (!Warps.checkString(a[1])) {  p.sendMessage( "§cВ описании есть недопустимые символы! §5Допускаются: §e 0-9 A-Z a-z А-Я а-я _ " ); return false; }
             
           boolean perm = false; 
           if ( a[2].equals("2") )    perm = true;
           
           int cost = 0;
           if ( CMD.isNumber(a[3]) ) cost = Integer.valueOf( a[3]);
                
            p.sendMessage("§aВы создали §eсерверный §aварп §b"+a[0]+" §aс описанием §7"+a[1].replaceAll("_", " ")+"§a, "+((perm)?"требующий право ostrow.warp."+a[0]:"доступный всем"   )+",  стоимость посещения: §e"+cost );
           Warps.Create_warp(p, a[0], "server", a[1].replaceAll("_", " "), perm, cost);
 
                
        return true;
    }
    
    
    
}
