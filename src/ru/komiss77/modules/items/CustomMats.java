package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.util.*;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.Cfg;
import ru.komiss77.OConfig;
import ru.komiss77.OStrap;
import ru.komiss77.utils.ItemUtil;

public abstract class CustomMats implements Keyed {

    public static final NamespacedKey KEY = OStrap.key("mat");
    public static final Map<String, CustomMats> VALUES = new HashMap<>();

    public static boolean exist = false;

    private static final String CON_NAME = "items.yml";

    private final Map<ItemType, ItemStack> mits;
    private final @Nullable ItemData shared;
    private final NamespacedKey key;

    protected CustomMats(final @Nullable ItemData data, final ItemStack... its) {
        this.shared = data;
        this.mits = new HashMap<>();
        this.key = OStrap.key(this.getClass().getSimpleName());
        final OConfig irc = Cfg.manager.getNewConfig(CON_NAME);
        final Collection<String> itls = irc.getStringList(key().value());
        if (itls.isEmpty()) {
            for (final ItemStack it : its) {
                if (ItemUtil.isBlank(it, false)) continue;
                mits.put(it.getType().asItemType(), it);
            }
            irc.set(key().value(), Arrays.stream(its).map(it -> ItemUtil.write(it)).toList());
            irc.saveConfig();
        } else {
            for (final String is : itls) {
                final ItemStack it = ItemUtil.parse(is);
                mits.put(it.getType().asItemType(), it);
            }
        }

        VALUES.put(key.value(), this);
        exist = true;
    }

    public static CustomMats[] values() {
        return VALUES.values().toArray(new CustomMats[0]);
    }

    public @Nullable ItemStack item(final ItemType mt) {
        final ItemStack it = mits.get(mt);
        if (it == null) return null;
        it.editMeta(im -> im.getPersistentDataContainer()
            .set(KEY, PersistentDataType.STRING, KEY.value()));
        return shared == null ? it : shared.addTo(it);
    }

    public Collection<ItemStack> allIts() {
        return mits.values();
    }

    public static CustomMats get(final ItemStack it) {
        final String id = it.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        return id == null ? null : get(id);
    }

    public static CustomMats get(final String id) {
        return VALUES.get(id);
    }

    protected abstract void onAttack(final EquipmentSlot[] es, final EntityDamageByEntityEvent e);

    protected abstract void onDefense(final EquipmentSlot[] es, final EntityDamageEvent e);

    protected abstract void onShoot(final EquipmentSlot[] es, final ProjectileLaunchEvent e);

    protected abstract void onInteract(final EquipmentSlot[] es, final PlayerInteractEvent e);

    protected abstract void onBreak(final EquipmentSlot[] es, final BlockBreakEvent e);

    protected abstract void onPlace(final EquipmentSlot[] es, final BlockPlaceEvent e);

    protected abstract void onExtra(final EquipmentSlot[] es, final PlayerEvent e);

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        return o instanceof CustomMats
            && Objects.equals(((CustomMats) o).key, key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
