package ru.komiss77.listener;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.AfkExt;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.bots.Botter;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WXYZ;

public class TestLst implements Listener {

    Botter bt = null;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void test(PlayerInteractEvent e) {
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
                    op.tag(true);
                    op.tag("dddd", "dddf");
                } else {
                    op.tag(false);
                }

            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                //Nms.PlaceType pt = Nms.isSafeLocation(p, new WXYZ(e.getClickedBlock().getLocation()));
                //p.sendMessage("§3" + pt);

                if (p.isSneaking()) {
//p.sendMessage("new AdvancementManager");
                    //Nms.sendFakeEquip(p, 5, new ItemStack(e.getClickedBlock().getType()));

                    final MiniMessage mm = MiniMessage.builder().tags(
                        TagResolver.builder()
                            .resolver(StandardTags.defaults())
                            .resolver(TagResolver.resolver("amber", Tag.styling(TextColor.color(0xCC8822))))
                            .build()).build();

                    p.sendMessage(mm.deserialize("<red>I am the storm that is <amber>approaching"));
                    p.sendMessage(mm.deserialize("<red>I am the storm that is <gradient:red:amber>approaching"));

                } else {

                    if (bt == null) {
                        bt = BotManager.createBot("Ботус", p.getWorld(), new AfkExt(new WXYZ(p.getLocation())));
                        bt.item(EquipmentSlot.OFF_HAND, new ItemStack(Material.SHIELD));
                    }

                    bt.use(bt.getEntity(), Botter.BLOCK_ACT, EquipmentSlot.OFF_HAND, true);
                    Ostrov.sync(() -> {
                        bt.use(bt.getEntity(), Botter.BLOCK_ACT, EquipmentSlot.HAND, false);
                        p.sendMessage("block2");
                    }, 40);
                    final int BOW_ACT = 5;
                    Ostrov.sync(() -> {
                        bt.item(EquipmentSlot.OFF_HAND, new ItemStack(Material.SHIELD));
                        bt.item(EquipmentSlot.HAND, new ItemStack(Material.BOW));
                        bt.use(bt.getEntity(), BOW_ACT, EquipmentSlot.HAND, true);
                    }, 80);
                    Ostrov.sync(() -> {
                        bt.item(EquipmentSlot.HAND, new ItemStack(Material.BOW));
                    }, 200);

                    p.sendMessage("block");
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
                    ApiOstrov.teleportSave(p, p.getLocation().clone().add(0, -100, 0), false);

                } else {

                    p.sendMessage("§3teleportSave UP");
                    ApiOstrov.teleportSave(p, p.getLocation().clone().add(0, 100, 0), false);

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
