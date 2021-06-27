package ru.komiss77.ProfileMenu;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Managers.PM;
import ru.komiss77.Objects.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.OstrovDB;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;



public class PassportHandler__ implements Listener {

    public static String pass_prefix="§aПаспорт "; 
    private static ItemStack passport;
    
    public static void givePassport(final Player p, final int slot) {
       /* passport = new ItemBuilder(Material.PAPER)
                .setName(PassportHandler.pass_prefix+p.getName())
                .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                .addFlags(ItemFlag.HIDE_ENCHANTS)
                .addFlags(ItemFlag.HIDE_UNBREAKABLE)
                .setUnbreakable(true)
                .setLore(ItemUtils.Gen_lore(null, "Держите паспорт в руке,<br>и окружающие смогут его<br>посмотреть, сделав правый<br>клик на Вас.<br>Вы всегда можете<br>достать документ из кармана,<br>набрав §b/passport get<br>Изменить паспортные данные<br>можно в профиле.", "§7"))
                .build();*/
        //passport.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
        if (slot>0) p.getInventory().setItem(slot,passport.clone());
        else p.getInventory().setItemInMainHand(passport.clone());
    }

    public PassportHandler__(final Ostrov plugin) {
        passport = new ItemBuilder(Material.PAPER)
            .setName(PassportHandler__.pass_prefix)
            .addFlags(ItemFlag.HIDE_ATTRIBUTES)
            .addFlags(ItemFlag.HIDE_ENCHANTS)
            .addFlags(ItemFlag.HIDE_UNBREAKABLE)
            .setUnbreakable(true)
            .setLore(ItemUtils.Gen_lore(null, "Держите паспорт в руке,<br>и окружающие смогут его<br>посмотреть, сделав правый<br>клик на Вас.<br>Вы всегда можете<br>достать документ из кармана,<br>набрав §b/passport get<br>Изменить паспортные данные<br>можно в профиле.", "§7"))
            .build();
    }
    
    
    
    
    @EventHandler( priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
//System.out.println("ru.komiss77.Listener.MenuListener.onInteract() "+e.getItem());        
        if ( e.getAction()!=Action.PHYSICAL && ItemUtils.compareItem(e.getItem(), passport, true)) {
        //if (  e.getPlayer().getInventory().getItemInMainHand()!=null &&
               // e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() &&
           //     e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
             //   e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().startsWith(pass_prefix)) {
                e.setUseItemInHand(Event.Result.DENY);
                if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK ) {
                    //PassportHandler.showPasport(e.getPlayer(), e.getPlayer().getName());
                    //PassportHandler.showLocal(e.getPlayer(),e.getPlayer().getName());
                    createBook(e.getPlayer(), PM.getOplayer(e.getPlayer()).getPassportData());
                } 
        }
    }
    
    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onInteractEntity(PlayerInteractEntityEvent e) {   
        if ( e.getRightClicked().getType()==EntityType.PLAYER && PM.exist(e.getRightClicked().getName()) ) {
            final Player target=(Player) e.getRightClicked();
            if  (ItemUtils.compareItem(target.getInventory().getItemInMainHand(), passport, true) || ItemUtils.compareItem(target.getInventory().getItemInOffHand(), passport, true)) {
                //if (target.getInventory().getItemInMainHand()!=null &&
               // target.getInventory().getItemInMainHand().hasItemMeta() &&
               // target.getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
               // target.getInventory().getItemInMainHand().getItemMeta().getDisplayName().startsWith(pass_prefix)) {
                e.setCancelled(true);
                //PassportHandler.showPasport(e.getPlayer(), target.getName());
                //PassportHandler.showLocal(e.getPlayer(),target.getName());
                createBook(e.getPlayer(), PM.getOplayer(target).getPassportData());
            }
        }
    }
    

    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if ( ItemUtils.compareItem(e.getItemDrop().getItemStack(), passport, true)) {
                //event.setCancelled(true);
                e.getItemDrop().remove();
                e.getPlayer().updateInventory();
        }
    } 
    
    /*
@EventHandler( priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void openInv(InventoryOpenEvent e) {
//System.out.println("ru.komiss77.Listener.MenuListener.InventoryOpenEvent() "+e.getPlayer().getInventory().getItemInMainHand());        
        if (  e.getPlayer().getInventory().getItemInMainHand().getType()!=Material.AIR &&
                e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() &&
                e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().startsWith(pass_prefix)) {
                e.getPlayer().getInventory().setItemInMainHand(null);
                //e.getPlayer().up
        }
    }*/
    
    @EventHandler( priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void clickInv(InventoryClickEvent e) {
//System.out.println("ru.komiss77.Listener.MenuListener.InventoryOpenEvent() "+e.getPlayer().getInventory().getItemInMainHand());        
        if ( ItemUtils.compareItem(e.getCurrentItem(), passport, true)) {
            e.setCurrentItem(null);
            ((Player)e.getWhoClicked()).updateInventory();
                //e.getPlayer().up
        }
    }
  /*   
@EventHandler( priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void swap(PlayerSwapHandItemsEvent e) {
//System.out.println("ru.komiss77.Listener.MenuListener.onInteract()");        
        if (  e.getPlayer().getInventory().getItemInMainHand()!=null &&
                e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() &&
                e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().startsWith(pass_prefix)) {
                e.getPlayer().getInventory().setItemInMainHand(null);
                //e.getPlayer().up
        }
    }
   
@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        if (  e.getPlayer().getInventory().getItemInMainHand().getType()!=Material.AIR &&
                e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() &&
                e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasDisplayName() &&
                e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().startsWith(pass_prefix)) {
                e.getPlayer().getInventory().setItemInMainHand(null);
                //e.getPlayer().up
        }
    }    
  */  
    
//@EventHandler(priority = EventPriority.MONITOR) 
//    public void PlayerQuit(PlayerQuitEvent e) {
        
//    }
  
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //public static void showPasport (final Player owner, final Player target) {
     //   if (Bukkit.getPlayer(name)!=null) {
     //       PassportHandler.showLocal(owner,name);
     //   } else {
     //       ApiOstrov.sendMessage(owner, ru.komiss77.Enums.Action.OSTROV_PASSPORT, name);
     //   }
    //}
    
    
    public static void showLocal(final Player owner, final Player target) {
        createBook(owner, PM.getOplayer(target.getName()).getPassportData());
    }

    
    public static void showGlobal(final Player player, final String bungee_raw_data) {
//System.out.println("--showPass raw="+bungee_raw_data);
       // String[]split;
        Map<E_Pass,String>pass_data=new HashMap<>();
        //for (String raw_:bungee_raw_data.split("<:>")) {
        //    split=raw_.split("<>");
       //     if (split.length==2 && Ostrov.isInteger(split[0]) && Data.byTag(Integer.valueOf(split[0]))!=null ) {
        //        pass_data.put(Data.byTag(Integer.valueOf(split[0])), split[1]);
        //    }
       // }
        createBook (player, pass_data);
        player.playSound(player.getEyeLocation(), Sound.BLOCK_SNOW_STEP, 0.5F, 2F);
    }
    
    
    
    
    
    
    
    private static void createBook (Player player, Map<E_Pass, String> pass_data) {
        
        
        ComponentBuilder page1=new ComponentBuilder("  §4§lПаспорт Островитянина\n");
        ComponentBuilder page2=new ComponentBuilder("");
        ComponentBuilder page3=new ComponentBuilder("");
        ComponentBuilder page4=new ComponentBuilder("");
        
        TextComponent text;
        String value;
        int int_value;
        
        for (E_Pass pass:pass_data.keySet()) {
            value = pass_data.get(pass);//pass.default_value;
            int_value=ApiOstrov.getInteger(value);
                //if (pass_data.containsKey(Data.fromName(pass.toString()))) {
                //    value=pass_data.get(Data.fromName(pass.toString()));
                //}
                //if (Ostrov.isInteger(value)) int_value = Integer.valueOf(value);
                
                
                switch (pass) {
                    
                    case USER_GROUPS:
                        final String[] groups = value.split(",");
                        value = "";
                        for (String gr_:groups) {
                            if (OstrovDB.groups.containsKey(gr_) ) {
                                value=value.replace(gr_, " §1"+OstrovDB.groups.get(gr_).chat_name);
                            }
                        }                        
                        break;
                        
                    case SIENCE: 
                        value = ApiOstrov.dateFromStamp(int_value);
                        break;
                        
                    case PLAY_TIME:
                        value = ApiOstrov.secondToTime(int_value);// + "\n §3("+ApiOstrov.secondToTime(op.);
                        break;
                        
                    case REPUTATION:
                        //int_value = int_value + (pass_data.containsKey(Data.РЕПУТАЦИЯ_БАЗА) ? Integer.valueOf(pass_data.get(Data.РЕПУТАЦИЯ_БАЗА)): 0);
                        value = (int_value<0?"§4":(int_value>0?"§2":"§1"))+int_value;
                        break;
                        
                    case KARMA:
                        value = (int_value<0?"§4":(int_value>0?"§2":"§1"))+int_value;
                        break;
                        
                    case BIRTH:
                        if (value.length()==10 && Ostrov.isInteger(value.substring(6, 10))) {
                            value = value+" ("+(Calendar.getInstance().get(Calendar.YEAR) - Integer.valueOf(value.substring(6, 10)))+")";
                        }
                        break;
                        
                    case IPPROTECT:
                        value = int_value==0 ? "§5Нет" : "§bДа";
                        break;
                }
            
                
                if (pass.slot<=8) {
                    
                    page1.append(new ComponentBuilder("§6"+pass.item_name+"\n §1"+value+"\n").create());
                    //text= new TextComponent("§6"+pass.item_name+"\n §1"+value+"\n");
                    //page1.addExtra(text);
                    
                } else if (pass.slot>=9 && pass.slot<=17) {
                    
                    page2.append(new ComponentBuilder("§6"+pass.item_name+"\n §1"+value+"\n").create());
                    //text= new TextComponent("§6"+pass.item_name+"\n §1"+value+"\n");
                    //page2.addExtra(text);
                    
                } else if (pass.slot>=18 && pass.slot<=26) {
                    
                    page3.append(new ComponentBuilder("§6"+pass.item_name+"\n §1"+value+"\n").create());
                    //text= new TextComponent("§6"+pass.item_name+"\n §1"+value+"\n");
                    //page3.addExtra(text);
                    
                } else if (pass.slot>=27 && pass.slot<=35) {
                    
                    switch (pass) {
                        
                        case DISCORD: 
                        case PHONE:
                            page4.append(new ComponentBuilder("§6"+pass.item_name+": §1"+value.replaceAll(" ", " §1")+"\n").create());
                            //text= new TextComponent("§6"+pass.item_name+": §1"+value.replaceAll(" ", " §1")+"\n");
                            break;
                            
                        case EMAIL: 
                            page4.append(new ComponentBuilder("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1")+"\n").create());
                            //text= new TextComponent("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1")+"\n");
                            break;
                            
                        case ABOUT: 
                            page4.append(new ComponentBuilder("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1")).create());
                            //text= new TextComponent("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1"));
                            break;
                            
                        default:
                            if (value.equals("не указано")) {
                                text= new TextComponent("§6"+pass.item_name+": §1"+value+"\n");
                            } else {
                                page4.append( new ComponentBuilder("§6"+pass.item_name+": §1§nссылка (клик)\n" )
                                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Клик-открыть")))
                                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, value))
                                        .create());
                            }
                            break;
                    }
                   /* if (pass==E_Pass.СКАЙП)   text= new TextComponent("§6"+pass.item_name+": §1"+value.replaceAll(" ", " §1")+"\n");
                    else if (pass==E_Pass.МЫЛО)   text= new TextComponent("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1")+"\n");
                    else if (pass==E_Pass.О_СЕБЕ)   text= new TextComponent("§6"+pass.item_name+"\n §1"+value.replaceAll(" ", " §1"));
                    else if (pass==E_Pass.ТЕЛЕФОН || value.equals("не указано")) text= new TextComponent("§6"+pass.item_name+": §1"+value+"\n");
                    else {
                        text= new TextComponent("§6"+pass.item_name+": §1§nссылка (клик)\n");
                        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Клик-открыть").create()));
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, value));
                    }*/
                    
                  //  page4.addExtra(text);
                }
            
        }
        
         
        final ItemStack book = new ItemBuilder(Material.WRITTEN_BOOK)
                .name("Паспорт Островитянина")
                .build();
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        
        bookMeta.spigot().addPage(page1.create());
        bookMeta.spigot().addPage(page2.create());
        bookMeta.spigot().addPage(page3.create());
        bookMeta.spigot().addPage(page4.create());
        
        //List <TextComponent> pagesList = new ArrayList<>();
       // pagesList.add(page1);
       // pagesList.add(page2);
       // pagesList.add(page3);
       // pagesList.add(page4);
        
        //bookMeta = VM.getNmsServer().addPages(bookMeta, pagesList);
        //bookMeta.spigot().addPage(page1);
        
        /*
        List<IChatBaseComponent> pages;
        try {
            pages = (List<IChatBaseComponent>) CraftMetaBook.class.getDeclaredField("pages").get(bookMeta);
        } catch (ReflectiveOperationException ex) {
            Ostrov.log_err("создание паспорта : "+ex.getMessage());
            return;
        }
        //create a page
        //TextComponent text = new TextComponent("Click me");
        //text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://spigotmc.org"));
        //text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Goto the spigot website!").create()));

        //add the page to the list of pages
        //IChatBaseComponent ibc_page = ChatSerializer.a(ComponentSerializer.toString(text));
        //pages.add(ibc_page);
        IChatBaseComponent ibc_page = ChatSerializer.a(ComponentSerializer.toString(page1));
        pages.add(ibc_page);
        ibc_page = ChatSerializer.a(ComponentSerializer.toString(page2));
        pages.add(ibc_page);
        ibc_page = ChatSerializer.a(ComponentSerializer.toString(page3));
        pages.add(ibc_page);
        ibc_page = ChatSerializer.a(ComponentSerializer.toString(page4));
        pages.add(ibc_page);*/

        bookMeta.setTitle("Паспорт");
        bookMeta.setAuthor("Остров77");

        book.setItemMeta(bookMeta);  
        
        player.openBook(book);
        //open(player,book );
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   //  public static void open(Player p, ItemStack book) {
//System.out.println("--openBook ");
   //      p.openBook(book); //from 1.14 https://www.spigotmc.org/threads/open-book-to-a-player-in-1-14.371118/
        /*final int slot = p.getInventory().getHeldItemSlot();
        final org.bukkit.inventory.ItemStack old = p.getInventory().getItem(slot);
        
        p.getInventory().setItem(slot, book); //+1.14.1
        
        final ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, (byte)0);
        buf.writerIndex(1);
        
        try {
            p.getInventory().setItem(slot, book);
            //PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(new PacketPlayOutOpenBook(EnumHand.MAIN_HAND));
            //final PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf)); 1.12
            (((CraftPlayer) p).getHandle()).playerConnection.sendPacket(new PacketPlayOutOpenBook(EnumHand.MAIN_HAND));
            //(((CraftPlayer) p).getHandle()).playerConnection.sendPacket(packet); 1.12
        } catch(Exception ex) {
            Ostrov.log_err("Passport open: "+ex.getMessage());
        } finally { 
            p.getInventory().setItem(slot, old);
        }*/
   // }    

     
     
     
     
     
     
}
