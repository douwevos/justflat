package net.github.douwevos.justflat.ttf.format.types;

public class Fixed {

	public final int value;
	
	public Fixed(final int value) {
		this.value = value;
	}
	
	public Fixed add(Fixed addVal) {
		int total = addVal.value+value;
		return new Fixed(total);
	}
	
	public boolean equals(Object other) {
		if (other instanceof Fixed) {
			Fixed f = (Fixed) other;
			return f.value == value;
		}
		return false;
	}
	
	public String toString() {
		
		float right = ((float) (value))/65536f;
		return "Fixed::"+right;
	}

	
}

