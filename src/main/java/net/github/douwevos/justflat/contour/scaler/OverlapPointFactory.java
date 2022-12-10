package net.github.douwevos.justflat.contour.scaler;

import java.util.HashMap;
import java.util.Map;

import net.github.douwevos.justflat.contour.scaler.OverlapPoint.Taint;
import net.github.douwevos.justflat.types.values.Point2D;

public class OverlapPointFactory {

	private Map<Point2D, OverlapPoint> overlapPoints = new HashMap<>();

	
	public OverlapPoint create(Point2D point, Taint obscured, Route ... routes) {
		OverlapPoint result = overlapPoints.get(point);
		if (result == null) {
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

	public OverlapPoint get(Point2D point) {
		return overlapPoints.get(point);
	}
}
