package ru.komiss77.version;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import ru.komiss77.Cfg;
import ru.komiss77.OConfig;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.events.PlayerDisguiseEvent;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.inventory.*;

//https://github.com/Owen1212055/CustomNames
//https://wiki.vg/Entity_metadata#Display
//https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Entity
//для fallingblock new ClientboundAddEntityPacket(this, entity, Block.getId(this.getBlockState()));
//при смене мира для клиента удалить с selfDisguiseId и через 4 тика sendSelfDisguise


public class Disguise {

  public org.bukkit.entity.EntityType type;
  public Input lastInput = Input.EMPTY;
  ServerPlayer sp;
  private final Oplayer op;
  public net.minecraft.world.entity.Entity nmsEnt;
  public Entity nmsVehicle;
  BukkitTask task;
  final GameMode oldGm;
  private static Listener disgLst;
  public static final NamespacedKey DISG = new NamespacedKey(Ostrov.instance, "disguise");
  private static OConfig cfg;
  private static final EnumMap<org.bukkit.entity.EntityType, Double> faces;
  double bbWidth, bbHeight, eyeHeight;
  double faceDelta;
  boolean calibrate;
  boolean isFlyingMob;
  private static Method travelInAir, travelInFluid, travelFlying;
  private int tick, lastInteractTick, lastShiftTick;
  boolean previousShift;
  int charge, chargeTime = 50;
  RayTraceResult rt;

  static {
    cfg = Cfg.manager.getNewConfig("disguise.yml");
    faces = new EnumMap<>(org.bukkit.entity.EntityType.class);
    if (cfg.getConfigurationSection("faces") != null) {
      cfg.getConfigurationSection("faces").getKeys(false).stream().forEach((t) -> {
        try {
          faces.put(org.bukkit.entity.EntityType.valueOf(t), cfg.getDouble("faces." + t));
        } catch (IllegalArgumentException | NullPointerException ex) {
          Ostrov.log_err("Disguise load faces " + t + " : " + ex.getMessage());
        }
      });
    }
    try {
      travelInAir = LivingEntity.class.getDeclaredMethod("travelInAir", Vec3.class);
      travelInAir.setAccessible(true);
      travelInFluid = LivingEntity.class.getDeclaredMethod("travelInFluid", Vec3.class);
      travelInFluid.setAccessible(true);
      travelFlying = LivingEntity.class.getDeclaredMethod("travelFlying", Vec3.class, float.class);
      travelFlying.setAccessible(true);
    } catch (SecurityException | IllegalArgumentException | NoSuchMethodException ex) {
      Ostrov.log_err("Disguise init : " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public Disguise(Player p, final Oplayer sv) {
    this.op = sv;
    sp = Craft.toNMS(p);
    oldGm = p.getGameMode();
  }

  public void disguise(final Player p, final BlockType blockType) {
    if (!makeVehicle(p, org.bukkit.entity.EntityType.FALLING_BLOCK, blockType)) return;
    nmsVehicle.setNoGravity(false);
    runBlockTask();
  }

  public void disguise(final Player p, final org.bukkit.entity.EntityType type) {
    if (type == null) {
      Ostrov.log_warn("disguise type==null недопустим, отмена.");
      return;
    }
    if (this.type != null) {
      if (this.type == type) {
        Ostrov.log_warn("disguise " + type + " уже используется, отмена.");
        return;
      } else {
        Ostrov.log_warn("disguise сейчас " + this.type.name() + ", замена на " + type);
        unDisguise();
      }
    }
    this.type = type;
    p.setGameMode(GameMode.SPECTATOR);
    chargeTime = 50;

    if (nmsEnt == null) {
      p.getWorld().spawn(p.getLocation(), type.getEntityClass(), CreatureSpawnEvent.SpawnReason.CUSTOM, ent -> {
        nmsEnt = (net.minecraft.world.entity.LivingEntity) Craft.toNMS(ent);
        if (nmsEnt instanceof Mob mb) {
          mb.setNoAi(true);
          mb.setCanPickUpLoot(true);
          MoveControl mc = mb.getMoveControl();
          if (mc != null && mc instanceof FlyingMoveControl) {
//Ostrov.log_warn("FlyingMoveControl");
            isFlyingMob = true;
          } else {
            isFlyingMob = false;
          }
          sp.startRiding(mb, true, false);
        }
        ent.getPersistentDataContainer().set(DISG, PersistentDataType.STRING, p.getName());
        runMobTask();
      });
    }

    switch (type) {
      case BAT -> {
        ((Bat) nmsEnt).setResting(false);
        isFlyingMob = true;
      }
      case ENDER_DRAGON -> {
        EnderDragon enderDragon = (EnderDragon) nmsEnt;
        enderDragon.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
        isFlyingMob = true;
      }
      case PHANTOM, GHAST, HAPPY_GHAST -> {
        isFlyingMob = true;
      }
    }

    if (isFlyingMob) {
      nmsEnt.setNoGravity(true);
    }
    op.tag.visible(false);
    if (disgLst == null) {
      setupLst();
    }
  }


  public void disguise_old(final Player p, final org.bukkit.entity.EntityType type) {
    if (!makeVehicle(p, type, null)) return;
    if (nmsEnt == null) {
      p.getWorld().spawn(p.getLocation(), type.getEntityClass(), CreatureSpawnEvent.SpawnReason.CUSTOM, ent -> {
        nmsEnt = (net.minecraft.world.entity.LivingEntity) Craft.toNMS(ent);
        if (nmsEnt instanceof Mob mb) {
          mb.setNoAi(true);
          mb.setCanPickUpLoot(true);
          MoveControl mc = mb.getMoveControl();
          if (mc != null && mc instanceof FlyingMoveControl) {
//Ostrov.log_warn("FlyingMoveControl");
            isFlyingMob = true;
          } else {
            isFlyingMob = false;
          }
        }
        //nmsLe.setSpeed(sp.getSpeed());
        bbWidth = nmsEnt.getBbWidth();
        bbHeight = nmsEnt.getBbHeight();
        eyeHeight = nmsEnt.getEyeHeight();
        if (bbHeight < 0.5) { //у мелких линию продлять или показывает подземелья
//Ostrov.log_warn("мелкое существо ");
          eyeHeight = 0.5;
        }
//Ostrov.log_warn(type+" bbWidth="+bbWidth+" Height="+bbHeight+" eyeHeight="+eyeHeight);
        faceDelta = faces.getOrDefault(type, bbWidth * 0.75); //обычно 75% от хитбокса
        ent.getPersistentDataContainer().set(DISG, PersistentDataType.STRING, p.getName());
        runMobTask();
      });
    }

    switch (type) {
      case BAT -> {
        ((Bat) nmsEnt).setResting(false);
        isFlyingMob = true;
      }
      case PHANTOM, GHAST, HAPPY_GHAST -> {
        isFlyingMob = true;
      }
    }

    if (isFlyingMob) {
      nmsEnt.setNoGravity(true);
    }

  }


  private boolean makeVehicle(final Player p, final org.bukkit.entity.EntityType type, BlockType bt) {
    if (type == null) {
      Ostrov.log_warn("disguise type==null недопустим, отмена.");
      return false;
    }
    if (this.type != null) {
      if (this.type == type) {
        Ostrov.log_warn("disguise " + type + " уже используется, отмена.");
        return false;
      } else {
        Ostrov.log_warn("disguise сейчас " + this.type.name() + ", замена на " + type);
        unDisguise();
      }
    }
    this.type = type;
    p.setGameMode(GameMode.SPECTATOR);

    if (nmsVehicle == null) {
      if (type == org.bukkit.entity.EntityType.FALLING_BLOCK) {
        nmsVehicle = new CustomFallingBlock(sp.level(), sp.getX(), sp.getY(), sp.getZ(),
            ((CraftBlockData) bt.createBlockData()).getState());
        sp.level().addFreshEntity(nmsVehicle, CreatureSpawnEvent.SpawnReason.CUSTOM);
        FallingBlockEntity f = (FallingBlockEntity) nmsVehicle;
        f.time = 1;
        f.dropItem = false;
        f.hurtEntities = false;
        f.setInvulnerable(true);
        f.setSilent(true);
        f.persist = true;
        sp.startRiding(nmsVehicle, true, false);
        //Ostrov.log_warn("riding="+riding);
        f.getBukkitEntity().getPersistentDataContainer().set(DISG, PersistentDataType.STRING, p.getName());
      } else {
        p.getWorld().spawn(p.getLocation(), org.bukkit.entity.ArmorStand.class, CreatureSpawnEvent.SpawnReason.CUSTOM, ent -> {
          org.bukkit.entity.ArmorStand s = (org.bukkit.entity.ArmorStand) ent;
          s.setSmall(true);
          s.setInvisible(true);
          s.setBasePlate(false);
          s.setInvulnerable(true);
          s.setGravity(false);
          s.setCollidable(false);
          s.setAI(false);
          nmsVehicle = (ArmorStand) Craft.toNMS(ent);
          nmsVehicle.setBoundingBox(new AABB(0, 0, 0, 0, 0, 0));
          ent.addPassenger(p);
          ent.getPersistentDataContainer().set(DISG, PersistentDataType.STRING, p.getName());
        });
      }
    }

    op.tag.visible(false);
    if (disgLst == null) {
      setupLst();
    }
    return true;
    //ScreenUtil.sendTitleDirect(p, "", "§7Нажать колёсико мыши - меню", 0, 20, 60);
  }


  private void setupLst() {
    //удаление при EntitiesLoadEvent прицепил в ItemManager.onLoad
    disgLst = new Listener() {

      @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
      public void onRemove(final EntityRemoveEvent e) {
        if (e.getEntity().getPersistentDataContainer().has(DISG)) {
          final String owner = e.getEntity().getPersistentDataContainer().get(DISG, PersistentDataType.STRING);
          Ostrov.log_warn("disguise onRemove " + owner);
          //e.setCancelled(true);
        }
      }

      @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
      public void onSpectate(PlayerStartSpectatingEntityEvent e) {
        if (e.getNewSpectatorTarget().getPersistentDataContainer().has(DISG)) {
          final String owner = e.getNewSpectatorTarget().getPersistentDataContainer().get(DISG, PersistentDataType.STRING);
          Ostrov.log_warn("disguise StartSpectating " + owner);
          e.setCancelled(true); //здесь, обработчик PlayerDisguiseEvent может разрешить
          final Player ownerPlayer = Bukkit.getPlayerExact(owner);
          if (ownerPlayer != null) {
            PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(ownerPlayer, PM.getOplayer(ownerPlayer).disguise, PlayerDisguiseEvent.DisguiseAction.SpectateEvent);
            disguiseEvent.event = e;
            Bukkit.getPluginManager().callEvent(disguiseEvent);
          } else {
            e.getNewSpectatorTarget().remove();
          }
        }
      }

      @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
      public void onInteract(final PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked().getPersistentDataContainer().has(DISG)) {
          final String owner = e.getRightClicked().getPersistentDataContainer().get(DISG, PersistentDataType.STRING);
          Ostrov.log_warn("disguise InteractAtEntity " + owner);
          e.setCancelled(true); //здесь, обработчик PlayerDisguiseEvent может разрешить
          final Player ownerPlayer = Bukkit.getPlayerExact(owner);
          if (ownerPlayer != null) {
            PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(ownerPlayer, PM.getOplayer(ownerPlayer).disguise, PlayerDisguiseEvent.DisguiseAction.InteractAtDisguiseEvent);
            disguiseEvent.event = e;
            Bukkit.getPluginManager().callEvent(disguiseEvent);
          } else {
            e.getRightClicked().remove();
          }
        }
      }

      @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
      public void onDamage(final EntityDamageEvent e) {
        if (e.getEntity().getPersistentDataContainer().has(DISG)) {
          final String owner = e.getEntity().getPersistentDataContainer().get(DISG, PersistentDataType.STRING);
//Ostrov.log_warn("disguise EntityDamage "+owner+" : "+e.getCause());
          e.setCancelled(true); //здесь, обработчик PlayerDisguiseEvent может разрешить
          final Player ownerPlayer = Bukkit.getPlayerExact(owner);
          if (ownerPlayer != null) {
            PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(ownerPlayer, PM.getOplayer(ownerPlayer).disguise, PlayerDisguiseEvent.DisguiseAction.DamageEvent);
            disguiseEvent.event = e;
            Bukkit.getPluginManager().callEvent(disguiseEvent);
          } else {
            e.getEntity().remove();
          }
        }
      }

      @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
      public void onMount(final EntityMountEvent e) {
        if (e.getMount().getPersistentDataContainer().has(DISG)) {
          final String owner = e.getEntity().getPersistentDataContainer().get(DISG, PersistentDataType.STRING);
          e.setCancelled(true); //здесь, обработчик PlayerDisguiseEvent может разрешить
          final Player ownerPlayer = Bukkit.getPlayerExact(owner);
          if (ownerPlayer != null) {
            PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(ownerPlayer, PM.getOplayer(ownerPlayer).disguise, PlayerDisguiseEvent.DisguiseAction.MountEvent);
            disguiseEvent.event = e;
            Bukkit.getPluginManager().callEvent(disguiseEvent);
            e.setCancelled(true);
          } else {
            e.getMount().remove();
          }
          Ostrov.log_warn("disguise EntityMount " + owner);
          e.setCancelled(true);
        }
      }

      @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
      public void onMount(final PlayerLeashEntityEvent e) {
        if (e.getEntity().getPersistentDataContainer().has(DISG)) {
          final String owner = e.getEntity().getPersistentDataContainer().get(DISG, PersistentDataType.STRING);
          e.setCancelled(true); //здесь, обработчик PlayerDisguiseEvent может разрешить
          final Player ownerPlayer = Bukkit.getPlayerExact(owner);
          e.setCancelled(true); //здесь, обработчик PlayerDisguiseEvent может разрешить
          if (ownerPlayer != null) {
            PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(ownerPlayer, PM.getOplayer(ownerPlayer).disguise, PlayerDisguiseEvent.DisguiseAction.LeashEvent);
            disguiseEvent.event = e;
            Bukkit.getPluginManager().callEvent(disguiseEvent);
          } else {
            e.getEntity().remove();
          }
          Ostrov.log_warn("disguise EntityMount " + owner);
          e.setCancelled(true);
        }
      }

      @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
      public void onDismount(final EntityDismountEvent e) {
        if (e.getDismounted().getPersistentDataContainer().has(DISG)) {
          final String owner = e.getEntity().getPersistentDataContainer().get(DISG, PersistentDataType.STRING);
          final Player ownerPlayer = owner == null ? null : Bukkit.getPlayerExact(owner);
          if (ownerPlayer != null) {
            PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(ownerPlayer, PM.getOplayer(ownerPlayer).disguise, PlayerDisguiseEvent.DisguiseAction.DismountEvent);
            disguiseEvent.event = e;
            Bukkit.getPluginManager().callEvent(disguiseEvent);
            //e.setCancelled(true); давать спешиться всегда
          } else {
            e.setCancelled(true);
            e.getDismounted().remove();
          }
          Ostrov.log_warn("disguise EntityDismount " + owner);
        }
      }

      @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
      public void onPickup(final EntityPickupItemEvent e) {
        if (e.getEntity().getPersistentDataContainer().has(DISG)) {
          final String owner = e.getEntity().getPersistentDataContainer().get(DISG, PersistentDataType.STRING);
          e.setCancelled(true); //здесь, обработчик PlayerDisguiseEvent может разрешить
          Ostrov.log_warn("disguise onPickup " + owner);
          final Player ownerPlayer = Bukkit.getPlayerExact(owner);
          if (ownerPlayer != null) {
            PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(ownerPlayer, PM.getOplayer(ownerPlayer).disguise, PlayerDisguiseEvent.DisguiseAction.PickupEvent);
            disguiseEvent.event = e;
            Bukkit.getPluginManager().callEvent(disguiseEvent);
          } else {
            e.getEntity().remove();
          }
        }
      }


      @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
      public void onTrans(final EntityTransformEvent e) {
        if (e.getEntity().getPersistentDataContainer().has(DISG)) {
          final String owner = e.getEntity().getPersistentDataContainer().get(DISG, PersistentDataType.STRING);
          Ostrov.log_warn("disguise EntityTransform " + e.getTransformReason() + " " + owner);
          e.setCancelled(true);
        }
      }
    /*@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTeleport(final EntityTeleportEvent e) {
        if (e.getEntity() instanceof final LivingEntity le) {
            final Botter be = botById.get(le.getEntityId());
            if (be != null) {
                final Location to = e.getTo();
                if (to == null) return;
                ((BotEntity) be).teleport(le, to);
            }
        }
    }*/

    };
    Bukkit.getPluginManager().registerEvents(disgLst, Ostrov.instance);
  }


  public void unDisguise() {
    if (!Bukkit.isPrimaryThread()) {
      Ostrov.sync(() -> unDisguise());
      return;
    }
    if (type == null) {
      //Ostrov.log_warn("disguise не используется ");
      return;
    }
    type = null;
    calibrate = false;
    if (task != null && !task.isCancelled()) {
      task.cancel();
      task = null;
    }
    tick = 0;
    if (nmsVehicle != null) {
      nmsVehicle.stopRiding();
      nmsVehicle.remove(Entity.RemovalReason.DISCARDED);
      nmsVehicle = null;
    }

    final Player p = op.getPlayer();
    if (p != null) {
      Location back;
      if (nmsEnt != null) {
        back = new Location(p.getWorld(), nmsEnt.getX(), nmsEnt.getY(), nmsEnt.getZ());
        nmsEnt.remove(Entity.RemovalReason.DISCARDED);
        nmsEnt = null;
      } else {
        back = p.getLocation().clone().add(0, 2, 0);
      }
      p.setGameMode(oldGm);
      op.tag.visible(true);
      p.teleport(back);
    }

    lastInput = Input.EMPTY;

    //откл. листенер если ни у кого не осталось маскировок
    boolean b = false;
    for (Oplayer op : PM.getOplayers()) {
      if (op.disguise.type != null) {
        b = true;
        break;
      }
    }
    if (!b) {
      if (disgLst != null) {
        HandlerList.unregisterAll(disgLst);
        disgLst = null;
      }
    }
  }


  private void runBlockTask() {
    task = new BukkitRunnable() {
      @Override
      public void run() {
//Ostrov.log_warn(" task="+this.getTaskId()+" nmsEnt="+nmsEnt);
        if (!sp.getBukkitEntity().isOnline() || sp.gameMode() != GameType.SPECTATOR || nmsVehicle == null || !nmsVehicle.isAlive()) {
          this.cancel();
          unDisguise();
          return;
        }
        if (lastInput.shift()) {
          op.disguise.action(PlayerDisguiseEvent.DisguiseAction.Shift);
          return;
        }
        CustomFallingBlock cf = (CustomFallingBlock) nmsVehicle;
//Ostrov.log_warn(" task="+this.getTaskId());
        float speed = 0.3f; //хз почему так, если 1 то бегают в 2 раза быстрее-бляя,был двойной таск
        float forward = 0.0f;
        float side = 0.0f;
        boolean inFluid = false;
        boolean onGround = nmsVehicle.onGround();

        FluidState fluidState = nmsVehicle.level().getFluidState(nmsVehicle.blockPosition());
        if (!fluidState.isEmpty()) {
          final double height = fluidState.getHeight(nmsVehicle.level(), nmsVehicle.blockPosition());
          if (height == 1) {
            inFluid = true; //1 даёт следующий после поверхности блок
          } else {
            onGround = true; //чтобы мог выпрыгнуть из воды
          }
        }

        if (lastInput.forward()) {
          forward = speed;
        } else if (lastInput.backward()) {
          forward = -speed * 0.5f;
        }
        if (lastInput.left()) {
          side = speed;
        } else if (lastInput.right()) {
          side = -speed;
        }
        if (isFlyingMob || inFluid) { //летающий или в воде
          if (lastInput.jump()) {
            cf.up = speed;
          } else if (isFlyingMob && !onGround) {//летающий будет опускаться если не жать пробел, плавающие в воде тонут сами
            cf.up = -speed;
          }
        } else if (onGround) {
          if (lastInput.jump()) {
            cf.up = 1;
          }
        } else {
          if (cf.up > 0) {
            cf.up *= 0.2;
          } else {
            cf.up = -0.3f;//(float) -nmsVehicle.getGravity(); //FallingBlockEntity = 0.04
          }
        }

        Vec3 input = new Vec3(side, cf.up, forward);
//Ostrov.log_warn (" input="+input+" inFluid?"+inFluid+" onGround?"+nmsVehicle.onGround());
        double d = input.lengthSqr();
        Vec3 travelVector;// = Vec3.ZERO;
        if (d < 1.0E-7) {
          travelVector = Vec3.ZERO;
        } else {
          if (d > 1.0) {
            input = input.normalize();//.scale((double)motionScaler)
          }
          float sin = Mth.sin(sp.getYRot() * 0.017453292F);
          float cos = Mth.cos(sp.getYRot() * 0.017453292F);
          travelVector = new Vec3(input.x * (double) cos - input.z * (double) sin, input.y, input.z * (double) cos + input.x * (double) sin);

        }

        nmsVehicle.setDeltaMovement(travelVector);
        nmsVehicle.hurtMarked = true;
        tick++;
      }
    }.runTaskTimer(Ostrov.instance, 1, 1);
  }


  private void runMobTask() {
    task = new BukkitRunnable() {
      @Override
      public void run() {
        if (!sp.getBukkitEntity().isOnline() || sp.gameMode() != GameType.SPECTATOR || nmsEnt == null || !nmsEnt.isAlive()) {
          this.cancel();
          unDisguise();
          sp.getBukkitEntity().sendMessage("§6Маскировка прервалась.");
          return;
        }
        if (lastInput.shift()) {
          if (previousShift == false) {
            previousShift = true;
          }
          lastShiftTick++;
//Ostrov.log_warn("шитф hold "+lastShiftTick);
          if (lastShiftTick >= 10) {
            ScreenUtil.sendActionBarDirect(op.getPlayer(), StringUtil.getPercentBar(chargeTime, charge, false));
            if (lastShiftTick == chargeTime) {
              previousShift = false;
              lastShiftTick = 0;
              charge = 0;
              ScreenUtil.sendActionBarDirect(op.getPlayer(), "");
              charged();
            } else {
              charge(charge);
              charge++;
            }
          }
        } else if (previousShift) {
          previousShift = false;
//Ostrov.log_warn("отпущен шитф "+lastShiftTick);
          if (lastShiftTick < 5) {
            op.disguise.action(PlayerDisguiseEvent.DisguiseAction.Shift);
            return;
          } else if (charge > 0) {
            ScreenUtil.sendActionBarDirect(op.getPlayer(), "");
          }
          lastShiftTick = 0;
          charge = 0;
        }

        LivingEntity nmsLe = (LivingEntity) nmsEnt;
//Ostrov.log_warn(" task="+this.getTaskId());
        float speed = 1f; //хз почему так, если 1 то бегают в 2 раза быстрее-бляя,был двойной таск
        float forward = 0.0f;
        float side = 0.0f;
        float up = 0.0f;
        boolean jumping = lastInput.jump();
        boolean inFluid = false;

        FluidState fluidState = nmsLe.level().getFluidState(nmsLe.blockPosition());
        if ((nmsLe.isInWater() || nmsLe.isInLava()) && nmsLe.isAffectedByFluids() && !nmsLe.canStandOnFluid(fluidState)) {
          inFluid = true;
        }

        if (lastInput.forward()) {
          forward = speed;
        } else if (lastInput.backward()) {
          forward = -speed * 0.5f;
        }
        if (lastInput.left()) {
          side = speed;
        } else if (lastInput.right()) {
          side = -speed;
        }

        if (isFlyingMob || inFluid) { //летающий или в воде
          if (lastInput.jump()) {
            up = speed;
          } else if (isFlyingMob && !nmsLe.onGround()) {//летающий будет опускаться если не жать пробел, плавающие в воде тонут сами
            up = -speed;
          }
        } else if (nmsLe.onGround() && jumping) {
          Vec3 vec3d1 = nmsLe.getDeltaMovement();
          double jumpPower = 0.25d;
          nmsLe.setDeltaMovement(vec3d1.x * jumpPower, 0.5d, vec3d1.z * jumpPower);
        }

//Ostrov.log_warn (" isFlyingMob?"+isFlyingMob+" inFluid?"+inFluid+" isFalling?"+isFalling);
        Vec3 travelVector = new Vec3(side, up, forward);
        if (travelVector.lengthSqr() > 1.0) {
          travelVector = travelVector.normalize();
        }

        if (nmsLe.onGround() || !isFlyingMob) { //!!! ставить ПЕРЕД travel
          nmsLe.setSpeed((float) sp.getAttributeValue(Attributes.MOVEMENT_SPEED));
        } else {
          nmsLe.setSpeed(sp.getAbilities().flyingSpeed);
        }

        switch (type) {
          //метод travel у некоторых переопределяется.
          //Спрут вообще двигается только рандомно. вызываем плавание напрямую
          case SQUID -> {
            try {
              travelInFluid.invoke(nmsLe, travelVector);
            } catch (IllegalAccessException | InvocationTargetException ex) {
              Ostrov.log_warn("disguise " + type + " travelInFluid : " + ex.getMessage());
            }
          }
          case AXOLOTL, FROG, GUARDIAN, ELDER_GUARDIAN, TURTLE -> { //в воде слишком шустрые
            if (inFluid) {
              nmsLe.travel(travelVector.multiply(.5, .5, .5));
            } else {
              nmsLe.travel(travelVector);
            }
          }
          case PHANTOM -> {//летучки меняют скорость полёта PHANTOM=0.2F GHAST=0.02F
            try {
              travelFlying.invoke(nmsLe, travelVector, 0.05f);
            } catch (IllegalAccessException | InvocationTargetException ex) {
              Ostrov.log_warn("disguise " + type + " travelFlying : " + ex.getMessage());
            }
          }
          case ALLAY -> { //летучки меняют скорость полёта PHANTOM=0.2F GHAST=0.02F ALLAY=getSpeed
            try {
              travelFlying.invoke(nmsLe, travelVector, 0.01f);
            } catch (IllegalAccessException | InvocationTargetException ex) {
              Ostrov.log_warn("disguise " + type + " travelFlying : " + ex.getMessage());
            }
          }
          case GHAST, HAPPY_GHAST -> { //летучки меняют скорость полёта PHANTOM=0.2F GHAST=0.02F
            try {
              travelFlying.invoke(nmsLe, travelVector, 0.02f);
            } catch (IllegalAccessException | InvocationTargetException ex) {
              Ostrov.log_warn("disguise " + type + " travelFlying : " + ex.getMessage());
            }
          }
          case ENDER_DRAGON -> { //летучки меняют скорость полёта PHANTOM=0.2F GHAST=0.02F ALLAY=getSpeed
            try {
              travelFlying.invoke(nmsLe, travelVector, 0.02f);
            } catch (IllegalAccessException | InvocationTargetException ex) {
              Ostrov.log_warn("disguise " + type + " travelFlying : " + ex.getMessage());
            }
            Ostrov.log_warn("phase=" + ((EnderDragon) nmsEnt).getPhaseManager().getCurrentPhase());
          }
          default -> {
            //if (inFluid) {
            //travelInFluid(travelVector);
            nmsLe.travel(travelVector);
            //} else {
            //nmsLe.travel(travelVector);
//Ostrov.log_warn(nmsLe.getSpeed()+"("+sp.getAttributeValue(Attributes.MOVEMENT_SPEED)+"/"+sp.getAbilities().flyingSpeed+") v="+travelVector);
            //}
          }

        }

        final Player p = op.getPlayer();
        double range = nmsLe.getAttributeValue(Attributes.FOLLOW_RANGE);//в TargetGoal берётся FOLLOW_RANGE
        double rangeSq = Math.sqrt(range);
        rt = p.getWorld().rayTrace(p.getEyeLocation(), p.getEyeLocation().getDirection(), rangeSq,
            FluidCollisionMode.NEVER, false, 1, en -> {
              return en.getEntityId() != p.getEntityId() && en.getEntityId() != nmsLe.getId();
            });
        if (rt != null) {
          Vector hit = rt.getHitPosition();
          ClientboundLevelParticlesPacket particlesPacket = new ClientboundLevelParticlesPacket(CraftParticle.createParticleParam(Particle.ELECTRIC_SPARK, null),
              true, false, hit.getX(), hit.getY(), hit.getZ(), 0, 0, 0, 0, 1);
          sp.connection.send(particlesPacket);
        }

        tick++;
      }
    }.runTaskTimer(Ostrov.instance, 1, 1);
  }

  private void charge(int charge) {
//Ostrov.log_warn("charge "+charge);
    switch (type) {
      case CREEPER -> {
        Creeper creeper = (Creeper) nmsEnt;
        if (charge == 0) {
          creeper.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
          creeper.setIgnited(true);
        }
        creeper.swell = 0;
      }
    }
  }

  private void charged() {
    //Ostrov.log_warn("BOOM");
    //Vec3 eyePos = sp.position().add(0, eyeHeight, 0);
    Vec3 view = nmsEnt.calculateViewVector(nmsEnt.getXRot(), nmsEnt.getYRot()).normalize();
    Level level = nmsEnt.level();
    //Vec3 bbWidthV = view.multiply(bbWidth, bbWidth, bbWidth);
    switch (type) {
      case ENDERMAN -> {
        EnderMan enderman = (EnderMan) nmsEnt;
        enderman.teleport();
      }
      case BLAZE -> {
        Blaze blaze = (Blaze) nmsEnt;
        level.levelEvent(null, LevelEvent.SOUND_BLAZE_FIREBALL, blaze.blockPosition(), 0);
        SmallFireball smallFireball = new SmallFireball(level, blaze, view);
        smallFireball.setPos(smallFireball.getX(), blaze.getY(0.5) + 0.5, smallFireball.getZ());
        level.addFreshEntity(smallFireball);
      }
      case CREEPER -> {
        Creeper creeper = (Creeper) nmsEnt;
        creeper.explodeCreeper();
      }
      case GHAST -> {
        Ghast ghast = (Ghast) nmsEnt;
        LargeFireball largeFireball = new LargeFireball(level, ghast, view, ghast.getExplosionPower());
        largeFireball.bukkitYield = (float) (largeFireball.explosionPower = ghast.getExplosionPower());
        largeFireball.setPos(ghast.getX() + view.x * 4.0, ghast.getY(0.5) + 0.5, largeFireball.getZ() + view.z * 4.0);
        level.addFreshEntity(largeFireball);
      }
    }

  }

 /* protected void pickUpItem(Mob mob, ItemEntity entity) {
Ostrov.log_warn("pickUpItem "+entity.getItem().asBukkitCopy().getType());
    net.minecraft.world.item.ItemStack item = entity.getItem();
    net.minecraft.world.item.ItemStack itemStack = mob.equipItemIfPossible((ServerLevel) mob.level(), item.copy(), entity); // CraftBukkit - add item
    if (!itemStack.isEmpty()) {
      mob.onItemPickup(entity);
      mob.take(entity, itemStack.getCount());
      item.shrink(itemStack.getCount());
      if (item.isEmpty()) {
        entity.discard(EntityRemoveEvent.Cause.PICKUP); // CraftBukkit - add Bukkit remove cause
      }
    }
  }*/

  private void runMobTask_old() {
    task = new BukkitRunnable() {
      @Override
      public void run() {
        if (!sp.getBukkitEntity().isOnline() || sp.gameMode() != GameType.SPECTATOR || nmsEnt == null || !nmsEnt.isAlive()) {
          this.cancel();
          unDisguise();
          return;
        }
        if (lastInput.shift()) {
          op.disguise.action(PlayerDisguiseEvent.DisguiseAction.Shift);
          return;
        }
        LivingEntity nmsLe = (LivingEntity) nmsEnt;
//Ostrov.log_warn(" task="+this.getTaskId());
        float speed = 1f; //хз почему так, если 1 то бегают в 2 раза быстрее-бляя,был двойной таск
        float forward = 0.0f;
        float side = 0.0f;
        float up = 0.0f;
        boolean jumping = lastInput.jump();
        boolean inFluid = false;
        //boolean isFalling = false;

        FluidState fluidState = nmsLe.level().getFluidState(nmsLe.blockPosition());
        if ((nmsLe.isInWater() || nmsLe.isInLava()) && nmsLe.isAffectedByFluids() && !nmsLe.canStandOnFluid(fluidState)) {
          //this.travelInFluid(travelVector);
          inFluid = true;
        }// else if (nmsLe.isFallFlying()) {
        //this.travelFallFlying(travelVector);
        //isFalling = true;
        // } else {
        //nmsLe.travelInAir(travelVector);
        // }

        if (calibrate) {
          if (lastInput.forward()) {
            if (calibrate) {
              if (faceDelta < nmsLe.getBbWidth()) faceDelta += 0.01;
              op.getPlayer().sendMessage("§3" + type + " bb=" + bbWidth + " d=§6" + faceDelta);
            }
          } else if (lastInput.backward()) {
            if (calibrate) {
              if (faceDelta > 0) faceDelta -= 0.01;
              op.getPlayer().sendMessage("§3" + type + " bb=" + bbWidth + " d=§6" + faceDelta);
            }
          }
        } else {

          if (lastInput.forward()) {
            forward = speed;
          } else if (lastInput.backward()) {
            forward = -speed * 0.5f;
          }
          if (lastInput.left()) {
            side = speed;
          } else if (lastInput.right()) {
            side = -speed;
          }

          if (isFlyingMob || inFluid) { //летающий или в воде
            if (lastInput.jump()) {
              up = speed;
            } else if (isFlyingMob && !nmsLe.onGround()) {//летающий будет опускаться если не жать пробел, плавающие в воде тонут сами
              up = -speed;
            }
          } else if (nmsLe.onGround() && jumping) {
            Vec3 vec3d1 = nmsLe.getDeltaMovement();
            double jumpPower = 0.25d;
            nmsLe.setDeltaMovement(vec3d1.x * jumpPower, 0.5d, vec3d1.z * jumpPower);
          }

//Ostrov.log_warn (" isFlyingMob?"+isFlyingMob+" inFluid?"+inFluid+" isFalling?"+isFalling);
          Vec3 travelVector = new Vec3(side, up, forward);
          if (travelVector.lengthSqr() > 1.0) {
            travelVector = travelVector.normalize();
          }

          if (nmsLe.onGround() || !isFlyingMob) { //!!! ставить ПЕРЕД travel
            nmsLe.setSpeed((float) sp.getAttributeValue(Attributes.MOVEMENT_SPEED));
          } else {
            nmsLe.setSpeed(sp.getAbilities().flyingSpeed);
          }

          switch (type) {
            //метод travel у некоторых переопределяется.
            //Спрут вообще двигается только рандомно. вызываем плавание напрямую
            case SQUID -> {
              try {
                travelInFluid.invoke(nmsLe, travelVector);
              } catch (IllegalAccessException | InvocationTargetException ex) {
                Ostrov.log_warn("disguise " + type + " travelInFluid : " + ex.getMessage());
              }
            }
            case AXOLOTL, FROG, GUARDIAN, ELDER_GUARDIAN, TURTLE -> { //в воде слишком шустрые
              if (inFluid) {
                nmsLe.travel(travelVector.multiply(.5, .5, .5));
              } else {
                nmsLe.travel(travelVector);
              }
            }
            case PHANTOM -> {//летучки меняют скорость полёта PHANTOM=0.2F GHAST=0.02F
              try {
                travelFlying.invoke(nmsLe, travelVector, 0.05f);
              } catch (IllegalAccessException | InvocationTargetException ex) {
                Ostrov.log_warn("disguise " + type + " travelFlying : " + ex.getMessage());
              }
            }
            case ALLAY -> { //летучки меняют скорость полёта PHANTOM=0.2F GHAST=0.02F ALLAY=getSpeed
              try {
                travelFlying.invoke(nmsLe, travelVector, 0.01f);
              } catch (IllegalAccessException | InvocationTargetException ex) {
                Ostrov.log_warn("disguise " + type + " travelFlying : " + ex.getMessage());
              }
            }
            case GHAST, HAPPY_GHAST -> { //летучки меняют скорость полёта PHANTOM=0.2F GHAST=0.02F
              try {
                travelFlying.invoke(nmsLe, travelVector, 0.02f);
              } catch (IllegalAccessException | InvocationTargetException ex) {
                Ostrov.log_warn("disguise " + type + " travelFlying : " + ex.getMessage());
              }
            }
            default -> {
              //if (inFluid) {
              //travelInFluid(travelVector);
              nmsLe.travel(travelVector);
              //} else {
              //nmsLe.travel(travelVector);
//Ostrov.log_warn(nmsLe.getSpeed()+"("+sp.getAttributeValue(Attributes.MOVEMENT_SPEED)+"/"+sp.getAbilities().flyingSpeed+") v="+travelVector);
              //}
            }
          }
//nmsLe.move(MoverType.SELF, travelVector);

          //nmsLe.travel(travelVector);
        }

        //расчёт коорд глаз
        Vec3 eyePos = nmsLe.position().add(0, eyeHeight, 0);
        Vec3 view = nmsLe.calculateViewVector(nmsLe.getXRot(), nmsLe.getYRot()).normalize();
        Vec3 bbWidthV = view.multiply(bbWidth, bbWidth, bbWidth);
        Vec3 faceDeltaV = view.multiply(faceDelta, faceDelta, faceDelta);
        Vec3 end = eyePos.add(bbWidthV);
        //Location loc = new Location(nmsLe.level().getWorld(), end.x(), end.y(), end.z(), 0, 0);
        //loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0, 0, 0, 0);

        ClipContext cc = new ClipContext(eyePos, end,
            ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty());
        HitResult hitResult = nmsLe.level().clip(cc);

        Vec3 face;
        if (hitResult.getType() == HitResult.Type.BLOCK) { //глаза упираются в блок
//Ostrov.log_warn("hitResult ="+hitResult.getType());
          Vec3 hit = hitResult.getLocation();
          Vec3 back = view.multiply(.2, .2, .2);
          face = hit.subtract(back);
        } else {
          face = eyePos.add(faceDeltaV);
        }

        //расчёт точки надо головой
        double dx = face.x() - nmsVehicle.getX();
        double dy = face.y() - nmsVehicle.getY() - 2.1;
        double dz = face.z() - nmsVehicle.getZ();

        double dist = dx * dx + dy * dy + dz * dz;

//Ostrov.log_warn("dist="+(dist));
        if (dist > 3E-15) {
          //if (Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01 || Math.abs(dz) > 0.01) {
//Ostrov.log_warn(type+" bb="+bbWidth+" d="+halfBbWidth);
          Vec3 m = new Vec3(dx, dy, dz);
          nmsVehicle.setDeltaMovement(m);
          nmsVehicle.move(MoverType.SELF, m); //обновить координаты игрока
          //sp.setDeltaMovement(m);
          //sp.hurtMarked = true;
          //sp.move(MoverType.SELF, m); //обновить координаты игрока
        } else {
//Ostrov.log_warn("zero");
          nmsVehicle.setDeltaMovement(Vec3.ZERO);
        }

      }
    }.runTaskTimer(Ostrov.instance, 1, 1);
  }


  public static void onEntitiesLoadEvent(final EntitiesLoadEvent e) {
    for (final org.bukkit.entity.Entity ent : e.getWorld().getEntities()) {
      if (ent instanceof final org.bukkit.entity.LivingEntity le) {
        if (le.getPersistentDataContainer().has(DISG)) {
          ent.remove();
        }
      }
    }
  }


  public void action(PlayerDisguiseEvent.DisguiseAction action) {
    //if (1==1) return;
    Ostrov.log_warn("action=" + action);
    Ostrov.sync(() -> {
      final Player p = op.getPlayer();
      PlayerDisguiseEvent event = new PlayerDisguiseEvent(p, this, action);
      Bukkit.getPluginManager().callEvent(event);
      if (action == PlayerDisguiseEvent.DisguiseAction.Shift && !event.isCanceled()) {
        SmartInventory.builder()
            .id("Disg" + p.getName())
            .provider(new DisgMenu())
            .size(1, 9)
            .title("§5Меню маскировки")
            .build().open(p);
      }
    });
  }

  //ServerboundInteractPacket для зрителя только лкп/пкм на энтити
  public void intercatAtEntity(int targetId, boolean attack) {
    if (tick - lastInteractTick < 5) return;
    lastInteractTick = tick;
    if (nmsEnt != null && nmsEnt instanceof LivingEntity le) {
      Entity target = sp.level().getEntity(targetId);
      if (attack) {
        Ostrov.log_warn("ЛКМ на энтити " + target.getType());
        PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(op.getPlayer(), this, PlayerDisguiseEvent.DisguiseAction.LeftClickOnEntity);
        disguiseEvent.target = target.getBukkitEntity();
        Bukkit.getPluginManager().callEvent(disguiseEvent);
        if (!disguiseEvent.isCanceled()) {
          le.doHurtTarget((ServerLevel) le.level(), target);
        }
      } else {
        Ostrov.log_warn("ПКМ на энтити " + target.getType());
        PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(op.getPlayer(), this, PlayerDisguiseEvent.DisguiseAction.RightClickOnEntity);
        disguiseEvent.target = target.getBukkitEntity();
        Bukkit.getPluginManager().callEvent(disguiseEvent);
        //action(PlayerDisguiseEvent.DisguiseAction.InteractEntity);
      }
    }
  }

  //ServerboundSwingPacket для зрителя только лкм куда-либо
  public void swing(InteractionHand hand) {
//Ostrov.log_warn("swing "+hand);
    if (nmsEnt != null && nmsEnt instanceof LivingEntity le) {
      //action(PlayerDisguiseEvent.DisguiseAction.SwingPacket);
      le.swing(hand, true);

      //final Player p = op.getPlayer();
      //double range = le.getAttributeValue(Attributes.FOLLOW_RANGE);//в TargetGoal берётся FOLLOW_RANGE
      //rt = p.getWorld().rayTrace(p.getEyeLocation(), p.getEyeLocation().getDirection(), range,
      //    FluidCollisionMode.NEVER, false, 0.1, en -> {return en.getEntityId() != p.getEntityId();});
      if (rt == null) {
        //ЛКМ в воздух
        PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(op.getPlayer(), this, PlayerDisguiseEvent.DisguiseAction.LeftClickOnAir);
        Bukkit.getPluginManager().callEvent(disguiseEvent);
        Ostrov.log_warn("ЛКМ в воздух range d=");
      } else if (rt.getHitEntity() != null) {
        //ЛКМ на энтити - не надо
      } else if (rt.getHitBlock() != null) {
        //ЛКМ на блок
        Ostrov.log_warn("ЛКМ на блок " + rt.getHitBlock().getType() + " range =");
        PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(op.getPlayer(), this, PlayerDisguiseEvent.DisguiseAction.LeftClickOnBlock);
        disguiseEvent.block = rt.getHitBlock();
        Bukkit.getPluginManager().callEvent(disguiseEvent);
        if (!disguiseEvent.isCanceled()) {
          ///Level level = nmsEnt.level();
          switch (type) {
            case ZOMBIE -> {
              //Zombie zombie = (Zombie) nmsEnt;
              //zombie.;
            }

          }
        }
      }

    }
  }

  //ServerboundUseItemOnPacket пкм на блок
  public void intercatAtBlock(BlockHitResult hitResult) {
    //action(PlayerDisguiseEvent.DisguiseAction.IntercatAtBlock);
    if (nmsEnt != null && nmsEnt instanceof LivingEntity le) {
      PlayerDisguiseEvent disguiseEvent = new PlayerDisguiseEvent(op.getPlayer(), this, PlayerDisguiseEvent.DisguiseAction.RightClickOnBlock);
      disguiseEvent.block = op.getPlayer().getWorld().getBlockAt(hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ());
      Bukkit.getPluginManager().callEvent(disguiseEvent);
      Ostrov.log_warn("ПКМ на блок " + disguiseEvent.block.getType());
      if (!disguiseEvent.isCanceled()) {
        Level level = nmsEnt.level();
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(blockPos);
        switch (type) {
          case ENDERMAN -> {
            EnderMan enderman = (EnderMan) nmsEnt;
            if (enderman.getCarriedBlock() == null) {
              if (blockState.is(BlockTags.ENDERMAN_HOLDABLE)) {
                level.removeBlock(blockPos, false);
                level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(enderman, blockState));
                enderman.setCarriedBlock(blockState.getBlock().defaultBlockState());
              }
            } else {
              enderman.tick(); //там только постановка блока

            }
          }

        }

      }
    }
  }

  public static class DisgMenu implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).name("§8.").build());
    private static final ItemStack end = new ItemBuilder(ItemType.MUSIC_DISC_TEARS).name("§6Демаскировка").build();

    @Override
    public void init(final Player p, final InventoryContent contents) {
      p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, .5f, 5);
      contents.fillBorders(fill);

      final Oplayer op = PM.getOplayer(p.getUniqueId());

      contents.set(8, ClickableItem.of(end, e -> {
        if (e.isLeftClick()) {
          p.closeInventory();
          op.disguise.unDisguise();
        } else {
          p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
        }
      }));

      /*if (ApiOstrov.isLocalBuilder(p) && op.disguise.nmsEnt instanceof LivingEntity) {
        if (op.disguise.calibrate) {
          contents.set(0, ClickableItem.of(new ItemBuilder(ItemType.NOTE_BLOCK)
              .name("§6Сохранить калибровку")
              .build(), e -> {
            if (e.isLeftClick()) {
              op.disguise.calibrate = false;
              faces.put(op.disguise.type, op.disguise.faceDelta);
              cfg.set("faces." + op.disguise.type.name(), op.disguise.faceDelta);
              cfg.saveConfig();
              p.closeInventory();
              p.sendMessage("Калибровка для "+op.disguise.type+" сохранена.");
            }
          }));
        } else {
          contents.set(0, ClickableItem.of(new ItemBuilder(ItemType.COMPARATOR)
              .name("§6Калибровка глаз")
              .build(), e -> {
            if (e.isLeftClick()) {
              op.disguise.calibrate = true;
              p.closeInventory();
              p.sendMessage("Калибровка для "+op.disguise.type+" : клавишами W/S выбери оптимальное положение для глаз, затем сохрани в меню.");
            }
          }));
        }
      }*/

    }
  }

}


// кусочек подбирания из Player.aiStep
      /*   AABB aabb;
        //if (this.isPassenger() && !this.getVehicle().isRemoved()) {
        //  aabb = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0, 0.0, 1.0);
        //} else {
          aabb = nmsLe.getBoundingBox().inflate(1.0, 0.5, 1.0);
        //}

       List<Entity> entities = nmsLe.level().getEntities(nmsLe, aabb);
        //List<ItemEntity> entities = nmsLe.level().getEntitiesOfClass(ItemEntity.class, aabb, EntitySelector.LIVING_ENTITY_STILL_ALIVE);
        //List<Entity> list = Lists.newArrayList();
        for (Entity entity : entities) {
  Ostrov.log_warn("pickUpItem "+entity.getBukkitEntity().getType());
          if (entity.getType() == EntityType.EXPERIENCE_ORB) {
            //list.add(entity);
          } else if (!entity.isRemoved()) {
            //this.touch(entity); -> entity.playerTouch(this);
            //pickUpItem((Mob) nmsLe, entity);
          }
        }*/
//if (!list.isEmpty()) {
//  this.touch(Util.getRandom(list, this.random));
//}
//Location loc = new Location(nmsLe.level().getWorld(), end.x(), end.y(), end.z(), 0, 0);
//loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0, 0, 0, 0);




              /*int floor = Mth.floor(enderman.getX() - 1.0 + nextDouble() * 2.0);
              int floor1 = Mth.floor(enderman.getY() + random.nextDouble() * 2.0);
              int floor2 = Mth.floor(enderman.getZ() - 1.0 + random.nextDouble() * 2.0);
              BlockPos blockPos = new BlockPos(floor, floor1, floor2);
              BlockState blockState = level.getBlockStateIfLoaded(blockPos);
              if (blockState != null) {
                BlockPos blockPos1 = blockPos.below();
                BlockState blockState1 = level.getBlockState(blockPos1);
                BlockState carriedBlock = this.enderman.getCarriedBlock();
                if (carriedBlock != null) {
                  carriedBlock = Block.updateFromNeighbourShapes(carriedBlock, this.enderman.level(), blockPos);
                  if (this.canPlaceBlock(level, blockPos, carriedBlock, blockState, blockState1, blockPos1) && CraftEventFactory.callEntityChangeBlockEvent(this.enderman, blockPos, carriedBlock)) {
                    level.setBlock(blockPos, carriedBlock, 3);
                    level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Context.of(this.enderman, carriedBlock));
                    this.enderman.setCarriedBlock((BlockState)null);
                  }
                }
              }*/
       /*
        double blockHitDistanceSquared = rangeSq;
        double raySize = 1;

        Vec3 eyePos = sp.getEyePosition();
        Vec3 view = nmsLe.calculateViewVector(nmsLe.getXRot(), nmsLe.getYRot()).normalize().multiply(rangeSq, rangeSq, rangeSq);
        Vec3 end = eyePos.add(view);

        ClipContext cc = new ClipContext(eyePos, end,
            ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty());
        HitResult blockHit = nmsLe.level().clip(cc);
        if (blockHit != null) {
          blockHitDistanceSquared = blockHit.distanceTo(sp);
        }

        //BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(raySize);
        //Collection<org.bukkit.entity.Entity> entities = this.getNearbyEntities(aabb, filter);
        //org.bukkit.entity.Entity nearestHitEntity = null;
        LivingEntity nearestHitEntity = null;
        RayTraceResult rayTraceResult = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        Optional<Vec3> optional = Optional.empty();

        AABB getTargetSearchArea = nmsLe.getBoundingBox().inflate(rangeSq, rangeSq, rangeSq);
        Iterator var14 = sp.level().getEntitiesOfClass(LivingEntity.class, getTargetSearchArea, (livingEntity2) -> {
          return true;//livingEntity2.getType().is(types);
        }).iterator();
        while(var14.hasNext()) {
          LivingEntity livingEntity1 = (LivingEntity)var14.next();
          /*if (targetingConditions.test(nmsLe.level(), source, livingEntity1)) {
            double d1 = livingEntity1.distanceToSqr(nmsLe.getX(), nmsLe.getY(), nmsLe.getZ());
            if (d1 < nearestDistanceSq) {
              nearestDistanceSq = d1;
              nearestHitEntity = livingEntity1;
            }
          }*/
          /* не даёт координату попадания, использовать RayTraceResult
          AABB aabb = livingEntity1.getBoundingBox().inflate(rangeSq);
          Optional<Vec3> optional1 = aabb.clip(eyePos, end);
          if (optional1.isPresent()) {
            double d1 = eyePos.distanceToSqr((Vec3)optional1.get());
            if (d1 < nearestDistanceSq) {
              nearestHitEntity = livingEntity1;
              nearestDistanceSq = d1;
              optional = optional1;
            }
          }/
          org.bukkit.entity.Entity entity = livingEntity1.getBukkitEntity();//(org.bukkit.entity.Entity)var16.next();
          BoundingBox boundingBox = entity.getBoundingBox().expand(raySize);
          Vector startPos = new Vector(eyePos.x, eyePos.y, eyePos.z);
          RayTraceResult hitResult = boundingBox.rayTrace(startPos, view, rangeSq);
          if (hitResult != null) {
            double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());
            if (distanceSq < nearestDistanceSq) {
              nearestHitEntity = entity;
              rayTraceResult = hitResult;
              nearestDistanceSq = distanceSq;
            }
          }
        }

        hitResult = null;
        if (blockHit == null) {
          hitResult = new EntityHitResult(nearestHitEntity);
          //return entityHit;
        } else if (rayTraceResult == null) {
          hitResult = blockHit;
          //return blockHit;
        } else {
          double entityHitDistanceSquared = nearestHitEntity.distanceTo(sp);//startVec.distanceSquared(entityHit.getHitPosition());
          //if (entityHitDistanceSquared < blockHitDistance * blockHitDistance) {
          if (entityHitDistanceSquared < blockHitDistanceSquared) {
            hitResult = new EntityHitResult(nearestHitEntity);
          } else {
            hitResult = blockHit;
          }
          //return entityHitDistanceSquared < blockHitDistance * blockHitDistance ? entityHit : blockHit;
        }*/








