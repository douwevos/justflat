package net.github.douwevos.justflat.contour;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.values.Point2D;

public class ContourLayerResolutionReducer {

	Log log = Log.instance(false);
	
	public ContourLayer reduceResolution(ContourLayer input, double distortionLevel, int stepping) {
		ContourLayer result = new ContourLayer(input.getWidth(), input.getHeight());
		for(Contour contour : input) {
			log.debug("dots.in="+contour.getDots().size());
			Contour contourReduced = reduceAndCreatePolyline(contour, contour.getDots(), distortionLevel, stepping);
			if (contourReduced!=null) {
				result.add(contourReduced);
				log.debug("dots.out="+contourReduced.getDots().size());
			}
		}

		return result;
	}

	private Contour reduceAndCreatePolyline(Contour mainPolyline, List<Point2D> pointList, double distortionLevel,
			int stepping) {
		
		List<Point2D> cleanedUp = cleanupList(pointList);
		
		ArrayList<Point2D> reducedList = reducePointList(cleanedUp, distortionLevel, stepping);
		Contour reducedPolyline = new Contour();
		if (reducedList != null) {
			Iterator<Point2D> pointIter = reducedList.iterator();
			while (pointIter.hasNext()) {
				Point2D p = pointIter.next();
				reducedPolyline.add(p);
			}
		} else {
			cleanedUp.forEach(reducedPolyline::add);
		}
		return reducedPolyline;
	}

	
	private List<Point2D> cleanupList(List<Point2D> input) {
		List<Point2D> result = new ArrayList<Point2D>(input.size());
		Point2D l = null;
		for(Point2D p : input) {
			if (!Objects.equals(p, l)) {
				result.add(p);
			}
			l = p;
		}
		return result;
	}


	private static final int MIN_POINTS_TO_REDUCE = 5; // do absolutely not set smaller then 1

	private ArrayList<Point2D> reducePointList(List<Point2D> pointList, double distortionLevel,
			int stepping) {
		int pointListSize = pointList.size();
		if (log.isDebugEnabled())
			log.debug("pointList.size=" + pointListSize);

		if (pointListSize <= MIN_POINTS_TO_REDUCE) {
			return null;
		}

		if (log.isDebugEnabled())
			log.debug("pntCnt=" + pointListSize);

		if (stepping >= pointListSize) { // if there are just to less points (i.e. a rectangle build up out of 4 points)
			stepping = pointListSize / 2;
		}

		if (stepping < 1) {
			stepping = 1;
		}

		ArrayList<Point2D> newList = new ArrayList<Point2D>();

		int lastOffset = 0;

		/*
		 * The offset of the first point in the pointList which has been/is being copied
		 * in the newList. The value -1 represents no value has been copied yet.
		 *
		 */
		int firstOffset = -1;

		boolean cont = true;
		while (cont) {

			/*
			 * calculate the next absolute maximum pointList-index where the the distortion
			 * lays within the distortionLevel.
			 */
			int nextAbsOffset = findNextOffset(pointList, lastOffset, distortionLevel, stepping);

			/*
			 * Calculate the offset of the 'break' point. The break point is the point that
			 * lays the farthest from the line that lays between the lastOffset and the
			 * nextAbsOffset. This break point will be the next point that is stored in the
			 * newList. The break point calculation has as advantage that edges in the shape
			 * stay intact.
			 */
			int nextOffset = calcDistortionMax(pointList, lastOffset, nextAbsOffset);

			if (log.isDebugEnabled())
				log.debug("lastOffset=" + lastOffset + ", nextAbsOffset=" + nextAbsOffset + ", nextOffset=" + nextOffset
						+ ", firstOffset=" + firstOffset);

			/*
			 * If the firstOffset is -1 it means the newList is empty and we can thus not be
			 * finished yet. Thus we copy the point at nextOffset and presume the nextOffset
			 * to be the firstOffset. When the firstOffset is not set to -1 we need to see
			 * if nextAbsOffset represents a point that (running from lastOffset to
			 * nextAbsOffset) passed the firstOffset. If it represents such a point then we
			 * should look if it is necessary to store the break point at nextOffset and
			 * eventually break the while loop.
			 *
			 */
			if (firstOffset == -1) {
				firstOffset = nextOffset;
				newList.add(pointList.get(nextOffset % pointListSize));
			} else {
				if (nextOffset >= firstOffset + pointListSize) {
					cont = false;
				} else {
					newList.add(pointList.get(nextOffset % pointListSize));
				}
			}

			// newList.add(pointList.get(nextOffset % pointList.size()));

			lastOffset = nextOffset;

		}
		return newList;
	}

	

	private int findNextOffset(List<Point2D> pointList, int base, double distortionLevel, int stepping) {
		int pntCnt = pointList.size();
		stepping = stepping < pntCnt ? stepping : pntCnt;
		int offset = stepping;
		boolean cont = true;
		double distortion;
		while (cont) {
			distortion = calcDistortion(pointList, base, base + offset);
			if (distortion < distortionLevel) {
				offset += stepping;
			} else {
				if (stepping == 1) {
					offset--;
					break;
				} else {
					offset -= stepping;
					stepping = 1;
				}
			}
			if (offset > pntCnt) {
				cont = false;
			} else {
				offset += stepping;
			}
		}
		return base + offset;
	}
	
	private java.awt.geom.Point2D.Double asGeomPoint(Point2D p) {
		return new java.awt.geom.Point2D.Double(p.x, p.y);
	}
	
	private double calcDistortion(List<Point2D> pointList, int lineStart, int lineEnd) {
		if (log.isDebugEnabled())
			log.debug("calcDistortion:pointList=" + pointList.size() + ",lineEnd-lineStart=" + (lineEnd - lineStart) + ",lineStart="
					+ lineStart);
		int pntCnt = pointList.size();
		java.awt.geom.Point2D.Double pc;
		Point2D pra = pointList.get(lineStart % pntCnt);
		Point2D prb = pointList.get(lineEnd % pntCnt);

		Line2D.Double line = new Line2D.Double(asGeomPoint(pra), asGeomPoint(prb));

		double totalRight = 0d;
		double totalLeft = 0d;
		double dist;
		int idx;

		for (idx = lineStart; idx < lineEnd; idx++) {
			Point2D prc = pointList.get(idx % pntCnt);
			pc = asGeomPoint(prc);

			dist = line.ptLineDistSq(pc);
			if (line.relativeCCW(pc) == 1.0d) {
				totalRight += dist;
			} else {
				totalLeft += dist;
			}

		}

		double total = Math.abs((totalLeft - totalRight));

		if (log.isDebugEnabled())
			log.debug("calcDistortion:Total=" + total + ",Left=" + totalLeft + ",Right=" + totalRight + ",lineEnd-lineStart="
					+ (lineEnd - lineStart) + ",lineStart=" + lineStart);
		return total;
	}

	
	private int calcDistortionMax(List<Point2D> pointList, int lineStart, int lineEnd) {
		if (log.isDebugEnabled())
			log.debug("calcDistortionMax:pointList=" + pointList.size() + ",lineStart=" + lineStart + ", lineEnd-lineStart="
					+ (lineEnd - lineStart));
		java.awt.geom.Point2D.Double pc;
		Point2D pa = pointList.get(lineStart % pointList.size());
		Point2D pb = pointList.get(lineEnd % pointList.size());

		if (log.isDebugEnabled())
			log.debug("calcDistortionMax:pnt[" + (lineStart) + "]=" + pa);
		if (log.isDebugEnabled())
			log.debug("calcDistortionMax:pnt[" + (lineEnd) + "]=" + pb);

		Line2D.Double line = new Line2D.Double(asGeomPoint(pa), asGeomPoint(pb));

		double dist;
		int idx;

		double maxDist = -1.0d;

		int result = lineEnd;

		for (idx = lineStart + 1; idx <= lineEnd; idx++) {
			Point2D prc = pointList.get(idx % pointList.size());
			pc = asGeomPoint(prc);

			dist = line.ptLineDistSq(pc);
			if (log.isDebugEnabled())
				log.debug("pnt[" + idx + "] = " + pc + ",lineStart=" + lineStart + ", dist=" + dist);
			if (dist >= maxDist) {
				maxDist = dist;
				result = idx;
			}
		}

		if (log.isDebugEnabled())
			log.debug("result=" + result);
		return result;
	}
	
}
