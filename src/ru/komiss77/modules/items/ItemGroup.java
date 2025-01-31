package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.util.*;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.Cfg;
import ru.komiss77.OConfig;
import ru.komiss77.boot.OStrap;
import ru.komiss77.objects.Onection;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.version.Nms;

public abstract class ItemGroup implements Keyed {

    public static final NamespacedKey KEY = OStrap.key("mat");

    private static final Map<String, ItemGroup> VALUES = new HashMap<>();
    private static final String CON_NAME = "items.yml";

    public static boolean exist = false;

    private final Map<ItemType, ItemStack> mits;
    private final NamespacedKey key;

    protected ItemGroup(final ItemStack... its) {
        this.key = OStrap.key(this.getClass().getSimpleName());
        this.mits = new HashMap<>(); before();
        final OConfig irc = Cfg.manager.config(CON_NAME, true);
        if (irc.load()) {
            final Collection<String> itls = irc.getStringList(key().value());
            if (!itls.isEmpty()) {
                for (final String is : itls) {
                    final ItemStack it = ItemUtil.parse(is);
                    mits.put(it.getType().asItemType(), it);
                }

                VALUES.put(key.value(), this);
                exist = true;
                return;
            }
        }
        for (final ItemStack it : its) {
            if (ItemUtil.isBlank(it, false)) continue;
            final PDC.Data pdc = new PDC.Data();
            pdc.add(KEY, key().value());
            Nms.setCustomData(it, pdc);
            final List<Data<?>> datas = data();
            if (datas != null) for (final Data<?> p : datas) p.merge(it);
            mits.put(it.getType().asItemType(), it);
        }
        irc.set(key().value(), Arrays.stream(its).map(it -> ItemUtil.write(it)).toList());
        irc.saveConfig();

        VALUES.put(key.value(), this);
        exist = true;
    }

    protected abstract void before();

    protected abstract @Nullable List<Data<?>> data();

    public @Nullable ItemStack item(final ItemType mt) {
        return mits.get(mt);
    }

    public Collection<ItemStack> items() {
        return mits.values();
    }

    protected abstract void onAttack(final EquipmentSlot[] es, final EntityDamageByEntityEvent e);

    protected abstract void onDefense(final EquipmentSlot[] es, final EntityDamageEvent e);

    protected abstract void onShoot(final EquipmentSlot[] es, final ProjectileLaunchEvent e);

    protected abstract void onInteract(final EquipmentSlot[] es, final PlayerInteractEvent e);

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        return o instanceof ItemGroup
            && Objects.equals(((ItemGroup) o).key.value(), key.value());
    }

    @Override
    public int hashCode() {
        return key.value().hashCode();
    }

    public record Data<D>(DataComponentType.Valued<D> vld, Onection<D> on) {
        private void merge(final ItemStack it) {
            final D val = it.getData(vld);
            if (val != null) {
                it.setData(vld, on.apply(val));
                return;
            }
            final D def = it.getType().asItemType().getDefaultData(vld);
            if (def == null) return;
            it.setData(vld, on.apply(def));
        }
    }

    public static @Nullable ItemGroup get(final ItemStack it) {
        final String id = it.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        return id == null ? null : get(id);
    }

    public static <E extends ItemGroup> @Nullable E get(final ItemStack it, final Class<E> cls) throws ClassCastException {
        final ItemGroup q = get(it);
        return q == null ? null : cls.cast(q);
    }

    public static @Nullable ItemGroup get(final String id) {
        return VALUES.get(id);
    }

    public static <E extends ItemGroup> @Nullable E get(final String id, final Class<E> cls) throws ClassCastException {
        final ItemGroup q = get(id);
        return q == null ? null : cls.cast(q);
    }

    public static Collection<ItemGroup> values() {
        return VALUES.values();
    }

    public static void process(final Entity ent, final ItemManager.GroupProc prc) {
        ItemManager.process(ent, new ItemManager.Processor() {
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {}
            public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {
                prc.onGroup(ess, cm);
            }
        });
    }
}
