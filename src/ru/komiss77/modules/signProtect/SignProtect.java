package ru.komiss77.modules.signProtect;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.version.Nms;

public class SignProtect {
    public static final NamespacedKey KEY = OStrap.key("signProtect");
    public static final int LIMIT = 10;
    public static final int UPDATE_TIME = 1209600; //14*24*60*60
    public static final int LOCK_TIME = 2592000; //30*24*60*60
    public static final Set<BlockType> SIGNS = OStrap.getAll(BlockTypeTagKeys.WALL_SIGNS);
    public static final Predicate<Block> predicate = b -> SIGNS.contains(b.getType().asBlockType());
    public static final Directional SIGN_DATA = BlockType.OAK_WALL_SIGN.createBlockData();
    public static EnumSet<Material> lockables = EnumSet.of(Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE,
        Material.BLAST_FURNACE, Material.SMOKER, Material.HOPPER, Material.BREWING_STAND, Material.LECTERN);


    public static void updateSign(final Sign s, final ProtectionData pd) {
        final SignSide f = s.getSide(Side.FRONT);
        f.line(0, TCUtil.form("§4[§сЧастный§4]"));
        f.line(1, TCUtil.form("§b" + pd.owner));
        f.line(2, TCUtil.form(getExpiriedInfo(pd.valid)));
        f.line(3, TCUtil.form("§7ПКМ - настройка"));
        s.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, pd.toString());
        s.update();
    }

    @Deprecated
    public static String getExpiriedInfo(final int validTo) {
        return getExpiredInfo(validTo);
    }

    public static String getExpiredInfo(final int validTo) {
        return validTo == -1 ? "§6⌚: Бессрочно" : "§6⌚: " + TimeUtil.dateFromStamp(validTo);
    }

    public static Sign findBlockProtection(final Block block) {
        if (!(block.getState() instanceof final InventoryHolder ih))
            return findProtectedSign(block);//info;
        final Inventory inv = ih.getInventory();
        if (!(inv instanceof final DoubleChestInventory dc))
            return findProtectedSign(block);//info;
        final Sign info = findProtectedSign(dc.getLeftSide().getLocation().getBlock());
        if (info != null) return info;
        return findProtectedSign(dc.getRightSide().getLocation().getBlock());
    }

    public static final BlockFace[] NSWE = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private static @Nullable Sign findProtectedSign(final Block bl) {
        for (final BlockFace bf : NSWE) {
            final BlockType bt = Nms.fastType(bl.getWorld(), BVec.of(bl).add(bf.getModX(), 0, bf.getModZ()));
            if (!SIGNS.contains(bt)) continue;
            final Block sign = bl.getRelative(bf);
            final Directional d = (Directional) sign.getBlockData();
            //проверить что табличка прикреплена именно к этому сундуку
            if (d.getFacing() != bf) continue;
            final Sign s = (Sign) sign.getState();
            if (s.getPersistentDataContainer().has(KEY)) return s;
        }
        return null;
    }


}
