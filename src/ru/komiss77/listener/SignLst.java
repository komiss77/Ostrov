package ru.komiss77.listener;
/*
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.LocaleUtils;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.ServerType;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.games.GameSign;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TCUtils;


public class SignLst implements Listener {
    public static Map<String,String[]> signCache;
    
    static {
        signCache = new HashMap<>(); 
    }
    


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void Sign_create(SignChangeEvent e) {
        final Player p = e.getPlayer();
    	final String line0 = TCUtils.stripColor(TCUtils.toString(e.line(0)));
        
        /*if (line0.equalsIgnoreCase("bs")) {
            if (!ApiOstrov.isLocalBuilder(p, false)) return;
            
            final String serverName = TCUtils.toString(e.line(1));
                if (serverName.isEmpty()) {
                    e.line(3, TCUtils.format("§4Ошибка!"));
                    p.sendMessage( "§4Строка 2 - сервер " );
                    return;
                }

                final Game game = Game.fromServerName(serverName);
                final GameInfo gi = GM.getGameInfo(game);
                if (gi==null) {
                    e.line(3, TCUtils.format("§4Ошибка!"));
                    p.sendMessage( "§4Нет игры для сервера "+serverName );
                    return;
                }

                if (!GM.allBungeeServersName.contains(serverName)) {
                    e.line(3, TCUtils.format("§4Ошибка!"));
                    p.sendMessage( "§4строка 2 - §fсервер. Доступные:" );
                    p.sendMessage( "§e"+ApiOstrov.listToString(GM.allBungeeServersName, ",") );
                    return;
                }


                final String arenaName = TCUtils.toString(e.line(2));
                if (game.type==ServerType.ARENAS && arenaName.isEmpty()) {
                    e.line(3, TCUtils.format("§4Ошибка!"));
                    p.sendMessage( "§аДля сервера с аренами §bстрока 2 §f- название арены с учётом регистра." ); 
                    p.sendMessage( "§аНайдены арены для сервера "+serverName+" :" ); 
                    p.sendMessage( "§e"+ApiOstrov.listToString(gi.getArenaNames(serverName), ",") );
                    return;
                }


                final String locAsString = ApiOstrov.stringFromLoc(e.getBlock().getLocation());

                GM.signs.put( locAsString, new GameSign(e.getBlock().getLocation(), serverName, arenaName));
                //добав в инфоб обновить
                final ArenaInfo ai = gi.getArena(serverName, arenaName);
                if (ai!=null) {
                    ai.signs.add(locAsString);
                    e.line(0, TCUtils.format(ai.line0));
                    e.line(1, TCUtils.format(ai.line1));
                    e.line(2, TCUtils.format(ai.line2));
                    e.line(3, TCUtils.format(ai.line3));
                }
                GM.gameSigns.set("signs."+locAsString+".server", serverName);
                GM.gameSigns.set("signs."+locAsString+".arena", arenaName);
                GM.gameSigns.saveConfig();

                if (arenaName.isEmpty()) {
                    e.getPlayer().sendMessage( "§6табличка для сервера §b" +  serverName+"§6 создана на локации "+ locAsString );
                } else {
                    e.getPlayer().sendMessage( "§6табличка для сервера §b" +  serverName +" §6и арены §b"+arenaName+"§6 создана на локации "+ locAsString );
                }
            return;
        }/
        
        if (line0.equalsIgnoreCase("[Команда]") || line0.equalsIgnoreCase("[Место]")) {
            if (!ApiOstrov.isLocalBuilder(p, true)) {
                e.line(0, Component.text("§8"+line0));
            } else {
                e.line(0, Component.text("§2"+line0));
            }
        } else {
            e.line(0, Component.text(line0.replaceAll("&", "§")));
        }
        
        e.line(1, Component.text(TCUtils.toString(e.line(1)).replaceAll("&", "§")));
        e.line(2, Component.text(TCUtils.toString(e.line(2)).replaceAll("&", "§")));
        e.line(3, Component.text(TCUtils.toString(e.line(3)).replaceAll("&", "§")));
    }

    
    



    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignClick(final PlayerInteractEvent e) {
        //if (e.getClickedBlock()==null || GM.signs.isEmpty()) return;
        //if (Tag.WALL_SIGNS.isTagged(e.getClickedBlock().getType()) || Tag.STANDING_SIGNS.isTagged(e.getClickedBlock().getType()) ) {
        if (e.getClickedBlock()!=null  && Tag.ALL_SIGNS.isTagged(e.getClickedBlock().getType()) ) {

            final String locAsString = LocationUtil.toString(e.getClickedBlock().getLocation());
            final GameSign gameSign = GM.signs.get(locAsString);

            if (gameSign!=null) {
                final Player p = e.getPlayer();

                if (Timer.has(p, "gameSign")) {
                    p.sendMessage("§8подождите 2 секунды..");
                    return;
                }
                Timer.add(p, "gameSign", 2);

                e.setUseInteractedBlock(Event.Result.DENY);
                e.setUseItemInHand(Event.Result.DENY); //если не отменять, то может сразу сработать слим выхода с арены


               /* if (e.getAction()==Action.LEFT_CLICK_BLOCK && ApiOstrov.isLocalBuilder(p, false)) {
                    //breack
                    e.getClickedBlock().breakNaturally();
                    GM.signs.remove(locAsString);  
                    GM.gameSigns.set("signs." + locAsString, null);
                    GM.gameSigns.saveConfig(); 
                    p.sendMessage("§6табличка для §b"+ gameSign.server+" : " + gameSign.arena+" §4удалена!");
                    final GameInfo gi = GM.getGameInfo(Game.fromServerName(gameSign.server));
                    if (gi!=null) {
                        ArenaInfo ai = null;
                        if ( gi.game.type==ServerType.ONE_GAME ) {
                            ai = gi.arenas.get(0);
                            //gi.arenas.get(0).signs.remove(locAsString);
                        } else if ( gi.game.type==ServerType.ARENAS || gi.game.type==ServerType.LOBBY ) {
                            ai = gi.getArena(gameSign.server, gameSign.arena);
                            //gi.getArena(gameSign.server, gameSign.arena).signs.remove(locAsString);
                        }
                        if (ai!=null ) {
                            ai.signs.remove(locAsString);
                        }
                    }
                    return;
                }/

                if (GM.GAME.type==ServerType.ARENAS) {
                    Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick( p, gameSign.arena ));
                } else {
                    ApiOstrov.sendToServer (p, gameSign.server, gameSign.arena);
                }
//p.sendMessage("info!!!!!");
            }

        }



    }



    
 

    
    
    
      
    
    
}
*/