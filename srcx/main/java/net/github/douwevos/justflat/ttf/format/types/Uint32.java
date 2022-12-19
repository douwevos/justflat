package net.github.douwevos.justflat.ttf.format.types;

public class Uint32 {

	final int value;
	
	public Uint32(final int value) {
		this.value = value;
	}
	
	
	public int getValue() {
		return value;
	}
	
	
	public String toString() {
		return "UInt32::"+value;
	}
}

