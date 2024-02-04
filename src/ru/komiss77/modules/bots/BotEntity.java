package ru.komiss77.modules.bots;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.AStarPath;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.objects.CustomTag;
import ru.komiss77.scoreboard.CustomScore;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.VM;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class BotEntity extends EntityPlayer {

    public int rid;
    private static final net.minecraft.world.item.ItemStack air
            = net.minecraft.world.item.ItemStack.fromBukkitCopy(ItemUtils.air);

    private static final DedicatedServer ds = VM.server().toNMS();

    public final World w;
//    public final CustomScore score;
    public final CustomTag tag;

    private boolean isDead;
    private WeakReference<LivingEntity> rplc;
//    private String prefix, affix, suffix;
    public static final double DHIT_DST_SQ = 4d;
    public static final int PARRY_TICKS = 40;
    public static final int BASH_TICKS = 40;
    private static final String[] empty = new String[]{"", ""};

    protected BotEntity(final String name, final World world) {
        super(ds, VM.server().toNMS(world), getProfile(name), ClientInformation.a());
        this.name = name;
        rid = -1;
        this.w = world;

        lastBash = -BASH_TICKS;
        lastParry = -PARRY_TICKS;
        rplc = new WeakReference<>(null);
        PlayerInventory pi = null;
        try {
            pi = (PlayerInventory) Class.forName(Bukkit.getServer().getClass().getPackageName()
                    + ".inventory.CraftInventoryPlayer").getConstructor(fS().getClass()).newInstance(fS());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        inv = pi;
//        prefix = ""; suffix = ""; affix = ChatLst.NIK_COLOR;
        tag = new CustomTag(getBukkitEntity());
        CustomScore.allStartTrack(name);
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
            w.playSound(mb.getEyeLocation(), Sound.BLOCK_AMETHYST_CLUSTER_PLACE, 1f, 0.6f);
            w.spawnParticle(Particle.ELECTRIC_SPARK, mb.getLocation().add(0d, 1.2d, 0d), 24, 0.4d, 0.5d, 0.4d, -0.25d);
            lastParry = mb.getTicksLived();
        } else {
            lastParry = -PARRY_TICKS;
        }
    }

    public void hurt(final LivingEntity mb) {
        VM.server().sendWorldPackets(w, new ClientboundHurtAnimationPacket(this));
        w.playSound(mb.getLocation(), Sound.ENTITY_GENERIC_HURT, 1f, 1.2f);
    }

    public void attack(final LivingEntity from, final Entity to, final boolean ofh) {
        if (ofh) {
            final EntityEquipment eq = from.getEquipment();
            final ItemStack it = eq.getItemInMainHand();
            eq.setItemInMainHand(eq.getItemInOffHand(), true);
            from.attack(to);
            w.playSound(from, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 0.8f);
            eq.setItemInOffHand(eq.getItemInMainHand(), true);
            eq.setItemInMainHand(it, true);
            VM.server().sendWorldPackets(w, new PacketPlayOutAnimation(this, 3));
        } else {
            from.attack(to);
            w.playSound(from, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 0.8f);
            VM.server().sendWorldPackets(w, new PacketPlayOutAnimation(this, 0));
        }
    }

    @OverrideMe
    public void telespawn(final Location to, @Nullable final LivingEntity le) {
        VM.server().sendWorldPackets(w,
                new PacketPlayOutEntityDestroy(this.aj()),
                remListPlayerPacket());

        if (le == null || !le.isValid() || isDead) {
            BotManager.botById.remove(rid);
            isDead = false;
            final Husk hs = (Husk) w.spawnEntity(to, EntityType.HUSK, false);
            this.rplc = new WeakReference<>(hs);
            this.rid = hs.getEntityId();
            hs.setSilent(true);
            hs.setPersistent(true);
            hs.setRemoveWhenFarAway(false);
            hs.customName(TCUtils.format(name));
            hs.setCustomNameVisible(true);
            Bukkit.getMobGoals().removeAllGoals(hs);
            Bukkit.getMobGoals().addGoal(hs, 0, getGoal(hs));
            BotManager.botById.put(rid, this);
            parry(hs, false);
            bash(hs, false);
            block(hs, false);
//			hs.teleportAsync(to);
        } else {
            le.teleportAsync(to);
        }

        try {
            this.a(EnumGamemode.a);
        } catch (NullPointerException e) {
        }
        setPosRaw(to.getX(), to.getY(), to.getZ(), true);
        VM.server().sendWorldPackets(w,
                addListPlayerPacket(), //ADD_PLAYER, UPDATE_LISTED, UPDATE_DISPLAY_NAME
                modListPlayerPacket(), //UPDATE_GAME_MODE
                new PacketPlayOutSpawnEntity(this),
                new PacketPlayOutEntityDestroy(rid));
        swapToSlot(0);

//		final Vector vc = to.toVector();
//		pss.add(vc); pss.add(vc); pss.add(vc); pss.add(vc);
    }

    @OverrideMe
    public Goal<Mob> getGoal(final Mob org) {
        return new BotGoal(this);
    }

    private ClientboundPlayerInfoUpdatePacket addListPlayerPacket() {
        return new ClientboundPlayerInfoUpdatePacket(EnumSet.of(
                ClientboundPlayerInfoUpdatePacket.a.a, //ADD_PLAYER
                ClientboundPlayerInfoUpdatePacket.a.d, //UPDATE_LISTED
                ClientboundPlayerInfoUpdatePacket.a.f), //UPDATE_DISPLAY_NAME
                List.of(this));
    }

    private ClientboundPlayerInfoUpdatePacket modListPlayerPacket() {
        return new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.c, this);
    }

    private ClientboundPlayerInfoUpdatePacket updListPlayerPacket() {
        return new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.f, this);
    }

    private ClientboundPlayerInfoRemovePacket remListPlayerPacket() {
        return new ClientboundPlayerInfoRemovePacket(Arrays.asList(this.ay));
    }

    private final PlayerInventory inv;

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
        VM.server().sendWorldPackets(w, new PacketPlayOutEntityEquipment(this.aj(), updateIts()));
    }

    public void item(final ItemStack it, final EquipmentSlot slot) {
        inv.setItem(slot, it);
        final LivingEntity mb = getEntity();
        if (mb != null) {
            mb.getEquipment().setItem(slot, it);
        }
        VM.server().sendWorldPackets(w, new PacketPlayOutEntityEquipment(this.aj(), updateIts()));
    }

    public void item(final ItemStack it, final int slot) {
        inv.setItem(slot, it);
        if (slot == getHandSlot()) {
            final LivingEntity mb = getEntity();
            if (mb != null) {
                mb.getEquipment().setItem(EquipmentSlot.HAND, item(slot));
            }
            VM.server().sendWorldPackets(w, new PacketPlayOutEntityEquipment(this.aj(), updateIts()));
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
        VM.server().sendWorldPackets(w, new PacketPlayOutEntityEquipment(this.aj(), updateIts()));
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
            this.a(EnumGamemode.d);
        } catch (NullPointerException e) {
        }
        isDead = true;
        if (mb != null) {
            BotManager.botById.remove(rid);
            mb.remove();
        }
        VM.server().sendWorldPackets(w,
                new PacketPlayOutEntityDestroy(this.aj()),
                modListPlayerPacket(), tag.killPacket());
    }

    public void remove() {
        BotManager.botByName.remove(name);
        BotManager.botById.remove(rid);
        die(getEntity());
        VM.server().sendWorldPackets(w,
                remListPlayerPacket());
        CustomScore.allStopTrack(name);
        this.a(RemovalReason.a);
    }

    private final String name;

    public String name() {
        return name;
    }

    public WXYZ getPos() {
        final BaseBlockPosition bp = dm();
        return new WXYZ(w, bp.u(), bp.v(), bp.w());
    }

    public void tab(final String prefix, final String affix, final String suffix) {
//        score.tab(prefix, affix, suffix);
        listName = PaperAdventure.asVanilla(TCUtils.format(prefix + affix + name + suffix));
        VM.server().sendWorldPackets(w, updListPlayerPacket());
    }

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

    @Deprecated
    public void updateTag(final String pfx, final String sfx, final char clr) {
        tag(pfx, Character.toString(clr), sfx);
    }

    @Deprecated
    public void updateTag(final char clr, final Player pl) {
        //VM.getNmsNameTag().updateTag(name, prefix, suffix, nameClr = clr, pl, p -> isTagVisFor(p));
    }

    @Deprecated
    public void updateTag(final Player pl) {
        //VM.getNmsNameTag().updateTag(name, prefix, suffix, nameClr, pl, p -> isTagVisFor(p));
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
        final NetworkManager nm = VM.server().toNMS(pl).c.c;
        nm.a(new PacketPlayOutEntityDestroy(this.aj()));
        nm.a(remListPlayerPacket());
        tag.hideTo(pl);
    }

    public void updateAll(final Player pl) {
        pl.sendMessage("bot-" + name);
        updateAll(VM.server().toNMS(pl).c.c);
        tag.showTo(pl);
    }

    @Deprecated(forRemoval = true)
    public void updateAll(final NetworkManager nm) {
        nm.a(new ClientboundBundlePacket(
                List.of(addListPlayerPacket(), modListPlayerPacket(), new PacketPlayOutSpawnEntity(this),
                        new PacketPlayOutEntityTeleport(this), new PacketPlayOutEntityDestroy(rid),
                        new PacketPlayOutEntityEquipment(this.aj(), updateIts()))));
    }

    private List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> updateIts() {
        final LivingEntity le = getEntity();
        final EnumItemSlot[] eis = EnumItemSlot.values();
        if (le == null) {
            @SuppressWarnings("unchecked")
            final Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[] its
                    = (Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[]) new Pair<?, ?>[6];
            for (int i = its.length - 1; i >= 0; i--) {
                its[i] = Pair.of(eis[i], air);
            }
            return Arrays.asList(its);
        }
        final EquipmentSlot[] ess = EquipmentSlot.values();
        final EntityEquipment eq = le.getEquipment();
        @SuppressWarnings("unchecked")
        final Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[] its
                = (Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>[]) new Pair<?, ?>[6];
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
        if (look) {
            loc.setDirection(vc);
        }
        final Vec3D ps = this.dk();
        this.b(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        //loc.getWorld().playSound(loc, Sound.ENTITY_SHEEP_STEP, 1f, 1.2f);
        final Vector dl = new Vector(loc.getX() - ps.c, loc.getY() - ps.d, loc.getZ() - ps.e);
        VM.server().sendWorldPackets(w,
                new PacketPlayOutEntityHeadRotation(this, (byte) (loc.getYaw() * 256 / 360)),
                new PacketPlayOutRelEntityMoveLook(this.aj(), (short) (dl.getX() * 4096), (short) (dl.getY() * 4096),
                        (short) (dl.getZ() * 4096), (byte) (loc.getYaw() * 256 / 360), (byte) (loc.getPitch() * 256 / 360), false));
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

            //Bukkit.broadcast(Component.text("le-" + rplc.getName()));
            final Location loc = rplc.getLocation();
            final Location eyel = rplc.getEyeLocation();
            final Vector vc = eyel.getDirection();

            vc.normalize();

            bot.move(loc, vc, true);

//			if (bot.tryJump(loc, rplc, vc)) return;
            vc.setY(0d);
            if (rplc.isInWater()) {
                rplc.setVelocity(rplc.getVelocity().setY(0.1d).add(vc.multiply(0.05d)));
            } else {
                if (rplc.isOnGround()) {

                    final Player pl = LocationUtil.getClsChEnt(new WXYZ(loc, false), 200, Player.class, le -> true);
                    if (pl == null) {
                        return;
                    }

                    if (!arp.hasTgt() || arp.isDone()) {
                        arp.setTgt(new WXYZ(pl.getLocation()));
                    }
                    arp.tickGo(1.5d);

                }
            }
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




    /*@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof BotEntity ? ((BotEntity) o).name.equals(name) : false;
	}*/
