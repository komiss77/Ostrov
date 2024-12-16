package ru.komiss77.modules.rolls;

import java.util.HashMap;
import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;
import ru.komiss77.Cfg;
import ru.komiss77.OVerConfig;
import ru.komiss77.Ostrov;

public abstract class Roll<R> {

    private static final HashMap<String, Roll<?>> rolls = new HashMap<>();
    private static final String CON_NAME = "rolls.yml";
    private static final int VERSION = 1;

    protected static OVerConfig CFG = null;

    protected final R it;
    protected final String id;
    protected final byte number;
    protected final byte extra;

    protected Roll(final String id, final R it, final int number, final int extra) {
        this.id = id;
        this.it = it;
        this.number = (byte) Math.max(0, number);
        this.extra = (byte) Math.max(1, extra + 1);
        if (rolls.putIfAbsent(id, this) == null) save();
        RollTree.tryFill(this);
    }

    protected abstract R asAmount(final int amt);

    public R generate() {
        return asAmount(number + Ostrov.random.nextInt(extra));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        return o instanceof Roll && id.equals(((Roll<?>) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    protected abstract String encode();

    protected static final String VAL = "val";
    protected static final String NUM = "num";
    protected static final String EX = "ex";

    public Roll<R> save() {
        if (CFG == null) CFG = Cfg.manager.getNewConfig(CON_NAME, VERSION);
        final String dir = getClass().getSimpleName() + "." + id + ".";
        CFG.set(dir + VAL, encode());
        CFG.set(dir + NUM, number);
        CFG.set(dir + EX, extra);
        CFG.saveConfig();
        return this;
    }

    public Roll<R> delete() {
        if (CFG == null) CFG = Cfg.manager.getNewConfig(CON_NAME, VERSION);
        CFG.removeKey(getClass().getSimpleName() + "." + id);
        CFG.saveConfig();
        return this;
    }

    public static Roll<?> get(final String id) {
        return rolls.get(id);
    }

    public static <R> Roll<R> get(final String id, final Class<Roll<R>> cls) {
        final Roll<?> rl = rolls.get(id);
        return rl != null && rl.getClass().isAssignableFrom(cls) ? cls.cast(rl) : null;
    }

    protected static <R extends Roll<?>> void load(final Class<R> rlc, final Function<ConfigurationSection, R> fun) {
        rolls.values().removeIf(rl -> rl.getClass().isAssignableFrom(rlc));
        if (CFG == null) CFG = Cfg.manager.getNewConfig(CON_NAME, VERSION);
        if (!CFG.isNew) return;
        final ConfigurationSection cs = CFG.getConfigurationSection(rlc.getSimpleName());
        if (cs == null) return;
        for (final String id : cs.getKeys(false)) {
            fun.apply(cs.getConfigurationSection(id));
        }
    }

    //unrelated
    public static boolean roll(final int chance) {
        return Ostrov.random.nextInt(Math.max(chance, 1)) == 0;
    }

    public static boolean rollIn(final float chance) {
        return Ostrov.random.nextFloat() < Math.clamp(chance, 0f, 1f);
    }

    public static boolean rollOut(final float chance) {
        return Ostrov.random.nextFloat() > Math.clamp(chance, 0f, 1f);
    }
}
