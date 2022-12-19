package net.github.douwevos.justflat.types.values;

import java.util.List;
import java.util.stream.Stream;

public class StartStopLine {

	private static final StartStop[] EMPTY_START_STOPS = new StartStop[0];

	private final StartStop startStops[];
	private final boolean isOrdered;

	
	public StartStopLine() {
		startStops = EMPTY_START_STOPS;
		isOrdered = true;
	}
	
	public StartStopLine(List<StartStop> startStops) {
		if (startStops == null || startStops.isEmpty()) {
			this.startStops = EMPTY_START_STOPS;
			isOrdered = true;
		} else {
			this.startStops = new StartStop[startStops.size()];
			startStops.toArray(this.startStops);
			isOrdered = testOrdered(this.startStops);
		}
	}

	private boolean testOrdered(StartStop[] startStops) {
		StartStop last = null;
		for(StartStop startStop : startStops) {
			if (last != null) {
				if (last.start>startStop.start) {
					return false;
				}
			}
			last = startStop;
		}
		return true;
	}
	
	public boolean contains(long x) {
		if (startStops.length == 0) {
			return false;
		}
		for(StartStop startStop : startStops) {
			if (x>=startStop.start && x<=startStop.stop) {
				return true;
			}
			if (x<startStop.start) {
				break;
			}
		}
		return false;
	}

	public boolean fullyCovers(StartStopLine other) {
		return Stream.of(other.startStops).allMatch(s -> fullyCovers(s));
	}

	public boolean fullyCovers(StartStop other) {
		for(StartStop startStop : startStops) {
			if ((startStop.stop<other.start) || (startStop.start>other.stop)) {
				continue;
			}
			if (startStop.fullyCovers(other)) {
				return true;
			}
			
			if (startStop.stop>other.start) {
				if (!fullyCovers(new StartStop(other.start, startStop.stop))) {
					break;
				}
			}
			if (startStop.start<other.stop) {
				if (!fullyCovers(new StartStop(startStop.start, other.stop))) {
					break;
				}
			}
			return true;
		}
		return false;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StartStopLine) {
			StartStopLine other = (StartStopLine) obj;
			if (other.startStops.length != startStops.length) {
				return false;
			}
			for(int idx=other.startStops.length-1; idx>=0; idx--) {
				if (!other.startStops[idx].equals(startStops[idx])) {
					return false;
				}
			}
		}
		return false;
	}

}
