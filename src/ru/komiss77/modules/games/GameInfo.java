package ru.komiss77.modules.games;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.GameInfoUpdateEvent;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemUtil;

public class GameInfo {

    public final Game game;

    private int gameOnline; //для одиночек, либо общий онлайн на аренах
    private final CaseInsensitiveMap<ArenaInfo> arenas = new CaseInsensitiveMap<>(); //position в меню, арена. String,Arena нельзя - могут быть одинаковые арены на разных серверах!!
    public Material mat;

    public GameInfo(final Game game) {
        this.game = game;
        mat = Material.matchMaterial(game.mat);
        if (mat == null) mat = Material.BEDROCK;

        // if (game.type==ServerType.ONE_GAME) {
        //для одиночек данные храним в нулевой арене
        //    final ArenaInfo ai = new ArenaInfo(this, game.suggestName, "", game.level, game.reputation, mat==null ? Material.BEDROCK : mat);
        //    arenas.put(game.suggestName, ai);

        // } else if (game.type==ServerType.ARENAS || game.type==ServerType.LOBBY) {

        //

        //  }

    }


    public ItemStack getIcon(final Oplayer op) {
        final boolean hasLevel = op.getStat(Stat.LEVEL) >= game.level;
        final boolean hasReputation = op.reputationCalc >= game.reputation;

        return switch (game.type) {

            case ONE_GAME -> new ItemBuilder(mat.asItemType())
                .name(op.eng ? Lang.t(game.displayName, Lang.EN) : game.displayName)
                .amount(Math.max(Math.min(gameOnline, 60), 1))
                .lore("")
                .lore(game.description)
                .lore("")
                .lore(getState().displayColor + getState().name())
                .lore((hasLevel && hasReputation && gameOnline >= 0
                    ? (op.eng ? "§a🢖 Click §с- PLAY" : "§a🢖 Клик §с- ИГРАТЬ")
                    : (op.eng ? "§кNot available!" : "§кНедоступен!"))
                    + " §7(" + (gameOnline >= 0 ? gameOnline : "§4X") + "§7)")
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP).build();

            case LOBBY -> new ItemBuilder(mat.asItemType())
                .name(op.eng ? Lang.t(game.displayName, Lang.EN) : game.displayName)
                .amount(Math.max(Math.min(gameOnline, 60), 1))
                .lore("")
                .lore(getState().displayColor + getState().name())
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP).build();

            case ARENAS -> new ItemBuilder(mat.asItemType())
                .name(op.eng ? Lang.t(game.displayName, Lang.EN) : game.displayName)
                .amount(Math.max(Math.min(gameOnline, 60), 1))
                .lore("")
                .lore(game.description)
                .lore("")
                .lore(getState().displayColor + getState().name())
                .lore((hasLevel && hasReputation && gameOnline >= 0
                    ? (op.eng ? "§a🢖 Left Click §с- PLAY" : "§a🢖 Левый Клик §с- ИГРАТЬ")
                    : (op.eng ? "§кNot available!" : "§кНедоступен!"))
                    + " §7(" + (gameOnline >= 0 ? gameOnline : "§4X") + "§7)")
                .lore(op.eng ? "§a🢖 Right Click §к- MAPS" : "§a🢖 Правый Клик §к- АРЕНЫ")
                .lore(gameOnline >= 0 ? (op.eng ? "§7Players: " : "§7Играют: ") + gameOnline : "")
                .flags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP).build();

            default -> ItemUtil.air.clone();

        };

    }


    public void update(final String serverName, final String arenaName, final GameState state, final int players,
                       final String line0, final String line1, final String line2, final String line3) {

        final ArenaInfo ai = arenas.computeIfAbsent(serverName + arenaName,
            k -> new ArenaInfo(this, serverName, arenaName, 0, -100, Material.BEDROCK, arenas.size()));

        switch (game.type) {
            case ONE_GAME -> {
                if (game == Game.SE) {
                    gameOnline -= ai.players;
                    gameOnline += players;
                } else {
                    gameOnline = players;
                }
                ai.update(state, players,
                    game.displayName,
                    arenaName, players >= 0 ? GameState.РАБОТАЕТ.displayColor + GameState.РАБОТАЕТ.name() : GameState.ВЫКЛЮЧЕНА.displayColor + GameState.ВЫКЛЮЧЕНА.name(), players > 0 ? "§1" + players : "");
            }
            case LOBBY -> {
                gameOnline -= ai.players;
                gameOnline += players;
                ai.update(state, players, game.displayName, arenaName, players >= 0 ? GameState.РАБОТАЕТ.displayColor + GameState.РАБОТАЕТ.name() : GameState.ВЫКЛЮЧЕНА.displayColor + GameState.ВЫКЛЮЧЕНА.name(), players > 0 ? "§1" + players : "");
            }
            case ARENAS -> {
                gameOnline -= ai.players; //убавить на старый онлайн арены
                if (gameOnline < 0) gameOnline = 0;
                gameOnline += players; //добавить новый онлайн арены
                ai.update(state, players, line0, line1, line2, line3);
            }
            default -> {
            }
        }

        if (Bukkit.isPrimaryThread()) {
            new GameInfoUpdateEvent(ai).callEvent();
        } else {
            Ostrov.sync(() -> new GameInfoUpdateEvent(ai).callEvent(), 0);
        }
    }


    //нужен поиск - могуть быть одинаковые арена на разных серверах
    public ArenaInfo getArena(final String serverName, final String arenaName) {
        return arenas.get(serverName + arenaName);
        //int online = 0;
       /* if (game.type==ServerType.ONE_GAME) {
            return arenas.get("");//arenas.get(0);
        }
        for (ArenaInfo a:arenas.values()) {
            if (a.server.equals(server) && a.arenaName.equals(arenaName)) {
                return a;
            }
        }
        return null;*/
    }

    public List<String> getArenaNames(final String server) {
        List<String> list = new ArrayList<>();
        for (ArenaInfo a : arenas.values()) {
            if (a.server.equalsIgnoreCase(server)) list.add(a.arenaName);
        }
        return list;
    }

    public List<String> getArenaNames() {
        //return arenas.values().stream().collect(Collectors.toList());
        List<String> list = new ArrayList<>(arenas.size());
        for (ArenaInfo a : arenas.values()) {
            list.add(a.arenaName);
        }
        return list;
    }


    //public String getServername() {
    // return arenas.get(0).server;
    //}

    public int getOnline() {
        return gameOnline;
    }

    public int count() {
//Ostrov.log_warn("arenas.size="+arenas.size());
        return arenas.size();
    }

    public GameState getState() {
        return switch (game.type) {
            case LOBBY -> GameState.РАБОТАЕТ;
            case ONE_GAME -> gameOnline >= 0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА;
            case ARENAS -> arenas.isEmpty() ? GameState.ВЫКЛЮЧЕНА : GameState.РАБОТАЕТ;
            default -> GameState.НЕОПРЕДЕЛЕНО;
        };

    }

    public ArenaInfo getArena(final int slot) {
        for (ArenaInfo ai : arenas.values()) {
            if (ai.slot == slot) {
                return ai;
            }
        }
        return null;
    }

    public Collection<ArenaInfo> arenas() {
        return arenas.values();
    }

    public void clear() {
        arenas.clear();
    }

}
