package ru.komiss77.modules.player;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.*;
import ru.komiss77.Timer;
import ru.komiss77.builder.SetupMode;
import ru.komiss77.enums.*;
import ru.komiss77.listener.ChatLst;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.entities.PvPManager;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.items.SpecialItem;
import ru.komiss77.modules.player.PM.Gender;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.player.profile.ProfileManager;
import ru.komiss77.modules.quests.Quest;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.objects.CaseInsensitiveSet;
import ru.komiss77.objects.DelayBossBar;
import ru.komiss77.objects.Group;
import ru.komiss77.scoreboard.CustomScore;
import ru.komiss77.utils.*;
import ru.komiss77.version.CustomTag;
import ru.komiss77.version.Nms;


public class Oplayer {

    public static final int ACTION_BAR_INTERVAL = 3;
    public final String nik;
    public final UUID id;
    public boolean eng; //true - english; false - russian
    public int karmaCalc, reputationCalc; //просчитывается в
    private final int loginTime = Timer.secTime();
    private int daylyLoginTime = loginTime;   //время входа для дневной статы, сброс в полночь
    public int onlineSecond; //счётчик секунд после входа
    public int tick; //каждые 20 тиков будет вызов secondTick из таймера
    public final boolean isGuest;
    public boolean isStaff; //флаг модератора
    public final Map<String, String> mysqlData = new HashMap<>();
    public int mysqRecordId = Integer.MIN_VALUE;
    protected final EnumMap<Data, String> dataString = new EnumMap<>(Data.class); //локальные снимки,сохранять не надо. сохраняются в банжи
    protected final EnumMap<Data, Integer> dataInt = new EnumMap<>(Data.class);  //локальные снимки,сохранять не надо. сохраняются в банжи
    protected final EnumMap<Stat, Integer> stat = new EnumMap<>(Stat.class);  //локальные снимки,сохранятьне надо. сохраняются в банжи
    protected final EnumMap<Stat, Integer> dailyStat = new EnumMap<>(Stat.class);  //локальные снимки,сохранять не надо. сохраняются в банжи
    public final Map<Integer, Boolean> missionIds = new HashMap<>(); //true-обрабатывается (блокировать операции)
    public boolean hasFakeBlock;
    public final HashMap<Long, BlockData> fakeBlock = new HashMap<>();
    public final CustomScore score;
    public final CustomTag tag;

    //подгружается с локальной ЮД
    public final CaseInsensitiveMap<String> homes = new CaseInsensitiveMap<>();
    public final CaseInsensitiveMap<String> world_positions = new CaseInsensitiveMap<>(); //сохранять или нет решает GAME.storeWorldPosition()
    public final CaseInsensitiveMap<Integer> kits_use_timestamp = new CaseInsensitiveMap<>();
    public final CaseInsensitiveMap<Integer> limits = new CaseInsensitiveMap<>();

    //вычисляется из данных прокси
//    public CaseInsensitiveSet groups = new CaseInsensitiveSet();
    public CaseInsensitiveMap<Group> groupMap = new CaseInsensitiveMap<>();
    public CaseInsensitiveSet user_perms = new CaseInsensitiveSet();
    public final EnumMap<CheatType, Integer> cheats = new EnumMap<>(CheatType.class); //локальные снимки,сохранятьне надо. сохраняются в банжи

    //Друзья
    public final CaseInsensitiveSet friends = new CaseInsensitiveSet(); //друг,сервер
    public final CaseInsensitiveSet friendInvite = new CaseInsensitiveSet(); //предложения дружить, до выхода
    public String party_leader = ""; //ник лидера команды
    public CaseInsensitiveMap<String> party_members = new CaseInsensitiveMap<>(); //ник, сервер всех членов команды
    public final CaseInsensitiveSet partyInvite = new CaseInsensitiveSet(); //предложения в комманду, до выхода
    protected final CaseInsensitiveSet blackList = new CaseInsensitiveSet(); //хранится на банжи до перезахода, синхронизируется
    public final Map<Quest, IProgress> quests = new HashMap<>();

    public PermissionAttachment permissionAttachmen = null;
    public ProfileManager menu;
    private boolean hideScore = false; //для лобби-чтобы не конфликтовал показ онлайна и кастомные значения

//    public Location last_death; //p.getLastDeathLocation()

    public String chat_group = "----";
    private String tabPreffix = "§7", beforeName = ChatLst.NIK_COLOR, tabSuffix = "";
    private String tagPreffix = "", tagSuffix = "";

    public int pvp_time, no_damage;//, bplace, bbreak, mobkill, monsterkill, pkill, dead;
    public boolean allow_fly, firstJoin, resourcepack_locked = true, pvp_allow = true;
    @Deprecated
    public boolean mysqlError;
    public LocalDB.Error dbError;
    //служебные
    public int lookSum = 0, afkLeft = Integer.MAX_VALUE;
    public SetupMode setup; //для билдеров
    public BukkitTask displayCube; //показ границы выделения
    public Location spyOrigin;
    public GameMode spyOldGm;
    public Gender gender = Gender.NEUTRAL;
    public String lastCommand; //последняя команда введёная билдером
    public UUID tpRequestFrom; //от кого пришел запрос на ТП

    //Боссбар, титры, экшэнбар с задержками
    public final List<String> delayActionbars = new ArrayList<>();
    public final BossBar bossbar = BossBar.bossBar(Component.text(""), 0, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_12, Collections.emptySet());
    public int barTime, barMaxTime, nextTitle, nextAb;
    public boolean timeBar;
    public float progress;
    public final List<DelayBossBar> delayBossBars = new ArrayList<>();
    public final List<Title> delayTitles = new ArrayList<>();
    public WeakReference<Entity> minecart;
    public WeakReference<Entity> boat; //для лимитера

    public Oplayer(final HumanEntity p) {
        nik = p.getName();
        id = p.getUniqueId();
        menu = new ProfileManager(this);
        firstJoin = (isGuest = nik.startsWith("guest_"));
        score = new CustomScore((Player) p);
        tag = new CustomTag(p);
        tag.visible(true);
        tag.seeThru(true);
        tag(tagPreffix, tagSuffix);
        beforeName(ChatLst.NIK_COLOR, (Player) p);
        Nms.addPlayerPacketSpy((Player) p, Oplayer.this);
    }


    public void secondTick() {
        final Player p = getPlayer();
        if (p == null) {
            Ostrov.log_warn("Oplayer " + nik + " secondTick : Player==null!");
            return;
        }

        if (Cfg.afk) {
            final Location loc = p.getEyeLocation();
            int look = ((int) loc.getYaw() << 1) + (int) loc.getPitch();
            if (look != lookSum) { //двигался
                lookSum = look;
                if (Cfg.afk_sec - afkLeft > 60) { //были титры афк
                    ScreenUtil.sendTitleDirect(p, "",
                        "<olive>-=: AFK :=-", 0, 1, 30);
                }
                afkLeft = Cfg.afk_sec;
            } else if (afkLeft-- == 0) { //неподвижен
                p.clearTitle();
                if (ApiOstrov.isLocalBuilder(p)) {
                    p.sendMessage(Ostrov.PREFIX + "<dark_gray>Билдер - сброс цикла АФК"); //типа отладка
                    afkLeft = Cfg.afk_sec;
                } else {
                    p.sendMessage(Ostrov.PREFIX + "Ты уже АФК более " + (Cfg.afk_sec / 60) + " мин!");
                    ApiOstrov.sendToServer(p, "lobby0", "");
                    return; //secondTick дальше не выполнять
                }
            } else {
                if (Cfg.afk_sec - afkLeft > 60) { //неподвижен больше минуты
                    ScreenUtil.sendTitleDirect(p, "",
                        "<beige>-=: AFK :=-", 0, 40, 20);
                    if (afkLeft < 15) ScreenUtil.sendActionBarDirect(p,
                        "<gold>Через <yellow>" + afkLeft + " сек. <gold>переход в лобби!");
                }
            }
        }

        if (pvp_time > 0) {
            pvp_time--;
            if (pvp_time == 0) {
                pvp_time = 1;
                PvPManager.pvpEndFor(this, p);    //не переставлять!!
            }
        }

        if (nextAb > 0) {
            nextAb--;
            if (nextAb == 0) {
                if (!delayActionbars.isEmpty()) {
                    nextAb = ACTION_BAR_INTERVAL;
                    final String ab = delayActionbars.removeFirst();
                    p.sendActionBar(TCUtil.form(ab));
                }
            }
        }

        if (nextTitle > 0) {
            nextTitle--;
            if (nextTitle == 0) {
                if (!delayTitles.isEmpty()) {
                    final Title t = delayTitles.removeFirst();
                    p.showTitle(t);
                    final Title.Times tm = t.times();
                    nextTitle = tm == null ? 0 : tm.fadeIn().toSecondsPart() + tm.stay().toSecondsPart() + tm.fadeOut().toSecondsPart() + 1;
                }
            }
        }

        if (barTime > 0) {
            barTime--;
            if (barTime == 0) {
                if (!delayBossBars.isEmpty()) {
                    final DelayBossBar bb = delayBossBars.removeFirst();
                    bb.apply(this, p);
                } else {
                    p.hideBossBar(bossbar);
                }
            } else if (timeBar) {
                final float time = barMaxTime > 0 ? (float) barTime / (float) barMaxTime : 0f;
                bossbar.progress(time > 1f ? 1f : Math.max(time, 0f));
            }
        }

        if (dataString.isEmpty() && onlineSecond > 1) {
            if (onlineSecond < 15) {
                SpigotChanellMsg.sendMessage(p, Operation.RESEND_RAW_DATA, nik);
                ScreenUtil.sendActionBarDirect(p, "§5Ожидание данных с Остров БД..");
                return;
            } else if (onlineSecond == 15) {
                p.sendMessage("§cДанные с прокси не получены, попробуйте перезайти!");
            }
        }

        menu.tick(p); //обновление lore в меню ProfileManager

        if (!hideScore && Cfg.ostrovStatScore && onlineSecond % 10 == 0) {
            showOstrovBoard();
        }

        if (Cfg.tablist_header_footer) {
            ScreenUtil.sendTabList(p, (eng ? "§7Server: §5" : "§7Сервер: §5") + GM.GAME.displayName + "§7 §6" + TimeUtil.getCurrentHourMin(), (eng ? "§fMain menu - §a/menu" : "  §fГлавное меню - §a/menu"));
        }
//Ostrov.log_warn("op tick WANT_ARENA_JOIN="+dataString.get(Data.WANT_ARENA_JOIN));
        if (onlineSecond == 4) {
            setData(Data.WANT_ARENA_JOIN, "");
        }

        onlineSecond++;
    }

    public int getOnlineSec() {
        return onlineSecond;//Timer.secTime()-loginTime;
    }

    public Set<String> getPartyMembers() {
        return party_members.keySet();
    }

    public CaseInsensitiveMap<String> getPartyData() {
        return party_members;
    }

    public boolean isPartyLeader() {
        return !party_members.isEmpty() && party_leader.equals(nik);
    }


    public void beforeName(@Nullable final String beforeName, final Player p) { //назвал так, поточто пвп режим, например, ставит "§c⚔ §4"
        this.beforeName = beforeName == null || beforeName.isBlank() ? ChatLst.NIK_COLOR : beforeName;
        updTabListName(p);
        tag(tagPreffix, tagSuffix);
    }

    public void tabPrefix(@Nullable final String tab_prefix, final Player p) {
        this.tabPreffix = tab_prefix == null ? "" : tab_prefix;
        updTabListName(p);
    }

    public void tabSuffix(@Nullable final String tab_suffix, final Player p) {
        this.tabSuffix = tab_suffix == null ? "" : tab_suffix;
        updTabListName(p);
    }

    public void color(@Nullable final NamedTextColor color) {
        score.color(color == null ? NamedTextColor.WHITE : color);
        for (final World w : Bukkit.getWorlds()) score.send(w);
    }

    public void updTabListName(final Player p) {
        if (Cfg.tablist_name) {
            final String displayName = isGuest ? "§8(Гость) " + beforeName + getDataString(Data.FAMILY) : beforeName + nik;
            p.playerListName(TCUtil.form(tabPreffix + displayName + "<reset>" + tabSuffix));
        }
    }

    //показать/скрыть ник этого оплеера от других
    public void tag(@Nullable final String tagPrefix, @Nullable final String tagSuffix) {
        if (tagPrefix != null) this.tagPreffix = tagPrefix; //чтобы можно было поменять что-то одно, не трогая другое
        if (tagSuffix != null) this.tagSuffix = tagSuffix;
        final String displayName = isGuest ? "§8(Гость) " + beforeName + getDataString(Data.FAMILY) : beforeName + nik;
        tag.content(this.tagPreffix + displayName + this.tagSuffix);
    }

    @Deprecated
    public void tag(final boolean visible) {
        tag.visible(visible);
    }

    @Deprecated
    public void tagThru(final boolean see) {
        tag.seeThru(see);
    }

    @Deprecated
    public void setTagVis(final Predicate<Player> canSee) {
        tag.canSee(canSee);
    }

    @Deprecated
    public boolean isTagVisTo(final Player pl) {
        return tag.canSee(pl);
    }

    public void onPVPEnter(final Player p, final int time,
                           final boolean blockFly, final boolean giveTag) {
        final int pt = pvp_time;
        pvp_time = time;
        if (pt > 0) return;
        ScreenUtil.sendActionBarDirect(p, eng ? "§cBattle mode " + time + " sec." : "§cРежим боя " + time + " сек.");
        if (blockFly) {
            p.setAllowFlight(false);
            p.setFlying(false);
            if (p.isGliding()) {
                p.setGliding(false);
                ScreenUtil.sendActionBarDirect(p, Lang.t(p, "§cТебе прострелили крыло :("));
            }
        }
        if (giveTag) beforeName("§4⚔ ", p);
    }

    public void onPVPEnd(final Player p,
                         final boolean blockFly, final boolean giveTag) {
        final int pt = pvp_time;
        pvp_time = 0;
        if (pt < 1) return;
        ScreenUtil.sendActionBarDirect(p, Lang.t(p, "§aТы больше не в бою!"));
        if (blockFly) {
            p.setAllowFlight(
                switch (p.getGameMode()) {
                    case CREATIVE, SPECTATOR -> true;
                    default -> allow_fly;
                });
        }
        if (giveTag) beforeName(null, p);
    }

    /**
     * просто ставим нужные данные из енум DATA, оно само дальше разберётся.
     *
     * @param data некие данные игрока
     * @return строковое значение для строковых данных
     */
    @Deprecated
    public String getDataString(final Data data) {
        return globalStr(data);
    }

    public String globalStr(final Data data) {
        return dataString.getOrDefault(data, data.def_value);
    }

    @Deprecated
    //непонятно что за DataInt?? LocalData?? Proxy Data?? просто ставим нужные данные из енум DATA, оно само дальше разберётся.
    public int getDataInt(final Data data) {
        return globalInt(data);
    }

    public int globalInt(final Data data) {
        return dataInt.getOrDefault(data, NumUtil.intOf(data.def_value, 0));
    }

    @Deprecated
    //непонятно что за Data?? LocalData?? Proxy Data?? просто ставим нужные данные из енум DATA, оно само дальше разберётся.
    public boolean setData(final Data data, final int value) {  //отправляем на банжи, и обнов.локально
        return globalInt(data, value);
    }

    public boolean globalInt(final Data data, final int value) {  //отправляем на банжи, и обнов.локально
        int old_val = dataInt.getOrDefault(data, 0);
        if (old_val == value) {
            return true;
        }
        if (SpigotChanellMsg.sendMessage(getPlayer(), Operation.SET_BUNGEE_DATA, nik, data.tag, value, "", "")) {
            dataInt.put(data, value);
            if (data == Data.RIL) {
                if (old_val > value) { //use
                    Ostrov.history(HistoryType.MONEY_REAL_USE, this, "setData RIL old=" + old_val + " new=" + value);
                } else { //add
                    Ostrov.history(HistoryType.MONEY_REAL_ADD, this, "setData RIL old=" + old_val + " new=" + value);
                }

            } else if (data == Data.LANG) {
                eng = value == 1;
            }
            return true;
        } else {
            getPlayer().sendMessage(Ostrov.PREFIX + "§cОшибка синхронизации данных! data=" + data.toString() + " value=" + value);
            Ostrov.log_err("§cОшибка синхронизации данных! data=" + data.toString() + " value=" + value);
            return false;
        }
    }

    @Deprecated //непонятно что за Data?? LocalData?? Proxy Data??
    public boolean setData(final Data data, final String value) {  //отправляем на банжи, и обнов.локально
        return globalStr(data, value);
    }

    public boolean globalStr(final Data data, final String value) {  //отправляем на банжи, и обнов.локально
        if (SpigotChanellMsg.sendMessage(getPlayer(), Operation.SET_BUNGEE_DATA, nik, data.tag, 0, value, "")) {
            dataString.put(data, value);
            return true;
        } else {
            getPlayer().sendMessage(Ostrov.PREFIX + "§cОшибка синхронизации данных! data=" + data.toString() + " value=" + value);
            Ostrov.log_err("§cОшибка синхронизации данных! data=" + data.toString() + " value=" + value);
            return false;
        }
    }

    public EnumMap<Stat, Integer> getStatMap() {
        return stat;
    }

    public int getStat(final Stat st) {
        int record = stat.getOrDefault(st, 0);
        return switch (st) {
            case PLAY_TIME -> record + (Timer.secTime() - loginTime);
            case REPUTATION -> reputationCalc;
            case KARMA -> karmaCalc;
            default -> record;
        };
    }

    public int getDailyStat(final Stat st) {
        final int record = dailyStat.getOrDefault(st, 0);
        return switch (st) {
            case PLAY_TIME -> record + (Timer.secTime() - daylyLoginTime);
            default -> record;
        };
    }

    public void addStat(final Stat st, final int value) {
        if (isGuest) return;
        switch (st) {
            case PLAY_TIME, REPUTATION, KARMA -> {
                return;
            }
            case EXP -> {
                addExp(getPlayer(), value);
                return;
            }
            default -> {
            }
        }
        stat.put(st, getStat(st) + value);
        dailyStat.put(st, getDailyStat(st) + value);
        SpigotChanellMsg.sendMessage(getPlayer(), Operation.ADD_BUNGEE_STAT, nik, st.tag, value, "", "");
        MissionManager.onStatAdd(this, st, value);
        //ApiOstrov.sendMessage(getPlayer(), Action.SET_DATA_TO_BUNGEE, st.tag+E_Stat.diff, getDaylyStat(st), "", ""); //надо отдельно, или вычислять старое значение ?
    }

    public void addExp(final Player p, int value) { //отдельным методом, т.к. надо ставить отдельно уровень и дневное накопление
        if (isGuest) return;
        if (value <= 0) return;
        int curr_level = getStat(Stat.LEVEL);
        int lvlAdd = 0; //расчёт, сколько добавится уровня
        int xpCache = value + getStat(Stat.EXP);
        if (xpCache >= curr_level * 25) { //опыта достаточно для след.уровня
            for (; xpCache >= (curr_level + lvlAdd) * 25; lvlAdd++) { //делаем грубый расчёт добавленных уровней
                xpCache -= (curr_level + lvlAdd) * 25; //сначала убавить опыт со старым lvlAdd!
            }
        }

        if (lvlAdd > 0) { //уровень добавляется
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
            addStat(Stat.LEVEL, lvlAdd); //добавляем уровень
            stat.put(Stat.EXP, xpCache); //запомнить оставшийся опыт
            SpigotChanellMsg.sendMessage(p, Operation.SET_BUNGEE_DATA, nik, Stat.EXP.tag, xpCache, "", ""); //обновить на банжи
            dailyStat.put(Stat.EXP, getDailyStat(Stat.EXP) + value); //увеличение дневного счётчика опыта
            SpigotChanellMsg.sendMessage(p, Operation.SET_BUNGEE_DATA, nik, Stat.EXP.tag + Stat.diff, getDailyStat(Stat.EXP), "", "");
            ScreenUtil.sendTitle(p, "§7.", Ostrov.PREFIX + (eng ? "New level : §b" : "Новый уровень : §b") + getStat(Stat.LEVEL), 20, 60, 40);
            ScreenUtil.sendBossbar(p, Ostrov.PREFIX + (eng ? "New level : §b" : "Новый уровень : §b") + getStat(Stat.LEVEL), 8, Color.GREEN, Overlay.NOTCHED_20);
        } else { //уровень не меняется - просто добавляем опыт
            //addStat(Stat.EXP, value); addStat нельзя - деадлок!
            stat.put(Stat.EXP, xpCache);
            dailyStat.put(Stat.EXP, getDailyStat(Stat.EXP) + value);
            SpigotChanellMsg.sendMessage(getPlayer(), Operation.ADD_BUNGEE_STAT, nik, Stat.EXP.tag, value, "", ""); //на банжике уровень не пересчитываем!
            if (value > 10)
                ScreenUtil.sendActionBar(getPlayer(), (curr_level * 25 - getStat(Stat.EXP) + 1) + (eng ? "§7 experience to next level" : "§7 опыта до следующего уровня")); //+1 - фикс, или писало 0 до след уровня
        }
    }

    public void resetDaylyStat() {
        dailyStat.clear();
        daylyLoginTime = Timer.secTime();
    }

    public boolean hasFlag(final StatFlag flag) {
        return StatFlag.hasFlag(getStat(Stat.FLAGS), flag);
    }

    public boolean hasDaylyFlag(final StatFlag flag) {
        return StatFlag.hasFlag(getDailyStat(Stat.FLAGS), flag);
    }

    //сетит флаг локально и в банжи, ТОЛЬКО В ГЛОБАЛЬНОЙ СТАТЕ! dayly не меняет!
    public void setFlag(final StatFlag flag, final boolean state) {
        int value = getStat(Stat.FLAGS);
        value = state ? (value | (1 << flag.tag)) : value & ~(1 << flag.tag);
        stat.put(Stat.FLAGS, value);
        SpigotChanellMsg.sendMessage(getPlayer(), Operation.SET_BUNGEE_DATA, nik, Stat.FLAGS.tag, value, "", "");
    }

    //сетит флаг локально и в банжи, ТОЛЬКО В dayly СТАТЕ! стату не меняет!
    public void setDaylyFlag(final StatFlag flag, final boolean state) {
        int value = getDailyStat(Stat.FLAGS);
        value = state ? (value | (1 << flag.tag)) : value & ~(1 << flag.tag);
        dailyStat.put(Stat.FLAGS, value);
        SpigotChanellMsg.sendMessage(getPlayer(), Operation.SET_BUNGEE_DATA, nik, Stat.FLAGS.tag + Stat.diff, value, "", "");
    }


    public boolean hasSettings(final Settings settings) {
        return Settings.hasSettings(getDataInt(Data.SETTINGS), settings);
    }

    public void setSettings(final Settings settings, final boolean state) {
        int value = getDataInt(Data.SETTINGS);
        value = state ? (value | (1 << settings.tag)) : value & ~(1 << settings.tag);
        dataInt.put(Data.SETTINGS, value);
        SpigotChanellMsg.sendMessage(getPlayer(), Operation.SET_BUNGEE_DATA, nik, Data.SETTINGS.tag, value, "", "");
    }

    @Deprecated //непонятно что за Data?? LocalData?? Proxy Data??
    public String getTextData(String key) {
        return globalData(key);
    }

    public String globalData(String key) {
        final String dataRaw = getDataString(Data.TEXTDATA);
        if (dataRaw.isEmpty()) return "";
        key = key + "。";
        for (final String s : dataRaw.split("︙")) { //∬ не использовать! конфликт с банжиданными!
            if (s.startsWith(key)) {
                return s.replaceFirst(key, "");
            }
        }
        return "";
    }

    @Deprecated //непонятно что за Data?? LocalData?? Proxy Data??
    public void setTextData(final String key, final String value) {
        globalData(key, value);
    }

    public void globalData(final String key, final String value) {
        if (key == null) return;
        final String dataRaw = getDataString(Data.TEXTDATA);
        final HashMap<String, String> data = new HashMap<>();
        if (!dataRaw.isEmpty()) {
            int splitterIndex;
            for (final String s : dataRaw.split("︙")) { //∬ не использовать! конфликт с банжиданными!
                splitterIndex = s.indexOf("。");
                if (splitterIndex > 0) {
                    data.put(s.substring(0, splitterIndex), s.substring(splitterIndex + 1));
                }
            }
        }
        if (value == null) data.remove(key);
        else data.put(key, value);
        StringBuilder build = new StringBuilder();
        for (String k : data.keySet()) {
            build.append("︙").append(k).append("。").append(data.get(k));
        }
        setData(Data.TEXTDATA, build.toString().replaceFirst("︙", ""));
    }


    protected void showOstrovBoard() {
        //if (!Config.ostrovStatScore || hideScore) return;
        if (eng) {
            score.getSideBar().setTitle("§7Total online: §f§l" + GM.bungee_online);//"§a-----------------"
            score.getSideBar().update(8, "§a--------------");
            score.getSideBar().update(7, "Level §b" + getStat(Stat.LEVEL));
            score.getSideBar().update(6, "Exp §5" + getStat(Stat.EXP));
            score.getSideBar().update(5, "Reputation " + getReputationDisplay());
            score.getSideBar().update(4, "Karma " + getKarmaDisplay());
            score.getSideBar().update(3, "Loni §6" + getDataInt(Data.LONI));
            score.getSideBar().update(2, "Ril §e" + getDataInt(Data.RIL));
            score.getSideBar().update(1, "§a--------------");
        } else {
            score.getSideBar().setTitle("§7Общий онлайн: §f§l" + GM.bungee_online);//"§a-----------------"
            score.getSideBar().update(8, "§a--------------");
            score.getSideBar().update(7, "Уровень §b" + getStat(Stat.LEVEL));
            score.getSideBar().update(6, "Опыт §5" + getStat(Stat.EXP));
            score.getSideBar().update(5, "Репутация " + getReputationDisplay());
            score.getSideBar().update(4, "Карма " + getKarmaDisplay());
            score.getSideBar().update(3, "Лони §6" + getDataInt(Data.LONI));
            score.getSideBar().update(2, "Рил §e" + getDataInt(Data.RIL));
            score.getSideBar().update(1, "§a--------------");
        }
    }


    public void setNoDamage(final int seconds, final boolean actionBar) {
        if (seconds <= no_damage) return;
        no_damage = seconds;
        if (!actionBar) return;
        ScreenUtil.sendActionBar(getPlayer(), eng
            ? "§aYou have invulnerability for " + no_damage + " sec!"
            : "§aУ тебя неуязвимость на " + no_damage + " сек!");
    }

    @Deprecated
    public boolean Pvp_is_allow() {
        return pvp_allow;
    }


    // ---------------------- Наборы ---------------------------------------------
    public boolean hasKitAcces(final String kitName) {
        return kits_use_timestamp.containsKey(kitName);
    }

    public void addKitAcces(final String kitName) {
        kits_use_timestamp.put(kitName, 0);
        mysqlData.put("kitsUseData", null); //пометить на сохранение
    }

    public void revokeKitAcces(final String kitName) {
        kits_use_timestamp.remove(kitName);
        mysqlData.put("kitsUseData", null); //пометить на сохранение
    }

    public int getKitUseStamp(final String kitName) {
//System.out.println("+++++++kit "+kits+"   contains "+kit+"?"+this.kits.containsKey(kit)+" value:"+((this.kits.containsKey(kit))?this.kits.get(kit):"0"));
        return kits_use_timestamp.getOrDefault(kitName, 0);
    }

    public void setKitUseTimestamp(final String kitName) {
        kits_use_timestamp.put(kitName, Timer.secTime());
        mysqlData.put("kitsUseData", null); //пометить на сохранение
    }
    // public Map <String, Long> GetKitsData() {
    //     return kits_use_timestamp;
    // }
//------------------------------------------------------------------------------


    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(id);
    }

    public boolean bungeeDataRecieved() {
        return !dataString.isEmpty();
    }

    public boolean hasGroup(final Group grp) {
        if (groupMap.containsKey(grp.name)) return true;
        return groupMap.values().stream().anyMatch(gr -> gr.inheritance.contains(grp.name));
    }

    @Deprecated
    public boolean hasGroup(final String group) {
        if (groupMap.containsKey(group)) return true;
        return groupMap.values().stream().anyMatch(gr -> gr.inheritance.contains(group));
    }

    @Deprecated
    public Collection<String> getGroups() {
        return groupMap.keySet();
    }

    public Collection<String> getUserPerms() {
        return user_perms;
    }

    public int getKarmaModifier(final Stat.KarmaChange kc) {
        int result = 0;
        for (Stat st : stat.keySet()) {
            if (st.karmaChange == kc) {
                result += stat.get(st);
            }
        }
        return result;
    }

    public String getKarmaDisplay() {
        return karmaCalc == 0 ? "§7равновесие" : (karmaCalc > 0 ? "§a+" + karmaCalc : "§c" + karmaCalc);
    }

    public String getReputationDisplay() {
        return reputationCalc == 0 ? "§8---" : (reputationCalc > 0 ? "§a+" + reputationCalc : "§c" + reputationCalc);
    }

    public int getStatFill() {
        return stat.size() <= 6 ? 0 : stat.size() - 6; //не учитываем PLAY_TIME EXP LEVEL FLAGS REPUTATION KARMA
    }

    public boolean hasData(final Data d) {
        return dataString.containsKey(d) || dataInt.containsKey(d);
    }

    public boolean addBlackList(final String name) { //только локально!! на банжи не изменится!!
        return blackList.add(name);
    }

    public boolean removeBlackList(final String name) { //только локально!! на банжи не изменится!!
        return blackList.remove(name);
    }

    public boolean isBlackListed(final String name) {
        return blackList.contains(name);
    }

    public Set<String> getBlackListed() {
        return blackList;
    }

    public int getOnlineTime() {
        return Timer.secTime() - loginTime;
    }

    public int loni() {
        return getDataInt(Data.LONI);
    }

    public void hideScore() {
        hideScore = true;
        score.getSideBar().reset();
    }

    public void showScore() {
        hideScore = false;
    }


    public void updateGender() {
        switch (TCUtil.strip(getDataString(Data.GENDER)).toLowerCase()) {
            case "девочка" -> gender = Gender.FEMALE;
            case "мальчик" -> gender = Gender.MALE;
            default -> gender = Gender.NEUTRAL;
        }
    }

    public String getTopPerm() {
        if (Perm.isStaff(this, 1))
            return "Создатель";
        if (Perm.isStaff(this, 2))
            return "Сис-Админ";
        if (Perm.isStaff(this, 5))
            return "Персонал";
        if (Perm.isRank(this, 1))
            return "Легенда";
        if (Perm.isRank(this, 2))
            return "Герой";
        if (Perm.isRank(this, 3))
            return "Воин";
        return "";
    }

    public boolean isLocalChat() {
        return hasFlag(StatFlag.LocalChat);
    }

    public boolean setLocalChat(boolean local) {
        boolean currentLocal = hasFlag(StatFlag.LocalChat);
        if ((currentLocal && local) || (!currentLocal && !local))
            return false; //итак локальный и ставим локальный, или итак глобальный и ставим глобальный
        setFlag(StatFlag.LocalChat, local);
        return true;
    }

    /**
     * Выполняется перед сохранением данных
     */
    public void preDataSave(final Player p, final boolean async) {
        final List<SpecialItem> sis = SpecialItem.getAll(p);
        if (sis.isEmpty()) return;
        for (final ItemStack it : p.getInventory()) {
            if (ItemUtil.isBlank(it, false)) continue;
            final SpecialItem si = SpecialItem.get(it);
            if (si == null) continue;
            if (!sis.remove(si)) {
                it.setAmount(0);
                si.info("Duplicate item removed!");
                return;
            }
            if (si.dropped()) {
                if (!(si.own() instanceof final Item ii)) {
                    it.setAmount(0);
                    si.info("Dropped item removed!");
                    return;
                }
                ii.remove();
                si.info("Duplicate item removed!");
            }
            si.apply(p.getWorld().dropItem(p.getLocation(), it));
            it.setAmount(0);
            si.info("Dropped item on logout!");
        }
    }

    /**
     * Выполняется после сохранения данных
     */
    public void postDataSave(final Player p, final boolean async) {

    }
}