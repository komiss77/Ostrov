package ru.komiss77.modules.entities;

import java.util.*;
import com.google.common.collect.Multimap;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.Weapon;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.*;
import ru.komiss77.boot.OStrap;
import ru.komiss77.boot.RegTag;
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
    public static List<Force> rules = new ArrayList<>();
    public interface Force {
        boolean test(final Player pl, final Oplayer op);
        String msg();
    }

    private static int battle_time;  //после первого удара - заносим обоих в режим боя
    public static int no_damage_on_tp;
    private static final EnumMap<PvpFlag, Boolean> flags;
    private static final Set<PotionEffectType> potion_pvp_type;

    private static final String PVP_NOTIFY = "§cТы в режиме боя!";
    private static final PotionEffect HASTE = new PotionEffect(PotionEffectType.HASTE,
        2, 255, true, false, false);

    public static final Set<ItemType> AXES = OStrap.getAll(ItemTypeTagKeys.AXES);
    public static final Set<ItemType> DUAL_HIT = Set.of(ItemType.DIAMOND_SWORD,
        ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD, ItemType.WOODEN_SWORD,
        ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD, ItemType.TRIDENT);
    public static final Set<ItemType> CAN_BLOCK = Set.of(ItemType.DIAMOND_SWORD,
        ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD, ItemType.WOODEN_SWORD,
        ItemType.STONE_SWORD, ItemType.NETHERITE_SWORD, ItemType.NETHERITE_AXE,
        ItemType.STONE_AXE, ItemType.WOODEN_AXE, ItemType.IRON_AXE,
        ItemType.GOLDEN_AXE, ItemType.DIAMOND_AXE);
  public static final BlocksAttacks MELEE_BLOCK = BlocksAttacks.blocksAttacks().blockDelaySeconds(0f)
      .disableSound(OStrap.keyOf(Sound.BLOCK_COPPER_BULB_BREAK)).blockSound(OStrap.keyOf(Sound.BLOCK_COPPER_BULB_STEP))
      .disableCooldownScale(1.5f).bypassedBy(RegTag.BYPASSES_WEAPON.tagKey()).build();
    private static final float MELEE_BREAK_SEC = 2f;
    //weapons - disable shield if axe || (offhand empty && (run || crit || !shield))
    //weapon block breaks if !shield || axe

    public static final int DHIT_CLD = 4;
    public static final int BLCK_CLD = 0;

    private static Listener damageListener;
    private static Listener flyListener;
    private static Listener elytraListener;
    private static Listener trimListener;
    private static Listener cmdListener;
    private static Listener advancedListener;

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
        block_elytra_on_pvp_mode, block_command_on_pvp_mode, disable_creative_attack_to_mobs, disable_creative_attack_to_player, armor_trim_buffs
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

    public static void addForce(final Force rule) {
        rules.add(rule);
        for (final Oplayer op : PM.getOplayers()) {
            if (op.pvp_allow) continue;
            final Player p = op.getPlayer();
            if (p == null || !isForced(p, op, true)) continue;
            PvPManager.pvpOn(op);
        }
    }

    public static boolean isForced(final Player pl, final Oplayer op, final boolean tell) {
        for (final Force fr : rules) {
            if (!fr.test(pl, op)) continue;
            if (tell) pl.sendMessage(fr.msg());
            return true;
        }
        return false;
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
        if (trimListener != null) {
            HandlerList.unregisterAll(trimListener);
            trimListener = null;
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
                    if (op == null) return;

                    if (flags.get(PvpFlag.drop_inv_inbattle) && op.pvp_time > 0) {            //дроп инвентаря
                        if (p.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) { //если сохранение вкл, то дроп в эвенте не образуется, нужно кидать вручную
                            for (final ItemStack is : p.getInventory().getContents()) {
                                if (ItemUtil.isBlank(is, false)) continue;
                                if (MenuItemsManager.isSpecItem(is)) continue; //не лутать менюшки!
                                p.getWorld().dropItemNaturally(p.getLocation(), is);
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
                    if (!e.getEntityType().isAlive()) return; //не обрабатывать урон рамкам, опыту и провее

                    switch (e.getCause()) {
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

                    if (!advanced) return;
                    final int lvl;
                    switch (e.getDamager()) {
                        case final Trident tr:
                            final ItemStack tit = tr.getItemStack();
                            lvl = tit.getEnchantmentLevel(Enchantment.IMPALING);
                            if (lvl == 0 || (!tr.isInRain() && !tr.isInWater())) break;
                            e.setDamage(lvl * 2.5d + e.getDamage());
                            break;
                        case final LivingEntity le:
                            if (le.getEquipment() == null) break;
                            final ItemStack mhd = le.getEquipment().getItemInMainHand();
                            lvl = mhd.getEnchantmentLevel(Enchantment.IMPALING);
                            if (lvl == 0 || (!le.isInRain() && !le.isInWater())) break;
                            e.setDamage(lvl * 2.5d + e.getDamage());
                            break;
                        default:
                            break;
                    }

                    final ItemStack tgtHand;
                    final LivingEntity target = (LivingEntity) e.getEntity();
                    if (target.getType() == EntityType.PLAYER) {//# v P
                        if (damager instanceof final Player dmgrPl) {//P v P

                            final PlayerInventory inv = dmgrPl.getInventory();
                            final ItemStack hand = inv.getItemInMainHand();
                          final Weapon wpn = hand.getData(DataComponentTypes.WEAPON);
                          if (wpn != null) {
                            if (wpn.disableBlockingForSeconds() != MELEE_BREAK_SEC) {
                              hand.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                                  .itemDamagePerAttack(wpn.itemDamagePerAttack())
                                  .disableBlockingForSeconds(MELEE_BREAK_SEC).build());
                              inv.setItemInMainHand(hand);
                            }
                            if (CAN_BLOCK.contains(hand.getType().asItemType())
                                && !hand.hasData(DataComponentTypes.BLOCKS_ATTACKS)) {
                              hand.setData(DataComponentTypes.BLOCKS_ATTACKS, MELEE_BLOCK);
                              inv.setItemInMainHand(hand);
                            }
                          }

                            Ostrov.sync(() -> EntityUtil.indicate(target.getEyeLocation(), (e.isCritical() ? "<red>✘" : "<gold>")
                                + StringUtil.toSigFigs(e.getFinalDamage(), (byte) 1), dmgrPl), 1);

                            if (dmgrPl.getAttackCooldown() != 1f || !dmgrPl.isSprinting()
                                || !DUAL_HIT.contains(hand.getType().asItemType())) return;

                            final ItemStack ofh = inv.getItemInOffHand();
                            if (!DUAL_HIT.contains(ofh.getType().asItemType())) return;

                            Ostrov.sync(() -> {
                                final ItemStack noh = inv.getItemInOffHand();
                                if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                                    final ItemStack it = inv.getItemInMainHand().clone();
                                    target.setNoDamageTicks(-1);
                                    dmgrPl.addPotionEffect(HASTE);
                                    inv.setItemInMainHand(ofh);
                                    dmgrPl.setSprinting(false);
                                    dmgrPl.attack(target);
                                    inv.setItemInOffHand(inv.getItemInMainHand());
                                    inv.setItemInMainHand(it);
                                    dmgrPl.removePotionEffect(HASTE.getType());
                                    Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
                                }
                            }, DHIT_CLD);
                        } else {
                            final Botter dbe = Cfg.bots ? BotManager.getBot(damager.getEntityId()) : null;
                            if (dbe == null) return; //B v P

                            final ItemStack hand = dbe.item(EquipmentSlot.HAND);
                            final ItemType type = hand == null ? ItemType.AIR : hand.getType().asItemType();
                          final Weapon wpn = hand == null ? null : hand.getData(DataComponentTypes.WEAPON);
                          if (wpn != null) {
                            if (wpn.disableBlockingForSeconds() != MELEE_BREAK_SEC) {
                              hand.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                                  .itemDamagePerAttack(wpn.itemDamagePerAttack())
                                  .disableBlockingForSeconds(MELEE_BREAK_SEC).build());
                              dbe.item(EquipmentSlot.HAND, hand);
                            }
                            if (CAN_BLOCK.contains(type) && !hand.hasData(DataComponentTypes.BLOCKS_ATTACKS)) {
                              hand.setData(DataComponentTypes.BLOCKS_ATTACKS, MELEE_BLOCK);
                              dbe.item(EquipmentSlot.HAND, hand);
                            }
                          }

                            if (hand == null || !DUAL_HIT.contains(type) || damager.getLocation()
                                .distanceSquared(target.getLocation()) > Botter.DHIT_DST_SQ) return;

                            final ItemStack ofh = dbe.item(EquipmentSlot.OFF_HAND);
                            if (ofh == null || dbe.useTicks(damager) != 0
                                || !DUAL_HIT.contains(ofh.getType().asItemType())) return;

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
                        return;
                    }

                    final Botter tbe = Cfg.bots ? BotManager.getBot(target.getEntityId()) : null;
                    if (tbe != null) {// # v B
                        if (damager instanceof final Player dmgrPl) {// P v B

                            final PlayerInventory inv = dmgrPl.getInventory();
                            final ItemStack hand = inv.getItemInMainHand();
                          final Weapon wpn = hand.getData(DataComponentTypes.WEAPON);
                          if (wpn != null) {
                            if (wpn.disableBlockingForSeconds() != MELEE_BREAK_SEC) {
                              hand.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                                  .itemDamagePerAttack(wpn.itemDamagePerAttack())
                                  .disableBlockingForSeconds(MELEE_BREAK_SEC).build());
                              inv.setItemInMainHand(hand);
                            }
                            if (CAN_BLOCK.contains(hand.getType().asItemType())
                                && !hand.hasData(DataComponentTypes.BLOCKS_ATTACKS)) {
                              hand.setData(DataComponentTypes.BLOCKS_ATTACKS, MELEE_BLOCK);
                              inv.setItemInMainHand(hand);
                            }
                          }

                            final ItemType handType = hand.getType().asItemType();
                            final boolean blocking = tbe.isBlocking(target);
                            final ItemStack ofh = inv.getItemInOffHand();
                            if (blocking) {
                              if (AXES.contains(handType) || (wpn != null && ItemUtil.isBlank(ofh, false)
                                  && (e.isCritical() || dmgrPl.isSprinting()))
                              ) {
                                    e.setDamage(0d); e.setCancelled(true);
                                    target.getWorld().playSound(target.getLocation(),
                                        Sound.ITEM_SHIELD_BREAK, 1f, 1f);
                                    tbe.stopUse(target);
                                    return;
                                }
                                e.setDamage(0d); e.setCancelled(true);
                                target.getWorld().playSound(target.getLocation(),
                                    Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                                return;
                            }

                            Ostrov.sync(() -> EntityUtil.indicate(target.getEyeLocation(), (e.isCritical() ? "<red>✘" : "<gold>")
                                + StringUtil.toSigFigs(e.getFinalDamage(), (byte) 1), dmgrPl), 1);

                            if (dmgrPl.getAttackCooldown() != 1f || !dmgrPl.isSprinting()
                                || !DUAL_HIT.contains(hand.getType().asItemType())
                                || !DUAL_HIT.contains(ofh.getType().asItemType())) return;

                            Ostrov.sync(() -> {
                                final ItemStack noh = inv.getItemInOffHand();
                                if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                                    final ItemStack it = inv.getItemInMainHand().clone();
                                    target.setNoDamageTicks(-1);
                                    dmgrPl.addPotionEffect(HASTE);
                                    inv.setItemInMainHand(ofh);
                                    dmgrPl.setSprinting(false);
                                    dmgrPl.attack(target);
                                    inv.setItemInOffHand(inv.getItemInMainHand());
                                    inv.setItemInMainHand(it);
                                    dmgrPl.removePotionEffect(HASTE.getType());
                                    Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
                                }
                            }, DHIT_CLD);
                            return;
                        }

                        final Botter dbe = Cfg.bots ? BotManager.getBot(damager.getEntityId()) : null;
                        if (dbe == null) return;// B v B

                        final ItemStack hand = dbe.item(EquipmentSlot.HAND);
                        final ItemType type = hand == null ? ItemType.AIR : hand.getType().asItemType();
                      final Weapon wpn = hand == null ? null : hand.getData(DataComponentTypes.WEAPON);
                      if (wpn != null) {
                        if (wpn.disableBlockingForSeconds() != MELEE_BREAK_SEC) {
                          hand.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                              .itemDamagePerAttack(wpn.itemDamagePerAttack())
                              .disableBlockingForSeconds(MELEE_BREAK_SEC).build());
                          dbe.item(EquipmentSlot.HAND, hand);
                        }
                        if (CAN_BLOCK.contains(type) && !hand.hasData(DataComponentTypes.BLOCKS_ATTACKS)) {
                          hand.setData(DataComponentTypes.BLOCKS_ATTACKS, MELEE_BLOCK);
                          dbe.item(EquipmentSlot.HAND, hand);
                        }
                      }

                        final boolean blocking = tbe.isBlocking(target);
                        final ItemStack ofh = dbe.item(EquipmentSlot.OFF_HAND);
                        if (blocking) {
                          if (AXES.contains(type) || (wpn != null && ItemUtil.isBlank(ofh, false)
                              && damager.getLocation().distanceSquared(target.getLocation()) < Botter.DHIT_DST_SQ)
                          ) {
                                e.setDamage(0d); e.setCancelled(true);
                                target.getWorld().playSound(target.getLocation(),
                                    Sound.ITEM_SHIELD_BREAK, 1f, 1f);
                                tbe.stopUse(target);
                                return;
                            }
                            e.setDamage(0d); e.setCancelled(true);
                            target.getWorld().playSound(target.getLocation(),
                                Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                            return;
                        }

                        if (hand == null || !DUAL_HIT.contains(type) || damager.getLocation()
                            .distanceSquared(target.getLocation()) > Botter.DHIT_DST_SQ
                            || ofh == null || dbe.useTicks(damager) != 0
                            || !DUAL_HIT.contains(ofh.getType().asItemType())) return;

                        dbe.startUse(damager, EquipmentSlot.OFF_HAND);
                        Ostrov.sync(() -> {
                            final LivingEntity ndp = dbe.getEntity();
                            if (ndp != null && target.isValid() && dbe.usedHand() == EquipmentSlot.OFF_HAND) {
                                target.setNoDamageTicks(-1);
                                dbe.attack(damager, target, true);
                                dbe.stopUse(damager);
                            }
                        }, DHIT_CLD);
                        return;
                    }

                    if (target instanceof Mob || target instanceof ArmorStand) {// # v M
                        final ItemStack shd = target.getEquipment().getItemInOffHand();
                        if (ItemUtil.is(shd, ItemType.SHIELD) && Ostrov.random.nextBoolean()) {
                            target.getWorld().playSound(target.getLocation(),
                                Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                            e.setDamage(0); e.setCancelled(true);
                            return;
                        }

                        if (damager instanceof final Player dmgrPl) {// P v M
                            final PlayerInventory inv = dmgrPl.getInventory();
                            final ItemStack hand = inv.getItemInMainHand();
                          final Weapon wpn = hand.getData(DataComponentTypes.WEAPON);
                          if (wpn != null) {
                            if (wpn.disableBlockingForSeconds() != MELEE_BREAK_SEC) {
                              hand.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                                  .itemDamagePerAttack(wpn.itemDamagePerAttack())
                                  .disableBlockingForSeconds(MELEE_BREAK_SEC).build());
                              inv.setItemInMainHand(hand);
                            }
                            if (CAN_BLOCK.contains(hand.getType().asItemType())
                                && !hand.hasData(DataComponentTypes.BLOCKS_ATTACKS)) {
                              hand.setData(DataComponentTypes.BLOCKS_ATTACKS, MELEE_BLOCK);
                              inv.setItemInMainHand(hand);
                            }
                          }

                            Ostrov.sync(() -> EntityUtil.indicate(target.getEyeLocation(), (e.isCritical() ? "<red>✘" : "<gold>")
                                + StringUtil.toSigFigs(e.getFinalDamage(), (byte) 1), dmgrPl), 1);

                            if (dmgrPl.getAttackCooldown() != 1f || !dmgrPl.isSprinting()
                                || !DUAL_HIT.contains(hand.getType().asItemType())) return;

                            final ItemStack ofh = inv.getItemInOffHand();
                            if (ItemUtil.isBlank(ofh, false)
                                || !DUAL_HIT.contains(ofh.getType().asItemType())) return;

                            Ostrov.sync(() -> {
                                final ItemStack noh = inv.getItemInOffHand();
                                if (dmgrPl.isValid() && target.isValid() && noh.equals(ofh)) {
                                    final ItemStack it = inv.getItemInMainHand().clone();
                                    target.setNoDamageTicks(-1);
                                    dmgrPl.addPotionEffect(HASTE);
                                    inv.setItemInMainHand(ofh);
                                    dmgrPl.setSprinting(false);
                                    dmgrPl.attack(target);
                                    inv.setItemInOffHand(inv.getItemInMainHand());
                                    inv.setItemInMainHand(it);
                                    dmgrPl.removePotionEffect(HASTE.getType());
                                    Nms.swing(dmgrPl, EquipmentSlot.OFF_HAND);
                                }
                            }, DHIT_CLD);
                            return;
                        }

                        final Botter dbe = Cfg.bots ? BotManager.getBot(damager.getEntityId()) : null;
                        if (dbe == null) return;// B v M
                        final ItemStack hnd = dbe.item(EquipmentSlot.HAND);
                        if (hnd == null || !DUAL_HIT.contains(hnd.getType().asItemType())
                            || !(damager.getLocation().distanceSquared(target.getLocation()) < Botter.DHIT_DST_SQ)) return;

                        final ItemStack ofh = dbe.item(EquipmentSlot.OFF_HAND);
                        if (ofh == null || dbe.useTicks(damager) != 0
                            || !DUAL_HIT.contains(ofh.getType().asItemType())) return;

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

                @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
                public static void onPotionSplash(PotionSplashEvent e) {
                    final ThrownPotion pot = e.getPotion();
                    if (e.getAffectedEntities().isEmpty()
                        || !(pot.getShooter() instanceof final Player pl)) return;

                    pot.getEffects().forEach(effect -> {
                        if (!potion_pvp_type.contains(effect.getType())) return;
                        e.getAffectedEntities().forEach(target -> {
                            if (target.getType().isAlive() && disablePvpDamage(pl,
                                target, EntityDamageEvent.DamageCause.MAGIC))
                                e.setIntensity(target, 0d);
                        });
                    });
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

        if (Boolean.TRUE.equals(flags.get(PvpFlag.armor_trim_buffs))) {
            trimListener = new Listener() {
                @EventHandler
                public void onSmith(final PrepareSmithingEvent e) {
                    final SmithingInventory ci = e.getInventory();
                    final ItemStack it = e.getResult();
                    if (!ItemUtil.isBlank(it, false)) {
                        final ItemStack tr = ci.getInputTemplate();
                        if (tr == null || ItemType.NETHERITE_UPGRADE_SMITHING_TEMPLATE
                            .equals(tr.getType().asItemType())) return;
                        final Material mt = it.getType();
                        final EquipmentSlot es = mt.getEquipmentSlot();
                        final EquipmentSlotGroup esg = es.getGroup();
                        final Multimap<Attribute, AttributeModifier> amt = mt.getDefaultAttributeModifiers(es);
                        final ItemMeta im = it.getItemMeta();
                        im.removeAttributeModifier(es);
                        double arm = 0d;
                        for (final AttributeModifier am : amt.get(Attribute.ARMOR)) {
                            switch (am.getOperation()) {
                                case ADD_NUMBER:
                                    arm += am.getAmount();
                                    break;
                                case ADD_SCALAR:
                                    arm *= am.getAmount();
                                    break;
                                case MULTIPLY_SCALAR_1:
                                    arm *= (1d + am.getAmount());
                                    break;
                            }
                        }
                        double ath = 0d;
                        for (final AttributeModifier am : amt.get(Attribute.ARMOR_TOUGHNESS)) {
                            switch (am.getOperation()) {
                                case ADD_NUMBER:
                                    ath += am.getAmount();
                                    break;
                                case ADD_SCALAR:
                                    ath *= am.getAmount();
                                    break;
                                case MULTIPLY_SCALAR_1:
                                    ath *= (1d + am.getAmount());
                                    break;
                            }
                        }
                        double akb = 0d;
                        for (final AttributeModifier am : amt.get(Attribute.KNOCKBACK_RESISTANCE)) {
                            switch (am.getOperation()) {
                                case ADD_NUMBER:
                                    akb += am.getAmount();
                                    break;
                                case ADD_SCALAR:
                                    akb *= am.getAmount();
                                    break;
                                case MULTIPLY_SCALAR_1:
                                    akb *= (1d + am.getAmount());
                                    break;
                            }
                        }

                        final ItemStack add = ci.getInputMineral();
                        im.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(NamespacedKey.minecraft("armor_defense"),
                            arm * (1d + ItemUtil.getTrimMod(add, Attribute.ARMOR)), AttributeModifier.Operation.ADD_NUMBER, esg));

                        im.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(NamespacedKey.minecraft("armor_toughness"),
                            ath * (1d + ItemUtil.getTrimMod(add, Attribute.ARMOR_TOUGHNESS)), AttributeModifier.Operation.ADD_NUMBER, esg));

                        im.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(NamespacedKey.minecraft("armor_knockback_resist"),
                            akb * (1d + ItemUtil.getTrimMod(add, Attribute.KNOCKBACK_RESISTANCE)), AttributeModifier.Operation.ADD_NUMBER, esg));

                        addAttr(im, Attribute.MAX_HEALTH, add, "armor_max_health", esg);
                        addAttr(im, Attribute.SCALE, add, "armor_scale", esg);
                        addAttr(im, Attribute.GRAVITY, add, "armor_gravity", esg);
                        addAttr(im, Attribute.ATTACK_DAMAGE, add, "armor_attack_damage", esg);
                        addAttr(im, Attribute.ATTACK_KNOCKBACK, add, "armor_attack_knockback", esg);
                        addAttr(im, Attribute.ATTACK_SPEED, add, "armor_attack_speed", esg);
                        addAttr(im, Attribute.MOVEMENT_SPEED, add, "armor_move_speed", esg);
                        addAttr(im, Attribute.SNEAKING_SPEED, add, "armor_sneak_speed", esg);
                        addAttr(im, Attribute.WATER_MOVEMENT_EFFICIENCY, add, "armor_water_speed", esg);
                        addAttr(im, Attribute.JUMP_STRENGTH, add, "armor_jump_strength", esg);
                        addAttr(im, Attribute.BLOCK_INTERACTION_RANGE, add, "armor_range_block", esg);
                        addAttr(im, Attribute.ENTITY_INTERACTION_RANGE, add, "armor_range_entity", esg);
                        addAttr(im, Attribute.BLOCK_BREAK_SPEED, add, "armor_break_speed", esg);

                        it.setItemMeta(im);
                        e.setResult(it);
                    }
                }

                private static void addAttr(final ItemMeta im, final Attribute at, final ItemStack in, final String name, final EquipmentSlotGroup esg) {
                    final double mod = ItemUtil.getTrimMod(in, at); if (mod == 0d) return;
                    im.addAttributeModifier(at, new AttributeModifier(NamespacedKey.minecraft(name),
                        mod, AttributeModifier.Operation.MULTIPLY_SCALAR_1, esg));
                }
            };
            Bukkit.getPluginManager().registerEvents(trimListener, Ostrov.instance);
        }

        if (advanced) {
            Ostrov.log_ok("§6Активно улучшенное ПВП!");
            advancedListener = new Listener() {

                //weapons - disable shield if axe || (offhand empty && (run || crit || !shield))
                //weapon block breaks if !shield || axe
                @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
                public void onCld(final PlayerShieldDisableEvent e) {
                    final ItemStack blIt = e.getPlayer().getActiveItem();
                    if (!ItemUtil.is(blIt, ItemType.SHIELD)) return;
                    switch (e.getDamager()) {
                        case Player pl:
                            if (AXES.contains(pl.getInventory()
                                .getItemInMainHand().getType().asItemType())) break;
                            if (!ItemUtil.isBlank(pl.getInventory()
                                .getItemInOffHand(), false)) break;
                            if (pl.getFallDistance() == 0 && !pl.isSprinting()) break;
                            e.setCooldown(0); e.setCancelled(true);
                            break;
                        case LivingEntity le:
                            final EntityEquipment eq = le.getEquipment();
                            if (eq == null) break;
                            if (AXES.contains(eq.getItemInMainHand()
                                .getType().asItemType())) break;
                            if (!ItemUtil.isBlank(eq.getItemInOffHand(), false)) break;
                            e.setCooldown(0); e.setCancelled(true);
                            break;
                        default: break;
                    }
                }

                @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
                public void onRes(final EntityResurrectEvent e) {
                    if (!e.isCancelled() || (!(e.getEntity() instanceof final Player p))) return;
                    final PlayerInventory pi = p.getInventory();
                    final int tsl = pi.first(Material.TOTEM_OF_UNDYING);
                    if (tsl != -1) {
                        pi.getItem(tsl).subtract();
                        ApiOstrov.addCustomStat((Player) e.getEntity(), "DA_ttm", 1);
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

        if (target != null && target.getNoDamageTicks() > 0) { //у жертвы иммунитет
            final int ndSec = target.getNoDamageTicks() / 20;
            if (ndSec == 0) return true;
            ScreenUtil.sendActionBarDirect(target, Lang.t(target, "§aУ тебя иммунитет еще §2{0} сек§a!", ndSec));
            if (damager != null) {
                ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§aУ {0} иммунитет еще §2{1} сек§a!", target.getName(), ndSec));
            }
            target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_HIT, 1, 1);
            return true;
        }

        if (damager != null && damager.getNoDamageTicks() > 0) { //у нападающего иммунитет
            final int ndSec = damager.getNoDamageTicks() / 20;
            if (ndSec == 0) return true;
            ScreenUtil.sendActionBarDirect(target, Lang.t(target, "§aУ тебя иммунитет еще §2{0} сек§a!", ndSec));
            return true;
        }

        if (damager != null && target != null) {                               //если обаигроки
            if (!targetOp.pvp_allow && targetOp.pvp_time < 1 && !isForced(target, targetOp, true)) {                         //если у жертвы выкл пвп
                ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§2У цели выключен ПВП!"));
                ScreenUtil.sendActionBarDirect(target, Lang.t(target, "§2У тебя выключен ПВП!"));
                return true;
            }
            if (!damagerOp.pvp_allow && damagerOp.pvp_time < 1 && !isForced(damager, damagerOp, true)) {                         //если у атакующего выкл пвп
                ScreenUtil.sendActionBarDirect(target, Lang.t(target, "§2У нападающего выключен ПВП!"));
                ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§2У тебя выключен ПВП!"));
                return true;
            }
        }

        if (damager != null) { //атакует игрок 
            if (damager.getGameMode() == GameMode.CREATIVE && !damager.isOp()) {
                if (target != null && flags.get(PvpFlag.disable_creative_attack_to_player)) {
                    ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§cАтака игроков невозможна в креативе!"));
                    return true;
                } else if (flags.get(PvpFlag.disable_creative_attack_to_mobs)) {
                    final EntityUtil.EntityGroup group = EntityUtil.group(targetEntity);
                    if (group != EntityUtil.EntityGroup.UNDEFINED) {
                        ScreenUtil.sendActionBarDirect(damager, Lang.t(damager, "§cАтака мобов невозможна в креативе!"));
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
                if (damager.getEntityId() == target.getEntityId()) return false;
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
            } else return false;
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
//Ostrov.log_warn("saveConfig f="+f+"="+en.getValue());
          config.set(f.name(), en.getValue());
        }
//        Bukkit.getConsoleSender().sendMessage("trying to save pvp config");
        config.saveConfig();
    }
}
