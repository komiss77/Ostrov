package ru.komiss77.commands;

import com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.block.Container;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.events.PlayerPVPEnterEvent;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.items.ItemClass;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.*;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class PvpCmd implements OCommand, Listener {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String act = "action";
        return Commands.literal("pvp")
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                if (!flags.get(PvpFlag.allow_pvp_command)) {
                    pl.sendMessage("§cУправление режимом ПВП отключено!");
                    return 0;
                }

                final Component msg;
                if (PM.getOplayer(pl).pvp_allow) {
                    msg = TCUtils.form(Lang.t(pl, "<gray>Сейчас ПВП <dark_red>Разрешен<gray>  <gold>[<gray>Клик - <green>ВЫКЛЮЧИТЬ<gold>]"))
                        .hoverEvent(HoverEvent.showText(Component.text("Клик - выключить")))
                        .clickEvent(ClickEvent.runCommand("/pvp off"));//Component.text("Сейчас ПВП ", NamedTextColor.GRAY)
                } else {
                    msg = TCUtils.form(Lang.t(pl, "<gray>Сейчас ПВП <green>Запрещён<gray> <gold>[<gray>Клик - <dark_red>ВКЛЮЧИТЬ<gold>]"))
                        .hoverEvent(HoverEvent.showText(Component.text("Клик - включить")))
                        .clickEvent(ClickEvent.runCommand("/pvp on"));//Component.text("Сейчас ПВП ", NamedTextColor.GRAY)
                }
                pl.sendMessage(msg);//p.sendMessage("§2ПВП выключен!");
                return com.mojang.brigadier.Command.SINGLE_SUCCESS;
            })
            .then(Resolver.player(act)
                .suggests((cntx, sb) -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        return sb.buildFuture();
                    }
                    if (ApiOstrov.isStaff(pl)) {
                        sb.suggest("setup");
                        sb.suggest("reload");
                    }
                    sb.suggest("on");
                    sb.suggest("off");
                    return sb.buildFuture();
                })
                .executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    final Oplayer op = PM.getOplayer(pl);
                    switch (Resolver.string(cntx, act)) {
                        case "on":
                            if (!flags.get(PvpFlag.allow_pvp_command)) {
                                pl.sendMessage(Lang.t(pl, "§cУправление режимом ПВП отключено!"));
                                return 0;
                            }
                            op.pvp_allow = true;
                            pvpOn(op);
                            pl.sendMessage(Lang.t(pl, "§4ПВП включен!"));
                            return Command.SINGLE_SUCCESS;
                        case "of":
                            if (!flags.get(PvpFlag.allow_pvp_command)) {
                                pl.sendMessage(Lang.t(pl, "§cУправление режимом ПВП отключено!"));
                                return 0;
                            }
                            pvpOff(op);
                            pl.sendMessage(Lang.t(pl, "§2ПВП выключен!"));
                            return Command.SINGLE_SUCCESS;
                        case "reload":
                            if (ApiOstrov.isLocalBuilder(cs, true)) {
                                init();
                                pl.sendMessage("§aНастройки ПВП режима загружены из файла pvp.yml");
                            }
                            return Command.SINGLE_SUCCESS;
                        case "setup":
                            if (ApiOstrov.isLocalBuilder(cs, true)) {
                                SmartInventory.builder()
                                    .id("PVPsetup" + pl.getName())
                                    .provider(new PvpSetupMenu())
                                    .size(6, 9)
                                    .title("§fНастройки ПВП режима")
                                    .build()
                                    .open(pl);
                            }
                            return Command.SINGLE_SUCCESS;
                        default:
                            return 0;
                    }
                }))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("пвп");
    }

    @Override
    public String description() {
        return "Вкл/Выкл ПВП Режим";
    }

    private static OstrovConfig config;

    private static int battle_time;  //после первого удара - заносим обоих в режим боя
    public static int no_damage_on_tp;
    private static final EnumMap<PvpFlag, Boolean> flags;
    private static final List<PotionEffectType> potion_pvp_type;

    private static Listener damageListener;
    private static Listener flyListener;
    private static Listener elytraListener;
    private static Listener cmdListener;
    private static Listener advancedListener;

    private static final String PVP_NOTIFY = "§cТы в режиме боя!";
    private static final PotionEffect spd = new PotionEffect(PotionEffectType.HASTE, 2, 255, true, false, false);
    private static final PotionEffect slw = new PotionEffect(PotionEffectType.MINING_FATIGUE, 32, 255, true, false, false);
    private static final HashSet<Integer> noClds = new HashSet<>();
    private static final int DHIT_CLD = 4;

    static {
        flags = new EnumMap<>(PvpFlag.class);
        for (final PvpFlag f : PvpFlag.values()) {
            flags.put(f, false);
        }

        potion_pvp_type = Lists.newArrayList(
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

    public PvpCmd() {
        loadConfig(); //загружаем только один раз при старте, потом меняется через ГУИ
        //PlayerDeathEvent слушаем всегда!!!
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
        init();
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

                @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
                public void EntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
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
                            break;
                        default:
                            return;
                    }

                    if (Config.disable_damage) {
                        e.setCancelled(true);
                        return;
                    }

                    if (advanced) {
                        final ItemStack targetHand;
                        if (e.getDamager() instanceof Projectile) {
                            //Ostrov.sync(() -> tgt.setNoDamageTicks(-1), 1);
                        } else {
                            final LivingEntity target = (LivingEntity) e.getEntity();
                            if (target.getType() == EntityType.PLAYER) {//# v P
                                final Player targetPlayer = (Player) target;
                                if (e.getDamager().getType() == EntityType.PLAYER) {//P v P
                                    final Player damagerPlayer = (Player) e.getDamager();
                                    targetHand = targetPlayer.getInventory().getItemInMainHand();
                                    final Material mt = targetHand.getType();
                                    if (targetPlayer.hasCooldown(mt) && ItemClass.MELEE_AXE.has(mt)) {
                                        targetPlayer.getWorld().playSound(targetPlayer.getLocation(), Sound.BLOCK_CHAIN_FALL, 2f, 0.8f);
                                        targetPlayer.setCooldown(mt, 0);
                                        if (!e.isCritical()) {
                                            e.setDamage(0d);
                                            e.setCancelled(true);
                                            targetPlayer.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                                            noClds.add(targetPlayer.getEntityId());
                                            targetPlayer.swingMainHand();
                                            targetPlayer.attack(damagerPlayer);
                                        }
                                        return;
                                    }

                                    final PlayerInventory inv = damagerPlayer.getInventory();
                                    final ItemStack damagerHand = inv.getItemInMainHand();
                                    if (isParying(damagerPlayer)) {
                                        e.setDamage(0d);
                                        e.setCancelled(true);
                                        return;
                                    }
                                    if (damagerPlayer.getAttackCooldown() == 1f
                                        && damagerPlayer.isSprinting() && ItemClass.MELEE_AXE.has(damagerHand.getType())) {
                                        final ItemStack ofh = inv.getItemInOffHand();
                                        if (ItemUtils.isBlank(ofh, false)) {
                                            if (targetPlayer.isBlocking()) {
                                                targetPlayer.setCooldown(Material.SHIELD, 40);
                                                targetPlayer.playEffect(EntityEffect.SHIELD_BREAK);
//                                                final PlayerInventory ti = tpl.getInventory();
//                                                final ItemStack ohs = ti.getItemInOffHand();
//                                                if (ohs != null && ohs.getType() == Material.SHIELD) {
//                                                    VM.getNmsServer().sendFakeEquip(tpl, 40, ItemUtils.air);
//                                                    Ostrov.sync(() -> ti.setItemInOffHand(ti.getItemInOffHand()), 4);
//                                                }
                                            }
                                        } else if (ItemClass.MELEE.has(ofh.getType()) && ItemClass.MELEE.has(damagerHand.getType())) {
                                            Ostrov.sync(() -> {
                                                final ItemStack noh = inv.getItemInOffHand();
                                                if (damagerPlayer.isValid() && target.isValid() && noh.equals(ofh)) {
                                                    final ItemStack it = inv.getItemInMainHand().clone();
                                                    target.setNoDamageTicks(-1);
                                                    damagerPlayer.addPotionEffect(spd);
                                                    inv.setItemInMainHand(ofh);
                                                    damagerPlayer.setSprinting(false);
                                                    damagerPlayer.attack(target);
                                                    inv.setItemInOffHand(inv.getItemInMainHand());
                                                    inv.setItemInMainHand(it);
                                                    damagerPlayer.swingOffHand();
                                                }
                                            }, DHIT_CLD);
                                        }
                                    }
                                } else {
                                    final BotEntity dbe = BotManager.enable ? BotManager.getBot(e.getDamager().getEntityId(), BotEntity.class) : null;
                                    if (dbe != null) {//B v P
                                        targetHand = targetPlayer.getInventory().getItemInMainHand();
                                        final Material mt = targetHand.getType();
                                        if (targetPlayer.hasCooldown(mt) && ItemClass.MELEE_AXE.has(mt)) {
                                            targetPlayer.getWorld().playSound(targetPlayer.getLocation(), Sound.BLOCK_CHAIN_FALL, 2f, 0.8f);
                                            targetPlayer.setCooldown(mt, 0);
                                            if (!e.isCritical()) {
                                                e.setDamage(0d);
                                                e.setCancelled(true);
                                                targetPlayer.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                                                noClds.add(targetPlayer.getEntityId());
                                                targetPlayer.swingMainHand();
                                                targetPlayer.attack(e.getDamager());
                                            }
                                            return;
                                        }

                                        final LivingEntity dle = (LivingEntity) e.getDamager();
                                        final ItemStack hnd = dbe.item(EquipmentSlot.HAND);
                                        if (dbe.parry(dle)) {
                                            e.setDamage(0d);
                                            e.setCancelled(true);
                                            return;
                                        }

                                        if (hnd != null && ItemClass.MELEE_AXE.has(hnd.getType())
                                            && dle.getLocation().distanceSquared(target.getLocation()) < BotEntity.DHIT_DST_SQ) {
                                            final ItemStack ofh = dbe.item(EquipmentSlot.OFF_HAND);
                                            if (ItemUtils.isBlank(ofh, false)) {
                                                if (targetPlayer.isBlocking()) {
                                                    targetPlayer.setCooldown(Material.SHIELD, 40);
                                                    targetPlayer.playEffect(EntityEffect.SHIELD_BREAK);
                                                }
                                            } else if (ItemClass.MELEE.has(ofh.getType()) && ItemClass.MELEE.has(hnd.getType()) && !dbe.busy(dle, null, DHIT_CLD)) {
                                                Ostrov.sync(() -> {
                                                    final ItemStack noh = dbe.item(EquipmentSlot.OFF_HAND);
                                                    final LivingEntity ndp = dbe.getEntity();
                                                    if (ndp != null && target.isValid() && noh != null && noh.equals(ofh)) {
                                                        dbe.busy(dle, true, DHIT_CLD);
                                                        target.setNoDamageTicks(-1);
                                                        dbe.attack(dle, target, true);
                                                    }
                                                }, DHIT_CLD);
                                            }
                                        }
                                    }
                                }
                            } else {
                                final BotEntity tbe = BotManager.enable ? BotManager.getBot(target.getEntityId(), BotEntity.class) : null;
                                if (tbe != null) {// # v B
                                    if (e.getDamager().getType() == EntityType.PLAYER) {// P v B
                                        final Player damagerPlayer = (Player) e.getDamager();
                                        targetHand = tbe.item(EquipmentSlot.HAND);
                                        if (targetHand != null) {
                                            final Material mt = targetHand.getType();
                                            if (tbe.parry(target) && ItemClass.MELEE.has(mt)) {
                                                target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CHAIN_FALL, 2f, 0.8f);
                                                tbe.parry(target, false);
                                                if (!e.isCritical()) {
                                                    e.setDamage(0d);
                                                    e.setCancelled(true);
                                                    tbe.attack(target, damagerPlayer, false);
                                                }
                                                return;
                                            }
                                        }

                                        final PlayerInventory inv = damagerPlayer.getInventory();
                                        final ItemStack damagerHand = inv.getItemInMainHand();
                                        if (isParying(damagerPlayer)) {
                                            e.setDamage(0d);
                                            e.setCancelled(true);
                                            return;
                                        }
                                        if (damagerPlayer.getAttackCooldown() == 1f
                                            && damagerPlayer.isSprinting() && ItemClass.MELEE_AXE.has(damagerHand.getType())) {
                                            final ItemStack ofh = inv.getItemInOffHand();
                                            if (ItemUtils.isBlank(ofh, false)) {
                                                if (tbe.block(target)) {
                                                    tbe.bash(target, true);
                                                }
                                            } else if (ItemClass.MELEE.has(ofh.getType()) && ItemClass.MELEE.has(damagerHand.getType())) {
                                                Ostrov.sync(() -> {
                                                    final ItemStack noh = inv.getItemInOffHand();
                                                    if (damagerPlayer.isValid() && target.isValid() && noh != null && noh.equals(ofh)) {
                                                        final ItemStack it = inv.getItemInMainHand().clone();
                                                        target.setNoDamageTicks(-1);
                                                        damagerPlayer.addPotionEffect(spd);
                                                        inv.setItemInMainHand(ofh);
                                                        damagerPlayer.setSprinting(false);
                                                        damagerPlayer.attack(target);
                                                        inv.setItemInOffHand(inv.getItemInMainHand());
                                                        inv.setItemInMainHand(it);
                                                        damagerPlayer.swingOffHand();
                                                    }
                                                }, DHIT_CLD);
                                            }
                                        }
                                    } else {
                                        final BotEntity dbe = BotManager.enable ? BotManager.getBot(e.getDamager().getEntityId(), BotEntity.class) : null;
                                        if (dbe != null) {// B v B
                                            targetHand = tbe.item(EquipmentSlot.HAND);
                                            if (targetHand != null) {
                                                final Material mt = targetHand.getType();
                                                if (tbe.parry(target) && ItemClass.MELEE.has(mt)) {
                                                    target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CHAIN_FALL, 2f, 0.8f);
                                                    tbe.parry(target, false);
                                                    if (!e.isCritical()) {
                                                        e.setDamage(0d);
                                                        e.setCancelled(true);
                                                        tbe.attack(target, e.getDamager(), false);
                                                    }
                                                    return;
                                                }
                                            }

                                            final LivingEntity dle = (LivingEntity) e.getDamager();
                                            final ItemStack hnd = dbe.item(EquipmentSlot.HAND);
                                            if (dbe.parry(dle)) {
                                                e.setDamage(0d);
                                                e.setCancelled(true);
                                                return;
                                            }

                                            if (hnd != null && ItemClass.MELEE_AXE.has(hnd.getType())
                                                && dle.getLocation().distanceSquared(target.getLocation()) < BotEntity.DHIT_DST_SQ) {
                                                final ItemStack ofh = dbe.item(EquipmentSlot.OFF_HAND);
                                                if (ItemUtils.isBlank(ofh, false)) {
                                                    if (tbe.block(target)) {
                                                        tbe.bash(target, true);
                                                    }
                                                } else if (ItemClass.MELEE.has(ofh.getType()) && ItemClass.MELEE.has(hnd.getType()) && !dbe.busy(dle, null, DHIT_CLD)) {
                                                    Ostrov.sync(() -> {
                                                        final ItemStack noh = dbe.item(EquipmentSlot.OFF_HAND);
                                                        final LivingEntity ndp = dbe.getEntity();
                                                        if (ndp != null && target.isValid() && noh != null && noh.equals(ofh)) {
                                                            dbe.busy(dle, true, DHIT_CLD);
                                                            target.setNoDamageTicks(-1);
                                                            dbe.attack(dle, target, true);
                                                        }
                                                    }, DHIT_CLD);
                                                }
                                            }
                                        }
                                    }
                                } else if (target instanceof Mob) {// # v M
                                    final ItemStack shd = target.getEquipment().getItemInOffHand();
                                    if (shd.getType() == Material.SHIELD && Ostrov.random.nextBoolean()) {
                                        target.getWorld().playSound(target.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 0.8f);
                                        e.setCancelled(true);
                                        e.setDamage(0);
                                        return;
                                    }

                                    if (e.getDamager().getType() == EntityType.PLAYER) {// P v M
                                        final Player dp = (Player) e.getDamager();

                                        final PlayerInventory inv = dp.getInventory();
                                        final ItemStack hnd = inv.getItemInMainHand();
                                        if (isParying(dp)) {
                                            e.setDamage(0d);
                                            e.setCancelled(true);
                                            return;
                                        }
                                        if (dp.getAttackCooldown() == 1f && dp.isSprinting()
                                            && ItemClass.MELEE.has(hnd.getType())) {
                                            final ItemStack ofh = inv.getItemInOffHand();
                                            if (!ItemUtils.isBlank(ofh, false) && ItemClass.MELEE.has(ofh.getType())) {
                                                Ostrov.sync(() -> {
                                                    final ItemStack noh = inv.getItemInOffHand();
                                                    if (dp.isValid() && target.isValid() && noh.equals(ofh)) {
                                                        final ItemStack it = inv.getItemInMainHand().clone();
                                                        target.setNoDamageTicks(-1);
                                                        dp.addPotionEffect(spd);
                                                        inv.setItemInMainHand(ofh);
                                                        dp.setSprinting(false);
                                                        dp.attack(target);
                                                        inv.setItemInOffHand(inv.getItemInMainHand());
                                                        inv.setItemInMainHand(it);
                                                        dp.swingOffHand();
                                                    }
                                                }, DHIT_CLD);
                                            }
                                        }
                                    } else {
                                        final BotEntity dbe = BotManager.enable ? BotManager.getBot(e.getDamager().getEntityId(), BotEntity.class) : null;
                                        if (dbe != null) {// B v M
                                            final LivingEntity dle = (LivingEntity) e.getDamager();
                                            final ItemStack hnd = dbe.item(EquipmentSlot.HAND);
                                            if (dbe.parry(dle)) {
                                                e.setDamage(0d);
                                                e.setCancelled(true);
                                                return;
                                            }

                                            if (hnd != null && ItemClass.MELEE.has(hnd.getType())
                                                && dle.getLocation().distanceSquared(target.getLocation()) < BotEntity.DHIT_DST_SQ) {
                                                final ItemStack ofh = dbe.item(EquipmentSlot.OFF_HAND);
                                                if (ofh != null && ItemClass.MELEE.has(ofh.getType()) && !dbe.busy(dle, null, DHIT_CLD)) {
                                                    Ostrov.sync(() -> {
                                                        final ItemStack noh = dbe.item(EquipmentSlot.OFF_HAND);
                                                        final LivingEntity ndp = dbe.getEntity();
                                                        if (ndp != null && target.isValid() && noh != null && noh.equals(ofh)) {
                                                            dbe.busy(dle, true, DHIT_CLD);
                                                            target.setNoDamageTicks(-1);
                                                            dbe.attack(dle, target, true);
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

                    final Entity damager = ApiOstrov.getDamager(e, true);

                    if (damager != null && damager.getEntityId() == e.getEntity().getEntityId() && flags.get(PvpFlag.disable_self_hit)) {
                        e.setCancelled(true);
                        return;
                    }

                    if (battle_time > 0 && damager != null && disablePvpDamage(damager, e.getEntity(), e.getCause())) {
                        e.setCancelled(true);
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
                        ApiOstrov.sendActionBarDirect(p, PVP_NOTIFY);
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
                        ApiOstrov.sendActionBarDirect(p, PVP_NOTIFY);
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

                    if (PvpCmd.battle_time > 1 && op.pvp_time > 0 && !ApiOstrov.isLocalBuilder(p)) {
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
                    if (iv.getType() == InventoryType.SHULKER_BOX) {
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Result.DENY);
                        e.setUseItemInHand(Result.DENY);
                        return;
                    }

                    switch (e.getAction()) {
                        case RIGHT_CLICK_BLOCK:
                            if (e.getClickedBlock().getState() instanceof Container) break;
                        case RIGHT_CLICK_AIR:
                            final Player p = e.getPlayer();
                            final ItemStack it = e.getItem();
                            if (!ItemUtils.isBlank(it, false)) {
                                final Material mt = it.getType();
                                if (e.getHand() == EquipmentSlot.HAND && !p.hasCooldown(mt)
                                    && ItemClass.MELEE_AXE.has(mt) && p.getAttackCooldown() == 1f) {
                                    final ItemStack ofh = p.getInventory().getItemInOffHand();
                                    if (ofh.getType() == Material.AIR) {
                                        p.getWorld().playSound(p.getEyeLocation(), Sound.BLOCK_AMETHYST_CLUSTER_PLACE, 1f, 0.6f);
                                        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                                            p.getLocation().add(0d, 1.2d, 0d), 24, 0.4d, 0.5d, 0.4d, -0.25d);
                                        p.addPotionEffect(slw);
                                        p.setCooldown(mt, 36);
                                        p.getInventory().setItemInMainHand(ItemUtils.air);
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
                        final Material mt = hnd.getType();
                        if (pl.hasCooldown(mt) && ItemClass.MELEE_AXE.has(mt)) {
                            e.getEntity().setVelocity(e.getEntity().getVelocity().multiply(-0.6d));
                            pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_CHAIN_FALL, 2f, 0.8f);
                            pl.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                            pl.setCooldown(mt, 0);
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
        op.last_death = p.getLocation();//PM.OP_Set_back_location(p.name(), p.getLocation());

        if (flags.get(PvpFlag.enable)) {

            if (flags.get(PvpFlag.drop_inv_inbattle) && op.pvp_time > 0) {            //дроп инвентаря
                if (p.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) { //если сохранение вкл, то дроп в эвенте не образуется, нужно кидать вручную
                    for (ItemStack is : p.getInventory().getContents()) {
                        if (is != null && is.getType() != Material.AIR) {
                            if (MenuItemsManager.isSpecItem(is)) {//не лутать менюшки!
                                //System.out.println("пропускаем si");
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
            ApiOstrov.sendActionBarDirect(target, Lang.t(target, "§aИммунитет к повреждениям  - осталось §f") + noDamageTicks + Lang.t(target, " §a сек.!"));
            if (damager != null) {
                ApiOstrov.sendActionBarDirect(damager, "§a" + target.getName() + Lang.t(damager, " - иммунитет к повреждениям! Осталось §f") + noDamageTicks + Lang.t(damager, " §a сек.!"));
            }
            target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_HIT, 1, 1);
            return true;
        }

        if (damager != null && damagerOp != null && damager.getNoDamageTicks() > 20) { //у нападающего иммунитет
            final int noDamageTicks = damager.getNoDamageTicks() / 20;
            ApiOstrov.sendActionBarDirect(damager, Lang.t(damager, "§aУ тебя иммунитет к повреждениям и атакам - осталось §f") + noDamageTicks + Lang.t(damager, " §a сек.!"));
            return true;
        }

        if (damager != null && target != null) {                               //если обаигроки
            if (!targetOp.pvp_allow) {                         //если у жертвы выкл пвп
                ApiOstrov.sendActionBarDirect(damager, Lang.t(damager, "§2У цели выключен режим ПВП!"));
                ApiOstrov.sendActionBarDirect(target, Lang.t(target, "§2У Вас выключен режим ПВП!"));
                return true;
            }
            if (!damagerOp.pvp_allow) {                         //если у атакующего выкл пвп
                ApiOstrov.sendActionBarDirect(target, Lang.t(target, "§2У нападающего выключен режим ПВП!"));
                ApiOstrov.sendActionBarDirect(damager, Lang.t(damager, "§2У Вас выключен режим ПВП!"));
                return true;
            }
        }

        if (damager != null) { //атакует игрок 
            if (damager.getGameMode() == GameMode.CREATIVE && !damager.isOp()) {
                if (target != null && PM.exists(target) && flags.get(PvpFlag.disable_creative_attack_to_player)) {
                    ApiOstrov.sendActionBarDirect(damager, Lang.t(damager, "§cАтака на игрока в креативе невозможна!"));
                    return true;
                } else if (flags.get(PvpFlag.disable_creative_attack_to_mobs)) {
                    final EntityGroup group = EntityUtil.group(targetEntity);
                    if (group != EntityGroup.UNDEFINED) {
                        ApiOstrov.sendActionBarDirect(damager, Lang.t(damager, "§cАтака на моба в креативе невозможна!"));
                        return true;
                    }
                }
            }
            if (flags.get(PvpFlag.block_fly_on_pvp_mode) && damager.isFlying() && !damager.isOp()) {
                ApiOstrov.sendActionBarDirect(damager, Lang.t(damager, "§cАтака в полёте невозможна!"));
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

    /*public static void pvpBattleModeBegin(final Oplayer op, final Player p, final int battle_time) { //эвент вызывается в Pvp.Проверка_режима_пвп()
//        op.getPlayer().sendMessage("8-" + op.pvp_time);
        if (op.pvp_time == 0) {
        	op.onPVPEnter(p, battle_time, flags.get(PvpFlag.block_fly_on_pvp_mode), flags.get(PvpFlag.display_pvp_tag));
            ApiOstrov.sendActionBar(op.nik, "§cРежим боя " + battle_time + " сек.!");
            if (p != null) {
                op.fly_speed = p.getFlySpeed();
                op.allow_fly = p.getAllowFlight();
                op.in_fly = p.isFlying();
                //не убирай p.isFlying(), из-за этого помню ловил баги - типа игрок не в полёте, флай блокируется, потом пытаешься включить полёт и ничего не получается
                if (flags.get(PvpFlag.block_fly_on_pvp_mode) && p.isFlying()) { 
                    p.setFlying(false); 
                    p.setAllowFlight(false);
                    p.setFlySpeed(0.1F);
                }
                
//                p.setWalkSpeed(0.2F); // хз если вообще нужно, просто откл. локальные настройки на пвп серверах...

                if (flags.get(PvpFlag.display_pvp_tag)) {
                    op.nameColor("§4⚔ ", p);
                }
            }
        }
        op.pvp_time = battle_time;

    }*/
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

    private static class PvpSetupMenu implements InventoryProvider {

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
                        PvpCmd.init();
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
                        PvpCmd.init();
                        reopen(p, content);
                    }
                ));

            }

            if (battle_time >= 1) {

                final ItemStack is = new ItemBuilder(Material.CLOCK)
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
                                PvpCmd.init();
                            }
                        } else if (e.isRightClick()) {
                            //if (battle_time>1) {
                            battle_time--;
                            saveConfig();
                            PvpCmd.init();
                            //}
                        }
                        reopen(p, content);
                    }
                ));

            } else {

                final ItemStack is = new ItemBuilder(Material.FIREWORK_STAR)
                    .name("§7Режим боя выключен")
                    .lore("§7ЛКМ - включить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        if (e.isLeftClick()) {
                            battle_time = 1;
                            saveConfig();
                            PvpCmd.init();
                        }
                        reopen(p, content);
                    }
                ));

            }

            if (no_damage_on_tp >= 1) {

                final ItemStack is = new ItemBuilder(Material.CLOCK)
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
                                PvpCmd.init();
                            }
                        } else if (e.isRightClick()) {
                            //if (no_damage_on_tp>1) {
                            no_damage_on_tp--;
                            saveConfig();
                            PvpCmd.init();
                            //}
                        }
                        reopen(p, content);
                    }
                ));

            } else {
                final ItemStack is = new ItemBuilder(Material.FIREWORK_STAR)
                    .name("§7Иммунитет при ТП и респавне")
                    .lore("§7ЛКМ - включить")
                    .build();
                content.add(ClickableItem.of(is, e -> {
                        if (e.isLeftClick()) {
                            no_damage_on_tp = 1;
                            saveConfig();
                            PvpCmd.init();
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

                final ItemStack is = new ItemBuilder(b ? Material.LIME_DYE : Material.GRAY_DYE)
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
                        PvpCmd.init();
                        reopen(p, content);
                        //}
                    }
                ));
            }
        }
    }

    private static void loadConfig() {
        config = Config.manager.getNewConfig("pvp.yml", new String[]{"Ostrov77 pvp config file"});

        //портировать старые настройки и убрать из старого конфига
        if (Config.getConfig().getConfigurationSection("modules.pvp") != null) {
            try {
                battle_time = Config.getConfig().getInt("modules.pvp.battle_mode_time");
                no_damage_on_tp = Config.getConfig().getInt("player.invulnerability_on_join_or_teleport");

                flags.put(PvpFlag.advanced_pvp, Config.getConfig().getBoolean("modules.pvp.advanced", false));
                flags.put(PvpFlag.allow_pvp_command, Config.getConfig().getBoolean("modules.pvp.use_pvp_command", false));
                flags.put(PvpFlag.antirelog, Config.getConfig().getBoolean("modules.pvp.kill_on_relog", false));
                flags.put(PvpFlag.drop_inv_inbattle, Config.getConfig().getBoolean("modules.pvp.drop_inv_inbattle", false));
                flags.put(PvpFlag.display_pvp_tag, Config.getConfig().getBoolean("modules.pvp.display_pvp_tag", false));
                flags.put(PvpFlag.disable_creative_attack_to_mobs, Config.getConfig().getBoolean("modules.pvp.disable_creative_attack_to_mobs", false));
                flags.put(PvpFlag.disable_creative_attack_to_player, Config.getConfig().getBoolean("modules.pvp.disable_creative_attack_to_player", false));
                flags.put(PvpFlag.block_fly_on_pvp_mode, battle_time > 0);
                flags.put(PvpFlag.block_elytra_on_pvp_mode, battle_time > 0);
                flags.put(PvpFlag.block_command_on_pvp_mode, battle_time > 0);
                boolean enable = battle_time > 0 || no_damage_on_tp > 0
                    || flags.get(PvpFlag.disable_creative_attack_to_mobs) || flags.get(PvpFlag.disable_creative_attack_to_player)
                    || flags.get(PvpFlag.allow_pvp_command) || flags.get(PvpFlag.antirelog) || flags.get(PvpFlag.drop_inv_inbattle);
                flags.put(PvpFlag.enable, enable);
                Config.getConfig().removeKey("modules.pvp");
                Config.getConfig().removeKey("player.invulnerability_on_join_or_teleport");
                Config.getConfig().saveConfig();

            } catch (Exception ex) {
                Ostrov.log_err("§4Не удалось портировать настройки PVP : " + ex.getMessage());
            }
        }

        //enable = config.getBoolean("enable");
        battle_time = config.getInt("battle_time", -1);
        no_damage_on_tp = config.getInt("no_damage_on_tp", -1);
        flags.replaceAll((f, v) -> config.getBoolean(f.name(), false));
        saveConfig();
    }

    //public static void init() {}
    public static void saveConfig() { //на будущее - для ГУИ настройки
        config.set("enable", flags.get(PvpFlag.enable), "можно отключить игнорируя настройки ниже");
        //config.set("allow_pvp_command", allow_pvp_command);
        config.set("battle_time", battle_time);
        config.set("no_damage_on_tp", no_damage_on_tp);
        for (final Map.Entry<PvpFlag, Boolean> en : flags.entrySet()) {
            final PvpFlag f = en.getKey();
            if (f == PvpFlag.enable) {
                continue;
            }
            config.set(f.name(), en.getValue());
        }
        config.saveConfig();
    }

}
