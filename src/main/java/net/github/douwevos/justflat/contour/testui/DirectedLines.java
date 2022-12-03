package net.github.douwevos.justflat.contour.testui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.startstop.StartStop;
import net.github.douwevos.justflat.types.Bounds2D;
import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Point2D;

public class DirectedLines implements Iterable<DirectedLine> {
	
	private List<DirectedLine> directedLines = new ArrayList<>();

	public DirectedLines(ContourLayer discLayer) {
		for(Contour contour : discLayer) {
			List<DirectedLine> directedContourLines = directContourLines(discLayer, contour);
			directedLines.addAll(directedContourLines);
		}
	}

	public DirectedLine lineAt(int index) {
		return directedLines.get(index);
	}
	
	public int lineCount() {
		return directedLines.size();
	}
	
	@Override
	public Iterator<DirectedLine> iterator() {
		return directedLines.iterator();
	}
	
	public Stream<DirectedLine> stream() {
		return directedLines.stream();
	}
	
	
	public Bounds2D bounds() {
		Bounds2D bounds = null;
		for(DirectedLine directedLine : directedLines) {
			Point2D pointA = directedLine.baseLine.pointA();
			Point2D pointB = directedLine.baseLine.pointA();
			if (bounds==null) {
				bounds = new Bounds2D(pointA.x, pointA.y, pointB.x, pointB.y);
			} else {
				bounds = bounds.extend(pointA.x, pointA.y);
				bounds = bounds.extend(pointB.x, pointB.y);
			}
		}
		return bounds;
	}
	
	private List<DirectedLine> directContourLines(ContourLayer discLayer, Contour contour) {
		List<Line2D> nonDirectedLines = contour.createLines(true);
		int lineCount = nonDirectedLines.size();

		
		
		ScanlineCache scanlineCacheVertical = new ScanlineCache(discLayer, false);
		ScanlineCache scanlineCacheHorizontal = new ScanlineCache(discLayer, true);
		
		Boolean directed[] = new Boolean[lineCount];
		int succesCount = 0;
		
		boolean lastWasInverted = false;
		
		List<DirectedLine> result = new ArrayList<>();
		for(int idx=0; idx<lineCount; idx++) {
			Line2D line = nonDirectedLines.get(idx);
			long lineYDiff = line.pointB().y-line.pointA().y;
			long lineXDiff = line.pointB().x-line.pointA().x;
			
			DirectedLine directedLine = null;
			if (lineXDiff*lineXDiff>lineYDiff*lineYDiff) {
				directedLine = directLineVertical(contour, scanlineCacheVertical, nonDirectedLines, idx);
			} else {
				directedLine = directLineHorizontal(contour, scanlineCacheHorizontal, nonDirectedLines, idx);
			}
			result.add(directedLine);
		}
		return result;

//		if (succesCount<lineCount) {
//			for(int idx=0; idx<lineCount; idx++) {
//				if (directed[idx] == null) {
//					Boolean prev = directed[(idx+lineCount-1)%lineCount];
//					Boolean next = directed[(idx+1)%lineCount];
//					if (((next==Boolean.TRUE) && (prev!=Boolean.FALSE)) 
//							|| ((prev==Boolean.TRUE) && (next!=Boolean.FALSE))){
//						Line line = nonDirectedLines.get(idx);
//						nonDirectedLines.set(idx, new Line(line.pointB, line.pointA));
//						directed[idx] = Boolean.TRUE;
//						succesCount++;
//					} else if (((next==Boolean.FALSE) && (prev!=Boolean.TRUE)) 
//							|| ((prev==Boolean.FALSE) && (next!=Boolean.TRUE))){
//						directed[idx] = Boolean.FALSE;
//						succesCount++;
//					}
//				}
//			}
//		}
		
//		if (accLeft<accRight) {
//			return nonDirectedLines.stream().map(l -> new DirectedLine(new Line(l.pointB, l.pointA))).collect(Collectors.toList());
//		}
//		return nonDirectedLines.stream().map(l -> new DirectedLine(l)).collect(Collectors.toList());
	}
	
	
	private DirectedLine directLineHorizontal(Contour contour, ScanlineCache scanlineCache, List<Line2D> nonDirectedLines, int idx) {
		Line2D line = nonDirectedLines.get(idx);
		
		List<StartStop> scanline = scanlineCache.scanline(line.pointA().y);
		StartStop startStop = findAtX(scanline, line.pointA().x);
		
		boolean isReverse = false;
		if (startStop!=null) {
			
			long left  = (line.pointA().x - startStop.start);
			long right = (startStop.stop - line.pointA().x);
			long lineYDiff = line.pointB().y-line.pointA().y;
			if (lineYDiff<0 || (lineYDiff==0 && line.pointB().x<line.pointA().x)) {
				long l = left;
				left = right;
				right = l;
			}
			if (left==right) {
//				if (lastWasInverted) {
//					nonDirectedLines.set(idx, new Line(line.pointB, line.pointA));
//				}
			} else if (left<right) {
//				nonDirectedLines.set(idx, new Line(line.pointB, line.pointA));
				isReverse = true;
			}
		}		
		return new DirectedLine(contour, line, isReverse);
	}

	private DirectedLine directLineVertical(Contour contour, ScanlineCache scanlineCache, List<Line2D> nonDirectedLines, int idx) {
		Line2D line = nonDirectedLines.get(idx);
		
		List<StartStop> scanline = scanlineCache.scanline(line.pointA().x);
		StartStop startStop = findAtX(scanline, line.pointA().y);
		
		boolean isReverse = false;
		if (startStop!=null) {
			
			long left  = (line.pointA().y - startStop.start);
			long right = (startStop.stop - line.pointA().y);
			long lineYDiff = line.pointB().x-line.pointA().x;
			if (lineYDiff<0 || (lineYDiff==0 && line.pointB().y<line.pointA().y)) {
				long l = left;
				left = right;
				right = l;
			}
			if (left==right) {
//				if (lastWasInverted) {
//					nonDirectedLines.set(idx, new Line(line.pointB, line.pointA));
//				}
			} else if (left>right) {
//				nonDirectedLines.set(idx, new Line(line.pointB, line.pointA));
				isReverse = true;
			}
		}		
		return new DirectedLine(contour, line, isReverse);
		
	}

	private StartStop findAtX(List<StartStop> scanline, long x) {
		return scanline.stream().filter(s -> x>=s.start && x<=s.stop).findAny().orElse(null);
	}
	
	static class ScanlineCache {
		
		private final ContourLayer discLayer;
		private final boolean horizontal;
		
		private Map<Long, List<StartStop>> map = new HashMap<>();
		
		public ScanlineCache(ContourLayer discLayer, boolean horizontal) {
			this.discLayer = discLayer;
			this.horizontal = horizontal;
		}
		
		public List<StartStop> scanline(long scanline) {
			Long key = Long.valueOf(scanline);
			List<StartStop> result = map.get(key);
			if (result == null) {
				result = horizontal ? discLayer.scanlineHorizontal(scanline) : discLayer.scanlineVertical(scanline);
				map.put(key, result);
			}
			return result;
		}
		
	}
	
}
