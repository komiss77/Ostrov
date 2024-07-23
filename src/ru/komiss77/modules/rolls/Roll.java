package ru.komiss77.modules.rolls;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.OstrovConfig;

import java.util.HashMap;
import java.util.function.Function;

public abstract class Roll<R> {

    private static final HashMap<String, Roll<?>> rolls = new HashMap<>();
    private static final String CON_NAME = "rolls.yml";

    protected final R it;
    protected final String id;
    protected final byte number;
    protected final byte extra;
    protected final byte chance;

    protected Roll(final String id, final R it, final int chance, final int number, final int extra) {
        this.id = id;
        this.it = it;
        this.chance = (byte) Math.max(1, chance);
        this.number = (byte) Math.max(0, number);
        this.extra = (byte) Math.max(1, extra + 1);
        if (rolls.putIfAbsent(id, this) == null) save();
    }

    protected abstract R asAmount(final int amt);

    public R generate() {
        return asAmount(Ostrov.random.nextInt(chance) == 0 ? number + Ostrov.random.nextInt(extra) : 0);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Roll && id.equals(((Roll<?>) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    protected abstract String encode();

    protected static final String VAL = "val";
    protected static final String NUM = "num";
    protected static final String CH = "ch";
    protected static final String EX = "ex";

    public Roll<R> save() {
        final OstrovConfig irc = Config.manager.getNewConfig(CON_NAME);
        final String dir = getClass().getSimpleName() + "." + id + ".";
        irc.set(dir + VAL, encode());
        irc.set(dir + NUM, number);
        irc.set(dir + CH, chance);
        irc.set(dir + EX, extra);
        irc.saveConfig();
        return this;
    }

    public Roll<R> delete() {
        final OstrovConfig irc = Config.manager.getNewConfig(CON_NAME);
        irc.removeKey(getClass().getSimpleName() + "." + id);
        irc.saveConfig();
        return this;
    }

    public static boolean roll(final int chance) {
        return Ostrov.random.nextInt(Math.max(chance, 1)) == 0;
    }

    public static boolean rollIn(final float chance) {
        return Ostrov.random.nextFloat() < Math.clamp(chance, 0f, 1f);
    }

    public static boolean rollOut(final float chance) {
        return Ostrov.random.nextFloat() > Math.clamp(chance, 0f, 1f);
    }

    public static Roll<?> getRoll(final String id) {
        return rolls.get(id);
    }

    public static <R> Roll<R> getRoll(final String id, final Class<Roll<R>> cls) {
        final Roll<?> rl = rolls.get(id);
        return rl != null && rl.getClass().isAssignableFrom(cls) ? cls.cast(rl) : null;
    }

    protected static <R extends Roll<?>> void load(final Class<R> rlc, final Function<ConfigurationSection, R> fun) {
        rolls.values().removeIf(rl -> rl.getClass().isAssignableFrom(rlc));
        final OstrovConfig irc = Config.manager.getNewConfig(CON_NAME);
        final ConfigurationSection cs = irc.getConfigurationSection(rlc.getSimpleName());
        if (cs == null) return;
        for (final String id : cs.getKeys(false)) {
            fun.apply(cs.getConfigurationSection(id));
        }
    }
}
