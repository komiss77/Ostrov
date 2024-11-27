package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import ru.komiss77.OStrap;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.ItemUtil.Texture;
import ru.komiss77.utils.TCUtil;

public class ItemBuilder {

    private ItemType type;//private Material mat;
    private int amount = 1; //по умолчанию 1, или build() выдаёт AIR!
    private Map<DataComponentType, Data> data = null;
    private CaseInsensitiveMap<Serializable> pdcs = null;
    private EnumSet<ItemFlag> flags = null;

    private interface Data {
        @Nullable <D> D get(final Class<D> cls);
        void apply(final ItemStack it);
        @SuppressWarnings("unchecked")
        static <D> Data of(final ItemStack it, final DataComponentType type) {
            return switch (type) {
                case final DataComponentType.NonValued cd -> new CheapData(cd);
                case final DataComponentType.Valued<?> ignored -> {
                    final DataComponentType.Valued<D> vd = (DataComponentType.Valued<D>) type;
                    final D data = it.getData(vd);
                    yield new ValuedData<>(vd, data);
                }
                default -> throw new IllegalArgumentException("'type' cannot be null!");
            };
        }
    }

    private record ValuedData<T>(DataComponentType.Valued<T> type, T val) implements Data {
        public void apply(final ItemStack it) {it.setData(type, val);}
        public @Nullable <D> D get(final Class<D> cls) {return cls.isAssignableFrom(val.getClass()) ? cls.cast(val) : null;}
    }

    private record CheapData(DataComponentType.NonValued type) implements Data {
        public void apply(final ItemStack it) {it.setData(type);}
        public @Nullable <D> D get(final Class<D> cls) {return null;}
    }

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
        final Set<DataComponentType> datas = from.getDataTypes();
        if (datas.isEmpty()) return;
        checkData();
        for (final DataComponentType dtc : datas) {
            data.put(dtc, Data.of(from, dtc));
        }
    }

    private void checkData() {
        if (data == null) data = new HashMap<>();
    }

    private @Nullable <D> D get(final DataComponentType type, final Class<D> cls) {
        if (data == null) return null;
        final Data lrd = data.get(type);
        if (lrd == null) return null;
        return lrd.get(cls);
    }

    /*private boolean has(final DataComponentType type) {
        return data != null && data.containsKey(type);
    }*/

    public <D> ItemBuilder set(final DataComponentType.Valued<D> type, final D val) {
        checkData(); data.put(type, new ValuedData<>(type, val));
        return this;
    }

    public ItemBuilder set(final DataComponentType.NonValued type) {
        checkData(); data.put(type, new CheapData(type));
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
        final ItemLore own = get(DataComponentTypes.LORE, ItemLore.class);
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
                case HIDE_ENCHANTS -> get(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.class);
                case HIDE_ATTRIBUTES -> get(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.class);
                case HIDE_UNBREAKABLE -> get(DataComponentTypes.UNBREAKABLE, Unbreakable.class);
                case HIDE_DESTROYS -> get(DataComponentTypes.CAN_BREAK, ItemAdventurePredicate.class);
                case HIDE_PLACED_ON -> get(DataComponentTypes.CAN_PLACE_ON, ItemAdventurePredicate.class);
                case HIDE_DYE -> get(DataComponentTypes.DYED_COLOR, DyedItemColor.class);
                case HIDE_ARMOR_TRIM -> get(DataComponentTypes.TRIM, ItemArmorTrim.class);
                case HIDE_STORED_ENCHANTS -> get(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.class);
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
        final ItemEnchantments ies = get(type, ItemEnchantments.class);
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

    public ItemBuilder unbreak(final boolean unbreakable) {
        return unbreakable ? set(DataComponentTypes.UNBREAKABLE, Unbreakable
            .unbreakable(!isOn(ItemFlag.HIDE_UNBREAKABLE)))
            : reset(DataComponentTypes.UNBREAKABLE);
    }

    @Deprecated
    public ItemBuilder attribute(final Attribute att, final double amount, final Operation op) {
        attribute(att, amount, op, type.asMaterial().getEquipmentSlot().getGroup());
        return this;
    }

    public ItemBuilder attribute(final Attribute att, final double amount, final Operation op, final EquipmentSlotGroup slotGroup) {
        final ItemAttributeModifiers iams = get(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.class);
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
        final ItemAttributeModifiers iams = get(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.class);
        if (iams == null) return this;
        final ItemAttributeModifiers.Builder iamb = ItemAttributeModifiers.itemAttributes();
        for (final ItemAttributeModifiers.Entry en : iams.modifiers()) {
            if (att.equals(en.attribute())) continue;
            iamb.addModifier(en.attribute(), en.modifier(), en.getGroup());
        }
        return set(DataComponentTypes.ATTRIBUTE_MODIFIERS, iamb.build());
    }

    public ItemBuilder modelData(final int data) {
        return set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData(data));
    }

    //вроде очень редко нужно, думаю не страшно будет ставить всегда одним методом через float
    //хз мне нужно довольно часто
    public ItemBuilder durability(final int dur) {
        final Integer mxd = get(DataComponentTypes.MAX_DAMAGE, Integer.class);
        final int maxDmg = mxd == null ? type.getMaxDurability() : mxd;
        if (maxDmg - dur < 1) return reset(DataComponentTypes.DAMAGE);
        return set(DataComponentTypes.DAMAGE, Math.max(maxDmg - dur, 0));
    }

    public ItemBuilder durability(final float dur) {
        if (dur == 1f) return reset(DataComponentTypes.DAMAGE);
        final Integer mxd = get(DataComponentTypes.MAX_DAMAGE, Integer.class);
        final int maxDmg = mxd == null ? type.getMaxDurability() : mxd;
        return set(DataComponentTypes.DAMAGE, (int) (maxDmg * (1f - Math.clamp(dur, 0f, 1f))));
    }

    public ItemBuilder maxDamage(final int dur) {
        final Integer dmg = get(DataComponentTypes.DAMAGE, Integer.class);
        final int damage = dmg == null ? 0 : dmg;
        final Integer mxd = get(DataComponentTypes.MAX_DAMAGE, Integer.class);
        final int maxDmg = mxd == null ? type.getMaxDurability() : mxd;
        set(DataComponentTypes.MAX_DAMAGE, dur);
        final float rel = (float) damage / (float) maxDmg;
        return set(DataComponentTypes.DAMAGE, (int) (rel * dur));
    }

    //взял глинт закомментил (((  наверное, случайно. сорян
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

    public ItemBuilder headTexture(final Texture texture) {
        return headTexture(texture.value);
    }

    public ItemBuilder headTexture(final @Nullable String texture) {
        return texture == null ? reset(DataComponentTypes.PROFILE) : set(DataComponentTypes.PROFILE,
            ResolvableProfile.resolvableProfile(ItemUtil.getProfile(texture)));
    }

    public ItemBuilder color(final @Nullable Color color) {
        final PotionContents pots = get(DataComponentTypes.POTION_CONTENTS, PotionContents.class);
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
        final PotionContents pots = get(DataComponentTypes.POTION_CONTENTS, PotionContents.class);
        final PotionContents.Builder pcb = PotionContents.potionContents();
        pcb.potion(pot);
        if (pots == null) {
            if (pot == null) return this;
            final DyedItemColor clr = get(DataComponentTypes.DYED_COLOR, DyedItemColor.class);
            if (clr != null) pcb.customColor(clr.color());
        } else {
            pcb.customName(pots.customName());
            pcb.customColor(pots.customColor());
            pcb.addCustomEffects(pots.customEffects());
        }
        return set(DataComponentTypes.POTION_CONTENTS, pcb.build());
    }

    public ItemBuilder addEffect(final PotionEffect effect) {
        final PotionContents pots = get(DataComponentTypes.POTION_CONTENTS, PotionContents.class);
        final PotionContents.Builder pcb = PotionContents.potionContents();
        if (pots == null) {
            final DyedItemColor clr = get(DataComponentTypes.DYED_COLOR, DyedItemColor.class);
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

    public ItemStack build() {
        if (amount < 1) {
            return ItemUtil.air.clone();
        }

        final ItemStack item = type.createItemStack(amount);
        if (data != null) {
            for (final Data dt : data.values()) dt.apply(item);
        }

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
