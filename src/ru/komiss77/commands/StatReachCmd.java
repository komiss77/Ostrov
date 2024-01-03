package ru.komiss77.commands;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Stat;

import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.mission.MissionManager;





public class StatReachCmd implements Listener, CommandExecutor, TabCompleter {
    

    public StatReachCmd() {
        //init();
    }
    
    
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] arg) {
        
        List <String> sugg = new ArrayList<>();
        switch (arg.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().startsWith(arg[0])) sugg.add(p.getName());
                }
                break;

            case 2:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                //if (strings[0].equalsIgnoreCase("build") || strings[0].equalsIgnoreCase("destroy") ) {
                    for (final Stat st : Stat.values()) {
                        if (st.name().toLowerCase().startsWith(arg[1].toLowerCase()) ) sugg.add(st.name());
                    }
                    for (final String name : MissionManager.customStatsDisplayNames.keySet()) {
                        if (name.toLowerCase().startsWith(arg[1].toLowerCase()) ) sugg.add(name);
                    }
                    sugg.add("локальная");
                //}
                break;
                
            case 3:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                //if (strings[0].equalsIgnoreCase("build") || strings[0].equalsIgnoreCase("destroy") ) {
                    for (int i=1; i<=10; i++) {
                        sugg.add(String.valueOf(i));
                    }

                //}
                break;
        }
        
       return sugg;
    }


    private void help (final CommandSender cs) {
        cs.sendMessage("");
        //cs.sendMessage("§3/"+this.getClass().getSimpleName()+" statadd bw_wi");
        cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
        cs.sendMessage("§fПримеры:");
        cs.sendMessage("§a/statreach komiss77 bw_game 5");
        //cs.sendMessage("§a/statadd komiss77 sg_game,sg_kill:5");
        cs.sendMessage("");
    }


    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( cs instanceof Player && !cs.isOp() ) {
            cs.sendMessage("");
            cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
            cs.sendMessage("");
            return false;
        }
        
        

        if (arg.length!=3) {
            help(cs);
            return false;
        }
        

        final String name = arg[0];
        //final Oplayer op = PM.getOplayer(arg[0]);
        if (!PM.exist(name)) {
            cs.sendMessage("§eИгрока "+name+" нет на локальном сервере.");
            return false;
        }
        
        final int value = ApiOstrov.getInteger(arg[2]);
        if (value<0 || value>100000) {
            cs.sendMessage("§eЗначение допустимо от 1 до 100000!");
            return false;
        }
        
        //final Stat st = Stat.fromName(arg[1]);
        //if (st==null) {
            ApiOstrov.reachCustomStat(Bukkit.getPlayer(name), arg[1], value);
            cs.sendMessage("§7Достижение статистики §e"+arg[1]+" §7(customStat) значения §e"+value+" §7для §f"+name+" §7передано менеджеру миссиий.");
            //cs.sendMessage("§eНет статистики "+arg[1]+"!");
            //return false;
        //} else {
        //    ApiOstrov.reachCustomStat(Bukkit.getPlayer(name), st, value);
       //     cs.sendMessage("§7Достижение статистики §e"+arg[1]+" §7(customStat) значения §e"+value+" §7для §f"+name+" §7передано менеджеру миссиий.");
       //     cs.sendMessage("§7Статистика §b"+st+" §7увеличена на §e"+value+" §7для §f"+name);
       // }
                
        
        //cs.sendMessage("§7Статистика §b"+st+" §7увеличена на §e"+value+" §7для §f"+name);
        //final String stat_raw = arg[1];
        
//System.out.print("StatAdd name="+name+" stats="+stat_raw);
        
//System.out.println(Action.OSTROV_REWARD+", "+for_name+", "+type.toString()+", "+param+", "+(forever?true:ammount)+", "+sender);
    


        
        //ApiOstrov.sendMessage(sender, Action.OSTROV_REWARD, for_name+":"+type.toString()+":"+param+":"+(forever?"forever":ammount));
        
        return true;

    }
    



    

    
    
    
    
    
    
    
    
    


}
    
    
 
