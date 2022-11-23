package net.github.douwevos.justflat.contour;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.github.douwevos.justflat.types.Line2D;
import net.github.douwevos.justflat.types.Point2D;

public class OverlapPoint {
	
	public final Point2D point;
	
	private Taint taint = Taint.NONE;
	
	private Map<Taint,Taint> taintedSet = new EnumMap<>(Taint.class);
	
	private ObscuredInfo obscuredInfo = new ObscuredInfo();
	
	private boolean isUSed;
	
	private List<Route> routes = new ArrayList<>();
	
	
	public OverlapPoint(Point2D point) {
		this.point = point;
	}
	
	
	public boolean add(Route route) {
		boolean result = !routes.contains(route);
		if (result) {
			routes.add(route);
		}
		return result;
	}

	public boolean remove(Route route) {
		return routes.remove(route);
	}
	
	public boolean contains(Route route) {
		return routes.contains(route);
	}
	
	
	public Iterable<Route> routeIterable() {
		return routes;
	}

	public boolean isTainted() {
		return !taint.isValid();
	}
	
	public void taintWith(Taint taint) {
		if (this.taint!=null) {
			if (!this.taint.isValid() && taint.isValid()) {
				taintedSet.put(taint, taint);
				return;
			}
		}
		taintedSet.put(taint, taint);
		this.taint = taint;
	}
	
	public Set<Taint> getTaintedSet() {
		return taintedSet.keySet();
	}
	
	public Taint getTaint() {
		return taint;
	}
	
	public enum Taint {
		NONE(true),
		RECONNECT(true),
		EDGE(true),
		OBSCURED(false), 
		OVERSHOOT(false),
		ORIGINAL(false), 
		INVALID(false);

		boolean valid;
		
		Taint(boolean isValid) {
			valid = isValid;
		}
		
		public boolean isValid() {
			return valid;
		}
	}
	
	public ObscuredInfo getObscuredInfo() {
		return obscuredInfo;
	}
	

	public void addObscure(Line2D from, boolean fromReverse, Line2D to, boolean toReverse) {
		
	}

	public void addObscure2(Line2D from, boolean fromReverse, Line2D to, boolean toReverse) {
		double fromA = from.getAlpha();
		if (fromReverse) {
			fromA = (fromA+180d)%360d;
		}
		double toA = to.getAlpha();
		if (toReverse) {
			toA = (toA+180d)%360d;
		}
		obscuredInfo = obscuredInfo.add(new Range(fromA, toA));
	}

	
	public void markUsed() {
		isUSed = true;
	}
	
	public boolean isUSed() {
		return isUSed;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof OverlapPoint) {
			OverlapPoint other = (OverlapPoint) obj;
			return other.point.equals(point) && taint == other.taint && isUSed==other.isUSed;
		}
		return false;
	}

	@Override
	public String toString() {
		return "OverlapPoint [point=" + point + ", taint=" + taint + ", isUSed=" + isUSed + "]";
	}


	
}