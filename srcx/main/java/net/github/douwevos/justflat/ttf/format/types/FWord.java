package net.github.douwevos.justflat.ttf.format.types;

public class FWord {

	final short value;
	
	public FWord(final short value) {
		this.value = value;
	}

	public String toString() {
		long r = value;
		return "FWord::"+r;
	}
	
	public int getValue() {
		return value;
	}
	
}

