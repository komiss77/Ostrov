package ru.komiss77.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.TCUtils;


//https://emn178.github.io/online-tools/sha1_checksum.html

//не переименовывать!
public final class ResourcePacksLst implements Initiable, Listener, CommandExecutor {

    public static ResourcePacksLst resourcePacks;
    private static OstrovConfig packsConfig;
    public static boolean use = false;
    private static boolean block_interact;
    private static boolean block_menu;
    private static String link;
    private static byte[] hash;
    public static ItemStack lock;
    public static ItemStack key;
    public static ItemStack lobby;
    
    


    public ResourcePacksLst() { //или пытается грузить дважды, в RegisterCommands и как модуль
        resourcePacks = this;
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
        packsConfig = Config.manager.getNewConfig("resoucepacks.yml", new String[]{"", "Ostrov77 resoucepacks", ""});
        
        packsConfig.addDefault("use", false);
        packsConfig.addDefault("block_interact", false);
        packsConfig.addDefault("block_menu", false);
        
        if (packsConfig.getString("default")!=null) {
            link = packsConfig.getString("default");
            packsConfig.addDefault("link", link);//"http://site.ostrov77.ru/uploads/resourcepacks/ostrov77.zip");
        } else {
            packsConfig.addDefault("link", "http://site.ostrov77.ru/uploads/resourcepacks/ostrov77.zip");
        }

        packsConfig.removeKey("default");//, "http://site.ostrov77.ru/uploads/resourcepacks/none.zip");
        packsConfig.removeKey("per_world");//, "http://site.ostrov77.ru/uploads/resourcepacks/ostrov77.zip");
        packsConfig.removeKey("separate_world");

        packsConfig.saveConfig();
    
        
        //packs = new HashMap<>();
        link = packsConfig.getString("link");
        block_interact=packsConfig.getBoolean("block_interact");
        block_menu=packsConfig.getBoolean("block_menu");

        HandlerList.unregisterAll(resourcePacks);
        
        if (!packsConfig.getBoolean("use")) { //если офф в конфиге
            if (use) { //и перед этим был включен
                //HandlerList.unregisterAll(resourcePacks);
                Ostrov.log_warn("Менеджер пакетов текстур - выгружен");
                return;
            }
            return;
        }
//Ostrov.log("--------------- link="+link);
        if (link==null || link.isEmpty()) {
            //use = false;
            Ostrov.log_err("Менеджер пакетов текстур выгружен - URL не указан");
            return;
        }
        
        Ostrov.async( ()-> {
            
            final String fileName = link.substring(link.lastIndexOf('/') + 1, link.length());
            try {
                final URL url = new URL(link);
    //System.out.println("1111111111 "+link);            
                final File rp_file = new File(Ostrov.instance.getDataFolder(), "resourcepacks/"+fileName);
                if (!rp_file.exists()) {
                    rp_file.getParentFile().mkdirs();
                }
                Files.copy(url.openStream(), rp_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    //System.out.println("22222 "+rp_file); 
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-1");
                    InputStream fis = new FileInputStream(rp_file);
                    int n = 0;
                    byte[] buffer = new byte[8192];
                    while (n != -1) {
                        n = fis.read(buffer);
                        if (n > 0) {
                            digest.update(buffer, 0, n);
                        }
                    }
                    
                    fis.close();
                    hash = digest.digest();
                    
                    Ostrov.sync( ()-> Bukkit.getPluginManager().registerEvents(resourcePacks, Ostrov.getInstance()), 0);
                    Ostrov.log_ok("§2Пакет ресурсов "+fileName+", hash="+byteArray2Hex(hash));
                    use = true;
                    
                } catch (NoSuchAlgorithmException ex) {
                    Ostrov.log_err("Не удалось вычислить SHA1 для файла "+fileName+": "+ex.getMessage());
                    //use = false;
                }

            } catch (IOException ex) {
                Ostrov.log_err("Не удалось загрузить пакет ресурсов : "+ex.getMessage());
                //use = false;
            }
            
             
        } , 5);  
        
        
        //resourcepack_test = Bukkit.createInventory(null, 45, "§4Проверка Ресурс-пака");
        
        key = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta im = key.getItemMeta();
        im.displayName(TCUtils.format("§bНажмите на ключик"));
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_UNBREAKABLE);
        im.setCustomModelData(1);
        key.setItemMeta(im);
        
        lock = new ItemStack(Material.GOLDEN_SWORD);
        im = lock.getItemMeta();
        im.displayName(TCUtils.format("§bНажмите на ключик"));
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_UNBREAKABLE);
        im.setCustomModelData(2);
        lock.setItemMeta(im);  
        
        lobby = new ItemBuilder(Material.CRIMSON_DOOR)
            .addLore("§eВернуться в лобби")
            .build();
    } 
    

    
    
    @Override
    public boolean onCommand ( CommandSender se, Command comandd, String cmd, String[] a) {
        
        if ( !(se instanceof Player) ) {
            se.sendMessage("§4команда только от игрока!"); 
            return true; 
        }
        final Player p = (Player) se;
        if (!use) {
            p.sendMessage( "§cДанный сервер не требует пакета ресурсов!");
            return true; 
        }
        if (!PM.getOplayer(p.getName()).resourcepack_locked) {
            p.sendMessage( "§aУ вас уже установлен пакет ресурсов!");
            return true; 
        }// else {
        //    Ostrov.log_err("§eЗапрошена проверка пакета ресурсов, но пакет не загружен на сервер.");
        //    return true;w
        //}
        p.setResourcePack( link, hash, Component.text("§eУстанови этот пакет ресурсов для игры!") );//sendPack(p,true);
        return true;
    }
    


    private static String byteArray2Hex(final byte[] hash) {
    final Formatter formatter = new Formatter();
    for (byte b : hash) {
        formatter.format("%02x", b);
    }
    final String frm = formatter.toString();
    formatter.close();
    return frm;
}   
    
    
    @EventHandler ( ignoreCancelled = true, priority = EventPriority.MONITOR )
    public static void onPlayerResourcePackStatusEvent(PlayerResourcePackStatusEvent e) {
//System.out.println("onPlayerResourcePackStatusEvent "+e.getStatus());         
        //if ( !PM.exist(e.getPlayer().getName()) || !e.getPlayer().isOnline()) return;
        final Player p = e.getPlayer();
        final Oplayer op =  PM.getOplayer(p);
        
        switch (e.getStatus()) {
            
            case ACCEPTED -> {
                op.resourcepack_locked=false;
            }
            
            case SUCCESSFULLY_LOADED -> {
                //op.resourcepack_locked=false;
                pack_ok(e.getPlayer());
            }
            
            case DECLINED -> {
                op.resourcepack_locked=true;
                p.sendMessage(Component.text("§e*******************************************************************\n"
                		+ "§4Твой клиент отверг пакет ресурсов. §eСкорее всего, проблема в настройках!\n" 
                		+ "§2>>> §aКлик сюда для решения. §2<<<\n" 
                		+ "§e*******************************************************************\n")
                	.hoverEvent(HoverEvent.showText(Component.text("§5§oНажми для перехода")))
                	.clickEvent(ClickEvent.openUrl("https://youtu.be/dWou50o-aDQ")));
                /*TextComponent star = new TextComponent("https://youtu.be/dWou50o-aDQ");
                star.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "https://youtu.be/dWou50o-aDQ" ) );
                star.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oНажмите для перехода") ) );
                p.sendMessage("");
                p.spigot().sendMessage(star);
                TextComponent message = new TextComponent();
                message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "https://youtu.be/dWou50o-aDQ" ) );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oНажмите для перехода") ) );
                p.spigot().sendMessage(message);
                message = new TextComponent("§2>>> §aКлик сюда для решения. §2<<<");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "https://youtu.be/dWou50o-aDQ" ) );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oНажмите для перехода") ) );
                p.spigot().sendMessage(message);
                p.spigot().sendMessage(star);*/
            }
            
            case FAILED_DOWNLOAD -> {
                op.resourcepack_locked=true;
                p.sendMessage(Component.text("§e*******************************************************************\n"
                		+ "§4Твой клиент не загрузил пакет ресурсов. §eСкорее всего, проблема в настройках!\n" 
                		+ "§2>>> §aКлик сюда для ручной загрузки. §2<<<\n" 
                		+ "§e*******************************************************************\n")
                	.hoverEvent(HoverEvent.showText(Component.text("§5§oНажми для загрузки")))
                	.clickEvent(ClickEvent.openUrl(link)));
                
                /*TextComponent star = new TextComponent("§e*******************************************************************");
                star.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, link ) );
                star.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oНажмите для загрузки") ) );
                p.sendMessage("");
                p.spigot().sendMessage(star);
                TextComponent message = new TextComponent("§4Ваш клиент не смог загрузить серверный пакет ресурсов.");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, link ) );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oНажмите для загрузки")) );
                p.spigot().sendMessage(message);
                message = new TextComponent("§2>>> §aКлик сюда для ручной загрузки. §2<<<");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, link ) );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oНажмите для загрузки") ) );
                p.spigot().sendMessage(message);
                p.spigot().sendMessage(star);*/
            }
        }
    }

    
    private static void pack_ok (final Player p) {
        final Oplayer op=PM.getOplayer(p.getName());
        op.resourcepack_locked=false;
        //String hash=hash;
        //op.setData(Data.RESOURCE_PACK_HASH, hash);
        //Ostrov.sendMessage( p, "Bauth_getdata", p.getName()+"<:>RP_HASH<:>"+hash+"<:> " );
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
        p.sendMessage("§2Пакет ресурсов установлен!");
    }
    
    
    

    


    
    
    @EventHandler (priority = EventPriority.MONITOR)
    public static void onLoad (final LocalDataLoadEvent e) {
        if (use) {
            e.getPlayer().performCommand("rp");
            //e.getPlayer().setResourcePack( url );
            //e.getPlayer().sendMessage("");
            //e.getPlayer().sendMessage("§5Пакет ресурсов отправлен.");
        }
    }


    
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onInventoryOpen(InventoryOpenEvent e) {
        //if (Ostrov.isCitizen(e.getPlayer())) return;
        if ( !use || !block_menu) return;
        final Oplayer op = PM.getOplayer(e.getPlayer().getName());
        if (op==null) return;
        
        if ( TCUtils.toString(e.getView().title()).equals("§4Проверка Ресурс-пака") || op.menu.isProfileInventory(TCUtils.toString(e.getView().title())) ) {
            return;
        }
        if (op.resourcepack_locked) {
            e.setCancelled(true);
            Ostrov.sync(()->openCheckMenu((Player)e.getPlayer()), 1);
        }
    }
    
    
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public static void onInteract(PlayerInteractEvent e) {
        final Oplayer op = PM.getOplayer(e.getPlayer().getName());
        if (op==null) return;
        if (use && block_interact && op.resourcepack_locked) {
            if (e.getAction()==Action.RIGHT_CLICK_BLOCK || e.getAction()==Action.LEFT_CLICK_BLOCK) {
                e.setCancelled(true);
                pack_err((Player) e.getPlayer());
            }
        }
    }   


   

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = false)
    public static void onInvClick(InventoryClickEvent e) {
        if (TCUtils.toString(e.getView().title()).equals("§4Проверка Ресурс-пака") ) {
            e.setCancelled(true);
            final Oplayer op = PM.getOplayer(e.getWhoClicked().getName());
            if (op==null) return;
            if(!(e.getWhoClicked() instanceof Player)) return;
            if (e.getInventory().getType()!=InventoryType.CHEST) return;
            if ( e.getSlot() <0 || e.getSlot() > 44 || e.getCurrentItem()==null || e.getCurrentItem().getType().isAir() ) return;
            
            final Player p=(Player) e.getWhoClicked();
            
            if (e.getCurrentItem().getType()==lobby.getType()) {
                ApiOstrov.sendToServer(p, "lobby0", "");
                return;
            }
            
            if ( ItemUtils.compareItem(e.getCurrentItem(), key, true) ) {//клик на замок обрабатывать не надо, сработает при InventoryCloseEvent
                if (e.getCurrentItem().getItemMeta().hasCustomModelData() && e.getCurrentItem().getItemMeta().getCustomModelData() == key.getItemMeta().getCustomModelData()) {
                    pack_ok(p);
                }
            }
            p.closeInventory();
            
        }    
    }
    



    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public static void onInvClose(InventoryCloseEvent e) {
        final Oplayer op = PM.getOplayer(e.getPlayer().getName());
        if (op==null) return;
        if (!use || e.getInventory().getType()!=InventoryType.CHEST) return;
            if (TCUtils.toString(e.getView().title()).equals("§4Проверка Ресурс-пака")) {
                if (op.resourcepack_locked) {
                    pack_err((Player) e.getPlayer());
                }
            }
    }   


    private static void pack_err(final Player p) {
        p.sendMessage("");
        p.sendMessage(Component.text("§cВы не сможете играть на этом сервере без пакета ресурсов!\n§eЧто делать?:")
        	.append(Component.text("§aВариант 1: Попытаться еще раз. §5§o>Клик сюда для установки<")
        		.hoverEvent(HoverEvent.showText(Component.text("§b§oНажми для установки")))
        		.clickEvent(ClickEvent.runCommand("/rp")))
        	.append(Component.text("§aВариант 2: Установить вручную. §5§o>Клик сюда для загрузки пакета<")
        		.hoverEvent(HoverEvent.showText(Component.text("§b§oНажми для установки")))
        		.clickEvent(ClickEvent.openUrl(link)))
        	.append(Component.text("§aВариант 3: Исправить настройки. §5§o>Клик сюда для перехода<")
        		.hoverEvent(HoverEvent.showText(Component.text("§b§oНажми для перехода")))
        		.clickEvent(ClickEvent.openUrl("https://youtu.be/dWou50o-aDQ"))));
        
        /*TextComponent message = new TextComponent();
        p.spigot().sendMessage(message);
        message = new TextComponent("§bЕсли в меню проверки вы видите золотые мечи, пакет §4НЕ УСТАНОВЛЕН!");
        p.spigot().sendMessage(message);
        p.sendMessage("§eЧто делать?:");
        message = new TextComponent("§aВариант 1: Попытаться еще раз. §5§o>Клик сюда для установки<");
        message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/rp" ) );
        message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§b§oНажмите для установки") ) );
        p.spigot().sendMessage(message);
        message = new TextComponent("§aВариант 2: Установить вручную. §5§o>Клик сюда для загрузки пакета<");
        message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, link ) );
        message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§b§oНажмите для загрузки") ) );
        p.spigot().sendMessage(message);
        message = new TextComponent("§aВариант 3: Исправить настройки клиента. §5§o>Клик сюда для перехода<");
        message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "https://youtu.be/dWou50o-aDQ" ) );
        message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§b§oНажмите для перехода") ) );
        p.spigot().sendMessage(message);*/
        p.sendMessage("");
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, 1);
    }
    
    
     
    
    
    public static void openCheckMenu ( final Player p ) {
        if (!use) return; //не открывать менюшку, а то берутся предметы
        final Inventory rp_check = Bukkit.createInventory(null, 45, TCUtils.format("§4Проверка Ресурс-пака"));
        for (int i=0; i<44; i++) {
            rp_check.addItem(lock);
        }
        rp_check.setItem(ApiOstrov.randInt(0, 43), key);
        rp_check.setItem(44, lobby);
        p.openInventory(rp_check);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
    }


    
    
    
  

        

    
}
