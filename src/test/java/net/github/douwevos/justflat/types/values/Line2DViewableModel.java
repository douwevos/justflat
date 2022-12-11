package net.github.douwevos.justflat.types.values;

import java.util.List;

import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.Selection;
import net.github.douwevos.justflat.demo.ViewableModel;

public class Line2DViewableModel implements ViewableModel {

	final Line2D lineA;
	final Line2D lineB;
	
	public Line2DViewableModel(Line2D lineA, Line2D lineB) {
		this.lineA = lineA;
		this.lineB = lineB;
	}
	
	@Override
	public Bounds2D bounds() {
		Bounds2D bounds = lineA.bounds();
		return bounds.extend(lineB);
	}
	
	@Override
	public Selection<?> selectAt(ModelMouseEvent modelMouseEvent) {
		
		double modelX = modelMouseEvent.modelX;
		double modelY = modelMouseEvent.modelY;
		double zoomFactor = modelMouseEvent.camera.getZoom();

		
		Point2D bestPoint = null;
		Line2D bestLine = null;
		double bestSqDist = 0;

		List<Line2D> lines = List.of(lineA, lineB);
		for(Line2D line : lines) {
			List<Point2D> points = List.of(line.pointA(), line.pointB());
			for(Point2D dot : points) {
				double sx = dot.x-modelX;
				double sy = dot.y-modelY;
				double d = sx*sx + sy*sy;
				if (bestPoint==null || d<bestSqDist) {
					bestSqDist = d;
					bestPoint = dot;
					bestLine = line;
				}
			}
		}

		if (bestPoint!=null && bestSqDist<(400d*zoomFactor*zoomFactor)) {
			return new LinePointSelection(bestLine, bestPoint);
		}

		
		return null;
	}
	
	
	public static class LineSelection implements Selection<Line2D> {
		
		public final Line2D line;
		
		public LineSelection(Line2D line) {
			this.line = line;
		}
		
		@Override
		public Line2D get() {
			return line;
		}
	}

	public static class LinePointSelection implements Selection<Point2D> {
		
		public final Line2D line;
		public final Point2D point;
		
		public LinePointSelection(Line2D line, Point2D point) {
			this.line = line;
			this.point = point;
		}
		
		@Override
		public Point2D get() {
			return point;
		}
	}

}
