package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.github.douwevos.justflat.demo.Selection;
import net.github.douwevos.justflat.startstop.OnOffLine;
import net.github.douwevos.justflat.startstop.StartStopLine;
import net.github.douwevos.justflat.types.Layer;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;
import net.github.douwevos.justflat.types.values.StartStop;
import net.github.douwevos.justflat.types.values.Line2D.IntersectionInfo;

public class ContourLayer implements Layer, Iterable<Contour> {

	private final long width;
	private final long height;
	
	public List<Contour> contours = new ArrayList<>();
	
	
	public ContourLayer(long width, long height) {
		this.width = width;
		this.height = height;
	
	}
	
	@Override
	public long bottom() {
		return 0;
	}

	@Override
	public long top() {
		return height;
	}
	
	public long getWidth() {
		return width;
	}
	
	public long getHeight() {
		return height;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ContourLayer duplicate() {
		ContourLayer result = new ContourLayer(width, height);
		for(Contour contour : contours) {
			result.contours.add(contour.duplicate());
		}
		return result;
	}

	@Override
	public Bounds2D bounds() {
		Bounds2D b = null;
		for(Contour contour : contours) {
			Bounds2D bounds = contour.getBounds();
			if (b==null) {
				b = bounds;
			} else {
				b = b.union(bounds);
			}
			
		}
		return b != null ? b : new Bounds2D(0, 0, width, height);
	}

	@Override
	public boolean testDot(long x, long y, boolean defaultValue) {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return contours.isEmpty();
	}

	@Override
	public void merge(Layer layer) {
		
	}
	
	
	public List<StartStop> scanlineHorizontal(long y) {
		OnOffLine onOffLine = new OnOffLine();
		StartStopLine ll = new StartStopLine();
		for(Contour contour : contours) {
			onOffLine.reset();
			contour.scanlineHorizontal(y, onOffLine);
			List<StartStop> apply = onOffLine.apply();
			ll.invert(apply);
		}
		return Arrays.asList(ll.startStops);
		
//		return onOffLine.apply();
	}

	public List<StartStop> scanlineVertical(long x) {
		OnOffLine onOffLine = new OnOffLine();
		StartStopLine ll = new StartStopLine();
		for(Contour contour : contours) {
			onOffLine.reset();
			contour.scanlineVertical(x, onOffLine);
			List<StartStop> apply = onOffLine.apply();
			ll.invert(apply);
		}
		return Arrays.asList(ll.startStops);
		
//		return onOffLine.apply();
	}
	
	@Override
	public Iterator<Contour> iterator() {
		return contours.iterator();
	}

	public void add(Contour contour) {
		contours.add(contour);
	}
	
	public int count() {
		return contours.size();
	}
	
	public Contour getAt(int index) {
		return contours.get(index);
	}
	
	public Selection<?> selectAt(double nx, double ny, double zoomFactor) {
		Point2D bestDot = null;
		Contour bestContour = null;
		int bestDotIndex = -1;
		double bestSqDist = 0;
		
		for(Contour contour : contours) {
			int dotIndex=0;
			for(Point2D dot : contour) {
				double sx = dot.x-nx;
				double sy = dot.y-ny;
				double d = sx*sx + sy*sy;
				if (bestDot==null || d<bestSqDist) {
					bestSqDist = d;
					bestDot = dot;
					bestDotIndex = dotIndex;
					bestContour = contour;
				}
				dotIndex++;
			}
		}
//		log.debug("nx="+nx+", ny="+ny+", bestDot="+bestDot+", zoomFactor="+zoomFactor+" bestSqDist="+bestSqDist);
		
		if (bestDot!=null && bestSqDist<(400d*zoomFactor*zoomFactor)) {
			return new ContourDotSelection(bestContour, bestDotIndex, nx, ny);
		}

		bestDot = null;
		bestContour = null;
		bestDotIndex = -1;
		bestSqDist = 0;
		
		for(Contour contour : contours) {
			int dotIndex=0;
			Point2D lastDot = contour.getLast();
			for(Point2D dot : contour) {
				double ptSegDistSq = java.awt.geom.Line2D.ptSegDistSq(dot.x, dot.y, lastDot.x, lastDot.y, nx,ny);
				if (bestDot==null || ptSegDistSq<bestSqDist) {
					bestSqDist = ptSegDistSq;
					bestDot = dot;
					bestDotIndex = dotIndex-1;
					bestContour = contour;
				}
				dotIndex++;
				lastDot = dot;
			}
		}

		if (bestDot!=null && bestSqDist<(400d*zoomFactor*zoomFactor)) {
			LineSelection lineSelection = new LineSelection(bestContour, bestDotIndex);
			return enrichIntersectionPoint(lineSelection, contours, nx, ny, zoomFactor);
		}
		
		return null;
	}
	
	
	private Selection<?> enrichIntersectionPoint(LineSelection lineSelection, List<Contour> contours, double nx, double ny,
			double zoomFactor) {
		Line2D lineMain = lineSelection.getLine();
		
		IntersectionInfo info = new IntersectionInfo();
		Point2D bestIntersectionPoint = null;
		double bestDistSq = 0d;
		for(Contour contour : contours) {
			for(Line2D line : contour.createLines(false)) {
				if (line.equals(lineMain)) {
					continue;
				}
				Point2D intersectionPoint = line.intersectionPoint(lineMain, info);
				if (intersectionPoint != null && info.ua>=0d && info.ua<=1d) {
					intersectionPoint = lineMain.intersectionPoint(line, info);
					if (intersectionPoint != null && info.ua>=0d && info.ua<=1d) {
						double dx = intersectionPoint.x - nx;
						double dy = intersectionPoint.y - ny;
						double sqDist = dx*dx + dy*dy;
						if (bestIntersectionPoint==null || sqDist<bestDistSq) {
							bestIntersectionPoint = intersectionPoint;
							bestDistSq = sqDist;
						}
					}
				}
			}
		}
		
		if (bestIntersectionPoint!=null) {
			return new LineSelection(lineSelection.contour, lineSelection.dotIndex, bestIntersectionPoint);
		}
		
		return lineSelection;
	}

	public boolean dragTo(Object selected, double nx, double ny) {
		if (selected instanceof ContourDotSelection) {
			return dragSelection((ContourDotSelection) selected, nx, ny);
		}
		return false;
	}
	
	
	
	private boolean dragSelection(ContourDotSelection selected, double nx, double ny) {
		if (selected.dotIndex<0) {
			return false;
		}
		Point2D point2d = selected.contour.dotAt(selected.dotIndex);
		if (selected.startDot==null) {
			selected.startDot = point2d;
		} else {
			double dx = selected.grabX-selected.startDot.x;
			double dy = selected.grabY-selected.startDot.y;
			
			long newX = Math.round(nx-dx);
			long newY = Math.round(ny-dy);
			Point2D moved = Point2D.of(newX, newY);
			selected.contour.setDotAt(selected.dotIndex, moved);
			
		}
		return true;
	}



	public static class ContourDotSelection implements Selection<Point2D> {
		public final Contour contour;
		public final int dotIndex;
		public final double grabX;
		public final double grabY;
		public Point2D startDot;
		
		public ContourDotSelection(Contour contour, int dotIndex, double grabX, double grabY) {
			this.contour = contour;
			this.dotIndex = dotIndex;
			this.grabX = grabX;
			this.grabY = grabY;
		}

		public Point2D getDot() {
			return dotIndex<0 ? null : contour.dotAt(dotIndex);
		}
		
		@Override
		public Point2D get() {
			return startDot;
		}
	}

	public static class LineSelection implements Selection<Line2D> {
		public final Contour contour;
		public final int dotIndex;
		public final Line2D line;
		public final Point2D intersectionPoint;
		
		public LineSelection(Contour contour, int dotIndex) {
			this(contour, dotIndex, null);
		}

		public LineSelection(Contour contour, int dotIndex, Point2D intersectionPoint) {
			this.contour = contour;
			this.dotIndex = dotIndex;
			Point2D pa = contour.dotAt(dotIndex);
			Point2D pb = contour.dotAt(dotIndex+1);
			line = new Line2D(pa, pb);
			this.intersectionPoint = intersectionPoint;
		}

		public Line2D getLine() {
			return line;
		}
		
		public Point2D getDot() {
			return dotIndex<0 ? null : contour.dotAt(dotIndex);
		}
		
		public Point2D getIntersectionPoint() {
			return intersectionPoint;
		}
		
		@Override
		public Line2D get() {
			return line;
		}
	}

	public void moveDot(Point2D from, Point2D to) {
		
		for(int idx=0; idx<contours.size(); idx++) {
			Contour contour = contours.get(idx);
			Contour replacement = contour.moveDot(from, to);
			if (replacement != null) {
				contours.set(idx, replacement);
				return;
			}
		}
	}

}
