package ru.komiss77.modules.figures;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.displays.DisplayManager;
import ru.komiss77.objects.FigureAnswer;


class SpeechTaskHD extends SpeechTask {
    //private final boolean vibration;
    private final boolean beforeEyes;
    
    public SpeechTaskHD(final FigureAnswer answer) {
        super(answer.player, answer.figure);
        tick = (answer.duration() >> 2) + 20;
        //vibration = answer.vibration;
        beforeEyes = answer.beforeEyes;
        sound = answer.sound;
        spawnHolo(answer);  //для пустых текстов просто мотать головой
    }
    
    private void spawnHolo(final FigureAnswer answer) {
        final Location pos;
        final Player p = Bukkit.getPlayerExact(name);
        if (answer.beforeEyes || f.entity == null) { //для плавающих перед глазами
            pos = SpeachManager.getHoloLoc(p, answer.getLines().size());
        } else { //для статичных
            pos = new Location(Bukkit.getWorld(f.worldName), f.x+0.5d, f.y + f.entity.getHeight() + 0.6d, f.z+0.5d);
        }
        
//        boolean hasClick;
        
        StringBuilder sb = new StringBuilder();
        boolean timer = true;
        int letters = 0;
        int items = 0;
        
        for (final String line : answer.getLines()) {
            if (line.startsWith("ITEM:")) {
                
                final Material mat = Material.matchMaterial(line.replaceFirst("ITEM:", ""));
                if (mat == null) {
                	sb.append(" ").append(line);
                } else {
                	if (!sb.isEmpty()) {
                		timer = !DisplayManager.fakeTextAnimate(p, pos.clone(), sb.substring(1), true, false, 2, timer);
                		if (timer) break; 
                	}
                	
                	letters += sb.length();
                	final int tmp = letters;
                    DisplayManager.fakeItemAnimate(p, pos.clone().add(0d, -items, 0d))
                    	.setItem(new ItemStack(mat)).setIsDone(tk -> p.isSneaking() && tk > tmp);
                    items++;
                }
                
            } else {
            	sb.append(" ").append(line);
                
                /*if (hasClick) {
                final TextHologramLine tl = speach.hologram.getLines().appendText(line);
                    tl.setClickListener( (hlce) -> {
                            SpeachManager.onHoloClick(p, answer.clickLines.get(line));
                        }
                    );
                } else {
                    speach.hologram.getLines().appendText(line);
                }*/
            }
        }
        
    	if (!sb.isEmpty()) {
    		timer = !DisplayManager.fakeTextAnimate(p, pos.clone(), sb.substring(1), true, false, 2, timer);
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
            super.sendLookResetPacket(); //тут игрок точно онлайн
            cancel(p);
            return;
        }
        
        if (beforeEyes) {// 3*1 if (beforeEyes && tick%3==0) {
//            hologram.setPosition(SpeachManager.getHoloLoc(p, 1));
        }
        
        if (tick%2==0) {// 3*2 if (tick%7==0) {
            super.sendLookAtPlayerPacket();
        }
        
        if (tick%7==0 && sound!=null && f.entity!=null) {// 3*7
            p.playSound(f.entity.getLocation(), sound, .5f, ApiOstrov.randInt(7, 15)/10f);
        }
        
        tick--;

    }
    
    
    

    
    private boolean isAway(final Player p) {
        return !p.getWorld().getName().equals(f.worldName) || 
                Math.abs(p.getLocation().getBlockX()-f.x)>4 ||
                Math.abs(p.getLocation().getBlockY()-f.y)>4 ||
                Math.abs(p.getLocation().getBlockZ()-f.z)>4
                ;
    }
    
    
    
    
    
    
    
    
    
    
}
