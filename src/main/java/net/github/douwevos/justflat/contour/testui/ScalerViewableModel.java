package net.github.douwevos.justflat.contour.testui;

import java.util.List;
import java.util.stream.Collectors;

import net.github.douwevos.justflat.contour.CrossPoint;
import net.github.douwevos.justflat.contour.MutableContour;
import net.github.douwevos.justflat.contour.OverlapPoint;
import net.github.douwevos.justflat.contour.TranslatedSegment;
import net.github.douwevos.justflat.types.Bounds2D;
import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Point2D;


public class ScalerViewableModel implements ViewableModel {

	public final List<MutableContour> mutableContours;
	
	
	public ScalerViewableModel(List<MutableContour> mutableContours) {
		this.mutableContours = mutableContours;
	}


	@Override
	public Bounds2D bounds() {
		
		return mutableContours.stream().map(s -> this.bound(s)).reduce(null, (a,b) -> {
			if (a==null) {
				return b;
			}
			return a.union(b);
		});
	}


	private Bounds2D bound(MutableContour mutableContour) {
		Bounds2D result = null;
		for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
			if (result == null) {
				result = translatedSegment.base.bounds();
			} else {
				result = result.extend(translatedSegment.base);
			}
			result = result.extend(translatedSegment.translated);
		}
		
		return result;
	}
	
	public Selection<?> selectAt(double nx, double ny, double zoomFactor) {
		CrossPoint bestCrossPoint = null;
		double bestSqDist = 0;
		
		for(MutableContour mutableContour : mutableContours) {
			
			List<OverlapPoint> points = mutableContour.segements.stream().flatMap(s -> s.streamOverlapPoints()).collect(Collectors.toList());
			for (OverlapPoint overlapPoint : points) {
				
				Point2D dot = overlapPoint.point;
				double sx = dot.x-nx;
				double sy = dot.y-ny;
				double d = sx*sx + sy*sy;
				if (bestCrossPoint==null || d<bestSqDist) {
					bestSqDist = d;
					bestCrossPoint = new CrossPoint(overlapPoint.point);
				}
			}			
		}
//		System.out.println("nx="+nx+", ny="+ny+", bestCrossPoint="+bestCrossPoint+", zoomFactor="+zoomFactor+" bestSqDist="+bestSqDist);
		
		if (bestCrossPoint!=null && bestSqDist<(400d*zoomFactor*zoomFactor)) {
			return new CrossPointSelection(bestCrossPoint);
		}

		
		TranslatedSegment bestTranslatedSegment = null;
		Line2D bestLine = null;
		double bestPointDistanceSq = 0d;
		boolean bestIsBase = true;
		
		Point2D mousePoint = new Point2D(Math.round(nx), Math.round(ny));
		for(MutableContour mutableContour : mutableContours) {
			for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
				
				Line2D baseLine = translatedSegment.base;
				double pointDistanceSq = baseLine.pointDistanceSq(mousePoint);
				if ((bestLine==null) || pointDistanceSq<bestPointDistanceSq) {
					bestTranslatedSegment = translatedSegment;
					bestLine = baseLine; 
					bestPointDistanceSq = pointDistanceSq;
					bestIsBase = true;
				}
				

				Line2D transLine = translatedSegment.translated;
				pointDistanceSq = transLine.pointDistanceSq(mousePoint);
				if ((bestLine==null) || pointDistanceSq<bestPointDistanceSq) {
					bestTranslatedSegment = translatedSegment;
					bestLine = transLine; 
					bestPointDistanceSq = pointDistanceSq;
					bestIsBase = false;
				}
			}
		}

		if (bestLine!=null && bestPointDistanceSq<(400d*zoomFactor*zoomFactor)) {
			return new TranslatedSegmentSelection(bestTranslatedSegment, bestLine, bestIsBase);
		}
		
		
		return null;
	}
	
	
	public interface Selection<T> {
		public T get();
	}
	
	public static class CrossPointSelection implements Selection<CrossPoint> {
		
		public final CrossPoint crossPoint;

		public CrossPointSelection(CrossPoint crossPoint) {
			this.crossPoint = crossPoint;
		}
		
		@Override
		public CrossPoint get() {
			return crossPoint;
		}
	}
	
	
	public static class TranslatedSegmentSelection implements Selection<Line2D> {
		
		public final TranslatedSegment translatedSegment;
		public final Line2D line;
		public final boolean isBase;

		public TranslatedSegmentSelection(TranslatedSegment translatedSegment, Line2D line, boolean isBase) {
			this.translatedSegment = translatedSegment;
			this.line = line;
			this.isBase = isBase;
		}
		
		@Override
		public Line2D get() {
			return line;
		}
	}
}