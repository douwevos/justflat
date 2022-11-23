package net.github.douwevos.justflat.contour;

public class Range {
	public final double start;
	public final double end;
	
	public Range(double start, double end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String toString() {
		return "Range["+start+", "+end+"]";
	}
}