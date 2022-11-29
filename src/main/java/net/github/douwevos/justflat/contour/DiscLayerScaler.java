package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.OverlapPoint.Taint;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.startstop.StartStop;
import net.github.douwevos.justflat.types.Bounds2D;
import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Line2D.IntersectionInfo;
import net.github.douwevos.justflat.types.Point2D;


public class DiscLayerScaler {

	boolean doDebug = false;
	
	Log log = Log.instance();

	
	public List<MutableContour> allMutableContours = new ArrayList<>();
	
	public ContourLayer scale(ContourLayer input, double thickness, boolean cleanup) {
		log.debug2("###################################################################### scaling "+thickness);
		List<MutableContour> directedContours = createMutableContours(input, thickness);
		List<Contour> scaledContourList = new ArrayList<>();
		
		for(MutableContour contour : directedContours) {
			List<ScaledContour> scaledContour = scale(contour, thickness, cleanup);
			
			scaledContour.removeIf((c) ->  c.lineCount()<2);
			scaledContour.stream().map(sc -> sc.createContour()).forEach(c -> scaledContourList.add(c));
//			scaledContourList.addAll(scaledContour);
		}
		
		ContourLayer result = new ContourLayer(input.getWidth(), input.getHeight());
		scaledContourList.stream().forEach(result::add);
		return result;
	}

	private List<MutableContour> createMutableContours(ContourLayer input, double thickness) {
		List<MutableContour> result = new ArrayList<>();
		for(Contour contour : input) {
			MutableContour c = correctDirection(input, contour, thickness);
			result.add(c);
		}
		return result;
	}

	private MutableContour correctDirection(ContourLayer input, Contour contour, double thickness) {
		int insideCount = 0;
		int totalCount = 0;
		Point2D pa = contour.getLast();
		for(Point2D pb : contour) {
			long dxab = pb.x - pa.x;
			long dyab = pb.y - pa.y;
			double length = Math.sqrt(dxab*dxab + dyab*dyab);
			if (length==0) {
				continue;
			}
				
			long nxa = pa.x + dxab/2 - 2*Math.round(dyab/length);
			long nya = pa.y + dyab/2 + 2*Math.round(dxab/length);

			List<StartStop> scanline = input.scanlineHorizontal(nya);
			if (scanline != null) {
				if (scanline.stream().anyMatch(ss -> nxa>=ss.start && nxa<=ss.stop)) {
					insideCount++;
				}
			}
			
			totalCount++;
			pa = pb;
		}
		
		boolean reverse = insideCount<(totalCount-insideCount);
		return new MutableContour(contour, !reverse, -thickness);
	}

	

	private List<ScaledContour> scale(MutableContour mutableContour, double thickness, boolean cleanup) {
		if (mutableContour.isEmpty()) {
			return Collections.emptyList();
		}
		
		OverlapPointFactory overlapPointFactory = new OverlapPointFactory();
		
		produceSegmentPoints(overlapPointFactory, mutableContour);
		
		markConnectingSegementPoints(overlapPointFactory, mutableContour);
		
		obscureInBetween(mutableContour);
		
		taintObscuredSegmentPoints(mutableContour);

		allMutableContours.add(mutableContour);
		
//		List<TargetLine> allTargetLines = mutableContour.lines.stream().flatMap(ml -> ml.getTargetLines().stream()).collect(Collectors.toList());
//		ScaledContour scaledContour = new ScaledContour(mutableContour.source, allTargetLines);
//		ArrayList<ScaledContour> result = new ArrayList<>();
//		result.add(scaledContour);
//		return result;
		
		List<ScaledContour> result = new ArrayList<>();
//		while(true) {
//			ScaledContour scaledContour = rebuiltScaledContourNew(mutableContour);
//			if (scaledContour==null) {
//				break;
//			}
//			if (scaledContour != null) {
//				
//				boolean isCCW = scaledContour.isCCW();
//				if (!isCCW) {
//					result.add(scaledContour);
//				}
//			}
//			
////			firstCleanSection = nextCleanUnusedSection(mutableContour, usedAsStartSection);
//		}
//		
//		result.clear();
//		
		
		while(true) {
			OverlapPoint startOverlapPoint = findNextUntaintedAndUnusedOverlapPoint(mutableContour);
			if (startOverlapPoint == null) {
				break;
			}
			ScaledContour scaledContour = pointsToScaledContour(mutableContour, startOverlapPoint);
			
			
			
			if (scaledContour != null && boundsDoNotExeed(scaledContour, mutableContour)) {
				result.add(scaledContour);
			} else if (scaledContour != null) {
				Bounds2D original = mutableContour.source.getBounds();
				System.err.println("original:"+original);
				Bounds2D scaled = scaledContour.getBounds();
				System.err.println("scaled  :"+scaled);
				Bounds2D union = original.union(scaled);
				System.err.println("union   :"+union);
				
			}
			
			startOverlapPoint.markUsed();
		}
		
		return result;
	}
	
	
	private boolean boundsDoNotExeed(ScaledContour scaledContour, MutableContour mutableContour) {
		Bounds2D original = mutableContour.source.getBounds();
		Bounds2D scaled = scaledContour.getBounds();
		Bounds2D union = original.union(scaled);
		return union.equals(original);
	}

	private OverlapPoint findNextUntaintedAndUnusedOverlapPoint(MutableContour mutableContour) {
		List<OverlapPoint> overlapPoints = mutableContour.streamSegments().flatMap(s -> s.streamAllOverlapPoints())
				.filter(s -> !s.isTainted() && !s.isUSed())
				.distinct().collect(Collectors.toList());
		return overlapPoints.isEmpty() ? null : overlapPoints.get(0);
		
	}

	private ScaledContour pointsToScaledContour(MutableContour mutableContour, OverlapPoint startOverlapPoint) {
//		List<OverlapPoint> overlapPoints = mutableContour.streamSegments().flatMap(s -> s.streamOverlapPoints())
//				.filter(s -> !s.isTainted() && !s.isUSed())
//				.distinct().collect(Collectors.toList());
//		if (overlapPoints.isEmpty()) {
//			return null;
//		}
//		
		OverlapPoint originating = null;
		OverlapPoint current = startOverlapPoint;
		List<OverlapPoint> passed = new ArrayList<>();
		StringBuilder buf = new StringBuilder();
		while(true) {
			buf.setLength(0);
			OverlapPoint next = null;
			buf.append("current="+current+"\n");
			for(Route route : current.routeIterable()) {
				route.ensureOrdered();
				int indexOf = route.indexOf(current);
				
				int s = 0;
				buf.append("   route-line: " + route.base + "\n");
				buf.append("   points    : ");
				for(OverlapPoint ov : route.overlapPointsIterable())  {
					if (s==indexOf) {
						buf.append("["+ov+"]");
					} else {
						buf.append(":"+ov);
					}
					s++;
				}
				buf.append("\n");

				
				
				OverlapPoint left = route.overlapPointAt(indexOf-1);
				buf.append("       left="+left+"\n");
				if (left!=null && !left.isTainted() && !Objects.equals(left, originating)) {
					next = left;
					break;
				}
				OverlapPoint right = route.overlapPointAt(indexOf+1);
				buf.append("       right="+right+"\n");
				if (right!=null && !right.isTainted() && !Objects.equals(right, originating)) {
					next = right;
					break;
				}
			}
			if (next == null) {
				passed.forEach(s -> s.markUsed());
				System.err.println("marked dirty at:"+current.point);
				System.err.println(buf.toString());
				return null;
			}
			int indexOf = passed.indexOf(next);

			if (indexOf>0) {
				passed.subList(0, indexOf).clear();
				break;
			}
			if (indexOf==0) {
				break;
			}
			passed.add(next);
			originating = current;
			current = next;
			
			System.err.println("from: "+originating.point+" .. "+current.point);
		}
		
		
		
		ObscuredInfo info = new ObscuredInfo();
		List<TargetLine> lines = new ArrayList<>();
		OverlapPoint overlapPoint = passed.get(0);
		overlapPoint.markUsed();
		Point2D start = overlapPoint.point;
		info = info.invert(overlapPoint.getObscuredInfo());
		CrossPoint cpStart = new CrossPoint(start);
		
		for(int idx=1; idx<passed.size(); idx++) {
			overlapPoint = passed.get(idx);
			info = info.invert(overlapPoint.getObscuredInfo());
			overlapPoint.markUsed();
			Point2D end = overlapPoint.point;
			CrossPoint cpEnd = new CrossPoint(end);
			TargetLine targetLine = new TargetLine(null, cpStart, cpEnd);
			lines.add(targetLine);
			cpStart = cpEnd;
		}

		if (!info.isFullyObscured()) {
			return null;
		}
		System.err.println("lines="+lines);
		
		ScaledContour scaledContour = new ScaledContour(mutableContour.getSource(), lines );
//		
//		
//		
//		List<OverlapPoint> enlisted = new ArrayList<>();
//		enlisted.add(overlapPoint);
//		while(true) {
//			for(TranslatedSegment translatedSegment : overlapPoint.segmentIterable()) {
//				translatedSegment.ensureOrdered();
//				
//			}
//		}
		return scaledContour;
	}

	private void taintOvershootingPoints(MutableContour mutableContour) {
		TranslatedSegment prevSegment = mutableContour.lastSegement();
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
			
			int validPoints = (int) translatedSegment.streamAllOverlapPoints().filter(s-> !s.isTainted()).count();
			if (validPoints==0) {
				continue;
			}
			List<OverlapPoint> untaintedPoints = translatedSegment.streamAllOverlapPoints().filter(s -> !s.isTainted()).collect(Collectors.toList());

			Route testBaseLine = translatedSegment.base;
			Route testTransLine = translatedSegment.translated;
			
			for(TranslatedSegment other : mutableContour.segmentIterable()) {
				IntersectionInfo info = new IntersectionInfo();
				Route baseLine = other.base;
				Point2D crossPoint = baseLine.crossPoint(testTransLine.base, info);
				
				if (crossPoint==null) {
					continue;
				}
				
				/* the raw 'translated line crosses a 'base' line this might be an overshoot */
				
				int testCCWA = baseLine.base.relativeCCW(testTransLine.base.getFirstPoint());
				int testCCWB = baseLine.base.relativeCCW(testTransLine.base.getSecondPoint());
				int baseCCWA = baseLine.base.relativeCCW(testBaseLine.base.getFirstPoint());
				int baseCCWB = baseLine.base.relativeCCW(testBaseLine.base.getSecondPoint());

				if ((baseCCWA<=0d) && (baseCCWB<=0d)) {
					// any test-CCWs larger then 0d are overshoots;
					for(int idx=untaintedPoints.size()-1; idx>=0; idx--) {
						OverlapPoint overlapPoint = untaintedPoints.get(idx);
						int relativeCCW = baseLine.base.relativeCCW(overlapPoint.point);
						if (relativeCCW>0d) {
							overlapPoint.taintWith(Taint.OVERSHOOT);
							untaintedPoints.remove(idx);
						}
					}
				} else if ((baseCCWA>=0d) && (baseCCWB>=0d)) {
					// any test-CCWs less then 0d are overshoots;
					for(int idx=untaintedPoints.size()-1; idx>=0; idx--) {
						OverlapPoint overlapPoint = untaintedPoints.get(idx);
						int relativeCCW = baseLine.base.relativeCCW(overlapPoint.point);
						if (relativeCCW<0d) {
							overlapPoint.taintWith(Taint.OVERSHOOT);
							untaintedPoints.remove(idx);
						}
					}
				} 
			
				if (untaintedPoints.isEmpty()) {
					break;
				}
			}
			
			
		}
	}


	private void markConnectingSegementPoints(OverlapPointFactory overlapPointFactory, MutableContour mutableContour) {
		TranslatedSegment prevSegment = mutableContour.lastSegement();
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
//			if (translatedSegment.translated.base.getFirstPoint() == prevSegment.translated.base.getSecondPoint()) {
			if (Objects.equals(translatedSegment.translated.base.getFirstPoint(), prevSegment.translated.base.getSecondPoint())) {
//				OverlapPoint overlapPoint = new OverlapPoint(translatedSegment.translated.base.getFirstPoint());
//				overlapPoint.taintWith(Taint.RECONNECT);
//				prevSegment.translated.add(overlapPoint);
//				translatedSegment.translated.add(overlapPoint);

				OverlapPoint op = overlapPointFactory.create(translatedSegment.translated.base.getFirstPoint(), Taint.RECONNECT, prevSegment.translated, translatedSegment.translated);
				op.addObscure2(translatedSegment.translated.base, false, prevSegment.translated.base, true, "reconnect");
			} else {
				boolean isSplit = translatedSegment.headTailCrossPoint != null;
				OverlapPoint op = overlapPointFactory.create(translatedSegment.translated.base.getFirstPoint(), Taint.NONE, translatedSegment.translated);
				op.addObscure2(translatedSegment.translated.base, isSplit, translatedSegment.head.base, !isSplit, "openThis");

//				overlapPoint = new OverlapPoint(prevSegment.translated.base.getSecondPoint());
//				prevSegment.translated.add(overlapPoint);

				if (prevSegment.headTailCrossPoint == null) {
					op = overlapPointFactory.create(prevSegment.translated.base.getSecondPoint(), Taint.NONE, prevSegment.translated, prevSegment.tail);
					op.addObscure2(prevSegment.tail.base, true, prevSegment.translated.base, true, "openPrecS");
				} else {
					op = overlapPointFactory.create(prevSegment.translated.base.getSecondPoint(), Taint.NONE, prevSegment.translated, prevSegment.tail);
					op.addObscure2(prevSegment.translated.base, true, prevSegment.tail.base, true, "openPrecHT");
				}


			}
			
			prevSegment = translatedSegment;
		}
	}
	
	private void obscureInBetween(MutableContour mutableContour) {
		mutableContour.streamSegments().forEach(t -> t.obscureInBetween());
		mutableContour.streamSegments().forEach(t -> t.reduceObscureInfo());
	}

	private void taintObscuredSegmentPoints(MutableContour mutableContour) {
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
			translatedSegment.streamAllOverlapPoints()
				.filter(s -> !s.isTainted())
				.filter(op -> isObscuredOverlapPoint(op, mutableContour, translatedSegment))
				.forEach(op -> op.taintWith(Taint.OBSCURED));
		}
	}

	private boolean isObscuredOverlapPoint(OverlapPoint overlapPoint, MutableContour mutableContour,
			TranslatedSegment translatedSegment2) {
		boolean j = false;
		
		if (overlapPoint.isFullyObscured()) {
			return true;
		}
//		
//		if (overlapPoint.point.equals(Point2D.of(3813, 1362))) {
//			System.err.println("he");
//			j = true;
//		}
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
//			if (j && translatedSegment.isObscuredOverlapPoint(overlapPoint)) {
//				System.err.println("me");
//			}
			if (translatedSegment.isObscuredOverlapPoint(overlapPoint)) {
				return true;
			}
		}
		return false;
	}

	private void produceSegmentPoints(OverlapPointFactory overlapPointFactory, MutableContour mutableContour) {
		
		IntersectionInfo info = new IntersectionInfo();
		
		
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {

			if (translatedSegment.headTailCrossPoint != null) {
				Point2D p = translatedSegment.headTailCrossPoint;
				OverlapPoint op = overlapPointFactory.create(p, Taint.NONE, translatedSegment.head, translatedSegment.tail);
				op.addObscure2(translatedSegment.tail.base, true, translatedSegment.head.base, true, "headTailSplitA");
				op.addObscure2(translatedSegment.tail.base, false, translatedSegment.head.base, false, "headTailSplitB");
			}
			
			for(TranslatedSegment subSegment : mutableContour.segmentIterable()) {
				if (subSegment == translatedSegment) {
					continue;
				}

//				if (subSegment.headTailCrossPoint == null) {
					
					createSegmentSidePoints(overlapPointFactory, translatedSegment, subSegment.head);
					createSegmentSidePoints(overlapPointFactory, translatedSegment, subSegment.tail);
//				} else {
//					createSegmentSidePoints(overlapPointFactory, translatedSegment, subSegment.base0);
//					createSegmentSidePoints(overlapPointFactory, translatedSegment, subSegment.base1);
//					createSegmentSidePoints(overlapPointFactory, translatedSegment, subSegment.translated0);
//					createSegmentSidePoints(overlapPointFactory, translatedSegment, subSegment.translated1);
//				}

				
				
				/* crossing translated lines */
				Point2D crossPoint = translatedSegment.translated.crossPoint(subSegment.translated.base, info);
				if (crossPoint != null) {
					OverlapPoint op = overlapPointFactory.create(crossPoint, Taint.NONE, translatedSegment.translated, subSegment.translated);
					op.addObscure2(translatedSegment.translated.base, false, subSegment.translated.base, true, "crossTrans");
				}

				/* translated line crosses original */
				Point2D badCrossPoint = translatedSegment.translated.crossPoint(subSegment.base.base, info);
				if (info.intersectionPoint != null) {
					OverlapPoint op = overlapPointFactory.create(info.intersectionPoint, Taint.ORIGINAL, translatedSegment.translated, subSegment.base);
					op.addObscure2(subSegment.base.base, true, translatedSegment.translated.base, true, "origA");
				}

				/* translated line crosses original */
				badCrossPoint = translatedSegment.base.crossPoint(subSegment.translated.base, info);
				if (info.intersectionPoint != null) {
					OverlapPoint op = overlapPointFactory.create(info.intersectionPoint, Taint.ORIGINAL, translatedSegment.base, subSegment.translated);
					op.addObscure2(translatedSegment.base.base, true, subSegment.translated.base, true, "origB");
				}
			}
			
		}
	}

	private void createSegmentSidePoints(OverlapPointFactory overlapPointFactory, TranslatedSegment translatedSegment, Route sideRoute) {
		IntersectionInfo info = new IntersectionInfo();
		Point2D crossPointSide = translatedSegment.translated.crossPoint(sideRoute.base, info );
		if (crossPointSide != null) {
			OverlapPoint op = overlapPointFactory.create(crossPointSide, Taint.NONE, translatedSegment.translated, sideRoute);
//			op.addObscure2(sideRoute.base, false, translatedSegment.translated.base, true, "spA");
		} else if (Objects.equals(info.intersectionPoint, sideRoute.base.getSecondPoint())) { // TODO what todo if the intersectionPoint is the wrong side of 'tail'
			overlapPointFactory.create(info.intersectionPoint, Taint.EDGE, translatedSegment.translated, sideRoute);
			// TODO addObscure
		}
		
		
		Point2D crossPointHead = translatedSegment.head.crossPoint(sideRoute.base, info);
		if (crossPointHead != null) {
			OverlapPoint op = overlapPointFactory.create(crossPointHead, Taint.NONE, translatedSegment.head, sideRoute);
//			op.addObscure2(sideRoute.base, false, translatedSegment.head.base, true, "spB");
		}

		Point2D crossPointTail = translatedSegment.tail.crossPoint(sideRoute.base, info);
		if (crossPointTail != null) {
			OverlapPoint op = overlapPointFactory.create(crossPointTail, Taint.NONE, translatedSegment.tail, sideRoute);
//			op.addObscure2(sideRoute.base, false, translatedSegment.tail.base, true, "spC");
		}

	}

}
