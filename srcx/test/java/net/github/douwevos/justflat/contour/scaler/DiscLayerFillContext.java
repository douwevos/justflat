package net.github.douwevos.justflat.contour.scaler;

import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.ContourLayerResolutionReducer;

public class DiscLayerFillContext {

	public final ContourLayer discLayer;
	public ContourLayer reduceResolution;
	public ContourLayer scaled;
	public ContourLayer scaled2;
	public ScalerViewableModel scalerViewableModel;
	public RouteListScaledDownCombinator routeListScaledDownCombinator;
	
	public DiscLayerFillContext(ContourLayer discLayer) {
		this.discLayer = discLayer;
	}

	public ContourLayer reduceResolution(ContourLayer discLayer, double distortionLevel, int stepping) {
		ContourLayerResolutionReducer resolutionReducer = new ContourLayerResolutionReducer();
		return resolutionReducer.reduceResolution(discLayer, distortionLevel, stepping);
	}
	
	public ContourLayer scale(ContourLayer discLayer, double thickness, boolean cleanup) {
		ContourLayerScaler scaler = new ContourLayerScaler();
		return scaler.scale(discLayer, thickness, cleanup);
	}
	
}
