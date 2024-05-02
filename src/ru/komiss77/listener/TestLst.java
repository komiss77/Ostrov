package ru.komiss77.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.enchants.CustomEnchant;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.version.Nms;


public class TestLst implements Listener {



  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final ItemStack it = e.getItem();
        if (it == null) return;

        if (!ApiOstrov.isLocalBuilder(p)) return;
//Ostrov.log("PlayerInteractEvent "+e.getMaterial());

        if (it.getType() == Material.ENCHANTED_BOOK) {
          p.sendMessage("b-" + Nms.getBiomeKey(new WXYZ(p.getLocation())));
          CustomEnchant.CHANNELING.level(it, 1, false);
          p.getInventory().setItemInMainHand(it);
          return;
        }

        if (it.getType() == Material.DRAGON_BREATH) {
          p.sendMessage("§8TestListener - interact cancel!");
          if (e.getClickedBlock() != null) {
            e.setCancelled(true);
          }
          /*if (e.getClickedBlock() == null) {
            if (bt != null) {
              bt.remove();
              bt = null;
            }
            bt = BotManager.createBot("Botus", AfkBot.class, nm -> new AfkBot(nm, new WXYZ(p.getLocation())));
          } else {
            p.sendMessage(ApiOstrov.toSigFigs((float) e.getClickedBlock().getBoundingBox().getVolume(), (byte) 2));
          }

          p.setGlowing(true);
          PM.getOplayer(p).color(switch (Ostrov.random.nextInt(5)) {
            case 1 -> NamedTextColor.YELLOW;
            case 2 -> NamedTextColor.GREEN;
            case 3 -> NamedTextColor.RED;
            case 4 -> NamedTextColor.BLUE;
            default -> NamedTextColor.WHITE;
          });*/
        }
        
        if (it.getType()==Material.WOODEN_PICKAXE) {
//            e.setCancelled(true);
           p.sendMessage("§8TestListener - interact cancel!");
            
            if (e.getAction()==Action.RIGHT_CLICK_AIR ) {
              //RDS.sendMessage("ostrov", "ostrov");
              //RDS.sendMessage("arenadata", "arenadata");
            }
            if (e.getAction()==Action.RIGHT_CLICK_BLOCK ) {

            }
            if (e.getAction()==Action.LEFT_CLICK_BLOCK ) {
               // op.addCd("test", count++);
                if (p.isSneaking()) {
                    //forEntity(bot.getBukkitEntity()).setName(TCUtils.format("§bdd☻§edfdsg §gк|avvvddedrfer §edffffff"));
                   // bot.tag(true);
                   // p.sendMessage("tag on");
                    //Lang.sendMessage(p, "ВСТАВЛЕНО");
                   // ApiOstrov.sendBossbar(p, "§7bar="+ ++count, 5, BarColor.BLUE, BarStyle.SOLID, true);
                } else {
                 //   bot.tag(false);
                    //forEntity(bot.getBukkitEntity()).setHidden(true);
                   // p.sendMessage("tag off");
                   // Lang.sendMessage(p, "Изменить паспортные данные");
                  //  ApiOstrov.sendTitle(p, "§7title=", ""+ ++count, 10, 40, 10);
                }
                //p.sendMessage("name="+Translate.getMaterialName(e.getClickedBlock().getType(), EnumLang.RU_RU));
            }
            if (e.getAction()==Action.LEFT_CLICK_AIR ) {
               // op.addCd("test", count++);
                if (p.isSneaking()) {
                   // bot.score.below("aaaaaa"+ApiOstrov.randInt(0, 10), 1);
                    //op.score.below("xxxxx"+ApiOstrov.randInt(0, 10), 1);

                   // p.sendMessage("below add");
                    //Lang.sendMessage(p, "ВСТАВЛЕНО");
                   // ApiOstrov.sendBossbar(p, "§7bar="+ ++count, 5, BarColor.BLUE, BarStyle.SOLID, true);
                } else {
                  //  bot.score.below(false);
                    //op.score.below(false);
                  //  p.sendMessage("below off");
                   // Lang.sendMessage(p, "Изменить паспортные данные");
                  //  ApiOstrov.sendTitle(p, "§7title=", ""+ ++count, 10, 40, 10);
                }
                //p.sendMessage("name="+Translate.getMaterialName(e.getClickedBlock().getType(), EnumLang.RU_RU));
            }
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
