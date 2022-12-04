package net.github.douwevos.justflat.startstop;

import java.util.Arrays;

import net.github.douwevos.justflat.types.values.StartStop;

public class StartStopLineTest {


	public static void main(String[] args) {
		StartStopLine linePre = new StartStopLine();
		StartStopLine linePost = new StartStopLine();
		StartStopLine layerLine = new StartStopLine();
		layerLine.startStops = new StartStop[] { new StartStop(10,15), new StartStop(19,35) };
//		layerLine.add(127, true);
//		layerLine.add(9, false);
//		layerLine.add(9, false);
//		layerLine.apply();
		
		layerLine.invert(Arrays.asList(new StartStop(4,30)));
		dumpLine(layerLine.startStops, 50);

//		linePre.startStops = new StartStop[] { new StartStop(10,27), new StartStop(35, 42) };
//		linePost.startStops = new StartStop[] { new StartStop(12,20), new StartStop(25,25), new StartStop(30, 37) };
//
//		dumpLine(linePre.startStops, 50);
//		dumpLine(layerLine.startStops, 50);
//		dumpLine(linePost.startStops, 50);
//
//		LayerLine edge1 = linePre.duplicate();
//		edge1.edge(new LayerLine(), layerLine);
//
//		LayerLine edge2 = layerLine.duplicate();
//		edge2.edge(linePre, linePost);
//
//		LayerLine edge3 = linePost.duplicate();
//		edge3.edge(layerLine, new LayerLine());
//		
//		System.out.println();
//		
//		dumpLine(edge1.startStops, 50);
//		dumpLine(edge2.startStops, 50);
//		dumpLine(edge3.startStops, 50);

//		System.out.println(layerLine);
	}

	
	public static void dumpLine(StartStop[] startStops, int till) {
		StringBuilder buf = new StringBuilder();
		for(StartStop ss : startStops) {
			while(buf.length()<ss.start) {
				buf.append("-");
			}

			while(buf.length()<=ss.stop) {
				buf.append("*");
			}
		}
		while(buf.length()<till) {
			buf.append("-");
		}
		System.out.println(buf);
	}

}
