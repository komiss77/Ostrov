package ru.komiss77.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.bots.AfkBot;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.InputButton;


public class TestLst implements Listener {

    private AfkBot bt;
    
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (!ApiOstrov.isLocalBuilder(p) || e.getItem()==null) return;
        final Oplayer op = PM.getOplayer(p);
        
        if (e.getItem().getType() == Material.BLAZE_ROD) {
            PM.getOplayer(p).tag("§2Лох полный на\n", "\n§6Тут что-то натво(рил)");
            if (bt != null) {
                bt.remove();
                bt = null;
            }
            bt = BotManager.createBot("Botus", AfkBot.class, nm -> new AfkBot(nm, new WXYZ(p.getLocation())));
//            Ostrov.sync(() -> bt.die(bt.getEntity()), 100);
        }


        
        
        if (e.getItem().getType()==Material.STICK) {
            e.setCancelled(true);
            p.sendMessage("§8TestListener - interact cancel!");
            
            if (e.getAction()==Action.RIGHT_CLICK_AIR ) {
                 //XYZ xyz = new XYZ(p.getWorld().getName(), p.getLocation().getBlockX(), p.getLocation().getBlockY()-3, p.getLocation().getBlockZ());
                //VM.getNmsServer().signInput(p, , xyz);
                if (bt != null) {
                    bt.remove();
                    bt = null;
                }
                if (p.isSneaking()) {
                    bt = BotManager.createBot("Botus", AfkBot.class, nm -> new AfkBot(nm, new WXYZ(p.getLocation())));
                    //op.tag.visible(false);
                    //p.sendMessage("custom name off");
                } else {
                    //op.tag("§bdd☻§edfdsg", "§к|avvvddedrfer §edffffff");
                    //op.upperName.visible(true);
                    //p.sendMessage("custom name on");
                }
            }
            
            if (e.getAction()==Action.LEFT_CLICK_AIR ) {
                PlayerInput.get(InputButton.InputType.SIGN, p, s -> p.sendMessage(s), "лохлохолхолхолхо");
                if (p.isSneaking()) {
                    //op.upperName.replaceName(true);
                    //p.sendMessage("replaceName name on");
                } else {
                    //op.upperName.replaceName(false);
                    //p.sendMessage("replaceName name off");
                }
            }
            return;
        }
        
        
        
        if (e.getItem().getType()==Material.WOODEN_PICKAXE) {
            e.setCancelled(true);
            p.sendMessage("§8TestListener - interact cancel!");
            
            if (e.getAction()==Action.RIGHT_CLICK_AIR ) {
                //final String name = "РоботДолбоёб";//+ApiOstrov.randInt(0, 10);
               // if (bot==null) {
                    //bot = BotManager.createBot(name, AfkBot.class, () -> new AfkBot(name, new WXYZ(p.getLocation())));
                    //p.sendMessage("spawn bot "+name);
               // } else {
                    //p.sendMessage("bot exist!");
               // }
                return;
            }
            if (e.getAction()==Action.RIGHT_CLICK_BLOCK ) {
               // Material c = TCUtils.changeColor(mat, DyeColor.YELLOW);
              //  p.sendMessage("inHand="+mat+" change="+c);
                if (p.isSneaking()) {
                  //  bot.tag("", "", "");
                   // p.sendMessage("tag reset");
                    //op.tag(Component.text("vvv", NamedTextColor.GOLD), Component.text("zzz", NamedTextColor.BLUE));
                } else {
                    //не меняет
                   // bot.tag("§aprefbckdl-§fkdfkjdfg", "§b", "§esuffasddsfgsdfgsdf");
                  //  p.sendMessage("tag set");
                }

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
