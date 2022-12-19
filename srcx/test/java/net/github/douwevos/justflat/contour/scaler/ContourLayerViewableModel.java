package net.github.douwevos.justflat.contour.scaler;

import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.Selection;
import net.github.douwevos.justflat.demo.ViewableModel;
import net.github.douwevos.justflat.types.values.Bounds2D;

public class ContourLayerViewableModel implements ViewableModel {

	final ContourLayer mainLayer;
	final ContourLayer ghostLayer;

	public ContourLayerViewableModel(ContourLayer mainLayer) {
		this.mainLayer = mainLayer;
		this.ghostLayer = null;
	}

	public ContourLayerViewableModel(ContourLayer mainLayer, ContourLayer ghostLayer) {
		this.mainLayer = mainLayer;
		this.ghostLayer = ghostLayer;
	}

	@Override
	public Bounds2D bounds() {
		if (ghostLayer!=null) {
			return ghostLayer.bounds().union(mainLayer.bounds());
		}
		return mainLayer.bounds();
	}
	
	@Override
	public Selection<?> selectAt(ModelMouseEvent modelMouseEvent) {
		double modelX = modelMouseEvent.modelX;
		double modelY = modelMouseEvent.modelY;
		double zoom = modelMouseEvent.camera.getZoom();
		Selection<?> result = mainLayer.selectAt(modelX, modelY, zoom);
		return result==null && ghostLayer!=null ? ghostLayer.selectAt(modelX, modelY, zoom) : result;
	}
}