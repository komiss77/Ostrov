package ru.komiss77.modules.protocols;

import java.util.HashSet;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Abilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.version.Nms;

public class Protocol43 implements Listener {

    public static final int SD_TICKS = 100;
    public static boolean active = false;
    private static final ItemStack sub = new ItemStack(Material.REDSTONE);
    private static final BlockData[] bds = new BlockData[]{
        Material.BEDROCK.createBlockData(),
        Material.DEEPSLATE_COAL_ORE.createBlockData(),
        Material.SMOOTH_BASALT.createBlockData(),
        Material.TUFF.createBlockData()};
    private static final String[] title =
        {"§к§kAA",
            "§к§kAAAA",
            "§к§kAAAAAA",
             "§к<obf>AAAA<!obf>§cок§к<obf>AAAA",
            "§к<obf>AAAA<!obf>§cток§к<obf>AAAA",
             "§к<obf>AAA<!obf>§cтоко§к<obf>AAA",
            "§к<obf>AAA<!obf>§cотоко§к<obf>AAA",
            "§к<obf>AA<!obf>§cротоко§к<obf>AA",
            "§к<obf>AA<!obf>§cротокол§к<obf>AA",
             "§к<obf>A<!obf>§cротокол §к<obf>A",
             "§к<obf>A<!obf>§cротокол 4§к<obf>A",
                          "§cПротокол 4§к<obf>A",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cПротокол 43",
            "§cротокол ",
            "§cотокол",
            "§cтоко",
            "§cок",
            ""};

    private final HashSet<UUID> immune;

    public Protocol43(final Player p) {
        active = true;

        immune = new HashSet<>();
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            if (!Perm.isStaff(PM.getOplayer(pl), 2)) continue;
            immune.add(pl.getUniqueId());
            ItemUtil.giveItemTo(pl, sub, 4 ,true);
            p.getInventory().setHeldItemSlot(4);
            pl.getInventory().setHeldItemSlot(4);
        }

        new BukkitRunnable() {
            private int li = 0;

            @Override
            public void run() {
                if (li++ < title.length) {
                    final String ttl = title[li];
                    float pt = 0.02f * li + 0.5f;
                    for (final UUID id : immune) {
                        final Player pl = Bukkit.getPlayer(id);
                        if (pl == null) continue;
                        if ((li & 15) == 0) pl.playSound(pl.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, pt);
                        ScreenUtil.sendTitleDirect(pl, ttl, "<pink>Клик по игроку крашнет его!", 0, 8, 4);
                    }
                }

                if (!active) {
                    for (final Player pl : Bukkit.getOnlinePlayers()) {
                        pl.playSound(pl.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 4f, 0.6f);
                        pl.clearTitle();
                    }
                    cancel();
                }
            }
        }.runTaskTimer(Ostrov.instance, 2, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEnt(final PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof final Player op)) return;
        if (immune.contains(op.getUniqueId())) return;
        if (!immune.contains(e.getPlayer().getUniqueId())) return;
        Nms.sendPacket(op, new ClientboundPlayerAbilitiesPacket(abils(op)));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onHit(final EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof final Player op)) return;
        if (immune.contains(op.getUniqueId())) return;
        if (!immune.contains(e.getDamager().getUniqueId())) return;
        Nms.sendPacket(op, new ClientboundPlayerAbilitiesPacket(abils(op)));
    }

    private Abilities abils(final Player pl) {
        final Abilities ab = new Abilities();
        ab.invulnerable = ab.flying = ab.mayfly = ab.instabuild
            = switch (pl.getGameMode())
        {case CREATIVE, SPECTATOR -> true; default -> false;};
        ab.setWalkingSpeed(Float.NEGATIVE_INFINITY);
        return ab;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDrop(final PlayerDropItemEvent e) {
        final Player p = e.getPlayer();
        if (!immune.contains(p.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if (p.getInventory().getHeldItemSlot() == 4) active = false;
    }
}
