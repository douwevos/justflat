package net.github.douwevos.justflat.ttf.format.types;

public class F2Dot14 {

	public final int value;
	
	public F2Dot14(final int value) {
		if ((value&0x8000f)==0) {
			this.value = value | 0xFFFF0000;
		} else {
			this.value = value & 0xFFFF;
		}
	}
	
	
	public boolean equals(Object other) {
		if (other instanceof F2Dot14) {
			F2Dot14 f = (F2Dot14) other;
			return f.value == value;
		}
		return false;
	}
	
	
	public float getValue() {
		return ((float) (value))/16384f;
	}
	
	public String toString() {
		return "F2Dot14::"+getValue();
	}

	
}

