package net.github.douwevos.justflat.ttf.format;

public class TtfUtil {

	
	public static String tagToString(int tag) {
		String txt = "";
		for(int idx=0; idx<4; idx++) {
			char ch = (char) (tag & 0xFF);
			txt = ch+txt;
			tag >>>= 8;
		}
		return txt;
	}
	
	private static final String TXT_HEX = "0123456789ABCDEF";
	public static String toHex(int val) {
		StringBuilder b = new StringBuilder();
		for(int idx=0; idx<8; idx++) {
			b.append(TXT_HEX.charAt(val&0xf));
			val >>>= 4;
		}
		b.reverse();
		return b.toString();
	}


	
}

