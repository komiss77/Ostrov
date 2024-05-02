package ru.komiss77.modules.signProtect;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import ru.komiss77.*;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.inventory.SmartInventory;

import java.util.Collection;
import java.util.function.Predicate;


public class SignProtectLst implements Initiable, Listener {

  public SignProtectLst () {
    SignProtectLst.this.reload();
  }

  public static boolean enable;
  public static final NamespacedKey key = new NamespacedKey(Ostrov.instance, "signProtect");
  public static final int LIMIT = 30;
  private static final Predicate<Block> predicate = b -> Tag.WALL_SIGNS.isTagged(b.getType());

  public void reload() {
    onDisable();
    if (!enable) {
      return;
    }
    Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
    Ostrov.log_ok("§2Приватные таблички в деле.");
  }

  @Override
  public void postWorld() {}

  @Override
  public void onDisable() {
    HandlerList.unregisterAll(this);
  }



  //@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onBucketEmpty(PlayerBucketEmptyEvent e) {
  }

  //@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onBucketUse(PlayerBucketFillEvent e) {
  }


  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void chunkLoad(ChunkLoadEvent e) {
    final Collection <BlockState> signs = e.getChunk().getTileEntities(predicate, false);
//Ostrov.log("chunkLoad signs:"+signs.size());
    for (final BlockState bs : signs) {
      //if (bs instanceof Sign) { predicate выдаст только таблички
        Sign sign = (Sign) bs;
        if (sign.getPersistentDataContainer().has(key)) {
          final ProtectionData pd = ProtectionData.of(sign);
//Ostrov.log("ProtectionData : isValid?"+pd.isValid());
          if (!pd.isValid()) {
            final Oplayer op = PM.getOplayer(pd.owner);
            if (op!=null) {
              int curr = 0;
              if (op.mysqlData.containsKey("signProtect") && !op.mysqlData.get("signProtect").isEmpty()) {
                curr = Integer.parseInt(op.mysqlData.get("signProtect"));
                curr--;
              }
              op.mysqlData.put("signProtect", String.valueOf(curr));
            } else {
              LocalDB.executePstAsync(Bukkit.getConsoleSender(),
                "UPDATE `playerData` SET signProtect=signProtect-1 WHERE `signProtect` > 0 AND `name`='"+pd.owner+"' ;");
            }
            SignSide f = sign.getSide(Side.FRONT);
            f.line(0, Component.text("§4[§сЧастный§4]"));
            f.line(1, Component.text("§b"+pd.owner));
            f.line(2, Component.text("§4Просрочено"));
            f.line(3, Component.empty());
            sign.getPersistentDataContainer().remove(key);
            sign.update();
          }
        }
     // }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void openSign(PlayerOpenSignEvent e) {
    Sign s = e.getSign();
    if (s.getPersistentDataContainer().has(key)) {
      e.setCancelled(true);
      final ProtectionData pd = new ProtectionData(s);//ProtectionData.of(s); в меню переваривается долго, может подмениться данные
      if (pd.isValid() && pd.isOwner(e.getPlayer())) {
        SmartInventory.builder()
          .provider(new AccesEdit(s, pd))
          .type(InventoryType.CHEST)
          .size(5)
          .title("§7Настройки доступа")
          .build()
          .open(e.getPlayer());
      }
    }
  }


  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void inventoryOpen(InventoryOpenEvent e) {
    //InventoryHolder ih = e.getInventory().getHolder();
    //if (ih instanceof DoubleChest){
    //  ih = ((DoubleChest)ih).getLeftSide();
    //}
    //if (ih instanceof BlockState) {
      final Block b = getInventoryBlock(e.getInventory());//((BlockState)ih).getBlock();
      if ( b!= null && SignProtect.lockables.contains(b.getType())) {
        Sign s = SignProtect.findBlockProtection(b);
        if (s!=null) {
          final ProtectionData pd = ProtectionData.of(s);
          if (pd.isValid() && pd.valid!=-1) {
            if (Timer.getTime()-pd.valid<1209600 && pd.isOwner((Player) e.getPlayer())) { //14*24*60*60
              pd.valid = Timer.getTime()+2592000; //30*24*60*60
              SignProtect.updateSign(s, pd); //автообновление срока за 2 недели до конца
              return; //владельцу точно открыть
            }
            if (!pd.canUse((Player) e.getPlayer())) {
              e.setCancelled(true);
              ApiOstrov.sendActionBarDirect((Player) e.getPlayer(), "§eДоступ к сундуку ограничен!");
              //e.getPlayer().sendMessage("Защищено");
            }
          }
        }
      }
    //}
  }


  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void blockBreak(BlockBreakEvent e) {
    if (Tag.WALL_SIGNS.isTagged(e.getBlock().getType()) ) {
      Sign s = (Sign) e.getBlock().getState();
      if (s.getPersistentDataContainer().has(key)) {
        final ProtectionData pd = ProtectionData.of(s);
//Ostrov.log("blockBreak valid?"+pd.isValid()+" isOwner?"+pd.isOwner(e.getPlayer())+" pd="+pd.toString());
        if (pd.isValid() && pd.isOwner(e.getPlayer()) && e.getPlayer().isSneaking()) {
          final Oplayer op = PM.getOplayer(e.getPlayer());
          int curr = 0;
          if (op.mysqlData.containsKey("signProtect") && !op.mysqlData.get("signProtect").isEmpty()) {
            curr = Integer.parseInt(op.mysqlData.get("signProtect"));
            curr--;
          }
          op.mysqlData.put("signProtect", String.valueOf(curr));
        } else {
          e.setCancelled(true);
        }
      }
      return;
    }
    if (SignProtect.lockables.contains(e.getBlock().getType())) {
      if(SignProtect.findBlockProtection(e.getBlock()) != null) {
        e.setCancelled(true);
      }
    }
  }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void blockPlace(BlockPlaceEvent e) {
      if (Tag.WALL_SIGNS.isTagged(e.getBlockPlaced().getType()) ) {
        final Block placed = e.getBlockPlaced();
        Directional d = (Directional)placed.getBlockData(); //Directional только для WALL_SIGNS, для STANDING_SIGNS=Rotatable
        final Block attachedTo = placed.getRelative(d.getFacing().getOppositeFace());
        if (SignProtect.lockables.contains(attachedTo.getType())) {
          Sign current = SignProtect.findBlockProtection(attachedTo);
          if (e.getPlayer().isSneaking()) {
            if (current==null) {
              final Oplayer op = PM.getOplayer(e.getPlayer());
              if (op.isGuest) {
                e.getPlayer().sendMessage("§5Гости не могут ставить защитные таблички!");
                return;
              }
              Sign s = (Sign) placed.getState();
              int curr = 1;
              if (op.mysqlData.containsKey("signProtect") && !op.mysqlData.get("signProtect").isEmpty()) {
                curr = Integer.parseInt(op.mysqlData.get("signProtect"));
                curr++;
              }
              if (curr>=LIMIT) {
                e.getPlayer().sendMessage("§cЛимит приватных табличек! ("+LIMIT+")");
                return;
              }
              op.mysqlData.put("signProtect", String.valueOf(curr));
              SignProtect.updateSign(s, new ProtectionData(e.getPlayer().getName()));
            } else {
              //e.getPlayer().sendMessage("§6Защита уже установлена!");
              ApiOstrov.sendActionBarDirect(e.getPlayer(), "§6Защита уже установлена!");
            }
          }
        }
      } else if ( SignProtect.lockables.contains(e.getBlock().getType())) {
        final Oplayer op = PM.getOplayer(e.getPlayer());
        if (!op.isGuest && (!op.mysqlData.containsKey("signProtect") || op.mysqlData.get("signProtect").isEmpty()) && !Timer.has(e.getPlayer(), "signProtect")) {
          e.getPlayer().sendMessage(Component.text("§6§k***§r §eЗащити ")
            .append(Lang.t(e.getBlock().getType(), e.getPlayer()))
            .append(Component.text(" §e- для этого присесть и §bправый клик табличкой! §6§k***"))
          );
          Timer.add(e.getPlayer(), "signProtect", 900);
        }
      }
    }


  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInventoryMove(InventoryMoveItemEvent e){
    Block b = getInventoryBlock(e.getSource());
    if ( b!= null && SignProtect.lockables.contains(b.getType())) {
      Sign s = SignProtect.findBlockProtection(b);
      if (s!=null) {
        final ProtectionData pd = ProtectionData.of(s);
        if (pd.isValid()) {
            e.setCancelled(true);
        }
      }
    }
    b = getInventoryBlock(e.getDestination());
    if ( b!= null && SignProtect.lockables.contains(b.getType())) {
      Sign s = SignProtect.findBlockProtection(b);
      if (s!=null) {
        final ProtectionData pd = ProtectionData.of(s);
        if (pd.isValid()) {
          e.setCancelled(true);
        }
      }
    }
  }

  private Block getInventoryBlock(final Inventory inventory) {
    final InventoryHolder holder = inventory.getHolder();
    if (holder instanceof BlockState) {
      return ((BlockState) holder).getBlock();
    }
    if (holder instanceof DoubleChest) {
      InventoryHolder leftHolder = ((DoubleChest) holder).getLeftSide();
      if (leftHolder instanceof BlockState) {
        return ((BlockState) leftHolder).getBlock();
      }
    }
    return null;
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onBlockBurnEvent(BlockBurnEvent e) {
    if (isProtected(e.getBlock())) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onBlockExplodeEvent(BlockExplodeEvent e) {
    e.blockList().removeIf(SignProtectLst::isProtected);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onEntityExplodeEvent(EntityExplodeEvent e) {
    e.blockList().removeIf(SignProtectLst::isProtected);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onBlockPistonExtend(BlockPistonExtendEvent e) {
    for (Block block : e.getBlocks()) {
      if (isProtected(block)) {
        e.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onBlockPistonRetract(BlockPistonRetractEvent e) {
    if (e.isSticky()) {
      for (Block block : e.getBlocks()) {
        if (isProtected(block)) {
          e.setCancelled(true);
          return;
        }
      }
    }
  }




  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onLecternTake(PlayerTakeLecternBookEvent e){
    final Sign s = SignProtect.findBlockProtection(e.getLectern().getBlock());
    if (s!=null) {
      final ProtectionData pd = ProtectionData.of(s);
      if (!pd.canUse(e.getPlayer())) {
        e.setCancelled(true);
        ApiOstrov.sendActionBarDirect(e.getPlayer(), "§eВы не можете взять книгу!");
        e.setCancelled(true);
      }
    }
  }


  //@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onStructureGrow(StructureGrowEvent e){
    for (BlockState bs : e.getBlocks()){
      if (isProtected(bs.getBlock())) {
        e.setCancelled(true);
        return;
      }
    }
  }


  //@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onBlockRedstoneChange(BlockRedstoneEvent e) {
    if (isProtected(e.getBlock())) {
      e.setNewCurrent(e.getOldCurrent());
    }
  }


  //@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onMobChangeBlock(EntityChangeBlockEvent e) {
    if (isProtected(e.getBlock())) {
      e.setCancelled(true);
    }
  }







    private static boolean isProtected (final Block b) {
      if (Tag.WALL_SIGNS.isTagged(b.getType()) ) {
        Sign s = (Sign) b.getState();
        return s.getPersistentDataContainer().has(key);
      }
      if (SignProtect.lockables.contains(b.getType())) {
        return SignProtect.findBlockProtection(b) != null;
      }
      return false;
    }

}
    

    
    
    
    
   
    
    
