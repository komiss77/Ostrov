package ru.komiss77.utils.inventory;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

//@SuppressWarnings({ "unchecked" })
public class ClickableItem {

    /**
     * ClickableItem constant with no item and empty consumer.
     */
    public static final ClickableItem NONE = empty(null);

    private final ItemStack item;
    private final Consumer<?> consumer;
    private final boolean legacy;
    private Predicate<Player> canSee = null, canClick = null;
    private ItemStack notVisibleFallBackItem = null;

    protected ClickableItem(ItemStack item, Consumer<?> consumer, boolean legacy) {
        this.item = item;
        this.consumer = consumer;
        this.legacy = legacy;
    }

    /**
     * Creates a ClickableItem made of a given item and an empty consumer, thus
     * doing nothing when we click on the item.
     *
     * @param item the item
     * @return the created ClickableItem
     */
    public static ClickableItem empty(ItemStack item) {
        return from(item, data -> {
        });
    }

    /**
     * Creates a ClickableItem made of a given item and a given InventoryClickEvent's consumer.
     *
     * @param item     the item
     * @param consumer the consumer which will be called when the item is clicked
     * @return the created ClickableItem
     */

    public static ClickableItem of(ItemStack item, Consumer<InventoryClickEvent> consumer) {
        return new ClickableItem(item, consumer, true);
    }

    /**
     * Creates a ClickableItem made of a given item and a given ItemClickData's consumer.
     *
     * @param item     the item
     * @param consumer the consumer which will be called when the item is clicked
     * @return the created ClickableItem
     */
    public static ClickableItem from(ItemStack item, Consumer<ItemClickData> consumer) {
        return new ClickableItem(item, consumer, false);
    }


    /**
     * Clones this ClickableItem using a different item.
     *
     * @param newItem the new item
     * @return the created ClickableItem
     */
    public ClickableItem clone(ItemStack newItem) {
        return new ClickableItem(newItem, this.consumer, this.legacy);
    }

    /**
     * Clones this ClickableItem while keeping its {@link Consumer}, but giving it a new {@link ItemStack}.
     *
     * @param item the new {@link ItemStack}
     * @return a new ClickableItem with its related {@link ItemStack} updated
     */
    public ClickableItem cloneWithNewItem(ItemStack item) {
        return new ClickableItem(item, consumer, legacy);
    }

    /**
     * Executes this ClickableItem's consumer using the given click data.
     *
     * @param data the data of the click
     */
    @SuppressWarnings("unchecked")
    public void run(ItemClickData data) {
        if ((canSee == null || canSee.test(data.getPlayer())) && (canClick == null || canClick.test(data.getPlayer()))) {
            if (this.legacy) {
                if (data.getEvent() instanceof final InventoryClickEvent e) {
                    if ((canSee == null || canSee.test((Player) e.getWhoClicked())) && (canClick == null || canClick.test((Player) e.getWhoClicked()))) {
                        Consumer<InventoryClickEvent> legacyConsumer = (Consumer<InventoryClickEvent>) this.consumer;
                        legacyConsumer.accept(e);
                    }
                }
            } else {
                Consumer<ItemClickData> newConsumer = (Consumer<ItemClickData>) this.consumer;
                newConsumer.accept(data);
            }
        }
    }

    /**
     * Returns the item contained in this ClickableItem disregarding the visibility test set via {@link #canSee(Predicate, ItemStack)}.
     * <br>
     * <b>Warning:</b> The item can be <code>null</code>.
     *
     * @return the item, or <code>null</code> if there is no item
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Returns the item contained in this ClickableItem or the fallback item, if the player is not allowed to see the item.
     * <br>
     * <b>Warning:</b> The item can be <code>null</code>.
     *
     * @param player The player to test against if he can see this item
     * @return the item, the fallback item when not visible to the player, or <code>null</code> if there is no item
     */
    public ItemStack getItem(Player player) {
        if (canSee == null || canSee.test(player)) {
            return this.item;
        } else {
            return this.notVisibleFallBackItem;
        }
    }

    /**
     * Sets a test to check if a player is allowed to see this item.
     * <br>
     * Note: If the player is not allowed to see the item, in the inventory this item will be empty.
     * <br>
     * Examples:
     * <ul>
     *     <li><code>.canSee(player -> player.hasPermission("my.permission"))</code></li>
     *     <li><code>.canSee(player -> player.getHealth() >= 10)</code></li>
     * </ul>
     *
     * @param canSee the test, if a player should be allowed to see this item
     * @return <code>this</code> for a builder-like usage
     * @see #canSee(Predicate, ItemStack) If you want to set a specific fallback item
     */
    public ClickableItem canSee(Predicate<Player> canSee) {
        return canSee(canSee, null);
    }

    /**
     * Sets a test to check if a player is allowed to see this item.
     * <br>
     * If the player is <b>not</b> allowed to see the item, the fallback item will be used instead.
     * <br>
     * Note: If the player is not allowed to see the item, the on click handler will not be run
     * <br>
     * Examples:
     * <ul>
     *     <li><code>.canSee(player -> player.hasPermission("my.permission"), backgroundItem)</code></li>
     *     <li><code>.canSee(player -> player.getHealth() >= 10, backgroundItem)</code></li>
     * </ul>
     *
     * @param canSee       the test, if a player should be allowed to see this item
     * @param fallBackItem the item that should be used, if the player is <b>not</b> allowed to see the item
     * @return <code>this</code> for a builder-like usage
     * @see #canSee(Predicate) If you want the slot to be empty
     */
    public ClickableItem canSee(Predicate<Player> canSee, ItemStack fallBackItem) {
        this.canSee = canSee;
        this.notVisibleFallBackItem = fallBackItem;
        return this;
    }

    /**
     * Sets a test to check if a player is allowed to click the item.
     * <br>
     * If a player is not allowed to click this item, the on click handler provided at creation will not be run
     *
     * @param canClick the test, if a player should be allowed to see this item
     * @return <code>this</code> for a builder-like usage
     */
    public ClickableItem canClick(Predicate<Player> canClick) {
        this.canClick = canClick;
        return this;
    }


}


/*
import java.util.List;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClickableItem_ {
    
    private final ItemStack item;
    private final ItemMeta meta;
    private final Consumer<InventoryClickEvent> consumer;
    
    public ClickableItem_(final ItemStack item, final Consumer<InventoryClickEvent> consumer) {
        this.item = item;
        meta = item.getItemMeta();
        this.consumer = consumer;
    }
    
    public static ClickableItem_ empty(final ItemStack item) {
        return of( item, p0 -> {} );
    }
    
    public static ClickableItem_ of(final ItemStack item, final Consumer<InventoryClickEvent> consumer) {
        return new ClickableItem_(item, consumer);
    }
    
    public void run(final InventoryClickEvent e) {
        consumer.accept(e);
    }
    
    public ItemStack getItem() {
        return item;
    }

    public void lore(final List<String> lore) {
        meta.deLore().lore(lore);
        item.setItemMeta(meta);
    }
    
    public void setLine(final int line, final String str) {
        if (!meta.hasLore()) return;
        final List<String> lore = meta.getLore();
        lore.set(line, str);
        lore(lore);
    }
    
    
    
    
    
    
    
    
    
}

*/
