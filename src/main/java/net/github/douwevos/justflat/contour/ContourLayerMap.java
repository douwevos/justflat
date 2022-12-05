package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.List;

import net.github.douwevos.justflat.startstop.OnOffLine;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.types.values.StartStopLine;

public class ContourLayerMap {

	public final ContourLayer layer;
	
	private OrderedContour orderedContour;
	
	public ContourLayerMap(ContourLayer layer) {
		this.layer = layer;
	}
	
	public void rebuild() {
		orderedContour = new OrderedContour(null);
		for(Contour contour : layer.contours) {
			sortContour(contour);
		}
	}

	private void sortContour(Contour contour) {
		List<OrderedContour> stack = new ArrayList<>();
		stack.add(orderedContour);

		OrderedContour insert = new OrderedContour(contour);
		
		
		s:
		while(!stack.isEmpty()) {
			OrderedContour parent = stack.get(stack.size()-1);
			for(int idx=parent.children.size()-1; idx>=0; idx--) {
				OrderedContour child = parent.children.get(idx);
				OverlapInfo overlapInfo = calculateOrder(child.contour, contour);
				if (overlapInfo == OverlapInfo.A_OVER_B ) {
					stack.add(child);
					continue s;
				}
				if (overlapInfo == OverlapInfo.B_OVER_A) {
					parent.children.remove(idx);
					insert.children.add(child);
				}
			}
			parent.children.add(insert);
			return;
		}
		
	}
	
	
	
	private OverlapInfo calculateOrder(Contour contourA, Contour contourB) {
		Bounds2D boundsA = contourA.getBounds();
		Bounds2D boundsB = contourB.getBounds();
		boolean encloses = boundsA.encloses(boundsB) || boundsB.encloses(boundsA);
		if (!encloses) {
			return OverlapInfo.NO_OVERLAP;
		}
		
		OnOffLine onOffLineA = new OnOffLine();
		OnOffLine onOffLineB = new OnOffLine();
		
		long midA = (boundsA.bottom +  boundsA.top);
		if (midA>=boundsB.bottom && midA<=boundsB.top) {
			OverlapInfo overlapInfo = calculateOrderByScanline(midA, contourA, onOffLineA, contourB, onOffLineB);
			if (overlapInfo != OverlapInfo.NO_OVERLAP) {
				return overlapInfo;
			}
		}

		long midB = (boundsB.bottom +  boundsB.top);
		if (midB>=boundsA.bottom && midB<=boundsA.top) {
			OverlapInfo overlapInfo = calculateOrderByScanline(midB, contourA, onOffLineA, contourB, onOffLineB);
			if (overlapInfo != OverlapInfo.NO_OVERLAP) {
				return overlapInfo;
			}
		}
		
		long bottom = boundsA.bottom>boundsB.bottom ? boundsA.bottom : boundsB.bottom;
		long top = boundsA.top<boundsB.top ? boundsA.top : boundsB.top;

		for(long y=bottom; y<=top; y++) {
			OverlapInfo overlapInfo = calculateOrderByScanline(midB, contourA, onOffLineA, contourB, onOffLineB);
			if (overlapInfo != OverlapInfo.NO_OVERLAP) {
				return overlapInfo;
			}
		}
		
		return OverlapInfo.NO_OVERLAP;
	}

	private OverlapInfo calculateOrderByScanline(long y, Contour contourA, OnOffLine onOffLineA, Contour contourB, OnOffLine onOffLineB) {
		onOffLineA.reset();
		contourA.scanlineHorizontal(y, onOffLineA);
		if (onOffLineA.isEmpty()) {
			return OverlapInfo.NO_OVERLAP;
		}

		onOffLineB.reset();
		contourB.scanlineHorizontal(y, onOffLineB);
		if (onOffLineB.isEmpty()) {
			return OverlapInfo.NO_OVERLAP;
		}

		StartStopLine startStopLineA = new StartStopLine(onOffLineA.apply());
		StartStopLine startStopLineB = new StartStopLine(onOffLineB.apply());
		
		if (startStopLineA.equals(startStopLineB)) {
			return OverlapInfo.NO_OVERLAP;
		}
		
		if (startStopLineA.fullyCovers(startStopLineB)) {
			return OverlapInfo.A_OVER_B;
		}

		if (startStopLineB.fullyCovers(startStopLineA)) {
			return OverlapInfo.B_OVER_A;
		}

		return OverlapInfo.NO_OVERLAP;
	}

	static enum OverlapInfo {
		NO_OVERLAP,
		A_OVER_B,
		B_OVER_A
	}
	
	
	public OrderedContour getRootOrderedContour() {
		return orderedContour;
	}

	public static class OrderedContour {
		public final List<OrderedContour> children = new ArrayList<>();
		public final Contour contour;
		
		public OrderedContour(Contour contour) {
			this.contour = contour;
		}
		
	}
}
