package ru.komiss77.listener;

import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;

public class TestLst implements Listener {

    Botter bt = null;


  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
//p.sendMessage("Interact "+Tag.BANNERS.isTagged(e.getClickedBlock().getType()));
        if (!ApiOstrov.isLocalBuilder(p)) return;
        final Oplayer op = PM.getOplayer(p);

        final ItemStack it = e.getItem();
        if (it == null) return;

        if (it.getType() == Material.WOODEN_PICKAXE) {
            e.setCancelled(true);
            p.sendMessage("§8TestListener - interact cancel! " + e.getAction());

            if (e.getAction() == Action.RIGHT_CLICK_AIR) {

              if (p.isSneaking()) {
                  p.getInventory().addItem(new ItemBuilder(ItemType.STRING).name("<red>try to blend in").rarity(ItemRarity.EPIC).edible(40, ItemUseAnimation.EAT, Sound.ENTITY_HORSE_EAT).build());
                  p.getInventory().addItem(ItemUtil.previosPage);
                  p.getInventory().addItem(ItemUtil.nextPage);
                /*WorldBorder wb = p.getWorldBorder();
                if (wb == null) {
                  wb = Bukkit.createWorldBorder();
                }
                wb.setCenter(p.getLocation());
                wb.setSize(10);
                p.setWorldBorder(wb);
                p.sendMessage("setSize(10) wb=" + wb);*/

                //MoveUtil.teleportSave(p, p.getLocation().clone().add(0, -100, 0), true);
                //op.tag(true);
                //op.tag("<blue>dddd", "<yellow>dddf");
                } else {
                //p.setWorldBorder(null);//p.getWorldBorder().setSize(100);
                /*WorldBorder wb = p.getWorldBorder();
                if (wb == null) {
                  p.sendMessage("wb = null");
                } else {
                  p.sendMessage("wb =" + wb + " world=" + (wb.getWorld() == null ? wb.getWorld() : wb.getWorld().getName())
                      + " center=" + wb.getCenter() + " size=" + wb.getSize());
                }*/
                //op.tag(false);
                //MoveUtil.teleportSave(p, p.getLocation().clone().add(0, 100, 0), true);
                   /* final BlockData gold = BlockType.GOLD_BLOCK.createBlockData();
                    LocUtil.trace(p.getEyeLocation(), p.getEyeLocation().getDirection(), 10d, (bp, bd) -> {
                        p.sendBlockChange(bp.toLocation(p.getWorld()), gold);
                        return !bd.getMaterial().asBlockType().isAir();
                    });
                    final BlockData bd = BlockType.YELLOW_CARPET.createBlockData();
                    for (final XYZ lc : AStarFinder.xyzPath(new WXYZ(p.getTargetBlockExact(100)), new WXYZ(p.getLocation()), 10000, true)) {
                        p.sendBlockChange(lc.getCenterLoc(p.getWorld()), bd);
                    }*/
                }

            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                //Nms.PlaceType pt = Nms.isSafeLocation(p, new WXYZ(e.getClickedBlock().getLocation()));
                //p.sendMessage("§3" + pt);

                if (p.isSneaking()) {
                    //GameApi.setFastMat(new WXYZ(e.getClickedBlock().getLocation()), 4, 1, 4, Material.AIR);

                   /*final MiniMessage mm = MiniMessage.builder().tags(
                        TagResolver.builder()
                            .resolver(StandardTags.defaults())
                            .resolver(TagResolver.resolver("amber", Tag.styling(TextColor.color(0xCC8822))))
                            .build()).build();

                    p.sendMessage(mm.deserialize("<red>I am the storm that is <amber>approaching"));
                    p.sendMessage(mm.deserialize("<red>I am the storm that is <gradient:red:amber>approaching"));*/

                } else {
                    //GameApi.setFastMat(new WXYZ(e.getClickedBlock().getLocation()), 4, 1, 4, Material.GOLD_BLOCK);
                    /*if (bt == null) {
                        bt = BotManager.createBot("Ботус", p.getWorld(), new AfkExt(new WXYZ(p.getLocation())));
                        bt.item(EquipmentSlot.OFF_HAND, new ItemStack(Material.SHIELD));
                    } else {
                        final LivingEntity le = bt.getEntity();
                        if (le != null && le.isValid()) {
                            le.teleport(p.getLocation());
                        }
                    }*/

                    /*Ostrov.sync(() -> {
                        bt.item(EquipmentSlot.HAND, new ItemStack(Material.BOW));
                    }, 200);*/
                    //ApiOstrov.teleportSave(p, p.getLocation().clone().add(0, 100, 0), true);

                }

            } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

                if (p.isSneaking()) {

                } else {

                    //   bot.tag(false);
                    //forEntity(bot.getBukkitEntity()).setHidden(true);
                    // p.sendMessage("tag off");
                    // Lang.sendMessage(p, "Изменить паспортные данные");
                    //  ApiOstrov.sendTitle(p, "§7title=", ""+ ++count, 10, 40, 10);
                }
                //p.sendMessage("name="+Translate.getMaterialName(e.getClickedBlock().getType(), EnumLang.RU_RU));
            } else if (e.getAction() == Action.LEFT_CLICK_AIR) {
                // op.addCd("test", count++);
                if (p.isSneaking()) {

                    p.sendMessage("§3teleportSave DOWN");
                    final Location loc = p.getLocation().clone().add(0, -100, 0);
//                    long t = System.currentTimeMillis();
                  //MoveUtil.safeTP(p, loc, true);
                    /*p.sendMessage("n-" + (System.currentTimeMillis() - t));
                    t = System.currentTimeMillis();
                    MoveUtil.teleportSave(p, loc, true);
                    p.sendMessage("n-" + (System.currentTimeMillis() - t));*/

                } else {

                    p.sendMessage("§3teleportSave UP");
                    final Location loc = p.getLocation();
                    loc.setY(loc.getWorld().getMaxHeight());
//                    long t = System.currentTimeMillis();
                  //MoveUtil.safeTP(p, loc, true);
                    /*p.sendMessage("n-" + (System.currentTimeMillis() - t));
                    t = System.currentTimeMillis();
                    MoveUtil.teleportSave(p, loc, true);
                    p.sendMessage("n-" + (System.currentTimeMillis() - t));*/

                }
                //p.sendMessage("name="+Translate.getMaterialName(e.getClickedBlock().getType(), EnumLang.RU_RU));
            }
        }

        if (it.getType() == Material.ENCHANTED_BOOK) {
            //CustomEnchant.CHANNELING.level(it, 1, false);
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
