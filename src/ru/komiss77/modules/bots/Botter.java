package ru.komiss77.modules.bots;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.AStarPath;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.LocUtil;

public interface Botter {

    //    private String prefix, affix, suffix;
    double DHIT_DST_SQ = 4d;
    int PARRY_TICKS = 40;
    int BASH_TICKS = 40;

    World world();

    Extent extent();

    <E extends Extent> E extent(final Class<E> cls);

    boolean busy(final LivingEntity mb, @Nullable final Boolean set, final int tks);

    boolean isBlocking(final LivingEntity mb);

    void blocking(final LivingEntity mb, final boolean set);

    boolean isBashed(final LivingEntity mb);

    void bashed(final LivingEntity mb, final boolean set);

    boolean isParrying(final LivingEntity mb);

    void parrying(final LivingEntity mb, final boolean set);

    void hurt(final LivingEntity mb);

    void swingHand(final boolean main);

    void attack(final LivingEntity from, final Entity to, final boolean ofh);

    void telespawn(final Location to, @Nullable final LivingEntity le);

    ItemStack item(final EquipmentSlot slot);

    ItemStack item(final int slot);

    int getHandSlot();

    void swapToSlot(final int slot);

    void item(final ItemStack it, final EquipmentSlot slot);

    void item(final ItemStack it, final int slot);

    Inventory inv();

    void clearInv();

    LivingEntity getEntity();

    boolean isDead();

    void hide(@Nullable final LivingEntity mb);

    void remove();

    void rid(final int rid);

    int rid();

    String name();

    WXYZ getPos();

    void tab(final String prefix, final String affix, final String suffix);

    void tag(final boolean show);

    void tag(final String prefix, final String affix, final String suffix);

    void setTagVis(final Predicate<Player> canSee);

    boolean isTagVisTo(final Player pl);

    void removeAll(final Player pl);

    void updateAll(final Player pl);

    void move(final Location loc, final Vector vc, final boolean look);

    void pickup(final Location loc);

    void drop(final Location loc);

    void interact(final PlayerInteractAtEntityEvent e);

    void damage(final EntityDamageEvent e);

    void death(final EntityDeathEvent e);

    void bug();

    /*@Override
    int hashCode();

    @Override
    boolean equals(final Object o) {
        return o instanceof final Botter bt
            && bt.name().equals(name());
    }*/

    interface Extent {
        void create(final Botter bt);

        void remove(final Botter bt);
        default void bug(final Botter bt) {
            bt.remove();
        }

        void spawn(final Botter bt, final @Nullable LivingEntity le);
        void hide(final Botter bt, final @Nullable LivingEntity le);

        void click(final Botter bt, final PlayerInteractAtEntityEvent e);
        default void death(final Botter bt, final EntityDeathEvent e) {
            e.getDrops().clear();
            final LivingEntity le = e.getEntity();
            le.getWorld().spawnParticle(Particle.CLOUD, le.getLocation()
                .add(0d, 1d, 0d), 20, 0.1d, 0.5d, 0.1d, 0.04d);
            bt.hide(le);
        }
        default void damage(final Botter bt, final EntityDamageEvent e) {
            bt.hurt((LivingEntity) e.getEntity());
        }

        void pickup(final Botter bt, final Location loc);

        void drop(final Botter bt, final Location loc);

        default Goal<Mob> goal(final Botter bt, final Mob mb) {
            return new BotGoal(bt);
        }
    }

    class BotGoal implements Goal<Mob> {

        private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Ostrov.instance, "bot"));

        private final Botter bot;
        private final AStarPath arp;

        private BotGoal(final Botter bot) {
            this.bot = bot;
            this.arp = new AStarPath((Mob) bot.getEntity(), 1000, true);
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
        public void tick() {
            final Mob rplc = (Mob) bot.getEntity();
            if (rplc == null || !rplc.isValid()) {
                return;
            }

            final Location loc = rplc.getLocation();
            final Location eyel = rplc.getEyeLocation();
            final Vector vc = eyel.getDirection();

            final Player pl = LocUtil.getClsChEnt(new WXYZ(loc, false), 200, Player.class, le -> true);
            if (pl == null) {
                return;
            }

            if (!arp.hasTgt()) {
                arp.setTgt(new WXYZ(pl.getLocation()));
            }
            arp.tickGo(1.5d);

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
