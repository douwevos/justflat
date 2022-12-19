//package net.github.douwevos.justflat.contour;
//
//import net.github.douwevos.justflat.contour.ContourLayerMap.OverlapInfo;
//import net.github.douwevos.justflat.startstop.OnOffLine;
//import net.github.douwevos.justflat.types.values.Bounds2D;
//import net.github.douwevos.justflat.types.values.StartStopLine;
//
//public class ContourRelation {
//
//
//	public Relation calculate(Contour contourA, Contour contourB) {
//		Bounds2D boundsA = contourA.getBounds();
//		Bounds2D boundsB = contourB.getBounds();
//		if (!boundsA.intersect(boundsB)) {
//			return Relation.NO_RELATION;
//		}
//		
//		OnOffLine onOffLineA = new OnOffLine();
//		OnOffLine onOffLineB = new OnOffLine();
//		
//		long midA = (boundsA.bottom +  boundsA.top)/2;
//		if (midA>=boundsB.bottom && midA<=boundsB.top) {
//			OverlapInfo overlapInfo = calculateOrderByScanline(midA, contourA, onOffLineA, contourB, onOffLineB);
//			if (overlapInfo != OverlapInfo.NO_OVERLAP) {
//				return overlapInfo;
//			}
//		}
//
//		long midB = (boundsB.bottom +  boundsB.top)/2;
//		if (midB>=boundsA.bottom && midB<=boundsA.top) {
//			OverlapInfo overlapInfo = calculateOrderByScanline(midB, contourA, onOffLineA, contourB, onOffLineB);
//			if (overlapInfo != OverlapInfo.NO_OVERLAP) {
//				return overlapInfo;
//			}
//		}
//		
//		long bottom = boundsA.bottom>boundsB.bottom ? boundsA.bottom : boundsB.bottom;
//		long top = boundsA.top<boundsB.top ? boundsA.top : boundsB.top;
//
//		for(long y=bottom; y<=top; y++) {
//			OverlapInfo overlapInfo = calculateOrderByScanline(midB, contourA, onOffLineA, contourB, onOffLineB);
//			if (overlapInfo != OverlapInfo.NO_OVERLAP) {
//				return overlapInfo;
//			}
//		}
//		
//		return OverlapInfo.NO_OVERLAP;
//	}
//
//	private Relation calculateByScanline(long y, Contour contourA, OnOffLine onOffLineA, Contour contourB, OnOffLine onOffLineB) {
//		onOffLineA.reset();
//		contourA.scanlineHorizontal(y, onOffLineA);
//		if (onOffLineA.isEmpty()) {
//			return Relation.NO_RELATION;
//		}
//
//		onOffLineB.reset();
//		contourB.scanlineHorizontal(y, onOffLineB);
//		if (onOffLineB.isEmpty()) {
//			return Relation.NO_RELATION;
//		}
//
//		StartStopLine startStopLineA = new StartStopLine(onOffLineA.apply());
//		StartStopLine startStopLineB = new StartStopLine(onOffLineB.apply());
//		
//		if (startStopLineA.equals(startStopLineB)) {
//			return Relation.SAME;
//		}
//		
//		if (startStopLineA.fullyCovers(startStopLineB)) {
//			return Relation.A_COVERS_B;
//		}
//
//		if (startStopLineB.fullyCovers(startStopLineA)) {
//			return Relation.B_COVERS_A;
//		}
//		
//		
//		
//		
//		
//
//		return OverlapInfo.NO_OVERLAP;
//	}
//	
//	public enum Relation {
//		NO_RELATION,
//		A_COVERS_B,
//		B_COVERS_A,
//		CUT_EACHOTHER,
//		SAME
//	}
//}
