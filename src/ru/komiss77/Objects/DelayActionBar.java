package ru.komiss77.Objects;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import ru.komiss77.Managers.Timer;





public class DelayActionBar {

    private final String nik;
    private final List<String> texts;
    private int delay;

    public DelayActionBar(String nik, String msg) {
        this.nik=nik;
        this.texts=new ArrayList<>();
        this.texts.add(msg);
        this.delay=0;
    }

    public void AddMsg(String msg){
        if(!this.texts.contains(msg) && this.texts.size()<7) {
            this.texts.add(msg);
            if (this.delay<0)this.delay=0;
        }
    }
    
    public void DoTick(){
        if (this.delay==0) {
            if (Bukkit.getPlayer(this.nik)==null) Remove();
            else if (!this.texts.isEmpty()) {
                //NmsUtils.sendActionBar(Bukkit.getPlayer(this.nik), this.texts.get(0));
                Bukkit.getPlayer(this.nik).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(this.texts.get(0)));
                this.texts.remove(0);
                this.delay=3;
            }
        } 
        if (this.delay==2) {
            if (Bukkit.getPlayer(this.nik)==null) Remove();
            else if (!this.texts.isEmpty()) {
                //NmsUtils.sendActionBar(Bukkit.getPlayer(this.nik), this.texts.get(0));
                Bukkit.getPlayer(this.nik).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(this.texts.get(0)));
            }
        } 
        this.delay--;
        if (this.delay==-6) Remove();
        //if (this.delay==-6) {
        //    if (this.texts.isEmpty()) Remove();
        //    else this.delay=0;
        //}
    }
    
    public void Remove(){
        Timer.delay_actionbars.remove(this.nik);
    }    
    

        
}
