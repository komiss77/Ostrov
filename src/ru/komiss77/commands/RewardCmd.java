package ru.komiss77.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Perm;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.RewardType;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.Group;





public class RewardCmd implements CommandExecutor, TabCompleter {
    

    
    
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
                    for (final Group g:Perm.getGroups()) {
                        if (!g.isStaff() && g.name.startsWith(strings[2])) sugg.add(g.name);
                    }
                    //sugg.addAll(OstrovDB.groups.keySet());
                }  else if (strings[1].equalsIgnoreCase("permission") ) {
                    sugg.add("ostrov.perm");
                    sugg.add(Ostrov.MOT_D+".builder");
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
                    sugg.add("1m");
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
    



    public RewardCmd() {
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
    //reward komiss77 loni          add                   1000     ostrov
    //reward komiss77 loni          get                rnd:0:100   ostrov
    //reward komiss77 permission serwer.world.perm.aaa     1      ostrov
    //reward komiss77 permission   perm.aaa             forever      ostrov
    //reward komiss77 group          vip                   10       ostrov
    //reward komiss77 group          vip                 forever       ostrov
    //reward komiss77 exp            add                   1000     ostrov
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( cs instanceof Player && !PM.getOplayer(cs.getName()).hasGroup("supermoder") ) {
            cs.sendMessage("");
            cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/supermoder!");
            cs.sendMessage("");
            return false;
        }
        
//Bukkit.broadcastMessage("RewardCmd arg="+Arrays.toString(arg));
        

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
//Bukkit.broadcastMessage("type="+type);
        
        //обработка колличества
        String amm = arg[3].toLowerCase();
        
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
                Ostrov.log_warn("reward error: "+Arrays.toString(arg));
                return false;
            }
            
        } else { //расчёт по длительности
            
            if (amm.equals("forever") ) {
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
            }  else if (amm.endsWith("m")) {
                amm = amm.replaceFirst("m","");
                if (ApiOstrov.isInteger(amm)) {
                    ammount = Integer.valueOf(amm)*30*24*60*60;
                }
            }
            if (ammount==0 && !forever) {
                cs.sendMessage("§cПустая награда! Для прав и групп указать время, например: §e1h §c- 1 час, §e2d §c- 2 дня, или §eforever §c- навсегда.");
                Ostrov.log_warn("reward error: "+Arrays.toString(arg));
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
//Bukkit.broadcastMessage("type="+type+" ammount="+ammount);
        

        if (Ostrov.MOT_D.equals("pay")) {
            
            if (type==RewardType.RIL ) {
                if (ammount<=0) {
                    cs.sendMessage("§cРил > 0!");
                    return false;
                }
    //Bukkit.broadcastMessage("executePstAsync target="+target+"type="+type+" ammount="+ammount);
                OstrovDB.executePstAsync(cs, "INSERT INTO `payments` (`name`, `rub`) VALUES ('"+target+"', '"+ammount+"')");
                cs.sendMessage("§a"+ammount+" рил для "+target+" : отправлена запись в БД");
                
            } else if (type==RewardType.GROUP ) {
    //Bukkit.broadcastMessage("executePstAsync target="+target+"type="+type+" ammount="+ammount);
                ammount = ammount/60/60/24; //привести к дням
                if (ammount<=0) {
                    cs.sendMessage("§cГруппа дни > 0!");
                    return false;
                }
                OstrovDB.executePstAsync(cs, "INSERT INTO `payments` (`name`, `gr`, `days`) VALUES ('"+target+"', '"+param+"', '"+ammount+"')");
                cs.sendMessage("§aГруппа "+param+" для "+target+" на "+ammount+"дн. : отправлена запись в БД");
                
            }
            
            return true;
        }
            

       
       
       
        //выполняем на банжи, чтобы кросссерверно!
        if (cs instanceof Player) {
            
            SpigotChanellMsg.sendMessage(  ((Player)cs),   Operation.REWARD,   cs.getName(),   type.tag,   ammount,    target,     param);
            //cs.sendMessage("§e*"+target+" нет на локальном сервере. Команда отправлена на прокси. Будет выполнена только если цель онлайн!");
            
        } else {
            
            SpigotChanellMsg.sendMessage(  Bukkit.getOnlinePlayers().stream().findAny().get(),   Operation.REWARD,   "консоль",      type.tag,   ammount,    target,     param);
            
        }
        
     
        
        return true;

    }
    



    

    
    
    
    
    
    
    
    
    


}
    
    
 
