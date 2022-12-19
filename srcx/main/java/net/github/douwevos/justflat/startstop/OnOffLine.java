package net.github.douwevos.justflat.startstop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.github.douwevos.justflat.types.values.StartStop;

public class OnOffLine {

	List<OnOffDot> rawDots;

	public OnOffLine duplicate() {
		OnOffLine copy = new OnOffLine();
		copy.rawDots = rawDots == null ? null : new ArrayList<>(rawDots);
		return copy;
	}

	public void reset() {
		if (rawDots != null) {
			rawDots.clear();
		}
	}
	
	public boolean isEmpty() {
		return rawDots==null || rawDots.isEmpty();
	}

	public List<StartStop> apply() {
		if (rawDots == null || rawDots.isEmpty()) {
			return null;
		}
		List<StartStop> shapeStartStop = new ArrayList<>();

		OnOffDot last = rawDots.get(0);
		OnOffDot start = last;
		OnOffDot stop = null;
		for (int in = 1; in < rawDots.size(); in++) {
			OnOffDot onOffDot = rawDots.get(in);

			if (onOffDot.up == last.up) {
				if (stop != null) {
					stop = onOffDot;
				}
			} else {
				if (stop == null) {
					stop = onOffDot;
				} else {
					shapeStartStop.add(new StartStop(start.x, stop.x));
					stop = null;
					start = onOffDot;
				}
			}
			last = onOffDot;
		}
		if (stop != null) {
			shapeStartStop.add(new StartStop(start.x, stop.x));
		}

		long offsets[] = new long[shapeStartStop.size() * 2];
		int ou = 0;
		for (StartStop ss : shapeStartStop) {
			offsets[ou++] = ss.start;
			offsets[ou++] = ss.stop;
		}
		shapeStartStop.clear();
		Arrays.sort(offsets);
		for (ou = 0; ou < offsets.length; ou += 2) {
			shapeStartStop.add(new StartStop(offsets[ou], offsets[ou + 1]));
		}

//			shapeStartStop.sort((a,b) -> {
//				return a.start<b.start ? -1 : 1;
//			});

//			System.out.println("rawDots="+rawDots);
//			System.out.println("shapeStartStop="+shapeStartStop);

		return shapeStartStop;
	}

	public void add(long pos, boolean up) {
		if (rawDots == null) {
			rawDots = new ArrayList<>();
		}
		rawDots.add(new OnOffDot(pos, up));
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (OnOffDot dot : rawDots) {
			if (s.length() > 0) {
				s.append(',');
			}
			s.append(dot.x).append(dot.up ? 'u' : 'd');

		}
		return "Line[" + s + "]";
	}

	

	static class OnOffDot {
		public final long x;
		public final boolean up;
		public OnOffDot(long x, boolean up) {
			this.x = x;
			this.up = up;
			
		}
		public OnOffDot withX(long newX) {
			if (x==newX) {
				return this;
			}
			return new OnOffDot(newX, up);
		}
		@Override
		public String toString() {
			return "OnOffDot[x=" + x + (up ? 'u' : 'd') + "]";
		}
	}


}