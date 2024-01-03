package ru.komiss77.modules.items;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

public class AttrMenu implements InventoryProvider {
	
    private final ItemStack it;

    public AttrMenu(final ItemStack it) {
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
        
        for (final Attribute at : Attribute.values()) {
        	final Collection<AttributeModifier> atm = im.hasAttributeModifiers() ? im.getAttributeModifiers(at) : Collections.emptyList();
        	final int slot = switch (at) {
				case GENERIC_ARMOR -> 1;
				case GENERIC_ARMOR_TOUGHNESS -> 2;
				case GENERIC_ATTACK_DAMAGE -> 6;
				case GENERIC_ATTACK_KNOCKBACK -> 7;
				case GENERIC_ATTACK_SPEED -> 10;
				case GENERIC_FLYING_SPEED -> 11;
				case GENERIC_FOLLOW_RANGE -> 12;
				case GENERIC_KNOCKBACK_RESISTANCE -> 14;
				case GENERIC_MAX_HEALTH -> 15;
				case GENERIC_MOVEMENT_SPEED -> 16;
				default -> 0;
			};
        	
        	if (atm == null || atm.isEmpty()) {
                its.set(slot, new InputButton(InputType.ANVILL, 
            		new ItemBuilder(Material.PINK_DYE).name("§7Аттрибут: §к" + at.name().toLowerCase())
            		.addLore("§7Сейчас: §8не указан").addLore("§кКлик §7- изменить").build(), "", msg -> {
                    if (msg.length() > 1) {
                    	final double amt;
                    	try {
							amt = Double.parseDouble(msg.substring(1));
						} catch (NumberFormatException ex) {
                        	p.sendMessage(Ostrov.PREFIX + "§cФормат: +|*|% число");
                            reopen(p, its);
                        	return;
						}
                    	
                    	switch (msg.charAt(0)) {
                    	case '+':
                    		im.removeAttributeModifier(at);
                    		im.addAttributeModifier(at, new AttributeModifier(UUID.randomUUID(), 
                    			at.name().substring(8).toLowerCase(), amt, Operation.ADD_NUMBER, it.getType().getEquipmentSlot()));
                    		break;
                    	case '*':
                    		im.removeAttributeModifier(at);
                    		im.addAttributeModifier(at, new AttributeModifier(UUID.randomUUID(), 
                    			at.name().substring(8).toLowerCase(), amt, Operation.MULTIPLY_SCALAR_1, it.getType().getEquipmentSlot()));
                    		break;
                    	case '%':
                    		im.removeAttributeModifier(at);
                    		im.addAttributeModifier(at, new AttributeModifier(UUID.randomUUID(), 
                    			at.name().substring(8).toLowerCase(), amt, Operation.ADD_SCALAR, it.getType().getEquipmentSlot()));
                    		break;
                		default:
                        	p.sendMessage(Ostrov.PREFIX + "§cФормат: +|*|% число");
                            reopen(p, its);
                        	return;
                    	}
                        it.setItemMeta(im);
                        reopen(p, its);
                    } else {
                    	p.sendMessage(Ostrov.PREFIX + "§cФормат: +|*|% число");
                        reopen(p, its);
                    	return;
                    }
                }));
        	} else {
                its.set(slot, new InputButton(InputType.ANVILL, 
            		new ItemBuilder(Material.PINK_DYE).name("§7Аттрибут: §к" + at.name().toLowerCase())
            		.addLore("§7Сейчас: §к" + getAtrStr(atm.iterator().next())).addLore("§кКлик §7- изменить").build(), 
            		getAtrStr(atm.iterator().next()).substring(2), msg -> {
                    if (msg.length() > 1) {
                    	final double amt;
                    	try {
							amt = Double.parseDouble(msg.substring(1));
						} catch (NumberFormatException ex) {
                        	p.sendMessage(Ostrov.PREFIX + "§cФормат: +|*|% число");
                            reopen(p, its);
                        	return;
						}
                    	
                    	switch (msg.charAt(0)) {
                    	case '+':
                    		im.removeAttributeModifier(at);
                    		im.addAttributeModifier(at, new AttributeModifier(UUID.randomUUID(), 
                    			at.name().substring(8).toLowerCase(), amt, Operation.ADD_NUMBER, it.getType().getEquipmentSlot()));
                    		break;
                    	case '*':
                    		im.removeAttributeModifier(at);
                    		im.addAttributeModifier(at, new AttributeModifier(UUID.randomUUID(), 
                    			at.name().substring(8).toLowerCase(), amt, Operation.MULTIPLY_SCALAR_1, it.getType().getEquipmentSlot()));
                    		break;
                    	case '%':
                    		im.removeAttributeModifier(at);
                    		im.addAttributeModifier(at, new AttributeModifier(UUID.randomUUID(), 
                    			at.name().substring(8).toLowerCase(), amt, Operation.ADD_SCALAR, it.getType().getEquipmentSlot()));
                    		break;
                		default:
                        	p.sendMessage(Ostrov.PREFIX + "§cФормат: +|*|% число");
                            reopen(p, its);
                        	return;
                    	}
                        it.setItemMeta(im);
                        reopen(p, its);
                    } else {
                    	p.sendMessage(Ostrov.PREFIX + "§cФормат: +|*|% число");
                        reopen(p, its);
                    	return;
                    }
                }));
			}
        }
    }

	private String getAtrStr(final AttributeModifier atm) {
		switch (atm.getOperation()) {
		case ADD_SCALAR:
			return "§a%" + ApiOstrov.toSigFigs((float) atm.getAmount(), (byte) 3);
		case MULTIPLY_SCALAR_1:
			return "§b*" + ApiOstrov.toSigFigs((float) atm.getAmount(), (byte) 3);
		default:
			return "§e+" + ApiOstrov.toSigFigs((float) atm.getAmount(), (byte) 3);
		}
	}
}
