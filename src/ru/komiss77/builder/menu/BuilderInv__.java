package ru.komiss77.builder.menu;
/*
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.listener.PlayerLst;
import ru.komiss77.modules.displays.DisplayMenu;
import ru.komiss77.modules.items.menu.ItemMenu;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

public class BuilderInv__ implements InventoryProvider {

    private static final ItemStack fill = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).name("§8.").build();

    @Override
    public void init(final Player p, final InventoryContent its) {
        its.fillRect(0, 0, 2, 8, ClickableItem.empty(fill));
		
        its.set(10, ClickableItem.of(new ItemBuilder(Material.ANVIL)
            .name("§6Меню локального сервера")
            .build(), e-> {
                PM.getOplayer(p).setup.lastEdit = "LocalGame";
                PM.getOplayer(p).setup.openLocalGameMenu(p);
            }));

        its.set(11, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
            .name("§7Миры")
            .addLore("§7Управление")
            .addLore("")
            .addLore("§7- перемещение в миры")
            .addLore("§7- настройки миров")
            .addLore("")
            .addLore("§6Вы находитесь в биоме:")
            .addLore("§e" + p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
            .addLore("")
            .addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e -> p.performCommand("world")));
		
        its.set(12, ClickableItem.of(new ItemBuilder(Material.ENDER_PEARL)
            .name("§7Места")
            .addLore("§7Управление")
            .addLore("")
            .addLore("§7Настройка варпов")
            .addLore("")
            .addLore( "§7ЛКМ - открыть" )
            .addLore("")
            .build(), e -> p.performCommand("warp")));
		
        its.set(13, ClickableItem.of(new ItemBuilder(Material.CARTOGRAPHY_TABLE)
            .name("§7Схематики")
            .addLore("")
            .addLore("§7Создание/редактирование/удаление")
            .addLore("§7схематиков")
            .addLore("")
            .addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e-> PM.getOplayer(p).setup.openSchemMainMenu(p)));
		
        its.set(14, ClickableItem.of(new ItemBuilder(Material.NOTE_BLOCK)
            .name("§7Прослушивание звуков")
            .addLore("§7Утилита")
            .addLore("")
            .addLore("§7ЛКМ - открыть")
            .addLore("")
            .build(), e -> p.performCommand("sound")));
		
        its.set(15, ClickableItem.of(new ItemBuilder(Material.WARPED_SIGN)
            .name("§7Таблички")
            .addLore("§7Утилита")
            .addLore("")
            .addLore("§7Получить предмет")
            .addLore("§7помогающий в работ	е")
            .addLore("§7с табличками")
            .addLore("")
            .addLore("§7ЛКМ - получить")
            .addLore("")
            .build(), e -> p.getInventory().addItem(PlayerLst.signEdit.clone())));
		
        its.set(6, ClickableItem.of(new ItemBuilder(Material.SMITHING_TABLE)
            .name("§7Создание предмета")
            .addLore("§7Утилита")
            .addLore("")
            .addLore("§7Клик - выдать / изменить")
            .addLore("§7предмет в левой руке")
            .addLore("")
            .build(), e -> {
                final ItemStack it = p.getInventory().getItemInOffHand();
                if (ItemUtils.isBlank(it, false)) {
                    p.sendMessage(Ostrov.PREFIX + "§cНужно держать что-то в левой руке!");
                    p.closeInventory();
                } else {
                    SmartInventory.builder().id("Item " + p.getName()).provider(new ItemMenu(it.hasItemMeta() ? it : new ItemStack(it)))
                        .size(3, 9).title("      §6Создание Предмета").build().open(p);
                }
            }));
		
        if (Config.displays) {
            its.set(24, ClickableItem.of(new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .name("§7Дисплей")
                .addLore("§7Утилита")
                .addLore("")
                .addLore("§7ЛКМ - найти дисплей рядом")
                .addLore("§7Шифт + ЛКМ - тп дисплей рядом")
                .addLore("§7ПКМ - создать дисплей")
                .addLore("§7Шифт + ПКМ - клон дисплея рядом")
                .addLore("")
                .build(), e -> {
                	final Display tds;
                	final Location loc = p.getLocation();
                	if (e.isLeftClick()) {
            			tds = LocationUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
                		if (tds != null && e.isShiftClick()) tds.teleport(loc);
                	} else {
                		if (e.isShiftClick()) {
                			final Display ods = LocationUtil.getClsChEnt(new WXYZ(loc), 100, Display.class, en -> true);
                			if (ods != null) {
                				switch (ods.getType()) {
								case BLOCK_DISPLAY:
									tds = p.getWorld().spawn(p.getLocation(), BlockDisplay.class);
									((BlockDisplay) tds).setBlock(((BlockDisplay) ods).getBlock());
									break;
								case ITEM_DISPLAY:
									tds = p.getWorld().spawn(p.getLocation(), ItemDisplay.class);
									((ItemDisplay) tds).setItemStack(((ItemDisplay) ods).getItemStack());
									((ItemDisplay) tds).setItemDisplayTransform(((ItemDisplay) ods).getItemDisplayTransform());
									break;
								case TEXT_DISPLAY:
									tds = p.getWorld().spawn(p.getLocation(), TextDisplay.class);
									((TextDisplay) tds).text(((TextDisplay) ods).text());
									((TextDisplay) tds).setSeeThrough(((TextDisplay) ods).isSeeThrough());
									((TextDisplay) tds).setShadowed(((TextDisplay) ods).isShadowed());
									((TextDisplay) tds).setLineWidth(((TextDisplay) ods).getLineWidth());
									break;
								default:
									tds = p.getWorld().spawn(p.getLocation(), TextDisplay.class);
									break;
								}
								tds.setPersistent(true);
								tds.setBillboard(ods.getBillboard());
								final Transformation atr = ods.getTransformation();
								tds.setTransformation(new Transformation(atr.getTranslation(), 
									atr.getLeftRotation(), atr.getScale(), atr.getRightRotation()));
                			} else tds = null;
                		} else {
                			tds = p.getWorld().spawn(p.getLocation(), TextDisplay.class);
                			((TextDisplay) tds).text(TCUtils.format("§оКекст"));
                		}
                	}
                	
                	if (tds == null) {
                		p.sendMessage(Ostrov.PREFIX + "Дисплей не найден!");
                		p.closeInventory();
                		return;
                	}
                	
            		SmartInventory.builder().id(p.getName() + " Display").title("     §яНастройки Дисплея")
            			.provider(new DisplayMenu(tds)).size(3, 9).build().open(p);
                }));
        }
		
        its.set(22, ClickableItem.of( new ItemBuilder(Material.ACACIA_FENCE).name("§cЗакрыть Cтроителя").build(), e -> p.performCommand("builder end")));
    }

}
*/
