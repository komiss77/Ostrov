package ru.komiss77.modules.player;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.World;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Perm;
import ru.komiss77.Timer;
import ru.komiss77.commands.TprCmd;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.events.PartyUpdateEvent;
import ru.komiss77.modules.player.profile.Friends;
import ru.komiss77.modules.player.profile.StatManager;
import ru.komiss77.objects.CaseInsensitiveMap;



public class PM {
	
    private static Function<HumanEntity, ? extends Oplayer> opSup = he -> new Oplayer(he);//могут указывать другие плагины
    
    private static final CaseInsensitiveMap <Oplayer> oplayers;
    //public static NametagManager nameTagManager;
    public static final EnumMap<Data,Integer> textEdit;
    private static final Component builderMsgRu;
    private static final Component builderMsgEn;
    
    static {
        oplayers = new CaseInsensitiveMap<>();
        //nameTagManager = new NametagManager();
        
        builderMsgRu = Component.text("§a>>>> §fКлик сюда - выполнить /builder §a<<<<")
        	.hoverEvent(HoverEvent.showText(Component.text("§aВключить ГМ1 и открыть меню строителя")))
        	.clickEvent(ClickEvent.runCommand("/builder"));
            
        builderMsgEn = Component.text("§a>>>> §fClick - execute command /builder §a<<<<")
        	.hoverEvent(HoverEvent.showText(Component.text("§aSet gamemode creative and open the builder menu")))
        	.clickEvent(ClickEvent.runCommand("/builder"));
            
        textEdit = new EnumMap<>(Data.class);
            textEdit.put(Data.FAMILY, 32);
            textEdit.put(Data.LAND, 32);
            textEdit.put(Data.CITY, 32);
            textEdit.put(Data.ABOUT, 256);
            textEdit.put(Data.DISCORD, 32);
            //textEdit.put(Data.NOTES, 256);
    }

    


    
    
    
    

    public static void setOplayerFun(final Function<HumanEntity, ? extends Oplayer> opSup, final boolean remake) {
        PM.opSup = opSup;
        if (remake) {
            oplayers.clear();
            Bukkit.getOnlinePlayers().stream().forEach(p -> PM.createOplayer(p));
        }
    }
    
    public static Oplayer createOplayer(final HumanEntity he) {
    	final Oplayer op = opSup.apply(he);
        PM.oplayers.put(he.getName(), op);
        return op;
    }
    
    public static Collection<Oplayer> getOplayers() {
        return oplayers.values();
    }
    
    public static <O extends Oplayer> Collection<O> getOplayers(final Class<O> cls) {
        return oplayers.values().stream().map(o -> cls.cast(o)).collect(Collectors.toList());
    }
    
    public static Set<String> getOplayersNames() {
        return oplayers.keySet();
    }

    public static Oplayer getOplayer(final String nik) {
        return getOplayer(nik, Oplayer.class);
    }

    public static <O extends Oplayer> O getOplayer(final String nik, final Class<O> cls) {
        return cls.cast(oplayers.get(nik));
    }
    
    public static Oplayer getOplayer(final HumanEntity p) {
        return oplayers.get(p.getName());
    }    
    
    //не убирать, посыпались все плагины!!  Caused by: java.lang.NoSuchMethodError: 'ru.komiss77.modules.player.Oplayer ru.komiss77.modules.player.PM.getOplayer(org.bukkit.entity.Player)'
    public static Oplayer getOplayer(final Player p) {
        return getOplayer(p.getName(), Oplayer.class);
    }

    public static <O extends Oplayer> O getOplayer(final HumanEntity p, final Class<O> cls) {
        return cls.cast(oplayers.get(p.getName()));
    }
    
    public static boolean exist (final String nik) {
        return oplayers.containsKey(nik) && Bukkit.getPlayerExact(nik)!=null;
        //переделать на runable
    }
    public static Oplayer remove (final String nik) {
        return oplayers.remove(nik);
    }

    public static int getOnlineCount() {
        return oplayers.size();
    }
    public static boolean hasOplayers() {
        return !oplayers.isEmpty();
    }



    
    

    
    
    
    



    //-создать оп сразу, пермишены и прочее зависимое от player пересчитать когда player будет не null, или бывает так:
    //UUID of player komiss77 is 227faf91-3a15-39b0-b1f9-bf8850f4d7b6
    //[00:05:49 INFO]: [Остров] onPluginMessage chanel OSTROV readbuff error chanelName=ostrov:type4 : Cannot invoke "org.bukkit.entity.Player.getName()" because "p" is null
    //[00:05:49 INFO]: komiss77[/46.32.91.134:45677] logged in with entity id 2300 at ([lobby]1.5, 7.0, 0.5)
    public static void bungeeDataInject(final Player p, final String raw) { //всё сразу
        
        final Oplayer op = oplayers.get(p.getName());

        op.dataString.put(Data.NAME, op.nik);
//Ostrov.log("+++bungeeDataInject raw="+raw);            
        int enumTag;
        String value;
        int v;

        for (String s:raw.split("∫")) {
            if ( s.length()<4) continue;

            enumTag = ApiOstrov.getInteger(s.substring(0, 3));
            value = s.substring(3);
//Ostrov.log("enumTag="+enumTag+" val="+value);            

            if (enumTag>=100 && enumTag<=299) {
                    final Data _data = Data.byTag(enumTag);
//Ostrov.log("_data="+_data);            
                    if (_data!=null) {
                        if (_data.is_integer) {
                            v = ApiOstrov.getInteger(value);
                            if (v>Integer.MIN_VALUE) {
                                op.dataInt.put (_data, v);
//Ostrov.log("dataInt.put="+value);            
                            }
                        } else {
//Ostrov.log("dataString.put="+value);            
                            op.dataString.put (_data, value);
                        }
                    } 
            } else if (enumTag>=300 && enumTag<=599) {
                final Stat e_stat = Stat.byTag(enumTag);
                v = ApiOstrov.getInteger(value);
                if (e_stat!=null && v>Integer.MIN_VALUE) {
                    op.stat.put(e_stat, v);
                }
            } else if (enumTag>=600 && enumTag<=899) {
                final Stat e_stat = Stat.byTag(enumTag-Stat.diff);
                v = ApiOstrov.getInteger(value);
                if (e_stat!=null && v>Integer.MIN_VALUE) {
                    op.daylyStat.put(e_stat, v);
                }
            }

        }
        op.eng = op.getDataInt(Data.LANG) == 1;
        if (op.dataString.containsKey(Data.FRIENDS) && !op.dataString.get(Data.FRIENDS).isEmpty()) { //друг:сервер, список
            op.friends.addAll(Arrays.asList(op.dataString.get(Data.FRIENDS).split(","))); //info = name:server:settings
        }
        if (op.dataString.containsKey(Data.MISSIONS)) {
            for (String id:op.dataString.get(Data.MISSIONS).split(";")) {
                if (ApiOstrov.isInteger(id)) {
                    op.missionIds.add(Integer.valueOf(id));
                }
            }
        }
        
        StatManager.recalc(op);
        
        onPartyRecieved(p, op, false);

        Perm.calculatePerms(p, op, false); //там еще добавить таблист префикс,суффикс для донатов и стафф

//Ostrov.log("JustGame?"+op.hasSettings(Settings.JustGame));
        Bukkit.getPluginManager().callEvent(new BungeeDataRecieved ( p, op ) ) ;
        
        Friends.updateViewMode(p);
        
        if (ApiOstrov.canBeBuilder(p)) {
            ApiOstrov.sendActionBarDirect(p, op.eng ? "§f* You are §eBuilder §fon this server." : "§f* У Вас есть право §eСтроителя §fна этом сервере.");
            p.sendMessage(op.eng ? builderMsgEn : builderMsgRu);
        }
        
        op.updScore();
        op.updateGender();
        op.updTabListName(p);
        
        op.setLocalChat(false); //вернуть глобальный чат - фикс проблемы выхода с миниигр из мира карты
        //if (op.hasFlag(StatFlag.LocalChat)) {
        //    p.sendMessage("§8Используем локальный чат (так указано в настройках)");
        //}
        if (Ostrov.MOT_D.equals("jail")) {
            ApiOstrov.sendTabList(p,  op.eng ?"§4PURGATORY":"§4ЧИСТИЛИЩЕ", "");
            if (op.isStaff) {
                p.sendMessage(op.eng ? "§cYou're in maintenance purgatory.." : "§cВы в чистилище для тех.обслуживания.");
            } else {
                final World w = Bukkit.getWorld("WORLD_NETHER");
                if (w!=null) {
                    TprCmd.runCommand(p, w, 300, true, true, null);
                }
                p.sendMessage(op.eng ?"§cYou are banned and placed in purgatory.":"§cВы забанены и помещены в чистилище.");
                p.sendMessage((op.eng ?"§cAfter §f":"§cЧерез §f")+ApiOstrov.secondToTime(op.getDataInt(Data.BAN_TO)-Timer.getTime()));
                p.sendMessage(op.eng ?"§cyou can return to the Ostrov.":"§cсможете вернуться на Остров.");
            }
        }
    }

    
    
    
    public static void onPartyRecieved(final Player p, final Oplayer op, final boolean callEvent) { //прилетает при входе, нажатии в меню и обновлении состава на банжи из пати-плагина
        op.party_members.clear();
        op.party_leader = "";
//System.out.println("---onPartyRecieved2 PARTY_MEBRERS="+dataString.get(Data.PARTY_MEBRERS));
        if (op.dataString.containsKey(Data.PARTY_MEBRERS) && !op.dataString.get(Data.PARTY_MEBRERS).isEmpty()) {
            boolean first = true;
            String[] split;
            for (String player_and_server:op.dataString.get(Data.PARTY_MEBRERS).split(",")) {
                split = player_and_server.split(":");
                if (split.length==2) {
                    if (first) {
                        op.party_leader=split[0];
                        first = false;
                    }
                    op.party_members.put(split[0], split[1]);
                }
            }
        }
//System.out.println("---onPartyRecieved2 party_leader="+party_leader+"  party_members="+party_members);
        Bukkit.getPluginManager().callEvent(new PartyUpdateEvent(p, op.party_leader, op.getPartyMembers()));
    }
    
    
    //обнова данных с банжи по SET_DATA_TO_OSTROV - значит на острове уже новое значение
    //в основном для даты, но можно изменить отдельную стату (например, E_Stat.FLAGS) минуя addStat
    public static void updateDataFromBungee(final Player p, final int enumTag, final int int2, final String string1) {
        final Oplayer op = PM.getOplayer(p);
        if (enumTag>=100 && enumTag<=299) {
            final Data d = Data.byTag(enumTag);
            if (d!=null) {
                final boolean change;
                if (d.is_integer) {
                    if (op.dataInt.containsKey(d)) { //такая схема подобрана мучительно, не менять!!
                        change = op.dataInt.put(d, int2)!=int2;
                    } else {
                        op.dataInt.put(d, int2);
                        change = true;
                    }
                    // = !dataInt.containsKey(d) || dataInt.put(d, int2)!=int2;//dataInt.put (_data, int2);
                } else {
                    if (op.dataString.containsKey(d)) {
                        change = !op.dataString.put(d, string1).equals(string1);
                    } else {
                        op.dataString.put(d, string1);
                        change = true;
                    }
                    //change =  !dataString.containsKey(d) || !dataString.put(d, string1).equals(string1);//dataString.put (d, string1);
                }
//System.out.println("-updateDataFromBungee Data="+d+" change?"+change+" int="+int2+" str="+string1);
                if (change) {
                    //для кармы и репутации свои тэги - REPUTATION_BASE_CHANGE и KARMA_BASE_CHANGE
                    switch (d) {
                        case REPUTATION -> StatManager.reputationCalc(op);
                        case KARMA -> StatManager.karmaCalc(op);
                        case USER_GROUPS -> Perm.calculatePerms(p, op, true);
                        //StatManager.calculateReputationBase(this);
                        case USER_PERMS -> Perm.calculatePerms(p, op, false);
                        case PARTY_MEBRERS -> PM.onPartyRecieved(p, op, true);
                        case LANG -> op.eng = int2!=0; //0-русский, 1-английский
                        default -> {}
                    }
                }
            }
            
        } else if (enumTag>=300 && enumTag<=599) {
            final Stat e_stat = Stat.byTag(enumTag);
            if (e_stat!=null) {
                op.stat.put(e_stat, int2);
            }
        } else if (enumTag>=600 && enumTag<=899) {
            final Stat e_stat = Stat.byTag(enumTag-Stat.diff);
            if (e_stat!=null) {
                op.daylyStat.put(e_stat, int2);
            }
        }

    }
    
    
    
    










//---------------------- Режим боя, сброс инвентаря ----------------------------
    public static boolean inBattle (String nik) {
        return oplayers.containsKey(nik) && getOplayer(nik).pvp_time>0;
    }
    public static int inBattle_time_left (String nik) {
    //System.out.println(" ???? inBattle "+nik+"  time:"+CMD.pvp_battle_time+" -> "+Timer.CD_has(nik, "pvp") );
        return oplayers.containsKey(nik)? getOplayer(nik).pvp_time : 0;
    }

//------------------------------------------------------------------------------





/*

    public static void updateTagAll(final Player of, final Oplayer op) {
        updateTag(of, op, Bukkit.getOnlinePlayers());
    }

    public static void updateTag(final Player of, final Oplayer op, final Player to) {
        VM.getNmsNameTag().updateTag( of, op, to);
    }

    public static void updateTag(final Player of, final Oplayer op, final Collection<? extends Player> to) {
        VM.getNmsNameTag().updateTag(of, op, to);
    }

*/




   public static void soundDeny(final Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.3f, 1);
    }

    public static String getGenderDisplay(final Oplayer op) {
        return switch (op.gender) {
            case FEMALE -> op.eng ? "§dGirl" : "§dДевочка";
            case MALE -> op.eng ? "§dBoy" : "§9Мальчик";
            default -> op.eng ? "§3Unisex" : "§3Бесполое";
        };
    }






    public enum Gender {
        MALE, FEMALE, NEUTRAL
    }


}
