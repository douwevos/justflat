package net.github.douwevos.justflat.ttf.format.types;

public class Int16 {

	final int value;
	
	public Int16(final short value) {
		if ((value & 0x8000) == 0) {
			this.value = ((int) value) & 0xFFFF;
		} else {
			this.value = ((int) value) | 0xFFFF0000;
		}
		
	}

	public int getValue() {
		return value;
	}
	
	public String toString() {
		return "Int16::"+value;
	}
	
}

