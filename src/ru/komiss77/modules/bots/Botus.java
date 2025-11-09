package ru.komiss77.modules.bots;

import java.util.UUID;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.entity.CreatureSpawnEvent;
import ru.komiss77.modules.player.profile.Skins;
import ru.komiss77.version.CustomTag;

public abstract class Botus {

    public final String name;
    public final UUID id;

    private ResolvableProfile prof;
    private Mannequin bot;
    private CustomTag tag;

    private boolean isDead = true;

    public Botus(final String name) {
        this.name = name;
        this.id = UUID.randomUUID();
//        BotManager.botById.put(id, this);
        Skins.future(name, pp -> {
            prof = ResolvableProfile.resolvableProfile(pp);
            if (bot != null) bot.setProfile(prof);
            if (tag == null) tag = new CustomTag(bot);
            tag.visible(true);
        });
    }

    protected void teleport(final Location to) {
        if (bot == null) {
            if (tag != null) {
                tag.visible(false);
                tag = null;
            }
            return;
        }
        bot.teleport(to);
        if (tag == null) tag = new CustomTag(bot);
        tag.visible(true);
//        tag(true); tagThru(true);
    }

    public LivingEntity telespawn(final Location to) {
        if (bot == null) {
            bot = to.getWorld().spawn(to, Mannequin.class, CreatureSpawnEvent.SpawnReason.REANIMATE);
            if (prof != null) bot.setProfile(prof);
            else Skins.future(name, pp -> {
                prof = ResolvableProfile.resolvableProfile(pp);
                if (bot != null) bot.setProfile(prof);
            });
            if (tag == null) tag = new CustomTag(bot);
            tag.visible(true);
            bot.setDescription(null);
            bot.setCustomNameVisible(false);
        }

//        Bukkit.getMobGoals().removeAllGoals(bot);
//        Bukkit.getMobGoals().addGoal(vc, 0, goal(vc));

        isDead = false;
//        Ostrov.sync(() -> move(to.add(1, 1, 1)), 16);
        return bot;
    }
}
