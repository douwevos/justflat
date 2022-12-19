package net.github.douwevos.justflat.contour.scaler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;
import net.github.douwevos.justflat.types.values.Line2D.IntersectionInfo;
import net.github.douwevos.justflat.util.NoRepeats;


public class MutableContour {

	Log log = Log.instance(false);

	public final Contour source;
	public final boolean reverse;

	public List<TranslatedSegment> segements;

	public MutableContour(Contour source, boolean reverse, double thickness) {
		this.reverse = reverse;
		this.source = source;
//		ArrayList<Point2D> dots = new ArrayList<>(source.getDots());
//		if (reverse) {
//			Collections.reverse(dots);
//			Point2D last = dots.remove(dots.size()-1);
//			dots.add(0, last);
//		}
//		
//		Point2D last = dots.get(dots.size()-1);
//		for(int d=dots.size()-2; d>=0; d--) {
//			Point2D next = dots.get(d);
//			if (Objects.equals(next, last)) {
//				dots.remove(d+1);
//			}
//			last = next;
//		}
//		
		List<Point2D> filteredPoints = source.getDots().stream().filter(NoRepeats.filter()).collect(Collectors.toList());
		if (reverse) {
			Collections.reverse(filteredPoints);
		}
		int pointCount = filteredPoints.size();
		List<Line2D> baseLines = new ArrayList<>();
		Point2D a = filteredPoints.get(0);
		for(int idx=0; idx<pointCount; idx++) {
			Point2D b = filteredPoints.get((idx+1) % pointCount);
			baseLines.add(new Line2D(a, b));
			a = b;
		}
		
		
		List<Line2D> translatedLines = new ArrayList<>();
		baseLines.stream().map(l -> translate(l, thickness)).forEach(translatedLines::add);
		
		
//		Set<Point2D> segmentConnectPoints = new HashSet<>();

		IntersectionInfo info = new IntersectionInfo();
		for(int idx=0; idx<translatedLines.size(); idx++) {
			int nidx = (idx+1) % translatedLines.size();
			
			Line2D line = translatedLines.get(idx);
			Line2D nline = translatedLines.get(nidx);
			Point2D nextPoint = line.intersectionPoint(nline, info);
			
			
			
			if (info.ua>=1d) {
				translatedLines.set(idx, line.withSecondPoint(nextPoint));
				translatedLines.set(nidx, nline.withFirstPoint(nextPoint));
//				segmentConnectPoints.add(nextPoint);
			} else if (info.intersectionPoint!=null) {
				nextPoint = info.intersectionPoint;
//				Point2D sp = line.getSecondPoint();
//				Point2D fp = nline.getFirstPoint();
//				nextPoint = Point2D.of((sp.x+fp.x)/2, (sp.y+fp.y)/2);
//				translatedLines.set(idx, line.withSecondPoint(nextPoint));
//				translatedLines.set(nidx, nline.withFirstPoint(nextPoint));
//				long squaredDistance = line.getSecondPoint().squaredDistance(nline.getFirstPoint());
//				long sqDist1 = info.intersectionPoint.squaredDistance(line.getSecondPoint());
//				long sqDist2 = info.intersectionPoint.squaredDistance(nline.getFirstPoint());
//				if (squaredDistance<sqDist1 || squaredDistance<sqDist2) {
//					translatedLines.set(idx, line.withSecondPoint(nextPoint));
//					translatedLines.set(nidx, nline.withFirstPoint(nextPoint));
//				}
			}
		}
		
		boolean isShrinking = thickness<0d;

		List<TranslatedSegment> segments = new ArrayList<>();
		for(int idx=0; idx<translatedLines.size(); idx++) {
			Line2D baseLine = baseLines.get(idx);
			Line2D transLine = translatedLines.get(idx);
			TranslatedSegment segment = new TranslatedSegment(baseLine, transLine, isShrinking);
			segments.add(segment);
		}
		this.segements = segments;
		
	}

	public TranslatedSegment lastSegement() {
		if (segements.isEmpty()) {
			return null;
		}
		return segements.get(segements.size()-1);
	}


	private Line2D translate(Line2D base, double thickness) {
		Point2D pa = base.pointA();
		Point2D pb = base.pointB();
		long dx = pb.x - pa.x;
		long dy = pb.y - pa.y;
		
		double lineLength = Math.sqrt(dx*dx + dy*dy);
		
		double transY = (dy*thickness)/lineLength;
		double transX = (dx*thickness)/lineLength;
		
		long xa = Math.round((pa.x - transY));
		long ya = Math.round((pa.y + transX));

		Point2D pointA = new Point2D(xa, ya);
		Point2D pointB = new Point2D(pointA.x+dx, pointA.y+dy);
		
		return new Line2D(pointA, pointB);
	}

	public void setIndex(Integer index) {
		source.setIndex(index);
	}

	public Integer getIndex() {
		return source.getIndex();
	}
	
	public Iterable<TranslatedSegment> segmentIterable() {
		return segements;
	}

	public Stream<TranslatedSegment> streamSegments() {
		return segements.stream();
	}
	
	
	public boolean isEmpty() {
		return source.isEmpty();
	}

	public void dumpLines() {
//		for (int idx = 0; idx < lines.size(); idx++) {
//			log.debug2(" >> " + idx + " " + lines.get(idx));
//		}
	}

	public Contour getSource() {
		return source;
	}

}
