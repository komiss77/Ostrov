package ru.komiss77.modules.quests.progs;

import java.util.Arrays;

public class VarProg implements IProgress {
	
	private final Comparable<?>[] mts;
	private int prog;
	
	public <G extends Comparable<?>> VarProg(final int prog, final G[] mts) {
		this.prog = prog;
		this.mts = mts;
		Arrays.sort(this.mts);
	}
	
	@Override
	public int getProg() {
//		int cnt = 0;
//		for (int t = prog; t != 0; t >>= 1) cnt += t & 1;
		return Integer.bitCount(prog);
	}
	
	@Override
	public int getSave() {
		return prog;
	}
	
	@Override
	public int getGoal() {return mts.length;}

	@Override
	public boolean isDone() {
		return getProg() == mts.length;
	}

	@Override
	public IProgress markDone() {
		prog = (1 << mts.length) - 1;
		return this;
	}

	@Override
	public boolean addNum(final int add) {
		if ((add | prog) == prog) return false;
		prog = add | prog;
		return true;
	}
	
	@Override
	public boolean addVar(final Comparable<?> vr) {
		final int lc = Arrays.binarySearch(mts, vr);
		if (lc < 0 || (prog | (1 << lc)) == prog) return false;
		prog |= 1 << lc;
		return true;
	}
}
