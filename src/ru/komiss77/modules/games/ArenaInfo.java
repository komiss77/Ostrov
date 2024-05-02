package ru.komiss77.modules.games;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.redis.RDS;
import ru.komiss77.modules.translate.Lang;

public final class ArenaInfo {

    public int slot;
    public Material mat;
    public GameInfo gameInfo; //araim daaria bw bb sg
    public String server; //araim daaria bw01 bb01 sg02
    public String arenaName;
    public GameState state = GameState.НЕОПРЕДЕЛЕНО;
    public int level;
    public int reputation;
    
    public Set<String>signs;
    
    public int players;
    public String line0="",line1="",line2="",line3="";
    
    
    //создаётся при загрузке из мускул
    public ArenaInfo(final GameInfo gameInfo, final String server, final String arenaName, final int level, final int reputation, final Material mat, final int slot) {
        this.slot=slot;//gameInfo.arenas.size();
        this.mat = mat==null ? Material.BEDROCK : mat;
        this.gameInfo=gameInfo;
        this.server=server;
        this.arenaName=arenaName;
        this.level = level;
        this.reputation = reputation;
        signs = new HashSet<>();
    }

    
    public void sendData() {
      final StringBuffer sb = new StringBuffer(gameInfo.game.name()).append(LocalDB.W_SPLIT)
        .append(Ostrov.MOT_D).append(LocalDB.W_SPLIT)
        .append(arenaName).append(LocalDB.W_SPLIT)
        .append(state.name()).append(LocalDB.W_SPLIT)
        .append(players).append(LocalDB.W_SPLIT)
        .append(line0).append(LocalDB.W_SPLIT)
        .append(line1).append(LocalDB.W_SPLIT)
        .append(line2).append(LocalDB.W_SPLIT)
        .append(line3).append(" ").append(LocalDB.W_SPLIT)
        ;
      RDS.sendMessage("arenadata", sb.toString());
    }
    
    
    public ItemStack getIcon (final Oplayer op) {
        
        final boolean hasLevel =  op.getStat(Stat.LEVEL)>=level;
        final boolean hasReputation =  op.reputationCalc>=reputation;
        
        final List<Component>lore = List.of(
                        Component.text(players>0 ? (op.eng?"§7Players: §b":"§7Игроки: §b")+players : (op.eng?"nobody here":"никого нет")),
                        Component.text(state.displayColor + (op.eng ? Lang.translate(state.name(), Lang.EN):state.name()) ),
                        Component.empty(),
                        Component.text(line0),
                        Component.text(line1),
                        Component.text(line2),
                        Component.text(line3),
                        Component.empty(),
                        Component.text( hasLevel && hasReputation ?  (op.eng?"§a⊳ Click - to arena":"§a⊳ Клик - на арену")  : (op.eng?"§eNot available !":"§eНедоступна !")),
                        Component.text(hasLevel ? (op.eng?"§7Required level : §6":"§7Требуемый уровень : §6") +level : (op.eng?"§cAvailable from level §e":"§cБудет доступна с уровня §e")+level),
                        Component.text(hasReputation ? (op.eng?"§7Required reputation : §a>":"§7Требуемая репутация : §a>") +reputation : (op.eng?"§cAvailable with reputation §a>":"§cДоступна при репутации §a>")+reputation)
                    );
        final ItemStack is = new ItemStack(mat);
        final ItemMeta im = is.getItemMeta();
        im.displayName(Component.text("§l" + (op.eng ? Lang.translate(arenaName, Lang.EN) : arenaName)) );
        im.lore(lore);
        is.setItemMeta(im);
        return is;
    }



    
    
    public boolean isWaitingMode() {
        return state==GameState.ОЖИДАНИЕ;
    }
    
    public boolean isStartingMode() {
        return state==GameState.СТАРТ;
    }
    
    
    
    
    
    protected void update(final GameState state, final int players, final String line0, final String line1, final String line2, final String line3) {
        this.players=players;
        this.state=state==null? GameState.НЕОПРЕДЕЛЕНО : state;
        this.line0 = line0;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;

        if (state == GameState.ОЖИДАНИЕ && players > 0) {
          mat = Material.AXOLOTL_BUCKET;
        } else {
          mat = state.iconMat;
        }
        
        if (!signs.isEmpty()) {
            GM.updateSigns(this);
        }

        //sendData(); сеть уходит в зацикливание!Ё!!!
    }



    

    
    
}
