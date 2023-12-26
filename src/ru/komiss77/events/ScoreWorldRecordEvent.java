package ru.komiss77.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ru.komiss77.modules.scores.ScoreBoard;



//@Deprecated
public final class ScoreWorldRecordEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancel = false;
	private final String name;
	private final int amt;
	private final ScoreBoard score;

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
	public ScoreWorldRecordEvent(final String name, final int amt, final ScoreBoard score) {
		this.name = name;
		this.amt = amt;
		this.score = score;
	}

	/**
	 * Gets a list of handlers handling this event.
	 *
	 * @return A list of handlers handling this event.
	 */
	public final static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * Gets a list of handlers handling this event.
	 *
	 * @return A list of handlers handling this event.
	 */
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * Returns the amount.
	 *
	 * @return An amount.
	 */
	public final int getAmount() {
		return amt;
	}

	/**
	 * Returns the name.
	 *
	 * @return A name.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Sets if this event should be cancelled.
	 *
	 * @param cancel If this event should be cancelled.
	 */
        @Override
	public final void setCancelled(final boolean cancel) {
		this.cancel = cancel;
	}

	/**
	 * Gets if this event is cancelled.
	 *
	 * @return If this event is cancelled
	 */
        @Override
	public final boolean isCancelled() {
		return cancel;
	}

	public final ScoreBoard getScoreBoard() {
		return score;
	}
}
