package net.github.douwevos.justflat.contour.scaler;

import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Point2D;

public class TargetLine {

	private final Line2D line;
	private final Point2D pointA;
	private final Point2D pointB;
	
	public TargetLine(Point2D pointA, Point2D pointB) {
		this.pointA = pointA;
		this.pointB = pointB;
		line = new Line2D(pointA, pointB);
	}

	public TargetLine withEnd(Point2D crossPointEnd) {
		return new TargetLine(pointA, crossPointEnd);
	}

	public TargetLine withStart(Point2D crossPointStart) {
		return new TargetLine(crossPointStart, pointB);
	}

	public Point2D pointA() {
		return pointA;
	}
	
	public Point2D pointB() {
		return pointB;
	}


	public Line2D asLine() {
		return line;
	}

	public Point2D getOtherPoint(Point2D pPoint) {
		return line.getOtherPoint(pPoint);
	}
	
	@Override
	public String toString() {
		return "TargetLine["+pointA()+".."+pointB()+"]";
	}


}