package net.github.douwevos.justflat.contour.testui;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.types.values.Line2D;

public class DirectedLine {
	public final Contour contour;
	public final Line2D baseLine;
	public final boolean reverse;
	
	public DirectedLine(Contour contour, Line2D baseLine, boolean reverse) {
		this.contour = contour;
		this.baseLine = baseLine;
		this.reverse = reverse;
	}
	
	
	@Override
	public String toString() {
		return "DL[baseLine="+baseLine+"]";
	}
}