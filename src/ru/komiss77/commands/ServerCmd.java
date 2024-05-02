package ru.komiss77.commands;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.games.GM;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.objects.CaseInsensitiveSet;

public class ServerCmd implements CommandExecutor, TabCompleter {

  private static final CaseInsensitiveSet displayNames; //для команды /server
  static {
    displayNames = new CaseInsensitiveSet();
    for (Game game : Game.values()) {
      if (game==Game.GLOBAL) continue;
      displayNames.add(game.suggestName);
    }
  }
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] strings) {
        
//System.out.println("l="+strings.length+" 0="+strings[0]);
        switch (strings.length) {
            
            case 1 -> {
                //0- пустой (то,что уже введено)
                return displayNames.stream().filter( name -> name.regionMatches(true, 0, strings[0], 0, strings[0].length()))
                    .limit(30).collect(Collectors.toList());
                //final List <String> sugg = new ArrayList<>();
                //for (String s : GM.allBungeeServersName) {
                //    if (s.startsWith(strings[0])) {
               //         sugg.add(s);
                //    }
               // }
                //return sugg;
            }

            case 2 -> {
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
//if (strings[0].equalsIgnoreCase("build") || strings[0].equalsIgnoreCase("destroy") ) {
                final Game game = Game.fromServerName(strings[0]);
//Ostrov.log("game="+game);
                if (game.type==ServerType.ARENAS) {
                    final GameInfo gi = GM.getGameInfo(game);
//Ostrov.log("GameInfo="+gi);
                    if (gi!=null) {
                        return (gi.getArenaNames(game.defaultServer));
                    }
                } else if (game.type==ServerType.LOBBY) {
                    final GameInfo gi = GM.getGameInfo(game);
                    if (gi!=null) {
                        return (gi.getArenaNames());
                    }
                }
                //   sugg.add("loni");
                //    sugg.add("permission");
                //   sugg.add("group");
                //  sugg.add("exp");
                //  sugg.add("reputation");
                ///}
            }

        }
        
        return ImmutableList.of();
    }    
    



    public ServerCmd() {
        //init();
    }


    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        final Player p = ( cs instanceof Player) ? (Player)cs : null;
        
        if ( p==null ) {
            cs.sendMessage("§cКоманда исполняется от имени игрока!");
            return false;
        }
        
        
        
        final Oplayer op = PM.getOplayer(p);
        
        p.getOpenInventory().close();
        
        
        if (arg.length==0) {
            //op.menu.open(p, Section.РЕЖИМЫ);
        	final TextComponent.Builder servers = Component.text().content("§bКлик на сервер: §e");
            for (final String serverName : displayNames) {
              servers.append(Component.text(serverName+"§7, §e")
            		.hoverEvent(HoverEvent.showText(Component.text("§7Клик - перейти")))
            		.clickEvent(ClickEvent.runCommand("/server "+serverName)));
            }
            p.sendMessage(servers.build());
            return true;
        }
        
        
        
        
        boolean hasLevel;
        boolean hasReputation;
        
        if (arg.length>=1) {
            
            String serverName = arg[0];
            
            if (serverName.equalsIgnoreCase("gui")) {
                op.menu.open(p, Section.РЕЖИМЫ);
                return true;
            }
            
            if (serverName.equalsIgnoreCase("jail")) {
                if (ApiOstrov.isLocalBuilder(p)) {
                    ApiOstrov.sendToServer(p, serverName,"");
                } else if (ApiOstrov.canBeBuilder(p)) {
                    p.sendMessage("§5Перейти в чистилище можно только в режиме билдера!");
                } else {
                    p.sendMessage("§5Перейти в чистилище могут только билдеры!");
                }
                return true;
            }
            
            if (op.getDataInt(Data.BAN_TO)>0) {
                p.sendMessage("§cВы сможете покинуть чистилище через "+ApiOstrov.secondToTime(op.getDataInt(Data.BAN_TO)-Timer.getTime()));
                return true;
            }

          final Game game = Game.fromServerName(serverName);
//Ostrov.log("onCommand serverName="+serverName+" game="+game);

          if (arg.length==1
            || game.type == ServerType.ONE_GAME //для больших аргументы не имеют значения
            || game.type == ServerType.LOBBY //для лобби аргументы не имеют значения
            || (arg.length==2 && arg[1].isEmpty()) ) {  //иногда в лобби отправляет как /server lobby0 [space] !

            if (game.type == ServerType.ONE_GAME) {

              //чекаем уровень и репу для перехода на серв вообще
              hasLevel = op.getStat(Stat.LEVEL) >= game.level;
              hasReputation = op.reputationCalc >= game.reputation;
              if (!hasLevel || !hasReputation) {
                p.sendMessage("§cДля перехода на данный сервер требуется уровень > " + game.level + " и репутация > " + game.reputation);
                return true;
              }
              if (game == Game.SE) {
                serverName = op.getTextData("sedna");//подставить сервер выхода с седны!?
                if (serverName.isEmpty() || !GM.allBungeeServersName.contains(serverName)) {
                  serverName = "sedna_wastes";
                }
//p.sendMessage("to Sedna:"+serverName);
              } else {
                serverName = game.defaultServer; //могло быть набрано /server Даария
              }

            } else if (game.type == ServerType.LOBBY) {

              if (arg[0].length() == 6 && arg[0].startsWith("lobby")) {
                serverName = arg[0]; //для лобби восстановить конкретный номер при прямом вводе
              } else {
                serverName = "lobby0";
              }

            } else if (game.type == ServerType.ARENAS) { //переход типа /server wz или /server поле_брани, без арены
              final GameInfo gi = GM.getGameInfo(game);
              if (gi==null) {
                p.sendMessage("§5Нет данных для игры "+game.name()+" - пробуем подключиться к §e"+game.defaultServer);
                serverName = game.defaultServer;
                //return true;
              } else if (gi.count()==0) {
                p.sendMessage("§5Для игры §6"+game.name()+" §5не найдено арен - пробуем подключиться к §e"+game.defaultServer);
                serverName = game.defaultServer;
                //return true;
              } else {
                serverName = gi.arenas().stream().findAny().get().server;//arenas.get(0).server;
              }
            }

            if (Ostrov.MOT_D.equalsIgnoreCase(serverName)) {
              p.sendMessage("§6Вы и так уже на этом сервере!");
              return true;
            } else {
              ApiOstrov.sendToServer(p, serverName, "");
              return true;
            }

          }

          //if (displayNames.contains(serverName)) {

         // }
          //далее сработает только если serverName указан напрямую, типа bw01

          //определяем арену, если указана как аргумент
          //одиночки сюда уже не дойдут
          final String arenaMane = arg[1];
          ArenaInfo ai = null;
          if (game == Game.GLOBAL) {
            ai = GM.lookup("", arenaMane);//пытаться найти арену по названию
          } else { //игра была определена (это могло быть типа /server поле_брани арена)
            final GameInfo gi = GM.getGameInfo(game);
            ai = gi.getArena(game.defaultServer, arenaMane);
            if (ai==null) {
              ai = GM.lookup("", arenaMane);//пытаться найти арену по названию
            }
          }
//Ostrov.log("arenaMane=>"+arenaMane+" ai="+ai);


          if (ai==null) { //арены не определить - просто на серв

            ApiOstrov.sendToServer(p, serverName, "");
            return true;

          } else {
//Ostrov.log("ai.server="+ai.server+" ai.arenaName="+ai.arenaName);

            //if (game.type!=ServerType.LOBBY && ai.server.equals(Ostrov.MOT_D)) {
            //    p.sendMessage("§6Вы и так уже на сервере с этой ареной!");
            //    return true;
            //}
            hasLevel =  op.getStat(Stat.LEVEL)>=ai.level;
            hasReputation =  op.reputationCalc>=ai.reputation;
            if (hasLevel && hasReputation) {
              ApiOstrov.sendToServer(p, ai.server, ai.arenaName);
            } else {
              p.sendMessage("§cДля перехода на данный сервер требуется уровень > "+ai.level+" и репутация > "+ai.reputation);
            }

          }
            //нераспознанные отправляем туда,куда набрал
            //if (game==null || game==Game.GLOBAL) {
            //    p.sendMessage("§5Не найдена игра с названием §b"+serverName);
            //    ApiOstrov.sendToServer(p, serverName, "");
            //    return true;
            //}
            //serverName = game.serverName;
            


            /*
            final GameInfo gi = GM.getGameInfo(game);
//Ostrov.log("game="+game+" gi="+gi); 
            if (gi==null || game.type==ServerType.ONE_GAME || game.type==ServerType.LOBBY || arg.length==1) { //в лобби отправляет как /server lobby0 [space] !

                if (game==Game.SE) {
                    serverName = op.getTextData("sedna");//подставить сервер выхода с седны!?
                    if (serverName.isEmpty() || !GM.allBungeeServersName.contains(serverName)) {
                        serverName = "sedna_wastes";
                    }
//p.sendMessage("to Sedna:"+serverName);
                } else if (game==Game.LOBBY) {
                    if ( arg[0].length()==6 && arg[0].startsWith("lobby")) {
                        serverName = arg[0]; //для лобби восстановить конкретный номер при прямом вводе
                    } else {
                        serverName = "lobby0";
                    }
                }
//Ostrov.log("serverName=>"+serverName+"<"); 
                if (Ostrov.MOT_D.equalsIgnoreCase(serverName)) {
                    p.sendMessage("§6Вы и так уже на этом сервере!");
                    return true;
                } else {
                    ApiOstrov.sendToServer(p, serverName, "");
                    return true;
                }
            }*/



            
        }
//System.out.print("Eco cs="+cs);


        
        return true;

    }
    



    

    
    
    
    
    
    
    
    
    


}
    
    
 
