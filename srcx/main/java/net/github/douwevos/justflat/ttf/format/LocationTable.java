package net.github.douwevos.justflat.ttf.format;

import java.io.IOException;

import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.ttf.reader.TtfInputStream;

public class LocationTable {

	private static final Log log = Log.instance(false);
	
	private final TtfHead head;
	private final MaxProfile maxProfile;
	private int offset[];
	
	public LocationTable(final TtfHead head, final MaxProfile maxProfile) {
		this.head = head;
		this.maxProfile = maxProfile;
	}
	
	
	public void read(TtfInputStream in) throws IOException {
		if (head.getIndexToLocFormat().getValue()==0) {
			readShort(in);
		} else {
			readLong(in);
		}
	}


	private void readShort(TtfInputStream in) throws IOException {
		int numOfGlyfs = maxProfile.getNumGlyphs().getValue()+1;
		offset = new int[numOfGlyfs];
		for(int idx=0; idx<numOfGlyfs; idx++) {
			offset[idx] = in.readUint16().getValue()*2;
		}
	}

	private void readLong(TtfInputStream in) throws IOException {
		int numOfGlyfs = maxProfile.getNumGlyphs().getValue()+1;
		offset = new int[numOfGlyfs];
		for(int idx=0; idx<numOfGlyfs; idx++) {
			offset[idx] = in.readUint32().getValue();
		}
	}

	
	public void dump(String pre) {
		log.log("GlyphLocationTable");
//		for(int idx=0; idx<offset.length-1; idx++) {
//			log.log(pre+"   glyf["+idx+"]  "+offset[idx]+" - "+(offset[idx+1]-offset[idx]));
//		}
	}
	
	public int getOffset(int index) {
		return offset[index];
	}
	
}

