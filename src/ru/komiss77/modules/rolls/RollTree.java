package ru.komiss77.modules.rolls;

import java.util.*;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.NumUtils;


public class RollTree extends Roll<String[]> {

    private static final String[] EMT = new String[0];

    private static final String SEP = "=";
    private static final char DLM = ':';

    public RollTree(final String id, final String[] roll) {
        super(id, roll, roll.length, 0);
    }

    public RollTree(final String id, final String[] roll, final int number) {
        super(id, roll, Math.min(number, roll.length), 0);
    }

    public RollTree(final String id, final String[] roll, final int number, final int extra) {
        super(id, roll, Math.min(number, roll.length), extra);
    }

    @Override
    protected String[] asAmount(final int amt) {
        ClassUtil.shuffle(it);
        return Arrays.copyOf(it, amt);
    }

    /*public <R> @Nullable R genRoll(final Class<R> cls) {
        if (it.length == 0) return null;
        final int amt = number + Ostrov.random.nextInt(extra);
        for ()
        return genFrom(ClassUtil.rndElmt(it));
    }

    private @Nullable <R> R genFrom(String s) {

    }*/

    public <R> List<R> genRolls(final Class<R> cls) {
        if (it.length == 0) return List.of();
        final int amt = number + Ostrov.random.nextInt(extra);
        final ArrayList<R> lst = new ArrayList<>(amt);
        if (amt < it.length >> 1) {
            for (int i = 0; i < amt; i++) {
                addGen(ClassUtil.rndElmt(it), lst, cls);
            }
        } else {
            ClassUtil.shuffle(it);
            for (int n = amt / it.length; n > 0; n--) {
                for (final String rl : it) addGen(rl, lst, cls);
            }
            for (int i = amt % it.length; i > 0; i--) {
                addGen(it[i - 1], lst, cls);
            }
        }
        return lst;
    }

    private <R> void addGen(final String roll, final ArrayList<R> lst, final Class<R> cls) {
//        Ostrov.log("tr-" + id);
        final Roll<?> rl = Roll.getRoll(roll);
        switch (rl) {
            case null:
                Ostrov.log_warn("No roll " + roll + " in table " + id + "!");
                return;
            case final NARoll ignored: return;
            case final RollTree rr:
                for (final String nr : rr.generate()) {
                    addGen(nr, lst, cls);
                }
                return;
            default:
                final Object gen = rl.generate();
                if (cls.isAssignableFrom(gen.getClass())) {
                    lst.add(cls.cast(gen));
                }
        }
    }

    @Override
    protected String encode() {
        final HashMap<String, Integer> rls = new HashMap<>();
        for (final String rl : it) rls.put(rl, rls.getOrDefault(rl, 0) + 1);
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, Integer> en : rls.entrySet())
            sb.append(SEP).append(en.getValue()).append(DLM).append(en.getKey());
        if (sb.isEmpty()) return "";
        return sb.substring(SEP.length());
    }

    public static void loadAll() {
        load(RollTree.class, cs -> {
            final String[] rls = cs.getString(VAL).split(SEP);
            final List<String> rolls = new ArrayList<>();
            for (final String rl : rls) {
                final int split = rl.indexOf(DLM);
                if (split < 1) continue;
                for (int i = NumUtils.intOf(rl.substring(0, split), 0); i != 0; i--)
                    rolls.add(rl.substring(split + 1));
            }
            return new RollTree(cs.getName(), rolls.toArray(EMT),
                cs.getInt(NUM, 0), cs.getInt(EX, 0));
        });
    }

    public static Builder of(final String id) {
        return new Builder(id, new ArrayList<>());
    }

    public static Builder of(final String id, final Map<String, Integer> weights) {
        final List<String> rolls = new ArrayList<>();
        for (final Map.Entry<String, Integer> en : weights.entrySet()) {
            for (int i = en.getValue(); i > 0; i--) rolls.add(en.getKey());
        }
        return new Builder(id, rolls);
    }

    public static class Builder {

        private final String id;
        private final List<String> rolls;

        private Builder(final String id, final List<String> rolls) {
            this.id = id;
            this.rolls = rolls;
        }

        public Builder add(final Roll<?> rl, final int weight) {
            final String[] nar = new String[weight];
            Arrays.fill(nar, rl.id);
            rolls.addAll(Arrays.asList(nar));
            return this;
        }

        public Builder remove(final Roll<?> rl) {
            rolls.removeIf(r -> r.equals(rl.id));
            return this;
        }

        public RollTree build() {
            return build(rolls.size(), 0);
        }

        public RollTree build(final int number) {
            return build(number, 0);
        }

        public RollTree build(final int number, final int extra) {
            return new RollTree(id, rolls.toArray(EMT), number, extra);
        }
    }
}
