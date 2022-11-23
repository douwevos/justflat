package net.github.douwevos.justflat.contour;

import java.util.HashMap;
import java.util.Map;

import net.github.douwevos.justflat.contour.OverlapPoint.Taint;
import net.github.douwevos.justflat.types.Point2D;

public class OverlapPointFactory {

	private Map<Point2D, OverlapPoint> overlapPoints = new HashMap<>();

	
	public OverlapPoint create(Point2D point, Taint obscured, Route ... routes) {
		OverlapPoint result = overlapPoints.get(point);
		if (result == null) {
			if (point.equals(Point2D.of(3813, 1363)) || point.equals(Point2D.of(3813, 1362))) {
				System.err.println("s");
			}
				
			result = new OverlapPoint(point);
			overlapPoints.put(point, result);
		}
//		OverlapPoint result = null;
//		for(Route route : routes) {
//			result = route.find(point);
//			if (result != null) {
//				break;
//			}
//		}
//		if (result == null) {
//			result = new OverlapPoint(point);
//		}
		result.taintWith(obscured);
		for(Route route : routes) {
			route.add(result);
		}
		return result;
	}

}