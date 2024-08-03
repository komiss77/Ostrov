package ru.komiss77.modules.bots;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.AStarPath;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.scoreboard.SubTeam;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.Craft;
import ru.komiss77.version.CustomTag;
import ru.komiss77.version.Nms;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


public class BotEntity extends ServerPlayer {

    public static final EntityDataAccessor<Byte> flags = DATA_SHARED_FLAGS_ID;

    private static final net.minecraft.world.item.ItemStack air
            = net.minecraft.world.item.ItemStack.fromBukkitCopy(ItemUtil.air);
    private static final String[] empty = new String[]{"", ""};

    public final World world;
    //    public final CustomScore score;
    public final CustomTag tag;
    public final SubTeam team;

    //private final PlayerInventory inv;
    private final PlayerInventory inv;

    public int rid;
    private boolean isDead;
    private WeakReference<LivingEntity> rplc;
    //    private String prefix, affix, suffix;
    public static final double DHIT_DST_SQ = 4d;
    public static final int PARRY_TICKS = 40;
    public static final int BASH_TICKS = 40;

    protected BotEntity(final String name, final World world) {
        super(MinecraftServer.getServer(), Craft.toNMS(world), getProfile(name), ClientInformation.createDefault());
        this.name = name;
        this.world = world;
        rid = -1;

        lastBash = -BASH_TICKS;
        lastParry = -PARRY_TICKS;
        rplc = new WeakReference<>(null);
        inv = Craft.fromNMS(getInventory());
        tag = new CustomTag(getBukkitEntity());
        team = new SubTeam(name).include(name)
                .tagVis(Team.OptionStatus.NEVER).seeInvis(false);
        team.send(world);
        BotManager.botByName.put(name, this);
    }

    private static GameProfile getProfile(final String name) {
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        final String[] skin = BotManager.skin.getOrDefault(name, empty);
        gameProfile.getProperties().put("textures", new Property("textures", skin[0], skin[1]));
        return gameProfile;
    }

    public int lastBusy;

    public boolean busy(final LivingEntity mb, @Nullable final Boolean set, final int tks) {
        if (set != null) {
            lastBusy = set ? mb.getTicksLived() : -tks;
        }
        return mb.getTicksLived() - lastBusy < tks;
    }

    public boolean block(final LivingEntity mb) {
        return false;
    }

    public void block(final LivingEntity mb, final boolean set) {
//		lastBash = is ? mb.getTicksLived() : -BASH_TICKS;
    }

    private int lastBash;

    public boolean bash(final LivingEntity mb) {
        return mb.getTicksLived() - lastBash < BASH_TICKS;
    }

    public void bash(final LivingEntity mb, final boolean set) {
        lastBash = set ? mb.getTicksLived() : -BASH_TICKS;
    }

    private int lastParry;

    public boolean parry(final LivingEntity mb) {
        return mb.getTicksLived() - lastParry < PARRY_TICKS;
    }

    public void parry(final LivingEntity mb, final boolean set) {
        if (set) {
            world.playSound(mb.getEyeLocation(), Sound.BLOCK_AMETHYST_CLUSTER_PLACE, 1f, 0.6f);
            world.spawnParticle(Particle.ELECTRIC_SPARK, mb.getLocation().add(0d, 1.2d, 0d), 24, 0.4d, 0.5d, 0.4d, -0.25d);
            lastParry = mb.getTicksLived();
        } else {
            lastParry = -PARRY_TICKS;
        }
    }

    public void hurt(final LivingEntity mb) {
        final ClientboundHurtAnimationPacket packet = new ClientboundHurtAnimationPacket(this);
        Nms.sendWorldPackets(world, packet);
        //VM.server().sendWorldPackets(world, new ClientboundHurtAnimationPacket(this));
        world.playSound(mb.getLocation(), Sound.ENTITY_GENERIC_HURT, 1f, 1.2f);
    }


    public void attack(final LivingEntity from, final Entity to, final boolean ofh) {
        if (ofh) {
            final EntityEquipment eq = from.getEquipment();
            final ItemStack it = eq.getItemInMainHand();
            eq.setItemInMainHand(eq.getItemInOffHand(), true);
            from.attack(to);
            world.playSound(from, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 0.8f);
            eq.setItemInOffHand(eq.getItemInMainHand(), true);
            eq.setItemInMainHand(it, true);
            Nms.sendWorldPackets(world, new ClientboundAnimatePacket(this, 3));// VM.server().sendWorldPackets(world, new PacketPlayOutAnimation(this, 3));
        } else {
            from.attack(to);
            world.playSound(from, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 0.8f);
            Nms.sendWorldPackets(world, new ClientboundAnimatePacket(this, 0));//VM.server().sendWorldPackets(world, new PacketPlayOutAnimation(this, 0));
        }
    }

    @OverrideMe
    public void telespawn(final Location to, @Nullable final LivingEntity le) {
        Nms.sendWorldPackets(world, remListPlayerPacket(),
                new ClientboundRemoveEntitiesPacket(this.hashCode()));
        //VM.server().sendWorldPackets(world,
        //        new PacketPlayOutEntityDestroy(this.aj()),
        //        remListPlayerPacket());

        if (le == null || !le.isValid() || isDead) {
            BotManager.botById.remove(rid);
            isDead = false;
            final PigZombie pz = world.spawn(to, PigZombie.class, false, mb -> {
                mb.setVisibleByDefault(false);
                mb.setSilent(true);
                mb.setPersistent(true);
                mb.setRemoveWhenFarAway(false);
                mb.customName(TCUtils.format(name));
                mb.setCustomNameVisible(true);
                mb.setAdult();
            });
            this.rplc = new WeakReference<>(pz);
            this.rid = pz.getEntityId();
            Bukkit.getMobGoals().removeAllGoals(pz);
            Bukkit.getMobGoals().addGoal(pz, 0, getGoal(pz));
            BotManager.botById.put(rid, this);
            parry(pz, false);
            bash(pz, false);
            block(pz, false);
//			hs.teleportAsync(to);
        } else {
            le.teleportAsync(to);
        }

        try {
            this.setGameMode(GameType.SURVIVAL); //?? setGameMode просто отправляет пакет игроку причём с эвентом (follow usage), переделать на нужные методы
        } catch (NullPointerException e) {
        }

        setPosRaw(to.getX(), to.getY(), to.getZ(), true);
        Nms.sendWorldPackets(world,
                addListPlayerPacket(), //ADD_PLAYER, UPDATE_LISTED, UPDATE_DISPLAY_NAME
                modListPlayerPacket(), //UPDATE_GAME_MODE
                new ClientboundAddEntityPacket(this, 0, blockPosition()));
        swapToSlot(0);

//		final Vector vc = to.toVector();
//		pss.add(vc); pss.add(vc); pss.add(vc); pss.add(vc);
    }

    @OverrideMe
    public Goal<Mob> getGoal(final Mob org) {
        return new BotGoal(this);
    }

    private List<ClientboundPlayerInfoUpdatePacket.Entry> entryList() {
        //private List<ClientboundPlayerInfoUpdatePacket.b> entryList() {
        return List.of(new ClientboundPlayerInfoUpdatePacket.Entry(getUUID(), getGameProfile(),
                true, 1, gameMode.getGameModeForPlayer(), getTabListDisplayName(), Optionull.map(getChatSession(), RemoteChatSession::asData)));
        //return List.of(new ClientboundPlayerInfoUpdatePacket.b(cw(), fR(),
        //true, 1, e.b(), N(), Optionull.a(ab(), RemoteChatSession::a)));
    }

    private ClientboundPlayerInfoUpdatePacket addListPlayerPacket() {
        return new ClientboundPlayerInfoUpdatePacket(EnumSet.of(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,//ClientboundPlayerInfoUpdatePacket.a.a, //ADD_PLAYER
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,//ClientboundPlayerInfoUpdatePacket.a.d, //UPDATE_LISTED
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),//ClientboundPlayerInfoUpdatePacket.a.f), //UPDATE_DISPLAY_NAME
                entryList());
    }

    private ClientboundPlayerInfoUpdatePacket liveListPlayerPacket() {
        return new ClientboundPlayerInfoUpdatePacket(EnumSet.of(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,//ClientboundPlayerInfoUpdatePacket.a.a, //ADD_PLAYER
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,//ClientboundPlayerInfoUpdatePacket.a.d, //UPDATE_LISTED
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),//ClientboundPlayerInfoUpdatePacket.a.f), //UPDATE_DISPLAY_NAME
                entryList());
    }

    private ClientboundPlayerInfoUpdatePacket dieListPlayerPacket() {
        return new ClientboundPlayerInfoUpdatePacket(EnumSet.of(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE),
                entryList());
    }

    private ClientboundPlayerInfoUpdatePacket modListPlayerPacket() {
        return new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE), entryList()); //ClientboundPlayerInfoUpdatePacket.Action.c
    }

    private ClientboundPlayerInfoUpdatePacket updListPlayerPacket() {
        return new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME), entryList());//ClientboundPlayerInfoUpdatePacket.a.f)
    }

    private ClientboundPlayerInfoRemovePacket remListPlayerPacket() {
        return new ClientboundPlayerInfoRemovePacket(Arrays.asList(this.uuid));
    }



    public ItemStack item(final EquipmentSlot slot) {
        final LivingEntity mb = getEntity();
        return mb == null ? null : mb.getEquipment().getItem(slot);
    }

    public ItemStack item(final int slot) {
        return inv.getItem(slot);
    }

    public int getHandSlot() {
        return inv.getHeldItemSlot();
    }

    public void swapToSlot(final int slot) {
        try {
            inv.setHeldItemSlot(slot);
        } catch (NullPointerException e) {
        }
        final LivingEntity mb = getEntity();
        if (mb != null) {
            mb.getEquipment().setItem(EquipmentSlot.HAND, item(slot));
        }
        Nms.sendWorldPacket(world, new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
    }

    public void item(final ItemStack it, final EquipmentSlot slot) {
        inv.setItem(slot, it);
        final LivingEntity mb = getEntity();
        if (mb != null) {
            mb.getEquipment().setItem(slot, it);
        }
        Nms.sendWorldPacket(world, new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
    }

    public void item(final ItemStack it, final int slot) {
        inv.setItem(slot, it);
        if (slot == getHandSlot()) {
            final LivingEntity mb = getEntity();
            if (mb != null) {
                mb.getEquipment().setItem(EquipmentSlot.HAND, item(slot));
            }
            Nms.sendWorldPacket(world, new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
        }
    }

    public Inventory inv() {
        return inv;
    }

    public void clearInv() {
        inv.clear();
        final LivingEntity mb = getEntity();
        if (mb != null) {
            mb.getEquipment().clear();
        }
        Nms.sendWorldPacket(world, new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
    }

    @OverrideMe
    public void dropInv(final Location loc) {
    }

    public LivingEntity getEntity() {
        final LivingEntity mb = rplc.get();
        return mb == null || !mb.isValid() ? null : mb;
    }

    public boolean isDead() {
        return isDead;
    }

    public void die(@Nullable final LivingEntity mb) {
        try {
            this.setGameMode(GameType.SPECTATOR); //?? setGameMode просто отправляет пакет игроку причём с эвентом (follow usage), переделать на нужные методы
        } catch (NullPointerException e) {
        }

        isDead = true;
        if (mb != null) {
            BotManager.botById.remove(rid);
            mb.remove();
        }
        Nms.sendWorldPackets(world, new ClientboundRemoveEntitiesPacket(this.hashCode()),
                modListPlayerPacket(), new ClientboundRemoveEntitiesPacket(tag.tagEntityId));
    }

    public void remove() {
        BotManager.botByName.remove(name);
        BotManager.botById.remove(rid);
        die(getEntity());
        Nms.sendWorldPackets(world, remListPlayerPacket());// VM.server().sendWorldPackets(world, remListPlayerPacket());
        this.remove(RemovalReason.KILLED);//this.a(RemovalReason.a);
        team.remove(world);
    }

    private final String name;

    public String name() {
        return name;
    }

    public WXYZ getPos() {
        final BlockPos bp = this.blockPosition();//dm();
        return new WXYZ(world, bp.getX(), bp.getY(), bp.getZ());
    }

    public void tab(final String prefix, final String affix, final String suffix) {
        listName = PaperAdventure.asVanilla(TCUtils.format(prefix + affix + name + suffix));
        Nms.sendWorldPacket(world, updListPlayerPacket());
    }

    /*public void color(@Nullable final NamedTextColor color) {
      if (color == null) {
        setGlowingTag(false);
        team.color(NamedTextColor.WHITE);
      } else {
        setGlowingTag(true);
        team.color(color);
      }
      for (final World w : Bukkit.getWorlds()) team.send(w);
    }*/

    public void tag(final boolean show) {
        tag.visible(show);
    }

    public void tag(final String prefix, final String affix, final String suffix) {
        tag.content(prefix + affix + name + suffix);
    }

    public void setTagVis(final Predicate<Player> canSee) {
        tag.canSee(canSee);
    }

    public boolean isTagVisTo(final Player pl) {
        return tag.canSee(pl);
    }


    @OverrideMe
    public boolean isTagVisFor(final Player p) {
        return true;
    }

    @OverrideMe
    public boolean isSeenBy(final Player p) {
        return true;
    }

    public void removeAll(final Player pl) {
        Nms.sendPackets(pl, remListPlayerPacket(),
                new ClientboundRemoveEntitiesPacket(this.hashCode()));
        team.remove(pl);
        tag.hideTo(pl);
    }

    public void updateAll(final Player pl) {
//      pl.sendMessage("bot-" + name);
        Nms.sendPackets(pl, addListPlayerPacket(), modListPlayerPacket(), new ClientboundAddEntityPacket(this, 0, blockPosition()),
                new ClientboundTeleportEntityPacket(this), new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
        team.send(pl);
        tag.showTo(pl);
    }

    private List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> updateIts() {
        final LivingEntity le = getEntity();
        final net.minecraft.world.entity.EquipmentSlot[] eis = net.minecraft.world.entity.EquipmentSlot.values();
        if (le == null) {
            @SuppressWarnings("unchecked") final Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>[] its
                    = (Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>[]) new Pair<?, ?>[6];
            for (int i = its.length - 1; i >= 0; i--) {
                its[i] = Pair.of(eis[i], air);
            }
            return Arrays.asList(its);
        }
        final EquipmentSlot[] ess = EquipmentSlot.values();
        final EntityEquipment eq = le.getEquipment();
        @SuppressWarnings("unchecked") final Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>[] its
                = (Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>[]) new Pair<?, ?>[6];
        for (int i = its.length - 1; i >= 0; i--) {
            final ItemStack it = eq.getItem(ess[i]);
            its[i] = Pair.of(eis[i], net.minecraft.world.item.ItemStack.fromBukkitCopy(it));
        }
        return Arrays.asList(its);
    }

    @OverrideMe
    public void pickupIts(final Location loc) {
        /*for (final Item it : w.getEntitiesByClass(Item.class)) {
			//rplc.getWorld().getPlayers().get(0).sendMessage(loc.distanceSquared(it.getLocation()) + "");
			if (loc.distanceSquared(it.getLocation()) < 4d && it.getPickupDelay() == 0) {
				final ItemStack is = it.getItemStack();
				final Integer slot = pickToSlot.apply(is);
				if (slot == null || slot < 0) continue;
				final ItemStack pi = item(slot);
				if (!ItemUtils.isBlank(pi, false))
					w.dropItem(loc, pi);
				item(is, slot);
				it.remove();
			}
		}*/
    }

    public void move(final Location loc, final Vector vc, final boolean look) {
        if (look) loc.setDirection(vc);
        final Vec3 ps = this.position();
        this.moveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        final Vector vector = new Vector(loc.getX() - ps.x, loc.getY() - ps.y, loc.getZ() - ps.z);
        Nms.sendWorldPackets(world, new ClientboundRotateHeadPacket(this, (byte) (loc.getYaw() * 256 / 360)),
                new ClientboundMoveEntityPacket.PosRot(this.hashCode(), (short) (vector.getX() * 4096), (short) (vector.getY() * 4096),
                        (short) (vector.getZ() * 4096), (byte) (loc.getYaw() * 256 / 360), (byte) (loc.getPitch() * 256 / 360), false));
    }

    @OverrideMe
    public void onInteract(final PlayerInteractAtEntityEvent e) {
    }

    @OverrideMe
    public void onDamage(final EntityDamageEvent e) {
        hurt((LivingEntity) e.getEntity());
    }

    @OverrideMe
    public void onDeath(final EntityDeathEvent e) {
        e.getDrops().clear();
        final LivingEntity le = e.getEntity();
        le.getWorld().spawnParticle(Particle.CLOUD, le.getLocation()
                .add(0d, 1d, 0d), 20, 0.1d, 0.5d, 0.1d, 0.04d);
        die(le);
    }

    public void onBug() {
        remove();
    }

    private static class BotGoal implements Goal<Mob> {

        private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Ostrov.instance, "bot"));

        private final BotEntity bot;
        private final AStarPath arp;

        private BotGoal(final BotEntity bot) {
            this.bot = bot;
            this.arp = new AStarPath((Mob) bot.getEntity(), 1000, true);
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
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void tick() {
            final Mob rplc = (Mob) bot.getEntity();
            if (rplc == null || !rplc.isValid()) {
                return;
            }

            //Bukkit.broadcast(Component.text("le-" + rplc.name()));
            final Location loc = rplc.getLocation();
            final Location eyel = rplc.getEyeLocation();
            final Vector vc = eyel.getDirection();

//			if (bot.tryJump(loc, rplc, vc)) return;

            final Player pl = LocUtil.getClsChEnt(new WXYZ(loc, false), 200, Player.class, le -> true);
            if (pl == null) {
                return;
            }

            if (!arp.hasTgt()) {
                arp.setTgt(new WXYZ(pl.getLocation()));
            }
            arp.tickGo(1.5d);

            bot.move(loc, vc, true);
        }

        @Override
        public @NotNull
        GoalKey<Mob> getKey() {
            return key;
        }

        @Override
        public @NotNull
        EnumSet<GoalType> getTypes() {
            return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
        }
    }


}
