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

	public final Route base;
	public final Route translated;
	public final Route head;
	public final Route tail;
	
	public TranslatedSegment(Line2D base, Line2D translated) {
		if (base == null) {
			throw new RuntimeException();
		}
		this.base = new Route(this, base);
		this.translated = new Route(this, translated);
		Line2D lineHead = new Line2D(base.getFirstPoint(), translated.getFirstPoint());
		head = new Route(this, lineHead);
		Line2D lineTail = new Line2D(base.getSecondPoint(), translated.getSecondPoint());
		tail = new Route(this, lineTail);
	}
	
//	public void add(OverlapPoint overlapPoint) {
//		if (overlapPoints.contains(overlapPoint)) {
//			return;
//		}
//		OverlapPoint other = overlapPoints.stream().filter(s -> s.point.equals(overlapPoint.point)).findAny().orElse(null);
//		if (other != null) {
//			if (overlapPoint.isTainted()!=other.isTainted()) {
//				System.err.println("###### other="+other+", overlapPoint="+overlapPoint);
//				other.taintWith(Taint.INVALID);
//			}
//			return;
//		}
//		overlapPoints.add(overlapPoint);
//		overlapPoint.add(this);
//		overlapPointsOrdered = false;
//	}

//	public TranslatedSegment ensureOrdered() {
//		if (!overlapPointsOrdered) {
//			Comparator<OverlapPoint> p = (a,b) -> {
//				if (a==null) {
//					return -1;
//				}
//				if (b==null) {
//					return 1;
//				}
//				Point2D basePoint = translated.pointA();
//				long dxa = a.point.x-basePoint.x;
//				long dya = a.point.y-basePoint.y;
//
//				long dxb = b.point.x-basePoint.x;
//				long dyb = b.point.y-basePoint.y;
//				
//				
//				long sqDistA = dxa*dxa + dya*dya;
//				long sqDistB = dxb*dxb + dyb*dyb;
//				return Long.compare(sqDistA, sqDistB);
//			};
//			Collections.sort(overlapPoints, p);
//			overlapPointsOrdered = true;
//		}
//		return this;
//	}
	
//	public OverlapPoint findByTranslatedSegment(TranslatedSegment segment) {
//		return overlapPoints.stream().filter(s -> s.contains(segment)).findAny().orElse(null);
//	}
	
	public Stream<OverlapPoint> streamAllOverlapPoints() {
		Stream<OverlapPoint> streamsA = Stream.concat(base.streamOverlapPoints(), translated.streamOverlapPoints());
		Stream<OverlapPoint> streamsB = Stream.concat(tail.streamOverlapPoints(), head.streamOverlapPoints());
		return Stream.concat(streamsA, streamsB); 
	}
//
//	public Iterable<OverlapPoint> overlapPointsIterable() {
//		return overlapPoints; 
//	}
//	
//	public int indexOf(OverlapPoint point) {
//		return overlapPoints.indexOf(point);
//	}
//	
//	public OverlapPoint overlapPointAt(int index) {
//		return index<0 || index>=overlapPoints.size() ? null : overlapPoints.get(index);
//	}

	public boolean isObscuredOverlapPoint(OverlapPoint overlapPoint) {
		int baseCCW = base.relativeCCW(overlapPoint);
		if (baseCCW == 0) {
			return false;
		}
		int translatedCCW = -translated.relativeCCW(overlapPoint);
		if (translatedCCW == 0) {
			return false;
		}
		int headCCW = -head.relativeCCW(overlapPoint);
		if (headCCW == 0) {
			return false;
		}
		int tailCCW = tail.relativeCCW(overlapPoint);
		if (tailCCW == 0) {
			return false;
		}
		
		return baseCCW==tailCCW && tailCCW==headCCW && headCCW==translatedCCW;
	}

	
	
}
