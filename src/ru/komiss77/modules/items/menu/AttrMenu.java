package ru.komiss77.modules.items.menu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

import static org.bukkit.attribute.Attribute.*;

public class AttrMenu implements InventoryProvider {

    private final ItemStack it;

    public AttrMenu(final ItemStack it) {
        this.it = it;
    }

    private final List<Attribute> ATTS = List.of(ARMOR, ARMOR_TOUGHNESS, ATTACK_DAMAGE, ATTACK_SPEED, FOLLOW_RANGE);

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 0.8f);

        its.fillRow(0, ClickableItem.empty(new ItemBuilder(ItemType.MAGENTA_STAINED_GLASS_PANE).name("<black>.").build()));

        its.set(4, ClickableItem.from(new ItemBuilder(it).lore(" ")
            .lore("§фКлик §7 - подтвердить").build(), e -> ItemMenu.open(p, it)));

        buildAttr(GRAVITY, 0, p, its);
        buildAttr(FALL_DAMAGE_MULTIPLIER, 1, p, its);

        buildAttr(SCALE, 7, p, its);
        buildAttr(STEP_HEIGHT, 8, p, its);

        buildAttr(ATTACK_DAMAGE, 9, p, its);
        buildAttr(ATTACK_SPEED, 10, p, its);
        buildAttr(ATTACK_KNOCKBACK, 11, p, its);
        buildAttr(KNOCKBACK_RESISTANCE, 12, p, its);
        buildAttr(ARMOR, 13, p, its);
        buildAttr(ARMOR_TOUGHNESS, 14, p, its);
        buildAttr(BLOCK_INTERACTION_RANGE, 15, p, its);
        buildAttr(ENTITY_INTERACTION_RANGE, 16, p, its);
        buildAttr(MAX_HEALTH, 17, p, its);

        buildAttr(BLOCK_BREAK_SPEED, 19, p, its);
        buildAttr(MINING_EFFICIENCY, 20, p, its);
        buildAttr(JUMP_STRENGTH, 21, p, its);
        buildAttr(WATER_MOVEMENT_EFFICIENCY, 22, p, its);
        buildAttr(MOVEMENT_SPEED, 23, p, its);
        buildAttr(SNEAKING_SPEED, 24, p, its);
        buildAttr(SUBMERGED_MINING_SPEED, 25, p, its);
    }

    private void buildAttr(final Attribute at, final int slot, final Player p, final InventoryContent its) {
        final ItemMeta im = it.getItemMeta();
        final Collection<AttributeModifier> atm = im.hasAttributeModifiers() ? im.getAttributeModifiers(at) : Collections.emptyList();

        if (atm == null || atm.isEmpty()) {
            its.set(slot, new InputButton(InputType.ANVILL,
                new ItemBuilder(ItemType.PINK_DYE).name("§7Аттрибут: §к" + at.key().asString())
                    .lore("§7Сейчас: §8не указан").lore("§кКлик §7- изменить").build(), "", msg -> {
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
                            im.addAttributeModifier(at, new AttributeModifier(at.getKey(), amt, Operation.ADD_NUMBER, it.getType().getEquipmentSlot().getGroup()));
                            break;
                        case '*':
                            im.removeAttributeModifier(at);
                            im.addAttributeModifier(at, new AttributeModifier(at.getKey(), amt, Operation.MULTIPLY_SCALAR_1, it.getType().getEquipmentSlot().getGroup()));
                            break;
                        case '%':
                            im.removeAttributeModifier(at);
                            im.addAttributeModifier(at, new AttributeModifier(at.getKey(), amt, Operation.ADD_SCALAR, it.getType().getEquipmentSlot().getGroup()));
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
                }
            }));
        } else {
            its.set(slot, new InputButton(InputType.ANVILL,
                new ItemBuilder(ItemType.PINK_DYE).name("§7Аттрибут: §к" + at.key().value())
                    .lore("§7Сейчас: §к" + getAtrStr(atm.iterator().next())).lore("§кКлик §7- изменить").build(),
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
                            im.addAttributeModifier(at, new AttributeModifier(at.getKey(), amt, Operation.ADD_NUMBER, it.getType().getEquipmentSlot().getGroup()));
                            break;
                        case '*':
                            im.removeAttributeModifier(at);
                            im.addAttributeModifier(at, new AttributeModifier(at.getKey(), amt, Operation.MULTIPLY_SCALAR_1, it.getType().getEquipmentSlot().getGroup()));
                            break;
                        case '%':
                            im.removeAttributeModifier(at);
                            im.addAttributeModifier(at, new AttributeModifier(at.getKey(), amt, Operation.ADD_SCALAR, it.getType().getEquipmentSlot().getGroup()));
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
                }
            }));
        }
    }

    private String getAtrStr(final AttributeModifier atm) {
        return switch (atm.getOperation()) {
            case ADD_SCALAR -> "§a%" + StringUtil.toSigFigs((float) atm.getAmount(), (byte) 3);
            case MULTIPLY_SCALAR_1 -> "§b*" + StringUtil.toSigFigs((float) atm.getAmount(), (byte) 3);
            default -> "§e+" + StringUtil.toSigFigs((float) atm.getAmount(), (byte) 3);
        };
    }
}
