package ru.komiss77.modules.signProtect;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.version.Nms;

import java.util.EnumSet;

public class SignProtect {
  public static EnumSet<Material> lockables = EnumSet.of(
    Material.CHEST,
    Material.TRAPPED_CHEST,
    Material.FURNACE,
    Material.BLAST_FURNACE,
    Material.SMOKER,
    Material.HOPPER,
    Material.BREWING_STAND,
    Material.LECTERN
  );
  public static BlockFace[] NSWE = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};


  public static void updateSign(final Sign s, final ProtectionData pd) {
//Ostrov.log("updateSign pd="+pd.toString());
    SignSide f = s.getSide(Side.FRONT);
    f.line(0, Component.text("§4[§сЧастный§4]"));
    f.line(1, Component.text("§b"+pd.owner));
    f.line(2, Component.text(getExpiriedInfo(pd.valid)));
    f.line(3, Component.text("§7ПКМ - настройка"));
    //String data = pd.valid + LocalDB.W_SPLIT + pd.owner + LocalDB.L_SPLIT + ApiOstrov.toString(pd.users,false);
    s.getPersistentDataContainer().set(SignProtectLst.key, PersistentDataType.STRING, pd.toString() );
    s.update();

  }


  public static String getExpiriedInfo(final int validTo) {
    return validTo==-1 ? "§6#Бессрочно" : "§6#"+ ApiOstrov.dateFromStamp(validTo);
  }


  public static Sign findBlockProtection(final Block block) {
    Sign info = null;

    if (block.getState() instanceof InventoryHolder) {
      Inventory inv = ((InventoryHolder) block.getState()).getInventory();
//Ostrov.log("InventoryHolder    DoubleChestInventory?"+(inv instanceof DoubleChestInventory));
      if (inv instanceof final DoubleChestInventory doubleChest) {
        Block left = doubleChest.getLeftSide().getLocation().getBlock();
        info = findProtectedSign(left);
        if (info!=null) return info;
        Block right = doubleChest.getRightSide().getLocation().getBlock();
        info = findProtectedSign(right);
        return info;
      }

    }

    //if (info==null) {
    //  info = findProtectedSign(block);
    //}
    return findProtectedSign(block);//info;
  }


  private static Sign findProtectedSign(final Block block){
    Material mat;
    for (BlockFace bf : NSWE){
      mat = Nms.getFastMat(block.getWorld(), block.getX()+bf.getModX(), block.getY(), block.getZ()+bf.getModZ());
      if (Tag.WALL_SIGNS.isTagged(mat) ) {
        Block signBlock = block.getRelative(bf);
        Directional d = (Directional)signBlock.getBlockData();
        if (d.getFacing()==bf) { //проверить что табличка прикреплена именно к этому сундуку
          Sign s = (Sign)signBlock.getState();
          if (s.getPersistentDataContainer().has(SignProtectLst.key)) {
            return s;
          }
        }
      }
    }
    return null;
  }




}
