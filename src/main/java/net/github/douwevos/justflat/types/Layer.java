package net.github.douwevos.justflat.types;

import java.util.List;

public interface Layer {

	long bottom();
	long top();
	
	<T extends Layer> T duplicate();

	Bounds2D bounds();
	boolean testDot(long x, long y, boolean defaultValue);
	boolean isEmpty();
	void merge(Layer layer);

//	Object selectAt(double nx, double ny, double zoomFactor);
//	boolean dragTo(Object selected, double nx, double ny);
	
}