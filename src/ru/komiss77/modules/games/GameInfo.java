package ru.komiss77.modules.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.GameInfoUpdateEvent;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;







public class GameInfo {
    
    public final Game game;
    
    private int gameOnline; //для одиночек, либо общий онлайн на аренах
    public final HashMap<Integer,ArenaInfo>arenas=new HashMap<>(); //position в меню, арена. String,Arena нельзя - могут быть одинаковые арены на разных серверах!!
    
    public Material mat;

     
    
    public GameInfo( final Game game ) {
        this.game = game;
        mat = Material.matchMaterial(game.mat);
        if (mat==null) mat = Material.BEDROCK;
        
        if (game.type==ServerType.ONE_GAME) {
             //для одиночек данные храним в нулевой арене
            final ArenaInfo ai = new ArenaInfo(this, game.suggestName, "", game.level, game.reputation, mat==null ? Material.BEDROCK : mat);
            arenas.put(0, ai);
            
        } else if (game.type==ServerType.ARENAS || game.type==ServerType.LOBBY) {
            
            //
            
        } 
        
    }
    


    public ItemStack getIcon (final Oplayer op) {
        final boolean hasLevel =  op.getStat(Stat.LEVEL)>=game.level;
        final boolean hasReputation =  op.reputationCalc>=game.reputation;

        switch (game.type) {

            case ONE_GAME:
                return new ItemBuilder(mat)
                    .name(game.displayName)
                    //.addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .addLore("")
                    .addLore(getState().displayColor+getState().name())
                    .addLore( gameOnline>=0 ? "§7Играют: "+gameOnline : "§4Сервер выключен" )
                    .addLore("")
                    .addLore( hasLevel && hasReputation ? (gameOnline >=0 ? "§a⊳ Клик - перейти на сервер" : "") : "§eНедоступен !")
                    .addLore(  hasLevel ? "§7Требуемый уровень : §6" +game.level : "§cБудет доступны с уровня §e"+game.level)
                    .addLore(  hasReputation ? "§7Требуемая репутация : §a>" +game.reputation : "§cДоступны при репутации §a>"+game.reputation)
                    .addLore("")
                    .addLore(game.description)
                    .build();


            case LOBBY:
                return new ItemBuilder(mat)
                    .name(game.displayName)
                    //.addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .addLore("")
                    .addLore(getState().displayColor+getState().name())
                    .addLore("")
                    .build();

            case ARENAS:
                //final boolean hasWarp = ApiOstrov.getWarpManager()!=null && ApiOstrov.getWarpManager().exist(game.name());
                return new ItemBuilder(mat)
                    .name(game.displayName)
                    //.addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .addLore("")
                    .addLore(getState().displayColor+getState().name())
                    .addLore( gameOnline>=0 ? "§7Играют: "+gameOnline : "" )
                    .addLore("")
                    .addLore( hasLevel && hasReputation ? (gameOnline >=0 ? "§a⊳ Клик - выбрать арену" : "") : "§eНедоступен !")
                    .addLore(  hasLevel ? "§7Требуемый уровень : §6" +game.level : "§cБудет доступны с уровня §e"+game.level)
                    .addLore(  hasReputation ? "§7Требуемая репутация : §a>" +game.reputation : "§cДоступны при репутации §a>"+game.reputation)
                    .addLore("")
                    .addLore(game.description)
                    .build();
                
                default:
                    return new ItemStack(Material.AIR);
                /*
                if (ApiOstrov.getWarpManager()!=null && ApiOstrov.getWarpManager().exist(game.name())) {
                    lore.set(1,"§a  Лев.клик - к табличкам");
                    lore.set(2,"§a⊳ Прав.клик - выбрать арену"); //line 1-4+разделитель
                } else {
                    lore.set(2,"§a⊳ Клик - выбрать арену");
                }

                        if (ApiOstrov.getWarpManager().exist(gameInfo.game.name())) {
                            p.closeInventory();
                            ApiOstrov.teleportSave(p, ApiOstrov.getWarpManager().getWarp(gameInfo.game.name()).loc, false);
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 2);
                        } else {
//System.out.println("openInventory "+si.server);
                            Ostrov.sync( () -> p.openInventory(gameInfo.arena_inv), 2 );
                            //p.openInventory(si.arena_inv);
                            p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1, 2);
                        }

                    } else {

                        p.openInventory(gameInfo.arena_inv);
                        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1, 2);

                    }

                */


        }
        
    }






















    public void update(final String serverName, final String arenaName, final GameState state, final int players, final String line0, final String line1, final String line2, final String line3, final String extra) {
//Bukkit.broadcastMessage("updateMulti server="+server+" arena="+arenaName); 
        ArenaInfo ai = getArena(serverName, arenaName);
        if (ai==null) {
            //неи инфо - кинуть пустышку для обновы табличек
            ai = new ArenaInfo(this, serverName, arenaName, 0, -100, Material.BEDROCK);
            arenas.put(ai.slot, ai);
            //return;
        }
        
        switch (game.type) {
            
            case ONE_GAME:
                if (game==Game.SE) {
                    gameOnline-=ai.players;
                    gameOnline+=players;
                } else {
                    gameOnline = players;
                }
                ai.update(state, players, game.displayName, arenaName, players>=0 ? GameState.РАБОТАЕТ.displayColor+GameState.РАБОТАЕТ.name() : GameState.ВЫКЛЮЧЕНА.displayColor+GameState.ВЫКЛЮЧЕНА.name(), players>0 ? "§1"+players : "", extra);
                break;
                
            case LOBBY:
                gameOnline-=ai.players;
                gameOnline+=players;
                ai.update(state, players, game.displayName, arenaName, players>=0 ? GameState.РАБОТАЕТ.displayColor+GameState.РАБОТАЕТ.name() : GameState.ВЫКЛЮЧЕНА.displayColor+GameState.ВЫКЛЮЧЕНА.name(), players>0 ? "§1"+players : "", extra);
                break;
                
            case ARENAS:
                gameOnline-=ai.players;
                if (gameOnline<0) gameOnline=0;
                gameOnline+=players;
                ai.update(state, players, line0, line1, line2, line3, extra);
                break;
			default:
				break;
                
        }

        if (Bukkit.isPrimaryThread()) {
                Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( ai ));
        } else {
            final ArenaInfo ai2 = ai;
            Ostrov.sync(() -> 
                Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( ai2 ))
            ,0);
        }
    }

    
    
    
    //нужен поиск - могуть быть одинаковые арена на разных серверах
    public ArenaInfo getArena(final String server, final String arenaName) {
        //int online = 0;
        if (game.type==ServerType.ONE_GAME) {
            return arenas.get(0);
        }
        for (ArenaInfo a:arenas.values()) {
            if (a.server.equals(server) && a.arenaName.equals(arenaName)) {
                return a;
            }
        }
        return null;
    }
    
    public List<String> getArenaNames(final String server) {
        List<String>list = new ArrayList<>();
        for (ArenaInfo a:arenas.values()) {
            if (a.server.equals(server)) list.add(a.arenaName);
        }
        return list;
    }
    
    public List<String> getArenaNames() {
        //return arenas.values().stream().collect(Collectors.toList());
        List<String>list = new ArrayList<>(arenas.size());
        for (ArenaInfo a:arenas.values()) {
            list.add(a.arenaName);
        }
        return list;
    }
    
    
    
    
    
    

    public String getServername() {
        return arenas.get(0).server;
    }

    public int getOnline() {
        return gameOnline;
    }

    public GameState getState() {
        switch (game.type) {
                    
            case LOBBY:
                return GameState.РАБОТАЕТ;
                
            case ONE_GAME:
                return gameOnline>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА;

            case ARENAS:
                return arenas.isEmpty() ? GameState.ВЫКЛЮЧЕНА : GameState.РАБОТАЕТ;
			default:
				break;
                    
            }
        
        return GameState.НЕОПРЕДЕЛЕНО;
    }




    
    
}
