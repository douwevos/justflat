package net.github.douwevos.justflat.types.values;

public class FracPoint2D {

	public final double x;
	public final double y;

	public FracPoint2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public static FracPoint2D of(double x, double y) {
		return new FracPoint2D(x, y);
	}
	
	public Point2D toNonFractional() {
		return new Point2D(Math.round(x), Math.round(y));
	}
	
	public FracPoint2D with(double x, double y) {
		if (this.x==x && this.y==y) {
			return this;
		}
		return new FracPoint2D(x, y);
	}
	
	public FracPoint2D withX(double x) {
		if (this.x == x) {
			return this;
		}
		return new FracPoint2D(x,y);
	}

	public FracPoint2D addX(double x) {
		if (x == 0l) {
			return this;
		}
		return new FracPoint2D(this.x+x,y);
	}


	public FracPoint2D withY(double y) {
		if (this.y == y) {
			return this;
		}
		return new FracPoint2D(x, y);
	}

	public FracPoint2D addY(double y) {
		if (y == 0l) {
			return this;
		}
		return new FracPoint2D(x,this.y + y);
	}

	public FracPoint2D add(double x, double y) {
		if (x == 0l && y == 0l) {
			return this;
		}
		return new FracPoint2D(this.x + x,this.y + y);
	}

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	public double squaredDistance(FracPoint2D other) {
		double dx = x-other.x;
		double dy = y-other.y;
		return dx*dx + dy*dy;
	}

	public double distance(FracPoint2D other) {
		double dx = x-other.x;
		double dy = y-other.y;
		return Math.sqrt(dx*dx + dy*dy);
	}

	@Override
	public int hashCode() {
		return (int) (x*13+y*7);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==this) {
			return true;
		}
		if (obj instanceof FracPoint2D) {
			FracPoint2D that = (FracPoint2D) obj;
			return that.x==x && that.y==y;
		}
		return false;
	}

	public boolean equals(double x, double y) {
		return this.x==x && this.y==y;
	}
	
	@Override
	public String toString() {
		return "[x:"+x+",y:"+y+"]";
	}

}
