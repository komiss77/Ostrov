package ru.komiss77.enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.games.GM;


public enum ServerType {

    NONE,
  //PROXY,
    REG_OLD,
    REG_NEW,
    LOBBY,
    ONE_GAME,
    ARENAS,

    ;

    private static final Map<String, ServerType> nameMap;

    static {
        Map<String, ServerType> sm = new ConcurrentHashMap<>();
        for (ServerType d : ServerType.values()) {
            sm.put(d.name().toUpperCase(), d);
        }
        nameMap = Collections.unmodifiableMap(sm);
    }

    public static ServerType fromString(String as_string) { //araim daaria bw01 bb01 sg02
        return nameMap.containsKey(as_string) ? nameMap.get(as_string.toUpperCase()) : NONE;
    }

    public static ServerType fromServerName(String serverName) {
        final Game game = Game.fromServerName(serverName);
        if (game != Game.GLOBAL) {
            return game.type;
        }
        if (serverName.length() == 3) {
            if (serverName.startsWith("rg")) {
                return REG_NEW;
            } else if (serverName.startsWith("ol")) {
                return REG_OLD;
            }
        }// else if (serverName.equals("proxy")) {
      //return PROXY;
      //}
        return NONE;
    }

    public boolean canAfk() {
      switch (GM.GAME) {
        case SK, OB -> { //удалите антиафк на скайблоке, никто не играет, какой смысл в том что его поставили
          return false;
        }
      }
        return switch (this) {
          //case NONE, ONE_GAME, REG_OLD, REG_NEW -> false;
          case ONE_GAME -> true;
          case NONE -> Ostrov.MOT_D.equals("home"); //для теста
          //default -> true;
          default -> false;
        };
    }


}

