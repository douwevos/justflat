package net.github.douwevos.justflat.contour.testui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.github.douwevos.justflat.contour.MutableContour;
import net.github.douwevos.justflat.contour.MutableLine;
import net.github.douwevos.justflat.contour.OverlapPoint;
import net.github.douwevos.justflat.contour.OverlapPoint.Taint;
import net.github.douwevos.justflat.contour.Route;
import net.github.douwevos.justflat.contour.TargetLine;
import net.github.douwevos.justflat.contour.TranslatedSegment;
import net.github.douwevos.justflat.contour.testui.ScalerViewableModel.TranslatedSegmentSelection;
import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Line2D.IntersectionInfo;
import net.github.douwevos.justflat.types.Point2D;

public class ScalerViewer3 extends ScalerViewerBase {

	TargetLine selectedTargetLine;
	
	double mouseModelX;
	double mouseModelY;
	
	TranslatedSegmentSelection selected;
	Point2D mouse;
	
	
	@Override
	public void paintOnTopLayer(Graphics2D gfx, ScalerViewableModel model) {
		if (selectedTargetLine != null) {
			Point2D point = new Point2D(Math.round(mouseModelX), Math.round(mouseModelY));
			paintProjection(gfx, model, selectedTargetLine, point);
		}
		
		if (selected!=null) {
			paintSelectedTranslatedSegment(gfx, selected);
		}
	}



	private void paintProjection(Graphics2D gfx, ScalerViewableModel model, TargetLine targetLine,
			Point2D point) {
		int viewHeight = getViewDimension().height;
		gfx.setColor(Color.CYAN);
		MutableLine mutableLine = targetLine.getMutableLine();
		Line2D translated = mutableLine.base;
		
		IntersectionInfo info = new IntersectionInfo();
		Point2D intersectionPoint = translated.intersectionPoint(point, info);
		if (info.ua>=0d && info.ua<=1d) {
			if (intersectionPoint != null) {
				Point2D pa = camera.toViewCoords(intersectionPoint, viewHeight);
				Point2D pb = camera.toViewCoords(point, viewHeight);
				gfx.drawLine((int) pa.x, (int) pa.y, (int) pb.x, (int) pb.y);
			}
		}
	}


	@Override
	protected void paintModel(BufferedImage image, Graphics2D gfx, ScalerViewableModel viewableModel) {
		List<MutableContour> mutableContours = viewableModel.mutableContours;
		Dimension viewDimension = getViewDimension();
		int viewHeight = viewDimension.height;


		gfx.setStroke(new BasicStroke(1f));
		
		drawSegments(gfx, viewHeight, viewableModel);
		
//		for(MutableContour mutableContour : mutableContours) {
//			drawMutableSegment(gfx, mutableContour, viewHeight);
//		}

		drawAllPoints(gfx, viewHeight, mutableContours);
		
		if (selectedTargetLine != null) {
			paintTargetLine(gfx, viewableModel, selectedTargetLine);
		}
		
		if (selected != null) {
			paintHighlightedTranslatedSegment(gfx, selected);
		}
		
		
		int y = 20;
		for(Taint taint : Taint.values()) {
			gfx.setColor(taintColorMap.get(taint));
			gfx.fillRect(0, y, 25, 25);
			gfx.drawString(""+taint, 40, y+20);
			y += 40;
			
		}
	}

	
	private void paintTargetLine(Graphics2D gfx, ScalerViewableModel viewableModel,
			TargetLine targetLine) {
		MutableLine mutableLine = targetLine.getMutableLine();
		Line2D line = mutableLine.base;
		Line2D translated = mutableLine.translated;
		
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
		
		double cosAlpha = line.getAlpha();
		int alpha = (int) Math.round(cosAlpha);
		double size = Math.abs(mutableLine.thickness*2d) / camera.getZoom();
		System.err.println("size="+size+" cosAlpha="+cosAlpha);
		int sizeI = (int) Math.round(size);
		int r = sizeI/2;
		int s1 = (alpha+180) % 360;
		int s2 = (alpha+270) % 360;
		gfx.fillArc((int) baseA.x-r, (int) baseA.y-r, sizeI, sizeI, s1, 91);

		
		gfx.fillArc((int) baseB.x-r, (int) baseB.y-r, sizeI, sizeI, s2, 91);
		
	}




	protected void drawOverlapPoint(Graphics2D gfx, int viewHeight, OverlapPoint overlapPoint) {
		Taint taint = overlapPoint.getTaint();
		gfx.setColor(taintColorMap.get(taint));
		Point2D point = camera.toViewCoords(overlapPoint.point, viewHeight);
		drawCircle(gfx, point, 5, false);
	}



	private void drawMutableSegment(Graphics2D gfx, MutableContour mutableContour, int viewHeight) {
		for(TranslatedSegment mutableLine : mutableContour.segmentIterable()) {
			drawSegment(gfx, viewHeight, mutableLine);
		}
	}
	
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
		
		gfx.setColor(new Color(192,192,156,80));
		gfx.drawPolygon(x, y, 4);

		drawCircle(gfx, pointA, 3, true);
		drawCircle(gfx, pointB, 3, true);

		
		gfx.setColor(new Color(255,0,0,100));
		gfx.drawLine(x[0], y[0], x[1], y[1]);

		gfx.setColor(new Color(255,255,0,140));
		gfx.drawLine(x[2], y[2], x[3], y[3]);
	}



	@Override
	public boolean onDrag(MouseEvent event, Object selected, double mouseX, double mouseY) {
		return false;
	}

	@Override
	protected void onMove(MouseEvent event, double modelX, double modelY) {
		
		mouseModelX = modelX;
		mouseModelY = modelY;

		if (selected == null) {
			highlighted = model==null ? null : model.selectAt(modelX, modelY, camera.getZoom());
		} else {
			mouse = Point2D.of(Math.round(modelX), Math.round(modelY));
		}
		repaint();
	}
	
	@Override
	protected void onClicked(MouseEvent event, double modelX, double modelY) {
		Object selectOnClick = model==null ? null : model.selectAt(modelX, modelY, camera.getZoom());
		if (selectOnClick instanceof TranslatedSegmentSelection) {
//			TargetLineSelection targetLineSelection = (TargetLineSelection) selectOnClick;
			selected = (TranslatedSegmentSelection) selectOnClick;
			layerImage = null;
			repaint();
		} else {
			if (selected!=null) {
				selected = null;
				layerImage = null;
				repaint();
			}
			if (selectedTargetLine!=null) {
				selectedTargetLine = null;
				layerImage = null;
				repaint();
			}
		}
	}
	


	protected void paintHighlightedTranslatedSegment(Graphics2D gfx, TranslatedSegmentSelection selected) {
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
//		
//		double cosAlpha = line.getAlpha();
//		int alpha = (int) Math.round(cosAlpha);
//		double size = Math.abs(mutableLine.thickness*2d) / camera.getZoom();
//		System.err.println("size="+size+" cosAlpha="+cosAlpha);
//		int sizeI = (int) Math.round(size);
//		int r = sizeI/2;
//		int s1 = (alpha+180) % 360;
//		int s2 = (alpha+270) % 360;
//		gfx.fillArc((int) baseA.x-r, (int) baseA.y-r, sizeI, sizeI, s1, 91);
//
//		
//		gfx.fillArc((int) baseB.x-r, (int) baseB.y-r, sizeI, sizeI, s2, 91);
		
		translatedSegment.translated.ensureOrdered();
		int idx=0;
		for(OverlapPoint overlapPoint : translatedSegment.translated.overlapPointsIterable()) {
			paintHighlightedOverlapPoint(gfx, viewHeight, overlapPoint, idx);
			idx++;
		}

	}
	
	
	protected void paintSelectedTranslatedSegment(Graphics2D gfx, TranslatedSegmentSelection selected) {
		TranslatedSegment translatedSegment = selected.translatedSegment;
		
		int viewHeight = getViewDimension().height;
		
		Graphics2D tempGfx = (Graphics2D) gfx.create();
		tempGfx.setStroke(new BasicStroke(2.5f));
		
		if (translatedSegment.headTailCrossPoint!=null) {
			paintSegmentPart(tempGfx, translatedSegment.base0, mouse, viewHeight);
			paintSegmentPart(tempGfx, translatedSegment.base1, mouse, viewHeight);
			paintSegmentPart(tempGfx, translatedSegment.base, mouse, viewHeight);
			
			paintSegmentPart(tempGfx, translatedSegment.translated0, mouse, viewHeight);
			paintSegmentPart(tempGfx, translatedSegment.translated1, mouse, viewHeight);
			paintSegmentPart(tempGfx, translatedSegment.translated, mouse, viewHeight);
		}


		paintSegmentPart(tempGfx, translatedSegment.base, mouse, viewHeight);
		paintSegmentPart(tempGfx, translatedSegment.translated, mouse, viewHeight);

		paintSegmentPart(tempGfx, translatedSegment.head, mouse, viewHeight);
		paintSegmentPart(tempGfx, translatedSegment.tail, mouse, viewHeight);

		
		//		Line2D line = translatedSegment.base.base;
//		Line2D translated = translatedSegment.translated.base;
//		
//		int viewHeight = getViewDimension().height;
//		
//		Point2D baseA = camera.toViewCoords(line.pointA(), viewHeight);
//		Point2D baseB = camera.toViewCoords(line.pointB(), viewHeight);
//		
//		
//		Point2D transA = camera.toViewCoords(translated.pointA(), viewHeight);
//		Point2D transB = camera.toViewCoords(translated.pointB(), viewHeight);
//		
//		int polyX[] = new int[4];
//		int polyY[] = new int[4];
//		
//		polyX[0] = (int) baseA.x;
//		polyX[1] = (int) baseB.x;
//		polyX[2] = (int) transB.x;
//		polyX[3] = (int) transA.x;
//
//		polyY[0] = (int) baseA.y;
//		polyY[1] = (int) baseB.y;
//		polyY[2] = (int) transB.y;
//		polyY[3] = (int) transA.y;
//		
//		gfx.setColor(new Color(255,128,255,40));
//		gfx.fillPolygon(polyX, polyY, 4);
////		
////		double cosAlpha = line.getAlpha();
////		int alpha = (int) Math.round(cosAlpha);
////		double size = Math.abs(mutableLine.thickness*2d) / camera.getZoom();
////		System.err.println("size="+size+" cosAlpha="+cosAlpha);
////		int sizeI = (int) Math.round(size);
////		int r = sizeI/2;
////		int s1 = (alpha+180) % 360;
////		int s2 = (alpha+270) % 360;
////		gfx.fillArc((int) baseA.x-r, (int) baseA.y-r, sizeI, sizeI, s1, 91);
////
////		
////		gfx.fillArc((int) baseB.x-r, (int) baseB.y-r, sizeI, sizeI, s2, 91);
//		
//		translatedSegment.translated.ensureOrdered();
//		int idx=0;
//		for(OverlapPoint overlapPoint : translatedSegment.translated.overlapPointsIterable()) {
//			paintHighlightedOverlapPoint(gfx, viewHeight, overlapPoint, idx);
//			idx++;
//		}

	}


	private void paintSegmentPart(Graphics2D gfx, Route route, Point2D mouse, int viewHeight) {
		Line2D line = route.base;
		int relativeCCW = line.relativeCCW(mouse);
		if (relativeCCW<0) {
			gfx.setColor(Color.red);
		} else if (relativeCCW>0) {
			gfx.setColor(Color.green);
		} else {
			gfx.setColor(Color.blue);
		}
		Point2D baseA = camera.toViewCoords(line.pointA(), viewHeight);
		Point2D baseB = camera.toViewCoords(line.pointB(), viewHeight);
		int xA = (int) baseA.x;
		int yA = (int) baseA.y;
		int xB = (int) baseB.x;
		int yB = (int) baseB.y;
		gfx.drawLine(xA, yA, xB, yB);
	}



	protected void paintHighlightedOverlapPoint(Graphics2D gfx, int viewHeight, OverlapPoint overlapPoint, int index) {
		Taint taint = overlapPoint.getTaint();
		gfx.setColor(taintColorMap.get(taint));
		Point2D point = camera.toViewCoords(overlapPoint.point, viewHeight);
		drawCircle(gfx, point, 5, true);
		
		gfx.setColor(Color.white);
		int textX = 11+(int) point.x;
		int textY = 6+(int) point.y;
		String text = ""+index;
		gfx.drawString(text, textX, textY);
		drawVisibleText(gfx, text, textX, textY, gfx.getColor(), Color.black);
	}


	
}
