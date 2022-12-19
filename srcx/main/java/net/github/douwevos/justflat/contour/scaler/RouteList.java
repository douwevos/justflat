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
}