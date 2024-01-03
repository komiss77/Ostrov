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
    private static final List <Component> messagesRu;
    private static final List <Component> messagesEn;
    private static boolean use;
    private static int sec, curr_msg_Ru, curr_msg_En, interval=0;
    private static final Component guestNotifyRu = TCUtils.format(Ostrov.PREFIX+"§6§lВы играете §5§lРежиме Гостя§6§l, ваши данные §c§lне будут сохраняться§6§l! §a§lЗарегистрируйтесь §6§lдля полноценной игры!");
    private static final Component guestNotifyEn = TCUtils.format(Ostrov.PREFIX+"§6§lYou are playing in §5§lGuest Mode§6§l, your player data §c§lwill not be saved§6§l! §a§lRegister §6§lfor complete game!");
    
    static {
        messagesRu = new ArrayList<>();
        messagesEn = new ArrayList<>();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
        reload();
    }

    @Override
    public void onDisable() {
    }   
    
    @Override
    public void reload() {
        messagesRu.clear();
        messagesEn.clear();
        
        inform = Config.manager.getNewConfig("informator.yml", new String[]{"",
            "Ostrov77 autoinformator",
            "", 
            "click actions: OPEN_URL CHANGE_PAGE OPEN_FILE RUN_COMMAND SUGGEST_COMMAND"});

        inform.addDefault("use", false);
        inform.addDefault("interval", 600);

        inform.saveConfig();
        
        use=inform.getBoolean("use");
        interval=inform.getInt("interval");
        
        
        try {
            ConfigurationSection cs = inform.getConfigurationSection("messagesRu");
            
            if(cs!=null) {
            	for (final String s : cs.getKeys(false)) {
                    final Action ac = Action.valueOf(cs.getString(s+".click.action"));
                    messagesRu.add(Component.text(Ostrov.PREFIX+cs.getString(s+".msg").replaceAll("&", "§"))
                		.hoverEvent(Component.text(cs.getString(s+".hover_text").replaceAll("&", "§")))
                		.clickEvent(ClickEvent.clickEvent(ac == null ? Action.SUGGEST_COMMAND : ac, cs.getString(s+".click.string"))));
                
            	}
            }
            
            cs = inform.getConfigurationSection("messagesEn");
            
            if(cs!=null) {
            	for (final String s : cs.getKeys(false)) {
                    final Action ac = Action.valueOf(cs.getString(s+".click.action"));
                    messagesEn.add(Component.text(Ostrov.PREFIX+cs.getString(s+".msg").replaceAll("&", "§"))
                		.hoverEvent(Component.text(cs.getString(s+".hover_text").replaceAll("&", "§")))
                		.clickEvent(ClickEvent.clickEvent(ac == null ? Action.SUGGEST_COMMAND : ac, cs.getString(s+".click.string"))));
                
            	}
            }
            
            Ostrov.log_ok("§fИнформатор - загружено сообщений Ru:§b"+messagesRu.size()+"§f, En:"+messagesEn.size()+", интервал "+interval+" сек.");
            
        } catch (Exception ex) {
            Ostrov.log_err("Информатор - сообщения не загружены : "+ex.getMessage());
            use = false;
        }
//System.out.println("Informator.init() use="+use+" interval="+interval+" messages");        
                
    }
    
    public static void tickAsync() {
        if (!use || !PM.hasOplayers()) return;      
        sec++;
        if (sec>=interval) {
            sec=0;       
            
            Oplayer op;
            for (final Player p : Bukkit.getOnlinePlayers()) {
                op = PM.getOplayer(p);
                if (!op.hasFlag(StatFlag.InformatorOff)) {
                    if (op.eng) {
                        p.sendMessage(messagesEn.get(curr_msg_En));
                    } else {
                        p.sendMessage(messagesRu.get(curr_msg_Ru));
                    }
                    
                }
            }
            
            curr_msg_Ru++;
            if (curr_msg_Ru>=messagesRu.size()) {
                curr_msg_Ru=0;
                for (final Player p : Bukkit.getOnlinePlayers()) {
                    op = PM.getOplayer(p);
                    if (op.isGuest && !op.eng) {
                        p.sendMessage(guestNotifyRu);
                    }
                }
            }
            
            curr_msg_En++;
            if (curr_msg_En>=messagesEn.size()) {
                curr_msg_En=0;
                for (final Player p : Bukkit.getOnlinePlayers()) {
                    op = PM.getOplayer(p);
                    if (op.isGuest && op.eng) {
                        p.sendMessage(guestNotifyEn);
                    }
                }
            }
        }
    }

    
    
    
    
    
 

 
 
 
 
   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
