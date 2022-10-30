package net.github.douwevos.justflat.types;

public class CircleCoords {
	
	public final int diameter;
	public final double radius;
	public int xCoords[];
	
	public CircleCoords(int diameter) {
		this.diameter = diameter;
		xCoords = new int[diameter+1];

		this.radius = diameter/2d;
		double radiusSq = radius * radius;
		
		for(int ys=0; ys<=diameter; ys++) {
			double ry = ys - diameter/2d;
			xCoords[ys] = (int) Math.round(Math.sqrt(radiusSq - ry*ry));
		}			
	}

	public boolean contains(int deltaX, int deltaY) {
		long py = deltaY + Math.round(radius);
		if (py<0 || py>diameter) {
			return false;
		}
		int rx = xCoords[(int) py];
		return deltaX>=-rx && deltaX<=rx; 
	}
	
}