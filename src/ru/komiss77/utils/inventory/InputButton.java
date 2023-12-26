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
        /*this(icon,
                type == InputType.SIGN ? (  e -> { PlayerInput.get (InputType.SIGN, (Player)e.getWhoClicked(), inputMsg -> {result.accept(inputMsg);}, suggest ); } ) :
                            (type == InputType.CHAT ? ( e -> { PlayerInput.get (InputType.CHAT, (Player)e.getWhoClicked(), inputMsg -> {result.accept(inputMsg);}, suggest ); } ) :
                                    ( e -> new AnvilGUI(Ostrov.instance, (Player)e.getWhoClicked(), suggest.replaceAll("§", "&"), (p1, inputMsg) -> { result.accept(inputMsg.replaceAll("&k", "").replaceAll("&", "§")); return null; }) )     )
                
                
        );*/
    }
    
    public InputButton onRightClick(final Consumer<InventoryClickEvent> consumer) {
        onRightClick = consumer;
        return this;
    }
    
    
    
    /*@Deprecated
    public InputButton(final Consumer<String> result) {
        this(new ItemStack(Material.BOOK), "Input", result);
    }
    
    @Deprecated
    public InputButton(final ItemStack icon, final String suggest, final Consumer<String> result) {
        this( icon, inventoryClickEvent -> new AnvilGUI(Ostrov.instance, (Player)inventoryClickEvent.getWhoClicked(), suggest, (p1, s) -> {
            result.accept(s);
            return null;
        }));
    }*/
    
    
    



    
    
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
