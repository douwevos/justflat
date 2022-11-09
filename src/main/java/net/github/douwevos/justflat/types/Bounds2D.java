package net.github.douwevos.justflat.types;


public class Bounds2D {

	public final long left;
	public final long right;
	public final long top;
	public final long bottom;
	
//	public Bounds2D(Point2D leftBottom, Point2D topRight) {
//		this.leftBottom = leftBottom;
//		this.topRight = topRight;
//	}

	public Bounds2D(long xa, long ya, long xb, long yb) {
		if (xa<xb) {
			left = xa;
			right = xb;
		} else {
			left = xb;
			right = xa;
		}

		if (ya<yb) {
			bottom = ya;
			top = yb;
		} else {
			bottom = yb;
			top = ya;
		}

	}

	public Bounds2D extend(long x, long y) {
		if (x>=left && x<=right
				&& y>=bottom && y<=top) {
			return this;
		}
		long xa = x<left ? x : left;
		long xb = x>right ? x : right;
		long ya = y<bottom ? y : bottom;
		long yb = y>top ? y : top;
		return new Bounds2D(xa, ya, xb, yb);
	}
	
	
	public Bounds2D extend(Point2D pa, Point2D pb) {
		long x0 = pa.x;
		long y0 = pa.y;
		long x1 = pb.x;
		long y1 = pb.y;

		long xl = left; 
		x0 = x0<xl ? x0 : xl;
		x0 = x1<xl ? x1 : xl;
		
		long xr = right;
		xr = x0>xr ? x0 : xr;
		xr = x1>xr ? x1 : xr;
		
		long yb = bottom;
		yb = y0<yb ? y0 : yb;
		yb = y1<yb ? y1 : yb;
		
		long yt = top;
		yt = y0>yt ? y0 : yt;
		yt = y1>yt ? y1 : yt;

		if (xl==left && xr<=right
				&& yb==bottom && yt==top) {
			return this;
		}

		return new Bounds2D(xl, yt, xr, yb);
	}

	
	
	public Bounds2D union(Bounds2D other) {
		long xa = other.left<left ? other.left : left;
		long xb = other.right>right ? other.right : right;
		long ya = other.bottom<bottom ? other.bottom : bottom;
		long yb = other.top>top ? other.top : top;
		return new Bounds2D(xa, ya, xb, yb);
	}


	public Bounds2D scale(double scalar) {
		return new Bounds2D(Math.round(left*scalar), Math.round(bottom*scalar), Math.round(right*scalar), Math.round(top*scalar));
	}

	@Override
	public String toString() {
		return "Bounds2D [left=" + left + ", right=" + right + ", top=" + top + ", bottom=" + bottom + "]";
	}

	public boolean doesIntersectWith(Bounds2D other) {
		return other.left<=right && other.right>=left
				&& other.bottom<=top && other.top>=bottom;
	}

	public long sqaure() {
		long dx = right-left;
		long dy = top-bottom;
		return dx*dy;
	}

	public Bounds2D extend(Line2D line) {
		return line==null ? this : extend(line.pointA(), line.pointB());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj==this) {
			return true;
		}
		if (obj instanceof Bounds2D) {
			Bounds2D other = (Bounds2D) obj;
			return other.left == left && other.right == right
					&& other.bottom == bottom && other.top == top;
		}
		return false;
	}
	
}
