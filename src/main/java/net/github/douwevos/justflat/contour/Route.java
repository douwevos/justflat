package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.OverlapPoint.Taint;
import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Line2D.IntersectionInfo;
import net.github.douwevos.justflat.types.Point2D;

public class Route {

	public final Line2D base;
	public final TranslatedSegment translatedSegment;
	
	private List<OverlapPoint> overlapPoints = new ArrayList<>();

	private boolean overlapPointsOrdered;

	public Route(TranslatedSegment translatedSegment, Line2D base) {
		this.translatedSegment = translatedSegment;
		this.base = base;
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
			} else {
				System.err.println("****** other="+other+", overlapPoint="+overlapPoint);
			}
			return;
		}
		overlapPoints.add(overlapPoint);
		overlapPoint.add(this);
		overlapPointsOrdered = false;
	}

	public Route ensureOrdered() {
		if (!overlapPointsOrdered) {
			Comparator<OverlapPoint> p = (a,b) -> {
				if (a==null) {
					return -1;
				}
				if (b==null) {
					return 1;
				}
				long distA = base.signedAlignedDistanceSq(a.point);
				long distB = base.signedAlignedDistanceSq(b.point);
//				Point2D basePoint = base.pointA();
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
				return Long.compare(distA, distB);
			};
			Collections.sort(overlapPoints, p);
			overlapPointsOrdered = true;
		}
		return this;
	}
	
	public Stream<OverlapPoint> streamOverlapPoints() {
		return overlapPoints.stream(); 
	}

	public Iterable<OverlapPoint> overlapPointsIterable() {
		return overlapPoints; 
	}

	public OverlapPoint find(Point2D point) {
		return overlapPoints.stream().filter(s -> Objects.equals(s.point, point)).findAny().orElse(null);
	}

	public int indexOf(OverlapPoint point) {
		return overlapPoints.indexOf(point);
	}
	
	public OverlapPoint overlapPointAt(int index) {
		return index<0 || index>=overlapPoints.size() ? null : overlapPoints.get(index);
	}

	public int relativeCCW(OverlapPoint overlapPoint) {
		if (overlapPoints.contains(overlapPoint)) {
			return 0;
		}
		return relativeCCW(overlapPoint.point);
	}

	public int relativeCCW(Point2D point) {
		if (overlapPoints.stream().anyMatch(op -> op.point.equals(point))) {
			return 0;
		}
		return base.relativeCCW(point);
	}

	public Point2D crossPoint(Line2D line, IntersectionInfo info) {
		return base.crossPoint(line, info);
	}


}
