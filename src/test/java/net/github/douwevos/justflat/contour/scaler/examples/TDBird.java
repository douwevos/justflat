package net.github.douwevos.justflat.contour.scaler.examples;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.scaler.ContourLayerTestProducer;
import net.github.douwevos.justflat.contour.scaler.ContourLayerScalerDemo.ScalerConfigProvider;
import net.github.douwevos.justflat.types.values.Point2D;

public class TDBird implements ContourLayerTestProducer, ScalerConfigProvider {

	@Override
	public int getThickness() {
		return 310;
	}

	@Override
	public String name() {
		return getClass().getSimpleName();
	}

	public ContourLayer produceSourceLayer() {
		ContourLayer contourLayer = new ContourLayer(100000, 100000);
		Contour contour0 = new Contour();
		contour0.add(Point2D.of(1000, 100));
		contour0.add(Point2D.of(5000, 100));
		contour0.add(Point2D.of(3775, 713));
		contour0.add(Point2D.of(4657, 2516));
		contour0.add(Point2D.of(3878, 1772));
		contour0.add(Point2D.of(3162, 2647));
		contour0.add(Point2D.of(1215, 3266));
		contour0.setClosed(true);
		contourLayer.add(contour0);

		return contourLayer;
	}

	public ContourLayer produceResultLayer() {
		ContourLayer contourLayer = new ContourLayer(100000, 100000);
		Contour contour0 = new Contour();
		contour0.add(Point2D.of(1000, 100));
		contour0.add(Point2D.of(5000, 100));
		contour0.add(Point2D.of(3775, 713));
		contour0.add(Point2D.of(4657, 2516));
		contour0.add(Point2D.of(3878, 1772));
		contour0.add(Point2D.of(3162, 2647));
		contour0.add(Point2D.of(1215, 3266));
		contour0.setClosed(true);
		contourLayer.add(contour0);

		return contourLayer;
	}
}
