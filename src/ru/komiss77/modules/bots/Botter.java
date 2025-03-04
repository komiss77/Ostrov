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
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.AStarPath;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.LocUtil;

public interface Botter {

    //    private String prefix, affix, suffix;
    EntityType TYPE = EntityType.VINDICATOR;
    double DHIT_DST_SQ = 4d;
    double SPEED_RUN = 1.25d;
    double SPEED_WALK = 1.0d;
    double SPEED_SLOW = 0.8d;
    /*int BASH_TICKS = 40;
    int BLOCK_ACT = 1;
    int PARRY_ACT = 2;
    int BASH_ACT = 3;
    int DHIT_ACT = 4;*/

    World world();

    Extent extent();

    void startUse(final LivingEntity mb, final EquipmentSlot hand);

    void stopUse(final LivingEntity mb);

    int useTicks(final LivingEntity mb);

    void useTicks(final LivingEntity mb, final int ticks);

    ItemType usedType();

    EquipmentSlot usedHand();

    <E extends Extent> E extent(final Class<E> cls);

    boolean isBlocking(final LivingEntity mb);

    boolean isParrying(final LivingEntity mb);

    void parrying(final LivingEntity mb, final boolean set);

    void hurt(final LivingEntity mb);

    void swingHand(final boolean main);

    void attack(final LivingEntity from, final Entity to, final boolean ofh);

    LivingEntity telespawn(final @Nullable LivingEntity mb, final Location to);

    ItemStack item(final EquipmentSlot slot);

    ItemStack item(final int slot);

    int getHandSlot();

    void swapToSlot(final int slot);

    void item(final EquipmentSlot slot, final ItemStack it);

    void item(final int slot, final ItemStack it);

    Inventory inv();

    void clearInv();

    LivingEntity getEntity();

    boolean isDead();

    void hide(@Nullable final LivingEntity mb);

    void remove();

//    void rid(final int rid);

    int rid();

    String name();

    BVec getPos();

    void tab(final String prefix, final String affix, final String suffix);

    void tag(final boolean show);

    void tag(final String prefix, final String affix, final String suffix);

    void setTagVis(final Predicate<Player> canSee);

    boolean isTagVisTo(final Player pl);

    void removeAll(final Player pl);

    void updateAll(final Player pl);

    @Deprecated
    void move(final Location loc, final Vector vc, final boolean look);

    void move(final Location loc, final Vector vc);

    void move(final Location loc);

    void sneak(final boolean sneak);

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

        void teleport(final Botter bt, final LivingEntity le);
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

            final Player pl = LocUtil.getClsChEnt(BVec.of(loc), 200, Player.class, le -> true);
            if (pl == null) {
                return;
            }

            if (!arp.hasTgt()) {
                arp.setTgt(BVec.of(pl.getLocation()));
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
