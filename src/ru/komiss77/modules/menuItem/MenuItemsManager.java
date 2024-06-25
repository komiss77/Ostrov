package ru.komiss77.modules.menuItem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;

import java.util.*;


public final class MenuItemsManager implements Initiable, Listener {

    private static final Map<String, MenuItem> itemByNames;
    private static final Map<Integer, MenuItem> itemById;
    private static final EnumSet<Material> possibleMat; //для быстрой первичной фильтрации
    public static boolean item_lobby_mode;

    static {
        itemByNames = new HashMap<>();
        itemById = new HashMap<>();
        possibleMat = EnumSet.noneOf(Material.class);
    }

    public MenuItemsManager() {
        reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }


    @Override
    public void onDisable() {
    }

    @Override
    public void reload() {
        HandlerList.unregisterAll(this);
        //items.clear(); удаляет предметы, созданные плагином!
        item_lobby_mode = Config.getConfig().getBoolean("player.item_lobby_mode");

        if (Config.getConfig().getBoolean("player.give_pipboy")) {
            Material mat = Material.matchMaterial(Config.getConfig().getString("system.pipboy_material"));
            if (mat == null) mat = Material.CLOCK;

            final ItemStack is = new ItemBuilder(mat)
                    .name(Config.getConfig().getString("system.pipboy_name"))
//                    .addEnchant(Enchantment.LUCK, 1)
                    .build();

            final MenuItem pipboy = new MenuItem("pipboy", is);
            pipboy.slot = Config.getConfig().getInt("player.give_pipboy_slot");
            pipboy.give_on_join = true;
            pipboy.give_on_world_change = true;
            pipboy.can_move = !item_lobby_mode;
            pipboy.can_drop = !item_lobby_mode;
            pipboy.give_on_respavn = true;
            pipboy.anycase = true;
            pipboy.duplicate = false;
            pipboy.on_left_click = p -> {
                final String cmd = Config.getConfig().getString("system.pipboy_left_click_command");
                if (!new PlayerCommandPreprocessEvent(p, cmd).callEvent()) return;
                p.performCommand(cmd);
            };
            pipboy.on_right_click = p -> {
                final String cmd = Config.getConfig().getString("system.pipboy_rigth_click_command");
                if (!new PlayerCommandPreprocessEvent(p, cmd).callEvent()) return;
                p.performCommand(cmd);
            };
            addItem(pipboy);
        }

        if (Config.getConfig().getBoolean("player.give_bow_teleport")) {

            final ItemStack is = new ItemBuilder(Material.BOW)
                    .name("§eЛук-Телепортер")
                    .setUnbreakable(true)
                    .addEnchant(Enchantment.ARROW_INFINITE)
                    .addFlags(ItemFlag.HIDE_UNBREAKABLE)
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .addFlags(ItemFlag.HIDE_ENCHANTS)
                    .build(); //не менять на unsaveEnchantment!!!! стрелы кончаются!!
            final MenuItem tpbow = new MenuItem("tpbow", is);
            tpbow.slot = 2;
            tpbow.give_on_join = true;
            tpbow.give_on_respavn = true;
            tpbow.anycase = true;
            addItem(tpbow);

            final MenuItem tparrow = new MenuItem("tparrow",
                    new ItemBuilder(Material.ARROW)
                            .name("§7Стрела-телепортер")
//                    .addEnchant(Enchantment.LUCK, 1)
                            .build()
            );
            tparrow.slot = 9;
            tparrow.give_on_join = true;
            tparrow.give_on_respavn = true;
            tparrow.anycase = true;
            addItem(tparrow);
            //}

        }

        Bukkit.getPluginManager().registerEvents(MenuItemsManager.this, Ostrov.instance);

    }
    
    


    
    
    
    
    
    
   /* @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLaunch(final ProjectileLaunchEvent e) { //PlayerElytraBoostEvent !!!
        final Projectile prj = e.getEntity();
        
        if (prj.getShooter() instanceof Player && prj.getType() == EntityType.FIREWORK) {
            Ostrov.sync(()-> ((HumanEntity) prj.getShooter()).getInventory().setItem(2, Main.fw), 8);
//            prj.remove();
        }
    } */


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent e) {
//System.out.println("onInventoryMove "+e.getItem());
        if (possibleMat.contains(e.getItem().getType())) {
            final MenuItem si = fromItemStack(e.getItem());
            if (si != null && !si.can_move) {
                e.setCancelled(true);
            }
        }
    }

    //@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    // public void onInventoryInteract (InventoryInteractEvent e) {
//System.out.println("onInventoryInteract ");
    // }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
//System.out.println("onInventoryDrag getType="+e.getType());
        if (e.getCursor() != null && possibleMat.contains(e.getCursor().getType())) {
            final MenuItem si = fromItemStack(e.getCursor());
            if (si != null && !si.can_move) {
                e.setCancelled(true);
            }
        }
        if (possibleMat.contains(e.getOldCursor().getType())) {
            final MenuItem si = fromItemStack(e.getOldCursor());
            if (si != null && !si.can_move) {
                e.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void InventoryClick(InventoryClickEvent e) {
//System.out.println("Menuitem invClick="+e.getClick()+" getSlotType="+e.getSlotType()+" getCurrentItem="+e.getCurrentItem()+" getCursor"+e.getCursor());
        //if (e.getCurrentItem()==null || e.getSlotType()==InventoryType.SlotType.OUTSIDE) return;
        if (e.getSlotType() == InventoryType.SlotType.OUTSIDE) return;

        //клик по хотбар
        if (e.getClick() == ClickType.NUMBER_KEY && e.getClickedInventory() != null && e.getClickedInventory().getSize() > e.getHotbarButton()) {
//System.out.println("NUMBER_KEY hotbar="+e.getClickedInventory().getItem(e.getHotbarButton()).getType());
            final ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
            if (hotbarItem != null && possibleMat.contains(hotbarItem.getType())) {
                final MenuItem si = fromItemStack(hotbarItem);
                if (si != null) {
                    if (!si.can_move) {
                        e.setResult(Event.Result.DENY);
                    }
                    if (si.on_inv_click != null) {
                        if (Timer.has(e.getWhoClicked().getEntityId())) return;
                        Timer.add(e.getWhoClicked().getEntityId(), 1);
                        si.on_inv_click.accept(e);
                    }
                    //if (e.getClick()==ClickType.CREATIVE) {
                    //e.setCancelled(true);
                    //    Ostrov.sync(()-> ((Player)e.getWhoClicked()).updateInventory(), 1);
                    //((Player)e.getWhoClicked()).updateInventory();
                    //}
                    //e.setCancelled(true);
                    //((Player)e.getWhoClicked()).updateInventory();
                }
            }
        }
        //подмена предмета на курсор
        if (e.getCursor() != null && possibleMat.contains(e.getCursor().getType())) {
            final MenuItem si = fromItemStack(e.getCursor());
            if (si != null && !si.can_move) {
                e.setResult(Event.Result.DENY);
            }
        }
        //просто клик по предмету
        if (e.getCurrentItem() != null && possibleMat.contains(e.getCurrentItem().getType())) {
            final MenuItem si = fromItemStack(e.getCurrentItem());
            if (si != null) {
                if (!si.can_move) {
                    e.setResult(Event.Result.DENY);
                }
                if (si.on_inv_click != null) {
                    if (Timer.has(e.getWhoClicked().getEntityId())) return;
                    Timer.add(e.getWhoClicked().getEntityId(), 1);
                    si.on_inv_click.accept(e);
                }
            }
        }

    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        final MenuItem si = fromItemStack(e.getItemDrop().getItemStack());
        if (si != null && !si.can_drop) {
            e.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {

//Ostrov.log("Lobbyitems PlayerInteractEvent="+e.getAction()+" useItemInHand="+e.useItemInHand());
        if (e.getItem() == null || e.getAction() == Action.PHYSICAL) return;

        final MenuItem menuItem = fromItemStack(e.getItem());
//Ostrov.log("menuItem="+menuItem);
        if (menuItem == null) return;

        final Player p = e.getPlayer();
//System.out.println("Lobbyitems p.getOpenInventory()="+p.getOpenInventory());

        //фикс - на 1.16 воспринимает клик в меню как интеракт!!!!!
        //CRAFTING, когда нет открытого инвентаря или инвентарь игроков открыт, и возвращает CREATIVE, если они находятся в творческом режиме
        if (p.getOpenInventory().getType() != InventoryType.CRAFTING && p.getOpenInventory().getType() != InventoryType.CREATIVE)
            return;

//menuItem.can_interact = e.getItem().getType()==Material.FIREWORK_ROCKET;
        if (!menuItem.can_interact) {
            e.setUseInteractedBlock(Event.Result.DENY);
        }
        //e.setUseItemInHand(Event.Result.DENY);

        if (!menuItem.can_interact && (menuItem.on_right_click != null || menuItem.on_right_sneak_click != null)) {
            e.setUseItemInHand(Event.Result.DENY); //отменять ПКМ только для командных. или не работает лук-телепортер
        }// else {
        //    e.setUseItemInHand(Event.Result.DENY); //отменять ПКМ только для командных. или не работает лук-телепортер
        //}
//Ostrov.log(" can_interact?"+menuItem.can_interact+" useInteractedBlock="+e.useInteractedBlock()+" useItemInHand="+e.useItemInHand());

        if (Timer.has(p.getEntityId())) return;

        if (menuItem.on_interact != null) {
            Timer.add(p.getEntityId(), 1);
            menuItem.on_interact.accept(e);
        }

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (p.isSneaking() && menuItem.on_right_sneak_click != null) {
                menuItem.on_right_sneak_click.accept(e.getPlayer());
                Timer.add(p.getEntityId(), 1);
            } else if (menuItem.on_right_click != null) {
                Timer.add(p.getEntityId(), 1);
                menuItem.on_right_click.accept(e.getPlayer());
            }
        } else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (p.isSneaking() && menuItem.on_left_sneak_click != null) {
                menuItem.on_left_sneak_click.accept(e.getPlayer());
                Timer.add(p.getEntityId(), 1);
            } else if (menuItem.on_left_click != null) {
                Timer.add(p.getEntityId(), 1);
                menuItem.on_left_click.accept(e.getPlayer());
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onLocalDataLoadEvent(final LocalDataLoadEvent e) {
//System.out.println("ru.komiss77.modules.LobbyItems.onBungeeDataRecieved()");
        final Set<Integer> has = new HashSet<>();  //создадим список, что итак есть в инвентаре
        for (final ItemStack is : e.getPlayer().getInventory().getContents()) {
            final int id = idFromItemStack(is);
            if (id > 0) {
                has.add(id);
            }
        }
//System.out.println("BungeeDataRecieved has="+has.toString());
        itemByNames.values().stream().filter((si) -> (si.give_on_join && !has.contains(si.id))).forEachOrdered((si) -> {
            si.give(e.getPlayer());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        if (!PM.exist(e.getPlayer().getName())) return;
        final Set<Integer> has = new HashSet<>();  //создадим список, что итак есть в инвентаре
        for (final ItemStack is : e.getPlayer().getInventory().getContents()) {
//System.out.println("Respawn is=="+is+" spec?"+isSpecItem(is));
            final int id = idFromItemStack(is);
            if (id > 0) { //(isSpecItem(is)) {
                has.add(id);
            }
        }
//System.out.println("Respawn has="+has.toString());
        itemByNames.values().stream().filter((si) -> (si.give_on_respavn && !has.contains(si.id))).forEachOrdered((si) -> {
            si.give(e.getPlayer());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCompleteWorldChange(PlayerChangedWorldEvent e) {
        final Set<Integer> has = new HashSet<>();  //создадим список, что итак есть в инвентаре
        for (final ItemStack is : e.getPlayer().getInventory().getContents()) {
            final int id = idFromItemStack(is);
            if (id > 0) {
                has.add(id);
            }
        }
//System.out.println("ChangedWorld has="+has.toString());
        itemByNames.values().stream().filter((si) -> (si.give_on_world_change && !has.contains(si.id))).forEachOrdered((si) -> {
            si.give(e.getPlayer());
        });
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPickUp(EntityPickupItemEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        if (item_lobby_mode) {
            e.setCancelled(true);
            e.getItem().remove();
        } else {
            final MenuItem si = fromItemStack(e.getItem().getItemStack());
            if (si != null && !si.can_pickup) {
                e.setCancelled(true);
                e.getItem().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSwitch(PlayerSwapHandItemsEvent e) {
        MenuItem si = fromItemStack(e.getMainHandItem());
        if (si != null && !si.can_swap_hand) {
            e.setCancelled(true);
        }
        si = fromItemStack(e.getOffHandItem());
        if (si != null && !si.can_swap_hand) {
            e.setCancelled(true);
        }
    }


    public static boolean isSpecItem(final ItemStack is) {
//System.out.println("--isSpecItem id="+idFromItemStack(is));
        return is != null && possibleMat.contains(is.getType()) && is.hasItemMeta() && is.getItemMeta().hasCustomModelData() && itemById.containsKey(is.getItemMeta().getCustomModelData());
    }

    public static boolean hasItem(final String name) {
        return itemByNames.containsKey(name);
    }


    public static void addItem(final MenuItem si) {
        if (si == null || si.name == null || si.name.isEmpty()) return;
        itemByNames.put(si.name, si);
        itemById.put(si.id, si);
        possibleMat.add(si.getMaterial());
    }

    public static boolean giveItem(final Player p, final String item_name) {
        return itemByNames.containsKey(item_name) && itemByNames.get(item_name).give(p);
    }

    public static MenuItem fromItemStack(final ItemStack is) {
        if (is == null || !possibleMat.contains(is.getType()) || !is.hasItemMeta() || !is.getItemMeta().hasCustomModelData())
            return null;
        //if (isSpecItem(is)) {
        return itemById.get(is.getItemMeta().getCustomModelData());
        //}
//System.out.println("--fromItemStack si="+si+" is dcec?"+si.isSpecItem(is));
        //return null;
    }

    public static int idFromItemStack(final ItemStack is) {
        if (is != null && possibleMat.contains(is.getType()) && is.hasItemMeta() && is.getItemMeta().hasCustomModelData()) {
            int id = is.getItemMeta().getCustomModelData();
            return (itemById.containsKey(id)) ? id : 0;
        }
        return 0;
    }

}
