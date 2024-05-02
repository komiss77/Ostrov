package ru.komiss77.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Perm;
import ru.komiss77.enums.Module;
import ru.komiss77.modules.games.GM;




public class OreloadCmd implements Listener, CommandExecutor, TabCompleter {
    

    private static final List <String> subCommands;
    
    static {
        subCommands = new ArrayList<>();
        subCommands.add("all");
        //subCommands.add("pvp");
        subCommands.add("gamemanager");
        subCommands.add("signs");
        subCommands.add("group");
        subCommands.add("connection_ostrov");
        subCommands.add("connection_local");
        //Arrays.asList("all", "group", "connection_ostrov");
        for (final Module m : Module.values()) {
            subCommands.add(m.name());
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] args) {
        
      /*  final List <String> sugg = new ArrayList<>();
        switch (args.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                if (ApiOstrov.isLocalBuilder(cs, false)){
                    for (final String s:subCommands) {
                        if (s.startsWith(args[0])) sugg.add(s);
                    }
                }
                break;


        }*/
        
       return subCommands;
    }
       
    
    

    
    
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        

        
        if (!ApiOstrov.isLocalBuilder(cs, true)) return false;
           
            switch (arg.length) {
                
                case 0:
                    final TextComponent result = Component.text("Какой модуль перезагрузить? ", NamedTextColor.RED);
                    for (final String subCmd : subCommands) {
                    	result.append(Component.text( subCmd+"    ", (subCmd.equals("all") ? NamedTextColor.BLUE: NamedTextColor.WHITE))
                    		.hoverEvent(HoverEvent.showText(Component.text("§5Нажмите, чтобы перезагрузить "+subCmd)))
                    		.clickEvent(ClickEvent.runCommand("/reload "+subCmd)));
                    }
                    cs.sendMessage(result);
                    break;
                    
                    
                    
                case 1:
                    Module module = null;
                    for (final Module m : Module.values()) {
                        if (m.name().equalsIgnoreCase(arg[0])) {
                            module = m;
                            break;
                        };
                    }
                    if (module!=null) {
                        (ApiOstrov.getModule(module)).reload();
                        cs.sendMessage("§aМодуль §f"+arg[0]+" §aперезагружен!");
                        return true;
                    }
                    
                    
                    switch (arg[0]) {
                        
                        case "all" -> {
                            Config.ReLoadAllConfig();
                            Ostrov.getModules().forEach(m -> m.reload());
                        }
                        
                        case "connection_ostrov" -> OstrovDB.init(false, true);
                        
                        case "connection_local" -> //!!!! релоад соединения - делать асинх
                            Ostrov.async(()-> LocalDB.init(), 0);//только соединение!
                            
                        case "group" -> Ostrov.async( ()-> {
                                //OstrovDB.getBungeeServerInfo(); //1!!! 
                                Perm.loadGroups(true); //2!!! сначала прогрузить allBungeeServersName, или не определяет пермы по серверам
                            }, 0 );
                //Perm.loadGroups(true);
                        case "gamemanager" -> {
                          //GM.reload = true;
                          Ostrov.async( ()-> GM.load(GM.State.RELOAD), 0);
                        }
                        
                        case "signs" -> GM.onWorldsLoadDone();
                        
                        default -> {
                            // cs.sendMessage( "§cМодули: all, moblimit/ml, pandora, servers, group, inform");
                            //cs.sendMessage( "§e/<команда> reload §7-Перезагрузка настроек команды");
                            return true;
                        }    
                    }

                    if (arg[0].equals("all")) {
                        cs.sendMessage("§fВсе модули §aперезагружены.");
                    } else {
                        cs.sendMessage("§f"+arg[0]+" §aперезагружен.");
                    }
                    
                    
                    break;

                    
                    
            }

        return true;
    }
    



    
    
    

    
    
    
    
    
    
    
    
    
    


}
    
    
 
