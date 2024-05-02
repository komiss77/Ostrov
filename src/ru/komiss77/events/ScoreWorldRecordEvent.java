package ru.komiss77.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.modules.scores.ScoreBoard;
import ru.komiss77.modules.scores.ScoreDis;


//@Deprecated
public final class ScoreWorldRecordEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancel = false;
	private final String name;
	private final int amt;
  private final ScoreDis display;

  @Deprecated
	private final ScoreBoard score;

  @Deprecated
	public ScoreWorldRecordEvent(final String name, final int amt, final ScoreBoard score) {
		this.name = name;
		this.amt = amt;
		this.score = score;
    this.display = null;
	}

  public ScoreWorldRecordEvent(final String name, final int amt, final ScoreDis score) {
    this.name = name;
    this.amt = amt;
    this.score = null;
    this.display = score;
  }

	/**
	 * Gets a list of handlers handling this event.
	 *
	 * @return A list of handlers handling this event.
	 */
	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * Gets a list of handlers handling this event.
	 *
	 * @return A list of handlers handling this event.
	 */
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * Returns the amount.
	 *
	 * @return An amount.
	 */
	public int getAmount() {
		return amt;
	}

	/**
	 * Returns the name.
	 *
	 * @return A name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets if this event should be cancelled.
	 *
	 * @param cancel If this event should be cancelled.
	 */
        @Override
	public void setCancelled(final boolean cancel) {
		this.cancel = cancel;
	}

	/**
	 * Gets if this event is cancelled.
	 *
	 * @return If this event is cancelled
	 */
        @Override
	public boolean isCancelled() {
		return cancel;
	}

  @Deprecated
	public ScoreBoard getScoreBoard() {
		return score;
	}

  public ScoreDis getScoreDis() {
    return display;
  }
}
