package net.github.douwevos.justflat.ttf.format;

public class TtfOffsetSubTable {

	public static final int SCALAR_TYPE_WINDOWS		= 0x00010000;
	public static final int SCALAR_TYPE_MACOS		= 0x74727565; // 'true'
	public static final int SCALAR_TYPE_POSTSCRIPT	= 0x74797031; // 'typ1'
	
	
	int scalerType;
	short numTables;
	short searchRange;
	short entrySelector;
	short rangeShift;
	
	public TtfOffsetSubTable(int scalerType, short numTables, short searchRange, short entrySelector, short rangeShift) {
		this.scalerType = scalerType;
		this.numTables = numTables;
		this.searchRange = searchRange;
		this.entrySelector = entrySelector;
		this.rangeShift = rangeShift;
	}
	
	
	
	
	public short getNumTables() {
		return numTables;
	}

}

