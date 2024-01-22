package ru.komiss77.modules.bots;

import java.util.EnumSet;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.LocationUtil;

public class BotGoal implements Goal<Mob> {
	
    private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Ostrov.instance, "bot"));
    
    private final BotEntity bot;
//    private AreaPath path;
    
    public BotGoal(final BotEntity bot) {
        this.bot = bot;
//        this.path = null;
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

        final Pathfinder pth = rplc.getPathfinder();
        //Bukkit.broadcast(Component.text("le-" + rplc.getName()));
        final Location loc = rplc.getLocation();
        final Location eyel = rplc.getEyeLocation();
        final Vector vc = eyel.getDirection();

        vc.normalize();

        bot.move(loc, vc, true);

//			if (bot.tryJump(loc, rplc, vc)) return;

        vc.setY(0d);
        if (rplc.isInWater()) {
            rplc.setVelocity(rplc.getVelocity().setY(0.1d).add(vc.multiply(0.05d)));
        } else {
            if (rplc.isOnGround()) {

                final Player pl = LocationUtil.getClsChEnt(new WXYZ(loc, false), 200, Player.class, le -> true);
                if (pl == null) return;
                rplc.getPathfinder().moveTo(pl, 1.4d);
					/*if (path == null && PlayerLst.ar != null) {
						path = new AreaPath(rplc, PlayerLst.ar);
					}

					if (path != null) {
						path.setTgt(new WXYZ(pl.getLocation()));
						path.tickGo(1.5d);
					}*/

            } else {
                if (pth.hasPath()) pth.stopPathfinding();
                rplc.setVelocity(rplc.getVelocity().add(vc.multiply(0.05d)));
            }
        }
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