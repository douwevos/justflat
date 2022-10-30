package net.github.douwevos.justflat.ttf.format;

import java.io.IOException;

import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.ttf.format.types.FWord;
import net.github.douwevos.justflat.ttf.format.types.Fixed;
import net.github.douwevos.justflat.ttf.format.types.Int16;
import net.github.douwevos.justflat.ttf.format.types.LongDateTime;
import net.github.douwevos.justflat.ttf.format.types.Uint16;
import net.github.douwevos.justflat.ttf.format.types.Uint32;
import net.github.douwevos.justflat.ttf.reader.TtfInputStream;

public class TtfHead {

	private static final Log log = Log.instance(); 
	
	private Fixed version;
	private Fixed fontRevision;
	private Uint32 checkSumAdjustment;
	private Uint32 magicNumber;
	private Uint16 flags;
	private Uint16 unitsPerEm;
	private LongDateTime created;
	private LongDateTime modified;
	private FWord xMin, yMin, xMay, yMax;
	private Uint16 macStyle;
	private Uint16 lowestRecPPEM;
	private Int16 fontDirectionHint, indexToLocFormat, glyphDataFormat;
	
	
	
	public TtfHead() {
	}

	public Uint32 getCheckSumAdjustment() {
		return checkSumAdjustment;
	}

	public void setCheckSumAdjustment(Uint32 checkSumAdjustment) {
		this.checkSumAdjustment = checkSumAdjustment;
	}

	public LongDateTime getCreated() {
		return created;
	}

	public void setCreated(LongDateTime created) {
		this.created = created;
	}

	public Uint16 getFlags() {
		return flags;
	}

	public void setFlags(Uint16 flags) {
		this.flags = flags;
	}

	public Int16 getFontDirectionHint() {
		return fontDirectionHint;
	}

	public void setFontDirectionHint(Int16 fontDirectionHint) {
		this.fontDirectionHint = fontDirectionHint;
	}

	public Fixed getFontRevision() {
		return fontRevision;
	}

	public void setFontRevision(Fixed fontRevision) {
		this.fontRevision = fontRevision;
	}

	public Int16 getGlyphDataFormat() {
		return glyphDataFormat;
	}

	public void setGlyphDataFormat(Int16 glyphDataFormat) {
		this.glyphDataFormat = glyphDataFormat;
	}

	public Int16 getIndexToLocFormat() {
		return indexToLocFormat;
	}

	public void setIndexToLocFormat(Int16 indexToLocFormat) {
		this.indexToLocFormat = indexToLocFormat;
	}

	public Uint16 getLowestRecPPEM() {
		return lowestRecPPEM;
	}

	public void setLowestRecPPEM(Uint16 lowestRecPPEM) {
		this.lowestRecPPEM = lowestRecPPEM;
	}

	public Uint16 getMacStyle() {
		return macStyle;
	}

	public void setMacStyle(Uint16 macStyle) {
		this.macStyle = macStyle;
	}

	public Uint32 getMagicNumber() {
		return magicNumber;
	}

	public void setMagicNumber(Uint32 magicNumber) {
		this.magicNumber = magicNumber;
	}

	public LongDateTime getModified() {
		return modified;
	}

	public void setModified(LongDateTime modified) {
		this.modified = modified;
	}

	public Uint16 getUnitsPerEm() {
		return unitsPerEm;
	}

	public void setUnitsPerEm(Uint16 unitsPerEm) {
		this.unitsPerEm = unitsPerEm;
	}

	public Fixed getVersion() {
		return version;
	}

	public void setVersion(Fixed version) {
		this.version = version;
	}

	public FWord getXMay() {
		return xMay;
	}

	public void setXMay(FWord may) {
		xMay = may;
	}

	public FWord getXMin() {
		return xMin;
	}

	public void setXMin(FWord min) {
		xMin = min;
	}

	public FWord getYMax() {
		return yMax;
	}

	public void setYMax(FWord max) {
		yMax = max;
	}

	public FWord getYMin() {
		return yMin;
	}

	public void setYMin(FWord min) {
		yMin = min;
	}
	
	
	public void read(TtfInputStream in) throws IOException {
		setVersion(in.readFixed());
		setFontRevision(in.readFixed()); 
		setCheckSumAdjustment(in.readUint32()); 
		setMagicNumber(in.readUint32());
		setFlags(in.readUint16());
		setUnitsPerEm(in.readUint16()); 
		setCreated(in.readLongDateTime());
		setModified(in.readLongDateTime());
		setXMin(in.readFWord());
		setYMin(in.readFWord());
		setXMay(in.readFWord());
		setYMax(in.readFWord());
		setMacStyle(in.readUint16());
		setLowestRecPPEM(in.readUint16());
		setFontDirectionHint(in.readInt16());
		setIndexToLocFormat(in.readInt16());
		setGlyphDataFormat(in.readInt16());
	}
	
	public void dump(String pre) {
		log.log(pre+"Version:            "+version);
		log.log(pre+"FontRevision:       "+fontRevision);
		log.log(pre+"checkSumAdjustment: "+checkSumAdjustment);
		log.log(pre+"magicNumber:        "+magicNumber);
		log.log(pre+"flags:              "+flags);
		log.log(pre+"unitsPerEm:         "+unitsPerEm);
		log.log(pre+"created:            "+created);
		log.log(pre+"modified:           "+modified);
		log.log(pre+"xMin:               "+xMin);
		log.log(pre+"yMin:               "+yMin);
		log.log(pre+"xMay:               "+xMay);
		log.log(pre+"yMax:               "+yMax);
		log.log(pre+"macStyle:           "+macStyle);
		log.log(pre+"lowestRecPPEM:      "+lowestRecPPEM);
		log.log(pre+"fontDirectionHint:  "+fontDirectionHint);
		log.log(pre+"indexToLocFormat:   "+indexToLocFormat);
		log.log(pre+"glyphDataFormat:    "+glyphDataFormat);
	}
	
	
}

