package ru.komiss77.ProfileMenu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.Objects.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;






public class statHandler {

    
    
    
    
    static void injectStatItems(final Oplayer op) {
        //op.profile.setItem(22, ItemUtils.profile_empty.clone());
        
        ItemStack stat_item;
        List <String> lore = new ArrayList<>();
        //ItemMeta meta;
        
        for (E_Stat_menu stat_icon : E_Stat_menu.values()) {
            lore.clear();
            //stat_item = new ItemBuilder(Material.).;
            //stat_item = new ItemBuilder(stat_icon.mat).setName(stat_icon.game_name).build();
            //stat_item = new ItemStack(stat_icon.mat);
            //if (stat_item==null) continue;
            //meta = stat_item.getItemMeta();
            //meta.setDisplayName(stat_icon.game_name);
            
            for (E_Stat stat : E_Stat.values()) {
                if (stat.toString().startsWith(stat_icon.toString()+"_")) {
//System.out.println("- stat="+stat.toString()+" len="+op.getStat(stat).length()+" value="+op.getStat(stat));                   
                    if (op.getStat(stat).length()>20) {
                        lore = ItemUtils.Gen_lore(lore, stat.as_string+"§7"+op.getStat(stat), "§7");
                    } else {
                        lore.add(stat.as_string+op.getStat(stat));
                    }
                }
            }
            
            //meta.setLore(lore);
            //stat_item.setItemMeta(meta);
            stat_item = new ItemBuilder(stat_icon.mat).setName(stat_icon.game_name).setLore(lore).build();
            
            op.profile.setItem(stat_icon.slot, stat_item);
        }
        op.getPlayer().updateInventory();
        
        
    }

    
    static void injectAchivItems(final Oplayer op, int page) {
        //потом сделать постранично
        mainHandler.cleanField(op.profile);
        if (page<0) page=0;
        
        int current=0;
        int begin=page*36;
        int out=36;
        mainHandler.setcolorGlassLine(op.profile, E_Prof.СТАТИСТИКА.mat);
        if (page>0) op.profile.setItem(36, ItemUtils.Set_name(ItemUtils.previos_page.clone(), "стр. "+(page)) );
        
        boolean next_page=false;
        
        int level;
        //short color1;
        Material mat;
        
        for (E_Stat e_stat:E_Stat.values()) {
            if (!e_stat.is_achiv) continue;
            
            if (current>=begin) {
                level = 0;
                if (op.achiv.contains(e_stat.tag*10+5)) {level=5; mat=Material.DIAMOND_HELMET;}//color=14;}
                else if (op.achiv.contains(e_stat.tag*10+4)) {level=4; mat=Material.GOLDEN_HELMET;}//color=6;}
                else if (op.achiv.contains(e_stat.tag*10+3)) {level=3; mat=Material.IRON_HELMET;}//color=10;}
                else if (op.achiv.contains(e_stat.tag*10+2)) {level=2; mat=Material.CHAINMAIL_HELMET;}//color=12;}
                else if (op.achiv.contains(e_stat.tag*10+1)) {level=1; mat=Material.TURTLE_HELMET;}//color=9;}
                else {level=0; mat=Material.LEATHER_HELMET;}//color=8;}
                
                
                op.profile.addItem(
                    new ItemBuilder(mat)//Material.INK_SAC, color)
                    .setName(E_Stat.gameNameFromStat(e_stat)+" : "+e_stat.as_string.replaceFirst(":", ""))
                    .setLore(ItemUtils.Gen_lore(null, "Набрано : "+op.getStat(e_stat)+"<br>"+(level>0 ? "Уровень : §f"+level : "§5Пока нечем гордиться"), "§7"))
                    .build()
                );
                
                out--;
                if (out==0) {
                    next_page=true;
                    break;
                }
            }
            current++;
        }
        if(next_page) op.profile.setItem(44, ItemUtils.Set_name(ItemUtils.next_page.clone(), "стр. "+(page+2)) );
        op.getPlayer().updateInventory();
        /*
        ItemStack achiv_item=new ItemStack;
        List <String> lore = new ArrayList<>();
        ItemMeta meta;
        
        for (E_Stat_menu stat_icon : E_Stat_menu.values()) {
            lore.clear();
            //stat_item = new ItemBuilder(Material.).;
            stat_item = ItemUtils.getItemStackFromString(stat_icon.mat);
            if (stat_icon==null) continue;
            meta = stat_item.getItemMeta();
            meta.setDisplayName(stat_icon.game_name);
            
            for (E_Stat stat : E_Stat.values()) {
                if (stat.toString().startsWith(stat_icon.toString()+"_")) lore.add(stat.as_string+op.getStat(stat));
            }
            
            meta.setLore(lore);
            stat_item.setItemMeta(meta);
            
        }*/
        
        
    }

    
    
    
    
    
    
    
    
    
    
    public static void onFieldClick(final InventoryClickEvent e, final Player p, final Oplayer op) {
        if ( e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && (e.getSlot()==36||e.getSlot()==44)) {
            String page_string=ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).replaceFirst("стр. ", "");
System.out.println("statHandler.onFieldClick() page_string="+page_string);            
            try {
                int page=Integer.parseInt(page_string);
                    if (e.getSlot()==36) {
                        injectAchivItems(op, page-1);
                    } else if (e.getSlot()==44) {
                        injectAchivItems(op, page-1);
                    }
               
            } catch (NumberFormatException ex) {
                Ostrov.log_err("statHandler.onFieldClick() - не расчитать номер страницы "+ex.getMessage());
            }
        }
        
     }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}



