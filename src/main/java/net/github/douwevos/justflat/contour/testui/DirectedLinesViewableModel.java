package net.github.douwevos.justflat.contour.testui;

import net.github.douwevos.justflat.types.Bounds2D;

public class DirectedLinesViewableModel implements ViewableModel {

	public final DirectedLines directedLines;
	
	public DirectedLinesViewableModel(DirectedLines directedLines) {
		this.directedLines = directedLines;
	}
	
	
	@Override
	public Bounds2D bounds() {
		return directedLines.bounds();
	}
	
}