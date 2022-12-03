package net.github.douwevos.justflat.contour.scaler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Line2D.IntersectionInfo;
import net.github.douwevos.justflat.types.Point2D;
import net.github.douwevos.justflat.util.NoRepeats;


public class MutableContour {

	Log log = Log.instance();

	public final Contour source;

//	public List<MutableLine> lines;
	
	public List<TranslatedSegment> segements;

//	public List<TranslatedSegmentPart> segementsParts;
	
//	public MutableContour(Contour source, List<MutableLine> lines) {
//		this.source = source;
////		this.lines = new ArrayList<>(lines);
//	}

//	public Contour createContour() {
//		MutableLine firstLine = lines.get(0);
//		MutableLine secondLine = lines.get(1);
//		Point2D startPoint = firstLine.translated.getFirstPoint();
//		if (secondLine.translated.getFirstPoint().equals(startPoint)
//				|| secondLine.translated.getSecondPoint().equals(startPoint)) {
//			startPoint = firstLine.translated.getSecondPoint();
//		}
//
//		Point2D iterPoint = startPoint;
//		List<Point2D> dots = new ArrayList<>();
//		dots.add(iterPoint);
//
//		for (MutableLine mutableLine : lines) {
//			iterPoint = mutableLine.translated.getOtherPoint(iterPoint);
//			dots.add(iterPoint);
//		}
//
//		Contour c = new Contour();
//		Point2D last = dots.get(dots.size() - 1);
//		for (Point2D cur : dots) {
//			if (last.equals(cur)) {
//				continue;
//			}
//			c.add(cur);
//			last = cur;
//		}
//		log.debug2("dots={}", c.getDots());
//		return c;
//	}
//
//	public int dotCount() {
//		return lines.size();
//	}

	public MutableContour(Contour source, boolean reverse, double thickness) {
		this.source = source;
		ArrayList<Point2D> dots = new ArrayList<>(source.getDots());
		if (reverse) {
			Collections.reverse(dots);
			Point2D last = dots.remove(dots.size()-1);
			dots.add(0, last);
		}
		
		Point2D last = dots.get(dots.size()-1);
		for(int d=dots.size()-2; d>=0; d--) {
			Point2D next = dots.get(d);
			if (Objects.equals(next, last)) {
				dots.remove(d+1);
			}
			last = next;
		}
		
//		last = null;
//		lines = new ArrayList<>();
//		for(Point2D next : dots) {
//			if (last!=null && !last.equals(next)) {
//				lines.add(new MutableLine(last, next, thickness));
//			}
//			last = next;
//		}
//		Point2D next = dots.get(0);
//		if (last!=null && !last.equals(next)) {
//			lines.add(new MutableLine(last, next, thickness));
//		}
//		
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
		
		
		Point2D observePoint = new Point2D(29566l, 14821l);
		
//		Set<Point2D> segmentConnectPoints = new HashSet<>();

		IntersectionInfo info = new IntersectionInfo();
		for(int idx=0; idx<translatedLines.size(); idx++) {
			int nidx = (idx+1) % translatedLines.size();
			
			Line2D lineBase = baseLines.get(idx);
			boolean isObservePoint = lineBase.pointB().equals(observePoint);
			
			Line2D line = translatedLines.get(idx);
			Line2D nline = translatedLines.get(nidx);
			Point2D nextPoint = line.intersectionPoint(nline, info);
			
			
			
			if (info.ua>=1d) {
				translatedLines.set(idx, line.withSecondPoint(nextPoint));
				translatedLines.set(nidx, nline.withFirstPoint(nextPoint));
//				segmentConnectPoints.add(nextPoint);
			} else if (info.intersectionPoint!=null) {
				nextPoint = info.intersectionPoint;
				Point2D sp = line.getSecondPoint();
				Point2D fp = nline.getFirstPoint();
				nextPoint = Point2D.of((sp.x+fp.x)/2, (sp.y+fp.y)/2);
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
			
			if (isObservePoint) {
				log.debug2("%%%%%%%%%%%%%%%%%%%% info:{}, nextPoint:{}", info, nextPoint);
				
				log.debug2("%%%%%%%%%%%%%%%%%%%% line:{}", line);
				log.debug2("%%%%%%%%%%%%%%%%%%%% nline:{}", nline);
				
				Point2D crossPoint = line.crossPoint(nline, info);
				log.debug2("%%%%%%%%%%%%%%%%%%%% info:{}, crossPoint-A:{}", info, crossPoint);
				crossPoint = nline.crossPoint(line, info);
				log.debug2("%%%%%%%%%%%%%%%%%%%% info:{}, crossPoint-A:{}", info, crossPoint);
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

//	public Stream<MutableLine> streamLines() {
//		return lines.stream();
//	}
//	
//	@Override
//	public Iterator<MutableLine> iterator() {
//		return lines.iterator();
//	}
	
	public Iterable<TranslatedSegment> segmentIterable() {
		return segements;
	}

	public Stream<TranslatedSegment> streamSegments() {
		return segements.stream();
	}
	
	
//	public void setSegementsParts(List<TranslatedSegmentPart> segementsParts) {
//		this.segementsParts = segementsParts;
//	}
//	
//	
//	public List<TranslatedSegmentPart> getSegementsParts() {
//		return segementsParts;
//	}

	
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
