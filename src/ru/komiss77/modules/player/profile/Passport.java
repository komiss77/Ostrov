package ru.komiss77.modules.player.profile;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.Duo;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.InputButton.InputType;


public class Passport {

    private static final Duo<String, String> COLORS1 = Duo.of("<black>", "<stale>"),
        COLORS2 = Duo.of("<dark_blue>", "<indigo>");
    private static final int PASS_ROWS = 14;
    private static final Data[] PASS_DATA;

    static {
        final List<Data> dts = new ArrayList<>(PASS_ROWS);
        final Data[] vals = Data.values();
        for (int i = 0; i != PASS_ROWS; i++) {
            for (final Data dt : vals) {
                if (dt.passSeq != i) continue;
                dts.add(dt); break;
            }
        }
        PASS_DATA = dts.toArray(new Data[0]);
    }

    private static final int MAX_LEN = 32;
    private static final int LINE_LEN = 14;
    private static final int PAGE_LINES = 5;
    public static void open(final Player p, final Oplayer op) {
        //Имя, Фамилия | Дата Рождения | Пол | Город | Страна | Телефон | Эл. Почта | ВКонтакте | Discord | YouTube | О Себе | Защита по IP | Пароль | Примечания
        int cnt = 0;
        p.closeInventory();
        final List<Component> cList = new ArrayList<>(PASS_DATA.length);
        for (final Data dt : PASS_DATA) {
            if (dt == null) continue;
            final Duo<String, String> color = (cnt++ & 1) == 0 ? COLORS1 : COLORS2;
            cList.add(TCUtil.form("<reset>" + color.key() + dt.desc + ":").appendNewline()
                .append(TCUtil.form(color.val() + " <u>" + adapt(dt, op.getDataString(dt)))
                    .hoverEvent(HoverEvent.showText(TCUtil.form(color.val() + "Клик <gray>- изменить")))
                    .clickEvent(ClickEvent.callback(ClickCallback.widen(pl -> {
                        if (p.getEntityId() != pl.getEntityId()) return;
                        if (dt == Data.GENDER) {
                            switch (op.gender) {
                                case NEUTRAL -> {
                                    op.globalStr(Data.GENDER, "Мальчик");
                                    op.gender = PM.Gender.MALE;
                                }
                                case MALE -> {
                                    op.globalStr(Data.GENDER, "Девочка");
                                    op.gender = PM.Gender.FEMALE;
                                }
                                case FEMALE -> {
                                    op.globalStr(Data.GENDER, "");
                                    op.gender = PM.Gender.NEUTRAL;
                                }
                            }
                            p.playSound(p.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.6f, 1);
                            open(p, op);
                            return;
                        }
                        PlayerInput.get(InputType.ANVILL, pl, val -> {
                            final String stv = TCUtil.strip(val);
                            switch (dt) {
                                case BIRTH:
                                    if (!parseDate(stv)) {
                                        p.sendMessage(Ostrov.PREFIX + "§cФормат ДД.ММ.ГГГГ");
                                        return;// "Формат ДД.ММ.ГГГГ";
                                    }
                                    break;
                                case VK, DISCORD, YOUTUBE:
                                    if (!parseURL(stv)) {
                                        p.sendMessage(Ostrov.PREFIX + "§cЭто не ссылка! Попробуй что-то вроде §ehttps://site.ostrov77.ru/local");
                                        return;// "это не URL ссылка";
                                    }
                                    break;
                                case EMAIL:
                                    if (!parseMail(stv)) {
                                        p.sendMessage(Ostrov.PREFIX + "§cЭл. Почта должна выглядеть как §6" + p.getName() + "@ostrov77.ru");
                                        return;// "пример: komiss77@ostrov77.ru";
                                    }
                                    break;
                                case PHONE:
                                    final String digits = stv.replaceAll("\\D+", "");
                                    if (digits.length() != 10) {
                                        p.sendMessage(Ostrov.PREFIX + "§cНомер должен быть похож на (911) 777-7777");
                                        return;// "пример: (911) 777-7777";
                                    }
                                    op.globalStr(dt, "(" + digits.substring(0, 3) + ") "
                                        + digits.substring(3, 6) + "-" + digits.substring(6, 10));
                                    p.playSound(p.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.2f, 1);
                                    open(pl, op);
                                    break;
                            }
                            if (stv.length() > MAX_LEN) {
                                final String csv = stv.substring(0, MAX_LEN);
                                pl.sendMessage(Ostrov.PREFIX + "§6Данные обрезаны для сохранения:");
                                pl.sendMessage(TCUtil.form("<gold>(<yellow>" + csv + "<gold>)"));
                                op.globalStr(dt, csv);
                                p.playSound(p.getLocation(), Sound.UI_STONECUTTER_TAKE_RESULT, 1.2f, 1);
                                open(pl, op);
                                return;
                            }

                            op.globalStr(dt, stv);
                            p.playSound(p.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.2f, 1);
                            open(pl, op);
                        }, op.getDataString(dt));
                    }, Player.class)))));
        }

        final Component[] book = new Component[cList.size() / PAGE_LINES + 1];
        for (int i = 0; i != book.length; i++) {
            book[i] = TCUtil.form("        <shadow:#000000FF><b><gradient:stale:gold>Паспорт</gradient>\n  <gradient:stale:gold>Островитянина</shadow>\n");
        }
        for (int i = 0; i != PASS_DATA.length; i++) {
            if (PASS_DATA[i] == null) continue;
            final int page = i / PAGE_LINES;
            book[page] = (book[page] == null ? cList.get(i)
                : book[page].append(cList.get(i))).appendNewline();
        }
        p.openBook(Book.book(TCUtil.form("<gradient:stale:gold>Паспорт Островитянина"), Component.text(p.getName()), book));
    }

    private static String adapt(final Data dt, final String val) {
        if (dt == Data.GENDER) return val.isBlank() ? "Создание ;)" : val;
        if (val.isBlank()) return "Не указано";
        final int length = LINE_LEN << (dt == Data.ABOUT || dt == Data.NOTES ? 1 : 0);
        return val.length() > length ? val.substring(0, length) + ".." : val;
    }

    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("dd.MM.yyyy");
    private static boolean parseDate(final String input) {
        DATE_FORMAT.setLenient(false);
        try {
            final Date date = DATE_FORMAT.parse(input);
            return DATE_FORMAT.format(date).equals(input);
        } catch (final ParseException ex) {
            return false;
        }
    }

    public static boolean parseURL(final String url) {
        try {return URI.create(url).toURL().getProtocol() != null;}
        catch (IllegalArgumentException | MalformedURLException e) {return false;}
    }

    private static final Pattern MAIL_PAT = Pattern.compile(
        "^([\\w-.]+){1,64}@(\\w&&[^_]+){2,255}.[a-z]{2,}$", Pattern.CASE_INSENSITIVE);
    public static boolean parseMail(final String email) {
        return MAIL_PAT.matcher(email).find();
    }
}
