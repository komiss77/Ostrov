package ru.komiss77.modules.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.ServerType;
import ru.komiss77.events.GameInfoUpdateEvent;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;







public class GameInfo {
    public final Game game;
    
    private int gameOnline; //для одиночек, либо общий онлайн на аренах
    public final HashMap<Integer,ArenaInfo>arenas=new HashMap<>(); //position в меню, арена. String,Arena нельзя - могут быть одинаковые арены на разных серверах!!
    //public GameState state; //для одиночек
    //public final String serverName; //для одиночек
    
    public Inventory arena_inv;
    
    public ItemStack item;
    public ItemMeta meta;
    private List<String>lore=new ArrayList<>();
    
    //private boolean flash;
     
    
    public GameInfo( final Game game ) {
        this.game = game;
        
        lore.add(0,"§7Игроки: 0");
        lore.add(1,"");
        
        if (game.type==ServerType.ONE_GAME) {
            lore.add(2,"§a⊳ Клик - перейти на сервер");//line 1-4+разделитель
            final Material mat = Material.matchMaterial(game.mat);

             //для одиночек данные храним в нулевой арене
            final ArenaInfo ai = new ArenaInfo(this, game.serverName, "", game.level, game.reputation, mat==null ? Material.BEDROCK : mat);
            arenas.put(0, ai);
           // genItem();
            
        } else if (game.type==ServerType.ARENAS || game.type==ServerType.LOBBY) {
            arena_inv=Bukkit.createInventory(null, 45, GM.main_inv_name+game.displayName);
            arena_inv.setItem(44, new ItemBuilder(Material.BARRIER).setName("§5Назад").build());
            lore.add(2,"");

            //genItem();
        } 
        
        genItem();
    }
    
    //SINGLE!!!
   /* public GameInfo( final Game game ) {
        this.game = game;
        
        lore.add(0,"§7Игроки: 0");
        lore.add(1,"");
        
        lore.add(2,"§a⊳ Клик - перейти на сервер");//line 1-4+разделитель
        final Material mat = Material.matchMaterial(game.mat);
        
         //для одиночек данные храним в нулевой арене
        final ArenaInfo ai = new ArenaInfo(this, game.serverName, "", game.level, game.reputation, mat==null ? Material.BEDROCK : mat);
        
        arenas.put(0, ai);
        
        genItem();
    }*/

    
    
    private void genItem() {
        
        lore.add("");
        lore.add("§7Требуемый уровень : §6"+game.level);
        lore.add("§7Требуемая репутация : §6"+game.reputation);
        
        item=new ItemStack(Material.matchMaterial(game.mat));//(  mat==null? Material.BEDROCK : mat );
        meta=item.getItemMeta();
        meta.setDisplayName(game.displayName);//meta.setDisplayName(description==null?"§cошибка":description);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_DESTROYS,ItemFlag.HIDE_PLACED_ON);
        meta.setLore(lore);
        item.setItemMeta(meta);

        GM.main_inv.setItem(game.menuSlot, item);

    }
    
    
    
    
    
  //  public void addArena(final String arena_name, final Arena arena) {
    //    arena.position=arenas.size();
   //     arena_inv.setItem(arena.position, arena.item);
  //      arenas.put(arena.position, arena);
   // }

    
    
    
    
    
    public void invClick(final Player p, final int slot) {
//System.out.println(" S_info invClick slot="+slot);                                    
        if (slot==44) {
            p.openInventory(GM.main_inv);
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 2, 2);
            return;
        }
        final ArenaInfo ai = arenas.get(slot);
        if (ai!=null) {
            if ( (game.type==ServerType.LOBBY || game.type==ServerType.ONE_GAME) && ai.server.equals(GM.this_server_name)) {
                p.sendMessage("§6Вы и так уже на этом сервере!");
                return;
            }
            ApiOstrov.sendToServer(p, arenas.get(slot).server, arenas.get(slot).arenaName);
        }
    }
    /*
    public void updatePlayerCount(final int players) {
//System.out.println("updatePlayerCount "+players);                                    
        if (game.type==GameType.SINGLE) { //для одиночек - имя=игра
            playerInGame=players;
            if (Bukkit.isPrimaryThread()) { //для одиночек сервер совпадает с game.name()
                Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent(game.name(), "", GameState.НЕОПРЕДЕЛЕНО, players, "", "", "", "", ""));
            } else {
                Ostrov.sync(()-> Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent(game.name(), "", GameState.НЕОПРЕДЕЛЕНО, players, "", "", "", "", "")), 0);
            }
        } else {
            playerInGame=0;
            arenas.values().stream().forEach((a) -> {
                playerInGame+=a.players;
            });
        }
        lore.set( 0, playerInGame>=0 ? "§7Играют: "+playerInGame : "" );
    }
    */
  //  public void updateSingle(final GameState state, final int players) {
//Bukkit.broadcastMessage("updateSingle server="+arenas.get(0).server+" state="+state+"  players="+players);
     //   gameOnline = players;
        
        //arenas.get(0).players = players;
        //arenas.get(0).state = state;
        //arenas.get(0).line1 = state.displayColor+state.name();
       // arenas.get(0).update(state, players, game.displayName, arenaName, players>=0 ? GameState.РАБОТАЕТ.displayColor+GameState.РАБОТАЕТ.name() : GameState.ВЫКЛЮЧЕНА.displayColor+GameState.ВЫКЛЮЧЕНА.name(), players>0 ? "§1"+players : "", extra);
        
      //  if (Bukkit.isPrimaryThread()) { //для одиночек сервер совпадает с game.name()
    //        Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent(arenas.get(0)));
     //   } else {
      //      Ostrov.sync(()-> Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent(arenas.get(0))), 0);
     //   }
       // lore.set( 0, players>=0 ? "§7Играют: "+players : "" );
       // lore.set (1, state.displayColor+state.name());
   // }
    
    public void update(final String serverName, final String arenaName, final GameState state, final int players, final String line0, final String line1, final String line2, final String line3, final String extra) {
//Bukkit.broadcastMessage("updateMulti server="+server+" arena="+arenaName); 
        ArenaInfo ai = getArena(serverName, arenaName);
        if (ai==null) {
            return;
            //ai = new ArenaInfo(this, serverName, arenaName, 0, 0, Material.MAP);
        }
        
        switch (game.type) {
            
            case ONE_GAME:
                ai.update(state, players, game.displayName, arenaName, players>=0 ? GameState.РАБОТАЕТ.displayColor+GameState.РАБОТАЕТ.name() : GameState.ВЫКЛЮЧЕНА.displayColor+GameState.ВЫКЛЮЧЕНА.name(), players>0 ? "§1"+players : "", extra);
                gameOnline = players;
                lore.set( 0, players>=0 ? "§7Играют: "+players : "" );
                lore.set (1, state.displayColor+state.name());
                break;
                
            case LOBBY:
                gameOnline-=ai.players;
                gameOnline+=players;
                ai.update(state, players, game.displayName, arenaName, players>=0 ? GameState.РАБОТАЕТ.displayColor+GameState.РАБОТАЕТ.name() : GameState.ВЫКЛЮЧЕНА.displayColor+GameState.ВЫКЛЮЧЕНА.name(), players>0 ? "§1"+players : "", extra);
                arena_inv.setItem(ai.slot, ai.item);
                lore.set( 0, gameOnline>=0 ? "§7Играют: "+gameOnline : "" );
                break;
                
            case ARENAS:
                gameOnline-=ai.players;
                gameOnline+=players;
                ai.update(state, players, line0, line1, line2, line3, extra);
                arena_inv.setItem(ai.slot, ai.item);
                lore.set( 0, gameOnline>=0 ? "§7Играют: "+gameOnline : "" );
                break;
                
                
            default:
                break;
        }
        
        //arena_inv.setItem(ai.slot, ai.item);
        
        //gameOnline=0;
        //arenas.values().stream().forEach( (a) -> {
        //    gameOnline+=a.players;
        //});             
       // lore.set( 0, gameOnline>=0 ? "§7Играют: "+gameOnline : "" );
        
        if (Bukkit.isPrimaryThread()) {
                Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( ai ));
        } else {
            Ostrov.sync(() -> 
                Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( ai ))
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
    
    
    
    
    public void updateIcon() {
//System.out.println("updateIcon="+serverName);        
//this.update(Ostrov.randInt(0, 100));
        if (gameOnline>=0) {
            
    //    flash=false;
            switch (game.type) {
                
                case ONE_GAME:
                    lore.set(2,"§a⊳ Клик - перейти на сервер");//line 1-4+разделитель
                    break;
                    
                case LOBBY:
                    lore.set(2,"§a⊳ Клик - перейти в лобби");//line 1-4+разделитель
                    break;
                    
                default:
                    if (ApiOstrov.getWarpManager()!=null && ApiOstrov.getWarpManager().exist(game.name())) {
                        lore.set(1,"§a  Лев.клик - к табличкам");
                        lore.set(2,"§a⊳ Прав.клик - выбрать арену"); //line 1-4+разделитель
                    } else {
                        lore.set(2,"§a⊳ Клик - выбрать арену");
//System.out.println("updateIcon lore.set(2,§a");        
                    }
                    break;
            }

            
        } else if (game.type==ServerType.ONE_GAME) {  //онлайн -1 значит офф
            lore.set(2,"§4Сервер выключен" );
            //if (SM.colorable.contains(item.getType())) item.setDurability((short)0); 1.12
        //   if (ApiOstrov.canChangeColor(item.getType())) ColorUtils.changeColor(item, (short) 0);
        }
        meta.setLore(lore); //1 ArrayIndexOutOfBoundsException: 7
        item.setItemMeta(meta);  //2 Null string not allowed
        GM.main_inv.setItem(game.menuSlot, item);
    }

    //public int getArenaCount() {
    //    return arenas.size();
    //}

   // public Collection<Arena> getArenas() {
  //      return arenas.values();
  //  }

    public String getServername() {
        return arenas.get(0).server;
    }

    public int getOnline() {
        return gameOnline;
    }




    
    
}
