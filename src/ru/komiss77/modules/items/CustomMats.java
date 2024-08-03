package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.util.*;
import org.bukkit.Keyed;
import org.bukkit.Material;
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
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.OConfig;


public abstract class CustomMats implements Keyed {

    private static final String CMD = "cmd";
    private static final String ITS = "its";
    private static final String SEP = "=";
    private static final String CON_NAME = "items.yml";
    private static final HashMap<Integer, CustomMats> VALUES = new HashMap<>();

    public final Integer cmd;
    private final EnumMap<Material, ItemStack> mits = new EnumMap<>(Material.class);

    protected final NamespacedKey key;

    protected CustomMats(final Integer cmd, final ItemStack... its) {
        this.cmd = cmd;
        this.key = new NamespacedKey(Ostrov.instance, this.getClass().getSimpleName());
        final OConfig irc = Cfg.manager.getNewConfig(CON_NAME);
        final Collection<String> itls = irc.getStringList(key().value());
        if (itls.isEmpty()) {
            for (final ItemStack it : its) {
                if (ItemUtil.isBlank(it, false)) continue;
                mits.put(it.getType(), it);
            }
            irc.set(key().value(), Arrays.stream(its).map(it -> ItemUtil.toString(it, SEP)).toList());
            irc.saveConfig();
        } else {
            for (final String is : itls) {
                final ItemStack it = ItemUtil.parseItem(is, SEP);
                mits.put(it.getType(), it);
            }
        }

        VALUES.put(cmd, this);
    }

    public static CustomMats[] values() {
        return VALUES.values().toArray(new CustomMats[0]);
    }

    public @Nullable ItemStack getItem(final Material mt) {
        return mits.get(mt);
    }

    public static CustomMats getCstmItm(final ItemStack it) {
        return it.hasItemMeta() ? getCstmItm(it.getItemMeta()) : null;
    }

    public static CustomMats getCstmItm(final ItemMeta im) {
        return im.hasCustomModelData() ? getCstmItm(im.getCustomModelData()) : null;
    }

    public static CustomMats getCstmItm(final Integer cmd) {
        return VALUES.get(cmd);
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
        return o instanceof CustomMats
            && Objects.equals(((CustomMats) o).cmd, cmd);
    }

    @Override
    public int hashCode() {
        return cmd == null ? 0 : cmd;
    }

    protected static void load() {
        VALUES.clear();
    }
}
