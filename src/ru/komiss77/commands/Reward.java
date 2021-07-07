package ru.komiss77.commands;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.RewardType;
import ru.komiss77.modules.games.GM;
import ru.komiss77.objects.Group;
import ru.komiss77.OstrovDB;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.StatManager;





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
                for (RewardType rt : RewardType.values()) {
                    sugg.add(rt.name().toLowerCase());
                }
                 //   sugg.add("loni");
                //    sugg.add("permission");
                 //   sugg.add("group");
                  //  sugg.add("exp");
                  //  sugg.add("reputation");
                ///}
                break;
                
            case 3:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                if (strings[1].equalsIgnoreCase("group") ) {
                    for (final Group g:OstrovDB.groups.values()) {
                        if (!g.isStaff() && g.name.startsWith(strings[2])) sugg.add(g.name);
                    }
                    //sugg.addAll(OstrovDB.groups.keySet());
                }  else if (strings[1].equalsIgnoreCase("permission") ) {
                    sugg.add("ostrov.perm");
                    sugg.add(Bukkit.getServer().getMotd()+".builder");
                } else { //if (strings[1].equalsIgnoreCase("loni") || strings[1].equalsIgnoreCase("loni") || strings[1].equalsIgnoreCase("exp") || strings[1].equalsIgnoreCase("reputation"))  {
                    sugg.add("add");
                    sugg.add("get");
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
                } else {//if (strings[1].equalsIgnoreCase("money") || strings[1].equalsIgnoreCase("exp") || strings[1].equalsIgnoreCase("reputation"))  {
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
        cs.sendMessage("§a/reward komiss77 loni add 1000");
        cs.sendMessage("§a/reward komiss77 loni get rnd:0:100");
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
    //reward komiss77 permission serwer.world.perm.aaa     1      ostrov
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
        
        final String target = arg[0];
        
        RewardType type = RewardType.fromString(arg[1]);//RewardType type = RewardType.fromString(arg[1]);
        if (type==null) {
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
        
        if (type.is_integer) { //для остальных типов простой числовой расчёт 
            
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

            } else if (ApiOstrov.isInteger(amm)) {
                ammount = Integer.valueOf(amm); 
            } else {
                cs.sendMessage("§eКолличество - целое положительное число!");
                return false;
            }
            
            if (ammount==0 ) {
                cs.sendMessage("§cПустая награда!");
                return false;
            }
            
        } else { //расчёт по длительности
            
            if (amm.equalsIgnoreCase("forever") ) {
                forever = true;
            } else if (amm.endsWith("h")) {
                amm = amm.replaceFirst("h","");
                if (ApiOstrov.isInteger(amm)) {
                    ammount = Integer.valueOf(amm)*60*60;
                }
            } else if (amm.endsWith("d")) {
                amm = amm.replaceFirst("d","");
                if (ApiOstrov.isInteger(amm)) {
                    ammount = Integer.valueOf(amm)*24*60*60;
                }
            }
            if (ammount==0 && !forever) {
                cs.sendMessage("§cПустая награда! Для прав и групп указать время, например: §e1h §c- 1 час, §e2d §c- 2 дня, или §eforever §c- навсегда.");
                return false;
            }
            
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
        
        if (type==RewardType.EXP && ammount<0) {
            cs.sendMessage("§eопыт нельзя убавить!");
            return false;
        }
            
        
        //String cause = SM.this_server_name+".";
       // if (cs instanceof Player) {
       //     cause = cause+cs.getName();
       // } else if (arg.length>=5) {
      //      cause = cause+arg[4];
      //  }
        //cause = SM.this_server_name+"."+cause;
        //if (cause.length()>16) {
        //    cause = cause.substring(0, 15);
       //     cs.sendMessage("§eПревышена длина источника, обрезано до "+cause);
       // }
       // if (target==null) {
      //      cs.sendMessage("§cИгрока "+arg[0]+" нет на локальном сервере.");
      //      return false;
      //  }
        
        
//System.out.println(Operation.REWARD+", "+target+", type="+type+", param="+param+", "+(forever?"forever":ammount));

        if (Bukkit.getPlayer(target)!=null) { //что-то локально, если онлайн
            switch (type) {
                case EXP:
                    ApiOstrov.addExp(Bukkit.getPlayer(target), ammount);
                    return true;
                case LONI:
                    ApiOstrov.moneyChange(Bukkit.getPlayer(target), ammount, "награда от "+cs.getName());
                    return true;
                case KARMA:
                    StatManager.karmaChange(PM.getOplayer(target), ammount);
                    return true;
                case REPUTATION:
                    StatManager.reputationChange(PM.getOplayer(target), ammount);
                    return true;
                default:
                    break;
            }
        }
        //param: add, get, группа или право
        //ApiOstrov.sendMessage(Action.REWARD, type.tag, ammount, cs.getName()+"∫"+target, param);
        //switch (type) {
            
          //  case EXP:
          //      ApiOstrov.addXP(p, ammount);
          //      break;
                
          //  default:
        //выполняем на банжи, чтобы кросссерверно!
        if (cs instanceof Player) {
            
            ApiOstrov.sendMessage(((Player)cs), Operation.REWARD, GM.this_server_name+":"+cs.getName(), type.tag, ammount, target, param);
            
        } else {
            
            ApiOstrov.sendMessage(Operation.REWARD, GM.this_server_name+":консоль", type.tag, ammount, target, param);
            
        }
        
        
        
        cs.sendMessage(Operation.REWARD+", "+target+", type="+type+", param="+param+", "+(forever?"forever":ammount));
           //     break;
       // }
        
        //обработчик часть тут, чать на банжи
      /*  switch (type) {
            
            case MONEY:
                ApiOstrov.moneyChange(target, ammount, cause);//targetOp.addsetData(Data.MONEY, targetOp.getDataInt(Data.MONEY_REAL)+ammount);
                break;
                
            case PERMISSION:
                //банжи, отправка сообщения ниже
                if (param.startsWith("bauth") || param.startsWith("staff") || param.startsWith("group") || param.startsWith("money")) {
                    cs.sendMessage("§eВы не можете награждать административными правами! Запись о попытке сохранена в логе.");
                    Ostrov.log_err("попытка выдачи административных прав: "+cs.getName()+" -> "+param);
                    return false;
                }
                //
                break;
                
            case GROUP:
                //банжи, отправка сообщения ниже
                break;
                
            case EXP:
                StatManager.addXP(targetOp, ammount);
                return true; //возврат, или зацепит обработчик банжи
                
            case REPUTATION:
                StatManager.reputationChange(targetOp, ammount);///банжи, отправка сообщения ниже
                break;
                
            case KARMA:
                StatManager.karmaChange(targetOp, ammount);//банжи, отправка сообщения ниже
                break;
            
        }*/

        
        //ApiOstrov.sendMessage(cause, Action.REWARD, for_name+":"+type.toString()+":"+param+":"+(forever?"forever":ammount));
        //ApiOstrov.sendMessage(cs, Action.REWARD, d.tag, 0, (forever?Integer.MAX_VALUE:ammount), for_name, param );
        
        return true;

    }
    



    

    
    
    
    
    
    
    
    
    


}
    
    
 