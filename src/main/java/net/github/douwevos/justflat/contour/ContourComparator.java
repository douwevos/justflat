package net.github.douwevos.justflat.contour;

import java.util.List;
import java.util.Objects;

import net.github.douwevos.justflat.startstop.OnOffLine;
import net.github.douwevos.justflat.types.values.Point2D;
import net.github.douwevos.justflat.types.values.StartStop;

public class ContourComparator {

	
	public boolean equal(ContourLayer layerA, ContourLayer layerB) {
		if (layerA==layerB) {
			return true;
		}
		if (layerA==null || layerB==null) {
			return false;
		}
		
		if (layerA.count()!=layerB.count()) {
			return false;
		}
		
		for(Contour contourA : layerA) {
			Contour contourB = findMatchingContour(layerB, contourA);
			if (contourB == null) {
				return false;
			}
		}
		
		
		return true;
	}

	private Contour findMatchingContour(ContourLayer layerB, Contour contourA) {
		for(Contour contourB : layerB) {
			if (equal(contourA, contourB)) {
				return contourB;
			}
		}
		return null;
	}

	private boolean equal(Contour contourA, Contour contourB) {
		if (contourA == contourB) {
			return true;
		}
		if (contourA==null ||  contourB==null) {
			return false;
		}
		if (contourA.dotCount()!=contourB.dotCount()) {
			return false;
		}
		
		for(Point2D pA : contourA) {
			if (contourB.streamDots().noneMatch(pB -> Objects.equals(pA, pB))) {
				return false;
			}
		}
		
		return true;
	}

	public boolean aContainsB(Contour mainContour, Contour cutContour) {
		return cutContour.streamDots().allMatch(dot -> contourContainsDot(mainContour, dot));
	}

	
	private boolean contourContainsDot(Contour mainContour, Point2D dot) {
		OnOffLine onOffLine = new OnOffLine(); 
		mainContour.scanlineHorizontal(dot.y, onOffLine);
		List<StartStop> ssList = onOffLine.apply();
		if (ssList==null) {
			return false;
		}
		return ssList.stream().anyMatch(ss -> ss.contains(dot.x) || ss.widen(2).contains(dot.x));
	}
	
}
