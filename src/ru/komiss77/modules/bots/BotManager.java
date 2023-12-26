package ru.komiss77.modules.bots;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity.b;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.EntityHuman;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.version.VM;



public class BotManager implements Initiable, Listener {

    protected static final HashMap<Integer, BotEntity> rIdBots = new HashMap<>();
    protected static final HashMap<String, BotEntity> nameBots = new HashMap<>();
//	protected static final String[] names = readNames();
    
	public BotManager() {
    	
    	if (!Config.bots) {
    		Ostrov.log_ok("§6Боты выключены!");
    		return;
    	}
    	
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            injectPlayer(pl);
        }
        reload();
        
        new BukkitRunnable() {
			@Override
			public void run() {
				final ArrayList<BotEntity> rbs = new ArrayList<>();
				for (final Entry<Integer, BotEntity> en : rIdBots.entrySet()) {
					final BotEntity be = en.getValue();
					if (!be.isDead()) {
						final LivingEntity le = be.getEntity();
						if (le == null || !le.isValid() || le.getEntityId() != en.getKey())
							rbs.add(be);
					}
				}
				
				if (!rbs.isEmpty()) {
					Ostrov.sync(() -> {
						for (final BotEntity be : rbs) {
							Ostrov.log(be.name() + " bugged out");
							be.onBug();
						}
					});
				}
			}
		}.runTaskTimerAsynchronously(Ostrov.instance, 100, 100);
    }
	
    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }
    
    @Override
    public void reload() {
        HandlerList.unregisterAll(this);
    	if (!Config.bots) {
    		Ostrov.log_ok("§6Боты выключены!");
    		return;
    	}
    	
    	clearBots();
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
		Ostrov.log_ok("§2Боты включены!");
    }

    @Override
    public void onDisable() {
    	if (!Config.bots) {
    		Ostrov.log_ok("§6Боты выключены!");
    		return;
    	}
    	
    	clearBots();
    	for (final Player p : Bukkit.getOnlinePlayers()) {
    		removePlayer(p);
    	}
    }
    
    public static final Field useId = getIdFld(PacketPlayInUseEntity.class);
    
    public static final Field entId = getIdFld(PacketPlayOutEntity.class);

    private static Field getIdFld(final Class<?> cls) {
        final Field fld = cls.getDeclaredFields()[0];
        fld.setAccessible(true);
        return fld;
    }

    public static void sendWrldPckts(final net.minecraft.world.level.World w, final Packet<?>... ps) {
        for (final EntityHuman e : w.v()) {
            if (e instanceof EntityPlayer) {
                final NetworkManager nm = ((EntityPlayer) e).c.h;
                for (final Packet<?> p : ps) {
                    nm.a(p);
                }
            }
        }
    }
    
    @EventHandler
    public void onJoin(final BungeeDataRecieved e) {
    	final Player pl = e.getPlayer();
    	injectPlayer(pl);
    	final UUID id = pl.getWorld().getUID();
    	Ostrov.async(() -> {
        	for (final BotEntity be : nameBots.values()) {
        		if (be.w.getUID().equals(id)) be.updateAll(pl);
        	}
    	}, 4);
    }
    
    @EventHandler
    public void onLeave(final PlayerQuitEvent e) {
    	removePlayer(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
    	final BotEntity be = rIdBots.get(e.getEntity().getEntityId());
    	if (be != null) be.onDamage(e);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(final EntityDeathEvent e) {
    	final BotEntity be = rIdBots.get(e.getEntity().getEntityId());
    	if (be != null) be.onDeath(e);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTrans(final EntityTransformEvent e) {
    	if (e.getEntity() instanceof final LivingEntity le && isBot(le)) 
    		e.setCancelled(true);
    }

    public static void removePlayer(final Player p) {
        final Channel channel = VM.getNmsServer().toNMS(p).c.h.m;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove("ostrov_bot_"+p.getName());
            return null;
        });
    }

    public static void injectPlayer(final Player p) {
        final NetworkManager nm = VM.getNmsServer().toNMS(p).c.h;
        nm.m.pipeline().addBefore("packet_handler", "ostrov_bot_"+p.getName(), new ChannelDuplexHandler() {
            @Override
            public void channelRead(final ChannelHandlerContext chc, final Object packet) throws Exception {
                if (packet instanceof final PacketPlayInUseEntity uep) {
                    if (uep.getActionType() == b.b) {
                    	final int id = uep.getEntityId();
                        for (final BotEntity bt : BotManager.rIdBots.values()) {
                            if (bt.af() == id) {
                                useId.set(uep, bt.rid);
                                break;
                            }
                        }
                    }
                }
                super.channelRead(chc, packet);
            }

            @Override
            public void write(final ChannelHandlerContext chc, final Object packet, final ChannelPromise channelPromise) throws Exception {

//                if (packet instanceof PacketPlayOutScoreboardTeam) {
//                	p.sendMessage(((PacketPlayOutScoreboardTeam) packet).toString());
//                }
                
                if (packet instanceof PacketPlayOutSpawnEntity) {
                    if (BotManager.rIdBots.containsKey(((PacketPlayOutSpawnEntity) packet).a())) {
                        return;
                    }
                } else if (packet instanceof PacketPlayOutEntityMetadata) {
                    if (BotManager.rIdBots.containsKey(((PacketPlayOutEntityMetadata) packet).a())) {
                        return;
                    }
                } else if (packet instanceof PacketPlayOutEntityTeleport) {
                    if (BotManager.rIdBots.containsKey(((PacketPlayOutEntityTeleport) packet).a())) {
                        return;
                    }
                } else if (packet instanceof PacketPlayOutUpdateAttributes) {
                    if (BotManager.rIdBots.containsKey(((PacketPlayOutUpdateAttributes) packet).a())) {
                        return;
                    }
                } else if (packet instanceof PacketPlayOutEntity) {
                    if (BotManager.rIdBots.containsKey(entId.get(packet))) {
                        return;
                    }
                } else if (packet instanceof ClientboundBundlePacket) {
                	final Iterator<Packet<PacketListenerPlayOut>> pit = ((ClientboundBundlePacket) packet).a().iterator();
                	while (pit.hasNext()) {
						final Packet<?> pc = pit.next();
		            	
		                if (pc instanceof PacketPlayOutSpawnEntity) {
		                    if (BotManager.rIdBots.containsKey(((PacketPlayOutSpawnEntity) pc).a())) {
		                        pit.remove();
		                    }
		                } else if (pc instanceof PacketPlayOutEntityMetadata) {
		                    if (BotManager.rIdBots.containsKey(((PacketPlayOutEntityMetadata) pc).a())) {
		                        pit.remove();
		                    }
		                } else if (packet instanceof PacketPlayOutEntityTeleport) {
		                    if (BotManager.rIdBots.containsKey(((PacketPlayOutEntityTeleport) pc).a())) {
		                        pit.remove();
		                    }
		                } else if (packet instanceof PacketPlayOutUpdateAttributes) {
		                    if (BotManager.rIdBots.containsKey(((PacketPlayOutUpdateAttributes) pc).a())) {
		                        pit.remove();
		                    }
		                } else if (pc instanceof PacketPlayOutEntity) {
		                    if (BotManager.rIdBots.containsKey(entId.get(pc))) {
		                        pit.remove();
		                    }
		                }
					}
                }
                
                /*if (packet instanceof PacketPlayOutKeepAlive 
                	|| packet instanceof PacketPlayOutUnloadChunk
                	|| packet instanceof ClientboundBundlePacket
                	|| packet instanceof PacketPlayOutViewCentre
                	|| packet instanceof ClientboundLevelChunkWithLightPacket
                	|| packet instanceof PacketPlayOutEntity
                	|| packet instanceof PacketPlayOutEntityDestroy) {
                    super.write(chc, packet, channelPromise);
                	return;
                }
                
                if (packet instanceof PacketPlayOutEntityMetadata 
                	|| packet instanceof ClientboundChunksBiomesPacket
                	|| packet instanceof PacketPlayOutUpdateTime
                	|| packet instanceof PacketPlayOutEntityHeadRotation
                	|| packet instanceof ClientboundSetActionBarTextPacket
                	|| packet instanceof PacketPlayOutEntityVelocity
                	|| packet instanceof PacketPlayOutUpdateAttributes) {
                	return;
                }*/
                
                
//                Bukkit.getConsoleSender().sendMessage("p-" + packet);
                
                super.write(chc, packet, channelPromise);
            }
        });
    }

    public static void clearBots() {
        final HashMap<Integer, BotEntity> ns = new HashMap<>(rIdBots);
		for (final BotEntity bt : ns.values()) {
			bt.remove();
		}
	}
    
    public static boolean isBot(final LivingEntity le) {
    	return rIdBots.containsKey(le.getEntityId());
    }
    
    @Nullable
    public static <Bot extends BotEntity> Bot getBot(final int rid, final Class<Bot> cls) {
    	final BotEntity be = rIdBots.get(rid);
    	return be == null ? null : cls.cast(be);
    }
    
    @Nullable
    public static <Bot extends BotEntity> Bot getBot(final String name, final Class<Bot> cls) {
    	final BotEntity be = nameBots.get(name);
    	return be == null ? null : cls.cast(be);
    }
    
	@Nullable
    public static <Bot extends BotEntity> Bot createBot(final String name, final Class<Bot> cls, final Supplier<Bot> crt) {
    	if (!Config.bots) {
    		Ostrov.log_warn("Tried creating a Bot while the module is off!");
    		return null;
    	}
    	
    	final BotEntity be = nameBots.get(name);
    	try {
			return be != null && be.getClass().isAssignableFrom(cls) ? cls.cast(be) : crt.get();
		} catch (IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
    }

}
