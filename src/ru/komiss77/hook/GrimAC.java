package ru.komiss77.hook;

import java.util.List;
import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.event.events.CompletePredictionEvent;
import ac.grim.grimac.api.event.events.FlagEvent;
import ac.grim.grimac.api.event.events.GrimVerboseCheckEvent;
import ac.grim.grimac.api.plugin.BasicGrimPlugin;
import ac.grim.grimac.api.plugin.GrimPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

//https://github.com/GrimAnticheat/GrimAPI
public class GrimAC implements Listener {

    public static final String CHEAT = "cheat";
  public GrimAbstractAPI api;

    public GrimAC() {
      final RegisteredServiceProvider<GrimAbstractAPI> provider =
            Bukkit.getServicesManager().getRegistration(GrimAbstractAPI.class);
      //api = provider == null ? null : provider.getProvider();
      if (provider != null) {
        // create a GrimPlugin instance from this plugin
        GrimPlugin plugin = new BasicGrimPlugin(
            Ostrov.instance.getLogger(),//this.getLogger(),
            Ostrov.instance.getDataFolder(),//this.getDataFolder(),
            "3.4",//this.getDescription().getVersion(),
            "Ostrov",//this.getDescription().getDescription(),
            List.of("komiss77", "romindous")//this.getDescription().getAuthors()
        );
        api = provider.getProvider();

        // use the event bus to subscribe to FlagEvent
        api.getEventBus().subscribe(plugin, FlagEvent.class, e -> {
          final Oplayer opl = PM.getOplayer(e.getUser().getUniqueId());
          if (opl.disguise.type != null || opl.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            e.setCancelled(true);
            return;
          }
          // broadcast to all players when a player flags a check
          final String msg = "§8Античит: " + e.getPlayer().getName() + " подозрение на " + e.getCheck().getCheckName();
          Ostrov.log_warn(msg);
          for (Oplayer op : PM.getOplayers()) {
            if (op.isStaff) {
              op.getPlayer().sendMessage(msg);
            }
          }
          ;
        });
        // api.getEventBus().subscribe(plugin, CompletePredictionEvent.class, e -> {
        //    Ostrov.log_warn("grimAC CompletePrediction "+e.getCheck().getCheckName());
        ;
        // });

        //  api.getEventBus().subscribe(plugin, GrimVerboseCheckEvent.class, e -> {
        //    Ostrov.log_warn("grimAC GrimVerbose "+e.getCheck().getCheckName());
          ;
        //   });




      }
    }

  //@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
