package ru.komiss77.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BarColor;


public class BossBar 
{
    public org.bukkit.boss.BossBar bar;
    public Player p;
    public String msg;
    public int show_time_sec;
    public BarColor bar_color;
    public BarStyle bar_style;
    public boolean show_progress;
    public double step;


    
    public BossBar(final Player p, final String msg, int show_time_sec, BarColor bar_color, BarStyle bar_style, boolean show_progress ) {
        this.p=p;
        this.msg=msg;
        this.show_time_sec=show_time_sec;
        if (bar_color==null) this.bar_color=BarColor.WHITE; else this.bar_color=bar_color;
        if (bar_style==null) this.bar_style=BarStyle.SOLID; else this.bar_style=bar_style;
        this.show_progress=show_progress;
        if (this.show_progress) this.step= (double)1/this.show_time_sec; else this.step=0;
    }
    
    public void Send(){
        if (this.p!=null && this.p.isOnline()) {
           //this.bar = new CraftBossBar(msg, bar_color, bar_style, new BarFlag[0]);
           this.bar = Bukkit.createBossBar(msg, bar_color, bar_style, new BarFlag[0]);
           this.addPlayer(p);
        }
    }    
    
    
    
    public void DoTick(){
        if (this.show_progress) {
            double progress = bar.getProgress()-this.step;
            if (progress<0) progress=0; else if (progress>1) progress=1;
            this.setProgress(progress);
        }
    }


    public void addPlayer(final Player player) {
        this.bar.addPlayer(player);
    }
    
    public void removePlayer(final Player player) {
        this.bar.removePlayer(player);
    }
    
    public void removeAll() {
        this.bar.removeAll();
    }
    
    
    
    
    public void setTitle(final String title) {
        this.bar.setTitle(title);
    }
    
    public void setProgress(final double progress) {
        this.bar.setProgress(progress);
    }
    
    
    
    
}
