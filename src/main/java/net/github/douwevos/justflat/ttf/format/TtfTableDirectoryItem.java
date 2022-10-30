package net.github.douwevos.justflat.ttf.format;

import net.github.douwevos.justflat.ttf.reader.TtfInputStream;

public class TtfTableDirectoryItem {

	public static final int TAG_HEAD = 0x68656164;
	public static final int TAG_HHEA = 0x68686561;
	public static final int TAG_CVT  = 0x63767420;
	public static final int TAG_MAXP = 0x6D617870;
	public static final int TAG_CMAP = 0x636D6170;
	public static final int TAG_GLYF = 0x676C7966;
	public static final int TAG_LOCA = 0x6C6F6361;
	
	private int tag;
	private int checkSum;
	private int offset;
	private int length;
	
	private byte data[];
	
	public TtfTableDirectoryItem(int tag, int checkSum, int offset, int length) {
		this.tag = tag;
		this.checkSum = checkSum;
		this.offset = offset;
		this.length = length;
	}
	

	public int getOffset() {
		return offset;
	}
	
	
	public int getTag() {
		return tag;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getCheckSum() {
		return checkSum;
	}

	
	public void setData(byte data[]) {
		this.data = data;
	}
	
	public TtfInputStream createInputStream() {
		return new TtfInputStream(data);
	}

	public int calculateCheckSum() {
		int sum = 0;
		int longCount = (data.length+3)/4;
		for(int idx=0; idx<longCount; idx++) {
			sum += getIntFromData(idx*4);
		}
		return sum;
	}
	
	private int getIntFromData(int pos) {
		int result = 0;
		for(int idx=0; idx<4; idx++) {
			result <<= 8;
			if (pos<data.length) {
				result += 0xFF & data[pos++];
			}
		}
		return result;
	}
	
	
	public String toString() {
		return "TableDirectory:tag="+TtfUtil.tagToString(tag)+", checkSum="+TtfUtil.toHex(checkSum)+", offset="+TtfUtil.toHex(offset)+", length="+length;
	}
	
}

