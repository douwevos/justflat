package net.github.douwevos.justflat.ttf.format.types;

public class Uint16 {

	final int value;
	

	public Uint16(final int value) {
		this.value = value & 0xffff;
	}
	
	public int getValue() {
		return value;
	}

	public String toString() {
		return "UInt16::"+value;
	}
	
}

