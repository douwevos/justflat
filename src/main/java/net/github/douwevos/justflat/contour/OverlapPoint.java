package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.List;

import net.github.douwevos.justflat.types.Point2D;

public class OverlapPoint {
	
	public final Point2D point;
	
	private Taint taint = Taint.NONE;
	
	private boolean isUSed;
	
	private List<TranslatedSegment> segments = new ArrayList<>();
	
	public OverlapPoint(Point2D point) {
		this.point = point;
	}
	
	public boolean add(TranslatedSegment segment) {
		boolean result = !segments.contains(segment);
		if (result) {
			segments.add(segment);
		}
		return result;
	}

	public boolean remove(TranslatedSegment segment) {
		return segments.remove(segment);
	}
	
	public boolean contains(TranslatedSegment segment) {
		return segments.contains(segment);
	}
	
	
	public Iterable<TranslatedSegment> segmentIterable() {
		return segments;
	}

	public boolean isTainted() {
		return taint != Taint.NONE && taint != Taint.RECONNECT;
	}
	
	public void taintWith(Taint taint) {
		this.taint = taint;
	}
	
	public Taint getTaint() {
		return taint;
	}
	
	public enum Taint {
		NONE,
		RECONNECT,
		OBSCURED, 
		OVERSHOOT,
		ORIGINAL, 
		INVALID
	}
	
	public void markUsed() {
		isUSed = true;
	}
	
	public boolean isUSed() {
		return isUSed;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof OverlapPoint) {
			OverlapPoint other = (OverlapPoint) obj;
			return other.point.equals(point) && taint == other.taint && isUSed==other.isUSed;
		}
		return false;
	}

	@Override
	public String toString() {
		return "OverlapPoint [point=" + point + ", taint=" + taint + ", isUSed=" + isUSed + "]";
	}
	
	
	
	
}