package ru.komiss77.Commands;


import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.komiss77.Managers.PM;





public class StatAdd implements Listener,CommandExecutor {
    

    public StatAdd() {
        //init();
    }
    
    private void help (final CommandSender cs) {
        cs.sendMessage("");
        cs.sendMessage("§3/"+this.getClass().getSimpleName()+" statadd bw_wi");
        cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
        cs.sendMessage("§fПримеры:");
        cs.sendMessage("§a/statadd komiss77 bw_game,bw_win,bw_bed:5");
        cs.sendMessage("§a/statadd komiss77 sg_game,sg_kill:5");
        cs.sendMessage("");
    }

    
    //reward <ник>  <тип_награды> <параметр>           <колл-во>  <источник> 
    //reward komiss77 money          add                   1000     ostrov
    //reward komiss77 money          get                rnd:0:100   ostrov
    //reward komiss77 permission serwer.world.perm.aaa     100      ostrov
    //reward komiss77 permission   perm.aaa             forever      ostrov
    //reward komiss77 group          vip                   10       ostrov
    //reward komiss77 group          vip                 forever       ostrov
    //reward komiss77 exp            add                   1000     ostrov
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( cs instanceof Player && !cs.isOp() ) {
            cs.sendMessage("");
            cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
            cs.sendMessage("");
            return false;
        }
        
        

        if (arg.length!=2) {
            help(cs);
            return false;
        }
        

        final String name = arg[0];
        if (!PM.exist(name)) {
            cs.sendMessage("§eИгрока "+name+" нет на локальном сервере.");
            //return false;
        }
        
        final String stat_raw = arg[1];
        
System.out.print("StatAdd name="+name+" stats="+stat_raw);
        
//System.out.println(Action.OSTROV_REWARD+", "+for_name+", "+type.toString()+", "+param+", "+(forever?true:ammount)+", "+sender);
    


        
        //ApiOstrov.sendMessage(sender, Action.OSTROV_REWARD, for_name+":"+type.toString()+":"+param+":"+(forever?"forever":ammount));
        
        return true;

    }
    



    

    
    
    
    
    
    
    
    
    


}
    
    
 