package ru.komiss77.modules.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.Perm;
import ru.komiss77.Timer;
import ru.komiss77.commands.PvpCmd;
import ru.komiss77.commands.TprCmd;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.BungeeDataRecieved;
import ru.komiss77.events.PartyUpdateEvent;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.profile.E_Pass;
import ru.komiss77.modules.player.profile.Friends;
import ru.komiss77.modules.player.profile.StatManager;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.version.Nms;


public class PM {
	
    private static Function<HumanEntity, ? extends Oplayer> opSup = Oplayer::new;// he -> new Oplayer(he); могут указывать другие плагины
    private static final CaseInsensitiveMap <Oplayer> oplayersByName;
    private static final ConcurrentHashMap <UUID, Oplayer> oplayersByUuid;
    public static final EnumMap<Data,Integer> textEdit;
    private static final Component builderMsgRu;
    private static final Component builderMsgEn;
    
    static {
        oplayersByName = new CaseInsensitiveMap<>();
        oplayersByUuid = new ConcurrentHashMap<>();
        
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
            oplayersByName.clear();
            oplayersByUuid.clear();
            Bukkit.getOnlinePlayers().forEach(PM::createOplayer);
        }
    }
    
    public static Oplayer createOplayer(final HumanEntity he) {
    	final Oplayer op = opSup.apply(he);
        oplayersByName.put(he.getName(), op);
        oplayersByUuid.put(he.getUniqueId(), op);
        return op;
    }
    
    public static Collection<Oplayer> getOplayers() {
        return oplayersByUuid.values();
    }
    
    public static <O extends Oplayer> Collection<O> getOplayers(final Class<O> cls) {
        return oplayersByUuid.values().stream().map(cls::cast).collect(Collectors.toList());
    }
    
    public static Set<String> getOplayersNames() {
        return oplayersByName.keySet();
    }


    public static Oplayer getOplayer(final String nik) { //@Dep иногда очень надо найти по имени, напр. при сообщении, или когда UUID передавать неудобно
        return oplayersByName.get(nik);
    }

    public static Oplayer getOplayer(final HumanEntity p) {
        return getOplayer(p.getUniqueId());
    }    
    
    public static Oplayer getOplayer(final UUID uuid) {
        return oplayersByUuid.get(uuid);
    }    
    
    public static Oplayer getOplayer(final Player p) {
      return oplayersByUuid.get(p.getUniqueId());
    }

    public static <O extends Oplayer> O getOplayer(final HumanEntity p, final Class<O> cls) {
        return getOplayer(p.getUniqueId(), cls);
    }

    public static <O extends Oplayer> O getOplayer(final UUID uuid, final Class<O> cls) {
        return cls.cast(oplayersByUuid.get(uuid));
    }

    public static boolean exists(final HumanEntity p) {
        return oplayersByUuid.containsKey(p.getUniqueId());
    }

    public static boolean exists(final UUID id) {
        return oplayersByUuid.containsKey(id);
    }

    public static boolean exist(final String nik) {
        return oplayersByName.containsKey(nik);
    }

    public static Oplayer remove(final HumanEntity p) {
        return remove(p.getUniqueId());
    }

    public static Oplayer remove(final UUID id) {
        final Oplayer op = oplayersByUuid.remove(id);
        if (op!=null) oplayersByName.remove(op.nik);
        return op;
    }

    public static Oplayer remove (final String nik) {
        Oplayer op = oplayersByName.remove(nik);
        if (op!=null) {
            oplayersByUuid.remove(op.id);
        }
        return op;
    }

    public static int getOnlineCount() {
        return oplayersByUuid.size();
    }

    public static boolean hasOplayers() {
        return !oplayersByUuid.isEmpty();
    }





    //-создать оп сразу, пермишены и прочее зависимое от player пересчитать когда player будет не null, или бывает так:
    public static void bungeeDataHandle(final Player p, final Oplayer op, final String raw) { //всё сразу

        op.dataString.put(Data.NAME, op.nik);
        
        int enumTag;
        String value;
        int v;

        for (String s:raw.split("∫")) {
            if ( s.length()<4) continue;

            enumTag = ApiOstrov.getInteger(s.substring(0, 3));
            value = s.substring(3);

            if (enumTag>=100 && enumTag<=299) {
                    final Data _data = Data.byTag(enumTag);
                    if (_data!=null) {
                        if (_data.is_integer) {
                            v = ApiOstrov.getInteger(value);
                            if (v>Integer.MIN_VALUE) {
                                op.dataInt.put (_data, v);
                            }
                        } else {
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
                    op.dailyStat.put(e_stat, v);
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
        
        StatManager.recalc(op); //пересчёт статы
        
        onPartyRecieved(p, op, false); //обработка пати

        Perm.calculatePerms(p, op, false); //там еще добавить таблист префикс,суффикс для донатов и стафф

        Bukkit.getPluginManager().callEvent(new BungeeDataRecieved ( p, op ) );
        
        Friends.updateViewMode(p);
        
        op.updateGender();
        
        op.beforeName(null, p);

        op.setLocalChat(false); //вернуть глобальный чат - фикс проблемы выхода с миниигр из мира карты
        
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
        
        if (ApiOstrov.canBeBuilder(p)) {
            ApiOstrov.sendActionBarDirect(p, op.eng ? "§f* You are §eBuilder §fon this server." : "§f* У Вас есть право §eСтроителя §fна этом сервере.");
            p.sendMessage(op.eng ? builderMsgEn : builderMsgRu);
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
                op.dailyStat.put(e_stat, int2);
            }
        }

    }
    
    
    
    public static void onLeave(final Player p, final boolean async) {
        final Oplayer op = remove(p.getName());
        if (op==null) {
            Ostrov.log_warn("PlayerQuitEvent : Oplayer == null!");
            return;
        }
        op.preDataSave(p, async);
        Nms.removePlayerPacketSpy(p);

        //в saveLocalData инвентарь не сохранит
        if (PvpCmd.getFlag(PvpCmd.PvpFlag.drop_inv_inbattle) &&  PvpCmd.getFlag(PvpCmd.PvpFlag.antirelog) && op.pvp_time>0) {      //если удрал во время боя
            final List<ItemStack> drop = new ArrayList<>();
            for (ItemStack is : p.getInventory().getContents()) {
                if (is != null && !MenuItemsManager.isSpecItem(is)) {
                    drop.add(is.clone());
                }
            }


            for (ItemStack is : drop) {
                p.getWorld().dropItemNaturally(p.getLocation(), is).setPickupDelay(40);
            }
        }
        if (!op.mysqlError && !op.mysqlData.isEmpty() && LocalDB.useLocalData) {
            if (async) {
                Ostrov.async(()->LocalDB.saveLocalData(p, op), 0); //op.mysqlData не должна быть пустой, если загружало!
            } else {
                LocalDB.saveLocalData(p, op);
            }
        }
        op.tag.visible(false);
        op.score.remove();
        op.postDataSave(p, async);
    }









//---------------------- Режим боя, сброс инвентаря ----------------------------
    public static boolean inBattle (String nik) {
        return oplayersByName.containsKey(nik) && getOplayer(nik).pvp_time>0;
    }
  //  public static int inBattle_time_left (String nik) {
    //System.out.println(" ???? inBattle "+nik+"  time:"+CMD.pvp_battle_time+" -> "+Timer.CD_has(nik, "pvp") );
   //     return oplayersByName.containsKey(nik)? getOplayer(nik).pvp_time : 0;
   // }

//------------------------------------------------------------------------------



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


    public static int getPasportFillPercent(final Oplayer op) {
        double max = 0;
        for (E_Pass ep:E_Pass.values()) {
            if (ep.editable) max++;
        }
        int complete = 0;
        E_Pass ePass;
        for (Data d:op.dataString.keySet()) {
            ePass = E_Pass.fromStrind(d.name());
            if (ePass!=null && ePass.editable && !op.getDataString(d).isEmpty()) {
                complete++;
            }
        }
        return (int) Math.round(complete/max*100);
    }

    public static Map<E_Pass, String> getPassportData(final Oplayer op, final boolean skipUneditable) { //для паспорта
        final EnumMap<E_Pass,String>result = new EnumMap<>(E_Pass.class);
        E_Pass ePass;
        for (Data d:op.dataString.keySet()) {
            ePass = E_Pass.fromStrind(d.name());
            if (ePass!=null && (!skipUneditable || ePass.editable)) {
                result.put(ePass, op.getDataString(d));
            }
        }
        for (Data d:op.dataInt.keySet()) {
            ePass = E_Pass.fromStrind(d.name());
            if (ePass!=null && (!skipUneditable || ePass.editable)) {
                result.put(ePass, String.valueOf(op.getDataInt(d)) );
            }
        }
        for (Stat st:op.stat.keySet()) {
            ePass = E_Pass.fromStrind(st.name());
            if (ePass!=null && (!skipUneditable || ePass.editable)) {
                result.put(ePass, String.valueOf(op.getStat(st)) );
            }
        }
        //for (Stat st:daylyStat.keySet()) {
        //    if (E_Pass.exist(st.name())) {
        //        result.put(E_Pass.valueOf(st.name()), ""+daylyStat.get(st));
        //    }
        //}
//System.out.println("result="+result);
        result.put(E_Pass.USER_GROUPS, op.chat_group);
        return result;
    }
    



    public enum Gender {
        MALE, FEMALE, NEUTRAL
    }


}
