package ru.komiss77.utils;

import java.time.Duration;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.DelayBossBar;

public class ScreenUtil {


    public static void sendTitle(final Player p, final String title, final String subtitle) {
        sendTitle(p, title, subtitle, 20, 40, 20);
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendTitle(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout) {
        final Title.Times times = Title.Times.times(Duration.ofMillis(fadein * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeout * 50L));
        sendTitle(p, TCUtil.form(title), TCUtil.form(subtitle), times);
    }

    public static void sendTitle(final Player p, final Component title, final Component subtitle, final int fadein, final int stay, final int fadeout) {
        final Title.Times times = Title.Times.times(Duration.ofMillis(fadein * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeout * 50L));
        sendTitle(p, title, subtitle, times);
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendTitle(final Player p, final Component title, final Component subtitle, final Title.Times times) {
        final Oplayer op = PM.getOplayer(p);
        final Title t = Title.title(title, subtitle, times);
        if (op != null) { //на авторизации нет оплеера!
            if (op.nextTitle > 0) {
                op.delayTitles.add(t);
            } else {
                p.showTitle(t);
                op.nextTitle = times.fadeIn().toSecondsPart() + times.stay().toSecondsPart() + times.fadeOut().toSecondsPart() + 1;
            }
        } else {
            p.showTitle(t);
        }

    }

    public static void sendTitleDirect(final Player p, final String title, final String subtitle) {
        sendTitleDirect(p, title, subtitle, 20, 40, 20);
    }

    public static void sendTitleDirect(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout) {
        final Title.Times times = Title.Times.times(Duration.ofMillis(fadein * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeout * 50L));
        p.showTitle(Title.title(TCUtil.form(title), TCUtil.form(subtitle), times));
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendActionBar(final Player p, final String text) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) { //на авторизации нет оплеера!
            if (op.nextAb > 0) {
                op.delayActionbars.add(text);
            } else {
                op.nextAb = Oplayer.ACTION_BAR_INTERVAL;
                p.sendActionBar(TCUtil.form(text));
            }
        } else {
            p.sendActionBar(TCUtil.form(text));
        }
    }

    public static void sendActionBarDirect(final Player p, final String text) {
        if (p != null) {
            p.sendActionBar(TCUtil.form(text));
        }
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbar(final Player p, final String text, final int seconds,
                                   final BossBar.Color color, final BossBar.Overlay style, final float progress) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) {
            if (op.barTime > 0) {
                op.delayBossBars.add(new DelayBossBar(text, seconds, color, style, progress, false));
            } else {
                DelayBossBar.apply(op, p, text, seconds, color, style, progress, false);
            }
        }
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbarDirect(final Player p, final String text, final int seconds,
                                         final BossBar.Color color, final BossBar.Overlay style, final float progress) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) DelayBossBar.apply(op, p, text, seconds, color, style, progress, false);
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbar(final Player p, final String text,
                                   final int seconds, final BossBar.Color color, final BossBar.Overlay style) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) {
            if (op.barTime > 0) {
                op.delayBossBars.add(new DelayBossBar(text, seconds, color, style, 1f, true));
            } else {
                DelayBossBar.apply(op, p, text, seconds, color, style, 1f, true);
            }
        }
    }

    // сообщения сохраняются и выводятся поочерёдно
    public static void sendBossbarDirect(final Player p, final String text,
                                         final int seconds, final BossBar.Color color, final BossBar.Overlay style) {
        final Oplayer op = PM.getOplayer(p);
        if (op != null) DelayBossBar.apply(op, p, text, seconds, color, style, 1f, true);
    }

    public static void sendTabList(final Player p, final String header, final String footer) {
        p.sendPlayerListHeaderAndFooter(TCUtil.form(header), TCUtil.form(footer));
    }
}
