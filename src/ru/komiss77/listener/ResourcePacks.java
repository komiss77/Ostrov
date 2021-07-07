package ru.komiss77.listener;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.enums.Data;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.Initiable;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.objects.ResourcePack;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.OstrovConfig;





public final class ResourcePacks extends Initiable implements Listener {

    public static ResourcePacks resourcePacks;
    private static OstrovConfig packsConfig;
    public static boolean use = false;
    private static boolean block_interact;
    private static HashMap <String,ResourcePack> packs;
    
    public static Inventory resourcepack_test;
    public static ItemStack lock;
    public static ItemStack key;
    
    


    public ResourcePacks() {
        resourcePacks = this;
        reload();
    }

    
    
    
    @Override
    public void reload() {
        packsConfig = Cfg.manager.getNewConfig("resoucepacks.yml", new String[]{"", "Ostrov77 resoucepacks", ""});
        
        packsConfig.addDefault("use", false);
        packsConfig.addDefault("separate_world", false);
        packsConfig.addDefault("block_interact", false);
        packsConfig.addDefault("per_world.example_world", "http://ostrov77.ru/uploads/resourcepacks/ostrov77.zip");
        packsConfig.addDefault("default", "http://ostrov77.ru/uploads/resourcepacks/none.zip");
        packsConfig.saveConfig();
    
        
        packs = new HashMap<>();
        block_interact=packsConfig.getBoolean("block_interact");

        if (!packsConfig.getBoolean("use")) { //если офф в конфиге
            if (use) { //и перед этим был включен
                HandlerList.unregisterAll(resourcePacks);
                Ostrov.log_warn("Менеджер пакетов текстур - выгружен");
                return;
            }
            return;
        }
        use=true;
        
        Ostrov.async( ()-> {
                
            if (packsConfig.getString("default")!=null) {
                ResourcePack pack = get_pack(packsConfig.getString("default"));
                if (pack!=null) {
                    packs.put("default", pack);
                }
            }

            if(packsConfig.getConfigurationSection("per_world")!=null) {
                packsConfig.getConfigurationSection("per_world").getKeys(false).stream().forEach((world_name) -> {
                        //per_world.put (s,Conf.GetPacks().getString("per_world."+s));
                        ResourcePack world_pack = get_pack(packsConfig.getString("per_world."+world_name));
                        if (world_pack!=null) packs.put(world_name, world_pack);
                });
            }

            if (packs.isEmpty()) {
                use = false;
                Ostrov.log_err("Менеджер пакетов текстур выгружен - не удалось загрузить текстуры по URL");
                return;
            }

            if (!packs.containsKey("default")) {
                use = false;
                Ostrov.log_err("Менеджер пакетов текстур выгружен - нет текстур по умолчанию");
                return;
            }
            //use = !packs.isEmpty() && packs.containsKey("default");
            //if (use) {
            Ostrov.sync( () -> Bukkit.getPluginManager().registerEvents(resourcePacks, Ostrov.GetInstance()) , 0);
            Ostrov.log_ok("Менеджер пакетов текстур - загружено : "+packs.size());
            //} else {
            //    HandlerList.unregisterAll(resourcePacks);
            //    Ostrov.log_err("Менеджер пакетов текстур выключился - не удалось загрузить текстуры по URL");
            //}
//System.out.println(" !!!! loaded "+packs+"  use="+use);        
        } , 5);       
        
        
        resourcepack_test = Bukkit.createInventory(null, 45, "§4Проверка Ресурс-пака");
        
        key = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta im = key.getItemMeta();
        im = key.getItemMeta();
        im.setDisplayName("§bНажмите на ключик");
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_UNBREAKABLE);
        im.setCustomModelData(1);
        key.setItemMeta(im);
        
        lock = new ItemStack(Material.GOLDEN_SWORD);
        im = lock.getItemMeta();
        im.setDisplayName("§bНажмите на ключик");
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_UNBREAKABLE);
        im.setCustomModelData(2);
        lock.setItemMeta(im);  
        
        for (int i=0; i<45; i++) {
            resourcepack_test.addItem(lock);
        }        
    }    
    
   
    @EventHandler ( ignoreCancelled = true, priority = EventPriority.MONITOR )
    public static void onPlayerResourcePackStatusEvent(PlayerResourcePackStatusEvent e) {
System.out.println("onPlayerResourcePackStatusEvent "+e.getStatus());         
        if ( !PM.exist(e.getPlayer().getName()) || !e.getPlayer().isOnline()) return;
        switch (e.getStatus()) {
            case ACCEPTED:
                pack_accepted(e.getPlayer());
                break;

            case DECLINED:
                err_decilined(e.getPlayer());
                break;

            case FAILED_DOWNLOAD:
                err_load(e.getPlayer());
                break;

            case SUCCESSFULLY_LOADED:
                pack_ok(e.getPlayer());
                break;
        }
    }
     
     
     
    private static void err_decilined (final Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PM.getOplayer(p.getName()).resourcepack_locked=true;
                TextComponent star = new TextComponent("§e*******************************************************************");
                star.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "http://site.ostrov77.su/resource_pack_setup.html" ) );
                star.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oНажмите для перехода") ) );
                p.sendMessage("");
                p.spigot().sendMessage(star);
                TextComponent message = new TextComponent("§4Ваш клиент отверг серверный пакет ресурсов. §eСкорее всего, проблема в настройках!");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "http://site.ostrov77.su/resource_pack_setup.html" ) );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oНажмите для перехода") ) );
                p.spigot().sendMessage(message);
                message = new TextComponent("§2>>> §aКлик на это сообщение для решения проблемы. §2<<<");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "http://site.ostrov77.su/resource_pack_setup.html" ) );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new Text("§5§oНажмите для перехода") ) );
                p.spigot().sendMessage(message);
                p.spigot().sendMessage(star);
            }
        }.runTask(Ostrov.instance);
    }
    
    private static void err_load (final Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PM.getOplayer(p.getName()).resourcepack_locked=true;
                TextComponent star = new TextComponent("§e*******************************************************************");
                star.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, get_rp(p).url ) );
                star.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§5§oНажмите для загрузки").create() ) );
                p.sendMessage("");
                p.spigot().sendMessage(star);
                TextComponent message = new TextComponent("§4Ваш клиент не смог загрузить серверный пакет ресурсов.");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, get_rp(p).url ) );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§5§oНажмите для загрузки").create() ) );
                p.spigot().sendMessage(message);
                message = new TextComponent("§2>>> §aКлик на это сообщение для ручной загрузки. §2<<<");
                message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, get_rp(p).url ) );
                message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§5§oНажмите для загрузки").create() ) );
                p.spigot().sendMessage(message);
                p.spigot().sendMessage(star);
            }
        }.runTask(Ostrov.instance);
    }
    
    private static void pack_accepted (final Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                PM.getOplayer(p.getName()).resourcepack_locked=true;
            }
        }.runTask(Ostrov.instance);
    }
    
    private static void pack_ok (final Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final Oplayer op=PM.getOplayer(p.getName());
                op.resourcepack_locked=false;
                String hash=get_rp(p).hash;
                op.setData(Data.RESOURCE_PACK_HASH, hash);
                //Ostrov.sendMessage( p, "Bauth_getdata", p.getName()+"<:>RP_HASH<:>"+hash+"<:> " );
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
                p.sendMessage("§2Пакет ресурсов установлен!");
            }
        }.runTask(Ostrov.instance);
    }
    
    
    
    
    
    
    
    private static ResourcePack get_pack (String link) {
        String fileName = link.substring(link.lastIndexOf('/') + 1, link.length());
        
        try {
            
            URL url = new URL(link);
//System.out.println("1111111111 "+link);            
            File rp_file = new File(Ostrov.instance.getDataFolder(), "resourcepacks/"+fileName);
            if (!rp_file.exists()) rp_file.getParentFile().mkdirs();
            Files.copy(url.openStream(), rp_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
//System.out.println("22222 "+rp_file);            
                try {
                        MessageDigest shaDigest = MessageDigest.getInstance("SHA-1");
                        String sha_1 = getFileChecksum(shaDigest, rp_file);
                        return new ResourcePack( link, sha_1, fileName);

                } catch (NoSuchAlgorithmException ex) {
                    Ostrov.log_err("Не удалось dsxbckbnm SHA1 для файла "+fileName+": "+ex.getMessage());
                    return null;
                }

            
        } catch (IOException ex) {
            Ostrov.log_err("Не удалось загрузить пакет ресурсов по ссылке "+link+": "+ex.getMessage());
            return null;
        }

    }
    

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0; 

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
       return sb.toString();
    }    

    
    
@EventHandler
    public static void BungeeDataRecieved (final BungeeDataRecieved e) {
        if (Ostrov.isCitizen(e.getPlayer())) return;
        if (use) new BukkitRunnable() {
            //final Player p=(Player) e.getPlayer();
            @Override
            public void run() {
                if (e.getPlayer()!=null && PM.exist(e.getPlayer().getName()) && e.getPlayer().isOnline()) Set_pack(e.getPlayer(), false);
            }
        }.runTask(Ostrov.instance);
        //Set_pack(e.Get_player(), get_pack_url(e.Get_player()), false);
    }

    
    @EventHandler(priority = EventPriority.MONITOR)
    public static void onPlayerWorldChange(PlayerChangedWorldEvent e) {
        if (Ostrov.isCitizen(e.getPlayer())) return;
        if (use) Set_pack(e.getPlayer(),false);
//System.out.println("----- onPlayerWorldChange: "+PM.getOplayer(e.getPlayer().getName()).rp_status.toString());
    }

    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public static void inventory_open(InventoryOpenEvent e) {
        if (Ostrov.isCitizen(e.getPlayer())) return;
        if ( use && block_interact && e.getPlayer() instanceof Player) {
            
        if ( e.getView().getTitle().equals("§4Проверка Ресурс-пака") 
                || e.getView().getTitle().startsWith(GM.main_inv_name) 
                || e.getView().getTitle().equals(ItemUtils.profile_master_inv_name) 
        ) return;
        
            if (PM.getOplayer(e.getPlayer().getName()).resourcepack_locked) {
                e.setCancelled(use);
                new BukkitRunnable() {
                    final Player p=(Player) e.getPlayer();
                    @Override
                    public void run() {
                        Меню_проверки_ресурспака(p);
                    }
                }.runTaskLater(Ostrov.instance, 1);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public static void interact(PlayerInteractEvent e) {
        if (Ostrov.isCitizen(e.getPlayer())) return;
        if (use && block_interact && PM.getOplayer(e.getPlayer().getName()).resourcepack_locked) {
            if (e.getAction()==Action.RIGHT_CLICK_BLOCK || e.getAction()==Action.LEFT_CLICK_BLOCK) {
                e.setCancelled(true);
                pack_err((Player) e.getPlayer());
            }
        }
    }   


 
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public static void rp_check(InventoryCloseEvent e) {
        if (Ostrov.isCitizen(e.getPlayer())) return;
        if (!use || e.getInventory().getType()!=InventoryType.CHEST) return;
            if (e.getView().getTitle().equals("§4Проверка Ресурс-пака")) {
                if (PM.getOplayer(e.getPlayer().getName()).resourcepack_locked) {
                    pack_err((Player) e.getPlayer());
                }
            }
    }   


    private static void pack_err (final Player p) {
        p.sendMessage("");
        p.sendMessage("");
        TextComponent message = new TextComponent("§cВы не сможете играть на этом сервере без пакета ресурсов!");
        p.spigot().sendMessage(message);
        message = new TextComponent("§bЕсли в меню проверки вы видите золотые мечи, пакет §4НЕ УСТАНОВЛЕН!");
        p.spigot().sendMessage(message);
        p.sendMessage("§eЧто делать?:");
        message = new TextComponent("§aВариант 1: Попытаться еще раз. §5§o>Клик сюда для установки<");
        message.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/rp" ) );
        message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§b§oНажмите для установки").create() ) );
        p.spigot().sendMessage(message);
        message = new TextComponent("§aВариант 2: Установить вручную. §5§o>Клик сюда для загрузки пакета<");
        message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, get_rp(p).url ) );
        message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§b§oНажмите для загрузки").create() ) );
        p.spigot().sendMessage(message);
        message = new TextComponent("§aВариант 3: Исправить настройки клиента. §5§o>Клик сюда для перехода<");
        message.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL, "http://site.ostrov77.su/resource_pack_setup.html" ) );
        message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§b§oНажмите для перехода").create() ) );
        p.spigot().sendMessage(message);
        p.sendMessage("");
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1, 1);
    }
    
    
    

@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
    public static void rp_unlock(InventoryClickEvent e){
        if (Ostrov.isCitizen(e.getWhoClicked())) return;
        if(!(e.getWhoClicked() instanceof Player)) return;
        if (e.getInventory().getType()!=InventoryType.CHEST) return;
        if ( e.getSlot() <0 || e.getSlot() > 44 || e.getCurrentItem()==null || e.getCurrentItem().getType()==Material.AIR ) return;
        
        if (e.getView().getTitle().equals("§4Проверка Ресурс-пака") ) {
                e.setCancelled(true);
                Player p=(Player) e.getWhoClicked();
                e.getWhoClicked().closeInventory();
                //if (VM.getNmsNbtUtil().hasString(e.getCurrentItem(), "ostrov_system")) {//клик на замок обрабатывать не надо, сработает при InventoryCloseEvent
                if ( ItemUtils.compareItem(e.getCurrentItem(), key, true) ) {//клик на замок обрабатывать не надо, сработает при InventoryCloseEvent
                    //if (CraftItemStack.asNMSCopy(e.getCurrentItem()).getDamage()==1) {
                    //if (VM.getNmsNbtUtil().getDamage(e.getCurrentItem())==1) {
                    if (e.getCurrentItem().getItemMeta().hasCustomModelData() && e.getCurrentItem().getItemMeta().getCustomModelData() == key.getItemMeta().getCustomModelData()) {
                        pack_ok(p);
                    }
                }
        }    
    }
    
    
    
    
    public static void Меню_проверки_ресурспака ( Player p ) {
        Inventory rp_check = Bukkit.createInventory( null, 45, "§4Проверка Ресурс-пака");
        rp_check.setContents(resourcepack_test.getContents());
        rp_check.setItem(ApiOstrov.randInt(0, 44), key);
        p.openInventory(rp_check);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
    }
    


    public static boolean Текстуры_утановлены(Player p) {
        if (use) return !PM.getOplayer(p.getName()).resourcepack_locked;
        else {
            Ostrov.log_err("§eЗапрошена проверка пакета ресурсов, но пакет не загружен на сервер.");
            return true;
        }
    }

    
    public static void Set_pack (Player p, boolean anycase) {
        if (p==null || !p.isOnline()) return;
//System.out.println("-----ресурспак-set: "+p.getName()+" "+pack+" hash:"+per_world_hash.get(pack)+ " уже стоит?"+PM.getOplayer(p.getName()).Pack_equals(per_world_hash.get(pack)) );
        ResourcePack pack = get_rp(p);
        if ( anycase || !PM.getOplayer(p.getName()).getDataString(Data.RESOURCE_PACK_HASH).equals(pack.hash)) {
System.out.println("Set_pack url="+pack.url+"  hash="+pack.hash+"  last_hash="+PM.getOplayer(p.getName()).getDataString(Data.RESOURCE_PACK_HASH) );
            //((CraftPlayer)p).getHandle().setResourcePack( pack.url, pack.hash );
            //p.setResourcePack( pack.url, pack.hash );
            p.setResourcePack( pack.url );
            p.sendMessage("");
            p.sendMessage("§5Пакет ресурсов отправлен.");
    } 
}    
    


    
    public static ResourcePack get_rp(Player p) {
       if ( packs.containsKey(p.getWorld().getName()) ) {
            return packs.get(p.getWorld().getName());
        } else {
            return packs.get("default");
        }
    }
    
   
      


        

    
}
