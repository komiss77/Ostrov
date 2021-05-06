package ru.komiss77.Listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.InvStatus;



public class InvSeeListener implements Listener {

  /*  private static HashMap listPlayers = new HashMap();


@EventHandler(ignoreCancelled = true)
    public static void onInventoryClick(InventoryClickEvent evt) {
        final InvStatus s = (InvStatus) listPlayers.get(evt.getWhoClicked());

        if (s != null) {
            if (!evt.getWhoClicked().hasPermission("ostrov.invsee.edit") || !s.isOnline()) {
                evt.setCancelled(true);
                return;
            }

            final Player p = (Player) evt.getWhoClicked();
            final Inventory inv = evt.getInventory();

            Bukkit.getScheduler().scheduleSyncDelayedTask(Ostrov.instance, () -> {
                updateArmor(s, inv);
                p.updateInventory();
            });
        }

    }

    private static void updateArmor(InvStatus s, Inventory inv) {
        Player p = s.getPlayer();

        if (s.isOnline() && s.isArmor()) {
            ItemStack[] armor = new ItemStack[4];

            for (int i = 0; i < 4; ++i) {
                armor[i] = inv.getContents()[i];
            }

            p.getInventory().setArmorContents(armor);
        }

    }

@EventHandler(ignoreCancelled = true)
    public static void onInventoryClose(InventoryCloseEvent evt) {
        InvStatus s = (InvStatus) listPlayers.get(evt.getPlayer());

        if (s != null) {
            updateArmor(s, evt.getInventory());
            removePlayer(evt.getPlayer());
        }

    }

    @EventHandler
    public static void onPlayerLeave(PlayerQuitEvent evt) {
        removePlayer(evt.getPlayer());
    }

    public static void addPlayer(Player p, InvStatus s) {
        listPlayers.put(p, s);
    }

    private static void removePlayer(HumanEntity e) {
        if (listPlayers.containsKey(e)) {
            listPlayers.remove(e);
        }

    }*/
}
