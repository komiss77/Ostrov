package ru.komiss77.listener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.komiss77.ApiOstrov;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.*;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.netty.QueryCode;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.modules.netty.OsQuery;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.InputButton;


//https://docs.advntr.dev/serializer/gson.html

//SpigotChanellMsg.sendChat(ce.getPlayer(), gsonMsg); - отправлять русск и англ сообщ
//не показ чат на англ!

public class ChatLst implements Listener {

    private static final boolean TRANSLATE_CHAT = false;

    public static final String NIK_COLOR;
    //    private static final TextColor NIK_COLOR;
    private static final TextColor MSG_COLOR;
    private static final Component SUGGEST_MUTE_TOOLTIP_RU;
    //private static final TextComponent SUGGEST_BLACKLIST_TOOLTIP_RU;
    private static final Component PREFIX_TOOLTIP_RU;
    private static final Component SUFFIX_TOOLTIP_RU;
    private static final Component SUGGEST_MUTE_TOOLTIP_EN;
    //private static final TextComponent SUGGEST_BLACKLIST_TOOLTIP_EN;
    private static final Component PREFIX_TOOLTIP_EN;
    private static final Component SUFFIX_TOOLTIP_EN;
    private static final ClickEvent DONATE_CLICK_URL;
    private static final Component URL_TOOLTIP_RU;
    private static final Component URL_TOOLTIP_EN;

    static {
        NIK_COLOR = switch (Ostrov.calendar.get(Calendar.MONTH)) {
            case 11, 0, 1 -> "<gradient:sky:blue>";
            case 2, 3, 4 -> "<gradient:pink:green>";
            case 8, 9, 10 -> "<gradient:beige:gold>";
            default -> "<gradient:apple:dark_aqua>";
        };
        MSG_COLOR = NamedTextColor.GRAY;
        SUGGEST_MUTE_TOOLTIP_RU = TCUtil.form("§кКлик - выдать молчанку");
        //SUGGEST_BLACKLIST_TOOLTIP_RU = TCUtils.format("§кКлик - кинуть в ЧС");
        PREFIX_TOOLTIP_RU = TCUtil.form("§7-=[§я✦§7]=-  §оХочешь префикс? Жми!!!  §7-=[§я✦§7]=-");
        SUFFIX_TOOLTIP_RU = TCUtil.form("§7-=[§я✦§7]=-  §сХочешь суффикс? Жми!!!  §7-=[§я✦§7]=-");
        URL_TOOLTIP_RU = TCUtil.form("§5Клик-перейти по ссылке");
        URL_TOOLTIP_EN = TCUtil.form("§5Click - open URL");
        SUGGEST_MUTE_TOOLTIP_EN = TCUtil.form("§кClick - mute player");
        //SUGGEST_BLACKLIST_TOOLTIP_EN = TCUtils.format("§кClick- add to blackList");
        PREFIX_TOOLTIP_EN = TCUtil.form("§7-=[§я✦§7]=-  §оDo you want prefix? Click here!!!  §7-=[§я✦§7]=-");
        SUFFIX_TOOLTIP_EN = TCUtil.form("§7-=[§я✦§7]=-  §сDo you want suffix? Click here!!!  §7-=[§я✦§7]=-");
        DONATE_CLICK_URL = ClickEvent.openUrl("http://www.ostrov77.ru/donate.html");
    }


    // на HIGHEST проверяется мут и формируется сообщение для прокси.
    // локальной рассылкой с проверкой ЧС занимается каждая игра самостоятельно на приоритете ниже
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncChatEvent e) {

        final Player sender = e.getPlayer();
        final Oplayer senderOp = PM.getOplayer(sender);

        //режим ввода из чата
        if (senderOp != null && PlayerInput.inputData.containsKey(sender) && PlayerInput.inputData.get(sender).type == InputButton.InputType.CHAT) {
            e.viewers().clear();
            //PlayerInput.onInput(sender.getName(), InputButton.InputType.CHAT, TCUtils.toString(e.message()));
            //Could not pass event AsyncChatEvent to Ostrov v2.0 java.lang.IllegalStateException: InventoryOpenEvent may only be triggered synchronously.
            Ostrov.sync(() -> PlayerInput.onInput(sender, InputButton.InputType.CHAT, TCUtil.deform(e.message()).replace("\\", "")));//цвета вписаные в чат не юзались
            return;
        }

        if (senderOp == null) return; //пока так, просто не обрабатываем - или баганёт в авторизации

        // проверка молчанки на банжике тоже не будет рассылки
        final boolean muted = senderOp.getDataInt(Data.MUTE_TO) > Timer.secTime();
        if (muted) {
            sender.sendMessage(Component.text("Чат ограничен - сообщения видят только друзья", NamedTextColor.RED)
                    .hoverEvent(HoverEvent.showText(TCUtil.form("§cУ Вас молчанка от §6" + senderOp.getDataString(Data.MUTE_BY)
                            + " §cза §b" + senderOp.getDataString(Data.MUTE_REAS) + "§c, осталось: §e" +
                            TimeUtil.secondToTime(senderOp.getDataInt(Data.MUTE_TO) - Timer.secTime()))))
                    .clickEvent(ClickEvent.openUrl("https://discord.com/channels/646762127335489540/679455910283706388")));
            // sender.sendMessage(TCUtils.format("§cУ Вас молчанка от §6"+senderOp.getDataString(Data.MUTE_BY)
            // 	+" §cза §b"+senderOp.getDataString(Data.MUTE_REAS)+"§c, осталось: §e"+
            //	TimeUtil.secondToTime(senderOp.getDataInt(Data.MUTE_TO)-Timer.getTime())));
            //e.viewers().clear();
            //for (String friendName : senderOp.friends) {
            //    ApiOstrov.executeBungeeCmd(sender, "friend mail "+friendName+" "+msg);
            //}
            //return;
        }
        final boolean banned = senderOp.getDataInt(Data.BAN_TO) > Timer.secTime();
        if (banned) {
            sender.sendMessage(Component.text("Чат ограничен чистилищем - вы забанены", NamedTextColor.RED)
                    .hoverEvent(HoverEvent.showText(TCUtil.form("§cУ Вас бан от §6" + senderOp.getDataString(Data.BAN_BY)
                            + " §cза §b" + senderOp.getDataString(Data.BAN_REAS) + "§c, осталось: §e" +
                            TimeUtil.secondToTime(senderOp.getDataInt(Data.BAN_TO) - Timer.secTime()))))
                    .clickEvent(ClickEvent.openUrl("https://discord.com/channels/646762127335489540/679455910283706388")));
            return;
        }

        //если у игрока в настройках локальны чат - точно не отправляем
        //if (op.isLocalChat()) return; - локально рассылаем ниже по дефолту!
        final String senderName = sender.getName();

        //создадим свой список получателей. У кого отправитель в ЧС, будут отфильтрованы
        final List<Player> list = new ArrayList<>();

        Oplayer oplayerTo;
        boolean hasRemoved = false;
        final Iterator<Audience> it = e.viewers().iterator();
        while (it.hasNext()) {
            if (it.next() instanceof Player playerTo) {
                if (playerTo.getName().equals(senderName))
                    continue; //себя не надо в спислк - в конце отправляется безусловно
                oplayerTo = PM.getOplayer(playerTo);
                if (oplayerTo != null) {
                    if (oplayerTo.isBlackListed(senderName)) {
                        it.remove();
                        hasRemoved = true;
                    } else if (muted && !senderOp.friends.contains(oplayerTo.nik)) { //замучен и получатель не друг
                        it.remove();
                    } else {
                        list.add(playerTo);
                    }
                } else {
                    list.add(playerTo);
                }
            }
        }

        //кинуть эфент - игра может добавить своё инфо или отменить отправку на прокси, если, например, не в лобби игры
        //игра может поставить gameInfo и фильтрануть ненужных получателей (например, для островного или кланового чата)
        String stripMsg = TCUtil.deform(e.message());

        final ChatPrepareEvent ce = new ChatPrepareEvent(sender, senderOp, list, stripMsg);
        Bukkit.getPluginManager().callEvent(ce);

        if (ce.isCancelled()) {
            e.viewers().clear();
            return;
        }
        if (!ce.sendProxy() && !ce.showLocal()) return; //игра отменила все отправки - значит рассылает сама

        if (ce.getMessage() != null) {
            stripMsg = ce.getMessage();
        }

        if (ce.showLocal()) { //игра оставила работать дальше по умолчанию - офф стандартный чат
            e.viewers().clear();
        }

        //вкинуть всё нужное в эвент для возможности перевода
        //данные примерно в порядке нужности
        //лого сервера - готовый компонент
        //инфо игры - готовый компонент
        ce.senderName = senderName;
        ce.banned = false;
        ce.muted = muted;
        ce.prefix = senderOp.getDataString(Data.PREFIX) + " ";

        StringBuilder sb = new StringBuilder();

        if (senderOp.isGuest) {

            if (senderOp.eng) {
                sb.append("§6Player is in §eGuet Mode§6!")
                        .append("\n§6Player data do not save!")
                        .append("\n§5Write at: §f").append(TimeUtil.getCurrentHourMin())
                        .append((muted ? "\n§4Молчанка: §cДА" : ""))
                        .append("\n§5Server: §a").append(Ostrov.MOT_D)
                        .append("\n§fClick - write a message");
            } else {
                sb.append("§6Игрок в §eГостевом режиме§6!")
                        .append("\n§6Игровые данные не сохраняются!")
                        .append("\n§5Написано: §f").append(TimeUtil.getCurrentHourMin())
                        .append((muted ? "\n§4Молчанка: §cДА" : ""))
                        .append("\n§5Сервер: §a").append(Ostrov.MOT_D)
                        .append("\n§fКлик - написать сообщение");
            }
            ce.suffix = "";
        } else {

            if (senderOp.eng) {
                sb.append(PM.getGenderDisplay(senderOp))
                        .append("\n§5Write at: §f").append(TimeUtil.getCurrentHourMin())
                        .append((muted ? "\n§4Muted: §cДА" : ""))
                        .append("\n§5Server: §a").append(Ostrov.MOT_D)
                        .append("\n§5Social status: ").append(getStatus(senderOp))
                        .append("\n§5Groups: §f").append(senderOp.chat_group)
                        .append("\n§5Totatl ply time: ").append(TimeUtil.secondToTime(senderOp.getStat(Stat.PLAY_TIME)))
                        .append("\n§fClick - write a message");
            } else {
                sb.append(PM.getGenderDisplay(senderOp))
                        .append("\n§5Написано: §f").append(TimeUtil.getCurrentHourMin())
                        .append((muted ? "\n§4Молчанка: §cДА" : ""))
                        .append("\n§5Сервер: §a").append(Ostrov.MOT_D)
                        .append("\n§5Социальный статус: ").append(getStatus(senderOp))
                        .append("\n§5Группы: §f").append(senderOp.chat_group)
                        .append("\n§5Игровое время: ").append(TimeUtil.secondToTime(senderOp.getStat(Stat.PLAY_TIME)))
                        .append("\n§fКлик - написать сообщение");
            }
            ce.suffix = " " + senderOp.getDataString(Data.SUFFIX);
        }
        ce.playerTooltip = sb.toString();


        //в переводчике будет дополнено поле оппозитного перевода обязательно!
        if (TRANSLATE_CHAT) {
            if (!senderOp.eng) {
                ce.stripMsgRu = stripMsg; //переводов не требуется
            } else {
                ce.stripMsgEn = stripMsg;  //отправитель не русскоязычный
            }
            Lang.translateChat(ce);
        } else {
            ce.stripMsgRu = stripMsg;
            ce.stripMsgEn = stripMsg;
            process(ce);
        }

        if (hasRemoved) {
            ScreenUtil.sendActionBarDirect(sender, "§6Некоторые игроки не увидели ваши сообщения - вы в ЧС");
        }
        OsQuery.send(QueryCode.CHAT_STRIP, senderName + LocalDB.WORD_SPLIT + stripMsg);
    }


    public static void process(final ChatPrepareEvent ce) {

        final boolean useColorCode = Perm.canColorChat(ce.getOplayer());

        final Component msgRU = TCUtil.form(useColorCode ?
                ce.stripMsgRu.replace('&', '§') : //сообщение от билдеров возможно с цветами
                ce.stripMsgRu);

        final Component msgEN = TCUtil.form(useColorCode ?
                ce.stripMsgEn.replace('&', '§') : //сообщение от билдеров возможно с цветами
                ce.stripMsgEn);

        //билдим итоговый компонент
        final TextComponent.Builder bRU = Component.text();
        final TextComponent.Builder bEN = Component.text();

        //префикс
        if (!ce.prefix.isEmpty()) {
            bRU.append(TCUtil.form(ce.prefix)
                    .hoverEvent(HoverEvent.showText(PREFIX_TOOLTIP_RU))
                    .clickEvent(DONATE_CLICK_URL)
            );
            bEN.append(TCUtil.form(ce.prefix)
                    .hoverEvent(HoverEvent.showText(PREFIX_TOOLTIP_EN))
                    .clickEvent(DONATE_CLICK_URL)
            );
        }

        //ник игрока
        if (ce.getOplayer().isGuest) {
            bRU.append(TCUtil.form(NIK_COLOR + ce.getOplayer().getDataString(Data.FAMILY))
                    .hoverEvent(HoverEvent.showText(TCUtil.form(ce.playerTooltip)))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + ce.senderName))
            );

            bEN.append(TCUtil.form(NIK_COLOR + ce.senderName)
                    .hoverEvent(HoverEvent.showText(TCUtil.form(ce.playerTooltip)))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + ce.senderName))
            );
        } else {
            bRU.append(TCUtil.form(NIK_COLOR + ce.senderName)
                    .hoverEvent(HoverEvent.showText(TCUtil.form(ce.playerTooltip)))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + ce.senderName))
            );

            bEN.append(TCUtil.form(NIK_COLOR + ce.senderName)
                    .hoverEvent(HoverEvent.showText(TCUtil.form(ce.playerTooltip)))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + ce.senderName))
            );
        }

        //суффикс
        if (!ce.suffix.isEmpty()) {
            bRU.append(TCUtil.form(ce.suffix)
                    .hoverEvent(HoverEvent.showText(SUFFIX_TOOLTIP_RU))
                    .clickEvent(DONATE_CLICK_URL)
            );
            bEN.append(TCUtil.form(ce.suffix)
                    .hoverEvent(HoverEvent.showText(SUFFIX_TOOLTIP_EN))
                    .clickEvent(DONATE_CLICK_URL)
            );
        }

        //стрелочки
        bRU.append(Component.text(" ≫ ", MSG_COLOR, TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showText(SUGGEST_MUTE_TOOLTIP_RU))
                .clickEvent(ClickEvent.suggestCommand("/tempmute " + ce.senderName + " 10m "))
        );
        bEN.append(Component.text(" ≫ ", MSG_COLOR, TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showText(SUGGEST_MUTE_TOOLTIP_EN))
                .clickEvent(ClickEvent.suggestCommand("/tempmute " + ce.senderName + " 10m "))
        );

        //сообщение
        if (ce.stripMsgRu.startsWith("http") && (ce.getOplayer().isStaff || ApiOstrov.canBeBuilder(ce.getPlayer()))) {
//Ostrov.log_warn("http!");
            bRU.append(msgRU.color(MSG_COLOR)
                .hoverEvent(HoverEvent.showText(URL_TOOLTIP_RU))
                .clickEvent(ClickEvent.openUrl(ce.stripMsgRu))
            );
            bEN.append(msgEN.color(MSG_COLOR)
                .hoverEvent(HoverEvent.showText(URL_TOOLTIP_EN))
                .clickEvent(ClickEvent.openUrl(ce.stripMsgRu))
            );

        } else {
            bRU.append(msgRU.color(MSG_COLOR)
                .hoverEvent(HoverEvent.showText(TCUtil.form("§7to english: \n§f" + ce.stripMsgEn + "\n§7click - §cignore")))
                .clickEvent(ClickEvent.suggestCommand("/ignore add " + ce.senderName))
            );
            bEN.append(msgEN.color(MSG_COLOR)
                .hoverEvent(HoverEvent.showText(TCUtil.form("§7по русски: \n§f" + ce.stripMsgRu + "\n§7Клик - §cв игнор")))
                .clickEvent(ClickEvent.suggestCommand("/ignore add " + ce.senderName))
            );
        }



        /*if (ce.getViewerGameInfo()!=null) {
            viewerResult = ce.getViewerGameInfo().append(b.build());
        } else {
            viewerResult = b.build();
        }*/

        final ServerType serverType = GM.GAME.type;
        final String senderWorldName = ce.getPlayer().getWorld().getName();

//Ostrov.log_warn("sendProxy?"+ce.sendProxy()+" isLocalChat?"+senderOp.isLocalChat());
        //игра не отменила отправку на прокси - работаем по дефолту:
        //на всех кроме миниигр отправляем,
        //а на минииграх отправляем если в мире лобби
        if (!ce.banned && !ce.muted && ce.sendProxy() && !ce.getOplayer().isLocalChat()) {
            //if (serverType!=ServerType.ARENAS || senderWorldName.equals("lobby")) {
            final Component proxyResultRU;
            final Component proxyResultEN;
            //убрать лишние элементы, пускай ГЛОБАЛЬНЫЕ сообщения будут всегда в формате: [Значек]_<Префикс>_Имя_<Суффикс>_»_(cообщение)
            //if (ce.getViewerGameInfo()!=null) {
            //    proxyResultRU = GM.getLogo().append(ce.getViewerGameInfo()).append(bRU.build());//proxyResult = GM.getLogo().append(ce.getViewerGameInfo()).append(b.build());
            //    proxyResultEN = GM.getLogo().append(ce.getViewerGameInfo()).append(bEN.build());//proxyResult = GM.getLogo().append(ce.getViewerGameInfo()).append(b.build());
            //} else {
            proxyResultRU = GM.getLogo().append(bRU.build());//proxyResult = GM.getLogo().append(b.build());
            proxyResultEN = GM.getLogo().append(bEN.build());//proxyResult = GM.getLogo().append(b.build());
            //}
//sender.sendMessage("");sender.sendMessage(" -- на прокси уйдёт такое сообщение: ");sender.sendMessage(proxyResult);sender.sendMessage("");
            final String gsonMsgRU = GsonComponentSerializer.gson().serialize(proxyResultRU);
            final String gsonMsgEN = GsonComponentSerializer.gson().serialize(proxyResultEN);
//SpigotChanellMsg.sendChat(ce.getPlayer(), gsonMsgRU, Chanell.CHAT);
            SpigotChanellMsg.sendChat(ce.getPlayer(), gsonMsgRU, Chanell.CHAT_RU);
            SpigotChanellMsg.sendChat(ce.getPlayer(), gsonMsgEN, Chanell.CHAT_EN);
            OsQuery.send(QueryCode.CHAT_RU, ce.getPlayer().getName() + LocalDB.WORD_SPLIT + gsonMsgRU);
            OsQuery.send(QueryCode.CHAT_EN, ce.getPlayer().getName() + LocalDB.WORD_SPLIT + gsonMsgEN);
            // }
        }


        //если игра не отменила показ на локальном сервере, рассылаем по умолчанию
        //по умолчанию:
        //на сервере миниигры - в мире lobby обычный прокси чат, в ГМ3 - чат зрителя, остальыне локальный с простым форматом
        //на больших cерверах простой глобальный

        final Component resultRU;
        final Component viewerResultRU;
        final Component resultEN;
        final Component viewerResultEN;

        if (ce.showLocal()) {

            //на миниигре подменяем сообщение, если отправитель зритель или в игре
            if (serverType == ServerType.ARENAS && !ApiOstrov.isLocalBuilder(ce.getPlayer())) {

                if (ce.getPlayer().getGameMode() == GameMode.SPECTATOR) { //отправитель в ГМ3 - зритель

                    resultRU = TCUtil.form("§8[Зритель] " + ce.senderName + " §7§o≫ §7")
                            .hoverEvent(HoverEvent.showText(TCUtil.form("§кКлик - кинуть в ЧС")))
                            .clickEvent(ClickEvent.suggestCommand("/ignore add " + ce.senderName))
                            .append(msgRU);

                    resultEN = TCUtil.form("§8[Spectator] " + ce.senderName + " §7§o≫ §7")
                            .hoverEvent(HoverEvent.showText(TCUtil.form("§кClick - add to blackList")))
                            .clickEvent(ClickEvent.suggestCommand("/ignore add " + ce.senderName))
                            .append(msgEN);

                } else if (!senderWorldName.equals("lobby")) { //отправитель не в мире лобби - игровое сообщение

                    resultRU = TCUtil.form("§6<§e" + ce.senderName + "§6> §7§o≫ §f")
                            .hoverEvent(HoverEvent.showText(TCUtil.form("§кКлик - кинуть в ЧС")))
                            .clickEvent(ClickEvent.suggestCommand("/ignore add " + ce.senderName))
                            .append(msgRU);
                    resultEN = TCUtil.form("§6<§e" + ce.senderName + "§6> §7§o≫ §f")
                            .hoverEvent(HoverEvent.showText(TCUtil.form("§кClick - add to blackList")))
                            .clickEvent(ClickEvent.suggestCommand("/ignore add " + ce.senderName))
                            .append(msgEN);

                } else {
                    resultRU = bRU.build();//GM.getLogo().append(bRU.build());//b.build();
                    resultEN = bEN.build();//GM.getLogo().append(bEN.build());//b.build();
                }

                viewerResultRU = ce.getViewerGameInfo() == null ? GM.getLogo().append(resultRU) :
                        GM.getLogo().append(ce.getViewerGameInfo()).append(resultRU);

                viewerResultEN = ce.getViewerGameInfo() == null ? GM.getLogo().append(resultEN) :
                        GM.getLogo().append(ce.getViewerGameInfo()).append(resultEN);

                //на минииграх - показать подготовленное сообщение всем, кто в одном мире или в лобби
                for (Player p : ce.viewers()) {
                    if (p.getWorld().getName().equals(senderWorldName) || StringUtil.isLobby(p.getWorld())) {
                        if (p.getClientOption(ClientOption.LOCALE).equals("ru_ru")) {
                            p.sendMessage(viewerResultRU);
                        } else {
                            p.sendMessage(viewerResultEN);
                        }
                    }
                }

            } else {

                resultRU = bRU.build();//GM.getLogo().append(bRU.build());//b.build();
                resultEN = bEN.build();//GM.getLogo().append(bEN.build());//b.build();

                viewerResultRU = ce.getViewerGameInfo() == null ? GM.getLogo().append(resultRU) :
                        GM.getLogo().append(ce.getViewerGameInfo()).append(resultRU);

                viewerResultEN = ce.getViewerGameInfo() == null ? GM.getLogo().append(resultEN) :
                        GM.getLogo().append(ce.getViewerGameInfo()).append(resultEN);

                //показать подготовленное сообщение всем, кто остался в эвенте
                for (Player p : ce.viewers()) {
                    if (p.getClientOption(ClientOption.LOCALE).equals("ru_ru")) {
                        p.sendMessage(viewerResultRU);
                    } else {
                        p.sendMessage(viewerResultEN);
                    }
                }

            }


            //если игра поставила отдельное инфо для отправителя, лепим с этим инфо
            if (ce.showSelf()) {//если нет, режим сам скинет игроку что надо
                if (ce.getSenderGameInfo() == null) {
                    if (ce.getPlayer().getClientOption(ClientOption.LOCALE).equals("ru_ru")) {
                        ce.getPlayer().sendMessage(viewerResultRU);
                    } else {
                        ce.getPlayer().sendMessage(viewerResultEN);
                    }
                    //ce.getPlayer().sendMessage(viewerResult);
                } else {
                    if (ce.getPlayer().getClientOption(ClientOption.LOCALE).equals("ru_ru")) {
                        //ce.getPlayer().sendMessage(ce.getSenderGameInfo().append(resultRU));
                        ce.getPlayer().sendMessage(GM.getLogo().append(ce.getSenderGameInfo()).append(resultRU));
                    } else {
                        //ce.getPlayer().sendMessage(ce.getSenderGameInfo().append(resultEN));
                        ce.getPlayer().sendMessage(GM.getLogo().append(ce.getSenderGameInfo()).append(resultEN));
                    }
                    //ce.getPlayer().sendMessage(ce.getSenderGameInfo().append(result));
                }
            }

            //отправить в консоль
            Bukkit.getConsoleSender().sendMessage(viewerResultRU);
            //Bukkit.getConsoleSender().sendMessage(viewerResultEN);

        }


    }


    public static String getStatus(final Oplayer op) {
        if (op != null) {
            final int m = op.getDataInt(Data.LONI);
            if (m < 1000000) {
                if (m < 100000) {
                    if (m < 10000) {
                        if (m < 1000) {
                            if (m < 100) {
                                if (m < 10) {
                                    return "§6Нищеброд";
                                } else {
                                    return "§6Бедняк";
                                }
                            } else {
                                return "§6Малоимущий";
                            }
                        } else {
                            return "§6В достатке";
                        }
                    } else {
                        return "§6Хозяин жизни";
                    }
                } else {
                    return "§6Богач";
                }
            } else {
                return "§6Олигарх";
            }
        } else {
            return "§cИгрок Оффлайн";
        }
    }


    //с прокси пришло сообшение от другого сервера по новому каналу
    //приходят 2 волны - на русском и английком
    public static void onProxyChat(final Chanell ch, final int proxyId, final String serverName, final String senderName, final String gsonMsg) {
//Ostrov.log_warn("serverName="+serverName+" senderName="+senderName+" msg="+msg);
        final Component c = GsonComponentSerializer.gson().deserialize(gsonMsg);

        Oplayer oplayerTo;
        for (Player p : Bukkit.getOnlinePlayers()) {
            oplayerTo = PM.getOplayer(p);
            if (oplayerTo != null) {
                if (oplayerTo.isBlackListed(senderName) || oplayerTo.isLocalChat()) {
                    continue;
                }
                if (oplayerTo.eng && ch == Chanell.CHAT_EN) { //русским русский чат
                    p.sendMessage(c);
                } else if (!oplayerTo.eng && ch == Chanell.CHAT_RU) { //остальным показываем английскую версию
                    p.sendMessage(c);
                }
            }
        }

    }


}

  /*public static void onProxyChat(final Chanell ch, final String senderName, final String gsonMsg) {
    final Component c = GsonComponentSerializer.gson().deserialize(gsonMsg);
    for (Player p : Bukkit.getOnlinePlayers()) {
      final Oplayer oplayerTo = PM.getOplayer(p);
      if (oplayerTo != null) {
        if (oplayerTo.isBlackListed(senderName) || oplayerTo.isLocalChat()) {
          continue;
        }
        if (oplayerTo.eng && ch == Chanell.CHAT_EN) { //русским русский чат
          p.sendMessage(c);
        } else if (!oplayerTo.eng && ch==Chanell.CHAT_RU) { //остальным показываем английскую версию
          p.sendMessage(c);
        }
      }
    }
  }*/
