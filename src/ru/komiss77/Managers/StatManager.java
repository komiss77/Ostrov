package ru.komiss77.Managers;

import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Enums.Action;
import ru.komiss77.Enums.Data;
import ru.komiss77.Objects.Group;
import ru.komiss77.Objects.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.ProfileMenu.E_Pass;
import ru.komiss77.Enums.Stat;
import ru.komiss77.modules.OstrovDB;




public class StatManager {


    public static void addStat(final Player p, final Stat e_stat, final int ammount) {
System.out.println("-addIntStat e_stat="+e_stat+"+"+ammount);
        final Oplayer op = PM.getOplayer(p.getName());
        if (op==null) return;
        int currentStatValue = op.getStat(e_stat);
        int newStatValue = currentStatValue + ammount;
        op.addStat(e_stat, ammount); //делать через адд, чтобы добавило дневную!
        
        //**** Изменение кармы ****
        int karma = op.getDataInt(Data.KARMA);
        if (e_stat.toString().endsWith("_win")) karma++;
        else if (e_stat.toString().endsWith("_loose")) karma--;
        if (karma>100) karma=100;
        else if (karma<-100) karma=-100;
        op.setData( Data.KARMA, String.valueOf(karma));
        //*************************
        
        if (e_stat.achiv!=null) {
            final int currentLevel = getLevel(e_stat, currentStatValue);
            final int newLevel = getLevel(e_stat, newStatValue);
            if (newLevel>currentLevel) {
                final String achiv = descFromAchiv(e_stat, 0);
                ApiOstrov.sendBossbar(p, e_stat.game.displayName+" : §d"+(achiv.isEmpty() ? "Достижение!" : achiv)+"§7, "+e_stat.desc+newStatValue, 15, BarColor.YELLOW, BarStyle.SEGMENTED_6, false);
                ApiOstrov.sendTitle(p, achiv.isEmpty() ? "§fДостижение!" : "§e"+achiv, e_stat.game.displayName+"§7, "+e_stat.desc+newStatValue, 20, 40, 20);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
                if (e_stat.exp_per_point>0) {
                    addXP(op,e_stat.exp_per_point);
                }
            }
        }
        //if (e_stat.exp_per_point != 0) {
            //onStatChange (p, op, e_stat, currentStatValue);
        //}
    }
    

    public static void addXP(final Oplayer op, final int value) {
        /*
        int curr_level = op.getStat(Stat.LEVEL);
        int curr_exp = op.getStat(Stat.EXP);
System.out.println("-xpChange() value="+value+" lvl="+curr_level+"  exp="+curr_exp);        
        if (value>0) {
            curr_exp+=value;
            if (curr_exp>curr_level*25) {
                curr_exp-=curr_level*25;
                if (curr_exp<0) curr_exp=0;
                curr_level++;
                ApiOstrov.sendTitle(op.getPlayer(), "§7.", Ostrov.prefix+"Новый уровень : §b"+curr_level, 20, 60, 40);
                op.getPlayer().playSound(op.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
                op.addStat(Stat.LEVEL, 1);
            } else {
                if (value>10) ApiOstrov.sendActionBar(op.nik, Ostrov.prefix+(curr_level*50-curr_exp) +"§7 опыта до следующего уровня");
            }
        } else {
            curr_exp+=value;
            if (curr_exp<0) curr_exp=0;
        }
        op.sets(Stat.EXP, curr_exp);*/
    }



    public static void karmaChange(final Oplayer op,final int value) {
        int current_carma = op.getDataInt(Data.KARMA);
        current_carma+=value;
        if (current_carma>100) current_carma=100;
        else if (current_carma<-100) current_carma=-100;
        op.setData(Data.KARMA, current_carma);
//System.out.println("-karmaChange() current="+current_carma+" new="+bp.getIntData(Data.КАРМА));        
    }    
    
    
    public static void reputationChange(final Oplayer op, final int value) {  //срабатывает от бан,мут,покупка групп 
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

    



    private static String descFromAchiv(final Stat stat, final int level) {
        switch(stat) {
            case BW_game: return "Любитель БедВарс";
            case BW_kill: return "Злой БедВарсер";
            case BW_win: return "Бедварсер-победитель";
            case BW_bed: return "Разоритель гнёзд";
            
            default: return "";
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

    
    
    
    
    
    
    
    
    
  //  private static int achivFromStat( final Oplayer op, final E_Stat e_stat, final int int_value, final String string_value) {
    //    if (!e_stat.is_achiv) return -1;
     //   final int level = getLevel(e_stat, int_value);// int_value>=1000 ? 3 : int_value>=100 ? 2 : int_value>=10 ? 1 : -1 ;
      //  return e_stat.tag*10+level;
        
       // int int_level=-1;
        
       /* switch (e_stat) {
            //ПРОПУСКАЕМ
            case BW_death:
            case BW_loose:
            case SG_loose:
            case BB_loose:
            case GR_loose:
            case GR_death:
            case HS_loose:
            case SW_loose:
            case QU_death:
            case QU_loose:
            case CS_loose:
            case CS_death:
            case TW_loose:
            case SN_loose:
            case ZH_loose:
                break;

                
                
            //ЧИСЛОВЫЕ  
            case BW_game:
            case BW_kill:
            case BW_win:
            case BW_bed:
            case SG_game:
            case SG_win:
            case SG_kill:
            case BB_win:
            case BB_game:
            case BB_vote:
            case BB_block:
            case GR_game:
            case GR_win:
            case GR_kill:
            case GR_gold:
            case GR_pz:
            case HS_game:
            case HS_win:
            case HS_hkill:
            case HS_skill:
            case HS_fw:
            case SW_game:
            case SW_win:
            case SW_kill:
            case CS_game:
            case CS_win:
            case CS_kill:
            case CS_hshot:
            case CS_bomb:
            case TW_game:
            case TW_win:
            case TW_gold:
            case QU_game:
            case QU_kill:
            case QU_twin:
            case QU_win:
            case SN_game:
            case SN_gold:
            case SN_win:
            case ZH_game:
            case ZH_win:
                int_level =  int_value>=1000 ? 3 : int_value>=100 ? 2 : int_value>=10 ? 1 : -1 ;
                break;
        }*/
        //if (int_level<0) {
        //    return -1;
        //} else {
        //}
        
   // }



    
    
}
