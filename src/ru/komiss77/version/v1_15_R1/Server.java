package ru.komiss77.version.v1_15_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.DedicatedServer;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_15_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import net.minecraft.server.v1_15_R1.PlayerInteractManager;
import net.minecraft.server.v1_15_R1.WorldServer;
import net.minecraft.server.v1_15_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_15_R1.TileEntitySign;
import net.minecraft.server.v1_15_R1.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftMetaBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Managers.Cuboid;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.version.IServer;


public class Server implements IServer {

    
    
    @Override
    public String getlevelName (final World bukkitWorld) {
        return ((DedicatedServer)((CraftWorld)bukkitWorld).getHandle().getMinecraftServer()).propertyManager.getProperties().levelName;
    }

    
    
    @Override
    public int getMaxWorldSize (final World bukkitWorld) {
        return ((DedicatedServer)((CraftWorld)bukkitWorld).getHandle().getMinecraftServer()).getDedicatedServerProperties().maxWorldSize;
    }

    
    
    @Override
    public int getTps () {
        return (int) MinecraftServer.getServer().recentTps[0];
    }
    
    
    
    @Override
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
    }
    
    
    
    @Override
    public ItemStack getCustomHead (String base64code, final String item_name, final String lore){
        base64code = "http://textures.minecraft.net/texture/" + base64code;
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        if (base64code == null || base64code.isEmpty()) {
            return new ItemBuilder(Material.BEDROCK).name("§cошибочная текстура!").build();
        }
        final SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(item_name);
        skullMeta.setLore(ItemUtils.Gen_lore(null, lore, ""));
        
        try {
            final GameProfile mojang = new GameProfile(UUID.randomUUID(), null);
            final byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", base64code).getBytes());
            mojang.getProperties().put("textures", new Property("textures", new String(encodedData)));
        
            final Field profileField = skullMeta.getClass().getDeclaredField("profile");
            if (profileField!=null) {
                profileField.setAccessible(true);
                profileField.set(skullMeta, mojang);
            } else {
                Ostrov.log_err("getCustomHead profileField = null !");
                return new ItemBuilder(Material.BEDROCK).name("§cошибка profileField текстуры!").build();
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            Ostrov.log_err("getCustomHead error: "+e.getMessage());
            return new ItemBuilder(Material.BEDROCK).name("§cошибочный профиль текстуры!").build();
        }
        //assert profileField != null;
        //profileField.setAccessible(true);
        //try {
        //    profileField.set(skullMeta, mojang);
        //} catch (IllegalArgumentException | IllegalAccessException e) {
        //    Ostrov.log_err("getCustomHead2 error: "+e.getMessage());
        //}
        //skullMeta.setCustomModelData(777);
        skull.setItemMeta(skullMeta);
        return skull;
        
        
        //UUID hashAsId = new UUID(base64code.hashCode(), base64code.hashCode());
	//return Bukkit.getUnsafe().modifyItemStack(skull, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64code + "\"}]}}}");
    }
    
    
    @Override
    public BookMeta addPages (BookMeta bookMeta, final List <TextComponent> pagesList) {
        List<IChatBaseComponent> pages;
        try {
            pages = (List<IChatBaseComponent>) CraftMetaBook.class.getDeclaredField("pages").get(bookMeta);
        } catch (ReflectiveOperationException ex) {
            Ostrov.log_err("BookMeta addPages  : "+ex.getMessage());
            return bookMeta;
        }

        for (TextComponent tc : pagesList) {
            IChatBaseComponent ibc_page = ChatSerializer.a(ComponentSerializer.toString(tc));
            pages.add(ibc_page);
        }
        return bookMeta;
    }
    
    @Override
    public void sendFakeEquip(final Player player, final int playerInventorySlot, final ItemStack itemStack) {
        final net.minecraft.server.v1_15_R1.EntityPlayer handle = ((CraftPlayer)player).getHandle();
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
        
        final WorldBorder oldWb = ((org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer) player).getHandle().getWorld().getWorldBorder();
        final PacketPlayOutWorldBorder ppowbOld = new PacketPlayOutWorldBorder(oldWb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);   

        final WorldBorder newWb = new WorldBorder();
        newWb.world = ((org.bukkit.craftbukkit.v1_15_R1.CraftWorld) player.getWorld()).getHandle();
        newWb.setSize(radius);
        newWb.setDamageAmount(0);
        newWb.setCenter(center.getBlockX(),center.getBlockZ());
        final PacketPlayOutWorldBorder ppowbNew = new PacketPlayOutWorldBorder(newWb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        
        ((org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(ppowbNew);
        
        new BukkitRunnable() {
        
            @Override
            public void run() {
                if ( player==null || !player.isOnline() || player.isSneaking() ) {
                    this.cancel();
                    if (player!=null && player.isOnline()) {
                        player.resetTitle();
                        ((org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket( ppowbOld );
                    }
                } else {
                    player.sendTitle("", "§7Шифт - остановить показ", 0, 30, 0);
                }
            }
        }.runTaskTimer(Ostrov.instance, 5, 11);
    }
    
    @Override
    public int getitemDespawnRate(World bukkitWorld) {
        return ((org.bukkit.craftbukkit.v1_15_R1.CraftWorld)bukkitWorld).getHandle().spigotConfig.itemDespawnRate;
    }
    
    @Override
    public void openSign(final Player player, final Block b) {
        WorldServer nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
        ((CraftPlayer) player).getHandle().openSign((TileEntitySign)nmsWorld.getTileEntity( new BlockPosition(b.getX(), b.getY(), b.getZ()) ) );
    }
    
}
