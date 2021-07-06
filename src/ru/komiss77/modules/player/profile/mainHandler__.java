package ru.komiss77.modules.player.profile;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Operation;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;




public class mainHandler__ {
    
    
    
    public static void invClick(final InventoryClickEvent e) {
//System.out.println("mainHandler invClick");
    final Player p=(Player) e.getWhoClicked();
    final Oplayer op = PM.getOplayer(p.getName());

    if (Section.isProfileIcon(e.getSlot())) { //если кликают по нижним иконкам
        Section new_prof=Section.profileBySlot(e.getSlot());
    
        setcolorGlassLine(e.getInventory(), new_prof.mat); //при каждом нажатии восстанавливает полоску
       // op.e_profile=new_prof;
       // cleanField(op.profile);
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 0.7F, 2);
    
    
            switch (new_prof) {

                case СТАТИСТИКА:
                    if (e.isShiftClick()) {
                        p.closeInventory();
                        ApiOstrov.sendMessage(p, Operation.EXECUTE_BUNGEE_CMD, p.getName(), "journal");
                    } else if (e.isLeftClick()) {
                       // StatHandler.injectStatItems(op);
                    } else if (e.isRightClick()) {
                        //StatHandler.injectAchivItems(op,0);
                    }
                    break;

               // case ЗАДАНИЯ:
                  //  op.profile.setItem(22, ItemUtils.profile_empty.clone());
               //     break;

                case ДРУЗЬЯ:
                  //  if (Ostrov.api_friends==null) op.profile.setItem(22, ItemUtils.profile_deny.clone());
                   // else Ostrov.api_friends.onFriendProfileIconClick(p, e);
                    return;

                case КОМАНДА:
                 //   if (Ostrov.api_friends==null) op.profile.setItem(22, ItemUtils.profile_deny.clone());
                  //  else Ostrov.api_friends.onPartyProfileIconClick(p, e);
                    break;

                case СЕРВЕР:
                    if (e.isLeftClick()) {
                        //p.performCommand("serv");
                        p.openInventory(GM.main_inv);
                    } else if (e.isRightClick()) {
                        p.performCommand("menu");
                    }                    
                    return;
/*
                case ПОМОЩЬ:
                    if (e.isShiftClick()) {
                        p.closeInventory();
                        ApiOstrov.sendMessage(p, Operation.EXECUTE_BUNGEE_CMD, p.getName(), "staff list");
                    } else if (e.isLeftClick()) {
                        p.closeInventory();
                        ApiOstrov.sendMessage(p, Operation.EXECUTE_BUNGEE_CMD, p.getName(), "help");
                    } else if (e.isRightClick()) {
                        p.closeInventory();
                        p.performCommand("ohelp");
                    }
                    ApiOstrov.sendTitle(p, "§fДля набора команды -", "§fклик на нужную в чате.");
                    break;

                case ДОНАТ:
                    if (e.isShiftClick()) {
                        //----------------------------------
                    } else if (e.isLeftClick()) {
                        OstrovDB.groups.values().stream().forEach((group) -> {
                            Material mat;
                            if (group.type.equals("donat") ) {
                                mat=Material.matchMaterial(group.mat);
                                if (mat!=null && mat!=Material.AIR) {
                                    ItemStack is=new ItemBuilder(mat).setName(group.chat_name).setLore(ItemUtils.Gen_lore(null, group.group_desc, "§b")) .build();
                                    is.addUnsafeEnchantment(Enchantment.LUCK, 1);
                                //    op.profile.setItem(group.inv_slot, is);
                                } else {
                                    Ostrov.log_err("Profile.mainHandler группа "+group.chat_name+", не найден материал "+group.mat);
                                }
                            }
                        });
                    } else if (e.isRightClick()) {
                        p.closeInventory();
                        ApiOstrov.sendTitle(p, "§fДля пополнения баланса", "§fклик на сообщения в чате.");
                        ApiOstrov.sendMessage(p, Operation.EXECUTE_BUNGEE_CMD, p.getName(), "money add");
                    }
                    return;

                case НАСТРОЙКИ:
                    if (e.isShiftClick()) {
                        //----------------------------------
                    } else if (e.isLeftClick()) {
                        if (!CMD.settings_command) {
                         //   op.profile.setItem(22, ItemUtils.profile_deny.clone());
                            return;
                        } else {
                            //p.performCommand("settings");
                            p.openInventory(PM.OP_Get_settings(p));
                        }
                    } else if (e.isRightClick()) {
                        settingsHandler__.injectPassportItems(p, op,false);
                    }
                    return;
*/
               // case ЗАКРЫТЬ:
             //       p.closeInventory();
             //       return;

            }
         //   p.updateInventory();
            //return;
            
            
            
            
            
            
        } else {                    //кликают на поле выше полосы
           // p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 2);
            /*
            switch (op.e_profile) {     //какой сейчас выбран раздел-узнаем из Оплаера

                case ДРУЗЬЯ: 
                    Ostrov.api_friends.onFriendFieldClick(p, e); 
                    return;

                case КОМАНДА: 
                    Ostrov.api_friends.onPartyFieldClick(p, e); 
                    return;

                case ДОНАТ: 
                    final Group group = OstrovDB.groupByItemName(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));
                    if (group==null) return;
                    p.closeInventory();
                    ApiOstrov.sendTitle(p, "§fДля покупки группы", "§fклик на сообщения в чате.");
                    ApiOstrov.sendMessage(p, Operation.EXECUTE_BUNGEE_CMD, p.getName(), "group buy "+p.getName()+" "+group.chat_name);
                    return;

                case СТАТИСТИКА:
                    StatHandler.onFieldClick(e, p, op);
                    return;

                case НАСТРОЙКИ:
                    settingsHandler__.onFieldClick(e, p, op);
                    break;



            }
        
        */
        
        
        
        }
    
    }
    
    
    
    
    
    
    
    
    
    
    public static void cleanField(Inventory inv) {
        for (int i=0;i<=35;i++) {
            inv.setItem(i, null);
        }
    }
    
    
    public static void setcolorGlassLine(Inventory to_set, final Material mat) {
        //ItemStack is=new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) color);
        final ItemStack is = new ItemStack(mat,1);//ItemUtils.changeColor(new ItemStack (Material.WHITE_STAINED_GLASS_PANE, 1), color);
        for (int i=36; i<=44; i++) {
            to_set.setItem(i, is);
        }
    }
    

    
    
    
    
    
    
    
    
    
    
    public enum Selection {
        ПРОСМОТР_ПАСПОРТА, ПРОСМОТР_СТАТИСТИКИ, 
        ;
        
        
        public static Selection fromString(final String name) {
            for (Selection current:Selection.values()) {
                if (current.toString().equals(name)) return Selection.valueOf(name);
            }
        return null;
        }

    }    
    
    
    
    
    
}
