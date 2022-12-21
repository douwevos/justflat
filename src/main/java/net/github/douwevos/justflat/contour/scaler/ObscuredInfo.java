package net.github.douwevos.justflat.contour.scaler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObscuredInfo implements Iterable<Range> {

	private static final ObscuredInfo EMPTY = new ObscuredInfo();
	
	private final List<Range> ranges;
	private final boolean clean; 
	private final boolean fullyObscured;

	public ObscuredInfo() {
		ranges = new CopyOnWriteArrayList<>();
		clean = true;
		fullyObscured = false;
	}
	
	private ObscuredInfo(List<Range> ranges, boolean clean) {
		this.ranges = new CopyOnWriteArrayList<>(ranges);
		this.clean = clean;
		
		boolean isFullyObscured = false;
		if (ranges.size() == 1) {
			Range range = ranges.get(0);
			isFullyObscured = (range.start<=0d && range.end>=359.9d);
		}
		
		this.fullyObscured = isFullyObscured;
	}
	
	public boolean isFullyObscured() {
		return fullyObscured;
	}
	
	public ObscuredInfo add(Range r) {
		CopyOnWriteArrayList<Range> copyOnWriteArrayList = new CopyOnWriteArrayList<>(ranges);
		copyOnWriteArrayList.add(r);
		return new ObscuredInfo(copyOnWriteArrayList, false);
	}
	
	
	public ObscuredInfo invert(ObscuredInfo obscuredInfo) {
		if (obscuredInfo.ranges.isEmpty()) {
			return this;
		}
		ArrayList<Range> result = new ArrayList<>(ranges);
		for(Range r : obscuredInfo) {
			result.add(r);
		}
		ObscuredInfo reuslt = new ObscuredInfo(result, false);
		return reuslt.reduceObscureInfo();
	}

	
	@Override
	public Iterator<Range> iterator() {
		return ranges.iterator();
	}

	public ObscuredInfo reduceObscureInfo() {
		if (ranges.isEmpty()) {
			return EMPTY;
		}
		if (clean) {
			return this;
		}
		
		Range current = null;
		ArrayList<Range> work = new ArrayList<>(ranges.size());
		for(Range p : ranges) {
			if (p.start>p.end) {
				if (p.end!=0d && p.end!=360d) {
					work.add(new Range(0, p.end, p.name));
				}
				if (p.start!=360d) {
					work.add(new Range(p.start, 360d, p.name));
				}
			} else if (p.start!=p.end){
				work.add(p);
			}
		}
		
		List<Range> out = new ArrayList<>();
		while(!work.isEmpty()) {
			if (current == null) {
				for(Range range : work) {
					if (current==null || range.start<current.start) {
						current = range;
					}
				}
				work.remove(current);
				continue;
			} 

			boolean didChange = false;
			for(int idx = work.size()-1; idx>=0; idx--) {
				Range rangeTest = work.get(idx);
				if (rangeTest.start<=current.end) {
					work.remove(idx);
					if (rangeTest.end>current.end) {
						current = current.withEnd(rangeTest.end);
						didChange = true;
					}
				}
			}
			
			if (!didChange) {
				out.add(current);
				current = null;
			}
		}
		
		if (current != null) {
			out.add(current);
		}
		
		return new ObscuredInfo(out, true);
	}


	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(Range range : ranges) {
			buf.append(",").append(range);
		}
		return buf.toString();
	}
}
