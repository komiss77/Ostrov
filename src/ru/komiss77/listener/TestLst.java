package ru.komiss77.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.bots.TestBot;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;


public class TestLst implements Listener {

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (!ApiOstrov.isLocalBuilder(p) || e.getItem()==null || e.getItem().getType()!=Material.WOODEN_PICKAXE) return;
        final Oplayer op = PM.getOplayer(p);
        
        e.setCancelled(true);
        p.sendMessage("§8TestListener - interact cancel!");

        if (e.getAction()==Action.RIGHT_CLICK_AIR ) {
            final String name = "bot_"+ApiOstrov.randInt(0, 10);
            BotManager.createBot(name, TestBot.class, () -> new TestBot(name, new WXYZ(p.getLocation())));
            p.sendMessage("spawn bot "+name);
        }
        if (e.getAction()==Action.RIGHT_CLICK_BLOCK ) {
           // Material c = TCUtils.changeColor(mat, DyeColor.YELLOW);
          //  p.sendMessage("inHand="+mat+" change="+c);
            if (p.isSneaking()) {
                op.tag("", "§7", "");
                p.sendMessage("tag reset");
                //op.tag(Component.text("vvv", NamedTextColor.GOLD), Component.text("zzz", NamedTextColor.BLUE));
            } else {
                op.tag("§apref", "§b", "§esuff");
                p.sendMessage("tag set");
            }

        }
        if (e.getAction()==Action.LEFT_CLICK_BLOCK ) {
           // op.addCd("test", count++);
            if (p.isSneaking()) {
                op.nameTag(true);
                p.sendMessage("tag on");
                //Lang.sendMessage(p, "ВСТАВЛЕНО");
               // ApiOstrov.sendBossbar(p, "§7bar="+ ++count, 5, BarColor.BLUE, BarStyle.SOLID, true);
            } else {
                op.nameTag(false);
                p.sendMessage("tag off");
               // Lang.sendMessage(p, "Изменить паспортные данные");
              //  ApiOstrov.sendTitle(p, "§7title=", ""+ ++count, 10, 40, 10);
            }
            //p.sendMessage("name="+Translate.getMaterialName(e.getClickedBlock().getType(), EnumLang.RU_RU));
        }
        if (e.getAction()==Action.LEFT_CLICK_AIR ) {
           // op.addCd("test", count++);
            if (p.isSneaking()) {
                op.score.showBelow("aaaaaa"+ApiOstrov.randInt(0, 10), 1);
                p.sendMessage("below add");
                //Lang.sendMessage(p, "ВСТАВЛЕНО");
               // ApiOstrov.sendBossbar(p, "§7bar="+ ++count, 5, BarColor.BLUE, BarStyle.SOLID, true);
            } else {
                op.score.removeBelow();
                p.sendMessage("below off");
               // Lang.sendMessage(p, "Изменить паспортные данные");
              //  ApiOstrov.sendTitle(p, "§7title=", ""+ ++count, 10, 40, 10);
            }
            //p.sendMessage("name="+Translate.getMaterialName(e.getClickedBlock().getType(), EnumLang.RU_RU));
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
