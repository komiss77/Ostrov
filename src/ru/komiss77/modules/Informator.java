package ru.komiss77.modules;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.StatFlag;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.TCUtils;




public final class Informator implements Initiable {
 
    private static OstrovConfig inform;
    private static List <Component> messages;
    private static boolean use;
    private static int sec,curr_msg,interval=0;
    private static final Component guestNotify = TCUtils.format(Ostrov.PREFIX+"§6§lВы играете в режиме §5§lГостя§6§l, ваши данные §c§lне будут сохраняться§6§l! §a§lЗарегистрируйтесь §6§lдля полноценной игры!");
    
    public Informator() {
        messages = new ArrayList<>();
        reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void onDisable() {
    }   
    
    @Override
    public void reload() {
        messages.clear();
        
        getConfig();
        
        use=inform.getBoolean("use");
        interval=inform.getInt("interval");
        
        
        try {
        	final ConfigurationSection cs = inform.getConfigurationSection("messages");
            if(cs!=null) {
            	for (final String s : cs.getKeys(false)) {
            		final Action ac = Action.valueOf(cs.getString(s+".click.action"));
                    messages.add(Component.text(Ostrov.PREFIX+cs.getString(s+".msg").replaceAll("&", "§"))
                		.hoverEvent(Component.text(cs.getString(s+".hover_text").replaceAll("&", "§")))
                		.clickEvent(ClickEvent.clickEvent(ac == null ? Action.SUGGEST_COMMAND : ac, cs.getString(s+".click.string"))));
                
            	}
            }
            Ostrov.log_ok("§fИнформатор - загружено сообщений :"+messages.size()+", интервал показа: "+interval+" сек.");
        } catch (Exception ex) {
            Ostrov.log_err("Информатор - сообщения не загружены : "+ex.getMessage());
            use = false;
        }
//System.out.println("Informator.init() use="+use+" interval="+interval+" messages");        
                
    }
    
    public static void tickAsync() {
        if (!use || messages.isEmpty() || !PM.hasOplayers()) return;      
        sec++;
        if (sec>=interval) {
            sec=0;       
            
            Oplayer op;
            for (final Player p : Bukkit.getOnlinePlayers()) {
                op = PM.getOplayer(p);
                if (!op.hasFlag(StatFlag.InformatorOff)) {
                    p.sendMessage(messages.get(curr_msg));
                }
            }
            
            curr_msg++;
            if (curr_msg>=messages.size()) {
                curr_msg=0;
                for (final Player p : Bukkit.getOnlinePlayers()) {
                    op = PM.getOplayer(p);
                    if (op.isGuest) {
                        p.sendMessage(guestNotify);
                    }
                }
            }
        }
    }

    
    
    
    
    
 
 
    private static void getConfig() {
        
    inform = Config.manager.getNewConfig("informator.yml", new String[]{"",
        "Ostrov77 autoinformator",
        "", 
        "click actions: OPEN_URL CHANGE_PAGE OPEN_FILE RUN_COMMAND SUGGEST_COMMAND"});
        
    inform.addDefault("use", false);
    inform.addDefault("interval", 600);
    
    inform.saveConfig();

    }
 
 
 
 
 
 
 
   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
