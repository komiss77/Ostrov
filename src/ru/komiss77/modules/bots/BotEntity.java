package ru.komiss77.modules.bots;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import com.destroystokyo.paper.entity.ai.Goal;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.scoreboard.CustomScore;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.VM;


public class BotEntity extends EntityPlayer {

    public int rid;
    private static final net.minecraft.world.item.ItemStack air
            = net.minecraft.world.item.ItemStack.fromBukkitCopy(ItemUtils.air);

    private static final DedicatedServer ds = VM.getNmsServer().toNMS();
    public final World world;
    private boolean isDead;
    private WeakReference<LivingEntity> rplc;
    //private String prefix, suffix;
    //private char nameClr;//	private Function<Mob, Goal<Mob>> goal;
    public static final double DHIT_DST_SQ = 4d;
    public static final int PARRY_TICKS = 40;
    public static final int BASH_TICKS = 40;//	
    public final CustomScore score;
    private static final String [] empty = new String []{"", ""};
    //private static int botID = 0;
//	protected Consumer<EntityDamageEvent> onDamage;
//	protected Consumer<EntityDeathEvent> onDeath;
//	private Predicate<Player> isTagVis;


    protected BotEntity(final String name, final World world) {
        super(ds, VM.getNmsServer().toNMS(world), getProfile(name));
        this.name = name;
        rid = -1;
        this.world = world;

        lastBash = -BASH_TICKS;
        lastParry = -PARRY_TICKS;
//		goal = m -> new BotGoal(this);
        rplc = new WeakReference<>(null);
        PlayerInventory pi = null;
        try {
            pi = (PlayerInventory) Class.forName(Bukkit.getServer().getClass().getPackageName()
                    + ".inventory.CraftInventoryPlayer").getConstructor(fN().getClass()).newInstance(fN());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        inv = pi;
        //prefix = "";
        //suffix = "";
        //nameClr = '7';
        score = new CustomScore(name);
        BotManager.nameBots.put(name, this);
        /*final Pair<String, String> pr = bt.txs[Main.srnd.nextInt(bt.txs.length)];
    	if (pr != null) {
        	this.fM().getProperties().put("textures", new Property("textures", pr.getFirst(), pr.getSecond()));
    	}*/
    }

    private static GameProfile getProfile(final String name) {
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        final String[] skin = BotManager.skin.getOrDefault(name, empty);
        gameProfile.getProperties().put("textures", new Property("textures", skin[0], skin[1]));
                //BotManager.skin.getOrDefault(name, ""),
                //BotManager.skinSignatures.getOrDefault(name, "")));
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
        BotManager.sendWrldPckts(this.dI(), new ClientboundHurtAnimationPacket(this));
        world.playSound(mb.getLocation(), Sound.ENTITY_GENERIC_HURT, 1f, 1.2f);
    }

    public void attack(final LivingEntity from, final Entity to, final boolean ofh) {
        if (ofh) {
            final EntityEquipment eq = from.getEquipment();
            final ItemStack it = eq.getItemInMainHand();
            eq.setItemInMainHand(eq.getItemInOffHand(), true);
            from.attack(to);
            world.playSound(from, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 0.8f);
//			to.setVelocity(to.getVelocity().multiply(4));
            eq.setItemInOffHand(eq.getItemInMainHand(), true);
            eq.setItemInMainHand(it, true);
            BotManager.sendWrldPckts(this.dI(), new PacketPlayOutAnimation(this, 3));
        } else {
            from.attack(to);
            world.playSound(from, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 0.8f);
//			to.setVelocity(to.getVelocity().multiply(4));
            BotManager.sendWrldPckts(this.dI(), new PacketPlayOutAnimation(this, 0));
        }
    }

    public void telespawn(final Location to, @Nullable final LivingEntity le) {
        BotManager.sendWrldPckts(this.dI(),
                new PacketPlayOutEntityDestroy(this.af()),
                remListPlayerPacket(this));

        if (le == null || !le.isValid() || isDead) {
            BotManager.rIdBots.remove(rid);
            isDead = false;
            final Husk hs = (Husk) world.spawnEntity(to, EntityType.HUSK, false);
            this.rplc = new WeakReference<LivingEntity>(hs);
            this.rid = hs.getEntityId();
            hs.setSilent(true);
            hs.setPersistent(true);
            hs.setRemoveWhenFarAway(false);
            hs.customName(TCUtils.format(name));
            hs.setCustomNameVisible(true);
            Bukkit.getMobGoals().removeAllGoals(hs);
            Bukkit.getMobGoals().addGoal(hs, 0, getGoal(hs));
            BotManager.rIdBots.put(rid, this);
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
        BotManager.sendWrldPckts(
                dI(),
                addListPlayerPacket(this), //ADD_PLAYER, UPDATE_LISTED, UPDATE_DISPLAY_NAME
                modListPlayerPacket(this), //UPDATE_GAME_MODE
                new PacketPlayOutNamedEntitySpawn(this),
                new PacketPlayOutEntityDestroy(rid));
        swapToSlot(0);
//		final Vector vc = to.toVector();
//		pss.add(vc); pss.add(vc); pss.add(vc); pss.add(vc);
    }

    public Goal<Mob> getGoal(final Mob org) {
        return new BotGoal(this);
    }

    private ClientboundPlayerInfoUpdatePacket addListPlayerPacket(final BotEntity be) {
        return new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(
                        ClientboundPlayerInfoUpdatePacket.a.a, //ADD_PLAYER
                        ClientboundPlayerInfoUpdatePacket.a.d, //UPDATE_LISTED
                        ClientboundPlayerInfoUpdatePacket.a.f), //UPDATE_DISPLAY_NAME
                Arrays.asList(be));
    }

    private ClientboundPlayerInfoUpdatePacket modListPlayerPacket(final BotEntity be) {
        return new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.c, be);
    }

    private ClientboundPlayerInfoRemovePacket remListPlayerPacket(final BotEntity be) {
        return new ClientboundPlayerInfoRemovePacket(Arrays.asList(be.ax));
    }

    private final PlayerInventory inv;

    public ItemStack item(final EquipmentSlot slot) {
        final LivingEntity mb = getEntity();
        if (mb == null) {
            return null;
        }
        return mb.getEquipment().getItem(slot);
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
        BotManager.sendWrldPckts(this.dI(), new PacketPlayOutEntityEquipment(this.af(), updateIts()));
    }

    public void item(final ItemStack it, final EquipmentSlot slot) {
        inv.setItem(slot, it);
        final LivingEntity mb = getEntity();
        if (mb != null) {
            mb.getEquipment().setItem(slot, it);
        }
        BotManager.sendWrldPckts(this.dI(), new PacketPlayOutEntityEquipment(this.af(), updateIts()));
    }

    public void item(final ItemStack it, final int slot) {
        inv.setItem(slot, it);
        if (slot == getHandSlot()) {
            final LivingEntity mb = getEntity();
            if (mb != null) {
                mb.getEquipment().setItem(EquipmentSlot.HAND, item(slot));
            }
            BotManager.sendWrldPckts(this.dI(), new PacketPlayOutEntityEquipment(this.af(), updateIts()));
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
        BotManager.sendWrldPckts(this.dI(), new PacketPlayOutEntityEquipment(this.af(), updateIts()));
    }

    public void dropInv(final Location loc) {
        /*int slot = 0;
		for (final ItemStack it : inv) {
			if (it != null && ifSlotItem.test(slot, it)) {
				w.dropItem(loc, it);
				drop = true;
			}
			slot++;
		}*/
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
            BotManager.rIdBots.remove(rid);
            mb.remove();
        }
        BotManager.sendWrldPckts(this.dI(),
                new PacketPlayOutEntityDestroy(this.af()),
                modListPlayerPacket(this));
    }

    public void remove() {
        BotManager.nameBots.remove(name);
        BotManager.rIdBots.remove(rid);
        die(getEntity());
        BotManager.sendWrldPckts(this.dI(),
                remListPlayerPacket(this));
        score.onQuit();
        this.a(RemovalReason.a);
    }

    private final String name;

    public String name() {
        return name;
    }

    public WXYZ getPos() {
        final BaseBlockPosition bp = di();
        return new WXYZ(world, bp.u(), bp.v(), bp.w());
    }


    
    
    public void tag(final boolean show) {
        score.tag(show);
    }
    
    public void tag(final String prefix, final String color, final String suffix) {
        score.tag(Component.text(prefix), color, Component.text(suffix));
        //VM.getNmsNameTag().updateTag(name, prefix = pfx, suffix = sfx, nameClr = clr, world.getPlayers(), p -> isTagVisFor(p));
    }

    
    
    
    @Deprecated
    public void updateTag(final String pfx, final String sfx, final char clr) {
        tag(pfx, Character.toString(clr), sfx);
        //VM.getNmsNameTag().updateTag(name, prefix = pfx, suffix = sfx, nameClr = clr, world.getPlayers(), p -> isTagVisFor(p));
    }
        
    public void updateTag(final char clr, final Player pl) {
        //VM.getNmsNameTag().updateTag(name, prefix, suffix, nameClr = clr, pl, p -> isTagVisFor(p));
    }

    public void updateTag(final Player pl) {
        //VM.getNmsNameTag().updateTag(name, prefix, suffix, nameClr, pl, p -> isTagVisFor(p));
    }

    public boolean isTagVisFor(final Player p) {
        return true;
    }

    public boolean isSeenBy(final Player p) {
        return true;
    }

    public void removeAll(final Player pl) {
        final NetworkManager nm = VM.getNmsServer().toNMS(pl).c.h;
        nm.a(new PacketPlayOutEntityDestroy(this.af()));
        nm.a(remListPlayerPacket(this));
    }

    public void updateAll(final Player pl) {
        updateAll(VM.getNmsServer().toNMS(pl).c.h);
        updateTag(pl);
    }

    @Deprecated(forRemoval = true)
    public void updateAll(final NetworkManager nm) {
        nm.a(addListPlayerPacket(this));
        nm.a(modListPlayerPacket(this));
        nm.a(new PacketPlayOutNamedEntitySpawn(this));
        nm.a(new PacketPlayOutEntityTeleport(this));
        nm.a(new PacketPlayOutEntityDestroy(rid));
        nm.a(new PacketPlayOutEntityEquipment(this.af(), updateIts()));
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
            its[i] = Pair.of(eis[i], it == null ? air : net.minecraft.world.item.ItemStack.fromBukkitCopy(it));
        }
        return Arrays.asList(its);
    }

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
        final Vec3D ps = this.dg();
        this.b(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        //loc.getWorld().playSound(loc, Sound.ENTITY_SHEEP_STEP, 1f, 1.2f);
        final Vector dl = new Vector(loc.getX() - ps.c, loc.getY() - ps.d, loc.getZ() - ps.e);
        BotManager.sendWrldPckts(this.dI(),
                new PacketPlayOutEntityHeadRotation(this, (byte) (loc.getYaw() * 256 / 360)),
                new PacketPlayOutRelEntityMoveLook(this.af(), (short) (dl.getX() * 4096), (short) (dl.getY() * 4096), (short) (dl.getZ() * 4096), (byte) (loc.getYaw() * 256 / 360), (byte) (loc.getPitch() * 256 / 360), false));
    }

    public void onDamage(final EntityDamageEvent e) {
        hurt((LivingEntity) e.getEntity());
    }

    public void onDeath(final EntityDeathEvent e) {
        die(e.getEntity());
    }

    public void onBug() {
        remove();
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