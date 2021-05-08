package ru.komiss77.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandException;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Enums.Action;
import ru.komiss77.Initiable;
import ru.komiss77.Listener.SpigotChanellMsg;
import ru.komiss77.Managers.Timer;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.DonatEffect;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.OstrovConfig;




public final class Pandora extends Initiable implements Listener {
    
    private static OstrovConfig config;
    private static Inventory confirm_inv;
    private static HashMap<String,ArmorStand>pandoras;
    private static BukkitTask tick_=null;
    private static int last_cmd;
    private static Set<String>clicked;
    private static Set<Location>music;
    public static boolean effect;
    private static String pandaName;
   
    private static final List<Material>head=Arrays.asList(
            Material.WHITE_GLAZED_TERRACOTTA,
            Material.ORANGE_GLAZED_TERRACOTTA,
            Material.MAGENTA_GLAZED_TERRACOTTA,
            Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
            Material.YELLOW_GLAZED_TERRACOTTA,
            Material.LIME_GLAZED_TERRACOTTA,
            Material.PINK_GLAZED_TERRACOTTA,
            Material.GRAY_GLAZED_TERRACOTTA,
            Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
            Material.CYAN_GLAZED_TERRACOTTA,
            Material.PURPLE_GLAZED_TERRACOTTA,
            Material.BLUE_GLAZED_TERRACOTTA,
            Material.BROWN_GLAZED_TERRACOTTA,
            Material.GREEN_GLAZED_TERRACOTTA,
            Material.RED_GLAZED_TERRACOTTA,
            Material.BLACK_GLAZED_TERRACOTTA
        );

    
    
    
    public Pandora(Ostrov instance) {
        pandoras=new HashMap<>();
        last_cmd=Timer.currentTimeSec();
        clicked=new HashSet<>();
        music=new HashSet<>();
        pandaName = "Шкатулка Пандоры";
        
        reload();
    }


    @Override
    public void reload() {
        config = Cfg.manager.getNewConfig("pandora.yml", new String[]{"", "Ostrov77 pandora config file", ""} );
        config.addDefault("use", false);
        config.saveConfig();   
        
        if (tick_!=null) tick_.cancel();
        pandoras.values().stream().forEach((e) -> {
            if (e!=null) e.remove();
        });
        pandoras.clear();
        HandlerList.unregisterAll(this);
        if (!config.getBoolean("use")) {
            Ostrov.log_ok ("§eПандора выключена.");
            return;
        }
        
        try {
            if (config.getStringList("координаты_шкатулок")!=null) {
                config.getStringList("координаты_шкатулок").stream().forEach((loc_) -> {
                    pandoras.put(loc_, null);
                });
            }
            
            confirm_inv=Bukkit.createInventory(null, 27, "§5Пандора - подтверждение");
            confirm_inv.setItem(11, new ItemBuilder(Material.GREEN_CONCRETE).setName("§2Да").build());
            confirm_inv.setItem(15, new ItemBuilder(Material.RED_CONCRETE).setName("§4Нет").build());
            Bukkit.getPluginManager().registerEvents(this, Ostrov.GetInstance());
            start_tick();
            Ostrov.log_ok ("§2Пандора активна!");
//System.out.println("+++++++++++++++Pandora.Load() pandora_loc="+pandoras);            
            
        } catch (IllegalArgumentException ex) { 
            Ostrov.log_err("§4Не удалось загрузить настройки пандоры: "+ex.getMessage());
        }
    }


    
    
    
    private static void start_tick() {
//System.out.println("start_tick !!!!!!!!!!!!!!!!! ");        
        tick_=new BukkitRunnable() {
            int tick=0;
            Set<String> brocken=new HashSet<>();
            @Override
            public void run() {
//Bukkit.broadcastMessage("last_check="+last_check/1000);
//System.out.println("tick="+tick);        
                
                if (Bukkit.getOnlinePlayers().size()>0) {
                    ArmorStand as;
                    for (String loc_string:pandoras.keySet()) {
                        as=pandoras.get(loc_string);
//System.out.println("tick -------- as="+as);        
                        if (as!=null && as.isValid()) {
                            
                            //переделать на пакет!
                            as.setHeadPose(as.getHeadPose().add(0.05, 0.05, 0.05));
                            if (tick%10==0) {
                                //if (!as.getCustomName().isEmpty()) as.setCustomName(ChatColor.values()[ApiOstrov.randInt(0, 15)]+as.getCustomName().substring(2));
                                if (!as.getCustomName().isEmpty()) as.setCustomName( ChatColor.values()[ApiOstrov.randInt(0, 15)] + ChatColor.stripColor(as.getCustomName()) );
                                clicked.clear();
                            }
                            
                            
                            Sound sound=Sound.values()[ApiOstrov.randInt(0,  Sound.values().length-1)];
                            if (tick%200==0 && music.isEmpty() && !sound.toString().startsWith("MUSIC_")) as.getWorld().playSound(as.getLocation(), sound, 0.3F, 2);
                            if (tick%30==0) {
                                ItemStack helmet = as.getEquipment().getHelmet();
                                if (helmet==null) {
                                    brocken.add(loc_string);
//System.out.println("Helmet broken!!");        
                                } else {
                                    helmet.setType(head.get(ApiOstrov.randInt(0, 15)));
                                    as.setHelmet(helmet);
//System.out.println("setHelmet="+helmet.getType());        
                                }
                            }
                        } else {
                            brocken.add(loc_string);
                        }
                    }
                        
                    tick++;
                    if (tick>=200) {
                        tick=0;
//System.out.println("tick 2 pandoras="+pandoras);
                        
                        brocken.stream().forEach((l_) -> {
                            CreatePandora(l_);
                        });
                        brocken.clear();

                    }

                }
                
            }
        }.runTaskTimer(Ostrov.instance, 13, 1);

    }    








    
    @EventHandler ( ignoreCancelled = true, priority = EventPriority.MONITOR )
    public void Command(PlayerCommandPreprocessEvent e) throws CommandException {
//System.out.println("------------> Command "+e.getMessage()+ " block_commands:"+Conf.block_commands+ " list:"+Conf.block_commands_except.toString());
//System.out.println("cmd 11111"+e.getMessage());
        if ( e.getPlayer().isOp() && e.getMessage().equals("/pandora++") && (Timer.currentTimeSec()-last_cmd >10)) {
//System.out.println("cmd 222");
            last_cmd=Timer.currentTimeSec();
            //pandora_loc.add(p.getLocation());
            final String loc_string=LocationUtil.StringFromLoc(e.getPlayer().getLocation());
            CreatePandora(loc_string);
            e.getPlayer().sendMessage("§6"+pandaName+" добавлена!");
            List<String>to_save=new ArrayList<>();
            pandoras.keySet().forEach((l_) -> {
                to_save.add(l_);
            });
//System.out.println("to_save="+to_save);            
            config.set("координаты_шкатулок", to_save);
            config.saveConfig();
        }
    }

    
@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public static void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getType()!=InventoryType.CHEST  || e.getSlot() <0 || e.getSlot() > 26 ) return;
        if (!e.getView().getTitle().equals("§5Пандора - подтверждение")) return;
        if (e.getCurrentItem()==null || e.getCurrentItem().getType()==Material.AIR) return;
        e.setCancelled(true);
        
        final Player p = (Player) e.getWhoClicked();
        if (e.getSlot()==11) { //да
            p.closeInventory();
            if (!clicked.contains(p.getName())) {
                clicked.add(p.getName());
                //Ostrov.sendMessage(p, "Bauth_getdata", p.getName()+"<:>ACTION<:>pandora_execute");
                SpigotChanellMsg.sendMessage(p, Action.OSTROV_PANDORA_EXECUTE, "-");
                
                DonatEffect.display(p.getLocation());
                //if (Ostrov.effect_manager!=null) {
                   // Effects.play("StarEffect", p.getLocation(), 10*20);
                    //Effect eff = new StarEffect (Ostrov.effect_manager); 
                    //eff.setLocation(p.getLocation());
                    //eff.iterations = 10 * 20;// Bleeding takes 15 seconds
                    //eff.start();
               // }
            } //else Ostrov.sendActionBar(p, "§eБудьте упорнее, шкатулочка с характером!");
            
        } else if (e.getSlot()==15) { //нет
            p.closeInventory();
            p.getWorld().strikeLightningEffect(p.getEyeLocation());
            kick(p);
        }

    }

    

@EventHandler(ignoreCancelled = false,priority=EventPriority.LOWEST)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent e) {
//System.out.println("PANDORA!!! onPlayerInteractAtEntityEvent 1");
        if(e.getRightClicked().getType() == EntityType.ARMOR_STAND && isPandora(e.getRightClicked())) {
            e.setCancelled(true);
//System.out.println("PANDORA!!! onPlayerInteractAtEntityEvent 2");
            if (!clicked.contains(e.getPlayer().getName())) {
                clicked.add(e.getPlayer().getName());
                //Ostrov.sendMessage(e.getPlayer(), "Bauth_getdata", e.getPlayer().getName()+"<:>ACTION<:>pandora_check");
                SpigotChanellMsg.sendMessage(e.getPlayer(), Action.OSTROV_PANDORA_CHECK, "-");
                
               // if (Ostrov.effect_manager!=null && !effect) {
                //    Effects.playCallback(this, "AtomEffect", e.getRightClicked().getLocation(), 5*20);
               // }
            }
//System.out.println("PANDORA!!! onPlayerInteractAtEntityEvent 3");
        }
    }


   

@EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e){
        if(e.getRightClicked().getType() ==EntityType.ARMOR_STAND && isPandora(e.getRightClicked())) {
            e.setCancelled(true);
//System.out.println("PANDORA!!! PlayerArmorStandManipulateEvent ");
        }
    }    

    
    
    
    private static void CreatePandora(final String loc_string) {
System.out.println("CreatePandora() ++++++++++++++");
        final Location loc=LocationUtil.LocFromString(loc_string);
        if (loc==null || !loc.getChunk().isLoaded()) return;
        //loc.getWorld().getNearbyEntities(loc, 2, 2, 2).stream().forEach((e) -> {
        //for (Entity e:loc.getChunk().getEntities()) {
        for (Entity e:loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
            //if (e.getType()==EntityType.ARMOR_STAND && e.getLocation().distanceSquared(loc)<2 && e.isCustomNameVisible() && ChatColor.stripColor(e.getCustomName()).equals("Шкатулка Пандоры")) {
            if (e.getType()==EntityType.ARMOR_STAND  && e.getCustomName()!=null && e.isCustomNameVisible() && ChatColor.stripColor(e.getCustomName()).equals(pandaName)) {
                e.remove();
            }
        }
        final ArmorStand as = (ArmorStand)loc.getWorld().spawn(loc, ArmorStand.class );
        as.setAI(false);
        as.setCollidable(false);
        as.setCanPickupItems(false);
        as.setGravity(false);
        as.setInvulnerable(true);
        //as.setMarker(true); нельзя! тогда не кликабельная
        as.setCustomName("§6"+pandaName);
        as.setCustomNameVisible(true);
        as.setVisible(false);
        as.setSilent(true);
        as.setSmall(true);
        as.getEquipment().setHelmet(new ItemStack(Material.WHITE_GLAZED_TERRACOTTA));
        pandoras.put(loc_string,as);
//System.out.println("CreatePandora() 2222 pandoras="+pandoras);
        //if (tick_==null) start_tick();
        //loc.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
    }
    
    public static void bungee_result_pandora_check (final Player p, final String result) {
        //final Player p = Bukkit.getPlayer(nik);
        //if (p==null) return;
//System.out.println("ОтветБанжи "+p+"   ?"+ok);
        if (result.equals("true")) {
            p.openInventory(confirm_inv);
            playMusic(p.getLocation());
        } else {
            kick(p);
        }
    }
    
    public static void bungee_pandora_result (final String nik, final String message) {
        final Player p = Bukkit.getPlayer(nik);
        if (p==null) return;
        final String msg = "§6[§eПандора§6] §f"+nik+" §b-> "+message;
//System.out.println("ОтветБанжи "+p+"   ?"+ok);
        p.getWorld().getPlayers().stream().forEach((p_)-> {
            ApiOstrov.sendBossbar(p_, msg, 5, BarColor.BLUE, BarStyle.SOLID, false);
        });
    }

   /* public static void result_pandora_check (final String nik, final boolean ok) {
        final Player p = Bukkit.getPlayer(nik);
        if (p==null) return;
//System.out.println("ОтветБанжи "+p+"   ?"+ok);
        if (ok) {
            p.openInventory(confirm_inv);
            playMusic(p.getLocation());
        } else {
            kick(p);
        }
    }*/

    private static void kick (final Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
        Vector v = p.getLocation().toVector().subtract(p.getLocation().toVector()).multiply(0.5D).add(new Vector(0.5D, 1.5D, 0.5D));
        v.setY(0);
        v.add(new Vector(0, 1, 0));
        p.setVelocity(v);
    }
    
    private static boolean isPandora(final Entity e) {
        return e.getType()==EntityType.ARMOR_STAND && pandoras.values().stream().anyMatch( (as) -> (as!=null && as.getUniqueId().toString().equals(e.getUniqueId().toString()) ) );
    }
    
   


   private static void playMusic(final Location location) {
       if (music.contains(location)) return;
       music.add(location);
       
      (new BukkitRunnable() {
         int step = 0;
         int step2 = 0;
         float increase = 0.0F;
         @Override
         public void run() {
            ++this.step;
            ++this.step2;
            if(this.step <= 60) {
                switch (this.step2) {
                    case 1:
                        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0F, 0.5F + this.increase);
                        break;
                    case 2:
                        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0F, 0.4F + this.increase);
                        break;
                    case 3:
                        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0F, 0.5F + this.increase);
                        break;
                    case 4:
                        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0F, 0.6F + this.increase);
                        this.step2 = 0;
                        this.increase += 0.08F;
                        break;
                    default:
                        break;
                }
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(Ostrov.instance, 0L, 2L);
      
      
      (new BukkitRunnable() {
         int step = 0;
         @Override
         public void run() {
            ++this.step;
            if(this.step <= 15) {
               location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_HAT, 0.7F, 1.0F);
            }
         }
      }).runTaskTimer(Ostrov.instance, 0L, 8L);
      
      
      (new BukkitRunnable() {
         int step = 0;
         float decrease = 0.0F;
         @Override
         public void run() {
            ++this.step;
            if(this.step <= 3) {
               location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 1.0F - this.decrease);
               this.decrease += 0.3F;
            } else {
               this.cancel();
               if (music.contains(location)) music.remove(location);
            }
         }
      }).runTaskTimer(Ostrov.instance, 140L, 2L);
   }










}
    
