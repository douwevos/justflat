package net.github.douwevos.justflat.contour.testui;

import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.types.Bounds2D;

public class DiscLayerViewableModel implements ViewableModel {

	final ContourLayer discLayer;

	public DiscLayerViewableModel(ContourLayer discLayer) {
		this.discLayer = discLayer;
	}
	
	@Override
	public Bounds2D bounds() {
		return discLayer.bounds();
	}
}