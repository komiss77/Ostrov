package ru.komiss77.hook;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.events.FigureClickEvent;
import ru.komiss77.modules.games.GM;
import ru.komiss77.utils.LocationUtil;









public class TradeLst implements Listener {
    
    //private final HashMap <String,BukkitTask> trades;
    //private final int minLevel = 25;
    //private final int LevelRange = 250;
    
    private BukkitTask tradeTask;
    private String p1Name = "";
    //private static final List<TradeInventory> tradeInventories = new ArrayList<>();
   /* protected static final List<Integer> activeSlots = Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39);
    
    protected static final ItemStack yes = new ItemBuilder(Material.GREEN_WOOL).name("§aПредложение устраивает.").build();
    protected static final ItemStack no = new ItemBuilder(Material.RED_WOOL).name("§4Неинтересно.").build();
    protected static final ItemStack wait = new ItemBuilder(Material.GRAY_DYE).name("§7Ждём предложение.").build();
    protected static final ItemStack ready = new ItemBuilder(Material.LIME_DYE).name("§2Предложение сделано").build();
    protected static final ItemStack notReady = new ItemBuilder(Material.RED_DYE).name("§4Нет подтверждения").build();
    protected static final ItemStack done = new ItemBuilder(Material.LIGHT_BLUE_DYE).name("§b > Обмен выполнен <").build();*/

    
    
    //NoSelfTrade + оба на одном острове
    //MinLevel
    //разница уровней
    
    @EventHandler(  priority = EventPriority.HIGH, ignoreCancelled = true) 
    public void onTrade(FigureClickEvent e) { //сработает при клике на торговца
//System.out.println(" ++++ FigureClickEvent "+e.getFigureName());
        if (!e.getFigure().getTag().equalsIgnoreCase("меняла")) return;
        
        final Location traderLocation = e.getFigure().getEntity().getLocation();
        final Player p = e.getPlayer();
        
        //NoSelfTrade
        if (p1Name.equals(p.getName())) { 
            p.sendMessage("§eТорговец ждёт второго участника..");
            return;
        }
        
        //проверки для обоих участников
        if (ApiOstrov.moneyGetBalance(p.getName())<1) {
            p.sendMessage("§eО милосердый, торговец несёт налоговое бремя, а у Вас нет хотя бы 1 лони для оплаты сделки..");
            return;
        }

        
        
        
        if (p1Name.isEmpty()) { //первый участник
            
            p.sendMessage("§aТорговец приветствует Вас, кто же второй участник обмена??");
            p1Name = p.getName();
            
            tradeTask = new BukkitRunnable() {
                private int sec = 5;

                @Override
                public void run() {
                    if ( p==null || 
                        !p.isOnline() ||
                        p.isDead() ||
                        !p.getWorld().getName().equals(traderLocation.getWorld().getName()) ||
                        LocationUtil.getDistance(p.getLocation(), traderLocation) > 30
                    ) {
                        reset();
                        return;
                    }


                    sec--;
                    if (sec==0) {
                        ApiOstrov.sendActionBarDirect(p, "§cВторой участник не нашелся, торговец отменил сделку.");
                        reset();
                        return;
                    }
                    ApiOstrov.sendActionBarDirect(p, "§eЖдём второго участника обмена §7: §f"+sec);
                }

                private void reset() {
                    this.cancel();
                    p1Name = "";
                }

            }.runTaskTimer(Ostrov.instance, 1, 20);

            
            
        } else {
            
            
            final Player p1 = Bukkit.getPlayer(p1Name); //первый участник
            
            if (p1==null || LocationUtil.getDistance(p1.getLocation(), traderLocation)>30) {
                p.sendMessage("§eГде же первый участник??..");
                resetTrade();
                return;
            }
            
            ApiOstrov.moneyChange(p1, -1, "Оплата торговцу");
            ApiOstrov.moneyChange(p, -1, "Оплата торговцу");
            
            p.performCommand("trade "+p1Name);
            Ostrov.sync( ()-> p1.performCommand("trade accept "+p.getName()), 5 );
            //p1.performCommand("trade accept "+p.getName());
             //TradeSystem.getInstance().getTradeManager().startTrade(p1, p, p.getName(), p.getUniqueId(), true);
             //TradeSystem.getInstance().getTradeManager().startTrade(p1, p, p.getName(), true);
             //TradeSystem.getInstance().getTradeManager().startTrade(p, p1);
//Ostrov.log("Trade session : "+p1Name+" - "+p.getName());
            

            //final TradeInventory tradeInventory = new TradeInventory(p1, p);
            //tradeInventories.add(tradeInventory);
            resetTrade();
            
            if (GM.GAME == Game.SK) {
                ApiOstrov.addCustomStat(p, "scTrade");
            } else {
                ApiOstrov.addCustomStat(p, "trade", 1);
            }
            
            
        }
        
        
        
        
        
    }





    
    
    private void resetTrade() {
        if (tradeTask!=null) tradeTask.cancel();
        p1Name="";
    }
    
    
    
     /* 
    private static TradeInventory getTradeInventory(final Player p) {
        if (tradeInventories.isEmpty()) return null;
        for (final TradeInventory ti : tradeInventories) {
            if (ti.getSender().equals(p.getName()) || ti.getReceiver().equals(p.getName())) {
                return ti;
            }
        }
        return null;
    }

    
    
    
    
    
    
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void closeInventory(final InventoryCloseEvent e) {
        final Player player = (Player)e.getPlayer();
        final TradeInventory ti = getTradeInventory(player);
        
        if (ti != null) {
            //if (ti.isFinished()) {
               // if (!ti.isClosed(player)) {
                  //  ti.closeInventory(player);
                  //  ti.addSingleClose();
                  //  if (ti.isFullyClosed()) {
                 //       tradeInventories.remove(ti);
                //    }
               // }
            //} else {
                ti.onClose(player);
                tradeInventories.remove(ti);
           // }
        }
    }
    
    
    
    
    
    
    
    
  @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(final InventoryMoveItemEvent e) {
        //final Inventory clickedInventory = e.getDestination();
       // TradeInventory ti = null;
        //if (clickedInventory != null) {
        if (tradeInventories.isEmpty()) return;
        for (final TradeInventory ti : tradeInventories) {
            if (ti.isInventory(e.getDestination())) {
                //ti = inventory;
//System.out.println("InventoryDragEvent setCancelled");
                e.setCancelled(true);
                return;
            }
        }
        //}
        //if (ti != null) {
        //    e.setCancelled(true);
        //}
    }*/
    
    
    
    
    
    
 /*   @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void dragInventory(final InventoryDragEvent e) {
        if (tradeInventories.isEmpty()) return;
        if (e.getWhoClicked().getType()!=EntityType.PLAYER) return;
        final Player p = (Player) e.getWhoClicked();
        final TradeInventory ti = getTradeInventory(p);
        if (ti==null) return;
        //final Inventory clickedInventory = e.getInventory();
//System.out.println("InventoryDragEvent 1");
        //TradeInventory ti = null;
        //if (clickedInventory != null) {
        //for (final TradeInventory inventory : tradeInventories) {
        //    if (inventory.isInventory(clickedInventory)) {
        //        ti = inventory;
        //    }
        //}

        //if (ti != null) {

          //  final Set<Integer> slots = (Set<Integer>)e.getRawSlots();
           // boolean disallowed = false;
         //   if (ti.isFinished()) {
         //       for (final Integer slotId : slots) {
         //           if (youSlots.contains(slotId)) {
          //              disallowed = true;
           //         }
           //     }
           // } else {
            if (Timer.has(p, "trade")) {
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
                return;
            }
            Timer.add(p, "trade", 1);
            boolean setWait = false;
            boolean canceled = false;

            for (final int slot : activeSlots) {
                if (!canceled) {
                    if (e.getRawSlots().contains(slot+5) || e.getRawSlots().contains(48) || e.getRawSlots().contains(50)) {
                        canceled = true;
                    }
                }
                if (!setWait && e.getRawSlots().contains(slot)) {
                    setWait = true;
                }
            }

            if (canceled) {
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
            }

            if (setWait) {
                ti.setWait(p);
            }*/
            //}
          /*  if (disallowed) {
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
System.out.println("InventoryDragEvent DENY 1");
                FM.soundDeny(p);
                return;
            }
            
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            //final int playerMinId = 54;
            //final int playerMaxId = 89;
            boolean playerInventory = false;
            boolean foundNonItem = false;
            for (final int id : slots) {
                if (id >= 54 && id <= 89) {
                    if (foundNonItem) {
                        continue;
                    }
                    playerInventory = true;
                } else {
                    playerInventory = false;
                    foundNonItem = true;
                }
            }
            if (playerInventory) {
                return;
            }
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
System.out.println("InventoryDragEvent DENY 2");
            for (final int slotId2 : slots) {
                if (ti.isFinished()) {
                    if (!otherSlots.contains(slotId2) || clickedInventory.getItem(slotId2) == null) {
                        continue;
                    }
                    if (clickedInventory.getItem(slotId2).getType() == Material.AIR) {
                        continue;
                    }
                    e.setCancelled(false);
                    e.setResult(Event.Result.ALLOW);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            TradeInventory ti = null;
                            for (final TradeInventory inventory : tradeInventories) {
                                if (inventory.isInventory(clickedInventory)) {
                                    ti = inventory;
                                }
                            }
                            if (ti != null) {
                                ti.updateSlots(clickedInventory, ti.getInventory(clickedInventory));
                            }
                        }
                    }.runTaskLater(Main.plugin, 10L);

                } else {

                    if (!youSlots.contains(slotId2)) {
                        continue;
                    }
                    e.setCancelled(false);
                    e.setResult(Event.Result.ALLOW);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            TradeInventory ti = null;
                            for (final TradeInventory inventory : tradeInventories) {
                                if (inventory.isInventory(clickedInventory)) {
                                    ti = inventory;
                                }
                            }
                            if (ti != null) {
                                ti.updateSlots(clickedInventory, ti.getInventory(clickedInventory));
                            }
                        }
                    }.runTaskLater(Main.plugin, 10L);
                }
            }*/
       // }
       // }
        //}
  //  }
    
    
    
    
    
  /*  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void clickInventory(final InventoryClickEvent e) {
        if (tradeInventories.isEmpty()) return;
        if (e.getWhoClicked().getType()!=EntityType.PLAYER) return;
        final Player p = (Player) e.getWhoClicked();
        final TradeInventory ti = getTradeInventory(p);
        if (ti==null) return;
        //final Inventory clickedInventory = e.getInventory();
        
        //if (e.getWhoClicked() instanceof Player) {
            //final Player player = (Player)e.getWhoClicked();
            //final TradeInventory ti = getTradeInventory(player);
            
            if ( e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
                //FM.soundDeny(p);
            }
            if (Timer.has(p, "trade")) {
                e.setCancelled(true);
                e.setResult(Event.Result.DENY);
                p.sendMessage("§eТорговец не успевает следить за руками, пожалуйста, помедленее!");
                return;
            }
            Timer.add(p, "trade", 1);
            
            //TradeInventory ti = null;
            //final Inventory clickedInventory = e.getClickedInventory();
            
            //if (clickedInventory != null) {
//System.out.println("ru.ostrov77.factions.trade.TradeListener.clickInventory()");
                p.playSound(p.getLocation(), Sound.BLOCK_BAMBOO_PLACE, 1, 1);
                final int slotId = e.getSlot();
                final int rawSlot = e.getRawSlot();
//System.out.println("InventoryClickEvent slotId="+slotId+" rawSlot="+rawSlot+" action="+e.getAction());
                
                if (slotId==rawSlot) { //все клики в верхней части
                    if (activeSlots.contains(slotId)) { //клик на своём поле предложений
                        //switch (e.getAction()) {
                            //case 
                        //}
                        ti.setWait(p);
                        ti.syncWiev(p.getName(),slotId);
                        return;
                    }
                    
                    e.setCancelled(true);
                    e.setResult(Event.Result.DENY);
                    
                    if (slotId==45) { //да
                        ti.sendReady(p.getName());
                    } else if (slotId==46) { //нет
                        ti.setDeny(p.getName());
                    }
                }*/
                //for (final TradeInventory inventory2 : tradeInventories) {
                    //if (inventory2.isInventory(clickedInventory)) {
                      /*  if (p.getName().equals(ti.getSender())) {
                            if (ti.getReadySend() && slotId != 46) {
                                e.setCancelled(true);
                                e.setResult(Event.Result.DENY);
 System.out.println("InventoryClickEvent DENY 2");
                               return;
                            }
                            if (ti.getReadyRec() && youSlots.contains(slotId) ) {
                                ti.setItem(46, ti.getReceiver());
                                e.setCancelled(true);
                                e.setResult(Event.Result.DENY);
 System.out.println("InventoryClickEvent DENY 3");
                            }
                        } else  if (p.getName().equals(ti.getReceiver())) {
                        //if (player.getUniqueId().toString().equals(inventory2.getReceiver().getUniqueId().toString())) {
                            if (ti.getReadyRec() && slotId != 46) {
                                e.setCancelled(true);
                                e.setResult(Event.Result.DENY);
 System.out.println("InventoryClickEvent DENY 4");
                                return;
                            }
                            if (youSlots.contains(slotId) && ti.getReadySend()) {
                                ti.setItem(46, ti.getSender());
                                e.setCancelled(true);
                                e.setResult(Event.Result.DENY);
 System.out.println("InventoryClickEvent DENY 5");
                            }
                        }*/
                        //ti = inventory2;
                    //}
                //}
            //}
            //if (ti == null) {
                //final Inventory inventory3 = e.getInventory();
               // if (clickedInventory != null) {
                    //TradeInventory tiValue = null;
                    //for (final TradeInventory tiCopy : tradeInventories) {
                    //    if (tiCopy.isInventory(clickedInventory)) {
                    //        tiValue = tiCopy;
                    //    }
                    //}
                    //if (tiValue != null && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    //    e.setCancelled(true);
                    //    e.setResult(Event.Result.DENY);
                    //    return;
                    //}
                //}
            //}
            //if (ti != null) {
            /*    boolean performed = false;
 System.out.println("rawSlot="+rawSlot+" perform?"+(youSlots.contains(rawSlot)));
                
                if (ti.isFinished()) {
 System.out.println("isFinished");
                    if (otherSlots.contains(rawSlot) && clickedInventory.getItem(rawSlot) != null && clickedInventory.getItem(rawSlot).getType() != Material.AIR) {
                       // if (clickedInventory.getItem(rawSlot) != null) {
                        //    if (clickedInventory.getItem(rawSlot).getType() != Material.AIR) {
                                performed = true;
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        TradeInventory ti = null;
                                        for (final TradeInventory inventory : tradeInventories) {
                                            if (inventory.isInventory(clickedInventory)) {
                                                ti = inventory;
                                            }
                                        }
                                        if (ti != null && clickedInventory.getItem(rawSlot) != null && clickedInventory.getItem(rawSlot).getType() != Material.AIR) {
                                            ti.updateSlots(clickedInventory, ti.getInventory(clickedInventory));
                                        }
                                    }
                                }.runTaskLater(Main.plugin, 10L);
                      //      } else {
                      //          e.setCancelled(true);
                      //          e.setResult(Event.Result.DENY);
                      //      }
                      //  } else {
                       //     e.setCancelled(true);
                       //     e.setResult(Event.Result.DENY);
                       // }
                    } else {
                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);
 System.out.println("InventoryClickEvent DENY 6");
                    }
                    
                } else if (youSlots.contains(rawSlot)) {
 System.out.println("ti.getYouSlots().contains(rawSlot)");
                    performed = true;
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            //TradeInventory ti = null;
                            //for (final TradeInventory inventory : tradeInventories) {
                            //    if (inventory.isInventory(clickedInventory)) {
                           //         ti = inventory;
                           //     }
                           // }
                            if (ti != null) {
                                ti.updateSlots(clickedInventory, ti.getInventory(clickedInventory));
                            }
                        }
                    }.runTaskLater(Main.plugin, 10L);
                }
                
                if (rawSlot == 45 || rawSlot == 46) {
                    ti.setItem(e.getSlot(), p.getName());
                }
                if (!performed) {
                    e.setCancelled(true);
                    e.setResult(Event.Result.DENY);
 System.out.println("InventoryClickEvent DENY 7");
                }*/
            //}
       // }
   // }
  
   

}    
    
/*    private void resetTrade() {
        if (tradeTask!=null) tradeTask.cancel();
        p1Name="";
    }
    
    
    
    
    
    
    
    
    
    
    
    
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void closeInventory(final InventoryCloseEvent e) {
        final Player player = (Player)e.getPlayer();
        TradeInventory tradeInventory = null;
        for (final TradeInventory inventory : this.tradeInventories) {
            if (inventory.getSender().getUniqueId().toString().equals(player.getUniqueId().toString()) || inventory.getReceiver().getUniqueId().toString().equals(player.getUniqueId().toString())) {
                tradeInventory = inventory;
            }
        }
        if (tradeInventory != null) {
            if (tradeInventory.isFinished()) {
                if (!tradeInventory.isClosed(player)) {
                    tradeInventory.closeInventory(player);
                    tradeInventory.addSingleClose();
                    if (tradeInventory.isFullyClosed()) {
                        tradeInventories.remove(tradeInventory);
                    }
                }
            }
            else {
                tradeInventory.closeInventories(player);
                tradeInventories.remove(tradeInventory);
            }
        }
    }
    
    
    
    
    
    
    
    
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(final InventoryMoveItemEvent e) {
        //final Inventory clickedInventory = e.getDestination();
        TradeInventory tradeInventory = null;
        //if (clickedInventory != null) {
            for (final TradeInventory inventory : tradeInventories) {
                if (inventory.isInventory(e.getDestination())) {
                    tradeInventory = inventory;
                }
            }
        //}
        if (tradeInventory != null) {
            e.setCancelled(true);
        }
    }
    
    
    
    
    
    
    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void dragInventory(final InventoryDragEvent e) {
        if (e.getWhoClicked().getType()==EntityType.PLAYER) {
            final Inventory clickedInventory = e.getInventory();
            
            TradeInventory tradeInventory = null;
            //if (clickedInventory != null) {
                for (final TradeInventory inventory : tradeInventories) {
                    if (inventory.isInventory(clickedInventory)) {
                        tradeInventory = inventory;
                    }
                }
                
                if (tradeInventory != null) {
                    final Player p = (Player) e.getWhoClicked();
                    
                    final Set<Integer> slots = (Set<Integer>)e.getRawSlots();
                    boolean disallowed = false;
                    if (tradeInventory.isFinished()) {
                        for (final Integer slotId : slots) {
                            if (tradeInventory.getYouSlots().contains(slotId)) {
                                disallowed = true;
                            }
                        }
                    } else {
                        for (final Integer slotId : slots) {
                            if (tradeInventory.getOtherSlots().contains(slotId)) {
                                disallowed = true;
                            }
                        }
                    }
                    if (disallowed) {
                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1);
                        return;
                    }
                    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
                    //final int playerMinId = 54;
                    //final int playerMaxId = 89;
                    boolean playerInventory = false;
                    boolean foundNonItem = false;
                    for (final int id : slots) {
                        if (id >= 54 && id <= 89) {
                            if (foundNonItem) {
                                continue;
                            }
                            playerInventory = true;
                        } else {
                            playerInventory = false;
                            foundNonItem = true;
                        }
                    }
                    if (playerInventory) {
                        return;
                    }
                    e.setCancelled(true);
                    e.setResult(Event.Result.DENY);
                    for (final int slotId2 : slots) {
                        if (tradeInventory.isFinished()) {
                            if (!tradeInventory.getOtherSlots().contains(slotId2) || clickedInventory.getItem(slotId2) == null) {
                                continue;
                            }
                            if (clickedInventory.getItem(slotId2).getType() == Material.AIR) {
                                continue;
                            }
                            e.setCancelled(false);
                            e.setResult(Event.Result.ALLOW);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    TradeInventory tradeInventory = null;
                                    for (final TradeInventory inventory : tradeInventories) {
                                        if (inventory.isInventory(clickedInventory)) {
                                            tradeInventory = inventory;
                                        }
                                    }
                                    if (tradeInventory != null) {
                                        tradeInventory.updateSlots(clickedInventory, tradeInventory.getInventory(clickedInventory));
                                    }
                                }
                            }.runTaskLater(Ostrov.instance, 10L);
                            
                        } else {
                            
                            if (!tradeInventory.getYouSlots().contains(slotId2)) {
                                continue;
                            }
                            e.setCancelled(false);
                            e.setResult(Event.Result.ALLOW);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    TradeInventory tradeInventory = null;
                                    for (final TradeInventory inventory : tradeInventories) {
                                        if (inventory.isInventory(clickedInventory)) {
                                            tradeInventory = inventory;
                                        }
                                    }
                                    if (tradeInventory != null) {
                                        tradeInventory.updateSlots(clickedInventory, tradeInventory.getInventory(clickedInventory));
                                    }
                                }
                            }.runTaskLater(Ostrov.instance, 10L);
                        }
                    }
                }
           // }
        }
    }
    
    
    
    
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void clickInventory(final InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            final Player player = (Player)e.getWhoClicked();
            
            if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                for (final TradeInventory inventory : tradeInventories) {
                    if (player.getName().equalsIgnoreCase(inventory.getSender().getName()) || player.getName().equalsIgnoreCase(inventory.getReceiver().getName())) {
                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1);
                        return;
                    }
                }
            }
            
            TradeInventory tradeInventory = null;
            final Inventory clickedInventory = e.getClickedInventory();
            if (clickedInventory != null) {
                player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
                final int slotId = e.getSlot();
                for (final TradeInventory inventory2 : tradeInventories) {
                    if (inventory2.isInventory(clickedInventory)) {
                        if (player.getUniqueId().toString().equals(inventory2.getSender().getUniqueId().toString())) {
                            if (inventory2.getReadySend() && slotId != 46) {
                                e.setCancelled(true);
                                e.setResult(Event.Result.DENY);
                                return;
                            }
                            if (inventory2.getYouSlots().contains(slotId) && inventory2.getReadyRec()) {
                                inventory2.setItem(46, inventory2.getReceiver());
                                e.setCancelled(true);
                                e.setResult(Event.Result.DENY);
                            }
                        }
                        if (player.getUniqueId().toString().equals(inventory2.getReceiver().getUniqueId().toString())) {
                            if (inventory2.getReadyRec() && slotId != 46) {
                                e.setCancelled(true);
                                e.setResult(Event.Result.DENY);
                                return;
                            }
                            if (inventory2.getYouSlots().contains(slotId) && inventory2.getReadySend()) {
                                inventory2.setItem(46, inventory2.getSender());
                                e.setCancelled(true);
                                e.setResult(Event.Result.DENY);
                            }
                        }
                        tradeInventory = inventory2;
                    }
                }
            }
            if (tradeInventory == null) {
                final Inventory inventory3 = e.getInventory();
                if (inventory3 != null) {
                    TradeInventory tradeInventoryValue = null;
                    for (final TradeInventory tradeInventoryCopy : tradeInventories) {
                        if (tradeInventoryCopy.isInventory(inventory3)) {
                            tradeInventoryValue = tradeInventoryCopy;
                        }
                    }
                    if (tradeInventoryValue != null && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);
                        return;
                    }
                }
            }
            if (tradeInventory != null) {
                final int slotId = e.getRawSlot();
                boolean performed = false;
                if (tradeInventory.isFinished()) {
                    if (tradeInventory.getOtherSlots().contains(slotId)) {
                        if (clickedInventory.getItem(slotId) != null) {
                            if (clickedInventory.getItem(slotId).getType() != Material.AIR) {
                                performed = true;
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        TradeInventory tradeInventory = null;
                                        for (final TradeInventory inventory : tradeInventories) {
                                            if (inventory.isInventory(clickedInventory)) {
                                                tradeInventory = inventory;
                                            }
                                        }
                                        if (tradeInventory != null && clickedInventory.getItem(slotId) != null && clickedInventory.getItem(slotId).getType() != Material.AIR) {
                                            tradeInventory.updateSlots(clickedInventory, tradeInventory.getInventory(clickedInventory));
                                        }
                                    }
                                }.runTaskLater(Ostrov.instance, 10L);
                            }
                            else {
                                e.setCancelled(true);
                                e.setResult(Event.Result.DENY);
                            }
                        }
                        else {
                            e.setCancelled(true);
                            e.setResult(Event.Result.DENY);
                        }
                    }
                    else {
                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);
                    }
                }
                else if (tradeInventory.getYouSlots().contains(slotId)) {
                    performed = true;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            TradeInventory tradeInventory = null;
                            for (final TradeInventory inventory : tradeInventories) {
                                if (inventory.isInventory(clickedInventory)) {
                                    tradeInventory = inventory;
                                }
                            }
                            if (tradeInventory != null) {
                                tradeInventory.updateSlots(clickedInventory, tradeInventory.getInventory(clickedInventory));
                            }
                        }
                    }.runTaskLater(Ostrov.instance, 10L);
                }
                if (slotId == 45 || slotId == 46) {
                    tradeInventory.setItem(e.getSlot(), player);
                }
                if (!performed) {
                    e.setCancelled(true);
                    e.setResult(Event.Result.DENY);
                }
            }
        }
    }
  
   



*/



    
    

