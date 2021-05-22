package ru.komiss77.Commands;


import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Events.BungeeDataRecieved;
import ru.komiss77.Listener.PlayerListener;
import ru.komiss77.Managers.PM;
import ru.komiss77.Ostrov;
import ru.komiss77.version.IEntityGroup.EntityGroup;
import ru.komiss77.version.VM;




public class Pvp implements Listener, CommandExecutor {
    
    public static boolean allow_pvp_command;                                          //включение команды pvp on-off
    public static int pvp_battle_time;                                          //после первого удара - заносим обоих в режим боя
    public static boolean pvp_antirelog;                                        //убивать при перезаходе во время боя
    public static boolean pvp_drop_inv_inbattle;                                //дроп во время боя
    private static Set<String> anti_quiter;
    public static List<PotionEffectType> potion_pvp_type;
    public static boolean display_pvp_tag;
    public static boolean disable_creative_attack_to_mobs;
    public static boolean disable_creative_attack_to_player;
    public static Pvp pvp;

    public Pvp() {
        pvp = Pvp.this;
        anti_quiter = new HashSet();
        anti_quiter = new HashSet();
        potion_pvp_type= Lists.newArrayList( PotionEffectType.POISON, PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM, PotionEffectType.HUNGER );
        init();
    }
    
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            if (arg.length==1 && arg[0].equals("reload")) {
                reload();
            } else {
                cs.sendMessage("§e/"+this.getClass().getSimpleName()+" reload §7- перезагрузить настройки команды");
            }
            return true;
        }
        
        final Player p=(Player) cs;
        
        if (allow_pvp_command) {
                switch (arg.length) { 
                    case 0:
                        if (PM.getOplayer(p.getName()).pvp_allow) {
                            //PM.getOplayer(p.getName()).pvp_allow=false;
                            PM.getOplayer(p.getName()).pvpOff();
                            p.sendMessage( "§2ПВП выключен!" );
                            return true;
                        } else { PM.getOplayer(p.getName()).pvp_allow=true;
                            PM.getOplayer(p.getName()).pvpOn();
                            p.sendMessage( "§4ПВП включен!");
                            return true;
                        }
                    case 1:
                        switch (arg[0]) {
                            case "on":
                                PM.getOplayer(p.getName()).pvp_allow=true;
                            PM.getOplayer(p.getName()).pvpOn();
                                p.sendMessage( "§4ПВП включен!" );
                                return true;
                            case "off":
                                //PM.getOplayer(p.getName()).pvp_allow=false;
                                PM.getOplayer(p.getName()).pvpOff();
                                p.sendMessage(  "§2ПВП выключен!" );
                                return true;
                            case "reload":
                                if (cs.isOp()) reload();
                                return true;
                            default:
                                p.sendMessage( "§c ?   §f/pvp,  §f/pvp on,  §f/pvp off");
                                break;
                        }
                        break;
                    default:
                        p.sendMessage( "§c ?   §f/pvp,  §f/pvp on,  §f/pvp off");
                        break;
                }
            } else p.sendMessage( "§cУправление режимом ПВП отключено!");
        
        
        return true;
    }
    



    
    
    
    

    public static void init() {
        try {
            allow_pvp_command = Cfg.GetCongig().getBoolean("modules.pvp.use_pvp_command");
            pvp_battle_time = Cfg.GetCongig().getInt("modules.pvp.battle_mode_time");
            pvp_antirelog = Cfg.GetCongig().getBoolean("modules.pvp.kill_on_relog");
            pvp_drop_inv_inbattle = Cfg.GetCongig().getBoolean("modules.pvp.drop_inv_inbattle");
            display_pvp_tag = Cfg.GetCongig().getBoolean("modules.pvp.display_pvp_tag");
            disable_creative_attack_to_mobs = Cfg.GetCongig().getBoolean("modules.pvp.disable_creative_attack_to_mobs");
            disable_creative_attack_to_player = Cfg.GetCongig().getBoolean("modules.pvp.disable_creative_attack_to_player");
    
            if (pvp_battle_time<1) {
                Ostrov.log_ok ("§eВремя режима битвы не установлено! (battle_mode_block_command_time = -1)");
                //return;
            }
//System.out.println("---- allow_pvp_command="+allow_pvp_command);
            Bukkit.getPluginManager().registerEvents(pvp, Ostrov.GetInstance());
            
            Ostrov.log_ok ("§2Модуль ПВП активен!");
            
        } catch (Exception ex) { 
            Ostrov.log_err("§4Не удалось загрузить настройки "+pvp.getClass().getSimpleName()+" : "+ex.getMessage());
        }
    }

    public static void reload () {
        HandlerList.unregisterAll(pvp);
        anti_quiter.clear();
        Cfg.LoadConfigs();
        init();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @EventHandler(priority = EventPriority.MONITOR) 
    public void onDataRecieved (BungeeDataRecieved e) {
           
        if (pvp_antirelog && anti_quiter.contains(e.getPlayer().getName())) {
            final Player p = e.getPlayer();
            anti_quiter.remove(p.getName());
            p.sendMessage("§4Вы пытались избежать смерти, но у Вас не получилось..");
            //if (Pvp.pvp_antirelog) {
            p.getInventory().clear(); //просто очищаем, т.к. при ВЫХОДЕ инвентарь дропнулся, и может выйти дюп при втором дропе - гибели
            p.updateInventory();
            //}
            p.setHealth(0);
        }
        
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR) 
    public void onQuit (PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        if (Pvp.pvp_antirelog && PM.inBattle(p.getName())) {      //если удрал во время боя
            anti_quiter.add(e.getPlayer().getName());                       //при входе будет убит
            if (Pvp.pvp_drop_inv_inbattle) {
                for (ItemStack item : p.getInventory().getContents()) {
                    if (item != null) {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
            }
            Bukkit.getOnlinePlayers().stream().forEach((pl) -> {
                pl.sendMessage("§f"+e.getPlayer().getName()+" §4пытался сбежать во время боя, и будет наказан!");
            });

         }
        
    }




    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST )
    public void EntityDamageByEntityEvent (EntityDamageByEntityEvent e) {
        if (!e.getEntityType().isAlive() || e.getEntityType()==EntityType.ARMOR_STAND) return;   //не обрабатывать урон рамкам, опыту и провее
//System.out.println("EDBE: cause="+e.getCause()+" entity="+e.getEntity()+" damager="+e.getDamager());

        switch (e.getCause()) {
            case DRAGON_BREATH:
            case ENTITY_ATTACK:
            case ENTITY_EXPLOSION:
            case MAGIC:
            case PROJECTILE:
            case WITHER:
                break;
            default:
                return;
        }
        //if ( Type.valueOf(e.getEntityType().toString()).getMeta()==NMSUtils.MobMeta.UNDEFINED ) return; //не обрабатывать урон рамкам, опыту и провее
        //if ( e.getEntityType()==EntityType.PLAYER || e.getDamager().getType()==EntityType.PLAYER) {
            if (PlayerListener.disable_damage){
                e.setCancelled(true);
                return;
            }

            boolean cancel=Проверка_режима_пвп(e.getDamager(), e.getEntity(), e.getCause());
            if (cancel) {
                e.setCancelled(true);
            } 
        //}

    }        


    
    
    @EventHandler(ignoreCancelled = true,priority = EventPriority.HIGH)
    public void PlayerToggleFlightEvent (PlayerToggleFlightEvent e) {
      // if ( e.getPlayer().isOp() ) return;
//System.err.println(">>>>>>>>>>> 2");       
      if ( pvp_battle_time > 1 && PM.inBattle(e.getPlayer().getName()) ) {
            if (e.getPlayer().getAllowFlight() && e.getPlayer().isFlying()) {
                e.getPlayer().setFlying(false);
                e.getPlayer().setAllowFlight(false);
                ApiOstrov.sendActionBarDirect(e.getPlayer(), "§cВраги вцепились в ботинки и не отпускают!");
                e.setCancelled(true);
            }
        }
        
    }
    
/*
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public static void BattleModeEvent( BattleModeEvent e) {
//System.out.println("333333333333 canceled?"+e.Is_canceled());                
        if (!e.Is_canceled()) {
            if (e.Atack_is_player()) {
                PM.getOplayer(e.Get_atack_entity().getName()).pvpBattleModeBegin(pvp_battle_time);
                
                //if (!PM.inBattle(e.Get_atack_entity().getName())) Ostrov.sendActionBarDirect(e.Get_atack_entity().getName(), "§cРежим боя "+pvp_battle_time+" сек.!");
                //PM.getOplayer(e.Get_atack_entity().getName()).pvp_time=pvp_battle_time;
//System.out.println("4444444 pvp_time"+PM.getOplayer(e.Get_atack_entity().getName()).pvp_time+"   pvp_tag?"+PM.pvp_tag);                
               // if (PM.pvp_tag) PM.OP_pvp_display_tag(e.Get_atack_entity().getName(), 3);     //pvp mode
            }
            if (e.Target_is_player()) {
                PM.getOplayer(e.Get_target_entity().getName()).pvpBattleModeBegin(pvp_battle_time);
                //if (!PM.inBattle(e.Get_target_entity().getName())) Ostrov.sendActionBarDirect(e.Get_target_entity().getName(), "§cРежим боя "+pvp_battle_time+" сек.!");
               // PM.getOplayer(e.Get_target_entity().getName()).pvp_time=pvp_battle_time;
//System.out.println("5555555 pvp_time"+PM.getOplayer(e.Get_target_entity().getName()).pvp_time+"   pvp_tag?"+PM.pvp_tag);                
                //if (PM.pvp_tag) PM.OP_pvp_display_tag(e.Get_target_entity().getName(), 3);     //pvp mode
            }
        }
    }*/
    
    
    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void PlayerDeath (PlayerDeathEvent e) {
        if ( e.getEntity().getType()!=EntityType.PLAYER || Ostrov.isCitizen(e.getEntity())) return;
        final Player p = e.getEntity();
        PM.OP_Set_back_location(p.getName(), p.getLocation());
        if (PM.inBattle(p.getName()) && pvp_drop_inv_inbattle) {            //дроп инвентаря
        /* Лут образуется только когда в настройках мира KeepInventory off !!
            вот код сервера:
                boolean keepInventory;
                ArrayList<Object> loot = new ArrayList<Object>(this.inventory.getSize());
                boolean bl = keepInventory = this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || this.isSpectator();
                if (!keepInventory) {
                    for (ItemStack item : this.inventory.getContents()) {
                        if (item.isEmpty() || EnchantmentManager.shouldNotDrop(item)) continue;
                        loot.add((Object)CraftItemStack.asCraftMirror((ItemStack)item));
                    }
                }
                for (ItemStack item : this.drops) {
                    loot.add(item);
                }
                this.drops.clear();
                PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent((EntityPlayer)this, loot, (String)deathmessage, (boolean)keepInventory);
            */
            if (p.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) { //если сохранение вкл, то дроп в эвенте не образуется, нужно кидать вручную
//System.out.println("Death KEEP_INVENTORY=true drop"+e.getDrops());
                for (ItemStack is : p.getInventory().getContents()) {
                    if (is != null && is.getType()!=Material.AIR) {
//System.out.println("drop si ? "+Ostrov.lobby_items.isSpecItem(is));
                        if (Ostrov.lobby_items.isSpecItem(is)) {//не лутать менюшки!
//System.out.println("пропускаем si");
                            continue;
                        }
                        p.getWorld().dropItemNaturally(p.getLocation(), is);
                    }
                }
                p.getInventory().clear();
                p.updateInventory();

            } else {
//System.out.println("Death drop"+e.getDrops());
                for (int i=e.getDrops().size()-1; i>=0; i--) {
//System.out.println("drop si ? "+Ostrov.lobby_items.isSpecItem(e.getDrops().get(i)));
                     if (Ostrov.lobby_items.isSpecItem(e.getDrops().get(i))) {  //отменить лут менюшек
                         e.getDrops().remove(i);
                     }
                }
//System.out.println("Death drop2"+e.getDrops());
                //ничего не надо, выпадет само!
            }
            
            p.sendMessage("§cВаши вещи достались победителю!");
//System.out.println("Death "+e.getDrops());
        } else {
            //e.setKeepInventory(true);//в остальных случаях по настройкам мира
        }
        if (!PlayerListener.clear_stats) PM.Addbdead(p.getName());
        PM.getOplayer(p.getName()).pvpBattleModeEnd();
        //PM.getOplayer(e.getEntity().getName()).pvp_time=0;
        //Bukkit.getPluginManager().callEvent(new BattleModeEndEvent ( e.getEntity() ) );
    }
    
    

    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onPotionSplash( PotionSplashEvent e) {
            if (e.getAffectedEntities().isEmpty() || !(e.getPotion().getShooter() instanceof Player)) return;

            e.getPotion().getEffects().stream().forEach((effect) -> {
                if (potion_pvp_type.contains(effect.getType()) ) {
                    e.getAffectedEntities().stream().forEach((target) -> {
                        if (target.getType().isAlive() && Проверка_режима_пвп( (Entity) e.getPotion().getShooter(), target, EntityDamageEvent.DamageCause.MAGIC )) {
                            e.setIntensity(target, 0);
                        } 
                    });
                }
            });
    }



    
    
    
    
    
    
    
    
    
    
     
    
    private static boolean Проверка_режима_пвп(Entity attack_entity, final Entity target_entity, final EntityDamageEvent.DamageCause cause) {
//System.out.println("pvp attack_entity="+attack_entity+" type="+"   target_entity="+target_entity+" type=");        
        
        Player damager = null;
        Player target = null;

        if (attack_entity instanceof Projectile ) { //при попадании стрелы принимаем атакующего за стреляющего
            if( ((Projectile) attack_entity).getShooter() != null) {
                attack_entity = (Entity) ((Projectile) attack_entity).getShooter(); 
            }
        }
        
        if (attack_entity.getType()==EntityType.PLAYER && !Ostrov.isCitizen(attack_entity))  damager = (Player) attack_entity; 
        if (target_entity.getType()==EntityType.PLAYER && !Ostrov.isCitizen(target_entity))  target = (Player) target_entity; 
//System.out.println("target_entity type="+target_entity.getType()+"  citizens?"+Ostrov.isCitizen(target_entity)+" target="+target);  



        //if (attack_entity.getType()==EntityType.PLAYER) {
            //attack =  (Player) attack_entity;
           // if (!Ostrov.isCitizen(attack_entity)) attack = (Player) attack_entity;
        //} else 
        //if (damager == null && attack_entity instanceof Projectile ) {
        //    if( ((Projectile) attack_entity).getShooter() != null && ((Projectile) attack_entity).getShooter() instanceof Player) {         //если стрелял игрок
        //        if (!Ostrov.isCitizen((Entity) ((Projectile) attack_entity).getShooter())) damager = (Player) ((Projectile) attack_entity).getShooter(); 
        //    }
        //}
//System.out.println("attack_entity type="+attack_entity.getType()+"  citizens?"+Ostrov.isCitizen(attack_entity)+" attack="+attack);        
        

        if (damager==null && target==null) return false; //если ни один не игрок, пропускаем
        
        if (target != null && PM.exist(target.getName()) && PM.getOplayer(target.getName()).no_damage>0 ) {           //если у жертвы иммунитет, отмана
            ApiOstrov.sendActionBarDirect(target, "§aИммунитет к повреждениям  - осталось §f"+PM.getOplayer(target.getName()).no_damage+" §a сек.!");
            if (damager!=null) ApiOstrov.sendActionBarDirect(damager, "§aУ "+target.getName()+" иммунитет к повреждениям  - осталось §f"+PM.getOplayer(target.getName()).no_damage+" §a сек.!");
            target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_HIT, 1, 1);
            return true;
        } 
        
        if (damager != null && PM.exist(damager.getName()) && PM.getOplayer(damager.getName()).no_damage>0 ) {
            ApiOstrov.sendActionBarDirect(damager, "§aУ Вас иммунитет к повреждениям и атакам - осталось §f"+PM.getOplayer(damager.getName()).no_damage+" §a сек.!");
            return true;
        }    

        if ( damager != null && target != null ) {                               //если обаигроки
            if ( !PM.getOplayer(target.getName()).pvp_allow ) {                         //если у жертвы выкл пвп
                ApiOstrov.sendActionBarDirect(damager, "§2У цели выключен режим ПВП!");
                ApiOstrov.sendActionBarDirect(target, "§2У Вас выключен режим ПВП!");
                return true;
            }
            if ( !PM.getOplayer(damager.getName()).pvp_allow ) {                         //если у атакующего выкл пвп
                ApiOstrov.sendActionBarDirect(target, "§2У нападающего выключен режим ПВП!");
                ApiOstrov.sendActionBarDirect(damager, "§2У Вас выключен режим ПВП!");
                return true;
            }
        }    
                 
                
        if ( damager != null) { //атакует игрок 
//System.out.println("111 attack="+attack+"  fly_off_on_damage?"+CMD.fly_off_on_damage+"  attack.isFlying()?"+attack.isFlying());        
            if ( damager.getGameMode()==GameMode.CREATIVE && !damager.isOp() ) {
                if (target != null && PM.exist(target.getName()) && disable_creative_attack_to_player) {
                    ApiOstrov.sendActionBarDirect(damager, "§cАтака на игрока в креативе невозможна!");
                    return true;
                } else if (disable_creative_attack_to_mobs) {
                    final EntityGroup group = VM.getNmsEntitygroup().getEntytyType(target_entity);
                    if (group!=EntityGroup.UNDEFINED) {
                        ApiOstrov.sendActionBarDirect(damager, "§cАтака на моба в креативе невозможна!");
                        return true;
                    }
                }
            }
            if (CMD.fly_block_atack_on_fly && damager.isFlying() && !damager.isOp() ) {
                ApiOstrov.sendActionBarDirect(damager, "§cАтака в полёте невозможна!");
                return true;
            }
        }
        
        if (target != null && CMD.fly_off_on_damage &&  target.isFlying() ) {
//System.out.println("222 target="+target+"  fly_off_on_damage?"+CMD.fly_off_on_damage+"  target.isFlying()?"+target.isFlying());        
            target.setFlying(false);
            target.setAllowFlight(false);
            target.setFlySpeed(0.1F);
            target.setFallDistance(0);
            ApiOstrov.sendActionBarDirect(target, "§cКажется, Вам прострелили крыло :(");
        }
        
        if (pvp_battle_time > 1 && (damager!=null || target!=null) ) {       //если активен режима боя и хотя бы один игрок
            
            //if ( attack != null && target_entity instanceof Monster ) {         //нападает игрок жертва монстр 
            if ( damager != null && VM.getNmsEntitygroup().getEntytyType(target_entity)==EntityGroup.MONSTER ) {         //нападает игрок жертва монстр 
//System.out.println("11111111111111111 нападает игрок жертва монстр  ");
                PM.getOplayer(damager.getName()).pvpBattleModeBegin(pvp_battle_time);
                //PM.getOplayer(attack.getName()).pvpBattleModeBegin(pvp_battle_time, pvp_tag);
                //Bukkit.getPluginManager().callEvent(new BattleModeEvent ( damager, target_entity, cause ) );
                return false;  
                
            //} else if ( target != null && attack_entity instanceof Monster )   {       //жертва игрок нападает монстр
            } else if ( target != null && VM.getNmsEntitygroup().getEntytyType(attack_entity)==EntityGroup.MONSTER )   {       //жертва игрок нападает монстр
//System.out.println("2222222222222 жертва игрок нападает монстр");                
                //PM.getOplayer(target.getName()).pvpBattleModeBegin(pvp_battle_time, pvp_tag);
                PM.getOplayer(target.getName()).pvpBattleModeBegin(pvp_battle_time);
                //Bukkit.getPluginManager().callEvent(new BattleModeEvent ( attack_entity, target, cause ) );
                return false;  
                
            } else if (  damager != null && target != null )   {                         //дерутся два игрока
                PM.getOplayer(damager.getName()).pvpBattleModeBegin(pvp_battle_time);
                PM.getOplayer(target.getName()).pvpBattleModeBegin(pvp_battle_time);
                //PM.getOplayer(attack.getName()).pvpBattleModeBegin(pvp_battle_time, pvp_tag);
                //PM.getOplayer(target.getName()).pvpBattleModeBegin(pvp_battle_time, pvp_tag);
                //Bukkit.getPluginManager().callEvent(new BattleModeEvent ( damager, target, cause ) );
                return false;            
            }
            
        }
        
        return false;  

    }
    
   
    
    
    
    


}
    
    
 