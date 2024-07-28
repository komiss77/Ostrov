package ru.komiss77;


import com.destroystokyo.paper.ClientOption;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.enums.Module;
import ru.komiss77.enums.*;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.listener.ResourcePacksLst;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.player.profile.StatManager;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.objects.DelayBossBar;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.TeleportLoc;
import ru.komiss77.version.Nms;

import java.sql.Connection;
import java.time.Duration;
import java.util.*;
import java.util.stream.StreamSupport;


public class ApiOstrov {
    
    private static final String PATTERN_ENG = "[A-Za-z_]";
    private static final String PATTERN_ENG_NUM = "\\w"; //[A-Za-z0-9_]";
    private static final String PATTERN_ENG_RUS = "[A-Za-zА-Яа-я_]";
    private static final String PATTERN_ENG_NUM_RUS = "[A-Za-z0-9А-Яа-я_]";
      
    public static Initiable getModule(final Module module) {
        return Ostrov.getModule(module);
    }

    //всё по Оплееру
    public static int getStat(final Player p, final Stat e_stat) {
        return PM.exists(p.getUniqueId()) ? PM.getOplayer(p).getStat(e_stat) : 0;
    }
    @Deprecated
    public static int getDaylyStat(final Player p, final Stat e_stat) {
        return getDailyStat(p, e_stat);
    }
    public static int getDailyStat(final Player p, final Stat e_stat) {
        return PM.exists(p.getUniqueId()) ? PM.getOplayer(p).getDailyStat(e_stat) : 0;
    }
    public static void addStat(final Player p, final Stat e_stat) {
        addStat(p, e_stat, 1);
    }
    public static void addStat(final Player p, final Stat e_stat, final int ammount) {
        StatManager.addStat(p, e_stat, ammount);
    }
    /**
     *
     * @param p
     * @param customStatName
     * отправить добавление локальной статы (для выплат лони и missionsManager)
     */
    public static void addCustomStat(final Player p, final String customStatName) {
        addCustomStat(p, customStatName, 1);
    }
    /**
     *
     * @param p
     * @param customStatName
     * @param ammount
     * отправить добавление локальной статы (для выплат лони и missionsManager)
     */
    public static void addCustomStat(final Player p, final String customStatName, final int ammount) {
        //sendMessage(p, Operation.ADD_CUSTOM_STAT, p.name(), ammount, customStatName);
        final Oplayer op = PM.getOplayer(p);
        if (op!=null) {
            StatManager.onCustomStat(p, op, customStatName, ammount);
            MissionManager.onCustomStat(op, customStatName, ammount, false);
        }
    }
    /**
     *
     * @param p
     * @param customStatName
     * @param value
     * отправить ДОСТИЖЕНИЕ локальной статы на банжи в missionsManager
     * достижение может убавляться от вызова к вызову,
     * но выполнением будет считаться значение ammount>=число в условии
     * переданное хоть один раз
     */
    public static void reachCustomStat(final Player p, final String customStatName, final int value) {
      if (StatManager.DEBUG) Ostrov.log("reachCustomStat "+(p==null?"null":p.getName())+" stat="+customStatName+" val="+value);
      //sendMessage(p, Operation.ADD_CUSTOM_STAT, p.name(), ammount, customStatName);
      final Oplayer op = PM.getOplayer(p);
      if (op!=null) MissionManager.onCustomStat(op, customStatName, value, true);
    }

    public static void addExp(final Player p, final int ammount) {
        final Oplayer op = PM.getOplayer(p);//StatManager.addExp(PM.getOplayer(p), ammount);
        if (op!=null) op.addExp(p, ammount);
    }


  /*  public static String getPlayTime(final Player p){
        final Oplayer op = PM.getOplayer(p);
        return op==null ? "0" : secondToTime(op.getStat(Stat.PLAY_TIME) );
    }
    public static String getPrefix(final Player p){
        final Oplayer op = PM.getOplayer(p);
        return op==null ? "" : op.getDataString(Data.PREFIX);
    }
    public static String getSuffix(final Player p){
        final Oplayer op = PM.getOplayer(p);
        return op==null ? "" : op.getDataString(Data.SUFFIX);
    }*/
    /**
     *
     * @param name ник. Возвращает true если у игрока активен режим боя. Так же, можно использовать BattleModeEvent и BattleModeEndEvent
     * @return
     */
    public static boolean inBattle (String name)  {
        return PM.inBattle(name);
    }

    public static void giveMenuItem(final Player p) {
        MenuItemsManager.giveItem(p, "pipboy");//ItemUtils.Add_to_inv(p, 8, ItemUtils.pipboy, true, false);
    }

    public static boolean hasResourcePack(final Player p) {
        if (ResourcePacksLst.use) {
            final Oplayer op = PM.getOplayer(p);
            return op==null || !op.resourcepack_locked;//ResourcePacks.Текстуры_утановлены(p);
        } else {
            return true;
        }
    }

    @Deprecated
    public static boolean hasPermission(final String worldName, final String nik, final String perm) {
        final Oplayer op = PM.getOplayer(nik);
        return op!=null && Perm.hasPermissions(op, worldName, perm);
    }

    public static boolean hasPerm(final Player pl, final String perm) {
      final Oplayer op = PM.getOplayer(pl);
      return op!=null && Perm.hasPerms(op, perm);
    }












    // друзья команды
    public static boolean hasParty(final Player p) {
        final Oplayer op = PM.getOplayer(p.getUniqueId());
        return op!=null && !op.getPartyMembers().isEmpty();//Ostrov.api_friends!=null && ApiFriends.hasParty(p);
    }
    public static boolean isInParty(final Player p1, final Player p2) {
        return PM.exists(p1.getUniqueId()) && !PM.getOplayer(p1.getUniqueId()).getPartyMembers().contains(p2.getName()) ||
                PM.exists(p2.getUniqueId()) && !PM.getOplayer(p2.getUniqueId()).getPartyMembers().contains(p1.getName());//Ostrov.api_friends!=null && ApiFriends.isInParty(p1,p2);
    }
    public static List<String> getPartyPlayers(final Player p) {
        if (!PM.exists(p.getUniqueId())) return new ArrayList<>();
        else return new ArrayList<>(PM.getOplayer(p.getUniqueId()).getPartyMembers());
    }
    public static String getPartyLeader(final Player p) {
        if (!PM.exists(p.getUniqueId())) return "";
        else return PM.getOplayer(p.getUniqueId()).party_leader;
    }
    public static boolean isPartyLeader(final Player p) {
        return PM.exists(p.getUniqueId()) && PM.getOplayer(p.getUniqueId()).isPartyLeader();
    }
    public static boolean isFriend(final Player p1, final Player p2) {
        return PM.exists(p1) && PM.getOplayer(p1).friends.contains(p2.getName());
    }
    @Deprecated
    public static boolean isFriend(final String p1, final String p2) {
        return PM.exist(p1) && PM.getOplayer(p1).friends.contains(p2);
    }




    public static boolean isNewDay() { //после рестарта определить, настал ли новый день
        return Ostrov.newDay;
    }





    /**
     *
     * @param target игрок
     * @param server название сервера, как в настройках bungeecord
     * @param arena название арены на сервере для вызова ArenaJoinEvent в плагине bsign
     */
    public static void sendToServer(final Player target, final String server, String arena) {
//Ostrov.log("sendToServer server="+server+" arena="+arena);
        if (server.equalsIgnoreCase(Ostrov.MOT_D)) {
            Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick ( target, arena ) );
        } else {
            SpigotChanellMsg.sendMessage(target, Operation.SEND_TO_ARENA, target.getName(), 0, 0, server, arena);
        }
    }

     public static Connection getLocalConnection() {
        return LocalDB.getConnection();
    }

    public static Connection getOstrovConnection() {
        return OstrovDB.getConnection();
    }











    public static boolean teleportSave(final Player p, final Location feetLoc, final boolean buildSavePlace) {
//Ostrov.log("teleportSave feetBlock="+feetLoc);
//сначала попытка коррекций +1..-1 из-за непоняток с точкой в ногах или под ногами
        if (!Bukkit.isPrimaryThread()) {
            Ostrov.sync(()->teleportSave(p, feetLoc, buildSavePlace));
            return true;
        }

//        if (!new PlayerTeleportEvent(p, p.getLocation(), feetLoc, PlayerTeleportEvent.TeleportCause.PLUGIN).callEvent()) {
//          p.sendMessage(Ostrov.prefixWARN + "§cТелепорт был отменен!");
//          return false;
//        }

        if (p.getGameMode()==GameMode.CREATIVE || p.getGameMode()==GameMode.SPECTATOR) {
            p.teleport(feetLoc, PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
        }

        final World w = feetLoc.getWorld();

        if (w.getEnvironment() == World.Environment.NETHER) { //фикс - тэпешит над верхним бедроком
          int y_max =  w.getHighestBlockYAt(feetLoc.getBlockX(), feetLoc.getBlockZ())-3;
          if (feetLoc.getBlockY()>y_max) {
            feetLoc.setY(y_max);
          }
        }
        final int x = feetLoc.getBlockX();
        int feet_y = feetLoc.getBlockY();
        final int y_ori = feet_y;
        final int z = feetLoc.getBlockZ();



        Material headMat = Nms.getFastMat(w, x, feet_y+1, z);
        Material feetMat = Nms.getFastMat(w, x, feet_y, z);
        Material downMat = Nms.getFastMat(w, x, feet_y-1, z);



        //проверка указанного места
        boolean safe = TeleportLoc.isSafePlace(headMat, feetMat, downMat);

        //проверка на блок выше
        if (!safe) {
            feet_y = y_ori+1;//feetLoc.add(0, 1, 0);
            final  Material upHead = Nms.getFastMat(w, x, y_ori+2, z);
            safe =  TeleportLoc.isSafePlace(upHead, headMat, feetMat);
            //LocationUtil.isPassable(upHead) && LocationUtil.isPassable(headMat)  && (LocationUtil.canStand(feetMat) || feetMat==Material.WATER);
            if (safe) downMat = feetMat; //если норм, прописать что под ногами в таком варианте
        }

        //проверка на блок ниже
        if (!safe) {
            feet_y = y_ori-1;//feetLoc.subtract(0, 2, 0);
            final Material subDown = Nms.getFastMat(w, x, y_ori-2, z);
            safe =  TeleportLoc.isSafePlace(feetMat, downMat, subDown);
            //safe = LocationUtil.isPassable(feetMat)  && LocationUtil.isPassable(downMat) && (LocationUtil.canStand(subDown) || subDown==Material.WATER);
            if (safe) downMat = subDown; //если норм, прописать что под ногами в таком варианте
        }

        //сканируем с самого верха до самого низа
        if (!safe) {
            final boolean nether = w.getEnvironment()==World.Environment.NETHER;
            feet_y = w.getMaxHeight()-2;
            for (; feet_y>w.getMinHeight()+1; feet_y--) {
                //в аду или при генерации как в аду (определяем потолок из бедрока)

                headMat = feetMat; //VM.getNmsServer().getFastMat(w, x, y-1, z);
                feetMat = downMat;//VM.getNmsServer().getFastMat(w, x, y, z);
                downMat = Nms.getFastMat(w, x, feet_y-1, z);
//Ostrov.log("find y="+y+" "+headMat+" "+feetMat+" "+downMat);
              if ( (nether || feet_y>0) && downMat==Material.BEDROCK ) {
                continue;
              }                //если над нижним блоком нет 2 блока для тела, пропускаем ниже
                //if (!LocationUtil.isPassable(headMat) || !LocationUtil.isPassable(feetMat)) {
                //    continue;
                //}
                //if (LocationUtil.canStand(downMat) || downMat==Material.WATER) { //вода или подходит для стояния - сойдёт
                feetLoc.setY(feet_y);
                if (TeleportLoc.isSafePlace(headMat, feetMat, downMat)) { //вода или подходит для стояния - сойдёт
                    safe = true;
                    break;
                }
            }
        }

        if (safe) {
            feetLoc.setY(feet_y+0.6);
//Ostrov.log("safe feetLoc="+feetLoc);
            if (downMat==Material.WATER) { //была найдена поверхность воды - ставим кувшинку
//Ostrov.log("WATER!!!!"+feetLoc.getBlock().getType()+"->LILY_PAD");
                feetLoc.getBlock().setType(Material.LILY_PAD);
                new BukkitRunnable () { //чтобы не давало новое ТПР пока не сошел с места
                    final String name = p.getName();
                    @Override
                    public void run() {
                        final Player pl = Bukkit.getPlayerExact(name);
                        if (pl==null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX()!=x || pl.getLocation().getBlockZ()!=z) {
                            this.cancel();
                            feetLoc.getBlock().setType(Material.AIR);
                        }
                    }
                }.runTaskTimer(Ostrov.instance, 30, 10 );
            }

        } else if (buildSavePlace) {

            feet_y = (y_ori > w.getMinHeight()+2 && y_ori > w.getMaxHeight()-2) ? y_ori  : w.getSpawnLocation().getBlockY();
            feetLoc.setY(feet_y+0.5);

            final Block upHeadBlock = feetLoc.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP);
            final Block headBlock = feetLoc.getBlock().getRelative(BlockFace.UP);
            final Block feetBlock = feetLoc.getBlock();
            final Block downBlock = feetLoc.getBlock().getRelative(BlockFace.DOWN);
            final Material upHeadMat = upHeadBlock.getType(); //headMat, feetMat, downMat уже есть выше
            final Material headMat1 = headMat;
            final Material feetMat1 = headMat;
            final Material downMat1 = headMat;

            new BukkitRunnable () { //чтобы не давало новое ТПР пока не сошел с места
                final String name = p.getName();
                @Override
                public void run() {
                    final Player pl = Bukkit.getPlayerExact(name);
                    if (pl==null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX()!=x || pl.getLocation().getBlockZ()!=z) {
                        this.cancel();
                        upHeadBlock.setType(upHeadMat);
                        headBlock.setType(headMat1);
                        feetBlock.setType(feetMat1);
                        downBlock.setType(downMat1);
                    }
                }
            }.runTaskTimer(Ostrov.instance, 30, 10 );

//Ostrov.log("!safe y_ori="+y+" feetLoc="+feetLoc);
            upHeadBlock.setType(Material.GLASS);
            headBlock.setType(Material.AIR);
            feetBlock.setType(Material.AIR);
            downBlock.setType(Material.GLASS);
            p.setVelocity(p.getVelocity().zero());
            p.setFallDistance(0);

        } else {
            feetLoc.setY(y_ori+0.5);
        }

        p.teleport(feetLoc, PlayerTeleportEvent.TeleportCause.COMMAND);
        return true;
    }

    //ентити

    public static @Nullable LivingEntity lastDamager(final LivingEntity ent, final boolean owner) {
      return getDamager(ent.getLastDamageCause(), owner);
    }

    public static @Nullable LivingEntity getDamager(final EntityDamageEvent e, final boolean owner) {
      if (e instanceof final EntityDamageByEntityEvent ev) {
        if (ev.getDamager() instanceof Projectile && ((Projectile) ev.getDamager()).getShooter() instanceof final LivingEntity le) {
          if (le instanceof final Tameable tm && owner) {
            return tm.getOwner() instanceof HumanEntity ? ((HumanEntity) tm.getOwner()) : null;
          } else return le;
        } else if (ev.getDamager() instanceof final LivingEntity le) {
          if (le instanceof final Tameable tm && owner) {
            return tm.getOwner() instanceof HumanEntity ? ((HumanEntity) tm.getOwner()) : null;
          } else return le;
        }
      }
      return null;
    }






    //   деньги

  private static final int SMALL = 9;

    /**
     *
     * @param target только онлайн игроки!
     * @param value изменение, если убавить, то с минусом
     * @param source источник
     */
    public static void moneyChange ( final Player target, final int value, final String source ) {
        final Oplayer targetOp = PM.getOplayer(target.getUniqueId());
        targetOp.setData(Data.LONI, targetOp.getDataInt(Data.LONI)+value);//moneySet(curr+value, send_update);
//System.out.println("--moneyChange Data.MONEY="+getIntData(Data.MONEY));
        if (value>SMALL || value<-SMALL) { //по копейкам не уведомляем
            target.sendMessage(TCUtils.form(Ostrov.PREFIX+"§7"+(value>9?"Поступление":"Расход")+" средств: "+source+" §7-> "+(value>9?"§2":"§4")+value+" "+Ostrov.L+" §7! §8<клик-баланс")
            .hoverEvent(HoverEvent.showText(TCUtils.form("§5Клик - сколько стало?")))
            .clickEvent(ClickEvent.runCommand("/money balance")));
        }
    }
    /**
     *
     * @param name ник. (в разработке-Если оффлайн, добавится при входе)
     * @param value изменение, если убавить, то с минусом
     * @param who кто изменяет
     */
    public static void moneyChange( final String name, final int value, final String who ) {
      final Player pl = Bukkit.getPlayer(name);
        if (pl == null) {
          LocalDB.moneyOffline(name, value, who);
        } else {//запомнить и дать при входе - оффлайн перевод
          moneyChange(pl, value, who);
        }
    }
    public static int moneyGetBalance ( final String name ) {
      final Player pl = Bukkit.getPlayer(name);
      if (pl == null) return 0;
      final Oplayer op = PM.getOplayer(pl);
      return op==null ? 0 : op.getDataInt(Data.LONI);
      //if (PM.exists(name)) return PM.getOplayer(name).getDataInt(Data.LONI);
      //else return 0;
    }

















    // сообщения сохраняются и выводятся поочерёдно
    public static void sendTitle(final Player p, final String title, final String subtitle) {
        sendTitle(p, title, subtitle, 20, 40, 20);
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendTitle(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout ) {
        final Times times =  Title.Times.times(Duration.ofMillis(fadein* 50L), Duration.ofMillis(stay* 50L), Duration.ofMillis(fadeout* 50L));
    	sendTitle(p, TCUtils.form(title), TCUtils.form(subtitle), times);
    }

    public static void sendTitle(final Player p, final Component title, final Component subtitle, final int fadein, final int stay, final int fadeout  ) {
        final Times times =  Title.Times.times(Duration.ofMillis(fadein* 50L), Duration.ofMillis(stay* 50L), Duration.ofMillis(fadeout* 50L));
        sendTitle(p, title, subtitle, times);
    }
    // сообщения сохраняются и выводятся поочерёдно
    public static void sendTitle(final Player p, final Component title, final Component subtitle, final Times times ) {
        final Oplayer op = PM.getOplayer(p);
        final Title t = Title.title(title, subtitle, times);
        if (op!=null) { //на авторизации нет оплеера!
            if (op.nextTitle > 0) {
                op.delayTitles.add(t);
            } else {
                p.showTitle( t );
                op.nextTitle = times.fadeIn().toSecondsPart()+times.stay().toSecondsPart()+times.fadeOut().toSecondsPart() + 1;
            }
        } else {
            p.showTitle( t );
        }

    }

    public static void sendTitleDirect(final Player p, final String title, final String subtitle) {
    	sendTitleDirect(p, title, subtitle, 20, 40, 20);
    }

    public static void sendTitleDirect(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout ) {
        final Times times = Title.Times.times(Duration.ofMillis(fadein* 50L), Duration.ofMillis(stay* 50L), Duration.ofMillis(fadeout* 50L));
        p.showTitle( Title.title(TCUtils.form(title), TCUtils.form(subtitle), times) );
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendActionBar(final Player p, final String text) {
        final Oplayer op = PM.getOplayer(p);
        if (op!=null) { //на авторизации нет оплеера!
            if (op.nextAb>0) {
                op.delayActionbars.add(text);
            } else {
                op.nextAb = Oplayer.ACTION_BAR_INTERVAL;
                p.sendActionBar(TCUtils.form(text));
            }
        } else {
            p.sendActionBar(TCUtils.form(text));
        }
    }

    public static void sendActionBarDirect(final Player p, final String text) {
        if (p!=null) {
            p.sendActionBar(TCUtils.form(text));
        }
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbar(final Player p, final String text, final int seconds,
    	final BossBar.Color color, final BossBar.Overlay style, final float progress) {
        final Oplayer op = PM.getOplayer(p);
        if (op!=null) {
            if (op.barTime>0) {
                op.delayBossBars.add(new DelayBossBar(text, seconds, color, style, progress, false));
            } else {
                DelayBossBar.apply(op, p, text, seconds, color, style, progress, false);
            }
        }
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbarDirect(final Player p, final String text, final int seconds,
    	final BossBar.Color color, final BossBar.Overlay style, final float progress) {
        final Oplayer op = PM.getOplayer(p);
        if (op!=null) DelayBossBar.apply(op, p, text, seconds, color, style, progress, false);
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbar(final Player p, final String text, final int seconds,
    	final BossBar.Color color, final BossBar.Overlay style) {
        final Oplayer op = PM.getOplayer(p);
        if (op!=null) {
            if (op.barTime>0) {
                op.delayBossBars.add(new DelayBossBar(text, seconds, color, style, 1f, true));
            } else {
                DelayBossBar.apply(op, p, text, seconds, color, style, 1f, true);
            }
        }
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbarDirect(final Player p, final String text, final int seconds,
        final BossBar.Color color, final BossBar.Overlay style) {
        final Oplayer op = PM.getOplayer(p);
        if (op!=null) DelayBossBar.apply(op, p, text, seconds, color, style, 1f, true);
    }

    public static void sendTabList(final Player p, final String header, final String footer) {
        p.sendPlayerListHeaderAndFooter(TCUtils.form(header), TCUtils.form(footer));
    }


    // *****************************************************************************













    //    числа
    public static int randInt(final int num1, final int num2) {
        if (num1==num2) return num1;
        return Math.min(num1, num2) + Ostrov.random.nextInt(FastMath.abs(num2 - num1));
    }

    public static boolean randBoolean() {
        return Ostrov.random.nextBoolean();
    }

    public static int rndSignNum(int init, final int rnd) {
      if (rnd > 0) init += Ostrov.random.nextInt(rnd);
      return Ostrov.random.nextBoolean() ? init : -init;
    }

    public static boolean isInteger(final String i) {
        try {
            Integer.parseInt(i);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Deprecated
    public static int getInteger(final String s) {
      return getInteger(s, Integer.MIN_VALUE);
    }

    public static int getInteger(final String num, final int or) {
      try {
        return Integer.parseInt(num);
      } catch (NumberFormatException ex) {
        return or;
      }
    }

    public static String getPercentBar(final int max, final int current, final boolean withPercent) {
        if (current<0 || current>max) return "§8||||||||||||||||||||||||| ";
//System.out.println("max="+max+" curr="+current);
        final double percent = (double)current / max * 100;
        int p10 = (int) (percent*10);
        final double percent1d = ((double) p10 / 10); //чтобы не показывало 100
        int pos = p10/40;
        //StringBuilder sb = new StringBuilder("§a||||||||||||||||||||||||| ");
        //return sb.insert(pos, "§8").append(percent1d).append("%").toString();
        if (pos<2) pos=2;
        else if (pos>26) pos=26;
        if (withPercent) {
            return new StringBuilder("§a||||||||||||||||||||||||| ").insert(pos, "§8").append("§f").append(percent1d).append("%").toString();
        } else {
            return new StringBuilder("§a||||||||||||||||||||||||| ").insert(pos, "§8").toString();
        }
    }

    public static String secondToTime(int second) { //c днями и нед!
        if (second<0) return "---";
        final int year = second / 30_758_400; //356*24*60*60
        second -= year*30_758_400; //от секунд отнимаем годы
        final int month = second / 2_678_400; //31*24*60*60
        second -= month*2_678_400; //от секунд отнимаем месяцы

        final int week = second / 604_800; //7*24*60*60
        if (year==0) second -= week*604_800; //от секунд отнимаем недели. недели не показываем и не отнимаем, если счёт на года

        final int day = second / 86_400; //24*60*60
        second -= day*86_400; //от секунд отнимаем дни
        final int hour = second / 3600; //60*60
        second-=hour*3600;  //от секунд отнимаем часы
        final int min = second / 60;
        second-=min*60; //от секунд отнимаем минуты

        StringBuilder sb = new StringBuilder();
        if (year>0)  sb.append(year).append("г. ");
        if (month>0)  sb.append(month).append("мес. ");
        if (week>0 && year==0)  sb.append(week).append("нед. ");
        if (day>0) sb.append(day).append("д. ");
        if (year>0) return sb.toString(); //счёт на года - достаточно до дней
        if (hour>0) sb.append(hour).append("ч. ");
        if (month>0 || week>0) return sb.toString(); //счёт на месяца - достаточно до часов
        if (min>0) sb.append(min).append("мин. ");
        if (second>0)   sb.append(second).append("сек. ");
        return sb.toString();
    }

    public static String dateFromStamp(final int stamp_in_second) {
        return Ostrov.dateFromStamp(stamp_in_second);
    }

    public static String getCurrentHourMin() {
        return Ostrov.getCurrentHourMin();
    }










    //   строки

    public static String listToString(final Iterable<?> array, final String splitter) {
      if (array==null) return "";
       /* StringBuilder sb=new StringBuilder();
        array.forEach( (s) -> {
            sb.append(s).append(splitter);
        });
        return sb.toString();*/
      return StreamSupport.stream(array.spliterator(), true)
          .map(Object::toString)
          .reduce( (t, u) -> t + "," + u)
          .orElse("");
    }

    @Deprecated
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String toString( final Collection array, final boolean commaspace) {
      return toString(array, commaspace ? ", " : ",");
    }

    public static <E> String toString(final Collection<E> array, final String separator) {
      if (array==null || array.isEmpty()) return "";
      return array.stream()
        .map(E::toString)
        .reduce( (t, u) -> t + separator + u)
        .orElse("");
    }

    public static String enumSetToString(final Set<?> enumSet) {
        StringBuilder sb=new StringBuilder();
        enumSet.forEach(eNum -> sb.append(eNum.toString()).append(","));
        return sb.toString();//allowRole;
    }









    public static String nrmlzStr(final String s) {
        final char[] ss = s.toLowerCase().toCharArray();
        ss[0] = Character.toUpperCase(ss[0]);
        for (byte i = (byte) (ss.length - 1); i > 0; i--) {
            switch (ss[i]) {
                case '_':
                    ss[i] = ' ';
                case ' ':
                    ss[i + 1] = Character.toUpperCase(ss[i + 1]);
                    break;
                default:
                    break;
            }
        }
        return String.valueOf(ss);
    }

    //   locations
    public static String stringFromLoc(final Location loc) {
        return LocationUtil.toString(loc);
    }
    public static Location locFromString(final String loc_as_string) {
        return LocationUtil.stringToLoc(loc_as_string, false, true);
    }







    public static Block getSignAttachedBlock(final Block b) {
        if (b.getState() instanceof final Sign sign
          && sign.getBlockData() instanceof final WallSign signData) {
              return b.getRelative(signData.getFacing().getOppositeFace());

        }
        return b.getRelative(BlockFace.DOWN);
    }

    @Deprecated //use GM.sendArenaData direct!
    public static void sendArenaData(final String arenaName, final GameState state, final String line0, final String line1, final String line2, final String line3, final String extra, final int playerInGame) {
      //GM.sendArenaData(arenaName, (state==null ? GameState.НЕОПРЕДЕЛЕНО : state), playerInGame, line0, line1, line2, line3, extra);
      GM.sendArenaData(Game.fromServerName(Ostrov.MOT_D), arenaName, (state==null ? GameState.НЕОПРЕДЕЛЕНО : state), playerInGame, line0, line1, line2, line3);
    }

    public static String[] wrap(final String msg, final int length, final String newLine) {
      if (msg.length() < 2) return new String[] {msg};
      final char split = '\n';
      final String line = split + newLine;
      return WordUtils.wrap(msg, length, line, false).substring(1).split(line);
    }

    public static boolean checkString (String message, final boolean allowNumbers, final boolean allowRussian) {
        return checkString(message, false, allowNumbers, allowRussian);
    }

    public static boolean checkString (String message, final boolean allowSpace, final boolean allowNumbers, final boolean allowRussian) {
        if (allowNumbers && allowRussian) {
            message = message.replaceAll(PATTERN_ENG_NUM_RUS, "");
        } else if (allowNumbers) {
            message = message.replaceAll(PATTERN_ENG_NUM, "");
        } else if (allowRussian) {
            message = message.replaceAll(PATTERN_ENG_RUS, "");
        } else {
            message = message.replaceAll(PATTERN_ENG, "");
        }
        return allowSpace ? message.isBlank() :  message.isEmpty() ;
   }

    public static boolean canBeBuilder(final CommandSender cs) {
        //return (cs instanceof ConsoleCommandSender) || cs.isOp() || cs.hasPermission(Bukkit.getServer().getMotd()+".builder") || hasGroup(cs.name(), "supermoder");
        if (cs == null) return false;
        if (!(cs instanceof Player pl) || cs.isOp() || cs.hasPermission("builder") ) return true;
        final Oplayer op = PM.getOplayer(pl);
        return op != null && op.hasGroup("xpanitely");
    }

    public static boolean isStaff(final CommandSender cs) {
      return switch (cs) {
        case null -> false;
        case ConsoleCommandSender cns -> true;
        case Player p -> {
          final Oplayer op = PM.getOplayer(p);
          yield op != null && op.isStaff;
        }
        default -> false;
      };
    }

    public static boolean isLocalBuilder(final CommandSender cs) {
        return isLocalBuilder(cs, false);
    }
    public static boolean isLocalBuilder(final CommandSender cs, final boolean message) {
        switch (cs) {
            case null:
                return false;
            case ConsoleCommandSender cns:
                return true;
            case Player p when canBeBuilder(p)://!! фиксить права в CDM case "gm", или не даст перейти в гм1
                if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                    return true;
                }
                if (message) {
                  final boolean eng = !p.getClientOption(ClientOption.LOCALE).equals("ru_ru");
                  p.sendMessage(TCUtils.form(eng ? "§e*Click on this message - §aenable Builder mode" : "§e*Клик на это сообшение - §aвключить режим Строителя")
                    .hoverEvent(HoverEvent.showText(TCUtils.form(eng ? "§7Click - enable" : "§7Клик - включить")))
                    .clickEvent(ClickEvent.runCommand("/builder")));
                }
              return false;
            default:
              return false;
        }
    }



    public static int generateId() {
        final String createStamp = String.valueOf(System.currentTimeMillis());
        return Integer.parseInt( createStamp.substring(createStamp.length()-8) );  //15868 94042329
    }

    public static boolean isSpyMode(final Player p) {
        return PM.getOplayer(p).spyOrigin != null;//SpyCmd.isSpy(p.name());
    }

    @SuppressWarnings("unchecked")
    public static <G> G rndElmt(final G... arr) {
        return arr[Ostrov.random.nextInt(arr.length)];
    }

    public static <G> G[] shuffle(final G[] ar) {
      int chs = ar.length >> 2;
      if (chs == 0) {
        if (ar.length > 1) {
          final G ne = ar[0];
          ar[0] = ar[ar.length - 1];
          ar[ar.length - 1] = ne;
        }
        return ar;
      }
      for (int i = ar.length - 1; i > chs; i--) {
        final int ni = Ostrov.random.nextInt(i);
        final G ne = ar[ni];
        ar[ni] = ar[i];
        ar[i] = ne;
        chs += ((chs-ni) >> 31) + 1;
      }
      return ar;
    }

    public static String toSigFigs(final double n, final byte sf) {
      final String nm = String.valueOf(n);
      return nm.indexOf('.') + sf + 1 < nm.length() ? nm.substring(0, nm.indexOf('.') + sf + 1) : nm;
    }

    @Deprecated
    public static String toSigFigs(final float n, final byte sf) {
      final String nm = String.valueOf(n);
      return nm.indexOf('.') + sf + 1 < nm.length() ? nm.substring(0, nm.indexOf('.') + sf + 1) : nm;
    }

    public static int currentTimeSec() {
        return Timer.getTime();
    }

    public static void makeWorldEndToWipe(final int afterSecond) {
        WorldManager.makeWorldEndToWipe(afterSecond);
    }

    public static void moveDeny(final PlayerMoveEvent e) {
        if (e.getTo().getY()<e.getFrom().getY()) {
            e.setTo( e.getFrom().add(0, 2, 0) );
        } else {
            e.setTo(e.getFrom());
        }
    }

    //команды на банжик передавать без /
    public static void executeBungeeCmd(final Player p, final String command) { //без /
        SpigotChanellMsg.sendMessage(p, Operation.EXECUTE_BUNGEE_CMD, p.getName(), command);
    }

    /**
     * 
     * @param op
     * @return 
     * выдаст таймштамп, до которого нужно хранить данные игрока с учётом групп
     */
    public static int getStorageLimit(final Oplayer op) {
        return Perm.getStorageLimit(op);
    }
    /**
     * 
     * @param op
     * @param perm
     * @return 
     * выдаст лимит для данного пермишена с учётом групп
     */
    public static int getLimit(final Oplayer op, final String perm) {
        return Perm.getLimit(op, perm);
    }

    @Deprecated // устаревшее, просто пишем обшим образом, типо "при открытии инвентаря" вместо "когда ты открыла инвентарь"
    public static boolean isFemale(final String name) {
      return PM.exist(name) && PM.getOplayer(name).gender==PM.Gender.FEMALE;
    }

}
