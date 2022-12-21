package net.github.douwevos.justflat.contour.scaler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.github.douwevos.justflat.types.values.Bounds2D;

class RouteList {
	
	final List<Route> routes;
	public RouteList(List<Route> routes) {
		this.routes = routes;
	}
	public Stream<Route> stream() {
		return routes.stream();
	}
	
	
	public Bounds2D bounds() {
		return routes.stream().map(s -> s.base.bounds()).reduce(Bounds2D::reduce).orElse(null);
	}
	public RouteList duplicate() {
		List<Route> copy = routes.stream().map(Route::duplicate).collect(Collectors.toList());
		return new RouteList(copy);
	}
	
	public boolean testIfRunningCCW() {
		double startAlpha = 0d;
		Route startRoute = null;
		for (Route route : routes) {
			route.ensureOrdered();
			double routeAlpha = route.base.getAlpha();
			if ((startRoute == null) || (verticalAngle(routeAlpha)>verticalAngle(startAlpha))) {
				startRoute = route;
				startAlpha = routeAlpha;
			}
		}
		
		return startAlpha<=180d;
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

}
