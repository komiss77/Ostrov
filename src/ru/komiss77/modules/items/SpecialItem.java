package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
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
import ru.komiss77.Ostrov;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.utils.TCUtil;

public abstract class SpecialItem implements Keyed {

  private static final ComponentLogger LOGGER = ComponentLogger.logger("OS-SI");
  private final String name;
  private final ItemStack item;
  private final NamespacedKey key;
  public ItemManager.State state = ItemManager.State.NOT_EXIST;
  private WeakReference<Entity> own; //текущий владелец (только PLAYER или  ITEM)
  protected @Nullable BVec lastLoc; //последняя известная локация (только для AS_ITEM, UNLOADED)

  //создаётся в другом плагине
  //public final AreaPick AREA_PICK = new AreaPick(new ItemBuilder(ItemType.DIAMOND_PICKAXE).glint(true).unbreak(true)
  //        .name("<stale>Кирка Троицы").lore("").lore(TCUtil.N + "Копает блоки площадью в 3x3").build());
  public SpecialItem(final ItemStack def) {
      if (Cfg.items == false) {
        Ostrov.log_warn("SpecialItem : добавляется предмет, но ItemManager выключен! (modules.items=false)");
      }
    name = this.getClass().getSimpleName().toLowerCase();
    key = OStrap.key(name);
    own = new WeakReference<>(null);
    item = ItemManager.load(this, def);
    }


  //прицепить к Item в мире. Условия:
  //WorldsLoadComplete+LOOCKUP
  //EntitiesLoadEvent+UNLOADED
  //onDrop,onLogout+PLAYER_HAS
  public void attachToItem(final Item i, final String cause) {
    //crafted = true;
    //dropped = true;
    state = ItemManager.State.AS_ITEM;
    i.setGlowing(true);
    i.setWillAge(false);
    i.setGravity(false);
    i.setPickupDelay(20);
    i.setCanMobPickup(false);
    i.setVelocity(new Vector());
    own = new WeakReference<>(i);
    lastLoc = BVec.of(i.getLocation());//loc(i.getLocation());
    save(i.getItemStack());
    info("ATTACH cause=" + cause);
    //return it;
  }

  //скрафтил, поднял или выдал командой
  public void obtain(final LivingEntity le, final ItemStack it) {
    //crafted = true;
    //dropped = false;
    state = ItemManager.State.PLAYER_HAS;
    own = new WeakReference<>(le);
    lastLoc = null;//loc(le.getLocation());
    save(it);
    info("OBTAIN");
  }

  //EntitiesUnloadEvent
  public void unload(final Item unloadedItem) {
    state = ItemManager.State.UNLOADED;
    unloadedItem.setVelocity(new Vector());
    lastLoc = BVec.of(unloadedItem.getLocation());//loc(unloadedItem.getLocation());
    own = new WeakReference<>(null);
    save(unloadedItem.getItemStack());
    info("EntitiesUnload: Unloaded");
  }

  //EntityRemoveEvent ItemDespawnEvent EntityDamageEvent PlayerItemBreakEvent
  public void destroy(final boolean vipeOwnerInv, final String cause) {
      //if (!crafted) return;
    //dropped = false;
    //crafted = false;
    state = ItemManager.State.NOT_EXIST;
    if (vipeOwnerInv) {
      final Entity owner = own.get();
      //чекать item не надо, удалится само, setHealth(0) или remove() порождают deadlock
      if (owner != null && owner instanceof Player p) {
        for (final ItemStack it : p.getInventory()) {
          if (it != null && this.equals(get(it))) {
            it.setAmount(0);
          }
        }
      }
    }
    lastLoc = null;
    own = new WeakReference<>(null);
    save(item);
    info("DESTROY cause=" + cause);
      /*switch (owner) {
            case null: break;
            case final Item le:
              //deadlock!!! удалится само
              //if (!le.isDead()) le.remove();
              //le.setHealth(0); //так сделает discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.PLUGIN)
                break;
            case final Player le:
                for (final ItemStack it : le.getInventory()) {
                    if (it != null && this.equals(get(it))) {
                        it.setAmount(0);
                    }
                }
                break;
            default:
        }*/

  }

  @OverrideMe
  public static @Nullable SpecialItem get(final ItemStack it) {
    if (it == null || ItemManager.RELICS.isEmpty()) return null;
    final String name = it.getPersistentDataContainer().get(ItemManager.DATA, PersistentDataType.STRING);
    return name == null ? null : ItemManager.RELICS.get(name);
  }

  @OverrideMe
  public static @Nullable SpecialItem get(final Item own) {
    if (own == null || ItemManager.RELICS.isEmpty()) return null;
    for (final SpecialItem si : ItemManager.RELICS.values()) {
      final Entity ent = si.own.get();
      if (ent != null && ent.getEntityId() == own.getEntityId())
        return si;
    }
    return null;
  }


  public String name() {
    return name;
  }

  public @Nullable BVec loc() {
    /*return switch (own.get()) {
      case null -> lastLoc;
      case final Entity e -> {
        loc(EntityUtil.center(e));
        yield lastLoc;
      }
    };*/
    return lastLoc;
  }

  //public void loc(final Location lc) {
  //  lastLoc = BVec.of(lc);
  //}

  //public boolean crafted() {
  //    return crafted;
  //}

  //public boolean dropped() {
  //    return dropped;
  //}

  public ItemStack item() {
    return item;
  }

  public Entity own() {
    return own.get();
  }

  private void save(final ItemStack curr) {
    ItemManager.save(this, curr);
  }

  public void info(final String msg) {
    LOGGER.info(TCUtil.form("SpecialItem " + msg + " " + name + ": state=" + state + " loc=" + loc()));
  }

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


  protected abstract void onAttack(final EquipmentSlot es, final EntityDamageByEntityEvent e);

  protected abstract void onDefense(final EquipmentSlot es, final EntityDamageEvent e);

  protected abstract void onShoot(final EquipmentSlot es, final ProjectileLaunchEvent e);

  protected abstract void onInteract(final EquipmentSlot es, final PlayerInteractEvent e);


}
  /*  @OverrideMe
    public static Set<SpecialItem> getAll(final LivingEntity own) {
        final Set<SpecialItem> sis = new HashSet<>();
      if (own == null || ItemManager.RELICTS.isEmpty()) return null;
        for (final SpecialItem si : ItemManager.RELICTS.values()) {
            final Entity ent = si.own.get();
            if (ent != null && ent.getEntityId() == own.getEntityId())
                sis.add(si);
        }
        return sis;
    }*/

//при выходе игрока - если в привате то в центр привата
//при загрузке specials.yml если указана координата
//при EntitiesLoadEvent
//при бросании onDrop
   /* public void spawn(final Location loc, final ItemStack it, final String cause) {
        loc.getWorld().getChunkAtAsync(loc).thenAccept(ch -> {
            for (final Entity e : ch.getEntities()) {
                if (e instanceof final Item ie && this.equals(get(ie.getItemStack()))) {
                  e.remove();
                }
            }
          loc.getWorld().dropItem(loc, it, this::apply);
          info("SPAWN cause="+cause);
        });
    }*/