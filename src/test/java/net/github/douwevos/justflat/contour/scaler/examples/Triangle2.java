package net.github.douwevos.justflat.contour.scaler.examples;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.scaler.ContourLayerTestProducer;
import net.github.douwevos.justflat.contour.scaler.ContourLayerScalerDemo.ScalerConfigProvider;
import net.github.douwevos.justflat.types.values.Point2D;

public class Triangle2 implements ContourLayerTestProducer, ScalerConfigProvider {

	@Override
	public int getThickness() {
		return 1440;
	}

	public ContourLayer produceSourceLayer() {
		ContourLayer contourLayer = new ContourLayer(100000, 100000);
		Contour contour0 = new Contour();
		contour0.add(Point2D.of(1380, 1595));
		contour0.add(Point2D.of(5000, 100));
		contour0.add(Point2D.of(3501, 1953));
		contour0.setClosed(true);
		contourLayer.add(contour0);

		return contourLayer;
	}

	public ContourLayer produceResultLayer() {
		ContourLayer contourLayer = new ContourLayer(100000, 100000);
		return contourLayer;
	}
}
