package net.github.douwevos.justflat.ttf.format;

import java.io.IOException;

import net.github.douwevos.justflat.ttf.format.types.FWord;
import net.github.douwevos.justflat.ttf.format.types.Fixed;
import net.github.douwevos.justflat.ttf.format.types.Int16;
import net.github.douwevos.justflat.ttf.format.types.UFWord;
import net.github.douwevos.justflat.ttf.format.types.Uint16;
import net.github.douwevos.justflat.ttf.reader.TtfInputStream;

public class TtfHorizontalHeader {

	private Fixed version;
	private FWord ascent;				// Distance from baseline of highest ascender
	private FWord descent;				// Distance from baseline of lowest descender
	private FWord lineGap;				// typographic line gap
	private UFWord advanceWidthMax;		// must be consistent with horizontal metrics
	private FWord minLeftSideBearing;	// must be consistent with horizontal metrics
	private FWord minRightSideBearing;	// must be consistent with horizontal metrics
	private FWord xMaxExtent;			// max(lsb + (xMax-xMin))
	private Int16 caretSlopeRise;		// used to calculate the slope of the caret (rise/run) set to 1 for vertical caret
	private Int16 caretSlopeRun;		// 0 for vertical
	private FWord caretOffset;			// set value to 0 for non-slanted fonts
	private Int16 reserved0;			// set value to 0
	private Int16 reserved1;			// set value to 0
	private Int16 reserved2;			// set value to 0
	private Int16 reserved3;			// set value to 0
	private Int16 metricDataFormat;		// 0 for current format
	private Uint16 numOfLongHorMetrics;	// number of advance widths in metrics table

	
	public UFWord getAdvanceWidthMax() {
		return advanceWidthMax;
	}
	public void setAdvanceWidthMax(UFWord advanceWidthMax) {
		this.advanceWidthMax = advanceWidthMax;
	}
	public FWord getAscent() {
		return ascent;
	}
	public void setAscent(FWord ascent) {
		this.ascent = ascent;
	}
	public FWord getCaretOffset() {
		return caretOffset;
	}
	public void setCaretOffset(FWord caretOffset) {
		this.caretOffset = caretOffset;
	}
	public Int16 getCaretSlopeRise() {
		return caretSlopeRise;
	}
	public void setCaretSlopeRise(Int16 caretSlopeRise) {
		this.caretSlopeRise = caretSlopeRise;
	}
	public Int16 getCaretSlopeRun() {
		return caretSlopeRun;
	}
	public void setCaretSlopeRun(Int16 caretSlopeRun) {
		this.caretSlopeRun = caretSlopeRun;
	}
	public FWord getDescent() {
		return descent;
	}
	public void setDescent(FWord descent) {
		this.descent = descent;
	}
	public FWord getLineGap() {
		return lineGap;
	}
	public void setLineGap(FWord lineGap) {
		this.lineGap = lineGap;
	}
	public Int16 getMetricDataFormat() {
		return metricDataFormat;
	}
	public void setMetricDataFormat(Int16 metricDataFormat) {
		this.metricDataFormat = metricDataFormat;
	}
	public FWord getMinLeftSideBearing() {
		return minLeftSideBearing;
	}
	public void setMinLeftSideBearing(FWord minLeftSideBearing) {
		this.minLeftSideBearing = minLeftSideBearing;
	}
	public FWord getMinRightSideBearing() {
		return minRightSideBearing;
	}
	public void setMinRightSideBearing(FWord minRightSideBearing) {
		this.minRightSideBearing = minRightSideBearing;
	}
	public Uint16 getNumOfLongHorMetrics() {
		return numOfLongHorMetrics;
	}
	public void setNumOfLongHorMetrics(Uint16 numOfLongHorMetrics) {
		this.numOfLongHorMetrics = numOfLongHorMetrics;
	}
	public Int16 getReserved0() {
		return reserved0;
	}
	public void setReserved0(Int16 reserved0) {
		this.reserved0 = reserved0;
	}
	public Int16 getReserved1() {
		return reserved1;
	}
	public void setReserved1(Int16 reserved1) {
		this.reserved1 = reserved1;
	}
	public Int16 getReserved2() {
		return reserved2;
	}
	public void setReserved2(Int16 reserved2) {
		this.reserved2 = reserved2;
	}
	public Int16 getReserved3() {
		return reserved3;
	}
	public void setReserved3(Int16 reserved3) {
		this.reserved3 = reserved3;
	}
	public Fixed getVersion() {
		return version;
	}
	public void setVersion(Fixed version) {
		this.version = version;
	}
	public FWord getXMaxExtent() {
		return xMaxExtent;
	}
	public void setXMaxExtent(FWord maxExtent) {
		xMaxExtent = maxExtent;
	}
	
	
	public void read(TtfInputStream in) throws IOException {
		setVersion(in.readFixed());
		setAscent(in.readFWord());
		setDescent(in.readFWord());
		setLineGap(in.readFWord());
		setAdvanceWidthMax(in.readUFWord());
		setMinLeftSideBearing(in.readFWord());
		setMinRightSideBearing(in.readFWord());
		setXMaxExtent(in.readFWord());
		setCaretSlopeRise(in.readInt16());
		setCaretSlopeRun(in.readInt16());
		setCaretOffset(in.readFWord());
		setReserved0(in.readInt16());
		setReserved1(in.readInt16());
		setReserved2(in.readInt16());
		setReserved3(in.readInt16());
		setMetricDataFormat(in.readInt16());
		setNumOfLongHorMetrics(in.readUint16());
		
	}
	
}

