package net.github.douwevos.justflat.ttf.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.ttf.format.types.Int16;
import net.github.douwevos.justflat.ttf.format.types.Uint16;
import net.github.douwevos.justflat.ttf.format.types.Uint32;
import net.github.douwevos.justflat.ttf.reader.TtfInputStream;

public class TtfCharacterMap {

	private static final Log log = Log.instance(false); 

	private Index index;
	private EncodingSubTable subTables[];
	
	
	public void read(TtfInputStream in) throws IOException {
		log.log("CMAP: length:{}", in.dataLength());
		index = new Index();
		index.read(in);
		int subCnt = index.getNumberSubtables().getValue();
		log.log("number-of-subtables:{}", subCnt);
		subTables = new EncodingSubTable[subCnt];
		for(int idx=0; idx<subCnt; idx++) {
			subTables[idx] = new EncodingSubTable();
			subTables[idx].read(in);
			log.log(" - subtables["+idx+"]:");
			subTables[idx].dump("             ");
		}
		for(int idx=0; idx<subCnt; idx++) {
			subTables[idx].readFormat(in);
		}
	}
	
	
	
	public int getGlyphIndex(int codepoint) {
		for(int idx=0; idx<subTables.length; idx++) {
			Integer result = subTables[idx].getGlyphIndex(codepoint);
			if (result != null) {
				return result;
			}
		}
		return -1;
	}

	public void dump(String pre) {
		log.log(pre+"Character map");
		pre += "   ";
		if (index!=null) {
			index.dump(pre);
		}
		if (subTables!=null) {
			for(int idx=0; idx<subTables.length; idx++) {
				subTables[idx].dump(pre);
			}
			
		}
	}
	
	
	public static class Index {
		private Uint16 version;
		private Uint16 numberSubtables;
		public Uint16 getNumberSubtables() {
			return numberSubtables;
		}
		public void setNumberSubtables(Uint16 numberSubtables) {
			this.numberSubtables = numberSubtables;
		}
		public Uint16 getVersion() {
			return version;
		}
		public void setVersion(Uint16 version) {
			this.version = version;
		}
		
		public void read(TtfInputStream in) throws IOException {
			setVersion(in.readUint16());
			setNumberSubtables(in.readUint16());
		}
		
		public void dump(String pre) {
			log.log(pre+"Index");
			log.log(pre+" - version:         "+version);
			log.log(pre+" - numberSubtables: "+numberSubtables);
		}
	}
	
	
	public static class EncodingSubTable {
		private Uint16 platformID;
		private Uint16 platformSpecificID;
		private Uint32 offset;
		
		private Format format;

		public Uint32 getOffset() {
			return offset;
		}
		public Integer getGlyphIndex(int codepoint) {
			if (format != null) {
				return format.getGlyphIndex(codepoint);
			}
			return null;
		}
		public void dump(String pre) {
			log.log(pre+"Index");
			log.log(pre+" - platformID:          "+platformID);
			log.log(pre+" - platformSpecificID:  "+platformSpecificID);
			log.log(pre+" - offset:              "+offset);
			if (format!=null) {
				format.dump(pre+"   ");
			}
		}

		public void setOffset(Uint32 offset) {
			this.offset = offset;
		}
		public Uint16 getPlatformID() {
			return platformID;
		}
		public void setPlatformID(Uint16 platformID) {
			this.platformID = platformID;
		}
		public Uint16 getPlatformSpecificID() {
			return platformSpecificID;
		}
		public void setPlatformSpecificID(Uint16 platformSpecificID) {
			this.platformSpecificID = platformSpecificID;
		}
		
		public void read(TtfInputStream in) throws IOException {
			platformID = in.readUint16();
			platformSpecificID = in.readUint16();
			offset = in.readUint32();
		}
			
		public void readFormat(TtfInputStream in) throws IOException {
			log.indent();
			try {
				in.setSeek(offset.getValue());
				
				Uint16 formatId = in.readUint16();
				log.log("formatId="+formatId);
				switch(formatId.getValue()) {
					case 0 : {
						format = new Format0();
						format.read(in);
					} break;
					
					case 4 : {
						format = new Format4();
						format.read(in);
						format.dump("               ");
					} break;
				}
			} finally {
				log.dedent();
			}
		}
		
	}
	
	public static interface Format {
		
		public void read(TtfInputStream in) throws IOException;
		public Integer getGlyphIndex(int codepoint);
		public void dump(String pre);
	}
	
	public static class Format0 implements Format {
		
//		private Uint16 format = new Uint16(0);
		private Uint16 length;
		private Uint16 language;
		private byte glyphIndexArray[] = new byte[256];
		
		
		public void read(TtfInputStream in) throws IOException {
			length = in.readUint16();
			language = in.readUint16();
			in.read(glyphIndexArray);
		}
		
		@Override
		public Integer getGlyphIndex(int codepoint) {
			if (codepoint<0 || codepoint>=glyphIndexArray.length) {
				return null;
			}
			return 0xFF & glyphIndexArray[codepoint];
		}
		
		public void dump(String pre) {
			log.log(pre+"Format0");
			log.log(pre+" - length:         "+length);
			log.log(pre+" - language:       "+language);

			log.log(pre+" - map[]");
			log.log(pre+"         FIX DUMP HERE");
			
//			int glidx=0;
//			for(int idx=0; idx<8; idx++) {
//				log.log(pre+"   ");
//				for(int row=0; row<32; row++) {
//					char ch = (char) glyphIndexArray[glidx++];
//					if (ch>31) {
//						log.log(ch);
//					} else {
//						log.log('.');
//					}
//				}
//				log.log("");
//			}
		}
	}
	

	public static class Format4 implements Format {
		
//		private Uint16 format = new Uint16(0);
		private Uint16 length;
		private Uint16 language;
		private Uint16 segCountX2;
		private Uint16 searchRange;
		private Uint16 entrySelector;
		private Uint16 rangeShift;
		private Uint16 endCode[];
		private Uint16 reservedPad;
		private Uint16 startCode[];
		private Int16 idDelta[];
		private Uint16 idRangeOffsets[];
		private Uint16 glyphIdArray[];
		
		
		
		public void read(TtfInputStream in) throws IOException {
			int seekOffset = in.getSeekOffset()-2;
			length = in.readUint16();
			log.log("seekOffset:{}, length:{}({})", seekOffset, length, Integer.toHexString(length.getValue()));
			int endOffset = seekOffset+length.getValue();
			language = in.readUint16();
			segCountX2 = in.readUint16();
			searchRange = in.readUint16();
			entrySelector = in.readUint16();
			rangeShift = in.readUint16();
			
			int segCount = segCountX2.getValue()/2;
			log.log("segCount:{}", segCount);
			endCode = readUInt16Array(in, segCount);
			reservedPad = in.readUint16();
			startCode = readUInt16Array(in, segCount);
			idDelta = readInt16Array(in, segCount);
			idRangeOffsets = readUInt16Array(in, segCount);
			List<Uint16> k = new ArrayList<>();
			while(in.getSeekOffset()<endOffset) {
				k.add(in.readUint16());
			}
			glyphIdArray = new Uint16[k.size()];
			k.toArray(glyphIdArray);
		}

		private Uint16[] readUInt16Array(TtfInputStream in, int segCount) throws IOException {
			Uint16 result[] = new Uint16[segCount];
			for(int idx=0; idx<result.length; idx++) {
				result[idx] = in.readUint16();
			}
			return result;
		}

		private Int16[] readInt16Array(TtfInputStream in, int segCount) throws IOException {
			Int16 result[] = new Int16[segCount];
			for(int idx=0; idx<result.length; idx++) {
				result[idx] = in.readInt16();
			}
			return result;
		}
		
		
		@Override
		public Integer getGlyphIndex(int codepoint) {
			for(int fidx=0; fidx<startCode.length; fidx++) {
				if (codepoint>= startCode[fidx].getValue() && codepoint<=endCode[fidx].getValue()) {
					return codepoint+idDelta[fidx].getValue();
				}
			}
			return null;
		}

		public void dump(String pre) {
			log.log(pre+"Format4");
			log.log(pre+" - length:         "+length);
			log.log(pre+" - language:       "+language);
			log.log(pre+" - segCountX2:     "+segCountX2);
			log.log(pre+" - searchRange:    "+searchRange);
			log.log(pre+" - entrySelector:  "+entrySelector);
			log.log(pre+" - rangeShift:     "+rangeShift);
			log.log(pre+" - endCode:        "+asDump(endCode));
			log.log(pre+" - reservedPad:    "+reservedPad);
			log.log(pre+" - startCode:      "+asDump(startCode));
			log.log(pre+" - idDelta:        "+asDump(idDelta));
			log.log(pre+" - idRangeOffsets: "+asDump(idRangeOffsets));
			log.log(pre+" - glyphIdArray:   "+asDump(glyphIdArray));
			
//			System.exit(0);
		}
		
		private String asDump(Uint16 data[]) {
			if (data == null) {
				return "nil";
			}
			StringBuilder buf = new StringBuilder('[');
			for(int idx=0; idx<data.length; idx++) {
				if (idx>0) {
					buf.append(',');
				}
				if (idx>20) {
					buf.append(".."+(data.length-idx)+" more");
					break;
				}
				buf.append(data[idx].getValue());
			}
			return buf.append(']').toString();
		}
		

		private String asDump(Int16 data[]) {
			if (data == null) {
				return "nil";
			}
			StringBuilder buf = new StringBuilder('[');
			for(int idx=0; idx<data.length; idx++) {
				if (idx>0) {
					buf.append(',');
				}
				if (idx>20) {
					buf.append(".."+(data.length-idx)+" more");
					break;
				}
				buf.append(data[idx].getValue());
			}
			return buf.append(']').toString();
		}		
	}
	
	
}

