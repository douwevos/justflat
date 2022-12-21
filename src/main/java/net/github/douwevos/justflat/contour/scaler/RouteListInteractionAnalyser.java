package net.github.douwevos.justflat.contour.scaler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.scaler.OverlapPoint.Taint;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.LineRelation;
import net.github.douwevos.justflat.types.values.Point2D;
import net.github.douwevos.justflat.types.values.LineRelation.InLineTyp;

public class RouteListInteractionAnalyser {

	public final RouteList routeListA;
	public final RouteList routeListB;
	
	public List<Route> allRoutes;

	public List<OverlapPoint> overlapPoints;

	OverlapPointFactory overlapPointFactory = new OverlapPointFactory();

	public RouteListInteractionAnalyser(RouteList routeListA, RouteList routeListB) {
		this.routeListA = routeListA.duplicate();
		this.routeListB = routeListB.duplicate();
	}

	public RouteList rebuildRoutList() {

		this.overlapPoints = enlistOutline(overlapPointFactory);

		List<Route> routes = new ArrayList<Route>();
		int opCount = overlapPoints.size();
		for (int idx = 0; idx < opCount; idx++) {
			OverlapPoint opA = overlapPoints.get(idx);
			OverlapPoint opB = overlapPoints.get((idx + 1) % opCount);
			Route route = new Route(new Line2D(opA.point, opB.point));
			routes.add(route);
		}
		return new RouteList(routes);
	}

	public boolean overlap() {
		List<Route> routes = new ArrayList<>();
		routes.addAll(routeListA.routes);
		routes.addAll(routeListB.routes);

		
		for (int mainIdx = 0; mainIdx < routes.size(); mainIdx++) {
			Route routeMain = routes.get(mainIdx);
			Point2D pointA = routeMain.base.pointA();
			Point2D pointB = routeMain.base.pointB();
			overlapPointFactory.create(pointA, Taint.ORIGINAL, routeMain);
			overlapPointFactory.create(pointB, Taint.ORIGINAL, routeMain);
		}
		
		boolean hasOverlap = false;
		for (int mainIdx = 0; mainIdx < routes.size(); mainIdx++) {
			Route routeMain = routes.get(mainIdx);
			for (int subIdx = mainIdx + 1; subIdx < routes.size(); subIdx++) {
				Route routeSub = routes.get(subIdx);
				LineRelation lineRelation = new LineRelation(routeMain.base, routeSub.base);
				Point2D crossPoint = lineRelation.getIntersectionPoint();
				if (crossPoint != null && !lineRelation.sharePoints) {
					overlapPointFactory.create(crossPoint, Taint.NONE, routeMain, routeSub);
					hasOverlap = true;
				} else if ((lineRelation.inLineTyp == InLineTyp.IN_LINE_OVERLAP)
						|| (lineRelation.inLineTyp == InLineTyp.IN_LINE_SHARED_POINT)) {
					routeMain = mergeRoutes(routeMain, routeSub);
					hasOverlap = true;
					routes.remove(subIdx);
					subIdx--;
					routes.set(mainIdx, routeMain);
				}
			}
		}
		allRoutes = routes;

		return hasOverlap;
	}

	private double verticalAngle(double in) {
		if (in<=90d) {
			return in;
		} else if (in<=180d) {
			return 180d-in;
		} else if (in<=270d) {
			return in-180d;
		} else {
			return 360d-in;
		}
	}
	
	private List<OverlapPoint> enlistOutline(OverlapPointFactory overlapPointFactory) {
		OverlapPoint startOverlapPoint = overlapPointFactory.stream().min((a,b) -> Long.compare(a.point.x, b.point.x)).orElse(null);
		
		double startAlpha = 0d;
		Route startRoute = null;
		OverlapPoint startNextPoint = null;
		boolean isForward = true;
		for (Route route : startOverlapPoint.routeIterable()) {
			route.ensureOrdered();
			int indexOf = route.indexOf(startOverlapPoint);
			double routeAlpha = route.base.getAlpha();
			OverlapPoint pointAt = route.overlapPointAt(indexOf + 1);
			if (pointAt != null) {
//				if ((startNextPoint == null) || (pointAt.point.x < startNextPoint.point.x)) {
				if ((startNextPoint == null) || (verticalAngle(routeAlpha)>verticalAngle(startAlpha))) {
					startNextPoint = pointAt;
					startRoute = route;
					isForward = true;
					startAlpha = routeAlpha;
				}

			}
			pointAt = route.overlapPointAt(indexOf - 1);
			if (pointAt != null) {
				if ((startNextPoint == null) || (verticalAngle(routeAlpha)>verticalAngle(startAlpha))) {
//				if ((startNextPoint == null) || (pointAt.point.x < startNextPoint.point.x)) {
					startNextPoint = pointAt;
					startRoute = route;
					isForward = false;
					startAlpha = (routeAlpha+180d) % 360d;
				}

			}
		}

		OverlapPoint pointA = startOverlapPoint;
		OverlapPoint pointB = startNextPoint;

		Point2D testP = new Point2D(-100+(pointA.point.x+pointB.point.x)/2, (pointA.point.y+pointB.point.y)/2);
		int relativeCCW = startRoute.base.relativeCCW(testP);
		
		double alpha = startRoute.base.getAlpha();
		if (!isForward) {
//			alpha = (alpha + 180d) % 360d;
			relativeCCW = -relativeCCW;
		} else {
			alpha = (alpha + 180d) % 360d;
		}
		
		boolean ccw = relativeCCW<=0;

		List<OverlapPoint> enlisted = new ArrayList<>();
		enlisted.add(pointA);
		enlisted.add(pointB);

		while (true) {

			OverlapPoint bestNextOP = null;
			double bestAlphaDiff = 0d;
			double bestNextAlpha = 0d;

			StringBuilder buf = new StringBuilder();
			buf.append(pointA.point+".."+pointB.point+" ("+asPointDegree(alpha)+")");

			if (pointB.point.equals(new Point2D(7163, 20363))) {
				System.err.println("gogog");
			}
			
			for (Route route : pointB.routeIterable()) {
				route.ensureOrdered();
				int indexOf = route.indexOf(pointB);
				OverlapPoint overlapPointFwd = route.overlapPointAt(indexOf + 1);
				if (overlapPointFwd != null && !overlapPointFwd.equals(pointA)) {
					double routeAlpha = route.base.getAlpha();
					double alphaDiff = (360d + routeAlpha - alpha) % 360d;
					buf.append("    "+overlapPointFwd.point+"("+routeAlpha+", "+alphaDiff+")");
					if (bestNextOP == null || (ccw && alphaDiff > bestAlphaDiff) || (!ccw && alphaDiff < bestAlphaDiff)) {
						bestNextOP = overlapPointFwd;
						bestAlphaDiff = alphaDiff;
						bestNextAlpha = routeAlpha;
					}
				}
				OverlapPoint overlapPointRev = route.overlapPointAt(indexOf - 1);
				if (overlapPointRev != null && !overlapPointRev.equals(pointA)) {
					double routeAlpha = route.base.getAlpha();
					routeAlpha = (routeAlpha + 180d) % 360d;
					double alphaDiff = (360d + routeAlpha - alpha) % 360d;
					buf.append("    "+overlapPointRev.point+"("+routeAlpha+", "+alphaDiff+")");
					if (bestNextOP == null || (ccw && alphaDiff > bestAlphaDiff) || (!ccw && alphaDiff < bestAlphaDiff)) {
						bestNextOP = overlapPointRev;
						bestAlphaDiff = alphaDiff;
						bestNextAlpha = routeAlpha;
					}
				}

			}

			System.err.println(""+buf);
			System.err.println("bestNextAlpha="+bestNextAlpha+" bestAlphaDiff="+bestAlphaDiff);
			
			if (bestNextOP == null) {
				break;
			}
			pointA = pointB;
			pointB = bestNextOP;
			int indexOf = enlisted.indexOf(bestNextOP);
			enlisted.add(bestNextOP);
			if (indexOf >= 0) {
				break;
			}
			alpha = (bestNextAlpha+180d)%360d;
		}
		return enlisted;
	}
	
	private String asPointDegree(double alpha) {
		String j = "" + (int) alpha*10;
		return j.substring(0,j.length()-1)+"."+j.substring(j.length()-1)+"°";
	}

	private Route mergeRoutes(Route routeA, Route routeB) {
		long deltaX = routeA.base.deltaX();
		long deltaY = routeA.base.deltaY();
		Point2D pointA1 = routeA.base.pointA();
		Point2D pointA2 = routeA.base.pointB();
		Point2D pointB1 = routeB.base.pointA();
		Point2D pointB2 = routeB.base.pointB();
		ArrayList<Point2D> pointList = new ArrayList<Point2D>();
		pointList.add(pointA1);
		pointList.add(pointA2);
		pointList.add(pointB1);
		pointList.add(pointB2);
		if (Math.abs(deltaX) > Math.abs(deltaY)) {
			pointList.sort((pa, pb) -> Long.compare(pa.x, pb.x));
		} else {
			pointList.sort((pa, pb) -> Long.compare(pa.y, pb.y));
		}
		Line2D newBaseLine = new Line2D(pointList.get(0), pointList.get(3));
		Route result = new Route(newBaseLine);
		List<OverlapPoint> fromA = routeA.streamOverlapPoints().collect(Collectors.toList());
		List<OverlapPoint> fromB = routeB.streamOverlapPoints().collect(Collectors.toList());
		fromA.forEach(routeA::remove);
		fromB.forEach(routeB::remove);
		Stream.concat(fromA.stream(), fromB.stream()).forEach(result::add);
		result.ensureOrdered();
		return result;
	}

}
