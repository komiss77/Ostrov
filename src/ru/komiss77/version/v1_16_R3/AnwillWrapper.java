package ru.komiss77.version.v1_16_R3;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.komiss77.version.IAnwillWrapper;



public class AnwillWrapper implements IAnwillWrapper {
    
    
    private int getRealNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNextContainerId(Player player, Object container) {
        return ((AnvilContainer) container).getContainerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketOpenWindow(Player player, int containerId, String guiTitle) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, new ChatMessage(guiTitle)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainerDefault(Player player) {
        toNMS(player).activeContainer = toNMS(player).defaultContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveContainer(Player player, Object container) {
        toNMS(player).activeContainer = (Container) container;
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
        ((Container) container).addSlotListener(toNMS(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inventory toBukkitInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object newContainerAnvil(Player player, String guiTitle) {
        return new AnvilContainer(player, guiTitle);
    }

    /**
     * Turns a {@link Player} into an NMS one
     *
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     */
    private EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    /**
     * Modifications to ContainerAnvil that makes it so you don't have to have xp to use this anvil
     */
    private class AnvilContainer extends ContainerAnvil {

        public AnvilContainer(Player player, String guiTitle) {
            super(getRealNextContainerId(player), ((CraftPlayer) player).getHandle().inventory,
                    ContainerAccess.at(((CraftWorld) player.getWorld()).getHandle(), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(new ChatMessage(guiTitle));
        }

        @Override
        public void e() {
            super.e();
            this.levelCost.set(0);
        }

        @Override
        public void b(EntityHuman entityhuman) {
        }

        @Override
        protected void a(EntityHuman entityhuman, World world, IInventory iinventory) {
        }

        public int getContainerId() {
            return windowId;
        }

    }
    
    
    
  /*  @Override
    private int getRealNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }
    
    @Override
    public int getNextContainerId(Player player) {
        return toNMS(player).nextContainerCounter();
    }


    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(toNMS(player));
    }


    @Override
    public void sendPacketOpenWindow(Player player, int containerId, String guiTitle) {
        toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, new ChatMessage(guiTitle)));        
//toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name"))); 1.12
        //toNMS(player).playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, Containers.ANVIL, new ChatMessage("Repair & Name")));
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
            field.set(container, containerId);
            
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
    public Object newContainerAnvil(Player player, String guiTitle) {
        return new AnvilContainer(player, guiTitle);
        //return new AnvlillWrapper.AnvilContainer(toNMS(player));
        //return new AnwillWrapper.AnvilContainer(toNMS(player));
    }


    private EntityPlayer toNMS(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

*/
    
    /* private class AnvilContainer extends ContainerAnvil {
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
            крашит папер!!    CraftEventFactory.callPrepareAnvilEvent((InventoryView)this.getBukkitView(), this.output);
                this.c();
            }
        }
    }*/

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

