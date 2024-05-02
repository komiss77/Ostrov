package ru.komiss77.commands;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.*;
import ru.komiss77.builder.menu.Sounds;
import ru.komiss77.builder.menu.ViewPerm;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.modules.figures.MenuMain;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.GameMenu;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.modules.player.profile.TPA;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.modules.warp.WarpManager;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.inventory.SmartInventory;

public class CMD  {


    public static boolean CommandHamdler(CommandSender sender, Command cmd, String label, String[] arg) {
        if (Ostrov.MOT_D.length()==3 || sender == null) return false;

        final Player p = sender instanceof Player ? (Player) sender : null;
        final Oplayer op = p == null ? null : PM.getOplayer(p);
        
        String home;
        int limit;
        
        //if ( pvp_battle_time > 1 && PM.inBattle(s) ) {            -- перенёс в PlayerCommandPreprocessEvent
        //    p.sendMessage( "§cРежим боя - команды заблокированы! Осталось " + Timer.CD_left(p.getName(), "pvp") + " сек." );
        //    return false;
       // }
        
    switch (label) {

        case "serv":
            if (op == null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            SmartInventory.builder()
                .id(op.nik + "Game")
                .title(op.eng ? Section.РЕЖИМЫ.item_nameEn : Section.РЕЖИМЫ.item_nameRu)
                .provider(new GameMenu(op.menu.section == Section.МИНИИГРЫ))
                .size(6, 9)
                .build()
                .open(p);
            break;

        case "profile":
            if (op == null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            if (op.isGuest) {
                sender.sendMessage(Ostrov.PREFIX+"§eИгровые данные Гостей не сохраняются!");
                return true;
            }
            op.menu.open(p,Section.ПРОФИЛЬ);//p.openInventory(GM.main_inv);
            break;

        case "menu":
            if (op == null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            PM.getOplayer(p).menu.openLocalMenu(p);//p.performCommand(Cfg.GetCongig().getString("modules.command.menu"));
            break;

        case "settings":
            if (op == null) {sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!"); return true;}
                if ( Config.settings_command ) {
                    op.menu.openLocalSettings(p, true);
                } else {
                    p.sendMessage( "§cЛичные настройки отключёны на этом сервере!");
                }
            break;





        case "figure":
            if (op == null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            if (ApiOstrov.isLocalBuilder(p, true)) {
                SmartInventory.builder()
                .id("MenuMain"+p.getName())
                .provider(new MenuMain())
                .size(1, 9)
                .title("§fФигуры")
                .build()
                .open(p);
            }
            break;



        case "sethome":
            if (op == null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            if (!Config.home_command) {
                p.sendMessage("§c"+Lang.t(p, "Дома отключены на этом сервере!"));
                return false;
            }
            home = "home";
            if ( arg.length == 1 ) {                                       //определяем название
                if (arg[0].length()>10) {
                    p.sendMessage( "§c"+Lang.t(p,"Слишком длинное название дома!"));
                    return false;
                }
                home = arg[0];
            }
            
            if (  arg.length ==0 && op.homes.size()>1 ) { //если не указал дом, но их больше 1 - уточнить какой
                final TextComponent.Builder homes = Component.text().content("§b"+Lang.t(p,"Какую точку дома обновить? "));
                for (final String homeName : op.homes.keySet()) {
                    homes.append(Component.text("§b- §e"+homeName+" ")
                		.hoverEvent(HoverEvent.showText(Component.text("§7"+Lang.t(p, "Клик - обновить точку дома")+" §6" + homeName)))
                		.clickEvent(ClickEvent.runCommand("/sethome " + homeName)));
                }
                sender.sendMessage(homes.build());

                return false;
            }
            limit = Perm.getLimit(op, "home");
            if ( op.homes.containsKey(home) ) {      //если есть такой, обновляем
                op.homes.put(home, LocationUtil.toString(p.getLocation()));//PM.OP_SetHome(p, home);
                op.mysqlData.put("homes", null); //пометить на сохранение
                p.sendMessage( "§2"+Lang.t(p, "Для дома ")+home+Lang.t(p, " установлена новая позиция."));
                return true;
            }  else if ( op.homes.size()>=limit ) { //если ставим новый дом, проверяем лимит     
                p.sendMessage( "§c"+Lang.t(p, "Лимит точек дома для вашей группы: ")+limit+", "+Lang.t(p, "Ваши дома")+": §6"+ApiOstrov.listToString(op.homes.keySet(), ","));
                p.sendMessage( "§c"+Lang.t(p, "Удалите ненужный командой")+" /delhome");
                return false;
            } else {
                op.homes.put(home, LocationUtil.toString(p.getLocation()));//PM.OP_SetHome(p, home);
                op.mysqlData.put("homes", null); //пометить на сохранение
                //if (home.equals("home")) p.setBedSpawnLocation(p.getLocation());
                p.sendMessage( "§2"+Lang.t(p, "Дом ")+((home.equals("home"))?"":home)+Lang.t(p, " установлен!"));
            }
            break;

        case "delhome":
            if (op == null) {sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!"); return true;}
            if (Config.home_command) {
                if (  arg.length ==0 && op.homes.size()>1 ) { //если не указал дом, но их больше 1 - уточнить какой
                	final TextComponent.Builder homes = Component.text().content("§c"+Lang.t(p, "Какой дом удалить? "));
                    for (final String homeName : op.homes.keySet()) {
                        homes.append(Component.text("§b- §e"+homeName+" ")
                    		.hoverEvent(HoverEvent.showText(Component.text("§7"+Lang.t(p, "Клик - удалить точку дома")+" §6"+homeName)))
                    		.clickEvent(ClickEvent.runCommand("/delhome " + homeName)));
                    }
                    sender.sendMessage(homes.build());
                    return false;
                }
                home = "home";
                if ( arg.length == 1 )  home = arg[0];
                if ( op.homes.containsKey(home) ) {
                    op.homes.remove(home);
                    op.mysqlData.put("homes", null); //пометить на сохранение
                    p.sendMessage( "§4"+Lang.t(p, "Точка дома ")+ (home.equals("home")? "":home)+Lang.t(p, " удалена!"));
                } else p.sendMessage( "§c"+Lang.t(p, "Нет такого дома! Ваши дома:")+" §6"+ApiOstrov.listToString(op.homes.keySet(), ",") );
                break;
            } else p.sendMessage( "§c"+Lang.t(p, "Дома отключены на этом сервере!"));


            
            
        case "lobby":
        case "hub":
            if (op == null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            p.performCommand("server lobby");//ApiOstrov.sendToServer(p, "lobby0", "");
            break;


        case "fly":
            if (op == null) {sender.sendMessage("§сне консольная команда!"); return true;}
            if (!Config.fly_command) {p.sendMessage( "§c"+Lang.t(p, "Полёт отключён на этом сервере!"));return false;}
                if ( p.hasPermission("ostrov.fly") ) {
                    switch (arg.length) {
                        case 0:
                            if (op.allow_fly && p.getAllowFlight()) {
                            	op.allow_fly = false;
                                p.setFlying(false);
                                p.setAllowFlight(false);
                                p.setFallDistance(0);
                                p.sendMessage("§e"+Lang.t(p, "Режим полёта выключен!"));
                                return true;
                            } else if (op.pvp_time == 0) {
                            	op.allow_fly = true;
                                p.setAllowFlight(true);
                                p.sendMessage("§a"+Lang.t(p, "Режим полёта включен!"));
                                return true;
                            }
                            break;
                        case 1:
                            switch (arg[0]) {
                                case "on" -> {
                                    p.setAllowFlight(true);
                                    // p.setFlying(true);
                                    p.sendMessage( "§6"+Lang.t(p, "Режим полёта включен!"));
                                    return true;
                                }
                                case "off" -> {
                                    p.setFlying(false);
                                    p.setAllowFlight(false); 
                                    p.sendMessage( "§6"+Lang.t(p, "Режим полёта выключен!"));
                                    return true;
                                }
                                default -> p.sendMessage( "§c ?   §f/fly,  §f/fly on,  §f/fly off");
                            }
                            break;

                        default:
                            p.sendMessage( "§c ?   §f/fly,  §f/fly on,  §f/fly off");
                            break;
                    }
                } else p.sendMessage( "§cНет права ostrov.fly!");
                break;

        case "tpa":
        case "tpo":
            if (p==null || op==null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            TPA.onTpaCmd(p, op, arg.length>=1 ? arg[0] : null);
            break;


        case "tpaccept": //для принятия ТП по запросу. Модеры переносятся сразу в меню
            if (p==null) {
                sender.sendMessage("§сне консольная команда!"); 
                return true;
            }
            if (arg.length==0) {
                sender.sendMessage("§счей запрос надо принять??!"); 
                return true;
            }
            final Oplayer from = PM.getOplayer(arg[0]);
            if (from==null) {
                sender.sendMessage("§c"+arg[0]+"§с"+Lang.t(p, "уже нет на сервере!")); 
                return true;
            }
            if (!Timer.has(p, "tp_request_from_"+arg[0])) {
                sender.sendMessage("§с"+Lang.t(p, "запрос не отправлялся или устарел!")); 
                return true;
            }
            int price=getTpPrice(from.getPlayer(), p.getLocation());
            if (price>0) {
                if (ApiOstrov.moneyGetBalance(from.nik)<price) {
                    from.getPlayer().sendMessage("§cУ Вас недостаточно лони для телепорта!");
                    p.sendMessage("§cУ "+from.nik+" недостаточно лони для телепорта!");
                    return true;
                }
                ApiOstrov.moneyChange(from.getPlayer(), -price, "телепорт к "+p.getName());
            }
            Timer.del(p, "tp_request_from_"+arg[0]); //баг: тыкают много раз принять и снимают деньги
            Timer.add(from.getPlayer(), "tpa_command", Config.tpa_command); //задержка даётся вызывающему
            DelayTeleport.tp(from.getPlayer(), p.getLocation(), 3, "Вы переместились к "+p.getName(), true, true, DyeColor.YELLOW);
            break;


        case "givemenu":
            if (p==null) {sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!"); return true;}
            if (MenuItemsManager.hasItem("pipboy")){
                if (!MenuItemsManager.giveItem(p, "pipboy")) p.sendMessage( "§cУ Вас уже есть предмет-меню!");
            } else p.sendMessage( "§cЧасики отключены на этом сервере!");
            break;

        

        case "top":
            if (p==null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            if ( Config.top_command ) {
                if ( p.hasPermission("ostrov.top")){
                   DelayTeleport.tp(p, p.getWorld().getHighestBlockAt(p.getLocation()).getLocation().add(0, 1, 0), 3, "Наивысшая точка над Вами..", true, true, DyeColor.BLUE);
                   //ApiOstrov.teleportSave(p, p.getWorld().getHighestBlockAt(p.getLocation()).getLocation(), false );
               } else p.sendMessage("§cУ Вас нет пава ostrov.top !");
            } else {
                p.sendMessage( "§ctop отключёна на этом сервере!");
            }
            break;


        case "spawn":
            if (op == null) {sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!"); return true;}
                if ( Config.spawn_command ){
                    if (WarpManager.exist("spawn")) {
                        DelayTeleport.tp(p, WarpManager.getWarp("spawn").getLocation(), 3, Lang.t(p, "Вы перемещены на спавн"), true, true, DyeColor.GREEN);
                } else {
                    DelayTeleport.tp(p, Bukkit.getWorlds().get(0).getSpawnLocation(), 3, Lang.t(p, "Вы перемещены на спавн"), true, true, DyeColor.GREEN);
                }
                } else {
                    p.sendMessage( "§c"+Lang.t(p, "spawn отключёна на этом сервере!"));
                }
            break;


        case "back":
            if (op == null) {sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!"); return true;}
                if ( Config.back_command ){
                    if ( p.hasPermission("ostrov.back") ) {
                        Location b1 = p.getLocation();
                        //if ( ! ApiOstrov.isLocationSave(p, op.last_death )) {
                        //    p.sendMessage( "§cТелепорт в место гибели слишком опасен!"); 
                        //    return false;
                        //} else {
                            DelayTeleport.tp(p, op.last_death, 3, Lang.t(p, "Вы вернулись на предыдущую позицию"), true, true, DyeColor.BROWN);
                            op.last_death = b1;//PM.OP_Set_back_location(p.getName(), b1);
                        //}
                    } else p.sendMessage("§c"+Lang.t(p, "У Вас нет пава ostrov.back !"));
                }else p.sendMessage( "§c"+Lang.t(p, "Возврат в место гибели отключён на этом сервере!"));
                break;

         case "get":
            if (p==null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!"); 
                return true;
            }
            if ( Config.get_command ){
                if ( p.hasPermission("ostrov.get") ) {
                    if (arg.length == 2) {
                        if ( !ApiOstrov.isInteger(arg[1])) {p.sendMessage( "§cКолличество должно быть числом!");return false;}
                        //if (Integer.valueOf(arg[1]) <0 || Integer.valueOf(arg[1])>640 ) {p.sendMessage( "§cОт 0 до 640!");return false;}
                            ItemStack i = new ItemStack(Material.matchMaterial(arg[1]), Integer.parseInt(arg[1]) );
                        if (i.getType() == Material.AIR) {
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "give "+p.getName()+" "+arg[0]+" "+arg[1] );
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "give "+p.getName()+" "+i.getType().toString().toLowerCase().replace("_", "")+" "+arg[1] );
                        }
                    } else p.sendMessage("§cФормат: get <ид/название> <кол-во>");
                } else p.sendMessage("§cУ Вас нет пава ostrov.get !");
            }else p.sendMessage( "§cget отключёна на этом сервере!");
            break;

         case "gm":
            if (op == null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            if ( (Config.gm_command && p.hasPermission("ostrov.gm")) || ApiOstrov.canBeBuilder(p) )  {
                if (arg.length == 1) {
                    switch (arg[0]) {
                        case "0" -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode survival "+p.getName() );
                        case "1" -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode creative "+p.getName() );
                        case "2" -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode adventure "+p.getName() );
                        case "3" -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode spectator "+p.getName() );
                        default -> p.sendMessage("§cФормат: gm <0..3>");
                    }
                } else {
                    p.sendMessage("§cФормат: gm <0..3>");
                }

            } else if (op.hasGroup("youtuber")) {
                if (arg.length == 1) {
                        switch (arg[0]) {
                            case "0" -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode survival "+p.getName() );
                            case "3" -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode spectator "+p.getName() );
                            default -> p.sendMessage("§cФормат: gm <0..3>");
                        }
                } else {
                    p.sendMessage("§cФормат: gm <0..3>");
                }
            } else {
                if (!Config.gm_command) {
                    p.sendMessage( "§cGm отключёна на этом сервере!");
                } else {
                    p.sendMessage("§cУ Вас нет пава ostrov.gm !");
                }
            }
            break;


         case "tppos":
            if (op == null) {
                sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!");
                return true;
            }
            if ( Config.tppos_command || op.hasGroup("youtuber")) {
                if ( p.hasPermission("ostrov.tppos") || op.hasGroup("youtuber") ) {
                    if (arg.length == 3) {
                        if ( ApiOstrov.isInteger(arg[0]) && ApiOstrov.isInteger(arg[1]) && ApiOstrov.isInteger(arg[2]) ) {
                            DelayTeleport.tp(p, new Location(p.getWorld(), Double.parseDouble(arg[0]), Double.parseDouble(arg[1]), Double.parseDouble(arg[2])), 3, "Вы вернулись на указанную локацию", true, true, DyeColor.BROWN);
                            //Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "tp "+p.getName()+" "+arg[0]+" "+arg[1]+" "+arg[2] );
                        } else {
                            p.sendMessage( "§c"+Lang.t(p, "Координаты должны быть числами!"));
                            return false;
                        }
                    } else {
                        p.sendMessage("§cФормат: tppos <x> <y> <z>");
                    }
                } else {
                    p.sendMessage("§cУ Вас нет пава ostrov.tppos !");
                }
            } else {
                p.sendMessage( "§ctppos отключёна на этом сервере!");
            }
            break;

            
         case "tphere":
            if (p==null) {sender.sendMessage(Ostrov.PREFIX+"§сне консольная команда!"); return true;}
                if ( Config.tphere_command ){
                    if ( p.hasPermission("ostrov.tphere") ) {
                        if (arg.length == 1) {
                                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "tp "+arg[0]+" "+p.getName() );
                        } else p.sendMessage("§cФормат: tphere <ник>");
                    } else p.sendMessage("§cУ Вас нет пава ostrov.tphere !");
                }else p.sendMessage( "§ctphere отключёна на этом сервере!");
            break;
















        case "operm":
            if (arg.length==0 || (arg.length==1 && arg[0].equalsIgnoreCase(sender.getName()))) { //админ - права других
                SmartInventory.builder()
                    .id("Права "+sender.getName())
                    .provider(new ViewPerm((Player) sender))
                    .size(6, 9)
                    .title("Ваши права")
                    .build()
                    .open(p);
                //sender.sendMessage("§c/operm <ник> [право]");
                return false;
            }

            if(!ApiOstrov.isLocalBuilder(sender, true) && !arg[0].equals(sender.getName())) {
                arg[0] = sender.getName();
                sender.sendMessage("§c"+Lang.t(p, "Вы можете посмтотреть только свои права!"));
                return false;
            }
            if (Bukkit.getPlayer( arg[0] )==null) {
                sender.sendMessage("§c"+Lang.t(p, "Игрок не найден!"));
                return false;
            }

            if (arg.length==1) {
                SmartInventory.builder()
                    .id("Права "+arg[0])
                    .provider(new ViewPerm(Bukkit.getPlayer( arg[0] )))
                    .size(6, 9)
                    .title("Права "+arg[0])
                    .build()
                    .open(p);

            } else if (arg.length==2) {
                sender.sendMessage("§f"+arg[0]+" §7право "+arg[1]+" : "+ (Bukkit.getPlayer(arg[0]).hasPermission(arg[1])? "§aДа" : "§4Нет") );
            }
            break;


        case "blockstate":
            BlockstateCmd.execute(p, arg);
            break;

            
        case "biome":
            p.sendMessage("§fВы находитесь в биоме: "+p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
            break;

                
/*        case "ohelp":
            if ( arg.length == 0 )  {
                Help(p,0);
            } else {
                if ( arg.length == 1 ) {
                    if (ApiOstrov.isInteger(arg[0])) {
                        Help(p,Integer.parseInt(arg[0]));
                        //Help(p,Integer.valueOf(arg[0]));
                        return true;
                    } p.sendMessage( "§cНаберите /help <страница> или просто  /help");
                } else p.sendMessage( "§cНаберите /help <страница> или просто  /help");
            } 
            break;
*/

        case "sound":
            if ( ApiOstrov.isLocalBuilder(sender, true) )  {
                SmartInventory.builder()
                    .id("Sounds"+p.getName())
                    .provider(new Sounds(0))
                    .size(6, 9)
                    .title("§2Звуки")
                    .build()
                    .open(p);
            } else {
                p.sendMessage( "§cдоступно билдерам");
            }


        default:
            break;
    }








            return true;

        }    








/*	public static void Help (Player p, int page ) {

        for (int i=0; i<20; i++) {
            p.sendMessage("");
        }

        int limit = CMD.ostrov_commands.size();  

        int from = page*15;
        if (from > limit) {
            p.sendMessage("§cСтраниц всего "+ limit /15);
            return;
        }
        int to = from+15;
        if (to> limit) to = limit;

        if (page == 0) {
            p.sendMessage( "§2Помощь по командам Острова." );
        } else {
            p.sendMessage(Component.text("§eПредыдущая страница - клик сюда").clickEvent(ClickEvent.runCommand("/help "+ (page-1))));
        }

            for (int i=from; i<to; i++) {
                final String cmd = CMD.ostrov_commands.get(i);
                p.sendMessage(Component.text("§a§l"+cmd+" §f- "+Ostrov.instance.getDescription().getCommands().get(cmd).get("description").toString()
                    .replaceFirst("<vip>", "§3(привилегия)").replaceFirst("<moder>", "§3(модерская)")+" §8<- клик - набрать" )
                    .clickEvent(ClickEvent.suggestCommand("/"+cmd+" ")));
            }

        //p.sendMessage("§b* §3- требуют привилегии, §b** §3- модераторские");
        if (to<limit) {
                p.sendMessage(Component.text("§eСледующая страница - клик сюда")
                    .clickEvent(ClickEvent.runCommand("/help "+ (page+1))));
        }
    }

*/















  /*  case "pinfo":
            if ( p.isOp() || p.hasPermission("ostrov.pinfo") ) {
            if ( arg.length != 1 ) {  p.sendMessage( "§c/pinfo <ник>"); return false; }
                Player about = Bukkit.getPlayerExact(arg[0]);
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
                        Player target = Bukkit.getPlayerExact(arg[0]);
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
                        Player target = Bukkit.getPlayerExact(arg[0]);
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
                        Player target = Bukkit.getPlayerExact(arg[0]);
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
    
    public static int getTpPrice (final Player p, final Location loc) {
        if (p.hasPermission("ostrov.tpa.free")) return 0;
        //учесть разные миры   Cannot measure distance between world and world_the_end
        if (!p.getWorld().getName().equals(loc.getWorld().getName())) return 100;
        
        return 50;
    }                
    
}    
            

