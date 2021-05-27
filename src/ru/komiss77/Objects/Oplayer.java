package ru.komiss77.Objects;

import java.lang.ref.WeakReference;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
import ru.komiss77.Events.BungeeStatRecieved;
import ru.komiss77.Events.FriendTeleportEvent;
import ru.komiss77.Events.GroupChangeEvent;
import ru.komiss77.Events.MysqlDataLoaded;
import ru.komiss77.Events.PartyUpdateEvent;
import ru.komiss77.Listener.SpigotChanellMsg;
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





public class Oplayer {
  
    public String nik;
    private final WeakReference<Player> player_link;    
    
    private final Map<Data,String>bungeeData=new ConcurrentHashMap<>();
    private final Map<Integer,String>stat=new ConcurrentHashMap<>();
    private final CaseInsensitiveMap <Location> homes=new CaseInsensitiveMap<>();
    private final Map <String, Location> world_positions=new ConcurrentHashMap<>();
    private final CaseInsensitiveMap <Integer> kits_use_timestamp=new CaseInsensitiveMap<>();
    public TreeSet <Integer> achiv = new TreeSet();
    public CaseInsensitiveLinkedTreeSet groups = new CaseInsensitiveLinkedTreeSet();
    public CaseInsensitiveLinkedTreeSet user_perms = new CaseInsensitiveLinkedTreeSet();
    
    private final HashMap<String,String> party_members = new HashMap<>();
    public PermissionAttachment permissionAttachmen=null;
    public Inventory settings,profile;
    public E_Prof e_profile=E_Prof.ПОМОЩЬ;
    Location last_death=Bukkit.getWorlds().get(0).getSpawnLocation();
    
    public CustomScore score;
    //private PlayerBoard board;
                                                                                                //  для режима пвп          для модеров
    public String party_leader="",chat_group=" ---- ",aac_last_pos="",tab_list_name_prefix="§7",tab_list_name_color="§7", tab_list_name_siffix="";
    
    private float fly_speed=0.1F,walk_speed=0.1F;
    private final int login_time = Timer.currentTimeSec();
    public int pvp_time, no_damage, bplace, bbreak, mobkill, monsterkill, pkill, dead, aac_violations,bow_teleport_cooldown=4;     
    
    public boolean mysqldata_loaded=false,allow_fly=false,in_fly=false,resourcepack_locked=true,pvp_allow=true;
       
    public boolean isStaff;
    
    
    
    
    public Oplayer (final Player p) {
        nik=p.getName();
        player_link=new WeakReference<>(p);
        profile=Bukkit.createInventory( p, 54,  ItemUtils.profile_master_inv_name );
        profile.setContents(ItemUtils.profile_master.getContents());
        
        //if (PM.score)score=new CustomScore(p);
        score=new CustomScore(p);
        //if (PM.boardManager!=null) {
        //    PM.boardManager.addPlayer(p);
        //    board = PM.boardManager.getBoard(p);
        //}
        //nametag = new NameTag(nik);
        
        if (CMD.no_damage_on_tp >0) {
            setNoDamage(CMD.no_damage_on_tp, true);
        }
    }    

    public void setNoDamage(final int seconds, final boolean actionBar) {
        no_damage+=seconds;
        if (actionBar) ApiOstrov.sendActionBar(player_link.get(), "§aВам дарована неуязвимость на "+no_damage+" сек!");
    }
    
    
    public Map<Data, String> getBungeeData() {
        return bungeeData;
    }

    public void onExit() {
        
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
    


    
    
    private void updScore() {
        if (!PM.ostrovStatScore) return;
        //if (board==null) return;
        score.getSideBar().setTitle("§7Общий онлайн: §f§l"+SM.bungee_online);//"§a-----------------"
        score.getSideBar().updateLine(15, "§a-----------------");
        score.getSideBar().updateLine(14, " уровень : "+getBungeeData(Data.УРОВЕНЬ));
        score.getSideBar().updateLine(13, " опыт : "+getBungeeData(Data.ОПЫТ));
        score.getSideBar().updateLine(12, " репутация: "+(getBungeeIntData(Data.РЕПУТАЦИЯ)>=0?"§2":"§4")+getBungeeData(Data.РЕПУТАЦИЯ));
        score.getSideBar().updateLine(11, " карма: "+(getBungeeIntData(Data.КАРМА)>=0?"§2":"§4")+getBungeeIntData(Data.КАРМА));
        score.getSideBar().updateLine(10, " лони: "+getBungeeData(Data.MONEY));
        score.getSideBar().updateLine(9, " рил: "+getBungeeData(Data.MONEY_REAL));
        score.getSideBar().updateLine(1, "§a-----------------");
        /*
        board.setTitle("§7Общий онлайн: §f§l"+SM.bungee_online);//"§a-----------------"
        board.updateLine(15, "§a-----------------");
        board.updateLine(14, " уровень : "+getBungeeData(Data.УРОВЕНЬ));
        board.updateLine(13, " опыт : "+getBungeeData(Data.ОПЫТ));
        board.updateLine(12, " репутация: "+(getBungeeIntData(Data.РЕПУТАЦИЯ)>=0?"§2":"§4")+getBungeeData(Data.РЕПУТАЦИЯ));
        board.updateLine(11, " карма: "+(getBungeeIntData(Data.КАРМА)>=0?"§2":"§4")+getBungeeIntData(Data.КАРМА));
        board.updateLine(10, " лони: "+getBungeeData(Data.MONEY));
        board.updateLine(9, " рил: "+getBungeeData(Data.MONEY_REAL));
        board.updateLine(1, "§a-----------------");
        */
    }
    


    
    public void bungeeDataInject(final String raw) {
        try {
            bungeeData.put(Data.NAME, nik);
            String[]split;
            for (String raw_:raw.split("<:>")) {
                split=raw_.split("<>");
                if (split.length==2 && Ostrov.isInteger(split[0]) && Data.exist(Integer.valueOf(split[0])) ) {
                    bungeeData.put(Data.byTag(Integer.valueOf(split[0])), split[1]);
                }
            }
            if (bungeeData.containsKey(Data.PARTY_MEBRERS) && !bungeeData.get(Data.PARTY_MEBRERS).isEmpty()) { //пати в виде списка, лидер - первый
//System.out.println("bungeeDataInject() PARTY_MEBRERS = "+bungeeData.get(Data.PARTY_MEBRERS));
                onPartyRecieved(bungeeData.get(Data.PARTY_MEBRERS), false);
            }
            calculatePerms(false); 
            StatManager.calculateReputationBase(this);
            
            if (bungeeData.containsKey(Data.ДОСТИЖЕНИЯ) && !bungeeData.get(Data.ДОСТИЖЕНИЯ).isEmpty()) {
                for (String i:getBungeeData(Data.ДОСТИЖЕНИЯ).split(",")) {
                    try {
                        achiv.add(Integer.parseInt(i));
//System.out.println("-bungeeDataInject() achiv="+achiv);                    
                    } catch (NumberFormatException ex) {
                        Ostrov.log_err("обработка ачивок-не цифровое значение : "+i);
                    }
                }
            }
            
            //Bukkit.getPluginManager().callEvent(new BungeeDataRecieved ( getPlayer(), getBungeeIntData(Data.MONEY) ) );
        
        } catch (NumberFormatException | IllegalStateException | ArrayIndexOutOfBoundsException | NullPointerException ex) {
            Ostrov.log_err("bungeeDataInject "+raw+" : "+ex.getMessage());
            getPlayer().sendMessage("§cОШИБКА в получении данных, сообщите Администрации! "+ex.getMessage());
        } finally {
            updScore();
            Bukkit.getPluginManager().callEvent(new BungeeDataRecieved ( getPlayer(), getBungeeIntData(Data.MONEY) ) );
        }
        
//System.out.println("-Данные с банжи получены! data="+bungeeData); 
    }
    
    public void onPartyRecieved(final String raw, final boolean callEvent) { //прилетает при входе, нажатии в меню и обновлении состава на банжи из пати-плагина
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
        Bukkit.getPluginManager().callEvent(new PartyUpdateEvent(getPlayer(), party_leader, getPartyMembers()));
    }

    public void bungeeStatInject(final String raw_stat) {
        String[]split;
        for (String st_:raw_stat.split("<>")) {
            split=st_.split(":");
            if (split.length==2 && Ostrov.isInteger(split[0]) ) {
                stat.put(Integer.valueOf(split[0]), split[1]);
            }
        }
        Bukkit.getPluginManager().callEvent(new BungeeStatRecieved (this) );
        
         new BukkitRunnable() {
            @Override
            public void run() {
                setData(Data.WANT_ARENA_JOIN, ""); //сбрасываем после эвента, а то каждый раз посылает на аренуAM.addPlayer(e.getPlayer(), e.getOplayer().getBungeeData(Data.WANT_ARENA_JOIN));
            }
        }.runTaskLater(Ostrov.instance, 40);//такая задержка нужна т.к. переход на арену делается с задержкой
        
//System.out.println("-Статистика с банжи получена! stat="+stat); 
    }

    
    public void calculatePerms(final boolean notify){
        isStaff = false;
        
        final Player p = getPlayer();
        try {
            groups.clear();
            user_perms.clear();
            chat_group=" ---- ";
//System.out.println("-calculatePerms notify="+notify); 
            
            //for (PermissionAttachmentInfo  ai : getPlayer().getEffectivePermissions()) {  //делать до удаления permissionAttachmen!
            for (String perm : OstrovDB.default_permissions) {  //закидываем дефолтные из файлика permissions.yml
//System.out.println("+"+ai.getPermission());        
                user_perms.add(perm);
            }
            
//System.out.println("--calculatePerms 2");        
//дефолтные слетают. сделать нах файлик в острове!
            //for (PermissionAttachmentInfo  ai : getPlayer().getEffectivePermissions()) {  //закидываем дефолтные из файлика permissions.yml

            if ( !getBungeeData(Data.USER_GROUPS).isEmpty() ) {                       //если у игрока есть группы
                chat_group="";
//System.out.println("--calculatePerms");        
                    for (String group_name : getBungeeData(Data.USER_GROUPS).split(",")) {                   //добавляем группы игроку
//System.out.println("--calculatePerms group_name="+group_name);        
                        if (OstrovDB.groups.containsKey(group_name)) {   
                            groups.add(group_name);
                            chat_group=chat_group+", "+OstrovDB.groups.get(group_name).chat_name;
                            //if (SM.this_server_name.length()!=4) { //на играх не ставим!
                                if (OstrovDB.groups.get(group_name).isStaff()) {
                                    tab_list_name_siffix = "§7(§e"+OstrovDB.groups.get(group_name).chat_name+"§7)";
                                    isStaff = true;
                                } else {
                                    tab_list_name_prefix = "§6✪ §f";
                                    //tab_list_name_color = "§f";
                                }
                            //}
                        } else {
                            if (OstrovDB.useOstrovData) Ostrov.log_err("У игрока "+nik+" есть группа "+group_name+", но её нет в базе групп!" );
                        }
                    }
                    chat_group=chat_group.replaceFirst(", ", "");
            }
           

            //for (String group_name : getBungeeData(Data.USER_GROUPS).split(",")) {                   //добавляем права групп игроку
            if (!groups.isEmpty()) {
                for (String group_name : groups) {                   //добавляем права групп игроку
                    //OstrovDB.groups.get(group_name).permissions.stream().forEach((perm) -> { //в группах права уже с учётом наследования!
                    for (String perm : OstrovDB.groups.get(group_name).permissions) { //в группах права уже с учётом наследования!
//System.out.println("----setPermission "+perm);   
                        //permissionAttachmen.setPermission(perm, true);
                        user_perms.add(perm);
                    }
                }
            }

//System.out.println("");
//System.out.println(" +++++ Группы игрока: " + groups);
//System.out.println("");
//System.out.println(" +++++ Права игрока: " + user_perms);
//System.out.println("");

//System.out.println("--allservers="+SM.allBungeeServersName);        

            if ( !getBungeeData(Data.USER_PERMS).isEmpty() ) {                       //если у игрока есть права
//System.out.println("--calculatePerms getBungeeData(Data.USER_PERMS)");   
                String split;
                    for (String perm : getBungeeData(Data.USER_PERMS).split(",")) {                   //добавляем группы игроку
                        
                        //perm=perm;  //отделить сервер
                        
                        if (perm.startsWith("allservers.")) {
                            perm = perm.replaceFirst("allservers.", "");
                            user_perms.add(perm);
//System.out.println("++личное право="+perm);        
                        } else if (perm.startsWith(SM.this_server_name+".")){
                            perm = perm.replaceFirst(SM.this_server_name+".", "");
                            user_perms.add(perm);
//System.out.println("++личное право="+perm);        
                        } else {
//System.out.println("1 perm="+perm);
//System.out.println("2 split="+perm.split("."));
//System.out.println("3 "+(perm.split("\\."))[0]);
                            split=(perm.split("\\."))[0];       //желательно проверять- если начинается с имени другого сервера, то пропускать. Но это надо
                            if (!SM.allBungeeServersName.contains(split)) { //вытаскивать данные из bungee_servers
                                //permissionAttachmen.setPermission(perm, true);
                                user_perms.add(perm);   //пока расчёт на то, что с другим сервером в начале право не сработает.
//System.out.println("++личное право="+perm);        
                            }
                        }
       
//System.out.println("++личное право="+perm);        
                    }
            }
            
            if (permissionAttachmen != null) getPlayer().removeAttachment(permissionAttachmen); //permissionAttachmen пришлось оставить, без него не работает DeluxeChat!!
            permissionAttachmen = p.addAttachment(Ostrov.instance);
            
            
            for (String perm : user_perms) {  //закидываем собранные пермы в атачмент
//System.out.println("+"+ai.getPermission());     
                permissionAttachmen.setPermission(perm, true);
            }
            
            getPlayer().recalculatePermissions();
            
            
//System.out.println("");
            
        } catch (Exception ex) {
            Ostrov.log_err("Ошибка calculatePermissions "+nik+" : "+ex.getMessage());
            p.sendMessage(Ostrov.prefix+" §c Ошибка calculatePermissions, сообщите администрации! : "+ex.getMessage());
        }
        
        Bukkit.getPluginManager().callEvent(new GroupChangeEvent ( getPlayer(), groups.treeSet ) );

        if (notify) p.sendMessage(Ostrov.prefix+"Ваши группы обновились: §e"+chat_group);

            


    }
    












    
    
    
    
    public void updateDataFromBungee(final Data e_data, final String value) {
//System.out.println("-updateDataFromBungee e_data="+e_data.toString()+" value="+value);
        final boolean change = !bungeeData.containsKey(e_data) || (bungeeData.containsKey(e_data) && !bungeeData.get(e_data).equals(value));
        bungeeData.put(e_data, value);
        
//System.out.println("-updateDataFromBungee "+e_data.toString()+" "+profile.getViewers());
        if (change) {
            switch (e_data) {
                case USER_GROUPS: 
                    calculatePerms(true); 
                    StatManager.calculateReputationBase(this);
                    break;
                case USER_PERMS: 
                    calculatePerms(false); 
                    break;
                case ИМЯ_ФАМИЛИЯ: break;
            }
        }
    }
    
    public boolean setData(final Data e_data, final String value) {  //отправляем на банжи, и обнов.локально
        if ( SpigotChanellMsg.sendMessage(getPlayer(), Action.OSTROV_SET_BUNGEE_DATA, e_data.tag+"<>"+value) ) {
            bungeeData.put(e_data, value);
            return true;
        } else {
            getPlayer().sendMessage(Ostrov.prefix+"§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            Ostrov.log_err("§cОшибка синхронизации данных! e_data="+e_data.toString()+" value="+value);
            return false;
        }
    }



    public String getBungeeData(final Data e_data) {
        if (bungeeData.containsKey(e_data)) {
             return e_data.is_integer ?  String.valueOf(getBungeeIntData(e_data)) : bungeeData.get(e_data);
        } else {
             return e_data.def_value;
        }
    }

    public int getBungeeIntData(final Data e_data) {
        int value=0;
        try {
            
            if (bungeeData.containsKey(e_data)) {
                value = Integer.valueOf(bungeeData.get(e_data)); //берём записанное при входе
            } else {
                value=Integer.valueOf(e_data.def_value);
            }
            
            switch (e_data) {
                case DAY_PLAY_TIME: 
                    value+=Timer.currentTimeSec()-login_time;
                    break;
                case PLAY_TIME: 
                    value+=((int)(Timer.currentTimeSec()/60) - (int)(login_time/60));
                    break;
                //case РЕПУТАЦИЯ: 
//System.out.println("get РЕПУТАЦИЯ get="+value+"  base = "+getBungeeIntData(Data.РЕПУТАЦИЯ_БАЗА)+" calc="+getBungeeIntData(Data.РЕПУТАЦИЯ_РАСЧЁТ) );                    
                    //value=getBungeeIntData(Data.РЕПУТАЦИЯ_БАЗА)+getBungeeIntData(Data.РЕПУТАЦИЯ_РАСЧЁТ);
                    //break;
            }
            
        } catch (NumberFormatException | NullPointerException | ConcurrentModificationException ex) {
            Ostrov.log_err("Bplayer getBungeeIntData error, e_data="+e_data.toString()+" value="+bungeeData.get(e_data)+ex.getMessage());
        }
        return value;
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









    public void Tick_every_second(final int seconds) {
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
        
        if (bungeeData.isEmpty() && ApiOstrov.currentTimeSec()-login_time > 1) {
            SpigotChanellMsg.sendMessage(getPlayer(), Action.OSTROV_RESEND_PLAYER_RAW_DATA, "");
        }
        if (PM.ostrovStatScore && seconds%10==0) {
            updScore();
        }
        
        if (PM.tablist_header_footer) { //if (SM.this_server_name.length()>4) {
            ApiOstrov.sendTabList(getPlayer(),  "§7Привет, §a"+nik+" §7Вы находитесь: §5"+SM.this_server_bungee_description+"§7 Сейчас: §6"+ApiOstrov.getCurrentHourMin(), "  §fПомощь - §a/help §fСервер - §a/serv §fПрофиль - §a/profile §fМеню - §a/menu");
            getPlayer().setPlayerListName(tab_list_name_prefix+tab_list_name_color+nik+tab_list_name_siffix);
        } 
        //if (SM.this_server_name.length()!=4) {
        //}
//getPlayer().setPlayerListName(nik);
//System.out.println("-passport=="+(passport==null?"null":passport));
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













    
    public int GetPlytime() {
        return getBungeeIntData(Data.PLAY_TIME);
    }
    
    public int GetBalance() {
        return getBungeeIntData(Data.MONEY);
    }

    public void moneyChange( int sum, final String add_from) {
        SpigotChanellMsg.sendMessage(getPlayer(), Action.OSTROV_BUNGEE_MONEY_CHANGE, String.valueOf(sum)+"<>"+add_from);
        //setData(Data.MONEY, )
    }

    public String GetPrefix() {
        return getBungeeData(Data.PREFIX);
    }
    public String GetSuffix() {
        return getBungeeData(Data.SUFFIX);
    }







    public boolean Pvp_is_allow() {
        return pvp_allow;
    }






// ---------------------- Счетчики ---------------------------------------------
public void Addbplace() { if (player_link.get().getGameMode()==GameMode.SURVIVAL) this.bplace++; }
public void Addbbreak() { if (player_link.get().getGameMode()==GameMode.SURVIVAL) this.bbreak++; }
public void Addmobkill() { if (player_link.get().getGameMode()==GameMode.SURVIVAL) this.mobkill++; }
public void Addmonsterkill() { if (player_link.get().getGameMode()==GameMode.SURVIVAL) this.monsterkill++; }
public void Addpkill() { if (player_link.get().getGameMode()==GameMode.SURVIVAL) this.pkill++; }
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
    public long Kit_last_acces(final String kitName) {
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
//System.out.println("getPlayer nik="+nik+" p="+Bukkit.getPlayer(nik)+" link="+player_link.get());
        //return Bukkit.getPlayer(nik);
        return  player_link.get();
    }

    public boolean bungeeDataRecieved() {
        return !bungeeData.isEmpty();
    }

    
    
    
    

    public void loadLocalData() { //вызывается после получения данных с банжи
        if (!MysqlLocal.useLocalData || getPlayer()==null) return;
        
        Ostrov.async( () -> {
                
                Statement stmt = null;
                ResultSet rs = null;
                
                try {  
                        stmt = MysqlLocal.GetConnection().createStatement(); 
                        
                        rs = stmt.executeQuery( "SELECT * FROM `data` WHERE `name` LIKE '"+nik+"' LIMIT 1" );
                           
                            if (rs.next()) {
                                String[] split;
                                
                                if ( rs.getString("homes").length()>10 && rs.getString("homes").contains("<:>")) {
                                    split = rs.getString("homes").split("<:>");
                                    String homeName;
                                        for (String homeAndLocationAsString : split) {
                                            if (homeAndLocationAsString.split("<>").length==5) {
                                                homeName = homeAndLocationAsString.split("<>")[0];
                                                try {
                                                    Location loc = LocationUtil.LocFromString(homeAndLocationAsString.replaceFirst(homeName+"<>", ""));
//System.out.println("homes "+nik+" raw="+homeAndLocationAsString+" home="+homeName+" loc="+loc);
                                                    //if (loc!=null) homes.put(s.split("<>")[0], s.split("<>")[1]);
                                                    if (loc!=null) homes.put(homeName, loc ); //название дома, локация
                                                } catch (Exception ex) {
                                                   Ostrov.log_err("Загрузка точки дома "+homeAndLocationAsString+" для "+nik+":"+ex.getMessage());
                                                }
                                            }
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
                                
                                Ostrov.sync( () ->  applyLocalSettings(fly, flyspeed,  walkspeed, pweather, rtime, ptime), 0);
                                    
                            } else {
                                
                                Ostrov.sync( () -> applyLocalSettings(false, -1,  -1, -1, true, -1), 0);

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
                                ApiOstrov.moneyChange(getPlayer(), rs.getInt("value"), "§5оффлайн платёж §f-> "+rs.getString("who"));
                            }
                            rs.close();
                            
                            if (offlinePayAdd!=0 || offlinePaySub!=0) {
                                
                                stmt.executeUpdate( "DELETE FROM `moneyOffline` WHERE `name` LIKE '"+nik+"'" );
                                
                                getPlayer().sendMessage( (offlinePayAdd!=0 ? "§fВам поступили оффлайн-платежи на §a"+offlinePayAdd+" §fлони" : "") +
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
    
    
    private void applyLocalSettings(final boolean fly, final int flyspeed, final int walkspeed, final int pweather, final boolean rtime, final int ptime) {
        final Player p = getPlayer();
        if (p==null || !p.isOnline()) return;
        
        MysqlDataLoaded event = new MysqlDataLoaded ( getPlayer() );
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
        return !getBungeeData(Data.USER_GROUPS).isEmpty();
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
        getPlayer().setFlySpeed(fly_speed);
        getPlayer().setWalkSpeed(walk_speed);
        getPlayer().setAllowFlight(allow_fly);
        getPlayer().setFlying(in_fly);
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

    
    
    
    
    
    
    
    
    
    
    
    
    public String getStat(final E_Stat e_stat) {
        if (stat.containsKey(e_stat.tag)) return stat.get(e_stat.tag);
        else return e_stat.def_value;
    }
    
    public int getIntStat(final E_Stat e_stat) {
        int value=0;
        try {
            
            if (stat.containsKey(e_stat.tag)) {
                value = Integer.valueOf(stat.get(e_stat.tag)); //берём записанное при входе
            } else {
                value=Integer.valueOf(e_stat.def_value);
            }
            
        } catch (NumberFormatException | NullPointerException | ConcurrentModificationException ex) {
            Ostrov.log_err("Bplayer getIntStat error, e_data="+e_stat.toString());
        }
        return value;
    }


    public void setStat(final E_Stat e_stat, final String value) {
//System.out.println("-setStat e_stat="+e_stat.toString()+" value="+value+" getPlayer()="+getPlayer());
        stat.put(e_stat.tag, value);
        SpigotChanellMsg.sendMessage(getPlayer(), Action.OSTROV_SET_STAT_DATA, e_stat.tag+":"+value);
    }



    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
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
