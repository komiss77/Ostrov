package ru.komiss77.listener;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
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
import ru.komiss77.enums.Chanell;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.netty.OsQuery;
import ru.komiss77.modules.netty.QueryCode;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.InputButton;


//https://docs.advntr.dev/serializer/gson.html

//SpigotChanellMsg.sendChat(ce.getPlayer(), gsonMsg); - отправлять русск и англ сообщ
//не показ чат на англ!

public class ChatLst implements Listener {

    private static final boolean TRANSLATE_CHAT = false;

    public static final String NIK_COLOR;
    private static final TextColor MSG_COLOR;
    private static final HoverEvent<Component> SUGGEST_MUTE_TOOLTIP_RU;
    //private static final TextComponent SUGGEST_BLACKLIST_TOOLTIP_RU;
    private static final HoverEvent<Component> PREFIX_TOOLTIP_RU;
    private static final HoverEvent<Component> SUFFIX_TOOLTIP_RU;
    private static final HoverEvent<Component> SUGGEST_MUTE_TOOLTIP_EN;
    //private static final TextComponent SUGGEST_BLACKLIST_TOOLTIP_EN;
    private static final HoverEvent<Component> PREFIX_TOOLTIP_EN;
    private static final HoverEvent<Component> SUFFIX_TOOLTIP_EN;
    private static final ClickEvent DONATE_CLICK_URL;
    private static final HoverEvent<Component> MSG_TOOLTIP_RU;
    private static final HoverEvent<Component> MSG_TOOLTIP_EN;
    private static final HoverEvent<Component> URL_TOOLTIP_RU;
    private static final HoverEvent<Component> URL_TOOLTIP_EN;
    private static final Format[] FORMATS;
    private static final StringUtil.Split SPLIT;
    private static final ClickCallback.Options OPTIONS;

    static {
        SPLIT = StringUtil.Split.MEDIUM;
        NIK_COLOR = switch (Ostrov.calendar.get(Calendar.MONTH)) {
            case 11, 0, 1 -> "<gradient:sky:blue>";
            case 2, 3, 4 -> "<gradient:pink:green>";
            case 8, 9, 10 -> "<gradient:beige:gold>";
            default -> "<gradient:apple:dark_aqua>";
        };
        MSG_COLOR = NamedTextColor.GRAY;
        SUGGEST_MUTE_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§кКлик - выдать молчанку"));
        //SUGGEST_BLACKLIST_TOOLTIP_RU = TCUtils.format("§кКлик - кинуть в ЧС");
        PREFIX_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §оХочешь префикс? Жми!!!  §7-=[§я✦§7]"));
        SUFFIX_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §сХочешь суффикс? Жми!!!  §7-=[§я✦§7]"));
        MSG_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§fКлик - §3опции"));
        MSG_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§fClick - §3options"));
        URL_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§9Клик - перейти по <u>ссылке"));
        URL_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§9Click - open <u>URL"));
        SUGGEST_MUTE_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§кClick - mute player"));
        //SUGGEST_BLACKLIST_TOOLTIP_EN = TCUtils.format("§кClick- add to blackList");
        PREFIX_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §оWant a prefix? Click here!!!  §7-=[§я✦§7]"));
        SUFFIX_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §сWant a suffix? Click here!!!  §7-=[§я✦§7]"));
        DONATE_CLICK_URL = ClickEvent.openUrl("http://www.ostrov77.ru/donate.html");
        FORMATS = new Format[] {new Format("**", "<b>", "</b>"), new Format("*", "<i>", "</i>"),
            new Format("||", "<obf>", "</obf>"), new Format("|", "<obf>", "</obf>"), new Format("~~", "<st>", "</st>"),
            new Format("~", "<st>", "</st>"), new Format("__", "<u>", "</u>"), new Format("_", "<i>", "</i>")};
        OPTIONS = ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(8)).build();
    }

    private static final class Format {
        private static final char[] RGX_CHARS = {'\\', '.', '^', '$', '*', '+', '?', '{', '}', '[', ']', '(', ')', '|'};
        static {
            Arrays.sort(RGX_CHARS);}
        private final int bgnLen, endLen;
        private final Pattern pat;
        private final String bgnTo;
        private final String endTo;

        private Format(final String bgnOf, final String endOf, final String bgnTo, final String endTo) {
            this.bgnLen = bgnOf.length(); this.endLen = endOf.length(); this.bgnTo = bgnTo; this.endTo = endTo;
            this.pat = Pattern.compile(parse(bgnOf) + "(.*?)" + parse(endOf));
            //\*\*(.*?)\^\^
            //**star face is here to see^^
        }

        private Format(final String bound, final String bgnTo, final String endTo) {
            this.bgnLen = this.endLen = bound.length(); this.bgnTo = bgnTo; this.endTo = endTo;
            this.pat = Pattern.compile(parse(bound) + "(.*?)" + parse(bound));
            //\*\*(.*?)\*\*
            //**star face is here to see**
        }

        private String parse(final String bound) {
            final StringBuilder sb = new StringBuilder(bound.length());
            for (final char c : bound.toCharArray()) {
                if (Arrays.binarySearch(RGX_CHARS, c) >= 0) sb.append("\\");
                sb.append(c);
            }
            return sb.toString();
        }

        private String process(final String msg) {
            final Matcher mt = pat.matcher(msg);
            final StringBuilder sb = new StringBuilder(msg.length());
            int end = 0;
            while (mt.find()) {
                final int stIx = mt.start();
                final String grp = mt.group();
                sb.append(msg, end, stIx).append(bgnTo)
                    .append(grp, bgnLen, grp.length() - endLen).append(endTo);
                end = stIx + grp.length();
            }
            return sb.append(msg.substring(end)).toString();
        }
    }

    // на HIGHEST проверяется мут и формируется сообщение для прокси.
    // локальной рассылкой с проверкой ЧС занимается каждая игра самостоятельно на приоритете ниже
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncChatEvent e) {
        chat(e.getPlayer(), e.message(), e.viewers());
    }

    public static void chat(final Player sender, final Component msg, final Set<Audience> view) {
        //режим ввода из чата
        final Oplayer senderOp = PM.getOplayer(sender);
        if (senderOp != null && PlayerInput.inputData.containsKey(sender) && PlayerInput.inputData.get(sender).type == InputButton.InputType.CHAT) {
            view.clear();
            //PlayerInput.onInput(sender.getName(), InputButton.InputType.CHAT, TCUtils.toString(e.message()));
            //Could not pass event AsyncChatEvent to Ostrov v2.0 java.lang.IllegalStateException: InventoryOpenEvent may only be triggered synchronously.
            Ostrov.sync(() -> PlayerInput.onInput(sender, InputButton.InputType.CHAT, TCUtil.deform(msg).replace("\\", "")));//цвета вписаные в чат не юзались
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
        final String senderName = sender.getName();

        //создадим свой список получателей. У кого отправитель в ЧС, будут отфильтрованы
        final List<Player> list = new ArrayList<>();

        boolean blacklisted = false;
        final Iterator<Audience> it = view.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof Player playerTo) {
                if (playerTo.getName().equals(senderName)) continue; //себя не надо в список - в конце отправляется безусловно
                final Oplayer opTo = PM.getOplayer(playerTo);
                if (opTo == null) {
                    list.add(playerTo);
                    continue;
                }
                if (opTo.isBlackListed(senderName)) {//пишущий в черном списке
                    it.remove();
                    blacklisted = true;
                    continue;
                }
                if (muted && !senderOp.friends.contains(opTo.nik)) { //замучен и получатель не друг
                    it.remove();
                    continue;
                }
                list.add(playerTo);
            }
        }

        //кинуть эфент - игра может добавить своё инфо или отменить отправку на прокси, если, например, не в лобби игры
        //игра может поставить gameInfo и фильтрануть ненужных получателей (например, для островного или кланового чата)
        String strMsg = TCUtil.deform(msg).replace("/<", "<");

        final ChatPrepareEvent ce = new ChatPrepareEvent(sender, senderOp, list, strMsg);
        Bukkit.getPluginManager().callEvent(ce);

        if (ce.isCancelled()) {
            view.clear();
            return;
        }
        if (!ce.sendProxy() && !ce.showLocal()) return; //игра отменила все отправки - значит рассылает сама

        if (ce.getMessage() != null) {
            strMsg = ce.getMessage();
        }

        if (ce.showLocal()) { //игра оставила работать дальше по умолчанию - офф стандартный чат
            view.clear();
        }

        //вкинуть всё нужное в эвент для возможности перевода
        //данные примерно в порядке нужности
        //лого сервера - готовый компонент
        //инфо игры - готовый компонент
        ce.senderName = senderName;
        ce.banned = false;
        ce.muted = muted;
        ce.prefix = senderOp.getDataString(Data.PREFIX) + " <reset>";

        final StringBuilder sb = new StringBuilder();

        if (senderOp.isGuest) {
            if (senderOp.eng) {
                sb.append("§6Player is in §eGuet Mode§6!")
                    .append("\n§6Player data is not saved!")
                    .append("\n§3Server: §a").append(Ostrov.MOT_D)
                    .append((muted ? "\n§4Молчанка: §cYes" : ""))
                    .append("\n<apple>Click - direct message");
            } else {
                sb.append("§6Игрок в §eГостевом режиме§6!")
                    .append("\n§6Игровые данные не сохраняются!")
                    .append("\n§3Сервер: §a").append(Ostrov.MOT_D)
                    .append((muted ? "\n§4Молчанка: §cДа" : ""))
                    .append("\n<apple>Клик - личное сообщение");
            }
            ce.suffix = "";
        } else {
            if (senderOp.eng) {
                sb.append("\n§3Server: §a").append(Ostrov.MOT_D)
                    .append((muted ? "\n§4Muted: §cYes" : ""))
                    .append("\n<amber>Social status: ").append(getStatus(senderOp))
                    .append("\n§5Groups: §f").append(senderOp.chat_group)
                    .append("\n<indigo>Badges: ") //TODO баджики
                    .append(PM.getGenderDisplay(senderOp))
                    .append("\n§ePlay time: ").append(TimeUtil.secondToTime(senderOp.getStat(Stat.PLAY_TIME)))
                    .append("\n<gray>Click - <gold>direct message");
            } else {
                sb.append("\n§3Сервер: §a").append(Ostrov.MOT_D)
                    .append((muted ? "\n§4Молчанка: §cДа" : ""))
                    .append("\n<amber>Соц. статус: ").append(getStatus(senderOp))
                    .append("\n§5Группы: §f").append(senderOp.chat_group)
                    .append("\n<indigo>Баджики: ") //TODO баджики
                    .append(PM.getGenderDisplay(senderOp))
                    .append("\n§eВремя игры: ").append(TimeUtil.secondToTime(senderOp.getStat(Stat.PLAY_TIME)))
                    .append("\n<gray>Клик - <gold>личное сообщение");
            }
            ce.suffix = "<reset> " + senderOp.getDataString(Data.SUFFIX);
        }
        ce.playerTooltip = sb.toString();


        //в переводчике будет дополнено поле оппозитного перевода обязательно!
        if (TRANSLATE_CHAT) {
            if (!senderOp.eng) {
                ce.strMsgRu = strMsg; //переводов не требуется
            } else {
                ce.strMsgEn = strMsg; //отправитель не русскоязычный
            }
            Lang.translateChat(ce);
        } else {
            ce.strMsgRu = strMsg;
            ce.strMsgEn = strMsg;
            process(ce);
        }

        if (blacklisted) {
            ScreenUtil.sendActionBarDirect(sender, "<amber>Игроки, у которых ты в ЧС, не увидят это сообщение");
        }
        OsQuery.send(QueryCode.CHAT_STRIP, senderName + LocalDB.WORD_SPLIT + strMsg);
    }

    public static void process(final ChatPrepareEvent ce) {

        final Oplayer sender = ce.getOplayer();
        final boolean useColorCode = Perm.canColorChat(sender);

        final Component[] msgRU = format(ce, false, useColorCode); //сообщение возможно с цветами
        final Component[] msgEN = format(ce, true, useColorCode); //сообщение возможно с цветами

        //билдим итоговый компонент
        final TextComponent.Builder bRU = Component.text();
        final TextComponent.Builder bEN = Component.text();

        //префикс
        if (!ce.prefix.isEmpty()) {
            bRU.append(TCUtil.form(ce.prefix)
                .hoverEvent(PREFIX_TOOLTIP_RU)
                .clickEvent(DONATE_CLICK_URL)
            );
            bEN.append(TCUtil.form(ce.prefix)
                .hoverEvent(PREFIX_TOOLTIP_EN)
                .clickEvent(DONATE_CLICK_URL)
            );
        }

        //ник игрока
        final String name = sender.isGuest ? sender.getDataString(Data.FAMILY) : ce.senderName;
        bRU.append(TCUtil.form(NIK_COLOR + name)
            .hoverEvent(HoverEvent.showText(TCUtil.form(ce.playerTooltip)))
            .clickEvent(ClickEvent.suggestCommand("/msg " + name + " "))
        );

        bEN.append(TCUtil.form(NIK_COLOR + name)
            .hoverEvent(HoverEvent.showText(TCUtil.form(ce.playerTooltip)))
            .clickEvent(ClickEvent.suggestCommand("/msg " + name + " "))
        );

        //суффикс
        if (!ce.suffix.isEmpty()) {
            bRU.append(TCUtil.form(ce.suffix)
                .hoverEvent(SUFFIX_TOOLTIP_RU)
                .clickEvent(DONATE_CLICK_URL)
            );
            bEN.append(TCUtil.form(ce.suffix)
                .hoverEvent(SUFFIX_TOOLTIP_EN)
                .clickEvent(DONATE_CLICK_URL)
            );
        }

        //стрелочки и сообщения
        bRU.append(Component.text(" ≫ ", MSG_COLOR, TextDecoration.ITALIC))
            .append(msgRU[msgRU.length - 1].colorIfAbsent(MSG_COLOR));
        bEN.append(Component.text(" ≫ ", MSG_COLOR, TextDecoration.ITALIC))
            .append(msgEN[msgEN.length - 1].colorIfAbsent(MSG_COLOR));

        final ServerType serverType = GM.GAME.type;
        //Ostrov.log_warn("sendProxy?"+ce.sendProxy()+" isLocalChat?"+senderOp.isLocalChat());
        //игра не отменила отправку на прокси - работаем по дефолту:
        //на всех кроме миниигр отправляем,
        //а на минииграх отправляем если в мире лобби
        final Player pl = ce.getPlayer();
        if (!ce.banned && !ce.muted && ce.sendProxy() && !sender.isLocalChat()) {
            final Component proxyResultRU;
            final Component proxyResultEN;
            //убрать лишние элементы, пускай ГЛОБАЛЬНЫЕ сообщения будут всегда в формате: [Значек]_<Префикс>_Имя_<Суффикс>_»_(cообщение)
            if (msgRU.length == 1) proxyResultRU = GM.getLogo().append(bRU.build());
            else proxyResultRU = msgRU[0].append(Component.newline().style(Style.empty()))
                    .append(GM.getLogo()).append(bRU.build());
            if (msgEN.length == 1) proxyResultEN = GM.getLogo().append(bEN.build());
            else proxyResultEN = msgEN[0].append(Component.newline().style(Style.empty()))
                .append(GM.getLogo()).append(bEN.build());
            final String gsonMsgRU = GsonComponentSerializer.gson().serialize(proxyResultRU);
            final String gsonMsgEN = GsonComponentSerializer.gson().serialize(proxyResultEN);
            SpigotChanellMsg.sendChat(pl, gsonMsgRU, Chanell.CHAT_RU);
            SpigotChanellMsg.sendChat(pl, gsonMsgEN, Chanell.CHAT_EN);
            OsQuery.send(QueryCode.CHAT_RU, pl.getName() + LocalDB.WORD_SPLIT + gsonMsgRU);
            OsQuery.send(QueryCode.CHAT_EN, pl.getName() + LocalDB.WORD_SPLIT + gsonMsgEN);
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
            final Component gi = ce.getViewerGameInfo();
            final Component logo = gi == null ? GM.getLogo() : GM.getLogo().append(gi);
            //на миниигре подменяем сообщение, если отправитель зритель или в игре
            if (serverType == ServerType.ARENAS && !ApiOstrov.isLocalBuilder(pl)) {

                if (pl.getGameMode() == GameMode.SPECTATOR) { //отправитель в ГМ3 - зритель

                    resultRU = TCUtil.form("§8[Зритель] " + ce.senderName + " §7§o≫ §7")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("§кКлик - кинуть в ЧС")))
                        .clickEvent(ClickEvent.suggestCommand("/ignore " + ce.senderName))
                        .append(msgRU[msgRU.length - 1]);

                    resultEN = TCUtil.form("§8[Spectator] " + ce.senderName + " §7§o≫ §7")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("§кClick - add to blackList")))
                        .clickEvent(ClickEvent.suggestCommand("/ignore " + ce.senderName))
                        .append(msgEN[msgEN.length - 1]);

                } else if (!StringUtil.isLobby(pl.getWorld())) { //отправитель не в мире лобби - игровое сообщение

                    resultRU = TCUtil.form("§6<§e" + ce.senderName + "§6> §7§o≫ §f")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("§кКлик - кинуть в ЧС")))
                        .clickEvent(ClickEvent.suggestCommand("/ignore " + ce.senderName))
                        .append(msgRU[msgRU.length - 1]);
                    resultEN = TCUtil.form("§6<§e" + ce.senderName + "§6> §7§o≫ §f")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("§кClick - add to blackList")))
                        .clickEvent(ClickEvent.suggestCommand("/ignore " + ce.senderName))
                        .append(msgEN[msgEN.length - 1]);

                } else {
                    resultRU = bRU.build();//GM.getLogo().append(bRU.build());//b.build();
                    resultEN = bEN.build();//GM.getLogo().append(bEN.build());//b.build();
                }

                viewerResultRU = msgRU.length == 1 ? logo.append(resultRU)
                    : msgRU[0].append(Component.newline().style(Style.empty()))
                    .append(logo).append(resultRU);

                viewerResultEN = msgEN.length == 1 ? logo.append(resultEN)
                    : msgEN[0].append(Component.newline().style(Style.empty()))
                    .append(logo).append(resultEN);

                //на минииграх - показать подготовленное сообщение всем, кто в одном мире или в лобби
                final UUID uid = pl.getWorld().getUID();
                for (final Player p : ce.viewers()) {
                    if (p.getWorld().getUID().equals(uid) || StringUtil.isLobby(p.getWorld())) {
                        p.sendMessage(p.getClientOption(ClientOption.LOCALE).equals("ru_ru")
                            ? viewerResultRU : viewerResultEN);
                    }
                }

            } else {

                resultRU = bRU.build();//GM.getLogo().append(bRU.build());//b.build();
                resultEN = bEN.build();//GM.getLogo().append(bEN.build());//b.build();

                viewerResultRU = msgRU.length == 1 ? logo.append(resultRU)
                    : msgRU[0].append(Component.newline().style(Style.empty()))
                    .append(logo).append(resultRU);

                viewerResultEN = msgEN.length == 1 ? logo.append(resultEN)
                    : msgEN[0].append(Component.newline().style(Style.empty()))
                    .append(logo).append(resultEN);

                //показать подготовленное сообщение всем, кто остался в эвенте
                for (Player p : ce.viewers()) {
                    p.sendMessage(p.getClientOption(ClientOption.LOCALE).equals("ru_ru")
                        ? viewerResultRU : viewerResultEN);
                }

            }


            //если игра поставила отдельное инфо для отправителя, лепим с этим инфо
            if (ce.showSelf()) {//если нет, режим сам скинет игроку что надо
                pl.sendMessage(pl.getClientOption(ClientOption.LOCALE).equals("ru_ru")
                    ? viewerResultRU : viewerResultEN);
            }

            //отправить в консоль
            Bukkit.getConsoleSender().sendMessage(viewerResultRU);
        }
    }

    private static Component[] format(final ChatPrepareEvent ce, final boolean eng, final boolean clr) {
        String msg = eng ? ce.strMsgEn : ce.strMsgRu;
        final HoverEvent<Component> url_ttp = eng ? URL_TOOLTIP_EN : URL_TOOLTIP_RU;
        final HoverEvent<Component> msg_ttp = eng ? MSG_TOOLTIP_EN : MSG_TOOLTIP_RU;
        final String[] split = SPLIT.split(msg, true);
        final String reply;
        if (split.length == 1) reply = null;
        else {reply = split[0]; msg = split[1];}
        if (!clr) {
            final Component cm = TCUtil.form(msg)
                .hoverEvent(msg_ttp).clickEvent(ClickEvent.callback(ClickCallback
                    .widen(new Message(msg, ce.getOplayer()), Player.class), OPTIONS));
            return reply == null ? new Component[]{cm} : new Component[]{TCUtil.form(reply), cm};
        }
        for (final Format frm : FORMATS) msg = frm.process(msg);
        msg = msg.replace('&', '§').replace("\\", "");
        final TextComponent.Builder mb = Component.text();
        final StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (final String s : msg.split(" ")) {
            if (!s.startsWith("http")) {
                if (start) start = false;
                else sb.append(" ");
                sb.append(s);
                continue;
            }
            if (start) start = false;
            else sb.append(" ");
            final String sbm = sb.toString();
            mb.append(TCUtil.form(sbm)
                .hoverEvent(msg_ttp).clickEvent(ClickEvent.callback(ClickCallback
                    .widen(new Message(sbm, ce.getOplayer()), Player.class), OPTIONS)));
            mb.append(Component.text(s, NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                .hoverEvent(url_ttp).clickEvent(ClickEvent.openUrl(s)));
            sb.setLength(0);
        }
        if (!sb.isEmpty()) {
            mb.append(TCUtil.form(sb.toString())
                .hoverEvent(msg_ttp).clickEvent(null));
        }
        return reply == null ? new Component[]{mb.build()} : new Component[]{TCUtil.form(reply), mb.build()};
    }

    private static final int MAX_LEN = 40;
    private record Message(String msg, Oplayer sender) implements ClickCallback<Player> {
        public void accept(final Player pl) {
            final String strip = TCUtil.strip(msg);
            final Oplayer op = PM.getOplayer(pl);
            if (op == null) return;
            pl.openBook(Book.book(TCUtil.form("<dark_aqua>Опции Сообщения"), Component.text(sender.nik), new Component[] {
                TCUtil.form("<olive><shadow:#000000FF>Опции Сообщения:<reset>\n")
                    .append(TCUtil.form((strip.length() > MAX_LEN ? strip.substring(0, MAX_LEN) + ".." : msg) + "\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<gold>Клик - <yellow>копировать")))
                        .clickEvent(ClickEvent.suggestCommand(msg)))

                    .append(TCUtil.form("<stale><shadow:#222222FF>╘› Ответить <beige>(Клик)<reset>\n\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<dark_aqua>Клик - <aqua>ответить")))
                        .clickEvent(ClickEvent.callback(ClickCallback.widen(p -> PlayerInput.get(InputButton.InputType.CHAT, p, rpl -> {
                            final Component reply = TCUtil.form("<gray>╒═<font:uniform> " + sender.nik
                                    + " <i>›</i> " + msg + SPLIT.get() + rpl)
                                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.NOT_SET);
                            chat(p, reply, new HashSet<>(Bukkit.getOnlinePlayers()));
                        }, ""), Player.class))))

                    .append(TCUtil.form("<stale><shadow:#000000FF>Отправитель:<reset>\n"))
                    .append(TCUtil.form(" <u>" + NIK_COLOR + (sender.isGuest ? sender.getDataString(Data.FAMILY) : sender.nik) + "\n\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<amber>Клик - <gold>написать ЛС")))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + sender.nik + " ")))

                    .append(TCUtil.form("<stale><shadow:#222222FF><gray>› Игнорировать <beige>(Клик)<reset>\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<dark_purple>Клик - <pink>добавить в ЧС")))
                        .clickEvent(ClickEvent.runCommand("/ignore " + sender.nik)))

                    .append(op.isStaff ? TCUtil.form("<stale><shadow:#222222FF><cardinal>Замутить <beige>(Клик)<reset>")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<dark_red>Клик - <red>выдать мут")))
                        .clickEvent(ClickEvent.runCommand("/mute " + sender.nik + " 10m "))

                    : TCUtil.form("<stale><shadow:#222222FF><cardinal>Подать Жалобу <beige>(Клик)<reset>")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<dark_red>Клик - <red>пожаловатся")))
                        .clickEvent(ClickEvent.callback(ClickCallback.widen(p -> PlayerInput.get(InputButton.InputType.ANVILL, p, rpl -> {
                            //TODO жалобы на чат
                        }, "Жалоба"), Player.class))))
            }));
        }
    }

    public static String getStatus(final Oplayer op) {
        if (op == null) return "§cИгрок Оффлайн";
        final int m = op.getDataInt(Data.LONI);
        if (m < 10) return "§6Нищеброд";
        if (m < 100) return "§6Бедняк";
        if (m < 1000) return "§6Малоимущий";
        if (m < 10000) return "§6В достатке";
        if (m < 100000) return "§6Хозяин жизни";
        if (m < 1000000) return "§6Богач";
        return "§6Олигарх";
    }

    //с прокси пришло сообшение от другого сервера по новому каналу
    //приходят 2 волны - на русском и английком
    @Deprecated
    public static void onProxyChat(final Chanell ch, final int proxyId, final String serverName, final String senderName, final String gsonMsg) {
        onProxyChat(ch, senderName, gsonMsg);
    }

    public static void onProxyChat(final Chanell ch, final String senderName, final String gsonMsg) {
        final Component c = GsonComponentSerializer.gson().deserialize(gsonMsg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            final Oplayer to = PM.getOplayer(p);
            if (to != null) {
                if (to.isBlackListed(senderName)
                    || to.isLocalChat()) continue;
                //русским русский чат
                if (to.eng && ch == Chanell.CHAT_EN) p.sendMessage(c);
                else //остальным показываем английскую версию
                    if (!to.eng && ch == Chanell.CHAT_RU) p.sendMessage(c);
            }
        }
    }
}
