package ru.komiss77.version.v1_20_R1;
/*
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Containers;

import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import ru.komiss77.version.IAnwillWrapper;
import ru.komiss77.version.VM;

public class AnwillWrapper implements IAnwillWrapper {
    
    
    
    
    //private static Field bR; //EntityHuman.Container
 //   private static Field bQ; //EntityHuman.ContainerPlayer
    //private static Method fB;
   // private static Method fN;
    
    /*static {
        try {
            bR = EntityHuman.class.getDeclaredField("bR");
            bQ = EntityHuman.class.getDeclaredField("bQ");
            fN = EntityHuman.class.getDeclaredMethod("fN");
        } catch (NoSuchFieldException | SecurityException | NoSuchMethodException ex) {
            Ostrov.log_warn("AnwillWrapper init : "+ex.getMessage());
        }
    }    
    /
    
    
    
    
    private int getRealNextContainerId(Player player) {
        return VM.getNmsServer().toNMS(player).nextContainerCounter();
    }

    /**
     * Turns a {@link Player} into an NMS one
     *
     * @param player The player to be converted
     * @return the NMS EntityPlayer
     /

    @Override
    public int getNextContainerId(Player player, Object container) {
        return ((AnvilContainer) container).getContainerId();
    }

    @Override
    public void handleInventoryCloseEvent(Player player) {
        CraftEventFactory.handleInventoryCloseEvent(VM.getNmsServer().toNMS(player), Reason.PLAYER);
    }

    @Override
    public void sendPacketOpenWindow(Player player, int containerId, String inventoryTitle) {
        //VM.getNmsServer().sendPacket(player, new PacketPlayOutOpenWindow(containerId, Containers.h, IChatBaseComponent.a(inventoryTitle)));
        VM.getNmsServer().toNMS(player).c.a(new PacketPlayOutOpenWindow(containerId, Containers.h, IChatBaseComponent.a(inventoryTitle)));
    }

    @Override
    public void sendPacketCloseWindow(Player player, int containerId) {
        //VM.getNmsServer().sendPacket(player, new PacketPlayOutCloseWindow(containerId));
        VM.getNmsServer().toNMS(player).c.a(new PacketPlayOutCloseWindow(containerId));
    }

    @Override
    public void setActiveContainerDefault(Player player) {
      //  try {
      //      Container c = (Container) bQ.get(VM.getNmsServer().getPlayer(player));
       //     bR.set(VM.getNmsServer().getPlayer(player), c);
      //  } catch (IllegalArgumentException | IllegalAccessException ex) {
     //       Ostrov.log_warn("setActiveContainerDefault : "+ex.getMessage());
     //   }
        VM.getNmsServer().toNMS(player).bR = VM.getNmsServer().toNMS(player).bQ;
    }

    @Override
    public void setActiveContainer(Player player, Object container) {
      ////  try {
      //      bR.set(VM.getNmsServer().getPlayer(player), (Container) container);
      ///  } catch (IllegalArgumentException | IllegalAccessException ex) {
      //      Ostrov.log_warn("setActiveContainer : "+ex.getMessage());
      //  }
        VM.getNmsServer().toNMS(player).bR = (Container) container;
    }

    @Override
    public void setActiveContainerId(Object container, int containerId) {

    }

    @Override
    public void addActiveContainerSlotListener(Object container, Player player) {
        VM.getNmsServer().toNMS(player).a((Container) container);
    }

    @Override
    public Inventory toBukkitInventory(Object container) {
        return ((Container) container).getBukkitView().getTopInventory();
    }

    @Override
    public Object newContainerAnvil(Player player, String title) {
      //  try {
       //     return new AnvilContainer(player, getRealNextContainerId(player), title);
      //  } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      //      Ostrov.log_warn("AnwillWrapper newContainerAnvil : "+ex.getMessage());
      //      return null;
     //   }
        return new AnvilContainer(player, getRealNextContainerId(player), title);

    }

    private static class AnvilContainer extends ContainerAnvil {
        public AnvilContainer(Player player, int containerId, String guiTitle)  {
            super(
                    containerId,
                    //(PlayerInventory)fN.invoke(((CraftPlayer) player).getHandle()),
                    VM.getNmsServer().toNMS(player).fN(),
                    ContainerAccess.a((VM.getNmsServer().toNMS(player.getWorld())), new BlockPosition(0, 0, 0)));
            this.checkReachable = false;
            setTitle(IChatBaseComponent.a(guiTitle));
        }

        @Override
        public void m() {
            super.m();
            this.w.a(0);
        }

        @Override
        public void b(EntityHuman player) {}

        @Override
        protected void a(EntityHuman player, IInventory container) {}

        public int getContainerId() {
            return this.j;
        }
    }
}




*/