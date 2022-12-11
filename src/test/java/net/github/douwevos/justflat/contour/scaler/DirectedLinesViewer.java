package net.github.douwevos.justflat.contour.scaler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.ModelViewer;
import net.github.douwevos.justflat.types.values.Point2D;

public class DirectedLinesViewer extends ModelViewer<DirectedLinesViewableModel> {

	
	@Override
	public void paintOnTopLayer(Graphics2D gfx, DirectedLinesViewableModel model) {
		
	}

	@Override
	protected void paintModel(BufferedImage image, Graphics2D gfx, DirectedLinesViewableModel viewableModel) {
		DirectedLines directedLines = viewableModel.directedLines;
		Dimension viewDimension = getViewDimension();
		int viewHeight = viewDimension.height;
		for(DirectedLine directedLine : directedLines) {
			drawDirectedLine(gfx, directedLine, viewHeight);
		}

		boolean drawNormals = (20/camera.getZoom()) > 1d;
		if (drawNormals) {
			for(DirectedLine directedLine : directedLines) {
				drawDirectedNormalLine(gfx, directedLine, viewHeight);
			}
		}

	}

	private void drawDirectedLine(Graphics2D gfx, DirectedLine directedLine, int viewHeight) {
		Point2D pointA = camera.toViewCoords(directedLine.baseLine.getFirstPoint(), viewHeight);
		Point2D pointB = camera.toViewCoords(directedLine.baseLine.getSecondPoint(), viewHeight);
		gfx.setColor(Color.yellow);
		gfx.drawLine((int) pointA.x, (int) pointA.y, (int) pointB.x, (int) pointB.y);
//		
//		boolean drawNormals = (20/camera.getZoom()) > 1d;
//		
//
//		if (drawNormals) {
//			
//			double lineLength = directedLine.baseLine.getLineLength();
//			if (lineLength == 0) {
//				return;
//			}
////			double midx = (pointA.x+pointB.x)/2d;
////			double midy = (pointA.y+pointB.y)/2d;
//
//			double midx = (pointA.x);
//			double midy = (pointA.y);
//
//			double normalX;
//			double normalY;
//			if (directedLine.reverse) {
//				normalX = midx - 15d*directedLine.baseLine.deltaY()/lineLength;
//				normalY = midy - 15d*directedLine.baseLine.deltaX()/lineLength;
//			} else {
//				normalX = midx + 15d*directedLine.baseLine.deltaY()/lineLength;
//				normalY = midy + 15d*directedLine.baseLine.deltaX()/lineLength;
//			}
//			
//			
//			gfx.drawLine((int) midx, (int) midy, (int) normalX, (int) normalY);
//		}
		
	}
	
	
	private void drawDirectedNormalLine(Graphics2D gfx, DirectedLine directedLine, int viewHeight) {
		Point2D pointA = camera.toViewCoords(directedLine.baseLine.getFirstPoint(), viewHeight);
		Point2D pointB = camera.toViewCoords(directedLine.baseLine.getSecondPoint(), viewHeight);
		gfx.setColor(Color.green);
		
		boolean drawNormals = (20/camera.getZoom()) > 1d;
		

		if (drawNormals) {
			
			double lineLength = directedLine.baseLine.getLineLength();
			if (lineLength == 0) {
				return;
			}
//			double midx = (pointA.x+pointB.x)/2d;
//			double midy = (pointA.y+pointB.y)/2d;

			double midx = (pointA.x);
			double midy = (pointA.y);

			double normalX;
			double normalY;
			if (directedLine.reverse) {
				normalX = midx - 15d*directedLine.baseLine.deltaY()/lineLength;
				normalY = midy - 15d*directedLine.baseLine.deltaX()/lineLength;
			} else {
				normalX = midx + 15d*directedLine.baseLine.deltaY()/lineLength;
				normalY = midy + 15d*directedLine.baseLine.deltaX()/lineLength;
			}
			
			
			gfx.drawLine((int) midx, (int) midy, (int) normalX, (int) normalY);
		}
		
	}

	@Override
	public boolean onDrag(ModelMouseEvent event, Object selected) {
		return false;
	}

	
	@Override
	protected void onMove(ModelMouseEvent modelEvent) {
	}

	public void setModel(DirectedLines directedLines) {
		setModel(directedLines==null ? null : new DirectedLinesViewableModel(directedLines));
	}
	
}
