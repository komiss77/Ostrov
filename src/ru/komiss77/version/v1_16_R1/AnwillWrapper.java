package ru.komiss77.version.v1_16_R1;

import java.lang.reflect.Field;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import ru.komiss77.Ostrov;
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
        try {
           /* final Field field = Container.class.getField("windowId");
            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(container, containerId);*/
            
            final Field field = Container.class.getField("windowId");
            field.setAccessible(true);
            field.setInt(container, containerId);
            
        } catch(IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            Ostrov.log_err(" AnvilGUI setActiveContainerId : "+ex.getMessage());
            //throw new RuntimeException(ex);
        }    
    }


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
        private final ItemStack output;
        
        public AnvilContainer(final EntityHuman entityhuman) {
            super(getNextContainerId((Player)entityhuman.getBukkitEntity()), entityhuman.inventory, ContainerAccess.at(entityhuman.world, new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            this.setTitle((IChatBaseComponent)new ChatMessage("Repair & Name", new Object[0]));
            final org.bukkit.inventory.ItemStack itemStack = new org.bukkit.inventory.ItemStack(org.bukkit.Material.COAL);
            this.output = CraftItemStack.asNMSCopy(itemStack);
            this.getSlot(1).set(CraftItemStack.asNMSCopy(itemStack));
        }
        
        @Override
        public void e() {
            this.levelCost.set(0);
            if (this.renameText != null && !this.renameText.isEmpty()) {
                this.output.a((IChatBaseComponent)new ChatComponentText(ChatColor.translateAlternateColorCodes('&', this.renameText)));
                this.getSlot(2).set(this.output);
                CraftEventFactory.callPrepareAnvilEvent((InventoryView)this.getBukkitView(), this.output);
                this.c();
            }
        }
    }

 /*   private class AnvilContainer extends ContainerAnvil {

        public AnvilContainer(EntityPlayer nms_player) {
            super(0, nms_player.inventory, ContainerAccess.at(nms_player.world, new BlockPosition(0, 0, 0)) );
            this.checkReachable = false;
        }

       @Override
        public void e() {
            super.e();
            this.levelCost.set(0);
        }
    
    }*/

}

