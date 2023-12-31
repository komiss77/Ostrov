package ru.komiss77.modules.games;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.translate.EnumLang;
import ru.komiss77.modules.translate.Lang;

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
    }

    
    
    
    
    public ItemStack getIcon (final Oplayer op) {
        
        final boolean hasLevel =  op.getStat(Stat.LEVEL)>=level;
        final boolean hasReputation =  op.reputationCalc>=reputation;
        
        final List<Component>lore = Arrays.asList(
                        Component.text(players>0 ? (op.eng?"Players":"Игроки: ")+players : (op.eng?"nobody here":"никого нет")),
                        Component.text(state.displayColor + (op.eng ? Lang.translate(state.name(), EnumLang.EN_US):state.name()) ),
                        Component.text(line0),
                        Component.text(line1),
                        Component.text(line2),
                        Component.text(line3),
                        Component.text(extra),
                        Component.text( hasLevel && hasReputation ?  (op.eng?"§a⊳ Click - to arena":"§a⊳ Клик - на арену")  : (op.eng?"§eNot available !":"§eНедоступна !")),
                        Component.text(hasLevel ? (op.eng?"§7Required level : §6":"§7Требуемый уровень : §6") +level : (op.eng?"§cAvailable from level §e":"§cБудет доступна с уровня §e")+level),
                        Component.text(hasReputation ? (op.eng?"§7Required reputation : §a>":"§7Требуемая репутация : §a>") +reputation : (op.eng?"§cAvailable with reputation §a>":"§cДоступна при репутации §a>")+reputation)
                    );
        final ItemStack is = new ItemStack(mat);
        final ItemMeta im = is.getItemMeta();
        im.displayName(Component.text(op.eng ? Lang.translate(arenaName, EnumLang.EN_US) : arenaName));
        im.lore(lore);
        is.setItemMeta(im);
        return is;
        /*return new ItemBuilder(mat)
                .name(arenaName)
                .addLore(players>0 ? (op.eng?"Players":"Игроки: ")+players : (op.eng?"nobody here":"никого нет"))
                .addLore(state.displayColor+state.name())
                .addLore(line0)
                .addLore(line1)
                .addLore(line2)
                .addLore(line3)
                .addLore(extra)
                .addLore( hasLevel && hasReputation ?  (op.eng?"§a⊳ Click - to arena":"§a⊳ Клик - на арену")  : (op.eng?"§eNot available !":"§eНедоступна !"))
                .addLore(  hasLevel ? (op.eng?"§7Required level : §6":"§7Требуемый уровень : §6") +level : (op.eng?"§cAvailable from level §e":"§cБудет доступна с уровня §e")+level)
                .addLore(  hasReputation ? (op.eng?"§7Required reputation : §a>":"§7Требуемая репутация : §a>") +reputation : (op.eng?"§cAvailable with reputation §a>":"§cДоступна при репутации §a>")+reputation)
                .build();*/
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
