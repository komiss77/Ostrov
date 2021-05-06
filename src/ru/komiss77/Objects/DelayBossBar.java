package ru.komiss77.Objects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import ru.komiss77.Managers.Timer;





public class DelayBossBar {

    private final String nik;
    private final List<BossBar> bossbars;
    private int delay;
    private BossBar current_bar;

    public DelayBossBar(Player p, final String msg, int show_time_sec, BarColor bar_color, BarStyle bar_style, boolean show_progress) {
        this.nik=p.getName();
        this.bossbars=new ArrayList<>();
        this.bossbars.add(new BossBar(p, msg, show_time_sec, bar_color, bar_style, show_progress));
        this.delay=0;
    }

    public void AddBar(Player p, final String msg, int show_time_sec, BarColor bar_color, BarStyle bar_style, boolean show_progress){
        for (BossBar b:this.bossbars) {
            if ( b.p.getName().equals(p.getName()) && b.msg.equals(msg) && b.show_time_sec==show_time_sec && b.bar_color==bar_color && b.bar_style==bar_style && b.show_progress==show_progress ) return;
        }
        this.bossbars.add(new BossBar(p, msg, show_time_sec, bar_color, bar_style, show_progress));
        if (this.delay<0)this.delay=0;
    }
    
    public void DoTick(){
        if (this.delay==0) {
            if (Bukkit.getPlayer(this.nik)==null) Remove();
            if (this.current_bar!=null){
                //this.current_bar.removePlayer(Bukkit.getPlayer(this.nik));
                this.current_bar.removeAll();
                this.current_bar=null;
            }
            if (!this.bossbars.isEmpty()) {
                this.current_bar=this.bossbars.get(0);
                this.delay=this.current_bar.show_time_sec+1;
                this.current_bar.Send();
                this.bossbars.remove(0);
            }
        } else if (this.delay>0 && this.current_bar!=null) this.current_bar.DoTick();
        this.delay--;
        if (this.delay==-6) Remove();
    }
    
    public void Remove(){
        Timer.delay_bossbars.remove(this.nik);
    }    
    
    
}
