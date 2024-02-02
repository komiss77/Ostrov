package ru.komiss77.listener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.bukkit.Sound;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.SeenCmd;
import ru.komiss77.enums.Chanell;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.events.OstrovChanelEvent;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.Friends;
import ru.komiss77.utils.TCUtils;


    //SPIGOT!!! 
public class SpigotChanellMsg implements Listener, PluginMessageListener {

    
    public static void sendChat(final Player sender, final String raw, final Chanell ch) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            out.writeUTF(raw);
            sender.sendPluginMessage(Ostrov.instance, ch.name, stream.toByteArray());
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("SpigotChanellMsg  sendChat : "+ex.getMessage());
        }   
    }
    
      
    public static boolean sendMessage(final Player msgTransport, final Operation action) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            out.writeInt(action.tag);
            msgTransport.sendPluginMessage(Ostrov.instance,Chanell.Action.name, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("Не удалось sendMessage : "+ex.getMessage());
            return false;
        }
    }

    public static boolean sendMessage(final Player msgTransport, final Operation action, final String senderInfo) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            out.writeInt(action.tag);
            out.writeUTF(senderInfo);
            msgTransport.sendPluginMessage(Ostrov.instance,Chanell.Action_Sender.name, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("Не удалось sendMessage : "+ex.getMessage());
            return false;
        }
    }

    public static boolean sendMessage(final Player msgTransport, final Operation action, final String senderInfo, final int int1) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            out.writeInt(action.tag);
            out.writeUTF(senderInfo);
            out.writeInt(int1);
            msgTransport.sendPluginMessage(Ostrov.instance,Chanell.Action_Sender_Int.name, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("Не удалось sendMessage : "+ex.getMessage());
            return false;
        }
    }

    public static boolean sendMessage(final Player msgTransport, final Operation action, final String senderInfo, final String s1) {
//System.out.println("--sendMessage senderInfo="+senderInfo+" s1="+s1);                            
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            out.writeInt(action.tag);
            out.writeUTF(senderInfo);
            out.writeUTF(s1);
            msgTransport.sendPluginMessage(Ostrov.instance,Chanell.Action_Sender_String.name, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("Не удалось sendMessage : "+ex.getMessage());
            return false;
        }
    }

    public static boolean sendMessage(final Player msgTransport, final Operation action, final String senderInfo, final int int1, final String s1) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            out.writeInt(action.tag);
            out.writeUTF(senderInfo);
            out.writeInt(int1);
            out.writeUTF(s1);
            msgTransport.sendPluginMessage(Ostrov.instance,Chanell.Action_Sender_Int_String.name, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("Не удалось sendMessage : "+ex.getMessage());
            return false;
        }
    }

    public static boolean sendMessage(final Player msgTransport, final Operation action, final String senderInfo, final int int1, final int int2, final String s1, final String s2) {
//Bukkit.broadcastMessage("sendMessage msgTransport="+msgTransport.getName()+" op="+action+"  sendr="+senderInfo+" tag="+int1+" amm="+int2+" target="+s1+" param="+s2);
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            out.writeInt(action.tag);
            out.writeUTF(senderInfo);
            out.writeInt(int1);
            out.writeInt(int2);
            out.writeUTF(s1);
            out.writeUTF(s2);
            msgTransport.sendPluginMessage(Ostrov.instance,Chanell.Action_Sender_Int2_String2.name, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("Не удалось sendMessage : "+ex.getMessage());
            return false;
        }
    }

    public static boolean sendMessage(final Player msgTransport, final Operation action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, final String s2, final String s3) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            out.writeInt(action.tag);
            out.writeUTF(senderInfo);
            out.writeInt(int1);
            out.writeInt(int2);
            out.writeInt(int3);
            out.writeUTF(s1);
            out.writeUTF(s2);
            out.writeUTF(s3);
            msgTransport.sendPluginMessage(Ostrov.instance,Chanell.Action_Sender_Int3_String3.name, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("Не удалось sendMessage : "+ex.getMessage());
            return false;
        }
    }

    public static boolean sendMessage(final Player msgTransport, final Operation action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, final String s2, final String s3, final String s4, final String s5, final String s6) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            out.writeInt(action.tag);
            out.writeUTF(senderInfo);
            out.writeInt(int1);
            out.writeInt(int2);
            out.writeInt(int3);
            out.writeUTF(s1);
            out.writeUTF(s2);
            out.writeUTF(s3);
            out.writeUTF(s4);
            out.writeUTF(s5);
            out.writeUTF(s6);
            msgTransport.sendPluginMessage(Ostrov.instance,Chanell.Action_Sender_Int3_String6.name, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("Не удалось sendMessage : "+ex.getMessage());
            return false;
        }    
    
    }

    
    
    
 
    
    
	@Override
    public void onPluginMessageReceived(final String chanelName, Player msgTransport, byte[] msg) {
        final Chanell ch = Chanell.fromName(chanelName);
//Ostrov.log_warn("1 >>>>MessageReceived: chanelName="+chanelName+" Chanell="+ch);  
        if (ch == null) {
            Ostrov.log_err("onPluginMessage Chanell=null : "+chanelName);
            return; 
        }
        
        
        try { 
            final ByteArrayDataInput in = ByteStreams.newDataInput(msg); 
            
            // отловить тут новый чат, он без операции - только канал
            if (ch == Chanell.CHAT_EN || ch == Chanell.CHAT_RU) {
                ChatLst.onProxyChat (ch, in.readInt(), in.readUTF(), in.readUTF(), in.readUTF());
                return;
            }
            
            final Operation operation = Operation.byTag(in.readInt());
            
            if (operation==null || operation == Operation.NONE) { //например, чат расылается в старом формате как NONE и в новом, как CHAT_TO_OSTROV
                Ostrov.log_err("onPluginMessage Action="+operation+" chanel="+chanelName);
                return; 
            }
//Ostrov.log_warn("chanel="+ch+" action="+action+" msgTransport="+msgTransport.getName());
            switch (ch) {
                
                case Action -> onChanelMsg(operation);
                    
                case Action_Sender -> onChanelMsg(operation, in.readUTF());
                    
                case Action_Sender_Int -> onChanelMsg(operation, in.readUTF(), in.readInt());
                    
                case Action_Sender_String -> onChanelMsg(operation, in.readUTF(), in.readUTF());
                    
                case Action_Sender_Int_String -> onChanelMsg(operation, in.readUTF(), in.readInt(), in.readUTF());
                    
                case Action_Sender_Int2_String2 -> onChanelMsg(operation, in.readUTF(), in.readInt(), in.readInt(), 0, in.readUTF(), in.readUTF(), null, null, null, null);
                    
                case Action_Sender_Int3_String3 -> onChanelMsg(operation, in.readUTF(), in.readInt(), in.readInt(), in.readInt(), in.readUTF(), in.readUTF(), in.readUTF(), null, null, null);
                    
                case Action_Sender_Int3_String6 -> onChanelMsg(operation, in.readUTF(), in.readInt(), in.readInt(), in.readInt(), in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF());
               
                default -> {}
            }

//System.out.println("3 вызов OstrovChanelEvent from="+from+"  action="+action.toString()+" raw="+bungee_raw_data);

        } catch (NumberFormatException|NullPointerException|ArrayIndexOutOfBoundsException ex) {
            Ostrov.log_err("onPluginMessage ch="+ch+" error : "+ ex.getMessage());
        }
            
    }
  
    
    



    private static void onChanelMsg(final Operation action) {
       // switch (action) {

                
                
       // }
    }


   
    private static void onChanelMsg(final Operation action, final String senderInfo) {
        final Player p;// p = Bukkit.getPlayerExact(senderInfo);
        final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {
            
            case RESET_DAYLY_STAT -> {
                p = Bukkit.getPlayerExact(senderInfo);
                op = PM.getOplayer(senderInfo);
                op.resetDaylyStat();
                ApiOstrov.sendBossbar(p, "§cДневная статистика сброшена!", 6, Color.RED, Overlay.NOTCHED_12);
                return;
            }
                
            default -> Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, 0, 0, 0, null, null, null, null, null, null));
                
        }
        //case AUTH_BEGIN: //обработать в эвенте
        //case AUTH_STAFF: //обработать в эвенте
    }


    private static void onChanelMsg(final Operation action, final String senderInfo, final int int1) {
        //final Player p;// p = Bukkit.getPlayerExact(senderInfo);
        final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {

            case GONLINE:
                GM.bungee_online=int1;
                break;

            case ADD_EXP: //приходит от RewardHandler
                op = PM.getOplayer(senderInfo);
                op.addStat(Stat.EXP, int1);
                break;


                
             default:
                Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, int1, 0, 0, null, null, null, null, null, null));
                break;
                
        }
    }

   
   
   
    private static void onChanelMsg(final Operation action, final String senderInfo, final String s1) {
        //final Player p = Bukkit.getPlayerExact(senderInfo);
        final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {

            case OSTROV_RAW_DATA -> PlayerLst.onBungeeData(senderInfo, s1);

            case FRIENDS_INFO_RESULT -> {
                //p = Bukkit.getPlayerExact(senderInfo);
                op = PM.getOplayer(senderInfo);
                Friends.onFriendsInfoRecieve(op, s1);
            }

            case EXECUTE_OSTROV_CMD -> {
                if (senderInfo.equals("console")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s1);
                } else {
                    Player p = Bukkit.getPlayerExact(senderInfo);
                    if (p!=null) {
                        if (s1.equals("PMsound")) { //такой фикс, если от консоли то пишет 'воспроизведён звук'
                            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
                        } else {
                            p.performCommand(s1);
                        }
                    }
                }
            }
                
            case ADD_IGNORE_OSTROV -> {
                op = PM.getOplayer(senderInfo);
                op.addBlackList(s1);
            }
                
            case REMOVE_IGNORE_OSTROV -> {
                op = PM.getOplayer(senderInfo);
                op.removeBlackList(s1);
            }
                
            case TELEPORT_EVENT -> {
                final Player sender=Bukkit.getPlayerExact(senderInfo);
                final Player target=Bukkit.getPlayerExact(s1);
                //System.out.println("333 who="+who+" target="+target);
                if(target==null || !target.isOnline()) {
                    sender.sendMessage(TCUtils.format("§cТелепорт не удалось завершить - "+s1+" не найден!"));
                    //return;
                } else {
                    FriendTeleportEvent event=new FriendTeleportEvent(sender, target);
                    Bukkit.getPluginManager().callEvent(event);
                    if(event.isCanceled()) {
                        sender.sendMessage(TCUtils.format("§cТелепорт не удалось завершить: "+event.cause));
                        target.sendMessage(TCUtils.format("§cТелепорт не удалось завершить: "+event.cause));
                    } else {
                        sender.teleport(target);
                        sender.sendMessage(TCUtils.format("§6Вы телепортировались к "+target.getName()));
                        target.sendMessage(TCUtils.format("§6К вам телепортировался "+senderInfo));
                    }
                }
            }
                
                
             default -> Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, 0, 0, 0, s1, null, null, null, null, null));
                
        }
    }

   private static void onChanelMsg(final Operation action, final String senderInfo, final int int1, final String s1) {
        final Player p;// p = Bukkit.getPlayerExact(senderInfo);
        //final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {

            case PLAYER_DATA_REQUEST_RESULT -> {
                p = Bukkit.getPlayerExact(senderInfo);
                SeenCmd.onResult(p, int1, s1);
            }
            
            default -> Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, int1, 0, 0, s1, null, null, null, null, null));
                
        }
    }



   
   
   
   private static void onChanelMsg (final Operation action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, String s2, String s3, String s4, String s5, String s6 ) {
        final Player p;// p = Bukkit.getPlayerExact(senderInfo);
        //final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {
            //case BS_lobby:  //данные для табличек, отправляется всем серверам, чьё имя > 4

            case SET_OSTROV_DATA: //при обновлении на острове - только отправка в банжи, и ожидание обновы с банжи
                p = Bukkit.getPlayerExact(senderInfo);
                //op = PM.getOplayer(senderInfo);
                //int1 - enumTag
                //int2, s1 - данные на обнову (в зависимости от d.is_integer)
                PM.updateDataFromBungee(p, int1, int2, s1);
                break;

            case GAME_INFO_TO_OSTROV:
                //senderInfo - сервер, отправивший данные сервер bw01 sg02 и тд + 
                //int1 - state.tag
                //int2 - arena online
                //int3 -
                //s1 - арена
                //s2,s3,s4,s5 - строки
                //s6 - extra
                final Game game = Game.fromServerName(senderInfo);
                if (game!=null) {
                    final GameInfo gi = GM.getGameInfo(game);
                    if (gi!=null) {
                        gi.update(senderInfo, s1, GameState.byTag(int1), int2, s2, s3, s4, s5, s6);
                    } else {
                        Ostrov.log_err("ARENA_INFO_TO_OSTROV GameInfo=null : "+senderInfo);
                    }
                }
                //SM.ArenaInfoFromBungeeHandler(s1);
                break;

            case PARTY_MEMBER_SERVER_SWITCH: //senderInfo-член, s1-лидер, s2-сервер лидера, s3-арена лидера
                Friends.onPartyMemberServerSwitch(senderInfo, s1, s2, s3);
                break;
                
             default:
                Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, int1, int2, int3, s1, s2, s3, s4, s5, s6));
                break;

        }
    }
}
