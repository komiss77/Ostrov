package ru.komiss77.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Timer;
import ru.komiss77.commands.PassportCmd;
import ru.komiss77.enums.ServerType;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameSign;
import ru.komiss77.modules.games.GameSignEditor;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.*;


public class GlobalBugFix implements Listener {

  //дискорд - голодомор
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onHungry(FoodLevelChangeEvent e) {
    if (e.getEntity().getType() != EntityType.PLAYER) return;
    final Player p = (Player) e.getEntity();
    boolean cancel = false;
    if (GM.GAME.type == ServerType.ARENAS) {
      switch (GM.GAME) {
        case BB, TW, SN, HS, QU -> {
          cancel = true;
        }
        default -> {
          if (p.getWorld().getName().equals("lobby") || p.getWorld().getName().equals("world")) {
            cancel = true;
          }
        }
      }
    } else if (GM.GAME.type == ServerType.LOBBY) {
      cancel = true;
    } else if (GM.GAME.type == ServerType.ONE_GAME) {
      switch (GM.GAME) {
        case PA -> {
          cancel = true;
        }
        case SK, OB, SG -> {
          if (p.getWorld().getName().equals("world")) {
            cancel = true;
          }
        }
      }
    }
    if (cancel) {
      e.setCancelled(true);
      p.setFoodLevel(20);
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
          if (p.getWorld().getName().equals("world")) {
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
