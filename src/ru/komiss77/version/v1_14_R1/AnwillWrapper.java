package ru.komiss77.version.v1_14_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.komiss77.version.IAnwillWrapper;



public class AnwillWrapper implements IAnwillWrapper {


    @Override
    public int getNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }


    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }


    @Override
    public void sendPacketOpenWindow(Player player, int containerId) {
        //toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name"))); 1.12
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, new ChatMessage("Repair & Name")));
    }


    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }


    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).activeContainer = toNMS(player).defaultContainer;
    }

 
    @Override
    public void setActiveContainer(Player player, Object container) {
        toNMS(player).activeContainer = (Container) container;
    }

    
    @Override
    public void setActiveContainerId(Object container, int containerId) {
        //((Container) container).windowId = containerId; 1.12
        try {
            final Field field = Container.class.getField("windowId");

            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(container, containerId);
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }    }


    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        ((Container) container).addSlotListener(toNMS(player));
    }

    @Override
    public Inventory toBukkitInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(Player player) {
        //return new AnvlillWrapper.AnvilContainer(toNMS(player));
        return new AnwillWrapper.AnvilContainer(toNMS(player));
    }


    private EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }


    private class AnvilContainer extends ContainerAnvil {

        public AnvilContainer(EntityPlayer nms_player) {
            super(0, nms_player.inventory, ContainerAccess.at(nms_player.world, new BlockPosition(0, 0, 0)) );
            this.checkReachable = false;
        }

       @Override
        public void e() {
            super.e();
            this.levelCost.set(0);
        }
    
    }

}

