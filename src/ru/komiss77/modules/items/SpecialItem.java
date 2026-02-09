package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.*;
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
import org.bukkit.util.Vector;
import ru.komiss77.Cfg;
import ru.komiss77.OConfig;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;

public abstract class SpecialItem implements Keyed {

    public static final CaseInsensitiveMap<SpecialItem> VALUES = new CaseInsensitiveMap<>();

    private static final String CON_NAME = "specials.yml";
  public static final NamespacedKey DATA = OStrap.key("special");
    private static final BVec DEF_SPAWN = BVec.of(Bukkit.getWorlds().getFirst(), 0, 100, 0);

    public static final BVec SPAWN = getSpawnLoc();

    public static boolean exist = false;
    private static OConfig config = null;
    public static OConfig config() {
      if (config == null) {
        config = Cfg.manager.config(CON_NAME, true);
      }
        return config;
    }

    private static BVec getSpawnLoc() {
      final OConfig config = config();
      if (config.contains("spawn")) {
//Ostrov.log_warn("SpecialItem getSpawnLoc BVec.parse "+config.getString("spawn"));
        final BVec spawn = BVec.parse(config.getString("spawn"));
            if (spawn != null) return spawn;
        }
      config.set("spawn", DEF_SPAWN.toString());
      config.saveConfig();
        return DEF_SPAWN;
    }

    private final String name;
    private final ItemStack item;
    private final NamespacedKey key;

    private WeakReference<Entity> own;
    private boolean crafted;
    private boolean dropped;
    private @Nullable BVec lastLoc;

  //создаётся в другом плагине
  //public final AreaPick AREA_PICK = new AreaPick(new ItemBuilder(ItemType.DIAMOND_PICKAXE).glint(true).unbreak(true)
  //        .name("<stale>Кирка Троицы").lore("").lore(TCUtil.N + "Копает блоки площадью в 3x3").build());
    public SpecialItem(final ItemStack it) {
      if (Cfg.items == false) {
        Ostrov.log_warn("SpecialItem : добавляется предмет, но ItemManager выключен! (modules.items=false)");
      }
        this.name = this.getClass().getSimpleName().toLowerCase();
        this.key = OStrap.key(name);

        own = new WeakReference<>(null);
      final OConfig config = config();
      crafted = config.getBoolean(name + ".crafted", false);
      dropped = config.getBoolean(name + ".dropped", false);
      if (config.contains(name)) {
        this.item = config.load() ? ItemUtil.parse(config.getString(name + ".org")) : it;
        final ItemStack curr = ItemUtil.parse(config.getString(name + ".curr"));
//Ostrov.log_warn("SpecialItem new BVec.parse="+config.getString(name + ".loc"));
        if (!config.getString(name + ".loc", "").isBlank()) {//(lastLoc != null) {
          lastLoc = BVec.parse(config.getString(name + ".loc"));
                Timer.task(() -> {
                    if (lastLoc == null || own() != null) return true;
                    final World w = lastLoc.w();
                    if (w == null) return false;
                    spawn(lastLoc.center(w), curr);
                    return true;
                }, "spec", 5, 10);
            }
        } else this.item = it;

        item.editPersistentDataContainer(pdc ->
            pdc.set(DATA, PersistentDataType.STRING, name));
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

    public Entity own() {
        return own.get();
    }

    public @Nullable BVec loc() {
        return switch (own.get()) {
            case null -> lastLoc;
            case final Entity e -> {
                loc(EntityUtil.center(e));
                yield lastLoc;
            }
        };
    }

    public void loc(final Location lc) {
        lastLoc = BVec.of(lc);
    }

    protected void destroy() {
      //if (!crafted) return;
        dropped = false;
        crafted = false;
        switch (own.get()) {
            case null: break;
            case final Item le:
              //if (!le.isDead()) le.remove();
              le.setHealth(0); //так сделает discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.PLUGIN)
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
      info("DESTROY");
        save(item);
    }

  //при выходе игрока - если в привате то в центр привата
  //при загрузке specials.yml если указана координата
  //при EntitiesLoadEvent
  //при бросании onDrop
    public void spawn(final Location loc, final ItemStack it) {
        loc.getWorld().getChunkAtAsync(loc).thenAccept(ch -> {
            for (final Entity e : ch.getEntities()) {
                if (e instanceof final Item ie
                    && this.equals(get(ie.getItemStack()))) e.remove();
            }

            loc.getWorld().dropItem(loc, it, this::apply);
          info("SPAWN");
        });
    }

    public Item apply(final Item it) {
      crafted = true;
      dropped = true;
        it.setGlowing(true);
        it.setWillAge(false);
        it.setGravity(false);
        it.setPickupDelay(20);
        it.setCanMobPickup(false);
        it.setVelocity(new Vector());
        own = new WeakReference<>(it);
        loc(it.getLocation());
        save(it.getItemStack());
      info("APPLY");
        return it;
    }

  //скрафтил, поднял или выдал командой
    public void obtain(final LivingEntity le, final ItemStack it) {
        crafted = true;
        dropped = false;
        own = new WeakReference<>(le);
        loc(le.getLocation());
      info("OBTAIN");
        save(it);
    }

  //сохранить текущее состояние спец.предмета, созданного в плагине
    public void save(final ItemStack curr) {
        final OConfig irc = config();
        if (irc.getString(name + ".org").isEmpty()) {
            irc.set(name + ".org", ItemUtil.write(item));
        }
        irc.set(name + ".loc", lastLoc == null ? null : lastLoc.toString());
        irc.set(name + ".curr", ItemUtil.write(curr));

        irc.set(name + ".dropped", dropped);
        irc.set(name + ".crafted", crafted);
        irc.saveConfig();
    }

    private static final ComponentLogger LOGGER = ComponentLogger.logger("OS-SI");
    public void info(final String msg) {
      LOGGER.info(TCUtil.form("SpecialItem " + msg + " " + name + ": crafted?=" + crafted + " dropped?" + dropped + " loc=" + loc()));
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

    @OverrideMe
    public static @Nullable SpecialItem get(final ItemStack it) {
        if (it == null || !exist) return null;
        final String nm = it.getPersistentDataContainer().get(DATA, PersistentDataType.STRING);
        return nm == null ? null : VALUES.get(nm);
    }

    @OverrideMe
    public static @Nullable SpecialItem get(final Item own) {
        if (own == null || !exist) return null;
        for (final SpecialItem si : VALUES.values()) {
            final Entity ent = si.own.get();
            if (ent != null && ent.getEntityId() == own.getEntityId())
                return si;
        }
        return null;
    }

    @OverrideMe
    public static Set<SpecialItem> getAll(final LivingEntity own) {
        final Set<SpecialItem> sis = new HashSet<>();
        if (own == null || !exist) return sis;
        for (final SpecialItem si : VALUES.values()) {
            final Entity ent = si.own.get();
            if (ent != null && ent.getEntityId() == own.getEntityId())
                sis.add(si);
        }
        return sis;
    }
}
