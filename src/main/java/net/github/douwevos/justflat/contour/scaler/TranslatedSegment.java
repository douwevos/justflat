package net.github.douwevos.justflat.contour.scaler;

import java.util.stream.Stream;

import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;


public class TranslatedSegment {

	private static final double DIST_DELTA = 0.00000001d;

	public final Route base;
	public final Route translated;
	public final Route head;
	public final Route tail;
	
	public final Point2D headTailCrossPoint;
	public final boolean shrink;
	
	public Route head0, tail0;
	public Route head1, tail1;
	
	
	public TranslatedSegment(Line2D base, Line2D translated, boolean shrink) {
		if (base == null) {
			throw new RuntimeException();
		}
		this.shrink = shrink;
		this.base = new Route(this, base);
		this.translated = new Route(this, translated);
		Line2D lineHead = new Line2D(base.getFirstPoint(), translated.getFirstPoint());
		Line2D lineTail = new Line2D(base.getSecondPoint(), translated.getSecondPoint());
		
		Point2D crossPoint = lineHead.crossPoint(lineTail, null);
		headTailCrossPoint = crossPoint;
		if (crossPoint != null) {
			head0 = new Route(this, new Line2D(crossPoint, base.getFirstPoint()));
			head1 = new Route(this, new Line2D(crossPoint, translated.getFirstPoint()));
			
			tail0 = new Route(this, new Line2D(base.getSecondPoint(), crossPoint));
			tail1 = new Route(this, new Line2D(translated.getSecondPoint(), crossPoint));

		}
		head = new Route(this, lineHead);
		tail = new Route(this, lineTail);
		
	}
	
	
	public Stream<OverlapPoint> streamAllOverlapPoints() {
		Stream<OverlapPoint> streamsA = Stream.concat(base.streamOverlapPoints(), translated.streamOverlapPoints());
		if (head != null) {
			Stream<OverlapPoint> streamsB = Stream.concat(tail.streamOverlapPoints(), head.streamOverlapPoints());
			return Stream.concat(streamsA, streamsB); 
		}
		Stream<OverlapPoint> bases = Stream.concat(head0.streamOverlapPoints(), tail0.streamOverlapPoints());
		Stream<OverlapPoint> translates = Stream.concat(head1.streamOverlapPoints(), tail1.streamOverlapPoints());
		Stream<OverlapPoint> sides = Stream.concat(bases, translates);
		return Stream.concat(streamsA, sides); 
	}

	public boolean isObscuredOverlapPoint(OverlapPoint overlapPoint) {
		if (headTailCrossPoint != null) {
			if (head.indexOf(overlapPoint)>=0 || tail.indexOf(overlapPoint)>=0) {
				return false;
			}
			return isObscuredOverlapPointForHourglassTranslated(overlapPoint) || isObscuredOverlapPointForHourglassBase(overlapPoint);
		}

		Point2D testPoint = overlapPoint.point;

		int baseCCW = base.relativeCCW(testPoint);
		if (baseCCW == 0 || base.base.pointDistanceSq(testPoint)<DIST_DELTA) {
			return false;
		}
		
		int translatedCCW = -translated.relativeCCW(testPoint);
		if (translatedCCW == 0 || translated.base.pointDistanceSq(testPoint)<DIST_DELTA) {
			return false;
		}
		int headCCW = -head.relativeCCW(testPoint);
		if (headCCW == 0 || head.base.pointDistanceSq(testPoint)<DIST_DELTA) {
			return false;
		}
		
		int tailCCW = tail.relativeCCW(testPoint);
		if (tailCCW == 0 || tail.base.pointDistanceSq(testPoint)<DIST_DELTA) {
			return false;
		}
		
		return baseCCW==tailCCW && tailCCW==headCCW && headCCW==translatedCCW;
	}

	
	private boolean isObscuredOverlapPointForHourglassTranslated(OverlapPoint overlapPoint) {

		int ccw0 = head1.relativeCCW(overlapPoint);
		if (ccw0 == 0) {
			return false;
		}
		int ccw1 = tail1.relativeCCW(overlapPoint);
		if (ccw1 == 0) {
			return false;
		}
		int ccw2 = translated.relativeCCW(overlapPoint);
		if (ccw2 == 0) {
			return false;
		}
		return ccw0==ccw1 && ccw1==ccw2;
	}


	private boolean isObscuredOverlapPointForHourglassBase(OverlapPoint overlapPoint) {
		int ccw0 = head0.relativeCCW(overlapPoint);
		if (ccw0 == 0) {
			return false;
		}
		int ccw1 = tail0.relativeCCW(overlapPoint);
		if (ccw1 == 0) {
			return false;
		}
		int ccw2 = base.relativeCCW(overlapPoint);
		if (ccw2 == 0) {
			return false;
		}
		return ccw0==ccw1 && ccw1==ccw2;
	}

	public void obscureInBetween() {
		if (headTailCrossPoint != null) {
			obscureInBetweenOverlapPointsHourglass(head.base.getFirstPoint(), head, "InBtwHead01");
			obscureInBetweenOverlapPointsHourglass(tail.base.getSecondPoint(), tail, "InBtwTail01");
			obscureInBetweenOverlapPoints(translated, true, "InBtwTransS");
			obscureInBetweenOverlapPoints(base, false, "InBtwBaseS");
			OverlapPoint opFirst = translated.overlapFirst();
			opFirst.addObscure2(head.base, true, translated.base, false, "transToHeadS");
			OverlapPoint opSecond = translated.overlapSecond();
			opSecond.addObscure2(translated.base, true, tail.base, true, "transToTailS");
		} else {
			obscureInBetweenOverlapPoints(head, false, "InBtwHead");
			obscureInBetweenOverlapPoints(tail, true, "InBtwTail");
			obscureInBetweenOverlapPoints(translated, false, "InBtwTransN");
			obscureInBetweenOverlapPoints(base, true, "InBtwBaseN");
			OverlapPoint opFirst = translated.overlapFirst();
			OverlapPoint opSecond = translated.overlapSecond();
			if (shrink) {
				opFirst.addObscure2(translated.base, false, head.base, true, "sh-transToHeadN");
				opSecond.addObscure2(tail.base, true, translated.base, true, "sh-transToTailN");
			} else {
				opFirst.addObscure2(head.base, true, translated.base, false, "ex-transToHeadN");
				opSecond.addObscure2(translated.base, true, tail.base, true, "ex-transToTailN");
			}
		}
	}
	
	public void reduceObscureInfo() {
		head.reduceObscureInfo();
		tail.reduceObscureInfo();
		translated.reduceObscureInfo();
		base.reduceObscureInfo();
	}


	private void obscureInBetweenOverlapPointsHourglass(Point2D firstPoint, Route route, String rangeName) {
		long length0Sq = firstPoint.distance(headTailCrossPoint);
		route.streamOverlapPoints().forEach(op -> {
			if (!route.base.isFirstOrSecond(op.point) && !op.point.equals(headTailCrossPoint)) {
				long lengthOpSq = firstPoint.distance(op.point);
				boolean up = lengthOpSq>length0Sq;
				op.addObscure2(route.base, up, route.base, !up, rangeName);
			}
		});
	}


	private void obscureInBetweenOverlapPoints(Route route, boolean up, String rangeName) {
		route.streamOverlapPoints().forEach(op -> {
			Line2D l = route.base;
			if (!l.isFirstOrSecond(op.point)) {
				op.addObscure2(l, up, l, !up, rangeName);
			}
		});
	}
	
	public boolean isShrinking() {
		return shrink;
	}
	
	
}
