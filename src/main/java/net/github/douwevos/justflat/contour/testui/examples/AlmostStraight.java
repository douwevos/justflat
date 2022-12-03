package net.github.douwevos.justflat.contour.testui.examples;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.testui.ContourLayerTestProducer;
import net.github.douwevos.justflat.contour.testui.LayerShower2.ScalerConfigProvider;
import net.github.douwevos.justflat.types.Point2D;

public class AlmostStraight implements ContourLayerTestProducer, ScalerConfigProvider {

	
	@Override
	public int getThickness() {
		return 609;
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
		contour0.add(Point2D.of(4590, 1364));
		contour0.add(Point2D.of(4034, 3204));
		contour0.add(Point2D.of(1215, 3266));
		contour0.setClosed(true);
		contourLayer.add(contour0);

		return contourLayer;
	}

	public ContourLayer produceResultLayer() {
		ContourLayer contourLayer = new ContourLayer(100000, 100000);
		return contourLayer;
	}
}
