package ru.komiss77.listener;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
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
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onLavaArmorstand(final EntityDamageEvent e) { //стойка не получает урон в лаве
    if (e.getCause() == EntityDamageEvent.DamageCause.LAVA && e.getEntityType() == EntityType.ARMOR_STAND) {
      ArmorStand as = (ArmorStand) e.getEntity();
      //as.setNoDamageTicks(20);
      //if (as.getFireTicks() < 1) as.setFireTicks(20);
      as.damage(e.getDamage(), DamageSource.builder(DamageType.IN_FIRE).withDamageLocation(as.getLocation()).build());
      //double h = as.getHealth() - e.getDamage();
      //Ostrov.log_warn("ARMOR_STAND  isCancelled?"+e.isCancelled()+" dmg="+e.getDamage()+" h="+as.getHealth());
      //as.setNoDamageTicks(20);
      //e.setCancelled(true);
    }
  }

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
