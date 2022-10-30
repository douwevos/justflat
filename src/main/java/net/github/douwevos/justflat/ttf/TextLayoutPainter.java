package net.github.douwevos.justflat.ttf;

import java.awt.Graphics2D;

import net.github.douwevos.justflat.types.Point2D;


public class TextLayoutPainter {

	private final TextLayout textLayout;
	private final double scalar;
	
	public TextLayoutPainter(TextLayout textLayout, int size) {
		this.textLayout = textLayout;
		scalar = (double) size/textLayout.getMaxHeight();
	}
	

	
	public void draw(Graphics2D gfx, int x, int y) {
		GfxGlyphOutput glyphOutput = new GfxGlyphOutput(gfx, scalar, textLayout.getMaxHeight());
		for(TextLayoutGlyph glyph : textLayout) {
			glyph.produce(glyphOutput, scalar, x, y);
//			draw(gfx, glyph, x, y);
		}
	}
	
	private static class GfxGlyphOutput implements TextLayoutGlyph.GlyphOutput {

		private final Graphics2D gfx;
		private final double scalar;
		private final int maxHeight;
		
		public GfxGlyphOutput(Graphics2D gfx, double scalar, int maxHeight) {
			
			this.gfx = gfx;
			this.scalar = scalar;
			this.maxHeight = (int) (maxHeight*scalar);
		}

		@Override
		public void line(Point2D pointA, Point2D pointB) {
			int xa = (int) pointA.x;
//			int ya = maxHeight - (int) pointA.y;
			int ya = (int) pointA.y;
			int xb = (int) pointB.x;
			int yb = (int) pointB.y;
			gfx.drawLine(xa, ya, xb, yb);
		}

		@Override
		public void contourBegin() {
		}

		@Override
		public void contourEnd() {
		}
		
	}

}
