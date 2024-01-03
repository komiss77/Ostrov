package ru.komiss77.events;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.modules.player.Oplayer;





public class ChatPrepareEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    private final Player sender;
    private final Oplayer senderOp;
    private String msg;
    private Component viewerGameInfo; //инфо, которое увидят получатели
    private Component senderGameInfo; //инфо, которое увидит отправитель (можно подставить другие кликЭвенты, например, создать остров а не пригласить
    private boolean sendProxy = true;
    private boolean showLocal = true;
    private boolean cancel = false;
    
    //список получателей. У кого отправитель в ЧС, уже отфильтрованы.
    //игра может поставить gameInfo и фильтрануть ненужных получателей (например, для островного или кланового чата)
    private final List<Player> viewers;
    
    
    //остальное для передачи в переводчик
    public String senderName, prefix, suffix, playerTooltip;
    public String stripMsgRu, stripMsgEn;
    public boolean banned, muted;
    
    
    
    public ChatPrepareEvent(final Player sender, final Oplayer senderOp, final List<Player> viewers, final String msg) {
        super(true);
        this.sender = sender;
        this.senderOp = senderOp;
        this.viewers = viewers;
        this.msg = msg;
    }

    public Oplayer getOplayer() {
        return senderOp;
    }
    
    public Player getPlayer() {
        return sender;
    }
    
    
    
    
    public Component getViewerGameInfo () {
        return viewerGameInfo;
    }
    
    public void setViewerGameInfo (final Component viewerGameInfo) {
        this.viewerGameInfo = viewerGameInfo;
    }
    
    public Component getSenderGameInfo () {
        return senderGameInfo;
    }
    
    public void setSenderGameInfo (final Component senderGameInfo) {
        this.senderGameInfo = senderGameInfo;
    }
    
    public List<Player>viewers () {
        return viewers;
    }
    
    
    public void showLocal (boolean show) {
        showLocal = show;
    }
    
    public boolean showLocal () {
        return showLocal;
    }
    
    public String getMessage() {
        return msg;
    }
    
    public void setMessage(final String msg) {
        this.msg = msg;
    }
    
    
    public void sendProxy(final boolean send){
        this.sendProxy = send;
    }
    
    public boolean sendProxy(){
        return sendProxy;
    }
    
    
    
    
    
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isCancelled() {
        return cancel;
    }
    
}
