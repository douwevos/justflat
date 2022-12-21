package net.github.douwevos.justflat.types.values;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

import net.github.douwevos.justflat.Selection;
import net.github.douwevos.justflat.demo.Camera;
import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.ModelViewer;
import net.github.douwevos.justflat.types.values.Line2DViewableModel.CrossPointSelection;
import net.github.douwevos.justflat.types.values.Line2DViewableModel.LinePointSelection;

@SuppressWarnings("serial")
public class Line2DRelationViewer extends ModelViewer<Line2DViewableModel> {

	public Line2DRelationViewer() {
		title = "Line-Relation";
	}
	
	@Override
	public void paintOnTopLayer(Graphics2D gfx, Line2DViewableModel model) {
		
	}
	
	@Override
	protected void paintModel(BufferedImage image, Graphics2D gfx, Line2DViewableModel model) {
		Line2D lineA = model.lines.get(0);
		Line2D lineB = model.lines.get(1);

		
		Camera camera = getCamera();
		double transX =  -camera.getTranslateX();
		double transY =  camera.getTranslateY();
		double cameraZoom = camera.getZoom();
		
		int viewHeight = getViewDimension().height;
		
		gfx.setColor(Color.WHITE);
		drawLine(gfx, lineA, viewHeight);
		drawLine(gfx, lineB, viewHeight);

		LineRelation lineRelation = model.lineRelation;
		if (lineRelation != null) {
			gfx.setColor(Color.green);
			paintCrossPoint(gfx, lineRelation, lineA, lineB);
		}
		
		
	}

	private void paintCrossPoint(Graphics2D gfx, LineRelation lineRelation, Line2D lineA, Line2D lineB) {
		int viewHeight = getViewDimension().height;

		int x = 0;
		int y = 80;
		
		drawVisibleText(gfx, "in-line    :"+lineRelation.inLineTyp, x, y, Color.white, Color.black);
		drawVisibleText(gfx, "shared-pnts:"+lineRelation.sharePoints, x, y+25, Color.white, Color.black);
		drawVisibleText(gfx, "na    :"+lineRelation.na, x, y+50, Color.white, Color.black);
		drawVisibleText(gfx, "nb    :"+lineRelation.nb, x, y+75, Color.white, Color.black);
	}

	
	private void drawLine(Graphics2D gfx, Line2D lineA, int viewHeight) {
		Camera camera = getCamera();
		Point2D firstPoint = lineA.getFirstPoint();
		Point2D secondPoint = lineA.getSecondPoint();
		Point2D pointA = camera.toViewCoords(firstPoint, viewHeight);
		Point2D pointB = camera.toViewCoords(secondPoint, viewHeight);

		int xa = (int) pointA.x;
		int ya = (int) pointA.y;
		int xb = (int) pointB.x;
		int yb = (int) pointB.y;

		gfx.drawLine(xa, ya, xb, yb);
		
		double lineLength = lineA.getLineLength();
		double factor = 20d*camera.getZoom()/lineLength;
		long ox = Math.round((lineA.deltaX()*factor));
		long oy = Math.round((lineA.deltaY()*factor));
		Point2D tpb = camera.toViewCoords(new Point2D(secondPoint.x + ox, secondPoint.y + oy), viewHeight);
		drawVisibleText(gfx, "B", (int) tpb.x, (int) tpb.y, Color.white, Color.black);
		Point2D tpa = camera.toViewCoords(new Point2D(firstPoint.x - ox, firstPoint.y - oy), viewHeight);
		drawVisibleText(gfx, "A", (int) tpa.x, (int) tpa.y, Color.white, Color.black);
	}
	

	@Override
	public boolean onDrag(ModelMouseEvent event, Selection<?> selected) {
		boolean altDown = (event.event.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;
		return model==null ? false : model.dragTo(selected, altDown, event.modelX, event.modelY);
	}

	
	@Override
	protected void paintSelected(Graphics2D g, Selection<?> selected) {
		if (selected instanceof LinePointSelection) {
			paintHighlightLinePointSelection(g, (LinePointSelection) selected);
		} else if (selected instanceof CrossPointSelection) {
			paintHighlightCrossPointSelection(g, (CrossPointSelection) selected);
			
		}
	}

	private void paintHighlightLinePointSelection(Graphics2D g, LinePointSelection selected) {
		Point2D point2d = selected.get();
		paintSelectedPointWithLocation(g, point2d);
	}

	private void paintHighlightCrossPointSelection(Graphics2D g, CrossPointSelection selected) {
		Point2D point2d = selected.get();
		paintSelectedPointWithLocation(g, point2d);
	}


	protected void onMove(ModelMouseEvent modelEvent) {
		Selection<?> selectedOld = highlighted;
		if (model==null) {
			highlighted = null;
		} else {
			highlighted = model.selectAt(modelEvent);
		}

		if (selectedOld == highlighted) {
			return;
		}
		repaint();
	}

	
}
