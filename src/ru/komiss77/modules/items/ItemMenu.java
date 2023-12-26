package ru.komiss77.modules.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.enchants.CustomEnchant;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;



public class ItemMenu implements InventoryProvider {

    private static final ItemStack[] invIts;
    private final ItemStack it;

    static {
        invIts = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            switch (i) {
                case 22:
                    invIts[i] = new ItemBuilder(Material.HONEYCOMB).name("§6Выдать Предмет").build();
                    break;
                case 16:
                    invIts[i] = new ItemBuilder(Material.BOOK).name("§dЗачарования").build();
                    break;
                case 14:
                    invIts[i] = new ItemBuilder(Material.MOJANG_BANNER_PATTERN).name("§eОписание Предмета").build();
                    break;
                case 13:
                    invIts[i] = new ItemBuilder(Material.POINTED_DRIPSTONE).name("§6§l\\/").build();
                    break;
                case 12:
                    invIts[i] = new ItemBuilder(Material.NAME_TAG).name("§aИмя Предмета").build();
                    break;
                case 10:
                    invIts[i] = new ItemBuilder(Material.QUARTZ).name("§bЗначение Модели").build();
                    break;
                case 4:
                    invIts[i] = new ItemBuilder(Material.AIR).build();
                    break;
                case 3, 5:
                    invIts[i] = new ItemBuilder(Material.HANGING_ROOTS).build();
                    break;
                default:
                    invIts[i] = new ItemBuilder((i & 1) == 1 ? Material.BROWN_STAINED_GLASS_PANE 
                    		: Material.ORANGE_STAINED_GLASS_PANE).name("§0.").build();
                    break;
            }
        }
    }

    public ItemMenu(final ItemStack it) {
        this.it = it.clone();
    }
    
    @Override
    public void init(final Player p, final InventoryContent its) {
        final Inventory inv = its.getInventory();
        final ItemMeta im = it.getItemMeta();
        inv.setContents(invIts);
        inv.setItem(4, it);
        its.set(22, ClickableItem.of(new ItemBuilder(Material.HONEYCOMB).name("§aВыдать (ЛКМ) §6/ §eЗаменить (ПКМ) §6Предмет").build(), e -> {
            switch (e.getClick()) {
                case LEFT:
                case SHIFT_LEFT:
                    p.getInventory().addItem(it);
                    p.sendMessage(Ostrov.PREFIX + "Предмет удачно создан!");
                    break;
                case RIGHT:
                case SHIFT_RIGHT:
                    p.getInventory().setItemInOffHand(it);
                    p.sendMessage(Ostrov.PREFIX + "Предмет удачно заменен!");
                    break;
                default:
                    break;
            }
            p.closeInventory();
        }));

        ItemBuilder prepare = new ItemBuilder(Material.BOOK).name("§dЗачарования");
        if (im.hasEnchants()) {
            prepare.addEnchant(CustomEnchant.GLINT, 1);
            prepare.addLore(" ", "§aЕсть зачарования,", "§8ЛКМ §7- Выдать 'cвечение'", "§cПКМ §7- Снять все зачары");
        } else {
            prepare.addLore(" ", "§7Зачарований нет,", "§dЛКМ §7- Выдать 'cвечение'", "§8ПКМ §7- Снять все зачары");
        }
        its.set(16, ClickableItem.of(prepare.build(), e -> {
        	if (Config.enchants) {
	            switch (e.getClick()) {
	                case LEFT:
	                case SHIFT_LEFT:
	                    if (!im.hasEnchant(CustomEnchant.GLINT)) {
	                        it.addEnchantment(CustomEnchant.GLINT, 1);
	                    }
	                    reopen(p, its);
	                    break;
	                case RIGHT:
	                case SHIFT_RIGHT:
	                    if (im.hasEnchants()) {
	                        for (final Enchantment en : im.getEnchants().keySet()) {
	                            it.removeEnchantment(en);
	                        }
	                    }
	                    it.removeEnchantment(CustomEnchant.GLINT);
	                    reopen(p, its);
	                    break;
	                default:
	                    break;
	            }
        	} else {
                p.sendMessage(Ostrov.PREFIX + "Зачарования выключены!");
			}
        }));

        final StringBuffer sb = new StringBuffer();
        if (im.hasLore()) {
            prepare = new ItemBuilder(Material.MOJANG_BANNER_PATTERN).name("§7Описание:").addLore(im.lore().toArray((Object[]) new Component[im.lore().size()]))
                .addLore(" ", "§eЛКМ §7- Добавить линию", "§eПКМ §7- Убрать посл. линию");
            for (final Component lr : im.lore()) {
                sb.append(TCUtils.toString(lr) + ";");
            }
        } else {
            prepare = new ItemBuilder(Material.MOJANG_BANNER_PATTERN).name("§7Описание: §8(Не Указано)")
                .addLore(" ", "§eЛКМ §7- Добавить линию");
        }
        
        its.set(14, ClickableItem.from(prepare.build(), e -> {
            if (e.getEvent() instanceof final InventoryClickEvent ev) {
            	final List<Component> lrs = im.lore();
            	if (ev.isLeftClick()) {
                    PlayerInput.get(InputType.ANVILL, p, text -> {
                    	if (lrs == null) {
                        	im.lore(Arrays.asList(TCUtils.format(text)));
                    	} else {
                        	lrs.add(TCUtils.format(text));
                        	im.lore(lrs);
                    	}
                    }, "");
            	} else if (lrs != null && !lrs.isEmpty()) {
            		lrs.remove(lrs.size() - 1);
                	im.lore(lrs);
            	}
                reopen(p, its);
            }
        }));
        
        its.set(12, new InputButton(InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
        	.name("§7Имя: " + (im.hasDisplayName() ? TCUtils.toString(im.displayName()) : "§8(Не Указано)"))
        	.addLore(" ", "§aКлик §7- Изменить имя", "§c'-' §7уберет имя предмета").build(), 
               im.hasDisplayName() ? TCUtils.toString(im.displayName()) : "&7Предмет", msg -> {
            im.displayName(msg.equals("-") ? null : TCUtils.format(msg.replace('&', '§')));
            it.setItemMeta(im);
            reopen(p, its);
        }));
        
        its.set(20, ClickableItem.from(new ItemBuilder(Material.ENDER_PEARL).name("§фСкрытые Флаги")
                .addLore("§7Клик - редактировать §ффлаги").build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
        		SmartInventory.builder().id(p.getName() + " Flags").title("     §фНастройки Флагов")
        			.provider(new FlagMenu(it)).size(1, 9).build().open(p);
            }
        }));
        
        its.set(24, ClickableItem.from(new ItemBuilder(Material.ENDER_EYE).name("§кАттрибуты")
                .addLore("§7Клик - редактировать §ксвойства").build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
        		SmartInventory.builder().id(p.getName() + " Flags").title("    §кНастройки Аттрибутов")
        			.provider(new AttrMenu(it)).size(2, 9).build().open(p);
            }
        }));

        final ItemBuilder cmd = new ItemBuilder(Material.QUARTZ);
        if (im.hasCustomModelData()) {
            cmd.name("§7Значение Модели: §b" + String.valueOf(im.getCustomModelData()))
                    .addLore(" ", "§c-1 §7- Сбросить значение модели");
        } else {
            cmd.name("§7Значение Модели: §8(Не Указано)")
                    .addLore(" ", "§8-1 §7- Сбросить значение модели");
        }
        its.set(10, new InputButton(InputType.ANVILL, cmd.build(), "10", msg -> {
            final int cd;
            try {
                cd = Integer.parseInt(msg);
            } catch (NumberFormatException ex) {
                p.sendMessage(Ostrov.PREFIX + "§cЗначение должно быть числом!");
                reopen(p, its);
                return;
            }

            im.setCustomModelData(cd < 0 ? null : cd);
            it.setItemMeta(im);
            reopen(p, its);
        }));
    }
}
