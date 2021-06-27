package ru.komiss77.Listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;
import ru.komiss77.ApiOstrov;

import ru.komiss77.Commands.Seen;
import ru.komiss77.Enums.Action;
import ru.komiss77.Enums.Chanell;
import ru.komiss77.Enums.Game.GameType;
import ru.komiss77.Enums.GameState;
import ru.komiss77.Ostrov;
import ru.komiss77.Events.OstrovChanelEvent;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.SM;
import ru.komiss77.Managers.StatManager;
import ru.komiss77.Objects.Oplayer;
import ru.komiss77.Objects.GameInfo;




public class SpigotChanellMsg implements Listener, PluginMessageListener {


    //SPIGOT!!! 
    
    
    
      
    public static boolean sendMessage(final Player msgTransport, final Action action) {
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

    public static boolean sendMessage(final Player msgTransport, final Action action, final String senderInfo) {
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

    public static boolean sendMessage(final Player msgTransport, final Action action, final String senderInfo, final int int1) {
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

    public static boolean sendMessage(final Player msgTransport, final Action action, final String senderInfo, final String s1) {
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

    public static boolean sendMessage(final Player msgTransport, final Action action, final String senderInfo, final int int1, final String s1) {
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

    public static boolean sendMessage(final Player msgTransport, final Action action, final String senderInfo, final int int1, final int int2, final String s1, final String s2) {
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

    public static boolean sendMessage(final Player msgTransport, final Action action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, final String s2, final String s3) {
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

    public static boolean sendMessage(final Player msgTransport, final Action action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, final String s2, final String s3, final String s4, final String s5, final String s6) {
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
//System.out.println("1 >>>>MessageReceived: "+ch);  
        final Chanell ch = Chanell.fromName(chanelName);
        if (ch == null) {
            Ostrov.log_err("onPluginMessage Chanell=null : "+chanelName);
            return; 
        }
        
        try { 
            final ByteArrayDataInput in = ByteStreams.newDataInput(msg); 
            final Action action = Action.byTag(in.readInt());
            
            if (action==null) {
                Ostrov.log_err("onPluginMessage Action=NONE");
                return; 
            }
            
            switch (ch) {
                
                case Action:
                    onChanelMsg(action);
                    break;
                    
                case Action_Sender:
                    onChanelMsg(action, in.readUTF());
                    break;
                    
                case Action_Sender_Int:
                    onChanelMsg(action, in.readUTF(), in.readInt());
                    break;
                    
                case Action_Sender_String:
                    onChanelMsg(action, in.readUTF(), in.readUTF());
                    break;
                    
                case Action_Sender_Int_String:
                    onChanelMsg(action, in.readUTF(), in.readInt(), in.readUTF());
                    break;
                    
                case Action_Sender_Int2_String2:
                    onChanelMsg(action, in.readUTF(), in.readInt(), in.readInt(), 0, in.readUTF(), in.readUTF(), null, null, null, null);
                    break;
                    
                case Action_Sender_Int3_String3:
                    onChanelMsg(action, in.readUTF(), in.readInt(), in.readInt(), in.readInt(), in.readUTF(), in.readUTF(), in.readUTF(), null, null, null);
                    break;
                    
                case Action_Sender_Int3_String6:
                    onChanelMsg(action, in.readUTF(), in.readInt(), in.readInt(), in.readInt(), in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF());
                    break;
                    
            }

//System.out.println("3 вызов OstrovChanelEvent from="+from+"  action="+action.toString()+" raw="+bungee_raw_data);

        } catch (NumberFormatException|NullPointerException|ArrayIndexOutOfBoundsException ex) {
            Ostrov.log_err("onPluginMessage chanel OSTROV readbuff error chanelName="+chanelName+" : "+ ex.getMessage());
        }
            
    }
  
    
    



    private static void onChanelMsg(final Action action) {
        switch (action) {

                
                
        }
    }


   
    private static void onChanelMsg(final Action action, final String senderInfo) {
        final Player p;// p = Bukkit.getPlayer(senderInfo);
        final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {
                
            case RESET_DAYLY_STAT:
                p = Bukkit.getPlayer(senderInfo);
                op = PM.getOplayer(senderInfo);
                op.resetDaylyStat();
                ApiOstrov.sendBossbar(p, "§cДневная статистика сброшена!", 12, BarColor.RED, BarStyle.SEGMENTED_12, true);
                return;
                
            default:
                Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, 0, 0, 0, null, null, null, null, null, null));
                break;
                
        }
    }


    private static void onChanelMsg(final Action action, final String senderInfo, final int int1) {
        final Player p;// p = Bukkit.getPlayer(senderInfo);
        final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {

            case GONLINE:
                SM.bungee_online=int1;
                break;
                
            case ADD_REPUTATION: //приходит от RewardHandler
                op = PM.getOplayer(senderInfo);
                StatManager.reputationChange(op, int1);
                break;

            case ADD_EXP: //приходит от RewardHandler
                op = PM.getOplayer(senderInfo);
                StatManager.addXP(op, int1);
                break;


                
             default:
                Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, int1, 0, 0, null, null, null, null, null, null));
                break;
                
        }
    }

   
   
   
    private static void onChanelMsg(final Action action, final String senderInfo, final String s1) {
        final Player p;// p = Bukkit.getPlayer(senderInfo);
        final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {

            case OSTROV_RAW_DATA:
                p = Bukkit.getPlayer(senderInfo);
                op = PM.getOplayer(senderInfo);
                op.bungeeDataInject(p, s1);
                break;

            case EXECUTE_OSTROV_CMD:
                p = Bukkit.getPlayer(senderInfo);
                p.performCommand(s1);
                break;
                
             default:
                Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, 0, 0, 0, s1, null, null, null, null, null));
                break;
                
        }
    }

   private static void onChanelMsg(final Action action, final String senderInfo, final int int1, final String s1) {
        final Player p;// p = Bukkit.getPlayer(senderInfo);
        final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {

            case PLAYER_DATA_REQUEST_RESULT:
                p = Bukkit.getPlayer(senderInfo);
                Seen.onResult(p, int1, s1);
                break;

                
             default:
                Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, int1, 0, 0, s1, null, null, null, null, null));
                break;
                
                
        }
    }



   
   
   
   private static void onChanelMsg (final Action action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, String s2, String s3, String s4, String s5, String s6 ) {
//System.out.println(" -- OstrovChanelEvent "+e.from+" "+e.action+" "+e.bungee_raw_data);
        final Player p;// p = Bukkit.getPlayer(senderInfo);
        final Oplayer op;// op = PM.getOplayer(senderInfo);
        switch (action) {
            //case BS_lobby:  //данные для табличек, отправляется всем серверам, чьё имя > 4

            case SET_OSTROV_DATA: //при обновлении на острове - только отправка в банжи, и ожидание обновы с банжи
                p = Bukkit.getPlayer(senderInfo);
                op = PM.getOplayer(senderInfo);
                op.updateDataFromBungee(p, int1, int2, s1);
                break;

            case GAME_INFO_TO_OSTROV:
                //senderInfo - сервер, отправивший данные сервер bw01 sg02 и тд + 
                //int1 - state.tag
                //int2 - arena online
                //int3 -
                //s1 - арена
                //s2,s3,s4,s5 - строки
                //s6 - extra
                final GameInfo gi = SM.getGameInfo(senderInfo);
                if (gi!=null) {
                    if (gi.game.type==GameType.SINGLE) {
                        gi.updateSingle(GameState.byTag(int1), int2);
                    } else {
                        gi.updateArena(senderInfo, s1, GameState.byTag(int1), int2, s2, s3, s4, s5, s6);
                    }
                } else {
                    Ostrov.log_err("ARENA_INFO_TO_OSTROV GameInfo=null : "+senderInfo);
                }
                //SM.ArenaInfoFromBungeeHandler(s1);
                break;

                
             default:
                Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( action, senderInfo, int1, int2, int3, s1, s2, s3, s4, s5, s6));
                break;

        }
    }
    
    //от имени любого игрока
    //public static boolean sendMessage(final Action action, final int value1, final int value2, final String s1, String s2) {
    //    if (Bukkit.getOnlinePlayers().isEmpty()) return false;
     //   return sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(), action, value1, value2, s1, s2);
//System.out.println("-SENDMESSAGE: from="+from+" action="+action.toString()+" raw="+raw);    
       // final ByteArrayOutputStream stream = new ByteArrayOutputStream();
       // final DataOutputStream out = new DataOutputStream(stream);

      //  try {
       //     out.writeInt(action.tag);
       //     out.writeUTF(from);
       //     out.writeUTF(raw);
       //     Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(Ostrov.instance, Cfg.chanelName, stream.toByteArray());
       //     return true;
   //     } catch (IOException | NullPointerException ex) {
    //        Ostrov.log_err("sendMessage from="+from+" chanell="+Cfg.chanelName+" action="+action+" raw="+raw+" : "+ex.getMessage());
      //      return false;
      //  }

   // }
    

    
    /*
    @Deprecated
 public static void SendToServer(final Player player, final String server, String arena) {

            if (arena.isEmpty()) arena="any";
            try {
                 ByteArrayOutputStream b = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(b);
                 out.writeUTF( player.getName() );
                 out.writeUTF(String.valueOf(Action.OSTROV_SEND_TO_ARENA.tag) );
                 out.writeUTF( server+"<:>"+arena );
                 player.sendPluginMessage( Ostrov.instance, Chanell.OSTROV.toString(), b.toByteArray() ); 
//System.out.println("!!SendToServer() "+player.getName()+" "+Action.OSTROV_SEND_TO_ARENA.toString()+" "+server+"<:>"+arena);
             } catch (IOException ex) {
                 Ostrov.log_err(Chanell.OSTROV.toString()+" : "+ex.getMessage());
             }

        
}  */
    
  
    
    


    
}
