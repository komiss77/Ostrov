package ru.komiss77.modules.menuItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.Cfg;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.Initiable;
import ru.komiss77.Timer;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.player.profile.PassportHandler__;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.version.VM;




public final class MenuItemsManager extends Initiable implements Listener {

    private final Map<String,MenuItem>items;
    public boolean item_lobby_mode;
    
    public MenuItemsManager() {
        items = new HashMap<>();
        reload();
    }
    
    @Override
    public void reload() {
        HandlerList.unregisterAll(this);
        items.clear();
        item_lobby_mode = Cfg.GetCongig().getBoolean("player.item_lobby_mode");
        
        if (Cfg.GetCongig().getBoolean("player.give_pipboy")) {
            Material mat = Material.matchMaterial(Cfg.GetCongig().getString("system.pipboy_material"));
            if (mat==null) mat = Material.CLOCK;
            final MenuItem pipboy = new MenuItem("pipboy", new ItemBuilder(mat).setName(Cfg.GetCongig().getString("system.pipboy_name")).unsaveEnchantment(Enchantment.LUCK, 1).build());
            pipboy.slot=Cfg.GetCongig().getInt("player.give_pipboy_slot");
            pipboy.give_on_join=true;
            pipboy.give_on_world_change=true;
            pipboy.can_move=!item_lobby_mode;
            pipboy.can_drop=!item_lobby_mode;
            pipboy.give_on_respavn=true;
            pipboy.anycase=true;
            pipboy.duplicate=false;
            pipboy.on_left_click = p -> p.performCommand(Cfg.GetCongig().getString("system.pipboy_left_click_command"));
            pipboy.on_right_click = p -> p.performCommand(Cfg.GetCongig().getString("system.pipboy_rigth_click_command"));
            addItem(pipboy);
        }
        
        if (Cfg.GetCongig().getBoolean("player.give_bow_teleport")) {
            
            ItemStack is=new ItemBuilder(Material.BOW)
                    .setName("§eЛук-Телепортер")
                    .setUnbreakable(true)
                    .enchantment(Enchantment.ARROW_INFINITE)
                    .addFlags(ItemFlag.HIDE_UNBREAKABLE)
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .addFlags(ItemFlag.HIDE_ENCHANTS)
                    .build(); //не менять на unsaveEnchantment!!!! стрелы кончаются!!
            //is=VM.getNmsNbtUtil().addString(is, "Unbreakable", "true");
            MenuItem tpbow = new MenuItem("tpbow", is);
            tpbow.slot=2;
            tpbow.give_on_join=true;
            tpbow.give_on_respavn=true;
            tpbow.anycase=true;
            addItem(tpbow);
            
            final MenuItem tparrow = new MenuItem("tparrow", 
                    new ItemBuilder(Material.ARROW)
                        .setName("§7Стрела-телепортер")
                        .unsaveEnchantment(Enchantment.LUCK, 1)
                        .build()
            );
            tparrow.slot=9;
            tparrow.give_on_join=true;
            tparrow.give_on_respavn=true;
            tparrow.anycase=true;
            addItem(tparrow);
        }
        
        Bukkit.getPluginManager().registerEvents(MenuItemsManager.this, Ostrov.instance);
        
    }
    
    
    
    public boolean hasItem(final String name){
        return items.containsKey(name);
    }
    
    
    public void addItem(final MenuItem si) {
        if (si==null || si.name==null || si.name.isEmpty()) return;
        items.put(si.name, si);
    }

    public boolean giveItem(final Player p, final String item_name) {
        return items.containsKey(item_name) && items.get(item_name).give(p);
    }
    
    public MenuItem fromItemStack(final ItemStack is) {
        if (is!=null && is.getType()!=Material.AIR && VM.getNmsNbtUtil().hasString(is, "ostrovItem")) return items.get(VM.getNmsNbtUtil().getString(is, "ostrovItem"));
        //for (SpecItem si:items.values()) {
//System.out.println("--fromItemStack si="+si+" is dcec?"+si.isSpecItem(is));
        //    if (si.isSpecItem(is)) return si;
        //}
        return null;
    }
    
    public boolean isSpecItem(final ItemStack is) {
        return is!=null && is.getType()!=Material.AIR && VM.getNmsNbtUtil().hasString(is, "ostrovItem") && items.containsKey(VM.getNmsNbtUtil().getString(is, "ostrovItem"));
//System.out.println("--fromItemStack si="+si+" is dcec?"+si.isSpecItem(is));
    }
    
    
    
    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void InventoryClick (InventoryClickEvent e) {
        if (e.getCurrentItem()==null || e.getSlotType()==InventoryType.SlotType.OUTSIDE) return;
        MenuItem si = fromItemStack(e.getCurrentItem());
            if (si!=null && !si.can_move) {
                e.setCancelled(true);
            }
    }
    
 /*   
@EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void cancelMove(InventoryDragEvent e) {
        for (ItemStack item : event.getNewItems().values()) {
            if (item != null   && item.hasItemMeta() && item.getItemMeta().hasDisplayName() ){
                    if ( item.getItemMeta().getDisplayName().equals(pipboy_name) ||  item.getItemMeta().getDisplayName().equals(cosmetic_name)
                            || item.getItemMeta().getDisplayName().equals(friend_name) ) {
                
                        event.setCancelled(true);
                        ((Player) event.getWhoClicked()).updateInventory();
                        return;
                    }
            }
        }
    }
  */      
    
    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        final MenuItem si = fromItemStack(e.getItemDrop().getItemStack());
            if (si!=null && !si.can_drop) {
                e.setCancelled(true);
            }
    } 
    


   /* @EventHandler( priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void leftClickAir(PlayerAnimationEvent e) {
System.out.println("ru.komiss77.Listener.MenuListener.PlayerAnimationEvent() type"+e.getAnimationType()+" canceled?"+e.isCancelled());        
        if (e.getAnimationType() == PlayerAnimationType.ARM_SWING ) {
            if (Timer.CD_has(e.getPlayer().getName(), "menu")) return;
            final SpecItem si = fromItemStack(e.getPlayer().getInventory().getItemInMainHand());
                if (si!=null && si.on_left_click!=null) {
                    si.on_left_click.accept(e.getPlayer());
                    Timer.CD_add(e.getPlayer().getName(), "menu", 1);
                    e.setCancelled(true);
                }
        }
    }*/
 
    
    @EventHandler( priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        
//System.out.println("Lobbyitems PlayerInteractEvent="+e.getAction()+" useItemInHand="+e.useItemInHand());
        if ( e.getItem()==null || e.getAction()==Action.PHYSICAL ) return;
        
        //if (Timer.CD_has(e.getPlayer().getName(), "menu")) return;
        
        final MenuItem si = fromItemStack(e.getItem());
//System.out.println("Lobbyitems PlayerInteractEvent="+e.getAction()+" useItemInHand="+e.useItemInHand()+" si="+si);
        if (si==null) return;
        
        final Player p = e.getPlayer();
//System.out.println("Lobbyitems p.getOpenInventory()="+p.getOpenInventory());
        if (p.getOpenInventory().getType()!=InventoryType.CRAFTING && p.getOpenInventory().getType()!=InventoryType.CREATIVE) return; 
        /*
        фикс - на 1.16 воспринимает клик в меню как интеракт!!!!!
        CRAFTING, когда нет открытого инвентаря или инвентарь игроков открыт, и возвращает CREATIVE, если они находятся в творческом режиме
        */
        e.setUseInteractedBlock(Event.Result.DENY);
        
        if (si.on_right_click!=null || si.on_right_sneak_click!=null) {
            e.setUseItemInHand(Event.Result.DENY); //отменять ПКМ только для командных. или не работает лук-телепортер
        }

        if (Timer.has(p.getEntityId())) return;
        
        if (e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction()==Action.RIGHT_CLICK_BLOCK) {
            if (p.isSneaking()&& si.on_right_sneak_click!=null) {
                si.on_right_sneak_click.accept(e.getPlayer());
                Timer.add(p.getEntityId(), 1);
            } else if (si.on_right_click!=null) {
                Timer.add(p.getEntityId(), 1);
                si.on_right_click.accept(e.getPlayer());
            }
        } else if ( e.getAction()==Action.LEFT_CLICK_AIR || e.getAction()==Action.LEFT_CLICK_BLOCK) {
            if (p.isSneaking()&& si.on_left_sneak_click!=null) {
                si.on_left_sneak_click.accept(e.getPlayer());
                Timer.add(p.getEntityId(), 1);
            } else if (si.on_left_click!=null) {
                Timer.add(p.getEntityId(), 1);
                si.on_left_click.accept(e.getPlayer());
            }
        }
    }
    
    
    
    
    
    




    
    
    
    
    
    @EventHandler( priority = EventPriority.MONITOR )
    public void onBungeeDataRecieved (final BungeeDataRecieved e) {
//System.out.println("ru.komiss77.modules.LobbyItems.onBungeeDataRecieved()");
        final Set<String> has = new HashSet<>();  //создадим список, что итак есть в инвентаре
        for (final ItemStack is : e.getPlayer().getInventory().getContents()) {
            if (isSpecItem(is)) {
                has.add(VM.getNmsNbtUtil().getString(is, "ostrovItem"));
            }
        }
//System.out.println("BungeeDataRecieved has="+has.toString());
        items.values().stream().filter( (si) -> (si.give_on_join && !has.contains(si.name)) ).forEachOrdered( (si) -> {
            ItemUtils.Add_to_inv(e.getPlayer(), si.getItem(), si.slot);//give(e.getPlayer() );
        });
        //items.values().stream().filter((si) -> (si.give_on_join)).forEachOrdered((si) -> {
        //    give(e.getPlayer());
        //});
        if (item_lobby_mode) PassportHandler__.givePassport(e.getPlayer(), 3);
    }   
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        if (Ostrov.isCitizen(e.getPlayer())) return;
        final Set<String> has = new HashSet<>();  //создадим список, что итак есть в инвентаре
        for (final ItemStack is : e.getPlayer().getInventory().getContents()) {
//System.out.println("Respawn is=="+is+" spec?"+isSpecItem(is));
            if (isSpecItem(is)) {
                has.add(VM.getNmsNbtUtil().getString(is, "ostrovItem"));
            }
        }
//System.out.println("Respawn has="+has.toString());
        items.values().stream().filter( (si) -> (si.give_on_respavn && !has.contains(si.name)) ).forEachOrdered( (si) -> {
            ItemUtils.Add_to_inv(e.getPlayer(), si.getItem(), si.slot);//give(e.getPlayer() );
        });
    }   
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCompleteWorldChange(PlayerChangedWorldEvent  e) {
        final Set<String> has = new HashSet<>();  //создадим список, что итак есть в инвентаре
        for (final ItemStack is : e.getPlayer().getInventory().getContents()) {
            if (isSpecItem(is)) {
                has.add(VM.getNmsNbtUtil().getString(is, "ostrovItem"));
            }
        }
//System.out.println("ChangedWorld has="+has.toString());
        items.values().stream().filter( (si) -> (si.give_on_world_change && !has.contains(si.name)) ).forEachOrdered( (si) -> {
            ItemUtils.Add_to_inv(e.getPlayer(), si.getItem(), si.slot);//give(e.getPlayer() );
        });
        //items.values().stream().filter((si) -> (si.give_on_world_change)).forEachOrdered((si) -> {
        //    give(e.getPlayer());
       // });
    }      
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPickUp(EntityPickupItemEvent e) {
        if (e.getEntityType()!=EntityType.PLAYER) return;
        if (item_lobby_mode) {
            e.setCancelled(true);
            e.getItem().remove();
        } else {
            final MenuItem si = fromItemStack(e.getItem().getItemStack());
            if (si!=null && !si.can_pickup) {
                e.setCancelled(true);
                e.getItem().remove();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSwitch(PlayerSwapHandItemsEvent e) {
        MenuItem si = fromItemStack(e.getMainHandItem());
            if (si!=null && !si.can_swap_hand) {
                e.setCancelled(true);
            }
        si = fromItemStack(e.getOffHandItem());
            if (si!=null && !si.can_swap_hand) {
                e.setCancelled(true);
            }
    } 

    
    
    
 
    
    
    
}
