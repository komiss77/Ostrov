package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.utils.inventory.InventoryContent;


public class LocalMenuEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  public final Player p;
  public final InventoryContent content;
  private boolean cancel = false;

  public LocalMenuEvent(final Player p, final InventoryContent content) {
    this.p = p;
    this.content = content;
  }

  @Override
  public void setCancelled(final boolean cancel) {
    this.cancel = cancel;
  }

  @Override
  public boolean isCancelled() {
    return cancel;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }


}
