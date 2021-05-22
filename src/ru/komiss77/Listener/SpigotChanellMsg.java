package ru.komiss77.Listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import ru.komiss77.Cfg;
import ru.komiss77.Enums.Action;
import ru.komiss77.Ostrov;
import ru.komiss77.Events.OstrovChanelEvent;




public class SpigotChanellMsg implements PluginMessageListener {



@Override
    public void onPluginMessageReceived(String ch, Player player, byte[] msg) {
//System.out.println("1 >>>>MessageReceived: "+ch);  
        //if (!Chanell.exist(ch)) return;
        //final Chanell chanell = Chanell.fromString(ch);
        //if (chanell==Chanell.Undefined) return;
        if ( !(ch.equalsIgnoreCase(Cfg.chanelName)) ) return;
        
        String from="";
        Action action=Action.NONE;
        String bungee_raw_data="";
        
        try {    
            final ByteArrayDataInput in = ByteStreams.newDataInput(msg); 
            from = in.readUTF();
            action = Action.byTag(in.readUTF());
            bungee_raw_data = in.readUTF();
//System.out.println("2 from="+from+"  action="+action.toString()+" raw="+bungee_raw_data);
            if (action==Action.NONE) return;
            
//System.out.println("3 вызов OstrovChanelEvent from="+from+"  action="+action.toString()+" raw="+bungee_raw_data);
            Bukkit.getPluginManager().callEvent(new OstrovChanelEvent( from, action, bungee_raw_data ));
//System.out.println("");

        }catch (NumberFormatException|NullPointerException|ArrayIndexOutOfBoundsException ex) {
            Ostrov.log_err("onPluginMessage chanel OSTROV error from="+from+" action="+action.toString()+" raw="+bungee_raw_data+" §eex:"+ex.getMessage());
        }
            
    }
  
    
    
    
    public static boolean sendMessage(final String from, final Action action, final String raw ) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return false;
//System.out.println("-SENDMESSAGE: from="+from+" action="+action.toString()+" raw="+raw);    
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(from);
            out.writeUTF(String.valueOf(action.ordinal()));
            out.writeUTF(raw);
            Bukkit.getOnlinePlayers().stream().findAny().get().sendPluginMessage(Ostrov.instance, Cfg.chanelName, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("sendMessage from="+from+" chanell="+Cfg.chanelName+" action="+action+" raw="+raw+" : "+ex.getMessage());
            return false;
        }

    }
    
    public static boolean sendMessage(final Player p, final Action action, final String raw ) {
//System.out.println("-SENDMESSAGE: Player="+p.getName()+" action="+action.toString()+" raw="+raw);    
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(p.getName());
            out.writeUTF(String.valueOf(action.ordinal()));
            out.writeUTF(raw);
            p.sendPluginMessage(Ostrov.instance, Cfg.chanelName, stream.toByteArray());
            return true;
        } catch (IOException | NullPointerException ex) {
            Ostrov.log_err("sendMessage player="+p+" chanell="+Cfg.chanelName+" action="+action+" raw="+raw+" : "+ex.getMessage());
            return false;
        }

    }
    
    
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
