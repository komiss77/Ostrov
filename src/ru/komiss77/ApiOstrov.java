package ru.komiss77;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.komiss77.Commands.Spy;
import ru.komiss77.Enums.Action;
import ru.komiss77.Enums.Data;
import ru.komiss77.Enums.Table;
import ru.komiss77.Enums.UniversalArenaState;
import ru.komiss77.Events.BsignLocalArenaClick;
import ru.komiss77.Listener.ResourcePacks;
import ru.komiss77.Listener.SpigotChanellMsg;
import ru.komiss77.Managers.MysqlLocal;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.SM;
import ru.komiss77.Managers.StatManager;
import ru.komiss77.Managers.Timer;
import ru.komiss77.Objects.DelayActionBar;
import ru.komiss77.Objects.DelayBossBar;
import ru.komiss77.Objects.DelayTitle;
import ru.komiss77.ProfileMenu.E_Stat;
import ru.komiss77.modules.OstrovDB;
import ru.komiss77.utils.ColorUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TeleportLoc;
import ru.ostrov77.friends.ApiFriends;






public class ApiOstrov {
    
    
    
    //всё по Оплееру
    public static void addIntStat(final Player p, final E_Stat e_stat) {
        try {
            StatManager.addIntStat (p, e_stat);
        } catch (NullPointerException ex) {
            Ostrov.log_err("ApiOstrov addIntStat: "+ex.getMessage());
        }    
    }
    public static void addXP(final Player p, final int ammount) {
        try {
            StatManager.AddXP(PM.getOplayer(p), ammount);
        } catch (NullPointerException ex) {
            Ostrov.log_err("ApiOstrov AddXP: "+ex.getMessage());
        }    
    }
    public static String getStringStat(final Player p, final E_Stat e_stat) {
        return StatManager.getStringStat (p, e_stat);
    }
    public static void setStringStat(final Player p, final E_Stat e_stat, final String new_value) {
        StatManager.setStringStat (p, e_stat, new_value);
    }
    public static String getPlayTime(final Player player){ 
        return IntToTime( PM.getOplayer(player.getName()).getBungeeIntData(Data.PLAY_TIME) );
    }
    public static String getPrefix(final Player player){ 
        return PM.getOplayer(player.getName()).getBungeeData(Data.PREFIX);
    }
    public static String getSuffix(final Player player){ 
         return PM.getOplayer(player.getName()).getBungeeData(Data.SUFFIX);
    }
    /**
     * 
     * @param name ник. Возвращает true если у игрока активен режим боя. Так же, можно использовать BattleModeEvent и BattleModeEndEvent
     * @return 
     */
    public static boolean inBattle (String name)  {
        return PM.inBattle(name);
    }      
        
    public static void giveMenuItem(final Player p) {
        Ostrov.lobby_items.giveItem(p, "pipboy");//ItemUtils.Add_to_inv(p, 8, ItemUtils.pipboy, true, false);
    }
    
    public static boolean hasResourcePack(final Player p) {
        return ResourcePacks.Текстуры_утановлены(p);
    }
    public static boolean hasGroup (final String nik, final String group_name) {
        return PM.exist(nik) && PM.getOplayer(nik).hasGroup(group_name);
    }
    public static String[] getGroups(final String nik) {
        return (String[]) PM.getOplayer(nik).getGroups().toArray(new String[0]);
    }
    public static String getChatGroups(final String nik) {
        return PM.getOplayer(nik).chat_group;
    }
    /**
     * 
     * @param worldName мир
     * @param nik ник
     * @param perm проверка этого права с учётом * (начиная с позиции * выдаётся как true) 
     * @return 
     */
    public static boolean hasPermission(final String worldName, final String nik, String perm) {
        if (!PM.exist(nik) || Bukkit.getPlayer(nik)==null) {
//System.out.println(" -- hasPermission "+nik+" : "+perm+" ? Bukkit.getPlayer(nik)==null");
            return false;
        }
//System.out.println(" -- hasPermission "+nik+" : "+perm+" ? "+PM.getOplayer(nik).hasPermissions(worldName, perm));
        return PM.getOplayer(nik).hasPermissions(worldName, perm);
    }

    
    
    
    
    
    
 
    
    
    
    
    // друзья команды
    public static boolean hasParty(final Player p) {
        return PM.exist(p.getName()) && !PM.getOplayer(p.getName()).getPartyMembers().isEmpty();//Ostrov.api_friends!=null && ApiFriends.hasParty(p);
    }
    public static boolean isInParty(final Player p1, final Player p2) {
        return PM.exist(p1.getName()) && !PM.getOplayer(p1.getName()).getPartyMembers().contains(p2.getName()) ||
                PM.exist(p2.getName()) && !PM.getOplayer(p2.getName()).getPartyMembers().contains(p1.getName());//Ostrov.api_friends!=null && ApiFriends.isInParty(p1,p2);
    }
    public static List<String> getPartyPlayers(final Player p) {
        if (!PM.exist(p.getName())) return new ArrayList<>();
        else return new ArrayList(PM.getOplayer(p.getName()).getPartyMembers());
        //if (Ostrov.api_friends==null) return new ArrayList<>();
        //else return ApiFriends.getPartyPlayers(p);
    }
    public static String getPartyLeader(final Player p) {
        if (!PM.exist(p.getName())) return "";
        else return PM.getOplayer(p.getName()).party_leader;
        //if (Ostrov.api_friends==null || !ApiFriends.hasParty(p)) return "";
        //else return ApiFriends.getPartyLeader(p);
    }
    public static boolean isPartyLeader(final Player p) {
        return PM.exist(p.getName()) && PM.getOplayer(p.getName()).isPartyLeader();
        //if (Ostrov.api_friends==null) return false;
        //else return ApiFriends.isPartyLeader(p);
    }
    public static boolean isFriend(final Player p1,final Player p2) {
        return Ostrov.api_friends!=null && ApiFriends.isFriend(p1,p2);
    }
    
   
    
    
    
    
    
    
    
    
    
    
    
    

    
    
    public static boolean isNewDay() {
        return Ostrov.новый_день;
    }
    
    
    
    
    
    
    

    
    
    
    
    
    
    

    
        
    
    //   каналы коммуникации
    public static boolean sendMessage (final Player p, final Action action, final String raw_data) {
        return SpigotChanellMsg.sendMessage(p, action, raw_data);
    }
    public static boolean sendMessage(final String from, final Action action, final String raw_data) {
        return SpigotChanellMsg.sendMessage(from, action, raw_data);
    }
    /**
     * 
     * @param player игрок
     * @param server название сервера, как в настройках bungeecord
     * @param arena название арены на сервере для вызова ArenaJoinEvent в плагине bsign
     */    
    public static void sendToServer(final Player player, final String server, String arena) {
        if (arena.isEmpty()) arena="any";
        if (server.equalsIgnoreCase(SM.this_server_name)) {
            Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick ( player, arena ) );
        } else {
            sendMessage(player, Action.OSTROV_SEND_TO_ARENA, server+"<:>"+arena);
        }
    }        
    
     public static Connection getLocalConnection() {
        return MysqlLocal.GetConnection();
    }
    
    public static Connection getOstrovConnection() {
        return OstrovDB.GetConnection();
    }
   

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    // телепорт
    public static boolean isLocationSave(final Player p, final Location loc) {
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return true;
        //return ! TeleportLoc.isBlockUnsafe (loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return TeleportLoc.isSafeLocation(loc);
    }
    public static Location findNearestSaveLocation (final Location loc) {
        if (loc==null || loc.getWorld()==null) return Bukkit.getWorlds().get(0).getSpawnLocation();
        //return TeleportLoc.getSafeDestination(loc);
        return TeleportLoc.findNearestSafeLocation(loc, null);
    }
    @Deprecated
    public static boolean teleportSave (final Player p , final Location loc) {
        teleportSave(p, loc, false);
        return true;
    }

    public static void teleportSave(final Player p, final Location loc, final boolean buildSavePlace) {
        if (isLocationSave(p, loc)) {
            if (Bukkit.isPrimaryThread()) {
                 p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
            } else {
                Ostrov.sync( ()->p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND), 0);
            }
//System.out.println("1 loc="+loc);
        } else {
            Location save = ApiOstrov.findNearestSaveLocation(loc);
            if (save != null) {
                if (Bukkit.isPrimaryThread()) {
                    p.teleport(save, PlayerTeleportEvent.TeleportCause.COMMAND);
               } else {
                   Ostrov.sync( ()->p.teleport(loc), 0);
               }
                //p.teleport(save, PlayerTeleportEvent.TeleportCause.COMMAND);
//System.out.println("2 loc="+loc);
            } else if (buildSavePlace) {
//System.out.println("3 loc="+loc2);
                    if (Bukkit.isPrimaryThread()) {
                        loc.getBlock().getRelative(BlockFace.DOWN).setType(Material.GLASS);
                        loc.getBlock().setType(Material.AIR);
                        loc.getBlock().getRelative(BlockFace.UP).setType(Material.AIR);
                        loc.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).setType(Material.GLASS);
//System.out.println("4 loc="+loc2);
                        p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                   } else {
                       Ostrov.sync( ()->{
                            loc.getBlock().getRelative(BlockFace.DOWN).setType(Material.GLASS);
                            loc.getBlock().setType(Material.AIR);
                            loc.getBlock().getRelative(BlockFace.UP).setType(Material.AIR);
                            loc.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).setType(Material.GLASS);
                            p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                       }, 0);
                   }
            } else {
                 p.sendMessage("§4Телепорт не удался - указанная локация опасна для жизни!");
            }
        }    
    }
 
    
    
    
    
    
    
    
        
     
    //   деньги
    /**
     * 
     * @param p только онлайн игроки!
     * @param value изменение, если убавить, то с минусом
     * @param who кто изменяет
     */
    public static void moneyChange ( final Player p, final int value, final String who ) {
        try {
            SpigotChanellMsg.sendMessage(p, Action.OSTROV_BUNGEE_MONEY_CHANGE, String.valueOf(value)+"<>"+who);
        } catch (NullPointerException ex) {
            Ostrov.log_err("ApiOstrov moneyChange "+ex.getMessage());
        }    
    } 
    /**
     * 
     * @param name ник. (в разработке-Если оффлайн, добавится при входе)
     * @param value изменение, если убавить, то с минусом
     * @param who кто изменяет
     */
    public static void moneyChange ( final String name, final int value, final String who ) {
        if (PM.exist(name)) {
            moneyChange(Bukkit.getPlayer(name), value, who);
        } else {//запомнить и дать при входе - оффлайн перевод
            if (getLocalConnection()!=null) {
                MysqlLocal.moneyOffline(name, value, who);
            } else {
                Ostrov.log_err("Оффлайн-перевод для "+name+" на сумму "+value+", но локальная БД отключена!");
            }
        }
    } 
    public static int moneyGetBalance ( final String name ) {
        if (PM.exist(name)) return PM.getOplayer(name).getBungeeIntData(Data.MONEY);
        else return 0;
    }  
    public static String GetBalString ( Player p ) {
        if (PM.exist(p.getName()) ) {
            final int m = PM.getOplayer(p.getName()).getBungeeIntData(Data.MONEY); 
            if ( m<=1000 ) return "Нищеброд";
            else if (m>1000 && m<=10000)return "Бедняк";
            else if (m>10000 && m<=100000)return "Малоимущий";
            else if (m>100000 && m<=1000000)return "В достатке";
            else if (m>1000000 && m<=10000000)return "Хозяин жизни";
            else if (m>10000000 && m<=100000000)return "Богач";
            else return "Олигарх";
        } else return "§cигрок оффлайн";
    }  
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //  Всякие надписи     
    public static void sendTitleDirect(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout ) {
        try {
            sendTitle(p, title, subtitle, fadein, stay, fadeout);
        } catch (NullPointerException ex) {
            Ostrov.log_err("ApiOstrov sendTitleDirect "+ex.getMessage());
        }    
    }
    /**
     * 
     * сообщения сохраняются и выводятся поочерёдно с интервалом 3 сек.
     * fadein=20 stay=40 fadeout=20 
     * @param p игрок. проверять есть ли он на сервере на надо
     * @param title титры
     * @param subtitle субтитры
     */
    public static void sendTitle(final Player p, final String title, final String subtitle) {
        sendTitle(p, title, subtitle, 20, 40, 20);
    }
    /**
     * 
     * титры сохраняются и выводятся поочерёдно с интервалом 3 сек.
     * @param p игрок. проверять есть ли он на сервере на надо
     * @param title титры
     * @param subtitle субтитры
     * @param fadein появление
     * @param stay показ
     * @param fadeout исчезание
     */
    public static void sendTitle(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout ) {
        try {
            if (Timer.delay_titles.containsKey(p.getName()))  Timer.delay_titles.get(p.getName()).AddTitle(p.getName(), title, subtitle, fadein, stay, fadeout);
            else Timer.delay_titles.put(p.getName(), new DelayTitle(p.getName(), title, subtitle, fadein, stay, fadeout));        } catch (NullPointerException ex) {
            Ostrov.log_err("ApiOstrov sendTitle "+ex.getMessage());
        }    
    }
    /**
     * 
     * сообщения сохраняются и выводятся поочерёдно с интервалом 3 сек.
     * @param p игрок. проверять есть ли он на сервере на надо
     * @param text сообщение
     */
    public static void sendActionBar(final Player p, final String text) {
        try {
            //Utils.sendActionBar(p, text);
            if (Timer.delay_actionbars.containsKey(p.getName())) Timer.delay_actionbars.get(p.getName()).AddMsg(text);
            else Timer.delay_actionbars.put(p.getName(), new DelayActionBar(p.getName(),text) );} catch (NullPointerException ex) {
            Ostrov.log_err("ApiOstrov sendActionBar "+ex.getMessage());
        }    
    }
    /**
     * 
     * сообщения сохраняются и выводятся поочерёдно с интервалом 5 сек.
     * @param nik ник игрока. проверять есть ли он на сервере на надо
     * @param text сообщение
     */
    public static void sendActionBar(final String nik, final String text) {
        if (Bukkit.getPlayer(nik)!=null) {
            sendActionBar(Bukkit.getPlayer(nik), text);
        } else Ostrov.log_err("ApiOstrov sendActionBar2 : "+nik+" не найден!");
        //try {
        //    sendActionBar(Bukkit.getPlayer(nik), text);
        //} catch (NullPointerException ex) {
            //Ostrov.log_err("ApiOstrov sendActionBar2 "+ex.getMessage());
        //}    
    }
    /**
     * 
     * сообщения выводятся моментально, перекрывая предыдущие
     * @param p игрок. проверять есть ли он на сервере на надо
     * @param text сообщение
     */
    public static void sendActionBarDirect(final Player p, final String text) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
    }
    /**
     * 
     * сообщения сохраняются и выводятся поочерёдно с интервалом 5 сек.
     * @param p игрок. проверять есть ли он на сервере на надо
     * @param text сообщение
     * @param seconds через сколько сек. удалить
     * @param bar_color если null то BarColor.WHITE
     * @param bar_style если null то BarStyle.SOLID
     * @param show_progress уменьшать шкалу
     */
    public static void sendBossbar (Player p, String text, int seconds, BarColor bar_color, BarStyle bar_style, boolean show_progress) {
        try {
            //Utils.sendBossBar(p, text, seconds, bar_color, bar_style, show_progress);
            if (Timer.delay_bossbars.containsKey(p.getName()))  Timer.delay_bossbars.get(p.getName()).AddBar(p, text, seconds, bar_color, bar_style, show_progress);
            else Timer.delay_bossbars.put(p.getName(), new DelayBossBar(p, text, seconds, bar_color, bar_style, show_progress));        
        } catch (NullPointerException ex) {
            Ostrov.log_err("ApiOstrov sendBossbar "+ex.getMessage());
        }    
    } 
    
    
    
    public static void sendTabList(final Player p, final String header, final String footer) {
        p.setPlayerListHeaderFooter(header, footer);
    }
        
        
    // *****************************************************************************    


    
    
    
    
    
    
    
    
    
    

    //    числа
    public static int randInt(int min, int max) {
        return Ostrov.random.nextInt((max - min) + 1) + min;
    }
    public static boolean randBoolean() {
        return Ostrov.random.nextBoolean();
    }
    
    public static boolean isInteger(final String int_as_string) {
        return Ostrov.isInteger(int_as_string);
    }
    
    public static String IntToTime(int min) { //c днями и нед!
        final int year = min / 3628800;
        min -= year*3628800;
        final int month = min / 302400;
        if (year>0) {
            if ( month==0) {
                return  year+"г. ";
            } else {
                return  year+"г. "+month+" мес. ";
            }
        }
        min -= month*302400;
        final int week = min / 10080;
        if (month>0) {
            if ( week==0) {
                return  month+" мес. ";
            } else {
                return  month+" мес. "+week+"нед. ";
            }
        }
        min -= week*10080;
        final int day = min / 1440;
        if (week>0) {
            if (day==0) {
                return  week+" нед. ";
            } else {
                return  week+"нед. "+ day+"дн. ";
            }
        }
        min -= day*1440;
        final int hour = min / 60;
        if (day>0) {
            if (hour==0) {
                return  day+"дн. ";
            } else { //в масштабах дня минуты не считаем!
                return  day+"дн. "+ ( hour>9 ? String.format("%02d",hour) : hour )+"ч ";
            }
        }
        min -= hour*60;
        if (hour==0) {
            if (min==0) {
                return  "меньше минуты";
            } else {
                return  ( min>9 ? String.format("%02d",min) : min )+"мин.";
            }
        } else {
            if (min==0) {
                return  hour+"ч";
            } else {
                return  hour+"ч "+( min>9 ? String.format("%02d",min) : min )+"мин.";
            }
        }
    }      
    public static String housrToTime(int hours) {
        final int days = hours / 24;
        hours-=days*24;
        return  (days==0 ? "" : days+"дн. ") + (hours==0 ? (days==0?"меньше часа":"") : hours+"ч. ");
    }
 
    public static String dateFromStamp(final long stamp_in_second) {
        return Ostrov.dateFromStamp(stamp_in_second);
        //Date date = new java.util.Date(stamp*1000L); 
        //SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd.MM.yy"); 
        //sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+3"));
         
        //Calendar mydate = Calendar.getInstance(); 
        //mydate.setTimeInMillis(stamp*1000);
        //return mydate.get(Calendar.DAY_OF_MONTH)+"."+mydate.get(Calendar.MONTH)+"."+mydate.get(Calendar.YEAR)+" "+mydate.get(Calendar.HOUR_OF_DAY)+":"+mydate.get(Calendar.MINUTE);
    }       
    public static String getCurrentHourMin() {
        return Ostrov.getCurrentHourMin();
    }


    

    
    
    
    
    
    
    //   строки
    @Deprecated
    public static String listToString(final Collection<String> array) {
        return listToString(array, ",");
    }
    
    public static String listToString(final Iterable array, final String splitter) {
        StringBuilder sb=new StringBuilder();
        array.forEach( (s) -> {
            sb.append(String.valueOf(s)).append(splitter);
            //res=res+splitter+s;
        });
        //res =  res.replaceFirst(splitter, "");
        return sb.toString();
    }
    @Deprecated
    public static String listToString(final List<String> array, final String splitter) {
        StringBuilder sb=new StringBuilder();
        array.forEach( (s) -> {
            sb.append(s).append(splitter);
        });
        return sb.toString();
    }/*
    public static String setToString(final Set array, final String splitter) {
        StringBuilder sb=new StringBuilder();
        array.forEach( (s) -> {
            sb.append(String.valueOf(s)).append(splitter);
            //res=res+splitter+s;
        });
        //res =  res.replaceFirst(splitter, "");
        return sb.toString();
    }*/
    public static String enumSetToString(final EnumSet enumSet) {
        //String allowRole="";
        StringBuilder sb=new StringBuilder();
        for (final Object eNum : enumSet) {
            sb.append(eNum.toString()).append(",");
            //allowRole=allowRole+","+eNum.toString();
        }
        // =  allowRole.replaceFirst(",", "");
        return sb.toString();//allowRole;
    }

    
    
    
 
 
 
 
 
    
    
    //    пол
    public static String genderEnd_Существительное(final String name) {
        if (PM.exist(name)) {
            switch (ChatColor.stripColor(PM.getOplayer(name).getBungeeData(Data.ПОЛ)).toLowerCase()) {
                case "девочка": 
                    return "а";
                case "бесполоe": 
                case "гермафродит": 
                    return "о";
                default:
                    return "";
            }
        } else return "";
    }

    public static boolean isFemale(final String name) {
        return PM.exist(name) && ChatColor.stripColor(PM.getOplayer(name).getBungeeData(Data.ПОЛ)).equalsIgnoreCase("девочка");
    }


    
    
    
    
    
    
    //   locations
    public static String stringFromLoc(final Location loc) {
        return LocationUtil.StringFromLoc(loc);
    }
    public static Location locFromString(final String loc_as_string) {
        return LocationUtil.LocFromString(loc_as_string);
    }

    
    
    
    
    //    color
    public static ChatColor randomColor() {
        return ColorUtils.randomColor();
    }
    public static boolean canChangeColor (Material check) {
        return ColorUtils.canChangeColor(check);
    }

    public static String getItemNameBaseWithOutColor (String source_type) {
        return ColorUtils.getItemNameBaseWithOutColor(source_type);
    }
    
    
    
    
    
    
    
    
    
    
    //    блоки
    @Deprecated
    public static boolean isSign (final Material mat) {
        return Tag.SIGNS.isTagged(mat);
        /*if (mat==null) return false;
        switch (mat) {
            case ACACIA_SIGN:
            case ACACIA_WALL_SIGN:
            case BIRCH_SIGN:
            case BIRCH_WALL_SIGN:
            case OAK_SIGN:
            case OAK_WALL_SIGN:
            case DARK_OAK_SIGN:
            case DARK_OAK_WALL_SIGN:
            case JUNGLE_SIGN:
            case JUNGLE_WALL_SIGN:
            case SPRUCE_SIGN:
            case SPRUCE_WALL_SIGN:
                return true;
                
            default: 
                return false;
        }*/
    }

    public static Block getSignAttachedBlock(final Block b) {
        if (b.getState() instanceof Sign) {
            final Sign sign = (Sign) b.getState();
            if (sign.getBlockData() instanceof WallSign) {
                WallSign signData = (WallSign) sign.getBlockData();
                return b.getRelative(signData.getFacing().getOppositeFace());
            }  
        }
        return b.getRelative(BlockFace.DOWN);
    }      

    public static String getArenasTabbleName() { //для Бсигн, конфликтовали по енум
        return Table.GAMES_ARENAS.table_name;
    }

    public static void sendArenaData(final String arena_name, final String line0, final String line1, final String line2, final String line3, final String string, final int players, final UniversalArenaState state, final boolean mysql, final boolean async) {
        SM.sendArenaData(arena_name, line0, line1, line2, line3, "", players, (state==null ? UniversalArenaState.НЕОПРЕДЕЛЕНО : state), mysql, async);
    }


    @Deprecated
    public static boolean checkString (final String message) { return checkString(message, true, true); }    
    public static boolean checkString (final String message, final boolean allowNumbers, final boolean allowRussian) {
        //replaceAll("[^A-Za-z0-9]",""); replace all the characters except alphanumeric 
      String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
      if (allowNumbers) allowed=allowed+"_0123456789";
      if (allowRussian) allowed=allowed+"АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
      for(int i = 0; i < message.length(); ++i) {
         if(!allowed.contains(String.valueOf(message.charAt(i)))) {
            return false;
         }
      }
      return true;
   }    
        
    @Deprecated
    public static boolean isLocalBuilder(final Player p) {
        return isLocalBuilder(p, false);
    }
    
    public static boolean isLocalBuilder(final CommandSender cs, final boolean message) {
        //if ( cs!=null && (cs.hasPermission("ostrov.builder") || hasGroup(cs.getName(), "builder")) ) {
       // if ( cs!=null && (cs.hasPermission("ostrov.builder") || hasGroup(cs.getName(), "builder")) ) {
            if (cs==null) {
                return false;
            } else if (cs instanceof ConsoleCommandSender) {
                return true;
            } else if (cs instanceof Player) {
                final Player p = (Player) cs;
                if (p.isOp() || p.hasPermission("builder") || hasGroup(p.getName(), "supermoder")) { //p.hasPermission(Bukkit.getServer().getMotd()+".builder") -сервер срезает!!!!
                     //!! фиксить права в CDM case "gm", или не даст перейти в гм1
                    if (p.getGameMode()==GameMode.CREATIVE || p.getGameMode()==GameMode.SPECTATOR) {
                        return true;
                    } else if (message) {
                        final TextComponent msg = new TextComponent("§e*Клик на это сообшение - §aвключить gm1 и режим Строителя" );
                        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Клик - ГМ1")));
                        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gm 1"));
                        p.spigot().sendMessage(msg);
                    }
                }
            }
            //if ( (cs instanceof Player) && ((Player)cs).getGameMode()==GameMode.CREATIVE ) return true;
            //else if (message) {
                //p.sendMessage("§e* Перейдите в режим Креатив для активации режима Строителя");
          //  }
       // }
        return false;
        //return p.hasPermission("ostrov.builder") && p.getGameMode()==GameMode.CREATIVE;
    }
    


    public static int generateId() {
        final String createStamp = String.valueOf(System.currentTimeMillis());
        return Integer.valueOf( createStamp.substring(createStamp.length()-8) );  //15868 94042329
    }

    public static boolean isSpyMode(final Player p) {
        return Spy.isSpy(p.getName());
    }
















}
