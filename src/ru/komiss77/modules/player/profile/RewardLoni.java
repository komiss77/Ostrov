package ru.komiss77.modules.player.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;


public class RewardLoni {

    protected static void onStatAdd(final Player p, final Oplayer op, final Stat stat, final int ammount) {
        int loniAdd = 0;

        switch (stat) {
            //case BW_kill -> { lonyAdd = 1; expAdd = ApiOstrov.randInt(1, 5); }
            case BW_bed -> loniAdd = 20;
            case BW_game -> loniAdd = 10;
//            case BW_kill -> loniAdd = 2;
            case BW_win -> loniAdd = 20;

            case SN_game -> loniAdd = 10;
            case SN_win -> loniAdd = 20;
            case SN_gold -> loniAdd = ammount >> 2;

            case QU_game -> loniAdd = 10;
            case QU_kill -> loniAdd = 10;
            case QU_twin -> loniAdd = 20;
            case QU_win -> loniAdd = 10;

            case SG_game -> loniAdd = 10;
            case SG_kill -> loniAdd = 20;
            case SG_win -> loniAdd = 15;

            case HS_fw -> loniAdd = 10;
            case HS_game -> loniAdd = 10;
            case HS_win -> loniAdd = 10;
            case HS_hkill -> loniAdd = 20;
            case HS_skill -> loniAdd = 20;

            case TW_game -> loniAdd = 10;
            case TW_win -> loniAdd = 25;
            case TW_gold -> loniAdd = ammount >> 2;

            case BB_game -> loniAdd = 10;
            //case BB_win -> loniAdd = 10; - в customstat распределение по местам

            case CS_bomb -> loniAdd = 20;
            case CS_game -> loniAdd = 10;
            //case CS_hshot -> loniAdd = 3;
            case CS_kill -> loniAdd = 5;
            case CS_spnrs -> loniAdd = 5;
            case CS_win -> loniAdd = 10;

            case GR_game -> loniAdd = 10;
            case GR_kill -> loniAdd = 20;
            case GR_win -> loniAdd = 20;

//            case KB_abil -> loniAdd = 1; не имба
            case KB_cwin -> loniAdd = 20;
            case KB_kill -> loniAdd = 10;
            case KB_twin -> loniAdd = 20;

            case PA_chpt -> loniAdd = 5;
            case PA_done -> loniAdd = 20;

            case SW_game -> loniAdd = 10;
            case SW_kill -> loniAdd = 10;
            case SW_win -> loniAdd = 10;

            case WZ_game -> loniAdd = 10;
            case WZ_klls -> loniAdd = 10;
            case WZ_win -> loniAdd = 10;

            case ZH_game -> loniAdd = 10;
            case ZH_pdths -> loniAdd = 5;
            case ZH_win -> loniAdd = 10;
            case ZH_zklls -> loniAdd = 10;
            default -> loniAdd = 0;

        }

        if (loniAdd > 0) {
            int loni = op.getDataInt(Data.LONI) + loniAdd;
            op.addExp(p, NumUtil.randInt(loniAdd, loniAdd * op.karmaCalc / 100 + loniAdd));
            op.setData(Data.LONI, loni);
            if (loniAdd >= 5) {
                //paper версия
                p.sendMessage(Component.text(Ostrov.PREFIX + "§7Награда за " + Lang.t(p, stat.desc) + " §7-> " + loniAdd + " лони §7! §8<клик-баланс")
                    .hoverEvent(HoverEvent.showText(Component.text("§fУ вас §e" + loni + " лони"))).clickEvent(ClickEvent.runCommand("/money balance")));
            }
        }
    }

    protected static void onCustomStat(final Player p, final Oplayer op, final String customStatName, final int ammount) {
        int loniAdd = 0;

        switch (customStatName) {
            case "Убийство бескроватного" -> {
                loniAdd = 10;
            }
            case "Захват флага" -> {
                loniAdd = 10;
            }
            case "Битва Строителей - 1 место" -> {
                loniAdd = 40;
            }
            case "Битва Строителей - 2 место" -> {
                loniAdd = 20;
            }
            case "Битва Строителей - 3 место" -> {
                loniAdd = 10;
            }
        }

        if (loniAdd > 0) {
            int loni = op.getDataInt(Data.LONI) + loniAdd;
            op.setData(Data.LONI, loni);
            op.addExp(p, NumUtil.randInt(loniAdd, loniAdd * op.karmaCalc / 100 + loniAdd));
            if (loniAdd > 4) {
                //paper версия
                p.sendMessage(TCUtil.form(Ostrov.PREFIX + "§7Награда за " + customStatName + " §7-> " + loniAdd + " лони §7! §8<клик-баланс")
                    .hoverEvent(HoverEvent.showText(TCUtil.form("§fУ вас §e" + loni + " лони"))).clickEvent(ClickEvent.runCommand("/money balance")));
            }
        }

    }


}
