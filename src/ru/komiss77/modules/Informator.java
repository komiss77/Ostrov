package ru.komiss77.modules;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Managers.PM;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.OstrovConfig;




public final class Informator extends Initiable {
 
    private static OstrovConfig inform;
    private static List <TextComponent> messages;
    private static boolean use;
    private static int sec,curr_msg,interval=0;
   
    
    public Informator() {
        messages = new ArrayList<>();
        reload();
    }

    @Override
    public void reload() {
        messages.clear();
        
        getConfig();
        
        use=inform.getBoolean("use");
        interval=inform.getInt("interval");
        
        
        try {
            if(inform.getConfigurationSection("messages")!=null) {
                inform.getConfigurationSection("messages").getKeys(false).stream().forEach((s) -> {
//System.out.println("- s="+s);                        
//System.out.println("-- msg="+s);                        
                    TextComponent msg = new TextComponent(Ostrov.prefix+inform.getString("messages."+s+".msg").replaceAll("&", "§"));
                    msg.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(inform.getString("messages."+s+".hover_text").replaceAll("&", "§")).create() ) );
                    msg.setClickEvent( new ClickEvent( ClickEvent.Action.valueOf(inform.getString("messages."+s+".click.action")), inform.getString("messages."+s+".click.string"))  );
                    messages.add(msg);
                });
            }
            Ostrov.log_ok("§fИнформатор - загружено сообщений :§e"+messages.size()+"§f, интервал показа: §e"+interval+"§f сек.");
        } catch (Exception ex) {
            Ostrov.log_err("Информатор - сообщения не загружены : "+ex.getMessage());
            use = false;
        }
//System.out.println("Informator.init() use="+use+" interval="+interval+" messages");        
                
    }

    
    
    
    public static void tick() {
        if (!use || messages.isEmpty() || PM.getOnlineCount()==0) return;
//System.out.println("Informator.tick() sec="+sec+" interval="+interval);        
        sec++;
        if (sec>=interval) {
            sec=0;
//System.out.println("Informator-send!!");        
            
            Bukkit.getOnlinePlayers().stream().forEach((p) -> {
                p.spigot().sendMessage(messages.get(curr_msg));
            });
            curr_msg++;
            if (curr_msg>=messages.size()) curr_msg=0;
        }
        
        
    }

    
    
    
    
    
 
 
    private static void getConfig() {
        
    inform = Cfg.manager.getNewConfig("informator.yml", new String[]{"", "Ostrov77 autoinformator", "", "click actions: OPEN_URL CHANGE_PAGE OPEN_FILE RUN_COMMAND SUGGEST_COMMAND"});
        
    inform.addDefault("use", false);
    inform.addDefault("interval", 600);
    
    inform.addDefault("messages.menu.msg", "&fЛокальное серверное меню - команда §6/menu &8<<Клик");
    inform.addDefault("messages.menu.hover_text", "&eНажмите на это сообщение!");
    inform.addDefault("messages.menu.click.action", "RUN_COMMAND");
    inform.addDefault("messages.menu.click.string", "/menu");
    
    inform.addDefault("messages.profile.msg", "&fВаш профиль - статистика,привилегии,журнал - §6/profile &8<<Клик");
    inform.addDefault("messages.profile.hover_text", "&eНажмите на это сообщение!");
    inform.addDefault("messages.profile.click.action", "RUN_COMMAND");
    inform.addDefault("messages.profile.click.string", "/profile");
    
    inform.addDefault("messages.serv.msg", "&fВыбор сервера - команда §6/serv &8<<Клик");
    inform.addDefault("messages.serv.hover_text", "&eНажмите на это сообщение!");
    inform.addDefault("messages.serv.click.action", "RUN_COMMAND");
    inform.addDefault("messages.serv.click.string", "/serv");
    
    inform.addDefault("messages.staff.msg", "&fАдминистрация сервера - команда §6/staff &8<<Клик");
    inform.addDefault("messages.staff.hover_text", "&eНажмите на это сообщение!");
    inform.addDefault("messages.staff.click.action", "RUN_COMMAND");
    inform.addDefault("messages.staff.click.string", "/staff list");

    inform.addDefault("messages.group.msg", "&fВаши группы, покупка групп - команда §6/group &8<<Клик");
    inform.addDefault("messages.group.hover_text", "&eНажмите на это сообщение!");
    inform.addDefault("messages.group.click.action", "RUN_COMMAND");
    inform.addDefault("messages.group.click.string", "/group");

    inform.addDefault("messages.money.msg", "&fУправление балансом, пополнение, перевод, обмен - §6/money &8<<Клик");
    inform.addDefault("messages.money.hover_text", "&eНажмите на это сообщение!");
    inform.addDefault("messages.money.click.action", "RUN_COMMAND");
    inform.addDefault("messages.money.click.string", "/money");
    
    inform.addDefault("messages.help.msg", "&aПОМЩЬ ПО КОМАНДАМ - §6/help &8<<Клик");
    inform.addDefault("messages.help.hover_text", "&eНажмите на это сообщение!");
    inform.addDefault("messages.help.click.action", "RUN_COMMAND");
    inform.addDefault("messages.help.click.string", "/help");
    
    inform.addDefault("messages.moneyadd.msg", "&fПополняйте свой рублёвый счёт - §e/money add &8<<Клик");
    inform.addDefault("messages.moneyadd.hover_text", "&eНажмите на это сообщение!");
    inform.addDefault("messages.moneyadd.click.action", "RUN_COMMAND");
    inform.addDefault("messages.moneyadd.click.string", "/money add");
    
    inform.addDefault("messages.groupbuy.msg", "&fКупите и получите группу, не выходя с сервера - §e/group buy &8<<Клик");
    inform.addDefault("messages.groupbuy.hover_text", "&eНажмите на это сообщение!");
    inform.addDefault("messages.groupbuy.click.action", "RUN_COMMAND");
    inform.addDefault("messages.groupbuy.click.string", "/group buy");
    
    inform.saveConfig();

    }
 
 
 
 
 
 
 
   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
