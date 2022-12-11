package net.github.douwevos.justflat.contour.scaler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.ContourLayer.ContourDotSelection;
import net.github.douwevos.justflat.contour.ContourLayer.LineSelection;
import net.github.douwevos.justflat.demo.ModelMouseEvent;
import net.github.douwevos.justflat.demo.ModelViewer;
import net.github.douwevos.justflat.demo.Selection;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.values.Line2D;
import net.github.douwevos.justflat.types.values.Point2D;
import net.github.douwevos.justflat.types.values.StartStop;


@SuppressWarnings("serial")
public class ContourLayerViewer extends ModelViewer<ContourLayerViewableModel> {

	private static Log log = Log.instance(false);
	
	final Color colors[] = new Color[] { Color.red, Color.orange, Color.cyan, Color.magenta, Color.BLUE, Color.green, Color.pink, Color.yellow, new Color(192, 64, 64) };

	protected long scanLine = -1;
	
	public boolean drawDirections = false;
	public boolean drawDiscSize = false;
	
	public void setModel(ContourLayer discLayer) {
		setModel(discLayer==null ? null : new ContourLayerViewableModel(discLayer));
	}

	public void setDrawDirections(boolean drawDirections) {
		this.drawDirections = drawDirections;
	}
	
	@Override
	protected void paintModel(BufferedImage image, Graphics2D g, ContourLayerViewableModel model) {
		ContourLayer modelLayer = model.discLayer;

		Dimension viewDimension = getViewDimension();
		
		double transX =  -camera.getTranslateX();
		double transY =  camera.getTranslateY();
		double cameraZoom = camera.getZoom();
		

		if (drawDiscSize) {
			g.setColor(Color.yellow);
//			int discSize = (int) Math.ceil(modelLayer.getDiscSize()/cameraZoom);
//			g.drawArc(discSize, discSize, discSize, discSize, 0,360);
		}
		
		g.setColor(Color.red);

		Contour selectedContour = getSelectedContour();
		
		boolean drawDots = (20/cameraZoom) > 1d;
		int contourIdx = 0;
		
		for(Contour contour : modelLayer) {
			if (contour.isEmpty()) {
				continue;
			}
			g.setColor(Color.red);
			Point2D dotLast = contour.getLast();
			long xa = dotLast.x;
			long ya = dotLast.y;
			int ixa = (int) Math.round((transX + xa)/cameraZoom);
			int iya = viewDimension.height+(int) Math.round((transY - ya)/cameraZoom);
			
			g.setColor(Color.green);
			g.fillArc(ixa-5, iya-5, 9,9,0,360);

			contourIdx++;
			Color contourColor = colors[contourIdx % colors.length];
			if (contour.getIndex()!=null) {
				contourColor = colors[contour.getIndex() % colors.length];
			}
			
			boolean doDrawLast = true;

			if (fillWithAlpha) {
				int alpha = selectedContour==contour ? 160 : 80;
				Color colors = new Color(contourColor.getRed(), contourColor.getGreen(), contourColor.getBlue(), alpha);
				g.setColor(colors);
				int xs[] = new int[contour.dotCount()];  
				int ys[] = new int[contour.dotCount()];
				int xp = 0;
				for(Point2D dot : contour) {
					long xb = dot.x;
					long yb = dot.y;
					
					int ixb = (int) Math.round((transX + xb)/cameraZoom);
					int iyb = viewDimension.height+(int) Math.round((transY - yb)/cameraZoom);
					xs[xp] = ixb;
					ys[xp] = iyb;
					xp++;
				}
				g.fillPolygon(xs, ys, xs.length);
			}
			
			for(Point2D dot : contour) {
				long xb = dot.x;
				long yb = dot.y;
				
				int ixb = (int) Math.round((transX + xb)/cameraZoom);
				int iyb = viewDimension.height+(int) Math.round((transY - yb)/cameraZoom);
				g.setColor(contourColor);
				
				if (doDrawLast) {
					
					drawVector(g, ixa, iya, ixb, iyb);
					
				} else {
					doDrawLast = true;
				}
				
//				g.setColor(Color.cyan);
//				g.drawLine((int) pointA.x,(int) pointA.y, (int) pointB.x,(int) pointB.y);

				
				ixa = ixb;
				iya = iyb;

				if (drawDots) {
					g.setColor(Color.green);
					g.drawArc(ixa-2, iya-2, 5,5,0,360);
				}
			}
		}
		
		g.dispose();		
	}

	@Override
	public void paintOnTopLayer(Graphics2D gfx, ContourLayerViewableModel model) {
		drawScanLine(gfx, model);
	}

	
	private void drawScanLine(Graphics2D gfx, ContourLayerViewableModel viewableModel) {
		if (viewableModel==null || viewableModel.discLayer == null) {
			return;
		}
		
		double cameraZoom = camera.getZoom();

		Dimension viewDimension = getViewDimension();
		int viewBottom = viewDimension.height;

		int dy =  viewBottom- (int)  Math.round((-camera.getTranslateY() +scanLine)/cameraZoom);
		gfx.setColor(Color.orange);
		gfx.drawLine(0, dy, 4, dy);
		
		List<StartStop> startStop = viewableModel.discLayer.scanlineHorizontal(scanLine);
		if (startStop == null) {
			return;
		}

		int dx =  (int) Math.round((-camera.getTranslateX())/cameraZoom);
		for(StartStop ss : startStop) {
			int x1 = dx + (int) Math.round(ss.start/cameraZoom);
			int x2 = dx + (int) Math.round(ss.stop/cameraZoom);
			gfx.drawLine(x1, dy, x2, dy);
		}
		
	}

	protected void paintSelected(Graphics2D g, Selection<?> sel) {
		super.paintSelected(g, sel);
		Dimension viewDimension = getViewDimension();
		if (sel instanceof ContourLayer.ContourDotSelection) {
			ContourLayer.ContourDotSelection selection = (ContourLayer.ContourDotSelection) sel;
			if (selection.dotIndex>=0) {
				
				double transX =  -camera.getTranslateX();
				double transY =  camera.getTranslateY();
				double cameraZoom = camera.getZoom();

				Point2D dot = selection.getDot();
				
				long xb = dot.x;
				long yb = dot.y;
				
				int ixa = (int) Math.round((transX + xb)/cameraZoom);
				int iya = viewDimension.height+(int) Math.round((transY - yb)/cameraZoom);

				g.setColor(Color.green);
				g.drawArc(ixa-6, iya-6, 13,13,0,360);
			
				g.drawString(""+dot, ixa, iya);
			}
		} else if (sel instanceof ContourLayer.LineSelection) {
			ContourLayer.LineSelection selection = (ContourLayer.LineSelection) sel;
			if (selection.dotIndex>=0) {

				double transX =  -camera.getTranslateX();
				double transY =  camera.getTranslateY();
				double cameraZoom = camera.getZoom();
				
				Line2D line = selection.getLine();

				long xa = line.getFirstPoint().getX();
				long ya = line.getFirstPoint().getY();
				int ixa = (int) Math.round((transX + xa)/cameraZoom);
				int iya = viewDimension.height+(int) Math.round((transY - ya)/cameraZoom);

				Point2D sp = camera.toViewCoords(line.getSecondPoint(), viewDimension.height);

				
				Graphics2D gfx = (Graphics2D) g.create();
				gfx.setColor(Color.green);
				gfx.setStroke(new BasicStroke(4));
				gfx.drawLine(ixa, iya, (int) sp.x, (int) sp.y);
				
//				double cosAlphaMain = (line.getAlpha()+90d)%360d;
				double cosAlphaMain = 360-line.getAlpha();
				
				
				Point2D dot2A = selection.contour.dotAt(selection.dotIndex);
				Point2D dot2b = selection.contour.dotAt(selection.dotIndex-1);
				Line2D line2 = new Line2D(dot2A, dot2b);
//				double cosAlpha2 = (line2.getAlpha()+90d)%360d;
				double cosAlpha2 = 360-(line2.getAlpha());

				
				Point2D dot2aView = camera.toViewCoords(dot2A, viewDimension.height);
				

				double angle = cosAlpha2-cosAlphaMain;
				if (angle<0) {
					angle+=360;
				}
				gfx.drawArc((int) dot2aView.x-20, (int) dot2aView.y-20, 40, 40, (int) Math.round(cosAlphaMain), (int) Math.round(angle));
				gfx.dispose();
				
				Point2D intersectionPoint = selection.getIntersectionPoint();
				if (intersectionPoint != null) {
					g.setColor(Color.magenta);
					drawPointWithLocation(g, viewDimension, intersectionPoint);
				}
			}
		}		
	}
	
	public Contour getSelectedContour() {
		Object sel = this.highlighted;

		if (sel instanceof ContourLayer.ContourDotSelection) {
			ContourLayer.ContourDotSelection selection = (ContourLayer.ContourDotSelection) sel;
			return selection.contour;
		} else if (sel instanceof ContourLayer.LineSelection) {
			ContourLayer.LineSelection selection = (ContourLayer.LineSelection) sel;
			return selection.contour;
		}
		
		return null;
	}

	
	private void drawVector(Graphics2D g, int ixa, int iya, int ixb, int iyb) {
		g.drawLine(ixa, iya, ixb, iyb);
		if (!drawDirections) {
			return;
		}
		
		int dx = ixb-ixa; 
		int dy = iyb-iya; 
		
		double len = Math.sqrt(dx*dx + dy*dy);
		
		double s = len*0.07d;
		
		
		double nx = ixa + 2d*dx/3d;
		double ny = iya + 2d*dy/3d;
		int inx = (int) Math.round(nx);
		int iny = (int) Math.round(ny);

		double nx2 = ixa + 1.9d*dx/3d;
		double ny2 = iya + 1.9d*dy/3d;
		int inx2 = (int) Math.round(nx2);
		int iny2 = (int) Math.round(ny2);
		
		
		int px = (int) Math.round(dx*s/len);
		int py = (int) Math.round(dy*s/len);
		
		g.drawLine(inx, iny, inx2-py, iny2+px);
		g.drawLine(inx, iny, inx2+py, iny2-px);
//		g.drawLine(inx, iny, inx+py, iny+px);
		
	}

	
	@Override
	protected void onClicked(ModelMouseEvent modelEvent) {
		super.onClicked(modelEvent);
		if (modelEvent.event.getButton() == 3) {
			
			JPopupMenu p = new JPopupMenu();
			
			log.debug("selected="+highlighted);
			
			if (highlighted instanceof ContourDotSelection) {
				ContourDotSelection s = (ContourDotSelection) highlighted;
				Action actDeletePoint = new AbstractAction("Delete point") {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						s.contour.removeAt(s.dotIndex);
						layerImage = null;
						repaint();
					}
				};
				p.add(actDeletePoint);
			}

			if (highlighted instanceof LineSelection) {
				LineSelection l = (LineSelection) highlighted;
				Action actDummy = new AbstractAction("Add point") {
					@Override
					public void actionPerformed(ActionEvent e) {
						l.contour.addAt(new Point2D(Math.round(modelEvent.modelX), Math.round(modelEvent.modelY)), l.dotIndex+1);
						layerImage = null;
						repaint();
					}
				};
				p.add(actDummy);
			}
			
			p.show(this, modelEvent.event.getX(), modelEvent.event.getY());
		}
	}
	
	
	

	@Override
	public boolean onDrag(ModelMouseEvent event, Selection<?> selected) {
		return model==null ? false : model.discLayer.dragTo(selected, event.modelX, event.modelY);
	}
	
	
	
	@Override
	protected void onMove(ModelMouseEvent modelEvent) {
		double modelX = modelEvent.modelX;
		double modelY = modelEvent.modelY;
		Contour oldContour = getSelectedContour();
		highlighted = model==null ? null : model.discLayer.selectAt(modelX, modelY, camera.getZoom());
		Contour newContour = getSelectedContour();
		long ns = (long) modelY;
		if (scanLine != ns || newContour!=oldContour) {
			if (newContour!=oldContour) {
				layerImage = null;
			}
			scanLine = ns;
			repaint();
		}
//		log.debug("scanLine="+scanLine+", loc="+loc);
	
	}
}
