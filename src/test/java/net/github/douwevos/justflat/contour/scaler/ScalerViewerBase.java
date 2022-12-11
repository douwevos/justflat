package net.github.douwevos.justflat.contour.scaler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.github.douwevos.justflat.contour.scaler.OverlapPoint.Taint;
import net.github.douwevos.justflat.contour.scaler.ScalerViewableModel.OverlapPointSelection;
import net.github.douwevos.justflat.contour.scaler.ScalerViewableModel.PointSelection;
import net.github.douwevos.justflat.contour.scaler.ScalerViewableModel.TranslatedSegmentSelection;
import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.ModelViewer;
import net.github.douwevos.justflat.demo.Selection;
import net.github.douwevos.justflat.types.values.Point2D;

@SuppressWarnings("serial")
public abstract class ScalerViewerBase extends ModelViewer<ScalerViewableModel> {

	protected Map<Taint, Color> taintColorMap = new EnumMap<>(Taint.class);
	
	{
		taintColorMap.put(Taint.NONE, Color.white);
		taintColorMap.put(Taint.RECONNECT, Color.green.brighter());
		taintColorMap.put(Taint.EDGE, Color.cyan.brighter());
		taintColorMap.put(Taint.OBSCURED, Color.darkGray.brighter());
		taintColorMap.put(Taint.ORIGINAL, Color.orange.darker());
		taintColorMap.put(Taint.INVALID, Color.red.darker());
	}
	



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
	protected void paintSelected(Graphics2D g, Selection<?> selected) {
		super.paintSelected(g, selected);

		if (selected instanceof PointSelection) {
			paintPointSelection(g, (PointSelection) selected);
		} 
		else if (selected instanceof OverlapPointSelection) {
			paintOverlapPointSelection(g, (OverlapPointSelection) selected);
		} 
		else if (selected instanceof TranslatedSegmentSelection) {
			paintHighlightedTranslatedSegment(g, (TranslatedSegmentSelection) selected);
		}

	}
	
	
	@Override
	public boolean onDrag(ModelMouseEvent event, Selection<?> selected) {
		return false;
	}

	@Override
	protected void onMove(ModelMouseEvent modelEvent) {
		Selection<?> selectedOld = highlighted;
		highlighted = model==null ? null : model.selectAt(modelEvent);

		if (selectedOld == highlighted) {
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
		segment.streamAllOverlapPoints().forEach(op -> {
			drawOverlapPoint(subgfx, viewHeight, op);
		});
		subgfx.dispose();
	}
	
	protected void drawOverlapPoint(Graphics2D gfx, int viewHeight, OverlapPoint overlapPoint) {
		Point2D point = camera.toViewCoords(overlapPoint.point, viewHeight);
		drawCircle(gfx, point, 3, false);
	}

	
	
	
	protected void paintPointSelection(Graphics2D g, PointSelection sel) {
		paintSelectedPointWithLocation(g, sel.point);
	}

	protected void paintOverlapPointSelection(Graphics2D g, OverlapPointSelection sel) {
		Point2D point = sel.get().point;
		paintSelectedPointWithLocation(g, point);

		Dimension viewDimension = getViewDimension();
		Point2D viewCoords = camera.toViewCoords(point, viewDimension.height);
		
		int tx = (int) viewCoords.x+10;
		int ty = (int) viewCoords.y+40;
		Set<Taint> taintedSet = sel.overlapPoint.getTaintedSet();
		for(Taint taint : taintedSet) {
			drawVisibleText(g, taint.name(), tx, ty, taintColorMap.get(taint), Color.BLACK);
			ty += 25;
		}
		
		ObscuredInfo obscuredInfo = sel.overlapPoint.getObscuredInfo();
		drawObscuredInfo(g, viewCoords, obscuredInfo);
	}
	
	private void drawObscuredInfo(Graphics2D g, Point2D viewCoords, ObscuredInfo obscuredInfo) {
		
		Graphics gfxTemp = g.create();
		
		final int N =28;
		
		final int R = 120;
		final int D = R*2;
		
		int xb = (int) viewCoords.x-10-R;
		int yb = (int) viewCoords.y-R / 2;
		int yb1 = (int) viewCoords.y +50 + R / 2;
		g.setColor(Color.RED);
		g.drawArc(xb, yb, R, R, 0, 360);

		g.setColor(new Color(255,0,0,100));

		for(Range range : obscuredInfo) {
//			int start = (int) Math.round(Math.toDegrees(range.start));
//			int end = (int) Math.round(Math.toDegrees(range.end));
			int start = (int) Math.round(range.start);
			int end = (int) Math.round(range.end);
			int ang = end-start;
			if (ang<0) {
				ang+=360;
			}
			g.fillArc(xb, yb, R, R, start, ang);
			drawVisibleText(gfxTemp, ""+range, xb, yb1, Color.LIGHT_GRAY, Color.black);
			yb1 += 30;

			g.fillArc(xb-N, yb1-N-20, N, N, start, ang);

		}
		
		gfxTemp.dispose();
	}


	protected void paintHighlightedTranslatedSegment(Graphics2D g, TranslatedSegmentSelection selected) {
		
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
