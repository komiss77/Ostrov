package ru.komiss77.events;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;
import ru.komiss77.listener.ChatLst;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.TimeUtil;


public class ChatPrepareEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Oplayer senderOp;
    private String msg;
    private String topData;
    private String proxyInfo;
    private String profileTip;
    private Component viewerGameInfo; //инфо, которое увидят получатели
    private Component senderGameInfo; //инфо, которое увидит отправитель (можно подставить другие кликЭвенты, например, создать остров а не пригласить
    private boolean sendProxy = true, showLocal = true, showSelf = true;
    private boolean cancel = false;

    //список получателей. У кого отправитель в ЧС, уже отфильтрованы.
    //игра может поставить gameInfo и фильтрануть ненужных получателей (например, для островного или кланового чата)
    private final List<Player> viewers;

    public boolean banned, muted;

    //остальное для передачи в переводчик
    @Deprecated
    public String senderName, prefix, suffix, playerTooltip;
    @Deprecated
    public String strMsgRu, strMsgEn;


    public ChatPrepareEvent(final Player sender, final Oplayer senderOp, final List<Player> viewers, final String msg) {
        super(sender, true);
        this.senderOp = senderOp;
        this.viewers = viewers;
        final String[] sm = ChatLst.TOP_SPLIT.split(msg, true);
        this.topData = sm.length == 1 ? null : sm[0];
        this.msg = sm[sm.length - 1];

        final StringBuilder sb = new StringBuilder();

        if (senderOp.isGuest) {
            if (senderOp.eng) {
                sb.append("§6Player is in §eGuet Mode§6!")
                    .append("\n§6Player data is not saved!")
                    .append("\n§3Server: §a").append(Ostrov.MOT_D)
                    .append((muted ? "\n§4Muted: §cYes" : ""))
                    .append("\n<gray>Click - <gold>direct message");
            } else {
                sb.append("§6Игрок в §eГостевом режиме§6!")
                    .append("\n§6Игровые данные не сохраняются!")
                    .append("\n§3Сервер: §a").append(Ostrov.MOT_D)
                    .append((muted ? "\n§4Молчанка: §cДа" : ""))
                    .append("\n<gray>Клик - <gold>личное сообщение");
            }
        } else {
            if (senderOp.eng) {
                sb.append("§3Server: §a").append(Ostrov.MOT_D)
                    .append("\n<amber>Social status: ").append(PM.getStatus(senderOp))
                    .append("\n<stale>Groups: §f").append(senderOp.chat_group)
                    .append("\n<indigo>Badges: ") //TODO баджики
                    .append(PM.getGenderDisplay(senderOp)).append("\n")
                    .append("\n§6Play time: §e").append(TimeUtil.secondToTime(senderOp.getStat(Stat.PLAY_TIME)))
                    .append((muted ? "\n§4Muted: §cYes" : "\n"))
                    .append("\n<gray>Click - <gold>direct message");
            } else {
                //TODO баджики
                sb.append("§3Сервер: §a").append(Ostrov.MOT_D)
                    .append("\n<amber>Соц. статус: ").append(PM.getStatus(senderOp))
                    .append("\n<stale>Группы: §f").append(senderOp.chat_group)
                    .append("\n<indigo>Баджики: §9").append("\n")
                    .append(PM.getGenderDisplay(senderOp))
                    .append("\n§6Время игры: §e").append(TimeUtil.secondToTime(senderOp.getStat(Stat.PLAY_TIME)))
                    .append((muted ? "\n§4Молчанка: §cДа" : "\n"))
                    .append("\n<gray>Клик - <gold>личное сообщение");
            }
        }
        this.profileTip = sb.toString();
    }

    public Oplayer getOplayer() {
        return senderOp;
    }

    public String topData() {
        return topData;
    }

    public void topData(final String topData) {
        this.topData = topData;
    }

    public String proxyInfo() {
        return proxyInfo;
    }

    public void proxyInfo(final String proxyInfo) {
        this.proxyInfo = proxyInfo;
    }

    public String profileTip() {
        return profileTip;
    }

    public void profileTip(final String profileTip) {
        this.profileTip = profileTip;
    }

    public Component getViewerGameInfo() {
        return viewerGameInfo;
    }

    public void setViewerGameInfo(final Component viewerGameInfo) {
        this.viewerGameInfo = viewerGameInfo;
    }

    public Component getSenderGameInfo() {
        return senderGameInfo;
    }

    public void setSenderGameInfo(final Component senderGameInfo) {
        this.senderGameInfo = senderGameInfo;
    }

    public List<Player> viewers() {
        return viewers;
    }


    public void showLocal(boolean show) {
        showLocal = show;
    }

    public boolean showLocal() {
        return showLocal;
    }

    public void showSelf(boolean show) {
        showSelf = show;
    }

    public boolean showSelf() {
        return showSelf;
    }

    public String getMessage() {
        return msg;
    }

    public void setMessage(final String msg) {
        this.msg = msg;
    }


    public void sendProxy(final boolean send) {
        this.sendProxy = send;
    }

    public boolean sendProxy() {
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
