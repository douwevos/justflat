package net.github.douwevos.justflat.startstop;

import java.util.Arrays;
import java.util.List;

import net.github.douwevos.justflat.types.Point2D;


public class OnOffArea {

	private long bottom;
	public OnOffLine onOffLine[];

	public OnOffArea(long bottom, int height) {
		this.bottom = bottom;
		onOffLine = new OnOffLine[height];
	}
	
	public void reset() {
		for(OnOffLine line : onOffLine) {
			if (line!=null) {
				line.reset();
			}
		}
		
	}

	
	public void line(Point2D pa, Point2D pb) {
		line(pa.x, pa.y, pb.x, pb.y);
	}

	public void line(long xa, long ya, long xb, long yb) {
		long deltaYBA = yb - ya;
		int delta = (int) deltaYBA;
		if (delta==0) {
			if (xb==xa) {
				return;
			}
//			System.out.println("["+xa+","+ya+"] - ["+xb+","+yb+"]");
//			write(xa,ya);
//			write(xb,yb);
			return;
		}
//		System.out.println("["+xa+","+ya+"] - ["+xb+","+yb+"]");
		boolean up = true;
		if (delta<0) {
			delta = -delta;
			up = false;
		}
		long deltaXBA = xb - xa;
		
		for(int step=0; step<=delta; step++) {
			long ny = ya + Math.round((step*deltaYBA)/delta);
			long nx = xa + Math.round((step*deltaXBA)/delta);
//			System.out.println("  ["+nx+","+ny+"] - "+(up ? 'u' : 'd'));
			write(nx, ny, up);
		}
		
	}
	

	public void write(long x, long y, boolean up) {
		int lineIndex = (int) (y-bottom);
		if (lineIndex<0 || lineIndex>=onOffLine.length) {
			return;
		}
		
		if (onOffLine[lineIndex] == null) {
			onOffLine[lineIndex] = new OnOffLine();
		}
		onOffLine[lineIndex].add(x, up);
		
//		System.out.println("     " + layerLine[(int) (y-bottom)]);
	}
	
	public List<StartStop> lineToStartStopList(int lineIndex) {
		if (lineIndex<0 || lineIndex>=onOffLine.length) {
			return null;
		}
		OnOffLine onOffLine = this.onOffLine[lineIndex];
		if (onOffLine==null || onOffLine.rawDots.isEmpty()) {
			return null;
		}
		
		return onOffLine.apply();
	}
	
	
	public static void main(String[] args) {
		OnOffLine onOffLine = new OnOffLine();
		
		onOffLine.add(10, true);
		onOffLine.add(11, true);
		onOffLine.add(19, false);
		onOffLine.add(20, false);
		List<StartStop> apply = onOffLine.apply();
		StartStopLine layerLine= new StartStopLine();
		layerLine.invert(apply);
//		layerLine.startStops = apply.toArray(new StartStop[apply.size()]);
		System.out.println("layerLine.startStops="+Arrays.asList(layerLine.startStops));
		StartStopLine.dumpLine(layerLine.startStops, 100);
	}
	
	

}
