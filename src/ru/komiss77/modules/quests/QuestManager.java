package ru.komiss77.modules.quests;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.quests.Quest.QuestFrame;
import ru.komiss77.modules.quests.progs.IProgress;
import ru.komiss77.notes.Slow;

public class QuestManager implements Initiable {

    public static final int QUEST_EXP = 40;
    public static IAdvance iAdvance; //не катит, раз есть ссылка на класс - грузит AdvanceCrazy и срёт ошибку. Надо прятать за интерфейс!

    public QuestManager() {
        reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        onDisable();
        if (Bukkit.getPluginManager().getPlugin("CrazyAdvancementsAPI") != null) {
            Ostrov.log_ok("§2Квесты включены (AdvanceCrazy)");
            iAdvance = new AdvanceCrazy();
        } else {
            Ostrov.log_ok("§2Квесты включены (AdvanceVanila)");
            iAdvance = new AdvanceVanila();
        }
    }

    @Override
    public void onDisable() {
        //if (Cfg.quests) {
        if (iAdvance != null) {
            for (final Player pl : Bukkit.getOnlinePlayers()) {
                iAdvance.resetProgress(pl, true);
            }
            iAdvance.unregister();//HandlerList.unregisterAll(iAdvance);
            iAdvance = null;
        }
        //Ostrov.log_ok("§6Квесты выключены!");
        // }
    }

    @Deprecated
    public static @Nullable Quest byCode(final char code) {
        return Quest.get(code);
    }


    @Slow(priority = 1)
    public static List<Quest> getQuests(final Predicate<Quest> pass) {
        return Quest.nameMap.values().stream().filter(pass).collect(Collectors.toList());
    }

    public static void setOnCloseTab(final Consumer<Player> onAdvCls) {
        //if (disabled()) return;
        iAdvance.onAdvCls(onAdvCls);
    }

    public static void loadQuests() {
        //if (disabled()) return;
        iAdvance.buildAdv();
    }

    public static void showForPl(final Player p, final Oplayer op) {
        if (justGame(op)) return;
        if (Bukkit.isPrimaryThread()) {
            iAdvance.loadPlQs(p, op);
        } else {
            Ostrov.sync(() -> iAdvance.loadPlQs(p, op));
        }
    }

    //для квестов где ammount>0
    @Deprecated
    public static int updatePrg(final Player p, final Oplayer op, final Quest qs) {
        return qs.updProg(p, op);
    }

    //для квестов где ammount>0
    @Deprecated
    public static int updatePrg(final Player p, final Oplayer op, final Quest qs, final IProgress prg, final boolean silent) {
        return qs.setProg(p, op, prg, silent);
    }

    // вызывать SYNC !!!
    //тут только дополнительные проверки.
    //По дефолту, раз сюда засланао проверка, квест должен быть завершен.
    //ну, естественно он будет завершен, если был получен и не был завершен, что проверяется выше.
    //checkProgress нужен для отладки из меню квестов (чтобы не засылало в updateProgress и не меняло lp.getProgress)
    @Deprecated
    public static boolean complete(final Player p, final Oplayer op, final Quest quest) {
        return quest.complete(p, op, false);
    }

    @Deprecated
    public static boolean addProgress(final Player p, final Oplayer op, final Quest qs) {
        return qs.addProg(p, op);
    }

    @Deprecated
    public static boolean addProgress(final Player p, final Oplayer op, final Quest qs, final int i) {
        return qs.addProg(p, op, i);
    }

    @Deprecated
    public static boolean addProgress(final Player p, final Oplayer op, final Quest qs, final Comparable<?> obj) {
        return qs.addProg(p, op, obj);
    }

    @Deprecated
    public static int getProgress(final Oplayer op, final Quest qs) {
        return qs.getProg(op);
    }

    @Deprecated
    public static boolean isComplete(final Oplayer op, final Quest qs) {
        return qs.isComplete(op);
    }

    public static boolean resetProgress(final Player p, final Oplayer op) {
        if (justGame(op)) return false;
        iAdvance.resetProgress(p, false);
        op.quests.clear();
        return true;
    }

    public static void sendToast(final Player p, final ItemStack it, final String msg, final QuestFrame frm) {
        if (justGame(PM.getOplayer(p))) return;
        iAdvance.sendToast(p, it, msg, frm);
    }


    //private static boolean disabled() {
    //    if (!Cfg.quests) {
    //         Ostrov.log_warn("Tried using while Quests are off!");
    //        return true;
    //    }
    //    return false;
    //}

    @Deprecated
    protected static boolean justGame(final Oplayer op) {
        return op.isGuest;
    }


}
