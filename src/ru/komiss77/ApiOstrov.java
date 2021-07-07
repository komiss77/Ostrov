package ru.komiss77;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.komiss77.commands.Spy;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.GameState;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.listener.ResourcePacks;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.profile.StatManager;
import ru.komiss77.objects.DelayActionBar;
import ru.komiss77.objects.DelayBossBar;
import ru.komiss77.objects.DelayTitle;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.kits.KitManager;
import ru.komiss77.modules.world.WE;
import ru.komiss77.modules.warp.WarpManager;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.Ostrov.Module;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.utils.ColorUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TeleportLoc;
import ru.ostrov77.friends.ApiFriends;






public class ApiOstrov {
    
    private static final String  pattern_Eng = "[A-Za-z]";
    private static final String  pattern_Eng_Num = "[A-Za-z0-9]";
    private static final String  pattern_Eng_Rus = "[A-Za-zА-Яа-я]";
    private static final String  pattern_Eng_Num_Rus = "[A-Za-z0-9А-Яа-я]";
    //private static final char[]  allowed_Eng = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    //private static final char[] allowed_Num = "_0123456789".toCharArray();
    //private static final char[] allowed_Rus = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя".toCharArray();
    //private static StringBuilder percentbar = new StringBuilder("§a||||||||||||||||||||||||| ");    
    
    public static WorldManager getWorldManager() {
        return Ostrov.worldManager;
    }
    public static WE getWorldEditor() {
        return Ostrov.worldEditor;
    }
    public static GM getGameManager() {
        return Ostrov.gameManager;
    }
    public static MenuItemsManager getMenuItemManager() {
        return Ostrov.menuItems;
    }
    public static KitManager getKitManager() {
        return Ostrov.kitManager;
    }
    public static WarpManager getWarpManager() {
        return (WarpManager) Ostrov.getModule(Module.warps);
    }    
    
    //всё по Оплееру
    public static void addStat(final Player p, final Stat e_stat) {
        addStat(p, e_stat, 1);
    }
    public static void addStat(final Player p, final Stat e_stat, final int ammount) {
        StatManager.addStat(p, e_stat, ammount);
    }
    public static void addExp(final Player p, final int ammount) {
        //StatManager.addExp(PM.getOplayer(p), ammount);
        PM.getOplayer(p).addExp(p, ammount);
    }
    public static int getStat(final Player p, final Stat e_stat) {
        return PM.getOplayer(p.getName()).getStat(e_stat);
    }
    //public static void setStat(final Player p, final E_Stat e_stat, final String new_value) {
    //    StatManager.setStringStat (p, e_stat, new_value);
    //}
    public static String getPlayTime(final Player player){ 
        return secondToTime(PM.getOplayer(player.getName()).getStat(Stat.PLAY_TIME) );
    }
    public static String getPrefix(final Player player){ 
        return PM.getOplayer(player.getName()).getDataString(Data.PREFIX);
    }
    public static String getSuffix(final Player player){ 
         return PM.getOplayer(player.getName()).getDataString(Data.SUFFIX);
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
        getMenuItemManager().giveItem(p, "pipboy");//ItemUtils.Add_to_inv(p, 8, ItemUtils.pipboy, true, false);
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
    
   
    
    
    
    
    
    
    
    
    
    
    
    

    
    /**
     * 
     * @return
     * после рестарта определить, настал новый день 
     * или тот же
     */
    public static boolean isNewDay() {
        return Ostrov.новый_день;
    }
    
    
    
    
    
    
    

    
    
    
    
    
    
    

    
        
    
    //   каналы коммуникации



    //без указания передатчика
    public static boolean sendMessage (final Operation action) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return false;
        final Player p = Bukkit.getOnlinePlayers().stream().findAny().get();
        return SpigotChanellMsg.sendMessage(p, action);
    }
    public static boolean sendMessage (final Operation action, final String senderInfo) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return false;
        final Player p = Bukkit.getOnlinePlayers().stream().findAny().get();
        return SpigotChanellMsg.sendMessage(p, action, senderInfo);
    }
    public static boolean sendMessage (final Operation action, final String senderInfo, final int int1) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return false;
        final Player p = Bukkit.getOnlinePlayers().stream().findAny().get();
        return SpigotChanellMsg.sendMessage(p,action, senderInfo, int1);
    }
    public static boolean sendMessage (final Operation action, final String senderInfo, final String string1) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return false;
        final Player p = Bukkit.getOnlinePlayers().stream().findAny().get();
        return SpigotChanellMsg.sendMessage(p, action, senderInfo, string1);
    }
    public static boolean sendMessage (final Operation action, final String senderInfo, final int int1, final String string1) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return false;
        final Player p = Bukkit.getOnlinePlayers().stream().findAny().get();
        return SpigotChanellMsg.sendMessage(p, action, senderInfo, int1, string1);
    }
    public static boolean sendMessage (final Operation action, final String senderInfo, final int int1, final int int2, final String string1, final String s2) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return false;
        final Player p = Bukkit.getOnlinePlayers().stream().findAny().get();
        return SpigotChanellMsg.sendMessage(p, action, senderInfo, int1, int2, s2, s2);
    }
    public static boolean sendMessage (final Operation action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, final String s2, final String s3) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return false;
        final Player p = Bukkit.getOnlinePlayers().stream().findAny().get();
        return SpigotChanellMsg.sendMessage(p, action, senderInfo, int1, int2, int3, s1, s2, s3);
    }
    public static boolean sendMessage (final Operation action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, final String s2, final String s3, final String s4, final String s5, final String s6) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return false;
        final Player p = Bukkit.getOnlinePlayers().stream().findAny().get();
        return SpigotChanellMsg.sendMessage(p, action, senderInfo, int1, int2, int3, s1, s2, s3, s4, s5, s6);
    }
    
    //с указанием передатчика
    public static boolean sendMessage (final Player msgTransport, final Operation action) {
        return SpigotChanellMsg.sendMessage(msgTransport, action);
    }
    public static boolean sendMessage (final Player msgTransport, final Operation action, final String senderInfo) {
        return SpigotChanellMsg.sendMessage(msgTransport, action, senderInfo);
    }
    public static boolean sendMessage (final Player msgTransport, final Operation action, final String senderInfo, final int int1) {
        return SpigotChanellMsg.sendMessage(msgTransport, action,senderInfo, int1);
    }
    public static boolean sendMessage (final Player msgTransport, final Operation action, final String senderInfo, final String string1) {
        return SpigotChanellMsg.sendMessage(msgTransport, action, senderInfo, string1);
    }
    public static boolean sendMessage (final Player msgTransport, final Operation action, final String senderInfo, final int int1, final String string1) {
        return SpigotChanellMsg.sendMessage(msgTransport, action, senderInfo, int1, string1);
    }
    public static boolean sendMessage (final Player msgTransport, final Operation action, final String senderInfo, final int int1, final int int2, final String s1, final String s2) {
        return SpigotChanellMsg.sendMessage(msgTransport, action, senderInfo, int1, int2, s1, s2);
    }
    public static boolean sendMessage (final Player msgTransport, final Operation action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, final String s2, final String s3) {
        return SpigotChanellMsg.sendMessage(msgTransport, action, senderInfo, int1, int2, int3, s1, s2, s3);
    }
    public static boolean sendMessage (final Player msgTransport, final Operation action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, final String s2, final String s3, final String s4, final String s5, final String s6) {
        return SpigotChanellMsg.sendMessage(msgTransport, action, senderInfo, int1, int2, int3, s1, s2, s3, s4, s5, s6);
    }
    
    
    /**
     * 
     * @param target игрок
     * @param server название сервера, как в настройках bungeecord
     * @param arena название арены на сервере для вызова ArenaJoinEvent в плагине bsign
     */    
    public static void sendToServer(final Player target, final String server, String arena) {
System.out.println("sendToServer server="+server+" arena="+arena);     
        if (server.equalsIgnoreCase(GM.this_server_name)) {
            Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick ( target, arena ) );
        } else {
            sendMessage(target, Operation.SEND_TO_ARENA, target.getName(), 0, 0, server, arena);
        }
    }        
    
     public static Connection getLocalConnection() {
        return LocalDB.GetConnection();
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
    public static boolean teleportSave(final Player p, final Location loc) {
        return teleportSave(p, loc, false);
    }
    public static boolean teleportSave(final Player p, final Location loc, final boolean buildSavePlace) {
        if (isLocationSave(p, loc)) {
            if (Bukkit.isPrimaryThread()) {
                p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
            } else {
                Ostrov.sync( ()->p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND), 0);
            }
            return true;
//System.out.println("1 loc="+loc);
        } else {
            Location save = ApiOstrov.findNearestSaveLocation(loc);
            if (save != null) {
                if (Bukkit.isPrimaryThread()) {
                    p.teleport(save, PlayerTeleportEvent.TeleportCause.COMMAND);
                } else {
                   Ostrov.sync( ()->p.teleport(loc), 0);
                }
                return true;
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
                    return true;
            } else {
                 p.sendMessage("§4Телепорт не удался - указанная локация опасна для жизни!");
                 return false;
            }
        }    
    }
 
    
    
    
    
    
    
    
        
     
    //   деньги
    /**
     * 
     * @param target только онлайн игроки!
     * @param value изменение, если убавить, то с минусом
     * @param source источник
     */
    public static void moneyChange ( final Player target, final int value, final String source ) {
        //PM.getOplayer(p).moneyChange(value, who);
            PM.getOplayer(target).setData(Data.LONI, PM.getOplayer(target).getDataInt(Data.LONI)+value);//moneySet(curr+value, send_update);
//System.out.println("--moneyChange Data.MONEY="+getIntData(Data.MONEY));        
            if (value>9 || value<-9) { //по копейкам не уведомляем
                target.spigot().sendMessage(new ComponentBuilder(Ostrov.prefix+"§7"+(value>9?"Поступление":"Расход")+" средств: "+source+" §7-> "+(value>9?"§2":"§4")+value+" лони §7! §8<клик-баланс")
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§5Клик - сколько стало?") ))
                    .event( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/money balance") )
                    .create());
            } else if (value<-9) {
                //?? писать ли что-нибудь??
            }
        //try {
        //    SpigotChanellMsg.sendMessage(p, Action.OSTROV_BUNGEE_MONEY_CHANGE, String.valueOf(value)+"<>"+who);
        //} catch (NullPointerException ex) {
        //    Ostrov.log_err("ApiOstrov moneyChange "+ex.getMessage());
        //}    
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
                LocalDB.moneyOffline(name, value, who);
            } else {
                Ostrov.log_err("Оффлайн-перевод для "+name+" на сумму "+value+", но локальная БД отключена!");
            }
        }
    } 
    public static int moneyGetBalance ( final String name ) {
        if (PM.exist(name)) return PM.getOplayer(name).getDataInt(Data.LONI);
        else return 0;
    }  
    public static String getBalanceStatus ( Player p ) {
        if (PM.exist(p.getName()) ) {
            final int m = PM.getOplayer(p.getName()).getDataInt(Data.LONI); 
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
       // try {
            if (Timer.delay_titles.containsKey(p.getName()))  {
                Timer.delay_titles.get(p.getName()).AddTitle(p.getName(), title, subtitle, fadein, stay, fadeout);
            } else {
                Timer.delay_titles.put(p.getName(), new DelayTitle(p.getName(), title, subtitle, fadein, stay, fadeout));
            }        
        //} catch (NullPointerException ex) {
        //    Ostrov.log_err("ApiOstrov sendTitle "+ex.getMessage());
        //}    
    }
    /**
     * 
     * сообщения сохраняются и выводятся поочерёдно с интервалом 3 сек.
     * @param p игрок. проверять есть ли он на сервере на надо
     * @param text сообщение
     */
    public static void sendActionBar(final Player p, final String text) {
        //try {
            //Utils.sendActionBar(p, text);
            if (Timer.delay_actionbars.containsKey(p.getName())) {
                Timer.delay_actionbars.get(p.getName()).AddMsg(text);
            } else{
                Timer.delay_actionbars.put(p.getName(), new DelayActionBar(p.getName(),text) );
            }
    //} catch (NullPointerException ex) {
     //       Ostrov.log_err("ApiOstrov sendActionBar "+ex.getMessage());
        //}    
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
       // try {
            //Utils.sendBossBar(p, text, seconds, bar_color, bar_style, show_progress);
            if (Timer.delay_bossbars.containsKey(p.getName())) {
                Timer.delay_bossbars.get(p.getName()).AddBar(p, text, seconds, bar_color, bar_style, show_progress);
            } else {
                Timer.delay_bossbars.put(p.getName(), new DelayBossBar(p, text, seconds, bar_color, bar_style, show_progress));
            }        
       // } catch (NullPointerException ex) {
       //     Ostrov.log_err("ApiOstrov sendBossbar "+ex.getMessage());
       // }    
    } 
    
    
    
    public static void sendTabList(final Player p, final String header, final String footer) {
        p.setPlayerListHeaderFooter(header, footer);
    }
        
        
    // *****************************************************************************    


    
    
    
    
    
    
    
    
    
    

    //    числа
    public static int randInt(int num1, int num2) {
       // int d;
       // if (num1>num2) {
       //     d = num2;
        //    num2 = num1;
        //    num1 = d;
        //}
        //d = Math.abs(num2 - num1);
//System.out.println("randInt() num1="+num1+" num2="+num2+" d="+d);
        if (num1==num2) return num1;
        return ((num1<num2) ? num1 : num2)  + Ostrov.random.nextInt(Math.abs(num2 - num1));
        //return Ostrov.random.nextInt((max - min) + 1) + min;
        //return -num1 + (int) (Math.random() * ((num2 - (-num1)) + 1));
    }
    public static boolean randBoolean() {
        return Ostrov.random.nextBoolean();
    }
    @Deprecated
    public static boolean isInteger(final String int_as_string) {
        return Ostrov.isInteger(int_as_string);
    }
    public static int getInteger(final String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return Integer.MIN_VALUE;
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
        if (year>0) {
            sb.append(year).append("г. ");
            //if ( month>0) {
            //    return  year+"г. "+month+" мес. ";
            //} else {
            //    return  year+"г. ";
            //}
        }
        
        if (month>0) {
            sb.append(month).append("мес. ");
            //if ( week==0) {
            //    return  month+" мес. ";
            //} else {
            //    return  month+" мес. "+week+"нед. ";
            //}
        }
        
        if (week>0 && year==0) {
            sb.append(week).append("нед. ");
            //if (day==0) {
            //    return  week+" нед. ";
            //} else {
            //    return  week+"нед. "+ day+"дн. ";
            //}
        }
        
        if (day>0) {
            sb.append(day).append("д. ");
            //if (day==0) {
            //    return  week+" нед. ";
            //} else {
            //    return  week+"нед. "+ day+"дн. ";
            //}
        }
        
        if (year>0) {
            return sb.toString(); //счёт на года - достаточно до дней
        }
        
        if (hour>0) {
            sb.append(hour).append("ч. ");
            //if (day==0) {
            //    return  week+" нед. ";
            //} else {
            //    return  week+"нед. "+ day+"дн. ";
            //}
        }
        
        
       // if (day>0) {
       //     if (hour==0) {
       //         return  day+"д. ";
       //     } else { //в масштабах дня минуты не считаем!
       //         return  day+"д. "+ ( hour>9 ? String.format("%02d",hour) : hour )+"ч ";
       //     }
      //  }
        
        
        if (month>0 || week>0) {
            return sb.toString(); //счёт на месяца - достаточно до часов
        }
        
        if (min>0) {
            sb.append(min).append("мин. ");
            //if (day==0) {
            //    return  week+" нед. ";
            //} else {
            //    return  week+"нед. "+ day+"дн. ";
            //}
        }
        if (second>0) {
            sb.append(second).append("сек. ");
            //if (day==0) {
            //    return  week+" нед. ";
            //} else {
            //    return  week+"нед. "+ day+"дн. ";
            //}
        }
      //  if (hour==0) {
            //second -= hour*60; -не надо, часы тут==0//в second - остаток минут + секунд
       //     if (min==0) {
        //        return  second+"сек.";//меньше минуты";
        //    } else {
        //        return  min+"мин. "+second+"сек.";//return  ( min>9 ? String.format("%02d",min) : min )+"мин.";
        //    }
       // } else {
       //     second -= hour*60; //в second - остаток минут + секунд
       //     if (min==0) {
      //          return  hour+"ч "+second+"сек.";
      //      } else {
      //          return  hour+"ч "+min+"мин. "+second+"сек.";//hour+"ч "+( min>9 ? String.format("%02d",min) : min )+"мин.";
      //      }
     //   }
        return sb.toString();
    }        

   // public static String housrToTime(int hours) {
   //     final int days = hours / 24;
   //     hours-=days*24;
   //     return  (days==0 ? "" : days+"дн. ") + (hours==0 ? (days==0?"меньше часа":"") : hours+"ч. ");
   // }
 
    public static String dateFromStamp(final int stamp_in_second) {
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
    //@Deprecated
    //public static String listToString(final Collection<String> array) {
    //    return listToString(array, ",");
    //}
    
    public static String listToString(final Iterable array, final String splitter) {
        StringBuilder sb=new StringBuilder();
        array.forEach( (s) -> {
            sb.append(String.valueOf(s)).append(splitter);
        });
        return sb.toString();
    }
    public static String enumSetToString(final EnumSet enumSet) {
        StringBuilder sb=new StringBuilder();
        enumSet.forEach((eNum) -> {
            sb.append(eNum.toString()).append(",");
        });
        return sb.toString();//allowRole;
    }
   // @Deprecated
  //  public static String listToString(final List<String> array, final String splitter) {
    //    StringBuilder sb=new StringBuilder();
   //     array.forEach( (s) -> {
   //         sb.append(s).append(splitter);
   //     });
    //    return sb.toString();
   /* }/*
    public static String setToString(final Set array, final String splitter) {
        StringBuilder sb=new StringBuilder();
        array.forEach( (s) -> {
            sb.append(String.valueOf(s)).append(splitter);
            //res=res+splitter+s;
        });
        //res =  res.replaceFirst(splitter, "");
        return sb.toString();
    }*/

    
    
    
 
 
 
 
 
    
    
    //    пол
    public static String genderEnd_Существительное(final String name) {
        if (PM.exist(name)) {
            switch (ChatColor.stripColor(PM.getOplayer(name).getDataString(Data.GENDER)).toLowerCase()) {
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
        return PM.exist(name) && ChatColor.stripColor(PM.getOplayer(name).getDataString(Data.GENDER)).equalsIgnoreCase("девочка");
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
   // @Deprecated
  //  public static boolean isSign (final Material mat) {
   //     return Tag.SIGNS.isTagged(mat);
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
 //   }

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

   // public static String getArenasTabbleName() { //для Бсигн, конфликтовали по енум
   //     return Table.GAMES_ARENAS.table_name;
   // }

    public static void sendArenaData(final String arenaName, final GameState state, final String line0, final String line1, final String line2, final String line3, final String extra, final int playerInGame) {
        GM.sendArenaData(arenaName, (state==null ? GameState.НЕОПРЕДЕЛЕНО : state), playerInGame, line0, line1, line2, line3, extra);
    }


    public static boolean checkString (String message, final boolean allowNumbers, final boolean allowRussian) {
        return checkString(message, false, allowNumbers, allowRussian);
    }
    public static boolean checkString (String message, final boolean allowSpace,  final boolean allowNumbers,final boolean allowRussian) {
        if (allowNumbers && allowRussian) {
            message = message.replaceAll(pattern_Eng_Num_Rus, "");
        } else if (allowNumbers) {
            message = message.replaceAll(pattern_Eng_Num, "");
        } else if (allowRussian) {
            message = message.replaceAll(pattern_Eng_Rus, "");
        } else {
            message = message.replaceAll(pattern_Eng, "");
        }
        return allowSpace ? message.isBlank() :  message.isEmpty() ;
   }    
        

    public static boolean canBeBuilder(final CommandSender cs) {
        //return (cs instanceof ConsoleCommandSender) || cs.isOp() || cs.hasPermission(Bukkit.getServer().getMotd()+".builder") || hasGroup(cs.getName(), "supermoder");
        return (cs instanceof ConsoleCommandSender) || cs.isOp() || cs.hasPermission("builder") || hasGroup(cs.getName(), "supermoder");
    } 
    
    public static boolean isLocalBuilder(final CommandSender cs) {
        return isLocalBuilder(cs, false);
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
                if (canBeBuilder(p)) { //p.hasPermission(Bukkit.getServer().getMotd()+".builder") -сервер срезает!!!!
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









    public static int currentTimeSec() {
        return Timer.getTime();
    }
    public static void makeWorldEndToWipe(final int afterSecond) {
        Ostrov.makeWorldEndToWipe(afterSecond);
    }

    public static void moveDeny(final PlayerMoveEvent e) {
        if (e.getTo().getY()<e.getFrom().getY()) {
            e.setTo( e.getFrom().add(0, 2, 0) );
        } else {
            e.setTo(e.getFrom());
        }
    }





}
