package net.github.douwevos.justflat.contour.scaler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.ContourLayerMap;
import net.github.douwevos.justflat.contour.ContourLayerMap.OrderedContour;
import net.github.douwevos.justflat.contour.scaler.OverlapPoint.Taint;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.types.values.Point2D;
import net.github.douwevos.justflat.types.values.StartStop;
import net.github.douwevos.justflat.types.values.Line2D.IntersectionInfo;


public class ContourLayerScaler {

	Log log = Log.instance(true);

	
	public List<MutableContour> allMutableContours = new ArrayList<>();
	
	public ContourLayer scale(ContourLayer input, double thickness, boolean cleanup) {
		ContourLayerMap contourLayerMap = new ContourLayerMap(input);
		contourLayerMap.rebuild();
		
		OrderedContour orderedContour = contourLayerMap.getRootOrderedContour();
		buildIt(orderedContour, thickness);
		
		
		
		log.debug("## scaling "+thickness);
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

	private void buildIt(OrderedContour orderedContour, double thickness) {
		for(OrderedContour main : orderedContour.children) {
			buildMain(main, thickness);
		}
		
	}

	private void buildMain(OrderedContour main, double thickness) {
		MutableContour mainMutableContour = new MutableContour(main.contour, false, -thickness);
		List<ScaledContour> scaledContour = scale(mainMutableContour, thickness);
	}

	

	private List<ScaledContour> scale(MutableContour mutableContour, double thickness) {
		if (mutableContour.isEmpty()) {
			return Collections.emptyList();
		}
		
		boolean isShrinking = thickness>=0d;
		
		OverlapPointFactory overlapPointFactory = new OverlapPointFactory();
		
		produceSegmentPoints(overlapPointFactory, mutableContour, isShrinking);
		
		markConnectingSegementPoints(overlapPointFactory, mutableContour, isShrinking);
		
		obscureInBetween(mutableContour);
		
		taintObscuredSegmentPoints(mutableContour);

		allMutableContours.add(mutableContour);
		
		List<ScaledContour> result = new ArrayList<>();
		
		while(true) {
			OverlapPoint startOverlapPoint = findNextUntaintedAndUnusedOverlapPoint(mutableContour);
			if (startOverlapPoint == null) {
				break;
			}
			ScaledContour scaledContour = pointsToScaledContour(mutableContour, startOverlapPoint);
			log.debug("scaledContour={}", scaledContour);
			log.debug("isShrinking:{}, thickness:{}, reverse:{}", isShrinking, thickness, mutableContour.reverse);
			
			
			if (scaledContour != null) {
				if ((!mutableContour.reverse && boundsDoNotExeed(scaledContour, mutableContour)) 
						|| (mutableContour.reverse && boundsExeed(scaledContour, mutableContour))) {
					result.add(scaledContour);
				} else {
					log.debug("mutableContour.reverse={}", mutableContour.reverse);
					Bounds2D original = mutableContour.source.getBounds();
					log.debug("original:{}", original);
					Bounds2D scaled = scaledContour.getBounds();
					log.debug("scaled  :{}", scaled);
					Bounds2D union = original.union(scaled);
					log.debug("union   :{}", union);
				}
			}
			
			startOverlapPoint.markUsed();
		}
		
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
		
		boolean isShrinking = thickness>=0d;
		
		OverlapPointFactory overlapPointFactory = new OverlapPointFactory();
		
		produceSegmentPoints(overlapPointFactory, mutableContour, isShrinking);
		
		markConnectingSegementPoints(overlapPointFactory, mutableContour, isShrinking);
		
		obscureInBetween(mutableContour);
		
		taintObscuredSegmentPoints(mutableContour);

		allMutableContours.add(mutableContour);
		
		List<ScaledContour> result = new ArrayList<>();
		
		while(true) {
			OverlapPoint startOverlapPoint = findNextUntaintedAndUnusedOverlapPoint(mutableContour);
			if (startOverlapPoint == null) {
				break;
			}
			ScaledContour scaledContour = pointsToScaledContour(mutableContour, startOverlapPoint);
			log.debug("scaledContour={}", scaledContour);
			log.debug("isShrinking:{}, thickness:{}, reverse:{}", isShrinking, thickness, mutableContour.reverse);
			
			
			if (scaledContour != null) {
				if ((!mutableContour.reverse && boundsDoNotExeed(scaledContour, mutableContour)) 
						|| (mutableContour.reverse && boundsExeed(scaledContour, mutableContour))) {
					result.add(scaledContour);
				} else {
					log.debug("mutableContour.reverse={}", mutableContour.reverse);
					Bounds2D original = mutableContour.source.getBounds();
					log.debug("original:{}", original);
					Bounds2D scaled = scaledContour.getBounds();
					log.debug("scaled  :{}", scaled);
					Bounds2D union = original.union(scaled);
					log.debug("union   :{}", union);
				}
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

	private boolean boundsExeed(ScaledContour scaledContour, MutableContour mutableContour) {
		Bounds2D original = mutableContour.source.getBounds();
		Bounds2D scaled = scaledContour.getBounds();
		Bounds2D union = original.union(scaled);
		return union.equals(scaled);
//		
//		return true;
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
				log.debug("marked dirty at:{}", current.point);
				log.debug(buf.toString());
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
			
			log.debug("from: {} .. {}", originating.point, current.point);
		}
		
		
		
		ObscuredInfo info = new ObscuredInfo();
		List<TargetLine> lines = new ArrayList<>();
		OverlapPoint overlapPoint = passed.get(0);
		overlapPoint.markUsed();
		Point2D start = overlapPoint.point;
		info = info.invert(overlapPoint.getObscuredInfo());
		Point2D cpStart = start;
		
		for(int idx=1; idx<passed.size(); idx++) {
			overlapPoint = passed.get(idx);
			info = info.invert(overlapPoint.getObscuredInfo());
			overlapPoint.markUsed();
			Point2D end = overlapPoint.point;
			Point2D cpEnd = end;
			TargetLine targetLine = new TargetLine(cpStart, cpEnd);
			lines.add(targetLine);
			cpStart = cpEnd;
		}

		if (!info.isFullyObscured()) {
			log.debug("not fully obscured info={}", info);
			return null;
		}
		log.debug("lines={}", lines);
		
		return new ScaledContour(mutableContour.getSource(), lines );
	}


	private void markConnectingSegementPoints(OverlapPointFactory overlapPointFactory, MutableContour mutableContour, boolean isShrinking) {
		TranslatedSegment prevSegment = mutableContour.lastSegement();
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
			if (Objects.equals(translatedSegment.translated.base.getFirstPoint(), prevSegment.translated.base.getSecondPoint())) {

				OverlapPoint op = overlapPointFactory.create(translatedSegment.translated.base.getFirstPoint(), Taint.RECONNECT, prevSegment.translated, translatedSegment.translated);
				if (isShrinking) {
					op.addObscure2(translatedSegment.translated.base, false, prevSegment.translated.base, true, "sh-reconnect");
				} else {
					op.addObscure2(prevSegment.translated.base, true, translatedSegment.translated.base, false, "ex-reconnect");
				}
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

				Point2D crossPoint = translatedSegment.translated.crossPoint(prevSegment.translated.base, null);
				if (crossPoint!=null) {
					op = overlapPointFactory.create(crossPoint, Taint.RECONNECT, prevSegment.translated, translatedSegment.translated);
					op.addObscure2(translatedSegment.translated.base, false, prevSegment.translated.base, true, "reconnectCrsPnt");
				} else {
					Point2D fp = translatedSegment.translated.base.getFirstPoint();
					Point2D sp = prevSegment.translated.base.getSecondPoint();
					crossPoint = Point2D.of((fp.x+sp.x)/2, (fp.y+sp.y)/2);
					op = overlapPointFactory.create(crossPoint, Taint.RECONNECT, prevSegment.translated, translatedSegment.translated);
					op.addObscure2(prevSegment.translated.base, true, translatedSegment.translated.base, false, "reconnectMid");
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
		if (overlapPoint.isFullyObscured()) {
			return true;
		}

		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
			if (translatedSegment.isObscuredOverlapPoint(overlapPoint)) {
				return true;
			}
		}
		return false;
	}

	private void produceSegmentPoints(OverlapPointFactory overlapPointFactory, MutableContour mutableContour, boolean isShrinking) {
		
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
//				Point2D badCrossPoint = 
						translatedSegment.translated.crossPoint(subSegment.base.base, info);
				if (info.intersectionPoint != null) {
					OverlapPoint op = overlapPointFactory.create(info.intersectionPoint, Taint.ORIGINAL, translatedSegment.translated, subSegment.base);
					op.addObscure2(subSegment.base.base, true, translatedSegment.translated.base, true, "origA");
				}

				/* translated line crosses original */
//				badCrossPoint = 
						translatedSegment.base.crossPoint(subSegment.translated.base, info);
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
//			OverlapPoint op = 
					overlapPointFactory.create(crossPointSide, Taint.NONE, translatedSegment.translated, sideRoute);
//			op.addObscure2(sideRoute.base, false, translatedSegment.translated.base, true, "spA");
		} else if (Objects.equals(info.intersectionPoint, sideRoute.base.getSecondPoint())) { // TODO what todo if the intersectionPoint is the wrong side of 'tail'
			overlapPointFactory.create(info.intersectionPoint, Taint.EDGE, translatedSegment.translated, sideRoute);
			// TODO addObscure
		}
		
		
		Point2D crossPointHead = translatedSegment.head.crossPoint(sideRoute.base, info);
		if (crossPointHead != null) {
//			OverlapPoint op = 
					overlapPointFactory.create(crossPointHead, Taint.NONE, translatedSegment.head, sideRoute);
//			op.addObscure2(sideRoute.base, false, translatedSegment.head.base, true, "spB");
		}

		Point2D crossPointTail = translatedSegment.tail.crossPoint(sideRoute.base, info);
		if (crossPointTail != null) {
//			OverlapPoint op = 
					overlapPointFactory.create(crossPointTail, Taint.NONE, translatedSegment.tail, sideRoute);
//			op.addObscure2(sideRoute.base, false, translatedSegment.tail.base, true, "spC");
		}

	}

}
