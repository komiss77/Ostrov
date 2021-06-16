package ru.komiss77.Commands;


import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Enums.Action;
import ru.komiss77.Enums.Data;
import ru.komiss77.Enums.RewardType;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.SM;
import ru.komiss77.Objects.Group;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.OstrovDB;





public class Reward implements CommandExecutor, TabCompleter {
    

    
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] strings) {
        final List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);
        switch (strings.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().startsWith(strings[0])) sugg.add(p.getName());
                }
                break;

            case 2:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                //if (strings[0].equalsIgnoreCase("build") || strings[0].equalsIgnoreCase("destroy") ) {
                    sugg.add("money");
                    sugg.add("permission");
                    sugg.add("group");
                    sugg.add("exp");
                    sugg.add("reputation");
                //}
                break;
                
            case 3:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                if (strings[1].equalsIgnoreCase("group") ) {
                    for (final Group g:OstrovDB.groups.values()) {
                        if (!g.isStaff() && g.name.startsWith(strings[2])) sugg.add(g.name);
                    }
                    //sugg.addAll(OstrovDB.groups.keySet());
                } else if (strings[1].equalsIgnoreCase("money") || strings[1].equalsIgnoreCase("exp") || strings[1].equalsIgnoreCase("reputation"))  {
                    sugg.add("add");
                    sugg.add("get");
                } else if (strings[1].equalsIgnoreCase("permission") ) {
                    sugg.add("ostrov.perm");
                    sugg.add(Bukkit.getServer().getMotd()+".builder");
                }
                break;

            case 4:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                //if (strings[0].equalsIgnoreCase("build") || strings[0].equalsIgnoreCase("destroy") ) {
                if (strings[1].equalsIgnoreCase("group") ||strings[1].equalsIgnoreCase("permission")  ) {
                    sugg.add("1h");
                    sugg.add("10h");
                    sugg.add("1d");
                    sugg.add("7d");
                    sugg.add("30d");
                    sugg.add("forever");
                } else if (strings[1].equalsIgnoreCase("money") || strings[1].equalsIgnoreCase("exp") || strings[1].equalsIgnoreCase("reputation"))  {
                    sugg.add("10");
                    sugg.add("100");
                    sugg.add("1000");
                    sugg.add("rnd:0:100");
                }
                //}
                break;
        }
        
       return sugg;
    }    
    



    public Reward() {
        //init();
    }
    
    private void help (final CommandSender cs) {
        cs.sendMessage("");
        cs.sendMessage("§3/"+this.getClass().getSimpleName()+" reward <ник> <тип_награды> <параметр> <колл-во> <причина>");
        cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
        cs.sendMessage("§fПримеры:");
        cs.sendMessage("§a/reward komiss77 money add 1000");
        cs.sendMessage("§a/reward komiss77 money get rnd:0:100");
        cs.sendMessage("§a/reward komiss77 permission serwer.world.perm.aaa 1h");
        cs.sendMessage("§a/reward komiss77 permission perm.aaa forever");
        cs.sendMessage("§a/reward komiss77 group vip 10d");
        cs.sendMessage("§a/reward komiss77 group vip forever");
        cs.sendMessage("§a/reward komiss77 exp add rnd:500:10000");
        cs.sendMessage("§a/reward komiss77 reputation get rnd:-5:5");
        cs.sendMessage("§a");
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
        
        if ( cs instanceof Player && !ApiOstrov.hasGroup(cs.getName(), "supermoder") ) {
            cs.sendMessage("");
            cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/supermoder!");
            cs.sendMessage("");
            return false;
        }
        
        
//System.out.print("Eco cs="+cs);

        if (arg.length<4) {
            help(cs);
            return false;
        }
        
        Data d = Data.fromName(arg[1]);//RewardType type = RewardType.fromString(arg[1]);
        if (type==RewardType.NONE) {
            cs.sendMessage("§cНет награды типа "+arg[1]+". §7Доступные: §a"+RewardType.possibleValues());
            return false;
        }
        
        String param = arg[2];
        if (param.length()>64) {
            param = param.substring(0, 63);
            cs.sendMessage("§eПревышена длина параметра, обрезано до "+param);
        }        
        
        //обработка колличества
        String amm = arg[3];
        
        if (amm.endsWith("h")) {
            amm = amm.replaceFirst("h","");
        } else if (amm.endsWith("d")) {
            amm = amm.replaceFirst("d","");
             if (ApiOstrov.isInteger(amm)) {
                 amm = String.valueOf(Integer.valueOf(amm)*24);
             }
        }
        int ammount=0;
        boolean forever = false;
        
        if (ApiOstrov.isInteger(amm)) {
            
            ammount = Integer.valueOf(amm);
            
        } else {
            if (amm.startsWith("rnd:")) {
                
                String[] split = amm.split(":");
                if (split.length!=3) {
                    cs.sendMessage("§cПри указании случайного значения формат rnd:min:max");
                    return false;
                }
                if ( !ApiOstrov.isInteger(split[1]) || !ApiOstrov.isInteger(split[2]) ) {
                    cs.sendMessage("§cПри указании случайного значения min и max - челые числа");
                    return false;
                }
                
                ammount = ApiOstrov.randInt(Integer.valueOf(split[1]), Integer.valueOf(split[2]));

            } else if (amm.equalsIgnoreCase("forever") && (type==RewardType.PERMISSION || type==RewardType.GROUP) ) {
                forever = true;
            } else {
                cs.sendMessage("§eКолличество - целое положительное число!");
                return false;
            }
            
        }
        
        if (ammount==0 && !forever) {
            cs.sendMessage("§cНет смысла выдавать пустую награду");
            return false;
        }

        if (type.is_integer) {
            if (ammount<0) {
                cs.sendMessage("§eКолличество - целое положительное число!");
                return false;
            }
            if (param.equals("add")) {
                //ammount = ammount;
            } else if (param.equals("get")) {
                ammount = -(ammount);
            } else {
                cs.sendMessage("§eДля награды типа "+type.toString()+" допустимы параметры add или get");
                return false;
            }
        }
        
        
        String cause = SM.this_server_name+".";
        if (cs instanceof Player) {
            cause = cause+cs.getName();
        } else if (arg.length>=5) {
            cause = cause+arg[4];
        }
        //cause = SM.this_server_name+"."+cause;
        if (cause.length()>16) {
            cause = cause.substring(0, 15);
            cs.sendMessage("§eПревышена длина источника, обрезано до "+cause);
        }
        
        final String for_name = arg[0];
        if (!PM.exist(for_name)) {
            cs.sendMessage("§eИгрока "+for_name+" нет на локальном сервере. Проверим на прокси.");
            //return false;
        }
        
System.out.println(Action.REWARD+", "+for_name+", "+type.toString()+", "+param+", "+(forever?true:ammount)+", "+cause);
    
        //обработчик часть тут, чать на банжи
        switch (type) {
            
            case MONEY:
                //банжи, отправка сообщения ниже
                break;
                
            case PERMISSION:
                //банжи, отправка сообщения ниже
                if (param.startsWith("bauth") || param.startsWith("staff") || param.startsWith("group") || param.startsWith("money")) {
                    cs.sendMessage("§eВы не можете награждать административными правами! Запись о попыте сохранена в логе.");
                    Ostrov.log_err("попытка выдачи административных прав: "+cs.getName()+" -> "+param);
                    return false;
                }
                break;
                
            case GROUP:
                //банжи, отправка сообщения ниже
                break;
                
            case EXP:
                ApiOstrov.addXP(PM.getOplayer(for_name).getPlayer(), ammount);
                return true; //возврат, или зацепит обработчик банжи
                
            case REPUTATION:
                //банжи, отправка сообщения ниже
                break;
                
            case KARMA:
                //банжи, отправка сообщения ниже
                break;
            
        }

        
        //ApiOstrov.sendMessage(cause, Action.REWARD, for_name+":"+type.toString()+":"+param+":"+(forever?"forever":ammount));
        ApiOstrov.sendMessage(cs, Action.REWARD, d.tag, 0, (forever?Integer.MAX_VALUE:ammount), for_name, param );
        
        return true;

    }
    



    

    
    
    
    
    
    
    
    
    


}
    
    
 