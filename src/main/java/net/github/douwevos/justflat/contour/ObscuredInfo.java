package net.github.douwevos.justflat.contour;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObscuredInfo implements Iterable<Range> {

	private final List<Range> ranges;

	public ObscuredInfo() {
		ranges = new CopyOnWriteArrayList<>();
	}
	
	private ObscuredInfo(List<Range> ranges) {
		this.ranges = new CopyOnWriteArrayList<>(ranges);
	}
	
	public ObscuredInfo add(Range r) {
		double start = (r.start + 360d) % 360d;
		double end = (r.end + 360d) % 360d;


		
		CopyOnWriteArrayList<Range> copyOnWriteArrayList = new CopyOnWriteArrayList<>(ranges);
		copyOnWriteArrayList.add(r);
//		if (start<end) {
//			copyOnWriteArrayList.add(new Range(start, end));
//		} else {
//			copyOnWriteArrayList.add(new Range(0, start));
//			copyOnWriteArrayList.add(new Range(end, 359d));
//			
//		}
		return new ObscuredInfo(copyOnWriteArrayList);
	}
	
	@Override
	public Iterator<Range> iterator() {
		return ranges.iterator();
	}
}
