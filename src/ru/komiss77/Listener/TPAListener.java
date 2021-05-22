
package ru.komiss77.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Commands.CMD;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ChatMsgUtil;



public class TPAListener implements Listener {
    
    private static HashMap <String,ArrayList<String>> banned = new HashMap<>();
    private static HashMap <String, String> request = new HashMap<>();          //цель, отправитель
    
    private static String tpgui_name;
    private static String tpgui_req;
    private static String tpgui_unban;
    private static String tpgui_disconn;
    private static String tpgui_cant_rec;
    private static String tpgui_ban_msg;
    private static String tpgui_ban_msg2;
    private static String tpgui_banlist_e;
    private static String tpgui_banlist_rem;
    private static String tpgui_exit;
    private static String tpgui_aacept;
    private static String tpgui_deny;
    private static String tpgui_ban;


public static void Init () {
    
    banned = new HashMap<>();
    request = new HashMap<>();
    
    tpgui_name = "Телепортер";//Cfg.GetMsg("tpgui.name");
    tpgui_req = "§2Запрос на ТП";//Cfg.GetMsg("tpgui.req");
    tpgui_unban = "Разбан";//Cfg.GetMsg("tpgui.unban");
    tpgui_disconn = "§c %n отключился!";//Cfg.GetMsg("tpgui.disconn");
    tpgui_cant_rec = "§c %n не может принять ваш запрос! Попробуйте позже!";//Cfg.GetMsg("tpgui.cant_rec");
    tpgui_ban_msg = "§cВы забанили %n! Для разбана наберите §6/tpa unban";//Cfg.GetMsg("tpgui.ban_msg_sender");
    tpgui_ban_msg2 = "§cВы больше не сможете ТП к %n! Он Вас забанил!";//Cfg.GetMsg("tpgui.ban_msg_reciever");
    tpgui_banlist_e = "§cВаш список забаненных пуст!";//Cfg.GetMsg("tpgui.tpgui_banlist_empty");
    tpgui_banlist_rem = "§a%n удалил Вас из бан-листа!";//Cfg.GetMsg("tpgui.tpgui_banlist_rem");
    tpgui_exit = "§cвыход";//Cfg.GetMsg("tpgui.exit");
    tpgui_aacept = "§cПринять";//Cfg.GetMsg("tpgui.aacept");
    tpgui_deny = "§cОтказать";//Cfg.GetMsg("tpgui.deny");
    tpgui_ban = "§cЗаблокировать";//Cfg.GetMsg("tpgui.ban");

}    
    
public static void ReloadVars () {
    Init ();
}

    
 public static boolean Tp_accept (Player confirm) {
     if (request.containsKey(confirm.getName())) {
             OpenConfirm(confirm, request.get(confirm.getName()) );
         request.remove(confirm.getName());
         return true;
     } else {
         confirm.sendMessage("§cЗапрос устарел");
         return false;
     }
 }    
    

    

    
    
 @EventHandler(ignoreCancelled = true)
    public static void onClick(InventoryClickEvent e) {

        if (CMD.tpa_command <1) return;
        if(!(e.getWhoClicked() instanceof Player)) return;
        if(e.getCurrentItem() == null) return;
        if (e.getInventory().getType()!=InventoryType.CHEST) return;
        
        //if (e.getView().getTitle().contains(tpgui_name)) {
        if (e.getView().getTitle().contains(tpgui_name)) {
            e.setCancelled(true);
            boolean moder = e.getView().getTitle().startsWith("§4");
            ItemStack click = e.getCurrentItem();
            Player p = (Player) e.getWhoClicked(); 

            if (click == null || !click.hasItemMeta() || !click.getItemMeta().hasDisplayName())  return;
            
            int page = Integer.parseInt( (e.getView().getTitle().split(" "))[2]);

            
            if (click.getType()==Material.RED_MUSHROOM) {
                p.closeInventory();
                
            } else  if (ChatColor.stripColor(click.getItemMeta().getDisplayName()).equals(">>>>")) { //следующ стр
                    openTPmenu(p, page+1, moder);
                    
                } else if (ChatColor.stripColor(click.getItemMeta().getDisplayName()).equals("<<<<")) { //предыд стр
                    openTPmenu(p, page-1, moder);
                    
                } else if (click.getType().equals(Material.PLAYER_HEAD) ) {
                    
                    Player target = Bukkit.getPlayer(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()) );
                    

                    if (target==null) {
                        p.closeInventory();
                        p.sendMessage("§с"+ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName())+" не найден.");
                        openTPmenu(p, page, moder);
                        //return;
                    } else {
                        p.closeInventory();
                            if (moder) {
                                ApiOstrov.teleportSave(p, target.getLocation());
                            } else {

                                p.sendMessage("§a"+target.getName()+" отправлен запрос на телепорт. Оплата после выполнения.");
                                
                                //ChatMsgUtil.Send_TextComponent_onclick_run(target,  "§f§k111§f Запрос на телепорт от §a"+p.getName()+"§f <- Клик на сообщение, чтобы открыть меню §k111", "", "/tpaccept");
                                 TextComponent temp = new TextComponent("§f§k111§f Запрос на телепорт от §a"+p.getName()+"§f <- Клик на сообщение, чтобы открыть меню §k111" );
                                //temp.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5Клик, чтобы удалить") ) );
                                temp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept") );
                                p.spigot().sendMessage( temp);
                                request.put( target.getName(), p.getName() );
                            }  
                    }
                        
                }

                
                
                
        }  else  if (e.getView().getTitle().startsWith(tpgui_req)) {
            
            Player p = (Player) e.getWhoClicked(); 
            ItemStack click = e.getCurrentItem();
            e.setCancelled(true);

            if (click == null || !click.hasItemMeta() || !click.getItemMeta().hasDisplayName())  return;

            String nik =  (e.getView().getTitle().replaceAll(tpgui_req, ""));
            
            Player sender = Bukkit.getPlayer(nik);
            
                if ( null!=click.getType() ) switch (click.getType()) {
                    
                case EMERALD_BLOCK:  //принять
                    if (sender==null) {
                        p.sendMessage(tpgui_disconn.replaceAll("%n", nik) );
                        p.closeInventory();
                    } else {
                        p.closeInventory();
                                
                                int price=getPrice(sender, p.getLocation());
//System.out.println("price="+price);
                                if (price>0) {
                                    if (ApiOstrov.moneyGetBalance(sender.getName())<price) {
                                        sender.sendMessage("§cУ Вас недостаточно лони для телепорта!");
                                        p.sendMessage("§cУ "+nik+" недостаточно лони для телепорта!");
                                        return;
                                    }
                                    ApiOstrov.moneyChange(sender, -price, "телепорт к "+p.getName());
                                }
                                
                        ApiOstrov.teleportSave(sender, p.getLocation());
                        //sender.teleport(p.getLocation());
                    }
                    break;
                    
                case REDSTONE_BLOCK:    //отказ
                    if (sender!=null) {
                        sender.sendMessage(tpgui_cant_rec.replaceAll("%n", p.getName()) );
                        p.closeInventory();
                    }
                    break;
                    
                case BLAZE_POWDER:   //бан
                    p.closeInventory();
                    p.sendMessage(tpgui_ban_msg.replaceAll("%n", nik) );
                    if (sender.isOnline())   sender.sendMessage( tpgui_ban_msg2.replaceAll("%n", p.getName()) );
                    if (banned.containsKey(p.getName())) banned.get(p.getName()).add(nik);
                    else {
                        banned.put( p.getName(), new ArrayList<>());
                        banned.get(p.getName()).add(nik);
                    }
                    break;
                    
                default:   
                    break;
                    
            }   
            
                
        } else  if (e.getView().getTitle().equals(tpgui_unban)) {
            
            Player p = (Player) e.getWhoClicked(); 
            ItemStack click = e.getCurrentItem();
            e.setCancelled(true);

            if (click == null || !click.hasItemMeta() || !click.getItemMeta().hasDisplayName())  return;

             if (click.getType()==Material.RED_MUSHROOM) p.closeInventory();
             
             String unban =  ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
            
            if (banned.containsKey(p.getName())) {
                ArrayList banlist = GetBanList(p.getName());
                if (banlist.contains(unban)) banlist.remove(unban);
                if (banlist.isEmpty()) {
                    banned.remove(p.getName());
                    p.closeInventory();
                    p.sendMessage(tpgui_banlist_e);
                } else {
                    banned.put(p.getName(), banlist);
                    OpenUnban(p);
                }
                Player u = Bukkit.getPlayer(unban);
                if (u!=null) u.sendMessage( tpgui_banlist_rem.replaceAll("%n", p.getName()) );
            }
            
        }

    }
       
    private static ArrayList<String> GetBanList ( String name) {
           return banned.get(name);
    }

   
    
    
    private static int getPrice (final Player p, final Location loc) {
        if (p.hasPermission("ostrov.tpa.free")) return 0;
        //учесть разные миры   Cannot measure distance between world and world_the_end
        if (!p.getWorld().getName().equals(loc.getWorld().getName())) return 100;
        
        return 50;
        
       /* double distance = Math.pow( p.getLocation().getBlockX()-loc.getBlockX(), 2) +
                        Math.pow( p.getLocation().getBlockY()- loc.getBlockY(), 2) +
                            Math.pow( p.getLocation().getBlockZ()- loc.getBlockZ(), 2) ;
        
        
        return (int) distance/100;*/
        //return (int) (distance<1000 ? distance/100 : distance<10000 ? distance/1000: distance/10000);
        //return (int) distance;
    }
    
    
    
    
    
    
    
    
    public static void openTPmenu(Player player, int page, boolean moder) {
        if (page<0) page = 0;
    //System.out.println(   " zzzzzzzzzzzzzzzzz "+friendsOnline+"    "+friendsOffline);   

        TreeSet <String>sort = new TreeSet();
            for (Player p : Bukkit.getOnlinePlayers()) {
                sort.add(p.getName());
            }
            sort.remove(player.getName());

        //Bukkit.getOnlinePlayers().stream().forEach((p) -> {
        //    sort.add(p.getName());
        //});
        List <String> all = new ArrayList();
        all.addAll(sort);

        int start = page*36;
        //int max = Bukkit.getOnlinePlayers().size()-1;
        int max = all.size();
        //if (Ostrov.v1710) max=v_1710_util.Online_player_count(); else max=V_110_util.Online_player_count();
        int end = start + 35;
        if ( end>max) end = max;
    //System.out.println("openTPmenu "+player.getName()+" page="+page+" moder="+moder+">> "+ (moder? "§4":"§2" +tpgui_name+" ( "+page+" )"));

        Inventory inv = Bukkit.createInventory( null, 45,  (moder? "§4":"§2") +tpgui_name+" ( "+page+" )" );

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta meta = skull.getItemMeta();

        int price;
        //double distance;
        for ( int n=start; n< end; n++) {
    //System.out.println(   " 111 "+n+"    "+sep);     
            if (  !moder && banned.containsKey(all.get(n)) && banned.get(all.get(n)).contains(player.getName()) ) continue;
                meta.setDisplayName("§a" + all.get(n) );
                //meta.setLore(Arrays.asList( "§5"+all.get(n) ));
                skull.setItemMeta(meta);
                SkullMeta metah = (SkullMeta) skull.getItemMeta();
                metah.setOwner(all.get(n));

                
                if (Bukkit.getPlayer(all.get(n))!=null) {
                    price=getPrice(player, Bukkit.getPlayer(all.get(n)).getLocation());
                } else {
                    price=0;
                }
                // смотрят стоимость тп к игроку, телепортируются через /tpr или бегают, и постоянно смотрят, как уменьшается стоимость телепортации к игроку и так постепенно его находят 
                if (price==0) metah.setLore(Arrays.asList("§fСтоимость телепорта: §2бесплатно"));
                else if (price<100) metah.setLore(Arrays.asList("§fСтоимость телепорта: §eне больше 100 лони"));
                else metah.setLore(Arrays.asList("§fСтоимость телепорта: §e~"+((int)price/100)+" сотни лони"));

                skull.setItemMeta(metah);
                inv.addItem(new ItemStack[] { skull});
        }

            ItemStack nav = new ItemStack(Material.RED_MUSHROOM, 1);
            ItemMeta m = nav.getItemMeta();
                m.setDisplayName(tpgui_exit);
                nav.setItemMeta(m);
                inv.setItem(40, nav);


            if (page != 0) {   //пред.стр
                nav = new ItemStack(Material.OAK_SIGN, 1);
                m = nav.getItemMeta();
                m.setDisplayName("§f<<<<");
                nav.setItemMeta(m);
                inv.setItem(36, nav);
            }

            if ( max > end ) {     //след.стр
                nav = new ItemStack(Material.OAK_SIGN, 1);
                m = nav.getItemMeta();
                m.setDisplayName("§f>>>>");
                nav.setItemMeta(m);
                inv.setItem(44, nav);
            }


        player.openInventory(inv);
    }

        
    
     

    
    private static void OpenConfirm (Player target, String sender) {

        target.closeInventory();

            Inventory confirm = Bukkit.createInventory( null, 9,  tpgui_req +sender );

            ItemStack is = new ItemStack(Material.EMERALD_BLOCK, 1);
            ItemMeta meta = (ItemMeta) is.getItemMeta();
            meta.setDisplayName(tpgui_aacept);
            is.setItemMeta(meta);
            confirm.setItem(0, is);

            is = new ItemStack(Material.REDSTONE_BLOCK, 1);
            meta = (ItemMeta) is.getItemMeta();
            meta.setDisplayName(tpgui_deny);
            is.setItemMeta(meta);
            confirm.setItem(4, is);

            is = new ItemStack(Material.BLAZE_POWDER, 1);
            meta = (ItemMeta) is.getItemMeta();
            meta.setDisplayName(tpgui_ban);
            is.setItemMeta(meta);
            confirm.setItem(8, is);

       target.openInventory(confirm);

    }
  




    public static void OpenUnban (Player p) {
                        
        Inventory unban = Bukkit.createInventory( null, 45,  tpgui_unban );
            
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta meta = skull.getItemMeta();
        
        int pos=0;
        for (String s: banned.get(p.getName())) {
                meta.setDisplayName("§a" + s );
                skull.setItemMeta(meta);
                SkullMeta metah = (SkullMeta) skull.getItemMeta();
                metah.setOwner(s);
                skull.setItemMeta(metah);
                unban.addItem(new ItemStack[] { skull});
                
                pos++;
                if (pos>44) break;
        }

        ItemStack nav = new ItemStack(Material.RED_MUSHROOM, 1);
        ItemMeta m = nav.getItemMeta();
            m.setDisplayName(tpgui_exit);
            nav.setItemMeta(m);
            unban.setItem(44, nav);
                    
               p.openInventory(unban);

    }
  





public static boolean HasBanList (String nik){
    return banned.containsKey(nik);
}


    
}
    
    
    
    
    

 
        
        
        
        
 
        
        

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   
   
   
   
   
   
   
   
   
   
   
   
   
    

    
    
    
    
