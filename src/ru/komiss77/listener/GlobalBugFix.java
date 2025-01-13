package ru.komiss77.listener;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.ServerType;
import ru.komiss77.modules.games.GM;
import ru.komiss77.utils.StringUtil;


public class GlobalBugFix implements Listener {

  //дискорд - голодомор - <3
  /*@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onHungry(FoodLevelChangeEvent e) {
  ПЕРЕНЕС В PlayerLst
  }*/

  //В режиме Паркур на начальной локации табличку можно редактировать
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onSignEdit(PlayerOpenSignEvent e) {
    final Player p = e.getPlayer();
    boolean cancel = false;
    if (GM.GAME.type == ServerType.ARENAS || GM.GAME.type == ServerType.LOBBY) {
      cancel = true;
    } else if (GM.GAME.type == ServerType.ONE_GAME) {
      switch (GM.GAME) {
        case PA -> {
          cancel = true;
        }
        case SK, OB, SG -> {
          if (StringUtil.isLobby(p.getWorld())) {
            cancel = true;
          }
        }
      }
    }
    if (cancel) {
      if (!ApiOstrov.isLocalBuilder(p, true)) {
        e.setCancelled(true);
      }
    }
  }

}
