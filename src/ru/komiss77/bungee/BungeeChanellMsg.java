package ru.komiss77.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import ru.komiss77.Enums.Action;






public class BungeeChanellMsg implements Listener {

            //BUNGEE !!!
    
    
@EventHandler
    public static void onPluginMessage(PluginMessageEvent e) {
        
//System.out.println("1 onPluginMessage 111 "+e.getTag());

    if ( !(e.getSender() instanceof Server) || !(e.getTag().equalsIgnoreCase(OstrovBungee.chanelName)))   return;

        String from="";
        Action action = Action.NONE;
        String spigot_raw_message="";
                
        ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
        DataInputStream in = new DataInputStream(stream);
        
        //final Server s = (Server) e.getSender();
        
        try {
            from = in.readUTF();
            action = Action.byTag(in.readUTF());
            spigot_raw_message = in.readUTF();
//System.out.println("2 from="+from+"  action="+action.toString()+" raw="+spigot_raw_message);
        } catch (IOException ex) {
            OstrovBungee.log_err("onPluginMessage chanel OSTROV readbuff error from="+from+" action="+action.toString()+" raw="+spigot_raw_message+" : "+ ex.getMessage());
        }        
            if (action==Action.NONE ) return;
        
//System.out.println("3 вызов OstrovBungeeChanelEvent from="+from+"  action="+action.toString()+" raw="+spigot_raw_message);
        OstrovBungee.getInstance().getProxy().getPluginManager().callEvent(new OstrovBungeeChanelEvent(from, action, spigot_raw_message));
//System.out.println("");
        
    }


    /*
    public static boolean sendBungeeMessageToServer(final String target_server_name, final String sender, final Action action, final String message) {
System.out.println("sendBungeeMessageToServer 1 target_server_name="+target_server_name+" "+action);
        if (OstrovBungee.getInstance().getProxy().getServers().containsKey(target_server_name)) {
System.out.println("sendBungeeMessageToServer 2 players="+OstrovBungee.getInstance().getProxy().getServers().get(target_server_name).getPlayers());
            if (!OstrovBungee.getInstance().getProxy().getServers().get(target_server_name).getPlayers().isEmpty()) {
System.out.println(" sendBungeeMessageToServer "+target_server_name+" action="+action+" raw="+message);
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF (sender);
                out.writeUTF(String.valueOf(action.tag));
                out.writeUTF ( message );

                OstrovBungee.getInstance().getProxy().getServers().get(target_server_name).sendData (Ostrov.chanelName, out.toByteArray());
            }
        }
        return true;
    }*/
    
    public static boolean sendBungeeMessage(final ProxiedPlayer pp, final Action action, final String message) {
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //DataOutputStream out = new DataOutputStream(stream);
        if (pp!=null) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        //try {
            out.writeUTF(pp.getName());
            out.writeUTF(String.valueOf(action.tag));
            out.writeUTF(message);
            //pp.getServer().getInfo().sendData(Ostrov.chanelName, stream.toByteArray());
            pp.getServer().getInfo().sendData(OstrovBungee.chanelName, out.toByteArray());
//System.out.println("sendBungeeMessage pp="+pp.getName()+" action="+action.toString()+" msg="+message);
//System.out.println("");
            return true;

        } else {
            OstrovBungee.log_err("Не удалось sendBungeeMessage : ProxiedPlayer pp=null");
            return false;
        }
        //} catch (IOException ex) {
           // MainB.log_err("sendMessage : "+ex.getMessage());
            //return false;
       // }
        
    }





}
