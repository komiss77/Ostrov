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
     * @param player        The player who completed.
     */
    //@Deprecated
    public QuestCompleteEvent(final Player player, final Quest quest) {
        super(player);
        this.quest = quest;
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

    public Quest getQuest() {
        return quest;
    }
}
