package ru.komiss77.hook;

import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.events.FlagEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.Operation;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.LocUtil;


public class GrimAC implements Listener {

    public static final String CHEAT = "cheat";
    public final GrimAbstractAPI api;

    public GrimAC() {
        final RegisteredServiceProvider<GrimAbstractAPI> prv =
            Bukkit.getServicesManager().getRegistration(GrimAbstractAPI.class);
        api = prv == null ? null : prv.getProvider();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCheat(final FlagEvent e) {
        final Oplayer op = PM.getOplayer(e.getUser().getUniqueId());
        if (op == null || op.getOnlineTime() < 10) return;
        final Player pl = op.getPlayer();
        if (pl == null || Timer.has(pl, CHEAT)) return;
        Timer.add(pl, CHEAT, 5);
        SpigotChanellMsg.sendMessage(pl, Operation.REPORT_SERVER, Ostrov.MOT_D, 0, 0, 0,
            pl.getName(), LocUtil.toString(pl.getLocation()), "Обнаружен чит " + e.getCheck().getCheckName());
    }
}
