package ru.komiss77.modules.figures;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.objects.Figure;
import ru.komiss77.version.Nms;


class SpeechTask implements Runnable {

    protected BukkitTask task;
    protected int tick;
    
    //protected final Player p;
    protected final Figure f;
    protected final String name;
    protected Sound sound;
    
    public SpeechTask (final Player p, final Figure f) {
        //this.p = p;
        name = p.getName();
        this.f = f;
        tick = 120/3;//second*20/3;
        
        if (f.getEntityType()==EntityType.ARMOR_STAND) {
            sound = Sound.BLOCK_COMPOSTER_READY;
        } else {
            sound = Sound.ENTITY_VILLAGER_TRADE;
        }
        
    }
    

    
    
    
    
    
    @Override
    public void run() {
        
        final Player p = Bukkit.getPlayerExact(name); 

        if (p==null || !p.isOnline()) {
            cancel(p);
            return;
        }
        
        if (p.isDead() || p.isSneaking() || tick<=0 || isAway(p)) {
            cancel(p);
            //sendLookResetPacket(); //если посылать тут, то при cancel фигура не встанет как надо 
            return;
        }
        

        if (tick%2==0) {// 3*2 if (tick%7==0) {
            sendLookAtPlayerPacket();
        }
        if (tick%7==0 && sound!=null && f.entity!=null) {// 3*7
            p.playSound(f.entity.getLocation(), sound, .5f, (float)ApiOstrov.randInt(7, 15)/10);
        }
        
        
        tick--;

    }
    
    
    
    
    public void cancel(final Player p) {
        task.cancel();
        SpeachManager.tasks.remove(name);
        if (p!=null) {
            sendLookResetPacket(); //посылаем тут. 
        }
    }
    
    
    
    
    protected void sendLookAtPlayerPacket () {
        /*if (f.entity==null) return;
        
        final Vector direction = f.entity.getLocation().toVector().subtract(p.getEyeLocation().toVector()).normalize();
        double vx = direction.getX();
        double vy = direction.getY();
        double vz = direction.getZ();
        
        final byte yawByte = toPackedByte(180 - toDegree(Math.atan2(vx, vz)) + ApiOstrov.randInt(-10, 10)  );
        final byte pitchByte = toPackedByte(90 - toDegree(Math.acos(vy)) + (ApiOstrov.randBoolean() ? 10 : -10) );
        
        final EntityPlayer entityPlayer = ((CraftPlayer)p).getHandle();
        
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(((CraftEntity)f.entity).getHandle(), yawByte);
        entityPlayer.b.a(head);
        
        PacketPlayOutEntityLook packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(f.entity.getEntityId(), yawByte, pitchByte, true);
        entityPlayer.b.a(packet);*/
        final Player p = Bukkit.getPlayerExact(name);
        if (p!=null) Nms.sendLookAtPlayerPacket(p, f.entity);
    }  
    
    protected void sendLookResetPacket () {
      /*  if (f.entity==null) return;
        
        final byte yawByte = toPackedByte(f.yaw);
        final byte pitchByte = toPackedByte(f.pitch);
        
        final EntityPlayer entityPlayer = ((CraftPlayer)p).getHandle();
        
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation(((CraftEntity)f.entity).getHandle(), yawByte);
        entityPlayer.b.a(head);
        
        PacketPlayOutEntityLook packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(f.entity.getEntityId(), yawByte, pitchByte, true);
        entityPlayer.b.a(packet);*/
        final Player p = Bukkit.getPlayerExact(name);
        if (p!=null) Nms.sendLookResetPacket(p, f.entity);
    }    
    
  
    /*
    private static float toDegree(double angle) {
        return (float) Math.toDegrees(angle);
    }    
    
    private static byte toPackedByte(float f) {
        return (byte) (f * 256.0F / 360.0F);
    }  */  

    
    
    private boolean isAway(final Player p) {
        return !p.getWorld().getName().equals(f.worldName) || 
                Math.abs(p.getLocation().getBlockX()-f.x)>4 ||
                Math.abs(p.getLocation().getBlockY()-f.y)>4 ||
                Math.abs(p.getLocation().getBlockZ()-f.z)>4
                ;
    }
    
    
    
    
    
    
    
    
    
    
}
