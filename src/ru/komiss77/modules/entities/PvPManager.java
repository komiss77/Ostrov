package ru.komiss77.modules.entities;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent;
import org.bukkit.*;
import org.bukkit.block.Container;
import org.bukkit.command.CommandException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.*;
import ru.komiss77.events.PlayerPVPEnterEvent;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.version.Nms;

public class PvPManager implements Initiable {

    public static OConfig config;

    private static int battle_time;  //после первого удара - заносим обоих в режим боя
    public static int no_damage_on_tp;
    private static final EnumMap<PvpFlag, Boolean> flags;
    private static final Set<PotionEffectType> potion_pvp_type;

    private static Listener damageListener;
    private static Listener flyListener;
    private static Listener elytraListener;
    private static Listener cmdListener;
    private static Listener advancedListener;

    private static final String PVP_NOTIFY = "§cТы в режиме боя!";
    private static final PotionEffect spd = new PotionEffect(PotionEffectType.HASTE, 2, 255, true, false, false);
    private static final PotionEffect slw = new PotionEffect(PotionEffectType.MINING_FATIGUE, 32, 255, true, false, false);
    private static final HashSet<Integer> noClds = new HashSet<>();

    public static final Set<ItemType> MELEE = Set.of(ItemType.DIAMOND_SWORD,
        ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD, ItemType.WOODEN_SWORD,
        ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD, ItemType.TRIDENT);
    public static final Set<ItemType> AXES = Set.of(ItemType.NETHERITE_AXE,
        ItemType.STONE_AXE, ItemType.WOODEN_AXE, ItemType.IRON_AXE,
        ItemType.GOLDEN_AXE, ItemType.DIAMOND_AXE);
    public static final Set<ItemType> CAN_PARRY = Set.of(ItemType.DIAMOND_SWORD,
        ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD, ItemType.WOODEN_SWORD,
        ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD, ItemType.NETHERITE_AXE,
        ItemType.STONE_AXE, ItemType.WOODEN_AXE, ItemType.IRON_AXE,
        ItemType.GOLDEN_AXE, ItemType.DIAMOND_AXE);

    public static final int DHIT_CLD = 4;
    public static final int BLCK_CLD = 0;

    /*public static final double TRIDENT_DMG = getDefDmg(ItemType.TRIDENT);
    public static double getDefDmg(final ItemType it) {
        double d = 1d;
        for (final AttributeModifier mod : it.getDefaultAttributeModifiers()
            .get(Attribute.ATTACK_DAMAGE)) {
            d += switch (mod.getOperation()) {
                case ADD_NUMBER -> mod.getAmount();
                case ADD_SCALAR -> mod.getAmount() * d;
                case MULTIPLY_SCALAR_1 -> (mod.getAmount() + 1d) * d;
            };
        }
        return d;
    }*/

    static {
        flags = new EnumMap<>(PvpFlag.class);
        for (final PvpFlag f : PvpFlag.values()) {
            flags.put(f, false);
        }

        potion_pvp_type = Set.of(
            PotionEffectType.POISON,
            PotionEffectType.BLINDNESS,
            PotionEffectType.NAUSEA,
            PotionEffectType.INSTANT_DAMAGE,
            PotionEffectType.HUNGER
        );
    }

    public enum PvpFlag {
        enable, allow_pvp_command, antirelog, drop_inv_inbattle, display_pvp_tag, block_fly_on_pvp_mode, advanced_pvp, disable_self_hit,
        block_elytra_on_pvp_mode, block_command_on_pvp_mode, disable_creative_attack_to_mobs, disable_creative_attack_to_player
    }

    public PvPManager() {
        loadConfig(); //загружаем только один раз при старте, потом меняется через ГУИ
        reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        Ostrov.log_ok("§2PvP включено!");
        init();
    }

    @Override
    public void onDisable() {
        Ostrov.log_ok("§6PvP выключено!");
    }

    private static void init() {
        //HandlerList.unregisterAll(pvpCmd);
        if (damageListener != null) {
            HandlerList.unregisterAll(damageListener);
            damageListener = null;
        }
        if (flyListener != null) {
            HandlerList.unregisterAll(flyListener);
            flyListener = null;
        }
        if (elytraListener != null) {
            HandlerList.unregisterAll(elytraListener);
            elytraListener = null;
        }
        if (cmdListener != null) {
            HandlerList.unregisterAll(cmdListener);
            cmdListener = null;
        }
        if (advancedListener != null) {
            HandlerList.unregisterAll(advancedListener);
            advancedListener = null;
        }
        //PlayerDeathEvent слушаем всегда!!!
        //Bukkit.getPluginManager().registerEvents(pvpCmd, Ostrov.getInstance());

        if (!flags.get(PvpFlag.enable)) { //только игнорим мелких слушателей
            Ostrov.log_ok("§eМодуль ПВП неактивен");
            return;
        }

        final boolean advanced = flags.get(PvpFlag.advanced_pvp);
        if (battle_time > 0 || no_damage_on_tp > 0 || flags.get(PvpFlag.disable_creative_attack_to_mobs)
            || flags.get(PvpFlag.disable_creative_attack_to_player) || advanced) {

            damageListener = new Listener() {

                @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
                public void PlayerDeath(final PlayerDeathEvent e) {
                    if (e.getEntity().getType() != EntityType.PLAYER) {
                        return;
                    }
                    final Player p = e.getEntity();
                    final Oplayer op = PM.getOplayer(p.getUniqueId());
                    if (op == null) {
                        return;
                    }
//                    op.last_death = p.getLocation();
// вайвай, зачем убрал?? ... у игрока сохраняется позиция смерти, 1.19+ p.getLastDeathLocation()
                    if (flags.get(PvpFlag.drop_inv_inbattle) && op.pvp_time > 0) {            //дроп инвентаря
                        if (p.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) { //если сохранение вкл, то дроп в эвенте не образуется, нужно кидать вручную
                            for (final ItemStack is : p.getInventory().getContents()) {
                                if (!ItemUtil.isBlank(is, false)) {
                                    if (MenuItemsManager.isSpecItem(is)) {//не лутать менюшки!
                                        continue;
                                    }
                                    p.getWorld().dropItemNaturally(p.getLocation(), is);
                                }
                            }
                            p.getInventory().clear();
                            p.updateInventory();

                        } else {
                            for (int i = e.getDrops().size() - 1; i >= 0; i--) {
                                if (MenuItemsManager.isSpecItem(e.getDrops().get(i))) {  //отменить лут менюшек
                                    e.getDrops().remove(i);
                                }
                            }
                            //ничего не надо, выпадет само!
                        }

                        p.sendMessage("§c" + Lang.t(p, "Ваши вещи достались победителю!"));
                    }

                    pvpEndFor(op, p);
                }

                @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
                public void EntityDamageByEntityEvent(final EntityDamageByEntityEvent e) {
                    if (!e.getEntityType().isAlive() || e.getEntityType() == EntityType.ARMOR_STAND) {
                        return;   //не обрабатывать урон рамкам, опыту и провее
                    }            //System.out.println("EDBE: cause="+e.getCause()+" entity="+e.getEntity()+" damager="+e.getDamager());

                    switch (e.getCause()) {
                        case DRAGON_BREATH:
                        case ENTITY_ATTACK:
                        case ENTITY_EXPLOSION:
                        case ENTITY_SWEEP_ATTACK:
                        case MAGIC:
                        case PROJECTILE:
                        case CRAMMING:
                        case SUICIDE:
                            break;
                        default:
                            return;
                    }

                    if (Cfg.disable_damage || e.getDamage() == 0d) {
                        e.setCancelled(true);
                        return;
                    }

                    final LivingEntity damager = EntityUtil.getDamager(e, false);
                    if (damager == null) return;

                    if (damager.getEntityId() == e.getEntity().getEntityId() && flags.get(PvpFlag.disable_self_hit)) {
                        e.setCancelled(true);
                        return;
                    }

                    if (battle_time > 0 && disablePvpDamage(damager, e.getEntity(), e.getCause())) {
                        e.setCancelled(true);
                        return;
                    }

                    if (advanced) {
                        switch (e.getDamager()) {
                            case Projectile pr://Ostrov.sync(() -> tgt.setNoDamageTicks(-1), 1);
                                if (e.getDamager() instanceof final Trident tr) {
                                    double dmg = tr.getDamage();
                                    final ItemStack tit = tr.getItemStack();
                                    final int lvl = tit.getEnchantmentLevel(Enchantment.IMPALING);
                                    if (lvl != 0 && (pr.getWorld().hasStorm()
                                        || pr.getLocation().getBlock().isLiquid())) {
                                        dmg += lvl * 2.5d;
                                    }
                                    e.setDamage(dmg);
                                }
                                break;
                            case LivingEntity le://Ostrov.sync(() -> tgt.setNoDamageTicks(-1), 1);
                                if (le.getEquipment() != null) {
                                    final ItemStack mhd = le.getEquipment().getItemInMainHand();
                                    final int lvl = mhd.getEnchantmentLevel(Enchantment.IMPALING);
                                    if (lvl != 0 && (le.getWorld().hasStorm()
                                        || le.getLocation().getBlock().isLiquid())) {
                                        e.setDamage(lvl * 2.5d + e.getDamage());
                                    }
                                }
                                break;
                            default:
                                break;
                        }

                        final ItemStack tgtHand;
                        final LivingEntity target = (LivingEntity) e.getEntity();
                        if (target.getType() == EntityType.PLAYER) {//# v P
                            final Player tgtPl = (Player) target;
                            if (damager instanceof final Player dmgrPl) {//P v P
                                tgtHand = tgtPl.getInventory().getItemInMainHand();
                                if (tgtPl.hasCooldown(tgtHand) && CAN_PARRY.contains(tgtHand.getType().asItemType())) {
                                    tgtPl.getWorld().playSound(tgtPl.getLocation(), Sound.BLOCK_CHAIN_FALL, 1f, 0.8f);
                                    tgtPl.setCooldown(tgtHand, 0);
                                    if (!e.isCritical()) {
                                        e.setDamage(0d);
                                        e.setCancelled(true);
                                        tgtPl.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                                        noClds.add(tgtPl.getEntityId());
                                        tgtPl.swingMainHand();
                                        tgtPl.attack(dmgrPl);
                                    }
                                    return;
                                }

                                final PlayerInventory inv = dmgrPl.getInventory();
                                final ItemStack damagerHand = inv.getItemInMainHand();
                                if (isParying(dmgrPl)) {
                                    e.setDamage(0d);
                                    e.setCancelled(true);
                                    return;
                                }

                                if (dmgrPl.getAttackCooldown() == 1f && dmgrPl.isSprinting()
                                    && MELEE.contains(damagerHand.getType().asItemType())) {
                                    final ItemStack ofh = inv.getItemInOffHand();
                                    if (ItemUtil.isBlank(ofh, false)) {
                                        final ItemStack shld = ItemType.SHIELD.createItemStack();
                                        if (tgtPl.isBlocking() && !tgtPl.hasCooldown(shld)) {
                                            tgtPl.setCooldown(shld, 40);
                                            tgtPl.playEffect(EntityEffect.SHIELD_BREAK);
                                            return;
//                                                final PlayerInventory ti = tpl.getInventory();
//                                                final ItemStack ohs = ti.getItemInOffHand();
//                                                if (ohs != null && ohs.getType() == ItemType.SHIELD) {
//                                                    VM.getNmsServer().sendFakeEquip(tpl, 40, ItemUtils.air);
//                                                    Ostrov.sync(() -> ti.setItemInOffHand(ti.getItemInOffHand()), 4);
//                                                }
                                        }
                                    } else if (MELEE.contains(ofh.getType().asItemType())) {
                                        Ostrov.sync(() -> {
                                            final ItemStack noh = inv.getItemInOffHand();
                                            if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                                                final ItemStack it = inv.getItemInMainHand().clone();
                                                target.setNoDamageTicks(-1);
                                                dmgrPl.addPotionEffect(spd);
                                                inv.setItemInMainHand(ofh);
                                                dmgrPl.setSprinting(false);
                                                dmgrPl.attack(target);
                                                inv.setItemInOffHand(inv.getItemInMainHand());
                                                inv.setItemInMainHand(it);
                                                dmgrPl.removePotionEffect(spd.getType());
                                                Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
                                            }
                                        }, DHIT_CLD);
                                    }
                                }

                                Ostrov.sync(() -> EntityUtil.indicate(target.getEyeLocation(), (e.isCritical() ? "<red>✘" : "<gold>")
                                    + StringUtil.toSigFigs(e.getFinalDamage(), (byte) 1), dmgrPl), 1);
                            } else {
                                final Botter dbe = Cfg.bots ? BotManager.getBot(damager.getEntityId()) : null;
                                if (dbe != null) {//B v P
                                    tgtHand = tgtPl.getInventory().getItemInMainHand();
                                    if (tgtPl.hasCooldown(tgtHand) && CAN_PARRY.contains(tgtHand.getType().asItemType())) {
                                        tgtPl.getWorld().playSound(tgtPl.getLocation(), Sound.BLOCK_CHAIN_FALL, 1f, 0.8f);
                                        tgtPl.setCooldown(tgtHand, 0);
                                        if (!e.isCritical()) {
                                            e.setDamage(0d);
                                            e.setCancelled(true);
                                            tgtPl.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                                            noClds.add(tgtPl.getEntityId());
                                            tgtPl.swingMainHand();
                                            tgtPl.attack(damager);
                                        }
                                        return;
                                    }

                                    final ItemStack hnd = dbe.item(EquipmentSlot.HAND);
                                    if (dbe.isParrying(damager)) {
                                        e.setDamage(0d);
                                        e.setCancelled(true);
                                        return;
                                    }

                                    if (hnd != null && CAN_PARRY.contains(hnd.getType().asItemType())
                                        && damager.getLocation().distanceSquared(target.getLocation()) < Botter.DHIT_DST_SQ) {
                                        final ItemStack ofh = dbe.item(EquipmentSlot.OFF_HAND);
                                        if (ItemUtil.isBlank(ofh, false)) {
                                            if (tgtPl.isBlocking()) {
                                                tgtPl.setCooldown(ItemType.SHIELD.createItemStack(), 40);
                                                tgtPl.playEffect(EntityEffect.SHIELD_BREAK);
                                            }
                                        } else if (MELEE.contains(ofh.getType().asItemType())
                                            && MELEE.contains(hnd.getType()) && dbe.useTicks(damager) == 0) {
                                            dbe.startUse(damager, EquipmentSlot.OFF_HAND);
                                            Ostrov.sync(() -> {
                                                final LivingEntity ndp = dbe.getEntity();
                                                if (ndp != null && target.isValid() && dbe.usedHand() == EquipmentSlot.OFF_HAND) {
                                                    target.setNoDamageTicks(-1);
                                                    dbe.attack(damager, target, true);
                                                    dbe.stopUse(damager);
                                                }
                                            }, DHIT_CLD);
                                        }
                                    }
                                }
                            }
                        } else {
                            final Botter tbe = Cfg.bots ? BotManager.getBot(target.getEntityId()) : null;
                            if (tbe != null) {// # v B
                                if (damager instanceof final Player dmgrPl) {// P v B
                                    tgtHand = tbe.item(EquipmentSlot.HAND);
                                    if (tgtHand != null) {
                                        final Material mt = tgtHand.getType();
                                        if (tbe.isParrying(target) && MELEE.contains(mt.asItemType())) {
                                            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CHAIN_FALL, 1f, 0.8f);
                                            tbe.parrying(target, false);
                                            if (!e.isCritical()) {
                                                e.setDamage(0d);
                                                e.setCancelled(true);
                                                tbe.attack(target, dmgrPl, false);
                                            }
                                            return;
                                        }
                                    }

                                    final PlayerInventory inv = dmgrPl.getInventory();
                                    final ItemStack damagerHand = inv.getItemInMainHand();
                                    if (isParying(dmgrPl)) {
                                        e.setDamage(0d);
                                        e.setCancelled(true);
                                        return;
                                    }

                                    final ItemType handType = damagerHand.getType().asItemType();
                                    final boolean blocking = tbe.isBlocking(target);
                                    if (blocking && AXES.contains(handType)) {
                                        e.setDamage(0d);
                                        e.setCancelled(true);
                                        tbe.stopUse(target);
                                        target.getWorld().playSound(target.getLocation(),
                                            Sound.ITEM_SHIELD_BREAK, 1f, 1f);
                                        return;
                                    }

                                    if (dmgrPl.getAttackCooldown() == 1f
                                        && dmgrPl.isSprinting() && CAN_PARRY.contains(handType)) {
                                        final ItemStack ofh = inv.getItemInOffHand();
                                        if (ItemUtil.isBlank(ofh, false)) {
                                            if (blocking) {
                                                e.setDamage(0d);
                                                e.setCancelled(true);
                                                tbe.stopUse(target);
                                                target.getWorld().playSound(target.getLocation(),
                                                    Sound.ITEM_SHIELD_BREAK, 1f, 1f);
                                                return;
                                            }
                                        } else if (MELEE.contains(ofh.getType().asItemType())
                                            && MELEE.contains(handType) && !blocking) {
                                            Ostrov.sync(() -> {
                                                final ItemStack noh = inv.getItemInOffHand();
                                                if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                                                    final ItemStack it = inv.getItemInMainHand().clone();
                                                    target.setNoDamageTicks(-1);
                                                    dmgrPl.addPotionEffect(spd);
                                                    inv.setItemInMainHand(ofh);
                                                    dmgrPl.setSprinting(false);
                                                    dmgrPl.attack(target);
                                                    inv.setItemInOffHand(inv.getItemInMainHand());
                                                    inv.setItemInMainHand(it);
                                                    dmgrPl.removePotionEffect(spd.getType());
                                                    Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
                                                }
                                            }, DHIT_CLD);
                                        }
                                    }

                                    if (blocking) {
                                        e.setDamage(0d);
                                        e.setCancelled(true);
                                        target.getWorld().playSound(target.getLocation(),
                                            Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                                    }

                                    Ostrov.sync(() -> EntityUtil.indicate(target.getEyeLocation(), (e.isCritical() ? "<red>✘" : "<gold>")
                                        + StringUtil.toSigFigs(e.getFinalDamage(), (byte) 1), dmgrPl), 1);
                                } else {
                                    final Botter dbe = Cfg.bots ? BotManager.getBot(damager.getEntityId()) : null;
                                    if (dbe != null) {// B v B
                                        tgtHand = tbe.item(EquipmentSlot.HAND);
                                        if (tgtHand != null) {
                                            final Material mt = tgtHand.getType();
                                            if (tbe.isParrying(target) && MELEE.contains(mt.asItemType())) {
                                                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CHAIN_FALL, 1f, 0.8f);
                                                tbe.parrying(target, false);
                                                if (!e.isCritical()) {
                                                    e.setDamage(0d);
                                                    e.setCancelled(true);
                                                    tbe.attack(target, damager, false);
                                                }
                                                return;
                                            }
                                        }

                                        final ItemStack hnd = dbe.item(EquipmentSlot.HAND);
                                        if (dbe.isParrying(damager)) {
                                            e.setDamage(0d);
                                            e.setCancelled(true);
                                            return;
                                        }

                                        final ItemType handType = hnd == null ?
                                            ItemType.AIR : hnd.getType().asItemType();
                                        final boolean blocking = tbe.isBlocking(target);
                                        if (blocking && AXES.contains(handType)) {
                                            e.setDamage(0d);
                                            e.setCancelled(true);
                                            tbe.stopUse(target);
                                            target.getWorld().playSound(target.getLocation(),
                                                Sound.ITEM_SHIELD_BREAK, 1f, 1f);
                                            return;
                                        }

                                        if (CAN_PARRY.contains(handType) && damager.getLocation()
                                            .distanceSquared(target.getLocation()) < Botter.DHIT_DST_SQ) {
                                            final ItemStack ofh = dbe.item(EquipmentSlot.OFF_HAND);
                                            if (ItemUtil.isBlank(ofh, false)) {
                                                if (blocking) {
                                                    e.setDamage(0d);
                                                    e.setCancelled(true);
                                                    tbe.stopUse(target);
                                                    target.getWorld().playSound(target.getLocation(),
                                                        Sound.ITEM_SHIELD_BREAK, 1f, 0.8f);
                                                    return;
                                                }
                                            } else if (MELEE.contains(ofh.getType().asItemType()) && !blocking
                                                && MELEE.contains(dbe.usedType()) && dbe.useTicks(damager) == 0) {
                                                dbe.startUse(damager, EquipmentSlot.OFF_HAND);
                                                Ostrov.sync(() -> {
                                                    final LivingEntity ndp = dbe.getEntity();
                                                    if (ndp != null && target.isValid() && dbe.usedHand() == EquipmentSlot.OFF_HAND) {
                                                        target.setNoDamageTicks(-1);
                                                        dbe.attack(damager, target, true);
                                                        dbe.stopUse(damager);
                                                    }
                                                }, DHIT_CLD);
                                            }
                                        }

                                        if (blocking) {
                                            e.setDamage(0d);
                                            e.setCancelled(true);
                                            target.getWorld().playSound(target.getLocation(),
                                                Sound.ITEM_SHIELD_BLOCK, 1f, 0.8f);
                                        }
                                    }
                                }
                            } else if (target instanceof Mob || target instanceof ArmorStand) {// # v M
                                final ItemStack shd = target.getEquipment().getItemInOffHand();
                                if (ItemUtil.is(shd, ItemType.SHIELD) && Ostrov.random.nextBoolean()) {
                                    target.getWorld().playSound(target.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                                    e.setCancelled(true);
                                    e.setDamage(0);
                                    return;
                                }

                                if (damager instanceof final Player dmgrPl) {// P v M
                                    final PlayerInventory inv = dmgrPl.getInventory();
                                    final ItemStack hnd = inv.getItemInMainHand();
                                    if (isParying(dmgrPl)) {
                                        e.setDamage(0d);
                                        e.setCancelled(true);
                                        return;
                                    }

                                    if (dmgrPl.getAttackCooldown() == 1f && dmgrPl.isSprinting()
                                        && MELEE.contains(hnd.getType().asItemType())) {
                                        final ItemStack ofh = inv.getItemInOffHand();
                                        if (!ItemUtil.isBlank(ofh, false)
                                            && MELEE.contains(ofh.getType().asItemType())) {
                                            Ostrov.sync(() -> {
                                                final ItemStack noh = inv.getItemInOffHand();
                                                if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                                                    final ItemStack it = inv.getItemInMainHand().clone();
                                                    target.setNoDamageTicks(-1);
                                                    dmgrPl.addPotionEffect(spd);
                                                    inv.setItemInMainHand(ofh);
                                                    dmgrPl.setSprinting(false);
                                                    dmgrPl.attack(target);
                                                    inv.setItemInOffHand(inv.getItemInMainHand());
                                                    inv.setItemInMainHand(it);
                                                    dmgrPl.removePotionEffect(spd.getType());
                                                    Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
                                                }
                                            }, DHIT_CLD);
                                        }
                                    }

                                    Ostrov.sync(() -> EntityUtil.indicate(target.getEyeLocation(), (e.isCritical() ? "<red>✘" : "<gold>")
                                        + StringUtil.toSigFigs(e.getFinalDamage(), (byte) 1), dmgrPl), 1);
                                } else {
                                    final Botter dbe = Cfg.bots ? BotManager.getBot(damager.getEntityId()) : null;
                                    if (dbe != null) {// B v M
                                        final ItemStack hnd = dbe.item(EquipmentSlot.HAND);
                                        if (dbe.isParrying(damager)) {
                                            e.setDamage(0d);
                                            e.setCancelled(true);
                                            return;
                                        }

                                        if (hnd != null && MELEE.contains(hnd.getType().asItemType())
                                            && damager.getLocation().distanceSquared(target.getLocation()) < Botter.DHIT_DST_SQ) {
                                            final ItemStack ofh = dbe.item(EquipmentSlot.OFF_HAND);
                                            if (ofh != null && dbe.useTicks(damager) == 0
                                                && MELEE.contains(ofh.getType().asItemType())) {
                                                dbe.startUse(damager, EquipmentSlot.OFF_HAND);
                                                Ostrov.sync(() -> {
                                                    final LivingEntity ndp = dbe.getEntity();
                                                    if (ndp != null && target.isValid() && dbe.usedHand() == EquipmentSlot.OFF_HAND) {
                                                        target.setNoDamageTicks(-1);
                                                        dbe.attack(damager, target, true);
                                                        dbe.stopUse(damager);
                                                    }
                                                }, DHIT_CLD);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                private boolean isParying(final Player dp) {
                    final PotionEffect pre = dp.getPotionEffect(PotionEffectType.MINING_FATIGUE);
                    return pre != null && pre.getAmplifier() == slw.getAmplifier();
                }

                @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
                public static void onPotionSplash(PotionSplashEvent e) {
                    if (e.getAffectedEntities().isEmpty() || !(e.getPotion().getShooter() instanceof Player)) {
                        return;
                    }

                    e.getPotion().getEffects().stream().forEach((effect) -> {
                        if (potion_pvp_type.contains(effect.getType())) {
                            e.getAffectedEntities().stream().forEach((target) -> {
                                if (target.getType().isAlive() && disablePvpDamage((Entity) e.getPotion().getShooter(), target, EntityDamageEvent.DamageCause.MAGIC)) {
                                    e.setIntensity(target, 0);
                                }
                            });
                        }
                    });
                }

                @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
                public void onCld(final PlayerAttackEntityCooldownResetEvent e) {
                    e.setCancelled(noClds.remove(e.getPlayer().getEntityId()));
                }

                @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
                public void onJoin(final PlayerJoinEvent e) {
                    e.getPlayer().setShieldBlockingDelay(BLCK_CLD);
                }

            };
            Bukkit.getPluginManager().registerEvents(damageListener, Ostrov.instance);
        }

        if (flags.get(PvpFlag.block_fly_on_pvp_mode)) {
            flyListener = new Listener() {
                @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
                public void onFly(PlayerToggleFlightEvent e) {
                    // if ( e.getPlayer().isOp() ) return;
                    //System.err.println(">>>>
                    final Player p = e.getPlayer();
                    if (battle_time > 1 && PM.inBattle(p.getName())
                        && p.getAllowFlight() && p.isFlying()) {
                        p.setFlying(false);
                        p.setAllowFlight(false);
                        ScreenUtil.sendActionBarDirect(p, PVP_NOTIFY);
                        e.setCancelled(true);
                    }
                }
            };
            Bukkit.getPluginManager().registerEvents(flyListener, Ostrov.instance);
        }

        if (Boolean.TRUE.equals(flags.get(PvpFlag.block_elytra_on_pvp_mode))) {
            elytraListener = new Listener() {
                @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
                public void onElytra(EntityToggleGlideEvent e) {
                    if (!e.isGliding() || e.getEntity().getType() != EntityType.PLAYER) {
                        return;
                    }
                    //System.err.println(">>>>>>>>>>> 2");  
                    final Player p = (Player) e.getEntity();
                    if (battle_time > 1 && PM.inBattle(p.getName())) {
                        ScreenUtil.sendActionBarDirect(p, PVP_NOTIFY);
                        e.setCancelled(true);
                    }
                }
            };
            Bukkit.getPluginManager().registerEvents(elytraListener, Ostrov.instance);
        }

        if (Boolean.TRUE.equals(flags.get(PvpFlag.block_command_on_pvp_mode))) {
            cmdListener = new Listener() {
                @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
                public void Command(PlayerCommandPreprocessEvent e) throws CommandException {
                    final String[] args = e.getMessage().replaceFirst("/", "").split(" ");
                    final String cmd = args[0].toLowerCase();
                    //final String arg0 = args.length>=2 ? args[1].toLowerCase() : "";
                    //Ostrov.log_warn("cmd="+cmd+", arg0="+arg0);
                    switch (cmd) {
                        case "server", "serv", "hub" -> {
                            return;
                        }
                    }
                    final Player p = e.getPlayer();
                    final Oplayer op = PM.getOplayer(p);

                    if (PvPManager.battle_time > 1 && op.pvp_time > 0 && !ApiOstrov.isLocalBuilder(p)) {
                        p.sendMessage("§c" + Lang.t(p, "Режим боя - команды заблокированы! Осталось ") + PM.getOplayer(p.getUniqueId()).pvp_time + Lang.t(p, " сек."));
                        e.setCancelled(true);
                    }
                }
            };
            Bukkit.getPluginManager().registerEvents(cmdListener, Ostrov.instance);
        }

        if (advanced) {
            Ostrov.log_ok("§6Активно улучшенное ПВП!");
            advancedListener = new Listener() {

                @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
                public void onIntr(final PlayerInteractEvent e) {
                    final InventoryView iv = e.getPlayer().getOpenInventory();
                    switch (iv.getType()) {
                        case CRAFTING, CREATIVE, PLAYER: break;
                        default:
                            e.setCancelled(true);
                            e.setUseInteractedBlock(Event.Result.DENY);
                            e.setUseItemInHand(Event.Result.DENY);
                            return;
                    }

                    switch (e.getAction()) {
                        case RIGHT_CLICK_BLOCK:
                            if (e.getClickedBlock().getState() instanceof Container) break;
                        case RIGHT_CLICK_AIR:
                            final Player p = e.getPlayer();
                            final ItemStack it = e.getItem();
                            if (!ItemUtil.isBlank(it, false)) {
                                if (e.getHand() == EquipmentSlot.HAND && !p.hasCooldown(it)
                                    && CAN_PARRY.contains(it.getType().asItemType()) && p.getAttackCooldown() == 1f) {
                                    final ItemStack ofh = p.getInventory().getItemInOffHand();
                                    if (ItemUtil.isBlank(ofh, false)) {
                                        p.getWorld().playSound(p.getEyeLocation(), Sound.BLOCK_AMETHYST_CLUSTER_PLACE, 1f, 0.6f);
                                        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                                            p.getLocation().add(0d, 1.2d, 0d), 24, 0.4d, 0.5d, 0.4d, -0.25d);
                                        p.addPotionEffect(slw);
                                        p.setCooldown(it, 36);
                                        p.getInventory().setItemInMainHand(ItemUtil.air);
                                        p.getInventory().setItemInMainHand(it);
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }

                @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
                public void onHit(final ProjectileHitEvent e) {
                    if (e.getHitEntity() instanceof final Player pl) {
                        final ItemStack hnd = pl.getInventory().getItemInMainHand();
                        if (pl.hasCooldown(hnd) && CAN_PARRY.contains(hnd.getType().asItemType())) {
                            e.getEntity().setVelocity(e.getEntity().getVelocity().multiply(-0.6d));
                            pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_CHAIN_FALL, 1f, 0.8f);
                            pl.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                            pl.setCooldown(hnd, 0);
                            pl.swingMainHand();
                            e.setCancelled(true);
                        }
                    }
                }

                @EventHandler
                public void onRes(final EntityResurrectEvent e) {
                    if (!e.isCancelled() || e.getEntityType() != EntityType.PLAYER) {
                        return;
                    }
                    final Player p = (Player) e.getEntity();
                    final PlayerInventory pi = p.getInventory();
                    final int tsl = pi.first(Material.TOTEM_OF_UNDYING);
                    if (tsl != -1) {
                        pi.getItem(tsl).subtract();
                        ApiOstrov.addCustomStat((Player) e.getEntity(), "DA_ttm", 1);
//            			final ItemStack it = pi.getItemInOffHand();
//            			it.setAmount(it.getAmount() + 1);
                        e.setCancelled(false);
                    }
                }
            };
            Bukkit.getPluginManager().registerEvents(advancedListener, Ostrov.instance);
        }

        Ostrov.log_ok("§2Модуль ПВП активен!");

    }

    private static boolean disablePvpDamage(final Entity atackEntity, final Entity targetEntity, final EntityDamageEvent.DamageCause cause) {
//System.out.println("pvp attack_entity="+attack_entity+" type="+"   target_entity="+target_entity+" type=");        

        Player damager = null;
        Player target = null;

        final Oplayer damagerOp = PM.getOplayer(atackEntity.getUniqueId());
        if (damagerOp != null) {//if (atackEntity.getType() == EntityType.PLAYER && damagerOp != null) { - раз есть Oplayer, значит точно игрок
            damager = (Player) atackEntity;
        }

        final Oplayer targetOp = PM.getOplayer(targetEntity.getUniqueId());
        if (targetOp != null) {//if (targetEntity.getType() == EntityType.PLAYER && targetOp != null) { - раз есть Oplayer, значит точно игрок
            target = (Player) targetEntity;
        }

        if (damager == null && target == null) {
            return false; //если ни один не игрок, пропускаем
        }

        if (target != null && targetOp != null && target.getNoDamageTicks() > 20) { //у жертвы иммунитет
            final int noDamageTicks = target.getNoDamageTicks() / 20;
            ScreenUtil.sendActionBarDirect(target, Lang.t(target, "§aИммунитет к повреждениям  - осталось §f") + noDamageTicks + Lang.t(target, " §a сек.!"));
            if (damager != null) {
                ScreenUtil.sendActionBarDirect(damager, "§a" + target.getName() + Lang.t(damager, " - иммунитет к повреждениям! Осталось §f") + noDamageTicks + Lang.t(damager, " §a сек.!"));
            }
            target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_HIT, 1, 1);
            return true;
        }

        if (damager != null && damagerOp != null && damager.getNoDamageTicks() > 20) { //у нападающего иммунитет
            final int noDamageTicks = damager.getNoDamageTicks() / 20;
            ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§aУ тебя иммунитет к повреждениям и атакам - осталось §f") + noDamageTicks + Lang.t(damager, " §a сек.!"));
            return true;
        }

        if (damager != null && target != null) {                               //если обаигроки
            if (!targetOp.pvp_allow) {                         //если у жертвы выкл пвп
                ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§2У цели выключен режим ПВП!"));
                ScreenUtil.sendActionBarDirect(target, Lang.t(target, "§2У Вас выключен режим ПВП!"));
                return true;
            }
            if (!damagerOp.pvp_allow) {                         //если у атакующего выкл пвп
                ScreenUtil.sendActionBarDirect(target, Lang.t(target, "§2У нападающего выключен режим ПВП!"));
                ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§2У Вас выключен режим ПВП!"));
                return true;
            }
        }

        if (damager != null) { //атакует игрок 
            if (damager.getGameMode() == GameMode.CREATIVE && !damager.isOp()) {
                if (target != null && PM.exists(target) && flags.get(PvpFlag.disable_creative_attack_to_player)) {
                    ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§cАтака на игрока в креативе невозможна!"));
                    return true;
                } else if (flags.get(PvpFlag.disable_creative_attack_to_mobs)) {
                    final EntityUtil.EntityGroup group = EntityUtil.group(targetEntity);
                    if (group != EntityUtil.EntityGroup.UNDEFINED) {
                        ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§cАтака на моба в креативе невозможна!"));
                        return true;
                    }
                }
            }
            if (flags.get(PvpFlag.block_fly_on_pvp_mode) && damager.isFlying() && !damager.isOp()) {
                ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§cАтака в полёте невозможна!"));
                return true;
            }
        }

        if (battle_time > 1) {       //если активен режима боя и хотя бы один игрок

            if (damager != null && target != null) {//дерутся два игрока
                if (!new PlayerPVPEnterEvent(damager, target, cause, true).callEvent()) {
                    return false;
                }
                if (!new PlayerPVPEnterEvent(target, damager, cause, false).callEvent()) {
                    return false;
                }
                pvpBeginFor(damagerOp, damager, battle_time);//damagerOp.pvpBattleModeBegin(battle_time);
                pvpBeginFor(targetOp, target, battle_time);//targetOp.pvpBattleModeBegin(battle_time);
            } else if (target != null && atackEntity instanceof Monster) {//жертва игрок нападает монстр
                if (!new PlayerPVPEnterEvent(target, null, cause, false).callEvent()) {
                    return false;
                }
                pvpBeginFor(targetOp, target, battle_time);//targetOp.pvpBattleModeBegin(battle_time);
            } else if (damager != null && targetEntity instanceof Monster) {//нападает игрок жертва монстр 
                if (!new PlayerPVPEnterEvent(damager, null, cause, true).callEvent()) {
                    return false;
                }
                pvpBeginFor(damagerOp, damager, battle_time);//damagerOp.pvpBattleModeBegin(battle_time);
            } else {
                return false;
            }
        }

        return false;

    }

    public static void pvpBeginFor(final Oplayer op, final Player p, final int time) {
        op.onPVPEnter(p, time, flags.get(PvpFlag.block_fly_on_pvp_mode), flags.get(PvpFlag.display_pvp_tag));
    }

    public static void pvpEndFor(final Oplayer op, final Player p) {
        op.onPVPEnd(p, flags.get(PvpFlag.block_fly_on_pvp_mode), flags.get(PvpFlag.display_pvp_tag));
    }

    public static void pvpOff(final Oplayer op) {
        op.pvp_allow = false;
        if (flags.get(PvpFlag.display_pvp_tag)) {
            final Player p = op.getPlayer();
            op.beforeName("§2☮ ", p);
        }
    }

    public static void pvpOn(final Oplayer op) {
        op.pvp_allow = true;
        if (flags.get(PvpFlag.display_pvp_tag)) {
            final Player p = op.getPlayer();
            op.beforeName(null, p);
        }
    }

    public static boolean getFlag(final PvpFlag f) {
        return flags.get(f);
    }

    public static class PvpSetupMenu implements InventoryProvider {

        @Override
        public void init(final Player p, final InventoryContent content) {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

            if (!flags.get(PvpFlag.enable)) {

                final ItemStack is = new ItemBuilder(ItemType.REDSTONE_BLOCK)
                    .name("§8Модуль неактивен")
                    .lore("§aВключить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        flags.put(PvpFlag.enable, true);
                        saveConfig();
                        PvPManager.init();
                        reopen(p, content);
                    }
                ));
                return;

            } else {

                final ItemStack is = new ItemBuilder(ItemType.EMERALD_BLOCK)
                    .name("§fМодуль активен")
                    .lore("§cВыключить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        flags.put(PvpFlag.enable, false);
                        saveConfig();
                        PvPManager.init();
                        reopen(p, content);
                    }
                ));

            }

            if (battle_time >= 1) {

                final ItemStack is = new ItemBuilder(ItemType.CLOCK)
                    .amount(battle_time)
                    .name("§7Режим боя - длительность")
                    .lore(battle_time + " сек.")
                    .lore(battle_time < 60 ? "§7ЛКМ - прибавить" : "макс.")
                    .lore(battle_time == 1 ? "§cПКМ - выключить" : "§7ПКМ - убавить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        if (e.isLeftClick()) {
                            if (battle_time < 60) {
                                battle_time++;
                                saveConfig();
                                PvPManager.init();
                            }
                        } else if (e.isRightClick()) {
                            //if (battle_time>1) {
                            battle_time--;
                            saveConfig();
                            PvPManager.init();
                            //}
                        }
                        reopen(p, content);
                    }
                ));

            } else {

                final ItemStack is = new ItemBuilder(ItemType.FIREWORK_STAR)
                    .name("§7Режим боя выключен")
                    .lore("§7ЛКМ - включить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        if (e.isLeftClick()) {
                            battle_time = 1;
                            saveConfig();
                            PvPManager.init();
                        }
                        reopen(p, content);
                    }
                ));

            }

            if (no_damage_on_tp >= 1) {

                final ItemStack is = new ItemBuilder(ItemType.CLOCK)
                    .amount(no_damage_on_tp)
                    .name("§7Иммунитет при ТП и респавне")
                    .lore(no_damage_on_tp + " сек.")
                    .lore(no_damage_on_tp < 60 ? "§7ЛКМ - прибавить" : "макс.")
                    .lore(no_damage_on_tp == 1 ? "§cПКМ - выключить" : "§7ПКМ - убавить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        if (e.isLeftClick()) {
                            if (no_damage_on_tp < 60) {
                                no_damage_on_tp++;
                                saveConfig();
                                PvPManager.init();
                            }
                        } else if (e.isRightClick()) {
                            //if (no_damage_on_tp>1) {
                            no_damage_on_tp--;
                            saveConfig();
                            PvPManager.init();
                            //}
                        }
                        reopen(p, content);
                    }
                ));

            } else {
                final ItemStack is = new ItemBuilder(ItemType.FIREWORK_STAR)
                    .name("§7Иммунитет при ТП и респавне")
                    .lore("§7ЛКМ - включить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        if (e.isLeftClick()) {
                            no_damage_on_tp = 1;
                            saveConfig();
                            PvPManager.init();
                        }
                        reopen(p, content);
                    }
                ));
            }

            for (PvpFlag f : PvpFlag.values()) {
                if (f == PvpFlag.enable) {
                    continue;
                }
                boolean b = flags.get(f);

                final ItemStack is = new ItemBuilder(b ? ItemType.LIME_DYE : ItemType.GRAY_DYE)
                    .name("§f" + f)
                    .lore(b ? "§cВыключить" : "§aВключить")
                    .build();

                content.add(ClickableItem.of(is, e -> {
                        //if (e.isLeftClick() ) {
                        //    player.closeInventory();
                        //    player.performCommand("spy "+p.name());
                        //} else {
                        flags.put(f, !b);
                        saveConfig();
                        PvPManager.init();
                        reopen(p, content);
                        //}
                    }
                ));
            }
        }
    }

    private static void loadConfig() {
        config = Cfg.manager.config("pvp.yml", new String[]{"Ostrov77 pvp config file"});

        //портировать старые настройки и убрать из старого конфига
        if (Cfg.getConfig().getConfigurationSection("modules.pvp") != null) {
            try {
                battle_time = Cfg.getConfig().getInt("modules.pvp.battle_mode_time");
                no_damage_on_tp = Cfg.getConfig().getInt("player.invulnerability_on_join_or_teleport");

                flags.put(PvpFlag.advanced_pvp, Cfg.getConfig().getBoolean("modules.pvp.advanced", false));
                flags.put(PvpFlag.allow_pvp_command, Cfg.getConfig().getBoolean("modules.pvp.use_pvp_command", false));
                flags.put(PvpFlag.antirelog, Cfg.getConfig().getBoolean("modules.pvp.kill_on_relog", false));
                flags.put(PvpFlag.drop_inv_inbattle, Cfg.getConfig().getBoolean("modules.pvp.drop_inv_inbattle", false));
                flags.put(PvpFlag.display_pvp_tag, Cfg.getConfig().getBoolean("modules.pvp.display_pvp_tag", false));
                flags.put(PvpFlag.disable_creative_attack_to_mobs, Cfg.getConfig().getBoolean("modules.pvp.disable_creative_attack_to_mobs", false));
                flags.put(PvpFlag.disable_creative_attack_to_player, Cfg.getConfig().getBoolean("modules.pvp.disable_creative_attack_to_player", false));
                flags.put(PvpFlag.block_fly_on_pvp_mode, battle_time > 0);
                flags.put(PvpFlag.block_elytra_on_pvp_mode, battle_time > 0);
                flags.put(PvpFlag.block_command_on_pvp_mode, battle_time > 0);
                boolean enable = battle_time > 0 || no_damage_on_tp > 0 || flags.get(PvpFlag.advanced_pvp)
                    || flags.get(PvpFlag.disable_creative_attack_to_mobs) || flags.get(PvpFlag.disable_creative_attack_to_player)
                    || flags.get(PvpFlag.allow_pvp_command) || flags.get(PvpFlag.antirelog) || flags.get(PvpFlag.drop_inv_inbattle);
                flags.put(PvpFlag.enable, enable);
                Cfg.getConfig().removeKey("modules.pvp");
                Cfg.getConfig().removeKey("player.invulnerability_on_join_or_teleport");
                Cfg.getConfig().saveConfig();

            } catch (Exception ex) {
                Ostrov.log_err("§4Не удалось портировать настройки PVP : " + ex.getMessage());
            }
        }

        //enable = Cfg.getBoolean("enable");
        battle_time = config.getInt("battle_time", -1);
        no_damage_on_tp = config.getInt("no_damage_on_tp", -1);
        flags.replaceAll((f, v) -> config.getBoolean(f.name(), false));
        saveConfig();
    }

    //public static void init() {}
    public static void saveConfig() { //на будущее - для ГУИ настройки
        config.set("enable", flags.get(PvpFlag.enable), "можно отключить игнорируя настройки ниже");
        //Cfg.set("allow_pvp_command", allow_pvp_command);
        config.set("battle_time", battle_time);
        config.set("no_damage_on_tp", no_damage_on_tp);
        for (final Map.Entry<PvpFlag, Boolean> en : flags.entrySet()) {
            final PvpFlag f = en.getKey();
            if (f == PvpFlag.enable) continue;
            Cfg.getConfig().set(f.name(), en.getValue());
        }
        config.saveConfig();
    }
}
