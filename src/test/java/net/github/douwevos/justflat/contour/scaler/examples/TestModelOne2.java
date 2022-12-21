package net.github.douwevos.justflat.contour.scaler.examples;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.scaler.ContourLayerTestProducer;
import net.github.douwevos.justflat.contour.scaler.ContourLayerScalerDemo.ScalerConfigProvider;
import net.github.douwevos.justflat.ttf.TextLayout;
import net.github.douwevos.justflat.ttf.TextLayoutToDiscLayer;
import net.github.douwevos.justflat.ttf.format.Ttf;
import net.github.douwevos.justflat.types.values.Point2D;

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
//		result.moveDot(Point2D.of(28218, 40764), Point2D.of(20944, 14946));
		result.moveDot(Point2D.of(28218, 40764), Point2D.of(29566, 14821));
		result.contours.remove(1);
		return result;
	}

	
	public ContourLayer produceResultLayer() {
		ContourLayer contourLayer = new ContourLayer(100000, 100000);
		Contour contour0 = new Contour();
		contour0.add(Point2D.of(5868, 15697));
		contour0.add(Point2D.of(5961, 13129));
		contour0.add(Point2D.of(6300, 10845));
		contour0.add(Point2D.of(6900, 8783));
		contour0.add(Point2D.of(7634, 7211));
		contour0.add(Point2D.of(8539, 5880));
		contour0.add(Point2D.of(9577, 4809));
		contour0.add(Point2D.of(10648, 4047));
		contour0.add(Point2D.of(11842, 3493));
		contour0.add(Point2D.of(13204, 3135));
		contour0.add(Point2D.of(14895, 2981));
		contour0.add(Point2D.of(16468, 3076));
		contour0.add(Point2D.of(17663, 3351));
		contour0.add(Point2D.of(18627, 3764));
		contour0.add(Point2D.of(19499, 4348));
		contour0.add(Point2D.of(20458, 5256));
		contour0.add(Point2D.of(26086, 12372));
		contour0.add(Point2D.of(26356, 15136));
		contour0.add(Point2D.of(26470, 16266));
		contour0.add(Point2D.of(20778, 24524));
		contour0.add(Point2D.of(20591, 24770));
		contour0.add(Point2D.of(20171, 25243));
		contour0.add(Point2D.of(19179, 26061));
		contour0.add(Point2D.of(18186, 26588));
		contour0.add(Point2D.of(16913, 26974));
		contour0.add(Point2D.of(15265, 27160));
		contour0.add(Point2D.of(13589, 27085));
		contour0.add(Point2D.of(12239, 26801));
		contour0.add(Point2D.of(10881, 26243));
		contour0.add(Point2D.of(9803, 25537));
		contour0.add(Point2D.of(8863, 24660));
		contour0.add(Point2D.of(7884, 23395));
		contour0.add(Point2D.of(7092, 21942));
		contour0.add(Point2D.of(6460, 20177));
		contour0.add(Point2D.of(6047, 18146));
		contour0.setClosed(true);
		contourLayer.add(contour0);

		Contour contour1 = new Contour();
		contour1.add(Point2D.of(26916, 20699));
		contour1.add(Point2D.of(28562, 37045));
		contour1.add(Point2D.of(27502, 37539));
		contour1.add(Point2D.of(26916, 37539));
		contour1.setClosed(true);
		contourLayer.add(contour1);

		return contourLayer;
	}
	
}
