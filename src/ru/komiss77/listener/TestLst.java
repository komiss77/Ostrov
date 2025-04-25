package ru.komiss77.listener;


import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Vector;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.items.ItemBuilder;


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

    /*private static final Quest qs = new Quest('a', ItemType.ACACIA_BOAT, 0, null, null, "<dark_green>First Advancement",
        "<gradient:cardinal:apple>Nice description", "block/flowering_azalea_leaves", Quest.QuestVis.ALWAYS, Quest.QuestFrame.TASK, 0);

    private static final Quest qs1 = new Quest('b', ItemType.ACACIA_DOOR, 2, null, qs, "<dark_green>Second Advancement",
        "<gradient:cardinal:apple>Nice description", "", Quest.QuestVis.HIDDEN, Quest.QuestFrame.CHALLENGE, 0);

    private static final Quest qs2 = new Quest('c', ItemType.ACACIA_FENCE_GATE, 0, null, qs, "<dark_green>3rd Advancement",
        "<gradient:cardinal:apple>Nice description", "", Quest.QuestVis.PARENT, Quest.QuestFrame.TASK, 0);

    private static final Quest qs3 = new Quest('d', ItemType.ACACIA_PRESSURE_PLATE, 20, null, qs2, "<dark_green>4th Advancement",
        "<gradient:cardinal:apple>Nice description", "block/dirt", Quest.QuestVis.ALWAYS, Quest.QuestFrame.TASK, 0);



    private static final Quest qs4 = new Quest('e', ItemType.ACACIA_SLAB, 0, null, null, "<dark_green>5th Advancement",
        "<gradient:cardinal:apple>Nice description", "block/bedrock", Quest.QuestVis.ALWAYS, Quest.QuestFrame.TASK, 0);

    private static final Quest qs5 = new Quest('f', ItemType.ACACIA_LEAVES, 2, null, qs4, "<dark_green>6th Advancement",
        "<gradient:cardinal:apple>Nice description", "", Quest.QuestVis.ALWAYS, Quest.QuestFrame.CHALLENGE, 0);*/

  //@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (!ApiOstrov.canBeBuilder(p)) return;
        final ItemStack inHand = e.getItem();
        if (inHand == null) return;

        if (inHand.getType() == Material.WOODEN_PICKAXE) {
            e.setCancelled(true);
            p.sendMessage("§8TestListener - interact cancel! " + e.getAction());
          ItemStack is = new ItemBuilder(ItemType.GOAT_HORN).set(
              DataComponentTypes.INSTRUMENT, MusicInstrument.DREAM_GOAT_HORN).build();
          p.getWorld().dropItemNaturally(p.getEyeLocation(), is);
            //final Player semen = Bukkit.getPlayerExact("semen");
            //if (semen!=null) {
            //     p.sendMessage("canSee?"+canSee(p, semen));
            //    return;
            //}

            if (e.getAction() == Action.RIGHT_CLICK_AIR) {
                if (p.isSneaking()) {
//                    QuestManager.loadQuests();
                    //MoveUtil.safeTP(p, p.getLocation().clone().add(0, -200, 0));
                    //p.sendMessage("safeTP down");
                } else {
                    /*qs.complete(p, PM.getOplayer(p), false);
                    qs1.addProg(p, PM.getOplayer(p), 5);
                    qs3.addProg(p, PM.getOplayer(p), 5);
                    qs5.addProg(p, PM.getOplayer(p), 5);*/
                    //MoveUtil.safeTP(p, p.getLocation().clone().add(0, 200, 0));
                    //p.sendMessage("safeTP up");
                }
            } else if (e.getAction() == Action.LEFT_CLICK_AIR) {
                if (p.isSneaking()) {
                    p.sendMessage("§3teleportSave DOWN");
                    //final Location loc = p.getLocation().clone().add(0, -200, 0);
                    //MoveUtil.teleportSave(p, loc, true);
                } else {
                    p.sendMessage("§3teleportSave UP");
                    //final Location loc = p.getLocation().clone().add(0, -200, 0);
                    //MoveUtil.teleportSave(p, loc, true);
                }
            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                //Nms.zoom(p, p.isSneaking() ? 10f : 11f);
            } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

            }
        }

        if (inHand.getType() == Material.ENCHANTED_BOOK) {
            //CustomEnchant.CHANNELING.level(inHand, 1, false);
            p.getInventory().setItemInMainHand(inHand);
            return;
        }

        if (inHand.getType() == Material.DRAGON_BREATH) {
            final Location loc = p.getEyeLocation();
            final Vector dir = loc.getDirection();
            final Vector nd = new Vector(-dir.getZ(), 0d, dir.getX()).normalize();
            loc.setDirection(dir.rotateAroundNonUnitAxis(nd, 10));
            p.setRotation(loc.getYaw(), loc.getPitch());
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
