
package ru.komiss77.Listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


import ru.komiss77.Enums.Action;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.Timer;


/*
https://www.spigotmc.org/resources/aac-advanced-anti-cheat-hack-kill-aura-blocker.6442/
*/


public class AAC_listener implements Listener {

//private static ConcurrentHashMap<String, Integer> violations;

public static void Init() {
    //violations = new ConcurrentHashMap();
    
 //  new BukkitRunnable() {
  //              @Override
  //              public void run() { 
  //                      violations.clear();
  //              }}.runTaskTimerAsynchronously(Ostrov.instance,600, 1200); 
    
}
 /*  
   
@EventHandler
    public void onPlayerViolation(PlayerViolationEvent e) {
        
        if (!PM.exist(e.getPlayer().getName())) return;
        
        if (e.getHackType()==HackType.MOVE || e.getHackType()==HackType.NOFALL) {
            
//System.out.println("->подозрение на полёт: "+e.getPlayer().getName()+"   "+e.getViolations());
            if (e.getViolations()>12 && e.getPlayer().getHealth()>5 && e.getPlayer().getLocation().clone().subtract(0, 1, 0).getBlock().getType()==Material.AIR && e.getPlayer().getLocation().clone().subtract(0, 2, 0).getBlock().getType()==Material.AIR && e.getPlayer().getLocation().clone().subtract(0, 3, 0).getBlock().getType()==Material.AIR ) {
            
                if (PM.getOplayer(e.getPlayer().getName()).aac_last_pos.equals(e.getPlayer().getLocation().getBlockX()+""+e.getPlayer().getLocation().getBlockZ())) return;
                PM.getOplayer(e.getPlayer().getName()).aac_last_pos=e.getPlayer().getLocation().getBlockX()+""+e.getPlayer().getLocation().getBlockZ();
                
//System.out.println("11111");
                boolean has_potion=false;
                for (PotionEffect pe:e.getPlayer().getActivePotionEffects()) {
                    if (pe.getType()==PotionEffectType.LEVITATION || pe.getType()==PotionEffectType.JUMP) {
                        has_potion = true;
                        break;
                    }
                }
                if (has_potion) return;
//System.out.println("22222");

                if (!Timer.CD_has( e.getPlayer().getName(), "aac_check" ) )  PM.getOplayer(e.getPlayer().getName()).aac_violations=0;      //если прошло 30 сек-обнуляем
                Timer.CD_add( e.getPlayer().getName(), "aac_check", 60);
                
                PM.getOplayer(e.getPlayer().getName()).aac_violations++;
                
System.out.println("------Подтверждён полёт: "+e.getPlayer().getName()+"  "+PM.getOplayer(e.getPlayer().getName()).aac_violations);
                    if (PM.getOplayer(e.getPlayer().getName()).aac_violations==25 ) {
//System.out.println(" !!!!!!!! отправлено в банжи"+e.getPlayer().getName());
                        //Ostrov.sendMessage( e.getPlayer(), "Bauth_getdata", e.getPlayer().getName()+"<:>AAC<:>"+Bukkit.getMotd()+"<:>"+e.getHackType().getName()+"<:> " );
                        SpigotChanellMsg.sendMessage(e.getPlayer(), Action.OSTROV_AAC, e.getHackType().getName());
                    }
            }
            //e.setCancelled(true);
       } else {
           // System.out.println("Подозрение на чит "+e.getHackType().toString()+" -> "+e.getPlayer().getName()+"  "+e.getMessage()+"   "+e.getViolations());
        }
        //e.setCancelled(true);
    }   
   
   */
   

   
   
 //        if (e.getFrom().getBlockX()==e.getTo().getBlockX() && e.getFrom().getBlockY()==e.getTo().getBlockY() && e.getFrom().getBlockZ()==e.getTo().getBlockZ()  ) return;
   

    
    
    
    
}
