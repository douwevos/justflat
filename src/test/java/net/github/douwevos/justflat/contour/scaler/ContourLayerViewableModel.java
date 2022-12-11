package net.github.douwevos.justflat.contour.scaler;

import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.Selection;
import net.github.douwevos.justflat.demo.ViewableModel;
import net.github.douwevos.justflat.types.values.Bounds2D;

public class ContourLayerViewableModel implements ViewableModel {

	final ContourLayer discLayer;

	public ContourLayerViewableModel(ContourLayer discLayer) {
		this.discLayer = discLayer;
	}
	
	@Override
	public Bounds2D bounds() {
		return discLayer.bounds();
	}
	
	@Override
	public Selection<?> selectAt(ModelMouseEvent modelMouseEvent) {
		return null;
	}
}