package net.github.douwevos.justflat.startstop;

public class StartStop {
	public final long start;
	public final long stop;

	public StartStop(StartStop other) {
		this.start = other.start;
		this.stop = other.stop;
	}
	
	public StartStop(long start, long stop) {
		if (start<stop) {
			this.start = start;
			this.stop = stop;
		} else {
			this.start = stop;
			this.stop = start;
		}
	}
	
	public StartStop withStart(long start) {
		if (this.start == start) {
			return this;
		}
		return new StartStop(start, stop);
	}

	public StartStop withStop(long stop) {
		if (this.stop == stop) {
			return this;
		}
		return new StartStop(start, stop);
	}
	
	
	public boolean fullyCovers(StartStop other) {
		return other.start>=start && other.stop<=stop;
	}
	
	@Override
	public String toString() {
		return "SS[" + start + " - " + stop + "]";
	}


	public StartStop widen(int l) {
		if (l==0) {
			return this;
		}
		if (l<0) {
			long nstart = start-l;
			long nstop = stop+l;
			if (nstart>nstop) {
				return null;
			}
		}
		return new StartStop(start-l, stop+l);
	}
	
	
}