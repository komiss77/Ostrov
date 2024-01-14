package ru.komiss77;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import ru.komiss77.enums.Data;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;



public class UniversalListenerExample implements Listener  {
    private static Plugin plugin;
    private static ItemStack mapSelector;
    private static ItemStack exit;
    public static ItemStack music;
    public static ItemStack leaveArena;
    private final ItemStack teleporter_itemstack;
    public static Inventory mapSelectMenu;
    private static final String joinCommad = "join ";
    public static final String leaveCommad = "leave ";

    public UniversalListenerExample(final Plugin plugin) {
        UniversalListenerExample.plugin=plugin;
        mapSelector = new ItemBuilder(Material.CAMPFIRE).name("§aВыбор Карты").build();
        exit = new ItemBuilder(Material.MAGMA_CREAM).name("§4Вернуться в лобби").build();
        music = new ItemBuilder(Material.NOTE_BLOCK).name("§4Музыка").addLore("§eЛКМ §7- §aвкл§7/§4выкл").addLore("§eПКМ §7- меню").build();
        leaveArena = new ItemBuilder(Material.SLIME_BALL).name("§4Покинуть Арену").build();
        teleporter_itemstack = new ItemBuilder(Material.COMPASS).name("§6ТП к доступным игрокам").build();
        mapSelectMenu = Bukkit.createInventory(null, 54, Component.text("§1Карты"));
        Inventory spectatorMenu = Bukkit.createInventory(null, 9, Component.text("§1Меню зрителя"));
        spectatorMenu.setItem(0, teleporter_itemstack);
        spectatorMenu.setItem(8, leaveArena);
    }

    
    
    
    
 /*
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            e.setCancelled(true);
            return;
        }
//System.out.println("onCreatureSpawn "+e.getSpawnReason()+" canceled?"+e.isCancelled());
        if ( ! (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG 
                || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT) ) {
            e.setCancelled(true);
//System.out.println("onCreatureSpawn setCancelled!!");
        }
    }
 
    */   
    
    
    @EventHandler
    public void onPlayerPreLogin(final PlayerLoginEvent playerLoginEvent) {
        ////if (Config.bungeeMode && (Kitbattle.bungeeMode == null || Kitbattle.bungeeMode.getMap() == null)) {
        //    playerLoginEvent.disallow(PlayerLoginEvent.Result.KICK_OTHER, Messages.NoAvailableMaps);
        //}
    }

    
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
//System.out.println("onPlayerJoin lobbyJoin");
        //lobbyJoin(e.getPlayer(), AM.lobby);
     }

    
        
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDataRecieved(final LocalDataLoadEvent e) {  

        //AM.onDataRecieved(e.getPlayer());    //load
        lobbyJoin(e.getPlayer(), Bukkit.getWorld("lobby").getSpawnLocation() );
        
        String wantArena = "";
        if (ApiOstrov.hasParty(e.getPlayer()) && !ApiOstrov.isPartyLeader(e.getPlayer())) {
            final String partyLeaderName = ApiOstrov.getPartyLeader(e.getPlayer());
            if (!partyLeaderName.isEmpty()) {
                //if (AM.getGRplayer(partyLeaderName)!=null && AM.getGRplayer(partyLeaderName).arena!=null && 
               //         (AM.getGRplayer(partyLeaderName).arena.gameState==GameState.ОЖИДАНИЕ || AM.getGRplayer(partyLeaderName).arena.gameState==GameState.СТАРТ) ) {
                //    wantArena = AM.getGRplayer(partyLeaderName).arena.name;
                    e.getPlayer().sendMessage("§aВы перенаправлены к арене лидера вашей Команды.");
                //    AM.getGRplayer(partyLeaderName).getPlayer().sendMessage("§aУчастиник вашей Команды "+(ApiOstrov.isFemale(e.getPlayer().getName())?"зашла":"зашел")+" на арену.");
                //}
            }
        }

        if (wantArena.isEmpty()) wantArena =PM.getOplayer(e.getPlayer().getName()).getDataString(Data.WANT_ARENA_JOIN);
       
            
        if (!wantArena.isEmpty()) {
            final String want = wantArena;
            new BukkitRunnable() {
                @Override
                public void run() {
                    e.getPlayer().performCommand(joinCommad+want);
                }
            }.runTaskLater(plugin, 10);
        }

    }

    
        
    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBsignLocalArenaClick (final BsignLocalArenaClick e) {
//System.out.println(" ---- BsignLocalArenaClick --- "+e.player.getName()+" "+e.arenaName);
         //Kitbattle.join(e.player, , 10);
         e.player.performCommand(joinCommad+e.arenaName);
    }
            
    
    /*@EventHandler (priority = EventPriority.MONITOR)
    public static void SignUpdateEvent (GameInfoUpdateEvent e) {}
       
       
       
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(final PlayerQuitEvent e) {
      //  final Player player = e.getPlayer();

    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(final PlayerKickEvent e) {
      //  final Player player = e.getPlayer();

    }*/

    
        
    
    
    
    
    
    public static void lobbyJoin (final Player player, final Location lobbyLocation) {
//System.out.println("lobbyJoin");
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (lobbyLocation!=null) ApiOstrov.teleportSave(player, lobbyLocation, false);
                //player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation(),PlayerTeleportEvent.TeleportCause.COMMAND);  //зациклило на onPlayerQuitArenaSpectatorEvent
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().setArmorContents(new ItemStack[4]);
                player.getInventory().clear();
                player.getInventory().setItem(0, mapSelector.clone());
                player.getInventory().setItem(4, music.clone());
                player.getInventory().setItem(7, exit.clone());
                player.updateInventory();

                player.setAllowFlight(false);
                player.setFlying(false);
                player.setExp(0.0F);
                player.setLevel(0);
                player.setSneaking(false);
                player.setSprinting(false);
                player.setFoodLevel(20);
                player.setSaturation(10.0F);
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
                player.setHealth(20.0D);
                player.setFireTicks(0);
                player.setExp(1.0F);
                player.setLevel(0);
                player.getActivePotionEffects().forEach((effect) -> {
                    player.removePotionEffect(effect.getType());
                });
                perWorldTabList(player);
                player.setWalkSpeed((float) 0.2);
                player.displayName(TCUtils.format("§7"+player.getName()));
//                final Oplayer op = PM.getOplayer(player);
                //op.tagPrefix("");
               // op.tagSuffix(": §8Не выбрана");
               // {
               //     PM.nameTagManager.updateTag(op, player);
               // }
                //player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
            }
        }.runTaskLater(plugin, 1);
    }
    
    
    
    
    
    
    @EventHandler
    public void FriendTeleport(FriendTeleportEvent e) {
        if (!e.target.getWorld().getName().equals("lobby")) e.setCanceled(true, "§f"+e.target.getName()+" §eиграет, не будем мешать!");
    }
   
   
    
    
    
    
    
    
    
    
    
    @EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = false)
    public static void onInteract(PlayerInteractEvent e) {
        
        final Player p = e.getPlayer();
        
        if ( p.getGameMode()==GameMode.SPECTATOR && (e.getAction()==Action.LEFT_CLICK_AIR || e.getAction()==Action.LEFT_CLICK_BLOCK) ) {
            if (p.getOpenInventory().getType()!=InventoryType.CHEST) {
                SmartInventory.builder()
                    .type(InventoryType.HOPPER)
                    .id("spectator") 
                    .provider(new SpectatorMenu())
                    .title("§fМеню зрителя")
                    .build()
                    .open(p);
            }
            return;
        }       
        
        if (e.getAction() == Action.PHYSICAL || e.getItem()==null) return;
        
        if ( ItemUtils.compareItem(e.getItem(), leaveArena, false)) {
            e.setCancelled(true);
            e.getPlayer().performCommand(leaveCommad);
        } else if (ItemUtils.compareItem(e.getItem(), exit, false) ) {
            e.setCancelled(true);
            ApiOstrov.sendToServer(e.getPlayer(), "lobby0", "");
        } else if ( ItemUtils.compareItem(e.getItem(), mapSelector, false)) {
            e.setCancelled(true);
            openArenaSelectMenu(e.getPlayer());
        } else if ( ItemUtils.compareItem(e.getItem(), music, false)) {
            e.setCancelled(true);
             e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 0.5F, 1);
            if (e.getAction()==Action.LEFT_CLICK_AIR || e.getAction()==Action.LEFT_CLICK_BLOCK) {
                e.getPlayer().performCommand("music switch");
            } else if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction()==Action.RIGHT_CLICK_BLOCK) {
               e.getPlayer().performCommand("music");
            }
        }
    }

    
    
    
    @EventHandler(  priority = EventPriority.NORMAL, ignoreCancelled = false)  //false = для GM 3
    public void onInventoryClick(InventoryClickEvent e) {
//System.out.println("InventoryClickEvent 1");
        if(e.getSlotType()==InventoryType.SlotType.OUTSIDE ||e.getCurrentItem()==null) return;
        final Player p = (Player) e.getWhoClicked();
        
        if (p.getGameMode()==GameMode.SPECTATOR) {
        	switch (TCUtils.toString(e.getView().title())) {
			case "§1Меню зрителя":
                e.setCancelled(true);
                if (ItemUtils.compareItem(e.getCurrentItem(), teleporter_itemstack,false)) {
                    p.openInventory(getTeleporterInventory(p));
                } else if (ItemUtils.compareItem(e.getCurrentItem(), leaveArena, false) ) {
                    e.setCancelled(true);
                    p.performCommand(leaveCommad);
                } else if (ItemUtils.compareItem(e.getCurrentItem(), mapSelector, false) ) {
                    e.setCancelled(true);
                    openArenaSelectMenu(p);
                }
				break;
			case "§6ТП к игроку":
                e.setCancelled(true);
                final Player target = Bukkit.getPlayerExact(TCUtils.stripColor(e.getCurrentItem().getItemMeta().displayName()));
                if (target == null) {
                    p.sendMessage("§cИгрок не найден");
                    p.closeInventory();
                    return;
                }
                p.teleport(target.getLocation().add(0.0, 3.0, 0.0));
                return;
			default:
				break;
			}
        }

        if (e.isCancelled()) return;
        
        if (ItemUtils.compareItem(e.getCurrentItem(), exit, false) ) {
            e.setCancelled(true);
            if(e.getAction()==InventoryAction.PICKUP_ONE || e.getAction()==InventoryAction.PICKUP_ALL) ApiOstrov.sendToServer(p, "lobby0", "");
        } else if (ItemUtils.compareItem(e.getCurrentItem(), mapSelector, false) ) {
            e.setCancelled(true);
            if(e.getAction()==InventoryAction.PICKUP_ONE || e.getAction()==InventoryAction.PICKUP_ALL) openArenaSelectMenu(p);
        } else if (ItemUtils.compareItem(e.getCurrentItem(), leaveArena, false) ) {
            e.setCancelled(true);
           if(e.getAction()==InventoryAction.PICKUP_ONE || e.getAction()==InventoryAction.PICKUP_ALL)  p.performCommand(leaveCommad);
        } else if (ItemUtils.compareItem(e.getCurrentItem(), music, false) ) {
            e.setCancelled(true);
            if(e.getAction()==InventoryAction.PICKUP_ONE || e.getAction()==InventoryAction.PICKUP_ALL) p.performCommand("music");
        }
        
        if (e.getInventory().getType()!=InventoryType.CHEST || e.getCurrentItem()==null ) return;
        if (TCUtils.toString(e.getView().title()).equals("§1Карты")) {
            e.setCancelled(true);
            final ItemStack clicked = e.getCurrentItem();
            if ( clicked.getType().toString().contains("TERRACOTTA") && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName() ) {
                if(e.getAction()==InventoryAction.PICKUP_ONE || e.getAction()==InventoryAction.PICKUP_ALL)  p.performCommand(joinCommad+TCUtils.stripColor(clicked.getItemMeta().displayName()));
            }
        }
    }
        
    
    private Inventory getTeleporterInventory(final Player p) {
        final Inventory inventory = Bukkit.createInventory(null, 54, Component.text("§6ТП к игроку"));
        //final Arena arena = AM.getArenaByWorld(p.getWorld().getName());
        //if (arena!=null) {
            //for (final Player player : arena.getPlayers(false)) {
            for (final Player player : p.getWorld().getPlayers()) {
                if (player.getGameMode()==GameMode.SPECTATOR ) {
                    continue;
                }
                inventory.addItem( new ItemBuilder(Material.PLAYER_HEAD).name("§b"+player.getName()).setSkullOwnerUuid(player.getUniqueId().toString()).build() );//plugin.getSkull(player.getName(), ChatColor.AQUA + player.getName()) );
            }
        //}
        return inventory;
    }     
    
    
    
    
    public static void openArenaSelectMenu(final Player p) {
        p.openInventory(mapSelectMenu);
        //plugin.arenaSelector.open(p);
    }
    

    public static void spectatorPrepare(final Player player) {
        player.closeInventory();
        player.getInventory().clear();
        final Iterator<PotionEffect> iterator = player.getActivePotionEffects().iterator();
        while (iterator.hasNext()) {
            player.removePotionEffect(iterator.next().getType());
        }
        player.setGameMode(GameMode.SPECTATOR);
        ApiOstrov.sendTitle(player, "§fРежим зрителя", "§a ЛКМ - открыть меню");
        player.playerListName(Component.text("§8"+player.getName()));
        player.sendMessage("§fРежим зрителя. §aЛевый клик -> открыть меню");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }    
 
        
 
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR) //стираем наметаг, или не даёт отображать скореб.команды!
    public void onTeleportChange (final PlayerTeleportEvent e) {
        if (!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())) {
            if (e.getFrom().getWorld().getName().equals("lobby")) {
            	final Oplayer op = PM.getOplayer(e.getPlayer());
              //  PM.nameTagManager.updateTag(op, e.getPlayer());
                op.score.getSideBar().reset();
            }
        }
    }
        
        
        
        
        
    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWorldChange (final PlayerChangedWorldEvent e) {
//System.out.println("PlayerChangedWorldEvent from="+e.getFrom().getName());
        //final Player p = e.getPlayer();
        new BukkitRunnable() {
            final String name = e.getPlayer().getName();
            @Override
            public void run() {
                final Player p = Bukkit.getPlayerExact(name);
                if (p==null) return;
                switchLocalGlobal(p, true);
                perWorldTabList(e.getPlayer());
            }
        }.runTaskLater(plugin, 1);
    }
        
        
        
    
    public static void perWorldTabList(final Player player) {
        for (Player other:Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals(other.getWorld().getName())) {
                player.showPlayer(plugin, other);
                other.showPlayer(plugin, player);
            } else {
                player.hidePlayer(plugin, other);
                other.hidePlayer(plugin, player);
            }
        }

    }
    
    public static void switchLocalGlobal(final Player p, final boolean notify) {
        final Oplayer op = PM.getOplayer(p);
        if (p.getWorld().getName().equalsIgnoreCase("lobby")) { //оказались в лобби, делаем глобальный
            if (op.setLocalChat(true) && notify) p.sendMessage("§8Чат переключен на глобальный");
            //if ( op.isLocalChat() ){
           //     if (notify) p.sendMessage("§8Чат переключен на глобальный");
            //    op.setLocalChat(true);
          //  }
        } else {
            if (op.setLocalChat(false) && notify) p.sendMessage("§8Чат переключен на Игровой");
           // if ( !DchatHook.isLocal(p) )  {
            //    if (notify) p.sendMessage("§8Чат переключен на Игровой");
          //      DchatHook.setLocal(p);
          //  }
        }
    }
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent e) { 

        if ( e.getEntityType()!=EntityType.PLAYER ) return;

        final Player p = (Player) e.getEntity();

        if (p.getWorld().getName().equals("lobby")) {
            e.setDamage(0);
            if (e.getCause()==EntityDamageEvent.DamageCause.VOID || e.getCause()==EntityDamageEvent.DamageCause.LAVA) {
                p.setFallDistance(0);
                p.setFireTicks(0);
                //p.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND); //от PLUGIN блокируются
                Ostrov.sync(() -> p.teleport(Bukkit.getWorld("lobby").getSpawnLocation().add(0.5, 0.6, 0.5), PlayerTeleportEvent.TeleportCause.COMMAND), 0);
                return;
            }
            e.setCancelled(true);
        }

    }      
    
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if ( (e.getEntity().getType()==EntityType.PLAYER) && e.getDamager()!=null && (e.getDamager() instanceof Firework) ){
            e.setDamage(0);
            e.setCancelled(true);
        }
    }
    
    
  // @EventHandler
  //  public void onPlayerSwapoffHand(PlayerSwapHandItemsEvent e) {
  //      if (e.getPlayer().getWorld().getName().equals("lobby") ) e.setCancelled(true);
  //  }
    
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFly(PlayerToggleFlightEvent e) {
        e.setCancelled( !ApiOstrov.isLocalBuilder(e.getPlayer(), false) );
    }

    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
//System.out.println("PlayerPickupItemEvent "+e.getItem());        
        if (e.getEntityType()==EntityType.PLAYER && e.getEntity().getWorld().getName().equals("lobby") && !ApiOstrov.isLocalBuilder(e.getEntity(), false)) {
            e.setCancelled(true);
            e.getItem().remove();
        }
    }

        
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        final ItemStack item = e.getItemDrop().getItemStack();
        if (ItemUtils.compareItem(item, mapSelector, false) || ItemUtils.compareItem(item, leaveArena, false) || ItemUtils.compareItem(item, exit, false) || ItemUtils.compareItem(item, music, false)) {
            e.setCancelled(true);
            e.getItemDrop().remove();
        }
        
        if (e.getPlayer().getWorld().getName().equals("lobby") && !ApiOstrov.isLocalBuilder(e.getPlayer(), false) ) {
            e.setCancelled(true);
            e.getItemDrop().remove();
        }
    }
    
 
    
    
    
    
    
    @EventHandler(ignoreCancelled = true,priority = EventPriority.LOWEST)    
	public void onPlace(BlockPlaceEvent e) {
            //PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.Единое_время();
            if ( !ApiOstrov.isLocalBuilder(e.getPlayer(), false) && e.getPlayer().getWorld().getName().equals("lobby") ) e.setCancelled(true);
        }
    
    @EventHandler(ignoreCancelled = true,priority = EventPriority.LOWEST)    
	public void onBreak(BlockBreakEvent e) {
            if ( !ApiOstrov.isLocalBuilder(e.getPlayer(), false) && e.getPlayer().getWorld().getName().equals("lobby") ) e.setCancelled(true);
        }
 
   
    
    
    
    
    
    
    
    
    public static void spawnRandomFirework(final Location location) {
        final Firework firework = location.getWorld().spawn(location, Firework.class);
        final FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(FireworkEffect.builder().flicker(ApiOstrov.randBoolean()).withColor(Color.fromBGR(ApiOstrov.randInt(0,255), ApiOstrov.randInt(0,255), ApiOstrov.randInt(0,255))).withFade(Color.fromBGR(ApiOstrov.randInt(0,255), ApiOstrov.randInt(0,255), ApiOstrov.randInt(0,255))).with(FireworkEffect.Type.BALL).trail(ApiOstrov.randBoolean()).build());
        fireworkMeta.setPower(0);
        firework.setFireworkMeta(fireworkMeta);
    }   
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void onBlockSpread(BlockSpreadEvent e) { 
        e.setCancelled(true);
    }  
        
    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void onBlockGrowth(BlockGrowEvent e) { 
      e.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void BlockFadeEvent(BlockFadeEvent e) { 
      e.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void BlockFromToEvent(BlockFromToEvent e) { 
      e.setCancelled(true);
    }    
    
    
     @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void BlockSpreadEvent(BlockSpreadEvent e) { e.setCancelled(true);}   
        
    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void FluidLevelChangeEvent(FluidLevelChangeEvent e) { e.setCancelled(true);}   
   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public static class SpectatorMenu implements InventoryProvider {

        public SpectatorMenu() {
        }

        @Override
        public void init(final Player p, final InventoryContent contents) {
            //p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
            //contents.fillRect(0,0, 2,3, ClickableItem.empty(fill1));
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, .5f, 1);




            contents.set( 0, ClickableItem.of(mapSelector, e -> {
                    if (e.isLeftClick()) {
                        //p.closeInventory();
                        if (p.getGameMode()==GameMode.SPECTATOR) {
                            //
                        } else {
                            p.closeInventory();
                        }
                    }
                }));        


            contents.set( 2, ClickableItem.of(music, e -> {
                    if (e.isLeftClick()) {
                        if (p.getGameMode()==GameMode.SPECTATOR) {
                            //Bukkit.getServer().dispatchCommand(p, "music");   
                        } else {
                            p.closeInventory();
                        }
                    }
                }));        


            contents.set( 4, ClickableItem.of(leaveArena, e -> {
                    if (e.isLeftClick()) {
                        p.closeInventory();
                        if (p.getGameMode()==GameMode.SPECTATOR) {
                            //PM.toLobby(p, true);
                        } else {
                            p.closeInventory();
                        }
                    }
                }));        



        }



    }    
    
    
    
    
    
    
    
}
