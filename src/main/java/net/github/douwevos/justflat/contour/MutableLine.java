package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Point2D;

public class MutableLine {
	
	public final double thickness;
	
	long baseDx;
	long baseDy;
	
	public Line2D base;
	
	public Line2D translated;
	
	public boolean cleanSection;
	
	private List<TargetLine> targetLines;
	
	private CrossPoint crossPointStart;
	private CrossPoint crossPointEnd;
	
	private Line2D crossPointStartEndLine;

	public MutableLine() {
		thickness = 0;
	}

	public MutableLine(Point2D pa, Point2D pb, double thickness) {
		this.thickness = thickness;
		base = new Line2D(pa, pb);
		long dx = pb.x - pa.x;
		long dy = pb.y - pa.y;
		baseDx = dx;
		baseDy = dy;
		
		
		double lineLength = Math.sqrt(dx*dx + dy*dy);
		float halfDx = dx/2f;
		float halfDy = dy/2f;
		float midX = pa.x+ halfDx;
		float midY = pa.y+ halfDy;
		
		double tMidX = midX - (dy*thickness)/lineLength;
		double tMidY = midY + (dx*thickness)/lineLength;
		
		Point2D pointA = new Point2D(Math.round(tMidX-halfDx), Math.round(tMidY-halfDy));
		Point2D pointB = new Point2D(Math.round(tMidX+halfDx), Math.round(tMidY+halfDy));
		translated = new Line2D(pointA, pointB);
		
		crossPointStart = new CrossPoint(pointA);
		crossPointEnd = new CrossPoint(pointB);
		
		targetLines = new ArrayList<>();
		targetLines.add(new TargetLine(this, crossPointStart, crossPointEnd));
		
	
		
	}

	public void cutAt(CrossPoint crossPoint) {
		for(int idx=0; idx<targetLines.size(); idx++) {
			TargetLine targetLine = targetLines.get(idx);
			Point2D pointA = targetLine.pointA().crossPoint;
			Point2D pointB = targetLine.pointB().crossPoint;
			
			long cpDeltaX = crossPoint.crossPoint.x-pointA.x;
			
			long deltaX = pointB.x-pointA.x;
			if (deltaX<0) {
				if (cpDeltaX<deltaX || cpDeltaX>0) {
					continue;
				}
			} else if (deltaX>0) {
				if (cpDeltaX>deltaX || cpDeltaX<0) {
					continue;
				}
			} else if (deltaX!=cpDeltaX) {
				continue;
			}

			long cpDeltaY = crossPoint.crossPoint.y-pointA.y;
			
			long deltaY = pointB.y-pointA.y;
			if (deltaY<0) {
				if (cpDeltaY<deltaY || cpDeltaY>0) {
					continue;
				}
			} else if (deltaY>0) {
				if (cpDeltaY>deltaY || cpDeltaY<0) {
					continue;
				}
			} else if (deltaY!=cpDeltaY) {
				continue;
			}

			
			TargetLine left = targetLine.withEnd(crossPoint);
			TargetLine right = targetLine.withStart(crossPoint);
			targetLines.set(idx, left);
			targetLines.add(idx+1, right);
			targetLine.dispose();
			break;
		}
	}

	
	
	public Stream<TargetLine> streamTargetLines() {
		return targetLines.stream();
	}
	
	public CrossPoint findCrossPoint(Point2D point) {
		return targetLines.stream().flatMap(s -> Stream.of(s.pointA(), s.pointB())).filter(s -> s.crossPoint.equals(point)).findAny().orElse(null);
	}

	public CrossPoint getCrossPointEnd() {
		return crossPointEnd;
	}
	
	public CrossPoint getCrossPointStart() {
		return crossPointStart;
	}
	
	public void setCrossPointStart(CrossPoint crossPointStart) {
		if (Objects.equals(this.crossPointStart, crossPointStart)) {
			return;
		}

		int index = 0;
		TargetLine targetLine = targetLines.get(index);
		TargetLine newTargetLine = targetLine.withStart(crossPointStart);
		targetLines.set(index, newTargetLine);
		targetLine.dispose();
		this.crossPointStart = crossPointStart;
		crossPointStartEndLine = null;
	}
	
	public void setCrossPointEnd(CrossPoint crossPointEnd) {
		if (Objects.equals(this.crossPointEnd, crossPointEnd)) {
			return;
		}
		int index = targetLines.size()-1;
		TargetLine targetLine = targetLines.get(index);
		TargetLine newTargetLine = targetLine.withEnd(crossPointEnd);
		targetLines.set(index, newTargetLine);
		targetLine.dispose();
		this.crossPointEnd = crossPointEnd;
		crossPointStartEndLine = null;
	}
	
	
	public List<TargetLine> getTargetLines() {
		return targetLines;
	}

	
	
	static class CrossPointWithoffset {
		public final CrossPoint crossPoint;
		public final long offset;
		
		public CrossPointWithoffset(CrossPoint crossPoint, CrossPoint startCrossPoint) {
			this.crossPoint = crossPoint;
			Point2D firstPoint = startCrossPoint.crossPoint;
			Point2D crossPP = crossPoint.crossPoint;

			offset = firstPoint.squaredDistance(crossPP);
			
//			long deltaX = line.deltaX();
//			long deltaY = line.deltaY();
//
//			if (deltaX*deltaX>deltaY*deltaY) {
//				long deltaCPX = firstPoint.x-crossPP.x;
//				offset = (double) deltaCPX/deltaX;
//			} else {
//				long deltaCPY = firstPoint.y-crossPP.y;
//				offset = (double) deltaCPY/deltaY;
//			}
		}
	}

	
	public boolean sameDirection() {
		Point2D pa = translated.getFirstPoint();
		Point2D pb = translated.getSecondPoint();
		long dx = pb.x - pa.x;
		long dy = pb.y - pa.y;
		
		boolean dxSame = (dx<0 && baseDx<0) || (dx>0 && baseDx>0) || (dx==baseDx);
		boolean dySame = (dy<0 && baseDy<0) || (dy>0 && baseDy>0) || (dy==baseDy);
		
		return dxSame && dySame;
	}

	public boolean sameDirection(Point2D start, Point2D end) {
		long dx = end.x - start.x;
		long dy = end.y - start.y;
		
		boolean dxSame = (dx<0 && baseDx<0) || (dx>0 && baseDx>0) || (dx==baseDx);
		boolean dySame = (dy<0 && baseDy<0) || (dy>0 && baseDy>0) || (dy==baseDy);
		
		return dxSame && dySame;
	}
	
	public double crossPointProjectionOffset(CrossPoint crossPoint) {
			
//		System.err.println("crossPointStart="+crossPointStart+", crossPoint="+crossPoint);
		if (Objects.equals(crossPointStart.crossPoint, crossPoint.crossPoint)) {
			return 0d;
		}
		if (Objects.equals(crossPointEnd.crossPoint, crossPoint.crossPoint)) {
			return 1d;
		}
		
		if (crossPointStartEndLine==null) {
			crossPointStartEndLine = new Line2D(crossPointStart.crossPoint, crossPointEnd.crossPoint);
		}
		
		double lineLength = crossPointStartEndLine.getLineLength();
		if (lineLength == 0d) {
			return Double.NaN;
		}
		
		long deltaX = crossPoint.crossPoint.x - crossPointStart.crossPoint.x;
		long deltaY = crossPoint.crossPoint.y - crossPointStart.crossPoint.y;
		
		double cpLength = Math.sqrt(deltaY*deltaY + deltaX*deltaX);
		return cpLength/lineLength;
	}

	@Override
	public String toString() {
//		if (crossPoints!=null && !crossPoints.isEmpty()) {
//			return "TranslatedLine[base="+base+"  translated="+translated+"  cps="+crossPoints+"]";
//		}
		
		return "TranslatedLine[base="+base+"  translated="+translated+"]";
	}




}