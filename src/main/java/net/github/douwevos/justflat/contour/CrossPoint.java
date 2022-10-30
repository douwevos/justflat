package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.github.douwevos.justflat.types.Point2D;

public class CrossPoint implements Iterable<TargetLine> {
	
	public final Point2D crossPoint;
	private final List<TargetLine> lines = new ArrayList<>();
	
	boolean tainted;

	public CrossPoint(Point2D crossPoint) {
		this.crossPoint = crossPoint;
	}

	public void add(TargetLine line) {
		lines.add(line);
	}
	
	public void remove(TargetLine targetLine) {
		lines.remove(targetLine);
	}

	
	public void markTainted() {
		tainted = true;
	}
	
	public boolean isTainted() {
		return tainted;
	}
	
//	public List<TargetLine> getLines() {
//		return lines;
//	}
	
	public int lineCount() {
		return lines.size();
	}
	
	
	@Override
	public Iterator<TargetLine> iterator() {
		return new ArrayList<>(lines).iterator();
	}
	
	@Override
	public String toString() {
		return "CP["+crossPoint+"]";
	}

	
}