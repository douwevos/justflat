package net.github.douwevos.justflat.ttf.format;

import net.github.douwevos.justflat.logging.Log;

public class Ttf {
	
	private static final Log log = Log.instance();

	private TtfHead head;
	private TtfHorizontalHeader horizontalHeader;
	private TtfControlValuesTable controlValuesTable;
	private MaxProfile maxProfile;
	private TtfCharacterMap characterMap;
	private LocationTable locationTable;
	private TtfGlyphData glyphData;
	
	public Ttf() {
		
	}

	public void setHead(TtfHead head) {
		this.head = head;
	}
	
	public TtfHead getHead() {
		return head;
	}

	public void setHorizontalHeader(TtfHorizontalHeader horizontalHeader) {
		this.horizontalHeader = horizontalHeader;
	}
	
	public TtfHorizontalHeader getHorizontalHeader() {
		return horizontalHeader;
	}

	public void setControlValuesTable(TtfControlValuesTable controlValuesTable) {
		this.controlValuesTable = controlValuesTable;
	}

	public void setMaxProfile(MaxProfile maxProfile) {
		this.maxProfile = maxProfile;
	}

	public MaxProfile getMaxProfile() {
		return maxProfile;
	}
	
	public void setCharacterMap(TtfCharacterMap characterMap) {
		this.characterMap = characterMap;
	}
	
	
	public TtfCharacterMap getCharacterMap() {
		return characterMap;
	}

	public void setLocationTable(LocationTable locationTable) {
		this.locationTable = locationTable;
	}
	
	public LocationTable getLocationTable() {
		return locationTable;
	}

	public void setGlyphData(TtfGlyphData glyphData) {
		this.glyphData = glyphData;
	}
	
	
	public TtfGlyphData getGlyphData() {
		return glyphData;
	}
	
	public void dump() {
		if (head != null) {
			log.log("head");
			log.log("-----------------------------------");
			head.dump("   ");
		}

		if (horizontalHeader != null) {
			log.log("horizontalHeader");
			log.log("-----------------------------------");
			log.log(horizontalHeader.toString());
		}
		
		if (controlValuesTable != null) {
			log.log("controlValuesTable");
			log.log("-----------------------------------");
			log.log(controlValuesTable.toString());
		}

		if (maxProfile != null) {
			log.log("maxProfile");
			log.log("-----------------------------------");
			maxProfile.dump("   ");
		}

		if (characterMap != null) {
			log.log("characterMap");
			log.log("-----------------------------------");
			characterMap.dump("   ");
		}
		
		if (locationTable != null) {
			log.log("");
			locationTable.dump("   ");
		}
		
		
		if (glyphData!=null) {
			log.log("");
			glyphData.dump("   ");
		}
		
	}


	
}

