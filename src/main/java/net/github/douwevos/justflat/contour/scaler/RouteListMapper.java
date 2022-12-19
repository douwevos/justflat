package net.github.douwevos.justflat.contour.scaler;

import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.util.NoRepeats;

public class RouteListMapper {

	
	public Contour routeListToScaledContour(RouteList routeList) {
		Contour contour = new Contour();
		routeList.stream().flatMap(r -> Stream.of(r.base.pointA(), r.base.pointB())).filter(NoRepeats.filter()).forEach(contour::add);
		return contour;
	}
	
}
