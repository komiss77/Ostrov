package ru.komiss77.modules.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

public class FlagMenu implements InventoryProvider {
	
    private final ItemStack it;

    public FlagMenu(final ItemStack it) {
        this.it = it;
    }
    
    @Override
    public void init(final Player p, final InventoryContent its) {
        final ItemMeta im = it.getItemMeta();
        
        its.set(4, ClickableItem.from(new ItemBuilder(it).addLore(" ").addLore("§фКлик §7 - подтвердить").build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                SmartInventory.builder().id("Item " + p.getName()).provider(new ItemMenu(it.hasItemMeta() ? it : new ItemStack(it)))
                    .size(3, 9).title("      §6Создание Предмета").build().open(p);
            }
        }));
        
        for (final ItemFlag hf : ItemFlag.values()) {
        	final ItemStack fli = im.hasItemFlag(hf) ? new ItemBuilder(Material.LIGHT_BLUE_DYE).name("§7Флаг: §b" + hf.name() + " §7[§чКлик§7]").build() 
    			: new ItemBuilder(Material.GRAY_DYE).name("§7Флаг: §ч" + hf.name() + " §7[§bКлик§7]").build();
        	its.set(hf.ordinal() > 3 ? hf.ordinal() + 1 : hf.ordinal(), ClickableItem.from(fli, e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                	if (im.hasItemFlag(hf)) im.removeItemFlags(hf);
                	else im.addItemFlags(hf);
                	it.setItemMeta(im);
                	reopen(p, its);
                }
            }));
        }
    }
}
