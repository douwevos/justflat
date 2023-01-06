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
import net.github.douwevos.justflat.types.values.Line2D.IntersectionInfo;
import net.github.douwevos.justflat.types.values.Line2DViewableModel.CrossPointSelection;
import net.github.douwevos.justflat.types.values.Line2DViewableModel.LinePointSelection;

@SuppressWarnings("serial")
public class Line2DCrossPointViewer extends ModelViewer<Line2DViewableModel> {

	Point2D mouse = new Point2D(0, 0);
	
	public Line2DCrossPointViewer() {
		title = "Cross-Point";
	}
	
	@Override
	public void paintOnTopLayer(Graphics2D gfx, Line2DViewableModel model) {
		Line2D lineA = model.lines.get(0);
		double pointDistance = lineA.pointDistance(mouse);
		drawVisibleText(gfx, "dist   :"+pointDistance, 20, 20, Color.white, Color.black);
		pointDistance = lineA.pointDistanceSq(mouse);
		drawVisibleText(gfx, "distSq :"+Math.sqrt(pointDistance), 20, 60, Color.white, Color.black);
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

		Point2D crossPoint = model.crossPoint;
		if (crossPoint != null) {
			gfx.setColor(Color.green);
			paintCrossPoint(gfx, crossPoint, lineA, lineB);
		} else if (model.info.intersectionPoint!=null) {
			gfx.setColor(Color.magenta);
			paintCrossPoint(gfx, model.info.intersectionPoint, lineA, lineB);
		}
		
		
	}

	private void paintCrossPoint(Graphics2D gfx, Point2D crossPoint, Line2D lineA, Line2D lineB) {
		int viewHeight = getViewDimension().height;
		Point2D cp = camera.toViewCoords(crossPoint, viewHeight);
		int x = (int) cp.x;
		int y = (int) cp.y;
		
		gfx.drawArc(x-2,y-2,5,5,0,360);

		
		x += 10;
		y += 30;
		
		IntersectionInfo info = model.info;
		drawVisibleText(gfx, "ua  :"+info.ua, x, y, Color.white, Color.black);
		drawVisibleText(gfx, "udd :"+info.udd, x, y+25, Color.white, Color.black);
		
		
		double alphaA = lineA.getAlpha();
		double alphaB = lineB.getAlpha();
		int a = (int) Math.round(360d+alphaB-alphaA);
		x = (int) cp.x;
		y = (int) cp.y;
		gfx.setStroke(new BasicStroke(3));
		gfx.setColor(Color.orange);
		gfx.drawArc(x-30, y-30, 60, 60, (int) alphaA, a % 360);
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
		mouse = new Point2D(Math.round(modelEvent.modelX), Math.round(modelEvent.modelY));
		Selection<?> selectedOld = highlighted;
		if (model==null) {
			highlighted = null;
		} else {
			highlighted = model.selectAt(modelEvent);
		}

		repaint();
		if (selectedOld == highlighted) {
			return;
		}
	}

	
}
