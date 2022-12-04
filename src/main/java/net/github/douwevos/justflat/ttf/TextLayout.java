package net.github.douwevos.justflat.ttf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import net.github.douwevos.justflat.ttf.format.Ttf;
import net.github.douwevos.justflat.ttf.format.TtfCharacterMap;
import net.github.douwevos.justflat.ttf.format.TtfGlyphData;
import net.github.douwevos.justflat.ttf.format.TtfGlyphData.GlyphDefinition;
import net.github.douwevos.justflat.ttf.format.TtfGlyphData.GlyphDot;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.ttf.format.TtfHorizontalHeader;

public class TextLayout implements Iterable<TextLayoutGlyph> {

	private final Ttf ttf;
	private final TextLayoutGlyph glyphs[];
	
	public TextLayout(Ttf ttf, String text) {
		this.ttf = ttf;
		
		glyphs = new TextLayoutGlyph[text.length()];
		
		TextLayoutGlyph spaceGlyphLayout = produceGlyphLayout(ttf, 'd', 0);
		int x = 0;
		for(int textIdx=0; textIdx<text.length(); textIdx++) {
			char charAt = text.charAt(textIdx);
			TextLayoutGlyph glyphLayout = produceGlyphLayout(ttf, charAt, x);
			if (charAt == ' ') {
				glyphLayout = glyphLayout.withX2(glyphLayout.getX1() + spaceGlyphLayout.getX2());
			}
			
			x = glyphLayout.getX2();
			glyphs[textIdx] = glyphLayout;
			
		}
	}
	
	public int getMaxHeight() {
		TtfHorizontalHeader horizontalHeader = ttf.getHorizontalHeader();
		int ascent = horizontalHeader.getAscent().getValue();
		int descent = horizontalHeader.getDescent().getValue();
		return ascent+-descent;
	}
	
	public TextLayoutGlyph[] getGlyphs() {
		return glyphs;
	}
	
	@Override
	public Iterator<TextLayoutGlyph> iterator() {
		return Arrays.asList(glyphs).iterator();
	}
	
	
	private TextLayoutGlyph produceGlyphLayout(Ttf ttf, char charAt, int xPos) {
		TtfCharacterMap characterMap = ttf.getCharacterMap();
		TtfGlyphData glyphData = ttf.getGlyphData();

		int glyphIndex = characterMap.getGlyphIndex(charAt);
		
		TtfGlyphData.GlypDescription description = glyphData.glypDescriptions.get(glyphIndex);
		GlyphDefinition glyphDefinition = description.getGlyphDefinition();
		
//		glyphDefinition.dump("  ");
		int xmax = 20;
		if (description != null && description.getXMax()!=null) {
			xmax = description.getXMax().getValue() + description.getXMin().getValue();
		} else {
			xmax = ttf.getHead().getXMay().getValue();
		}

		return new TextLayoutGlyph(charAt, glyphIndex, glyphDefinition, xPos, xPos+xmax);
	}


	
	public Bounds2D calculateBounds() {
		
		Bounds2D result = null;
		
		for(TextLayoutGlyph glyphLayout : glyphs) {
			ArrayList<GlyphDot> dotList = glyphLayout.getGlyphDefinition().getDotList();
			if (result == null && !dotList.isEmpty()) {
				GlyphDot firstDot = dotList.get(0);
				result = new Bounds2D(firstDot.x, firstDot.y, firstDot.x, firstDot.y);
			}
			for(GlyphDot dot : dotList) {
				result = result.extend(dot.x, dot.y);
			}
		}
		return result;
	}
	
}
