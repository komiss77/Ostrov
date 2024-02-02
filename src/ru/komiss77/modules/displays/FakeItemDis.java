package ru.komiss77.modules.displays;

import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.mojang.math.Transformation;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.VM;

public class FakeItemDis extends BukkitRunnable {

	private static final float HGHT = 1.6f;
	private static final float WDTH = 0.6f;
	
	private final Player pl;
	private final Vector dv;
	private final ItemDisplay tds;
	private final int tdId;
	private final HashSet<FakeItemDis> anms;
	protected final Interaction ine;
	protected final Location olc;

	private float scale = 1f;
	private boolean showName = false, follow = false, rotate = false;
	private BiConsumer<Player, FakeItemDis> onClick = (pl, fid) -> {};
	private BiConsumer<Player, FakeItemDis> onLook = (pl, fid) -> {};
	private Predicate<Integer> isDone = tm -> false;
	
	private static final ItemStack stn = ItemStack.fromBukkitCopy(new org.bukkit.inventory.ItemStack(Material.STONE));
	
	protected FakeItemDis(final Player pl, final Location at) {
		this.pl = pl;
		this.olc = at;
		DisplayManager.animations.putIfAbsent(pl.getEntityId(), new HashSet<>());
		anms = DisplayManager.animations.get(pl.getEntityId());
		dv = at.toVector().subtract(pl.getEyeLocation().toVector());
		
		final WorldServer wm = VM.server().toNMS(at.getWorld());
		tds = new ItemDisplay(EntityTypes.ae, wm);
		tdId = tds.aj();
		tds.a(BillboardConstraints.b);
		tds.a(ItemDisplayContext.g);
		tds.a(stn);
		tds.setPosRaw(at.getX(), at.getY(), at.getZ(), false);
		
		ine = at.getWorld().spawn(at, Interaction.class);
		ine.customName(TCUtils.format(""));
		ine.setCustomNameVisible(false);
		ine.setInteractionHeight(HGHT);
		ine.setInteractionWidth(WDTH);
		ine.setResponsive(true);
		anms.add(this);
		
		final PacketPlayOutEntityDestroy dp = new PacketPlayOutEntityDestroy(ine.getEntityId());
		for (final Player op : pl.getWorld().getPlayers()) {
			if (op.getEntityId() != pl.getEntityId()) {
				VM.server().toNMS(op).c.a(dp);
			}
		}
	}
	
	public FakeItemDis setNameVis(final boolean vis) {
		showName = vis; return this;
	}
	
	public FakeItemDis setFollow(final boolean flw) {
		follow = flw; return this;
	}
	
	public FakeItemDis setRotate(final boolean rtt) {
		tds.a(rtt ? BillboardConstraints.a : BillboardConstraints.b);
		rotate = rtt; return this;
	}
	
	public FakeItemDis setIsDone(final Predicate<Integer> pr) {
		isDone = pr; return this;
	}
	
	public FakeItemDis setOnClick(final BiConsumer<Player, FakeItemDis> cn) {
		onClick = cn; return this;
	}

	@Deprecated
	public FakeItemDis setOnClick(final Consumer<Player> cn) {
		return setOnClick((pl, fid) -> cn.accept(pl));
	}

	protected void click(final Player pl) {
		onClick.accept(pl, this);
	}

	public FakeItemDis setOnLook(final BiConsumer<Player, FakeItemDis> cn) {
		onLook = cn; return this;
	}

	@Deprecated
	public FakeItemDis setOnLook(final Consumer<Player> cn) {
		return setOnLook((pl, fid) -> cn.accept(pl));
	}
	
	protected void look(final Player pl) {
		onLook.accept(pl, this);
	}

	public FakeItemDis setScale(final float sc) {
		this.scale = sc;
		tds.a(new Transformation(new Vector3f(), new Quaternionf(),
			new Vector3f(sc, sc, sc), new Quaternionf()));
		ine.teleport(olc.clone().add(0d, -1d * scale - 0.1d, 0d));
		ine.setInteractionHeight(HGHT * sc);
		ine.setInteractionWidth(WDTH * sc);
		return this;
	}
	
	public FakeItemDis setItem(final org.bukkit.inventory.ItemStack it) {
		tds.a(ItemStack.fromBukkitCopy(it));
		VM.server().toNMS(pl).c.a(new PacketPlayOutEntityMetadata(tdId, tds.an().c()));
		return this;
	}
	
	public FakeItemDis setName(final String nm) {
		ine.customName(TCUtils.format(nm)); return this;
	}
	
	public void create() {
		ine.setCustomNameVisible(showName);
		ine.teleport(olc.clone().add(0d, -1d * scale - 0.1d, 0d));
		final PlayerConnection pc = VM.server().toNMS(pl).c;
		pc.a(new PacketPlayOutSpawnEntity(tds));
		pc.a(new PacketPlayOutEntityMetadata(tdId, tds.an().c()));
		this.runTaskTimer(Ostrov.instance, 2, 1);
	}
	
	public void remove() {
		final PlayerConnection pc = VM.server().toNMS(pl).c;
		pc.a(new PacketPlayOutEntityDestroy(tdId));
		tds.a(RemovalReason.a);
//		if (setRem) anms.remove(this);
		ine.remove();
		cancel();
	}
	
	private int i = 0;
	
	@Override
	public void run() {
		/*final Location elc = PLAYER_EYE_LOC;
		final Location dlc = ENTITY_LOC.subtract(elc);
		final double ln = Math.sqrt(Math.pow(dlc.getX(), 2d) + Math.pow(dlc.getZ(), 2d));
		if (ln < MAX_DIST) {
			if (Math.pow(-Math.sin(Math.toRadians((180f - elc.getYaw()))) - dlc.getX() / ln, 2d) + 
				Math.pow(-Math.cos(Math.toRadians((180f - elc.getYaw()))) - dlc.getZ() / ln, 2d) < ( THRESHOLD (I used 0.16d) ) / (ln * ln)) {
				final double pty = elc.getY() + Math.tan(Math.toRadians(-elc.getPitch())) * ln - ENTITY_LOC.getY();
				if (pty < 2d && pty > 0) {
					//YES!
				}
			}
		}
		break;*/
		
		if (!ine.isValid() || !pl.isValid() || isDone.test(i++)) anms.remove(this);
		final PlayerConnection pc = VM.server().toNMS(pl).c;
		if (!anms.contains(this)) {
//			pl.sendMessage("remove ine- " + ine.isValid());
			pc.a(new PacketPlayOutEntityDestroy(tdId));
			tds.a(RemovalReason.a);
			ine.remove();
			cancel();
			return;
		}
		
		final int yaw = rotate ? i << 1 : 0;
		final Location elc = pl.getEyeLocation();
		if (follow) {
			final Location nls = elc.clone().add(dv);
			final Location ile = ine.getLocation();
			final Vector dlc = nls.toVector().subtract(new Vector(ile.getX(), ile.getY() + 1d, ile.getZ()));
			pc.a(new PacketPlayOutRelEntityMoveLook(tdId, (short) (dlc.getX() * 4096), (short) (dlc.getY() * 4096), 
				(short) (dlc.getZ() * 4096), (byte) (( yaw - 360 * (yaw / 180) ) * 0.7f), (byte) 0, true));
			ine.teleportAsync(new Location(nls.getWorld(), nls.getX(),
				nls.getY() - 1d * scale, nls.getZ(), nls.getYaw(), nls.getPitch()));
			
			boolean look = false;
			final double ln = Math.sqrt(Math.pow(dv.getX(), 2d) + Math.pow(dv.getZ(), 2d));
			if (ln < 6d) {
				if (Math.pow(-Math.sin(Math.toRadians((180f - elc.getYaw()))) - dv.getX() / ln, 2d) + 
					Math.pow(-Math.cos(Math.toRadians((180f - elc.getYaw()))) - dv.getZ() / ln, 2d) < 0.16d / (ln * ln)) {
					final double pty = elc.getY() + Math.tan(Math.toRadians(-elc.getPitch())) * ln - nls.getY();
					if (pty < 0.6d * scale && pty > -1d * scale) {
						look = true;
						look(pl);
					}
				}
			}
			
			ine.setCustomNameVisible(showName || look);
		} else {
			pc.a(new PacketPlayOutRelEntityMoveLook(tdId, (short) 0, (short) 0, (short) 0, 
				(byte) (( yaw - 360 * (yaw / 180) ) * 0.7f), (byte) 0, true));
			
			boolean look = false;
			final Location dlc = ine.getLocation().subtract(elc);
			final double ln = Math.sqrt(Math.pow(dlc.getX(), 2d) + Math.pow(dlc.getZ(), 2d));
			if (ln < 6d) {
				if (Math.pow(-Math.sin(Math.toRadians((180f - elc.getYaw()))) - dlc.getX() / ln, 2d) + 
					Math.pow(-Math.cos(Math.toRadians((180f - elc.getYaw()))) - dlc.getZ() / ln, 2d) < 0.16d / (ln * ln)) {
					final double pty = elc.getY() + Math.tan(Math.toRadians(-elc.getPitch())) * ln - ine.getLocation().getY();
					if (pty < 2d && pty > 0) {
						look = true;
						look(pl);
					}
				}
			}
			ine.setCustomNameVisible(showName || look);
		}
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o instanceof final FakeItemDis fd)
			return fd.tdId == tdId;
		return false;
	}
	
	@Override
	public int hashCode() {return tdId;}
}
