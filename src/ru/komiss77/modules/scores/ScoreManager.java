package ru.komiss77.modules.scores;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;

import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;

public class ScoreManager implements Initiable, Listener {

    protected static final HashMap<UUID, ScoreBoard> scores = new HashMap<>();
    protected static final NamespacedKey key = new NamespacedKey(Ostrov.instance, "score");

    public ScoreManager() {
        reload();
    }

    @Override
    public void postWorld() {
    }

    @Override
    public void reload() {
        HandlerList.unregisterAll(this);
        Ostrov.log_ok("§2Статистика включена!");

        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
    }

    @Override
    public void onDisable() {
        Ostrov.log_ok("§6Статистика выключена!");
    }

    @EventHandler
    public void onEntLoad(final EntitiesLoadEvent e) {
        boolean fnd = false;
        for (final Entity ent : e.getEntities()) {
            if (ent.getPersistentDataContainer().has(key)) {
                if (scores.containsKey(ent.getUniqueId())) {
                    continue;
                } else {
                    ent.remove();
                    fnd = true;
                }
            }
        }

        if (fnd) {
            for (final ScoreBoard sb : scores.values()) {
                sb.reanimate(sb.getEntity());
            }
        }
    }

}
