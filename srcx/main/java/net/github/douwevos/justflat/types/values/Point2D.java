package net.github.douwevos.justflat.types.values;

public class Point2D {

	public final long x;
	public final long y;

	public Point2D(long x, long y) {
		this.x = x;
		this.y = y;
	}

	public static Point2D of(long x, long y) {
		return new Point2D(x, y);
	}

	
	public Point2D with(long x, long y) {
		if (this.x==x && this.y==y) {
			return this;
		}
		return new Point2D(x, y);
	}
	
	public Point2D withX(long x) {
		if (this.x == x) {
			return this;
		}
		return new Point2D(x,y);
	}

	public Point2D addX(long x) {
		if (x == 0l) {
			return this;
		}
		return new Point2D(this.x+x,y);
	}


	public Point2D withY(long y) {
		if (this.y == y) {
			return this;
		}
		return new Point2D(x, y);
	}

	public Point2D addY(long y) {
		if (y == 0l) {
			return this;
		}
		return new Point2D(x,this.y + y);
	}

	public Point2D add(long x, long y) {
		if (x == 0l && y == 0l) {
			return this;
		}
		return new Point2D(this.x + x,this.y + y);
	}

	public long getX() {
		return x;
	}
	
	public long getY() {
		return y;
	}

	public long squaredDistance(Point2D other) {
		long dx = x-other.x;
		long dy = y-other.y;
		return dx*dx + dy*dy;
	}

	public long distance(Point2D other) {
		long dx = x-other.x;
		long dy = y-other.y;
		return Math.round(Math.sqrt(dx*dx + dy*dy));
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
		if (obj instanceof Point2D) {
			Point2D that = (Point2D) obj;
			return that.x==x && that.y==y;
		}
		return false;
	}

	public boolean equals(long x, long y) {
		return this.x==x && this.y==y;
	}
	
	@Override
	public String toString() {
		return "[x:"+x+",y:"+y+"]";
	}
}
