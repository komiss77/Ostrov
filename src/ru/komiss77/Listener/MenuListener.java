
package ru.komiss77.Listener;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.inventory.InventoryType;
import ru.komiss77.ApiOstrov;

import ru.komiss77.Commands.CMD;
import ru.komiss77.Commands.Pvp;
import ru.komiss77.Cfg;
import ru.komiss77.Kits.KitManager;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.Timer;
import ru.komiss77.Ostrov;
import ru.komiss77.ProfileMenu.mainHandler;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.ChatMsgUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.WGutils;



public class MenuListener implements Listener {


    
    
private static boolean allow_rg_tp;


public static void Init () {
    
    
    //pipboy_name = Conf.GetCongig().getString("system.pipboy_name");
    //cosmetic_name = Conf.GetCongig().getString("system.cosmetic_name");
    allow_rg_tp = Cfg.GetCongig().getBoolean("modules.teleport_to_region_in_settings_menu");

}    
    
public static void ReloadVars () {
    Init ();
}
    
    

    
    
    
    
    
// ------------------------------- ITEM -------------------------------------------    
    
//@EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
//    public void upd_last_click (InventoryClickEvent e) {
//        PM.getOplayer(e.getWhoClicked().getName()).last_inv_click=Timer.Единое_время();
//    }
   // @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
   // public void test (InventoryClickEvent e) {
//System.out.println("invClick "+e.getView().getTitle()+" canceled?"+e.isCancelled());

   // }
    //слушаем нижний ряд всего начинающегося на профиль самым последним (плагины могут отменить раньше свои слоты)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void InventoryProfile (InventoryClickEvent e) {
        if (e.getInventory().getType()!=InventoryType.CHEST || e.getSlot() <0 || e.getSlot() > 53) return;
        if (e.getCurrentItem()== null || !e.getView().getTitle().equals(ItemUtils.profile_master_inv_name)) return; //Title not set
        e.setCancelled(true);
        //if (e.getCurrentItem().getType()==Material.STAINED_GLASS_PANE || e.getCurrentItem().getType()==Material.GLASS_BOTTLE) return;
        if(Timer.CD_has(e.getWhoClicked().getName(), "InventoryProfile")) return;
        Timer.CD_add(e.getWhoClicked().getName(), "InventoryProfile", 1);
        if (e.getCurrentItem().getType().toString().contains("STAINED_GLASS_PANE") || e.getCurrentItem().getType()==Material.GLASS_BOTTLE) return;
        Ostrov.sync( ()->mainHandler.invClick(e), 1);
    }
    
    
    
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void InventoryClick (InventoryClickEvent e) {
        
        if (e.getInventory().getType()!=InventoryType.CHEST) return;
        
        if ( e.getSlot() <0 || e.getSlot() > 44) return;
        
        String itemname = "";
       // List <String> lore;
        if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
            itemname=e.getCurrentItem().getItemMeta().getDisplayName();
         //   lore = e.getCurrentItem().getItemMeta().getLore();
        }
 
        //if (PlayerListener.item_lobby_mode) {
        //    if ( itemname.equals(pipboy_name) || itemname.equals(cosmetic_name) || itemname.equals(friend_name) ) {
        //        e.setCancelled(true);
        //        return;
        //    }
        //}
        
        Player p = (Player) e.getWhoClicked();

        switch (e.getView().getTitle() ) {
            
        case "§1Личные настройки":
                e.setCancelled(true);
                //if ( itemname.isEmpty() || e.getCurrentItem().getType().equals(Material.STAINED_GLASS_PANE) ) return;
                if ( itemname.isEmpty() || e.getCurrentItem().getType().toString().contains("STAINED_GLASS_PANE")  ) return;
                //if ( !Ostrov.v1710 && (e.getClickedInventory() == null || e.getClickedInventory().getTitle() == null)  ) return;   //фикс-в калдроне нету getTitle
                
                   /* 
                    if (!Ostrov.v1710 && e.getCurrentItem().getType().equals(Material.valueOf("ELYTRA"))) {
                            if (CMD.fly_command && itemname.equals("§6Полёт") ) {
                                if ( p.isOp() || p.hasPermission("ostrov.fly") ) {
                                    if (e.isRightClick()) {
                                        p.setFlying(false); p.setAllowFlight(false); 
                                        Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.off);
                                    } else if (e.isLeftClick()) {
                                        p.setAllowFlight(true); p.setFlying(true); 
                                        Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.on);
                                    }
                                } else Res(p,"noperm");
                            } else Res(p,"off");
                            p.updateInventory();
                    }
                */
                
                
                
                
                
                
               // switch (e.getCurrentItem().getType()) {
                switch (e.getSlot()) {
                    
                   // case LEATHER:
                    case 9:
                            if (CMD.fly_command && itemname.equals("§6Полёт") ) {
                                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.fly") ) {
                                    if (e.isRightClick()) {
                                        p.setFlying(false); p.setAllowFlight(false); 
                                        Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.off);
                                    } else if (e.isLeftClick()) {
                                        p.setAllowFlight(true); p.setFlying(true); 
                                        Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.on);
                                    }
                                } else Res(p,"noperm");
                            } else Res(p,"off");
                            p.updateInventory();
                        break;
                        
                    //case DIAMOND_SWORD:
                    case 10:
                            if (Pvp.allow_pvp_command && itemname.equals("§aРежим ПВП") ) {
                                    if (e.isRightClick()) {
                                        PM.getOplayer(p.getName()).pvp_allow=false; 
                                        Res(p,"ok");
                                        e.getInventory().setItem(e.getSlot()-9, ItemUtils.off);
                                    } else if (e.isLeftClick()) {
                                        PM.getOplayer(p.getName()).pvp_allow=true; 
                                        Res(p,"ok");
                                        e.getInventory().setItem(e.getSlot()-9, ItemUtils.on);
                                    }
                            } else Res(p,"off");
                            p.updateInventory();
                        break;
                        
                    //case FEATHER:
                    case 11:
                            if (CMD.speed_command && itemname.equals("§6Скорость полёта") ) {
                                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.flyspeed") ) {
                                    if (e.isRightClick()) {
                                        p.setFlySpeed(0.1F);
                                        Res(p,"ok"); e.getInventory().getItem(e.getSlot()-9).setAmount(1);
                                    } else if (e.isLeftClick()) {
                                        int curr = e.getInventory().getItem(e.getSlot()-9).getAmount();
                                        curr++; if (curr>10) {Res(p,"max"); return;} 
                                        p.setFlySpeed((float)curr/10);
                                        Res(p,"ok"); e.getInventory().getItem(e.getSlot()-9).setAmount(curr);
                                    }
                                } else Res(p,"noperm");
                            } else Res(p,"off");
                            p.updateInventory();
                        break;
                        
                    //case CHAINMAIL_BOOTS:
                    case 12:
                            if (CMD.speed_command && itemname.equals("§6Скорость ходьбы") ) {
                                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.walkspeed") ) {
                                    if (e.isRightClick()) {
                                        p.setWalkSpeed(0.1F);
                                        Res(p,"ok"); e.getInventory().getItem(e.getSlot()-9).setAmount(1);
                                    } else if (e.isLeftClick()) {
                                        int curr = e.getInventory().getItem(e.getSlot()-9).getAmount();
                                        curr++; if (curr>10) {Res(p,"max"); return;} 
                                        p.setWalkSpeed((float)curr/10);
                                        Res(p,"ok"); e.getInventory().getItem(e.getSlot()-9).setAmount(curr);
                                    }
                                 } else Res(p,"noperm");
                           } else Res(p,"off");
                            p.updateInventory();
                        break;
                        
                    //case WATER_BUCKET:
                    case 13:
                            if (CMD.pweather_command && itemname.equals("§6Личная погода") ) {
                                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.pweather") ) {
                                    if (e.isRightClick()) {
                                        p.resetPlayerWeather();
                                        Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.panel);
                                    } else if (e.isLeftClick()) {
                                        if(p.getPlayerWeather()==null) {
                                            p.setPlayerWeather(WeatherType.CLEAR);
                                            Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.sun);
                                        } else if (p.getPlayerWeather().equals(WeatherType.CLEAR)) {
                                            p.setPlayerWeather(WeatherType.DOWNFALL);
                                            Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.rain);
                                        } else {
                                            p.setPlayerWeather(WeatherType.CLEAR);
                                            Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.sun);
                                        }
                                    }
                                 } else Res(p,"noperm");
                            } else Res(p,"off");
                            p.updateInventory();
                        break;
                        
                    //case GOLD_NUGGET:
                    case 14:
                            if (CMD.ptime_command && itemname.equals("§6Личное время - управление") ) {
                                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.ptime") ) {
                                    if (e.isRightClick()) {
                                        p.resetPlayerTime();                                        //поставить серверное и не менять
                                        Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.panel);
                                        e.getInventory().getItem(e.getSlot()-8).setAmount( 1 );
                                    } else if (e.isLeftClick()) {
                                        if (p.isPlayerTimeRelative()) {                     //заморозка и фикс серверного
                                            p.setPlayerTime(p.getWorld().getTime(), false);
                                            Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.off);
                                            e.getInventory().getItem(e.getSlot()-8).setAmount( (int)p.getWorld().getTime()/1000 );
                                        } else {
                                            p.setPlayerTime(p.getPlayerTimeOffset(), true);  //разморозка и фикс личного
                                            Res(p,"ok"); e.getInventory().setItem(e.getSlot()-9, ItemUtils.on);
                                            e.getInventory().getItem(e.getSlot()-8).setAmount( (int)p.getPlayerTimeOffset()/1000 );
                                        }
                                    }
                                 } else Res(p,"noperm");
                            } else Res(p,"off");
                            p.updateInventory();
                        break;
                        
                    //case WATCH:
                    case 15:
                            if (CMD.ptime_command && itemname.equals("§6Личное время - настройка")) {
                                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.ptime") ) {
                                if (!e.getInventory().getItem(e.getSlot()-10).equals(ItemUtils.panel)) {
                                    if (e.isRightClick()) {
                                        int curr =  e.getInventory().getItem(e.getSlot()-9).getAmount();
                                        curr--; if (curr<1) {curr = 1; Res(p,"min"); return;} 
                                        p.setPlayerTime(curr*1000, p.isPlayerTimeRelative());
                                        Res(p,"ok"); e.getInventory().getItem(e.getSlot()-9).setAmount(curr);
                                    } else if (e.isLeftClick()) {
                                        int curr =  e.getInventory().getItem(e.getSlot()-9).getAmount();
                                        curr++; if (curr>24) {curr = 24;Res(p,"max"); return;} 
                                        p.setPlayerTime(curr*1000, p.isPlayerTimeRelative());
                                        Res(p,"ok"); e.getInventory().getItem(e.getSlot()-9).setAmount(curr);
                                    }
                                } else Res(p,"notime");
                                 } else Res(p,"noperm");
                            } else Res(p,"off");
                            p.updateInventory();
                        break;
                        
                   // case GOLDEN_APPLE:
                    case 16:
                            if (CMD.heal_command && itemname.equals("§6Санаторий")) {
                                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.heal") ) {
                                    if ( p.getHealth() < p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                                        if (!PM.inBattle(p.getName())) {
                                            if (p.getHealth() == 0) return;
                                            final double amount =p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - p.getHealth();
                                            final EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(p, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
                                            Ostrov.GetInstance().getServer().getPluginManager().callEvent(erhe);
                                            double newAmount = p.getHealth() + erhe.getAmount();
                                            if (newAmount > p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) newAmount = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

                                            p.setHealth(newAmount);
                                            p.setFoodLevel(20);
                                            p.setFireTicks(0);
                                            p.getActivePotionEffects().stream().forEach((effect) -> {
                                                p.removePotionEffect(effect.getType());
                                            });
                                            Res(p,"ok"); 
                                            e.getInventory().setItem(e.getSlot()-9, ItemUtils.on);
                                            e.getInventory().getItem(e.getSlot()-9).setAmount((int)p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                                        } else Res(p,"pvp");
                                    } else Res(p,"max");
                                 } else Res(p,"noperm");
                            } else Res(p,"off");
                            p.updateInventory();
                        break;
                        
                  // case ANVIL:
                   case 17:
                            if (CMD.repair_command && itemname.equals("§6Кузница")) {
                                if ( p.isOp() || p.hasPermission("ostrov.repair") ) {
                                    if ( ItemUtils.Need_repair(p) ) {
                                        if (!PM.inBattle(p.getName())) {
                                            Res(p,"ok"); 
                                            p.sendMessage( "§aОтремонтировано: "+ItemUtils.Repair_all(p).toString().replaceAll("\\[|\\]", "") );
                                            e.getInventory().setItem(e.getSlot()-9, ItemUtils.on);
                                        } else Res(p,"pvp");
                                    } else Res(p,"no_repair");
                                 } else Res(p,"noperm");
                            } else Res(p,"off");
                            p.updateInventory();
                        break;
                    
                        
                        
                    //дома
                    case 37:
                        ItemUtils.Reset_info(e.getInventory());
                        if ( CMD.home_command && itemname.equals("§bМои дома")) {
                            Res(p,"ok");
                            Set <String> homes = PM.OP_GetHomeList(p.getName());
                           if (homes.isEmpty()) {
                               e.getInventory().setItem( 18, ItemUtils.no_homes);
                               return;
                           }
                            int pos = 18;
                            
                            Location h_loc;
                            for (String h: homes ) {
                                h_loc = PM.OP_GetHomeLocation(p, h);
                                ItemStack bed = new ItemBuilder(Material.RED_BED).name(h).build();
                                //ItemUtils.Set_name(bed, h);
                                if (h_loc != null) ItemUtils.Set_lore(bed, "§6Координаты: §7"+h_loc.getWorld().getName()+",", "§7  "+h_loc.getBlockX()+" x "+h_loc.getBlockY()+" x "+h_loc.getBlockZ(), "§aЛевый клик - §2ТП В ЭТОТ ДОМ", "§6Правый клик - §4УДАЛИТЬ" );
                                else ItemUtils.Set_lore(bed, "§6Координаты:", "§cНеисправность,", "§cНужно установить заново.", "§6Правый клик - §4УДАЛИТЬ");
                                bed.addUnsafeEnchantment(Enchantment.LUCK, pos);
                                e.getInventory().setItem(pos, bed);
                                e.getInventory().getItem(pos).setAmount(pos-17);
                                pos+=1;
                                    if ( pos > 35) {
                                        e.getInventory().setItem( 35, ItemUtils.too_many );
                                        return;
                                    }
                                }
                        } else Res(p,"off");
                        break;
                        
                    //приваты
                    case 39:
                        ItemUtils.Reset_info(e.getInventory());
                            Res(p,"ok");
                        if (Ostrov.getWorldGuard()!=null && itemname.equals("§bМои приваты")) {
                            
                            if (e.isLeftClick()) {
                                
                                List <ProtectedRegion> regions = WGutils.Get_world_player_owned_region(p);
                                if ( regions==null || regions.isEmpty()) {
                                    e.getInventory().setItem( 18, ItemUtils.no_regions);
                                    return;
                                }
                                
                                int pos = 18;

                                for (ProtectedRegion rg: regions ) {                //где владелец
                                    Location h_loc = WGutils.Get_region_center( p, rg );

                                    ItemStack bed = new ItemStack(Material.OAK_FENCE, 1);
                                    ItemUtils.Set_name( bed, rg.getId() );
                                    ItemUtils.Set_lore(bed, "§6Координаты: §7"+h_loc.getWorld().getName()+",", "§7  "+h_loc.getBlockX()+" x "+h_loc.getBlockY()+" x "+h_loc.getBlockZ(), (allow_rg_tp)?"§aЛевый клик - §2ТП В ЭТОТ ПРИВАТ":"§aЛевый клик - подробно", "§6Правый клик - §4УДАЛИТЬ" );
                                    bed.addUnsafeEnchantment(Enchantment.LUCK, pos);
                                    e.getInventory().setItem(pos, bed);
                                    e.getInventory().getItem(pos).setAmount(pos-17);
                                    pos+=1;
                                        if ( pos > 35) {
                                            e.getInventory().setItem( 35, ItemUtils.too_many );
                                            return;
                                        }
                                    }

                                regions = WGutils.Get_world_player_member_region(p);
 
                                    for (ProtectedRegion rg: regions ) {                //где член

                                        Location h_loc = WGutils.Get_region_center( p, rg );
                                        ItemStack bed = new ItemStack(Material.OAK_FENCE, 1);
                                        ItemUtils.Set_name( bed, "§5Информация" );
                                        ItemUtils.Set_lore(bed, "§fВ этом привате вы пользователь!",   "§6Название: §b"+rg.getId(),   "§6Координаты: §7"+h_loc.getWorld().getName()+", "+h_loc.getBlockX()+" x "+h_loc.getBlockY()+" x "+h_loc.getBlockZ(), "§aЛевый клик - подробно" );
                                        e.getInventory().setItem(pos, bed);
                                        e.getInventory().getItem(pos).setAmount(pos-17);
                                        pos+=1;
                                            if ( pos > 35) {
                                                e.getInventory().setItem( 35, ItemUtils.too_many );
                                                return;
                                            }
                                        }
                            
                            } else if (e.isRightClick()) {
                                
                                List <ProtectedRegion> regions = WGutils.Get_all_player_region(p);
                                if (regions==null || regions.isEmpty()) {
                                    e.getInventory().setItem( 18, ItemUtils.no_regions);
                                    return;
                                }
                                
                            int pos = 18;

                            for (ProtectedRegion rg: regions ) {
                                Location h_loc = WGutils.Get_region_center( p, rg );
                                
                                ItemStack bed = new ItemStack(Material.OAK_FENCE, 1);
                                ItemUtils.Set_name( bed, "§5Информация" );
                                ItemUtils.Set_lore(bed, "" , "§6Название: §b"+rg.getId(), "§6Координаты: §7"+h_loc.getWorld().getName()+",", "§7  "+h_loc.getBlockX()+" x "+h_loc.getBlockY()+" x "+h_loc.getBlockZ() );
                                e.getInventory().setItem(pos, bed);
                                e.getInventory().getItem(pos).setAmount(pos-17);
                                pos+=1;
                                    if ( pos > 35) {
                                        e.getInventory().setItem( 35, ItemUtils.too_many );
                                        return;
                                    }
                                }
                                
                            }
                        } else Res(p,"off");
                        break;
                        
                    //наборы
                    case 41:
                        ItemUtils.Reset_info(e.getInventory());
                        if ( CMD.home_command && itemname.equals("§bМои наборы")) {
                            Res(p,"ok");
                            KitManager.openGuiMain(p);
                           //p.closeInventory();
                           //p.openInventory(KitManager.createInventory(p) );
                            
                        } else Res(p,"off");
                        break;
                        
                    //варпы
                    case 43:
                        ItemUtils.Reset_info(e.getInventory());
                        if ( CMD.home_command && itemname.equals("§bМои варпы")) {
                            Res(p,"ok");
                            
                            
                        } else Res(p,"off");
                        break;
                        
                        
                        
                        
                        
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                    case 32:
                    case 33:
                    case 34:
                    case 35:
                        
                        if (e.getCurrentItem() == null || e.getCurrentItem().getType()==null ) return;
                        
                        if ( e.isLeftClick() ){
                            
                            
                            switch ( e.getCurrentItem().getType() ) {             //срабатывае если больше 17
                                
                                case REDSTONE_TORCH:
                                    if ( e.getInventory().getItem(18)!=null && e.getInventory().getItem(18).getType()!=null ) {
                                        switch (e.getInventory().getItem(18).getType()) {
                                            case RED_BED:
                                                p.closeInventory();
                                                p.sendMessage( "§bВаши дома: §6"+PM.OP_GetHomeList(p.getName()).toString().replaceAll("\\[|\\]", "") );
                                                break;
                                            case OAK_FENCE:
                                                p.closeInventory();
                                                p.sendMessage( "§bПриваты в этом мирe (вы-владелец): §6"+WGutils.Get_world_player_owned_region_text(p).toString().replaceAll("\\[|\\]", "") );
                                                p.sendMessage( "§bПриваты в этом мирe (вы-пользователь): §6"+WGutils.Get_world_player_member_region_text(p).toString().replaceAll("\\[|\\]", "") );
                                                break;

                                            case ENDER_CHEST:

                                                break;

                                            case COMPASS:

                                                break;

                                            default:
                                                break;
                                        }
                                    }
                                    
                                    break;

                                case RED_BED:
                                    if (PM.OP_GetHomeList(p.getName()).isEmpty() ){
                                        p.closeInventory();
                                        p.performCommand("sethome");
                                    } else {
                                        if ( !itemname.isEmpty() && PM.OP_GetHomeList(p.getName()).contains(itemname) ) {
                                            p.closeInventory();
                                            p.performCommand("home "+itemname);
                                        }
                                    }
                                    break;
                                    
                                case OAK_FENCE:
                                if (WGutils.Get_world_player_owned_region(p).isEmpty()) {
                                        p.closeInventory();
                                        p.performCommand("land");
                                } else {
                                    if (allow_rg_tp) {
                                            
                                                if (Timer.CD_has( p.getName(), "rg_tp"+itemname ) ) {
                                                    Res(p, "dont");
                                                    p.sendMessage("§cТП в каждый приват возможен один раз в сутки!");
                                                    return;
                                                }
                                                Res(p, "ok");
                                                Timer.CD_add( p.getName(), "rg_tp"+itemname, 1440);

                                            
                                                if ( !itemname.isEmpty() && !itemname.equals("§5Информация")) {
                                                Location loc = WGutils.Get_region_center_by_id(p, itemname);
                                                    if (loc!=null) ApiOstrov.teleportSave(p, loc );
                                        } 
                                    } else {
                                        if ( !itemname.isEmpty() ){
                                            Res(p, "ok");
                                            if ( itemname.equals("§5Информация") ) {
                                                p.performCommand("rg info "+e.getCurrentItem().getItemMeta().getLore().get(1).replaceFirst("§6Название: §b", ""));
                                            } else {
                                                p.performCommand("rg info "+itemname);
                                            }
                                        } else Res(p, "dont");
                                    }
                                }
                                    break;
                                    
                                case ENDER_CHEST:
                                    
                                    break;
                                    
                                case COMPASS:
                                    
                                    break;
                                    
                                default:
                                    break;
                            }
                            
                            
                            
                        } else if (e.isRightClick()) {
                            
                            switch (e.getCurrentItem().getType()) {
                                
                                case RED_BED:
                                    if ( !itemname.isEmpty() && PM.OP_GetHomeList(p.getName()).contains(itemname) ) {
                                        p.closeInventory();
                                    ChatMsgUtil.Send_TextComponent_onclick_run(p, "§e -------->>> Клик сюда, чтобы удалить дом §b"+itemname+" §e<<<-------- ", "§5Клик, чтобы удалить", "/delhome "+itemname );
                                //TextComponent temp = new TextComponent("§e -------->>> Клик сюда, чтобы удалить дом §b"+itemname+" §e<<<-------- " );
                                //        temp.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§5Клик, чтобы удалить").create() ) );
                                //        temp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/delhome "+itemname) );
                                //        p.spigot().sendMessage( temp);    
                                    }
                                    break;
                                    
                                case OAK_FENCE:
                                    if ( !itemname.isEmpty() && !itemname.equals("§5Информация") ) {
                                        p.closeInventory();
                                        ChatMsgUtil.Send_TextComponent_onclick_run(p, "§e -------->>> Клик сюда, чтобы удалить приват §b"+itemname+" §e<<<-------- ", "§5Клик, чтобы удалить", "/region delete "+itemname );
                                        //TextComponent temp = new TextComponent("§e -------->>> Клик сюда, чтобы удалить приват §b"+itemname+" §e<<<-------- " );
                                        //temp.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§5Клик, чтобы удалить").create() ) );
                                       // temp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/region delete "+itemname) );
                                       // p.spigot().sendMessage( temp);    
                                    }
                                    break;
                                    
                                case ENDER_CHEST:
                                    break;
                                    
                                case COMPASS:
                                    break;
                                    
                                default:
                                    break;
                            }
                        } 
                        break;
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                    //case RED_MUSHROOM:
                    case 44:
                        ItemUtils.Reset_info(e.getInventory());
                        p.closeInventory();
                        break;
                        
                    
                }
                
                
                break;
            
                






                
                
                
      //  case "§2Смена мира":
              //  e.setCancelled(true);
               // if ( itemname.isEmpty() ) return;
                //if ( !Ostrov.v1710 && (e.getClickedInventory() == null || e.getClickedInventory().getTitle() == null)  ) return;   //фикс-в калдроне нету getTitle
               //if (e.getCurrentItem() == null  || !e.getCurrentItem().hasItemMeta()  || !e.getCurrentItem().getItemMeta().hasDisplayName()) return;
                      //  if (Bukkit.getWorld(e.getCurrentItem().getItemMeta().getDisplayName()) != null) {
                       // if (!itemname.isEmpty() && Bukkit.getWorld(itemname) != null) {
                       //     p.teleport( Bukkit.getWorld(itemname).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                       // }
               // break;
       
 /*
case "§aНаборы":
                e.setCancelled(true);
                if ( itemname.isEmpty() ) return;
                    
                String kitname = KitManager.Kit_name_by_display_name (itemname);
                if (kitname==null) return;
                    p.closeInventory();
                    
                    //if (e.getCurrentItem().getType().equals(Material.STAINED_GLASS_PANE)) {
                    if (e.getCurrentItem().getType().toString().contains("STAINED_GLASS_PANE") ) {
                        p.performCommand("kit buy "+kitname);
                    } else p.performCommand("kit "+kitname);
                break;
                */
                
                
                
                
                
                
                
                
        
        } //конец switch по названию инвентаря 
        
        
        
        
        
        
        
        
    }    
   
    
    
private static void Res(Player p, String type) {
    
    switch (type) {
        case "notime":
            //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1F, 1F);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT , 3F, 2.0F);
            p.sendMessage("§4Выберите режим работы самородком");
            break;
            
        case "ok":
            //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1F, 1F);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT , 3F, 2.0F);
            break;
            
        case "off":
            p.sendMessage("§4Данная фунция отключена.");
            //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASEDRUM, 1F, 1F);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM , 3F, 2.0F);
            break;
            
        case "noperm":
            p.sendMessage("§4У Вас нет привилегии для данной функции.");
            //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_SNARE, 1F, 1F);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE , 3F, 2.0F);
            break;
            
        case "max":
            p.sendMessage("§4Значение достигло верхнего предела. Правый клик - сброс.");
            //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1F, 1F);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING , 3F, 2.0F);
            break;

        case "min":
            p.sendMessage("§4Значение достигло нижнего предела.");
            //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BASS, 1F, 1F);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS , 3F, 2.0F);
            break;

        case "pvp":
            p.sendMessage("§4Во время боя недоступно!");
            //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_SNARE, 1F, 1F);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE , 3F, 2.0F);
            break;

        case "no_repair":
            p.sendMessage("§2Все предметы в инвентаре в порядке!");
            //p.playSound(p.getLocation(), Sound.BLOCK_NOTE_SNARE, 1F, 1F);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE , 3F, 2.0F);
            break;
            
        case "dont":
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS , 3F, 2.0F);
            break;


        default:
            break;
    }

}


    
    
    
    
    
    
    
    
    
// ------------------------------------------------------------------------
    
    
    
    
    
    
         

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   
   
   
   
   
   
   
   
   
   
   
   
   
    

    
    
        
        
        
        
//-------------------- просмотр и модификация чужого инвентаря -----------------        
    /*    final Inventory top = e.getView().getTopInventory();
        final InventoryType type = top.getType();
        Player refreshPlayer = null;
        final InventoryHolder invHolder = top.getHolder();
        
    //System.out.println("-------- top.getTitle():"+top.getTitle()+"  invHolder:"+invHolder); 
    
        if (type == InventoryType.PLAYER ) {
            if (invHolder != null && invHolder instanceof HumanEntity) {
                    //final Player invOwner = ((Player)invHolder);
                    if ( !p./helpission("ostrov.invsee.modify")) {
                        e.setCancelled(true);
                        refreshPlayer = p;
                    }
            }
        } else if (type == InventoryType.ENDER_CHEST) {
            if (invHolder != null && invHolder instanceof HumanEntity) {
                    //final Player invOwner = ((Player)invHolder);
                    if ( !p.hasPermission("ostrov.enderchest.modify")) {
                        e.setCancelled(true);
                        refreshPlayer = p;
                    }
            }
        }
//----------------------------------------------------------------------        
        
        if (e.getView().getTitle().startsWith("§1Аммуниция игрока")) {
            if ( p.hasPermission("ostrov.invsee.modify")) {
                String nik = e.getView().getTitle().replaceFirst("§1Аммуниция игрока ", "");
                Player target = Bukkit.getPlayer(nik);
//System.out.println("Ник "+nik+" игрок:"+target);
                    if (target!=null && target.isOnline()) {
                        target.getInventory().setArmorContents(e.getInventory().getContents());
                        target.updateInventory();
                    } else {
                        p.closeInventory();
                        p.sendMessage("§cИгрок вышел с сервера!");
                    }
            } else e.setCancelled(true);
        } else if (e.getView().getTitle().startsWith("§1Инвентарь игрока")) {
             e.setCancelled(true);
        }
	*/    
    
}
