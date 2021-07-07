package ru.komiss77.objects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import ru.komiss77.Timer;
import ru.komiss77.utils.ChatMsgUtil;





public class DelayTitle {

    private final String nik;
    private final List<Title> titles;
    private int delay;

    public DelayTitle(String nik, String title, String subtitle, int fadein, int stay, int fadeout) {
        this.nik=nik;
        this.titles=new ArrayList<>();
        this.titles.add(new Title(title, subtitle, fadein, stay, fadeout));
        this.delay=0;
    }

    public void AddTitle(String nik, String title, String subtitle, int fadein, int stay, int fadeout){
        for (Title t:this.titles) {
            if ( t.title.equals(title) && t.subtitle.equals(subtitle) && t.fadein==fadein && t.stay==stay && t.fadeout==fadeout) return;
        }
        this.titles.add(new Title(title, subtitle, fadein, stay, fadeout));
        if (this.delay<0)this.delay=0;
    }
    
    public void DoTick(){
        if (this.delay==0) {
            if (Bukkit.getPlayer(this.nik)==null) Remove();
            else if (!this.titles.isEmpty()) {
                //NmsUtils.sendTitle(Bukkit.getPlayer(this.nik), this.titles.get(0).title, this.titles.get(0).subtitle, this.titles.get(0).fadein, this.titles.get(0).stay, this.titles.get(0).fadeout); 1.12
                Bukkit.getPlayer(this.nik).sendTitle( this.titles.get(0).title, this.titles.get(0).subtitle, this.titles.get(0).fadein, this.titles.get(0).stay, this.titles.get(0).fadeout);
                this.delay=(this.titles.get(0).fadein+this.titles.get(0).stay+this.titles.get(0).fadeout)/20+1;
                this.titles.remove(0);
            }
        } 
        this.delay--;
        if (this.delay==-6) Remove();
    }
    
    public void Remove(){
        Timer.delay_titles.remove(this.nik);
    }    
    
    
}
