package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import ru.komiss77.modules.quests.Quest;



//@Deprecated
public final class QuestCompleteEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancel = false;
	private final Quest quest;

	/**
	 * Constructor for the QuestCompleteEvent.
	 *
	 * @param player The player who completed.
         * @param equipType
	 * @param type The quest completed.
	 * @param oldArmorPiece The ItemStack of the armor removed.
	 * @param newArmorPiece The ItemStack of the armor added.
	 */
        //@Deprecated
	public QuestCompleteEvent(final Player player, final Quest quest){
		super(player);
		this.quest = quest;
	}

	/**
	 * Gets a list of handlers handling this event.
	 *
	 * @return A list of handlers handling this event.
	 */
	public final static HandlerList getHandlerList(){
		return handlers;
	}

	/**
	 * Gets a list of handlers handling this event.
	 *
	 * @return A list of handlers handling this event.
	 */
	@Override
	public final HandlerList getHandlers(){
		return handlers;
	}

	/**
	 * Sets if this event should be cancelled.
	 *
	 * @param cancel If this event should be cancelled.
	 */
        @Override
	public final void setCancelled(final boolean cancel){
		this.cancel = cancel;
	}

	/**
	 * Gets if this event is cancelled.
	 *
	 * @return If this event is cancelled
	 */
        @Override
	public final boolean isCancelled(){
		return cancel;
	}

	public final Quest getQuest(){
		return quest;
	}
}
