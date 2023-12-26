package ru.komiss77.modules.games;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;







public final class ArenaInfo {

    public int slot;
    public Material mat;
    public GameInfo gameInfo; //araim daaria bw bb sg
    public String server; //araim daaria bw01 bb01 sg02
    public String arenaName;
    public GameState state;
    public int level;
    public int reputation;
    
    public Set<String>signs;
    
    public int players;
    public String line0="",line1="",line2="",line3="",extra="";
    
    
    
    //создаётся при загрузке из мускул
    public ArenaInfo(final GameInfo gameInfo, final String server, final String arenaName, final int level, final int reputation, final Material mat) {
        this.slot=gameInfo.arenas.size();
        this.mat = mat==null ? Material.BEDROCK : mat;
        this.gameInfo=gameInfo;
        this.server=server;
        this.arenaName=arenaName;
        this.level = level;
        this.reputation = reputation;
        signs = new HashSet<>();
        
        //ищем таблички для этой арены
       /* GameSign gs = null;
        for (final String locStr : GM.signs.keySet()) {
            gs = GM.signs.get(locStr);
            if (gs.server.equals(server) && gs.arena.equals(arenaName)) {
                signs.add(locStr);
            }
        }
        if (gs!=null) { //была найдена хотя бы одна табличка
            GM.updateSigns(this);
        }*/
    }

    
    
    
    
    public ItemStack getIcon (final Oplayer op) {
        
        final boolean hasLevel =  op.getStat(Stat.LEVEL)>=level;
        final boolean hasReputation =  op.reputationCalc>=reputation;
            
        return new ItemBuilder(mat)
                .name(arenaName)
                //.addFlags(ItemFlag.HIDE_ATTRIBUTES)
                .addLore(players>0 ? "Игроки: "+players : "никого нет")
                .addLore(state.displayColor+state.name())
                .addLore(line0)
                .addLore(line1)
                .addLore(line2)
                .addLore(line3)
                .addLore(extra)
                //.addLore(game.description)
                .addLore( hasLevel && hasReputation ?  "§a⊳ Клик - на арену"  : "§eНедоступна !")
                .addLore(  hasLevel ? "§7Требуемый уровень : §6" +level : "§cБудет доступна с уровня §e"+level)
                .addLore(  hasReputation ? "§7Требуемая репутация : §a>" +reputation : "§cДоступна при репутации §a>"+reputation)
                .build();
    }



    
    
    public boolean isWaitingMode() {
        return state==GameState.ОЖИДАНИЕ;
    }
    
    public boolean isStartingMode() {
        return state==GameState.СТАРТ;
    }
    
    
    
    
    
    protected void update(final GameState state, final int players, final String line0, final String line1, final String line2, final String line3, final String extra) {
        this.players=players;
        this.state=state;
        this.line0 = line0;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.extra = extra;
        
        this.mat = state.iconMat;
        
        if (!signs.isEmpty()) {
            GM.updateSigns(this);
        }
    }



    

    
    
}
