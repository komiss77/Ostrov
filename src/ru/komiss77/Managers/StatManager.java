package ru.komiss77.Managers;

import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import ru.komiss77.ApiOstrov;
import ru.komiss77.Enums.Data;
import ru.komiss77.Objects.Group;
import ru.komiss77.Objects.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.ProfileMenu.E_Pass;
import ru.komiss77.ProfileMenu.E_Stat;
import ru.komiss77.modules.OstrovDB;




public class StatManager {

    @Deprecated
    public static void addIntStat(final Player p, final E_Stat e_stat) {
//System.out.println("-addIntStat e_stat="+e_stat.toString());
        addStat(p, e_stat, 1);
    }
    
    public static void addStat(final Player p, final E_Stat e_stat, final int ammount) {
System.out.println("-addIntStat e_stat="+e_stat+"+"+ammount);
        final Oplayer op = PM.getOplayer(p.getName());
        if (op==null) return;
        int currentStatValue = op.getStat(e_stat);
        int newStatValue = currentStatValue + ammount;
        op.addStat(e_stat, ammount); //делать через адд, чтобы добавило дневную!
        
        //**** Изменение кармы ****
        int karma = op.getDataInt(Data.КАРМА);
        if (e_stat.toString().endsWith("_win")) karma++;
        else if (e_stat.toString().endsWith("_loose")) karma--;
        if (karma>100) karma=100;
        else if (karma<-100) karma=-100;
        op.setData( Data.КАРМА, String.valueOf(karma));
        //*************************
        
        if (e_stat.is_achiv) {
            final int currentLevel = getLevel(e_stat, currentStatValue);
            final int newLevel = getLevel(e_stat, newStatValue);
            if (newLevel>currentLevel) {
                final String achiv = descFromAchiv(e_stat, 0);
                ApiOstrov.sendBossbar(p, E_Stat.gameNameFromStat(e_stat)+" : §d"+(achiv.isEmpty() ? "Достижение!" : achiv)+"§7, "+e_stat.desc+newStatValue, 15, BarColor.YELLOW, BarStyle.SEGMENTED_6, false);
                ApiOstrov.sendTitle(p, achiv.isEmpty() ? "§fДостижение!" : "§e"+achiv, E_Stat.gameNameFromStat(e_stat)+"§7, "+e_stat.desc+newStatValue, 20, 40, 20);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
                if (e_stat.exp_per_point>0) {
                    AddXP(op,e_stat.exp_per_point);
                }
            }
        }
        //if (e_stat.exp_per_point != 0) {
            //onStatChange (p, op, e_stat, currentStatValue);
        //}
    }
    
    
    
    
    
    
    
    @Deprecated
    public static String getStringStat(final Player p, final E_Stat e_stat) {
        //if (!e_stat.is_integer)
            return ""+PM.getOplayer(p.getName()).getStat(e_stat);
        //else return e_stat.def_value;
      //return "";
    }

    @Deprecated
    public static void setStringStat(final Player p, final E_Stat e_stat, final String new_value) {
        //if (!e_stat.is_integer) PM.getOplayer(p.getName()).setStat(e_stat, new_value);
    }

    
    
    /*
    private static void onStatChange(final Player p, final Oplayer op, final E_Stat e_stat, final int old_value, final int new_value) {//проверка достижений
System.out.println("-onStatChange() stat="+e_stat.toString()+"  value="+new_value);        
        
        

        if (e_stat.is_achiv) {
            final int currentLevel = getLevel(e_stat, op.getStat(e_stat));
            
            final int achiv_tag=achivFromStat(op, e_stat, new_value, "");
                if (achiv_tag>0) {
                    if (!op.achiv.contains(achiv_tag)) {
System.out.println("+++ достижение : e_stat="+e_stat.toString()+" achiv_tag="+achiv_tag);   
                        op.achiv.add(achiv_tag);
                        //op.setData( Data.ДОСТИЖЕНИЯ, Joiner.on(',').join(op.achiv) );
                        
                        final String achiv = descFromAchiv(e_stat, 0);
                        ApiOstrov.sendBossbar(p, E_Stat.gameNameFromStat(e_stat)+" : §d"+(achiv.isEmpty() ? "Достижение!" : achiv)+"§7, "+e_stat.desc+new_value, 15, BarColor.YELLOW, BarStyle.SEGMENTED_6, false);
                        ApiOstrov.sendTitle(p, achiv.isEmpty() ? "§fДостижение!" : "§e"+achiv, E_Stat.gameNameFromStat(e_stat)+"§7, "+e_stat.desc+new_value, 20, 40, 20);
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
                    }
                }
        }
            
        AddXP(op,e_stat.exp_per_point);
        
    }*/

    
    
    public static void AddXP(final Oplayer op, final int value) {
        /
        int curr_level = op.getStat(E_Stat.LEVEL);
        int curr_exp = op.getStat(E_Stat.EXP);
System.out.println("-xpChange() value="+value+" lvl="+curr_level+"  exp="+curr_exp);        
        if (value>0) {
            curr_exp+=value;
            if (curr_exp>curr_level*25) {
                curr_exp-=curr_level*25;
                if (curr_exp<0) curr_exp=0;
                curr_level++;
                ApiOstrov.sendTitle(op.getPlayer(), "§7.", Ostrov.prefix+"Новый уровень : §b"+curr_level, 20, 60, 40);
                op.getPlayer().playSound(op.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
                op.addStat(E_Stat.LEVEL, 1);
            } else {
                if (value>10) ApiOstrov.sendActionBar(op.nik, Ostrov.prefix+(curr_level*50-curr_exp) +"§7 опыта до следующего уровня");
            }
        } else {
            curr_exp+=value;
            if (curr_exp<0) curr_exp=0;
        }
        op.setData(Data.ОПЫТ, String.valueOf(curr_exp));
    }

    
    
    public static void calculateReputationBase(final Oplayer op) { //когда данные с банжи получены или изменили паспорт
        int current_calc=op.getDataInt(Data.РЕПУТАЦИЯ_РАСЧЁТ);
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
        if (current_calc!=new_calc) op.setData(Data.РЕПУТАЦИЯ_РАСЧЁТ, String.valueOf(new_calc));
        int rep=new_calc+op.getBungeeIntData(Data.РЕПУТАЦИЯ_БАЗА);
        if (rep>100) rep=100;
        else if (rep<-100) rep=-100;
        op.setData(Data.РЕПУТАЦИЯ, String.valueOf(rep));
    }

    


    
    
    
    
    
    
    
    
    
    private static int achivFromStat( final Oplayer op, final E_Stat e_stat, final int int_value, final String string_value) {
        if (!e_stat.is_achiv) return -1;
        final int level = getLevel(e_stat, int_value);// int_value>=1000 ? 3 : int_value>=100 ? 2 : int_value>=10 ? 1 : -1 ;
        return e_stat.tag*10+level;
        
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
        
    }




    private static String descFromAchiv(final E_Stat stat, final int level) {
        switch(stat) {
            case BW_game: return "Любитель БедВарс";
            case BW_kill: return "Злой БедВарсер";
            case BW_win: return "Бедварсер-победитель";
            case BW_bed: return "Разоритель гнёзд";
            
            default: return "";
        }
    }

    public static int getLevel(final E_Stat st, final int value) {
        //потом доработать в зависимости от типа
        return value>=1000 ? 3 : value>=100 ? 2 : value>=10 ? 1 : 0 ;
    }
    
    
    
}
