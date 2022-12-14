package net.github.douwevos.justflat.contour.scaler;

import java.util.List;
import java.util.stream.Collectors;

import net.github.douwevos.justflat.Selection;
import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.ViewableModel;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;


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
				result = translatedSegment.base.base.bounds();
			} else {
				result = result.extend(translatedSegment.base.base);
			}
			result = result.extend(translatedSegment.translated.base);
		}
		
		return result;
	}
	
	@Override
	public Selection<?> selectAt(ModelMouseEvent modelMouseEvent) {
		OverlapPoint bestOverlapPoint = null;
		double bestSqDist = 0;
		
		double nx = modelMouseEvent.modelX;
		double ny = modelMouseEvent.modelY;
		double zoomFactor = modelMouseEvent.camera.getZoom();
		
		for(MutableContour mutableContour : mutableContours) {
			
			List<OverlapPoint> points = mutableContour.segements.stream().flatMap(s -> s.streamAllOverlapPoints()).collect(Collectors.toList());
			for (OverlapPoint overlapPoint : points) {
				
				Point2D dot = overlapPoint.point;
				double sx = dot.x-nx;
				double sy = dot.y-ny;
				double d = sx*sx + sy*sy;
				if (bestOverlapPoint==null || d<bestSqDist) {
					bestSqDist = d;
					bestOverlapPoint = overlapPoint;
				}
			}			
		}
//		log.debug("nx="+nx+", ny="+ny+", bestCrossPoint="+bestCrossPoint+", zoomFactor="+zoomFactor+" bestSqDist="+bestSqDist);
		
		if (bestOverlapPoint!=null && bestSqDist<(400d*zoomFactor*zoomFactor)) {
			return new OverlapPointSelection(bestOverlapPoint, 0d);
		}

		
		TranslatedSegment bestTranslatedSegment = null;
		Line2D bestLine = null;
		double bestPointDistanceSq = 0d;
		boolean bestIsBase = true;
		
		Point2D mousePoint = new Point2D(Math.round(nx), Math.round(ny));
		for(MutableContour mutableContour : mutableContours) {
			for(TranslatedSegment translatedSegment : mutableContour.segmentIterable()) {
				
				Line2D baseLine = translatedSegment.base.base;
				double pointDistanceSq = baseLine.pointDistanceSq(mousePoint);
				if ((bestLine==null) || pointDistanceSq<bestPointDistanceSq) {
					bestTranslatedSegment = translatedSegment;
					bestLine = baseLine; 
					bestPointDistanceSq = pointDistanceSq;
					bestIsBase = true;
				}
				

				Line2D transLine = translatedSegment.translated.base;
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
	
	
	public static class PointSelection implements Selection<Point2D> {
		
		public final Point2D point;

		public PointSelection(Point2D point) {
			this.point = point;
		}
		
		@Override
		public Point2D get() {
			return point;
		}
	}

	public static class OverlapPointSelection implements Selection<OverlapPoint> {
		
		public final OverlapPoint overlapPoint;
		public final double alpha;

		public OverlapPointSelection(OverlapPoint overlapPoint, double alpha) {
			this.overlapPoint = overlapPoint;
			this.alpha = alpha;
		}
		
		@Override
		public OverlapPoint get() {
			return overlapPoint;
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