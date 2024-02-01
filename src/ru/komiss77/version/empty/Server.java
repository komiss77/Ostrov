package ru.komiss77.version.empty;

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
import ru.komiss77.version.IServer;
import ru.komiss77.version.v1_20_R1.PlayerPacketHandler;

public class Server implements IServer {

    @Override
    public byte[] encodeBase64(byte[] binaryData) {
        return binaryData;
    }

    @Override
    public void pathServer() {
    }

    @Override
    public void pathWorld(World bukkitWorld) {
    }

    @Override
    public int getTps() {
        return 20;
    }

    @Override
    public int getitemDespawnRate(final World bukkitWorld) { //skyworld
        return 200;
    }

    public void sendTabListInfo(final Player p) {
    }

    @Override
    public void sendFakeEquip(final Player player, final int playerInventorySlot, final ItemStack itemStack) {
    }

    @Override
    public void sendChunkChange(final Player player, final Chunk chunk) {
    }

    @Override
    public void BorderDisplay(final Player player, final XYZ minPoint, final XYZ maxPoint, final boolean tpToCenter) {
    }

    @Override
    public void pathPermissions() {
    }

    @Override
    public void chatFix() {
    }

    @Override
    public void signInput(Player p, String suggest, XYZ xyz) {
    }

    @Override
    public Material getFastMat(final World w, int x, int y, int z) {
        return w.getBlockAt(x, y, z).getType();
    }

    @Override
    public Material getFastMat(final WXYZ loc) {
        return loc.w.getBlockAt(loc.x, loc.y, loc.z).getType();
    }

    @Override
    public BlockData getBlockData(IBlockData iBlockData) {
        return null;
    }

    @Override
    public WorldServer toNMS(final World w) {
        return null;
    }

    @Override
    public net.minecraft.world.entity.Entity toNMS(final Entity w) {
        return null;
    }

    @Override
    public EntityLiving toNMS(final LivingEntity le) {
        return null;
    }

    @Override
    public EntityPlayer toNMS(final Player p) {
        return null;
    }

    @Override
    public DedicatedServer toNMS() {
            return null;
    }

    @Override
    public PlayerPacketHandler addPacketSpy(Player p, final Oplayer op) {
        return null;
    }

    @Override
    public void removePacketSpy(Player p) {
    }

    @Override
    public void addPacketSpy() {
    }

    @Override
    public void sendPacket(final Player p, final Packet<?> packet) {
    }

}
