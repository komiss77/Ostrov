package ru.komiss77.modules.bots;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.listener.ChatLst;
import ru.komiss77.modules.world.WXYZ;

import java.util.EnumSet;

public class AfkBot extends BotEntity {

    public AfkBot(final String name, final WXYZ loc) {
        super(name, loc.w);
        telespawn(loc.getCenterLoc(), null);
        tab("", ChatLst.NIK_COLOR, "");
        tag("§3А вот и ", ChatLst.NIK_COLOR, " §2заспавнен");
        //TCUtils.N + "[" + TCUtils.P + "Bot" + TCUtils.N + "]
//        tag("Просто ", "стою", " и сосу лапу");
    }
    
    static class AfkGoal implements Goal<Mob> {

        private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Ostrov.instance, "bot"));

        private final BotEntity bot;

        public AfkGoal(final BotEntity bot) {
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
        public void start() {
        }

        @Override
        public void stop() {
            bot.die(bot.getEntity());
        }

        @Override
        public void tick() {
            final Mob rplc = (Mob) bot.getEntity();
            if (rplc == null || !rplc.isValid()) {
                bot.die(rplc);
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
