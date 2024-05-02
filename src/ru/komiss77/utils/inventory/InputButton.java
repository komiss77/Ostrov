package ru.komiss77.utils.inventory;

import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.PlayerInput;


public class InputButton extends ClickableItem {
    
    private Consumer<InventoryClickEvent> onRightClick;

    
    public InputButton(final InputType type, final Consumer<String> result) {
        this(type, new ItemStack(Material.BOOK), "Input", result);
    }
    
    public InputButton(final InputType type, final ItemStack icon, final String suggest, final Consumer<String> result) {
    	this(icon, e -> PlayerInput.get (type, (Player) e.getWhoClicked(), inputMsg -> result.accept(inputMsg), suggest ) );
    }
    
    public InputButton onRightClick(final Consumer<InventoryClickEvent> consumer) {
        onRightClick = consumer;
        return this;
    }
    
    
    private InputButton (final ItemStack icon, final Consumer<InventoryClickEvent> consumer) {
        super(icon, consumer, true);
    }
    
    
    @Override
    public void run(final ItemClickData e) {
        if (onRightClick != null && e.getClick() == ClickType.RIGHT && e.getEvent() instanceof InventoryClickEvent) {
            onRightClick.accept((InventoryClickEvent) e.getEvent());
            return;
        }
        super.run(e);
    }
    
    
    
    public enum InputType {
        CHAT, ANVILL, SIGN;
    }
    
}
