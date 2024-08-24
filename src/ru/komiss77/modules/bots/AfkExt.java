package ru.komiss77.modules.bots;

import java.util.EnumSet;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.Ostrov;
import ru.komiss77.listener.ChatLst;
import ru.komiss77.modules.world.WXYZ;


public class AfkExt implements Botter.Extent {

    private final WXYZ loc;

    public AfkExt(final WXYZ loc) {
        this.loc = loc;
    }

    @Override
    public void create(final Botter bt) {
        bt.telespawn(null, loc.getCenterLoc());
        bt.tab("", ChatLst.NIK_COLOR, "");
        bt.tag("§3А вот и ", ChatLst.NIK_COLOR, " §2заспавнен");
    }

    public void remove(Botter bt) {}
    public void teleport(Botter bt, LivingEntity le) {}
    public void spawn(Botter bt, @Nullable LivingEntity le) {}
    public void hide(Botter bt, @Nullable LivingEntity le) {}
    public void click(Botter bt, PlayerInteractAtEntityEvent e) {}
    public void death(Botter bt, EntityDeathEvent e) {
        e.getDrops().clear();
        final LivingEntity le = e.getEntity();
        le.getWorld().spawnParticle(Particle.CLOUD, le.getLocation()
            .add(0d, 1d, 0d), 20, 0.1d, 0.5d, 0.1d, 0.04d);
        bt.remove();
    }
    public void pickup(Botter bt, Location loc) {}
    public void drop(Botter bt, Location loc) {}

    @Override
    public Goal<Mob> goal(Botter bt, Mob mb) {
        return new AfkGoal(bt);
    }

    static class AfkGoal implements Goal<Mob> {

        private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Ostrov.instance, "bot"));

        private final Botter bot;

        public AfkGoal(final Botter bot) {
            this.bot = bot;
        }

        @Override
        public boolean shouldActivate() {
            return true;
        }

        @Override
        public boolean shouldStayActive() {
            return true;
        }

        @Override
        public void stop() {
            bot.hide(bot.getEntity());
        }

        @Override
        public void tick() {
            final Mob rplc = (Mob) bot.getEntity();
            if (rplc == null || !rplc.isValid()) {
                bot.hide(rplc);
                return;
            }

            final Location loc = rplc.getLocation();
            final Location eyel = rplc.getEyeLocation();
            final Vector vc = eyel.getDirection();

            vc.normalize();

            bot.move(loc, vc, true);

        }

        @Override
        public @NotNull GoalKey<Mob> getKey() {
            return key;
        }

        @Override
        public @NotNull EnumSet<GoalType> getTypes() {
            return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
        }
    }
}
