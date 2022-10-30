package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.OverlapPoint.Taint;
import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Point2D;


public class TranslatedSegment {

	public final Line2D base;
	public final Line2D translated;
	public final Line2D head;
	public final Line2D tail;
	
	private List<OverlapPoint> overlapPoints = new ArrayList<>();
	
	private boolean overlapPointsOrdered;
	
	public TranslatedSegment(Line2D base, Line2D translated) {
		if (base == null) {
			throw new RuntimeException();
		}
		this.base = base;
		this.translated = translated;
		head = new Line2D(base.getFirstPoint(), translated.getFirstPoint());
		tail = new Line2D(base.getSecondPoint(), translated.getSecondPoint());
	}
	
	public void add(OverlapPoint overlapPoint) {
		if (overlapPoints.contains(overlapPoint)) {
			return;
		}
		OverlapPoint other = overlapPoints.stream().filter(s -> s.point.equals(overlapPoint.point)).findAny().orElse(null);
		if (other != null) {
			if (overlapPoint.isTainted()!=other.isTainted()) {
				System.err.println("###### other="+other+", overlapPoint="+overlapPoint);
				other.taintWith(Taint.INVALID);
			}
			return;
		}
		overlapPoints.add(overlapPoint);
		overlapPoint.add(this);
		overlapPointsOrdered = false;
	}

	public TranslatedSegment ensureOrdered() {
		if (!overlapPointsOrdered) {
			Comparator<OverlapPoint> p = (a,b) -> {
				if (a==null) {
					return -1;
				}
				if (b==null) {
					return 1;
				}
				Point2D basePoint = translated.pointA();
				long dxa = a.point.x-basePoint.x;
				long dya = a.point.y-basePoint.y;

				long dxb = b.point.x-basePoint.x;
				long dyb = b.point.y-basePoint.y;
				
				
				long sqDistA = dxa*dxa + dya*dya;
				long sqDistB = dxb*dxb + dyb*dyb;
				return Long.compare(sqDistA, sqDistB);
			};
			Collections.sort(overlapPoints, p);
			overlapPointsOrdered = true;
		}
		return this;
	}
	
	public OverlapPoint findByTranslatedSegment(TranslatedSegment segment) {
		return overlapPoints.stream().filter(s -> s.contains(segment)).findAny().orElse(null);
	}
	
	public Stream<OverlapPoint> streamOverlapPoints() {
		return overlapPoints.stream(); 
	}

	public Iterable<OverlapPoint> overlapPointsIterable() {
		return overlapPoints; 
	}
	
	public int indexOf(OverlapPoint point) {
		return overlapPoints.indexOf(point);
	}
	
	public OverlapPoint overlapPointAt(int index) {
		return index<0 || index>=overlapPoints.size() ? null : overlapPoints.get(index);
	}

	public boolean isObscuredOverlapPoint(OverlapPoint overlapPoint) {
		if (overlapPoints.contains(overlapPoint)) {
			return false;
		}
		Point2D testPoint = overlapPoint.point;
		
		int baseCCW = base.relativeCCW(testPoint);
		if (baseCCW == 0) {
			return false;
		}
		int translatedCCW = -translated.relativeCCW(testPoint);
		if (translatedCCW == 0) {
			return false;
		}
		int headCCW = -head.relativeCCW(testPoint);
		if (headCCW == 0) {
			return false;
		}
		int tailCCW = tail.relativeCCW(testPoint);
		if (tailCCW == 0) {
			return false;
		}
		
		return baseCCW==tailCCW && tailCCW==headCCW && headCCW==translatedCCW;
	}

	
	
}
