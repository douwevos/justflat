package net.github.douwevos.justflat.contour.scaler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.scaler.OverlapPoint.Taint;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;
import net.github.douwevos.justflat.types.values.Line2D.IntersectionInfo;

public class Route {
	
	private final static Log log = Log.instance(false);

	public final Line2D base;
	
	private List<OverlapPoint> overlapPoints = new ArrayList<>();

	private boolean overlapPointsOrdered;

	public Route(Line2D base) {
		this.base = base;
	}
	
	public void add(OverlapPoint overlapPoint) {
		if (overlapPoints.contains(overlapPoint)) {
			return;
		}
		OverlapPoint other = overlapPoints.stream().filter(s -> s.point.equals(overlapPoint.point)).findAny().orElse(null);
		if (other != null) {
			if (overlapPoint.isTainted()!=other.isTainted()) {
				log.debug("###### other={}, overlapPoint={}", other, overlapPoint);
				other.taintWith(Taint.INVALID);
			} else {
				log.debug("****** other={}, overlapPoint={}",other, overlapPoint);
			}
			return;
		}
		overlapPoints.add(overlapPoint);
		overlapPoint.add(this);
		overlapPointsOrdered = false;
	}

	public void remove(OverlapPoint overlapPoint) {
		if (!overlapPoints.contains(overlapPoint)) {
			return;
		}
		overlapPoint.remove(this);
		overlapPoints.remove(overlapPoint);
	}

	
	public Route ensureOrdered() {
		if (!overlapPointsOrdered) {
			Point2D bpa = base.getFirstPoint();
			Point2D bpb = base.getSecondPoint();
			
			long dx = bpa.x - bpb.x;
			long dy = bpa.y - bpb.y;
			final Point2D pr = Point2D.of(bpa.x + dx*10, bpa.y + dy*10);
			Comparator<OverlapPoint> p = (a,b) -> {
				if (a==null) {
					return -1;
				}
				if (b==null) {
					return 1;
				}
				
				long distA = pr.squaredDistance(a.point);
				long distB = pr.squaredDistance(b.point);
				return Long.compare(distA, distB);
			};
			Collections.sort(overlapPoints, p);
			overlapPointsOrdered = true;
		}
		return this;
	}

	
	public OverlapPoint overlapFirst() {
		Point2D firstPoint = base.getFirstPoint();
		return overlapPoints.stream().filter(op -> op.point.equals(firstPoint)).findAny().orElse(null);
	}

	public OverlapPoint overlapSecond() {
		Point2D secondPoint = base.getSecondPoint();
		return overlapPoints.stream().filter(op -> op.point.equals(secondPoint)).findAny().orElse(null);
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

	public void reduceObscureInfo() {
		streamOverlapPoints().forEach(op -> op.reduceObscureInfo());
	}

	public Route duplicate() {
		return new Route(base);
	}



}
