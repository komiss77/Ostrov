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
  public final @Nonnull DisguiseAction action;
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
    //InteractSelfPacket,
    PickItemFromBlockPacket,
    UseItemOnPacket, //ServerboundUseItemOnPacket
    PickItemFromEntityPacket,
    //SwingPacket, //
    Shift,
    DamageEvent,
    PickupEvent,
    InteractAtDisguiseEvent, //кто-то PlayerInteractAtEntityEvent на моба маскировки
    MountEvent,
    DismountEvent,
    SpectateEvent,
    LeashEvent,
    LeftClickOnEntity, //ServerboundInteractPacket для зрителя только лкп/пкм на энтити
    RightClickOnEntity, //ServerboundInteractPacket для зрителя только лкп/пкм на энтити
    RightClickOnBlock, //ServerboundUseItemOnPacket пкм
    LeftClickOnBlock,
    LeftClickOnAir,
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
