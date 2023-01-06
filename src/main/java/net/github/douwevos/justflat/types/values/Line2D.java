package net.github.douwevos.justflat.types.values;

import java.util.Objects;

import net.github.douwevos.justflat.logging.Log;

public class Line2D {

	Log log = Log.instance(false);

    private final Point2D pointA, pointB;

    boolean mainValuesCalculated;
    double lineLengthSq, lineLength, lineCosAlpha, lineAlpha, lineAlphaDeg;

    public Line2D(Point2D pointA, Point2D pointB) {
        if (pointA == null) {
            throw new IllegalArgumentException("argument pointA is mandatory");
        }
        if (pointB == null) {
            throw new IllegalArgumentException("argument pointB is mandatory");
        }
        this.pointA = pointA;
        this.pointB = pointB;
    }

	public Line2D createByOffsets(double leftOffset, double rightOffset) {
		long dx = pointB.x - pointA.x;
		long dy = pointB.y - pointA.y;
		
		long nxa = Math.round(pointA.x + dx*leftOffset);
		long nya = Math.round(pointA.y + dy*leftOffset);

		long nxb = Math.round(pointA.x + dx*rightOffset);
		long nyb = Math.round(pointA.y + dy*rightOffset);

		return new Line2D(new Point2D(nxa, nya), new Point2D(nxb, nyb));
	}

    
    public Line2D createInverted() {
        Line2D result = new Line2D(pointB, pointA);
        // TODO: fix and finish below;
        // if (mainValuesCalculated) {
        // result.mainValuesCalculated = true;
        // result.lineLengthSq = lineLengthSq;
        // result.lineLength = lineLength;
        // result.lineCosAlpha = lineCosAlpha;
        // }

        return result;
    }

	public Line2D withFirstPoint(Point2D point) {
		if (Objects.equals(point, pointA)) {
			return this;
		}
		return new Line2D(point, pointB);
	}

	public Line2D withSecondPoint(Point2D point) {
		if (Objects.equals(point, pointB)) {
			return this;
		}
		return new Line2D(pointA, point);
	}

    
    public double pointDistanceSq(Point2D point)  {
		return java.awt.geom.Line2D.ptSegDistSq(pointA.x, pointA.y, pointB.x, pointB.y, point.x, point.y);
    }
    
    public double pointDistance(Point2D E) {
    	Point2D A = pointA;
    	Point2D B = pointB;
    	// vector AB
    	double abX = B.x - A.x;
    	double abY = B.y - A.y;

    	// vector BP
    	double beX = E.x - B.x;
    	double beY = E.y - B.y;

    	// vector AP
    	double aeX = E.x - A.x;
    	double aeY = E.y - A.y;

    	// Calculating the dot product
    	double AB_BE = (abX * beX + abY * beY);
    	double AB_AE = (abX * aeX + abY * aeY);

    	// Minimum distance from
    	// point E to the line segment
    	double reqAns = 0;

    	// Case 1
    	if (AB_BE > 0) {

    		// Finding the magnitude
    		double y = E.y - B.y;
    		double x = E.x - B.x;
    		reqAns = Math.sqrt(x * x + y * y);
    	}

    	// Case 2
    	else if (AB_AE < 0) {
    		double y = E.y - A.y;
    		double x = E.x - A.x;
    		reqAns = Math.sqrt(x * x + y * y);
    	}

    	// Case 3
    	else {

    		// Finding the perpendicular distance
    		double x1 = abX;
    		double y1 = abY;
    		double x2 = aeX;
    		double y2 = aeY;
    		double mod = Math.sqrt(x1 * x1 + y1 * y1);
    		reqAns = Math.abs(x1 * y2 - y1 * x2) / mod;
    	}
    	return reqAns;
    }
    
    
	public long signedAlignedDistanceSq(Point2D point) {
		long dxA = point.x-pointA.x;
		long dyA = point.y-pointA.y;
		
		long squaredDistanceA = dxA*dxA + dyA*dyA;
		if (squaredDistanceA == 0) {
			return 0;
		}

		if (dxA != 0) {
			long dxB = point.x-pointB.x;
			if (dxB!=0) {
				boolean sameDir = dxB<0 && dxA<0 ||  dxB>0 && dxA>0;
				return sameDir ? -squaredDistanceA : squaredDistanceA; 
			}
		}
		
		long dyB = point.y-pointB.y;
		boolean sameDir = dyB<0 && dyA<0 ||  dyB>0 && dyA>0;
		return sameDir ? -squaredDistanceA : squaredDistanceA; 
	}

    
    public Point2D crossPoint(Line2D otherSegment, IntersectionInfo info) {
    	if (info == null) {
    		info = new IntersectionInfo();
    	}
    	info.intersectionPoint = null;
    	IntersectionInfo info2 = new IntersectionInfo();

    	Point2D result = null;
    	Point2D mainIntPoint = otherSegment.intersectionPoint(this, info);
    	if (mainIntPoint!=null && info.ua>=0.0d && info.ua<=1.0d) {
	    	Point2D intersectionPoint = this.intersectionPoint(otherSegment, info2);
	    	if (info2.ua>=0.0d && info2.ua<=1.0d) {
	    		info.intersectionPoint = intersectionPoint;
	    		if (!(otherSegment.isFirstOrSecond(mainIntPoint) || isFirstOrSecond(mainIntPoint))) {
	    			result = mainIntPoint;
	    		}
	    	} else {
	    		info.intersectionPoint = null;    		
	    	}
    	} else {
    		info.intersectionPoint = null;    		
    	}
    	
    	return result;
    }
    
    
    public boolean hasCrossPointWith(Line2D other) {
    	return crossPoint(other, null) != null;
    }
    

    public Point2D intersectionPoint(Line2D otherSegment, IntersectionInfo info) {
        if (info == null) {
            info = new IntersectionInfo();
        }
        info.dxAC = (pointA.getX() - otherSegment.pointA.getX()); 
        info.dyAC = (pointA.getY() - otherSegment.pointA.getY());
        info.dxBA = (pointB.getX() - pointA.getX());
        info.dyBA = (pointB.getY() - pointA.getY());

        info.dxDC = (otherSegment.pointB.getX() - otherSegment.pointA.getX());
        info.dyDC = (otherSegment.pointB.getY() - otherSegment.pointA.getY());

        info.udd = (info.dyDC * info.dxBA - info.dxDC * info.dyBA);
        info.ua = java.lang.Double.NaN;
        if (info.udd == 0f) {
            return null;
        }

        info.ua = (info.dxDC * info.dyAC - info.dyDC * info.dxAC) / info.udd;
        // if ((ua<0f) || (ua>1f)) {
        // return null;
        // }
        // reuse some local vars :)
        info.dxAC = pointA.getX() + Math.round(info.ua * info.dxBA);
        info.dyAC = pointA.getY() + Math.round(info.ua * info.dyBA);
        info.intersectionPoint = new Point2D(info.dxAC, info.dyAC);
        return info.intersectionPoint;
    }

    public Point2D intersectionPoint(Point2D point, IntersectionInfo info) {
        if (info == null) {
            info = new IntersectionInfo();
        }
        
        info.dxBA = (pointB.getX() - pointA.getX());
        info.dyBA = (pointB.getY() - pointA.getY());
        
        long vpx = point.getX() - info.dyBA;
        long vpy = point.getY() + info.dxBA;
        
        info.dxAC = (pointA.getX() - vpx); 
        info.dyAC = (pointA.getY() - vpy);

        info.dxDC = info.dyBA;
        info.dyDC = -info.dxBA;

        info.udd = (-info.dxBA * info.dxBA - info.dyBA * info.dyBA);
        info.ua = java.lang.Double.NaN;
        if (info.udd == 0f) {
            return null;
        }

        info.ua = (info.dyBA * info.dyAC - -info.dxBA * info.dxAC) / info.udd;
        // if ((ua<0f) || (ua>1f)) {
        // return null;
        // }
        // reuse some local vars :)
        info.dxAC = pointA.getX() + Math.round(info.ua * info.dxBA);
        info.dyAC = pointA.getY() + Math.round(info.ua * info.dyBA);
        info.intersectionPoint = new Point2D(info.dxAC, info.dyAC);
        return info.intersectionPoint;
    }

    

    public Point2D intersectionPoint3(Line2D otherSegment, IntersectionInfo info) {
        if (info == null) {
            info = new IntersectionInfo();
        }


        info.dxDC = otherSegment.pointB.x - otherSegment.pointA.x;
        info.dyDC = otherSegment.pointB.y - otherSegment.pointA.y;

        info.dxBA = pointB.x - pointA.x;
        info.dyBA = pointB.y - pointA.y;

        long denominator = info.dxBA * info.dyDC - info.dxDC * info.dyBA;

        if (denominator == 0)  {
            return null;
        }

        boolean denominatorIsPositive = denominator > 0;

        info.dxAC = pointA.x - otherSegment.pointA.x;
        info.dyAC = pointA.y - otherSegment.pointA.y;

        long sPart = info.dxBA * info.dyAC - info.dyBA * info.dxAC;

//        if ((sPart < 0) == denominatorIsPositive) {
//            return null;
//        }

        long tPart = info.dxDC * info.dyAC - info.dyDC * info.dxAC;

//        if ((tPart < 0) == denominatorIsPositive) {
//            return null;
//        }
//
        if (((sPart > denominator) == denominatorIsPositive) || ((tPart > denominator) == denominatorIsPositive)) {
            return null;
        }

        double t = (double) tPart / denominator;
        
        info.ua = t;
        
        return new Point2D(Math.round(pointA.x + (t * info.dxBA)), Math.round(pointA.y + (t * info.dyBA)));

    }

    public Point2D intersectionPoint2(Line2D otherSegment, IntersectionInfo info) {
        if (info == null) {
            info = new IntersectionInfo();
        }
        Point2D A = pointA;
        Point2D B = pointB;
        Point2D C = otherSegment.pointA;
        Point2D D = otherSegment.pointB;
        
    	// Line AB represented as a1x + b1y = c1
        double dyBA = B.y - A.y;
        double dxBA = B.x - A.x;
        double c1 = dyBA*(A.x) - dxBA*(A.y);
      
        // Line CD represented as a2x + b2y = c2
        double dyDC = D.y - C.y;
//        double dxCD = C.x - D.x;
        double dxDC = D.x - C.x;
        double c2 = dyDC*(C.x) - dxDC*(C.y);
      
        double determinant = dxBA*dyDC - dyBA*dxDC;

        if (determinant == 0)
        {
            // The lines are parallel. This is simplified
            // by returning a pair of FLT_MAX
            return null;
        }
        else
        {
        	
        	
            double x = (dxBA*c2 - dxDC*c1)/determinant;
            double y = (dyBA*c2 - dyDC*c1)/determinant;

            
            double dnyBA = y - A.y;
            double dnxBA = x - A.x;

            if (dnxBA==0 || dxBA==0) {
            	info.ua = dnyBA/dyBA;
            } else {
            	info.ua = dnxBA/dxBA;
            }
            
            
            return new Point2D(Math.round(x), Math.round(y));
        }
    }
    

    public static class IntersectionInfo implements Comparable<IntersectionInfo> {

        public long dxAC;
        public long dyAC;

        public long dxBA;
        public long dyBA;

        public long dxDC;
        public long dyDC;

        public double udd;
        public double ua;
        public Point2D intersectionPoint;

        @Override
        public int compareTo(IntersectionInfo o) {
            return o.ua == ua ? 0 : o.ua < ua ? -1 : 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IntersectionInfo) {
                IntersectionInfo oinfo = (IntersectionInfo) obj;
                return oinfo.ua == ua;
            }
            return false;
        }

        @Override
        public String toString() {
            return "IntersectionInfo[intersectionPoint=" + intersectionPoint + ", ua=" + ua + ", udd=" + udd + ", ac("+dxAC+","+dyAC+") # ba("+dxBA+","+dyBA+")  # dc("+dxDC+","+dyDC+") ]";
        }
    }

    public Point2D pointA() {
		return pointA;
	}
    
    public Point2D pointB() {
		return pointB;
	}
    
    public Point2D getFirstPoint() {
        return pointA;
    }

    public Point2D getSecondPoint() {
        return pointB;
    }
    
    public boolean isFirstOrSecond(Point2D p) {
    	return p==pointA || p==pointB || pointA.equals(p) || pointB.equals(p);
    }

    public void reset() {
        mainValuesCalculated = false;
    }

    public void calculate() {
        if (mainValuesCalculated) {
            return;
        }


        double dx = pointA.getX() - pointB.getX();
        double dy = pointA.getY() - pointB.getY();

        lineLengthSq = dx * dx + dy * dy;
        lineLength = Math.sqrt(lineLengthSq);

        if (dx == 0) {
            lineAlpha = Math.PI * 1.5d;
            if (dy < 0)
                lineAlpha = Math.PI * 0.5d;
        }
        else {
            lineCosAlpha = (double) dy / (double) dx;
            lineAlpha = Math.atan(lineCosAlpha);
            if (lineAlpha < 0.0d) {
                lineAlpha = 2 * Math.PI + lineAlpha;
            }
        }

        lineAlphaDeg = Math.toDegrees(lineAlpha);
        if (dx > 0) {
            lineAlphaDeg = (lineAlphaDeg + 180.0d) % 360d;
        }

        mainValuesCalculated = true;

    }

    public double getAlpha() {
        calculate();
        return lineAlphaDeg;
    }

    public double getCosAlpha() {
        calculate();
        return lineCosAlpha;
    }

    public double getAlphaRad() {
        calculate();
        return lineAlpha;
    }

    public double getLineLength() {
        calculate();
        return lineLength;
    }
    

	public int relativeCCW(Point2D p) {
		return relativeCCW(p.x, p.y);
	}
    
	public int relativeCCW(long px, long py) {
		long x1 = pointA.x;
		long y1 = pointA.y;
		long x2 = pointB.x;
		long y2 = pointB.y;
		x2 -= x1;
		y2 -= y1;
		px -= x1;
		py -= y1;
		long ccw = px * y2 - py * x2;
		if (ccw == 0) {
			// The point is colinear, classify based on which side of
			// the segment the point falls on.  We can calculate a
			// relative value using the projection of px,py onto the
			// segment - a negative value indicates the point projects
			// outside of the segment in the direction of the particular
			// endpoint used as the origin for the projection.
			ccw = px * x2 + py * y2;
			if (ccw > 0) {
				// Reverse the projection to be relative to the original x2,y2
				// x2 and y2 are simply negated.
				// px and py need to have (x2 - x1) or (y2 - y1) subtracted
				//    from them (based on the original values)
				// Since we really want to get a positive answer when the
				//    point is "beyond (x2,y2)", then we want to calculate
				//    the inverse anyway - thus we leave x2 & y2 negated.
				px -= x2;
				py -= y2;
				ccw = px * x2 + py * y2;
				if (ccw < 0) {
					ccw = 0;
				}
			}
		}
		return (ccw < 0) ? -1 : ((ccw > 0) ? 1 : 0);
	}
    


	public boolean sharePoints(Line2D other) {
		return Objects.equals(pointA, other.pointA) || Objects.equals(pointA, other.pointB) ||
				Objects.equals(pointB, other.pointA) || Objects.equals(pointB, other.pointB);
	}
	

	public long deltaX() {
		return pointB.x - pointA.x;
	}

	public long deltaY() {
		return pointB.y - pointA.y;
	}

	public Point2D getOtherPoint(Point2D iterPoint) {
		return pointA.equals(iterPoint) ? pointB : pointA;
	}



	
	public boolean farAwayTest(Line2D other) {
		long xa = pointA.x;
		long xb = pointB.x;
		
		long oxa = other.pointA.x;
		long oxb = other.pointB.x;
		
		if ((xa>oxa) && (xa>oxb) && (xb>oxa) && (xb>oxb)) {
			return true;
		}
		if ((xa<oxa) && (xa<oxb) && (xb<oxa) && (xb<oxb)) {
			return true;
		}

		long ya = pointA.y;
		long yb = pointB.y;
		
		long oya = other.pointA.y;
		long oyb = other.pointB.y;
		
		if ((ya>oya) && (ya>oyb) && (yb>oya) && (yb>oyb)) {
			return true;
		}
		if ((ya<oya) && (ya<oyb) && (yb<oya) && (yb<oyb)) {
			return true;
		}

		return false;
	}

	public Bounds2D bounds() {
		return new Bounds2D(pointA.x, pointA.y, pointB.x, pointB.y);
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Line2D) {
			Line2D other = (Line2D) obj;
			return Objects.equals(pointA, other.pointA)
					&& Objects.equals(pointB, other.pointB);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return pointA.hashCode() + 13 * pointB.hashCode();
	}

    @Override
    public String toString() {
        String stext = super.toString();
        stext = stext.substring(stext.length() - 6);
        stext = "Line[" + stext + ", A=" + pointA + ", B=" + pointB + "]";
        return stext;
    }



}
