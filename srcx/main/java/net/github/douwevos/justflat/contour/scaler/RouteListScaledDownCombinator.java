package net.github.douwevos.justflat.contour.scaler;

import java.util.ArrayList;
import java.util.List;

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
		ArrayList<RouteList> result = new ArrayList<>(combined);
		result.addAll(mainRouteLists);
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
