package ru.komiss77.modules.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.Perm;
import ru.komiss77.Timer;
import ru.komiss77.builder.SetupMode;
import ru.komiss77.commands.PvpCmd;
import ru.komiss77.enums.CheatType;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.GlobalLogType;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.Settings;
import ru.komiss77.enums.Stat;
import ru.komiss77.enums.StatFlag;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.PM.Gender;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.player.profile.E_Pass;
import ru.komiss77.modules.player.profile.ProfileManager;
import ru.komiss77.modules.quests.Quest;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.objects.CaseInsensitiveSet;
import ru.komiss77.objects.DelayBossBar;
import ru.komiss77.objects.InputData;
import ru.komiss77.scoreboard.CustomScore;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.VM;





public class Oplayer {

    public static final int ACTION_BAR_INTERVAL = 3;

    //юзает паспорт и LobbyOstrov в QuestManager.updateProgress
    public int getPasportFillPercent() {
        double max = 0;
        for (E_Pass ep:E_Pass.values()) {
            if (ep.editable) max++;
        }
        int complete = 0;
        E_Pass ePass;
        for (Data d:dataString.keySet()) {
            ePass = E_Pass.fromStrind(d.name());
            if (ePass!=null && ePass.editable && !dataString.get(d).isEmpty()) {
                complete++;
            }
        }
        return (int) Math.round(complete/max*100);
    }

    @Deprecated 
    public Map<E_Pass, String> getPassportData(final boolean skipUneditable) { //для паспорта
        final EnumMap<E_Pass,String>result = new EnumMap<>(E_Pass.class);
        E_Pass ePass;
        for (Data d:dataString.keySet()) {
            ePass = E_Pass.fromStrind(d.name());
            if (ePass!=null && (!skipUneditable || ePass.editable)) {
                result.put(ePass, dataString.get(d));
            }
        }
        for (Data d:dataInt.keySet()) {
            ePass = E_Pass.fromStrind(d.name());
            if (ePass!=null && (!skipUneditable || ePass.editable)) {
                result.put(ePass, String.valueOf(dataInt.get(d)) );
            }
        }
        for (Stat st:stat.keySet()) {
            ePass = E_Pass.fromStrind(st.name());
            if (ePass!=null && (!skipUneditable || ePass.editable)) {
                result.put(ePass, String.valueOf(stat.get(st)) );
            }
        }
        //for (Stat st:daylyStat.keySet()) {
        //    if (E_Pass.exist(st.name())) {
        //        result.put(E_Pass.valueOf(st.name()), ""+daylyStat.get(st));
        //    }
        //}
//System.out.println("result="+result);
        result.put(E_Pass.USER_GROUPS, chat_group);
        return result;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public final String nik;
    public final UUID id;
    public boolean eng; //true - english; false - russian
    public int karmaCalc, reputationCalc; //просчитывается в 
    private final int loginTime = ApiOstrov.currentTimeSec();
    private int daylyLoginTime=loginTime;   //время входа для дневной статы, сброс в полночь
    public int onlineSecond; //счётчик секунд после входа
    public int tick; //каждые 20 тиков будет вызов secondTick из таймера
    public final boolean isGuest;
    public boolean isStaff; //флаг модератора
    public final Map <String, String> mysqlData = new HashMap<>();
    public int mysqRecordId = Integer.MIN_VALUE;
    
    protected final EnumMap<Data,String>dataString = new EnumMap<>(Data.class); //локальные снимки,сохранятьне надо. сохраняются в банжи
    protected final EnumMap<Data,Integer>dataInt = new EnumMap<>(Data.class);  //локальные снимки,сохранятьне надо. сохраняются в банжи
    protected final EnumMap<Stat,Integer> stat = new EnumMap<>(Stat.class);  //локальные снимки,сохранятьне надо. сохраняются в банжи
    protected final EnumMap<Stat,Integer>daylyStat = new EnumMap<>(Stat.class);  //локальные снимки,сохранятьне надо. сохраняются в банжи
    public final Set <Integer> missionIds=new HashSet<>();
    
    //подгружается с локальной ЮД
    public final CaseInsensitiveMap <String> homes=new CaseInsensitiveMap<>();
    public final CaseInsensitiveMap <String> world_positions=new CaseInsensitiveMap<>();
    public final CaseInsensitiveMap <Integer> kits_use_timestamp=new CaseInsensitiveMap<>();
    public final CaseInsensitiveMap <Integer> limits=new CaseInsensitiveMap<>();

    //вычисляется из данных прокси
    public CaseInsensitiveSet groups = new CaseInsensitiveSet();
    public CaseInsensitiveSet user_perms = new CaseInsensitiveSet();
    public final EnumMap<CheatType,Integer>cheats = new EnumMap<>(CheatType.class); //локальные снимки,сохранятьне надо. сохраняются в банжи
    
    //Друзья
    public final CaseInsensitiveSet friends=new CaseInsensitiveSet(); //друг,сервер
    public final CaseInsensitiveSet friendInvite = new CaseInsensitiveSet(); //предложения дружить, до выхода
    public String party_leader = ""; //ник лидера команды
    public CaseInsensitiveMap<String> party_members = new CaseInsensitiveMap<>(); //ник, сервер всех членов команды
    public final CaseInsensitiveSet partyInvite = new CaseInsensitiveSet(); //предложения в комманду, до выхода
    private final CaseInsensitiveSet blackList=new CaseInsensitiveSet(); //хранится на банжи до перезахода, синхронизируется
    public final Map <Quest, IProgress> quests = new HashMap<>();
    
    public PermissionAttachment permissionAttachmen=null;
    public ProfileManager menu;
    public CustomScore score;
    private boolean hideScore = false; //для лобби-чтобы не конфликтовал показ онлайна и кастомные значения

    public Location last_death=Bukkit.getWorlds().get(0).getSpawnLocation();

    public String chat_group=" ---- ";
    private String tab_prefix="§7",name_color="§7", tab_suffix="";
    private TextComponent tag_prefix=TCUtils.format("§7");
    private TextComponent tag_suffix=TCUtils.format("");
    
    public float flyf_speed=0.1F;
    public int mysql_stage, pvp_time, no_damage;//, bplace, bbreak, mobkill, monsterkill, pkill, dead;
    public boolean mysqlError, allow_fly, /*in_fly, */resourcepack_locked=true, pvp_allow=true;
    
    public InputData inputData; //ввод данных с чата или таблички
    
    //служебные
    public SetupMode setup; //для билдеров
    public BukkitTask displayCube; //показ границы выделения
    public BukkitTask spyTask;
    public Gender gender = Gender.NEUTRAL;
    public String lastCommand; //последняя команда введёная билдером
    
    //Боссбар, титны, экшэнбар с задержками
    public final List<String>delayActionbars = new ArrayList<>();
    public final BossBar bossbar = BossBar.bossBar(Component.text(""), 0, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_12, Collections.emptySet());
    public int barTime, barMaxTime, nextTitle, nextAb;
    public boolean timeBar; public float progress;
    public final List<DelayBossBar>delayBossBars = new ArrayList<>();
    public final List<Title>delayTitles = new ArrayList<>();


    public Oplayer (final HumanEntity p) {
        nik=p.getName();
        id=p.getUniqueId();
        menu = new ProfileManager(this);
        isGuest = nik.startsWith("guest_");
        if (p instanceof Player) score=new CustomScore((Player) p);
    	VM.getNmsNameTag().updateTag(Oplayer.this, Bukkit.getOnlinePlayers());
    }    
    
    
    public void secondTick() {
        final Player p = getPlayer();
        if (p==null) {
            Ostrov.log_warn("Oplayer "+nik+" secondTick : Player==null!");
            return;
        }
        
        if (pvp_time>0) {
            pvp_time--;
            if (pvp_time==0) PvpCmd.pvpEndFor(this, p);    //не переставлять!!
        }

        if (nextAb>0) {
            nextAb--;
            if (nextAb==0) {
                if (!delayActionbars.isEmpty()) {
                    nextAb = ACTION_BAR_INTERVAL;
                    final String ab = delayActionbars.remove(0);
                    p.sendActionBar(Component.text(ab));
                }
            }
        }     
        
        if (nextTitle>0) {
            nextTitle--;
            if (nextTitle==0) {
                if (!delayTitles.isEmpty()) {
                    final Title t = delayTitles.remove(0);
                    p.showTitle(t);
                    final Title.Times tm = t.times();
                    nextTitle = tm.fadeIn().toSecondsPart()+tm.stay().toSecondsPart()+tm.fadeOut().toSecondsPart() + 1;
                }
            }
        }
        
        if (barTime>0) {
            barTime--;
            if (barTime == 0) {
                if (!delayBossBars.isEmpty()) {
                    final DelayBossBar bb = delayBossBars.remove(0);
                    bb.apply(this, p);
                } else p.hideBossBar(bossbar);
            } else if (timeBar) {
                final float time = barMaxTime > 0 ? (float) barTime / (float) barMaxTime : 0f;
                bossbar.progress(time > 1f ? 1f : time < 0f ? 0f : time);
			}
        }
        
        if (dataString.isEmpty() && getOnlineSec() > 1 ) {
            if (getOnlineSec()<15) {
                SpigotChanellMsg.sendMessage(p, Operation.RESEND_RAW_DATA, nik);
                ApiOstrov.sendActionBarDirect(p, "§5Ожидание данных с прокси..");
                return;
            } else  if (getOnlineSec()==15) {
                p.sendMessage("§cДанные с прокси не получены, попробуйте перезайти!");
            }
        }
        
        menu.tick(p); //обновление lore в меню ProfileManager

        if (Config.ostrovStatScore && onlineSecond%10==0) {
            updScore();
        }
//Ostrov.log_warn(" ============== tablist_header_footer="+PM.tablist_header_footer);
        if (Config.tablist_header_footer) {
            ApiOstrov.sendTabList(p,  "§7Сервер: §5"+GM.GAME.displayName+"§7 §6"+ApiOstrov.getCurrentHourMin(), "  §fГлавное меню - §a/menu");
            //p.setPlayerListName(tab_list_name_prefix+tab_list_name_color+nik+tab_list_name_siffix); - один раз при входе, или мешает плагинам
        }
        
        if (onlineSecond==4) {
            setData(Data.WANT_ARENA_JOIN, "");
        }
        
        onlineSecond++;
    }

    public int getOnlineSec() {
        return onlineSecond;//ApiOstrov.currentTimeSec()-loginTime;
    }
    
    public Set<String> getPartyMembers() {
        return party_members.keySet();
    }
    
    public CaseInsensitiveMap<String> getPartyData() {
        return party_members;
    }
    
    public boolean isPartyLeader() {
//System.out.println("** isPartyLeader() party_members="+party_members+"  party_leader="+party_leader+" nik="+nik);
        return !party_members.isEmpty() && party_leader.equals(nik);
    }
    
    
    
    public String tabPrefix() {
    	return tab_prefix;
    }
    
    public void tabPrefix(final String pr, final Player p) {
    	tab_prefix = pr == null ? "" : pr;
        if (Config.tablist_name) {
        	final String name = name_color + (isGuest ? "Гость_" + nik.substring(6) : nik);
    		p.playerListName(TCUtils.format(tab_prefix + name + tab_suffix));
        }
    }
    
    public String tabSuffix() {
    	return tab_suffix;
    }
    
    public void tabSuffix(final String sf, final Player p) {
    	tab_suffix = sf == null ? "" : sf;
        if (Config.tablist_name) {
        	final String name = name_color + (isGuest ? "Гость_" + nik.substring(6) : nik);
    		p.playerListName(TCUtils.format(tab_prefix + name + tab_suffix));
        }
    }
    
    public String nameColor() {
    	return name_color;
    }
    
    public void nameColor(final String nc, final Player p) {
    	name_color = nc == null ? "" : nc;
        VM.getNmsNameTag().updateTag(this, Bukkit.getOnlinePlayers());
        if (Config.tablist_name) {
        	final String name = name_color + (isGuest ? "Гость_" + nik.substring(6) : nik);
    		p.playerListName(TCUtils.format(tab_prefix + name + tab_suffix));
        }
    }
    
    
    
    public void tag(final String prefix, final String suffix) {
    	tag_prefix = TCUtils.format(prefix);// == null ? Component.text("", NamedTextColor.GRAY) : pr;
    	tag_suffix = TCUtils.format(suffix);// == null ? Component.text("") : sf;
        VM.getNmsNameTag().updateTag(this, Bukkit.getOnlinePlayers());
    }
    
    public void tag(final String prefix, final String color, final String suffix) {
    	tag_prefix = TCUtils.format(prefix);// == null ? Component.text("", NamedTextColor.GRAY) : pr;
    	tag_suffix = TCUtils.format(suffix);// == null ? Component.text("") : sf;
    	name_color = color == null ? "" : color;
        VM.getNmsNameTag().updateTag(this, Bukkit.getOnlinePlayers());
    }
    
    public TextComponent tagPrefix() {
    	return tag_prefix;
    }
    
    public TextComponent tagSuffix() {
    	return tag_suffix;
    }
    
    public boolean isTagVis(final Player to) {
    	return true;
    }
    
    public void onPVPEnter(final Player p, final int time, 
    	final boolean blockFly, final boolean giveTag) {
        ApiOstrov.sendActionBarDirect(p, "§cРежим боя " + time + " сек.!");
    	if (blockFly) {
//            p.setFlySpeed(fly_speed);
            p.setAllowFlight(false);
            p.setFlying(false);
            if (p.isGliding()) {
            	p.setGliding(false);
                ApiOstrov.sendActionBarDirect(p, "§cКажется, Вам прострелили крыло :(");
            }
    	}
        if (giveTag) nameColor("§4⚔ ", p);
        pvp_time = time;
    }
    
    public void onPVPEnd(final Player p, 
    	final boolean blockFly, final boolean giveTag) {
        ApiOstrov.sendActionBarDirect(p, "§aТы больше не в бою!");
//    	if (p == null) return;
    	if (blockFly) {
//            p.setFlySpeed(fly_speed);
            p.setAllowFlight(
                switch (p.getGameMode()) {
                case CREATIVE, SPECTATOR -> true;
                default -> allow_fly;
            });
    	}
        if (giveTag) nameColor("§7", p);
    }
    
    public String getDataString(final Data data) {
        return dataString.getOrDefault(data, data.def_value);
    }

    public int getDataInt(final Data data) {
        return dataInt.getOrDefault(data, ApiOstrov.getInteger(data.def_value));
    }

    public boolean setData(final Data e_data, final int value) {  //отправляем на банжи, и обнов.локально
        if ( SpigotChanellMsg.sendMessage(getPlayer(), Operation.SET_BUNGEE_DATA, nik, e_data.tag, value, "", "") ) {
            if (e_data==Data.RIL) {
                Ostrov.globalLog(GlobalLogType.ADD_RIL, nik, "setData RIL old="+dataInt.get(e_data)+" new="+value);
            } else if (e_data==Data.LANG) {
                eng = value == 1;
            }
            dataInt.put(e_data, value);
            return true;
        } else {
            getPlayer().sendMessage(Ostrov.PREFIX+"§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            Ostrov.log_err("§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            return false;
        }
    }
    public boolean setData(final Data e_data, final String value) {  //отправляем на банжи, и обнов.локально
        //if (getPlayer()==null) return false;
        if ( SpigotChanellMsg.sendMessage(getPlayer(), Operation.SET_BUNGEE_DATA, nik, e_data.tag, 0, value, "") ) {
            dataString.put(e_data, value);
            return true;
        } else {
            getPlayer().sendMessage(Ostrov.PREFIX+"§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            Ostrov.log_err("§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            return false;
        }
    }

    public EnumMap<Stat,Integer> getStatMap() {
        return stat;
    }
    
    public int getStat(final Stat st) {
        int record = stat.getOrDefault(st, 0);
        return switch (st) {
            case PLAY_TIME -> record + (ApiOstrov.currentTimeSec() - loginTime);
            case REPUTATION -> reputationCalc;
            case KARMA -> karmaCalc;
            default -> record;
        };
    }
    
    public int getDaylyStat(final Stat st) {
        int record = daylyStat.getOrDefault(st, 0);
        return switch (st) {
            case PLAY_TIME -> record + (ApiOstrov.currentTimeSec() - daylyLoginTime);
            default -> record;
        };
    }

    public void addStat(final Stat st, final int value) {
//System.out.println("-setStat e_stat="+e_stat.toString()+" value="+value+" getPlayer()="+getPlayer());
        switch (st) {
            case PLAY_TIME, REPUTATION, KARMA -> {
                return;
            }
            case EXP -> {
                addExp(getPlayer(), value);
                return;
            }
            default -> {}
        }
        stat.put(st, getStat(st)+value);
        daylyStat.put(st, getDaylyStat(st)+value);
        SpigotChanellMsg.sendMessage(getPlayer(), Operation.ADD_BUNGEE_STAT, nik, st.tag, value, "", "");
        MissionManager.onStatAdd(this, st, value);
        //ApiOstrov.sendMessage(getPlayer(), Action.SET_DATA_TO_BUNGEE, st.tag+E_Stat.diff, getDaylyStat(st), "", ""); //надо отдельно, или вычислять старое значение ?
    }
    
    public void addExp(final Player p, int value) { //отдельным методом, т.к. надо ставить отдельно уровень и дневное накопление
        if (value<=0) return;
        int curr_level = getStat(Stat.LEVEL);
        int lvlAdd = 0; //расчёт, сколько добавится уровня
        int xpCache = value + getStat(Stat.EXP);
//System.out.println("addExp "+p.getName()+":"+value+" curr_level="+curr_level+" xpCache="+xpCache);
        if (xpCache>=curr_level*25) { //опыта достаточно для след.уровня
	        for ( ;xpCache >= (curr_level+lvlAdd)*25; lvlAdd++ ) { //делаем грубый расчёт добавленных уровней
                xpCache -= (curr_level+lvlAdd)*25; //сначала убавить опыт со старым lvlAdd!
            }
        }     
        
        if (lvlAdd > 0) { //уровень добавляется
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
            addStat(Stat.LEVEL, lvlAdd); //добавляем уровень
            stat.put(Stat.EXP, xpCache); //запомнить оставшийся опыт
            SpigotChanellMsg.sendMessage(p, Operation.SET_BUNGEE_DATA, nik, Stat.EXP.tag, xpCache, "", ""); //обновить на банжи
            daylyStat.put(Stat.EXP, getDaylyStat(Stat.EXP)+value); //увеличение дневного счётчика опыта
            SpigotChanellMsg.sendMessage(p, Operation.SET_BUNGEE_DATA, nik, Stat.EXP.tag+Stat.diff, getDaylyStat(Stat.EXP), "", "");
            ApiOstrov.sendTitle(p, "§7.", Ostrov.PREFIX+"Новый уровень : §b"+getStat(Stat.LEVEL), 20, 60, 40);
            ApiOstrov.sendBossbar(p, Ostrov.PREFIX+"Новый уровень : §b"+getStat(Stat.LEVEL), 8, Color.GREEN, Overlay.NOTCHED_20);
        } else { //уровень не меняется - просто добавляем опыт
            //addStat(Stat.EXP, value); addStat нельзя - деадлок!
            stat.put(Stat.EXP, xpCache);
            daylyStat.put(Stat.EXP, getDaylyStat(Stat.EXP)+value);
            SpigotChanellMsg.sendMessage(getPlayer(), Operation.ADD_BUNGEE_STAT, nik, Stat.EXP.tag, value, "", ""); //на банжике уровень не пересчитываем!
            if (value>10) ApiOstrov.sendActionBar(nik, (curr_level*25-getStat(Stat.EXP)+1) +"§7 опыта до следующего уровня"); //+1 - фикс, или писало 0 до след уровня
        }
    }

        public void resetDaylyStat() {
        daylyStat.clear();
        daylyLoginTime = ApiOstrov.currentTimeSec();
    }


    public boolean hasFlag(final StatFlag flag) {
        return StatFlag.hasFlag(getStat(Stat.FLAGS), flag);
    }
    
    public boolean hasDaylyFlag(final StatFlag flag) {
        return StatFlag.hasFlag(getDaylyStat(Stat.FLAGS), flag);
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
        int value = getDaylyStat(Stat.FLAGS);
        value = state ? (value | (1 << flag.tag)) : value & ~(1 << flag.tag);
        daylyStat.put(Stat.FLAGS, value);
        SpigotChanellMsg.sendMessage(getPlayer(), Operation.SET_BUNGEE_DATA, nik, Stat.FLAGS.tag+Stat.diff, value, "", "");
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

    public String getTextData (String key) {
        final String dataRaw = getDataString(Data.TEXTDATA);
        if (dataRaw.isEmpty()) return "";
        key = key+"。";
        for (final String s : dataRaw.split("︙")) { //∬ не использовать! конфликт с банжиданными!
            if (s.startsWith(key)) {
                return s.replaceFirst(key, "");
            }
        }
        return "";
    }
    
    public void setTextData (final String key, final String value) {
        if (key==null) return;
        final String dataRaw = getDataString(Data.TEXTDATA);
        final HashMap<String,String>data = new HashMap<>();
        if (!dataRaw.isEmpty()) {
            int splitterIndex;
            for (String s : dataRaw.split("︙")) { //∬ не использовать! конфликт с банжиданными!
                splitterIndex = s.indexOf("。");
                if (splitterIndex>0) {
                    data.put(s.substring(0, splitterIndex), s.substring(splitterIndex+1) );
                }
            }
        }
        if (value==null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
        String build="";
        for (String k : data.keySet()) {
            build = build+"︙"+k+"。"+data.get(k);
        }
        setData(Data.TEXTDATA, build.replaceFirst("︙", ""));
    }


    
    protected void updScore() {
        if (!Config.ostrovStatScore || hideScore) return;
        //if (board==null) return;
        score.getSideBar().setTitle("§7Общий онлайн: §f§l"+GM.bungee_online);//"§a-----------------"
        score.getSideBar().updateLine(15, "§a--------------");
        score.getSideBar().updateLine(14, "уровень §b"+getStat(Stat.LEVEL));
        score.getSideBar().updateLine(13, "опыт §5"+getStat(Stat.EXP));
        score.getSideBar().updateLine(12, "репутация "+getReputationDisplay());
        score.getSideBar().updateLine(11, "карма "+getKarmaDisplay());
        score.getSideBar().updateLine(10, "лони §6"+getDataInt(Data.LONI));
        score.getSideBar().updateLine(9, "рил §e"+getDataInt(Data.RIL));
        score.getSideBar().updateLine(1, "§a--------------");
    }
    




    public void setNoDamage(final int seconds, final boolean actionBar) {
        if (seconds>no_damage) {
            no_damage=seconds;
            if (actionBar) {
                ApiOstrov.sendActionBar(getPlayer(), "§aВам дарована неуязвимость на "+no_damage+" сек!");
            }
        }
    }



    public boolean Pvp_is_allow() {
        return pvp_allow;
    }



// ---------------------- Наборы ---------------------------------------------
    public boolean hasKitAcces(final String kitName) {
        return kits_use_timestamp.containsKey(kitName);
    }
    public void addKitAcces(final String kitName) {
        kits_use_timestamp.put( kitName, 0 );
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
        kits_use_timestamp.put( kitName, ApiOstrov.currentTimeSec());
        mysqlData.put("kitsUseData", null); //пометить на сохранение
    }
   // public Map <String, Long> GetKitsData() {
   //     return kits_use_timestamp;
   // }
//------------------------------------------------------------------------------


    public Player getPlayer(){
        return  Bukkit.getPlayerExact(nik);
    }

    public boolean bungeeDataRecieved() {
        return !dataString.isEmpty();
    }

    public void onLeave(final Player p) {
        //в saveLocalData инвентарь не сохранит
        if (PvpCmd.getFlag(PvpCmd.PvpFlag.drop_inv_inbattle) &&  PvpCmd.getFlag(PvpCmd.PvpFlag.antirelog) && pvp_time>0) {      //если удрал во время боя
            final List<ItemStack> drop = new ArrayList<>();
            for (ItemStack is : p.getInventory().getContents()) {
                if (is != null && !MenuItemsManager.isSpecItem(is)) {
                    drop.add(is.clone());
                }
            }
            Ostrov.sync( () -> {
                for (ItemStack is : drop) {
                    p.getWorld().dropItemNaturally(p.getLocation(), is).setPickupDelay(40);
                }
            }, 10);
        }
        
    	if (!mysqlError && !mysqlData.isEmpty() && LocalDB.useLocalData) {
            Ostrov.async(()->LocalDB.saveLocalData(p, this), 0); //op.mysqlData не должна быть пустой, если загружало!
    	}
    }
    
    

    
    
    








    public boolean hasGroup(final String group_name) {
        if (groups.contains(group_name)) return true;
        return groups.stream().anyMatch( (gr) -> (Perm.getGroup(gr)!=null && Perm.getGroup(gr).inheritance.contains(group_name)) );
    }

    public Collection<String> getGroups() {
        return groups;
    }

    public Collection<String> getUserPerms() {
        return user_perms;
    }
    
    public int getKarmaModifier(final Stat.KarmaChange kc) {
        int result = 0;
        for (Stat st : stat.keySet()) {
            if (st.karmaChange==kc) {
                result+=stat.get(st);
            }
        }
        return result;
    }

    public String getKarmaDisplay() {
        return karmaCalc==0 ? "§7равновесие" : (karmaCalc>0 ? "§a"+karmaCalc : "§c"+karmaCalc);
    }

    public String getReputationDisplay() {
        return reputationCalc==0 ? "§7---" : (reputationCalc>0 ? "§a"+reputationCalc : "§c"+reputationCalc);
    }

    public int getStatFill() {
        return stat.size()<=6 ? 0 : stat.size() - 6; //не учитываем PLAY_TIME EXP LEVEL FLAGS REPUTATION KARMA
    }
    public boolean hasData(final Data d) {
        return dataString.containsKey(d)  || dataInt.containsKey(d);
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
        return Timer.getTime() - loginTime;
    }

    public void hideScore() {
        hideScore = true;
        score.getSideBar().reset();
    }
    public void showScore() {
        hideScore = false;
    }


    public void updateGender() {
        switch (TCUtils.stripColor(getDataString(Data.GENDER)).toLowerCase()) {
            case "девочка" -> gender = Gender.FEMALE;
            case "мальчик" -> gender = Gender.MALE;
            default -> gender = Gender.NEUTRAL;
        }
    }

    public String getTopPerm() {
        if (hasGroup("owner"))
            return "Создатель";
        if (hasGroup("xpanitely"))
            return "Сис-Админ";
        if (hasGroup("supermoder"))
            return "Персонал";
        if (hasGroup("legend"))
            return "Легенда";
        if (hasGroup("hero"))
            return "Герой";
        if (hasGroup("warior"))
            return "Воин";
        return "";
    }



    public boolean isLocalChat() {
        return hasFlag(StatFlag.LocalChat);
    }

    public boolean setLocalChat(boolean local) {
        boolean currentLocal = hasFlag(StatFlag.LocalChat);
        if ( (currentLocal && local) || (!currentLocal && !local) ) return false; //итак локальный и ставим локальный, или итак глобальный и ставим глобальный
        setFlag(StatFlag.LocalChat, local);
        return true;
    }

    

    @Deprecated
    public void addCd(final String type, final int seconds) {
       Timer.add(nik, type, seconds);//if (seconds<=0) return;coutDown.put(type.hashCode(), Timer.getTime() + seconds);
    }
    @Deprecated
    public void delCd(final  String type) {
       Timer.del(nik, type);//coutDown.remove(type.hashCode());
    }
    @Deprecated
    public int getCd(final  String type) {
        return Timer.getLeft(nik, type);//int left = coutDown.getOrDefault(type.hashCode(), 0);return left==0 ? 0 : left - Timer.getTime();
    }
    @Deprecated
    public boolean hasCd(final  String type) {
        return Timer.has(nik, type);
    }

}
 /*
    
    //    пол
    public static String genderEnd_Существительное(final String name) {
        final Oplayer op = PM.getOplayer(name);
        if (op!=null) {
            return switch (TCUtils.stripColor(op.getDataString(Data.GENDER)).toLowerCase()) {
                case "девочка" -> "а";
                case "бесполоe", "гермафродит" -> "о";
                default -> "";
            };
        } else return "";
    }

    public static boolean isFemale(final String name) {
        final Oplayer op = PM.getOplayer(name);
        return op!=null && TCUtils.stripColor(op.getDataString(Data.GENDER)).equalsIgnoreCase("девочка");
    }
*/
