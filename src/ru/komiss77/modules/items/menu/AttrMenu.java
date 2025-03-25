package ru.komiss77.modules.items.menu;

import java.util.Collections;
import java.util.List;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers.Entry;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
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
        final ItemAttributeModifiers iams = it.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        final List<Entry> ates = iams == null ? Collections.emptyList() : iams.modifiers();
        final Entry prevAtr = getEntry(ates, at);

        if (prevAtr == null) {
            its.set(slot, new InputButton(InputType.ANVILL,
                new ItemBuilder(ItemType.PINK_DYE).name("§7Аттрибут: §к" + at.key().asString())
                    .lore("§7Сейчас: §8не указан").lore("§кКлик §7- добавить").build(), "*1", msg -> {
                if (msg.length() < 2) {
                    p.sendMessage(Ostrov.PREFIX + "§cФормат: ±|*|% число");
                    reopen(p, its);
                    return;
                }
                final double amt;
                try {
                    amt = Double.parseDouble(msg.substring(1)) * (msg.charAt(0) == '-' ? -1 : 1);
                } catch (NumberFormatException ex) {
                    p.sendMessage(Ostrov.PREFIX + "§cФормат: ±|*|% число");
                    reopen(p, its);
                    return;
                }

                final ItemAttributeModifiers.Builder iamb = ItemAttributeModifiers.itemAttributes();
                for (final Entry en : ates) iamb.addModifier(en.attribute(), en.modifier(), en.getGroup());
                final EquipmentSlotGroup group = it.getType().getEquipmentSlot().getGroup();
                iamb.showInTooltip(iams == null || iams.showInTooltip());
                switch (msg.charAt(0)) {
                    case '+', '-':
                        iamb.addModifier(at, new AttributeModifier(getKey(at, group),//add_value
                            amt, Operation.ADD_NUMBER, group), group);//[Base] + [amt] + ...
                        break;
                    case '*':
                        iamb.addModifier(at, new AttributeModifier(getKey(at, group),//add_multiplied_total
                            amt, Operation.MULTIPLY_SCALAR_1, group), group);//[(1 + amt) * Base] + ...
                        break;
                    case '%':
                        iamb.addModifier(at, new AttributeModifier(getKey(at, group),//add_multiplied_base
                            amt, Operation.ADD_SCALAR, group), group);//[Base] + [Base * amt] + ...
                        break;
                    default:
                        p.sendMessage(Ostrov.PREFIX + "§cФормат: ±|*|% число");
                        reopen(p, its);
                        return;
                }
                it.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, iamb.build());
                reopen(p, its);
            }));
        } else {
            its.set(slot, ClickableItem.of(new ItemBuilder(ItemType.PINK_DYE).name("§7Аттрибут: §к" + at.key().value())
                .lore("§7Сейчас: §к" + getAtrStr(prevAtr.modifier())).lore("§кЛКМ §7- изменить").lore("§4ПКМ §7- удалить").build(), e -> {
                if (e.isRightClick()) {
                    final ItemAttributeModifiers.Builder iamb = ItemAttributeModifiers.itemAttributes();
                    for (final Entry en : ates) {
                        if (en.attribute().equals(at)) continue;
                        iamb.addModifier(en.attribute(), en.modifier(), en.getGroup());
                    }
                    iamb.showInTooltip(iams == null || iams.showInTooltip());
                    it.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, iamb.build());
                    reopen(p, its);
                    return;
                }
                PlayerInput.get(InputType.ANVILL, p, msg -> {
                    if (msg.length() < 2) {
                        p.sendMessage(Ostrov.PREFIX + "§cФормат: ±|*|% число");
                        reopen(p, its);
                        return;
                    }
                    final double amt;
                    try {
                        amt = Double.parseDouble(msg.substring(1)) * (msg.charAt(0) == '-' ? -1 : 1);
                    } catch (NumberFormatException ex) {
                        p.sendMessage(Ostrov.PREFIX + "§cФормат: ±|*|% число");
                        reopen(p, its);
                        return;
                    }

                    final ItemAttributeModifiers.Builder iamb = ItemAttributeModifiers.itemAttributes();
                    for (final Entry en : ates) {
                        if (en.attribute().equals(at)) continue;
                        iamb.addModifier(en.attribute(), en.modifier(), en.getGroup());
                    }
                    iamb.showInTooltip(iams == null || iams.showInTooltip());
                    final AttributeModifier mod = prevAtr.modifier();
                    switch (msg.charAt(0)) {
                        case '+', '-':
                            iamb.addModifier(at, new AttributeModifier(mod.getKey(),
                                mod.getOperation() == Operation.ADD_NUMBER
                                    ? mod.getAmount() + amt : mod.getAmount() / (1 + amt),
                                mod.getOperation(), mod.getSlotGroup()), prevAtr.getGroup());
                            break;
                        case '*', '%':
                            iamb.addModifier(at, new AttributeModifier(mod.getKey(),
                                mod.getOperation() == Operation.ADD_NUMBER
                                    ? mod.getAmount() * (1 + amt) : mod.getAmount() + amt,
                                mod.getOperation(), mod.getSlotGroup()), prevAtr.getGroup());
                            break;
                        default:
                            p.sendMessage(Ostrov.PREFIX + "§cФормат: ±|*|% число");
                            reopen(p, its);
                            return;
                    }
                    it.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, iamb.build());
                    reopen(p, its);
                }, TCUtil.strip(getAtrStr(prevAtr.modifier())));
            }));
        }
    }

    private NamespacedKey getKey(final Attribute at, final EquipmentSlotGroup grp) {
        if (at.equals(ATTACK_DAMAGE)) return NamespacedKey.minecraft("base_attack_damage");
        if (at.equals(ATTACK_SPEED)) return NamespacedKey.minecraft("base_attack_speed");
        if (!at.equals(ARMOR) && !at.equals(ARMOR_TOUGHNESS)
            && !at.equals(KNOCKBACK_RESISTANCE)) return at.getKey();
        if (grp.equals(EquipmentSlotGroup.BODY)) return NamespacedKey.minecraft("armor.body");
        if (grp.test(EquipmentSlot.CHEST)) return NamespacedKey.minecraft("armor.chestplate");
        if (grp.test(EquipmentSlot.LEGS)) return NamespacedKey.minecraft("armor.leggings");
        if (grp.test(EquipmentSlot.HEAD)) return NamespacedKey.minecraft("armor.helmet");
        if (grp.test(EquipmentSlot.FEET)) return NamespacedKey.minecraft("armor.boots");
        return at.getKey();
    }

    private Entry getEntry(final List<Entry> ents, final Attribute at) {
        for (final Entry en : ents) {
            if (en.attribute().equals(at)) return en;
        }
        return null;
    }

    private String getAtrStr(final AttributeModifier atm) {
        return switch (atm.getOperation()) {
            case ADD_SCALAR -> "§a%" + StringUtil.toSigFigs(atm.getAmount(), (byte) 3);
            case MULTIPLY_SCALAR_1 -> "§b*" + StringUtil.toSigFigs(atm.getAmount(), (byte) 3);
            default -> (atm.getAmount() < 0 ? "§e" : "§e+") + StringUtil.toSigFigs(atm.getAmount(), (byte) 3);
        };
    }
}
