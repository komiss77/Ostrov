package ru.komiss77.Managers;

import com.google.common.base.Joiner;
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

    public static void addIntStat(final Player p, final E_Stat e_stat) {
//System.out.println("-addIntStat e_stat="+e_stat.toString());
        final Oplayer op = PM.getOplayer(p.getName());
        if (op!=null && e_stat.is_integer) {
            String value = PM.getOplayer(p.getName()).getStat(e_stat);
            if (!Ostrov.isInteger(value)) value = e_stat.def_value;
            int v=Integer.valueOf(value);
            v++;
            PM.getOplayer(p.getName()).setStat(e_stat, String.valueOf(v));
            if (e_stat.exp_per_point != 0) onStatChange (op, e_stat, v);
        }
    }

    public static String getStringStat(final Player p, final E_Stat e_stat) {
        if (!e_stat.is_integer) return PM.getOplayer(p.getName()).getStat(e_stat);
        else return e_stat.def_value;
    }

    
    public static void setStringStat(final Player p, final E_Stat e_stat, final String new_value) {
        if (!e_stat.is_integer) PM.getOplayer(p.getName()).setStat(e_stat, new_value);
    }

    
    
    
    private static void onStatChange(final Oplayer op, final E_Stat e_stat, final int new_value) {//проверка достижений
System.out.println("-onStatChange() stat="+e_stat.toString()+"  value="+new_value);        
        int karma = op.getBungeeIntData(Data.КАРМА);
        if (e_stat.toString().endsWith("_win")) karma++;
        else if (e_stat.toString().endsWith("_loose")) karma--;
        if (karma>100) karma=100;
        else if (karma<-100) karma=-100;
        op.setData(Data.КАРМА, String.valueOf(karma));

        if (e_stat.is_achiv) {
            final int achiv_tag=achivFromStat(op, e_stat, new_value, "");
                if (achiv_tag>0) {
                    if (!op.achiv.contains(achiv_tag)) {
System.out.println("+++ достижение : e_stat="+e_stat.toString()+" achiv_tag="+achiv_tag);   
                        op.achiv.add(achiv_tag);
                        op.setData(Data.ДОСТИЖЕНИЯ, Joiner.on(',').join(op.achiv) );
                        
                        final String achiv = descFromAchiv(e_stat, 0);
                        ApiOstrov.sendBossbar(op.getPlayer(), E_Stat.gameNameFromStat(e_stat)+" : §d"+(achiv.isEmpty() ? "Достижение!" : achiv)+"§7, "+e_stat.as_string+new_value, 15, BarColor.YELLOW, BarStyle.SEGMENTED_6, false);
                        ApiOstrov.sendTitle(op.getPlayer(), achiv.isEmpty() ? "§fДостижение!" : "§e"+achiv, E_Stat.gameNameFromStat(e_stat)+"§7, "+e_stat.as_string+new_value, 20, 40, 20);
                        op.getPlayer().playSound(op.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
                    }
                }
        }
            
        AddXP(op,e_stat.exp_per_point);
        
    }

    
    
    public static void AddXP(final Oplayer op, final int value) {
        int curr_level = op.getBungeeIntData(Data.УРОВЕНЬ);
        int curr_exp = op.getBungeeIntData(Data.ОПЫТ);
System.out.println("-xpChange() value="+value+" lvl="+curr_level+"  exp="+curr_exp);        
        if (value>0) {
            curr_exp+=value;
            if (curr_exp>curr_level*25) {
                curr_exp-=curr_level*25;
                if (curr_exp<0) curr_exp=0;
                curr_level++;
                ApiOstrov.sendTitle(op.getPlayer(), "§7.", Ostrov.prefix+"Новый уровень : §b"+curr_level, 20, 60, 40);
                op.getPlayer().playSound(op.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
                op.setData(Data.УРОВЕНЬ, String.valueOf(curr_level));
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
        int current_calc=op.getBungeeIntData(Data.РЕПУТАЦИЯ_РАСЧЁТ);
        int new_calc=0;

        //!!!!!!!!!!создал клан,остров,выбрал класс - проверять через ачивки
        Data data;
        for (E_Pass e_pass:E_Pass.values()) {
            data = Data.fromName(e_pass.toString());
            if ( data !=null && !op.getBungeeData(data).isEmpty()) new_calc++;  //проверка заполнения данных в паспорте, +1 за каждые данные
        }
        
        new_calc+=(int)(op.GetPlytime()/1000);
        
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
        
        int int_level=-1;
        
        switch (e_stat) {
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
            case SP_loose:
            case TR_loose:
                break;

            //ТЕКСТОВЫЕ
           /* case SG_kits:
                if (string_value.isEmpty()) break;
                else {
                    int_level=string_value.split(",").length;
                    if (int_level>3) int_level=3;
                }
                break;*/
                
                
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
            case SP_game:
            case SP_win:
            case TR_game:
            case TR_win:
                int_level =  int_value>=1000 ? 3 : int_value>=100 ? 2 : int_value>=10 ? 1 : -1 ;
                break;
                
            
            
        }
        
        if (int_level<0) return -1;
        else return e_stat.tag*10+int_level;
        
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
    
    
    
}
