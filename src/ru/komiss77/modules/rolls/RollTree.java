package ru.komiss77.modules.rolls;

import java.util.*;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.Ostrov;
import ru.komiss77.notes.Slow;
import ru.komiss77.objects.Duo;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.StringUtil;


public class RollTree extends Roll<Roll<? extends @Nullable Object>[]> {

    protected static final Map<String, Wait> waits = new HashMap<>();
    protected record Wait(Roll<?>[] rls, Integer pos) {}

    public static void tryFill(final Roll<?> roll) {
        final Wait wait = waits.remove(roll.id);
        if (wait == null) return; wait.rls[wait.pos] = roll;
    }

    private final int[] wgts;
    private final int total;

    private RollTree(final String id, final Roll<?>[] roll,
        final int[] wgts, final int number, final int extra) {
        super(id, roll, number, extra); this.wgts = wgts;
        int total = 0;
        for (int i = 0; i != wgts.length; i++)
            total += wgts[i];
        this.total = total;
    }

    public static RollTree get(final String id) {
        return ClassUtil.cast(Roll.get(id), RollTree.class);
    }

    @Override
    @Slow(priority = 1)
    protected Roll<?>[] asAmount(final int amt) {
        final int[] sar = new int[amt];
        for (int i = 0; i != amt; i++)
            sar[i] = Ostrov.random.nextInt(total);
        Arrays.sort(sar);
        final Roll<?>[] rls = new Roll<?>[amt];
        int ix = 0, curr = sar[ix], cnt = 0;
        for (int i = 0; i != wgts.length; i++) {
            cnt += wgts[i];
            while (curr < cnt) {
                rls[ix] = it[i]; ix++;
                if (ix == sar.length)
                    return rls;
                curr = sar[ix];
            }
        }
        return rls;
    }

    public <R> @Nullable R genRoll(final Class<R> cls) {
        if (it.length == 0) return null;
        int ttl = 0;
        for (int i = 0; i != wgts.length; i++) {
            final Roll<?> rl = it[i];
            ttl += rl == null ? 0 : wgts[i]
                * (rl.number + rl.extra >> 1);
        }
        final int pos = Ostrov.random.nextInt(ttl);
        int cnt = 0;
        for (int i = 0; i != wgts.length; i++) {
            final Roll<?> rl = it[i];
            cnt += rl == null ? 0 : wgts[i]
                * (rl.number + rl.extra >> 1);
            if (pos < cnt) return genFrom(rl, cls);
        }
        return null;
    }

    private @Nullable <R> R genFrom(final @Nullable Roll<?> roll, final Class<R> cls) {
//        Ostrov.log("tr-" + id);
        return switch (roll) {
            case null -> {
                Ostrov.log_warn("No roll in table " + id + " yet!");
                yield null;
            }
            case final NARoll ignored -> null;
            case final RollTree rr -> rr.genRoll(cls);
            default -> {
                final Object gen = roll.generate();
                yield cls.isInstance(gen) ? cls.cast(gen) : null;
            }
        };
    }

    public <R> List<R> genRolls(final Class<R> cls) {
        if (it.length == 0) return List.of();
        final ArrayList<R> lst = new ArrayList<>();
        for (final Roll<?> rl : generate()) {
            addGen(rl, lst, cls);
        }
        return lst;
    }

    private <R> void addGen(final Roll<?> roll, final ArrayList<R> lst, final Class<R> cls) {
//        Ostrov.log("tr-" + id);
        switch (roll) {
            case null:
                Ostrov.log_warn("No roll in table " + id + " yet!");
                return;
            case final NARoll ignored: return;
            case final RollTree rr:
                for (final Roll<?> nr : rr.generate()) {
                    addGen(nr, lst, cls);
                }
                return;
            default:
                final Object gen = roll.generate();
                if (cls.isInstance(gen))
                    lst.add(cls.cast(gen));
        }
    }

    @Override
    protected String encode() {
        final HashMap<String, Integer> rls = new HashMap<>();
        for (final Roll<?> rl : it) rls.put(rl.id, rls.getOrDefault(rl, 0) + 1);
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, Integer> en : rls.entrySet())
            sb.append(StringUtil.SPLIT_0).append(en.getValue()).append(StringUtil.SPLIT_1).append(en.getKey());
        if (sb.isEmpty()) return "";
        return sb.substring(StringUtil.SPLIT_0.length());
    }

    public static void loadAll() {
        load(RollTree.class, cs -> {
            final Builder bld = of(cs.getName());
            final String[] rls = cs.getString(VAL).split(StringUtil.SPLIT_0);
            for (final String rl : rls) {
                final int split = rl.indexOf(StringUtil.SPLIT_1);
                if (split < 0) continue;
                bld.add(rl.substring(0, split),
                    NumUtil.intOf(rl.substring(split + 1), 1));
            }
            return bld.build(cs.getInt(NUM, 0), cs.getInt(EX, 0));
        });
    }

    public static Builder of(final String id) {
        return new Builder(id);
    }

    public static class Builder {

        private final String id;
        private final List<Duo<String, Integer>> weighed;

        private Builder(final String id) {
            this.id = id;
            this.weighed = new ArrayList<>();
        }

        public Builder add(final Roll<?> rl, final int weight) {
            weighed.add(new Duo<>(rl.id, weight));
            return this;
        }

        public Builder add(final String rl, final int weight) {
            weighed.add(new Duo<>(rl, weight));
            return this;
        }

        public RollTree build() {
            return build(weighed.size(), 0);
        }

        public RollTree build(final int number) {
            return build(number, 0);
        }

        public RollTree build(final int number, final int extra) {
            final int sz = weighed.size();
            final Roll<?>[] rls = new Roll<?>[sz];
            final int[] weigh = new int[sz];
            for (int i = 0; i != sz; i++) {
                final Duo<String, Integer> wrl = weighed.get(i);
                final Roll<?> rl = get(wrl.key());
                if (rl == null) waits.put(wrl.key(), new Wait(rls, i));
                rls[i] = rl; weigh[i] = wrl.val();
            }
            return new RollTree(id, rls, weigh, number, extra);
        }
    }
}
