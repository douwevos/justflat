package net.github.douwevos.justflat.contour.testui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import net.github.douwevos.justflat.contour.MutableContour;
import net.github.douwevos.justflat.contour.TargetLine;
import net.github.douwevos.justflat.contour.TranslatedSegment;
import net.github.douwevos.justflat.contour.testui.ScalerViewableModel.TranslatedSegmentSelection;
import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Point2D;


@SuppressWarnings("serial")
public class ScalerViewer2 extends ScalerViewerBase {

	TargetLine selectedTargetLine;
	
	double mouseModelX;
	double mouseModelY;
	

	@Override
	protected void paintModel(BufferedImage image, Graphics2D gfx, ScalerViewableModel viewableModel) {
		List<MutableContour> mutableContours = viewableModel.mutableContours;
		Dimension viewDimension = getViewDimension();
		int viewHeight = viewDimension.height;

		gfx.setStroke(new BasicStroke(1f));
		drawSegments(gfx, viewHeight, viewableModel);


		drawAllPoints(gfx, viewHeight, mutableContours);
	}

	@Override
	public void paintOnTopLayer(Graphics2D gfx, ScalerViewableModel model) {
	}
	

	
	@Override
	protected void drawSegment(Graphics2D gfx, int viewHeight, TranslatedSegment translatedSegment) {
		Point2D pointA = camera.toViewCoords(translatedSegment.base.base.getFirstPoint(), viewHeight);
		Point2D pointB = camera.toViewCoords(translatedSegment.base.base.getSecondPoint(), viewHeight);
		Point2D pointC = camera.toViewCoords(translatedSegment.translated.base.getSecondPoint(), viewHeight);
		Point2D pointD = camera.toViewCoords(translatedSegment.translated.base.getFirstPoint(), viewHeight);
		
		int x[] = new int[4];
		int y[] = new int[4];
		
		x[0] = (int) pointA.x;
		y[0] = (int) pointA.y;
		x[1] = (int) pointB.x;
		y[1] = (int) pointB.y;
		x[2] = (int) pointC.x;
		y[2] = (int) pointC.y;
		x[3] = (int) pointD.x;
		y[3] = (int) pointD.y;
		
		gfx.setColor(new Color(128,64,128,40));
		gfx.fillPolygon(x, y, 4);
		
		gfx.setColor(new Color(255,255,128,80));
		gfx.drawPolygon(x, y, 4);
	}



	@Override
	public boolean onDrag(MouseEvent event, Object selected, double mouseX, double mouseY) {
		return false;
	}

	@Override
	protected void onMove(MouseEvent event, double modelX, double modelY) {
		
		mouseModelX = modelX;
		mouseModelY = modelY;

		
		Object selectedOld = selected;
		selected = model==null ? null : model.selectAt(modelX, modelY, camera.getZoom());

//		if (selectedOld == selected) {
//			return;
//		}
		
		repaint();
	}
	
	@Override
	protected void onClicked(MouseEvent event, double modelX, double modelY) {
		Object selectOnClick = model==null ? null : model.selectAt(modelX, modelY, camera.getZoom());
//		if (selectOnClick instanceof TargetLineSelection) {
//			TargetLineSelection targetLineSelection = (TargetLineSelection) selectOnClick;
//			selectedTargetLine = targetLineSelection.get();
//			layerImage = null;
//			repaint();
//		} else {
			if (selectedTargetLine!=null) {
				selectedTargetLine = null;
				layerImage = null;
				repaint();
			}
//		}
	}
	
	
	protected void paintTranslatedSegmentSelection(Graphics2D gfx, TranslatedSegmentSelection selected) {
		TranslatedSegment translatedSegment = selected.translatedSegment;
		
		Line2D line = translatedSegment.base.base;
		Line2D translated = translatedSegment.translated.base;
		
		int viewHeight = getViewDimension().height;
		
		Point2D baseA = camera.toViewCoords(line.pointA(), viewHeight);
		Point2D baseB = camera.toViewCoords(line.pointB(), viewHeight);
		
		
		Point2D transA = camera.toViewCoords(translated.pointA(), viewHeight);
		Point2D transB = camera.toViewCoords(translated.pointB(), viewHeight);
		
		int polyX[] = new int[4];
		int polyY[] = new int[4];
		
		polyX[0] = (int) baseA.x;
		polyX[1] = (int) baseB.x;
		polyX[2] = (int) transB.x;
		polyX[3] = (int) transA.x;

		polyY[0] = (int) baseA.y;
		polyY[1] = (int) baseB.y;
		polyY[2] = (int) transB.y;
		polyY[3] = (int) transA.y;
		
		gfx.setColor(new Color(255,128,255,40));
		gfx.fillPolygon(polyX, polyY, 4);
	}
	
}
