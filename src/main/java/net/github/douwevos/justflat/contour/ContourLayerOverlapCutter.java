package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.github.douwevos.justflat.contour.scaler.OverlapPoint;
import net.github.douwevos.justflat.contour.scaler.OverlapPoint.Taint;
import net.github.douwevos.justflat.contour.scaler.OverlapPointFactory;
import net.github.douwevos.justflat.contour.scaler.Route;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.LineRelation;
import net.github.douwevos.justflat.types.values.Point2D;

public class ContourLayerOverlapCutter {

	Log log = Log.instance(false);
	
	public ContourLayer scale(ContourLayer input, boolean cleanup) {
		
		OverlapPointFactory overlapPointFactory = new OverlapPointFactory();
		
		List<Line2D> enlistedLines = enlistAsLines(input);
		log.debug("directedLines created line.count="+enlistedLines.size());
		List<Route> crossLines = createCrossLines(overlapPointFactory, enlistedLines);
		log.debug("CrossableLines created "+crossLines.size());
		List<Line2D> targetLines = createCutLines(crossLines);
		log.debug("TargetLines created");
		
		ContourLayer result = new ContourLayer(input.getWidth(), input.getHeight());
		
		for(int idx=0; idx<500 && !targetLines.isEmpty(); idx++) {
			Contour contour = extractContour(targetLines);
			if (contour!=null) {
				result.add(contour);
			} else {
				break;
			}
		}
		
		log.debug("finished");
		return result;
	}
	
	
	private List<Line2D> enlistAsLines(ContourLayer layer) {
		List<Line2D> result = new ArrayList<>();
		for(Contour contour : layer) {
			if (contour.getBounds()==null) {
				continue;
			}
			List<Line2D> contourLines = contour.createLines(true);
			result.addAll(contourLines);
		}
		return result;
		
	}

	private List<Line2D> createCutLines(List<Route> crossLines) {
		List<Line2D> result = new ArrayList<Line2D>();
		for (Route route : crossLines) {

			Line2D directedLine = route.base;
			if (route.overlapPointCount()<3) {
				Line2D target = new Line2D(directedLine.pointA(), directedLine.pointB());
				Line2D targetLine = target;
				log.debug("target: "+target.pointA() + " targetLine="+targetLine);
				result.add(targetLine);
			} else {
				route.ensureOrdered();
				int overlapPointCount = route.overlapPointCount();
				for(int idx=0; idx<overlapPointCount-1; idx++) {
					OverlapPoint opA = route.overlapPointAt(idx);
					OverlapPoint opB = route.overlapPointAt((idx+1) % overlapPointCount);
					Line2D targetLine = new Line2D(opA.point, opB.point);
					result.add(targetLine);
					log.debug("targetLine="+targetLine);
				}
			}
		}
		return result;
	}

	private Contour extractContour(List<Line2D> targetLines) {
		Point2D mostLeftPoint = null;
		for(Line2D line : targetLines) {
			Line2D baseLine = line;
			mostLeftPoint = getMostLeftPoint(mostLeftPoint, baseLine.pointA());
			mostLeftPoint = getMostLeftPoint(mostLeftPoint, baseLine.pointB());
		}
		
		if (mostLeftPoint == null) {
			return null;
		}

		List<Line2D> linesTroughPoint = collectLinesThroughPoint(targetLines, mostLeftPoint);
		log.debug("mostLeft={}, cp.count={}", mostLeftPoint, linesTroughPoint==null ? 0 : linesTroughPoint.size());
		
		if (linesTroughPoint.isEmpty()) {
			return null;
		}
		
		Line2D startLine = findBestStartLine(linesTroughPoint, mostLeftPoint);
		log.debug("startLine={}", startLine);
		
		List<Line2D> passed = new ArrayList<>();
		
		
		Contour result = new Contour();
		result.add(mostLeftPoint);
		Point2D departurePoint = startLine.getOtherPoint(mostLeftPoint);
		Line2D lastLine = startLine;
		passed.add(lastLine);
		
		
		while(true) {
			Line2D nextLine = continueContour(targetLines, lastLine, departurePoint);
			
			log.debug("nextLine={}", nextLine);
			if (nextLine == null) {
				log.error("unfinished contour");
				return null;
			}
			if (passed.contains(nextLine)) {
				if (nextLine == startLine) {
					log.debug("Contour needly closed");
				} else {
					log.error("Non clean contour");
				}
				break;
			}
			passed.add(nextLine);
			result.add(departurePoint);
			
			
			lastLine = nextLine;
			Point2D nextPoint = nextLine.getOtherPoint(departurePoint);
			departurePoint = nextPoint;
		}
		
		targetLines.removeAll(passed);
		
		return result;
	}
	
	private Line2D continueContour(List<Line2D> crossLines, Line2D lastLine,
			Point2D departurePoint) {
		List<Line2D> linesThroughPoint = collectLinesThroughPoint(crossLines, departurePoint);
		if (linesThroughPoint==null) {
			return null;
		}
		if (linesThroughPoint.size()>2) {
			log.debug("departurePoint={}, cp.count={}", departurePoint, linesThroughPoint.size());
		}
		
		double lastAlpha = lastLine.getAlpha();
		log.debug("--------------------------------");
		log.debug("alpha={}, cosAlpha={}", lastAlpha, lastLine.getCosAlpha());
		if (!lastLine.getFirstPoint().equals(departurePoint)) {
			lastAlpha = (180d+lastAlpha)%360d;
		}
		log.debug("departure: {}, alpha={}", lastLine, lastAlpha);
		
		double bestDiffAlpha = 0d;
		Line2D bestLine = null; 
		
		for (Line2D crossableLine : linesThroughPoint) {
			if (crossableLine==lastLine) {
				continue;
			}

			double testAlpha = crossableLine.getAlpha();
			if (!crossableLine.getFirstPoint().equals(departurePoint)) {
				testAlpha = (180d+testAlpha)%360d;
			}

			double diffAlpha = testAlpha-lastAlpha;
			if (diffAlpha<0d) {
				diffAlpha +=360d;
			}

			log.debug("test: {}, alpha={}, diffAlpha={}", crossableLine, testAlpha, diffAlpha);

			if (bestLine==null || diffAlpha<bestDiffAlpha) {
				bestLine = crossableLine;
				bestDiffAlpha = diffAlpha;
			}
		}		
		
		return bestLine;
	}


	private Line2D findBestStartLine(List<Line2D> linesTroughPoint, Point2D mostLeftPoint) {
		Line2D bestLine = null;
		double bestCosAlpha = 0d;
		for (Line2D crossableLine : linesTroughPoint) {
			Line2D baseLine = crossableLine;
			double cosAlpha = baseLine.getAlpha();
			log.debug("baseLine={}, cosAlpha={}", baseLine, cosAlpha);
			if (!baseLine.pointA().equals(mostLeftPoint)) {
				cosAlpha = (cosAlpha+180d)%360d;
			}
			cosAlpha = 360d+cosAlpha-90d;
			cosAlpha = cosAlpha % 360d;
//			if (cosAlpha<180d) {
//				cosAlpha += 360d;
//			}
			log.debug("baseLine={}, cosAlpha={}", baseLine, cosAlpha);
			if (bestLine==null || bestCosAlpha<cosAlpha) {
				bestLine = crossableLine;
				bestCosAlpha = cosAlpha;
			}
		}
		return bestLine;
	}

	
	private List<Line2D> collectLinesThroughPoint(List<Line2D> targetLines, Point2D point) {
		return targetLines.stream().filter(s -> s.pointA().equals(point) || s.pointB().equals(point)).collect(Collectors.toList());
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


	private List<Route> createCrossLines(OverlapPointFactory overlapPointFactory, List<Line2D> lines) {
		List<Route> cutLines = lines.stream().map(l -> new Route(l)).collect(Collectors.toList());
		
		for(int idxA=0; idxA<cutLines.size(); idxA++) {
			Route cutLineA = cutLines.get(idxA);
			overlapPointFactory.create(cutLineA.base.pointA(), Taint.NONE, cutLineA);
			overlapPointFactory.create(cutLineA.base.pointB(), Taint.NONE, cutLineA);
			
			Line2D lineA = cutLineA.base;
			for(int idxB=idxA+1; idxB<cutLines.size(); idxB++) {
				Route cutLineB = cutLines.get(idxB);
				Line2D lineB = cutLineB.base;

				LineRelation lineRelation = new LineRelation(lineA, lineB);
				
				if (lineA.farAwayTest(lineB)) {
					continue;
				}
				
				if (lineRelation.sharePoints) {
					continue;
				}

				Point2D intersectionPoint = lineRelation.getIntersectionPoint();
				if (intersectionPoint!=null) {
					overlapPointFactory.create(intersectionPoint, Taint.NONE, cutLineA, cutLineB);
				}
			}
		}
		return cutLines;
	}
	
}
