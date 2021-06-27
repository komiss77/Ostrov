package ru.komiss77.Commands;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.OstrovDB;





public class Oreload implements Listener, CommandExecutor, TabCompleter {
    

    private final List <String> subCommands = Arrays.asList("all", "limiter", "pandora", "servers", "group", "informator");
    
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] args) {
        
        List <String> sugg = new ArrayList<>();
        switch (args.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                if (ApiOstrov.isLocalBuilder(cs, false)){
                    for (final String s : subCommands) {
                        if (s.startsWith(args[0])) sugg.add(s);
                    }
                    for (final Ostrov.Module m : Ostrov.Module.values()) {
                        if (m.name().toLowerCase().startsWith(args[0].toLowerCase())) sugg.add(m.name());
                    }
                }
                break;


        }
        
       return sugg;
    }
       
    
    

    
    
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        

        
        if (!ApiOstrov.isLocalBuilder(cs, true)) return false;
           
            switch (arg.length) {
                
                case 0:
                    final TextComponent result = new TextComponent("");
                    for (final String subCmd : subCommands) {
                        final TextComponent msg = new TextComponent( (subCmd.equals("all") ? "§l": "") + "§f"+subCmd+"    " );
                        final HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§5Нажмите, чтобы перезагрузить "+subCmd).create());
                        msg.setHoverEvent( he );
                        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reload "+subCmd ) );
                        result.addExtra(msg);
                    }
                    cs.spigot().sendMessage( result);
                    break;
                    
                    
                    
                case 1:
                    Ostrov.Module module = null;
                    for (final Ostrov.Module m : Ostrov.Module.values()) {
                        if (m.name().equalsIgnoreCase(arg[0])) {
                            module = m;
                            break;
                        };
                    }
                    if (module!=null) {
                        ((Initiable)Ostrov.getModule(module)).reload();
                        cs.sendMessage("§aМодуль §f"+arg[0]+" §aперезагружен!");
                        return true;
                    }
                    switch (arg[0]) {
                        case "all":
                            Cfg.ReLoadAllConfig();
                            Ostrov.getModules().forEach( (initiable) -> {
                                ((Initiable)initiable).reload();
                            });
                            break;
                        //case "limiter":
                        //    LimiterListener.init();
                       //     break;
                        //case "pandora":
                        //    Ostrov.pandora.Reload();
                        //    break;
                        //case "servers":
                            //Ostrov.servers.Reload();
                            //break;
                        case "group":
                            OstrovDB.reload();
                            break;
                        //case "informator":
                        //    Informator.reload();
                        //    break;
                        default:
                           // cs.sendMessage( "§cМодули: all, moblimit/ml, pandora, servers, group, inform");
                            //cs.sendMessage( "§e/<команда> reload §7-Перезагрузка настроек команды");
                            return true;
                    }    

                    cs.sendMessage("§aМодуль §f"+arg[0]+" §aперезагружен!");
                    
                    
                    break;
                    
                    
            }

        return true;
    }
    



    
    
    

    
    
    
    
    
    
    
    
    
    


}
    
    
 