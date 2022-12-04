package net.github.douwevos.justflat.startstop;

import java.util.List;

import net.github.douwevos.justflat.types.CircleCoords;
import net.github.douwevos.justflat.types.Layer;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.types.values.StartStop;

public class StartStopLayer implements Layer {

	public final long bottom;
	public final long top;
	public final StartStopLine layerLine[];
	private Long left;
	private Long right;
	
	private StartStopLayer(long bottom, StartStopLine layerLine[]) {
		this.bottom = bottom;
		this.top = bottom+layerLine.length-1;
		this.layerLine = layerLine;
	}
	
	public StartStopLayer(long bottom, int height) {
		this.bottom = bottom;
		this.top = bottom+height-1;
		layerLine = new StartStopLine[height];
		for(int idx=0; idx<height; idx++) {
			layerLine[idx] = new StartStopLine();
		}
	}
	
	
	@Override
	public long top() {
		return top;
	}

	@Override
	public long bottom() {
		return bottom;
	}

	@SuppressWarnings("unchecked")
	public StartStopLayer duplicate() {
		StartStopLine newLines[] = new StartStopLine[this.layerLine.length];
		for(int idx=0; idx<newLines.length; idx++) {
			newLines[idx] = layerLine[idx].duplicate();
		}
		return new StartStopLayer(bottom, newLines);
	}

	
	public void invert(int lineIndex, List<StartStop> startStopList) {
		layerLine[lineIndex].invert(startStopList);
	}
	
	public int lineCount() {
		return layerLine.length;
	}


	public long cutAt(CircleCoords circleCoords, long x, long y) {
		long cutCount = 0;
		double radius = circleCoords.radius;
		
		long py = y - Math.round(radius);
		
		for(int ys=0; ys<=circleCoords.diameter; ys++) {

			int rx = circleCoords.xCoords[ys];
			long leftPx = x-rx;
			long rightPx = x+rx;
			
			if (py<bottom || py>=bottom+layerLine.length) {
			} else {
				StartStopLine layerLineAtY = layerLine[(int) (py-bottom)];
				cutCount += layerLineAtY.cut(leftPx, rightPx);
			}
			py++;
		}
		if (cutCount>0) {
			left = null;
			right = null;
		}
		return cutCount;
	}

	public long cutAt(CircleCoords circleCoords, long xa, long xb, long y) {
		long cutCount = 0;
		double radius = circleCoords.radius;
		
		long py = y - Math.round(radius);
		
		for(int ys=0; ys<=circleCoords.diameter; ys++) {
			if (py<bottom || py>=bottom+layerLine.length) {
			} else {
				int rx = circleCoords.xCoords[ys];
				long leftPx = xa-rx;
				long rightPx = xb+rx;
				StartStopLine layerLineAtY = layerLine[(int) (py-bottom)];
				cutCount += layerLineAtY.cut(leftPx, rightPx);
			}
			py++;
		}
		if (cutCount>0) {
			left = null;
			right = null;
		}
		return cutCount;
	}

	
	public LayerCollisionInfo calculateAt(CircleCoords circleCoords, long x, long y) {
		LayerCollisionInfo info = new LayerCollisionInfo();
		double radius = circleCoords.radius;
		
		long py = y - Math.round(radius);
		
		for(int ys=0; ys<=circleCoords.diameter; ys++) {

			int rx = circleCoords.xCoords[ys];
			long leftPx = x-rx;
			long rightPx = x+rx;
			
			if (py<bottom || py>=bottom+layerLine.length) {
				info.misCount += 1+rightPx-leftPx;
			} else {
				StartStopLine layerLineAtY = layerLine[(int) (py-bottom)];
				layerLineAtY.fillCollisionInfo(info, leftPx, rightPx);
			}
			py++;
		}
		return info;
	}

	public boolean doesBreach(CircleCoords circleCoords, long x, long y) {
		double radius = circleCoords.radius;
		
		long py = y - Math.round(radius);
		
		for(int ys=0; ys<=circleCoords.diameter; ys++) {

			int rx = circleCoords.xCoords[ys];
			long leftPx = x-rx;
			long rightPx = x+rx;
			
			if (py<bottom || py>=bottom+layerLine.length) {
				return true;
			} else {
				StartStopLine layerLineAtY = layerLine[(int) (py-bottom)];
				if (layerLineAtY.doesBreach(leftPx, rightPx)) {
					return true;
				}
			}
			py++;
		}
		return false;
	}

	
	public Bounds2D bounds() {
		Bounds2D result = null;
		if ((left == null) || (right==null)) { 
			long currentY = bottom;
			for(StartStopLine line : layerLine) {
				StartStop[] startStops = line.startStops;
				if (startStops.length>0) {
					long firstX = startStops[0].start;
					long lastX = startStops[startStops.length-1].stop;
	
					if (result == null) {
						result = new Bounds2D(firstX, currentY, lastX, currentY);
					} else {
						result = result.extend(firstX, currentY);
						result = result.extend(lastX, currentY);
					}
					
				}
				currentY++;
			}
			if (result !=null) {
				left = result.left;
				right = result.right;
			}
		} else {
			result = new Bounds2D(left, bottom, right, top);
		}
		return result;
	}

	public boolean testDot(long x, long y, boolean defaultValue) {
		if (y<bottom || y>top) {
			return defaultValue;
		}
		
		StartStopLine layerLineAtY = layerLine[(int) (y-bottom)];
		return layerLineAtY.testDot(x);
	}

//
//	public static void main(String[] args) {
//		Layer layer = new Layer(0, 16);
////		layer.fill(100, 16);
//		layer.dump();
//
//		CircleCoords cc = new CircleCoords(12);
//		LayerCollisionInfo info = layer.calculateAt(cc, 10, 0);
//		System.out.println("hitCount="+info.hitCount);
//
//		layer.cutAt(cc, 10, 0);
//		layer.dump();
//		info = layer.calculateAt(cc, 10, 0);
//		System.out.println("hitCount="+info.hitCount);
//		info = layer.calculateAt(cc, 10, 1);
//		System.out.println("hitCount="+info.hitCount);
//		info = layer.calculateAt(cc, 10, 3);
//		System.out.println("hitCount="+info.hitCount);
//	}

	public void dump() {
//		System.out.println("-------------------------");
//		for(int idx=0; idx<layerLine.length; idx++) {
//			System.out.println(":"+idx+"("+(idx+bottom)+") : " +layerLine[idx].toString());
//		}
	}

//
//	public void apply() {
//		for(LayerLine ll : layerLine) {
//			ll.apply();
//		}
//		
//	}

	public void exclude(StartStopLayer layer) {
		for(int idx=0; idx<layerLine.length; idx++) {
			layerLine[idx].exclude(layer.layerLine[idx]);
		}
		left = null;
		right = null;
	}

	public void merge(Layer layer) {
		if (layer instanceof StartStopLayer) {
			mergeLayer((StartStopLayer) layer);
		} else {
			throw new RuntimeException("not implemented");
		}
	}
	
	public void mergeLayer(StartStopLayer layer) {
		for(long scanY=layer.bottom; scanY<=layer.top; scanY++) {
			int layerLineIndex = (int) (scanY-layer.bottom);
			StartStopLine mergeLine = layer.layerLine[layerLineIndex];
			if (mergeLine==null || mergeLine.isEmpty()) {
				continue;
			}
			int lineIndex = (int) (scanY-bottom);
			if (lineIndex<0 || lineIndex>=layerLine.length) {
				continue;
			}
			layerLine[lineIndex].merge(mergeLine);
			
		}
	}

	public boolean isEmpty() {
		for(StartStopLine line : layerLine) {
			if (line!=null && !line.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public Object selectAt(double nx, double ny, double zoomFactor) {
		return null;
	}
	
	public boolean dragTo(Object selected, double nx, double ny) {
		return false;
	}

}
