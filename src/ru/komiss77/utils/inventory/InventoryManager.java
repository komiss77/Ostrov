package ru.komiss77.utils.inventory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import ru.komiss77.Objects.CaseInsensitiveMap;

public class InventoryManager {
    
    private static InventoryManager instance;
    private final CaseInsensitiveMap<SmartInventory> inventories;
    private final CaseInsensitiveMap<InventoryContent> contents;
    private final ChestInventoryOpener chestOpener;
    private final SpecialInventoryOpener otherOpener;
    
    protected InventoryManager() {
        instance = this;
        this.inventories = new CaseInsensitiveMap();
        this.contents = new CaseInsensitiveMap();
        this.chestOpener = new ChestInventoryOpener();
        this.otherOpener = new SpecialInventoryOpener();
    }
    
    public static InventoryManager get() {
        Preconditions.checkNotNull(InventoryManager.instance, "Unable to retrieve InventoryManager instance - Variable not initialized");
        return InventoryManager.instance;
    }
    
    public Optional<InventoryOpener> findOpener(final InventoryType type) {
        if (type == InventoryType.CHEST && chestOpener.supports(type)) {
            return Optional.of(chestOpener);
        }
        if (otherOpener.supports(type)) {
            return Optional.of(otherOpener);
        }
        return Optional.empty();
    }
    
    public List<Player> getOpenedPlayers(final SmartInventory inv) {
        final List<Player> list = new ArrayList<>();
        inventories.forEach( (name, obj) -> {
            if (inv.equals(obj) && Bukkit.getPlayer(name)!=null) {
                list.add(Bukkit.getPlayer(name));
            }
            //return;
        });
        return list;
    }
    
    public Optional<SmartInventory> getInventory(final Player p) {
        return Optional.ofNullable(inventories.get(p.getName()));
    }
    
    protected void setInventory(final Player p, final SmartInventory inv) {
        if (inv == null) {
            inventories.remove(p.getName());
        }
        else {
            inventories.put(p.getName(), inv);
        }
    }
    
    public Optional<InventoryContent> getContents(final Player p) {
        return Optional.ofNullable(contents.get(p.getName()));
    }
    
    protected void setContents(final Player p, final InventoryContent contents) {
        if (contents == null) {
            this.contents.remove(p.getName());
        }
        else {
            this.contents.put(p.getName(), contents);
        }
    }
    
    public Map<String, SmartInventory> getInventories() {
        return inventories;
    }
    
    public Map<String, InventoryContent> getContents() {
        return contents;
    }
}
