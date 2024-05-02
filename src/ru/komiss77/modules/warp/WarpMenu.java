package ru.komiss77.modules.warp;


import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.ApiOstrov;
import ru.komiss77.LocalDB;
import ru.komiss77.Perm;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.ConfirmationGUI;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;
import ru.komiss77.utils.inventory.SmartInventory;




public class WarpMenu implements InventoryProvider {
    
    
    
    private static final ItemStack fill = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§8.").build();;
    private boolean hidePrivate;
    private boolean hidePaid;
    private boolean hideClosed;
    
    //отдельный файл настроек ?
    //настройка сonsoleOnlyUse - разрешить игрокам команду warp
    //настройка по группам
    //меню настройки
    
    @Override
    public void init(final Player p, final InventoryContent contents) {

        contents.fillRect(0,0, 4,8, ClickableItem.empty(fill));
        
        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        
        
        //final WarpManager wm = ApiOstrov.getWarpManager();
        
        boolean hasPerm;
        int count = 0;
        
        for (final String warpName : WarpManager.getWarpNames()) {
            
            final Warp w = WarpManager.getWarp(warpName);
            
            if (!w.system && hidePrivate) continue;
            if (!w.open && hideClosed) continue;
            if (w.use_cost>0 && hidePaid) continue;
            
            hasPerm = !w.need_perm || ApiOstrov.isLocalBuilder(p, false) || w.isOwner(p) || p.hasPermission("warp.use."+warpName);
            
            if (ApiOstrov.isLocalBuilder(p, false) || w.isOwner(p)) {
                
                if (w.isOwner(p)) count++; //счётчик варпов владельца, нужно ниже
                
                menuEntry.add(ClickableItem.of(new ItemBuilder(w.open ? w.dispalyMat : Material.BARRIER)
                    .name("§f"+warpName)
                    .addLore(w.isOwner(p) ? "§7Вы владелец" : "§7Владелец "+w.owner)
                    .addLore(w.descr)
                    .addLore("§7Создан "+ApiOstrov.dateFromStamp(w.create_time))
                    .addLore("")
                    .addLore( w.open ? (w.system ? "§3Общий" : "§6Частный") : "§cВыключен" )
                    .addLore("§7Посещений: §b"+w.use_counter)
                    .addLore(w.isPaid() && !ApiOstrov.isLocalBuilder(p, false) && !w.isOwner(p) ? "§7Плата за посещение: "+w.use_cost+" лони" : "")
                    .addLore( hasPerm ? "" : "§cнет права warp.use."+warpName)
                    .addLore("")
                    .addLore(w.open && hasPerm ? "§7ЛКМ - §aпосетить" : "")
                    .addLore("§7ПКМ - настройки")
                    .addLore(w.open ? "§7Шифт+ПКМ - §4закрыть" : "§7Шифт+ПКМ - §2открыть")
                    .addLore("§7Клав. Q - §cудалить")
                    .addLore("")
                    .build(), e -> {
                        switch (e.getClick()) {
                            
                            case LEFT:
                                WarpManager.tryWarp(p, warpName);
                                return;
                                
                            case RIGHT:
                                SmartInventory.builder()
                                    .type(InventoryType.HOPPER)
                                    .id("WarpSettings"+p.getName()) 
                                    .provider(new WarpSetupMenu(w))
                                    .title("§fНастройки "+warpName)
                                    .build()
                                    .open(p);
                                return;
                                
                            case SHIFT_RIGHT:
                                if (!LocalDB.useLocalData) {
                                    p.sendMessage("§eЛокальная БД отключена, действие невозможно.");
                                    return;
                                }
                                WarpManager.changeOpen(warpName);
                                reopen(p, contents);
                                return;
                                
                            case DROP:
                                if (!LocalDB.useLocalData) {
                                    p.sendMessage("§eЛокальная БД отключена, действие невозможно.");
                                    return;
                                }
                                ConfirmationGUI.open( p, "§cУдалить место "+warpName+"?", result -> {
                                    if (result) {
                                        WarpManager.delWarp(p, warpName);
                                    }
                                    reopen(p, contents);
                                });
                                return;
							default:
								break;
                        }

                    }));  
                
            } else {

                menuEntry.add( ClickableItem.of(new ItemBuilder(w.open ? w.dispalyMat : Material.BARRIER)
                    .name("§f"+warpName)
                    .addLore(w.isOwner(p) ? "§7Вы владелец" : "§7Владелец "+w.owner)
                    .addLore(w.descr)
                    .addLore("§7Создан "+ApiOstrov.dateFromStamp(w.create_time))
                    .addLore("")
                    .addLore( w.open ? (w.system ? "§3Общий" : "§6Частный") : "§cВыключен" )
                    .addLore("§7Посещений: §b"+w.use_counter)
                    .addLore(w.isPaid() ? "§7Плата за посещение: "+w.use_cost+" лони" : "")
                    .addLore( hasPerm ? "" : "§cнет права warp.use."+warpName)
                    .addLore("")
                    .addLore(w.open && hasPerm ? "§7ЛКМ - §aпосетить" : "")
                    .addLore("")
                    .build(), e -> {
                        if (e.isLeftClick()) {
                            WarpManager.tryWarp(p, warpName);
                        }

                    }));  

                
            }
            
            
        }
        
        
        
        
        
        if (menuEntry.isEmpty()) {
            contents.set(2,4, ClickableItem.empty(new ItemBuilder( Material.GLASS_BOTTLE)
                 .name("§7Доступных мест не найдено!")
                 .build()));  
        }
        
        
        
        
        
        
        
        
        if (!LocalDB.useLocalData) {
            
            contents.set( 5, 2,  ClickableItem.empty(new ItemBuilder(Material.HOPPER)
                .name("§eДобавить серверное место")
                .addLore("§cЛокальная БД отключена,")
                .addLore("§cдобавить новое место")
                .addLore("§сневозможно.")
                .build()
            ));
            
        } else if (ApiOstrov.isLocalBuilder(p, false)) {
            
            contents.set(5, 2, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.HOPPER)
                .name("§eДобавить серверное место")
                .addLore("§7")
                .addLore("§7Создать общедоступное")
                .addLore("§7место в точке, где вы стоите.")
                .addLore("§7")
                .addLore("§7ЛКМ - создать")
                .addLore("§7")
                .build(),  "название", msg -> {
                    final String strip = TCUtils.stripColor(msg);
                    
                    if(strip.length()>24 ) {
                        p.sendMessage("§cЛимит 24 символа!");
                        PM.soundDeny(p);
                        return;
                    }
                    
                    if (WarpManager.getWarpNames().contains(strip)) {
                        p.sendMessage("§cМесто с таким названием уже есть!");
                        PM.soundDeny(p);
                        return;
                    }
                    
                    final Warp warp = new Warp(strip, "ostrov", ApiOstrov.currentTimeSec());
                    warp.descr = "";
                    warp.setLocation (p.getLocation());
                    warp.system = true;
                    
                    WarpManager.saveWarp(p, warp);

                    reopen(p, contents);
                }));    
            

        }  else {
            
            
            
            
          /*  if (limit==0) {
                
                contents.set( 5, 2,  ClickableItem.empty(new ItemBuilder(Material.HOPPER)
                    .name("§eДобавить серверное место")
                    .addLore("§cДля вышей группы")
                    .addLore("§cне предусмотрена")
                    .addLore("§сустановка мест.")
                    .build()
                ));
                
            } else if (count>=limit) {
                
                contents.set( 5, 2,  ClickableItem.empty(new ItemBuilder(Material.HOPPER)
                    .name("§eДобавить серверное место")
                    .addLore("§cВы уже создали мест: "+count)
                    .addLore("§cЛимит вашей группы: "+limit)
                    .addLore("§сДобавить невозможно.")
                    .build()
                ));
                
            } else {*/
            final int count_ = count;
            int  limit =  Perm.getLimit(PM.getOplayer(p), "warp.set");
            
            //if (wm.canSetPrivate) {
                
                contents.set(5, 2, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.HOPPER)
                    .name("§eДобавить место")
                    .addLore("§7")
                    .addLore("§7Лимит для вашей группы: §b"+limit)
                    .addLore("§7Вы уже создали мест: §6"+count)
                    .addLore("§7")
                    .addLore("§7Создать частное")
                    .addLore("§7место в точке, где вы стоите.")
                    .addLore("§7")
                    .addLore("§7ЛКМ - создать")
                    .addLore("§7")
                    .build(),  "название", msg -> {

                        if (limit==0) {
                            p.sendMessage("§cДля вышей группы не предусмотрена установка мест.");
                            PM.soundDeny(p);
                            return;
                        }
                        if (count_>=limit) {
                            p.sendMessage("§cВы уже создали мест: "+count_+" Лимит вашей группы: "+limit+"§сДобавить невозможно.");
                            PM.soundDeny(p);
                            return;
                        }

                        final String strip = TCUtils.stripColor(msg);

                        if(strip.length()>24 ) {
                            p.sendMessage("§cЛимит 24 символа!");
                            PM.soundDeny(p);
                            return;
                        }

                        if (WarpManager.getWarpNames().contains(strip)) {
                            p.sendMessage("§cМесто с таким названием уже есть!");
                            PM.soundDeny(p);
                            return;
                        }

                        final Warp warp = new Warp(strip, p.getName(), ApiOstrov.currentTimeSec());
                        warp.descr = "";
                        warp.setLocation (p.getLocation());

                        WarpManager.saveWarp(p, warp);

                        reopen(p, contents);
                    }));                 
                //}

            }
            
        
        
        
        
        
        
        
        
        contents.set( 5, 4,  ClickableItem.of(new ItemBuilder(hideClosed ? Material.LEVER :  Material.REDSTONE_TORCH )
            .name(hideClosed ? "§eПоказывать выключенные" : "§eСкрыть выключенные")
            .build(), e -> {
                if (e.isLeftClick()) {
                    hideClosed = !hideClosed;
                    reopen(p, contents);
                }
            }));

        contents.set( 5, 5,  ClickableItem.of(new ItemBuilder(hidePaid ? Material.LEVER :  Material.REDSTONE_TORCH)
            .name(hidePaid ? "§eПоказывать платные" : "§eСкрыть платные")
            .build(), e -> {
                if (e.isLeftClick()) {
                    hidePaid = !hidePaid;
                    reopen(p, contents);
                }
            }));

        contents.set( 5, 6,  ClickableItem.of(new ItemBuilder(hidePrivate ? Material.LEVER : Material.REDSTONE_TORCH)
            .name(hidePrivate ? "§eПоказывать с правом" : "§eСкрыть с правом")
            .build(), e -> {
                if (e.isLeftClick()) {
                    hidePrivate = !hidePrivate;
                    reopen(p, contents);
                }
            }));
            
            
            
                
        
                
           /*     
            case 2:
                if (Warps.Warp_exist(a[0])) {
                    if ( ApiOstrov.isLocalBuilder(p, true) || Warps.Get_owner(a[0]).equals(p.getName()) ) {
                       /* switch (a[1]) {
                            case "on":
                            case "off":
                                boolean on = true;
                                if (a[1].equals("off")) on = false;
                                
                                Warps.Set_open (p, a[0], on);
                                if (on) p.sendMessage( "§aВы открыли доступ к варпу!" );
                                else p.sendMessage( "§4Вы закрыли доступ к варпу!" );
                                return true;
                            case "del":
                                Warps.Del_warp(p, a[0]);
                                p.sendMessage( "§aВы удалили варп "+a[0] );
                                return true;
                            default:
                                p.sendMessage( "§con - открыт, off - заблокировать." );
                                break;
                        }
                    } else p.sendMessage( "§cВы не владелец данного варпа!" );
                } else p.sendMessage( "§cТакого варпа не существует!" );
                break;
              
                
                
                
           /* case 3:
                if ( ApiOstrov.isLocalBuilder(p, true) || p.hasPermission("ostrov.setswarp")) {
                    if (Warps.Warp_exist(a[0])) {
                        if ( Warps.Get_type(a[0]).equals("server") ) {
                            if (a[1].equals("cost") ) {
                                int cost = 0;
                                if ( CMD.isNumber(a[2]) ) cost = Integer.valueOf( a[2]);
                                if (cost <0 || cost > 100000) {  p.sendMessage( "§cЦена от 0 до 100000" ); return false; }

                                Warps.Set_cost (p, a[0], cost);
                                p.sendMessage( "§aДля варпа "+a[0]+" Вы установили плату за посещение "+cost );

                            } else p.sendMessage( "§c/warp <название> cost сумма - установить плату за посещение!" );
                        } else p.sendMessage( "§cЦену можно установить только для серверных варпов!" );
                    } else p.sendMessage( "§cТакого варпа не существует!" );
                } else p.sendMessage("§cУ Вас нет права управлять серверными варпами!");
                break;*/        
        
        
        
        
        
        
        

        
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(21);
        
        contents.set( 5, 7, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("закрыть").build(), e -> 
           p.closeInventory()
        ));
        

        
        if (!pagination.isLast()) {
            contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> contents.getHost().open(p, pagination.next().getPage()) )
            );
        }

        if (!pagination.isFirst()) {
            contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> contents.getHost().open(p, pagination.previous().getPage()) )
            );
        }
        
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));
        

        
        

    }
    
    
    
    
    
    
    
    
    
    
}
