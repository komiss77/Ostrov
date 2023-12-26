package ru.komiss77.version.v1_20_R1;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam.a;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcher.b;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.scores.ScoreboardTeam;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.FastMath;
import ru.komiss77.version.IEntityGroup;
import ru.komiss77.version.VM;


public class EntityGroup implements IEntityGroup { 

    
    //net.minecraft.server.v1_16_R1.EntityTypes - есть все типы   EntityTypes.ZOMBIFIED_PIGLIN.e();
    @Override
    public EntityGroup getEntytyGroup(final EntityType type) {
    	return getEntityGroup(type);
    }
    
    @Override
    public EntityGroup getEntityGroup(final EntityType type) {
        
        
        
        switch (type) {

            case RAVAGER:																											
            case PILLAGER:																											
            case ZOGLIN:																											
            case PIGLIN:																											
            case DROWNED:																											
            case SHULKER:																											
            case ENDERMITE:																											
            case WITCH:																											
            case ENDER_DRAGON:																											
            case MAGMA_CUBE:																											
            case BLAZE:																											
            case SILVERFISH:																											
            case ENDERMAN:																											
            case ZOMBIFIED_PIGLIN:																											
            case GIANT:			
            case CREEPER:
            case SPIDER:																											
            case GHAST:																											
            case SLIME:																											
            case PHANTOM:
            case ZOMBIE:
            case SKELETON:
            case CAVE_SPIDER:																											
            case GUARDIAN:
            case ZOMBIE_VILLAGER:
            case VEX:
            case VINDICATOR:
            case EVOKER:
            case ILLUSIONER:
            case WITHER:
            case WITHER_SKELETON:
            case STRAY:                    
            case HUSK:
            case PIGLIN_BRUTE:
            case WARDEN:
            case ELDER_GUARDIAN:
                return EntityGroup.MONSTER;																										


            case PARROT:
            case LLAMA_SPIT:
            case LLAMA:
            case RABBIT:
            case CAT:
            case HORSE:
            case OCELOT:
            case FOX:
            case MUSHROOM_COW:
            case WOLF:
            case COW:
            case SHEEP:
            case POLAR_BEAR:
            case PIG:
            case PANDA:
            case BEE:
            case CHICKEN:
            case VILLAGER:
            case WANDERING_TRADER:
            case IRON_GOLEM:
            case SNOWMAN:
            case DONKEY:
            case MULE:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
            case TURTLE:
            case HOGLIN:
            case GOAT:
    		case CAMEL:
    		case SNIFFER:
            case TRADER_LLAMA:
            case ALLAY:
            case STRIDER:
                return EntityGroup.CREATURE;
                
                
            case BAT:
                return EntityGroup.AMBIENT;
			
                                
            case DOLPHIN:
            case SQUID:
            case GLOW_SQUID:
            case AXOLOTL:
            case FROG:
            case TADPOLE:
                return EntityGroup.WATER_CREATURE;
			
                
            case TROPICAL_FISH:
            case COD:
            case SALMON:
            case PUFFERFISH:
                return EntityGroup.WATER_AMBIENT;
                
                
                
                
                
            case AREA_EFFECT_CLOUD:
            case ARMOR_STAND:
            case ARROW:
            case BOAT:
            case DRAGON_FIREBALL:
            case DROPPED_ITEM:
            case EGG:
            case ENDER_CRYSTAL:
            case ENDER_PEARL:
            case ENDER_SIGNAL:
            case EVOKER_FANGS:
            case EXPERIENCE_ORB:
            case FALLING_BLOCK:
            case FIREBALL:
            case FIREWORK:
            case FISHING_HOOK:
            case ITEM_FRAME:
            case LEASH_HITCH:
            case LIGHTNING:
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
            case PAINTING:
            case PRIMED_TNT:
            case SHULKER_BULLET:
            case SMALL_FIREBALL:
            case SNOWBALL:
            case SPECTRAL_ARROW:
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
            case TRIDENT:
            case UNKNOWN:
            case WITHER_SKULL:
            case PLAYER:
            case GLOW_ITEM_FRAME:
            case CHEST_BOAT:
                
            case MARKER:
    		case INTERACTION:
    		case ITEM_DISPLAY:
    		case TEXT_DISPLAY:
    		case BLOCK_DISPLAY:
                
                break;
				
                
        }
        
        //если выше ничего не выстрелило, то определяем о старинке
        return EntityGroup.UNDEFINED;
    }

    @Override
    public EntityGroup getEntytyType(final Entity entity) {
    	return getEntityType(entity);
    }
    
    @Override
    public EntityGroup getEntityType(final Entity entity) {      
        if (entity == null || entity.isDead()) return EntityGroup.UNDEFINED;
        return getEntityGroup(entity.getType());
    }

    @Override
    public EntityGroup byTag(final String tag_as_string){
        for(EntityGroup set: EntityGroup.values()){
                if(String.valueOf(set.toString()).equals(tag_as_string.toUpperCase())){
                        return set;
                }
        }
        return EntityGroup.UNDEFINED;
    }

    @Override
    public boolean isGroup(final String tag_as_string){
        for(EntityGroup set: EntityGroup.values()){
                if(String.valueOf(set.toString()).equals(tag_as_string.toUpperCase())){
                        return true;
                }
        }
        return false;
    }
    

    @Override
    public void sendLookAtPlayerPacket(final Player p, final Entity e) {
        if (p==null || !p.isOnline() || e==null) return;
        
        final Vector direction = e.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
        double vx = direction.getX();
        double vy = direction.getY();
        double vz = direction.getZ();
        
        final byte yawByte = FastMath.toPackedByte(180f - FastMath.toDegree((float) Math.atan2(vx, vz)) + ApiOstrov.randInt(-10, 10) );
        final byte pitchByte = FastMath.toPackedByte(90 - FastMath.toDegree((float) Math.acos(vy)) + (ApiOstrov.randBoolean() ? 10 : -5) );
        
        final EntityPlayer entityPlayer = VM.getNmsServer().toNMS(p);
        
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(VM.getNmsServer().toNMS(e), yawByte);
        entityPlayer.c.a(head);
        
        PacketPlayOutEntity.PacketPlayOutEntityLook packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(e.getEntityId(), yawByte, pitchByte, true);
        entityPlayer.c.a(packet);   
    }

    @Override
    public void sendLookResetPacket(final Player p, final Entity e) {
        if (p==null || !p.isOnline() || e==null) return;
        
        final byte yawByte = FastMath.toPackedByte(e.getLocation().getYaw());//toPackedByte(f.yaw);
        final byte pitchByte = FastMath.toPackedByte(e.getLocation().getPitch());//toPackedByte(f.pitch);
        
        final EntityPlayer entityPlayer = VM.getNmsServer().toNMS(p);
        
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(VM.getNmsServer().toNMS(e), yawByte);
        entityPlayer.c.a(head);
        
        PacketPlayOutEntity.PacketPlayOutEntityLook packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(e.getEntityId(), yawByte, pitchByte, true);
        entityPlayer.c.a(packet); 
    }

    @Override
	public void colorGlow(final Entity le, final char color, final boolean fakeGlow) {
    	if (le != null && le.isValid()) {
    		Ostrov.async(() -> {
        		final net.minecraft.world.entity.Entity el = VM.getNmsServer().toNMS(le);
				final ScoreboardServer sb = VM.getNmsServer().toNMS().aF();
				final ScoreboardTeam st = sb.g(le.getUniqueId().toString());
				st.a(EnumChatFormat.a(color));
    			
        		if (fakeGlow) {
        			final PacketPlayOutEntityMetadata pem = new PacketPlayOutEntityMetadata(le.getEntityId(), el.aj().c());
        			pem.c().add(new b<Byte>(0, DataWatcher.a(net.minecraft.world.entity.Entity.class, DataWatcherRegistry.a).b(), (byte) 64));
					
        			sendWrldPckts(el.dI(), PacketPlayOutScoreboardTeam.a(st), PacketPlayOutScoreboardTeam.a(st, true), 
        				PacketPlayOutScoreboardTeam.a(st, le.getUniqueId().toString(), a.a), PacketPlayOutScoreboardTeam.a(st, false), pem);
        			return;
        		}

    			sendWrldPckts(el.dI(), PacketPlayOutScoreboardTeam.a(st), PacketPlayOutScoreboardTeam.a(st, true), 
    				PacketPlayOutScoreboardTeam.a(st, le.getUniqueId().toString(), a.a), PacketPlayOutScoreboardTeam.a(st, false));
				sb.d(st);
    		});
			if (!fakeGlow) le.setGlowing(true);
    	}
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
}
