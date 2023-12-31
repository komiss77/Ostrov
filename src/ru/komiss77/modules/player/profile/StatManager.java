package ru.komiss77.modules.player.profile;

import java.util.EnumSet;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Perm;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.StatChangeEvent;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.Group;



public class StatManager {
    
    public static final EnumSet<Data> passportData = EnumSet.of( Data.PREFIX, Data.SUFFIX, Data.PHONE, Data.EMAIL, Data.FAMILY, Data.GENDER, Data.BIRTH, Data.LAND, 
            Data.CITY, Data.ABOUT, Data.DISCORD, Data.VK, Data.MARRY, Data.YOUTUBE) ;
    


    public static void addStat(final Player p, final Stat stat, int ammount) {
//System.out.println("-addIntStat e_stat="+stat+"+"+ammount);
        if (p==null || ammount<0) {
            Ostrov.log_warn("addStat ammount<0 для "+(p==null? "null":p.getName())+", stat="+stat.name()+", ammount="+ammount);
            return;
        }
        final Oplayer op = PM.getOplayer(p.getName());
        if (op==null) {
            Ostrov.log_warn("addStat op==null для "+p.getName()+", stat="+stat.name()+", ammount="+ammount);
            return;
        }
        int currentStatValue = op.getStat(stat);
        int newStatValue = currentStatValue + ammount;
        
        //**** Эвент. Можно отменить или изменить ammount ****
        final StatChangeEvent e = new StatChangeEvent (p, op, stat, currentStatValue, ammount);
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancelled()) return;
        
        if (e.getAmmount()<=0) {
            Ostrov.log_err("addStat StatChangeEvent: установлена ammount<=0 для "+p.getName()+", "+stat);
            return;
        }
        ammount = e.getAmmount();
        //*************************
        
        op.addStat(stat, ammount); //делать через адд, чтобы добавило дневную!
        
        //**** награда лони ****
        RewardLoni.onStatAdd(p, op, stat, ammount);
        //*************************

        //**** Изменение кармы ****
        karmaCalc(op);
        //*************************
        
        //**** Проверка на ачивку ****
        if (stat.achiv!=null) {
            final int currentLevel = getLevel(stat, currentStatValue);
            final int newLevel = getLevel(stat, newStatValue);
            if (newLevel>currentLevel) {
                //final String achiv = descFromAchiv(stat, 0);
                //ApiOstrov.sendBossbar(p, stat.game.displayName+" : §d"+(achiv.isEmpty() ? "Достижение!" : achiv)+"§7, "+stat.desc+newStatValue, 15, BarColor.YELLOW, BarStyle.SEGMENTED_6, false);
                //ApiOstrov.sendTitle(p, achiv.isEmpty() ? "§fДостижение!" : "§e"+achiv, stat.game.displayName+"§7, "+stat.desc+newStatValue, 20, 40, 20);
                ApiOstrov.sendBossbar(p, Lang.t(p, stat.game.displayName)+" : §d"+(newLevel==5 ? "Достижение!" : topAdv(stat))+"§7, "+Lang.t(p, stat.desc)+newStatValue, 8, Color.YELLOW, Overlay.NOTCHED_6);
                ApiOstrov.sendTitle(p, newLevel==5 ? "§e"+topAdv(stat) : "§fДостижение!" , Lang.t(p, stat.game.displayName)+"§7, "+Lang.t(p, stat.desc)+newStatValue, 20, 40, 20);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
            }
        }
        //*************************
        
        
        //**** Накинуть опыт ****
        if (stat.exp_per_point>0) {
            op.addExp(p, stat.exp_per_point * ammount);
        }
        //*************************
        
    }
    

    public static void onCustomStat(final Player p, final Oplayer op, final String customStatName, final int ammount) {
         RewardLoni.onCustomStat(p, op, customStatName, ammount);
    }
    
    
    
    
    
    public static void recalc(final Oplayer op) { //когда данные с банжи получены или изменили паспорт
        karmaCalc(op);
        reputationCalc(op);
    }
    
    //public static void karmaBaseChange(Oplayer op, int value) { //только события - группы,пандора. Без ограничений
    //    op.setData(Data.KARMA, op.getDataInt(Data.KARMA)+value);
   //     karmaCalc(op);
   // }
    
    //public static void reputationBaseChange(final Oplayer op, final int value) {  //срабатывает от бан,мут,покупка групп 
    //    op.setData(Data.REPUTATION, op.getDataInt(Data.REPUTATION)+value);
    //    reputationCalc(op);
   // }    

    public static void karmaCalc(final Oplayer op) {
        op.karmaCalc = op.getDataInt(Data.KARMA) + op.getKarmaModifier(Stat.KarmaChange.ADD) - op.getKarmaModifier(Stat.KarmaChange.SUB);
        if (op.karmaCalc>100) op.karmaCalc=100;
        else if (op.karmaCalc<-100) op.karmaCalc = -100;
//System.out.println("-karmaChange() current="+current_carma+" new="+bp.getIntData(Data.КАРМА));        
    }    
    
    
    public static void reputationCalc(final Oplayer op) {
        
        //!!!!!!!!!!создал клан,остров,выбрал класс - проверять через ачивки
        if ( op.getPlayer().hasPermission("ostrov.trust") ) { //if (ApiOstrov.hasGroup(op.nik, "trust")) {
            op.reputationCalc = 100;
            return;
        }
        //базовая
        op.reputationCalc = op.getDataInt(Data.REPUTATION);
        
        //игровые дни
        op.reputationCalc+=op.getStat(Stat.PLAY_TIME)/86400; //24*60*60
        
        //наполненность статы
        op.reputationCalc+=op.getStatFill();
        
        //паспорт
        op.reputationCalc+=getPassportFill(op);
        
        //группы
        op.reputationCalc+=getGroupCounter(op);
        
        //друзья
        op.reputationCalc += op.friends.size();
        
        //репорты
        op.reputationCalc -= op.getDataInt(Data.REPORT_C);
        op.reputationCalc -= op.getDataInt(Data.REPORT_P);
        
        if (op.reputationCalc>100) op.reputationCalc=100;
        else if (op.reputationCalc<-100) op.reputationCalc = -100;
    }    
    
    public static int getPassportFill(final Oplayer op) {
        int count = 0;
        for (Data d:passportData) {
            if (op.hasData(d)) count++;
        }
        return count;
    }

    public static int getGroupCounter(final Oplayer op) {
        int groupCount = 0;
        Group group;
        for (String group_name:op.getGroups()) {
            group=Perm.getGroup(group_name);
            switch (group.name) {
                case "mchat": groupCount+=5;break;
                case "moder": groupCount+=10;break;
                case "moder_spy": groupCount+=30;break;
                case "supermoder": groupCount+=50;break;
                case "xpanitely": groupCount+=70;break;
                case "owner": groupCount+=100;break;
            }
            break;
        }    
        return groupCount;
    }    
    
    
    
    


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static String topAdv (final Stat stat) {
        switch(stat) {
            case BW_game: return "Любитель БедВарс";
            case BW_kill: return "Злой БедВарсер";
            case BW_win: return "Бедварсер-победитель";
            case BW_bed: return "Разоритель гнёзд";
            
            default: return "Предел";
        }
    }

    public static int getLevel(final Stat st, final int value) {
        //потом доработать в зависимости от типа
        //return value>=1000 ? 3 : value>=100 ? 2 : value>=10 ? 1 : 0 ;
        if (st.achiv==null) return 0;
        if (value>=st.achiv[4]) return 5;
        if (value>=st.achiv[3]) return 4;
        if (value>=st.achiv[2]) return 3;
        if (value>=st.achiv[1]) return 2;
        if (value>=st.achiv[0]) return 1;
        return 0;
    }
    
    public static int getLeftToNextLevel(final Stat st, final int value) {
        if (st.achiv==null) return 0;
        if (value<st.achiv[0]) return st.achiv[0]-value;
        if (value<st.achiv[1]) return st.achiv[1]-value;
        if (value<st.achiv[2]) return st.achiv[2]-value;
        if (value<st.achiv[3]) return st.achiv[3]-value;
        if (value<st.achiv[4]) return st.achiv[4]-value;
        return 0;
    }    



    



    
    
}
