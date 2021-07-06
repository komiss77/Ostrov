package ru.komiss77.modules.player.profile;

import ru.komiss77.modules.player.PM;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Stat;




public class StatManager {


    public static void addStat(final Player p, final Stat stat, final int ammount) {
//System.out.println("-addIntStat e_stat="+stat+"+"+ammount);
        if (p==null || ammount<0) {
            Ostrov.log_warn("addStat "+p.getName()+", "+stat.name()+", ammount="+ammount);
            return;
        }
        final Oplayer op = PM.getOplayer(p.getName());
        if (op==null) return;
        int currentStatValue = op.getStat(stat);
        int newStatValue = currentStatValue + ammount;
        op.addStat(stat, ammount); //делать через адд, чтобы добавило дневную!
        
        //**** Изменение кармы ****
        int karma = op.getDataInt(Data.KARMA);
        if (stat.toString().endsWith("_win")) karma++;
        else if (stat.toString().endsWith("_loose")) karma--;
        if (karma>100) karma=100;
        else if (karma<-100) karma=-100;
        op.setData( Data.KARMA, String.valueOf(karma));
        //*************************
        
        //**** Проверка на ачивку ****
        if (stat.achiv!=null) {
            final int currentLevel = getLevel(stat, currentStatValue);
            final int newLevel = getLevel(stat, newStatValue);
            if (newLevel>currentLevel) {
                //final String achiv = descFromAchiv(stat, 0);
                //ApiOstrov.sendBossbar(p, stat.game.displayName+" : §d"+(achiv.isEmpty() ? "Достижение!" : achiv)+"§7, "+stat.desc+newStatValue, 15, BarColor.YELLOW, BarStyle.SEGMENTED_6, false);
                //ApiOstrov.sendTitle(p, achiv.isEmpty() ? "§fДостижение!" : "§e"+achiv, stat.game.displayName+"§7, "+stat.desc+newStatValue, 20, 40, 20);
                ApiOstrov.sendBossbar(p, stat.game.displayName+" : §d"+(newLevel==5 ? "Достижение!" : topAdv(stat))+"§7, "+stat.desc+newStatValue, 15, BarColor.YELLOW, BarStyle.SEGMENTED_6, false);
                ApiOstrov.sendTitle(p, newLevel==5 ? "§e"+topAdv(stat) : "§fДостижение!" , stat.game.displayName+"§7, "+stat.desc+newStatValue, 20, 40, 20);
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
    



    public static void karmaChange(final Oplayer op,final int value) {
        int current = op.getDataInt(Data.KARMA);
        current+=value;
        if (current<-100) current = -100;
        else if (current>100) current = 100;
        op.setData(Data.KARMA, current);
//System.out.println("-karmaChange() current="+current_carma+" new="+bp.getIntData(Data.КАРМА));        
    }    
    
    
    public static void reputationChange(final Oplayer op, final int value) {  //срабатывает от бан,мут,покупка групп 
        int current = op.getDataInt(Data.REPUTATION);
        current+=value;
        if (current<-100) current = -100;
        else if (current>100) current = 100;
        
        
        op.setData(Data.REPUTATION, current);
     //   int repBase = op.getDataInt(Data.РЕПУТАЦИЯ_БАЗА) + value;
     //   final int reputation_calc = op.getDataInt(Data.РЕПУТАЦИЯ_РАСЧЁТ);
     //   
       // switch (action) {
          //  case GMUTE: reputation_bd--; break;
         //   case GBAN: reputation_bd-=5; break;
         //   case GBANIP: reputation_bd-=10; break;
            
         //   case GROUP_ADD: reputation_bd+=1; break;
         //   case GROUP_TIME_ADD: reputation_bd+=1; break;
       // }
      //  if (repBase+reputation_calc<100 && repBase+reputation_calc>-100) {
      //      op.setData(Data.РЕПУТАЦИЯ_БАЗА, repBase); //сохран для мускула
      //      op.setData(Data.РЕПУТАЦИЯ, repBase+reputation_calc); //для острова
      //  }
        //if ( reputation_base+reputation<100 && reputation_base+reputation>-100 ) bp.setData(Data.РЕПУТАЦИЯ, String.valueOf(reputation), true);
//System.out.println("-reputationChange() calc="+reputation_calc+" current="+reputation_bd+" new="+bp.getIntData(Data.РЕПУТАЦИЯ));        
    }    
    
    public static void calculateReputationBase(final Oplayer op) { //когда данные с банжи получены или изменили паспорт
      /*  int current_calc=op.getDataInt(Data.РЕПУТАЦИЯ_РАСЧЁТ);
        int new_calc=0;

        //!!!!!!!!!!создал клан,остров,выбрал класс - проверять через ачивки
        Data data;
        for (E_Pass e_pass:E_Pass.values()) {
            data = Data.fromName(e_pass.toString());
            if ( data !=null && !op.getDataInt(data).isEmpty()) new_calc++;  //проверка заполнения данных в паспорте, +1 за каждые данные
        }
        
        new_calc+=(int)(op.GetPlyTime()/1000);
        
        Group group;
        for (String group_name:op.groups) {
            group=OstrovDB.groups.get(group_name);
            if (group.isStaff()) {
                switch (group.name) {
                    case "mchat": new_calc+=5;break;
                    case "moder": new_calc+=10;break;
                    case "moder_spy": new_calc+=30;break;
                    case "supermoder": new_calc+=50;break;
                    case "xpanitely": new_calc+=70;break;
                    case "owner": new_calc+=100;break;
                }
                break;
            }
        }
//System.out.println("----calculateReputationBase reputation_base="+reputation_base);        
        if ( new_calc>100 ) new_calc=100;
        else if ( new_calc<-100 ) new_calc=-100;
//System.out.println("-----calculateReputationBase() current_base="+current_calc+" new_calc="+new_calc+" send change?"+(current_calc!=new_calc));
        if (current_calc!=new_calc) op.setData(Data.РЕПУТАЦИЯ_РАСЧЁТ,new_calc);
        int rep=new_calc+op.getDataInt(Data.РЕПУТАЦИЯ_БАЗА);
        if (rep>100) rep=100;
        else if (rep<-100) rep=-100;
        op.setData(Data.РЕПУТАЦИЯ, rep);*/
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
