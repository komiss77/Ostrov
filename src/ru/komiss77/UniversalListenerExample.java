package ru.komiss77;


import java.util.Iterator;
import me.clip.deluxechat.DeluxeChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.enums.Data;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.events.GameInfoUpdateEvent;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
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
    private static Inventory spectatorMenu;

    public UniversalListenerExample(final Plugin plugin) {
        UniversalListenerExample.plugin=plugin;
        mapSelector = new ItemBuilder(Material.CAMPFIRE).setName("§aВыбор Карты").build();
        exit = new ItemBuilder(Material.MAGMA_CREAM).setName("§4Вернуться в лобби").build();
        music = new ItemBuilder(Material.NOTE_BLOCK).setName("§4Музыка").lore("§eЛКМ §7- §aвкл§7/§4выкл").lore("§eПКМ §7- меню").build();
        leaveArena = new ItemBuilder(Material.SLIME_BALL).setName("§4Покинуть Арену").build();
        teleporter_itemstack = new ItemBuilder(Material.COMPASS).name("§6ТП к доступным игрокам").build();
        mapSelectMenu = Bukkit.createInventory(null, 54, "§1Карты");
        spectatorMenu = Bukkit.createInventory(null, 9, "§1Меню зрителя");
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
    public void onDataRecieved(final BungeeDataRecieved e) {  

        //AM.onDataRecieved(e.getPlayer());    //load

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
            
    
    @EventHandler (priority = EventPriority.MONITOR)
    public static void SignUpdateEvent (GameInfoUpdateEvent e) {
//System.out.println(" ---- SignUpdateEvent 1 --- "+e.server+" "+e.arena+" this="+SM.this_server_name+" exist?"+AM._ARENAS.containsKey(e.arena));
        /*if (e.server.equals(SM.this_server_name) && !e.arena.isEmpty() && !e.arena.equals("any") && AM._ARENAS.containsKey(e.arena)) {
            final Arena arena = AM.getArena(e.arena);
            if (ApiOstrov.isInteger(arena.world.replaceFirst("map", ""))) {
                final int slot = Integer.valueOf(arena.world.replaceFirst("map", ""));
                if (mapSelectMenu.getItem(slot)==null) {
                    mapSelectMenu.setItem(slot, new ItemBuilder(Material.RED_TERRACOTTA)
                                            .name("§e"+arena.name)
                                            .lore("мин. игроков: §a"+arena.minPlayers)
                                            .lore("собрать золота: §6"+arena.goldToWin)
                                            .build()
                                        );
                } else {
//System.out.println(" ---- SignUpdateEvent 2 --- "+e.arena+" state="+e.state);
                    if (e.state==UniversalArenaState.ОЖИДАНИЕ || e.state==UniversalArenaState.РАБОТАЕТ) mapSelectMenu.getItem(slot).setType(Material.GREEN_TERRACOTTA);
                    else mapSelectMenu.getItem(slot).setType(Material.RED_TERRACOTTA);
                }
                
            } else {
                Ostrov.log_err("Мир арены "+arena.name+" не имеет формат map+номер, арена не будет отображаться в меню.");
            }
        }*/
        

    }
       
       
       
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(final PlayerQuitEvent e) {
      //  final Player player = e.getPlayer();

    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(final PlayerKickEvent e) {
      //  final Player player = e.getPlayer();

    }

    
        
    
    
    
    
    
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
                player.getActivePotionEffects().stream().forEach((effect) -> {
                    player.removePotionEffect(effect.getType());
                });
                perWorldTabList(player);
                player.setWalkSpeed((float) 0.2);
                player.setDisplayName("§7"+player.getName());
                if (PM.nameTagManager!=null) {
                    PM.nameTagManager.setNametag(player.getName(), "", ": §8Не выбрана");
                }
                //player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
            }
        }.runTaskLater(plugin, 1);
    }       
    
    
    
    
    
    
    @EventHandler
    public void FriendTeleport(FriendTeleportEvent e) {
        if (!e.target.getWorld().getName().equals("lobby")) e.Set_canceled(true, "§f"+e.target.getName()+" §eиграет, не будем мешать!");
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
            if (e.getView().getTitle().equals("§1Меню зрителя")) {
                e.setCancelled(true);
    //System.out.println("Spectator-Menu");
                if (ItemUtils.compareItem(e.getCurrentItem(), teleporter_itemstack,false)) {
                    p.openInventory(getTeleporterInventory(p));
                } else if (ItemUtils.compareItem(e.getCurrentItem(), leaveArena, false) ) {
                    e.setCancelled(true);
                    p.performCommand(leaveCommad);
                } else if (ItemUtils.compareItem(e.getCurrentItem(), mapSelector, false) ) {
                    e.setCancelled(true);
                    openArenaSelectMenu(p);
                }
            } else if (e.getView().getTitle().equals("§6ТП к игроку")) {
                e.setCancelled(true);
//System.out.println("Spectator1");
                final Player target = Bukkit.getPlayer(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));
                if (target == null) {
                    p.sendMessage("§cИгрок не найден");
                    p.closeInventory();
                    return;
                }
                //if (!playerData.arena.players.contains(target.getName()) ) {
                //    p.sendMessage("§cИгрок недоступен для ТП");
                //    p.closeInventory();
                //    return;
                //}
                p.teleport(target.getLocation().add(0.0, 3.0, 0.0));
                return;
            } 
        }

        if (e.isCancelled()) return;
        
        if (ItemUtils.compareItem(e.getCurrentItem(), exit, false) ) {
            e.setCancelled(true);
            if(e.getAction()==InventoryAction.PICKUP_ONE) ApiOstrov.sendToServer(p, "lobby0", "");
        } else if (ItemUtils.compareItem(e.getCurrentItem(), mapSelector, false) ) {
            e.setCancelled(true);
            if(e.getAction()==InventoryAction.PICKUP_ONE) openArenaSelectMenu(p);
        } else if (ItemUtils.compareItem(e.getCurrentItem(), leaveArena, false) ) {
            e.setCancelled(true);
           if(e.getAction()==InventoryAction.PICKUP_ONE)  p.performCommand(leaveCommad);
        } else if (ItemUtils.compareItem(e.getCurrentItem(), music, false) ) {
            e.setCancelled(true);
            if(e.getAction()==InventoryAction.PICKUP_ONE) p.performCommand("music");
        }
        
        
        if (e.getInventory().getType()!=InventoryType.CHEST || e.getCurrentItem()==null ) return;
        if (e.getView().getTitle().equals("§1Карты")) {
            e.setCancelled(true);
            final ItemStack clicked = e.getCurrentItem();
            if ( clicked.getType().toString().contains("TERRACOTTA") && clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName() ) {
                if(e.getAction()==InventoryAction.PICKUP_ONE)  p.performCommand(joinCommad+ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
//System.out.println("click="+joinCommad+ChatColor.stripColor(clicked.getItemMeta().getDisplayName()));
            }
        }
        
        
        
    }
        
    
    private Inventory getTeleporterInventory(final Player p) {
        final Inventory inventory = Bukkit.createInventory(null, 54, "§6ТП к игроку");
        //final Arena arena = AM.getArenaByWorld(p.getWorld().getName());
        //if (arena!=null) {
            //for (final Player player : arena.getPlayers(false)) {
            for (final Player player : p.getWorld().getPlayers()) {
                if (player.getGameMode()==GameMode.SPECTATOR ) {
                    continue;
                }
                inventory.addItem( new ItemBuilder(Material.PLAYER_HEAD).name("§b"+player.getName()).setSkullOwner(player.getName()).build() );//plugin.getSkull(player.getName(), ChatColor.AQUA + player.getName()) );
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
        player.setPlayerListName("§8"+player.getName());
        player.sendMessage("§fРежим зрителя. §aЛевый клик -> открыть меню");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }    
 
        
 
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR) //стираем наметаг, или не даёт отображать скореб.команды!
    public void onTeleportChange (final PlayerTeleportEvent e) {
        if (!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())) {
            if (e.getFrom().getWorld().getName().equals("lobby")) {
                if (PM.nameTagManager!=null) PM.nameTagManager.reset(e.getPlayer().getName());
                PM.getOplayer(e.getPlayer()).score.getSideBar().reset();
            }
        }
    }        
        
        
        
        
        
    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWorldChange (final PlayerChangedWorldEvent e) {
//System.out.println("PlayerChangedWorldEvent from="+e.getFrom().getName());
        //final Player p = e.getPlayer();
        new BukkitRunnable() {
            final Player p = e.getPlayer();
            @Override
            public void run() {
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
        if (p.getWorld().getName().equalsIgnoreCase("lobby")) { //оказались в лобби, делаем глобальный
            if ( DeluxeChat.isLocal(p.getUniqueId().toString()) ){
                if (notify) p.sendMessage("§8Чат переключен на глобальный");
                Ostrov.deluxechatPlugin.setGlobal(p.getUniqueId().toString());
            }
        } else {
            if ( !DeluxeChat.isLocal(p.getUniqueId().toString()) )  {
                if (notify) p.sendMessage("§8Чат переключен на Игровой");
                Ostrov.deluxechatPlugin.setLocal(p.getUniqueId().toString());
            }
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
                Ostrov.sync(() -> p.teleport(Bukkit.getWorld("lobby").getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND), 0);
                return;
            }
            e.setCancelled(true);
            return;
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
        if (e.getEntityType()==EntityType.PLAYER && e.getEntity().getWorld().getName().equals("lobby") && !ApiOstrov.isLocalBuilder((Player) e.getEntity(), false)) {
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
        final Firework firework = (Firework)location.getWorld().spawn(location, (Class)Firework.class);
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
