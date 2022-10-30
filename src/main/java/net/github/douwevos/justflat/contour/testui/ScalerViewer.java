package net.github.douwevos.justflat.contour.testui;
//package net.github.douwevos.cnc.layer;
//
//import java.awt.BasicStroke;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Graphics2D;
//import java.awt.event.MouseEvent;
//import java.awt.image.BufferedImage;
//import java.util.List;
//
//import net.github.douwevos.cnc.layer.ScalerViewableModel.CrossPointSelection;
//import net.github.douwevos.cnc.layer.ScalerViewableModel.TranslatedSegmentSelection;
//import net.github.douwevos.cnc.layer.disc.Line;
//import net.github.douwevos.cnc.layer.disc.Line.IntersectionInfo;
//import net.github.douwevos.cnc.layer.disc.scaler.CrossPoint;
//import net.github.douwevos.cnc.layer.disc.scaler.MutableContour;
//import net.github.douwevos.cnc.layer.disc.scaler.MutableLine;
//import net.github.douwevos.cnc.layer.disc.scaler.TargetLine;
//import net.github.douwevos.cnc.type.Point2D;
//
//public class ScalerViewer extends ModelViewer<ScalerViewableModel> {
//
//	TargetLine selectedTargetLine;
//	
//	double mouseModelX;
//	double mouseModelY;
//	
//	
//	@Override
//	public void paintOnTopLayer(Graphics2D gfx, ScalerViewableModel model) {
//		if (selectedTargetLine != null) {
//			Point2D point = new Point2D(Math.round(mouseModelX), Math.round(mouseModelY));
//			paintProjection(gfx, model, selectedTargetLine, point);
//		}
//	}
//
//
//
//	private void paintProjection(Graphics2D gfx, ScalerViewableModel model, TargetLine targetLine,
//			Point2D point) {
//		int viewHeight = getViewDimension().height;
//		gfx.setColor(Color.CYAN);
//		MutableLine mutableLine = targetLine.getMutableLine();
//		Line translated = mutableLine.base;
//		
//		IntersectionInfo info = new IntersectionInfo();
//		Point2D intersectionPoint = translated.intersectionPoint(point, info);
//		if (info.ua>=0d && info.ua<=1d) {
//			if (intersectionPoint != null) {
//				Point2D pa = camera.toViewCoords(intersectionPoint, viewHeight);
//				Point2D pb = camera.toViewCoords(point, viewHeight);
//				gfx.drawLine((int) pa.x, (int) pa.y, (int) pb.x, (int) pb.y);
//			}
//		}
//	}
//
//
//
//	@Override
//	protected void paintSelected(Graphics2D g, Object sel) {
//		if (sel instanceof CrossPointSelection) {
//			CrossPointSelection crossPointSelection = (CrossPointSelection) sel;
//			paintSelectedPointWithLocation(g, crossPointSelection.crossPoint.crossPoint);
//		} else if (sel instanceof TranslatedSegmentSelection) {
//			paintTranslatedSegmentSelection(g, (TranslatedSegmentSelection) sel);
//		}
//
//	}
//	
//
//	private void paintTranslatedSegmentSelection(Graphics2D gfx, TranslatedSegmentSelection selection) {
//		Line line = selection.get();
//		Dimension viewDimension = getViewDimension();
//		int viewHeight = viewDimension.height;
//		
//
//		Graphics2D subGfx = (Graphics2D) gfx.create();
//		subGfx.setColor(new Color(255,0,255,100));
////		
////		MutableLine mlMain = targetLine.getMutableLine();
////		Line asLineMain = targetLine.asLine();
////		
////		CrossPoint mainCpLeft = mlMain.findCrossPoint(asLineMain.getFirstPoint());
////		CrossPoint mainCpRight = mlMain.findCrossPoint(asLineMain.getSecondPoint());
////		double leftOffset = mlMain.crossPointProjectionOffset(mainCpLeft);
////		double rightOffset = mlMain.crossPointProjectionOffset(mainCpRight);
////		
////		
////		Line projectionMain = mlMain.base.createByOffsets(rightOffset, leftOffset);
////		
////		Line projectionLeft = new Line(projectionMain.getSecondPoint(), asLineMain.getFirstPoint());
////		Line projectionRight = new Line(asLineMain.getSecondPoint(), projectionMain.getFirstPoint());
////
////		
////		simplyDrawLine(subGfx, asLineMain);
////		simplyDrawLine(subGfx, projectionMain);
////		simplyDrawLine(subGfx, projectionLeft);
////		simplyDrawLine(subGfx, projectionRight);
////		subGfx.dispose();
//		
//	}
//
//
//
//	private void simplyDrawLine(Graphics2D subGfx, Line line) {
//		subGfx.setStroke(new BasicStroke(8f));
//		Dimension viewDimension = getViewDimension();
//		int viewHeight = viewDimension.height;
//		Point2D pointA = camera.toViewCoords(line.getFirstPoint(), viewHeight);
//		Point2D pointB = camera.toViewCoords(line.getSecondPoint(), viewHeight);
//		subGfx.drawLine((int) pointA.x, (int) pointA.y, (int) pointB.x, (int) pointB.y);
//		
//		subGfx.setStroke(new BasicStroke(15f));
//		Line tail = line.createByOffsets(0.8d, 1.0d);
//		pointA = camera.toViewCoords(tail.getFirstPoint(), viewHeight);
//		pointB = camera.toViewCoords(tail.getSecondPoint(), viewHeight);
//		subGfx.drawLine((int) pointA.x, (int) pointA.y, (int) pointB.x, (int) pointB.y);
//
//	}
//
//	@Override
//	protected void paintModel(BufferedImage image, Graphics2D gfx, ScalerViewableModel viewableModel) {
//		List<MutableContour> mutableContours = viewableModel.mutableContours;
//		Dimension viewDimension = getViewDimension();
//		int viewHeight = viewDimension.height;
//
//		gfx.setStroke(new BasicStroke(15f));
//		gfx.setColor(Color.blue.darker());
//
//		for(MutableContour mutableContour : mutableContours) {
//			drawMutableSegment(gfx, mutableContour, viewHeight);
//		}
//
//		gfx.setStroke(new BasicStroke(1f));
//
//		for(MutableContour mutableContour : mutableContours) {
//			drawMutableContour(gfx, mutableContour, viewHeight);
//		}
//
//		double cameraZoom = camera.getZoom();
//		boolean drawDots = (20/cameraZoom) > 1d;
//		if (drawDots) {
//			for(MutableContour mutableContour : mutableContours) {
//				drawDots(gfx, mutableContour, viewHeight);
//			}
//		}
//		
//		
//		if (selectedTargetLine != null) {
//			paintTargetLine(image, gfx, viewableModel, selectedTargetLine);
//		}
//	}
//	
//	private void paintTargetLine(BufferedImage image, Graphics2D gfx, ScalerViewableModel viewableModel,
//			TargetLine targetLine) {
//		MutableLine mutableLine = targetLine.getMutableLine();
//		Line line = mutableLine.base;
//		Line translated = mutableLine.translated;
//		
//		int viewHeight = getViewDimension().height;
//		
//		Point2D baseA = camera.toViewCoords(line.pointA(), viewHeight);
//		Point2D baseB = camera.toViewCoords(line.pointB(), viewHeight);
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
//
//	}
//
//
//
//	private void drawDots(Graphics2D gfx, MutableContour mutableContour, int viewHeight) {
//		gfx.setColor(Color.white);
//		mutableContour.streamSegments()
//			.flatMap(s -> s.streamOverlapPoints())
//			.map(p -> p.point)
//			.forEach(p -> {
//				Point2D pointA = camera.toViewCoords(p, viewHeight);
//				drawCircle(gfx, pointA, 2, false);
//			});
//	}
//
//
//	private void drawCircle(Graphics2D gfx, Point2D point, int size, boolean doFill) {
//		if (point==null) {
//			return;
//		}
//		int x = (int) point.x;
//		int y = (int) point.y;
//		int d = size*2+1;
//		if (doFill) {
//			gfx.fillArc(x-size, y-size, d,d,0,360);
//		} else {
//			gfx.drawArc(x-size, y-size, d,d,0,360);
//		}
//	}
//
//	private void drawMutableSegment(Graphics2D gfx, MutableContour mutableContour, int viewHeight) {
////		for(MutableLine mutableLine : mutableContour) {
////			drawMutableSegment(gfx, mutableLine, viewHeight);
////		}
//	}
//	
//	private void drawMutableSegment(Graphics2D gfx, MutableLine mutableLine, int viewHeight) {
//		if (!mutableLine.cleanSection) {
//			return;
//		}
//		Point2D pointA = camera.toViewCoords(mutableLine.base.getFirstPoint(), viewHeight);
//		Point2D pointB = camera.toViewCoords(mutableLine.base.getSecondPoint(), viewHeight);
//		gfx.drawLine((int) pointA.x, (int) pointA.y, (int) pointB.x, (int) pointB.y);
//	}
//
//
//	private void drawMutableContour(Graphics2D gfx, MutableContour mutableContour, int viewHeight) {
////		for(MutableLine mutableLine : mutableContour) {
////			drawMutableLine(gfx, mutableLine, viewHeight);
////		}
//	}
//
//	private void drawMutableLine(Graphics2D gfx, MutableLine mutableLine, int viewHeight) {
//		Point2D pointA = camera.toViewCoords(mutableLine.base.getFirstPoint(), viewHeight);
//		Point2D pointB = camera.toViewCoords(mutableLine.base.getSecondPoint(), viewHeight);
//		gfx.setColor(Color.yellow.darker().darker());
//		gfx.drawLine((int) pointA.x, (int) pointA.y, (int) pointB.x, (int) pointB.y);
//
////		mutableLine.getTargetLines()
////		pointA = camera.toViewCoords(mutableLine.translated.getFirstPoint(), viewHeight);
////		pointB = camera.toViewCoords(mutableLine.translated.getSecondPoint(), viewHeight);
////		gfx.setColor(Color.cyan.darker().darker());
////		gfx.drawLine((int) pointA.x, (int) pointA.y, (int) pointB.x, (int) pointB.y);
//
//		
//		List<TargetLine> targetLines = mutableLine.getTargetLines();
//		for(TargetLine tl : targetLines) {
//			
//			Color color = Color.white;
//			
//			if (tl.isTainted()) {
//				color = tl.isFlipped() ? Color.magenta : Color.red;
//			} else {
//				color = tl.isFlipped() ? Color.cyan : Color.green;
//			}
//			
//			if (tl.isHidden()) {
//				color = color.darker().darker().darker();
//			} else {
//				color = color.brighter();
//			}
//			gfx.setColor(color);
//			
//			Line asLine = tl.asLine();
//			pointA = camera.toViewCoords(asLine.getFirstPoint(), viewHeight);
//			pointB = camera.toViewCoords(asLine.getSecondPoint(), viewHeight);
//			drawVector(gfx, (int) pointA.x, (int) pointA.y, (int) pointB.x, (int) pointB.y);
//			
////			if (!tl.isTainted()) {
////				drawDirectedNormalLine(gfx, asLine, viewHeight);				
////			}
//			
//		}
//	}
//	
//	
//	private void drawVector(Graphics2D g, int ixa, int iya, int ixb, int iyb) {
//		g.drawLine(ixa, iya, ixb, iyb);
////		if (!drawDirections) {
////			return;
////		}
//		
//		int dx = ixb-ixa; 
//		int dy = iyb-iya; 
//		
//		double len = Math.sqrt(dx*dx + dy*dy);
//		
//		double rp = len*2/3d;
//		
//		double s = len*0.07d;
//		
//		
//		double nx = ixa + 2d*dx/3d;
//		double ny = iya + 2d*dy/3d;
//		int inx = (int) Math.round(nx);
//		int iny = (int) Math.round(ny);
//
//		double nx2 = ixa + 1.9d*dx/3d;
//		double ny2 = iya + 1.9d*dy/3d;
//		int inx2 = (int) Math.round(nx2);
//		int iny2 = (int) Math.round(ny2);
//		
//		
//		int px = (int) Math.round(dx*s/len);
//		int py = (int) Math.round(dy*s/len);
//		
//		g.drawLine(inx, iny, inx2-py, iny2+px);
//		g.drawLine(inx, iny, inx2+py, iny2-px);
////		g.drawLine(inx, iny, inx+py, iny+px);
//		
//	}
//
//	@Override
//	public boolean onDrag(MouseEvent event, Object selected, double mouseX, double mouseY) {
//		return false;
//	}
//
//	@Override
//	protected void onMove(MouseEvent event, double modelX, double modelY) {
//		
//		mouseModelX = modelX;
//		mouseModelY = modelY;
//
//		
//		Object selectedOld = selected;
//		selected = model==null ? null : model.selectAt(modelX, modelY, camera.getZoom());
//
////		if (selectedOld == selected) {
////			return;
////		}
//		
//		repaint();
//	}
//	
//	@Override
//	protected void onClicked(MouseEvent event, double modelX, double modelY) {
//		Object selectOnClick = model==null ? null : model.selectAt(modelX, modelY, camera.getZoom());
////		if (selectOnClick instanceof TargetLineSelection) {
////			TargetLineSelection targetLineSelection = (TargetLineSelection) selectOnClick;
////			selectedTargetLine = targetLineSelection.get();
////			layerImage = null;
////			repaint();
////		} else {
//			if (selectedTargetLine!=null) {
//				selectedTargetLine = null;
//				layerImage = null;
//				repaint();
//			}
////		}
//	}
//	
//	
//	
//}
