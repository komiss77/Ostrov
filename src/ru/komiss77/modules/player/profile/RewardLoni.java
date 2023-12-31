package ru.komiss77.modules.player.profile;

import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.translate.Lang;


public class RewardLoni {

    protected static void onStatAdd(final Player p, final Oplayer op, final Stat stat, final int ammount) {
        int loniAdd = 0;
        int expAdd = 0;
        
        switch (stat) {
            //case BW_kill -> { lonyAdd = 1; expAdd = ApiOstrov.randInt(1, 5); }
            case BW_bed -> loniAdd = 10;
            case BW_game -> loniAdd = 5;
            case BW_kill -> loniAdd = 1;
            case BW_win -> loniAdd = 5;

            case SN_game -> loniAdd = 2;
            case SN_win -> loniAdd = 2;
            case SN_gold -> loniAdd = ammount >> 2;
            
            case QU_game -> loniAdd = 5;
            case QU_kill -> loniAdd = 2;
            case QU_twin -> loniAdd = 5;
            case QU_win -> loniAdd = 5;
            
            case SG_game -> loniAdd = 5;
            case SG_kill -> loniAdd = 5;
            case SG_win -> loniAdd = 5;

            case HS_fw -> loniAdd = 1;
            case HS_game -> loniAdd = 5;
            case HS_win -> loniAdd = 5;            
            case HS_hkill -> loniAdd = 10;
            case HS_skill -> loniAdd = 5;
            
            case TW_game -> loniAdd = 2;
            case TW_win -> loniAdd = 5;
            //case TW_gold -> loniAdd = ammount; - слишком много монет

            case BB_game -> loniAdd = 20;
            //case BB_win -> loniAdd = 10; - в customstat распределение по местам

            case CS_bomb -> loniAdd = 5;
            case CS_game -> loniAdd = 5;
            //case CS_hshot -> loniAdd = 1;
            case CS_kill -> loniAdd = 2;
            case CS_spnrs -> loniAdd = 2;
            case CS_win -> loniAdd = 2;

            case GR_game -> loniAdd = 5;
            case GR_kill -> loniAdd = 2;
            case GR_win -> loniAdd = 5;

            case KB_abil -> loniAdd = 1;
            case KB_cwin -> loniAdd = 5;
            case KB_kill -> loniAdd = 2;
            case KB_twin -> loniAdd = 5;

            case PA_chpt -> loniAdd = 2;
            case PA_done -> loniAdd = 10;

            case SW_game -> loniAdd = 2;
            case SW_kill -> loniAdd = 5;
            case SW_win -> loniAdd = 2;

            case WZ_game -> loniAdd = 5;
            case WZ_klls -> loniAdd = 2;
            case WZ_win -> loniAdd = 5;

            case ZH_game -> loniAdd = 5;
            case ZH_pdths -> loniAdd = 2;
            case ZH_win -> loniAdd = 2;
            case ZH_zklls -> loniAdd = 5;
            default -> loniAdd = 0;

        }
        /*case SK_biome:
        break;
        case SK_ch:
        break;
        case SK_chu:
        break;
        case SK_emer:
        break;
        case SK_size:
        break;*/
                
        if (loniAdd>0) {
            int loni = op.getDataInt(Data.LONI) + loniAdd;
            op.setData(Data.LONI, loni);
            if (loniAdd>=5) {
                //paper версия
                p.sendMessage(Component.text(Ostrov.PREFIX+"§7Награда за "+Lang.t(p, stat.desc)+" §7-> "+loniAdd+" лони §7! §8<клик-баланс")
                	.hoverEvent(HoverEvent.showText(Component.text("§fУ вас §e"+loni+" лони"))).clickEvent(ClickEvent.runCommand("/money balance")));
            }
        }

        if (expAdd>0) {
            op.addExp(p, expAdd);
        } else if (loniAdd>0) {
            op.addExp(p, ApiOstrov.randInt(1, loniAdd));
        }
        
    }
    
    protected static void onCustomStat(final Player p, final Oplayer op, final String customStatName, final int ammount) {
        int loniAdd = 0;
        int expAdd = 0;
        
        switch (customStatName) {
            case "Убийство бескроватного" -> { loniAdd = 5; expAdd = ApiOstrov.randInt(5, 15); }
            case "Захват флага" -> { loniAdd = 5; expAdd = ApiOstrov.randInt(5, 15); }
            case "Битва Строителей - 1 место" -> { loniAdd = 20; expAdd = ApiOstrov.randInt(20, 40); }
            case "Битва Строителей - 2 место" -> { loniAdd = 10; expAdd = ApiOstrov.randInt(15, 35); }
            case "Битва Строителей - 3 место" -> { loniAdd = 5; expAdd = ApiOstrov.randInt(10, 30); }
        }
        
        if (loniAdd>0) {
            int loni = op.getDataInt(Data.LONI) + loniAdd;
            op.setData(Data.LONI, loni);
            if (loniAdd>=5) {
                //paper версия
                p.sendMessage(Component.text(Ostrov.PREFIX+"§7Награда за "+customStatName+" §7-> "+loniAdd+" лони §7! §8<клик-баланс")
                	.hoverEvent(HoverEvent.showText(Component.text("§fУ вас §e"+loni+" лони"))).clickEvent(ClickEvent.runCommand("/money balance")));
                
                /*p.spigot().sendMessage(new ComponentBuilder(Ostrov.prefix+"§7"+"Награда за "+customStatName+" §7-> "+lonyAdd+" лони §7! §8<клик-баланс")
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§fУ вас §e"+loni+" лони") ))
                    .event( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/money balance") )
                    .create());*/
            }
        }

        if (expAdd>0) {
            op.addExp(p, expAdd);
        }
        
    }
    
    
    
}
