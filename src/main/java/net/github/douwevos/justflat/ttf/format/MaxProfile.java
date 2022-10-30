package net.github.douwevos.justflat.ttf.format;

import java.io.IOException;

import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.ttf.format.types.Fixed;
import net.github.douwevos.justflat.ttf.format.types.Uint16;
import net.github.douwevos.justflat.ttf.reader.TtfInputStream;

public class MaxProfile {
	private static final Log log = Log.instance();
	
	private Fixed  	version;
	private Uint16 	numGlyphs;
	private Uint16 	maxPoints;
	private Uint16 	maxContours;
	private Uint16 	maxComponentPoints;
	private Uint16 	maxComponentContours;
	private Uint16 	maxZones;
	private Uint16 	maxTwilightPoints;
	private Uint16 	maxStorage;
	private Uint16 	maxFunctionDefs;
	private Uint16 	maxInstructionDefs;
	private Uint16 	maxStackElements;
	private Uint16 	maxSizeOfInstructions;
	private Uint16 	maxComponentElements;
	private Uint16 	maxComponentDepth;

	
	public void dump(String pre) {
		log.log("MaxProfile");
		log.log(pre+" - version:                      "+version);
		log.log(pre+" - numGlyphs:                    "+numGlyphs);
		log.log(pre+" - maxPoints:                    "+maxPoints);
		log.log(pre+" - maxContours:                  "+maxContours);
		log.log(pre+" - maxComponentPoints:           "+maxComponentPoints);
		log.log(pre+" - maxComponentContours:         "+maxComponentContours);
		log.log(pre+" - maxZones:                     "+maxZones);
		log.log(pre+" - maxTwilightPoints:            "+maxTwilightPoints);
		log.log(pre+" - maxStorage:                   "+maxStorage);
		log.log(pre+" - maxFunctionDefs:              "+maxFunctionDefs);
		log.log(pre+" - maxInstructionDefs:           "+maxInstructionDefs);
		log.log(pre+" - maxStackElements:             "+maxStackElements);
		log.log(pre+" - maxSizeOfInstructions:        "+maxSizeOfInstructions);
		log.log(pre+" - maxComponentElements:         "+maxComponentElements);
		log.log(pre+" - maxComponentDepth:            "+maxComponentDepth);
	}

	public Uint16 getMaxComponentContours() {
		return maxComponentContours;
	}


	public void setMaxComponentContours(Uint16 maxComponentContours) {
		this.maxComponentContours = maxComponentContours;
	}


	public Uint16 getMaxComponentDepth() {
		return maxComponentDepth;
	}


	public void setMaxComponentDepth(Uint16 maxComponentDepth) {
		this.maxComponentDepth = maxComponentDepth;
	}


	public Uint16 getMaxComponentElements() {
		return maxComponentElements;
	}


	public void setMaxComponentElements(Uint16 maxComponentElements) {
		this.maxComponentElements = maxComponentElements;
	}


	public Uint16 getMaxComponentPoints() {
		return maxComponentPoints;
	}


	public void setMaxComponentPoints(Uint16 maxComponentPoints) {
		this.maxComponentPoints = maxComponentPoints;
	}


	public Uint16 getMaxContours() {
		return maxContours;
	}


	public void setMaxContours(Uint16 maxContours) {
		this.maxContours = maxContours;
	}


	public Uint16 getMaxFunctionDefs() {
		return maxFunctionDefs;
	}


	public void setMaxFunctionDefs(Uint16 maxFunctionDefs) {
		this.maxFunctionDefs = maxFunctionDefs;
	}


	public Uint16 getMaxInstructionDefs() {
		return maxInstructionDefs;
	}


	public void setMaxInstructionDefs(Uint16 maxInstructionDefs) {
		this.maxInstructionDefs = maxInstructionDefs;
	}


	public Uint16 getMaxPoints() {
		return maxPoints;
	}


	public void setMaxPoints(Uint16 maxPoints) {
		this.maxPoints = maxPoints;
	}


	public Uint16 getMaxSizeOfInstructions() {
		return maxSizeOfInstructions;
	}


	public void setMaxSizeOfInstructions(Uint16 maxSizeOfInstructions) {
		this.maxSizeOfInstructions = maxSizeOfInstructions;
	}


	public Uint16 getMaxStackElements() {
		return maxStackElements;
	}


	public void setMaxStackElements(Uint16 maxStackElements) {
		this.maxStackElements = maxStackElements;
	}


	public Uint16 getMaxStorage() {
		return maxStorage;
	}


	public void setMaxStorage(Uint16 maxStorage) {
		this.maxStorage = maxStorage;
	}


	public Uint16 getMaxTwilightPoints() {
		return maxTwilightPoints;
	}


	public void setMaxTwilightPoints(Uint16 maxTwilightPoints) {
		this.maxTwilightPoints = maxTwilightPoints;
	}


	public Uint16 getMaxZones() {
		return maxZones;
	}


	public void setMaxZones(Uint16 maxZones) {
		this.maxZones = maxZones;
	}


	public Uint16 getNumGlyphs() {
		return numGlyphs;
	}


	public void setNumGlyphs(Uint16 numGlyphs) {
		this.numGlyphs = numGlyphs;
	}


	public Fixed getVersion() {
		return version;
	}


	public void setVersion(Fixed version) {
		this.version = version;
	}


	public void read(TtfInputStream in) throws IOException {
		version = in.readFixed();				// 0x00010000 (1.0)
		numGlyphs = in.readUint16();			// the number of glyphs in the font
		maxPoints = in.readUint16();			// points in non-compound glyph
		maxContours = in.readUint16();			// contours in non-compound glyph
		maxComponentPoints = in.readUint16();	// points in compound glyph
		maxComponentContours = in.readUint16();	// contours in compound glyph
		maxZones = in.readUint16();				// set to 2
		maxTwilightPoints = in.readUint16();	// points used in Twilight Zone (Z0)
		maxStorage = in.readUint16();			// number of Storage Area locations
		maxFunctionDefs = in.readUint16();		// number of FDEFs
		maxInstructionDefs = in.readUint16();	// number of IDEFs
		maxStackElements = in.readUint16();		// maximum stack depth
		maxSizeOfInstructions = in.readUint16(); // byte count for glyph instructions
		maxComponentElements = in.readUint16();	// number of glyphs referenced at top level
		maxComponentDepth = in.readUint16();	// levels of recursion, set to 0 if font has only simple glyphs
	}
}

