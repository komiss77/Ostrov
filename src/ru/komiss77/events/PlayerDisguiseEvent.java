package ru.komiss77.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.version.Disguise;


public class PlayerDisguiseEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  public final @Nonnull Player owner;
  public final @Nonnull Disguise disguise;
  public @Nonnull DisguiseAction action;
  public @Nullable Event event;
  public @Nullable Entity target;
  public @Nullable Block block;
  private boolean canceled;

  public PlayerDisguiseEvent(final Player p, final Disguise disguise, final DisguiseAction action) {
    this.owner = p;
    this.disguise = disguise;
    this.action = action;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public void setCancelled(boolean b) {

  }

  public enum DisguiseAction {
    NONE,

    //пакеты
    PICK_ITEM_FROM_BLOCK_PACKET,
    PICK_ITEM_FROM_ENTITY_PACKET,

    //действия зрителя
    LEFT_CLICK_ENTITY,
    RIGHT_CLICK_ENTITY,
    RIGHT_CLICK_BLOCK,
    LEFT_CLICK_BLOCK,
    RIGHT_CLICK_AIR,
    LEFT_CLICK_AIR,
    SHIFT, //короткое нажатие шифта
    LONG_SHIFT, //нажатие шифта больше chargeTime (50тиков)

    //на основе событий с маскировкой
    DAMAGE_EVENT,
    PICKUP_EVENT,
    INTERACT_AT_DISGUISE_EVENT, //кто-то PlayerInteractAtEntityEvent на моба маскировки
    MOUNT_EVENT,
    DISMOUNT_EVENT,
    SPECTATE_EVENT,
    LEASH_EVENT,
  }


  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }


  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }


}
