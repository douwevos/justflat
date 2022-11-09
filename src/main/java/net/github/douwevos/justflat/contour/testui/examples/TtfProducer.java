package net.github.douwevos.justflat.contour.testui.examples;

import java.io.File;
import java.io.IOException;

import net.github.douwevos.justflat.ttf.format.Ttf;
import net.github.douwevos.justflat.ttf.reader.TrueTypeFontParser;

public class TtfProducer {

	private static Ttf ttfFreeSans;
	
	static {
		TrueTypeFontParser ttfParser = new TrueTypeFontParser();
		try {
			ttfFreeSans = ttfParser.parse(new File("/usr/share/fonts/truetype/freefont/FreeSans.ttf"));
		} catch (IOException e) {
		}
	}

	
	public static Ttf getFreeSansTtf() {
		return ttfFreeSans;
	}
}
