package net.github.douwevos.justflat.contour;

import net.github.douwevos.justflat.contour.testui.ScalerViewableModel;

public class DiscLayerFillContext {

	public final ContourLayer discLayer;
	public ContourLayer reduceResolution;
	public ContourLayer scaled;
	public ContourLayer scaled2;
	public ScalerViewableModel scalerViewableModel;
	
	public DiscLayerFillContext(ContourLayer discLayer) {
		this.discLayer = discLayer;
	}

	public ContourLayer reduceResolution(ContourLayer discLayer, double distortionLevel, int stepping) {
		DiscLayerResolutionReducer resolutionReducer = new DiscLayerResolutionReducer();
		return resolutionReducer.reduceResolution(discLayer, distortionLevel, stepping);
	}
	
	public ContourLayer scale(ContourLayer discLayer, double thickness, boolean cleanup) {
		DiscLayerScaler scaler = new DiscLayerScaler();
		return scaler.scale(discLayer, thickness, cleanup);
	}
	
}
