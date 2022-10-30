package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.github.douwevos.justflat.contour.testui.DirectedLine;
import net.github.douwevos.justflat.contour.testui.DirectedLines;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Line2D.IntersectionInfo;
import net.github.douwevos.justflat.types.Point2D;

public class DiscLayerOverlapCutter2 {

	Log log = Log.instance();

	boolean doDebug = false;

	
	public ContourLayer scale(ContourLayer input, boolean cleanup) {
		ContourLayer discLayer = new ContourLayer(input.getWidth(), input.getHeight());
		
		// create a contour list we can safely modify
		for(Contour contour : input) {
			log.debug("contour="+contour);
			if (contour.getBounds()!=null) {
				discLayer.add(contour);
			}
		}
		log.debug("disclayer cleaned up : contour.count="+discLayer.count());
		
		DirectedLines directedLines = new DirectedLines(discLayer);
		log.debug("directedLines created line.count="+directedLines.lineCount());
		List<CrossableLine> crossLines = createCrossLines(directedLines);
		log.debug("CrossableLines created");
		
		List<TargetLine> targetLines = createCutLines(crossLines);
		
		log.debug("TargetLines created");
		
		
		ContourLayer result = new ContourLayer(input.getWidth(), input.getHeight());
		
		for(int idx=0; idx<127 && !targetLines.isEmpty(); idx++) {
			Contour contour = extractContour2(targetLines);
			if (contour!=null) {
				result.add(contour);
			} else {
				break;
			}
		}
		
		
//		Map<Point2D, List<TargetLine>> departureMap = createDepartureMap(crossLines);
//		log.debug("departureMap created");
//		for(int idx=0; idx<10; idx++) {
//			Contour contour = extractContour(departureMap);
//			if (contour == null) {
//				break;
//			}
//			result.add(contour);
//		}
		
		log.debug("finished");
		
		
		// create a new DiscLayer with the contourList holding all (merged) contours
		return result;
	}

	private List<TargetLine> createCutLines(List<CrossableLine> crossLines) {
		List<TargetLine> result = new ArrayList<TargetLine>();
		for (CrossableLine crossableLine : crossLines) {

			DirectedLine directedLine = crossableLine.directedLine;
			if ((crossableLine.crossPoints == null) || crossableLine.crossPoints.isEmpty()) {
				Line2D target;
				if (directedLine.reverse) {
					target = new Line2D(directedLine.baseLine.pointB(), directedLine.baseLine.pointA());
				} else {
					target = new Line2D(directedLine.baseLine.pointA(), directedLine.baseLine.pointB());
				}
				TargetLine targetLine = new TargetLine(directedLine, target);
				log.debug("target: "+target.pointA() + " targetLine="+targetLine);
				result.add(targetLine);
			} else {
				
				Line2D baseLine = crossableLine.directedLine.baseLine;
				boolean reverse = crossableLine.directedLine.reverse;
				Point2D startPoint = reverse ? baseLine.pointB() : baseLine.pointA();
				
				List<CrossPoint> crossPoints = new ArrayList<>(crossableLine.crossPoints);
				crossPoints.add(new CrossPoint(baseLine.pointA()));
				crossPoints.add(new CrossPoint(baseLine.pointB()));
				Collections.sort(crossPoints, (a,b) -> Long.compare(a.crossPoint.squaredDistance(startPoint), b.crossPoint.squaredDistance(startPoint)));

				
				
				Point2D pointA = null;
				
				for(CrossPoint cp : crossPoints) {
					Point2D pointB = cp.crossPoint;
					
					if (pointA!=null && !pointA.equals(pointB)) {
						Line2D target;
						if (reverse) {
							target = new Line2D(pointB, pointA);
						} else {
							target = new Line2D(pointA, pointB);
						}
						TargetLine targetLine = new TargetLine(directedLine, target);
						result.add(targetLine);
	
						log.debug("target: "+target.pointA() + " at cp:"+cp+" targetLine="+targetLine);
					}
					
					pointA = pointB;
					
					reverse = !reverse;
				}
			}
		}
		return result;
	}

	private Contour extractContour2(List<TargetLine> targetLines) {
		Point2D mostLeftPoint = null;
		for(TargetLine line : targetLines) {
			Line2D baseLine = line.targetLine;
			mostLeftPoint = getMostLeftPoint(mostLeftPoint, baseLine.pointA());
			mostLeftPoint = getMostLeftPoint(mostLeftPoint, baseLine.pointB());
		}
		
		
		if (mostLeftPoint == null) {
			return null;
		}

		List<TargetLine> linesTroughPoint = collectLinesThroughPoint(targetLines, mostLeftPoint);
		log.debug("mostLeft={}, cp.count={}", mostLeftPoint, linesTroughPoint==null ? 0 : linesTroughPoint.size());
		
		if (linesTroughPoint.isEmpty()) {
			return null;
		}
		
		TargetLine startLine = findBestStartLine(linesTroughPoint, mostLeftPoint);
		log.debug("startLine={}", startLine);
		
		List<TargetLine> passed = new ArrayList<>();
		
		
		Contour result = new Contour();
		result.add(mostLeftPoint);
		Point2D departurePoint = startLine.targetLine.getOtherPoint(mostLeftPoint);
		TargetLine lastLine = startLine;
		passed.add(lastLine);
		
		
		while(true) {
			boolean shouldDebug = isDebugPoint(departurePoint);

			TargetLine nextLine = continueContour(targetLines, lastLine, departurePoint);
			
			if (shouldDebug) {
				log.debug("nextLine={}", nextLine);
			}
			if (nextLine == null) {
				System.err.println("unfinished contour");
				if (!doDebug) {
					doDebug = true;
					extractContour2(targetLines);
				}
				
				return null;
			}
			if (passed.contains(nextLine)) {
				if (nextLine == startLine) {
					log.debug("Contour needly closed");
				} else {
					System.err.println("Non clean contour");
				}
				break;
			}
			passed.add(nextLine);
			result.add(departurePoint);
			
			
			lastLine = nextLine;
			Point2D nextPoint = nextLine.targetLine.getOtherPoint(departurePoint);
			departurePoint = nextPoint;
		}
		
		targetLines.removeAll(passed);
		
		return result;
	}
	
	public Contour produceCountour(List<TargetLine> targetLines, TargetLine startLine, Point2D mostLeftPoint, List<TargetLine> passed, boolean right) {
		
		Contour result = new Contour();
		result.add(mostLeftPoint);
		Point2D departurePoint = startLine.targetLine.getOtherPoint(mostLeftPoint);
		TargetLine lastLine = startLine;
		passed.add(lastLine);
		
		
		while(true) {
			boolean shouldDebug = isDebugPoint(departurePoint);

			TargetLine nextLine = continueContour(targetLines, lastLine, departurePoint);
			
			if (shouldDebug) {
				log.debug("nextLine={}", nextLine);
			}
			if (nextLine == null) {
				System.err.println("unfinished contour");
				return null;
			}
			if (passed.contains(nextLine)) {
				System.err.println("should check if it is the startLine");
				break;
			}
			passed.add(nextLine);
			result.add(departurePoint);
			
			
			lastLine = nextLine;
			Point2D nextPoint = nextLine.targetLine.getOtherPoint(departurePoint);
			departurePoint = nextPoint;
		}
		return result;
				
	}

	private boolean isDebugPoint(Point2D departurePoint) {
		return doDebug || departurePoint.equals(22980, 26947) || departurePoint.equals(23691, 28130) || departurePoint.equals(32344, 21471);
	}

	private TargetLine continueContour(List<TargetLine> crossLines, TargetLine lastLine,
			Point2D departurePoint) {
		List<TargetLine> linesThroughPoint = collectLinesThroughPoint(crossLines, departurePoint);
		if (linesThroughPoint==null) {
			return null;
		}
		boolean shouldDebug = isDebugPoint(departurePoint);
		if (linesThroughPoint.size()>2) {
			log.debug("departurePoint={}, cp.count={}", departurePoint, linesThroughPoint.size());
		}
		
		double lastAlpha = lastLine.targetLine.getAlpha();
		if (shouldDebug) {
			log.debug("--------------------------------");
			log.debug("alpha={}, cosAlpha={}", lastAlpha, lastLine.targetLine.getCosAlpha());
		}
		if (!lastLine.targetLine.getFirstPoint().equals(departurePoint)) {
			lastAlpha = (180d+lastAlpha)%360d;
		}
		if (shouldDebug) {
			log.debug("departure: {}, alpha={}", lastLine, lastAlpha);
		}
		
		double bestDiffAlpha = 0d;
		TargetLine bestLine = null; 
		
		for (TargetLine crossableLine : linesThroughPoint) {
			if (crossableLine==lastLine) {
				continue;
			}

			double testAlpha = crossableLine.targetLine.getAlpha();
			if (!crossableLine.targetLine.getFirstPoint().equals(departurePoint)) {
				testAlpha = (180d+testAlpha)%360d;
			}

			double diffAlpha = testAlpha-lastAlpha;
			if (diffAlpha<0d) {
				diffAlpha +=360d;
			}

			if (shouldDebug) {
				log.debug("test: {}, alpha={}, diffAlpha={}", crossableLine, testAlpha, diffAlpha);
			}

			if (bestLine==null || diffAlpha<bestDiffAlpha) {
				bestLine = crossableLine;
				bestDiffAlpha = diffAlpha;
			}
		}		
		
		return bestLine;
	}

	private TargetLine findBestStartLine2(List<TargetLine> linesTroughPoint, Point2D mostLeftPoint) {
		TargetLine bestLine = null;
		long bestDeltaY=0;
		long bestDeltaX=0;
		for (TargetLine crossableLine : linesTroughPoint) {
			Line2D baseLine = crossableLine.targetLine;
			Point2D otherPoint = baseLine.getOtherPoint(mostLeftPoint);
			
			long deltaY = otherPoint.y - mostLeftPoint.y;
			long deltaX = otherPoint.x - mostLeftPoint.x;
			if (bestLine==null || deltaY>bestDeltaY || (deltaY==bestDeltaY && deltaX<bestDeltaX)) {
				bestLine = crossableLine;
				bestDeltaX = deltaX;
				bestDeltaY = deltaY;
			}
		}
		return bestLine;
	}

	private TargetLine findBestStartLine(List<TargetLine> linesTroughPoint, Point2D mostLeftPoint) {
		TargetLine bestLine = null;
		double bestCosAlpha = 0d;
		boolean doDebug = (isDebugPoint(mostLeftPoint));
		doDebug = true;
		for (TargetLine crossableLine : linesTroughPoint) {
			Line2D baseLine = crossableLine.targetLine;
			double cosAlpha = baseLine.getAlpha();
			if (doDebug) {
				log.debug("baseLine={}, cosAlpha={}", baseLine, cosAlpha);
			}
			if (!baseLine.pointA().equals(mostLeftPoint)) {
				cosAlpha = (cosAlpha+180d)%360d;
			}
			cosAlpha = 360d+cosAlpha-90d;
			cosAlpha = cosAlpha % 360d;
//			if (cosAlpha<180d) {
//				cosAlpha += 360d;
//			}
			if (doDebug) {
				log.debug("baseLine={}, cosAlpha={}", baseLine, cosAlpha);
			}
			if (bestLine==null || bestCosAlpha<cosAlpha) {
				bestLine = crossableLine;
				bestCosAlpha = cosAlpha;
			}
		}
		return bestLine;
	}

	
	private List<TargetLine> collectLinesThroughPoint(List<TargetLine> targetLines, Point2D point) {
		return targetLines.stream().filter(s -> s.targetLine.pointA().equals(point) || s.targetLine.pointB().equals(point)).collect(Collectors.toList());
	}

	private Point2D getMostLeftPoint(Point2D pointThusFar, Point2D testPoint) {
		if (pointThusFar == null) {
			return testPoint;
		}
		if (pointThusFar.x<testPoint.x) {
			return pointThusFar;
		}
		if (pointThusFar.x>testPoint.x) {
			return testPoint;
		}
		return testPoint.y>pointThusFar.y ? testPoint : pointThusFar;
	}

	private Contour extractContour(Map<Point2D, List<TargetLine>> departureMap) {
		Iterator<Entry<Point2D, List<TargetLine>>> iterator = departureMap.entrySet().iterator();
		if (!iterator.hasNext()) {
			return null;
		}
		Entry<Point2D, List<TargetLine>> startEntry = iterator.next();
		List<TargetLine> targetLineList = startEntry.getValue();
		TargetLine targetLine = targetLineList.get(0);
		List<TargetLine> contourLines = new ArrayList<>();
		contourLines.add(targetLine);
		while(true) {
			Point2D nextStart = targetLine.targetLine.pointB();
			log.debug("contour-adding:"+nextStart);
			targetLineList = departureMap.get(nextStart);
			if (targetLineList == null) {
				log.debug("no next target-line-list "+nextStart);
				break;
			}
			
			TargetLine nextTargetLine = null;
			if (targetLineList.size()==1) {
				nextTargetLine = targetLineList.get(0);
			} else {
				double departingAlpha = targetLine.targetLine.getAlpha();
				nextTargetLine = null;
				double bestAngle=0d;
				for(TargetLine l : targetLineList) {
					double testAlpha = l.targetLine.getAlpha();
					double testAngle = testAlpha-departingAlpha;
					if (testAngle<0) {
						testAngle += 360;
					}
					
					if (nextTargetLine==null || testAngle<bestAngle) {
						nextTargetLine = l;
						bestAngle = testAngle;
					}

				}
				
			}
			
			
			if (nextTargetLine == null) {
				log.debug("no next target-line "+nextStart);
				break;
			}
			
			int idxa = contourLines.indexOf(nextTargetLine);
			if (idxa==0) {
				Contour result = new Contour();
				for(TargetLine tline : contourLines) {
					result.add(tline.targetLine.pointA());
					List<TargetLine> list = departureMap.get(tline.targetLine.pointA());
					list.remove(tline);
					if (list.isEmpty()) {
						departureMap.remove(tline.targetLine.pointA());
					}
				}
				return result;
			} else if (idxa>0) {
				log.debug("weird looping at "+nextStart);
				break;
			}
			
			
			targetLine = nextTargetLine;
			contourLines.add(targetLine);
		}
		
		return null;
	}

	private Map<Point2D, List<TargetLine>> createDepartureMap(List<CrossableLine> crossLines) {
		Map<Point2D, List<TargetLine>> targetMap = new HashMap<>();
		
		for(CrossableLine crossableLine : crossLines) {
			DirectedLine directedLine = crossableLine.directedLine;
			if (crossableLine.crossPoints == null) {
				Line2D target;
				if (directedLine.reverse) {
					target = new Line2D(directedLine.baseLine.pointB(), directedLine.baseLine.pointA());
				} else {
					target = new Line2D(directedLine.baseLine.pointA(), directedLine.baseLine.pointB());
				}
				TargetLine targetLine = new TargetLine(directedLine, target);
				List<TargetLine> targetLines = targetMap.computeIfAbsent(target.pointA(), (p) -> new ArrayList<>());
				log.debug("target: "+target.pointA() + " targetLine="+targetLine);
				targetLines.add(targetLine);
			} else {
				
				Line2D baseLine = crossableLine.directedLine.baseLine;
				boolean reverse = crossableLine.directedLine.reverse;
				Point2D startPoint = reverse ? baseLine.pointB() : baseLine.pointA();
				
				List<CrossPoint> crossPoints = new ArrayList<>(crossableLine.crossPoints);
				Collections.sort(crossPoints, (a,b) -> Long.compare(a.crossPoint.squaredDistance(startPoint), b.crossPoint.squaredDistance(startPoint)));

				
				crossPoints.add(new CrossPoint(reverse ? baseLine.pointA() : baseLine.pointB()));
				
				Point2D pointA = startPoint;
				
				for(CrossPoint cp : crossPoints) {
					Point2D pointB = cp.crossPoint;
					
					Line2D target;
					if (reverse) {
						target = new Line2D(pointB, pointA);
					} else {
						target = new Line2D(pointA, pointB);
					}
					TargetLine targetLine = new TargetLine(directedLine, target);
					List<TargetLine> targetLines = targetMap.computeIfAbsent(target.pointA(), (p) -> new ArrayList<>());
					targetLines.add(targetLine);

					log.debug("target: "+target.pointA() + " at cp:"+cp+" targetLine="+targetLine);
					
					pointA = pointB;
					
					reverse = !reverse;
				}
				
				
				
			}
		}
		return targetMap;
	}

	private List<CrossableLine> createCrossLines(DirectedLines directedLines) {
		List<CrossableLine> cutLines = directedLines.stream().map(dl -> new CrossableLine(dl)).collect(Collectors.toList());
		
		IntersectionInfo info = new IntersectionInfo();
		IntersectionInfo info2 = new IntersectionInfo();
		
		for(int idxA=0; idxA<cutLines.size(); idxA++) {
			CrossableLine cutLineA = cutLines.get(idxA);
			Line2D lineA = cutLineA.directedLine.baseLine;
			for(int idxB=idxA+1; idxB<cutLines.size(); idxB++) {
				CrossableLine cutLineB = cutLines.get(idxB);
				Line2D lineB = cutLineB.directedLine.baseLine;

				if (lineA.farAwayTest(lineB)) {
					continue;
				}

				if (lineA.sharePoints(lineB)) {
//					CrossPoint crossPoint;
//					if (lineA.pointA.equals(lineB.pointA) || lineA.pointA.equals(lineB.pointB)) {
//						crossPoint = new CrossPoint(lineA.pointA);
//					} else {
//						crossPoint = new CrossPoint(lineA.pointB);
//					}
//					crossPoint.add(cutLineA);
//					crossPoint.add(cutLineB);
					continue;
				}
				

				Point2D intersectionPoint = lineA.intersectionPoint(lineB, info);
				
				if (intersectionPoint!=null && info.ua>0.0d && info.ua<1.0d) {
					Point2D intersectionPoint2 = lineB.intersectionPoint(lineA, info2);
					if (intersectionPoint2!=null &&  info2.ua>0.0d && info2.ua<1.0d) {
						CrossPoint crossPoint = new CrossPoint(intersectionPoint2);
						crossPoint.add(cutLineA);
						crossPoint.add(cutLineB);
					}
				}
			}
		}
		return cutLines;
	}
	
	
	static class CrossPoint {
		
		public final Point2D crossPoint;
		private final List<CrossableLine> lines = new ArrayList<>();
		
		public CrossPoint(Point2D crossPoint) {
			this.crossPoint = crossPoint;
		}

		public void add(CrossableLine line) {
			lines.add(line);
			line.add(this);
		}
		
		@Override
		public String toString() {
			return "CP["+crossPoint+"]";
		}
		
	}
	
	
	static class CrossableLine {
		
		public final DirectedLine directedLine;
		
		public List<CrossPoint> crossPoints;
		
		public CrossableLine(DirectedLine directedLine) {
			this.directedLine = directedLine;
		}

		public void add(CrossPoint crossPoint) {
			if (crossPoints==null) {
				crossPoints = new ArrayList<>();				
			}
			crossPoints.add(crossPoint);
		}
		
		@Override
		public String toString() {
			return "CrossableLine[line="+directedLine+", CPs="+crossPoints+"]";
		}
	}
	
	static class TargetLine {
		
		public final DirectedLine pLine;
		public final Line2D targetLine;
		
		public TargetLine(DirectedLine directedLine, Line2D targetLine) {
			this.pLine = directedLine;
			this.targetLine = targetLine;
		}
		
		
		@Override
		public String toString() {
			return "TL[line="+targetLine+", directed="+pLine+"]";
		}
		
	}
	
}
