package ru.komiss77.bungee;


import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

import ru.komiss77.Enums.Action;




/**
 * 
 */
@Deprecated
public class OstrovBungeeChanelEvent extends Event
{

   //public final MsgSenderType senderType;
    public final String from;
    public final Action action;
    public String bungee_raw_data;

    public OstrovBungeeChanelEvent( String from, Action action, String bungee_raw_data)
    {
        //this.senderType=senderType;
        this.from=from;
        this.action = action;
        this.bungee_raw_data = bungee_raw_data;
    }


    public boolean isPlayer() {
        return getPlayer()!=null;
    }

    public ProxiedPlayer getPlayer() {
        return ProxyServer.getInstance().getPlayer(from);
    }





}
