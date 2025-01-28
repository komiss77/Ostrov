package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.Cfg;
import ru.komiss77.OConfig;
import ru.komiss77.Ostrov;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemUtil;

public abstract class SpecialItem implements Keyed {

    public static final CaseInsensitiveMap<SpecialItem> VALUES = new CaseInsensitiveMap<>();

    private static final String CON_NAME = "specials.yml";
    private static final NamespacedKey DATA = OStrap.key("special");
    private static final XYZ DEF_SPAWN = new WXYZ(Bukkit.getWorlds().getFirst(), 0, 100, 0);

    public static boolean exist = false;

    public static final XYZ SPAWN = getSpawnLoc();

    private static XYZ getSpawnLoc() {
        final OConfig irc = Cfg.manager.config(CON_NAME, true);
        if (irc.contains("spawn")) {
            final XYZ spawn = XYZ.fromString(irc.getString("spawn"));
            if (spawn != null) return spawn;
        }
        irc.set("spawn", DEF_SPAWN.toString());
        irc.saveConfig();
        return DEF_SPAWN;
    }

    private final String name;
    private final ItemStack item;
    private final NamespacedKey key;

    private WeakReference<Entity> own;
    private boolean crafted;
    private boolean dropped;
    private @Nullable WXYZ lastLoc;

    public SpecialItem(final ItemStack it) {
        this.name = this.getClass().getSimpleName();
        this.key = OStrap.key(name);

        own = new WeakReference<>(null);
        final OConfig irc = Cfg.manager.config(CON_NAME, true);
        crafted = irc.getBoolean(name + ".crafted", false);
        dropped = irc.getBoolean(name + ".dropped", false);
        final XYZ loc = XYZ.fromString(irc.getString(name + ".loc"));
        this.item = irc.load() ? ItemUtil.parse(irc.getString(name + ".org")) : it;
        final ItemStack curr = ItemUtil.parse(irc.getString(name + ".curr"));
        lastLoc = loc == null ? null : new WXYZ(loc);
        if (lastLoc != null) spawn(lastLoc.getCenterLoc(), curr);

        VALUES.put(name, this);
        exist = true;
    }

    public String name() {
        return name;
    }

    public boolean crafted() {
        return crafted;
    }

    public boolean dropped() {
        return dropped;
    }

    public ItemStack item() {
        return item;
    }

    public @Nullable WXYZ loc() {
        return switch (own.get()) {
            case null -> lastLoc;
            case final Entity le -> {
                loc(le.getLocation());
                yield lastLoc;
            }
        };
    }

    public void loc(final Location lc) {
        lastLoc = new WXYZ(lc);
    }

    protected void destroy() {
        if (crafted) {
            dropped = false;
            crafted = false;
            switch (own.get()) {
                case null: break;
                case final Item le:
                    le.remove();
                    break;
                case final Player le:
                    for (final ItemStack it : le.getInventory()) {
                        if (it != null && this.equals(get(it))) {
                            it.setAmount(0);
                        }
                    }
                    break;
                default:
            }
            lastLoc = null;
            own = new WeakReference<>(null);
            save(item);
        }
    }

    public void spawn(final Location loc, final ItemStack it) {
        if (!loc.isChunkLoaded()) {
            loc.getWorld().getChunkAtAsync(loc).thenAccept(ch -> {
                for (final Entity e : ch.getEntities()) {
                    if (e instanceof final Item ie && this.equals(get(ie.getItemStack()))) {
                        e.remove();
                    }
                }

                loc.getWorld().dropItem(loc, it, this::apply);
            });
        }
    }

    public Item apply(final Item it) {
        crafted = true;
        dropped = true;
        it.setGlowing(true);
        it.setWillAge(false);
        it.setGravity(false);
        it.setPickupDelay(20);
        own = new WeakReference<>(it);
        loc(it.getLocation());
        save(it.getItemStack());
        return it;
    }

    public void obtain(final LivingEntity le, final ItemStack it) {
        crafted = true;
        dropped = false;
        own = new WeakReference<>(le);
        loc(le.getLocation());
        save(it);
    }

    public void save(final ItemStack curr) {
        Ostrov.async(() -> {
            final OConfig irc = Cfg.manager.config(CON_NAME, true);
            irc.set(name + ".loc", lastLoc == null ? null : lastLoc.toString());
            irc.set(name + ".curr", curr);

            irc.set(name + ".dropped", dropped);
            irc.set(name + ".crafted", crafted);
            irc.saveConfig();
        });
    }

    public void saveAll(final ItemStack curr) {
        Ostrov.async(() -> {
            final OConfig irc = Cfg.manager.config(CON_NAME, true);
            irc.set(name + ".loc", lastLoc == null ? null : lastLoc.toString());
            irc.set(name + ".org", ItemUtil.write(item));
            irc.set(name + ".curr", curr);

            irc.set(name + ".dropped", dropped);
            irc.set(name + ".crafted", crafted);
            irc.saveConfig();
        });
    }

    protected abstract void onAttack(final EquipmentSlot es, final EntityDamageByEntityEvent e);

    protected abstract void onDefense(final EquipmentSlot es, final EntityDamageEvent e);

    protected abstract void onShoot(final EquipmentSlot es, final ProjectileLaunchEvent e);

    protected abstract void onInteract(final EquipmentSlot es, final PlayerInteractEvent e);

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof SpecialItem && name.equals(((SpecialItem) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static @Nullable SpecialItem get(final ItemStack it) {
        if (it == null) return null;
        final String nm = it.getPersistentDataContainer().get(DATA, PersistentDataType.STRING);
        return nm == null ? null : VALUES.get(nm);
    }

    public static @Nullable SpecialItem get(final Entity own) {
        for (final SpecialItem si : VALUES.values()) {
            final Entity ent = si.own.get();
            if (ent != null && ent.getEntityId() == own.getEntityId())
                return si;
        }
        return null;
    }

    public static void process(final Entity ent, final ItemManager.SpecProc prc) {
        ItemManager.process(ent, new ItemManager.Processor() {
            public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {
                prc.onSpec(es, si);
            }
        });
    }
}
