package ru.komiss77.Objects;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Commands.CMD;
import ru.komiss77.Commands.Pvp;
import ru.komiss77.Enums.Action;
import ru.komiss77.Enums.Data;
import ru.komiss77.Events.BungeeDataRecieved;
import ru.komiss77.Events.FriendTeleportEvent;
import ru.komiss77.Events.MysqlDataLoaded;
import ru.komiss77.Events.PartyUpdateEvent;
import ru.komiss77.Managers.MysqlLocal;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.SM;
import ru.komiss77.Managers.StatManager;
import ru.komiss77.Managers.Timer;
import ru.komiss77.Ostrov;
import ru.komiss77.ProfileMenu.E_Prof;
import ru.komiss77.ProfileMenu.E_Stat;
import ru.komiss77.modules.OstrovDB;
import ru.komiss77.scoreboard.CustomScore;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import builder.SetupMode;
import ru.komiss77.Enums.StatFlag;





public final class Oplayer {
  
    public String nik;
    public SetupMode setup;
    
    private final EnumMap<Data,String>dataString = new EnumMap(Data.class); //локальные снимки,сохранятьне надо. сохраняются в банжи
    private final EnumMap<Data,Integer>dataInt = new EnumMap(Data.class);  //локальные снимки,сохранятьне надо. сохраняются в банжи
    private final EnumMap<E_Stat,Integer> stat = new EnumMap(E_Stat.class);  //локальные снимки,сохранятьне надо. сохраняются в банжи
    private final EnumMap<E_Stat,Integer>daylyStat = new EnumMap(E_Stat.class);  //локальные снимки,сохранятьне надо. сохраняются в банжи
    
    private final CaseInsensitiveMap <Location> homes=new CaseInsensitiveMap<>();
    private final Map <String, Location> world_positions=new ConcurrentHashMap<>();
    private final CaseInsensitiveMap <Integer> kits_use_timestamp=new CaseInsensitiveMap<>();

    public CaseInsensitiveLinkedTreeSet groups = new CaseInsensitiveLinkedTreeSet();
    public CaseInsensitiveLinkedTreeSet user_perms = new CaseInsensitiveLinkedTreeSet();
    
    private final HashMap<String,String> party_members = new HashMap<>();
    public PermissionAttachment permissionAttachmen=null;
    public Inventory settings,profile;
    public E_Prof e_profile=E_Prof.ПОМОЩЬ;
    Location last_death=Bukkit.getWorlds().get(0).getSpawnLocation();
    
    public CustomScore score;
                                                                                   //  для режима пвп          для модеров
    public String party_leader="",chat_group=" ---- ",tab_list_name_prefix="§7",tab_list_name_color="§7", tab_list_name_siffix="";
    
    private float fly_speed=0.1F,walk_speed=0.1F;
    private final int login_time = Timer.currentTimeSec();
    public int pvp_time, no_damage, bplace, bbreak, mobkill, monsterkill, pkill, dead, aac_violations,bow_teleport_cooldown=4;     
    private int resetWantArena;
    public boolean mysqldata_loaded=false,allow_fly=false,in_fly=false,resourcepack_locked=true,pvp_allow=true;
    public boolean isStaff;
    
    



    
    
    public Oplayer (final Player p) {
        nik=p.getName();
        profile=Bukkit.createInventory( p, 54,  ItemUtils.profile_master_inv_name );
        profile.setContents(ItemUtils.profile_master.getContents());
        
        score=new CustomScore(p);

        if (CMD.no_damage_on_tp >0) {
            setNoDamage(CMD.no_damage_on_tp, true);
        }
    }    

    
    
    
    public void Tick_every_second(final Player p, final int serverSecCounter) {
//System.out.println("tick pvp_time="+pvp_time);
        if (pvp_time>0) {
            pvp_time--;
            if (pvp_time==0) pvpBattleModeEnd();    //не переставлять!!
        }
        if (no_damage>0) {
            no_damage--;
            if (no_damage==0) ApiOstrov.sendActionBar(nik, "§4Время неуязвимости закончилось!");
        }
        if (bow_teleport_cooldown>0) bow_teleport_cooldown--;
        
        if (dataString.isEmpty() && ApiOstrov.currentTimeSec()-login_time > 1 ) {
            ApiOstrov.sendMessage(p, Action.RESEND_RAW_DATA, 0, 0, "", "");
        }
        if (PM.ostrovStatScore && serverSecCounter%10==0) {
            updScore();
        }
        
        if (PM.tablist_header_footer) { //if (SM.this_server_name.length()>4) {
            ApiOstrov.sendTabList(p,  "§7Привет, §a"+nik+" §7Вы находитесь: §5"+SM.this_server_bungee_description+"§7 Сейчас: §6"+ApiOstrov.getCurrentHourMin(), "  §fПомощь - §a/help §fСервер - §a/serv §fПрофиль - §a/profile §fМеню - §a/menu");
            p.setPlayerListName(tab_list_name_prefix+tab_list_name_color+nik+tab_list_name_siffix);
        }
        
        if (resetWantArena>0) {
            resetWantArena--;
            if (resetWantArena==0) {
                dataString.remove(Data.WANT_ARENA_JOIN);
            }
        }
        //if (SM.this_server_name.length()!=4) {
        //}
//getPlayer().setPlayerListName(nik);
//System.out.println("-passport=="+(passport==null?"null":passport));
    }

    public void tickAsync(final Player p, int counter) {
        if (setup!=null && counter%2==0) {
            setup.updateAsync(p);
        }
    }





     
    @Deprecated
    public Map<Data, String> getBungeeData() { //для паспорта
        final EnumMap<Data,String>result = new EnumMap(Data.class);
        for (Data d:dataString.keySet()) {
            result.put(d, dataString.get(d));
        }
        for (Data d:dataInt.keySet()) {
            result.put(d, String.valueOf(dataInt.get(d)));
        }
        return result;
    }


    

    
    public void bungeeDataInject(final Player p, final String raw) { //всё сразу

        dataString.put(Data.NAME, nik);
//System.out.println("+++bungeeDataInject raw="+raw);            
        int enumTag;
        String value;
        int v;

        for (String s:raw.split("∫")) {
            if ( s.length()<4) continue;

            enumTag = ApiOstrov.getInteger(s.substring(0, 3));
            value = s.substring(3);

            if (enumTag>100 && enumTag<299) {
                    final Data _data = Data.byTag(enumTag);
                    if (_data!=null) {
                        if (_data.is_integer) {
                            v = ApiOstrov.getInteger(value);
                            if (v>Integer.MIN_VALUE) dataInt.put (_data, v);
                        } else {
                            dataString.put (_data, value);
                        }
                    } 
            } else if (enumTag>300 && enumTag<599) {
                final E_Stat e_stat = E_Stat.byTag(enumTag);
                v = ApiOstrov.getInteger(value);
                if (e_stat!=null && v>Integer.MIN_VALUE) {
                    stat.put(e_stat, login_time);
                }
            } else if (enumTag>600 && enumTag<899) {
                final E_Stat e_stat = E_Stat.byTag(enumTag-E_Stat.diff);
                v = ApiOstrov.getInteger(value);
                if (e_stat!=null && v>Integer.MIN_VALUE) {
                    daylyStat.put(e_stat, v);
                }
            }

        }
        if (dataString.containsKey(Data.WANT_ARENA_JOIN)) {
            resetWantArena = 5;
        }

        if (dataString.containsKey(Data.PARTY_MEBRERS) && !dataString.get(Data.PARTY_MEBRERS).isEmpty()) { //пати в виде списка, лидер - первый
//System.out.println("bungeeDataInject() PARTY_MEBRERS = "+bungeeData.get(Data.PARTY_MEBRERS));
            try {
                onPartyRecieved(p, dataString.get(Data.PARTY_MEBRERS), false);
            } catch (NumberFormatException | IllegalStateException | ArrayIndexOutOfBoundsException | NullPointerException ex) {
                Ostrov.log_err("bungeeDataInject : onPartyRecieved "+ex.getMessage());
                p.sendMessage("§cОШИБКА в onPartyRecieved, сообщите Администрации! "+ex.getMessage());

            }
        }

        try {
            PM.calculatePerms(p, this, false); 
        } catch (NumberFormatException | IllegalStateException | ArrayIndexOutOfBoundsException | NullPointerException ex) {
            Ostrov.log_err("bungeeDataInject : calculatePerms "+ex.getMessage());
            p.sendMessage("§cОШИБКА в calculatePerms, сообщите Администрации! "+ex.getMessage());

        }


        try {
             StatManager.calculateReputationBase(this);
        } catch (NumberFormatException | IllegalStateException | ArrayIndexOutOfBoundsException | NullPointerException ex) {
            Ostrov.log_err("bungeeDataInject : calculateReputationBase "+ex.getMessage());
            p.sendMessage("§cОШИБКА в calculateReputationBase, сообщите Администрации! "+ex.getMessage());

        }

        updScore();
        Bukkit.getPluginManager().callEvent(new BungeeDataRecieved ( p, this ) );
        //Bukkit.getPluginManager().callEvent(new BungeeStatRecieved (this) );

//System.out.println("-Данные с банжи получены! data="+bungeeData); 
    }
    
    //обнова данных с банжи по SET_DATA_TO_OSTROV - значит на острове уже новое значение
    //в основном для даты, но можно изменить отдельную стату (например, E_Stat.FLAGS) минуя addStat
    public void updateDataFromBungee(final Player p, final int enumTag, final int int2, final String string1) {
//System.out.println("-updateDataFromBungee e_data="+e_data.toString()+" value="+value);
        if (enumTag>100 && enumTag<299) {
            final Data d = Data.byTag(enumTag);
            if (d!=null) {
                boolean change;
                if (d.is_integer) {
                    change = dataInt.put(d, int2)!=int2;//dataInt.put (_data, int2);
                } else {
                    change = !dataString.put(d, string1).equals(string1);//dataString.put (d, string1);
                }
                if (change) {
                    switch (d) {
                        case USER_GROUPS: 
                            PM.calculatePerms(p, this, true); 
                            StatManager.calculateReputationBase(this);
                            break;
                        case USER_PERMS: 
                            PM.calculatePerms(p, this, false);
                            break;
                        //case ИМЯ_ФАМИЛИЯ: 
                        //    break;
                    }
                }
            }
            
        } else if (enumTag>300 && enumTag<599) {
            final E_Stat e_stat = E_Stat.byTag(enumTag);
            if (e_stat!=null) {
                stat.put(e_stat, int2);
            }
        } else if (enumTag>600 && enumTag<899) {
            final E_Stat e_stat = E_Stat.byTag(enumTag-300);
            if (e_stat!=null) {
                daylyStat.put(e_stat, int2);
            }
        }

    }
    
   // public void updateStatFromBungee(final Player p, final E_Stat es, final int value) {
//System.out.println("-updateDataFromBungee e_data="+e_data.toString()+" value="+value);
  //      stat.put(es, value);
 //   }    
    
    
    
    
    public String getDataString(final Data e_data) {
        return dataString.containsKey(e_data) ? dataString.get(e_data) : e_data.def_value;
    }

    public int getDataInt(final Data e_data) {
        return dataInt.containsKey(e_data) ? dataInt.get(e_data) : ApiOstrov.getInteger(e_data.def_value);
    }

    public boolean setData(final Data e_data, final int value) {  //отправляем на банжи, и обнов.локально
        if ( ApiOstrov.sendMessage(getPlayer(), Action.SET_DATA_TO_BUNGEE, e_data.tag, value, "", "") ) {
            dataInt.put(e_data, value);
            return true;
        } else {
            getPlayer().sendMessage(Ostrov.prefix+"§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            Ostrov.log_err("§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            return false;
        }
    }
    public boolean setData(final Data e_data, final String value) {  //отправляем на банжи, и обнов.локально
        //if (getPlayer()==null) return false;
        if ( ApiOstrov.sendMessage(getPlayer(), Action.SET_DATA_TO_BUNGEE, e_data.tag, 0, value, "") ) {
            dataString.put(e_data, value);
            return true;
        } else {
            getPlayer().sendMessage(Ostrov.prefix+"§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            Ostrov.log_err("§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            return false;
        }
    }

    public int getStat(final E_Stat st) {
        return stat.containsKey(st) ? stat.get(st) : 0;
    }
    
    public int getDaylyStat(final E_Stat st) {
        return daylyStat.containsKey(st) ? daylyStat.get(st) : 0;
    }

    public void addStat(final E_Stat st, final int value) {
//System.out.println("-setStat e_stat="+e_stat.toString()+" value="+value+" getPlayer()="+getPlayer());
        stat.put(st, getStat(st)+value);
        daylyStat.put(st, getDaylyStat(st)+value);
        ApiOstrov.sendMessage(getPlayer(), Action.ADD_STAT_TO_BUNGEE, st.tag, value, "", "");
        //ApiOstrov.sendMessage(getPlayer(), Action.SET_DATA_TO_BUNGEE, st.tag+E_Stat.diff, getDaylyStat(st), "", ""); //надо отдельно, или вычислять старое значение ?
    }


    public boolean hasFlag(final StatFlag flag) {
        final int value = getStat(E_Stat.FLAGS);
        return (value & (1 << flag.tag)) == (1 << flag.tag);
    }
    
    public boolean hasDaylyFlag(final StatFlag flag) {
        final int value = getDaylyStat(E_Stat.FLAGS);
        return (value & (1 << flag.tag)) == (1 << flag.tag);
    }
    
    //сетит флаг локально и в банжи, ТОЛЬКО В ГЛОБАЛЬНОЙ СТАТЕ! dayly не меняет!
    public void setFlag(final StatFlag flag, final boolean state) {
        int value = getStat(E_Stat.FLAGS);
        value = state ? (value | (1 << flag.tag)) : value & ~(1 << flag.tag);
        stat.put(E_Stat.FLAGS, value);
        ApiOstrov.sendMessage(getPlayer(), Action.SET_DATA_TO_BUNGEE, E_Stat.FLAGS.tag, value, "", "");
    }
    
    //сетит флаг локально и в банжи, ТОЛЬКО В dayly СТАТЕ! стату не меняет!
    public void setDaylyFlag(final StatFlag flag, final boolean state) {
        int value = getDaylyStat(E_Stat.FLAGS);
        value = state ? (value | (1 << flag.tag)) : value & ~(1 << flag.tag);
        daylyStat.put(E_Stat.FLAGS, value);
        ApiOstrov.sendMessage(getPlayer(), Action.SET_DATA_TO_BUNGEE, E_Stat.FLAGS.tag+E_Stat.diff, value, "", "");
    }









    
    
    
    
    @Deprecated //использует пати
    public void onPartyRecieved(final String raw, final boolean callEvent) { //прилетает при входе, нажатии в меню и обновлении состава на банжи из пати-плагина
        onPartyRecieved(getPlayer(), raw, callEvent);
    }
    public void onPartyRecieved(final Player p, final String raw, final boolean callEvent) { //прилетает при входе, нажатии в меню и обновлении состава на банжи из пати-плагина
        party_members.clear();
        boolean first=true;
        if(!raw.isEmpty()) {
            for (String player_and_server:raw.split(",")) {
                if (player_and_server.contains("<>")) {
                    if (first) {
                        party_leader=player_and_server.split("<>")[0];
                        first = false;
                    }
                    party_members.put(player_and_server.split("<>")[0], player_and_server.split("<>")[1]);
                } else {
                    party_members.put(player_and_server,"");  //при получении  bungeeData приходит в виде списка
                    if (first) {
                        party_leader=player_and_server;
                        first = false;
                    }
                }
            }
//System.out.println("---onPartyRecieved() party_leader="+party_leader+"  party_members="+party_members);
            //party_members.addAll(Arrays.asList(raw.split(",")));
        }
        Bukkit.getPluginManager().callEvent(new PartyUpdateEvent(p, party_leader, getPartyMembers()));
    }
    public Set<String> getPartyMembers() {
        return party_members.keySet();
    }
    public HashMap<String,String> getPartyData() {
        return party_members;
    }
    
    public boolean isPartyLeader() {
//System.out.println("** isPartyLeader() party_members="+party_members+"  party_leader="+party_leader+" nik="+nik);
        return !party_members.isEmpty() && !party_leader.isEmpty() && party_leader.equals(nik);
    }
    


















    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

/*
    public boolean Can_interact(int milliseconds) {
        return this.last_interact+milliseconds <= Timer.Единое_время();
    }
    public boolean Can_breack(int milliseconds) {
        return this.last_breack+milliseconds <= Timer.Единое_время();
    }
    public boolean Can_place(int milliseconds) {
        return this.last_place+milliseconds <= Timer.Единое_время();
    }
    public boolean Can_inv_click(int milliseconds) {
        return this.last_place+milliseconds <= Timer.Единое_время();
    }
*/












    
    private void updScore() {
        if (!PM.ostrovStatScore) return;
        //if (board==null) return;
        score.getSideBar().setTitle("§7Общий онлайн: §f§l"+SM.bungee_online);//"§a-----------------"
        score.getSideBar().updateLine(15, "§a-----------------");
        score.getSideBar().updateLine(14, " уровень : "+getStat(E_Stat.LEVEL));
        score.getSideBar().updateLine(13, " опыт : "+getStat(E_Stat.EXP));
        score.getSideBar().updateLine(12, " репутация: "+(getDataInt(Data.РЕПУТАЦИЯ)>=0?"§2":"§4")+getDataInt(Data.РЕПУТАЦИЯ));
        score.getSideBar().updateLine(11, " карма: "+(getDataInt(Data.КАРМА)>=0?"§2":"§4")+getDataInt(Data.КАРМА));
        score.getSideBar().updateLine(10, " лони: "+getDataInt(Data.MONEY));
        score.getSideBar().updateLine(9, " рил: "+getDataInt(Data.MONEY_REAL));
        score.getSideBar().updateLine(1, "§a-----------------");
    }
    





   public void setNoDamage(final int seconds, final boolean actionBar) {
        no_damage+=seconds;
        if (actionBar) ApiOstrov.sendActionBar(Bukkit.getPlayer(nik), "§aВам дарована неуязвимость на "+no_damage+" сек!");
    }







//---------------------- Дома, позиции -----------------------------------------
    public Map <String, Location> GetHomeData() {
        return this.homes;
    }
    public Map <String, Location> GetWorldposData() {
        return this.world_positions;
    }
    public void SetHome(String home, Location loc) {
        this.homes.put(home, loc);
    }
    public Set<String> HomeList() {
        return this.homes.keySet();
    }
    public Location GetHomeLoc(String home) {
        return this.homes.get(home);
    }
    public void DelHome(String home) {
        if (this.homes.containsKey(home)) this.homes.remove(home);
    }
    public void Set_world_position(String world, Location loc) {
//System.out.println("Set_world_position "+world+" -- "+loc);
        this.world_positions.put(world, loc);
    }
    public Set<String> Get_world_positions() {
        return this.world_positions.keySet();
    }
    public Location Get_world_position_at(String world) {
        return this.world_positions.get(world);
    }
    public void Set_back_location(Location loc) {
        this.last_death = loc;
    }
    public Location Get_back_location() {
        return this.last_death;
    }
//------------------------------------------------------------------------------













    
    public int GetPlyTime() {
        return getStat(E_Stat.PLAY_TIME) + (ApiOstrov.currentTimeSec() - login_time);//getBungeeIntData(Data.PLAY_TIME);
    }
    
    public int GetDayPlyTime() {
        return getDaylyStat(E_Stat.PLAY_TIME) + (ApiOstrov.currentTimeSec() - login_time);//getBungeeIntData(Data.PLAY_TIME);
    }
    
 /*   public int GetBalance() {
        return getDataInt(Data.MONEY);
    }


    public void moneyChange( int sum) {
//System.out.println("moneyChange sum="+sum+" Data.MONEY="+getIntData(Data.MONEY)+" curr="+curr);
        setData(Data.MONEY, dataInt.get(Data.MONEY)+sum);//moneySet(curr+value, send_update);
//System.out.println("--moneyChange Data.MONEY="+getIntData(Data.MONEY));        
            if (sum>9 || sum<-9) { //по копейкам не уведомляем
            getPlayer().spigot().sendMessage(new ComponentBuilder(Ostrov.prefix+"§7"+(sum>9?"Поступление":"Расход")+" средств: "+source+" §7-> "+(sum>9?"§2":"§4")+sum+" лони §7! §8<клик-баланс")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§5Клик - сколько стало?") ))
                .event( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/money balance") )
                .create());
            } else if (sum<-9) {
                
            }
        //ApiOstrov.sendMessage(getPlayer(), Action.SET_DATA_TO_BUNGEE, Data.MONEY.tag, sum, "", "");
        //setData(Data.MONEY, dataInt.get(Data.MONEY)+sum);
    }

    public String GetPrefix() {
        return getDataString(Data.PREFIX);
    }
    public String GetSuffix() {
        return getDataString(Data.SUFFIX);
    }
*/






    public boolean Pvp_is_allow() {
        return pvp_allow;
    }






// ---------------------- Счетчики ---------------------------------------------
public void Addbplace() { if (getPlayer().getGameMode()==GameMode.SURVIVAL) this.bplace++; }
public void Addbbreak() { if (getPlayer().getGameMode()==GameMode.SURVIVAL) this.bbreak++; }
public void Addmobkill() { if (getPlayer().getGameMode()==GameMode.SURVIVAL) this.mobkill++; }
public void Addmonsterkill() { if (getPlayer().getGameMode()==GameMode.SURVIVAL) this.monsterkill++; }
public void Addpkill() { if (getPlayer().getGameMode()==GameMode.SURVIVAL) this.pkill++; }
public void Addbdead() { this.dead++; }

public int Getbplace() { return this.bplace; }
public int Getbbreak() { return this.bbreak; }
public int Getmobkill() { return this.mobkill; }
public int Getmonsterkill() { return this.monsterkill; }
public int Getpkill() { return this.pkill; }
public int Getbdead() { return this.dead; }
//------------------------------------------------------------------------------





// ---------------------- Наборы ---------------------------------------------
    public boolean Has_kit_acces(final String kitName) {
        return kits_use_timestamp.containsKey(kitName);
    }
    public void Add_kit_acces(final String kitName) {
        kits_use_timestamp.put( kitName, 0 );
    }
    public void Remove_kit_acces(final String kitName) {
        if (kits_use_timestamp.containsKey(kitName)) kits_use_timestamp.remove(kitName);
    }
    public int Kit_last_acces(final String kitName) {
//System.out.println("+++++++kit "+kits+"   contains "+kit+"?"+this.kits.containsKey(kit)+" value:"+((this.kits.containsKey(kit))?this.kits.get(kit):"0"));    
        if (kits_use_timestamp.containsKey(kitName)) return kits_use_timestamp.get(kitName);
        //else return Timer.Единое_время()/1000/60;
        else return 0;
    }
    public void Kit_recieved(final String kitName) {
        kits_use_timestamp.put( kitName, Timer.currentTimeSec());
    }
   // public Map <String, Long> GetKitsData() {
   //     return kits_use_timestamp;
   // }
//------------------------------------------------------------------------------


    public Player getPlayer(){
//System.out.println("getPlayer nik="+nik+" p="+Bukkit.getPlayer(nik));
        //return Bukkit.getPlayer(nik);
        return  Bukkit.getPlayer(nik);
    }

    public boolean bungeeDataRecieved() {
        return !dataString.isEmpty();
    }

    
    
    
    

    public void loadLocalData(final Player p) { //вызывается после получения данных с банжи
        if (!MysqlLocal.useLocalData || p==null) return;
        
        Ostrov.async( () -> {
                
                Statement stmt = null;
                ResultSet rs = null;
                
                try {  
                        stmt = MysqlLocal.GetConnection().createStatement(); 
                        
                        rs = stmt.executeQuery( "SELECT * FROM `data` WHERE `name` LIKE '"+nik+"' LIMIT 1" );
                           
                            if (rs.next()) {
                                String[] split;
                                String[] split2;
                                
                                //if ( rs.getString("homes").length()>10 && rs.getString("homes").contains("<:>")) {
                                if ( rs.getString("homes").length()>10 ) {
                                    split = rs.getString("homes").split("<:>"); //массив дом+коорд
                                    String homeName;
                                        for (String homeAndLocationAsString : split) {
                                            split2 = homeAndLocationAsString.split("<>");
                                            if (split2.length==5) { //старый тип координат
                                                homeName = split2[0];
                                                if (!homeName.isEmpty()) {
                                                    final Location loc = LocationUtil.LocFromString(homeAndLocationAsString.replaceFirst(homeName+"<>", ""));
                                                    if (loc!=null) homes.put(homeName, loc );
                                                }
                                            } else if (split2.length==2) { //новый тип координат
                                                homeName = split2[0];
                                                if (!homeName.isEmpty()) {
                                                    final Location loc = LocationUtil.LocFromString(split2[1]);
                                                    if (loc!=null) homes.put(homeName, loc );
                                                }
                                            }
                                                //try {
                                                   // Location loc = LocationUtil.LocFromString(homeAndLocationAsString.replaceFirst(homeName+"<>", ""));
//System.out.println("homes "+nik+" raw="+homeAndLocationAsString+" home="+homeName+" loc="+loc);
                                                    //if (loc!=null) homes.put(s.split("<>")[0], s.split("<>")[1]);
                                                  //  if (loc!=null) homes.put(homeName, loc ); //название дома, локация
                                                //} catch (Exception ex) {
                                                //   Ostrov.log_err("Загрузка точки дома "+homeAndLocationAsString+" для "+nik+":"+ex.getMessage());
                                                //}
                                            //homes.put(s.split("<>")[0], s.split("<>")[1]);
                                        }
                                }
                                
                                if (CMD.save_location_on_world_change) {
                                    if ( rs.getString("world_pos").length()>10 && rs.getString("world_pos").contains("<:>")) {
                                        split = rs.getString("world_pos").split("<:>");
                                            for (String locationAsString : split) {
                                                try {
                                                    //Location loc = LocationUtil.LocFromString(s.split("<>")[1]); //первое-мир
                                                    Location loc = LocationUtil.LocFromString(locationAsString); //первое-мир
                                                    //if (loc!=null) world_positions.put(s.split("<>")[0], loc );
                                                    if (loc!=null) world_positions.put(loc.getWorld().getName(), loc );
                                                } catch (Exception ex) {
                                                   Ostrov.log_err("Загрузка точки world_pos "+locationAsString+" для "+nik+":"+ex.getMessage());
                                                }
                                            }
                                    }
                                }
                                
                                if ( rs.getString("kits").length()>5 && rs.getString("kits").contains("<:>")) {
                                    split = rs.getString("kits").split("<:>");
                                        for (String s : split) {
                                            kits_use_timestamp.put(s.split("<>")[0], Integer.valueOf(s.split("<>")[1]));
                                        }
                                }
                                pvp_allow = rs.getBoolean("pvp");
                                bplace = rs.getInt("bplace");
                                bbreak = rs.getInt("bbreak");
                                mobkill = rs.getInt("mobkill");
                                monsterkill = rs.getInt("monsterkill");
                                pkill = rs.getInt("pkill");
                                dead = rs.getInt("dead");

                                final boolean fly = rs.getBoolean("fly");
                                final int flyspeed = rs.getInt("flyspeed");
                                final int walkspeed = rs.getInt("walkspeed");
                                final int pweather = rs.getInt("pweather");
                                final boolean rtime = rs.getBoolean("rtime");
                                final int ptime = rs.getInt("ptime");
                                
                                Ostrov.sync( () ->  applyLocalSettings(p, fly, flyspeed,  walkspeed, pweather, rtime, ptime), 0);
                                    
                            } else {
                                
                                Ostrov.sync( () -> applyLocalSettings(p, false, -1,  -1, -1, true, -1), 0);

                            }
                            
                            mysqldata_loaded=true; //если данных не было, то создадутся при выходе
                            
                            Ostrov.log_ok("§2данные островитянина "+nik+" загружены!");
                            rs.close();
                            
                            
                            
                            int offlinePayAdd = 0;
                            int offlinePaySub = 0;
                            
                            rs = stmt.executeQuery( "SELECT * FROM `moneyOffline` WHERE `name` LIKE '"+nik+"' " );
                            while (rs.next()) {
//System.out.println("-- moneyOffline "+rs.getInt("value")+" §5оффлайн платёж -> "+rs.getString("who"));                                
                                if ( rs.getInt("value")>0 ) {
                                    offlinePayAdd += rs.getInt("value");
                                } else {
                                    offlinePaySub += rs.getInt("value");
                                }
                                ApiOstrov.moneyChange(p, rs.getInt("value"), "§5оффлайн платёж §f-> "+rs.getString("who"));
                            }
                            rs.close();
                            
                            if (offlinePayAdd!=0 || offlinePaySub!=0) {
                                
                                stmt.executeUpdate( "DELETE FROM `moneyOffline` WHERE `name` LIKE '"+nik+"'" );
                                
                                p.sendMessage( (offlinePayAdd!=0 ? "§fВам поступили оффлайн-платежи на §a"+offlinePayAdd+" §fлони" : "") +
                                        (offlinePayAdd!=0 && offlinePaySub!=0 ? "§f, и оффлайн-счета на §4"+offlinePaySub+"§f лони" : "") +
                                        (offlinePayAdd==0 && offlinePaySub!=0 ? "§fВам доставлены оффлайн-счета на §4"+offlinePaySub+"§f лони" : "") +
                                        "."
                                        );
                            }
                            
                            
                            
                        
                    } catch (SQLException ex) {
                        
                        Ostrov.log_err("loadLocalData error  "+nik+" -> "+ex.getMessage());
                        
                    } finally {
                        try{
                            if (rs!=null && !rs.isClosed()) rs.close();
                            if (stmt!=null) stmt.close();
                        } catch (SQLException ex) {
                            Ostrov.log_err("loadLocalData close error - "+ex.getMessage());
                        }
                    }
                
        }, 1);   

    }
    
    
    private void applyLocalSettings(final Player p, final boolean fly, final int flyspeed, final int walkspeed, final int pweather, final boolean rtime, final int ptime) {
       // final Player p = getPlayer();
        if (p==null || !p.isOnline()) return;
        
        MysqlDataLoaded event = new MysqlDataLoaded ( p );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        
        if (!pvp_allow) pvpOff();
        
            if (p.getGameMode()==GameMode.SURVIVAL || p.getGameMode()==GameMode.ADVENTURE ) {
                if ( CMD.fly_command && p.hasPermission("ostrov.fly") && fly ) {
                    p.setAllowFlight(true); 
                    p.setFallDistance(0);
                    //p.setFlying(true); 
                } else {
                    //if (p.isFlying()) 
                        p.setFlying(false);
                    //if (p.getAllowFlight()) 
                        p.setAllowFlight(false);
                }
            }
            if (  CMD.fly_command && CMD.speed_command && p.hasPermission("ostrov.flyspeed") && flyspeed >0 ) {
                p.setFlySpeed((float)flyspeed/10);
            } else {
                p.setFlySpeed(0.1F);
            }
            
           if (  CMD.speed_command && (p.hasPermission("ostrov.walkspeed")) && walkspeed >0 ) {
                p.setWalkSpeed((float)walkspeed/10);
            } else {
               p.setWalkSpeed(0.2F);
           }

           if (  CMD.pweather_command && p.hasPermission("ostrov.pweather")  ) {
               switch (pweather) {
                   case 0:
                       p.setPlayerWeather(WeatherType.CLEAR);
                       break;
                   case 1:
                       p.setPlayerWeather(WeatherType.DOWNFALL);
                       break;
                   default:
                       p.resetPlayerWeather();
                       break;
               }
            } else p.resetPlayerWeather();

            if ( CMD.ptime_command && p.hasPermission("ostrov.ptime") && ptime > 1) {
                p.setPlayerTime(ptime*1000, rtime);
            } else p.resetPlayerTime();

        
        
    }
    
    
    
    










    public void saveLocalData ( final int pweather, final boolean allowFlight, final int flyspeed, final int walkspeed, final boolean playerTimeRelative, final int playerTimeOffset ) {           
        
            String build="";
            for (String i : world_positions.keySet()) {
                //build = build + i + "<>" + LocationUtil.StringFromLoc(world_positions.get(i)) + "<:>";
                build = build + LocationUtil.StringFromLoc(world_positions.get(i)) + "<:>";
            }
            final String world_pos = build;

            build="";
            for (String i : kits_use_timestamp.keySet()) {
                build = build + i + "<>" + kits_use_timestamp.get(i) + "<:>";
            }
            final String kits_ = build;
            //build="";

    
        new BukkitRunnable(){ 
          @Override
            public void run() {
                PreparedStatement pst = null;
            
                try { 
                        pst = MysqlLocal.GetConnection().prepareStatement("INSERT INTO `data` (name, world_pos, fly, flyspeed, walkspeed, pvp, pweather, rtime, ptime, `bplace`, `bbreak`, `mobkill`, `monsterkill`, `pkill`, `dead`, `kits` ) VALUES "
                                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE "
                                + "name=VALUES(name), "
                                + "world_pos=VALUES(world_pos), "
                                + "fly=VALUES(fly), "
                                + "flyspeed=VALUES(flyspeed), "
                                + "walkspeed=VALUES(walkspeed), "
                                + "pvp=VALUES(pvp), "
                                + "pweather=VALUES(pweather), "
                                + "rtime=VALUES(rtime), "
                                + "ptime=VALUES(ptime), "
                                + "bplace=VALUES(bplace), "
                                + "bbreak=VALUES(bbreak), "
                                + "mobkill=VALUES(mobkill), "
                                + "monsterkill=VALUES(monsterkill), "
                                + "pkill=VALUES(pkill), "
                                + "dead=VALUES(dead), "
                                + "kits=VALUES(kits) ");
                         
                                pst.setString(1, nik);
                                pst.setString(2, world_pos);
                                pst.setBoolean(3, allowFlight);
                                pst.setInt(4, flyspeed);
                                pst.setInt(5, walkspeed);
                                pst.setBoolean(6, pvp_allow);
                                pst.setInt(7, pweather);
                                pst.setBoolean(8, playerTimeRelative);
                                pst.setInt(9, playerTimeOffset);
                                pst.setInt(10, bplace);
                                pst.setInt(11, bbreak);
                                pst.setInt(12, mobkill);
                                pst.setInt(13, monsterkill);
                                pst.setInt(14, pkill);
                                pst.setInt(15, dead);
                                pst.setString(16, kits_);

                                pst.executeUpdate();
                                
                        Ostrov.log_ok("§2данные островитянина "+nik+" сохранены!");

                    } catch (SQLException e) { 
                        Ostrov.log_err("saveLocalData error "+nik+" -> "+e.getMessage());
                    } finally {
//System.out.println("!!! .run(oplayers.remove(name)) nik="+nik);  
                        try{
                            if (pst!=null) pst.close();
                        } catch (SQLException ex) {
                            Ostrov.log_err("saveLocalData close error - "+ex.getMessage());
                        }
                        PM.remove(nik);
                    }
                   
                   
                
        }}.runTaskAsynchronously( Ostrov.instance );   
    
    }    

    public boolean hasGroup(final String group_name) {
        if (groups.contains(group_name)) return true;
        return groups.stream().anyMatch( (gr) -> (OstrovDB.groups.containsKey(gr) && OstrovDB.groups.get(gr).inheritance.contains(group_name)) );
    }

    public Collection<String> getGroups() {
        return groups;
    }

    public boolean hasAnyGroup() {
        return !getDataString(Data.USER_GROUPS).isEmpty();
    }

    public void teleportEvent(final String target_name) {
        final Player target=Bukkit.getPlayer(target_name);
//System.out.println("333 who="+who+" target="+target);
            if(target==null || !target.isOnline()) {
                getPlayer().sendMessage("§cТелепорт не удалось завершить - "+target_name+" не найден!");
                //return;
            } else {
                FriendTeleportEvent event=new FriendTeleportEvent(getPlayer(), target);
                Bukkit.getPluginManager().callEvent(event);
                if(event.Is_canceled()) {
                    getPlayer().sendMessage("§cТелепорт не удалось завершить: "+event.cause);
                    target.sendMessage("§cТелепорт не удалось завершить: "+event.cause);
                } else {
                    getPlayer().teleport(target);
                    getPlayer().sendMessage("§6Вы телепортировались к "+target.getName());
                    target.sendMessage("§6К вам телепортировался "+nik);
                }
            }
    }
    
    
 
    
    
    
    
    
    
    
    
    public void pvpBattleModeBegin(final int battle_time) { //эвент вызывается в Pvp.Проверка_режима_пвп()
//System.out.println("pvpBattleModeBegin "+battle_time);
        //if (pvp_time==0) ApiOstrov.sendActionBar(nik, "§cРежим боя "+battle_time+" сек.!");
        if (pvp_time==0) {
            ApiOstrov.sendActionBar(nik, "§cРежим боя "+battle_time+" сек.!");
            fly_speed=getPlayer().getFlySpeed();
            walk_speed=getPlayer().getWalkSpeed();
            allow_fly = getPlayer().getAllowFlight();
            in_fly = getPlayer().isFlying();
            if (getPlayer().getAllowFlight() && getPlayer().isFlying()) {
                    getPlayer().setFlying(false);
                    getPlayer().setAllowFlight(false);
            }
            getPlayer().setFlySpeed(0.1F);
            getPlayer().setWalkSpeed(0.2F);
        } 
        pvp_time=battle_time;
        
        if (Pvp.display_pvp_tag) {
            //score.removeBelow(); //NullPointerException если scoreboard выключен!!
            //score.showBelow("§4"+nik, 0);//NullPointerException если scoreboard выключен!!
            //ChatMsgUtil.sendNameTag(nik, "§4", "");       //в бою
            //nametag.sendNameTag("§4", "");
            //getPlayer().setPlayerListName("§4⚔");
            //score.setPrefix("§4⚔ ");
            if (PM.nameTagManager!=null) PM.nameTagManager.setNametag(nik, "§4⚔ ", "");
            //if (PM.boardManager!=null) PM.boardManager.setPrefix(getPlayer(), "§4⚔ ");
            tab_list_name_color = "§4⚔ ";
        }
        
    }

    public void pvpBattleModeEnd() {
        //if (pvp_time==0) return;
        final Player p = getPlayer();
        if (p!=null) {
            p.setFlySpeed(fly_speed);
            p.setWalkSpeed(walk_speed);
            p.setAllowFlight(allow_fly);
            p.setFlying(in_fly);
        }
        //pvp_time=0;
        if (Pvp.display_pvp_tag) {
            //score.removeBelow();//NullPointerException если scoreboard выключен!!
            //NmsUtils.sendNameTag(nik, "§f", ""); //нейтральный
            //nametag.sendNameTag("§f", "");
            //getPlayer().setPlayerListName("§f"+nik);
//System.out.println("pvpBattleModeEnd");
            //score.setPrefix("");
            if (PM.nameTagManager!=null) PM.nameTagManager.setNametag(nik, "", "");
            //if (PM.boardManager!=null) PM.boardManager.setPrefix(getPlayer(), "");
            tab_list_name_color = "";
        }
        //if (Bukkit.getPlayer(nik)!=null) Bukkit.getPluginManager().callEvent(new BattleModeEndEvent ( Bukkit.getPlayer(nik) ) );
    }

    public void pvpOff () {
        pvp_allow=false;
        if (Pvp.display_pvp_tag) {
            
            //score.removeBelow();//NullPointerException если scoreboard выключен!!
            //score.showBelow("§2☮", 0);//NullPointerException если scoreboard выключен!!
            //ChatMsgUtil.sendNameTag(nik, "§2", "");       //пвп запрещено
            //nametag.sendNameTag("§2", "");
            //getPlayer().setPlayerListName("§2"+nik);
//System.out.println("pvpOff");
            //score.setPrefix("§2☮ ");
            if (PM.nameTagManager!=null) PM.nameTagManager.setNametag(nik, "§2☮ ", "");
            //if (PM.boardManager!=null) PM.boardManager.setPrefix(getPlayer(), "§2☮ ");
            tab_list_name_color = "§2☮ ";
        } 
    }
    public void pvpOn () {
        pvp_allow=true;
        if (Pvp.display_pvp_tag) {
            //score.removeBelow();//NullPointerException если scoreboard выключен!!
            //score.showBelow("§7", 0);//NullPointerException если scoreboard выключен!!
            
            //score.removeBelow();//NullPointerException если scoreboard выключен!!
            //score.showBelow("§2☮", 0);//NullPointerException если scoreboard выключен!!
            //ChatMsgUtil.sendNameTag(nik, "§2", "");       //пвп запрещено
            //nametag.sendNameTag("§7", "");
            //getPlayer().setPlayerListName("§2"+nik);
//System.out.println("pvpOn");
            //score.setPrefix("");
            if (PM.nameTagManager!=null) PM.nameTagManager.setNametag(nik, "", "");
            //if (PM.boardManager!=null) PM.boardManager.setPrefix(getPlayer(), "");
            tab_list_name_color = "";
        } 
    }

    
    
    
    
    
    
    
    
    
    

    
  /*  public int getIntStat(final E_Stat e_stat) {
        int value=0;
        try {
            
            if (stat.containsKey(e_stat)) {
                value = Integer.valueOf(stat.get(e_stat)); //берём записанное при входе
            } else {
                value=Integer.valueOf(e_stat.def_value);
            }
            
        } catch (NumberFormatException | NullPointerException | ConcurrentModificationException ex) {
            Ostrov.log_err("Bplayer getIntStat error, e_data="+e_stat.toString());
        }
        return value;
    }*/





    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //если на входе ostrov.home  и есть перм ostrov.home.4 вернёт 4
    //добавить учитывая сервер??
    public int getBigestPermValue(final String perm) {
        if( perm==null || perm.isEmpty() || !perm.contains(".") ) return 0;
        
        int final_res=0;
        int current_res;

     /* for (PermissionAttachmentInfo  ai : getPlayer().getEffectivePermissions()) {
                if (ai.getPermission().contains(".") && ai.getPermission().startsWith(perm) && Ostrov.isInteger(ai.getPermission().replaceFirst(perm+".","")) ) {
//System.out.println("--getBigestPermValue() perm="+ai.getPermission());
                    current_res=Integer.valueOf(ai.getPermission().replaceFirst(perm+".",""));
                    if (current_res>final_res) final_res=current_res;
                } 
        }*/
        for (String find : user_perms ) {
                if (find.contains(".") && find.startsWith(perm) && Ostrov.isInteger(find.replaceFirst(perm+".","")) ) {
//System.out.println("--getBigestPermValue() perm="+ai.getPermission());
                    current_res=Integer.valueOf(find.replaceFirst(perm+".",""));
                    if (current_res>final_res) final_res=current_res;
                } 
        }
//System.out.println("---getBigestPermValue() result="+final_res);
        return final_res;
    }

    public boolean hasPermissions(final String worldName, String perm) { //при первом поиске worldName должен игнорироваться. Если мир указан, то право только для этого мира
//System.out.println("---Oplayer hasPermissions world=>"+worldName+"< perm="+perm+" ? user_perms="+user_perms);
        
        //if (worldName.isEmpty()) { //так нельзя! дефолтные права запрашиваются с миром, и если есть без мира должно давать true
            if( user_perms.contains(perm) || getPlayer().hasPermission(perm) ) return true;
            if ( findMultiPermissions(perm) ) return true;
        //} else {
            perm = perm.replaceFirst(worldName+".", "");
            for (String p:user_perms) {
                if (p.startsWith(worldName+".")) {
                    if( user_perms.contains(perm) ) return true;
                    return findMultiPermissions(perm);               
                }
            }
            return false;
        //}
         
     }

    private boolean findMultiPermissions(String perm) {
        boolean has  = false;
        int dotPos;
        for (int q=1; q<perm.length(); q++ ) {
            dotPos  = perm.lastIndexOf(".");                               //проверяем, заменяя последнее слово на *
            if (dotPos > -1) {
    //System.out.println("2222 "+perm+"("+p.hasPermission(perm)+") -> "+perm.substring(0, dotPos)+".* ("+(p.hasPermission(perm.substring(0, dotPos)+".*")+")") );    
                perm = perm.substring(0, dotPos);
                    if ( user_perms.contains(perm+".*") ) {
                        has = true;
                        break;
                    }
            } else break;
        }    
        return has;

    }


    
}
