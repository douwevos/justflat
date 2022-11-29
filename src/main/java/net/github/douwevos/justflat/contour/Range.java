package net.github.douwevos.justflat.contour;

public class Range {
	public final double start;
	public final double end;
	public final String name;
	
	public Range(double start, double end, String name) {
		this.start = start;
		this.end = end;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "Range[" + name + ":" + start + ", " + end + "]";
	}

	public Range withEnd(double end) {
		return new Range(start, end, name);
	}

	public Range invert() {
		return new Range(end, start, name);
	}
}