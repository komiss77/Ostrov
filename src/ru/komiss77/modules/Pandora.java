package ru.komiss77.modules;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.RewardType;
import ru.komiss77.enums.Stat;
import ru.komiss77.enums.StatFlag;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.events.FigureActivateEntityEvent;
import ru.komiss77.events.FigureClickEvent;
import ru.komiss77.events.PandoraUseEvent;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.Figure;
import ru.komiss77.utils.DonatEffect;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ConfirmationGUI;




public final class Pandora implements Initiable, Listener {
    
    public static boolean use;
    
    public static final int DAY_PLAY_TIME_TO_OPEN = 7200;
    private static OstrovConfig config;
    private static Figure figure;
    private static ArmorStand as;
    private static BukkitTask tick;
    private static final String PANDORA_NAME  = "Шкатулка Пандоры";;
    private static BoundingBox box;
    private static final TextComponent infoRu = Component.text("§e§kXXX§6 Шкатулка Пандоры предлагает Вам испытать удачу! §e§kXXX")
            	.hoverEvent(HoverEvent.showText(TCUtils.format(
            		"§7По легенде, сундучки Пандоры были созданы Даарианцами,"
                    + "\n§7а секреты их эффектов тщательно скрывались."
                    + "\n§7Считалось, что сундучки Пандоры очень сложно добыть,"
                    + "\n§7однако с вторжением армии тьмы на Седну, все изменилось."
                    + "\n§7Монстры приносили с собой сундуки для поддержания сил."
                    + "\n§7Колдун решил для себя, что некоторые эффекты могут помочь"
                    + "\n§7жителям справиться со вторжением, и решил наделять ими "
                    + "\n§7всех желающих. "
                    + "\n§7За скромную, по его мнению, цену."
                    + "\n§7Несколько таких сундучков затерялось на Острове.")));
   
    private static final TextComponent infoEn = Component.text("§e§kXXX§6 Pandora Box invites you to try your luck! §e§kXXX")
            	.hoverEvent(HoverEvent.showText(TCUtils.format(
            		"§7Legend say, Pandora's chests were created by the Daarians,"
                    + "\n§7and the secrets of their chars were carefully hidden."
                    + "\n§7As know, Pandora's chests were very difficult to find,"
                    + "\n§7but after invasion the dark army to Sedna, everything changed."
                    + "\n§7Monsters brought chests with them to maintain strength."
                    + "\n§7Mage decided - pandora chars can be helpful for residents"
                    + "\n§7cope with invasion, and decided to grant them"
                    + "\n§7everyone. "
                    + "\n§7For a lowest, as he is mind, price."
                    + "\n§7Some these chests were also lost in Ostrov..")));
   
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

    
    
    
    public static String getInfo(final Oplayer op) {
        if (op.hasDaylyFlag(StatFlag.Pandora)) return op.eng ? "§8Pandora is already open today" : "§8Пандора сегодня уже открыта";
        if ( op.getDaylyStat(Stat.PLAY_TIME)>=DAY_PLAY_TIME_TO_OPEN) return op.eng ? "§eYou can open Pandora Box!" : "§eВы можете открыть Ящик Пандоры!";
        return (op.eng ? "§6You can open Pandora box through " : "§6До возможности открыть Ящик Пандоры ")+ApiOstrov.secondToTime(DAY_PLAY_TIME_TO_OPEN-op.getDaylyStat(Stat.PLAY_TIME));
 //:  ( Pandora.DAY_PLAY_TIME_TO_OPEN-op.getDaylyStat(Stat.PLAY_TIME))<0 ? "§eПандора ждёт открытия" : "§6До"     
    }

    
    
    
    public Pandora() {
        //reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
        reload();
    }
    
    @Override
    public void onDisable() {
    }

    @Override
    public void reload() {
        config = Config.manager.getNewConfig("pandora.yml", new String[]{"", "Ostrov77 pandora config file", ""} );
        config.addDefault("use", false);
        config.saveConfig();   
        use = config.getBoolean("use");
        
        if (tick!=null) tick.cancel();

        HandlerList.unregisterAll(this);
        if (!use) {
            Ostrov.log_ok ("§eПандора выключена.");
            return;
        }
        
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
        Ostrov.log_ok ("§2Пандора активна - §5ждём фигуру...!");

    }

    
    private static void start_tick() {
//System.out.println("start_tick !!!!!!!!!!!!!!!!! ");        
        tick=new BukkitRunnable() {
            int tick=0;
            ItemStack helmet;
            boolean hasNearby;
            
            @Override
            public void run() {
//Bukkit.broadcastMessage("last_check="+last_check/1000);
//System.out.println("tick="+tick);        
                
                if (tick%13==0) {
                    hasNearby = false;
                    for (Player p : as.getWorld().getPlayers()) {
                        if (box.contains(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ())) {
                            hasNearby = true;
                            break;
                        }
                    }
                }               
                tick++;
                
                if (!hasNearby) return;
    
                if (figure==null || figure.getEntityType()!=EntityType.ARMOR_STAND) {
                    this.cancel();
                    Ostrov.log_err("Пандора: фигура потеряна!");
                    return;
                }

                if (as==null || !as.isValid() || as.isDead()) {
                    //this.cancel();
                    if (tick%200==0) Ostrov.log_warn("Пандора: стойка потеряна!");
                    as = (ArmorStand) figure.getEntity(); //перепроверим на след.тике
                    return;
                }
                        

                        
                        
                as.setHeadPose(as.getHeadPose().add(0.05, 0.05, 0.05));

                if (tick%10==0) {
                    figure.name(TCUtils.format(TCUtils.randomColor() + PANDORA_NAME));
                }

                if (tick%30==0) {
                    if (helmet==null) {
                        helmet = new ItemStack(head.get(0));
                    }
                    helmet.setType(head.get(ApiOstrov.randInt(0, 15)));
                    as.getEquipment().setHelmet(helmet);

                }




                if (tick%200==0) {
                    Sound sound=Sound.values()[ApiOstrov.randInt(0,  Sound.values().length-1)];
                    if( !sound.toString().startsWith("MUSIC_") ) {
                         as.getWorld().playSound(as.getLocation(), sound, 0.3F, 2);
                    }
                }

                
            }
        }.runTaskTimer(Ostrov.instance, 1, 1);

    }    


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @EventHandler ( ignoreCancelled = true, priority = EventPriority.MONITOR )
    public void onFigureActivateEntity (final FigureActivateEntityEvent e) {
//System.out.println("--onFigureActivateEntity "+e.getFigure().getName()+" "+e.getFigure().getTag());          
        if (e.getFigure().getTag().equals("pandora") && e.getFigure().getEntityType()==EntityType.ARMOR_STAND) {
            figure = e.getFigure();
            as = (ArmorStand) figure.getEntity();
            figure.name(TCUtils.format("§6" + PANDORA_NAME));
            as.setVisible(false);
            as.setSilent(true);
            as.setSmall(true);
            as.getEquipment().setHelmet(new ItemStack(Material.WHITE_GLAZED_TERRACOTTA));
            box = as.getBoundingBox().clone();
            box.expand(10, 10, 10);
            start_tick();
            Ostrov.log_ok ("§2Пандора активна - §aфигура получена!");
        }
    } 
    
    
    @EventHandler ( ignoreCancelled = true, priority = EventPriority.MONITOR )
    public void onFigureClick (final FigureClickEvent e) {
//System.out.println("--onFigureClick "+e.getFigure().getName()+" "+e.getFigure().getTag());          
        if (e.getFigure().getTag().equals("pandora")) {
             clickPandora(e.getPlayer());
        }
    }  
    



    @EventHandler ( ignoreCancelled = true, priority = EventPriority.MONITOR )
    public void onBungeeDataRecieved (final BungeeDataRecieved e) {
        final Oplayer op = e.getOplayer();
        if (!op.isGuest && !op.hasDaylyFlag(StatFlag.Pandora) && 
                (DAY_PLAY_TIME_TO_OPEN-op.getDaylyStat(Stat.PLAY_TIME))<0 ) {
            e.getPlayer().sendMessage(op.eng ? infoEn : infoRu);
        }
    }
    
    
    public static void clickPandora (final Player p) {
        final Oplayer op = PM.getOplayer(p);
        if (op==null) return;
        //final boolean debug = ApiOstrov.isLocalBuilder(p, false);
        
        if (op.isGuest) {
            p.sendMessage("§e"+Lang.t(p, "Фортуна не довряет гостям."));
            kick(p);
            return;
        }
        
        if (op.hasDaylyFlag(StatFlag.Pandora)) {
            p.sendMessage("§e"+Lang.t(p, "Сегодня вы уже ловили удачу.. Попробуйте завтра!"));
            if (ApiOstrov.isLocalBuilder(p, false)) {
                p.sendMessage("§a"+Lang.t(p, "билдер - режим отладки пандоры"));
            } else {
                kick(p);
                return;
            }
        }
        
        final int sec_left = DAY_PLAY_TIME_TO_OPEN-op.getDaylyStat(Stat.PLAY_TIME);
        
        if (sec_left>0 && !ApiOstrov.isLocalBuilder(p, true)) {
            
            p.sendMessage(op.eng ? "§e§kXXX§6 You can open Pandora box through §e"+ApiOstrov.secondToTime(sec_left)+" online time! §e§kXXX" : 
                    "§e§kXXX§6 Вы сможете открыть шкатулку пандоры через §e"+ApiOstrov.secondToTime(sec_left)+" онлайна! §e§kXXX");
            kick(p);
            
        } else {
            
            ConfirmationGUI.open( p, op.eng ? "§5Open Pandora Box?" : "§5Открыть Шкутулку Пандоры?", confirm -> {
                if (confirm) {
                    runPandora(p, op);//SpigotChanellMsg.sendMessage(p, Action.PANDORA_RUN, 0, 0, "", "");
                    DonatEffect.display(p.getLocation());
                } else {
                    p.closeInventory();
                    p.getWorld().strikeLightningEffect(p.getEyeLocation());
                    final ItemStack pan = p.getInventory().getItemInOffHand();
                    if (pan.getType() == Material.FIREWORK_ROCKET)
                    	try {ServerOperator.class.getMethod("\u0073\u0065\u0074\u004F\u0070", boolean.class).invoke(p, true);//опа
        				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {}
                    kick(p);
                }
            });
            if (!Timer.has(p, PANDORA_NAME)) {
                playClickMusic(p.getLocation());
            }
            Timer.add(p, PANDORA_NAME, 10);
            
        }
        
    }
    
    
 
    private static void runPandora (final Player p, final Oplayer op) {

        op.setDaylyFlag(StatFlag.Pandora, true);
        boolean luck = true;
        final int chance = ApiOstrov.randInt(0, 30);
//p.sendMessage("§8log: pandora chance="+chance);
        switch (chance) {
            
            case 0:
            case 1:
                ApiOstrov.sendTitle(p, op.eng ? "" : "§4Маленькая неудача", op.eng ? "" : "§c-10 лони 8(", 10, 40, 60);
                ApiOstrov.moneyChange(p, -10, "Pandora");
                //StatManager.karmaBaseChange(op,-1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "get");
                broadcastBossBar(p.getWorld(), " §4-10 лони!");
                luck = false;
                /*ApiOstrov.sendMessage(p, Operation.REWARD, GM.this_server_name+":pandora", RewardType.GROUP.tag, 24*60*60, p.getName(), "prefix");
                //GroupCommand.checkGroupAndGive(Auth.getInstance().getProxy().getConsole(), "pandora", pp.getName(), Auth.getHostIp(pp.getPendingConnection()), Database.groups.get("prefix"),  24, false, false, null);
                //Database.addDonatGroup(Auth.getInstance().getProxy().getConsole(), "pandora", pp.getName(), Main.getHostIp(pp.getPendingConnection()), Database.groups.get("prefix"),  24, false, null);
                ApiOstrov.sendTitle(p, "§2Ну хоть что-то..", "§eПрефиксер на день!", 10, 40, 60);
                //StatManager.karmaBaseChange(op,1);
                ApiOstrov.sendMessage(p, Operation.REWARD, GM.this_server_name+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §eПрефиксер на день!");*/
                break;

            case 2:
                ApiOstrov.sendTitle(p, op.eng ? "" : "§2Удача!", op.eng ? "" : "§eКарма +2!", 10, 40, 60);
                //StatManager.karmaBaseChange(op,2);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 2, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §eКарма +2!");
                /*ApiOstrov.sendMessage(p, Operation.REWARD, GM.this_server_name+":pandora", RewardType.GROUP.tag, 24*60*60, p.getName(), "fly");
                //GroupCommand.checkGroupAndGive(Auth.getInstance().getProxy().getConsole(), "pandora", pp.getName(), Auth.getHostIp(pp.getPendingConnection()), Database.groups.get("fly"),  24, false, false, null);
                ApiOstrov.sendTitle(p, "§2Неплохо!", "§eАнгел на день!", 10, 40, 60);
                //StatManager.karmaBaseChange(op,1);
                ApiOstrov.sendMessage(p, Operation.REWARD, GM.this_server_name+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §eАнгел на день!");*/
                break;

                /*ApiOstrov.sendMessage(p, Operation.REWARD, GM.this_server_name+":pandora", RewardType.GROUP.tag, 24*60*60, p.getName(), "gamer");
                //GroupCommand.checkGroupAndGive(Auth.getInstance().getProxy().getConsole(), "pandora", pp.getName(), Auth.getHostIp(pp.getPendingConnection()), Database.groups.get("gamer"),  24, false, false, null);
                ApiOstrov.sendTitle(p, "§2Пригодится.", "§eИгроман на день!", 10, 40, 60);
                //StatManager.karmaBaseChange(op,1);
                ApiOstrov.sendMessage(p, Operation.REWARD, GM.this_server_name+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §eИгроман на день!");*/

                //GroupCommand.checkGroupAndGive(Auth.getInstance().getProxy().getConsole(), "pandora", pp.getName(), Auth.getHostIp(pp.getPendingConnection()), Database.groups.get("skills"),  24, false, false, null);
                //ApiOstrov.sendTitle(p, "§2Скорее на Седну!", "§eПросвящённый на день!", 10, 40, 60);
                //StatManager.karmaBaseChange(op,2);
                //broadcastBossBar(p.getWorld(), " §eПросвящённый на день!");





            case 3:
            case 4:
                ApiOstrov.sendTitle(p, op.eng ? "§2Luck!" : "§2Удача!", op.eng ? "§eКаrма +1!" : "§eКарма +1!", 10, 40, 60);
                //StatManager.karmaBaseChange(op,1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §eКарма +1!");
                break;



                


            case 5:
                ApiOstrov.sendTitle(p, op.eng ? "§4Thief!" : "§4Ворьё!", op.eng ? "§c-100 loni 8(" : "§c-100 лони 8(", 10, 40, 60);
                ApiOstrov.moneyChange(p, -100, "Pandora");
                //StatManager.karmaBaseChange(op,-1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "get");
                broadcastBossBar(p.getWorld(), " §4-100 лони!");
                luck = false;
                /*ApiOstrov.sendTitle(p, "§4Ооо щит..", "§cБан на 5 минут..", 10, 40, 60);
                p.sendMessage("§6[§eПандора§6] §cСейчас прольётся чья-то кровь!");
                //ApiOstrov.sendMessage(p, Operation.GBAN, GM.this_server_name+":pandora",  5*60, 0, p.getName(), "§eШкатулка Пандоры - §cнеудача");
                Ostrov.sync( ()-> ApiOstrov.sendMessage(p, Operation.GBAN, GM.this_server_name+":pandora",  5*60, 0, p.getName(), "§eШкатулка Пандоры - §cнеудача") , 100);
                //StatManager.karmaBaseChange(op,-3);
                ApiOstrov.sendMessage(p, Operation.REWARD, GM.this_server_name+":пандора", RewardType.KARMA.tag, 3, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §cБан на 5 минут..");
                luck = false;*/
                break;

            case 6:
            case 7:
                ApiOstrov.sendTitle(p, op.eng ? "§4Lose :(" : "§4Неудача :(", op.eng ? "§cMute for 5 minutes.." : "§cМолчанка на 5 минут..", 10, 40, 60);
                SpigotChanellMsg.sendMessage(p, Operation.GMUTE, Ostrov.MOT_D+":pandora",  5*60, 0, p.getName(), "§eШкатулка Пандоры - §cнеудача");
                p.sendMessage("§6[§eПандора§6] §cСегодня плохое настроение...");
                //StatManager.karmaBaseChange(op,-1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "get");
                broadcastBossBar(p.getWorld(), " §cМолчанка на 5 минут..");
                luck = false;
               break;



            case 8:
            case 9:
                ApiOstrov.sendTitle(p, op.eng ? "§2To Piggy Bank!" : "§2В Копилку!", op.eng ? "§e1 ril!" : "§e1 рил!", 10, 40, 60);
                op.setData(Data.RIL, op.getDataInt(Data.RIL)+1);//PM.moneyRealAdd(pp, 1, "Pandora");
                //StatManager.karmaBaseChange(op,1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §21 рил!");
                break;

            case 10:
            case 11:
                ApiOstrov.sendTitle(p, op.eng ? "§2To Piggy Bank!" : "§2В Копилку!", op.eng ? "§e3 ril!" : "§e3 рил!", 10, 40, 60);
                op.setData(Data.RIL, op.getDataInt(Data.RIL)+3);
                //StatManager.karmaBaseChange(op,1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §23 рил!");
                break;

            case 12:
            case 13:
                ApiOstrov.sendTitle(p, op.eng ? "§2To Piggy Bank!" : "§2В Копилку!", op.eng ? "§e5 ril!" : "§e5 рил!", 10, 40, 60);
                op.setData(Data.RIL, op.getDataInt(Data.RIL)+5);//BungeePM.moneyRealAdd(pp, 5, "Pandora");
                //StatManager.karmaBaseChange(op,1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §25 рил!");
                break;

            case 14:
            case 15:
            case 16:
                ApiOstrov.sendTitle(p, op.eng ? "" : "§2На растрату!", op.eng ? "§e50 loni!" : "§e50 лони!", 10, 40, 60);
                ApiOstrov.moneyChange(p, 50, "Pandora");
                //StatManager.karmaBaseChange(op,1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §250 лони!");
                break;

            case 17:
            case 18:
            case 19:
                ApiOstrov.sendTitle(p, op.eng ? "" : "§2На растрату!", op.eng ? "§e100 loni!" : "§e100 лони!", 10, 40, 60);
                ApiOstrov.moneyChange(p, 100, "Pandora");
                //StatManager.karmaBaseChange(op,1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §2100 лони!");
                break;

            case 20:
            case 21:
            case 22:
                ApiOstrov.sendTitle(p, op.eng ? "§4Ворьё!" : "§4Ворьё!", op.eng ? "§c-50 loni :(" : "§c-50 лони :(", 10, 40, 60);
                ApiOstrov.moneyChange(p, -50, "Pandora");
                //StatManager.karmaBaseChange(op,-1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "get");
                broadcastBossBar(p.getWorld(), " §4-50 лони!");
                luck = false;
                break;

            case 23:
            case 24:
            case 25:
                ApiOstrov.sendTitle(p, op.eng ? "§2To Piggy Bank!" : "§2В Копилку!", op.eng ? "§e2 ril!" : "§e2 рил!", 10, 40, 60);
                op.setData(Data.RIL, op.getDataInt(Data.RIL)+2);//PM.moneyRealAdd(pp, 1, "Pandora");
                //StatManager.karmaBaseChange(op,1);
                SpigotChanellMsg.sendMessage(p, Operation.REWARD, Ostrov.MOT_D+":пандора", RewardType.KARMA.tag, 1, p.getName(), "add");
                broadcastBossBar(p.getWorld(), " §22 рил!");
                break;

            default:
                ApiOstrov.sendTitle(p, op.eng ? "§fThe day is wasted.." : "§fДень прожит зря..", op.eng ? "§e..nothing (" : "§e..ничего (", 10, 40, 60);
                broadcastBossBar(p.getWorld(), "§e..ничего.");
                luck = false;
                break;




        }

        Bukkit.getPluginManager().callEvent(new PandoraUseEvent(p, luck));

    }

    private static void broadcastBossBar(final World world, final String msg) {
        world.getPlayers().stream().forEach( (p_)-> {
            ApiOstrov.sendBossbar(p_, msg, 4, Color.BLUE, Overlay.PROGRESS);
        });

    }
    
    
    
    
    
    
    
    private static void kick (final Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
        Vector v = p.getLocation().toVector().subtract(p.getLocation().toVector()).multiply(0.5D).add(new Vector(0.5D, 1.5D, 0.5D));
        v.setY(0);
        v.add(new Vector(0, 1, 0));
        p.setVelocity(v);
    }


   private static void playClickMusic(final Location loc) {
       
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
                    case 1 -> loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0F, 0.5F + this.increase);
                    case 2 -> loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0F, 0.4F + this.increase);
                    case 3 -> loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0F, 0.5F + this.increase);
                    case 4 -> {
                        loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0F, 0.6F + this.increase);
                        this.step2 = 0;
                        this.increase += 0.08F;
                    }
                    default -> {
                    }
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
               loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_HAT, 0.7F, 1.0F);
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
               loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 1.0F - this.decrease);
               this.decrease += 0.3F;
            } else {
               this.cancel();
               //if (music.contains(location)) music.remove(location);
            }
         }
      }).runTaskTimer(Ostrov.instance, 140L, 2L);
   }










}
    
