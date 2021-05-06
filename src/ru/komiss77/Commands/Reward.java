package ru.komiss77.Commands;


import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Enums.Action;
import ru.komiss77.Enums.RewardType;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.SM;
import ru.komiss77.Ostrov;





public class Reward implements Listener,CommandExecutor {
    

    public Reward() {
        //init();
    }
    
    private void help (final CommandSender cs) {
        cs.sendMessage("");
        cs.sendMessage("§3/"+this.getClass().getSimpleName()+" reward <ник> <тип_награды> <параметр> <колл-во> <источник>");
        cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
        cs.sendMessage("§fПримеры:");
        cs.sendMessage("§a/reward komiss77 money add 1000 ostrov");
        cs.sendMessage("§a/reward komiss77 money get rnd:0:100 plugin");
        cs.sendMessage("§a/reward komiss77 permission serwer.world.perm.aaa 100 ostrov");
        cs.sendMessage("§a/reward komiss77 permission perm.aaa forever ostrov");
        cs.sendMessage("§a/reward komiss77 group vip 10 ostrov");
        cs.sendMessage("§a/reward komiss77 group vip forever ostrov");
        cs.sendMessage("§a/reward komiss77 exp add rnd:500:10000 ostrov");
        cs.sendMessage("§a/reward komiss77 reputation get rnd:-5:5 ostrov");
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
        
        if ( cs instanceof Player && !cs.isOp() ) {
            cs.sendMessage("");
            cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
            cs.sendMessage("");
            return false;
        }
        
        
//System.out.print("Eco cs="+cs);

        if (arg.length!=5) {
            help(cs);
            return false;
        }
        
        RewardType type = RewardType.fromString(arg[1]);
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
                ammount = ammount;
            } else if (param.equals("get")) {
                ammount = -(ammount);
            } else {
                cs.sendMessage("§eДля награды типа "+type.toString()+" допустимы параметры add или get");
                return false;
            }
        }
        
        
        String sender = arg[4];
        sender = SM.this_server_name+"."+sender;
        if (sender.length()>16) {
            sender = sender.substring(0, 15);
            cs.sendMessage("§eПревышена длина источника, обрезано до "+sender);
        }
        
        final String for_name = arg[0];
        if (!PM.exist(for_name)) {
            cs.sendMessage("§eИгрока "+for_name+" нет на локальном сервере. Проверим на прокси.");
            //return false;
        }
        
System.out.println(Action.OSTROV_REWARD+", "+for_name+", "+type.toString()+", "+param+", "+(forever?true:ammount)+", "+sender);
    
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

        
        ApiOstrov.sendMessage(sender, Action.OSTROV_REWARD, for_name+":"+type.toString()+":"+param+":"+(forever?"forever":ammount));
        
        return true;

    }
    



    

    
    
    
    
    
    
    
    
    


}
    
    
 