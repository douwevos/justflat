package net.github.douwevos.justflat.contour.scaler;

import java.util.ArrayList;
import java.util.List;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourComparator;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.ContourLayerMap;
import net.github.douwevos.justflat.contour.ContourLayerMap.OverlapInfo;
import net.github.douwevos.justflat.contour.ContourLayerOverlapCutter;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;

public class RouteListScaledDownCombinator {

	private List<RouteList> mainRouteLists;
	private List<RouteList> subsRouteLists;
	
	private List<RouteList> combined;
	
	private RouteListInteractionAnalyser analyser;
	
	public RouteListScaledDownCombinator(List<RouteList> mainRouteLists, List<RouteList> subsRouteLists) {
		this.mainRouteLists = mainRouteLists;
		this.subsRouteLists = subsRouteLists;
	}

	public List<RouteList> rebuiltRouteLists() {
		combined  = combineInflated(subsRouteLists);
		
//		ArrayList<RouteList> result = new ArrayList<>();
//		for(RouteList mainRoute : mainRouteLists) {
//			cutByMain(result, mainRoute, combined);
//		}
//		
//		
		ArrayList<RouteList> result = new ArrayList<>(combined);
		result.addAll(mainRouteLists);
		return result;
		
	}

	private void cutByMain(ArrayList<RouteList> output, RouteList mainRoute, List<RouteList> combinedInnerRouteLists) {
		if (combinedInnerRouteLists==null || combinedInnerRouteLists.isEmpty()) {
			output.add(mainRoute);
			return;
		}
		
		RouteListMapper routeListMapper = new RouteListMapper();
		Contour mainContour = routeListMapper.routeListToScaledContour(mainRoute);

		
		List<CutByMainContourInfo> cutByMainContourInfos = new ArrayList<>(); 
		boolean mainValid = true;
		
		for(int idx=combinedInnerRouteLists.size()-1; idx>=0; idx--) {
			RouteList inner = combinedInnerRouteLists.get(idx);
			Contour innerContour = routeListMapper.routeListToScaledContour(inner);

			ContourComparator c = new ContourComparator();
			if (c.aContainsB(mainContour, innerContour)) {
				combinedInnerRouteLists.remove(idx);
				cutByMainContourInfos.add(new CutByMainContourInfo(innerContour, inner));
			} else if (c.aContainsB(innerContour, mainContour)) {
				combinedInnerRouteLists.remove(idx);
				mainValid = false;
			} else {
				cutByMainContourInfos.add(new CutByMainContourInfo(innerContour, null));
			}
		}

		
		if (!mainValid) {
			return;
		}
		
		boolean mainCutting = false;
		if (!cutByMainContourInfos.isEmpty()) {
			ContourLayer layer = new ContourLayer(10, 10);
			layer.add(mainContour);
			for(CutByMainContourInfo info : cutByMainContourInfos) {
				if (info.routeList==null) {
					layer.add(info.contour);
					mainCutting = true;
				} else {
					output.add(info.routeList);
				}
			}
			
			if (mainCutting) {
				ContourLayerOverlapCutter cutter = new ContourLayerOverlapCutter();
				ContourLayer contourLayer = cutter.scale(layer, true);
				for(int contourIndex=contourLayer.count()-1; contourIndex>=0; contourIndex--) {
					Contour cutContour = contourLayer.getAt(contourIndex);
					ContourComparator cc = new ContourComparator();
					if (cc.aContainsB(mainContour, cutContour)) {
						List<Route> asRouteList = routeListAsContour(cutContour);
						output.add(new RouteList(asRouteList));
					}
				}
			}
			
		}
		if (!mainCutting) {
			output.add(mainRoute);
		}
	}
	
	static class CutByMainContourInfo {
		
		public final Contour contour;
		public final RouteList routeList;
		
		public CutByMainContourInfo(Contour contour, RouteList routeList) {
			this.contour = contour;
			this.routeList = routeList;
		}
	}

	private List<Route> routeListAsContour(Contour contour) {
		List<Route> result = new ArrayList<Route>();
		int dotCount = contour.dotCount();
		for(int dotIdx=0; dotIdx<dotCount; dotIdx++) {
			Point2D dotA = contour.dotAt(dotIdx);
			Point2D dotB = contour.dotAt((dotIdx+1) % dotCount);
			result.add(new Route(new Line2D(dotA, dotB)));
		}
		return result;
	}

	private List<RouteList> combineInflated(List<RouteList> inflated) {
		List<RouteList> input = new ArrayList<>(inflated);
		List<RouteList> result = new ArrayList<>();
		while(!input.isEmpty()) {
			RouteList routeList = input.remove(input.size()-1);
			for(int idx=input.size()-1; idx>=0; idx--) {
				RouteList otherRouteList = input.get(idx);
				RouteListInteractionAnalyser analyser = new RouteListInteractionAnalyser(routeList, otherRouteList);
				if (analyser.overlap()) {
					routeList = analyser.rebuildRoutList();
					input.remove(idx);
					this.analyser = analyser;
				}
			}
			result.add(routeList);
		}
		return result;
	}

	public List<RouteList> getCombined() {
		return combined;
	}
	
	
	public RouteListInteractionAnalyser getAnalyser() {
		return analyser;
	}
	
}
