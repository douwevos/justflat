package net.github.douwevos.justflat.types.values;

import net.github.douwevos.justflat.types.values.Line2D.IntersectionInfo;

public class LineRelation {

	public final Line2D lineA;
	public final Line2D lineB;

	public final boolean sharePoints;
	
	public final Point2D intersectionPoint;
	
	public final InLineTyp inLineTyp;
	
	public double na, nb;
	
	public LineRelation(Line2D lineA, Line2D lineB) {
		this.lineA = lineA;
		this.lineB = lineB;
		Point2D pAA = lineA.pointA();
		Point2D pAB = lineA.pointB();
		Point2D pBA = lineB.pointA();
		Point2D pBB = lineB.pointB();
		sharePoints = pAA.equals(pBA) || pAA.equals(pBB)
				|| pAB.equals(pBA) || pAB.equals(pBB);

		
    	IntersectionInfo info = new IntersectionInfo();
    	info.intersectionPoint = null;
    	IntersectionInfo info2 = new IntersectionInfo();

    	Point2D result = null;
    	Point2D mainIntPoint = lineA.intersectionPoint(lineB, info);
    	if (mainIntPoint!=null && info.ua>=0.0d && info.ua<=1.0d) {
	    	Point2D intersectionPoint = lineB.intersectionPoint(lineA, info2);
	    	if (info2.ua>=0.0d && info2.ua<=1.0d) {
	    		info.intersectionPoint = intersectionPoint;
    			result = mainIntPoint;
	    	} else {
	    		info.intersectionPoint = null;    		
	    	}
    	} else {
    		info.intersectionPoint = null;    		
    	}
    	intersectionPoint = result;
    	
    	InLineTyp inLineTyp = InLineTyp.NONE_PARALLEL;
    	if (intersectionPoint == null) {
    		if (runParallel(lineA, lineB)) {
    	    	inLineTyp = InLineTyp.PARALLEL;
    			if (runInLine(lineA, lineB)) {
        	    	inLineTyp = InLineTyp.IN_LINE;
        	    	if (sharePoints) {
            	    	inLineTyp = InLineTyp.IN_LINE_SHARED_POINT;
        	    	} else {
        	    		if (checkInLineOverlap(lineA, lineB)) {
                	    	inLineTyp = InLineTyp.IN_LINE_OVERLAP;
        	    		}
        	    	}
    			}
    		}
    	}
    	this.inLineTyp = inLineTyp;
	}
	

	private boolean checkInLineOverlap(Line2D lineA, Line2D lineB) {
		Point2D pointA1 = lineA.pointA();
		Point2D pointA2 = lineA.pointB();
		Point2D pointB1 = lineB.pointA();
		Point2D pointB2 = lineB.pointB();
		if (lineA.deltaX()==0) {
			return testOverlap(pointA1.y, pointA2.y, pointB1.y, pointB2.y);
		}
		return testOverlap(pointA1.x, pointA2.x, pointB1.x, pointB2.x);
	}


	private boolean testOverlap(long a1, long a2, long b1, long b2) {
		long left = a1;
		long right = a2;
		
		if (a1>a2) {
			left = a2;
			right = a1;
		}
		return (b1>=left && b1<=right)
				|| (b2>=left && b2<=right);
	}


	public Point2D getIntersectionPoint() {
		return intersectionPoint;
	}

	
	
	private boolean runParallel(Line2D lineA, Line2D lineB) {
		long deltaXA = lineA.deltaX();
		long deltaYA = lineA.deltaY();
		long deltaXB = lineB.deltaX();
		long deltaYB = lineB.deltaY();
		
		if (deltaXA==deltaXB && deltaYA==deltaYB) {
			return true;
		}

		
		if (deltaXA==0 || deltaXB==0) {
			return deltaXA==deltaXB;
		}

		if (deltaYA==0 || deltaYB==0) {
			return deltaYA==deltaYB;
		}
		
		
		double da = (double) deltaXA / deltaYA;
		double db = (double) deltaXB / deltaYB;
		return da == db;
	}


	private boolean runInLine(Line2D lineA, Line2D lineB) {
		long deltaXA = lineA.deltaX();
		long deltaYA = lineA.deltaY();
		if (deltaXA == 0) {
			return lineA.getFirstPoint().x == lineB.getFirstPoint().x;
		}

		if (deltaYA == 0) {
			return lineA.getFirstPoint().y == lineB.getFirstPoint().y;
		}

		Point2D pointA = lineA.pointA();
		Point2D pointB = lineB.pointA();

		if (Math.abs(deltaXA)>Math.abs(deltaYA)) {
			double nyA = pointA.y - ((double) pointA.x * deltaYA)/deltaXA;
			double nyB = pointB.y - ((double) pointB.x * deltaYA)/deltaXA;
			na = nyA;
			nb = nyB;
			
			return nyA == nyB;
		}
		double nxA = pointA.x - ((double) pointA.y * deltaXA)/deltaYA;
		double nxB = pointB.x - ((double) pointB.y * deltaXA)/deltaYA;
		na = nxA;
		nb = nxB;
		return nxA == nxB;
	}

	
	public static enum InLineTyp {
		NONE_PARALLEL,
		PARALLEL,
		IN_LINE,
		IN_LINE_SHARED_POINT,
		IN_LINE_OVERLAP
	}
	
}
