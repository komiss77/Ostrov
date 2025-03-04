package ru.komiss77.listener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.google.common.collect.ArrayListMultimap;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.hook.WGhook;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.objects.IntHashMap;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;

//https://github.com/ds58/Panilla
//просто скинул сюда всё из двух мелких плагинов

public class ArcaimLst implements Listener {

    private static final String admin = "komiss77";
    private static final int ENT_DST = 100;

    public ArcaimLst() {
        BotManager.regSkin(admin);
    }

    public static final NamespacedKey key;
    public static final int MAX_CHUNK_PULSES_PER_SEC = 50;
    private static final IntHashMap<RC> redstoneChunkClocks;

    static {
        key = new NamespacedKey(Ostrov.instance, "redstoneclock");
        redstoneChunkClocks = new IntHashMap<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!redstoneChunkClocks.isEmpty()) {
                    redstoneChunkClocks.values().forEach(rc -> {
                        rc.second++; //надо считать внутренние секунды, илил если серв в лагах то таймштампы дают меньше секунд
                        if (rc.second == 6) { //каждые 5 сек. обновление счётчика для нового подсчёта
                            rc.second = 0;
                            rc.count = 0;
                        }
                    });
                    redstoneChunkClocks.entrySet().removeIf(entry -> entry.getValue().second > 6);
                }
            }
        }.runTaskTimer(Ostrov.instance, 7, 20);
    }


    // *************** RedstoneClockController END ***************
    @EventHandler(priority = EventPriority.LOWEST)
    public void redstone(BlockRedstoneEvent e) {
        if (e.getOldCurrent() == 0) { //CheckTPS.isTpsOK() &&
            check(e.getBlock());
        }
    }

    @EventHandler
    public void pistonExtend(BlockPistonExtendEvent e) {
        check(e.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.WARPED_SIGN) {
            Sign s = (Sign) e.getBlock().getState();
            if (s.getPersistentDataContainer().has(key)) {
                final Material mat = Material.matchMaterial(s
                    .getPersistentDataContainer().get(key, PersistentDataType.STRING));
                if (mat != null) {
                    e.getBlock().setType(mat);
                    e.setCancelled(true);
                }
            }
        }
    }

    private void check(final Block b) {
        int cLoc = LocUtil.cLoc(b.getLocation());
        RC chunkRc = redstoneChunkClocks.get(cLoc);

        if (chunkRc == null) {
            redstoneChunkClocks.put(cLoc, new RC());
        } else {
            chunkRc.count++;
            if (chunkRc.second == 5) { //один раз в 5 секунд подсчитываем среднюю импульсность
                if (chunkRc.count / 5 > MAX_CHUNK_PULSES_PER_SEC) { //в секунду в среднем импульсов больше лимита
                    chunkRc.count = 0;//Timer.redstoneChunkClocks.remove(cLoc); не удалять rc, только перезапуск счётчика!
                    Ostrov.log_warn("CHUNK RC REMOVE " + b.getType().name() + " at " + LocUtil.toString(b.getLocation()));
                    remove(b); //на 5-й секунде проредит механизмы
                }
            }
        }
    }

    private static void remove(final Block b) {
        if (Tag.ALL_SIGNS.isTagged(b.getType())) return; //уже могла поставиться в этом тике
        final String oldMat = b.getType().name();
        b.setType(Material.AIR);
        Ostrov.sync(() -> {
            b.setType(Material.WARPED_SIGN);
            final Sign sign = (Sign) b.getState();
            SignSide side = sign.getSide(Side.FRONT);
            side.line(0, Component.text("§4Чанк перегружен"));
            side.line(1, Component.text("§4механизмами."));
            side.line(2, Component.text("§6(Сломай табличку-"));
            side.line(3, Component.text("§6вернём предмет)"));
            side = sign.getSide(Side.BACK);
            side.line(0, Component.text("§4Чанк перегружен"));
            side.line(1, Component.text("§4механизмами."));
            side.line(2, Component.text("§6(Сломай табличку-"));
            side.line(3, Component.text("§6вернём предмет)"));
            sign.getPersistentDataContainer().set(key, PersistentDataType.STRING, oldMat);
//Ostrov.log_warn("setdata "+oldMat);
            sign.update(false, false);
        }, 1);

    }

    public static class RC {
        public int second, count;//, numberOfClock, status;
        //public RC() {
        // stamp = Timer.getTime();
        //}
    }
// *************** RedstoneClockController END ***************


   /* @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (e.getAction() == Action.PHYSICAL) return;

        switch (p.getGameMode()) {
            case SPECTATOR:
                if (p.getOpenInventory().getType() == InventoryType.CHEST) break;
                if (PM.getOplayer(p.getUniqueId()).setup != null) break;
                p.performCommand("menu");
                break;
            case CREATIVE, SURVIVAL: //давно убрал это ограничение - с ним на аркаиме просто никто не играет
                if (!isOutsideWG(p.getLocation())) break;
                if (ApiOstrov.isStaff(p)) break;
                e.getPlayer().sendMessage("§cСтроить можно только в приватах!");
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setUseItemInHand(Event.Result.DENY);
                break;
        }
    }*/

    //--------------------------------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent e) {
        if (e.getBlock().getType() == Material.LAVA || e.getBlock().getType() == Material.WATER) {
            BlockData bd = e.getBlock().getBlockData();
            if (bd instanceof Levelled lv) { //разливаться только с уменьшением (не давать столбы) и не расползаться в стороны по воздуху
                if ((lv.getLevel() == 0 && e.getToBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) ||
                    (lv.getLevel() != 0 && e.getToBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onlavaPlaceEntity(PlayerInteractAtEntityEvent e) {
        final ItemStack is = e.getPlayer().getInventory().getItem(e.getHand());//ItemInOffHand();
        switch (is.getType()) {
            case WATER_BUCKET:
                e.setCancelled(EntityUtil.group(e.getRightClicked().getType()) != EntityGroup.WATER_AMBIENT);
                break;
            case LAVA, LAVA_BUCKET, WATER, AXOLOTL_BUCKET, COD_BUCKET,
                 PUFFERFISH_BUCKET, SALMON_BUCKET, TADPOLE_BUCKET, TROPICAL_FISH_BUCKET:
                e.setCancelled(true);
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawn(final CreatureSpawnEvent e) {
        final LivingEntity ent = e.getEntity();
        switch (e.getEntityType()) {
            case ENDER_DRAGON:
                if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) ent.remove();
                break;
            case WITHER, WARDEN:
                if (!isOutsideWG(ent.getLocation())) break;
                final LivingEntity le = LocUtil.getClsChEnt(BVec.of(ent.getLocation()),
                    ENT_DST, ent.getClass(), ne -> ne.getEntityId() != ent.getEntityId());
                if (le == null) break;
                ent.remove();
                break;
            default:
                switch (e.getSpawnReason()) {
                    case DISPENSE_EGG, EGG, SPAWNER_EGG, SPAWNER:
                        if (isOutsideWG(ent.getLocation())) {
                            e.setCancelled(true);
                        }
                        break;
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent e) {
        if (Ostrov.wg) e.blockList().removeIf(block -> isOutsideWG(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreative(final InventoryCreativeEvent e) {
        final ItemStack cursor = e.getCursor();

        if (ItemUtil.isBlank(cursor, true)
            || ApiOstrov.isLocalBuilder(e.getWhoClicked(), false)
            || MenuItemsManager.isSpecItem(cursor)) return;

        final ItemMeta meta = cursor.getItemMeta();
        boolean modify = false;
        switch (meta) {
            case final PotionMeta pm:
                final List<PotionEffect> bad = new ArrayList<>();
                int i = 0;
                for (final PotionEffect effect : pm.getCustomEffects()) {
                    if (effect.getAmplifier() > 10 || effect.getDuration() > 9600 || i >= 8) { // 8мин*60*20 + лимит 8 эффектов
                        bad.add(effect.withAmplifier(10).withDuration(9600));
                        i++;
                        modify = true;
                    }
                }
                i = 0;
                for (final PotionEffect pe : bad) {
                    if (i < 8) pm.addCustomEffect(pe, true); //overwrite перекрывает плохой
                    else pm.removeCustomEffect(pe.getType());
                    i++;
                }
                break;
            case final EnchantmentStorageMeta pm:
                if (pm.hasStoredEnchants()) {
                    for (final Map.Entry<Enchantment, Integer> en : pm.getStoredEnchants().entrySet()) {
                        final Enchantment ench = en.getKey();
                        if (en.getValue() > ench.getMaxLevel()) {
                            pm.removeStoredEnchant(ench);
                            pm.addStoredEnchant(ench, ench.getMaxLevel(), true);
                            modify = true;
                        }
                    }
                }
                break;
            case final SpawnEggMeta pm:
                if (pm.getCustomSpawnedType() != null) {
                    pm.setCustomSpawnedType(null);
                    modify = true;
                }
                break;
            default:
                break;
        }

        if (meta.hasEnchants()) {
            for (final Map.Entry<Enchantment, Integer> en : meta.getEnchants().entrySet()) {
                final Enchantment ench = en.getKey();
                if (en.getValue() > ench.getMaxLevel()) {
                    meta.removeEnchant(ench);
                    meta.addEnchant(ench, ench.getMaxLevel(), true);
                    modify = true;
                }
            }
        }

        if (meta.hasAttributeModifiers()) {
            modify = true;
            meta.setAttributeModifiers(ArrayListMultimap.create());
        }

        if (modify) cursor.setItemMeta(meta);
        e.setCursor(cursor);
    }

    private static boolean isOutsideWG(final Location loc) {
        if (!Ostrov.wg) return false;
        final ApplicableRegionSet regs = WGhook.getRegionsOnLocation(loc);
        return regs.size() == 0;
    }

    @EventHandler
    public void onFirst(final LocalDataLoadEvent e) {
        final Oplayer op = e.getOplayer();
        if (op.firstJoin) {
            final Player p = e.getPlayer();
            p.setGameMode(GameMode.SURVIVAL);
            op.firstJoin = false;
            Ostrov.sync(() -> {
                if (!p.isValid() || !p.isOnline()) return;
                final Location loc = new Location(p.getWorld(), 130, 73, -281);
                p.teleport(loc);
                final Botter ab = BotManager.createBot(admin, p.getWorld(), new AdminExt(p));
                if (ab != null) {
                    ab.telespawn(null, loc);
                    ab.tab("", ChatLst.NIK_COLOR, " §7(§eСисАдмин§7)");
                    ab.tag("", ChatLst.NIK_COLOR, " §7(§eСисАдмин§7)");
                    final LivingEntity aent = ab.getEntity();
                    if (aent != null) aent.setGravity(false);
                    p.playSound(loc, Sound.ENTITY_WANDERING_TRADER_AMBIENT, 2f, 0.8f);
                    p.sendMessage(GM.getLogo().append(TCUtil.form(
                        ChatLst.NIK_COLOR + admin + " §7<i>»</i> О, привет, " + p.getName() + "! ты тут новичек?")));
                    Ostrov.sync(() -> {
                        if (!p.isValid() || !p.isOnline()) return;
                        p.playSound(loc, Sound.ENTITY_WANDERING_TRADER_TRADE, 2f, 0.8f);
                        p.sendMessage(GM.getLogo().append(TCUtil.form(
                            ChatLst.NIK_COLOR + admin + " §7<i>»</i> Я тут заскучал строить уже, может ты мне сможешь помочь?")));
                        Ostrov.sync(() -> {
                            if (!p.isValid() || !p.isOnline()) return;
                            p.playSound(loc, Sound.ENTITY_WANDERING_TRADER_YES, 2f, 0.8f);
                            p.sendMessage(GM.getLogo().append(TCUtil.form(
                                ChatLst.NIK_COLOR + admin + " §7<i>»</i> Вот! Бери креатив, и иди построй что пожелаешь в этом мире!")));
                            p.sendMessage("Ваш игроаой режим был изменен на Творческий режим");
                            p.setGameMode(GameMode.CREATIVE);
                            Ostrov.sync(() -> {
                                if (!p.isValid() || !p.isOnline()) return;
                                p.teleport(p.getWorld().getSpawnLocation());
                                ab.remove();
                            }, 80);
                        }, 120);
                    }, 80);
                }
            }, 80);
        }
    }


    private static class AdminExt implements Botter.Extent {

        final WeakReference<Player> trf;

        private AdminExt(final Player tgt) {
            this.trf = new WeakReference<>(tgt);
        }

        public void create(Botter bt) {}
        public void remove(Botter bt) {}

        public void teleport(Botter bt, LivingEntity le) {}
        public void spawn(Botter bt, @Nullable LivingEntity le) {}
        public void hide(Botter bt, @Nullable LivingEntity le) {}

        public void click(Botter bt, PlayerInteractAtEntityEvent e) {}

        public void death(Botter bt, EntityDeathEvent e) {
            Botter.Extent.super.death(bt, e);
        }

        @Override
        public void damage(Botter bt, EntityDamageEvent e) {
            e.setCancelled(true);
            e.setDamage(0d);
        }

        @Override
        public void pickup(Botter bt, Location loc) {}

        @Override
        public void drop(Botter bt, Location loc) {}

        @Override
        public Goal<Mob> goal(Botter bt, Mob mb) {
            return new AdminGoal(bt, trf);
        }
    }

    private static class AdminGoal implements Goal<Mob> {

        private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Ostrov.instance, "bot"));

        private final Botter bot;
        private final WeakReference<Player> trf;

        private int tick;

        public AdminGoal(final Botter bot, final WeakReference<Player> trf) {
            this.trf = trf;
            this.bot = bot;
            this.tick = 0;
        }

        @Override
        public boolean shouldActivate() {
            return true;
        }

        @Override
        public boolean shouldStayActive() {
            return true;
        }

        @Override
        public void stop() {
            bot.remove();
        }

        @Override
        public void tick() {
            final Mob rplc = (Mob) bot.getEntity();
            if (rplc == null || !rplc.isValid()) {
                bot.remove();
                return;
            }

            final Player tgt = trf.get();
            if (tgt == null || !tgt.isValid() || !tgt.isOnline()) {
                bot.remove();
                return;
            }
            //Bukkit.broadcast(Component.text("le-" + rplc.getName()));
            final Location loc = rplc.getLocation();

            final Vector vc;
            if ((tick++ & 7) == 0 && Ostrov.random.nextBoolean()) {
                vc = tgt.getLocation().add(Ostrov.random.nextDouble() - 0.5d,
                        Ostrov.random.nextDouble() - 0.5d, Ostrov.random.nextDouble() - 0.5d)
                    .subtract(loc).toVector();
                if (vc.lengthSquared() < 10) {
                    bot.swingHand(true);
                    tgt.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 1f);
                }
            } else vc = tgt.getLocation().subtract(loc).toVector();
            bot.move(loc, vc);
        }

        @Override
        public @NotNull GoalKey<Mob> getKey() {
            return key;
        }

        @Override
        public @NotNull EnumSet<GoalType> getTypes() {
            return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
        }
    }
}
