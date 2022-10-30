package net.github.douwevos.justflat.contour;

import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Point2D;

public class TargetLine {

	private final MutableLine mutableLine;
	private final Line2D line;
	private final CrossPoint pointA;
	private final CrossPoint pointB;
	
	private boolean isValid = true;
	
	private boolean used = false;
	
	private boolean tainted = false;

	private boolean flipped = false;

	private boolean hidden;
	
	public TargetLine(MutableLine mutableLine, CrossPoint pointA, CrossPoint pointB) {
		this.pointA = pointA;
		this.pointB = pointB;
		this.pointA.add(this);
		this.pointB.add(this);
		line = new Line2D(pointA.crossPoint, pointB.crossPoint);
		this.mutableLine = mutableLine;
	}

	public TargetLine withEnd(CrossPoint crossPointEnd) {
		return new TargetLine(mutableLine, pointA, crossPointEnd);
	}

	public TargetLine withStart(CrossPoint crossPointStart) {
		return new TargetLine(mutableLine, crossPointStart, pointB);
	}

	
	public void dispose() {
		pointA.remove(this);
		pointB.remove(this);
	}

	
	public MutableLine getMutableLine() {
		return mutableLine;
	}
	
	public void markInvalid() {
		isValid = false;
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public void markUsed() {
		used = true;
	}
	
	public boolean isUsed() {
		return used;
	}

	public void markTainted() {
		tainted = true;
	}
	
	public boolean isTainted() {
		return tainted;
	}

	public void markFlipped() {
		flipped = true;
	}
	
	public boolean isFlipped() {
		return flipped;
	}

	
	public void markHidden() {
		this.hidden = true;
		tainted = true;
		pointA.remove(this);
		pointB.remove(this);
	}
	
	public boolean isHidden() {
		return hidden;
	}

	

	public CrossPoint pointA() {
		return pointA;
	}
	
	public CrossPoint pointB() {
		return pointB;
	}


	public Line2D asLine() {
		return line;
	}

	public boolean sameDirection() {
		long baseDx = mutableLine.baseDx;
		long baseDy = mutableLine.baseDy;

		Point2D pa = pointA().crossPoint;
		Point2D pb = pointB().crossPoint;
		long dx = pb.x - pa.x;
		long dy = pb.y - pa.y;
		
		boolean dxSame = (dx<0 && baseDx<0) || (dx>0 && baseDx>0) || (dx==baseDx);
		boolean dySame = (dy<0 && baseDy<0) || (dy>0 && baseDy>0) || (dy==baseDy);
		
		return dxSame && dySame;

	}

	public Point2D getOtherPoint(Point2D pPoint) {
		return line.getOtherPoint(pPoint);
	}
	
	
	@Override
	public String toString() {
		return "TargetLine["+pointA()+".."+pointB()+", "+(isValid ? "valid" : "invalid")+", tainted="+tainted+"]";
	}


}