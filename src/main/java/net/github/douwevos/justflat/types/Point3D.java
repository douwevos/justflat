package net.github.douwevos.justflat.types;

public class Point3D {

	public final long x;
	public final long y;
	public final long z;
	
	public Point3D(long x, long y, long z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3D with(long x, long y, long z) {
		if (this.x==x && this.y==y && this.z==z) {
			return this;
		}
		return new Point3D(x, y, z);
	}
	
	public Point3D withX(long x) {
		return new Point3D(x,y,z);
	}

	public Point3D addX(long x) {
		return new Point3D(this.x+x,y,z);
	}

	public Point3D addY(long y) {
		return new Point3D(this.x,y+y,z);
	}


	public Point3D withY(long y) {
		return new Point3D(x,y,z);
	}
	
	public Point3D withZ(long z) {
		return new Point3D(x,y,z);
	}


	public static Point3D of(long x, long y, long z) {
		return new Point3D(x, y, z);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==this) {
			return true;
		}
		if (obj instanceof Point3D) {
			Point3D that = (Point3D) obj;
			return that.x==x && that.y==y && that.z==z;
		}
		return false;
	}

	public boolean equals(long x, long y, long z) {
		return this.x==x && this.y==y && this.z==z;
	}

	@Override
	public int hashCode() {
		return (int) (x*3 + y*7 + z*11);
	}
	
	
	@Override
	public String toString() {
		return "Point["+x+","+y+","+z+"]";
	}



}
