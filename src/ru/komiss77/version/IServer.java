package ru.komiss77.version;

import net.minecraft.network.protocol.Packet;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.block.state.IBlockData;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.notes.ThreadSafe;
import ru.komiss77.version.v1_20_R1.PlayerPacketHandler;


public interface IServer {
    
    void pathServer();
     
    void pathWorld(World bukkitWorld);
     
    void pathPermissions();
    
    WorldServer toNMS(final World w);
    
    net.minecraft.world.entity.Entity toNMS(final Entity p);
    
    EntityLiving toNMS(final LivingEntity le);
    
    EntityPlayer toNMS(final Player p);

    DedicatedServer toNMS();

    int getitemDespawnRate (final World bukkitWorld);

    int getTps();

    byte[] encodeBase64(byte[] binaryData);
   
    //5-шлем
    void sendFakeEquip(final Player player, final int playerInventorySlot, final ItemStack itemStack);
    
    void sendChunkChange(final Player player, final Chunk chunk); //skyworld

    @Deprecated
    void BorderDisplay(final Player player, final XYZ minPoint, final XYZ maxPoint, final boolean tpToCenter);
    
    void chatFix();
    
    @ThreadSafe
    Material getFastMat(final World w, final int x, final int y, final int z);

    @ThreadSafe
    Material getFastMat(final WXYZ loc);
    
    BlockData getBlockData(final IBlockData iBlockData);
   
    void signInput(final Player p, final String suggest, final XYZ signXyz);
    
    PlayerPacketHandler addPacketSpy(final Player p, final Oplayer op);
    
    void addPacketSpy();
    
    void removePacketSpy(final Player p);

    void sendPacket(final Player p, final Packet<?> packet);
    
}
