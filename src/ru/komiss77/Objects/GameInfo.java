package ru.komiss77.Objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Enums.Game;
import ru.komiss77.Enums.Game.GameType;
import ru.komiss77.Enums.GameState;
import ru.komiss77.Events.GameInfoUpdateEvent;
import ru.komiss77.Managers.SM;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;







public class GameInfo {
    public final Game game;
    
    public int playerInGame; //для одиночек, либо общий онлайн на аренах
    public GameState state; //для одиночек
    public final String serverName; //для одиночек
    
    public Inventory arena_inv;
    public final HashMap<Integer,ArenaInfo>arenas=new HashMap<>(); //position в меню, арена. String,Arena нельзя - могут быть одинаковые арены на разных серверах!!
    
    public ItemStack item;
    public ItemMeta meta;
    private List<String>lore=new ArrayList<>();
    
    //private boolean flash;
     
    
    public GameInfo( final Game game, final String serverName ) {
        this.game = game;
        this.serverName = serverName;
        
        lore.add(0,"§7Игроки: "+playerInGame);
        lore.add(1,"");
        
        if (game.type==GameType.SINGLE) {
            
            lore.add(2,"§a⊳ Клик - перейти на сервер");//line 1-4+разделитель
            
        } else {
            
            arena_inv=Bukkit.createInventory(null, 45, SM.main_inv_name+game.displayName);
            arena_inv.setItem(44, new ItemBuilder(Material.BARRIER).setName("§5Назад").build());
            lore.add(2,"");
            //if (Ostrov.getWarpManager().exist(game.name())) { -getWarpManager тут еще null!!
           //     lore.add(1,"§a  Лев.клик - к табличкам");
         //       lore.add(2,"§a⊳ Прав.клик - выбрать арену"); //line 1-4+разделитель
         //   } else {
         //       lore.add(2,"§a⊳ Клик - выбрать арену");
         //   }
            
        }
        lore=ItemUtils.Gen_lore(lore, game.description, "§7");
        
        
        item=new ItemStack(Material.matchMaterial(game.mat));//(  mat==null? Material.BEDROCK : mat );
        meta=item.getItemMeta();
        meta.setDisplayName(game.displayName);//meta.setDisplayName(description==null?"§cошибка":description);
        //this.meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_DESTROYS,ItemFlag.HIDE_PLACED_ON);
        meta.setLore(lore);
        item.setItemMeta(meta);

        SM.main_inv.setItem(game.menuSlot, item);
        
    }


    
    
    
    
    
  //  public void addArena(final String arena_name, final Arena arena) {
    //    arena.position=arenas.size();
   //     arena_inv.setItem(arena.position, arena.item);
  //      arenas.put(arena.position, arena);
   // }

    
    
    
    
    
    public void invClick(final Player p, final int slot) {
//System.out.println(" S_info invClick slot="+slot);                                    
        if (slot==44) {
            p.openInventory(SM.main_inv);
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 2, 2);
            return;
        }
        
        if (arenas.containsKey(slot)) {
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
    public void updateSingle(final GameState state, final int players) {
//Bukkit.broadcastMessage("updateArena server="+server+" arena="+arena_name+"  lines="+lines); 
        playerInGame = players;
        this.state = state;
        if (Bukkit.isPrimaryThread()) { //для одиночек сервер совпадает с game.name()
            Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent(game, game.name(), "", GameState.НЕОПРЕДЕЛЕНО, players, "", "", "", "", ""));
        } else {
            Ostrov.sync(()-> Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent(game, game.name(), "", GameState.НЕОПРЕДЕЛЕНО, players, "", "", "", "", "")), 0);
        }
        lore.set( 0, playerInGame>=0 ? "§7Играют: "+playerInGame : "" );
    }
    
    public void updateArena(final String server, final String arenaName, final GameState state, final int players, final String line0, final String line1, final String line2, final String line3, final String extra) {
//Bukkit.broadcastMessage("updateArena server="+server+" arena="+arena_name+"  lines="+lines); 
        final ArenaInfo arena = getArena(server, arenaName);
        if (arena!=null) {
            arena.update(state, players, line0, line1, line2, line3, extra);  //SignUpdateEvent по имени арены внутри
           // updatePlayerCount(players); //пересчитать онлайн

            playerInGame=0;
            arenas.values().stream().forEach((a) -> {
                playerInGame+=a.players;
            });             
            //if (Bukkit.isPrimaryThread()) {
            //    Bukkit.getPluginManager().callEvent(new SinfoUpdateEvent(server.substring(0, 2), players ));
            //} else {
            //    Ostrov.sync(() -> 
            //        Bukkit.getPluginManager().callEvent(new SinfoUpdateEvent(server.substring(0, 2), players ))
            //    ,0);
            //}

        }

        lore.set( 0, playerInGame>=0 ? "§7Играют: "+playerInGame : "" );
    }

    
    
    
    
    public ArenaInfo getArena(final String server, final String arenaName) {
        for (ArenaInfo a:arenas.values()) {
            if (a.server.equals(server) && a.arenaName.equals(arenaName)) return a;
        }
        return null;
    }
    
    
    
    
    public void updateIcon() {
//System.out.println("do_Tick server="+this.server+" wiew="+this.arena_inv.getViewers());        
//this.update(Ostrov.randInt(0, 100));
        if (playerInGame>=0) {
           // if (flash) {
            //    flash=false;
            if (game.type==GameType.SINGLE) {
                lore.set(2,"§a⊳ Клик - перейти на сервер");//line 1-4+разделитель
            } else {
                if (ApiOstrov.getWarpManager().exist(game.name())) {
                    lore.set(1,"§a  Лев.клик - к табличкам");
                    lore.set(2,"§a⊳ Прав.клик - выбрать арену"); //line 1-4+разделитель
                } else {
                    lore.set(2,"§a⊳ Клик - выбрать арену");
                }
            }
               // lore.set(1,lore.get(1).replaceFirst("§a⊳", "§a ") );
              //  lore.set(2,lore.get(2).replaceFirst("§a ", "§a⊳") );
          //  } else {
          //      flash=true;
           //     lore.set(1,lore.get(1).replaceFirst("§a ", "§a⊳") );
            //    lore.set(2,lore.get(2).replaceFirst("§a⊳", "§a ") );
          //  }
            //if (SM.colorable.contains(item.getType())) item.setDurability((short) Ostrov.randInt(0, 15)); 1.12
          // if (ApiOstrov.canChangeColor(item.getType())) ColorUtils.changeColor(item, (short) ApiOstrov.randInt(0, 15));
        } else if (game.type==GameType.SINGLE) {
            lore.set(2,"§4Сервер выключен" );
            //if (SM.colorable.contains(item.getType())) item.setDurability((short)0); 1.12
        //   if (ApiOstrov.canChangeColor(item.getType())) ColorUtils.changeColor(item, (short) 0);
        }
        meta.setLore(lore); //1 ArrayIndexOutOfBoundsException: 7
        item.setItemMeta(meta);  //2 Null string not allowed
        //? SM.main_inv.setItem(slot, item);
    }

    //public int getArenaCount() {
    //    return arenas.size();
    //}

   // public Collection<Arena> getArenas() {
  //      return arenas.values();
  //  }



    
    
}
