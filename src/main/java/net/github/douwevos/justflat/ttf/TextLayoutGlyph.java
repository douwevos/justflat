package net.github.douwevos.justflat.ttf;

import java.util.ArrayList;
import java.util.List;

import net.github.douwevos.justflat.ttf.format.TtfGlyphData.Contour;
import net.github.douwevos.justflat.ttf.format.TtfGlyphData.GlyphDefinition;
import net.github.douwevos.justflat.ttf.format.TtfGlyphData.GlyphDot;
import net.github.douwevos.justflat.types.values.Point2D;

public class TextLayoutGlyph {

	private final char ch;
	private final int glyphIndex;
	private final GlyphDefinition glyphDefinition;
	private final int x1;
	private final int x2;
	
	
	public TextLayoutGlyph(char ch, int glyphIndex, GlyphDefinition glyphDefinition, int x1, int x2) {
		this.ch = ch;
		this.glyphIndex = glyphIndex;
		this.glyphDefinition = glyphDefinition;
		this.x1 = x1;
		this.x2 = x2;
	}
	
	public TextLayoutGlyph withX2(int x2) {
		return new TextLayoutGlyph(ch, glyphIndex, glyphDefinition, x1, x2);
	}
	
	public int getX1() {
		return x1;
	}
	
	public int getX2() {
		return x2;
	}

	public GlyphDefinition getGlyphDefinition() {
		return glyphDefinition;
	}
	
	
	public void produce(GlyphOutput output, double scalar, long xpos, long ypos) {
		boolean drawCurves = true;
		ArrayList<Contour> contourList = getGlyphDefinition().getContourList();
		int glyphLeft = getX1();
		double zoomFactor = scalar;
		
		for (Contour contour : contourList) {
			
			output.contourBegin();
			ArrayList<GlyphDot> dotList = contour.dotList;

			int dotCount = dotList.size();
			GlyphDot dot = dotList.get(0);
			GlyphDot lastDot = dot; 
			double x0 = (double) lastDot.x + glyphLeft;
			double y0 = (double) lastDot.y;

			Point2D pointA = Point2D.of(xpos + (long) Math.round(x0*zoomFactor), ypos + (long) Math.round(y0*zoomFactor));
			
			for(int dotIdx=0; dotIdx<=dotCount; dotIdx++) {
				dot = dotList.get(dotIdx % dotCount);
				GlyphDot nextDot = dotList.get((dotIdx+1)%dotCount);
				
				double x1 = dot.x + glyphLeft;
				double y1 = dot.y;
//				log.log(""+(dotIdx % dotCount)+" ## x0="+x0+", y0="+y0+", x1="+x1+", y1="+y1);
				if (dot.isCurve && drawCurves) {

					
					double x2 = (double) nextDot.x + glyphLeft;
					double y2 = (double) nextDot.y;
					
					if (nextDot.isCurve) {
						x2 = (x1+x2)/2f;
						y2 = (y1+y2)/2f;
					}
					double xa = (x0+2f*x1)/3f;
					double ya = (y0+2f*y1)/3f;

					double xb = (2f*x1+x2)/3f;
					double yb = (2f*y1+y2)/3f;

					pointA = layerCurveTo(output, pointA
							, xpos + x0*zoomFactor, ypos + y0*zoomFactor
							, xpos + xa*zoomFactor, ypos + ya*zoomFactor
							, xpos + xb*zoomFactor, ypos + yb*zoomFactor
							, xpos + x2*zoomFactor, ypos + y2*zoomFactor);

					x0 = x2;
					y0 = y2;
				} else {
//					if (nextDot.isCurve) {
//						x1 = (x1+x0)/2f;
//						y1 = (y1+y0)/2f;
//					}
					Point2D pointB = Point2D.of(xpos + (int) Math.round(x1*zoomFactor), ypos + (int) Math.round(y1*zoomFactor));
					output.line(pointA, pointB);
//					gfx.drawLine(xpos + (int) (x0*zoomFactor), ypos + (int) (y0*zoomFactor), xpos + (int) (x1*zoomFactor), ypos + (int) (y1*zoomFactor));
					x0 = x1;
					y0 = y1;
					pointA = pointB;
				}
			}
			
			output.contourEnd();
			
		}
			
		
	}
	

	private Point2D layerCurveTo(GlyphOutput output, Point2D pointA, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
		double xp[] = new double[] {x0,x1,x2,x3};
		double yp[] = new double[] {y0,y1,y2,y3};
		List<XY> bezierCurve = bezierCurve(xp, yp);
		
		for(XY xy : bezierCurve) {
			Point2D pointB = Point2D.of(Math.round(xy.x), Math.round(xy.y));
			output.line(pointA, pointB);
			pointA = pointB;
		}
		return pointA;
	}

	List<XY> bezierCurve(double x[] , double y[]) {
	    double xu = 0.0 , yu = 0.0;
	    List<XY> result = new ArrayList<>();
	    for(int p=0; p<=100; p++) {
	    	double u = p/100d;
	    	double v = 1.0d - u;
	    	double powMU2 = v*v;
	        double powMU3 = powMU2*v;
			double powU2 = u*u;
			double powU3 = powU2*u;
			xu = powMU3*x[0]+3*u*powMU2*x[1]+3*powU2*(v)*x[2] + powU3*x[3];
	        yu = powMU3*y[0]+3*u*powMU2*y[1]+3*powU2*(v)*y[2] + powU3*y[3];
	        result.add(new XY(xu, yu));
	    }
	    return result;
	}



	static class XY {
		public final double x;
		public final double y;
		
		public XY(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	
	
	public interface GlyphOutput {
		void contourBegin();
		void line(Point2D pointA, Point2D pointB);
		void contourEnd();
	}
}
