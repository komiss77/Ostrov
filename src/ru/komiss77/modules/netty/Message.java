package ru.komiss77.modules.netty;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Game;
import ru.komiss77.events.ChatPrepareEvent;
import ru.komiss77.listener.ChatLst;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.InputButton;

public class Message {

    private static final CaseInsensitiveMap<Part> partMap = new CaseInsensitiveMap<>();
    private static final StringUtil.Split PART_SPLIT = StringUtil.Split.LARGE;
    private static final StringUtil.Split KEY_SPLIT = StringUtil.Split.MEDIUM;
    private static final Part[] VALUES = Part.values();

    public enum Part {
        SERVER, //режим
        SENDER, //имя

        ENGLISH, //язык
        PREFIX, //...
        SUFFIX, //...
        PROFILE, //HoverEvent над ником

        MESSAGE, //текст
        TOP_DATA, //линия сверху, в основном для ответов
        GAME_INFO; //инфа игрока на режиме

        Part() {partMap.put(name(), this);}
    }

    private static final ClickCallback.Options OPTIONS = ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(8)).build();
    private static final HoverEvent<Component> PREFIX_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §оХочешь префикс? Жми!  §7-=[§я✦§7]"));
    private static final HoverEvent<Component> SUFFIX_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §сХочешь суффикс? Жми!  §7-=[§я✦§7]"));
    private static final HoverEvent<Component> MSG_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("<beige>🢖 Клик §3- Опции"));
    private static final HoverEvent<Component> MSG_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("<beige>🢖 Click §3- Options"));
    private static final HoverEvent<Component> URL_TOOLTIP_RU = HoverEvent.showText(TCUtil.form("§9Клик - перейти по <u>ссылке"));
    private static final HoverEvent<Component> URL_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§9Click - open <u>URL"));
//    private static final HoverEvent<Component> SUGGEST_MUTE_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§кClick - mute player"));
    private static final HoverEvent<Component> PREFIX_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §оWant a prefix? Click here!  §7-=[§я✦§7]"));
    private static final HoverEvent<Component> SUFFIX_TOOLTIP_EN = HoverEvent.showText(TCUtil.form("§7[§я✦§7]=-  §сWant a suffix? Click here!  §7-=[§я✦§7]"));
    private static final ClickEvent DONATE_CLICK_URL = ClickEvent.openUrl("http://www.ostrov77.ru/donate.html");
    private static final Format[] FORMATS = new Format[] {new Format("**", "<b>", "</b>"), new Format("*", "<i>", "</i>"),
        new Format("||", "<obf>", "</obf>"), new Format("|", "<obf>", "</obf>"), new Format("~~", "<st>", "</st>"),
        new Format("~", "<st>", "</st>"), new Format("__", "<u>", "</u>"), new Format("_", "<i>", "</i>")};
    public static final TextColor MSG_COLOR = NamedTextColor.GRAY;
    private static final String ARROW = "➠";

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

    private final String[] data = new String[VALUES.length];

    public Message(final String proxy) {
        final String[] params = PART_SPLIT.split(proxy);
        for (final String comp : params) {
            final String[] csp = KEY_SPLIT.split(comp, true);
            final Part pr = partMap.get(csp[0]);
            if (pr == null || csp.length == 1) continue;
            data[pr.ordinal()] = csp[1];
        }
    }

    public Message(final ChatPrepareEvent e) {
        final Oplayer senderOp = e.getOplayer();
        for (final Part pt : VALUES) {
            data[pt.ordinal()] = switch (pt) {
                case SERVER -> GM.GAME.suggestName;
                case SENDER -> senderOp.isGuest
                    ? senderOp.globalStr(Data.FAMILY) : senderOp.nik;
                case ENGLISH -> String.valueOf(senderOp.eng);
                case PREFIX -> senderOp.globalStr(Data.PREFIX);
                case SUFFIX -> senderOp.globalStr(Data.SUFFIX);
                case PROFILE -> e.profileTip();
                case GAME_INFO -> e.proxyInfo();
                case TOP_DATA -> e.topData();
                case MESSAGE -> {
                    String msg = e.getMessage();
                    if (!Perm.canColorChat(e.getPlayer(), senderOp)) yield msg;
                    for (final Format frm : FORMATS) msg = frm.process(msg);
                    yield msg.replace('&', '§').replace("\\", "");
                }
            };
        }
    }

    public String toString() {
        final List<String> sls = new LinkedList<>();
        for (int i = 0; i != data.length; i++) {
            if (!has(VALUES[i])) continue;
            sls.add(KEY_SPLIT.join(VALUES[i].name(), data[i]));
        }
        return PART_SPLIT.join(sls);
    }

    public boolean has(final Part part) {
        final String dt = data[part.ordinal()];
        return dt != null && !dt.isBlank();
    }

    public @Nullable String data(final Part part) {
        return data[part.ordinal()];
    }

    public void data(final Part part, final @Nullable String str) {
        data[part.ordinal()] = str;
    }

    public Component build(final boolean eng) {
        final TextComponent.Builder mb = Component.text();
        if (has(Part.TOP_DATA)) {
            mb.append(TCUtil.form(data(Part.TOP_DATA)))
                .append(Component.newline().style(Style.empty()));
        }
        final Game gm = Game.fromServerName(data(Part.SERVER));
        mb.append(TCUtil.form(gm.defaultlogo)
            .hoverEvent(HoverEvent.showText(TCUtil.form(gm.displayName
                + (eng ? "\n<dark_gray>(Click - go there)" : "\n<dark_gray>(Клик - перейти)"))))
            .clickEvent(ClickEvent.suggestCommand("/server " + gm.defaultServer)));

        if (has(Part.GAME_INFO)) {
            mb.append(TCUtil.form(data(Part.GAME_INFO)))
                .append(Component.space().style(Style.empty()));
        }

        if (has(Part.PREFIX)) {
            mb.append(TCUtil.form(data(Part.PREFIX))
                    .hoverEvent(eng ? PREFIX_TOOLTIP_EN : PREFIX_TOOLTIP_RU)
                    .clickEvent(DONATE_CLICK_URL))
                .append(Component.space().style(Style.empty()));
        }

        final String name = has(Part.SENDER)
            ? data(Part.SENDER) : (eng ? "Unknown" : "Нечто");
        mb.append(TCUtil.form(ChatLst.NIK_COLOR + name)
                .hoverEvent(has(Part.PROFILE)
                    ? HoverEvent.showText(TCUtil.form(data(Part.PROFILE))) : null)
                .clickEvent(ClickEvent.suggestCommand("/msg " + name + " ")))
            .append(Component.space().style(Style.empty()));

        if (has(Part.SUFFIX)) {
            mb.append(TCUtil.form(data(Part.SUFFIX))
                    .hoverEvent(eng ? SUFFIX_TOOLTIP_EN : SUFFIX_TOOLTIP_RU)
                    .clickEvent(DONATE_CLICK_URL))
                .append(Component.space().style(Style.empty()));
        }

        mb.append(Component.text(ARROW + " ", MSG_COLOR));

        if (has(Part.MESSAGE)) {
            mb.append(format(data(Part.MESSAGE), name, eng)
                .colorIfAbsent(MSG_COLOR));
        }
        return mb.build();
    }

    public Component build(final ChatPrepareEvent ce, final boolean self, final boolean eng) {
        final TextComponent.Builder mb = Component.text();
        if (has(Part.TOP_DATA)) {
            mb.append(TCUtil.form(data(Part.TOP_DATA)))
                .append(Component.newline().style(Style.empty()));
        }
        final Game gm = Game.fromServerName(data(Part.SERVER));
        mb.append(TCUtil.form(gm.defaultlogo)
            .hoverEvent(HoverEvent.showText(TCUtil.form(gm.displayName
                + (eng ? "\n<dark_gray>(Click - go there)" : "\n<dark_gray>(Клик - перейти)"))))
            .clickEvent(ClickEvent.suggestCommand("/server " + gm.defaultServer)));

        /*if (msg.has(Part.GAME_INFO)) {
            mb.append(TCUtil.form(msg.data(Part.GAME_INFO)))
                .append(Component.space().style(Style.empty()));
        }*/
        if (self) {
            if (ce.getSenderGameInfo() != null) mb.append(ce.getSenderGameInfo())
                .append(Component.space().style(Style.empty()));
        } else {
            if (ce.getViewerGameInfo() != null) mb.append(ce.getViewerGameInfo())
                .append(Component.space().style(Style.empty()));
        }

        if (has(Part.PREFIX)) {
            mb.append(TCUtil.form(data(Part.PREFIX))
                    .hoverEvent(eng ? PREFIX_TOOLTIP_EN : PREFIX_TOOLTIP_RU)
                    .clickEvent(DONATE_CLICK_URL))
                .append(Component.space().style(Style.empty()));
        }

        final String name = has(Part.SENDER)
            ? data(Part.SENDER) : (eng ? "Unknown" : "Нечто");
        mb.append(TCUtil.form(ChatLst.NIK_COLOR + name)
                .hoverEvent(has(Part.PROFILE)
                    ? HoverEvent.showText(TCUtil.form(data(Part.PROFILE))) : null)
                .clickEvent(ClickEvent.suggestCommand("/msg " + name + " ")))
            .append(Component.space().style(Style.empty()));

        if (has(Part.SUFFIX)) {
            mb.append(TCUtil.form(data(Part.SUFFIX))
                    .hoverEvent(eng ? SUFFIX_TOOLTIP_EN : SUFFIX_TOOLTIP_RU)
                    .clickEvent(DONATE_CLICK_URL))
                .append(Component.space().style(Style.empty()));
        }

        mb.append(Component.text(ARROW + " ", MSG_COLOR));

        if (has(Part.MESSAGE)) {
            mb.append(format(data(Part.MESSAGE), name, eng)
                .colorIfAbsent(MSG_COLOR));
        }
        return mb.build();
    }

    private static Component format(final String msg, final String name, final boolean eng) {
        final HoverEvent<Component> url_ttp = eng ? URL_TOOLTIP_EN : URL_TOOLTIP_RU;
        final HoverEvent<Component> msg_ttp = eng ? MSG_TOOLTIP_EN : MSG_TOOLTIP_RU;
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
                    .widen(new Callback(sbm, name), Player.class), OPTIONS)));
            mb.append(Component.text(s, NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                .hoverEvent(url_ttp).clickEvent(ClickEvent.openUrl(s)));
            sb.setLength(0);
        }
        if (!sb.isEmpty()) {
            final String sbm = sb.toString();
            mb.append(TCUtil.form(sbm)
                .hoverEvent(msg_ttp).clickEvent(ClickEvent.callback(ClickCallback
                    .widen(new Callback(sbm, name), Player.class), OPTIONS)));
        }
        return mb.build();
    }

    private static final int MAX_LEN = 40;
    private record Callback(String msg, String name) implements ClickCallback<Player> {
        public void accept(final Player pl) {
            final String strip = TCUtil.strip(msg);
            final Oplayer op = PM.getOplayer(pl);
            if (op == null) return;
            pl.openBook(Book.book(TCUtil.form("<dark_aqua>Опции"), Component.text(name), new Component[] {
                TCUtil.form("<dark_aqua><b><shadow:#000000FF>Опции:<reset>\n\n")
                    .append(TCUtil.form((strip.length() > MAX_LEN ? strip.substring(0, MAX_LEN) + ".." : msg) + "\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<yellow>Клик <gold>- копировать")))
                        .clickEvent(ClickEvent.copyToClipboard(msg)))

                    .append(TCUtil.form("<amber>╘› <gold>Ответить<reset>\n\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<aqua>Клик <dark_aqua>- ответить")))
                        .clickEvent(ClickEvent.callback(ClickCallback.widen(p ->
                            PlayerInput.get(InputButton.InputType.CHAT, p, rpl -> {
                                final Component reply = TCUtil.form(ChatLst.TOP_SPLIT.join("<gray>┌─ <font:uniform>"
                                    + ChatLst.NIK_COLOR + name + " <reset><font:uniform><gray>► " + msg, rpl));
                                Ostrov.async(() -> ChatLst.chat(p, reply, new HashSet<>(Bukkit.getOnlinePlayers())));
                            }, ""), Player.class))))

                    .append(TCUtil.form("<dark_green><b><shadow:#000000FF>Отправитель:\n"))
                    .append(TCUtil.form("- <u>" + ChatLst.NIK_COLOR + name + "</u>\n\n")
//                        .hoverEvent(HoverEvent.showText(TCUtil.form("<gold>Клик <amber>- написать ЛС")))
                        .clickEvent(ClickEvent.suggestCommand("/msg " + name + " ")))

                    .append(TCUtil.form("<dark_gray>› Игнорировать<reset>\n\n")
                        .hoverEvent(HoverEvent.showText(TCUtil.form("<pink>Клик <dark_purple>- добавить в ЧС")))
                        .clickEvent(ClickEvent.runCommand("/ignore " + name)))

                    .append(op.isStaff ? TCUtil.form("<stale><cardinal>› Замутить<reset>")
                    .hoverEvent(HoverEvent.showText(TCUtil.form("<red>Клик <dark_red>- выдать мут")))
                    .clickEvent(ClickEvent.suggestCommand("/mute " + name + " 10m "))

                    : TCUtil.form("<stale><cardinal>› Пожаловаться<reset>")
                    .hoverEvent(HoverEvent.showText(TCUtil.form("<red>Клик <dark_red>- подать жалобу")))
                    .clickEvent(ClickEvent.callback(ClickCallback.widen(p -> PlayerInput.get(InputButton.InputType.ANVILL, p, rpl -> {
                        //TODO жалобы на чат
                    }, "Жалоба"), Player.class))))
            }));
        }
    }
}
