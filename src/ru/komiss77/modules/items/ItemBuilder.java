package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import ru.komiss77.OStrap;
import ru.komiss77.Ostrov;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.ItemUtil.Texture;
import ru.komiss77.utils.TCUtil;

public class ItemBuilder {

    private ItemType type;//private Material mat;
    private int amount = 1; //по умолчанию 1, или build() выдаёт AIR!
    private ItemData data = null;
    private CaseInsensitiveMap<Serializable> pdcs = null;
    private EnumSet<ItemFlag> flags = null;

    public ItemBuilder(final ItemType type) {
        if (type == null || ItemType.AIR.equals(type)) {
            throw new IllegalArgumentException("'type' cannot be null or air!");
        }
        this.type = type;
    }

    public ItemBuilder(final ItemStack from) {
        if (from == null || from.isEmpty()) { //каждое from == null ? ниже - отдельное if. Собираем три if в одно.
            throw new IllegalArgumentException("'from' cannot be null or empty!");
        }
        type = from.getType().asItemType();
        amount = from.getAmount();
        final ItemData datas = ItemData.of(from);
        if (datas == null || datas.isEmpty()) return;
        checkData();
    }

    private void checkData() {
        if (data == null) data = new ItemData();
    }

    private @Nullable <D> D get(final DataComponentType.Valued<D> type) {
        if (data == null) return null;
        return data.get(type);
    }

    /*private boolean has(final DataComponentType type) {
        return data != null && data.containsKey(type);
    }*/

    public <D> ItemBuilder set(final DataComponentType.Valued<D> type, final D val) {
        checkData(); data.put(type, val);
        return this;
    }

    public ItemBuilder set(final DataComponentType.NonValued type) {
        checkData(); data.put(type);
        return this;
    }

    public ItemBuilder reset(final DataComponentType type) {
        if (data == null) return this;
        data.remove(type);
        if (data.isEmpty()) data = null;
        return this;
    }

    public ItemBuilder type(final ItemType tp) {
        if (type == null || ItemType.AIR.equals(type)) {
            throw new IllegalArgumentException("'type' cannot be null or air!");
        }
        type = tp;
        return this;
    }

    public ItemBuilder data(final String key, final Serializable data) {
        if (pdcs == null) pdcs = new CaseInsensitiveMap<>();
        pdcs.put(key, data);
        return this;
    }

    public ItemBuilder dePDC() {
        pdcs = null; return this;
    }

    public ItemBuilder amount(final int ammount) {
        this.amount = ammount;
        return this;
    }

    public ItemBuilder maxStack(final int maxStack) {
        set(DataComponentTypes.MAX_STACK_SIZE, maxStack);
        return this;
    }

    public ItemBuilder name(final @Nullable String name) {
        return name == null ? reset(DataComponentTypes.ITEM_NAME)
            : set(DataComponentTypes.ITEM_NAME, TCUtil.form(name));
    }

    public ItemBuilder name(final @Nullable Component name) {
        if (name == null) {
            reset(DataComponentTypes.ITEM_NAME);
            return this;
        }
        set(DataComponentTypes.ITEM_NAME, name);
        return this;
    }

    private List<Component> lores() {
        final ItemLore own = get(DataComponentTypes.LORE);
        return own == null ? new ArrayList<>() : new ArrayList<>(own.lines());
    }

    public ItemBuilder lore(final String s) {
        if (s==null) return this;
        final List<Component> lores = lores();
        lores.add(TCUtil.form(s));
        return set(DataComponentTypes.LORE, ItemLore.lore(lores));
    }

    public ItemBuilder lore(final Collection<String> sc) {
        if (sc==null || sc.isEmpty()) return this;
        final List<Component> lores = lores();
        for (final String s : sc) lores.add(TCUtil.form(s));
        return set(DataComponentTypes.LORE, ItemLore.lore(lores));
    }

    public ItemBuilder lore(final Component c) {
        if (c==null) return this;
        final List<Component> lores = lores();
        lores.add(c);
        return set(DataComponentTypes.LORE, ItemLore.lore(lores));
    }

    public ItemBuilder lore(final List<Component> lc) {
        if (lc==null || lc.isEmpty()) return this;
        final List<Component> lores = lores();
        lores.addAll(lc);
        return set(DataComponentTypes.LORE, ItemLore.lore(lores));
    }

    public ItemBuilder lore(final Component... lores) {
        if (lores == null) return this;
        for (final Component c : lores) {
            lore(c);
        }
        return this;
    }

    public ItemBuilder lore(final String... lores) {
        if (lores == null) return this;
        for (final String c : lores) {
            lore(c);
        }
        return this;
    }

    public ItemBuilder deLore() {
        reset(DataComponentTypes.LORE);
        return this;
    }

    public ItemBuilder repLore(final String from, final String to) {
        return repLore(TCUtil.form(from), TCUtil.form(to));
    }

    public ItemBuilder repLore(final Component from, final Component to) {
        final List<Component> lores = lores();
        if (lores.isEmpty()) return this;
        for (int i = 0; i < lores.size(); i++) {
            if (TCUtil.compare(lores.get(i), from)) {
                lores.set(i, to);
            }
        }
        return set(DataComponentTypes.LORE, ItemLore.lore(lores));
    }

    private boolean isOn(final ItemFlag flag) {
        return flags != null && flags.contains(flag);
    }

    public ItemBuilder flags(final boolean on, final ItemFlag... fls) {
        if (flags == null) flags = EnumSet.noneOf(ItemFlag.class);
        for (final ItemFlag f : fls) {
            if (on) if (!flags.add(f)) continue;
            else if (!flags.remove(f)) continue;
            final ShownInTooltip<?> sit = switch (f) {
                case HIDE_ENCHANTS -> get(DataComponentTypes.ENCHANTMENTS);
                case HIDE_ATTRIBUTES -> get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                case HIDE_UNBREAKABLE -> get(DataComponentTypes.UNBREAKABLE);
                case HIDE_DESTROYS -> get(DataComponentTypes.CAN_BREAK);
                case HIDE_PLACED_ON -> get(DataComponentTypes.CAN_PLACE_ON);
                case HIDE_DYE -> get(DataComponentTypes.DYED_COLOR);
                case HIDE_ARMOR_TRIM -> get(DataComponentTypes.TRIM);
                case HIDE_STORED_ENCHANTS -> get(DataComponentTypes.STORED_ENCHANTMENTS);
                case HIDE_ADDITIONAL_TOOLTIP -> {
                    if (on) set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
                    else reset(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
                    yield null;
                }
            };
            if (sit != null) sit.showInTooltip(!on);
        }
        return this;
    }

    public void trim(final TrimMaterial mat, final TrimPattern pat) {
        set(DataComponentTypes.TRIM, ItemArmorTrim.itemArmorTrim(
            new ArmorTrim(mat, pat), !isOn(ItemFlag.HIDE_ARMOR_TRIM)));
    }

    public ItemBuilder enchant(final Enchantment enchant) {
        return enchant(enchant, 1, false);
    }

    public ItemBuilder enchant(final Enchantment enchant, final int level) {
        return enchant(enchant, level, false);
    }

    public ItemBuilder enchant(final Enchantment enchant, final int level, final boolean stored) {
        final DataComponentType.Valued<ItemEnchantments> type = stored
            ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS;
        final ItemEnchantments ies = get(type);
        final Map<Enchantment, Integer> enchs = ies == null
            ? new HashMap<>() : new HashMap<>(ies.enchantments());
        if (level < 1) {
            enchs.remove(enchant);
            if (enchs.size() == 0)
                return disEnchant();
        }
        enchs.put(enchant, level);
        final ItemFlag flag = stored ? ItemFlag.HIDE_STORED_ENCHANTS : ItemFlag.HIDE_ENCHANTS;
        return set(type, ItemEnchantments.itemEnchantments(enchs, !isOn(flag)));
    }

    public ItemBuilder disEnchant() {
        return reset(DataComponentTypes.ENCHANTMENTS);
    }

    public ItemBuilder unbreak(final boolean set) {
        return set ? set(DataComponentTypes.UNBREAKABLE, Unbreakable
            .unbreakable(!isOn(ItemFlag.HIDE_UNBREAKABLE)))
            : reset(DataComponentTypes.UNBREAKABLE);
    }

    @Deprecated
    public ItemBuilder attribute(final Attribute att, final double amount, final Operation op) {
        attribute(att, amount, op, type.asMaterial().getEquipmentSlot().getGroup());
        return this;
    }

    public ItemBuilder attribute(final Attribute att, final double amount, final Operation op, final EquipmentSlotGroup slotGroup) {
        final ItemAttributeModifiers iams = get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        final ItemAttributeModifiers.Builder iamb = ItemAttributeModifiers.itemAttributes();
        if (iams != null) {
            for (final ItemAttributeModifiers.Entry en : iams.modifiers()) {
                iamb.addModifier(en.attribute(), en.modifier(), en.getGroup());
            }
        }
        return set(DataComponentTypes.ATTRIBUTE_MODIFIERS, iamb.addModifier(att,
            new AttributeModifier(att.getKey(), amount, op, slotGroup))
            .showInTooltip(!isOn(ItemFlag.HIDE_ATTRIBUTES)).build());
    }

    public ItemBuilder removeAttribute(final Attribute att) {
        final ItemAttributeModifiers iams = get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (iams == null) return this;
        final ItemAttributeModifiers.Builder iamb = ItemAttributeModifiers.itemAttributes();
        for (final ItemAttributeModifiers.Entry en : iams.modifiers()) {
            if (att.equals(en.attribute())) continue;
            iamb.addModifier(en.attribute(), en.modifier(), en.getGroup());
        }
        return set(DataComponentTypes.ATTRIBUTE_MODIFIERS, iamb.build());
    }

    //вроде очень редко нужно, думаю не страшно будет ставить всегда одним методом через float
    //хз мне нужно довольно часто
    public ItemBuilder durability(final int dur) {
        final Integer mxd = get(DataComponentTypes.MAX_DAMAGE);
        final int maxDmg = mxd == null ? type.getMaxDurability() : mxd;
        if (maxDmg - dur < 1) return reset(DataComponentTypes.DAMAGE);
        return set(DataComponentTypes.DAMAGE, Math.max(maxDmg - dur, 0));
    }

    public ItemBuilder durability(final float dur) {
        if (dur == 1f) return reset(DataComponentTypes.DAMAGE);
        final Integer mxd = get(DataComponentTypes.MAX_DAMAGE);
        final int maxDmg = mxd == null ? type.getMaxDurability() : mxd;
        return set(DataComponentTypes.DAMAGE, (int) (maxDmg * (1f - Math.clamp(dur, 0f, 1f))));
    }

    public ItemBuilder maxDamage(final int dur) {
        final Integer dmg = get(DataComponentTypes.DAMAGE);
        final int damage = dmg == null ? 0 : dmg;
        final Integer mxd = get(DataComponentTypes.MAX_DAMAGE);
        final int maxDmg = mxd == null ? type.getMaxDurability() : mxd;
        set(DataComponentTypes.MAX_DAMAGE, dur);
        final float rel = (float) damage / (float) maxDmg;
        return set(DataComponentTypes.DAMAGE, (int) (rel * dur));
    }

    public ItemBuilder glint(final @Nullable Boolean glint) {
        return glint == null ? reset(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)
            : set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);
    }

    public ItemBuilder skullOf(final @Nullable String name) { //хз что вернет)
        return skullOf(name == null ? null : Bukkit.getOfflinePlayer(name));
    }

    public ItemBuilder skullOf(final @Nullable UUID id) {
        return skullOf(id == null ? null : Bukkit.getOfflinePlayer(id));
    }

    public ItemBuilder skullOf(final @Nullable OfflinePlayer pl) {
        return pl == null ? reset(DataComponentTypes.PROFILE) : set(DataComponentTypes.PROFILE,
            ResolvableProfile.resolvableProfile(pl.getPlayerProfile()));
    }

    public ItemBuilder cooldown(final int ticks, final Key group) {
        return set(DataComponentTypes.USE_COOLDOWN, UseCooldown
            .useCooldown(ticks * 0.05f).cooldownGroup(group).build());
    }

    public ItemBuilder remainder(final ItemStack rem) {
        return set(DataComponentTypes.USE_REMAINDER, UseRemainder.useRemainder(rem));
    }

    public ItemBuilder edible(final int ticks, final ItemUseAnimation iua, final Sound snd, final FoodProperties fps) {
        set(DataComponentTypes.FOOD, fps);
        final Consumable cns = get(DataComponentTypes.CONSUMABLE);
        final Consumable.Builder cnb = Consumable.consumable();
        if (cns != null) cnb.addEffects(cns.consumeEffects());
        cnb.consumeSeconds(ticks * 0.05f);
        cnb.hasConsumeParticles(true);
        cnb.animation(iua);
        cnb.sound(Ostrov.registries.SOUNDS.getKey(snd));
        return set(DataComponentTypes.CONSUMABLE, cnb.build());
    }

    public ItemBuilder edible(final int ticks, final ItemUseAnimation iua, final Sound snd) {
        final Consumable cns = get(DataComponentTypes.CONSUMABLE);
        final Consumable.Builder cnb = Consumable.consumable();
        if (cns != null) cnb.addEffects(cns.consumeEffects());
        cnb.consumeSeconds(ticks * 0.05f);
        cnb.hasConsumeParticles(false);
        cnb.animation(iua);
        cnb.sound(Ostrov.registries.SOUNDS.getKey(snd));
        return set(DataComponentTypes.CONSUMABLE, cnb.build());
    }

    public ItemBuilder eatEffect(final ConsumeEffect effect) {
        final Consumable cns = get(DataComponentTypes.CONSUMABLE);
        final Consumable.Builder cnb = Consumable.consumable();
        if (cns != null) {
            cnb.addEffects(cns.consumeEffects());
            cnb.consumeSeconds(cns.consumeSeconds());
            cnb.hasConsumeParticles(cns.hasConsumeParticles());
            cnb.animation(cns.animation());
            cnb.sound(cns.sound());
        }
        cnb.addEffect(effect);
        return set(DataComponentTypes.CONSUMABLE, cnb.build());
    }

    public ItemBuilder resist(final TagKey<DamageType> types) {
        return set(DataComponentTypes.DAMAGE_RESISTANT, DamageResistant.damageResistant(types));
    }

    public ItemBuilder enchantable(final int lvl) {
        return set(DataComponentTypes.ENCHANTABLE, Enchantable.enchantable(lvl));
    }

    public ItemBuilder equippable(final Equippable eq) {
        return set(DataComponentTypes.EQUIPPABLE, eq);
    }

    public ItemBuilder repairable(final List<ItemType> reps) {
        return set(DataComponentTypes.REPAIRABLE, Repairable.repairable(OStrap.regSetOf(RegistryKey.ITEM, reps)));
    }

    public ItemBuilder tool(final int dmg, final float speed) {
        return set(DataComponentTypes.TOOL, Tool.tool()
            .damagePerBlock(dmg).defaultMiningSpeed(speed).build());
    }

    public ItemBuilder toolRule(final Tool.Rule rl) {
        final Tool tl = get(DataComponentTypes.TOOL);
        final Tool.Builder tb = Tool.tool();
        if (tl != null) {
            tb.addRules(tl.rules());
            tb.damagePerBlock(tl.damagePerBlock());
            tb.defaultMiningSpeed(tl.defaultMiningSpeed());
        }
        tb.addRule(rl);
        return set(DataComponentTypes.TOOL, tb.build());
    }

    public ItemBuilder rarity(final ItemRarity ir) {
        return set(DataComponentTypes.RARITY, ir);
    }

    public ItemBuilder model(final Key path) {
        return set(DataComponentTypes.ITEM_MODEL, path);
    }

    public ItemBuilder headTexture(final Texture texture) {
        return headTexture(texture.value);
    }

    public ItemBuilder headTexture(final @Nullable String texture) {
        return texture == null ? reset(DataComponentTypes.PROFILE) : set(DataComponentTypes.PROFILE,
            ResolvableProfile.resolvableProfile(ItemUtil.getProfile(texture)));
    }

    public ItemBuilder color(final @Nullable Color color) {
        final PotionContents pots = get(DataComponentTypes.POTION_CONTENTS);
        if (pots != null) {
            final PotionContents.Builder pcb = PotionContents.potionContents();
            pcb.customColor(color);
            pcb.potion(pots.potion());
            pcb.customName(pots.customName());
            pcb.addCustomEffects(pots.customEffects());
            set(DataComponentTypes.POTION_CONTENTS, pcb.build());
        }
        if (color == null) return reset(DataComponentTypes.DYED_COLOR);
        return set(DataComponentTypes.DYED_COLOR,
            DyedItemColor.dyedItemColor(color, !isOn(ItemFlag.HIDE_DYE)));
    }

    public ItemBuilder basePotion(final @Nullable PotionType pot) {
        final PotionContents pots = get(DataComponentTypes.POTION_CONTENTS);
        final PotionContents.Builder pcb = PotionContents.potionContents();
        if (pots == null) {
            if (pot == null) return this;
            final DyedItemColor clr = get(DataComponentTypes.DYED_COLOR);
            if (clr != null) pcb.customColor(clr.color());
        } else {
            pcb.customName(pots.customName());
            pcb.customColor(pots.customColor());
            pcb.addCustomEffects(pots.customEffects());
        }
        pcb.potion(pot);
        return set(DataComponentTypes.POTION_CONTENTS, pcb.build());
    }

    public ItemBuilder potEffect(final PotionEffect effect) {
        final PotionContents pots = get(DataComponentTypes.POTION_CONTENTS);
        final PotionContents.Builder pcb = PotionContents.potionContents();
        if (pots == null) {
            final DyedItemColor clr = get(DataComponentTypes.DYED_COLOR);
            if (clr != null) pcb.customColor(clr.color());
        } else {
            pcb.potion(pots.potion());
            pcb.customName(pots.customName());
            pcb.customColor(pots.customColor());
            pcb.addCustomEffects(pots.customEffects());
        }
        pcb.addCustomEffect(effect);
        return set(DataComponentTypes.POTION_CONTENTS, pcb.build());
    }

    public ItemBuilder fireFlight(final int dst) {
        final Fireworks fws = get(DataComponentTypes.FIREWORKS);
        final Fireworks.Builder fbd = Fireworks.fireworks();
        if (fws != null) fbd.addEffects(fws.effects());
        fbd.flightDuration(dst);
        return set(DataComponentTypes.FIREWORKS, fbd.build());
    }

    public ItemBuilder fireEffect(final FireworkEffect fef) {
        final Fireworks fws = get(DataComponentTypes.FIREWORKS);
        final Fireworks.Builder fbd = Fireworks.fireworks();
        if (fws != null) fbd.addEffects(fws.effects())
            .flightDuration(fws.flightDuration());
        return set(DataComponentTypes.FIREWORKS, fbd.addEffect(fef).build());
    }

    public ItemStack build() {
        if (amount < 1) {
            return ItemUtil.air.clone();
        }

        final ItemStack item = type.createItemStack(amount);
        if (data != null) data.addTo(item);

        if (pdcs != null) {
            final ItemMeta im = item.getItemMeta();
            final PersistentDataContainer pdc = im.getPersistentDataContainer();
            for (final Map.Entry<String, Serializable> en : pdcs.entrySet()) {
                switch (en.getValue()) {
                    case final Byte d -> pdc.set(OStrap.key(en.getKey()), PersistentDataType.BYTE, d);
                    case final Long d -> pdc.set(OStrap.key(en.getKey()), PersistentDataType.LONG, d);
                    case final Integer d -> pdc.set(OStrap.key(en.getKey()), PersistentDataType.INTEGER, d);
                    case final Float d -> pdc.set(OStrap.key(en.getKey()), PersistentDataType.FLOAT, d);
                    case final Double d -> pdc.set(OStrap.key(en.getKey()), PersistentDataType.DOUBLE, d);
                    case final byte[] d -> pdc.set(OStrap.key(en.getKey()), PersistentDataType.BYTE_ARRAY, d);
                    case final int[] d -> pdc.set(OStrap.key(en.getKey()), PersistentDataType.INTEGER_ARRAY, d);
                    case final String d -> pdc.set(OStrap.key(en.getKey()), PersistentDataType.STRING, d);
                    default -> pdc.set(OStrap.key(en.getKey()), PersistentDataType.STRING, en.getValue().toString());
                }
            }
            item.setItemMeta(im);
        }
        return item;
    }
}