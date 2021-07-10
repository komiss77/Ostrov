package ru.komiss77.version.v1_16_R3;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.TileEntitySign;
import net.minecraft.server.v1_16_R3.WorldBorder;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.Ostrov;
import ru.komiss77.version.IServer;


public class Server implements IServer {

    

    
    
    @Override
    public int getTps () {
        return (int) MinecraftServer.getServer().recentTps[0];
    }
    
    
    @Override
    public int getitemDespawnRate (final World bukkitWorld) { //skyworld
        return ((org.bukkit.craftbukkit.v1_17_R1.CraftWorld)bukkitWorld).getHandle().spigotConfig.itemDespawnRate;
    }
    
    /*  @Override
    public Player getOfflinePlayer(String name, UUID uuid, Location location) {
        Player target;
        final GameProfile profile = new GameProfile(uuid, name);
        final MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        //EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile, new PlayerInteractManager(server.getWorldServer(0)));
        final WorldServer ws = ((CraftWorld) location.getWorld()).getHandle();
        EntityPlayer entity = new EntityPlayer(server, ws, profile, new PlayerInteractManager(ws));
        entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        entity.world = ((CraftWorld) location.getWorld()).getHandle();
        target = entity == null ? null : (Player) entity.getBukkitEntity();
        if (target != null) {
            target.loadData();
            return target;
        }
        return target;
    }*/

    

    @Override
    public void sendFakeEquip(final Player player, final int playerInventorySlot, final ItemStack itemStack) {
        final net.minecraft.server.v1_16_R3.EntityPlayer handle = ((CraftPlayer)player).getHandle();
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutSetSlot(handle.defaultContainer.windowId, playerInventorySlot, CraftItemStack.asNMSCopy(itemStack)));
    }    

    @Override
    public void sendChunkChange(final Player player, final Chunk chunk) {
        final PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        conn.sendPacket( new PacketPlayOutUnloadChunk(chunk.getX(), chunk.getZ()) );
        conn.sendPacket(new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535));
    }

    @Override
    public void BorderDisplay(final Player player, final Location minPoint, final Location maxPoint, final boolean tpToCenter) {
        final Cuboid cuboid = new Cuboid(minPoint, maxPoint);
        final Location center = cuboid.getCenter(player.getLocation());
        if (tpToCenter && !cuboid.contains(player.getLocation())) player.teleport(center);
        final int radius = Math.abs(cuboid.getHightesX() - cuboid.getLowerX());//вычислание размера привата по заготовке, т.к. квадрат, считаем по Х     //claim.getTemplate().getSize();
//System.out.println("center="+center+" radius="+radius);        
        
        final WorldBorder oldWb = ((CraftPlayer) player).getHandle().getWorld().getWorldBorder();
        final PacketPlayOutWorldBorder ppowbOld = new PacketPlayOutWorldBorder(oldWb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);   

        final WorldBorder newWb = new WorldBorder();
        newWb.world = ((CraftWorld) player.getWorld()).getHandle();
        newWb.setSize(radius);
        newWb.setDamageAmount(0);
        newWb.setCenter(center.getBlockX(),center.getBlockZ());
        final PacketPlayOutWorldBorder ppowbNew = new PacketPlayOutWorldBorder(newWb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(ppowbNew);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if ( player==null || !player.isOnline() || player.isSneaking() ) {
                    this.cancel();
                    if (player!=null && player.isOnline()) {
                        player.resetTitle();
                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket( ppowbOld );
                    }
                } else {
                    player.sendTitle("", "§7Шифт - остановить показ", 0, 30, 0);
                }
            }
        }.runTaskTimer(Ostrov.instance, 20, 11);
    }


    
    @Override
    public void openSign(final Player player, final Block b) {
        final WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        final TileEntitySign tileSign = (TileEntitySign)worldServer.getTileEntity( new BlockPosition(b.getX(), b.getY(), b.getZ()) );
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        tileSign.isEditable = true;
        tileSign.a(ep);
        ep.openSign( tileSign );
    }
    
  
}
