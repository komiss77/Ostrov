package ru.komiss77.listener;


import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.commands.TprCmd;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.MoveUtil;


public class TestLst implements Listener {


    Botter bt = null;


    //@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
//        Nms.totemPop(p, ItemUtil.nextPage);
//p.sendMessage("Interact "+Tag.BANNERS.isTagged(e.getClickedBlock().getType()));
        if (!ApiOstrov.canBeBuilder(p)) return;
        final Oplayer op = PM.getOplayer(p);

        final ItemStack inHand = e.getItem();
        if (inHand == null) return;

        if (inHand.getType() == Material.WOODEN_PICKAXE) {
            e.setCancelled(true);
            p.sendMessage("§8TestListener - interact cancel! " + e.getAction());

            if (e.getAction() == Action.RIGHT_CLICK_AIR) {
                if (p.isSneaking()) {
                    MoveUtil.safeTP(p, p.getLocation().clone().add(0, -200, 0));
                    p.sendMessage("safeTP down");
                } else {
                    MoveUtil.safeTP(p, p.getLocation().clone().add(0, 200, 0));
                    p.sendMessage("safeTP up");
                }
            } else if (e.getAction() == Action.LEFT_CLICK_AIR) {
                if (p.isSneaking()) {
                    p.sendMessage("§3teleportSave DOWN");
                    final Location loc = p.getLocation().clone().add(0, -200, 0);
                    MoveUtil.teleportSave(p, loc, true);
                } else {
                    p.sendMessage("§3teleportSave UP");
                    final Location loc = p.getLocation().clone().add(0, -200, 0);
                    MoveUtil.teleportSave(p, loc, true);
                }
            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (p.isSneaking()) {
                } else {
                    p.sendMessage("TprCmd.runCommand on safeTP");
                    TprCmd.runCommand(p, p.getWorld(), 1000, true, true, null);
                }
            } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (p.isSneaking()) {
                } else {
                }
            }
        }

        if (inHand.getType() == Material.ENCHANTED_BOOK) {
            //CustomEnchant.CHANNELING.level(inHand, 1, false);
            p.getInventory().setItemInMainHand(inHand);
            return;
        }

        if (inHand.getType() == Material.DRAGON_BREATH) {
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
