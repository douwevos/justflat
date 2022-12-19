package net.github.douwevos.justflat.contour.scaler;

import java.util.List;
import java.util.stream.Collectors;

import net.github.douwevos.justflat.contour.scaler.ScalerViewableModel.OverlapPointSelection;
import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.Selection;
import net.github.douwevos.justflat.demo.ViewableModel;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.types.values.Point2D;

public class RouteListScaledDownCombinatorViewableModel implements ViewableModel {

	private RouteListScaledDownCombinator routeListScaledDownCombinator;
	private final Bounds2D bounds;
	
	public RouteListScaledDownCombinatorViewableModel(RouteListScaledDownCombinator routeListScaledDownCombinator) {
		this.routeListScaledDownCombinator = routeListScaledDownCombinator;
		
		List<RouteList> routeList = routeListScaledDownCombinator.getCombined();
		bounds = routeList.stream().map(RouteList::bounds).reduce(Bounds2D::reduce).orElse(null);
	}
	
	public List<RouteList> getCombined() {
		return routeListScaledDownCombinator.getCombined();
	}

	public RouteListInteractionAnalyser getAnalyser() {
		return routeListScaledDownCombinator.getAnalyser();
	}

	@Override
	public Bounds2D bounds() {
		return bounds;
	}

	@Override
	public Selection<?> selectAt(ModelMouseEvent modelMouseEvent) {
		RouteListInteractionAnalyser analyser = getAnalyser();
		if (analyser==null || analyser.overlapPointFactory==null) {
			return null;
		}
		
		OverlapPoint bestOverlapPoint = null;
		double bestSqDist = 0;
		double bestAlpha = 0d;
		
		double nx = modelMouseEvent.modelX;
		double ny = modelMouseEvent.modelY;
		double zoomFactor = modelMouseEvent.camera.getZoom();

		List<OverlapPoint> points = analyser.overlapPointFactory.stream().collect(Collectors.toList());

		for (OverlapPoint overlapPoint : points) {
			
			Point2D dot = overlapPoint.point;
			double sx = dot.x-nx;
			double sy = dot.y-ny;
			double d = sx*sx + sy*sy;
			if (bestOverlapPoint==null || d<bestSqDist) {
				bestSqDist = d;
				bestOverlapPoint = overlapPoint;
				
				double lineAlpha;
		        if (sx == 0) {
		            lineAlpha = Math.PI * 1.5d;
		            if (sy < 0)
		                lineAlpha = Math.PI * 0.5d;
		        }
		        else {
		            double lineCosAlpha = (double) sy / (double) sx;
		            lineAlpha = Math.atan(lineCosAlpha);
		            if (lineAlpha < 0.0d) {
		                lineAlpha = 2 * Math.PI + lineAlpha;
		            }
		        }

		        bestAlpha = Math.toDegrees(lineAlpha);
		        if (sx > 0) {
		        	bestAlpha = (bestAlpha + 180.0d) % 360d;
		        }

				
			}
		}			
//		log.debug("nx="+nx+", ny="+ny+", bestCrossPoint="+bestCrossPoint+", zoomFactor="+zoomFactor+" bestSqDist="+bestSqDist);
		
		if (bestOverlapPoint!=null && bestSqDist<(10000d*zoomFactor*zoomFactor)) {
			return new OverlapPointSelection(bestOverlapPoint, bestAlpha);
		}
		return null;
	}

}
