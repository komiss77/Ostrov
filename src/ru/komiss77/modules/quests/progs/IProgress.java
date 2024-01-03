package ru.komiss77.modules.quests.progs;

public interface IProgress {
	
	public int getProg();
	
	public int getSave();

	public int getGoal();

	public boolean isDone();

	public IProgress markDone();

	public boolean addNum(final int prog);

	public boolean addVar(final Comparable<?> vr);
	
}
