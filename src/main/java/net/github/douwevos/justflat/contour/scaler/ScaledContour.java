package net.github.douwevos.justflat.contour.scaler;

import java.util.ArrayList;
import java.util.List;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;

class ScaledContour {
	Log log = Log.instance(false);
	
	
	public final Contour source;
	
	List<TargetLine> lines;

	public ScaledContour(Contour source, List<TargetLine> lines) {
		this.source = source;
		this.lines = new ArrayList<>(lines);
	}

	
	public Contour createContour() {
		TargetLine firstLine = lines.get(0);
		TargetLine secondLine = lines.get(1);
		Point2D startPoint = firstLine.pointA();
		if (secondLine.pointA().equals(startPoint) || secondLine.pointB().equals(startPoint)) {
			startPoint = firstLine.pointB();
		}
		
		Point2D iterPoint = startPoint;
		List<Point2D> dots = new ArrayList<>();
		dots.add(iterPoint);
		
		for(TargetLine mutableLine : lines) {
			iterPoint = mutableLine.getOtherPoint(iterPoint);
			dots.add(iterPoint);
		}

		Contour c = new Contour();
		Point2D last = dots.get(dots.size()-1);
		for(Point2D cur : dots) {
			if (last.equals(cur)) {
				continue;
			}
			c.add(cur);
			last = cur;
		}
		log.debug("dots={}", c.getDots());
		return c;
	}


	public int lineCount() {
		return lines.size();
	}


	public void setIndex(Integer index) {
		source.setIndex(index);
	}

	public Integer getIndex() {
		return source.getIndex();
	}
	
	
	public Bounds2D getBounds() {
		Bounds2D result = null;
		for(TargetLine tl : lines) {
			if (result == null) {
				result = tl.asLine().bounds();
			} else {
				result = result.extend(tl.asLine());
			}
		}
		return result;
	}


//	
//	public void keepLinesInSameDirection() {
//		List<MutableLine> translatedLines = lines;
//		boolean keepLooping = true;
//		int lastCount = -1;
//		while(keepLooping && !translatedLines.isEmpty()) {
//			keepLooping = false;
//			MutableLine left = translatedLines.get(translatedLines.size()-1);
//			for(MutableLine right : translatedLines) {
//				left.reconnectTranslated(right);
//				left = right;
//			}
//			
////				log.debug("revaluate");
////				List<MutableLine> reduced = new ArrayList<>();
//			int count = 0;
//			for(MutableLine line : translatedLines) {
////					log.debug(" line ### "+line+"   "+line.sameDirection());
//				if (line.sameDirection()) {
//					count++;
//				}
//			}
//			keepLooping = (count!=lastCount) && count!=translatedLines.size();
//			lastCount = count;
//		}
////			log.debug("finished:"+translatedLines.size());
////			lines.clear();
////			lines.addAll(translatedLines);
//	}

	public boolean isEmpty() {
		return source.isEmpty();
	}
	
	public void dumpLines() {
		for(int idx=0; idx<lines.size(); idx++) {
			log.debug(" >> "+idx+" "+ lines.get(idx));
		}
	}


	public boolean isCCW() {
		TargetLine topLine = lines.stream()
				.reduce((a,b) -> {
			if (a==null) {
				return b;
			}
			if (a.pointA().x == a.pointB().x) {
				return b;
			}
			if (b.pointA().x == b.pointB().x) {
				return a;
			}
			
			long yA0 = a.pointA().y;
			long yA1 = a.pointB().y;
			long yB0 = b.pointA().y;
			long yB1 = b.pointB().y;
			
			if ((yA0<yB0 && yA0<yB1)
					|| (yA1<yB0 && yA1<yB1)) {
				return a;
			}
			return b;
		}).orElse(null);
		
		if (topLine == null) {
			return false;
		}
		
		Line2D line = topLine.asLine();
		Point2D pointA = line.pointA();
		Point2D pointB = line.pointB();
		
		boolean result = pointA.x<pointB.x;
		log.debug("ccw: {} result={}", line, result);
		
//		Point2D testPoint = new Point2D((pointA.x+pointB.x)/2, -5+(pointA.y+pointB.y)/2);
//		return line.relativeCCW(testPoint)<0;
		return result;
	}
}