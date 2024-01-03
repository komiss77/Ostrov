package ru.komiss77.modules.displays;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityTypes;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.VM;

public class DisplayManager implements Initiable, Listener {
	
	public static final HashMap<Integer, HashSet<FakeItemDis>> animations = new HashMap<>();
	
	public DisplayManager() {
		reload();
	}

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }
    
	@Override
	public void reload() {
        HandlerList.unregisterAll(this);
    	if (Config.displays) {
    		Ostrov.log_ok("§2Дисплеи включены!");
        	
            Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
    	}
	}
	
	@Override
	public void onDisable() {
    	if (Config.displays) {
    		Ostrov.log_ok("§6Дисплеи выключены!");
    		
    		for (final HashSet<FakeItemDis> fis : animations.values()) {
    			for (final FakeItemDis id : fis) id.remove();
    			fis.clear();
    		}
    	}
	}
    
    /*@EventHandler
    public void onJoin(final BungeeDataRecieved e) {
//    	injectPlayer(e.getPlayer());
    }*/
	
	@EventHandler
	public void onQuit(final PlayerQuitEvent e) {
		rmvDis(e.getPlayer());
//    	removePlayer(e.getPlayer());
	}
	
	@EventHandler
	public void onSwitch(final PlayerChangedWorldEvent e) {
		rmvDis(e.getPlayer());
	}
	
	@EventHandler
	public void onClick(final PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof final Interaction ie) {
			final HashSet<FakeItemDis> ids = animations.get(e.getPlayer().getEntityId());
			if (ids == null) return;
			FakeItemDis fi = null;
			final int eid = ie.getEntityId();
			for (final FakeItemDis i : ids) {
				if (i.ine.getEntityId() == eid) {
					fi = i; break;
				}
			}
			if (fi != null) fi.click(e.getPlayer());
		}
	}
	
	@EventHandler
	public void onDmg(final EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof final Interaction ie && e.getDamager() instanceof final Player pl) {
			final HashSet<FakeItemDis> ids = animations.get(pl.getEntityId());
			if (ids == null) return;
			FakeItemDis fi = null;
			final int eid = ie.getEntityId();
			for (final FakeItemDis i : ids) {
				if (i.ine.getEntityId() == eid) {
					fi = i; break;
				}
			}
			if (fi != null) fi.click(pl);
		}
	}
	
	public static FakeItemDis fakeItemAnimate(final Player pl, final Location loc) {
		if (!Config.displays) {
			Ostrov.log_warn("Tried creating ItemAnimation while Displays are off");
			return null;
		}
		
		return new FakeItemDis(pl, loc);
	}
	
	public static void rmvDis(final Player pl) {
		final HashSet<FakeItemDis> ids = animations.remove(pl.getEntityId());
		if (ids == null) return;
		for (final FakeItemDis id : ids) id.remove();
		ids.clear();
	}
	
	public static boolean fakeTextAnimate(final Player pl, final Location loc, final String msg, 
		final boolean xray, final boolean shadow, final int showForSec, final boolean timer) {//можно кинуть в IServer
		if (!Config.displays) {
			Ostrov.log_warn("Tried creating TextAnimation while Displays are off");
			return false;
		}
		
		if (timer && Timer.has(pl, "FakeText")) {
			ApiOstrov.sendActionBarDirect(pl, "§eПодождите немного перед просмотром!");
			return false;
		}
		final int ln = 20 * showForSec + msg.length();
		Timer.add(pl, "FakeText", ln >> 5);
		
		final WorldServer wm = VM.getNmsServer().toNMS(loc.getWorld());
		final TextDisplay tds = new TextDisplay(EntityTypes.aX, wm);
		final int eid = tds.af();
		tds.e(true);
		tds.a(BillboardConstraints.b);
		tds.b(160);
		
	    byte flagBits = tds.t();//getFlags
	    flagBits = (byte)(shadow ? flagBits | 1 : flagBits & (1 ^ 0xFFFFFFFF));
	    flagBits = (byte)(xray ? flagBits | 2 : flagBits & (2 ^ 0xFFFFFFFF));
	    tds.d(flagBits);
		
		tds.setPosRaw(loc.getX(), loc.getY(), loc.getZ(), false);
		tds.c(IChatBaseComponent.a(""));
		
		final PlayerConnection pc = VM.getNmsServer().toNMS(pl).c;
		pc.a(new PacketPlayOutSpawnEntity(tds));
		pc.a(new PacketPlayOutEntityMetadata(eid, tds.aj().c()));
		new BukkitRunnable() {
			int i = 1;
			@Override
			public void run() {
				if ((i++) > ln || !pl.isValid() || pl.isSneaking()) {
					Timer.del(pl, "FakeText");
					pc.a(new PacketPlayOutEntityDestroy(eid));
					tds.a(RemovalReason.a);
					cancel();
					return;
				}
				
				if (i <= msg.length()) {
					tds.c(PaperAdventure.asVanilla(TCUtils.format(msg.substring(0, i))));
					pc.a(new PacketPlayOutEntityMetadata(eid, tds.aj().c()));
				}
			}
		}.runTaskTimer(Ostrov.instance, 0, 1);
		return true;
	}
}
