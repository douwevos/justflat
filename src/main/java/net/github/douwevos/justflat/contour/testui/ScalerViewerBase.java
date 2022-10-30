package net.github.douwevos.justflat.contour.testui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.List;

import net.github.douwevos.justflat.contour.MutableContour;
import net.github.douwevos.justflat.contour.OverlapPoint;
import net.github.douwevos.justflat.contour.TranslatedSegment;
import net.github.douwevos.justflat.contour.testui.ScalerViewableModel.CrossPointSelection;
import net.github.douwevos.justflat.contour.testui.ScalerViewableModel.TranslatedSegmentSelection;
import net.github.douwevos.justflat.types.Point2D;

public abstract class ScalerViewerBase extends ModelViewer<ScalerViewableModel> {

//	TargetLine selectedTargetLine;
	
	protected double mouseModelX;
	protected double mouseModelY;



	protected void drawSegments(Graphics2D gfx, int viewHeight, ScalerViewableModel viewableModel) {
		for(MutableContour mutableContour : viewableModel.mutableContours) {
			drawSegments(gfx, viewHeight, mutableContour);
		}
		
	}
	
	
	protected void drawSegments(Graphics2D gfx, int viewHeight, MutableContour mutableContour) {
		for(TranslatedSegment mutableLine : mutableContour.segmentIterable()) {
			drawSegment(gfx, viewHeight, mutableLine);
		}
	}


	protected abstract void drawSegment(Graphics2D gfx, int viewHeight, TranslatedSegment translatedSegment);


	
	@Override
	protected void paintSelected(Graphics2D g, Object selected) {
		super.paintSelected(g, selected);

		if (selected instanceof CrossPointSelection) {
			paintCrossPointSelection(g, (CrossPointSelection) selected);
		} else
		if (selected instanceof TranslatedSegmentSelection) {
			paintTranslatedSegmentSelection(g, (TranslatedSegmentSelection) selected);
		}

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

		if (selectedOld == selected) {
			return;
		}
		
		repaint();
	}
	
	
	

	protected void drawAllPoints(Graphics2D gfx, int viewHeight, List<MutableContour> mutableContours) {
		double cameraZoom = camera.getZoom();
		boolean drawDots = (200/cameraZoom) > 1d;
		if (drawDots) {
			for(MutableContour mutableContour : mutableContours) {
				drawAllContourPoints(gfx, viewHeight, mutableContour);
			}
		}
	}

	protected void drawAllContourPoints(Graphics2D gfx, int viewHeight, MutableContour mutableContour) {
		mutableContour.streamSegments().forEach(seg -> drawAllSegmentPoints(gfx, viewHeight, mutableContour, seg));
	}

	
	
	protected void drawAllSegmentPoints(Graphics2D gfx, int viewHeight, MutableContour mutableContour,
			TranslatedSegment segment) {
		Graphics2D subgfx = (Graphics2D) gfx.create();
		subgfx.setStroke(new BasicStroke(2.5f));
		segment.streamOverlapPoints().forEach(op -> {
			drawOverlapPoint(subgfx, viewHeight, op);
		});
		subgfx.dispose();
	}
	
	protected void drawOverlapPoint(Graphics2D gfx, int viewHeight, OverlapPoint overlapPoint) {
		Point2D point = camera.toViewCoords(overlapPoint.point, viewHeight);
		drawCircle(gfx, point, 3, false);
	}

	
	
	
	protected void paintCrossPointSelection(Graphics2D g, CrossPointSelection sel) {
		paintSelectedPointWithLocation(g, sel.crossPoint.crossPoint);
	}
	
	
	protected void paintTranslatedSegmentSelection(Graphics2D g, TranslatedSegmentSelection selected) {
		
	}

	
	
	protected void drawCircle(Graphics2D gfx, Point2D point, int size, boolean doFill) {
		if (point==null) {
			return;
		}
		int x = (int) point.x;
		int y = (int) point.y;
		int d = size*2+1;
		if (doFill) {
			gfx.fillArc(x-size, y-size, d,d,0,360);
		} else {
			gfx.drawArc(x-size, y-size, d,d,0,360);
		}
	}

	
}
