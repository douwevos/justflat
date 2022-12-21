package net.github.douwevos.justflat.contour.scaler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import net.github.douwevos.justflat.Selection;
import net.github.douwevos.justflat.contour.scaler.ScalerViewableModel.OverlapPointSelection;
import net.github.douwevos.justflat.demo.Camera;
import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.ModelViewer;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;

public class RouteListScaledDownCombinatorViewer extends ModelViewer<RouteListScaledDownCombinatorViewableModel> {

	@Override
	public void paintOnTopLayer(Graphics2D gfx, RouteListScaledDownCombinatorViewableModel model) {
	}
	
	
	@Override
	protected void paintSelected(Graphics2D g, Selection<?> selected) {
		super.paintSelected(g, selected);
		if (selected instanceof OverlapPointSelection) {
			paintSelectedOverlapPoint(g, (OverlapPointSelection) selected);
		}
	}

	private void paintSelectedOverlapPoint(Graphics2D gfx, OverlapPointSelection selected) {
		OverlapPoint overlapPoint = selected.get();
		
		
		
		int mx = (int) Math.round(Math.cos(selected.alpha*Math.PI/180d)*50);
		int my = -(int) Math.round(Math.sin(selected.alpha*Math.PI/180d)*50);
		
		int viewHeight = getViewDimension().height;
		Point2D pvc = camera.toViewCoords(overlapPoint.point, viewHeight);
		
		int px = (int) pvc.x;
		int py = (int) pvc.y;
		
//		gfx.drawLine(px, py, px+mx, py+my);
		
		List<Line2D> lines = new ArrayList<>();
		for(Route route : overlapPoint.routeIterable()) {
//			paintRoute(gfx, route);
			int idx = route.indexOf(overlapPoint);
			OverlapPoint prevOP = route.overlapPointAt(idx-1);
			if (prevOP!=null) {
				lines.add(new Line2D(overlapPoint.point, prevOP.point));
			}
			OverlapPoint nextOP = route.overlapPointAt(idx+1);
			if (nextOP!=null) {
				lines.add(new Line2D(overlapPoint.point, nextOP.point));
			}
		}
		
		
		double bestAlphaDiff=0d;
		Line2D selectedLine = null;
		for(Line2D l : lines) {
			double alphaDiff = Math.abs(l.getAlpha() - selected.alpha);
			if ((selectedLine==null) || bestAlphaDiff>alphaDiff) {
				selectedLine = l;
				bestAlphaDiff = alphaDiff;
			}
		}
		
		BasicStroke efatStroke = new BasicStroke(8f);
		BasicStroke fatStroke = new BasicStroke(4f);
		BasicStroke thinStroke = new BasicStroke(1f);
		int r = 45;
//		int alphaBase = (int) (selectedLine.getAlpha()+180)%360;
		int alphaBase = (int) selectedLine.getAlpha();
		
		for(Line2D l : lines) {
			if (l == selectedLine) {
				gfx.setStroke(fatStroke);
				gfx.setColor(Color.cyan);
			} else {
				gfx.setColor(Color.yellow);
				gfx.setStroke(efatStroke);
				int alpha = (int) l.getAlpha();
				
				int diff = (2*360+alpha-alphaBase)%360;
//				System.out.println(""+r+" -> diff="+diff);
				
				gfx.drawArc(px-r, py-r, r*2, r*2, (int) alphaBase, (int) diff);
				gfx.setColor(Color.blue);
				gfx.setStroke(thinStroke);
				r+=20;
				
			}
			paintLine(gfx, l);
			
			
			
			
		}

		gfx.setStroke(thinStroke);
		paintSelectedPointWithLocation(gfx, overlapPoint.point);

		
	}


	@Override
	protected void paintModel(BufferedImage image, Graphics2D gfx, RouteListScaledDownCombinatorViewableModel model) {
		RouteListInteractionAnalyser analyser = model.getAnalyser();
		if (analyser==null || analyser.allRoutes==null) {
			return;
		}
		
//		Graphics2D gfx2 = (Graphics2D) gfx.create();
//		
//		
//		gfx2.setStroke(new BasicStroke(5));
//		gfx2.setColor(new Color(255,128,0,100));
//		RouteList routeList = analyser.routeListA;		
//		paintRouteLists(gfx2, List.of(routeList));
//
//		gfx2.setColor(new Color(0,128,255,100));
//		routeList = analyser.routeListB;		
//		paintRouteLists(gfx2, List.of(routeList));
//
//		gfx2.dispose();

//		gfx.setColor(Color.white);
//		gfx.setStroke(new BasicStroke(5));
		gfx.setColor(new Color(255,128,255,100));
		List<RouteList> routeLists = model.getCombined();
//		paintRouteLists(gfx, routeLists);

		analyser.allRoutes.stream().forEach(r -> paintReducedRoute(gfx, r));

		
		int index=0;
		for(OverlapPoint overlapPoint : analyser.overlapPoints) {
			paintOverlapPoint(gfx, overlapPoint, index++);
		}
		
	}

	private void paintRouteLists(Graphics2D gfx, List<RouteList> routeLists) {
		for(RouteList routeList : routeLists) {
			routeList.stream().forEach(route -> {
				paintRoute(gfx, route);
			});
		}
	}

	private void paintOverlapPoint(Graphics2D gfx, OverlapPoint overlapPoint, int index) {
		int viewHeight = getViewDimension().height;
		Camera camera = getCamera();
		
		Point2D p = camera.toViewCoords(overlapPoint.point, viewHeight);
		int x1 = (int) p.x;
		int y1 = (int) p.y;
		
		gfx.drawArc(x1-3, y1-3, 7, 7, 0, 360);
		
//		drawVisibleText(gfx, ""+index, x1+5, y1, Color.WHITE, Color.black);
		
	}

	private void paintRoute(Graphics2D gfx, Route route) {
		Line2D line = route.base;
		paintLine(gfx, line);
	}


	private void paintLine(Graphics2D gfx, Line2D line) {
		int viewHeight = getViewDimension().height;
		Camera camera = getCamera();
		Point2D pointA = line.pointA();
		Point2D pointB = line.pointB();
		Point2D pa = camera.toViewCoords(pointA, viewHeight);
		Point2D pb = camera.toViewCoords(pointB, viewHeight);
		
		int x1 = (int) pa.x;
		int y1 = (int) pa.y;
		int x2 = (int) pb.x;
		int y2 = (int) pb.y;
		gfx.drawLine(x1, y1, x2, y2);
	}


	private void paintReducedRoute(Graphics2D gfx, Route route) {
		int viewHeight = getViewDimension().height;
		Camera camera = getCamera();
		Line2D line = route.base;
		Point2D pointA = line.pointA();
		Point2D pointB = line.pointB();
		Point2D pa = camera.toViewCoords(pointA, viewHeight);
		Point2D pb = camera.toViewCoords(pointB, viewHeight);
		
		int x1 = (int) pa.x;
		int y1 = (int) pa.y;
		int x2 = (int) pb.x;
		int y2 = (int) pb.y;
		
		
		int dx = x2-x1;
		int dy = y2-y1;
		
		double lineLength = line.getLineLength()/camera.getZoom();
		dx = (int) Math.round((dx*11)/lineLength);
		dy = (int) Math.round((dy*11)/lineLength);
		
		gfx.drawLine(x1+dx, y1+dy, x2-dx, y2-dy);
	}
	
	@Override
	public boolean onDrag(ModelMouseEvent event, Selection<?> selected) {
		return false;
	}

}
