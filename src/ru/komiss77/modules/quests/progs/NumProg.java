package ru.komiss77.modules.quests.progs;

public class NumProg implements IProgress {
	
	private final int max;
	private int prog;
	
	public NumProg(final int prog, final int max) {
		this.prog = prog;
		this.max = max;
	}
	
	@Override
	public int getProg() {
		return prog;
	}
	
	@Override
	public int getSave() {
		return prog;
	}
	
	@Override
	public int getGoal() {return max;}

	@Override
	public boolean isDone() {
		return prog == max;
	}

	@Override
	public IProgress markDone() {
		prog = max;
		return this;
	}

	@Override
	public boolean addNum(final int add) {
		final int ttl = Math.min(max, prog + add);
		if (ttl == prog) return false;
		prog = Math.min(max, prog + add);
		return true;
	}

	@Override
	public boolean addVar(final Comparable<?> vr) {
		if (vr == null) return false;
		if (vr instanceof final Integer vri) {
			final int ttl = Math.min(max, prog + vri);
			if (ttl == prog) return false;
			prog = ttl;
			return true;
		}
		return false;
	}

}
