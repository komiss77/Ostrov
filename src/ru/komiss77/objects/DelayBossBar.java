package ru.komiss77.objects;

import org.bukkit.entity.Player;
import net.kyori.adventure.bossbar.BossBar;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.TCUtils;






public class DelayBossBar {

    private final String text;
    private final int showTime;
    private final BossBar.Color color;
    private final BossBar.Overlay style;
    private final float progress;
    private final boolean timeBar;

    public DelayBossBar(final String text, final int showTime, final BossBar.Color color, 
    	final BossBar.Overlay style, final float progress, final boolean timeBar) {
        this.text=text;
        this.showTime=showTime;
        this.timeBar=timeBar;
        this.progress=progress;
        this.color=color;
        this.style=style;
    }

    public void apply(final Oplayer op, final Player p) {
        apply(op, p, text, showTime, color, style, progress, timeBar);
    }
    
    public static void apply(final Oplayer op, final Player p, final String text, final int seconds, 
    	final BossBar.Color color, final BossBar.Overlay style, final float progress, final boolean timeBar) {
        op.barTime = seconds;
        op.barMaxTime = seconds;
        op.timeBar = timeBar;
        op.bossbar.color(color);
        op.bossbar.overlay(style);
        op.bossbar.name(TCUtils.format(text));
        op.bossbar.progress(timeBar ? 1f : (progress > 1f ? 1f : progress < 0f ? 0f : progress));
        p.showBossBar(op.bossbar);
    }
    
}
