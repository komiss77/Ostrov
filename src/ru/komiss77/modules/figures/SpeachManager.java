package ru.komiss77.modules.figures;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.komiss77.Ostrov;
import ru.komiss77.objects.Figure;
import ru.komiss77.objects.FigureAnswer;


public class SpeachManager  {
    
    protected static final ConcurrentHashMap<String,SpeechTask> tasks = new ConcurrentHashMap<>();
    

 //   @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
 //   public void onAnswer(final FigureClickEvent e) {
  //      if (e.getAnswer()!=null) {
            //дефолтное движение
  //      }
    //}
    
    
    /*
    final TextHologramLine tl = speach.hologram.getLines().appendText(line.replaceFirst("ITEM:", "§c"));
                    tl.setClickListener( (hlce) -> {
                            hlce.getPlayer().sendMessage(tl.getText());
                        }
                    );
    */
    public static void onAnswer(final FigureAnswer answer) {
        
        final Player p = answer.player;
        if (tasks.containsKey(p.getName())) {
            tasks.get(p.getName()).cancel(p);
        }
        
        if (answer.getLines().isEmpty()) {
            
            for (final String line :answer.getLines()) {
               p.sendMessage(line);
            }
            animate(p, answer.figure);
                    
        } else {
            
            final SpeechTaskHD speach = new SpeechTaskHD(answer);
            //Только синх!    или IllegalStateException: async operation is not supported
            //at ru.komiss77.modules.figure.SpeachTaskHD.run(SpeachTaskHD.java:117) ~[Ostrov.jar:?]
            speach.task = Bukkit.getScheduler().runTaskTimer(Ostrov.instance, speach, 1, 3);
            tasks.put(p.getName(), speach);
            
        }

    }
    
    protected static void animate(final Player p, final Figure figure) {
        if (tasks.containsKey(p.getName())) {
            tasks.get(p.getName()).cancel(p);
        }
        final SpeechTask speach = new SpeechTask(p, figure);
        //speach.task = Bukkit.getScheduler().runTaskTimerAsynchronously(FigureManager.instance, speach, 1, 3); HolographicDisplays ConcurrentModificationException
        speach.task = Bukkit.getScheduler().runTaskTimer(Ostrov.instance, speach, 1, 3);
        tasks.put(p.getName(), speach);
    }
    
    
    protected static void onHoloClick(final Player p, final Consumer<Player> consumer) {
        
        if (tasks.containsKey(p.getName())) {
            tasks.get(p.getName()).cancel(p);
        }
        consumer.accept(p);
        //p.sendMessage("click--> "+line);

    }        
    
    
    
    
    
    
    protected static Location getHoloLoc(final Player p, final int lines) { //локация на 2 блока вперёд перед глазами
        //final Location l = p.getEyeLocation();
        final Vector direction = p.getLocation().getDirection();
        final Location holoLoc = p.getEyeLocation().add(direction.multiply(2));
        holoLoc.setY(holoLoc.getY() + 1 + lines*0.25);//adelante.setY(adelante.getY() + hologram.size()*0.15);
        return holoLoc;
    }    


    



    
    

        
        
    
    
    
    
    
    
    
    
    
    
    
    
    
}
