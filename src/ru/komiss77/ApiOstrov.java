package ru.komiss77;

import java.util.*;
import com.destroystokyo.paper.ClientOption;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import ru.komiss77.enums.*;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.listener.ResourcePacksLst;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.player.profile.StatManager;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.utils.FastMath;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.MoveUtil;


public class ApiOstrov {


    public static void executeBungeeCmd(final Player p, final String command) { //команды на банжик передавать без /
        SpigotChanellMsg.sendMessage(p, Operation.EXECUTE_BUNGEE_CMD, p.getName(), command);
    }

    /**
     * @param target игрок
     * @param server название сервера, как в настройках bungeecord
     * @param arena  название арены на сервере для вызова ArenaJoinEvent в плагине bsign
     */
    public static void sendToServer(final Player target, final String server, String arena) {
//Ostrov.log("sendToServer server="+server+" arena="+arena);
        if (server.equalsIgnoreCase(Ostrov.MOT_D)) {
            Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick(target, arena));
        } else {
            ResourcePacksLst.preDisconnect(target);
            SpigotChanellMsg.sendMessage(target, Operation.SEND_TO_ARENA, target.getName(), 0, 0, server, arena);
        }
    }

    //вроде часто нужно, пусть тут будет ссылочка
    public static boolean teleportSave(final Player p, Location feetLoc, final boolean buildSafePlace) {
        return MoveUtil.teleportSave(p, feetLoc, buildSafePlace);
    }

    /**
     * @param name ник. Возвращает true если у игрока активен режим боя. Так же, можно использовать BattleModeEvent и BattleModeEndEvent
     * @return
     */
    public static boolean inBattle(String name) {
        return PM.inBattle(name);
    }

    public static void giveMenuItem(final Player p) {
        MenuItemsManager.giveItem(p, "pipboy");//ItemUtils.Add_to_inv(p, 8, ItemUtils.pipboy, true, false);
    }

    public static boolean hasResourcePack(final Player p) {
        if (ResourcePacksLst.use) {
            final Oplayer op = PM.getOplayer(p);
            return op == null || !op.resourcepack_locked;//ResourcePacks.Текстуры_утановлены(p);
        } else {
            return true;
        }
    }

    // выдаст таймштамп, до которого нужно хранить данные игрока в БД с учётом групп
    public static int getStorageLimit(final Oplayer op) {
        return Perm.getStorageLimit(op);
    }

    // выдаст лимит для данного пермишена с учётом групп
    public static int getLimit(final Oplayer op, final String perm) {
        return Perm.getLimit(op, perm);
    }

    //@Dep устаревшее, просто пишем обшим образом, типо "при открытии инвентаря" вместо "когда ты открыла инвентарь"
    //в играх очень часто нужно! самое недавнее что делал: в строителях "строил" или "строила" чем заменить одним словом?
    public static boolean isFemale(final String name) {
        final Oplayer op = PM.getOplayer(name);
        return op != null && op.gender == PM.Gender.FEMALE;
    }


    //*************** стата *********************
    public static int getStat(final Player p, final Stat e_stat) {
        return PM.exists(p.getUniqueId()) ? PM.getOplayer(p).getStat(e_stat) : 0;
    }

    //@Deprecated
    //public static int getDaylyStat(final Player p, final Stat e_stat) {return getDailyStat(p, e_stat);}
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
     * @param p
     * @param customStatName отправить добавление локальной статы (для выплат лони и missionsManager)
     */
    public static void addCustomStat(final Player p, final String customStatName) {
        addCustomStat(p, customStatName, 1);
    }

    /**
     * @param p
     * @param customStatName
     * @param ammount        отправить добавление локальной статы (для выплат лони и missionsManager)
     */
    public static void addCustomStat(final Player p, final String customStatName, final int ammount) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) {
            StatManager.onCustomStat(p, op, customStatName, ammount);
            MissionManager.onCustomStat(op, customStatName, ammount, false);
        }
    }

    /**
     * @param p
     * @param customStatName
     * @param value          отправить ДОСТИЖЕНИЕ локальной статы на банжи в missionsManager
     *                       достижение может убавляться от вызова к вызову,
     *                       но выполнением будет считаться значение ammount>=число в условии
     *                       переданное хоть один раз
     */
    public static void reachCustomStat(final Player p, final String customStatName, final int value) {
        if (StatManager.DEBUG)
            Ostrov.log("reachCustomStat " + (p == null ? "null" : p.getName()) + " stat=" + customStatName + " val=" + value);
        final Oplayer op = PM.getOplayer(p);
        if (op != null) MissionManager.onCustomStat(op, customStatName, value, true);
    }

    public static void addExp(final Player p, final int ammount) {
        final Oplayer op = PM.getOplayer(p);//StatManager.addExp(PM.getOplayer(p), ammount);
        if (op != null) op.addExp(p, ammount);
    }
    //****************************************************


    //*************** друзья команды *********************
    public static boolean hasParty(final Player p) {
        final Oplayer op = PM.getOplayer(p.getUniqueId());
        return op != null && !op.getPartyMembers().isEmpty();//Ostrov.api_friends!=null && ApiFriends.hasParty(p);
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
        return isFriend(p1.getName(), p2.getName());
    }

    //@Dep друзья могуть быть на разных серверах, поэтому чекается именно по нику (это как раз к вопросу, зачем в РМ метод getOplayer(ник)
    public static boolean isFriend(final String p1, final String p2) {
        final Oplayer op1 = PM.getOplayer(p1);
        return op1 != null && op1.friends.contains(p2);
    }
    //****************************************************


    //*************** по билдеру *********************
    public static boolean isSpyMode(final Player p) {
        return PM.getOplayer(p).spyOrigin != null;//SpyCmd.isSpy(p.getName());
    }

    public static boolean canBeBuilder(final CommandSender cs) {
        if (cs == null) return false;
        if ((cs instanceof ConsoleCommandSender) || cs.isOp() || cs.hasPermission("builder")) return true;
        final Oplayer op = PM.getOplayer(cs.getName());
        return op != null && op.hasGroup("owner");
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
                    p.sendMessage(TCUtil.form(eng ? "§e*Click on this message - §aenable Builder mode" : "§e*Клик на это сообшение - §aвключить режим Строителя")
                            .hoverEvent(HoverEvent.showText(TCUtil.form(eng ? "§7Click - enable" : "§7Клик - включить")))
                            .clickEvent(ClickEvent.runCommand("/builder")));
                }
                return false;
            default:
                return false;
        }
    }
    //****************************************************


    //*************** деньги *********************
    /**
     * @param target только онлайн игроки!
     * @param value  изменение, если убавить, то с минусом
     * @param source источник
     */
    public static void moneyChange(final Player target, final int value, final String source) {
        final Oplayer targetOp = PM.getOplayer(target.getUniqueId());
        targetOp.setData(Data.LONI, targetOp.getDataInt(Data.LONI) + value);//moneySet(curr+value, send_update);
        if (value > 9 || value < -9) { //по копейкам не уведомляем
            target.sendMessage(TCUtil.form(Ostrov.PREFIX + "§7" + (value > 9 ? "Поступление" : "Расход") + " средств: " + source + " §7-> " + (value > 9 ? "§2" : "§4") + value + " " + Ostrov.L + " §7! §8<клик-баланс")
                    .hoverEvent(HoverEvent.showText(TCUtil.form("§5Клик - сколько стало?")))
                    .clickEvent(ClickEvent.runCommand("/money balance")));
        } else {
            //?? писать ли что-нибудь??
        }
    }

    /**
     * @param name  ник. (в разработке-Если оффлайн, добавится при входе)
     * @param value изменение, если убавить, то с минусом
     * @param who   кто изменяет
     */
    public static void moneyChange(final String name, final int value, final String who) {
        final Player p = Bukkit.getPlayerExact(name);
        if (p != null) {
            moneyChange(p, value, who);
        } else {//запомнить и дать при входе - оффлайн перевод
            LocalDB.moneyOffline(name, value, who);
        }
    }

    public static int moneyGetBalance(final String name) {
        final Oplayer op = PM.getOplayer(name); //и тут тоже удобнее работать по нику, чем сначала брать игрока и потом оплеера))))
        return op == null ? 0 : op.getDataInt(Data.LONI);
        //if (PM.exists(name)) return PM.getOplayer(name).getDataInt(Data.LONI);
        //else return 0;
    }
    //****************************************************

    //ScreenUtil
    //*************** всякие титры,бары *********************
    /*public static void sendTitle(final Player p, final String title, final String subtitle) {
        sendTitle(p, title, subtitle, 20, 40, 20);
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendTitle(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout) {
        final Times times = Title.Times.times(Duration.ofMillis(fadein * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeout * 50L));
        sendTitle(p, TCUtils.form(title), TCUtils.form(subtitle), times);
    }

    public static void sendTitle(final Player p, final Component title, final Component subtitle, final int fadein, final int stay, final int fadeout) {
        final Times times = Title.Times.times(Duration.ofMillis(fadein * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeout * 50L));
        sendTitle(p, title, subtitle, times);
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendTitle(final Player p, final Component title, final Component subtitle, final Times times) {
        final Oplayer op = PM.getOplayer(p);
        final Title t = Title.title(title, subtitle, times);
        if (op != null) { //на авторизации нет оплеера!
            if (op.nextTitle > 0) {
                op.delayTitles.add(t);
            } else {
                p.showTitle(t);
                op.nextTitle = times.fadeIn().toSecondsPart() + times.stay().toSecondsPart() + times.fadeOut().toSecondsPart() + 1;
            }
        } else {
            p.showTitle(t);
        }

    }

    public static void sendTitleDirect(final Player p, final String title, final String subtitle) {
        sendTitleDirect(p, title, subtitle, 20, 40, 20);
    }

    public static void sendTitleDirect(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout) {
        final Times times = Title.Times.times(Duration.ofMillis(fadein * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeout * 50L));
        p.showTitle(Title.title(TCUtils.form(title), TCUtils.form(subtitle), times));
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendActionBar(final Player p, final String text) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) { //на авторизации нет оплеера!
            if (op.nextAb > 0) {
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
        if (p != null) {
            p.sendActionBar(TCUtils.form(text));
        }
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbar(final Player p, final String text, final int seconds,
        final BossBar.Color color, final BossBar.Overlay style, final float progress) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) {
            if (op.barTime > 0) {
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
        if (op != null) DelayBossBar.apply(op, p, text, seconds, color, style, progress, false);
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbar(final Player p, final String text,
        final int seconds, final BossBar.Color color, final BossBar.Overlay style) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) {
            if (op.barTime > 0) {
                op.delayBossBars.add(new DelayBossBar(text, seconds, color, style, 1f, true));
            } else {
                DelayBossBar.apply(op, p, text, seconds, color, style, 1f, true);
            }
        }
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbarDirect(final Player p, final String text,
        final int seconds, final BossBar.Color color, final BossBar.Overlay style) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) DelayBossBar.apply(op, p, text, seconds, color, style, 1f, true);
    }

    public static void sendTabList(final Player p, final String header, final String footer) {
        p.sendPlayerListHeaderAndFooter(TCUtils.form(header), TCUtils.form(footer));
    }*/
    // *****************************************************************************


    //*************** числа *********************
    public static int randInt(final int num1, final int num2) {
        if (num1 == num2) return num1;
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

    //@Deprecated мне удобнее получать без лишних аргументов, где по дефолту MIN_VALUE
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

    public static int generateId() {
        final String createStamp = String.valueOf(System.currentTimeMillis());
        return Integer.parseInt(createStamp.substring(createStamp.length() - 8));  //15868 94042329
    }
    // *****************************************************************************


    public static int currentTimeSec() {
        return Timer.getTime();
    }

    public static void makeWorldEndToWipe(final int afterSecond) {
        WorldManager.makeWorldEndToWipe(afterSecond);
    }






/* StringUtil
    public static String getPercentBar(final int max, final int current, final boolean withPercent) {
        if (current < 0 || current > max) return "§8||||||||||||||||||||||||| ";
//System.out.println("max="+max+" curr="+current);
        final double percent = (double) current / max * 100;
        int p10 = (int) (percent * 10);
        final double percent1d = ((double) p10 / 10); //чтобы не показывало 100
        int pos = p10 / 40;
        //StringBuilder sb = new StringBuilder("§a||||||||||||||||||||||||| ");
        //return sb.insert(pos, "§8").append(percent1d).append("%").toString();
        if (pos < 2) pos = 2;
        else if (pos > 26) pos = 26;
        if (withPercent) {
            return new StringBuilder("§a||||||||||||||||||||||||| ").insert(pos, "§8").append("§f").append(percent1d).append("%").toString();
        } else {
            return new StringBuilder("§a||||||||||||||||||||||||| ").insert(pos, "§8").toString();
        }
    }*/



   /* ClassUtil @SuppressWarnings("unchecked")
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
            chs += ((chs - ni) >> 31) + 1;
        }
        return ar;
    }*/



    /* MoveUtil
    public static void moveDeny(final PlayerMoveEvent e) {//блокировка перемещения, для миниигр
        if (e.getTo().getY() < e.getFrom().getY()) {
            e.setTo(e.getFrom().add(0, 2, 0));
        } else {
            e.setTo(e.getFrom());
        }
    }*/




    /* StringUtil
    public static String toSigFigs(final double n, final byte sf) {
        final String nm = String.valueOf(n);
        return nm.indexOf('.') + sf + 1 < nm.length() ? nm.substring(0, nm.indexOf('.') + sf + 1) : nm;
    }

    @Deprecated
    public static String toSigFigs(final float n, final byte sf) {
        final String nm = String.valueOf(n);
        return nm.indexOf('.') + sf + 1 < nm.length() ? nm.substring(0, nm.indexOf('.') + sf + 1) : nm;
    }*/


    //TimeUtil
    //public static boolean isNewDay() { //после рестарта определить, настал ли новый день
    //    return Ostrov.newDay;
    //}


    //ентити
/* EntityUtil
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
    }*/


    //невостребовано вообще
    /*public static Connection getLocalConnection() {
        return LocalDB.getConnection();
    }
    public static Connection getOstrovConnection() {
        return OstrovDB.getConnection();
    }*/



/* StringUtil
    private static final String PATTERN_ENG = "[A-Za-z_]";
    private static final String PATTERN_ENG_NUM = "\\w"; //[A-Za-z0-9_]";
    private static final String PATTERN_ENG_RUS = "[A-Za-zА-Яа-я_]";
    private static final String PATTERN_ENG_NUM_RUS = "[A-Za-z0-9А-Яа-я_]";*/

    //невостребовано вообще
    // public static Initiable getModule(final Module module) {
    //return Ostrov.getModule(module);
    // }


    //public static boolean hasPermission(final String worldName, final String nik, String perm) {
    //     final Oplayer op = PM.getOplayer(nik);
    //     return op != null && Perm.hasPermissions(op, worldName, perm);
    //  }



    /* TimeUtil
    public static String secondToTime(int second) { //c днями и нед!
        if (second < 0) return "---";
        final int year = second / 30_758_400; //356*24*60*60
        second -= year * 30_758_400; //от секунд отнимаем годы
        final int month = second / 2_678_400; //31*24*60*60
        second -= month * 2_678_400; //от секунд отнимаем месяцы

        final int week = second / 604_800; //7*24*60*60
        if (year == 0)
            second -= week * 604_800; //от секунд отнимаем недели. недели не показываем и не отнимаем, если счёт на года

        final int day = second / 86_400; //24*60*60
        second -= day * 86_400; //от секунд отнимаем дни
        final int hour = second / 3600; //60*60
        second -= hour * 3600;  //от секунд отнимаем часы
        final int min = second / 60;
        second -= min * 60; //от секунд отнимаем минуты

        StringBuilder sb = new StringBuilder();
        if (year > 0) sb.append(year).append("г. ");
        if (month > 0) sb.append(month).append("мес. ");
        if (week > 0 && year == 0) sb.append(week).append("нед. ");
        if (day > 0) sb.append(day).append("д. ");
        if (year > 0) return sb.toString(); //счёт на года - достаточно до дней
        if (hour > 0) sb.append(hour).append("ч. ");
        if (month > 0 || week > 0) return sb.toString(); //счёт на месяца - достаточно до часов
        if (min > 0) sb.append(min).append("мин. ");
        if (second > 0) sb.append(second).append("сек. ");
        return sb.toString();
    }

    public static String dateFromStamp(final int stamp_in_second) {
        return Ostrov.dateFromStamp(stamp_in_second);
    }

    public static String getCurrentHourMin() {
        return Ostrov.getCurrentHourMin();
    }*/


    /* use StingUtil
    public static String listToString(final Iterable<?> array, final String splitter) {
        if (array == null) return "";
       /* StringBuilder sb=new StringBuilder();
        array.forEach( (s) -> {
            sb.append(s).append(splitter);
        });
        return sb.toString();
        return StreamSupport.stream(array.spliterator(), true)
                .map(Object::toString)
                .reduce((t, u) -> t + "," + u)
                .orElse("");
    }

    @Deprecated
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String toString(final Collection array, final boolean commaspace) {
        return toString(array, commaspace ? ", " : ",");
    }

    public static <E> String toString(final Collection<E> array, final String separator) {
        if (array == null || array.isEmpty()) return "";
        return array.stream()
                .map(E::toString)
                .reduce((t, u) -> t + separator + u)
                .orElse("");
    }

    public static String enumSetToString(final Set<?> enumSet) {
        StringBuilder sb = new StringBuilder();
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
    */

    /*use LocUtil
    public static String stringFromLoc(final Location loc) {
        return LocationUtil.toString(loc);
    }

    public static Location locFromString(final String loc_as_string) {
        return LocationUtil.stringToLoc(loc_as_string, false, true);
    }
*/

    /*BlockUtils
    public static Block getSignAttachedBlock(final Block b) {
        if (b.getState() instanceof final Sign sign
                && sign.getBlockData() instanceof final WallSign signData) {
            return b.getRelative(signData.getFacing().getOppositeFace());

        }
        return b.getRelative(BlockFace.DOWN);
    }*/

/* StringUtil
    public static String[] wrap(final String msg, final int length, final String newLine) {
        if (msg.length() < 2) return new String[]{msg};
        final char split = '\n';
        final String line = split + newLine;
        return WordUtils.wrap(msg, length, line, false).substring(1).split(line);
    }

    public static boolean checkString(String message, final boolean allowNumbers, final boolean allowRussian) {
        return checkString(message, false, allowNumbers, allowRussian);
    }

    public static boolean checkString(String message, final boolean allowSpace, final boolean allowNumbers, final boolean allowRussian) {
        if (allowNumbers && allowRussian) {
            message = message.replaceAll(PATTERN_ENG_NUM_RUS, "");
        } else if (allowNumbers) {
            message = message.replaceAll(PATTERN_ENG_NUM, "");
        } else if (allowRussian) {
            message = message.replaceAll(PATTERN_ENG_RUS, "");
        } else {
            message = message.replaceAll(PATTERN_ENG, "");
        }
        return allowSpace ? message.isBlank() : message.isEmpty();
    }*/


}




