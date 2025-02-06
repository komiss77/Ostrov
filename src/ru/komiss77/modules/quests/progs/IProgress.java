package ru.komiss77.modules.quests.progs;

public interface IProgress {

    int getProg();

    int getSave();

    int getGoal();

    boolean isDone();

    IProgress markDone();

    boolean addNum(final int prog);

    boolean addVar(final Comparable<?> vr);

}
