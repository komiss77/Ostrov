package ru.komiss77.Commands;


import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Enums.Data;
import ru.komiss77.Managers.PM;
import ru.komiss77.Ostrov;
import ru.komiss77.ProfileMenu.PassportHandler;




public class Passport implements Listener,CommandExecutor {
    
    

    public Passport() {
        init();
    }
    
    private void help(final Player p) {
        p.spigot().sendMessage(
            new ComponentBuilder("§3/passport see <ник> - §7посмотреть паспорт Островитянина §8<<клик")
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aКлик-набрать").create()))
            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/passport see "))
            .create()
        );
        p.spigot().sendMessage(
            new ComponentBuilder("§3/passport get - §7получить свой паспорт Островитянина §8<<клик")
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aКлик-набрать").create()))
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/passport get"))
            .create()
        );
    }

    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            if (arg.length==1 && arg[0].equals("reload")) {
                reload();
            } else {
                cs.sendMessage("§e/"+this.getClass().getSimpleName()+" reload §7- перезагрузить настройки команды");
            }
            return true;
        }

        final Player p=(Player) cs;
        
        //if (!allow_command) {
        //    p.sendMessage( "");
        //    return true;
        //}
            
            switch (arg.length) { 
                
                case 0:
                    break;
                    
                case 1:
                    if (arg[0].equals("get")) {
//System.out.println("get() "+p.getInventory().getItemInMainHand());        
                        if (p.getInventory().getItemInMainHand().getType()!=Material.AIR) {
                            cs.sendMessage(Ostrov.prefix+"§cПравая рука занята, в карман не залезть!");
                            return true;
                        }
                        PassportHandler.givePassport(p, -1);
                        return true;
                    } else if (arg[0].equals("see")) {
                        cs.sendMessage(Ostrov.prefix+"§cУкажите ник!");
                        return false;
                    }
                    break;
                    
                case 2:
                    if (arg[0].equals("see")) {
                        if (arg[0].equals(cs.getName()) || ApiOstrov.hasGroup(cs.getName(), "moder") || ApiOstrov.hasGroup(cs.getName(), "vip") || PM.getOplayer(cs.getName()).getBungeeIntData(Data.PLAY_TIME)>18000) { 
                            //PassportHandler.showPasport(p,arg[1]);
                            ApiOstrov.sendMessage(p, ru.komiss77.Enums.Action.OSTROV_PASSPORT, arg[1]);
                        } else {
                            cs.sendMessage(Ostrov.prefix+"§cПросматривать чужой паспорт могут модераторы,вип,премиум или наигравшие боьльше 300 часов!");
                        }
                        return true;
                    }
                    break;
                    
                    
            }

        help(p);
        return true;
    }
    



    
    
    
    

    public void init() {
        try {
            //allow_command = Conf.GetCongig().getBoolean("modules.command.pvp.use");
     
            //if (!allow_command) {
             //   Ostrov.log_ok ("§e"+this.getClass().getSimpleName()+" выключен.");
            //    return;
            //}
            
            //Bukkit.getPluginManager().registerEvents(this, Ostrov.GetInstance());
            
            Ostrov.log_ok ("§2"+this.getClass().getSimpleName()+" активен!");
            
        } catch (Exception ex) { 
            Ostrov.log_err("§4Не удалось загрузить настройки "+this.getClass().getSimpleName()+" : "+ex.getMessage());
        }
    }

    public void reload () {
        //HandlerList.unregisterAll(this);
        Cfg.LoadConfigs();
        init();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
/*    
@EventHandler(priority = EventPriority.MONITOR) 
    public void onDataRecieved (BungeeDataRecieved e) {
           
        
    }
    
    
@EventHandler(priority = EventPriority.MONITOR) 
    public void onQuit (PlayerQuitEvent e) {
           
        
    }
*/

    
    
    
    
    
    
    
    
    


}
    
    
 