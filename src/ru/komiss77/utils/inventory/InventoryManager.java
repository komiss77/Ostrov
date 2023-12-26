package ru.komiss77.utils.inventory;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Ostrov;

//https://github.com/MinusKube/SmartInvs

public class InventoryManager {

    private static final Map<String, SmartInventory> inventories;
    private static final Map<String, InventoryContent> contents;
    private static final Map<String, BukkitRunnable> updateTasks;
    private static final List<InventoryOpener> defaultOpeners;
    private static final List<InventoryOpener> openers;
    private static final InvListener invListener;

    static {
        inventories = new HashMap<>();
        contents = new HashMap<>();
        updateTasks = new HashMap<>();
        defaultOpeners = Arrays.asList(
                new ChestInventoryOpener(),
                new SpecialInventoryOpener()
        );
        openers = new ArrayList<>();
        
        invListener = new InvListener();
        Bukkit.getPluginManager().registerEvents(invListener, Ostrov.instance);
        
    }

   // public static InventoryManager get() {
  //      return Ostrov.invManager;
 //   }   
    
  //  public InventoryManager() {
  //      Bukkit.getPluginManager().registerEvents(new InvListener(), Ostrov.instance);
  //  }

    //public void init() {
   //     Bukkit.getPluginManager().registerEvents(new InvListener(), Ostrov.instance);
//      new InvTask().runTaskTimer(plugin, 1, 1);
   // }

    public static Optional<InventoryOpener> findOpener(InventoryType type) {
        Optional<InventoryOpener> opInv = InventoryManager.openers.stream()
                .filter(opener -> opener.supports(type))
                .findAny();

        if(!opInv.isPresent()) {
            opInv = InventoryManager.defaultOpeners.stream()
                    .filter(opener -> opener.supports(type))
                    .findAny();
        }

        return opInv;
    }

    public static void registerOpeners(InventoryOpener... openers) {
        InventoryManager.openers.addAll(Arrays.asList(openers));
    }

    public static List<Player> getOpenedPlayers(SmartInventory inv) {
        List<Player> list = new ArrayList<>();

        inventories.forEach( (name, playerInv) -> {
            if(inv.equals(playerInv))
                list.add(Bukkit.getPlayerExact(name));
        });

        return list;
    }

    public static Optional<SmartInventory> getInventory(Player p) {
        return Optional.ofNullable(inventories.get(p.getName()));
    }

    protected static void setInventory(Player p, SmartInventory inv) {
        if(inv == null)
            inventories.remove(p.getName());
        else
            inventories.put(p.getName(), inv);
    }

    public static Optional<InventoryContent> getContents(Player p) {
        return Optional.ofNullable(contents.get(p.getName()));
    }

    protected static void setContents(Player p, InventoryContent contents) {
        if(contents == null)
            InventoryManager.contents.remove(p.getName());
        else
            InventoryManager.contents.put(p.getName(), contents);
    }
    
    protected static void scheduleUpdateTask(Player p, SmartInventory inv) {
    	PlayerInvTask task = new PlayerInvTask(p, inv.getProvider(), contents.get(p.getName()));
    	task.runTaskTimer(Ostrov.instance, 1, inv.getUpdateFrequency());
    	updateTasks.put(p.getName(), task);
    }
    
    protected static void cancelUpdateTask(Player p) {
    	if(updateTasks.containsKey(p.getName())) {
          int bukkitTaskId = updateTasks.get(p.getName()).getTaskId();
          Bukkit.getScheduler().cancelTask(bukkitTaskId);
          updateTasks.remove(p.getName());
    	}
    }

    
    
    
    
    
    
    
    
    
    
    
    @SuppressWarnings("unchecked")
    static class InvListener implements Listener {

        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryClick(InventoryClickEvent e) {
            Player p = (Player) e.getWhoClicked();
            final SmartInventory inv = inventories.get(p.getName());
//Ostrov.log("InventoryClickEvent inv="+inv);
            if(inv == null) return;
//Ostrov.log("CLICK="+e.getClick()+" ACTION="+e.getAction());

            if( e.getAction() == InventoryAction.COLLECT_TO_CURSOR ||
                //e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                e.getAction() == InventoryAction.NOTHING) {
                e.setCancelled(true);
                return;
            } else if( e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                e.setCancelled(true);
                //if (e.getClickedInventory()==p.getOpenInventory().getBottomInventory()) return;
                if(e.getSlot()!=e.getRawSlot()) return; //клики в нижнем окне не интересуют
            }
        /*
            в старой версии было так:
            if (e.getAction() == InventoryAction.NOTHING || e.getClickedInventory() == null) {
                e.setCancelled(true);
                return;
            }
            if (e.getClickedInventory().equals(player.getOpenInventory().getBottomInventory()) && (e.getAction() == InventoryAction.COLLECT_TO_CURSOR || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                e.setCancelled(true);
                return;
            }
        */
            if(e.getClickedInventory() == p.getOpenInventory().getTopInventory()) {
                int row = e.getSlot() / 9;
                int column = e.getSlot() % 9;
                
                if(!inv.checkBounds(row, column)) return;

                InventoryContent invContents = contents.get(p.getName());
                SlotPos slot = SlotPos.of(row, column);
                
                if(!invContents.isEditable(slot)) {
                    e.setCancelled(true);
                }

                inv.getListeners().stream()
                        .filter(listener -> listener.getType() == InventoryClickEvent.class)
                        .forEach(listener -> ((InventoryListener<InventoryClickEvent>) listener).accept(e));

                invContents.get(slot).ifPresent(item -> item.run(new ItemClickData(p, e, e.getClick(), e.getCurrentItem(), slot)));

                // Don't update if the clicked slot is editable - prevent item glitching
                if(!invContents.isEditable(slot)) {
                    p.updateInventory();
                }
            }
        }
        /*
        final Player player = (Player)e.getWhoClicked();
        if (!manager.getInventories().containsKey(player.getName())) {
            return;
        }
        if (e.getAction() == InventoryAction.NOTHING || e.getClickedInventory() == null) {
            e.setCancelled(true);
            return;
        }
        if (e.getClickedInventory().equals(player.getOpenInventory().getBottomInventory()) && (e.getAction() == InventoryAction.COLLECT_TO_CURSOR || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            e.setCancelled(true);
            return;
        }
        if (e.getClickedInventory() == player.getOpenInventory().getTopInventory()) {
            e.setCancelled(true);
            final int row = e.getSlot() / 9;
            final int column = e.getSlot() % 9;
            if (row < 0 || column < 0) {
                return;
            }
            final SmartInventory smartInventory = manager.getInventories().get(player.getName());
            if (row >= smartInventory.getRows() || column >= smartInventory.getColumns()) {
                return;
            }
            manager.getContents().get(player.getName()).get(row, column).ifPresent(clickableItem -> clickableItem.run(e));
            player.updateInventory();
        }
        */
        
        
        
        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryDrag(InventoryDragEvent e) {
            Player p = (Player) e.getWhoClicked();
            //if(!inventories.containsKey(p.getName())) return;
            final SmartInventory inv = inventories.get(p.getName());
            if(inv == null) return;
            
            InventoryContent content = contents.get(p.getName());

            for(int slot : e.getRawSlots()) {
                SlotPos pos = SlotPos.of(slot/9, slot%9);
                if(slot >= p.getOpenInventory().getTopInventory().getSize() || content.isEditable(pos))
                    continue;

                e.setCancelled(true);
                break;
            }

            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryDragEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryDragEvent>) listener).accept(e));
        }
        /*
        final Player player = (Player)e.getWhoClicked();
        if (!manager.getInventories().containsKey(player.getName())) {
            return;
        }
        final Iterator<Integer> iterator = e.getRawSlots().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() >= player.getOpenInventory().getTopInventory().getSize()) {
                continue;
            }
            e.setCancelled(true);
            break;
        }
        */
        
        
        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryOpen(InventoryOpenEvent e) {
            Player p = (Player) e.getPlayer();
            if(!inventories.containsKey(p.getName())) return;

            SmartInventory inv = inventories.get(p.getName());

            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryOpenEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryOpenEvent>) listener).accept(e));
        }

        
        
        @EventHandler(priority = EventPriority.LOW)
        public void onInventoryClose(InventoryCloseEvent e) {
            Player p = (Player) e.getPlayer();

            if(!inventories.containsKey(p.getName())) return;

            final SmartInventory inv = inventories.get(p.getName());

            try{
                inv.getListeners().stream()
                        .filter(listener -> listener.getType() == InventoryCloseEvent.class)
                        .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener).accept(e));
            } finally {
                if(inv.isCloseable()) {
                    e.getInventory().clear();
                    cancelUpdateTask(p);
                    //fix - old
                    inv.getProvider().onClose(p, contents.get(p.getName()));
                    //
                    inventories.remove(p.getName());
                    contents.remove(p.getName());
                } else {
                    Bukkit.getScheduler().runTask(Ostrov.instance, () -> p.openInventory(e.getInventory()));
                }
            }
        }
        /*
        final Player player = (Player)e.getPlayer();
        if (!manager.getInventories().containsKey(player.getName())) {
            return;
        }
        manager.getInventories().get(player.getName()).getProvider().onClose(player, manager.getContents().get(player.getName()));
        manager.getInventories().remove(player.getName());
        manager.getContents().remove(player.getName());
        */
        
        
        
        
        
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent e) {
            Player p = e.getPlayer();

            if(!inventories.containsKey(p.getName())) return;

            SmartInventory inv = inventories.get(p.getName());

            try{
                inv.getListeners().stream()
                        .filter(listener -> listener.getType() == PlayerQuitEvent.class)
                        .forEach(listener -> ((InventoryListener<PlayerQuitEvent>) listener).accept(e));
            } finally {
                inventories.remove(p.getName());
                contents.remove(p.getName());
            }
        }
        /*
        final Player player = e.getPlayer();
        if (!manager.getInventories().containsKey(player.getName())) {
            return;
        }
        manager.getInventories().get(player.getName()).getProvider().onClose(player, manager.getContents().get(player.getName()));
        manager.getInventories().remove(player.getName());
        manager.getContents().remove(player.getName());
        */
        
        
        
        
        @EventHandler(priority = EventPriority.LOW)
        public void onPluginDisable(PluginDisableEvent e) {
            new HashMap<>(inventories).forEach( (name, inv) -> {
                try{
                    inv.getListeners().stream()
                            .filter(listener -> listener.getType() == PluginDisableEvent.class)
                            .forEach(listener -> ((InventoryListener<PluginDisableEvent>) listener).accept(e));
                } finally {
                    inv.close(Bukkit.getPlayerExact(name));
                }
            });

            inventories.clear();
            contents.clear();
        }
        /*
        public void onPluginDisable(final PluginDisableEvent e) {
        if (!e.getPlugin().getName().equals(host.getName())) {
            return;
        }
        manager.getInventories().clear();
        manager.getContents().clear();
        */

    }

    
    
    //class InvTask extends BukkitRunnable {
     //   @Override
     //   public void run() {
    //        new HashMap<>(inventories).forEach((player, inv) -> inv.getProvider().update(player, contents.get(player)));
    //    }
   // }
    


}










/*
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.komiss77.Ostrov;

public final class InventoryAPI {
    
    private static InventoryAPI api;
    private final InventoryManager manager;
    
    
    public InventoryAPI() {
        api = this;
        manager = new InventoryManager();
        Bukkit.getPluginManager().registerEvents(new InventoryAPIListener(manager, getHost()), getHost());
        
    }
    
    public static InventoryAPI get() {
        return InventoryAPI.api;
    }
    
    public InventoryManager getManager() {
        return this.manager;
    }
    
    public JavaPlugin getHost() {
        return Ostrov.instance;
    }
}
*/

















/*
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import ru.komiss77.objects.CaseInsensitiveMap;

public class InventoryManager {
    
    private static InventoryManager instance;
    private final CaseInsensitiveMap<SmartInventory> inventories;
    private final CaseInsensitiveMap<InventoryContent> contents;
    private final ChestInventoryOpener chestOpener;
    private final SpecialInventoryOpener otherOpener;
    
    protected InventoryManager() {
        instance = this;
        this.inventories = new CaseInsensitiveMap();
        this.contents = new CaseInsensitiveMap();
        this.chestOpener = new ChestInventoryOpener();
        this.otherOpener = new SpecialInventoryOpener();
    }
    
    public static InventoryManager get() {
        Preconditions.checkNotNull(InventoryManager.instance, "Unable to retrieve InventoryManager instance - Variable not initialized");
        return InventoryManager.instance;
    }
    
    public Optional<InventoryOpener> findOpener(final InventoryType type) {
        if (type == InventoryType.CHEST && chestOpener.supports(type)) {
            return Optional.of(chestOpener);
        }
        if (otherOpener.supports(type)) {
            return Optional.of(otherOpener);
        }
        return Optional.empty();
    }
    
    public List<Player> getOpenedPlayers(final SmartInventory inv) {
        final List<Player> list = new ArrayList<>();
        inventories.forEach( (name, obj) -> {
            if (inv.equals(obj) && Bukkit.getPlayerExact(name)!=null) {
                list.add(Bukkit.getPlayer(name));
            }
            //return;
        });
        return list;
    }
    
    public Optional<SmartInventory> getInventory(final Player p) {
        return Optional.ofNullable(inventories.get(p.getName()));
    }
    
    protected void setInventory(final Player p, final SmartInventory inv) {
        if (inv == null) {
            inventories.remove(p.getName());
        }
        else {
            inventories.put(p.getName(), inv);
        }
    }
    
    public Optional<InventoryContent> getContents(final Player p) {
        return Optional.ofNullable(contents.get(p.getName()));
    }
    
    protected void setContents(final Player p, final InventoryContent contents) {
        if (contents == null) {
            this.contents.remove(p.getName());
        }
        else {
            this.contents.put(p.getName(), contents);
        }
    }
    
    public Map<String, SmartInventory> getInventories() {
        return inventories;
    }
    
    public Map<String, InventoryContent> getContents() {
        return contents;
    }
    
    
    
    
    public boolean hasContent(final Player p) {
        return contents.containsKey(p.getName());
    }
}
*/