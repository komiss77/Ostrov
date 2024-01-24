package ru.komiss77.listener;

import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import io.papermc.paper.event.player.PlayerUntrackEntityEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.bots.AfkBot;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.TCUtils;


public class TestLst implements Listener {

    private AfkBot bot;
    
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (!ApiOstrov.isLocalBuilder(p) || e.getItem()==null) return;
        
        final Oplayer op = PM.getOplayer(p);
        
        if (e.getItem().getType()==Material.STICK) {
            e.setCancelled(true);
            p.sendMessage("§8TestListener - interact cancel!");
            
            if (e.getAction()==Action.RIGHT_CLICK_AIR ) {
                forEntity(p).setName(TCUtils.format("§bdd☻§edfdsg §gк|avvvddedrfer §edffffff"));
                //name.setName(MiniMessage.miniMessage().deserialize("<rainbow>%s</rainbow>".formatted(event.getPlayer().getName())));

            }
            return;
        }
        
        if (e.getItem().getType()==Material.WOODEN_PICKAXE) {
            e.setCancelled(true);
            p.sendMessage("§8TestListener - interact cancel!");
            
            if (e.getAction()==Action.RIGHT_CLICK_AIR ) {
                final String name = "РоботДолбоёб";//+ApiOstrov.randInt(0, 10);
                bot = BotManager.createBot(name, AfkBot.class, () -> new AfkBot(name, new WXYZ(p.getLocation())));
                p.sendMessage("spawn bot "+name);
                return;
            }
            if (e.getAction()==Action.RIGHT_CLICK_BLOCK ) {
               // Material c = TCUtils.changeColor(mat, DyeColor.YELLOW);
              //  p.sendMessage("inHand="+mat+" change="+c);
                if (p.isSneaking()) {
                    bot.tag("", "", "");
                    p.sendMessage("tag reset");
                    //op.tag(Component.text("vvv", NamedTextColor.GOLD), Component.text("zzz", NamedTextColor.BLUE));
                } else {
                    bot.tag("§aprefbckdl-§fkdfkjdfg", "§b", "§esuffasddsfgsdfgsdf");
                    p.sendMessage("tag set");
                }

            }
            if (e.getAction()==Action.LEFT_CLICK_BLOCK ) {
               // op.addCd("test", count++);
                if (p.isSneaking()) {
                    bot.tag(true);
                    p.sendMessage("tag on");
                    //Lang.sendMessage(p, "ВСТАВЛЕНО");
                   // ApiOstrov.sendBossbar(p, "§7bar="+ ++count, 5, BarColor.BLUE, BarStyle.SOLID, true);
                } else {
                    bot.tag(false);
                    p.sendMessage("tag off");
                   // Lang.sendMessage(p, "Изменить паспортные данные");
                  //  ApiOstrov.sendTitle(p, "§7title=", ""+ ++count, 10, 40, 10);
                }
                //p.sendMessage("name="+Translate.getMaterialName(e.getClickedBlock().getType(), EnumLang.RU_RU));
            }
            if (e.getAction()==Action.LEFT_CLICK_AIR ) {
               // op.addCd("test", count++);
                if (p.isSneaking()) {
                    bot.score.below("aaaaaa"+ApiOstrov.randInt(0, 10), 1);
                    op.score.below("xxxxx"+ApiOstrov.randInt(0, 10), 1);

                    p.sendMessage("below add");
                    //Lang.sendMessage(p, "ВСТАВЛЕНО");
                   // ApiOstrov.sendBossbar(p, "§7bar="+ ++count, 5, BarColor.BLUE, BarStyle.SOLID, true);
                } else {
                    bot.score.below(false);
                    op.score.below(false);
                    p.sendMessage("below off");
                   // Lang.sendMessage(p, "Изменить паспортные данные");
                  //  ApiOstrov.sendTitle(p, "§7title=", ""+ ++count, 10, 40, 10);
                }
                //p.sendMessage("name="+Translate.getMaterialName(e.getClickedBlock().getType(), EnumLang.RU_RU));
            }
        }
        
        
        
        
        
    }
    
    
    
    
    private final Map<UUID, CustomName> nameStorage = new HashMap<>();  

    public CustomName forEntity(Entity entity) {
        CustomName cn = nameStorage.get(entity.getUniqueId());
        if (cn == null) {
            cn = new CustomName(entity);
            nameStorage.put(entity.getUniqueId(), cn);
            // Send to trackers
            cn.setHidden(false);
        }
        return cn;
    }

    public void unregister(Entity entity) {
        CustomName cn = nameStorage.remove(entity.getUniqueId());
        if (cn != null) {
            // Remove from trackers
            cn.setHidden(true);
        }
    }
 
   /*@EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                CustomName cn = nameStorage.remove(event.getPlayer().getUniqueId());
                if (cn != null) {
                    cn.close();
                }
            }
        }.runTaskLater(Ostrov.instance, 5);
    }*/

    @EventHandler(ignoreCancelled = true)
    public void toggleSneak(PlayerToggleSneakEvent event) {
        CustomName cn = nameStorage.get(event.getPlayer().getUniqueId());
        // Does the entity have a custom name?
        if (cn != null && !event.getPlayer().isInsideVehicle()) {
            cn.setTargetEntitySneaking(event.isSneaking());
        }
    }

    @EventHandler
    public void trackEntity(PlayerTrackEntityEvent event) {
        CustomName cn = nameStorage.get(event.getEntity().getUniqueId());
        // Does the entity have a custom name?
        if (cn != null) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    cn.sendToClient(event.getPlayer());
                }
            }.runTaskLater(Ostrov.instance, 1);
        }
    }

    @EventHandler
    public void untrackEntity(PlayerUntrackEntityEvent event) {
        CustomName cn = nameStorage.get(event.getEntity().getUniqueId());
        // Does the entity have a custom name?
        if (cn != null) {
            cn.removeFromClient(event.getPlayer());
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void mountEntity(EntityMountEvent event) {
        CustomName cn = nameStorage.get(event.getMount().getUniqueId());
        if (cn != null) {
            cn.setHidden(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void dismountEntity(EntityDismountEvent event) {
        CustomName cn = nameStorage.get(event.getDismounted().getUniqueId());
        if (cn != null && event.getDismounted().getPassengers().size() == 1) {
            // Run 2 ticks later, we need to ensure that the game sends the packets to update the
            // passengers.
            new BukkitRunnable(){
                @Override
                public void run() {
                    cn.setHidden(false);
                }
            }.runTaskLater(Ostrov.instance, 2);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
/*
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void testHIGHEST(PlayerInteractEvent e) {
        System.out.println("Interac HIGHEST canceled?"+e.isCancelled()+" useInteractedBlock="+e.useInteractedBlock()+" useItemInHand="+e.useItemInHand());
    }  
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = false)
    public void testHIGH(PlayerInteractEvent e) {
        System.out.println("Interac HIGH canceled?"+e.isCancelled()+" useInteractedBlock="+e.useInteractedBlock()+" useItemInHand="+e.useItemInHand());
    }
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void testNORMAL(PlayerInteractEvent e) {
        System.out.println("Interac NORMAL canceled?"+e.isCancelled()+" useInteractedBlock="+e.useInteractedBlock()+" useItemInHand="+e.useItemInHand());
    }
    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = false)
    public void testLOW(PlayerInteractEvent e) {
        System.out.println("Interac LOW canceled?"+e.isCancelled()+" useInteractedBlock="+e.useInteractedBlock()+" useItemInHand="+e.useItemInHand());
    }
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void testLOWEST(PlayerInteractEvent e) {
        System.out.println("Interac LOWEST canceled?"+e.isCancelled()+" useInteractedBlock="+e.useInteractedBlock()+" useItemInHand="+e.useItemInHand());
    }
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void testMONITOR(PlayerInteractEvent e) {
        System.out.println("Interac MONITOR canceled?"+e.isCancelled()+" useInteractedBlock="+e.useInteractedBlock()+" useItemInHand="+e.useItemInHand());
    }

*/

    
}
