package ru.komiss77;

import java.util.ArrayList;
import java.util.List;
import com.destroystokyo.paper.ClientOption;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.Stat;
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
import ru.komiss77.utils.MoveUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;


public class ApiOstrov {


    public static void executeBungeeCmd(final Player p, final String command) { //команды на банжик передавать без /
        SpigotChanellMsg.sendMessage(p, Operation.EXECUTE_BUNGEE_CMD, p.getName(), command);
    }

    /**
     * @param target игрок
     * @param server название сервера, как в настройках bungeecord
     * @param arena название арены на сервере для вызова ArenaJoinEvent в плагине bsign
     */
    public static void sendToServer(final Player target, final String server, final String arena) {
//Ostrov.log("sendToServer server="+server+" arena="+arena);
        if (server.equalsIgnoreCase(Ostrov.MOT_D)) {
            Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick(target, arena));
        } else {
            ResourcePacksLst.preDisconnect(target);
            SpigotChanellMsg.sendMessage(target, Operation.SEND_TO_ARENA, target.getName(), 0, 0, server, arena);
        }
    }

    @Deprecated // юзаем напрямую
    public static boolean teleportSave(final Player p, final Location feetLoc, final boolean buildSafePlace) {
        //return MoveUtil.safeTP(p, feetLoc, buildSafePlace);//тестить будем
        return MoveUtil.safeTP(p, feetLoc);
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
            return p.hasResourcePack();
//            final Oplayer op = PM.getOplayer(p);
//            return op == null || !op.resourcepack_locked;//ResourcePacks.Текстуры_утановлены(p);
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
    // ... если на Вы говоришь то "Вы построили", там легко. Но даже если нет, я на Седне сделал без пола, на Ты
    //вместо "Строила: Дев. Ник", "Постройка Ник"
    @Deprecated
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
        if (op1 != null) return op1.friends.contains(p2);
        final Oplayer op2 = PM.getOplayer(p2);
        return op2 != null && op2.friends.contains(p1);
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
        return op != null && Perm.isStaff(op, 2);
    }

    public static boolean isStaff(final CommandSender cs) {
        if (cs == null) return false;
        if ((cs instanceof ConsoleCommandSender) || cs.isOp()) return true;
        final Oplayer op = PM.getOplayer(cs.getName());
        return op != null && op.isStaff;
    }

    public static boolean isLocalBuilder(final CommandSender cs) {
        return isLocalBuilder(cs, false);
    }

    public static boolean isLocalBuilder(final CommandSender cs, final boolean message) {
        return switch (cs) {
            case ConsoleCommandSender ignored -> true;
            case Player p when canBeBuilder(p) ->
                switch (p.getGameMode()) {
                    case CREATIVE, SPECTATOR -> true;
                    default -> {
                        if (!message) yield false;
                        //!! фиксить права в CDM case "gm", или не даст перейти в гм1
                        final boolean eng = !p.getClientOption(ClientOption.LOCALE).equals("ru_ru");
                        p.sendMessage(TCUtil.form(eng ? "§e*Click on this message - §aenable Builder mode" : "§e*Клик на это сообшение - §aвключить режим Строителя")
                            .hoverEvent(HoverEvent.showText(TCUtil.form(eng ? "§7Click - enable" : "§7Клик - включить")))
                            .clickEvent(ClickEvent.runCommand("/builder")));
                        yield false;
                    }
                };
            case null, default -> false;
        };
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

    @Deprecated //ник не юзать
    public static int moneyGetBalance(final String name) {
        final Oplayer op = PM.getOplayer(name); //и тут тоже удобнее работать по нику, чем сначала брать игрока и потом оплеера))))
        return op == null ? 0 : op.loni();
        //if (PM.exists(name)) return PM.getOplayer(name).getDataInt(Data.LONI);
        //else return 0;
    }
    //****************************************************
    @Deprecated
    public static int randInt(final int num1, final int num2) {
        return NumUtil.randInt(num1, num2);
    }
    @Deprecated
    public static boolean randBoolean() {
        return NumUtil.rndBool();
    }
    @Deprecated
    public static int rndSignNum(int init, final int rnd) {
        return NumUtil.rndSignNum(init, rnd);
    }
    @Deprecated
    public static boolean isInteger(final String i) {
        return NumUtil.isInt(i);
    }
    @Deprecated
    public static int getInteger(final String num) {//удобнее получать без лишних аргументов, чтобы дважды не парсить - ", 0" момент
        return NumUtil.intOf(num, Integer.MIN_VALUE);
    }
    @Deprecated
    public static int getInteger(final String num, final int or) {
        return NumUtil.intOf(num, or);
    }

    @Deprecated //use Oplayer.userID
    public static int generateId() {
        final String createStamp = String.valueOf(System.currentTimeMillis());
        return Integer.parseInt(createStamp.substring(createStamp.length() - 8));  //15868 94042329
    }
    // *****************************************************************************


    @Deprecated
    public static int currentTimeSec() {
        return Timer.getTime();
    }

    public static void makeWorldEndToWipe(final int afterSecond) {
        WorldManager.makeWorldEndToWipe(afterSecond);
    }
}




