package ru.komiss77.listener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.commands.CMD;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import static ru.komiss77.listener.ServerListener.block_nether_portal;
import ru.komiss77.menu.SignEditSelectLine;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.VM;


public class PlayerListener implements Listener {

/*
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onNetherCreate(PortalCreateEvent e) { 
        System.out.println("PortalCreateEvent isCancelled?"+e.isCancelled());
    }



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




    private static boolean set_gm = false;
    private static GameMode gm_on_join = GameMode.ADVENTURE;
    private static boolean tp_on_join = false;
    private static float walkspeed_on_join = 0.1F;
    public static boolean clear_stats = false;
    private static boolean disable_void;
    public static boolean disable_damage;
    private static boolean disable_hungry;
    public static boolean enable_jump_plate;
    public static boolean disable_break_place;
    public static boolean disable_lava;
    public static int test;
    
    public static ItemStack signEdit;
    public static Map<String,String[]> signCache;

    
    public static void GetVar () {
        
        signEdit = new ItemBuilder(Material.WARPED_SIGN)
                .name("??f???????????????? ???? ??????????????????")
                .lore("")
                .lore("??7???????? ???? ????????????????.")
                .lore("")
                .lore("??7?????? - ??????????????????????????")
                .lore("??7????????+?????? - ?????????????? ??????")
                .lore("")
                .lore("??7?????? - ??????????????????????")
                .lore("??7????????+?????? - ????????????????")
                .lore("")
                .build();
        signCache = new HashMap<>();
        set_gm = Cfg.GetCongig().getBoolean("player.change_gamemode_on_join");
        gm_on_join = GameMode.valueOf( Cfg.GetCongig().getString("player.gamemode_set_to") );
        tp_on_join = Cfg.GetCongig().getBoolean("player.teleport_on_first_join");
        walkspeed_on_join = Float.valueOf( Cfg.GetCongig().getString("player.walkspeed_on_join") );
        clear_stats = Cfg.GetCongig().getBoolean("player.clear_stats");
        disable_void = Cfg.GetCongig().getBoolean("player.disable_void");
        disable_damage = Cfg.GetCongig().getBoolean("player.disable_damage");
        disable_hungry = Cfg.GetCongig().getBoolean("player.disable_hungry");
        enable_jump_plate = Cfg.GetCongig().getBoolean("modules.enable_jump_plate");
        disable_break_place = Cfg.GetCongig().getBoolean("player.disable_break_place");
        disable_lava = Cfg.GetCongig().getBoolean("player.disable_lava");

        test=0;
    }    


    public static void Init () {
        GetVar ();
    }    

    public static void ReloadVars () {
        //anti_quiter.clear();
        GetVar ();
    }
    
 
    //?????????????? ??????????????????,????????????????,????????????
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void recipeDiscover (PlayerRecipeDiscoverEvent e) {
        if (Bukkit.getServer().getMotd().length()==4 || Bukkit.getServer().getMotd().startsWith("lobby")) {
            e.setCancelled(true);
        }
    }
    public void advancementDone (PlayerAdvancementDoneEvent e) {
        if (Bukkit.getServer().getMotd().length()==4 || Bukkit.getServer().getMotd().startsWith("lobby")) {
            e.getPlayer().sendMessage("aaa "+e.getAdvancement());
        }
    }
   /* @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void test(PlayerInteractEvent e) {
        if (e.getItem()==null) return;
        
        //if (e.getItem().getType().toString().startsWith("LEATHER_")) {
        //    Bukkit.broadcastMessage("leather, "+e.getItem().toString()+" hasMeta?"+e.getItem().hasItemMeta());
       // }
        //e.getPlayer().sendMessage("++ "+LanguageHelper.getItemName(e.getBlock().getType(), "ru_ru"));
        //Bukkit.broadcastMessage("toString "+e.getBlock().getType().toString());
        //Bukkit.broadcastMessage("name "+e.getBlock().getType().name());
        //Bukkit.broadcastMessage("getNamespace "+e.getBlock().getType().getKey().getNamespace());
        //Bukkit.broadcastMessage("getKey "+e.getBlock().getType().getKey().getKey());

       // Bukkit.broadcastMessage("res="+(e.getItem().getType().isBlock()?"block":"item") + "."
       //         + e.getItem().getType().getKey().getNamespace() + "." 
       //         + e.getItem().getType().getKey().getKey());
        
      //  Bukkit.broadcastMessage("lang="+LanguageHelper.getMaterialName(e.getItem().getType(), "ru_ru"));
        //Bukkit.broadcastMessage("biome="+LanguageHelper.getBiomeName(e.getPlayer().getLocation().getBlock().getBiome(), "ru_ru"));
        // (e.getItem().getType().toString().endsWith("_SPAWN_EGG")) {
        //        final EntityType type = EntityType.valueOf( e.getItem().getType().toString().replaceFirst("_SPAWN_EGG", ""));
       // Bukkit.broadcastMessage("entity="+LanguageHelper.getEntityName(type, "ru_ru"));
       // }

        //if (!e.getItem().getEnchantments().isEmpty()) {
       //     for (Enchantment enc : e.getItem().getEnchantments().keySet()) {
       // Bukkit.broadcastMessage("enc="+LanguageHelper.getEnchantmentName(enc, "ru_ru")+" - "+ LanguageHelper.getEnchantmentLevelName(e.getItem().getEnchantments().get(enc), "ru_ru"));
                
       //     }
      //  }
    }*/

/*
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChange(final PlayerChangedWorldEvent e) {
        final Player p = e.getPlayer();
        if (p.isInsideVehicle()) {
            final Entity ent = p.getVehicle();
            if (ent.getType()==EntityType.DONKEY || ent.getType()==EntityType.HORSE || ent.getType()==EntityType.MULE || ent.getType()==EntityType.LLAMA) {
                final ChestedHorse chest = (ChestedHorse)ent;
                final Inventory inv = (Inventory)chest.getInventory();
                //try {
                    for (final HumanEntity p2 : inv.getViewers()) {
                        ((Player)p2).closeInventory();
                    }
               // }
                //catch (ConcurrentModificationException ex) {
               //     this.getServer().getConsoleSender().sendMessage("ConcurrentModificationException encountered!");
               // }
            }
        }
    }*/


    
    
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void FriendTeleport(FriendTeleportEvent e) {
        if (PM.inBattle(e.source.getName())) e.Set_canceled(true, "??c??????????.");
    }





    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void Sign_create(SignChangeEvent e) {

        if ( ChatColor.stripColor(e.getLine(0)).equalsIgnoreCase("[??????????????]") || ChatColor.stripColor(e.getLine(0)).equalsIgnoreCase("[??????????]")) {
            if (ApiOstrov.isLocalBuilder(e.getPlayer(), true)) {
                e.setLine(0, "??8"+e.getLine(0));
            } else {
                e.setLine(0, "??2"+e.getLine(0));
                //e.setLine(1, "??5"+e.getLine(1));
            }
        } else {
            e.setLine(0, e.getLine(0).replaceAll("&", "??"));
        }
        
        e.setLine(1, e.getLine(1).replaceAll("&", "??"));
        e.setLine(2, e.getLine(2).replaceAll("&", "??"));
        e.setLine(3, e.getLine(3).replaceAll("&", "??"));
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void Interact (PlayerInteractEvent e) {
//System.out.println("Sign_click 111");
        if ( e.getAction()==Action.PHYSICAL ) return;
        //if (e.getClickedBlock().getType()==Material.SIGN || e.getClickedBlock().getType()==Material.SIGN_POST || e.getClickedBlock().getType()==Material.WALL_SIGN ) {
        
            
        if (ApiOstrov.isLocalBuilder(e.getPlayer(), false)) {
            if ( (Tag.SIGNS.isTagged(e.getClickedBlock().getType()) || Tag.WALL_SIGNS.isTagged(e.getClickedBlock().getType())) && ItemUtils.compareItem(signEdit, e.getItem(), false)) {
                final Player p = e.getPlayer();
                e.setCancelled(true);
                
                Sign sign = (Sign)e.getClickedBlock().getState();
                if (e.getAction()==Action.LEFT_CLICK_BLOCK) {
                    if (p.isSneaking()) {
                        final Block b = e.getClickedBlock();
                        final List<Material> types = new ArrayList<>( Tag.WALL_SIGNS.getValues());
                        int order = types.indexOf(b.getType());
                        order++;
                        if (order>=types.size()) order=0;
                        final String[] lns = sign.getLines();
//System.out.println("Sign_click "+b.getType());
                         
                        if (Tag.WALL_SIGNS.isTagged(b.getType())) {
                            final WallSign wsData = (WallSign) types.get(order).createBlockData();//org.bukkit.block.data.type.WallSign
                            wsData.setFacing(((Directional)b.getBlockData()).getFacing());
                            wsData.setWaterlogged(((Waterlogged) b.getBlockData()).isWaterlogged());
                            b.setBlockData(wsData);
                        } else if (Tag.SIGNS.isTagged(b.getType())) {
                            final org.bukkit.block.data.type.Sign snData = (org.bukkit.block.data.type.Sign) types.get(order).createBlockData();//org.bukkit.block.data.type.Sign
                            snData.setRotation(((Rotatable) b.getBlockData()).getRotation());
                            snData.setWaterlogged(((Waterlogged) b.getBlockData()).isWaterlogged());
                            b.setBlockData(snData);
                        }
                        
                        sign = (Sign)b.getState();
                        for (byte i = 0; i < 4; i++) {
                            sign.setLine(i, lns[i]);
                        }
                        sign.update();
                        
                    } else {
                        SmartInventory.builder()
                            .type(InventoryType.HOPPER)
                            .id("SignEditSelectLine"+p.getName()) 
                            .provider(new SignEditSelectLine(sign))
                            .title("??f???????????????? ????????????")
                            .build()
                            .open(p);
                    }
                } else if (e.getAction()==Action.RIGHT_CLICK_BLOCK) {
                    if (p.isSneaking()) {
                        if (signCache.containsKey(p.getName())) {
                            sign.setLine(0, signCache.get(p.getName())[0]);
                            sign.setLine(1, signCache.get(p.getName())[1]);
                            sign.setLine(2, signCache.get(p.getName())[2]);
                            sign.setLine(3, signCache.get(p.getName())[3]);
                            sign.update();
                        } else {
                            p.sendMessage("?? ???????????? ?????? ?????????????????????????? ????????????????.");
                        }
                    } else {
                        if (!signCache.containsKey(p.getName())) {
                            signCache.put(p.getName(), new String[4]);
                        }
                        signCache.get(p.getName())[0] = sign.getLine(0);
                        signCache.get(p.getName())[1] = sign.getLine(1);
                        signCache.get(p.getName())[2] = sign.getLine(2);
                        signCache.get(p.getName())[3] = sign.getLine(3);
                        p.sendMessage("???????????????????? ???????????????? ?????????????????????? ?? ??????????. ????????+?????? ???? ???????????? - ????????????????.");
                    }
                }
                return;
            }
        }
        
        
        if ( e.getAction()==Action.RIGHT_CLICK_BLOCK) {
            
            if (disable_lava && e.getItem()!=null && e.getItem().getType().toString().contains("LAVA") && !ApiOstrov.isLocalBuilder(e.getPlayer(), false)) {
                e.setUseItemInHand(Event.Result.DENY);
                ApiOstrov.sendActionBarDirect(e.getPlayer(), "??c???????? ?????????????????? ???? ???????? ??????????????!");
                return;
            }

            if (Tag.WALL_SIGNS.isTagged(e.getClickedBlock().getType())) {
                final Sign sign = (Sign)e.getClickedBlock().getState();
                final String line0=ChatColor.stripColor( sign.getLine(0).toLowerCase());
                final String line1=ChatColor.stripColor( sign.getLine(1));
                if (line0.isEmpty() || line1.isEmpty()) return;
    //System.out.println("Sign_click 222 "+line0);
                switch (line0) {
                    case "[??????????????]":
                        if (ServerListener.checkCommand(e.getPlayer(), line1.toLowerCase())) return;
                        e.getPlayer().performCommand(line1.toLowerCase());
                        return;

                    case "[??????????]":
                        e.getPlayer().performCommand( "warp "+ChatColor.stripColor(line1.toLowerCase()) );
                        return;
                    //case"[money]":
                    //    if (e.getPlayer().isOp() && Ostrov.isInteger(line1)) Ostrov.moneyChange(e.getPlayer(), Integer.valueOf(line1), Ostrov.prefix+":"+line1);
                    //     e.getPlayer().sendMessage( "local balance="+Ostrov.moneyGetBalance(e.getPlayer()) );
                    //    break;
                    //case"[stat]":
                    //     if (e.getPlayer().isOp()) ApiOstrov.addIntStat(e.getPlayer(), E_Stat.valueOf(line1));
                    //    break;
                    default:
                        break;

                }
            }
        }
        

        
        
    }
    
    





    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    @EventHandler(priority = EventPriority.HIGHEST) 
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        final Player p = e.getPlayer();
        ApiOstrov.sendTabList(p, "", "");
        
        PM.createOplayer(p);
            
        if ( set_gm && !p.isOp() ) p.setGameMode(gm_on_join);
        if ( walkspeed_on_join>0)  p.setWalkSpeed(walkspeed_on_join);

        if ( tp_on_join ){
            if (!p.hasPlayedBefore() ){
                if (ServerListener.spawn!=null) {
                    p.teleport(ServerListener.spawn);
                } else {
                    p.teleport (Bukkit.getWorlds().get(0).getSpawnLocation());
                }
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
            } else {
                ApiOstrov.teleportSave(p, p.getLocation());  
            }
        }

        if ( clear_stats && !ApiOstrov.isLocalBuilder(p, false)) {
            p.setFireTicks(0);
            p.setFlying(false);
            p.setAllowFlight(false);
            if ( disable_hungry ) p.setFoodLevel(20);
            if ( disable_damage ) p.setHealth(20);
            p.getInventory().clear();
            p.updateInventory();
            p.getActivePotionEffects().stream().forEach((pe) -> {  p.removePotionEffect(pe.getType()); });
        }
        
    }

    
    @EventHandler( priority = EventPriority.HIGHEST )
    public void onBungeeDataRecieved (BungeeDataRecieved e) {
        final Player p = e.getPlayer();
        if (PM.exist(p.getName())) {
            PM.getOplayer(p.getName()).loadLocalData(p);
        }
        if (ApiOstrov.canBeBuilder(p)) {
            p.sendMessage("??f* ?? ?????? ???????? ?????????? ??e?????????????????? ??f???? ???????? ??????????????.");
            TextComponent msg = new TextComponent( "??a>>>> ??f???????? ???????? - ?????????????????? ?????????????? /builder ??a<<<<" );
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("??a???????????????? ????1 ?? ?????????????? ???????? ??????????????????"));
            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/builder");
            msg.setHoverEvent( he );
            msg.setClickEvent( ce );
            p.spigot().sendMessage(msg);
        }
    }   
    
    
    
    
    @EventHandler(priority = EventPriority.HIGHEST) 
    public void PlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        PM.onExit(e.getPlayer());
        if (PM.nameTagManager!=null) PM.nameTagManager.reset(e.getPlayer().getName());
            
       /* if (e.getPlayer().isInsideVehicle()) {
            final Entity ent = e.getPlayer().getVehicle();
            if (ent.getType()==EntityType.DONKEY || ent.getType()==EntityType.HORSE || ent.getType()==EntityType.MULE || ent.getType()==EntityType.LLAMA) {
                final ChestedHorse chest = (ChestedHorse)ent;
                final Inventory inv = (Inventory)chest.getInventory();
                for (final HumanEntity p2 : inv.getViewers()) {
        Caused by: java.util.ConcurrentModificationException
        at java.util.ArrayList$Itr.checkForComodification(Unknown Source) ~[?:1.8.0_251]
        at java.util.ArrayList$Itr.next(Unknown Source) ~[?:1.8.0_251]
        at ru.komiss77.Listener.PlayerListener.PlayerQuit(PlayerListener.java:438) ~[?:?]
                    ((Player)p2).closeInventory();
                }
            }
        }*/
    }
  
    
    
    
    
        
        
        
        
 // ----------------------------- ACTION ----------------------
            
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onPlace(BlockPlaceEvent e) {
        //PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.????????????_??????????();
        if ( disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer()) ) e.setCancelled(true);
        else if (!clear_stats) PM.Addbplace(e.getPlayer().getName());
    }
    
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onBreak(BlockBreakEvent e) {
      //  PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.????????????_??????????();
        if ( disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer()) ) e.setCancelled(true);
        else if (!clear_stats) PM.Addbbreak(e.getPlayer().getName());
    }
 
        
        
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent e) {
        if ( e.getRemover().getType()==EntityType.PLAYER && !Ostrov.isCitizen(e.getEntity()) ) {
                if ( disable_break_place &&  !ApiOstrov.isLocalBuilder((Player) e.getRemover()) ) e.setCancelled(true);
        } 

    }
       
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onHangingBreakEvent(HangingBreakEvent e) {
        if ( e.getEntity() instanceof Player) {
                if ( disable_break_place &&   !ApiOstrov.isLocalBuilder((Player) e.getEntity()) ) e.setCancelled(true);
        } 
    }
    
    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent e)
    {
        if( PlayerListener.disable_break_place && e.getRightClicked().getType() ==EntityType.ARMOR_STAND && !e.getPlayer().isOp() ) e.setCancelled(true);
    }


   

    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e){
        if ( PlayerListener.disable_break_place && !e.getPlayer().isOp()) e.setCancelled(true);
    }    
        
    
//---------------------------------------------------
       
        
        
        
        
        
        
        
        
        
        
        
        
        
// ----------------------------------- MOVE --------------------------------
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onPlayerMove (PlayerMoveEvent e) { 
    if (!PlayerListener.enable_jump_plate) return;
    if (e.getFrom().getBlockX()==e.getTo().getBlockX() && e.getFrom().getBlockY()==e.getTo().getBlockY() && e.getFrom().getBlockZ()==e.getTo().getBlockZ()  ) return;

    if ( e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.REDSTONE_BLOCK ||
            e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.LAPIS_BLOCK  ) {

        switch (e.getPlayer().getLocation().getBlock().getType()) {
            case OAK_PRESSURE_PLATE:
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().setY(1.1D));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP , 3F, 2.0F);
                break;

            case STONE_PRESSURE_PLATE:
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.2D).setY(1.2D));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP , 3F, 2.0F);
                break;

            case LIGHT_WEIGHTED_PRESSURE_PLATE:
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.4D).setY(1.4D));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP , 3F, 2.0F);
                break;

            case HEAVY_WEIGHTED_PRESSURE_PLATE:
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.8D).setY(1.8D));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP , 3F, 2.0F);
                break;

            default:
                break;
        }
    }

   }


        
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (Ostrov.isCitizen(e.getPlayer())) return;
            //if (give_pipboy ) ItemUtils.Add_to_inv(e.getPlayer(), give_pipboy_slot, ItemUtils.pipboy, false, false);
            PM.getOplayer(e.getPlayer().getName()).pvpBattleModeEnd();
            if ( !PM.OP_GetHomeList(e.getPlayer().getName()).isEmpty() ) e.getPlayer().performCommand("home home");
        }      
    
 
    
    
    
    /*
    @EventHandler(  priority = EventPriority.MONITOR, ignoreCancelled = false) 
    public void onEntityPortalEnterEvent(PlayerChangedWorldEvent e) {  //?????? ???????????? ???????????????? ??????????????
        
        
//System.out.println("PlayerChangedWorldEvent cgetLocation="+e.());
    }
    */
    
 //   @EventHandler(  priority = EventPriority.HIGH, ignoreCancelled = true) 
//    public void onPlayerPortalEvent(PlayerPortalEvent e) {
//System.out.println("onPlayerPortalEvent cause="+e.getCause()+" getCreationRadius="+e.getCreationRadius()+" getSearchRadius="+e.getSearchRadius()+" canceled?"+e.isCancelled());
        //final Player p = e.getPlayer();
        //if (Timer.has(e.getPlayer(), "portal")) {
        //    ApiOstrov.sendActionBarDirect(e.getPlayer(), "??e*?????????????????? "+Timer.getLeft(e.getPlayer(), "portal")+"??????. ???? ???????????????????? ?????????????????????????? ??????????????! ");
        //    e.setCancelled(true);//e.setTo(e.getFrom());
        //    return;
        //}
        //Timer.add(e.getPlayer(), "portal", 15);
   // }
    /*
    
    @EventHandler(  priority = EventPriority.MONITOR, ignoreCancelled = false) 
    public void onEntityPortalEnterEvent(EntityPortalEnterEvent e) {  //?????? ???????????? ???????????????? ??????????????
        if (e.getEntityType()!=EntityType.PLAYER) return;
        
System.out.println("EntityPortalEnterEvent cgetLocation="+e.getLocation());
    }*/
    
    
    @EventHandler ( ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerTeleportEvent e) {
        
//System.out.println(">>>>>>>>> PlayerTeleportEvent "+e.getCause()+" canceled?"+e.isCancelled());
        
        if (e.getTo() == null)  return;
         
        //if ( e.getCause() == TeleportCause.END_GATEWAY || (e.getFrom().getWorld().getName().equals("world_the_end") && e.getCause() == TeleportCause.END_GATEWAY) )  {
        //    e.setCancelled(true);
       //     return;
       // }
        
        String world_from = e.getFrom().getWorld().getName();
        String world_to = e.getTo().getWorld().getName();
        
        if (world_from.equals(world_to)) return;                                            //??????????????, ???????? ?????? ???? ??????????????
        
       // if (e.getCause()==TeleportCause.END_GATEWAY || e.getCause()==TeleportCause.END_PORTAL || e.getCause()==TeleportCause.NETHER_PORTAL) {
      //      if (Timer.has(e.getPlayer(), "portal")) {
      //          ApiOstrov.sendActionBarDirect(e.getPlayer(), "??e*?????????????????? "+Timer.getLeft(e.getPlayer(), "portal")+"??????. ???? ???????????????????? ?????????????????????????? ??????????????! ");
       //         e.setTo(e.getFrom());
       //         return;
      //      }
      //      Timer.add(e.getPlayer(), "portal", 15);
      //  }
        
        
        if (CMD.no_damage_on_tp != -1) {
            //Timer.CD_add(e.getPlayer().getName(), "nodamage", CMD.no_damage_on_tp);
            PM.getOplayer(e.getPlayer().getName()).no_damage=CMD.no_damage_on_tp;
        }
        
//System.out.println("?????????? ???????? "+e.getPlayer().getName()+" ????:"+from+" ??:"+to );

        if (CMD.save_location_on_world_change ) {                                                    //???????? ?????? ????????????????
//System.out.println("?????????? ???????? - ?????????????????? world_pos" );
            PM.OP_Set_world_position(e.getPlayer(), world_from);                      //?????????????????? ?????????? ????????????
            
            if ( e.getCause() ==  PlayerTeleportEvent.TeleportCause.COMMAND )  {    //???? ???????????????????????? ???????? ???????????????? home
                return;
            }
            
            if (PM.OP_Get_world_positions(e.getPlayer().getName()).contains(world_to)) {  //???????? ???????? ??????????????????????
//System.out.println("?????????? ???????? - ???????? ??????????????????????" );
                Location stored = PM.OP_Get_world_position_at(e.getPlayer(), world_to);   //?????????????????????????????? ?????????????????????? ??????????????
                    if (stored==null) return;
                    if ( ApiOstrov.isLocationSave(e.getPlayer(), stored) ) {    //???????? ???????????????????? ?????????? ??????????????????,
                        e.setTo(stored);
                        e.getPlayer().sendMessage( "??2???? ?????????????? ?? ?????? "+world_to+" ?? ???????????????????? ???? ?????????? ????????????.");
                    } else {
                        stored = ApiOstrov.findNearestSaveLocation(stored);
                        if (stored != null) {
                            e.setTo(stored);
                            e.getPlayer().sendMessage( "??4???? ?????????????? ?????????????????????? ?????? ?? ?????????? ???????????? ???? ????????, ?????????? ???????????????????? ??????????.");
                        } else {
                            e.getPlayer().sendMessage( "??4???? ?????????????? ?????????????????????? ?????? ?? ?????????? ???????????? ???? ????????, ?????????? ??????????????????.");
                        }
                    }
            }
        }
        
    }
        
        
    
 //------------------------------------------------------------------------   
 
        

    
 

    
    
    
    
    
    
    
    
// ------------------------------- ITEM -------------------------------------------    
 
    

    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EntityBowShoot(EntityShootBowEvent e) {
        if (e.getEntityType()==EntityType.PLAYER && !Ostrov.isCitizen(e.getEntity())) {

            final Player p = (Player) e.getEntity();
            
            if (ApiOstrov.getMenuItemManager().hasItem("tpbow")) {//if (give_bow_teleport) {
                final MenuItem si = ApiOstrov.getMenuItemManager().fromItemStack(e.getBow());
//System.out.println("EntityBowShoot si="+si);        
                //if ( p.getItemInHand().hasItemMeta() && p.getItemInHand().getItemMeta().hasDisplayName() && p.getItemInHand().getItemMeta().getDisplayName().equals(ItemUtils.tpbow.getItemMeta().getDisplayName()) ) {
                if (si!=null) {
                    if (Timer.has(p, "bow_teleport")) {//if (PM.getOplayer(p.getName()).bow_teleport_cooldown>0) {
                        p.sendMessage("??c?????????????????????? ????????.. ???????????????? ??4"+Timer.getLeft(p, "bow_teleport")+" ??????.");
                        e.setCancelled(true);
                        e.getProjectile().remove();
                    } else {
                        Timer.add(p, "bow_teleport", 4);
                        e.getProjectile().setMetadata("bowteleport", new FixedMetadataValue(Ostrov.instance, "ostrov"));
                        //e.getProjectile().setPassenger(p);
                        //PM.getOplayer(p.getName()).bow_teleport_cooldown=2;
                    }
                }
            }
            
            if ( CMD.fly_block_atack_on_fly && !p.isOp()) {
                if ( p.getGameMode().equals(GameMode.CREATIVE)) {
                    ApiOstrov.sendActionBar(p, "??c?????? ?? ???????????????? ????????????????????????!");
                    e.setCancelled(true);
                    //return;
                } else if (p.isFlying()) {
                    ApiOstrov.sendActionBar(p, "??c?????? ?? ???????????? ????????????????????????!");
                    e.setCancelled(true);
                    //return;
                }
            }
            
        }
    }    
  
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void ProjectileHitEvent(final ProjectileHitEvent e) {
        
        if (ApiOstrov.getMenuItemManager().hasItem("tpbow") && e.getEntity().getShooter() instanceof Player && e.getEntity().hasMetadata("bowteleport")) {
            Location destination =  (e.getEntity()).getLocation().clone();
            e.getEntity().remove();
            final Player p = (Player)e.getEntity().getShooter();
            destination.setPitch(p.getLocation().getPitch());
            destination.setYaw(p.getLocation().getYaw());
            p.teleport(destination, PlayerTeleportEvent.TeleportCause.COMMAND);
            p.playSound(p.getLocation(),Sound.ENTITY_BAT_HURT, 2, 1);
        }
        
        //if (e.getEntity().getShooter() instanceof LivingEntity) {
        //    boolean cancel=????????????????_????????????_??????((Entity) e.getEntity().getShooter(), e.getHitEntity());
            //if (cancel) {
            //    e.setIntensity(target, 0);
            //    e.setCancelled(true);
            //}
        //}

    }
        
// ------------------------------------------------------------------------
    
    
    
    
    
    
    
    
    
    
    
// ---------------------------- ???????????? ?????????? ---------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) { 
        if ( e.getEntityType()==EntityType.PLAYER && !Ostrov.isCitizen(e.getEntity()) ) {
                    
            switch (e.getCause()) {
                case VOID:
                    if (disable_void) {
                        e.setDamage(0);
                        ((Player) e.getEntity()).teleport (Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                    }
                    return;
                    
                case FALL:
                case THORNS:        //???????? ???????? ???? ????????????-?????????? ??????????????????????
                case LIGHTNING:     //????????????
                case DRAGON_BREATH: //?????????????? ??????????????
                case CONTACT:       //??????????????
                case FIRE:          //??????????
                case FIRE_TICK:     //??????????????
                case HOT_FLOOR:     //BlockMagma
                case CRAMMING:      //EntityVex
                case DROWNING:      //??????????????????
                case STARVATION:    //??????????
                case LAVA:
                    if ( disable_damage ) e.setCancelled(true);
                    return;
                    //break;
                    
                default:
                    if ( disable_damage ) e.setCancelled(true);
                    //return;
            }
        } else if ( disable_damage ) e.setCancelled(true);
        
    }
   
    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void EntityDeath (EntityDeathEvent e) {
        if (!clear_stats && e.getEntity().getKiller()!=null && e.getEntity().getKiller().getType()==EntityType.PLAYER && !Ostrov.isCitizen(e.getEntity().getKiller())) {
            
            //switch (Type.valueOf(e.getEntityType().toString()).getMeta()) {
            switch (VM.getNmsEntitygroup().getEntytyType(e.getEntity())) {
                case MONSTER:
                    PM.Addmonsterkill(e.getEntity().getKiller().getName());
                    break;
                case CREATURE:
                case AMBIENT:
                case WATER_CREATURE:
                    PM.Addmobkill(e.getEntity().getKiller().getName());
                    break;
                case UNDEFINED:
                    if (e.getEntityType()==EntityType.PLAYER && !Ostrov.isCitizen(e.getEntity())) PM.Addmonsterkill(e.getEntity().getKiller().getName());
                    break;

            } 
            //if (e.getEntity() instanceof Monster) PM.Addmonsterkill(e.getEntity().getKiller().getName());
            //else if (e.getEntity() instanceof LivingEntity) {
            //    if (e.getEntity() instanceof Player) PM.Addpkill(e.getEntity().getKiller().getName());
            //    else PM.Addmobkill(e.getEntity().getKiller().getName());
            //}
        }
    }

//------------------------------------------------------------------------------ 
    
    
    
    

    
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerLoseFood(FoodLevelChangeEvent e) { 
        if ( disable_hungry ) {
            e.setCancelled(true);
            ((Player)e.getEntity()).setFoodLevel(20);
        }
    }


        
        
        
        
        
        

    
    
    
}
