package net.github.douwevos.justflat.types.values;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import net.github.douwevos.justflat.demo.Camera;
import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.ModelViewer;

@SuppressWarnings("serial")
public class Line2DViewer extends ModelViewer<Line2DViewableModel> {

	@Override
	public void paintOnTopLayer(Graphics2D gfx, Line2DViewableModel model) {
		
	}
	
	@Override
	protected void paintModel(BufferedImage image, Graphics2D gfx, Line2DViewableModel model) {
		Line2D lineA = model.lineA;
		Line2D lineB = model.lineB;

		
		Camera camera = getCamera();
		double transX =  -camera.getTranslateX();
		double transY =  camera.getTranslateY();
		double cameraZoom = camera.getZoom();
		
		int viewHeight = getViewDimension().height;
		
		gfx.setColor(Color.GREEN);
		drawLine(gfx, lineA, viewHeight);

		gfx.setColor(Color.ORANGE);
		drawLine(gfx, lineB, viewHeight);

	}

	private void drawLine(Graphics2D gfx, Line2D lineA, int viewHeight) {
		Camera camera = getCamera();
		Point2D pointA = camera.toViewCoords(lineA.getFirstPoint(), viewHeight);
		Point2D pointB = camera.toViewCoords(lineA.getSecondPoint(), viewHeight);

		int xa = (int) pointA.x;
		int ya = (int) pointA.y;
		int xb = (int) pointB.x;
		int yb = (int) pointB.y;

		gfx.drawLine(xa, ya, xb, yb);
	}

	
	@Override
	public boolean onDrag(ModelMouseEvent event, Object selected) {
		return false;
	}

	
	@Override
	protected void onMove(ModelMouseEvent modelEvent) {
		
	}

}
