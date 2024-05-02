package ru.komiss77.modules.quests.progs;

public class BlnProg implements IProgress {
	
	private boolean done;
	
	public BlnProg(final int prog) {
		this.done = prog > 0;
	}
	
	@Override
	public int getProg() {
		return done ? 1 : 0;
	}
	
	@Override
	public int getSave() {
		return done ? 1 : 0;
	}
	
	@Override
	public int getGoal() {return 1;}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public IProgress markDone() {
		done = true;
		return this;
	}

	@Override
	public boolean addNum(final int add) {
		if (done) return false;
		done = true; return true;
	}

	@Override
	public boolean addVar(final Comparable<?> vr) {
		if (done) return false;
		done = true; return true;
	}

}
