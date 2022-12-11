package net.github.douwevos.justflat.ttf.format;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.ttf.TextLayout;
import net.github.douwevos.justflat.ttf.TextLayoutPainter;
import net.github.douwevos.justflat.ttf.format.TtfGlyphData.Contour;
import net.github.douwevos.justflat.ttf.format.TtfGlyphData.GlyphDefinition;
import net.github.douwevos.justflat.ttf.format.TtfGlyphData.GlyphDot;
import net.github.douwevos.justflat.ttf.reader.TrueTypeFontParser;

public class TtfViewer extends JFrame {

	private static final Log log = Log.instance(false);
	
	public static void main(String[] args) {
		TtfViewer ttfViewer = new TtfViewer();
		ttfViewer.setBounds(0,0, 1600,1800);
		ttfViewer.setVisible(true);
	}
	
	
	public TtfViewer() {
		setLayout(new BorderLayout());
		TtfPanel ttfPanel = new TtfPanel();
		getContentPane().add(ttfPanel, BorderLayout.CENTER);

		try {
			TrueTypeFontParser ttfParser = new TrueTypeFontParser();
			Ttf ttf = ttfParser.parse(new File("/usr/share/fonts/truetype/freefont/FreeSans.ttf"));
//			Ttf ttf = ttfParser.parse(new File("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"));
//			Ttf ttf = ttfParser.parse(new File("/opt/android-studio/jre/lib/fonts/DroidSansMono.ttf"));
		
			
			ttf.dump();
			
			ttfPanel.seTtf(ttf);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	static class TtfPanel extends JPanel implements MouseListener {

		Ttf ttf;
		
		public void seTtf(Ttf ttf) {
			this.ttf = ttf;
			addMouseListener(this);
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			Graphics2D gfx = (Graphics2D) g;
			
			if (ttf==null) {
				return;
			}
			
//			drawTtfText(gfx, "HWAallo", 0, 0, 80);
			
			TextLayout textLayout = new TextLayout(ttf, "Blauw.");
			TextLayoutPainter textLayoutPainter = new TextLayoutPainter(textLayout, 800);
			textLayoutPainter.draw(gfx, 0, 1000);

			
		}

		
		private void drawTtfText(Graphics2D gfx, String text, int x, int y, int size) {

			TtfGlyphData glyphData = ttf.getGlyphData();
			
			offset = offset%glyphData.glypDescriptions.size();
			
			
			TtfHead head = ttf.getHead();
			int mainXMax = head.getXMay().getValue();
			int mainYMax = head.getYMax().getValue();
			int mainXMin = head.getXMin().getValue();
			int mainYMin = head.getYMin().getValue();
			log.log(" mainXMax="+mainXMax+", mainYMax="+mainYMax+", mainXMin="+mainXMin+", mainYMin="+mainYMin);
			
			for(int textIdx=0; textIdx<text.length(); textIdx++) {
				
				TtfCharacterMap characterMap = ttf.getCharacterMap();
				char charAt = text.charAt(textIdx);
				int glyphIndex = characterMap.getGlyphIndex(charAt);
				log.log("charAt="+charAt+"("+((int) charAt)+"), glyphIndex="+glyphIndex);
				
				TtfGlyphData.GlypDescription description = glyphData.glypDescriptions.get(glyphIndex);
				if (description==null || description.getGlyphDefinition()==null) {
					return;
				}

				int xMin = description.getXMin().getValue();
				int yMin = description.getYMin().getValue();

				int xMax = description.getXMax().getValue();
				int yMax = description.getYMax().getValue();
				
				
				log.log(" xMin="+xMin+", yMin="+yMin+", xMax="+xMax+", yMax="+yMax);
				
//				description.dump("");
				GlyphDefinition glyphDefinition = description.getGlyphDefinition();
				
				ArrayList<Contour> contourList = glyphDefinition.getContourList();
	
				float zoomFactor = 0.08f;
	
				
				gfx.setColor(Color.blue);
				draw(gfx, x + textIdx*1500, y, contourList, zoomFactor, true);
				
			}
			
		}

		public void draw(Graphics2D gfx, float xpos, float ypos, ArrayList<Contour> contourList, float zoomFactor, boolean drawCurves) {
			drawCurves = false;
			for (Contour contour : contourList) {
				ArrayList<GlyphDot> dotList = contour.dotList;
	
				
//				gfx.setXORMode(new Color(-1^gfx.getColor().getRGB()));
				gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
				GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	
				boolean first = true;
				GlyphDot dot = dotList.get(dotList.size()-1);
				float x0 = dot.x+xpos;
				float y0 = dot.y+ypos;
				for(int dotIdx=0; dotIdx<=contour.dotList.size()*2; dotIdx++) {
					if (dotIdx==contour.dotList.size()) {
						Point2D currentPoint = path.getCurrentPoint();
						path.reset();
						path.moveTo((float)currentPoint.getX(), (float) currentPoint.getY());
					}
					dot = dotList.get(dotIdx%contour.dotList.size());
					if (first) {
						first = false;
						x0 = dot.x+xpos;
						y0 = dot.y+ypos;
						
						GlyphDot nextDot = dotList.get((dotIdx+1)%dotList.size());
						if (nextDot.isCurve) {
							x0 = (x0+nextDot.x+xpos)/2f;
							y0 = (y0+nextDot.y+ypos)/2f;
						}
	
						path.moveTo(x0*zoomFactor, y0*zoomFactor);
					} else if (dot.isCurve && drawCurves) {
						dotIdx++;
						GlyphDot nextDot = dotList.get((dotIdx)%dotList.size());
						dotIdx--;
						float x2 = (float) nextDot.x+xpos;
						float y2 = (float) nextDot.y+ypos;
	
						float x1 = dot.x+xpos;
						float y1 = dot.y+ypos;
						
						if (nextDot.isCurve) {
							x2 = (x1+x2)/2f;
							y2 = (y1+y2)/2f;
						}
						
	//					path.quadTo(dot.x*zoomFactor, dot.y*zoomFactor, x2*zoomFactor, y2*zoomFactor);
						
						
						float xa = (x0+2f*x1)/3f;
						float ya = (y0+2f*y1)/3f;
	
						float xb = (2f*x1+x2)/3f;
						float yb = (2f*y1+y2)/3f;
						
						
						path.curveTo(xa*zoomFactor, ya*zoomFactor, xb*zoomFactor, yb*zoomFactor, x2*zoomFactor, y2*zoomFactor);
	
//						int startX=10;
//						int startY=10;
//						 
//						int endX=300;
//						int endY=10;
//						 
//						int bezierX=150;
//						int bezierY=200;
//						 
//						for(double t=0.0;t<=1;t+=0.01)
//						{
//						    int x = (int) (  (1-t)*(1-t)*startX + 2*(1-t)*t*bezierX+t*t*endX);
//						    int y = (int) (  (1-t)*(1-t)*startY + 2*(1-t)*t*bezierY+t*t*endY);
//						 
//						    //plot something @  x,y coordinate here...
//						}						
						
						
	//					gfx.drawRect(-2+(int) (xa*zoomFactor), -2+(int) (ya*zoomFactor), 5, 5);
						
						if (nextDot.isCurve) {
	//						x0 = (dot.x+x2)*0.5f;
	//						y0 = (dot.y+y2)*0.5f;
							x0 = xb;
							y0 = yb;
						} else {
							x0 = x2;
							y0 = y2;
						}
						
						
					} else {
						x0 = dot.x+xpos;
						y0 = dot.y+ypos;
						path.lineTo(x0*zoomFactor, y0*zoomFactor);
					}
					
					
	//				gfx.drawRect(-1+(int) (x0*zoomFactor), -1+(int) (y0*zoomFactor), 3, 3);
				}
				
				path.closePath();
				
				
				
				AffineTransform at = new AffineTransform(1, 0, 0, -1, -1, 150);
				gfx.setTransform(at);
				gfx.draw(path);
			}
			
		}
		
		int offset = 0;
		
		public void mouseClicked(MouseEvent e) {
			int ex = e.getModifiersEx();
			offset++;
			if ((ex & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
				log.log("offset="+offset);
			} else if ((ex & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					offset-=2;
			}
			repaint();
		}

		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}

