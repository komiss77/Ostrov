package ru.komiss77.version.empty;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import ru.komiss77.version.IAnwillWrapper;

public class AnwillWrapper implements IAnwillWrapper {
    //private int getRealNextContainerId(Player player) {
   //     return toNMS(player).nextContainerCounter();
   // }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextContainerId(Player player, Object container) {
        return 0;//((AnvilContainer) container).getContainerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleInventoryCloseEvent(Player player) {
        //CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketOpenWindow(Player player, int containerId, String guiTitle) {
       // toNMS(player).b.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.h, new ChatComponentText(guiTitle)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
      //  toNMS(player).b.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerDefault(Player player) {
       // (toNMS(player)).bV = (Container)(toNMS(player)).bU;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainer(Player player, Object container) {
       // (toNMS(player)).bV = (Container)container;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerId(Object container, int containerId) {
        //noop
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
      //  toNMS(player).initMenu((Container) container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inventory toBukkitInventory(Object container) {
        return Bukkit.createInventory(null, InventoryType.CHEST, Component.text("error"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object newContainerAnvil(Player player, String guiTitle) {
        return null;//return new AnvilContainer(player, guiTitle);
    }
}
