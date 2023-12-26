package ru.komiss77.version;

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
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.notes.ThreadSafe;


public interface IServer {
    
    public void pathServer();
     
    public void pathWorld(World bukkitWorld);
     
    public void pathPermissions();
    
    public WorldServer toNMS(final World w);
    
    public net.minecraft.world.entity.Entity toNMS(final Entity p);
    
    public EntityLiving toNMS(final LivingEntity le);
    
    public EntityPlayer toNMS(final Player p);

	public DedicatedServer toNMS();

    int getitemDespawnRate (final World bukkitWorld);

    int getTps();

    byte[] encodeBase64(byte[] binaryData);
   
    //5-шлем
    public void sendFakeEquip(final Player player, final int playerInventorySlot, final ItemStack itemStack);
    
    public void sendChunkChange (final Player player, final Chunk chunk); //skyworld

    public void BorderDisplay(final Player player, final XYZ minPoint, final XYZ maxPoint, final boolean tpToCenter);
    
    public void chatFix();
    
    @ThreadSafe
    public Material getFastMat (final World w, final int x, final int y, final int z);
    
    public BlockData getBlockData(final IBlockData iBlockData);
   
    public void signInput(final Player p, final String suggest, final XYZ signXyz);
}
