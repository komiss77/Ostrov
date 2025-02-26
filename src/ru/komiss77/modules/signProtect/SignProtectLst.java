package ru.komiss77.modules.signProtect;

import javax.annotation.Nullable;
import java.util.Collection;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.BlockDisplay;
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
import org.bukkit.util.Vector;
import ru.komiss77.*;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.Nms;

public class SignProtectLst implements Initiable, Listener {

    public SignProtectLst() {
        reload();
    }

    public void reload() {
        onDisable();
        if (!Cfg.signProtect) {
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
        Ostrov.log_ok("§2Приватные таблички в деле.");
    }

    @Override
    public void postWorld() {
    }

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
    public void chunkLoad(final ChunkLoadEvent e) {
        final Collection<BlockState> signs = e.getChunk().getTileEntities(SignProtect.predicate, false);
        for (final BlockState bs : signs) {
            if (!(bs instanceof final Sign sign)) continue;
            if (!sign.getPersistentDataContainer().has(SignProtect.KEY)) continue;
            final ProtectionData pd = ProtectionData.of(sign);
            if (pd.isValid()) continue;
            final Oplayer op = PM.getOplayer(pd.owner);
            final SignSide f = sign.getSide(Side.FRONT);
            f.line(0, TCUtil.form("§4[§сЧастный§4]"));
            f.line(1, TCUtil.form("§b" + pd.owner));
            f.line(2, TCUtil.form("§4Просрочено"));
            f.line(3, Component.empty());
            sign.getPersistentDataContainer().remove(SignProtect.KEY);
            sign.update();
            if (op == null) {
                LocalDB.executePstAsync(Bukkit.getConsoleSender(),
                    "UPDATE `playerData` SET signProtect=signProtect-1 WHERE `signProtect` > 0 AND `name`='" + pd.owner + "' ;");
                continue;
            }
            int curr = 0;
            final String spr = op.mysqlData.get("signProtect");
            if (spr != null && !spr.isEmpty()) {
                curr = Math.max(NumUtil.intOf(spr, 0) - 1, 0);
            }
            op.mysqlData.put("signProtect", String.valueOf(curr));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void openSign(final PlayerOpenSignEvent e) {
        final Sign s = e.getSign();
        if (!s.getPersistentDataContainer().has(SignProtect.KEY)) return;
        e.setCancelled(true);
        final ProtectionData pd = new ProtectionData(s);
        if (!pd.isValid() || !pd.isOwner(e.getPlayer())) return;
        SmartInventory.builder()
            .provider(new AccesEdit(s, pd))
            .type(InventoryType.CHEST)
            .size(5)
            .title("§7Настройки доступа")
            .build()
            .open(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void inventoryOpen(final InventoryOpenEvent e) {
        final Block b = getInventoryBlock(e.getInventory());//((BlockState)ih).getBlock();
        if (b == null || !SignProtect.lockables.contains(b.getType())) return;
        final Sign s = SignProtect.findBlockProtection(b);
        if (s == null) return;
        final ProtectionData pd = ProtectionData.of(s);
        if (!pd.isValid() || pd.valid == -1) return;
        final Player pl = (Player) e.getPlayer();
        if (Timer.secTime() - pd.valid < SignProtect.UPDATE_TIME && pd.isOwner(pl)) {
            pd.valid = Timer.secTime() + SignProtect.LOCK_TIME;
            SignProtect.updateSign(s, pd); //автообновление срока за 2 недели до конца
            return; //владельцу точно открыть
        }
        if (pd.canUse(pl)) return;
        if (ApiOstrov.isStaff(pl)) {
            pl.sendMessage("§eДоступ к сундуку для Персонала");
            return;
        }
        e.setCancelled(true);
        ScreenUtil.sendActionBarDirect(pl, "§cДоступ к сундуку ограничен!");
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void blockBreak(final BlockBreakEvent e) {
        final Block bl = e.getBlock();
        if (SignProtect.lockables.contains(bl.getType())) {
            if (SignProtect.findBlockProtection(bl) == null) return;
            e.setCancelled(true);
            return;
        }
        if (!SignProtect.SIGNS.contains(bl.getType().asBlockType())) return;
        final Sign s = (Sign) bl.getState();
        if (!s.getPersistentDataContainer().has(SignProtect.KEY)) return;
        final ProtectionData pd = ProtectionData.of(s);
        final Player pl = e.getPlayer();
        if (!pd.isValid() || !pd.isOwner(pl)) {
            e.setCancelled(true);
            return;
        }
        final Oplayer op = PM.getOplayer(pl);
        int curr = 0;
        final String spr = op.mysqlData.get("signProtect");
        if (spr != null && !spr.isEmpty()) {
            curr = Math.max(NumUtil.intOf(spr, 0) - 1, 0);
        }
        op.mysqlData.put("signProtect", String.valueOf(curr));
        pl.sendMessage(Ostrov.PREFIX + "§eТабличка привата удалена!");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void blockPlace(final BlockPlaceEvent e) {
        final Player pl = e.getPlayer();
        final Block bl = e.getBlock();
        if (SignProtect.lockables.contains(bl.getType())) {
            final Oplayer op = PM.getOplayer(pl);
            if (op.isGuest || (op.mysqlData.containsKey("signProtect") && !op.mysqlData.get("signProtect").isEmpty())
                || Timer.has(pl, "signProtect")) return;
            pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "§6<obf>*<!obf> ПКМ Табличкой §e- приват блока ")
                .append(Lang.t(bl.getType(), pl)).append(TCUtil.form("§e! §6<obf>*")));
            SignProtect.SIGN_DATA.setFacing(LocUtil.vecToFace(pl.getEyeLocation()
                .subtract(bl.getLocation().toCenterLocation()).toVector(), false));
            pl.sendMessage(SignProtect.SIGN_DATA.getFacing().name());
//            final BlockDisplay sd = pl.getWorld().spawn(bl.getRelative(SIGN_DATA.getFacing()).getLocation(),
//                BlockDisplay.class, bd -> {bd.setBlock(SIGN_DATA); bd.setVisibleByDefault(false); bd.setInvisible(true);});
            final BlockDisplay sd = pl.getWorld().spawn(signLoc(bl, SignProtect.SIGN_DATA.getFacing()), BlockDisplay.class,
                bd -> {bd.setBlock(SignProtect.SIGN_DATA); bd.setVisibleByDefault(false); bd.setInvisible(true);});
            pl.showEntity(Ostrov.instance, sd);
            Nms.colorGlow(sd, NamedTextColor.YELLOW, p -> p.getEntityId() == pl.getEntityId());
            Ostrov.sync(() -> sd.remove(), 80);
            Timer.add(pl, "signProtect", 900);
            return;
        }

        if (!SignProtect.SIGNS.contains(bl.getType().asBlockType())) return;
        final Directional d = (Directional) bl.getBlockData(); //Directional только для WALL_SIGNS, для STANDING_SIGNS=Rotatable
        final Block attachedTo = bl.getRelative(d.getFacing().getOppositeFace());
        if (!SignProtect.lockables.contains(attachedTo.getType())) return;
        if (!pl.isSneaking()) return;
        final Sign current = SignProtect.findBlockProtection(attachedTo);
        if (current != null) {
            ScreenUtil.sendActionBarDirect(pl, "§6Защита уже установлена!");
            return;
        }
        final Oplayer op = PM.getOplayer(pl);
        if (op.isGuest) {
            pl.sendMessage("§5Гости не могут ставить защитные таблички!");
            return;
        }
        final Sign s = (Sign) bl.getState();
        int curr = 1;
        final String spr = op.mysqlData.get("signProtect");
        if (spr != null && !spr.isEmpty())
            curr = NumUtil.intOf(spr, 0) + 1;
        if (curr > SignProtect.LIMIT) {
            pl.sendMessage("§cЛимит приватных табличек! (" + SignProtect.LIMIT + ")");
            return;
        }
        op.mysqlData.put("signProtect", String.valueOf(curr));
        SignProtect.updateSign(s, new ProtectionData(pl.getName()));
    }

    private Location signLoc(final Block bl, final BlockFace bf) {
        final Location loc = bl.getLocation().toCenterLocation();
        return new Location(bl.getWorld(), bf.getModZ() * -0.5d + loc.getX(),
            -0.8d + loc.getY(), bf.getModX() * -0.5d + loc.getZ(),
            NumUtil.getYaw(new Vector(bf.getModX(), 0, bf.getModZ())), 0f);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMove(final InventoryMoveItemEvent e) {
        final Block src = getInventoryBlock(e.getSource());
        if (src != null && SignProtect.lockables.contains(src.getType())) {
            final Sign s = SignProtect.findBlockProtection(src);
            if (s != null && ProtectionData.of(s).isValid()) e.setCancelled(true);
        }
        final Block dst = getInventoryBlock(e.getDestination());
        if (dst != null && SignProtect.lockables.contains(dst.getType())) {
            final Sign s = SignProtect.findBlockProtection(dst);
            if (s != null && ProtectionData.of(s).isValid()) e.setCancelled(true);
        }
    }

    private @Nullable Block getInventoryBlock(final Inventory inventory) {
        return switch (inventory.getHolder()) {
            case BlockState bs -> bs.getBlock();
            case DoubleChest dc -> dc.getLeftSide() instanceof
                final BlockState bs ? bs.getBlock() : null;
            case null, default -> null;
        };
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
    public void onLecternTake(PlayerTakeLecternBookEvent e) {
        final Sign s = SignProtect.findBlockProtection(e.getLectern().getBlock());
        if (s != null) {
            final ProtectionData pd = ProtectionData.of(s);
            if (!pd.canUse(e.getPlayer())) {
                e.setCancelled(true);
                ScreenUtil.sendActionBarDirect(e.getPlayer(), "§eВы не можете взять книгу!");
                e.setCancelled(true);
            }
        }
    }


    //@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent e) {
        for (BlockState bs : e.getBlocks()) {
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


    private static boolean isProtected(final Block b) {
        if (SignProtect.SIGNS.contains(b.getType().asBlockType()))
            return ((Sign) b.getState())
                .getPersistentDataContainer().has(SignProtect.KEY);
        if (SignProtect.lockables.contains(b.getType()))
            return SignProtect.findBlockProtection(b) != null;
        return false;
    }

}
    

    
    
    
    
   
    
    
