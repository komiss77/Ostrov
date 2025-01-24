package ru.komiss77.modules.bots;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import com.destroystokyo.paper.entity.ai.Goal;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.math.FinePosition;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
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
import ru.komiss77.Ostrov;
import ru.komiss77.modules.entities.PvPManager;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.scoreboard.SubTeam;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Craft;
import ru.komiss77.version.CustomTag;
import ru.komiss77.version.Nms;


public class BotEntity extends ServerPlayer implements Botter {

    public static final EntityDataAccessor<Byte> flags = DATA_SHARED_FLAGS_ID;
    public static final int BOT_ID = 1337;//random val

    private static final net.minecraft.world.item.ItemStack air
        = net.minecraft.world.item.ItemStack.fromBukkitCopy(ItemUtil.air);
    private static final String[] empty = new String[]{"", ""};

    private final World world;
    private final Extent ext;
    //    protected final CustomScore score;
    private final CustomTag tag;
    private final SubTeam team;

    //private final PlayerInventory inv;
    private final PlayerInventory inv;

    private int rid;
    private boolean isDead;
    private GameType currMode;
    private WeakReference<LivingEntity> rplc;

    protected BotEntity(final String name, final World world, final Extent ext) {
        super(MinecraftServer.getServer(), Craft.toNMS(world), getProfile(name), ClientInformation.createDefault());
        this.name = name;
        this.world = world;
        this.ext = ext;
        rid = -1;
        currMode = GameType.SPECTATOR;
        rplc = new WeakReference<>(null);
        inv = Craft.fromNMS(getInventory());
        tag = new CustomTag(getBukkitEntity());
        team = new SubTeam(name).include(name)
            .tagVis(Team.OptionStatus.NEVER).seeInvis(false);
        team.send(world);

        BotManager.botByName.put(name, this);
        ext.create(this);
    }

    protected BotEntity(final String name, final World world, final Function<Botter, Extent> exs) {
        super(MinecraftServer.getServer(), Craft.toNMS(world), getProfile(name), ClientInformation.createDefault());
        this.name = name;
        this.world = world;
        this.ext = exs.apply(this);
        rid = -1;

        rplc = new WeakReference<>(null);
        inv = Craft.fromNMS(getInventory());
        tag = new CustomTag(getBukkitEntity());
        team = new SubTeam(name).include(name)
            .tagVis(Team.OptionStatus.NEVER).seeInvis(false);
        team.send(world);

        BotManager.botByName.put(name, this);
        ext.create(this);
    }

    private static GameProfile getProfile(final String name) {
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        final String[] skin = BotManager.skin.getOrDefault(name, empty);
        gameProfile.getProperties().put("textures", new Property("textures", skin[0], skin[1]));
        return gameProfile;
    }

    /*@ApiStatus.Internal
    public int botId() {
        return getId();
    }*/

    public int rid() {
        return rid;
    }

    public World world() {
        return world;
    }

    public Extent extent() {
        return ext;
    }

    public <E extends Extent> E extent(final Class<E> cls) {
        try {
            return ext.getClass().isAssignableFrom(cls) ? cls.cast(ext) : null;
        } catch (IllegalArgumentException | SecurityException ex) {
            Ostrov.log_err("BotEntity extent : " + ex.getMessage());
            return null;
        }
    }

    private static final byte USE_MAIN = 1, USE_OFF = 3, USE_STOP = 0, SNEAK = 1;
    private static final int NO_USE = -1;
    private int lastUseTick = NO_USE;
    private ItemType lastType = ItemType.AIR;
    private EquipmentSlot lastHand = EquipmentSlot.HAND;

    public void startUse(final LivingEntity mb, final EquipmentSlot hand) {
        final byte data;
        switch (hand) {
            case HAND:
                data = USE_MAIN;
                break;
            case OFF_HAND:
                data = USE_OFF;
                break;
            default:
                Ostrov.log_warn("BotEntity tried using non-hand");
                return;
        }
        if (hand != lastHand || lastUseTick == NO_USE) {
            this.entityData.set(DATA_LIVING_ENTITY_FLAGS, data, true);
            lastUseTick = mb.getTicksLived();
            lastHand = hand;
            final ItemStack hit = item(hand);
            lastType = hit == null ? ItemType.AIR : hit.getType().asItemType();
            Nms.sendWorldPackets(world, new ClientboundSetEntityDataPacket(this.getId(),
                List.of(entityData.getItem(DATA_LIVING_ENTITY_FLAGS).value())));
        }
    }

    public void stopUse(final LivingEntity mb) {
        if (lastUseTick != NO_USE) {
            lastUseTick = NO_USE;
            lastType = ItemType.AIR;
            lastHand = EquipmentSlot.HAND;
            this.entityData.set(DATA_LIVING_ENTITY_FLAGS, USE_STOP, true);
            Nms.sendWorldPackets(world, new ClientboundSetEntityDataPacket(this.getId(),
                List.of(entityData.getItem(DATA_LIVING_ENTITY_FLAGS).value())));
        }
    }

    public int useTicks(final LivingEntity mb) {
        if (lastUseTick == NO_USE) return NO_USE;
        return mb.getTicksLived() - lastUseTick + 1;
    }

    public void useTicks(final LivingEntity mb, final int ticks) {
        if (ticks == lastUseTick) return;
        lastUseTick = ticks + 1;
    }

    public ItemType usedType() {
        return lastType;
    }

    public EquipmentSlot usedHand() {
        return lastHand;
    }

    public boolean isBlocking(final LivingEntity mb) {
        return ItemType.SHIELD.equals(lastType) && useTicks(mb) > PvPManager.BLCK_CLD;
    }

    public boolean isParrying(final LivingEntity mb) {
        return PvPManager.CAN_PARRY.contains(lastType) && useTicks(mb) != 0;
    }

    public void parrying(final LivingEntity mb, final boolean set) {
        if (PvPManager.CAN_PARRY.contains(item(EquipmentSlot.HAND))) {
            if (set) {
                world.playSound(mb.getEyeLocation(), Sound.BLOCK_AMETHYST_CLUSTER_PLACE, 1f, 0.6f);
                world.spawnParticle(Particle.ELECTRIC_SPARK, mb.getLocation().add(0d, 1.2d, 0d), 24, 0.4d, 0.5d, 0.4d, -0.25d);
                startUse(mb, EquipmentSlot.HAND);
            } else if (lastHand == EquipmentSlot.HAND) {
                stopUse(mb);
            }
        }
    }

    public void hurt(final LivingEntity mb) {
        final ClientboundHurtAnimationPacket packet = new ClientboundHurtAnimationPacket(this);
        Nms.sendWorldPackets(world, packet);
        //VM.server().sendWorldPackets(world, new ClientboundHurtAnimationPacket(this));
        world.playSound(mb.getLocation(), Sound.ENTITY_GENERIC_HURT, 1f, 1.2f);
    }

    public void swingHand(final boolean main) {
        Nms.sendWorldPackets(world, new ClientboundAnimatePacket(this, main ? 0 : 3));
        this.swinging = false;
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
            if (lastHand == EquipmentSlot.OFF_HAND) stopUse(from);
        } else {
            from.attack(to);
            world.playSound(from, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 0.8f);
            Nms.sendWorldPackets(world, new ClientboundAnimatePacket(this, 0));//VM.server().sendWorldPackets(world, new PacketPlayOutAnimation(this, 0));
            if (lastHand == EquipmentSlot.HAND) stopUse(from);
        }
    }

    protected void teleport(final LivingEntity mb, final Location to) {
        Nms.sendWorldPackets(world, new ClientboundRemoveEntitiesPacket(this.hashCode()));
        setPosRaw(to.getX(), to.getY(), to.getZ(), true);
        Nms.sendWorldPackets(world, addEntityPacket(to),
            new ClientboundTeleportEntityPacket(getId(), PositionMoveRotation.of(this), Relative.DELTA, true),
            new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
        tag(true); tagThru(true);
        ext.teleport(this, mb);
    }

    public LivingEntity telespawn(final @Nullable LivingEntity mb, final Location to) {
        if (mb != null && mb.isValid()) {
            mb.teleportAsync(to);
            return mb;
        }

        Nms.sendWorldPackets(world, remListPlayerPacket(),
            new ClientboundRemoveEntitiesPacket(this.hashCode()));

        isDead = false;
        lastHand = EquipmentSlot.HAND;
        lastType = ItemType.AIR;
        lastUseTick = NO_USE;
        BotManager.botById.remove(rid);
        final Vindicator vc = world.spawn(to, Vindicator.class, false, v -> {
            v.setVisibleByDefault(false);
            v.setSilent(true);
            v.setPersistent(true);
            v.setRemoveWhenFarAway(false);
            v.customName(TCUtil.form(name));
            v.setCustomNameVisible(true);
            v.setMaximumAir(BOT_ID);
        });
        this.rplc = new WeakReference<>(vc);
        this.rid = vc.getEntityId();
        Bukkit.getMobGoals().removeAllGoals(vc);
        Bukkit.getMobGoals().addGoal(vc, 0, goal(vc));
        BotManager.botById.put(rid, this);
        currMode = GameType.SURVIVAL;

        setPosRaw(to.getX(), to.getY(), to.getZ(), true);
//        Bukkit.getConsoleSender().sendMessage("pos2-" + position().toString());
        Nms.sendWorldPackets(world, addListPlayerPacket(), modListPlayerPacket(), addEntityPacket(to),
            new ClientboundTeleportEntityPacket(getId(), PositionMoveRotation.of(this), Relative.DELTA, true),
            new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
        swapToSlot(0);
        ext.spawn(this, getEntity());
        tag(true); tagThru(true);
//        Ostrov.sync(() -> move(to.add(1, 1, 1)), 16);
        return vc;
    }

    @OverrideMe
    protected Goal<Mob> goal(final Mob org) {
        return ext.goal(this, org);
    }

    private List<ClientboundPlayerInfoUpdatePacket.Entry> entryList() {
        return List.of(new ClientboundPlayerInfoUpdatePacket.Entry(getUUID(), getGameProfile(), true, 1,
            currMode, getTabListDisplayName(), true, 0, Optionull.map(getChatSession(), RemoteChatSession::asData)));
    }

    private ClientboundAddEntityPacket addEntityPacket(final FinePosition fp) {
        return new ClientboundAddEntityPacket(getId(), getUUID(), fp.x(), fp.y(), fp.z(),
            getXRot(), getYRot(), getType(), 0, getDeltaMovement(), getYHeadRot());
    }

    private ClientboundPlayerInfoUpdatePacket addListPlayerPacket() {
        return new ClientboundPlayerInfoUpdatePacket(EnumSet.of(
            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
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
        } catch (NullPointerException e) {}
        final LivingEntity mb = getEntity();
        if (mb != null) {
            mb.getEquipment().setItem(EquipmentSlot.HAND, item(slot));
            if (lastHand == EquipmentSlot.HAND) stopUse(mb);
        }
        Nms.sendWorldPacket(world, new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
    }

    public void item(final EquipmentSlot slot, final ItemStack it) {
        inv.setItem(slot, it);
        final LivingEntity mb = getEntity();
        if (mb != null) {
            mb.getEquipment().setItem(slot, it);
            if (slot.isHand() && lastHand == slot) stopUse(mb);
        }
        Nms.sendWorldPacket(world, new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
    }

    public void item(final int slot, final ItemStack it) {
        inv.setItem(slot, it);
        if (slot == getHandSlot()) {
            final LivingEntity mb = getEntity();
            if (mb != null) {
                mb.getEquipment().setItem(EquipmentSlot.HAND, item(slot));
                if (lastHand == EquipmentSlot.HAND) stopUse(mb);
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

    public @Nullable LivingEntity getEntity() {
        final LivingEntity mb = rplc.get();
        return mb == null || !mb.isValid() ? null : mb;
    }

    public boolean isDead() {
        return isDead;
    }

    public void hide(@Nullable final LivingEntity mb) {
        ext.hide(this, mb);
        isDead = true;
        if (mb != null) {
            BotManager.botById.remove(rid);
            mb.remove();
        }
        currMode = GameType.SPECTATOR;
        Nms.sendWorldPackets(world, new ClientboundRemoveEntitiesPacket(this.hashCode()),
            modListPlayerPacket(), new ClientboundRemoveEntitiesPacket(tag.tagEntityId));
//            new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, GameType.SPECTATOR.getId())
//        gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
    }

    public void remove() {
        ext.remove(this);
        BotManager.botByName.remove(name);
        BotManager.botById.remove(rid);
        hide(getEntity());
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
        listName = PaperAdventure.asVanilla(TCUtil.form(prefix + affix + name + suffix));
        Nms.sendWorldPacket(world, updListPlayerPacket());
    }

    /*protected void color(@Nullable final NamedTextColor color) {
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

    public void tagThru(final boolean see) {
        tag.seeThru(see);
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

    public void removeAll(final Player pl) {
        Nms.sendPackets(pl, remListPlayerPacket(),
            new ClientboundRemoveEntitiesPacket(this.hashCode()));
        team.remove(pl);
        tag.hideTo(pl);
    }

    public void updateAll(final Player pl) {
//      pl.sendMessage("bot-" + name);
        Nms.sendPackets(pl, addListPlayerPacket(), modListPlayerPacket(), new ClientboundAddEntityPacket(this, 0, blockPosition()),
            new ClientboundTeleportEntityPacket(getId(), PositionMoveRotation.of(this), Relative.DELTA, true),
            new ClientboundSetEquipmentPacket(this.hashCode(), updateIts()));
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

    public void pickup(final Location loc) {
        ext.pickup(this, loc);
    }

    public void drop(final Location loc) {
        ext.drop(this, loc);
    }

    @Deprecated
    public void move(final Location loc, final Vector vc, final boolean look) {
        if (look) move(loc, vc); else move(loc);
    }

    public void move(final Location loc, final Vector vc) {
        loc.setDirection(vc); move(loc);
    }

    public void move(final Location loc) {
        final Vec3 ps = this.position();
        this.moveTo(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        final Vector vector = new Vector(loc.getX() - ps.x, loc.getY() - ps.y, loc.getZ() - ps.z);
//        Bukkit.getConsoleSender().sendMessage("dx=" + vector.getX() + " dy=" + vector.getY() + " dz=" + vector.getZ());
        Nms.sendWorldPackets(world, new ClientboundRotateHeadPacket(this, (byte) (loc.getYaw() * 256 / 360)),
            new ClientboundMoveEntityPacket.PosRot(this.hashCode(), (short) (vector.getX() * 4096), (short) (vector.getY() * 4096),
                (short) (vector.getZ() * 4096), (byte) (loc.getYaw() * 256 / 360), (byte) (loc.getPitch() * 256 / 360), false));
    }

    public void sneak(final boolean sneak) {
        setShiftKeyDown(sneak);
//        this.entityData.set(DATA_SHARED_FLAGS_ID, SNEAK, sneak);
//        Nms.sendWorldPackets(world, new ClientboundSetEntityDataPacket(this.getId(),
//            List.of(entityData.getItem(DATA_SHARED_FLAGS_ID).value())));
    }

    public void interact(final PlayerInteractAtEntityEvent e) {
        ext.click(this, e);
    }

    public void damage(final EntityDamageEvent e) {
        ext.damage(this, e);
    }

    public void death(final EntityDeathEvent e) {
        ext.death(this, e);
    }

    public void bug() {
        ext.bug(this);
    }
}
