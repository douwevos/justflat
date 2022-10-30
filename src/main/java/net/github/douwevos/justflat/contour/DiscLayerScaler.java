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
		
		
		produceSegmentPoints(mutableContour);
		
		markConnectingSegementPoints(mutableContour);
		
		taintObscuredSegmentPoints(mutableContour);
		
//		taintOvershootingPoints(mutableContour);
		
		
		
		createSegmentParts(mutableContour);
		
		
//		
////		mutableContour.connectTranslatedLines();
//		
//		
//		reconnectAfterTranslate(mutableContour);
//		
//		
//		stripFlipped(mutableContour);
//		
//		markCleanSections(mutableContour);
//
//		calculateAllIntermediateCrossPoints(mutableContour);
//		
////		hideLinesInOppositeDirection(mutableContour);
//
//		markLinesAsFlippedInOppositeDirection(mutableContour);

//		int firstCleanSection = taintTargetLines(mutableContour);
//		taintNewTargetLines(mutableContour);

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
			if (scaledContour != null) {
				result.add(scaledContour);
			}
			
			startOverlapPoint.markUsed();
		}
		
		return result;
	}
	
	
	private void createSegmentParts(MutableContour mutableContour) {
		for(TranslatedSegment segment : mutableContour.segmentIterable()) {
			List<OverlapPoint> overlapPoints = segment.streamOverlapPoints().filter(s -> s.isTainted()).collect(Collectors.toList());
			
			for(int idx=0; idx<overlapPoints.size(); idx+=2) {
				
			}
		}
	}
	
	private OverlapPoint findNextUntaintedAndUnusedOverlapPoint(MutableContour mutableContour) {
		List<OverlapPoint> overlapPoints = mutableContour.streamSegments().flatMap(s -> s.streamOverlapPoints())
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
			for(TranslatedSegment translatedSegment : current.segmentIterable()) {
				translatedSegment.ensureOrdered();
				int indexOf = translatedSegment.indexOf(current);
				
				int s = 0;
				buf.append("   points: ");
				for(OverlapPoint ov : translatedSegment.overlapPointsIterable())  {
					if (s==indexOf) {
						buf.append("["+ov+"]");
					} else {
						buf.append(":"+ov);
					}
					s++;
				}
				buf.append("\n");

				
				
				OverlapPoint left = translatedSegment.overlapPointAt(indexOf-1);
				buf.append("       left="+left+"\n");
				if (left!=null && !left.isTainted() && !Objects.equals(left, originating)) {
					next = left;
					break;
				}
				OverlapPoint right = translatedSegment.overlapPointAt(indexOf+1);
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
		
		
		
		List<TargetLine> lines = new ArrayList<>();
		OverlapPoint overlapPoint = passed.get(0);
		overlapPoint.markUsed();
		Point2D start = overlapPoint.point;
		CrossPoint cpStart = new CrossPoint(start);
		for(int idx=1; idx<passed.size(); idx++) {
			overlapPoint = passed.get(idx);
			overlapPoint.markUsed();
			Point2D end = overlapPoint.point;
			CrossPoint cpEnd = new CrossPoint(end);
			TargetLine targetLine = new TargetLine(null, cpStart, cpEnd);
			lines.add(targetLine);
			cpStart = cpEnd;
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
			
			int validPoints = (int) translatedSegment.streamOverlapPoints().filter(s-> !s.isTainted()).count();
			if (validPoints==0) {
				continue;
			}
			List<OverlapPoint> untaintedPoints = translatedSegment.streamOverlapPoints().filter(s -> !s.isTainted()).collect(Collectors.toList());

			Line2D testBaseLine = translatedSegment.base;
			Line2D testTransLine = translatedSegment.translated;
			
			for(TranslatedSegment other : mutableContour.segmentIterable()) {
				IntersectionInfo info = new IntersectionInfo();
				Line2D baseLine = other.base;
				Point2D crossPoint = baseLine.crossPoint(testTransLine, info);
				
				if (crossPoint==null) {
					continue;
				}
				
				/* the raw 'translated line crosses a 'base' line this might be an overshoot */
				
				int testCCWA = baseLine.relativeCCW(testTransLine.getFirstPoint());
				int testCCWB = baseLine.relativeCCW(testTransLine.getSecondPoint());
				int baseCCWA = baseLine.relativeCCW(testBaseLine.getFirstPoint());
				int baseCCWB = baseLine.relativeCCW(testBaseLine.getSecondPoint());

				if ((baseCCWA<=0d) && (baseCCWB<=0d)) {
					// any test-CCWs larger then 0d are overshoots;
					for(int idx=untaintedPoints.size()-1; idx>=0; idx--) {
						OverlapPoint overlapPoint = untaintedPoints.get(idx);
						int relativeCCW = baseLine.relativeCCW(overlapPoint.point);
						if (relativeCCW>0d) {
							overlapPoint.taintWith(Taint.OVERSHOOT);
							untaintedPoints.remove(idx);
						}
					}
				} else if ((baseCCWA>=0d) && (baseCCWB>=0d)) {
					// any test-CCWs less then 0d are overshoots;
					for(int idx=untaintedPoints.size()-1; idx>=0; idx--) {
						OverlapPoint overlapPoint = untaintedPoints.get(idx);
						int relativeCCW = baseLine.relativeCCW(overlapPoint.point);
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


	private void markConnectingSegementPoints(MutableContour mutableContour) {
		TranslatedSegment prevSegment = mutableContour.lastSegement();
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
			if (translatedSegment.translated.getFirstPoint() == prevSegment.translated.getSecondPoint()) {
				OverlapPoint overlapPoint = new OverlapPoint(translatedSegment.translated.getFirstPoint());
				overlapPoint.taintWith(Taint.RECONNECT);
				prevSegment.add(overlapPoint);
				translatedSegment.add(overlapPoint);
			}
			
			prevSegment = translatedSegment;
		}
		
	}

	private void taintObscuredSegmentPoints(MutableContour mutableContour) {
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
			for(OverlapPoint overlapPoint : translatedSegment.overlapPointsIterable()) {
				if (overlapPoint.isTainted()) {
					continue;
				}
				if (isObscuredOverlapPoint(overlapPoint, mutableContour, translatedSegment)) {
					overlapPoint.taintWith(Taint.OBSCURED);
				}
			}
		}
	}

	private boolean isObscuredOverlapPoint(OverlapPoint overlapPoint, MutableContour mutableContour,
			TranslatedSegment translatedSegment2) {
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
			if (translatedSegment.isObscuredOverlapPoint(overlapPoint)) {
				return true;
			}
		}
		return false;
	}

	private void produceSegmentPoints(MutableContour mutableContour) {
		
		IntersectionInfo info = new IntersectionInfo();
		
		
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
			
			for(TranslatedSegment subSegment : mutableContour.segmentIterable()) {
				if (subSegment == translatedSegment) {
					continue;
				}
				
				Point2D crossPointSide = translatedSegment.translated.crossPoint(subSegment.head, info);
				if (crossPointSide != null) {
					OverlapPoint overlapPoint = new OverlapPoint(crossPointSide);
					overlapPoint.taintWith(Taint.OBSCURED);
					translatedSegment.add(overlapPoint);
				}
				
				crossPointSide = translatedSegment.translated.crossPoint(subSegment.tail, info);
				if (crossPointSide != null) {
					OverlapPoint overlapPoint = new OverlapPoint(crossPointSide);
					overlapPoint.taintWith(Taint.OBSCURED);
					translatedSegment.add(overlapPoint);
				}

				
				
				if (translatedSegment.findByTranslatedSegment(subSegment) == null) {
					Point2D crossPoint = translatedSegment.translated.crossPoint(subSegment.translated, info);
					if (crossPoint != null) {
						OverlapPoint overlapPoint = new OverlapPoint(crossPoint);
						translatedSegment.add(overlapPoint);
						subSegment.add(overlapPoint);
					}

					Point2D badCrossPoint = translatedSegment.translated.crossPoint(subSegment.base, info);
					if (badCrossPoint != null) {
						OverlapPoint overlapPoint = new OverlapPoint(badCrossPoint);
						translatedSegment.add(overlapPoint);
//						subSegment.add(overlapPoint);
						overlapPoint.taintWith(Taint.ORIGINAL);
					}

					badCrossPoint = translatedSegment.base.crossPoint(subSegment.translated, info);
					if (badCrossPoint != null) {
						OverlapPoint overlapPoint = new OverlapPoint(badCrossPoint);
//						translatedSegment.add(overlapPoint);
						subSegment.add(overlapPoint);
						overlapPoint.taintWith(Taint.ORIGINAL);
					}
					
					

				}
			}
			
//			Line translated = translatedSegment.translated;
//			
//			
//			for(TranslatedSegment translatedSegmentTest : mutableContour.segmentIterable()) {
//				translatedSegmentTest.intersectingSegement
//			}
//			List<Point2D> collect = mutableContour.streamSegments().map(ts -> ts.translated).map(o -> translated.crossPoint(o, info)).filter(Objects::nonNull).collect(Collectors.toList());
//			
			
		}
	}
//
//	private void stripFlipped(MutableContour mutableContour) {
//		List<MutableLine> lines = mutableContour.lines;
//		int lineCount = lines.size();
//		for(int idx=0; idx<lineCount; idx++) {
//			MutableLine mutableLine = lines.get(idx);
//			CrossPoint crossPointStart = mutableLine.getCrossPointStart();
//			CrossPoint crossPointEnd = mutableLine.getCrossPointEnd();
//			if (!mutableLine.sameDirection(crossPointStart.crossPoint, crossPointEnd.crossPoint)) {
//				
//				
//				MutableLine prevMl = lines.get((idx+lineCount-1) % lineCount);
//				MutableLine nextMl = lines.get((idx+1) % lineCount);
//				
//				IntersectionInfo info = new IntersectionInfo();
//				Point2D intersectionPoint = prevMl.translated.intersectionPoint(nextMl.translated, info);
//				if (intersectionPoint!=null && info.ua>0d) {
////					CrossPoint crossPoint = new CrossPoint(intersectionPoint);
////					prevMl.setCrossPointEnd(crossPoint);
////					nextMl.setCrossPointStart(crossPoint);
////					
////					
////					mutableLine.setCrossPointStart(crossPoint);
////					mutableLine.setCrossPointEnd(crossPoint);
////					lines.remove(idx);
////					idx--;
////					lineCount--;
//				}
//			}
//		}
//	}
//
//	private void markLinesAsFlippedInOppositeDirection(MutableContour mutableContour) {
//		for (MutableLine mutableLine : mutableContour) {
//			CrossPoint crossPointStart = mutableLine.getCrossPointStart();
//			CrossPoint crossPointEnd = mutableLine.getCrossPointEnd();
//			if (!mutableLine.sameDirection(crossPointStart.crossPoint, crossPointEnd.crossPoint)) {
//				mutableLine.streamTargetLines().forEach(TargetLine::markFlipped);
//			}
//		}
//	}
//
//	private void hideLinesInOppositeDirection(MutableContour mutableContour) {
//		for (MutableLine mutableLine : mutableContour) {
//			CrossPoint crossPointStart = mutableLine.getCrossPointStart();
//			CrossPoint crossPointEnd = mutableLine.getCrossPointEnd();
//			if (!mutableLine.sameDirection(crossPointStart.crossPoint, crossPointEnd.crossPoint)) {
//				mutableLine.streamTargetLines().forEach(TargetLine::markHidden);
//			}
//		}
//		
//		boolean repeat = true;
//		while(repeat) {
//			repeat = false;
//			for (MutableLine mutableLine : mutableContour) {
//				int changeCount = mutableLine.streamTargetLines()
//						.filter(s -> !s.isHidden())
//						.mapToInt(tl -> {
//							if ((tl.pointA().lineCount()<=1) || (tl.pointB().lineCount()<=1)) {
//								tl.markHidden();
//								return 1;
//							}
//							return 0;
//						}).sum();
//				if (changeCount > 0) {
//					repeat = true;
//				}
//			}
//		}
//	}
//
//	private void reconnectAfterTranslate(MutableContour mutableContour) {
//		List<MutableLine> lines = mutableContour.lines;
//		int linesCount = lines.size();
//		IntersectionInfo info = new IntersectionInfo();
//		for(int idx=0; idx<lines.size(); idx++) {
//			MutableLine mutableLineA = lines.get(idx);
//			CrossPoint rightCrossPoint = mutableLineA.getCrossPointEnd();
//			
//			MutableLine mutableLineB = lines.get((idx+1) % linesCount);
//			CrossPoint leftCrossPoint = mutableLineB.getCrossPointStart();
//			
//			if (leftCrossPoint == rightCrossPoint) {
//				continue;
//			}
//			
//			List<TargetLine> targetLinesA = mutableLineA.getTargetLines();
//			if (targetLinesA.isEmpty()) {
//				continue;
//			}
//			TargetLine targetLineA = targetLinesA.get(0);
//
//			List<TargetLine> targetLinesB = mutableLineB.getTargetLines();
//			if (targetLinesB.isEmpty()) {
//				continue;
//			}
//			TargetLine targetLineB = targetLinesB.get(0);
//
//			Line lineA = targetLineA.asLine();
//			Line lineB = targetLineB.asLine();
//			Point2D point = lineA.intersectionPoint(lineB, info);
//			if (point==null) {
//				// parallel lines ?
//				continue;
//			}
//			CrossPoint crossPoint = new CrossPoint(point);
//			
//			mutableLineA.setCrossPointEnd(crossPoint);
//			mutableLineB.setCrossPointStart(crossPoint);
//		}
//	}
//	
//	private ScaledContour rebuiltScaledContourNew(MutableContour mutableContour) {
//		List<MutableLine> lines = mutableContour.lines;
//		List<TargetLine> allTargetLines = lines.stream().flatMap(p -> p.getTargetLines().stream()).collect(Collectors.toList());
//		List<TargetLine> targetLinesOut = new ArrayList<>();
//		
//		TargetLine startTargetLine = mutableContour.streamLines().flatMap(ml -> ml.streamTargetLines()).filter(s -> !s.isTainted() && !s.isUsed()).findAny().orElse(null);
//		log.debug2("startTargetLine={}", startTargetLine);
//		if (startTargetLine == null) {
//			return null;
//		}
//		
//		targetLinesOut.add(startTargetLine);
//		while(true) {
//			TargetLine tlHead = targetLinesOut.get(targetLinesOut.size()-1);
//			Point2D headPoint = tlHead.asLine().getSecondPoint();
//			List<TargetLine> targetLinesThrougPoint = targetLinesThroughPoint(allTargetLines, headPoint);
//			List<TargetLine> targetLinesLeavingPoint = targetLinesThrougPoint.stream()
//				.filter(tl -> !tl.isTainted() && !tl.isUsed())
//				.filter(tl -> tl.asLine().getFirstPoint().equals(headPoint))
//				.collect(Collectors.toList());
//			
//			log.debug2("tlHead={}, headPoint={}, targetLinesLeavingPoint={}", tlHead, headPoint, targetLinesLeavingPoint);
//			
//			if (targetLinesLeavingPoint.size()>1) {
//				targetLinesLeavingPoint = targetLinesLeavingPoint.stream()
//					.filter(tl -> tl.getMutableLine()!=tlHead.getMutableLine())
//					.collect(Collectors.toList());
//
//				if (targetLinesLeavingPoint.size()>1) {
//					System.err.println("multipe targetLinesLeaving point:"+headPoint+" tls:"+targetLinesLeavingPoint);
//				}
//			}
//			if (targetLinesLeavingPoint.isEmpty()) {
//				System.err.println("empty !!");
//				targetLinesLeavingPoint.stream().forEach(tl -> tl.markUsed());
//				return null;
//			}
//			
//			TargetLine targetLine = targetLinesLeavingPoint.get(0);
//			int indexOftargetLine = targetLinesOut.indexOf(targetLine);
//			if (indexOftargetLine==0) {
//				break;
//			} else if (indexOftargetLine>0) {
//				targetLinesOut.subList(0, indexOftargetLine).clear();
//				System.err.println("indexOftargetLine gt 0 : indexOftargetLine="+indexOftargetLine);
//				break;
//			}
//			targetLinesOut.add(targetLine);
//		}
//		
//		targetLinesOut.stream().forEach(s -> s.markUsed());
//
//		return new ScaledContour(mutableContour.source, targetLinesOut);
//	}


//	private int taintNewTargetLines(MutableContour mutableContour) {
//		List<MutableLine> lines = mutableContour.lines;
//		int lineCount = lines.size();
//		for(int idxA=0; idxA<lineCount; idxA++) {
//			MutableLine mlMain = lines.get(idxA);
//			if (mlMain.cleanSection) {
//				continue;
//			}
//
//			for(int idxB=0; idxB<lineCount; idxB++) {
//				MutableLine mlSub = lines.get(idxB);
//				if (mlSub.cleanSection || idxB==idxA) {
//					continue;
//				}
//				
//				taintTargetLines(mlMain, mlSub);
//			}
//		}
//		return 0;
//	}

//	
//	private void taintTargetLines(MutableLine mlMain, MutableLine mlSub) {
//		
//		List<TargetLine> targetLinesMain = mlMain.getTargetLines();
//		for (TargetLine targetLineMain : targetLinesMain) {
//			if (targetLineMain.isTainted() || targetLineMain.isFlipped()) {
//				continue;
//			}
//			Line asLineMain = targetLineMain.asLine();
//			
//			CrossPoint mainCpLeft = mlMain.findCrossPoint(asLineMain.getFirstPoint());
//			CrossPoint mainCpRight = mlMain.findCrossPoint(asLineMain.getSecondPoint());
//			double leftOffset = mlMain.crossPointProjectionOffset(mainCpLeft);
//			double rightOffset = mlMain.crossPointProjectionOffset(mainCpRight);
//			
//			Line projectionMain = mlMain.base.createByOffsets(rightOffset, leftOffset);
//			
//			Line projectionLeft = new Line(projectionMain.getSecondPoint(), asLineMain.getFirstPoint());
//			Line projectionRight = new Line(asLineMain.getSecondPoint(), projectionMain.getFirstPoint());
//
//			
//			List<CrossPoint> subCrossPoints = mlSub.streamTargetLines().filter(s -> !s.isHidden()).flatMap(s -> Stream.of(s.pointA(), s.pointB())).filter(NoRepeats.filter())
//				.collect(Collectors.toList());
//			
//			for(CrossPoint crossPointSub : subCrossPoints) {
//				if (asLineMain.isFirstOrSecond(crossPointSub.crossPoint)) {
//					continue;
//				}
//				Point2D testPoint = crossPointSub.crossPoint;
//				if (asLineMain.relativeCCW(testPoint)<=0 && projectionMain.relativeCCW(testPoint)<=0
//						&& projectionLeft.relativeCCW(testPoint)<=0 && projectionRight.relativeCCW(testPoint)<=0) {
////					targetLineMain.markTainted();
//					crossPointSub.forEach(tl -> {
//						tl.markTainted();
//						System.err.println("tainting: "+tl+" for croispoint:"+crossPointSub+" targetLineMain:"+targetLineMain);
//					});
//					
//				}
//			}
//			
//			
//			mlSub.streamTargetLines().filter(s -> !s.isTainted() && !s.isFlipped()).forEach(tlSub -> {
//				if (tlSub == targetLineMain) {
//					return;
//				}
//				if (tlSub.asLine().hasCrossPointWith(projectionLeft) || tlSub.asLine().hasCrossPointWith(projectionRight)) {
//					tlSub.markTainted();
//				}
//			});
//			
//		}		
//	}

//
//	private List<TargetLine> targetLinesThroughPoint(List<TargetLine> allTargetLines, Point2D point) {
//		return allTargetLines.stream().filter(p -> p.asLine().isFirstOrSecond(point)).collect(Collectors.toList());
//	}

//
//	private void calculateAllIntermediateCrossPoints(MutableContour mutableContour) {
//		List<MutableLine> lines = mutableContour.lines;
//		int lineCount = lines.size();
//		for(int idxA=0; idxA<lineCount; idxA++) {
//			MutableLine mlMain = lines.get(idxA);
//			if (mlMain.cleanSection) {
//				continue;
//			}
//
//			for(int idxB=idxA+1; idxB<lineCount; idxB++) {
//				MutableLine mlSub = lines.get(idxB);
//				if (mlSub.cleanSection) {
//					continue;
//				}
//				
//				calculateAllIntermediateCrossPoints(mlMain, mlSub);
//			}
//		}
//	}

//	private void calculateAllIntermediateCrossPoints(MutableLine mlMain, MutableLine mlSub) {
//		IntersectionInfo info = new IntersectionInfo();
//		List<TargetLine> targetLinesMain = mlMain.getTargetLines();
//
//		List<CrossPoint> mainCuttingPoints = new ArrayList<>();
//		for (TargetLine targetLineMain : targetLinesMain) {
//			Line asLineMain = targetLineMain.asLine();
//			List<CrossPoint> cuttingCrossPoints = mlSub.streamTargetLines()
//				.filter(tl -> targetLineMain!=tl && !asLineMain.sharePoints(tl.asLine()))
//				.map(tl-> asLineMain.crossPoint(tl.asLine(), info))
//				.filter(Objects::nonNull)
//				.map(p -> new CrossPoint(p))
//				.collect(Collectors.toList());
//			
//			cuttingCrossPoints.forEach(crossPoint -> {
//				mlSub.cutAt(crossPoint);
//			});
//			mainCuttingPoints.addAll(cuttingCrossPoints);
//		}
//		
//		mainCuttingPoints.forEach(crossPoint -> {
//			mlMain.cutAt(crossPoint);
//		});
//	}

//	private void markCleanSections(MutableContour mutableContour) {
//		IntersectionInfo info = new IntersectionInfo();
//		int lineCount = mutableContour.lines.size();
//		for(int idx=0; idx<lineCount; idx++) {
//			MutableLine mutableLine = mutableContour.lines.get(idx);
//			Line base = mutableLine.base;
//			List<TargetLine> targetLines = mutableLine.getTargetLines();
//			if (targetLines.isEmpty()) {
//				continue;
//			}
//			Line targetLine = targetLines.get(0).asLine();
//			
//			Line left = new Line(base.getFirstPoint(), targetLine.getFirstPoint());
//			Line right = new Line(base.getSecondPoint(), targetLine.getSecondPoint());
//			
//			Point2D crossPoint = left.crossPoint(right, info);
//			if (crossPoint != null) {
//				continue;
//			}
//			
//			mutableLine.cleanSection = true;
//
//			for(int testidx=0; testidx<lineCount; testidx++) {
//				if (testidx == idx) {
//					continue;
//				}
//				MutableLine testMutableLine = mutableContour.lines.get(testidx);
//				List<TargetLine> testTargetLines = testMutableLine.getTargetLines();
//				if (testTargetLines.isEmpty()) {
//					continue;
//				}
//				Line testTargetLine = testTargetLines.get(0).asLine();
//
//				
//				if (testTargetLine.hasCrossPointWith(base) || testTargetLine.hasCrossPointWith(targetLine)
//						|| testTargetLine.hasCrossPointWith(left) || testTargetLine.hasCrossPointWith(right)) {
//					mutableLine.cleanSection = false;
//					break;
//				}
//
//				testTargetLine = testMutableLine.base;
//
//				if (testTargetLine.hasCrossPointWith(base) || testTargetLine.hasCrossPointWith(targetLine)
//						|| testTargetLine.hasCrossPointWith(left) || testTargetLine.hasCrossPointWith(right)) {
//					mutableLine.cleanSection = false;
//					break;
//				}
//
//			}
//		}
//		
//	}
}
