package ru.komiss77.modules.quests;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.quests.progs.BlnProg;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.modules.quests.progs.NumProg;
import ru.komiss77.modules.quests.progs.VarProg;
import ru.komiss77.notes.OverrideMe;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemUtil;

public class Quest {

    //смещения работают относительно parent
    protected static final Map<Character, Quest> codeMap = new HashMap<>();
    protected static final Map<String, Quest> nameMap = new CaseInsensitiveMap<>();
    protected static final Map<Quest, List<Component>> loreMap = new HashMap<>();

    public final char code; //только для загрузки/сохранения!
    public final int amount;
    public final ItemStack icon;
    public final String displayName;
    public final String description;
    public final String backGround;
    public final QuestVis vision;
    public final QuestFrame frame;
    public final Comparable<?>[] needs;
    public final Quest parent;
    //    public final Quest root;
    public final int pay;

    public Quest[] children;
    public float dx, dy;
    public int size;

    //с квестами связано
    //public static final Map<String,Integer>racePlayers = new HashMap<>();
    public <G extends Comparable<?>> Quest(final char code, final ItemType icon, final int amount,
        final @Nullable G[] needs, final Quest parent, final String displayName, final String description,
        final String backGround, final QuestVis vision, final QuestFrame frame, final int pay) {
        this(code, icon.createItemStack(), amount, needs, parent, displayName,
            description, backGround, vision, frame, pay);
    }

    public <G extends Comparable<?>> Quest(final char code, final ItemStack icon, final int amount,
        final @Nullable G[] needs, final Quest parent, final String displayName, final String description,
        final String backGround, final QuestVis vision, final QuestFrame frame, final int pay) {

        this.code = code;
        this.icon = icon;
        this.amount = amount;
        this.parent = parent == null ? this : parent;
        this.displayName = displayName;
        this.description = description;
        this.backGround = backGround;
        this.vision = vision;
        this.frame = frame;
        this.needs = needs;
        this.pay = pay;

        children = new Quest[0];
        dx = 0f;
        dy = 0f;
        size = 1;

        codeMap.put(code, this);
        nameMap.put(displayName, this);
        loreMap.put(this, ItemUtil.genLore(null, description));
    }

    @OverrideMe
    public static @Nullable Quest get(final char code) {
        return Quest.codeMap.get(code);
    }

    public IProgress createPrg(final int prg) {
        if (needs != null) return new VarProg(prg, needs);
        else if (amount == 0) return new BlnProg(prg);
        else return new NumProg(prg, amount);
    }

    //для квестов где ammount>0
    public int setProg(final Player p, final Oplayer op, final IProgress prg, final boolean silent) {
        if (op.isGuest) return 0;
        QuestManager.iAdvance.sendProgress(p, this, prg.getProg(), false);
        if (prg.isDone()) QuestManager.iAdvance.sendComplete(p, this, silent);
        return prg.getProg();
    }

    //для квестов где ammount>0
    public int updProg(final Player p, final Oplayer op) {
        if (op.isGuest) return 0;
        final IProgress prg = op.quests.get(this);
        if (prg != null) return setProg(p, op, prg, true);
        QuestManager.iAdvance.sendProgress(p, this, 0, true);
        return 0;
    }

    // вызывать SYNC !!!
    //тут только дополнительные проверки.
    //По дефолту, раз сюда засланао проверка, квест должен быть завершен.
    //ну, естественно он будет завершен, если был получен и не был завершен, что проверяется выше.
    //checkProgress нужен для отладки из меню квестов (чтобы не засылало в updateProgress и не меняло lp.getProgress)
    public boolean complete(final Player p, final Oplayer op, final boolean silent) {
        if (op.isGuest) return false;

        if (!Bukkit.isPrimaryThread()) {
            Ostrov.log_warn("Асинхронный вызов tryCompleteQuest :" + toString() + ", " + p.getName());
        }

        //перед завершением квестов со счётчиками обновить прогресс
//        if (amount > 0) updProg(p, op);
        final IProgress pr = op.quests.get(this);
        if (pr == null) {
            final IProgress np = createPrg(0);
            op.quests.put(this, np.markDone());
            setProg(p, op, np, silent);
            return true;
        }
        if (!pr.isDone()) {
            setProg(p, op, pr.markDone(), silent);
            return true;
        }
        return false;
    }

    public boolean addProg(final Player p, final Oplayer op) {
        if (op.isGuest) return false;
        if (addProg(p, op, 1)) return true;
        if (needs == null) return false;
        final IProgress prg = op.quests.get(this);
        for (final Comparable<?> c : needs) {
            if (prg.addVar(c)) {
                setProg(p, op, prg, false);
                return true;
            }
        }
        return false;
    }

    public boolean addProg(final Player p, final Oplayer op, final int i) {
        if (op.isGuest) return false;
        final IProgress prg = op.quests.get(this);
        if (prg == null) {
            final IProgress np = createPrg(i);
            op.quests.put(this, np);
            setProg(p, op, np, false);
            return true;
        }
        if (prg.addNum(i)) {
            setProg(p, op, prg, false);
            return true;
        }
        return false;
    }

    public boolean addProg(final Player p, final Oplayer op, final Comparable<?> obj) {
        if (op.isGuest) return false;
        final IProgress prg = op.quests.get(this);
        if (prg == null) {
            final IProgress np = createPrg(0);
            if (np.addVar(obj)) {
                op.quests.put(this, np);
                setProg(p, op, np, false);
                return true;
            } else {
                return false;
            }
        }
        if (prg.addVar(obj)) {
            setProg(p, op, prg, false);
            return true;
        }
        return false;
    }

    public int getProg(final Oplayer op) {
        if (op.isGuest) return 0;
        final IProgress prg = op.quests.get(this);
        if (prg == null) return 0;
        return prg.getProg();
    }

    public boolean isComplete(final Oplayer op) {
        if (op.isGuest) return false;
        final IProgress prg = op.quests.get(this);
        return prg != null && prg.isDone();
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Quest && ((Quest) o).code == code;
    }

    @Override
    public int hashCode() {
        return code;
    }

    public enum QuestVis {
        ALWAYS, PARENT, HIDDEN,
    }

    public enum QuestFrame {
        TASK, GOAL, CHALLENGE,
    }

    public Color getBBColor() {
        return switch (frame) {
            case CHALLENGE -> Color.PINK;
            case GOAL -> Color.BLUE;
            case TASK -> Color.YELLOW;
        };
    }

    @Override
    public String toString() {
        return displayName + ", n=" + amount + ", dx/dy=" + dx + "/" + dy + ", chs=" + children.length + ", sz=" + size;
    }

}
