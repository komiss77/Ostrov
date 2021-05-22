package ru.komiss77.Commands;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Listener.ResourcePacks;
import ru.komiss77.Listener.TPAListener;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.SM;
import ru.komiss77.Managers.Timer;
import ru.komiss77.Managers.Warps;
import ru.komiss77.Ostrov;
import ru.komiss77.menu.Sounds;
import ru.komiss77.utils.ChatMsgUtil;
import ru.komiss77.utils.TeleportLoc;
import ru.komiss77.utils.inventory.SmartInventory;


public class CMD implements TabCompleter  {

    //public static Inventory worlds;
    
    private static Map <String,Integer> homes_per_group;
    
    public static boolean home_command;
    
    public static boolean fly_command;
    public static boolean fly_block_atack_on_fly;
    public static boolean fly_off_on_damage;
    
    public static int no_damage_on_tp;                                          //время неуязвимости при входе или тп
    
    public static int tpa_command;
    public static boolean save_location_on_world_change;
    public static int tpr_command;
    
    public static boolean back_command;
    public static boolean settings_command;
    public static boolean get_command;
    public static boolean world_command;
    public static boolean tppos_command;
    public static boolean tphere_command;
    public static boolean spawn_command;
    public static boolean gm_command;
    public static boolean invsee_command;
    public static boolean speed_command;
    public static boolean pweather_command;
    public static boolean ptime_command;
    public static boolean repair_command;
    public static boolean heal_command;
    public static boolean top_command;
    public static List <String> ostrov_commands;
    public static Set <String> all_server_commands;
    
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmd, String string, String[] input) {
        List <String> sugg = new ArrayList<>();
       if (input.length==1) {
           sugg.addAll( Arrays.asList("list") );
       } else if (input.length==2) {
            //sugg.addAll(AM.getArenasNames());
       }
       return sugg;
    }
    

    
public static void LoadVars() {
    
    //worlds = null;
    
    Cfg.GetCongig().getConfigurationSection("modules.command.home.amount_per_group").getKeys(false).stream().forEach((s) -> {
        homes_per_group.put ( s, Cfg.GetCongig().getInt("modules.command.home.amount_per_group."+s) );
    });
    home_command=Cfg.GetCongig().getBoolean("modules.command.home.use");
    
    fly_command=Cfg.GetCongig().getBoolean("modules.command.fly.use");
    fly_block_atack_on_fly=Cfg.GetCongig().getBoolean("modules.command.fly.disable_atack_on_fly");
    fly_off_on_damage=Cfg.GetCongig().getBoolean("modules.command.fly.fly_off_on_damage");
    
    no_damage_on_tp = Cfg.GetCongig().getInt("player.invulnerability_on_join_or_teleport");

    tpa_command=Cfg.GetCongig().getInt("modules.command.tpa");
//System.out.println("--LoadVars tpa_command="+tpa_command);
    save_location_on_world_change=Cfg.GetCongig().getBoolean("modules.save_location_on_world_change");
    tpr_command=Cfg.GetCongig().getInt("modules.command.tpr");

    back_command=Cfg.GetCongig().getBoolean("modules.command.back");
    settings_command=Cfg.GetCongig().getBoolean("modules.command.settings");
    get_command=Cfg.GetCongig().getBoolean("modules.command.get");
    world_command=Cfg.GetCongig().getBoolean("modules.command.world");
    tppos_command=Cfg.GetCongig().getBoolean("modules.command.tppos");
    tphere_command=Cfg.GetCongig().getBoolean("modules.command.tphere");
    spawn_command=Cfg.GetCongig().getBoolean("modules.command.spawn");
    gm_command=Cfg.GetCongig().getBoolean("modules.command.gm");
    invsee_command=Cfg.GetCongig().getBoolean("modules.command.invsee");
    speed_command=Cfg.GetCongig().getBoolean("modules.command.speed");
    pweather_command=Cfg.GetCongig().getBoolean("modules.command.pweather");
    ptime_command=Cfg.GetCongig().getBoolean("modules.command.ptime");
    repair_command=Cfg.GetCongig().getBoolean("modules.command.repair");
    heal_command=Cfg.GetCongig().getBoolean("modules.command.heal");
    top_command=Cfg.GetCongig().getBoolean("modules.command.top");
    
    ostrov_commands = new ArrayList<>( Ostrov.instance.getDescription().getCommands().keySet() );
    all_server_commands = new HashSet<>();
    for (Plugin plugin:Bukkit.getServer().getPluginManager().getPlugins()){
//System.out.println("+++++++++ plugin="+plugin);        
//System.out.println("+++++++++ getDescription="+plugin.getDescription());        
//System.out.println("+++++++++ getCommands="+plugin.getDescription().getCommands());        
        if (plugin.getDescription().getCommands()!=null) {
            plugin.getDescription().getCommands().keySet().stream().forEach((command) -> {
                all_server_commands.add(command);
//System.out.println("------------> Command add "+command); 
            });
        }
    }
}

    
    
    public static void Init() {
        homes_per_group = new HashMap<String, Integer>() {} ;
        LoadVars();

    }

    public static void ReLoadVars() {
        homes_per_group.clear();
        LoadVars();
        }

 







    public static boolean CommandHamdler(CommandSender sender, Command cmd, String label, String[] arg) {
//System.out.println("------------> handleCommand "+cmd+ " s:"+s+" arg:"+arg.toString());
    if (Bukkit.getMotd().length()==3) return false;
    

        Player p= null;
        if(sender instanceof Player ) p=(Player) sender;
        
        String home;
        int limit;
        
        //if ( pvp_battle_time > 1 && PM.inBattle(s) ) {            -- перенёс в PlayerCommandPreprocessEvent
        //    p.sendMessage( "§cРежим боя - команды заблокированы! Осталось " + Timer.CD_left(p.getName(), "pvp") + " сек." );
        //    return false;
       // }
        
switch (label) {
    

    case "serv":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            switch (arg.length) {
                case 0:
                    p.openInventory(SM.main_inv);
                    p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1, 2);
                    break;
                case 1:
                    ApiOstrov.sendToServer(p, arg[0], "");
                    break;
                case 2:
                    ApiOstrov.sendToServer(p, arg[0], arg[1]);
                    break;
                default:
                    break;
            }
            break;
              
    case "rp":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
        if (ResourcePacks.use) {
            if ( ResourcePacks.Текстуры_утановлены(p)) {
                p.sendMessage( "§aУ вас уже установлен пакет ресурсов!");
            } else ResourcePacks.Set_pack(p,true);
        } else p.sendMessage( "§cДанный сервер не требует пакета ресурсов!");
           break;
              
              
    case "menu":
        if (p==null) {
            sender.sendMessage(Ostrov.prefix+"§сне консольная команда!");
            return true;
        }
        if (Bukkit.getPluginManager().getPlugin("")!=null) {
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "cc open menu "+p.getName() );
        } else {
            p.performCommand(Cfg.GetCongig().getString("modules.command.menu"));
        }
        //Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "cc open menu "+p.getName() );
        break;
        

    case "sethome":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
        if (!home_command) {p.sendMessage( "§cДома отключены на этом сервере!");return false;}
            home = "home";
            if ( arg.length == 1 ) {                                       //определяем название
                if (arg[0].length()>10) {
                    p.sendMessage( "§cСлишком длинное название дома!");
                    return false;
                }
                home = arg[0];
            }
                limit = 1;
                if (!PM.getOplayer(p.getName()).hasAnyGroup()) {                             //вычисление лимита
                    if (homes_per_group.containsKey("default")) limit = homes_per_group.get("default");
                } else {
                    for ( String gr : PM.getOplayer(p.getName()).getGroups()) {
                       if ( homes_per_group.containsKey(gr) && homes_per_group.get(gr)>limit ) limit = homes_per_group.get(gr); 
                    }
                } 
                if (  arg.length ==0 && PM.OP_GetHomeList(p.getName()).size()>1 ) { //если не указал дом, но их больше 1 - уточнить какой
                //p.sendMessage( "§bУ Вас несколько домов, клик на нужный:");// §6"+PM.OP_GetHomeList(p.getName()).toString().replaceAll("\\[|\\]", "") );
                    TextComponent homes = new TextComponent( "§bКакую точку дома обновить?  ");
                    for (final String homeName : PM.OP_GetHomeList(p.getName())) {
                        final TextComponent msg=new TextComponent( "§b- §e"+homeName+"  " );
                        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "§7Клик - обновить точку дома §6").create() ) );
                        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sethome " + homeName));
                        homes.addExtra(msg);
                    }
                    sender.spigot().sendMessage(homes);
                    return false;
                }
                    if ( PM.OP_GetHomeList(p.getName()).contains(home) ) {      //если есть такой, обновляем
                        PM.OP_SetHome(p, home);
                        p.sendMessage( "§2Для дома "+home+" установлена новая позиция.");
                        return true;
                    }  else if ( PM.OP_GetHomeList(p.getName()).size()>=limit && !p.isOp() ) { //если ставим новый дом, проверяем лимит     
                        p.sendMessage( "§cВы не можете добавить еще один дом! Лимит Вашей группы: "+limit+", Ваши дома: §6"+PM.OP_GetHomeList(p.getName()).toString().replaceAll("\\[|\\]", ""));
                        p.sendMessage( "§cУдалите ненужный командой /delhome");
                        return false;
                    } else {
                        PM.OP_SetHome(p, home);
                        if (home.equals("home")) p.setBedSpawnLocation(p.getLocation());
                        p.sendMessage( "§2Дом "+((home.equals("home"))?"":home)+" установлен!");
                    }
              break;
              
   case "home":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
        if (!home_command) {p.sendMessage( "§cДома отключены на этом сервере!");return false;}
            home = "home";
            if ( arg.length == 0 &&  PM.OP_GetHomeList(p.getName()).size()>1 ) {
                //p.sendMessage( "§bУ Вас несколько домов, выберите нужный: §6"+PM.OP_GetHomeList(p.getName()).toString().replaceAll("\\[|\\]", "") );
                TextComponent homes = new TextComponent( "§aВ какой дом вернуться? ");
                for (final String homeName : PM.OP_GetHomeList(p.getName())) {
                    final TextComponent msg=new TextComponent( "§e"+homeName+"  " );
                    msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "§7Клик - вернуться в точку дома "+homeName).create() ) );
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + homeName));
                    homes.addExtra(msg);
                }
                sender.spigot().sendMessage(homes);
                return false;
            } 
            if ( arg.length == 1 )  home = arg[0];
            if ( PM.OP_GetHomeList(p.getName()).contains(home) ) {
                Location loc = PM.OP_GetHomeLocation(p, home);
                if (loc!=null) {
                    if (!loc.getChunk().isLoaded()) loc.getChunk().load();
                    if ( ApiOstrov.isLocationSave(p, loc) ) {
                        p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                        p.sendMessage( "§2Дом милый дом!");
                    } else {
                        Location loc2 = TeleportLoc.findNearestSafeLocation(loc,null);
                            if ( loc2 != null) {
                                p.teleport(loc2, PlayerTeleportEvent.TeleportCause.COMMAND);
                                p.sendMessage( "§4Дома что-то случилось, некуда вернуться! Дух Острова перенёс Вас в ближайшее безопасное место.");    
                                p.sendMessage( "§cУстановите точку дома заново.");    
                            } else {
                                p.sendMessage( "§cДома что-то случилось, некуда вернуться! Вернитесь пешком, проверьте и установите точку дома заново.");
                                p.sendMessage( "§cЕсли Вы забыли где Ваш дом "+home+" , вот его координаты x:"+(int)loc.getBlockX()+", y:"+(int)loc.getBlockY()+", z:"+(int)loc.getBlockZ() );
                            }
                    }
                } else p.sendMessage( "§cЧто-то пошло не так при получении координат.");
            } else p.sendMessage( (PM.OP_GetHomeList(p.getName()).isEmpty())?"§cУ Вас нет дома! Установите его командой /sethome":"§cНет такого дома! Ваши дома: §6"+PM.OP_GetHomeList(p.getName()).toString().replaceAll("\\[|\\]", "") );
              break;

    case "delhome":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
        if (home_command) {
            if (  arg.length ==0 && PM.OP_GetHomeList(p.getName()).size()>1 ) { //если не указал дом, но их больше 1 - уточнить какой
                //p.sendMessage( "§bУ Вас несколько домов, выберите нужный: §6"+PM.OP_GetHomeList(p.getName()).toString().replaceAll("\\[|\\]", "") );
                //p.sendMessage( "§cКакой дом удалить? ");// §6"+PM.OP_GetHomeList(p.getName()).toString().replaceAll("\\[|\\]", "") );
                    TextComponent homes = new TextComponent( "§cКакой дом удалить? ");
                    for (final String homeName : PM.OP_GetHomeList(p.getName())) {
                        final TextComponent msg=new TextComponent( "§e"+homeName+"  " );
                        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "§7Клик - удалить точку дома "+homeName).create() ) );
                        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/delhome " + homeName));
                        homes.addExtra(msg);
                    }
                    sender.spigot().sendMessage(homes);
                return false;
            }
            home = "home";
            if ( arg.length == 1 )  home = arg[0];
            if ( PM.OP_GetHomeList(p.getName()).contains(home) ) {
                PM.OP_DelHome(p, home);
                p.sendMessage( "§4Точка дома "+ (home.equals("home")? "":home)  +" удалена!");
            } else p.sendMessage( "§cНет такого дома! Ваши дома: §6"+PM.OP_GetHomeList(p.getName()).toString().replaceAll("\\[|\\]", "") );
              break;
        } else p.sendMessage( "§cДома отключены на этом сервере!");

    case "fly":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
        if (!fly_command) {p.sendMessage( "§cПолёт отключён на этом сервере!");return false;}
            if ( p.hasPermission("ostrov.fly") ) {
                switch (arg.length) { 
                    case 0:
                        if (p.getAllowFlight()) {
                            p.setFlying(false); 
                            p.setAllowFlight(false);
                            p.setFallDistance(0);
                            p.sendMessage( "§eРежим полёта выключен!"); 
                            return true;
                        } else { 
                            p.setAllowFlight(true);
                            //p.setFlying(true); 
                            p.sendMessage( "§aРежим полёта включен!"); 
                            return true;
                        }
                    case 1:
                        switch (arg[0]) {
                            case "on":
                                p.setAllowFlight(true); 
                               // p.setFlying(true);
                                p.sendMessage( "§6Режим полёта включен!");
                                return true;
                            case "off":
                                p.setFlying(false);
                                p.setAllowFlight(false); 
                                p.sendMessage( "§6Режим полёта выключен!");
                                return true;
                            default:
                                p.sendMessage( "§c ?   §f/fly,  §f/fly on,  §f/fly off");
                                break;
                        }
                        break;
                    default:
                        p.sendMessage( "§c ?   §f/fly,  §f/fly on,  §f/fly off");
                        break;
                }
            } else p.sendMessage( "§cЭто команда доступна группам Вип/Премиум/Ангел/Цербер!");
              break;
        
    case "tpa":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
        if (tpa_command>0) {
                if (arg.length==1 && arg[0].equals("unban")) {
                    if (TPAListener.HasBanList(p.getName())) {
                        TPAListener.OpenUnban(p);
                    } else p.sendMessage("§cСписок забаненых игроков пуст.");
                } else {
                    if (!Timer.CD_has( p.getName(), "tpa_command" ) ) {
                         Timer.CD_add( p.getName(), "tpa_command", tpr_command);
                         TPAListener.openTPmenu(p, 0, false);
                    } else p.sendMessage("§8Телепортер перезаряжается! Осталось: "+Timer.CD_left(p.getName(), "tpa_command")+" сек.!");
                }
        } else p.sendMessage( "§cТелепорт по запросу отключён на этом сервере! (command.tpa<0)");
            break;
        
    case "tpo":
//System.out.println("--tpa_command="+tpa_command);
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
        if (tpa_command>0) {
                if (p.hasPermission("ostrov.tpo")) {
                    TPAListener.openTPmenu(p, 0, true);
                } else p.sendMessage( "§cУ вас нет права ostrov.tpo!!");
             } else p.sendMessage( "§cТелепорт отключён на этом сервере! (command.tpa<0)");
              break;
    case "tpaccept":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
             if (tpa_command>0) {
                if ( ! TPAListener.Tp_accept(p)) p.sendMessage( "§cCписок запросов не телепорт пуст!"); 
             } else p.sendMessage( "§cТелепорт по запросу отключён на этом сервере!");
              break;
        
        
    case "givemenu":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
        if (Ostrov.lobby_items.hasItem("pipboy")){
            if (!Ostrov.lobby_items.giveItem(p, "pipboy")) p.sendMessage( "§cУ Вас уже есть часики!");
        } else p.sendMessage( "§cЧасики отключены на этом сервере!");
               break;
        
    case "tpr":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            if ( tpr_command > 1 ){
                // if (p.isOp() || p.hasPermission("ostrov.tpr")){
                     if (!Timer.CD_has( p.getName(), "tpr_command" ) ) {
                         Timer.CD_add( p.getName(), "tpr_command", tpr_command);
                         Tpr.runCommand(p);
                    } else p.sendMessage("§8Телепортер перезаряжается! Осталось: "+Timer.CD_left(p.getName(), "tpr_command")+" сек.!");
               // } else p.sendMessage("§cУ Вас нет пава ostrov.tpr !");
            }else p.sendMessage( "§cТелепорт в случайное место отключён на этом сервере!");
            break;
        
    case "top":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
             if ( top_command ){
                 if ( p.hasPermission("ostrov.top")){
                     ApiOstrov.teleportSave(p, p.getWorld().getHighestBlockAt(p.getLocation()).getLocation() );
                } else p.sendMessage("§cУ Вас нет пава ostrov.top !");
            }else p.sendMessage( "§ctop отключёна на этом сервере!");
            break;
        
    case "world":       //! указывать PlayerTeleportEvent.TeleportCause.PLUGIN
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            if ( world_command || ApiOstrov.isLocalBuilder(sender, false) ){
                if ( p.hasPermission("ostrov.world") || ApiOstrov.isLocalBuilder(sender, false)) {
                    
                    WorldManagerCommand.openWorldMenu(p);
                    /*if (worlds==null) {
                        worlds = Bukkit.createInventory( null, 18,  "§2Смена мира" );
                        int pos=1;
                        for (World w: Bukkit.getWorlds()) {
                            switch (w.getEnvironment()) {
                                case NORMAL:
                                    CMD.worlds.setItem(pos, new ItemBuilder(Material.GRASS).name(w.getName()).build() );
                                    break;
                                case NETHER:
                                    CMD.worlds.setItem(pos,new ItemBuilder(Material.NETHERRACK).name(w.getName()).build() );
                                    break;
                                case THE_END:
                                    CMD.worlds.setItem(pos, new ItemBuilder(Material.END_STONE).name(w.getName()).build() );
                                    break;
                                default:
                                    CMD.worlds.setItem(pos, new ItemBuilder(Material.WHITE_GLAZED_TERRACOTTA).name(w.getName()).build() );
                                    break;
                            }
                            pos++;
                        }
                    }
                    p.openInventory(worlds);*/
                    
                } else p.sendMessage("§cУ Вас нет пава ostrov.world !");
            } else p.sendMessage( "§cСмена мира командой world отключён на этом сервере!");
              break;
        
    case "settings":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            if ( settings_command ){
                p.openInventory(PM.OP_Get_settings(p));
            }else p.sendMessage( "§cЛичные настройки отключёны на этом сервере!");
        break;
              
    case "spawn":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            if ( spawn_command ){
                if (Ostrov.getWarpManager().exist("spawn")) {
                    ApiOstrov.teleportSave(p, Ostrov.getWarpManager().getWarp("spawn").loc, true);
                //p.performCommand("warp spawn");
            } else {
                ApiOstrov.teleportSave(p, Bukkit.getWorlds().get(0).getSpawnLocation(), true);
            }
            //ApiOstrov.teleportSave(p, Bukkit.getWorlds().get(0).getSpawnLocation());
            }else p.sendMessage( "§cspawn отключёна на этом сервере!");
        break;
              
            
            
            
            
            
            
            
        
        
            
            
            
            
            
            
            
            
    case "back":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            if ( back_command ){
                if ( p.hasPermission("ostrov.back") ) {
                    Location b1 = p.getLocation();
                    if ( ! ApiOstrov.teleportSave(p, PM.OP_Get_back_location(p.getName()) )) {
                        p.sendMessage( "§cТелепорт в место гибели слишком опасен!"); 
                        return false;
                    } else PM.OP_Set_back_location(p.getName(), b1);
                } else p.sendMessage("§cУ Вас нет пава ostrov.back !");
            }else p.sendMessage( "§cВозврат в место гибели отключён на этом сервере!");
              break;
        
     case "get":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            if ( get_command ){
                if ( p.hasPermission("ostrov.get") ) {
                    if (arg.length == 2) {
                        if ( !isNumber(arg[1])) {p.sendMessage( "§cКолличество должно быть числом!");return false;}
                        //if (Integer.valueOf(arg[1]) <0 || Integer.valueOf(arg[1])>640 ) {p.sendMessage( "§cОт 0 до 640!");return false;}
                            ItemStack i = new ItemStack(Material.matchMaterial(arg[1]), Integer.valueOf(arg[1]) );
                        if (i==null || i.getType()==Material.AIR) {
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "give "+p.getName()+" "+arg[0]+" "+arg[1] );
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "give "+p.getName()+" "+i.getType().toString().toLowerCase().replaceAll("_", "")+" "+arg[1] );
                        }
                    } else p.sendMessage("§cФормат: get <ид/название> <кол-во>");
                } else p.sendMessage("§cУ Вас нет пава ostrov.get !");
            }else p.sendMessage( "§cget отключёна на этом сервере!");
        break;
              
     case "gm":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            //if ( gm_command ){
                if ( (gm_command && p.hasPermission("ostrov.gm")) || ApiOstrov.canBeBuilder(p) )  {
                    if (arg.length == 1) {
                            switch (arg[0]) {
                                case "0":
                                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode survival "+p.getName() );
                                    break;
                                case "1":
                                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode creative "+p.getName() );
                                    break;
                                case "2":
                                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode adventure "+p.getName() );
                                    break;
                                case "3":
                                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode spectator "+p.getName() );
                                    break;
                                default:
                                    p.sendMessage("§cФормат: gm <0..3>");
                                    break;
                            }
                        
                            //Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode "+arg[0]+" "+p.getName() );
                    } else p.sendMessage("§cФормат: gm <0..3>");
                } else {
                    if (!gm_command) {
                        p.sendMessage( "§cGm отключёна на этом сервере!");
                    } else {
                        p.sendMessage("§cУ Вас нет пава ostrov.gm !");
                    }
                }
            //}else p.sendMessage( "§cGm отключёна на этом сервере!");
        break;
              
             
     case "tppos":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            if ( tppos_command ){
                if ( p.hasPermission("ostrov.tppos") ) {
                    if (arg.length == 3) {
                        if ( isNumber(arg[0]) && isNumber(arg[1]) && isNumber(arg[2]) ) {
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "tp "+p.getName()+" "+arg[0]+" "+arg[1]+" "+arg[2] );
                        } else {
                            p.sendMessage( "§cКоординаты должны быть числами!");
                            return false;
                        }
                    } else p.sendMessage("§cФормат: tppos <x> <y> <z>");
                } else p.sendMessage("§cУ Вас нет пава ostrov.tppos !");
            }else p.sendMessage( "§ctppos отключёна на этом сервере!");
        break;
              
     case "tphere":
        if (p==null) {sender.sendMessage(Ostrov.prefix+"§сне консольная команда!"); return true;}
            if ( tphere_command ){
                if ( p.hasPermission("ostrov.tphere") ) {
                    if (arg.length == 1) {
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "tp "+arg[0]+" "+p.getName() );
                    } else p.sendMessage("§cФормат: tphere <ник>");
                } else p.sendMessage("§cУ Вас нет пава ostrov.tphere !");
            }else p.sendMessage( "§ctphere отключёна на этом сервере!");
        break;
              
              
             
              
              
              
              
              
              
              
              
              




    case "operm":
        if (arg.length==0) { //админ - права других
            sender.sendMessage("§c/operm <ник> [право]");
            return false;
        }
        
            if(!ApiOstrov.isLocalBuilder(sender, true) && !arg[0].equals(sender.getName())) {
                arg[0] = sender.getName();
                sender.sendMessage("§cВы можете посмтотреть только свои права!");
            }
            if (Bukkit.getPlayer( arg[0] )==null) {
                sender.sendMessage("§cИгрок не найден!");
                return false;
            }
            sender.sendMessage("");
            sender.sendMessage("");
            
        if (arg.length==1) { 
            
            sender.sendMessage("§fПрава "+arg[0]+", §atrue §7/ §cfalse");

            for (PermissionAttachmentInfo  attacement_info : Bukkit.getPlayer(arg[0]).getEffectivePermissions()) {
                sender.sendMessage( (attacement_info.getValue()?"§a":"§c")+attacement_info.getPermission() );
            }
            
        } else if (arg.length==2) {
            
            sender.sendMessage("§f"+arg[0]+" §7право "+arg[1]+" : "+ (Bukkit.getPlayer(arg[0]).hasPermission(arg[1])? "§aДа" : "§4Нет") );

        }

            break;




/*
    case "oreload":
            if ( !p.isOp()) return false;
            if (arg.length==0) {
                Conf.ReLoadConfig();
                p.sendMessage( "§aКонфиг перезагружен! §8(/oreload <модуль> - помодульно)");
            }else if (arg.length==1) {
                switch (arg[1]) {
                    case "moblimit":
                        Ostrov.mob_limit.Reload();
                        break;
                    default:
                        p.sendMessage( "§cМодули: moblimit, ");
                        break;
                }
                p.sendMessage( "§aКонфиг перезагружен! §8(/oreload <модуль> - помодульно)");
            }
              break;*/

        
   /* case "okill":
            if ( !ApiOstrov.isLocalBuilder(p, true)) return false;
            switch (arg.length) {
                case 1:
                    if ( isNumber(arg[0]) ) {
                        int r = Integer.valueOf(arg[0]);
                        int rem=0;
                        for (Entity e: p.getNearbyEntities(r, r, r)) {
                            //p.getNearbyEntities(r, r, r).stream().forEach((e) -> {
                            if ( !( e instanceof Player)) {
                                e.remove();
                                rem++;
                            }
                        }
                        p.sendMessage( "§eВ радиусе §b"+r+" §eнайдено и убрано сущностей: §b"+rem);
                        return true;
                    } else p.sendMessage( "радиус должен быть числом!");
                    break;
                case 2:
                    if ( isNumber(arg[0]) ) {
                        //MobMeta group=MobGroup.valueOf(arg[1].toUpperCase());
                        EntityGroup group=null;
                        if (VM.getNmsEntitygroup().isGroup(arg[1])) group=VM.getNmsEntitygroup().byTag(arg[1]);
                        EntityType type=null;
                        if (group==null) {
                            try {
                                type = EntityType.valueOf(arg[1].toUpperCase());
                            } catch (IllegalArgumentException ex) {}
                            if (type==null) {
                                p.sendMessage("§cГруппы: CREATURE,MONSTER,AMBIENT,WATER_CREATURE,OTHER или тип (BOAT,MINECART,ARROW,DROPPED_ITEM....)");
                                return true;
                            }
                        }
                        
                        int r = Integer.valueOf(arg[0]);
                        int rem=0;
                        for (Entity e: p.getNearbyEntities(r, r, r)) {
                            //p.getNearbyEntities(r, r, r).stream().forEach((e) -> {
                            if ( e.getType()!=EntityType.PLAYER) {
                                //if (group!=null && MobGroup.get_MobGroup(e)==group ) {
                                if (group!=null && VM.getNmsEntitygroup().getEntytyType(e)==group ) {
                                    e.remove();
                                    rem++;
                                } else if (type!=null && e.getType()==type ) {
                                    e.remove();
                                    rem++;
                                } else {
                                    e.remove();
                                    rem++;
                                }
                            }
                        }
                        p.sendMessage( "§eВ радиусе §b"+r+" §eнайдено и убрано сущностей: §b"+rem+" §e"+(group!=null?"группа="+group.toString():" (группа не указана)")+(type!=null?"тип="+type.toString():" (тип не указан)") );
                        return true;
                    } else p.sendMessage( "§cрадиус должен быть числом!");
                    break;
                default:
                    p.sendMessage( "§cokill <радиус> [тип]");
                    break;
            }
              break;*/
        
    case "entity":
        EntityCmd.execute(p, arg);
            break;
        
    case "blockstate":
        BlockstateCmd.execute(p, arg);
            break;
        
    case "biome":
        p.sendMessage("§fВы находитесь в биоме: "+p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
            break;
        
    case "ohelp":
            if ( arg.length == 0 )  {
                ChatMsgUtil.Help(p,0);
            }
            else {
                if ( arg.length == 1 ) {
                    if (isNumber(arg[0])) {
                        ChatMsgUtil.Help(p,Integer.valueOf(arg[0]));
                        //Help(p,Integer.valueOf(arg[0]));
                        return true;
                    } p.sendMessage( "§cНаберите /help <страница> или просто  /help");
                } else p.sendMessage( "§cНаберите /help <страница> или просто  /help");
            } 
              break;
              

                
    case "sound":
            if ( ApiOstrov.isLocalBuilder(sender, true) )  {
                SmartInventory.builder()
                    .id("Sounds"+p.getName())
                    .provider(new Sounds())
                    .size(6, 9)
                    .title("§2Звуки")
                    .build()
                    .open(p);
            }


            default:
                break;
    }
    
    
    
    
        
        
        
         
                return true;
        
    }    
        






        
public static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }    




















  /*  case "pinfo":
            if ( p.isOp() || p.hasPermission("ostrov.pinfo") ) {
            if ( arg.length != 1 ) {  p.sendMessage( "§c/pinfo <ник>"); return false; }
                Player about = Bukkit.getPlayer(arg[0]);
                if (about!=null) {
                    //p.sendMessage( "§6Информация по §b"+arg[0]+" §6- сейчас на сервере!");
                    //p.sendMessage( "§5IP "+about.getAddress().getAddress());
                    p.sendMessage( "§6Информация по §b"+arg[0]+" §6от сервера §b"+Bukkit.getMotd()+" §2- сейчас на сервере!");
                    p.sendMessage( "§5Первый вход: §7"+  (new Date(about.getFirstPlayed())) );
                    p.sendMessage( "§5Последний выход: §7"+  (new Date(about.getLastPlayed())) );
                    //p.sendMessage( "§5Наиграл: §7"+  Ostrov.GetPlayTime(about) );
                    p.sendMessage( "§5Группы: §7"+  Ostrov.GetGroups(about) );
                    //p.sendMessage( "§5Деньги: §7"+  Ostrov.GetBalance(about) );
                } else {
                OfflinePlayer off = Bukkit.getOfflinePlayer(arg[0]);
                    if ( off.hasPlayedBefore()) {
                    p.sendMessage( "§6Информация по §b"+arg[0]+" §6от сервера §b"+Bukkit.getMotd()+" §4- сейчас оффлайн!");
                        p.sendMessage( "§5Первый вход: "+  (new Date(off.getFirstPlayed())) );
                        p.sendMessage( "§5Последний выход: "+  (new Date(off.getLastPlayed())) );
                    } else p.sendMessage( "§c"+arg[0]+ " никогда не играл на этом сервере!");
                } 
            } else p.sendMessage( "§cЭто команда доступна модераторам!");
              break;
*/
  /*     case "invsee":
            if ( invsee_command ){
                CmdInvSee.InvSee(p, cmd, home, arg);
              if ( p.hasPermission("ostrov.invsee") || p.isOp() ) {
                    if ( arg.length == 1 ) {
                        Player target = Bukkit.getPlayer(arg[0]);
                             if (target!=null) {
                                if  ( p.hasPermission("ostrov.invsee.modify") || p.isOp() ) {
                                    p.closeInventory();
                                    p.openInventory(target.getInventory());
                                } else {
                                    Inventory inv = Bukkit.createInventory( p, 45,  "§1Инвентарь игрока "+arg[0] );
                                    inv = p.getInventory();
                                    p.openInventory(inv);
                                }                                 
                            } else p.sendMessage("§cИгрок "+arg[0]+" не найден!");
                    } else if ( arg.length == 2 &&  arg[1].equals("armor") ) {
                        Player target = Bukkit.getPlayer(arg[0]);
                            if (target!=null) {
                                Inventory inv = Bukkit.createInventory( p, 9,  "§1Аммуниция игрока "+arg[0] );
                                inv.setContents(target.getInventory().getArmorContents());
                                p.closeInventory();
                                p.openInventory(inv);
                                return true;
                            } else p.sendMessage("§cИгрок "+arg[0]+" не найден!");
                    } else p.sendMessage("§cФормат: invsee <ник> [armor]");
                } else p.sendMessage("§cУ Вас нет пава ostrov.invsee !");
            }else p.sendMessage( "§cinvsee отключёна на этом сервере!");
        break;
              
     case "ender":
            if ( invsee_command ){
                if ( p.hasPermission("ostrov.ender") || p.isOp() ) {
                    if ( arg.length == 1 ) {
                        Player target = Bukkit.getPlayer(arg[0]);
                            if (target!=null) {
                                Inventory inv = Bukkit.createInventory( p, 9,  "§1Эндэр-сундук игрока "+arg[0] );
                                //inv.setContents(target.getInventory().getArmorContents());
                                p.closeInventory();
                                p.openInventory(inv);
                                return true;
                            } else p.sendMessage("§cИгрок "+arg[0]+" не найден!");
                    } else p.sendMessage("§cФормат: ender <ник>");
                } else p.sendMessage("§cУ Вас нет пава ostrov.ender !");
            }else p.sendMessage( "§cender отключёна на этом сервере!");
        break;*/
              
    
}    
            

