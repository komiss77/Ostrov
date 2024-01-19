package ru.komiss77.modules.quests;

import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import ru.komiss77.modules.quests.progs.BlnProg;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.modules.quests.progs.NumProg;
import ru.komiss77.modules.quests.progs.VarProg;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quest {

//смещения работают относительно parent
    protected static final Map<Character, Quest> codeMap = new HashMap<>();
    protected static final Map<String, Quest> nameMap = new CaseInsensitiveMap<>();
    protected static final Map<Quest, List<Component>> loreMap = new HashMap<>();

    public final char code; //только для загрузки/сохранения!
    public final int amount;
    public final Material icon;
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
    public <G extends Comparable<?>> Quest(final char code, final Material icon, final int amount,
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
        loreMap.put(this, ItemUtils.genLore(null, description));

//        Quest rq = this;
//        while (rq.code != ((rq = rq.parent).code));
//        root = rq;
    }

    public IProgress createPrg(final int prg) {
        if (needs != null) {
            return new VarProg(prg, needs);
        } else if (amount == 0) {
            return new BlnProg(prg);
        } else {
            return new NumProg(prg, amount);
        }
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
