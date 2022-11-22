package net.github.douwevos.justflat.contour;

import java.util.stream.Stream;

import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Point2D;


public class TranslatedSegment {

	public final Route base;
	public final Route translated;
	public final Route head;
	public final Route tail;
	
	public final Point2D headTailCrossPoint;
	
	public Route base0, base1;
	public Route translated0, translated1;
	
	
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
		
		Point2D crossPoint = head.crossPoint(lineTail, null);
		headTailCrossPoint = crossPoint;
		if (crossPoint != null) {
			Line2D lineBase0 = new Line2D(crossPoint, base.getFirstPoint());
			base0 = new Route(this, lineBase0);
			
			Line2D lineBase1 = new Line2D(base.getSecondPoint(), crossPoint);
			base1 = new Route(this, lineBase1);
	
			Line2D lineTail0 = new Line2D(crossPoint, translated.getFirstPoint());
			translated0 = new Route(this, lineTail0);
			Line2D lineTail1 = new Line2D(translated.getSecondPoint(), crossPoint);
			translated1 = new Route(this, lineTail1);
		}
		
	}
	
	
	public Stream<OverlapPoint> streamAllOverlapPoints() {
		Stream<OverlapPoint> streamsA = Stream.concat(base.streamOverlapPoints(), translated.streamOverlapPoints());
		Stream<OverlapPoint> streamsB = Stream.concat(tail.streamOverlapPoints(), head.streamOverlapPoints());
		return Stream.concat(streamsA, streamsB); 
	}

	public boolean isObscuredOverlapPoint(OverlapPoint overlapPoint) {
		if (headTailCrossPoint != null) {
			return isObscuredOverlapPointForHourglassTranslated(overlapPoint) || isObscuredOverlapPointForHourglassBase(overlapPoint);
		}
		
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

	private boolean isObscuredOverlapPointForHourglassTranslated(OverlapPoint overlapPoint) {

		int ccw0 = translated0.relativeCCW(overlapPoint);
		if (ccw0 == 0) {
			return false;
		}
		int ccw1 = translated1.relativeCCW(overlapPoint);
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
		int ccw0 = base0.relativeCCW(overlapPoint);
		if (ccw0 == 0) {
			return false;
		}
		int ccw1 = base1.relativeCCW(overlapPoint);
		if (ccw1 == 0) {
			return false;
		}
		int ccw2 = base0.relativeCCW(overlapPoint);
		if (ccw2 == 0) {
			return false;
		}
		return ccw0==ccw1 && ccw1==ccw2;
	}

	
	
}
