package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import net.github.douwevos.justflat.startstop.OnOffLine;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;

public class Contour implements Iterable<Point2D> {

	private List<Point2D> dots = new ArrayList<>();
	
	private boolean closed = true;
	
	private Bounds2D bounds;
	
	private Integer index;
	

	public Contour moveDot(Point2D from, Point2D to) {
		for(int idx=0; idx<dots.size(); idx++) {
			Point2D point2d = dots.get(idx);
			if (point2d.equals(from)) {
				Contour duplicate = duplicate();
				duplicate.dots.set(idx, to);
				duplicate.refreshBounds();
				return duplicate;
			}
		}
		return null;
	}


	
	public Contour duplicate() {
		Contour result = new Contour();
		result.dots.addAll(dots);
		result.bounds = bounds;
		result.index = index;
		return result;
	}

	public void add(Point2D dot) {
		dots.add(dot);
		if (bounds==null) {
			bounds = new Bounds2D(dot.x, dot.y, dot.x, dot.y);
		} else {
			bounds = bounds.extend(dot.x, dot.y);
		}
	}

	public void addAt(Point2D dot, int dotIndex) {
		dots.add(dotIndex, dot);
		if (bounds==null) {
			bounds = new Bounds2D(dot.x, dot.y, dot.x, dot.y);
		} else {
			bounds = bounds.extend(dot.x, dot.y);
		}
		
	}

	
	public void removeAt(int dotIndex) {
		dots.remove(dotIndex);
		refreshBounds();
		
	}

	
	public void addAll(List<Point2D> collectedPoints) {
		dots.addAll(collectedPoints);
		refreshBounds();
	}

	
	private void refreshBounds() {
		if (dots.isEmpty()) {
			bounds = null;
			return;
		}
		Point2D dot = dots.get(0);
		Bounds2D newBounds = new Bounds2D(dot.x, dot.y, dot.x, dot.y);
		for(Point2D d : dots) {
			newBounds = newBounds.extend(d.x, d.y);
		}
		this.bounds = newBounds;
		
	}
	
	public Bounds2D getBounds() {
		return bounds;
	}

	public int dotCount() {
		return dots.size();
	}
	
	public Point2D dotAt(int index) {
		int dotCount = dots.size();
		while(index<0) {
			index += dotCount*100;
		}
		index = index%dotCount;
		return dots.get(index);
	}

	public void setDotAt(int dotIndex, Point2D moved) {
		dots.set(dotIndex, moved);
		refreshBounds();
	}

	
	public boolean isEmpty() {
		return dots.isEmpty();
	}
	
	
	@Override
	public Iterator<Point2D> iterator() {
		return dots.iterator();
	}


	public Point2D getLast() {
		return dots.isEmpty() ? null : dots.get(dots.size()-1);
	}
	
	public Stream<Point2D> streamDots() {
		return dots.stream();
	}
	
	public List<Point2D> getDots() {
		return dots;
	}

	public Integer getIndex() {
		return index;
	}
	
	public void setIndex(Integer index) {
		this.index = index;
	}

	public void reverse() {
		Collections.reverse(dots);
		
	}

	public List<Line2D> createLines(boolean noZeroLength) {
		ArrayList<Line2D> result = new ArrayList<>();
		Point2D pa = getLast();
		for(Point2D pb : dots) {
			if (noZeroLength && pb.equals(pa)) {
				continue;
			}
			result.add(new Line2D(pa, pb));
			pa = pb;
		}
		return result;
	}


	public boolean isClosed() {
		return closed;
	}
	
	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public void scanlineHorizontal(long y, OnOffLine onOffLine) {
		Point2D pa = getLast();
		for(Point2D pb : dots) {
			if ((pa.y<y &&  pb.y<y) ||
					(pa.y>y &&  pb.y>y)) {
				pa = pb;
				continue;
			}
			if (pa.y==pb.y) {
				pa = pb;
				continue;
			}

			long deltaYBA = pb.y - pa.y;
			boolean up = true;
			if (deltaYBA<0) {
				up = false;
			}

			long deltaXBA = pb.x - pa.x;
			long deltaYYA = y - pa.y;
			long nx = pa.x + (deltaXBA*deltaYYA)/deltaYBA;
			onOffLine.add(nx, up);

			pa = pb;
		}
	}

	public void scanlineVertical(long x, OnOffLine onOffLine) {
		Point2D pa = getLast();
		for(Point2D pb : dots) {
			if ((pa.x<x &&  pb.x<x) ||
					(pa.x>x &&  pb.x>x)) {
				pa = pb;
				continue;
			}
			if (pa.x==pb.x) {
				pa = pb;
				continue;
			}

			long deltaXBA = pb.x - pa.x;
			boolean up = true;
			if (deltaXBA<0) {
				up = false;
			}

			long deltaYBA = pb.y - pa.y;
			long deltaXXA = x - pa.x;
			long ny = pa.y + (deltaYBA*deltaXXA)/deltaXBA;
			onOffLine.add(ny, up);

			pa = pb;
		}
	}







//	public void scanLineDouble(long y, OnOffLineDouble onOffLine) {
//		Point2D pa = getLast();
//		for(Point2D pb : dots) {
//			if ((pa.y<y &&  pb.y<y) ||
//					(pa.y>y &&  pb.y>y)) {
//				pa = pb;
//				continue;
//			}
//			if (pa.y==pb.y) {
//				pa = pb;
//				continue;
//			}
//
//			long deltaYBA = pb.y - pa.y;
//			boolean up = true;
//			if (deltaYBA<0) {
//				up = false;
//			}
//
//			long deltaXBA = pb.x - pa.x;
//			long deltaYYA = y - pa.y;
//			double nx = pa.x + (deltaXBA*deltaYYA)/ (double) deltaYBA;
//			onOffLine.add(nx, up);
//			pa = pb;
//		}
//	}

//	
//	public void scanLineOld(long y, OnOffLine onOffLine) {
//		Point2D pa = getLast();
//		for(Point2D pb : dots) {
//			if ((pa.y<y &&  pb.y<y) ||
//					(pa.y>y &&  pb.y>y)) {
//				pa = pb;
//				continue;
//			}
//			if (pa.y==pb.y) {
//				pa = pb;
//				continue;
//			}
//
//			long deltaYBA = pb.y - pa.y;
//			boolean up = true;
//			if (deltaYBA<0) {
//				up = false;
//			}
//
//			long deltaXBA = pb.x - pa.x;
//			long deltaYYA = y - pa.y;
//			long nx = pa.x + (deltaYBA/2 +  deltaXBA*deltaYYA)/deltaYBA;
//			onOffLine.add(nx, up);
//
//			pa = pb;
//		}
//	}

}
