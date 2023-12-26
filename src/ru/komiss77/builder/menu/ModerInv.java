package ru.komiss77.builder.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class ModerInv implements InventoryProvider {

	private static final ItemStack fill = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§8.").build();
	
	@Override
	public void init(final Player p, final InventoryContent its) {
		its.fillRect(0, 0, 2, 8, ClickableItem.empty(fill));
		
		its.set(10, ClickableItem.of(new ItemBuilder(Material.PAPER)
            .name("§6Репорты")
            .addLore("§7Модерация")
            .addLore("")
            .addLore("§7Просмотр репортов")
            .addLore("")
            .addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e -> p.performCommand("report")));
		
		its.set(11, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
            .name("§7Шпионаж")
            .addLore("§7Модерация")
            .addLore("")
            .addLore("§7Скрытый контроль")
            .addLore("§7действий игроков.")
            .addLore("")
            .addLore(p.hasPermission("ostrov.spy") ? "§7ЛКМ - открыть" : "§cv=нет права ostrov.spy")
            .addLore("")
            .build(), e -> p.performCommand("spy")));
		
		its.set(22, ClickableItem.of( new ItemBuilder(Material.PRISMARINE_WALL).name("§cПопустить Модера").build(), e -> p.performCommand("builder end")));
	}

}
