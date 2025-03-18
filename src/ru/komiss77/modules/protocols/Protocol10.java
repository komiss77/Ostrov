package ru.komiss77.modules.protocols;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.ScreenUtil;

public class Protocol10 implements Listener {

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
             "§к<obf>A<!obf>§cротокол 1§к<obf>A",
                          "§cПротокол 1§к<obf>A",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cПротокол 10",
            "§cротокол ",
            "§cотокол",
            "§cтоко",
            "§cок",
            ""};

    private final UUID pid;

    public Protocol10(final Player p) {
        active = true;
        pid = p.getUniqueId();

        final PlayerInventory inv = p.getInventory();
        final ItemStack it = inv.getItem(4);
        inv.setItem(4, sub);
        if (!ItemUtil.isBlank(it, false)) {
            ItemUtil.giveItemsTo(p, it);
        }
        inv.setHeldItemSlot(4);

        new BukkitRunnable() {
            private int i = 0, li = 0;

            @Override
            public void run() {
                if (li++ < title.length) {
                    final String ttl = title[li];
                    for (final Player pl : Bukkit.getOnlinePlayers()) {
                        if ((li & 15) == 0) pl.playSound(pl.getLocation(),
                            Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 0.02f * i + 0.5f);
                        ScreenUtil.sendTitleDirect(pl, ttl, "§6§l" + (SD_TICKS - i) / 20, 0, 8, 4);
                    }
                } else {
                    for (final Player pl : Bukkit.getOnlinePlayers()) {
                        if ((li & 15) == 0) pl.playSound(pl.getLocation(),
                            Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 0.02f * i + 0.5f);
                        ScreenUtil.sendTitleDirect(pl, "", "§6§l" + (SD_TICKS - i) / 20, 0, 8, 4);
                    }
                }

                if (i++ > SD_TICKS) {
                    Ostrov.log_warn("Тестовое закрытие: Protocol 10 sd!");
                    Runtime.getRuntime().halt(0);
                    cancel();
                    return;
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
    public void onDrop(final PlayerDropItemEvent e) {
        final Player p = e.getPlayer();
        if (pid.equals(p.getUniqueId()) && p.getInventory().getHeldItemSlot() == 4) active = false;
    }
}
