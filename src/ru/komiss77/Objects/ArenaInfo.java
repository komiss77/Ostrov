package ru.komiss77.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.Enums.Game;
import ru.komiss77.Enums.GameState;
import ru.komiss77.Events.GameInfoUpdateEvent;
import ru.komiss77.Ostrov;







public final class ArenaInfo {

    public GameInfo gameInfo; //araim daaria bw bb sg
    public String server; //araim daaria bw01 bb01 sg02
    public String arenaName;
    public GameState state;
    public int players;
    
    private final List<String>lore=Arrays.asList("","","","","","","");//new ArrayList<>(7); //без размера кидает java.lang.IndexOutOfBoundsException:!!
    //private String line0,line1,line2,line3,extra;
    
    public int slot;
    public ItemStack item;
    public ItemMeta meta;
    
    
    
    //создаётся при загрузке из мускул
    public ArenaInfo(final GameInfo gameInfo, 
            final String server, 
            final String arenaName, 
            final GameState state, 
            final int players, 
            final String line0, final String line1, final String line2, final String line3, final String extra,
            final int level, final int reputation,
            final int position, final Material mat) {

        this.gameInfo=gameInfo;
        this.server=server;
        this.arenaName=arenaName;
        //this.slot=slot;
        this.state = state;
        
        item=new ItemStack(  mat==null? Material.BEDROCK : mat );
        
        meta=item.getItemMeta();
        meta.setDisplayName(gameInfo.item.getItemMeta().getDisplayName() + arenaName );
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_DESTROYS,ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_PLACED_ON);
        //lore; //0-игроки 1-пустая 2-5-строки - минимум для эвента
        
        update(state, players, line0, line1, line2, line3, extra);
        
        //item.setItemMeta(meta);
//Bukkit.broadcastMessage("Arena slot="+position+" serv="+this.server+" arena="+this.arena_name);
    }

    
    
    public boolean isWaitingMode() {
        return state==GameState.ОЖИДАНИЕ;
        //return lore.get(4).toLowerCase().contains("ожидание") ||  lore.get(5).toLowerCase().contains("ожидание");
    }
    
    public boolean isStartingMode() {
        return state==GameState.СТАРТ;
        //return lore.get(4).toLowerCase().contains("старт");
    }
    
    
    
    
    
    public void update(final GameState state, final int players, final String line0, final String line1, final String line2, final String line3, final String extra) {
        this.players=players;
        this.state=state;
        
        lore.set(0, players>0 ? "Игроки: "+players : "");
        lore.set(1,"");

        lore.set(2,line0);
        lore.set(3,line1);
        lore.set(4,line2);
        lore.set(5,line3);
        lore.set(6,extra);

        meta.setLore(lore);
        //if (SM.colorable.contains(item.getType())) item.setDurability(color); 1.12
        //if (ApiOstrov.canChangeColor(item.getType())) ColorUtils.changeColor(item, state.attachedColor);
        item.setItemMeta(meta);
        
       // try {
        gameInfo.arena_inv.setItem(slot, item);
       // } catch (NullPointerException ex) {
       //     Ostrov.log_err("Arena item update position="+slot+" item="+item+" : "+ex.getMessage());
      //  }
        if (Bukkit.isPrimaryThread()) {
                Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( Game.fromServerName(server), server, arenaName, state, players, line0, line1, line2, line3, extra));
        } else {
            Ostrov.sync(() -> 
                Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( Game.fromServerName(server), server, arenaName, state, players, line0, line1, line2, line3, extra))
            ,0);
        }
        
    }


    
    
    
    
}
