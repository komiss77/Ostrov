package ru.komiss77.listener;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.TprCmd;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.MoveUtil;


public class TestLst implements Listener {

    /*public static boolean canSee(final Player shoter, final LivingEntity target) {
        final Vector line = shoter.getEyeLocation().toVector().clone().subtract(target.getLocation().toVector()).normalize();
        final Vector dirFacing = target.getEyeLocation().getDirection().clone().normalize();
        double angle = Math.acos(line.dot(dirFacing));  //Angle in radians
        return angle <= 0.785398163;

        Location l = shoter.getLocation();
        int[] entityVector = getVectorForPoints(startPos[0], startPos[1], l.getBlockX(), l.getBlockY());

        double angle = getAngleBetweenVectors(endA, entityVector);
        if(Math.toDegrees(angle) < degrees && Math.toDegrees(angle) > 0)
            return true;//newEntities.add(e);
    }
    public static int[] getVectorForPoints(int x1, int y1, int x2, int y2) {
        return new int[] { x2 - x1, y2 - y1 };
    }
    public static double getAngleBetweenVectors(int[] vector1, int[] vector2) {
        return Math.atan2(vector2[1], vector2[0]) - Math.atan2(vector1[1], vector1[0]);
    }*/

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


            //final Player semen = Bukkit.getPlayerExact("semen");
            //if (semen!=null) {
            //     p.sendMessage("canSee?"+canSee(p, semen));
            //    return;
            //}

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
                    // p.getWorld().createExplosion(e.getClickedBlock().getLocation().add(0,1,0), 5.0f, false, false);
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
