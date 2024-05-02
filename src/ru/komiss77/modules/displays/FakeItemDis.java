package ru.komiss77.modules.displays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.TCUtils;

import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FakeItemDis extends BukkitRunnable {

	private static final float HGHT = 1.6f;
	private static final float WDTH = 0.6f;
	
	private final Player pl;
	private final Vector dv;
	private final HashSet<FakeItemDis> anms;
  private final ItemDisplay tds;
	protected final Interaction ine;
	protected final Location olc;

	private float scale = 1f;
	private boolean showName = false, follow = false, rotate = false;
	private BiConsumer<Player, FakeItemDis> onClick = (pl, fid) -> {};
	private BiConsumer<Player, FakeItemDis> onLook = (pl, fid) -> {};
	private Predicate<Integer> isDone = tm -> false;
	
	private static final ItemStack stn = new ItemStack(Material.STONE);
	
	protected FakeItemDis(final Player pl, final Location at) {
		this.pl = pl;
		this.olc = at;
		DisplayManager.animations.putIfAbsent(pl.getEntityId(), new HashSet<>());
		anms = DisplayManager.animations.get(pl.getEntityId());
		dv = at.toVector().subtract(pl.getEyeLocation().toVector());

		tds = at.getWorld().spawn(at, ItemDisplay.class, id -> {
      id.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GUI);
      id.setBillboard(Display.Billboard.VERTICAL);
      id.setVisibleByDefault(false);
      id.setItemStack(stn);
    });

    ine = at.getWorld().spawn(at, Interaction.class, in -> {
      in.customName(TCUtils.format(""));
      in.setCustomNameVisible(false);
      in.setVisibleByDefault(false);
      in.setInteractionHeight(HGHT);
      in.setInteractionWidth(WDTH);
      in.setResponsive(true);
    });

		anms.add(this);
	}
	
	public FakeItemDis setNameVis(final boolean vis) {
		showName = vis; return this;
	}
	
	public FakeItemDis setFollow(final boolean flw) {
		follow = flw; return this;
	}
	
	public FakeItemDis setRotate(final boolean rtt) {
    tds.setBillboard(rtt ? Display.Billboard.FIXED : Display.Billboard.VERTICAL);
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
		tds.setTransformation(new Transformation(new Vector3f(), new Quaternionf(),
			new Vector3f(sc, sc, sc), new Quaternionf()));
		ine.teleport(olc.clone().add(0d, -0.1d, 0d));
		ine.setInteractionHeight(HGHT * sc);
		ine.setInteractionWidth(WDTH * sc);
		return this;
	}
	
	public FakeItemDis setItem(final ItemStack it) {
		tds.setItemStack(it);
		return this;
	}
	
	public FakeItemDis setName(final String nm) {
		ine.customName(TCUtils.format(nm)); return this;
	}
	
	public void create() {
		ine.setCustomNameVisible(showName);
    ine.teleport(olc.clone().add(0d, -0.1d, 0d));
    pl.showEntity(Ostrov.instance, tds);
    pl.showEntity(Ostrov.instance, ine);
		this.runTaskTimer(Ostrov.instance, 2, 1);
	}
	
	public void remove() {
		tds.remove();
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
		if (!anms.contains(this)) {
			tds.remove();
			ine.remove();
			cancel();
			return;
		}
		
		final int yaw = rotate ? i << 1 : 0;
		final Location elc = pl.getEyeLocation();
		if (follow) {
			final Location nls = elc.clone().add(dv);
      nls.setYaw((yaw - 360 * (yaw / 180)) * 0.7f);
      tds.teleportAsync(nls);
      ine.teleportAsync(new Location(nls.getWorld(), nls.getX(),
        nls.getY() - 0.1d, nls.getZ(), nls.getYaw(), nls.getPitch()));
			
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
      tds.setRotation((yaw - 360 * (yaw / 180)) * 0.7f, 0f);
			
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
			return fd.tds.getEntityId() == tds.getEntityId();
		return false;
	}
	
	@Override
	public int hashCode() {return tds.getEntityId();}
}
