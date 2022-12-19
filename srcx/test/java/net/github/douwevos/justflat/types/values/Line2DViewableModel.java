package net.github.douwevos.justflat.types.values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.Selection;
import net.github.douwevos.justflat.demo.ViewableModel;
import net.github.douwevos.justflat.types.values.Line2D.IntersectionInfo;

public class Line2DViewableModel implements ViewableModel {

	final List<Line2D> lines = new ArrayList<>();
	
	Point2D crossPoint;
	IntersectionInfo info = new IntersectionInfo();
	
	LineRelation lineRelation;
	
	public Line2DViewableModel(Line2D lineA, Line2D lineB) {
		lines.add(lineA);
		lines.add(lineB);
		updateValues();
	}
	
	void updateValues() {
		Line2D lineA = lines.get(0);
		Line2D lineB = lines.get(1);
		crossPoint = lineA.crossPoint(lineB, info);
		lineRelation = new LineRelation(lineA, lineB);
	}
	
	@Override
	public Bounds2D bounds() {
		Bounds2D bounds = lines.get(0).bounds();
		for(int idx=1; idx<lines.size(); idx++) {
			bounds = bounds.extend(lines.get(idx));
		}
		return bounds;
	}
	
	public Point2D getCrossPoint() {
		return crossPoint;
	}
	
	public LineRelation getLineRelation() {
		return lineRelation;
	}
	
	
	@Override
	public Selection<?> selectAt(ModelMouseEvent modelMouseEvent) {
		
		double modelX = modelMouseEvent.modelX;
		double modelY = modelMouseEvent.modelY;
		double zoomFactor = modelMouseEvent.camera.getZoom();

		
		Point2D bestPoint = null;
		Line2D bestLine = null;
		int bestLineIndex = -1;
		double bestSqDist = 0;

		for(int lineIdx=0; lineIdx<lines.size(); lineIdx++) {
			Line2D line = lines.get(lineIdx);
			List<Point2D> points = List.of(line.pointA(), line.pointB());
			for(Point2D dot : points) {
				double sx = dot.x-modelX;
				double sy = dot.y-modelY;
				double d = sx*sx + sy*sy;
				if (bestPoint==null || d<bestSqDist) {
					bestSqDist = d;
					bestPoint = dot;
					bestLine = line;
					bestLineIndex = lineIdx;
				}
			}
		}

		if (bestPoint!=null && bestSqDist<(400d*zoomFactor*zoomFactor)) {
			return new LinePointSelection(modelX, modelY, bestLineIndex, bestLine, bestPoint);
		}

		if (crossPoint != null) {
			double sx = crossPoint.x-modelX;
			double sy = crossPoint.y-modelY;
			double distSq = sx*sx + sy*sy;
			if (distSq<(400d*zoomFactor*zoomFactor)) {
				return new CrossPointSelection(crossPoint, info);
			}
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
		
		public final double grabX;
		public final double grabY;

		public final int lineIndex;
		public final boolean isFirstPoint;
		public Point2D point;

		public Point2D start;
		
		public LinePointSelection(double grabX, double grabY, int lineIndex, Line2D line, Point2D point) {
			this.grabX = grabX;
			this.grabY = grabY;
			this.lineIndex = lineIndex;
			isFirstPoint = line.getFirstPoint()==point;
			this.point = point;
		}
		
		@Override
		public Point2D get() {
			return point;
		}
	}

	public static class CrossPointSelection implements Selection<Point2D> {
		
		public Point2D point;
		public IntersectionInfo info;

		public CrossPointSelection(Point2D point, IntersectionInfo info) {
			this.point = point;
			this.info = info;
		}
		
		@Override
		public Point2D get() {
			return point;
		}
	}

	public boolean dragTo(Selection<?> selected, boolean snapAngle, double modelX, double modelY) {
		if (selected instanceof LinePointSelection) {
			drag((LinePointSelection) selected, snapAngle, modelX, modelY);
			return true;
		}
		return false;
	}

	private void drag(LinePointSelection selected, boolean snapAngle, double modelX, double modelY) {
		Line2D line2d = lines.get(selected.lineIndex);
		if (selected.start == null) {
			selected.start = selected.isFirstPoint ? line2d.getFirstPoint() : line2d.getSecondPoint();
		} else {
			double dx = selected.grabX-selected.start.x;
			double dy = selected.grabY-selected.start.y;
			
			long newX = Math.round(modelX-dx);
			long newY = Math.round(modelY-dy);
			Point2D moved = Point2D.of(newX, newY);
			selected.point = moved;
			Line2D movedLine = selected.isFirstPoint ? line2d.withFirstPoint(moved) : line2d.withSecondPoint(moved);
			if (snapAngle) {
				double alpha = movedLine.getAlpha();
				double rest = alpha % 15;
				alpha-=rest;
				if (rest>7.5d) {
					alpha += 15d;
				}
				double lineLength = movedLine.getLineLength();
				long ady = Math.round(Math.sin(alpha*Math.PI/180d) * lineLength);
				long adx = Math.round(Math.cos(alpha*Math.PI/180d) * lineLength);
				
				if (selected.isFirstPoint) {
					Point2D pointB = line2d.pointB();
					Point2D pointA = new Point2D(pointB.x-adx, pointB.y-ady);
					movedLine = new Line2D(pointA, pointB);
				} else {
					Point2D pointA = line2d.pointA();
					Point2D pointB = new Point2D(pointA.x+adx, pointA.y+ady);
					movedLine = new Line2D(pointA, pointB);
					
				}
			}
			lines.set(selected.lineIndex, movedLine);
			updateValues();
		}
 	}

}
