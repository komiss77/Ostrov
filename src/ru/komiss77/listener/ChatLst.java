package ru.komiss77.listener;

import java.util.*;
import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Chanell;
import ru.komiss77.enums.Data;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.modules.netty.Message;
import ru.komiss77.modules.netty.OsQuery;
import ru.komiss77.modules.netty.QueryCode;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.InputButton;


//https://docs.advntr.dev/serializer/gson.html

//SpigotChanellMsg.sendChat(ce.getPlayer(), gsonMsg); - отправлять русск и англ сообщ
//не показ чат на англ!

public class ChatLst implements Listener {

    public static final StringUtil.Split TOP_SPLIT = StringUtil.Split.MEDIUM;
    public static final String NIK_COLOR = switch (Ostrov.calendar.get(Calendar.MONTH)) {
        case 11, 0, 1 -> "<gradient:sky:blue>";
        case 2, 3, 4 -> "<gradient:pink:green>";
        case 8, 9, 10 -> "<gradient:beige:gold>";
        default -> "<gradient:apple:dark_aqua>";
    };

    /*private static final TextColor MSG_COLOR;
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
    private static final String ARROW = "➠";

    static {
        SPLIT = StringUtil.Split.MEDIUM;
        MSG_COLOR = NamedTextColor.GRAY;
        SUGGEST_MUTE_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§кКлик - выдать молчанку"));
        //SUGGEST_BLACKLIST_TOOLTIP_RU = TCUtils.format("§кКлик - кинуть в ЧС");
        PREFIX_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §оХочешь префикс? Жми!  §7-=[§я✦§7]"));
        SUFFIX_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §сХочешь суффикс? Жми!  §7-=[§я✦§7]"));
        MSG_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("<beige>🢖 Клик §3- Опции"));
        MSG_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("<beige>🢖 Click §3- Options"));
        URL_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§9Клик - перейти по <u>ссылке"));
        URL_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§9Click - open <u>URL"));
        SUGGEST_MUTE_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§кClick - mute player"));
        //SUGGEST_BLACKLIST_TOOLTIP_EN = TCUtils.format("§кClick- add to blackList");
        PREFIX_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §оWant a prefix? Click here!  §7-=[§я✦§7]"));
        SUFFIX_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §сWant a suffix? Click here!  §7-=[§я✦§7]"));
        DONATE_CLICK_URL = ClickEvent.openUrl("http://www.ostrov77.ru/donate.html");
        FORMATS = new Format[] {new Format("**", "<b>", "</b>"), new Format("*", "<i>", "</i>"),
            new Format("||", "<obf>", "</obf>"), new Format("|", "<obf>", "</obf>"), new Format("~~", "<st>", "</st>"),
            new Format("~", "<st>", "</st>"), new Format("__", "<u>", "</u>"), new Format("_", "<i>", "</i>")};
        OPTIONS = ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(8)).build();
    }*/

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
        final boolean muted = senderOp.globalInt(Data.MUTE_TO) > Timer.secTime();
        if (muted) {
            sender.sendMessage(Component.text("Чат ограничен - сообщения видят только друзья", NamedTextColor.RED)
                .hoverEvent(HoverEvent.showText(TCUtil.form("§cМолчанка от §6" + senderOp.globalStr(Data.MUTE_BY)
                    + " §cдо §b" + senderOp.globalStr(Data.MUTE_REAS) + "§c, осталось: §e" +
                    TimeUtil.secondToTime(senderOp.globalInt(Data.MUTE_TO) - Timer.secTime()))))
                .clickEvent(ClickEvent.openUrl("https://discord.com/channels/646762127335489540/679455910283706388")));
        }
        final boolean banned = senderOp.globalInt(Data.BAN_TO) > Timer.secTime();
        if (banned) {
            sender.sendMessage(Component.text("Чат ограничен чистилищем - ты в бане", NamedTextColor.DARK_RED)
                .hoverEvent(HoverEvent.showText(TCUtil.form("§cБан от §6" + senderOp.globalStr(Data.BAN_BY)
                    + " §cдо §b" + senderOp.globalStr(Data.BAN_REAS) + "§c, осталось: §e" +
                    TimeUtil.secondToTime(senderOp.globalInt(Data.BAN_TO) - Timer.secTime()))))
                .clickEvent(ClickEvent.openUrl("https://discord.com/channels/646762127335489540/679455910283706388")));
            return;
        }

        //если у игрока в настройках локальны чат - точно не отправляем
        final String name = sender.getName();

        //создадим свой список получателей. У кого отправитель в ЧС, будут отфильтрованы
        final List<Player> list = new ArrayList<>();

        boolean blacklisted = false;
        final Iterator<Audience> it = view.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof Player to) {
                if (to.getEntityId() == sender.getEntityId()) continue; //себя не надо в список - в конце отправляется безусловно
                final Oplayer opTo = PM.getOplayer(to);
                if (opTo == null) {
                    list.add(to);
                    continue;
                }
                if (opTo.isBlackListed(name)) { //пишущий в черном списке
                    it.remove();
                    blacklisted = true;
                    continue;
                }
                if (muted && !senderOp.friends.contains(opTo.nik)) { //замучен и получатель не друг
                    it.remove();
                    continue;
                }
                list.add(to);
            }
        }

        //кинуть эфент - игра может добавить своё инфо или отменить отправку на прокси, если, например, не в лобби игры
        //игра может поставить gameInfo и фильтрануть ненужных получателей (например, для островного или кланового чата)

        final ChatPrepareEvent ce = new ChatPrepareEvent(sender,
            senderOp, list, TCUtil.deform(msg).replace("/<", "<"));
        ce.banned = false; ce.muted = muted;
        Bukkit.getPluginManager().callEvent(ce);

        if (ce.isCancelled()) {
            view.clear();
            return;
        }
        if (!ce.sendProxy() && !ce.showLocal()) return; //игра отменила все отправки - значит рассылает сама

        if (ce.showLocal()) { //игра оставила работать дальше по умолчанию - офф стандартный чат
            view.clear();
        }

        //вкинуть всё нужное в эвент для возможности перевода
        //данные примерно в порядке нужности
        //лого сервера - готовый компонент
        //инфо игры - готовый компонент

        /*ce.name = name;
        ce.prefix = senderOp.globalStr(Data.PREFIX);

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
            ce.suffix = "";
        } else {
            if (senderOp.eng) {
                sb.append("§3Server: §a").append(Ostrov.MOT_D)
                    .append("\n<amber>Social status: ").append(getStatus(senderOp))
                    .append("\n<stale>Groups: §f").append(senderOp.chat_group)
                    .append("\n<indigo>Badges: ") //TODO баджики
                    .append(PM.getGenderDisplay(senderOp)).append("\n")
                    .append("\n§6Play time: §e").append(TimeUtil.secondToTime(senderOp.getStat(Stat.PLAY_TIME)))
                    .append((muted ? "\n§4Muted: §cYes" : "\n"))
                    .append("\n<gray>Click - <gold>direct message");
            } else {
                //TODO баджики
                sb.append("§3Сервер: §a").append(Ostrov.MOT_D)
                    .append("\n<amber>Соц. статус: ").append(getStatus(senderOp))
                    .append("\n<stale>Группы: §f").append(senderOp.chat_group)
                    .append("\n<indigo>Баджики: §9").append("\n")
                    .append(PM.getGenderDisplay(senderOp))
                    .append("\n§6Время игры: §e").append(TimeUtil.secondToTime(senderOp.getStat(Stat.PLAY_TIME)))
                    .append((muted ? "\n§4Молчанка: §cДа" : "\n"))
                    .append("\n<gray>Клик - <gold>личное сообщение");
            }
            ce.suffix = senderOp.globalStr(Data.SUFFIX);
        }
        ce.playerTooltip = sb.toString();*/

        //в переводчике будет дополнено поле оппозитного перевода обязательно!
        /*if (TRANSLATE_CHAT) {
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
        }*/

        if (blacklisted) {
            ScreenUtil.sendActionBarDirect(sender, "<amber>Игроки, у которых ты в ЧС, не увидят это сообщение!");
        }

        if (ce.getMessage() != null) OsQuery.send(QueryCode.CHAT_STRIP,
            name + LocalDB.WORD_SPLIT + ce.getMessage());

        process(ce);
    }

    public static void process(final ChatPrepareEvent ce) {
        /*final boolean useColorCode = Perm.canColorChat(ce.getPlayer(), sender);

        final Component[] splitRU = format(ce, false, useColorCode);
        final Component msgRU = splitRU[splitRU.length - 1]; //сообщение возможно с цветами
        final Component topRU = splitRU.length == 1 ? Component.empty()
            : splitRU[0].append(Component.newline().style(Style.empty()));
        final Component[] splitEN = format(ce, true, useColorCode);
        final Component msgEN = splitEN[splitEN.length - 1]; //сообщение возможно с цветами
        final Component topEN = splitEN.length == 1 ? Component.empty()
            : splitEN[0].append(Component.newline().style(Style.empty()));

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
        bRU.append(Component.text(" " + ARROW + " ", MSG_COLOR))
            .append(msgRU.colorIfAbsent(MSG_COLOR));
        bEN.append(Component.text(" " + ARROW + " ", MSG_COLOR))
            .append(msgEN.colorIfAbsent(MSG_COLOR));*/

        final Message msg = new Message(ce);

//        final ServerType serverType = GM.GAME.type;
        //Ostrov.log_warn("sendProxy?"+ce.sendProxy()+" isLocalChat?"+senderOp.isLocalChat());
        //игра не отменила отправку на прокси - работаем по дефолту:
        //на всех кроме миниигр отправляем,
        //а на минииграх отправляем если в мире лобби
        final Player pl = ce.getPlayer();
        final Oplayer sender = ce.getOplayer();
        if (!ce.banned && !ce.muted && ce.sendProxy() && !sender.isLocalChat()) {
            /*final Component proxyResultRU;
            final Component proxyResultEN;
            //убрать лишние элементы, пускай ГЛОБАЛЬНЫЕ сообщения будут всегда в формате: [Значек]_<Префикс>_Имя_<Суффикс>_»_(cообщение)
            proxyResultRU = topRU.append(GM.getLogo()).append(bRU.build());
            proxyResultEN = topEN.append(GM.getLogo()).append(bEN.build());
            final String gsonMsgRU = GsonComponentSerializer.gson().serialize(proxyResultRU);
            final String gsonMsgEN = GsonComponentSerializer.gson().serialize(proxyResultEN);*/
            SpigotChanellMsg.sendChat(pl, msg.toString(), Chanell.CHAT_RU);
            SpigotChanellMsg.sendChat(pl, msg.toString(), Chanell.CHAT_EN);
            OsQuery.send(QueryCode.CHAT_RU, msg.toString());
            OsQuery.send(QueryCode.CHAT_EN, msg.toString());
        }

        //если игра не отменила показ на локальном сервере, рассылаем по умолчанию
        //по умолчанию:
        //на сервере миниигры - в мире lobby обычный прокси чат, в ГМ3 - чат зрителя, остальыне локальный с простым форматом
        //на больших cерверах простой глобальный

        if (!ce.showLocal()) return;
        //на миниигре подменяем сообщение, если отправитель зритель или в игре
        /*if (serverType == ServerType.ARENAS && !ApiOstrov.isLocalBuilder(pl)) {

            if (pl.getGameMode() == GameMode.SPECTATOR) { //отправитель в ГМ3 - зритель

                resultRU = TCUtil.form("§8[Зритель] " + ce.senderName + " §7" + ARROW + " ")
                    .hoverEvent(HoverEvent.showText(TCUtil.form("<gray>Клик - <gold>личное сообщение")))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + ce.senderName + " "))
                    .append(msgRU);

                resultEN = TCUtil.form("§8[Spectator] " + ce.senderName + " §7" + ARROW + " ")
                    .hoverEvent(HoverEvent.showText(TCUtil.form("<gray>Click - <gold>direct message")))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + ce.senderName + " "))
                    .append(msgEN);

            } else if (!StringUtil.isLobby(pl.getWorld())) { //отправитель не в мире лобби - игровое сообщение

                resultRU = TCUtil.form("§6<§e" + ce.senderName + "§6> §7" + ARROW + " §f")
                    .hoverEvent(HoverEvent.showText(TCUtil.form("<gray>Клик - <gold>личное сообщение")))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + ce.senderName + " "))
                    .append(msgRU);
                resultEN = TCUtil.form("§6<§e" + ce.senderName + "§6> §7" + ARROW + " §f")
                    .hoverEvent(HoverEvent.showText(TCUtil.form("<gray>Click - <gold>direct message")))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + ce.senderName + " "))
                    .append(msgEN);

            } else {
                resultRU = bRU.build();//GM.getLogo().append(bRU.build());//b.build();
                resultEN = bEN.build();//GM.getLogo().append(bEN.build());//b.build();
            }

            viewerResultRU = topRU.append(logo).append(resultRU);
            viewerResultEN = topEN.append(logo).append(resultEN);

            //на минииграх - показать подготовленное сообщение всем, кто в одном мире или в лобби
            final UUID uid = pl.getWorld().getUID();
            for (final Player p : ce.viewers()) {
                if (p.getWorld().getUID().equals(uid) || StringUtil.isLobby(p.getWorld())) {
                    p.sendMessage(p.getClientOption(ClientOption.LOCALE).equals("ru_ru")
                        ? viewerResultRU : viewerResultEN);
                }
            }

        }*/


        final Component viewerResultRU = msg.build(ce, false, false);
        final Component viewerResultEN = msg.build(ce, false, true);

        //показать подготовленное сообщение всем, кто остался в эвенте
        for (Player p : ce.viewers()) {
            p.sendMessage(p.getClientOption(ClientOption.LOCALE).equals("ru_ru")
                ? viewerResultRU : viewerResultEN);
        }


        //если игра поставила отдельное инфо для отправителя, лепим с этим инфо
        if (ce.showSelf()) {//если нет, режим сам скинет игроку что надо
            pl.sendMessage(msg.build(ce, true, sender.eng));
        }

        //отправить в консоль
        Bukkit.getConsoleSender().sendMessage(viewerResultRU);
    }

    /*private static Component[] format(final ChatPrepareEvent ce, final boolean eng, final boolean clr) {
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
                    .widen(new Callback(msg, ce.getOplayer()), Player.class), OPTIONS));
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
                    .widen(new Callback(sbm, ce.getOplayer()), Player.class), OPTIONS)));
            mb.append(Component.text(s, NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                .hoverEvent(url_ttp).clickEvent(ClickEvent.openUrl(s)));
            sb.setLength(0);
        }
        if (!sb.isEmpty()) {
            final String sbm = sb.toString();
            mb.append(TCUtil.form(sbm)
                .hoverEvent(msg_ttp).clickEvent(ClickEvent.callback(ClickCallback
                    .widen(new Callback(sbm, ce.getOplayer()), Player.class), OPTIONS)));
        }
        return reply == null ? new Component[]{mb.build()} : new Component[]{TCUtil.form(reply), mb.build()};
    }

    private static final int MAX_LEN = 40;
    private record Callback(String msg, Oplayer sender) implements ClickCallback<Player> {
        public void accept(final Player pl) {
            final String strip = TCUtil.strip(msg);
            final Oplayer op = PM.getOplayer(pl);
            if (op == null) return;
            final String name = sender.isGuest ? sender.getDataString(Data.FAMILY) : sender.nik;
            pl.openBook(Book.book(TCUtil.form("<dark_aqua>Опции"), Component.text(name), new Component[] {
                TCUtil.form("<dark_aqua><b><shadow:#000000FF>Опции:<reset>\n\n")
                    .append(TCUtil.form((strip.length() > MAX_LEN ? strip.substring(0, MAX_LEN) + ".." : msg) + "\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<yellow>Клик <gold>- копировать")))
                        .clickEvent(ClickEvent.copyToClipboard(msg)))

                    .append(TCUtil.form("<amber>╘› <gold>Ответить<reset>\n\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<aqua>Клик <dark_aqua>- ответить")))
                        .clickEvent(ClickEvent.callback(ClickCallback.widen(p ->
                            PlayerInput.get(InputButton.InputType.CHAT, p, rpl -> {
                            final Component reply = TCUtil.form("<gray>┌─ <font:uniform>" + NIK_COLOR
                                    + name + " <reset><font:uniform><gray>► " + msg + SPLIT.get() + rpl)
                                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.NOT_SET);
                            Ostrov.async(() -> chat(p, reply, new HashSet<>(Bukkit.getOnlinePlayers())));
                        }, ""), Player.class))))

                    .append(TCUtil.form("<dark_green><b><shadow:#000000FF>Отправитель:<reset>\n"))
                    .append(TCUtil.form(TCUtil.sided("<u>" + NIK_COLOR + name + "</u>") + "\n\n")
//                        .hoverEvent(HoverEvent.showText(TCUtil.form("<gold>Клик <amber>- написать ЛС")))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + name + " ")))

                    .append(TCUtil.form("<dark_gray>› Игнорировать<reset>\n\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<pink>Клик <dark_purple>- добавить в ЧС")))
                        .clickEvent(ClickEvent.runCommand("/ignore " + sender.nik)))

                    .append(op.isStaff ? TCUtil.form("<stale><cardinal>› Замутить<reset>")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<red>Клик <dark_red>- выдать мут")))
                        .clickEvent(ClickEvent.suggestCommand("/mute " + sender.nik + " 10m "))

                    : TCUtil.form("<stale><cardinal>› Пожаловаться<reset>")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<red>Клик <dark_red>- подать жалобу")))
                        .clickEvent(ClickEvent.callback(ClickCallback.widen(p -> PlayerInput.get(InputButton.InputType.ANVILL, p, rpl -> {
                            //TODO жалобы на чат
                        }, "Жалоба"), Player.class))))
            }));
        }
    }*/

    //с прокси пришло сообшение от другого сервера по новому каналу
    //приходят 2 волны - на русском и английком
    @Deprecated
    public static void onProxyChat(final Chanell ch, final int proxyId, final String serverName, final String senderName, final String msg) {
        onProxyChat(ch, senderName, msg);
    }

    public static void onProxyChat(final Chanell ch, final String sender, final String msg) {
        final Component c = new Message(msg).build(ch == Chanell.CHAT_EN);
        for (Player p : Bukkit.getOnlinePlayers()) {
            final Oplayer to = PM.getOplayer(p);
            if (to == null) continue;
            if (to.isBlackListed(sender)
                || to.isLocalChat()) continue;
            //русским русский чат
            if (to.eng && ch == Chanell.CHAT_EN) p.sendMessage(c);
            else //остальным показываем английскую версию
                if (!to.eng && ch == Chanell.CHAT_RU) p.sendMessage(c);
        }
    }
}
