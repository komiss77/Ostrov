package ru.komiss77.builder.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.builder.menu.ViewPerm.SelectPlayer;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

public class AdminInv implements InventoryProvider {

    private static final ItemStack fill = new ItemBuilder(Material.MAGENTA_STAINED_GLASS_PANE).name("§8.").build();

    @Override
    public void init(final Player p, final InventoryContent its) {
        its.fillRect(0, 0, 2, 8, ClickableItem.empty(fill));

        its.set(10, ClickableItem.of(new ItemBuilder(Material.COW_SPAWN_EGG)
            .name("§7Сущности")
            .lore("§7Управление")
            .lore("")
            .lore("§7поиск, просмотр и удаление")
            .lore("§7сущностей")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e -> p.performCommand("entity")));

        its.set(11, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
            .name("§7Фигуры")
            .lore("§7Управление")
            .lore("")
            .lore("§7Открыть главное")
            .lore("§7меню фигур")
            .lore("")
            .lore("§7ЛКМ - получить")
            .lore("")
            .build(), e -> p.performCommand("figure")));
		
	/*	its.set(12, ClickableItem.of(new ItemBuilder(Material.LIME_DYE)
            .name("§7Проверить права")
            .addLore("§7Показать загруженные")
            .addLore("§7пермишены для")
            .addLore("§7этого сервера")
            .addLore("")
            .build(), e -> SmartInventory.builder()
                .id("Чьи права показать")
                .provider(new SelectPlayer())
                .size(6, 9)
                .title("Чьи права показать?")
                .build()
                .open(p)));*/
        its.set(12, ClickableItem.of(new ItemBuilder(Material.LIME_DYE)
            .name("§7Пермишены")
            .lore("")
            .lore("§7ЛКМ - Проверить права игрока")
            .lore("")
            .lore("§7ПКМ - Показать загруженные")
            .lore("§7группы и пермишены для")
            .lore("§7этого сервера")
            //.addLore("§7ЛКМ - открыть")
            .lore("")
            .build(), e -> {
            if (e.isLeftClick()) {
                SmartInventory.builder()
                    .id("Чьи права показать")
                    .provider(new SelectPlayer())
                    .size(6, 9)
                    .title("Чьи права показать?")
                    .build()
                    .open(p);
            } else if (e.isRightClick()) {
                SmartInventory.builder()
                    .id("Загружанные группы")
                    .provider(new ViewGroups())
                    .size(6, 9)
                    .title("Загружанные группы")
                    .build()
                    .open(p);
            }
        }));

        its.set(13, ClickableItem.of(new ItemBuilder(Material.GOLD_INGOT)
            .name("§7Редактор миссий")
            .lore("")
            .build(), e -> MissionManager.openMissionsEditMenu(p)));

        its.set(14, ClickableItem.of(new ItemBuilder(Material.GOLDEN_SWORD)
            .name("§6Наборы")
            .lore("§7Настройки")
            .lore("")
            .lore("§7ЛКМ - получение")
            .lore("§7ПКМ - редактировать")
            .lore("")
            .build(), e -> p.performCommand(e.isRightClick() ? "kit admin" : "kit")));

        its.set(22, ClickableItem.of(new ItemBuilder(Material.CRIMSON_FENCE).name("§cЗакрыть Абьюзера").build(), e -> p.performCommand("builder end")));
    }

}
