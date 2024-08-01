package ru.komiss77.modules.items;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ItemManager implements Initiable, Listener {

    public static boolean enable;

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        ItemRoll.loadAll();
        HandlerList.unregisterAll(this);
        if (!enable) return;

        Ostrov.log_ok("§2Предметы включены!");
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
    }

    @Override
    public void onDisable() {
        if (!enable) return;

        Ostrov.log_ok("§6Предметы выключены!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
        process(e.getEntity(), (es, cm) -> cm.onDefense(es, e));
        if (e instanceof final EntityDamageByEntityEvent ee) {
            process(e.getDamageSource().getCausingEntity(), (es, cm) -> cm.onAttack(es, ee));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onInteract(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onBreak(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onPlace(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(final ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof final LivingEntity le) {
            process(le, (es, cm) -> cm.onShoot(es, e));
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerJumpEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onExtra(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerToggleFlightEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onExtra(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerPickupExperienceEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onExtra(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerChangedMainHandEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onExtra(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerStopUsingItemEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onExtra(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerRiptideEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onExtra(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerItemConsumeEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onExtra(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerElytraBoostEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onExtra(es, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(final PlayerInteractEntityEvent e) {
        process(e.getPlayer(), (es, cm) -> cm.onExtra(es, e));
    }


    private static void process(final Entity ent, final BiConsumer<EquipmentSlot[], CustomMats> ec) {
        if (ent instanceof final LivingEntity le) {
            final HashMap<CustomMats, List<EquipmentSlot>> cmp = new HashMap<>();
            final EntityEquipment eq = le.getEquipment();
            if (eq != null) {
                for (final EquipmentSlot es : EquipmentSlot.values()) {
                    final ItemStack is = eq.getItem(es);
                    if (ItemUtils.isBlank(is, true)) continue;
                    final CustomMats cm = CustomMats.getCstmItm(is.getItemMeta());
                    if (cm == null) continue;
                    final List<EquipmentSlot> ess = cmp.get(cm);
                    if (ess == null) {
                        final List<EquipmentSlot> nes = new ArrayList<>();
                        nes.add(es);
                        cmp.put(cm, nes);
                    } else {
                        ess.add(es);
                    }
                }
            }
            for (final Map.Entry<CustomMats, List<EquipmentSlot>> en : cmp.entrySet()) {
                ec.accept(en.getValue().toArray(new EquipmentSlot[0]), en.getKey());
            }
        }
    }
}
