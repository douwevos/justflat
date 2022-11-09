package net.github.douwevos.justflat.contour.testui.examples;

import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.testui.ContourLayerTestProducer;
import net.github.douwevos.justflat.contour.testui.LayerShower2.ScalerConfigProvider;
import net.github.douwevos.justflat.ttf.TextLayout;
import net.github.douwevos.justflat.ttf.TextLayoutToDiscLayer;
import net.github.douwevos.justflat.ttf.format.Ttf;
import net.github.douwevos.justflat.types.Point2D;

public class TestModelOne2 implements ContourLayerTestProducer, ScalerConfigProvider {

	@Override
	public int getThickness() {
		return 3225;
	}

	@Override
	public String name() {
		return "SansFree outer-d 2";
	}

	@Override
	public ContourLayer produceSourceLayer() {
		Ttf freeSansTtf = TtfProducer.getFreeSansTtf();
		TextLayout textLayout = new TextLayout(freeSansTtf, "d");
		int textSize = 60000;

		TextLayoutToDiscLayer textLayoutToDiscLayer = new TextLayoutToDiscLayer(textLayout, textSize);
		ContourLayer result = new ContourLayer(100000, 100000);
		textLayoutToDiscLayer.produceLayer(result, 1000, 1000);
		result.moveDot(Point2D.of(28218, 40764), Point2D.of(32000, 39000));
		result.moveDot(Point2D.of(28218, 40764), Point2D.of(20944, 14946));
		result.contours.remove(1);
		return result;
	}

	
	
	
}
