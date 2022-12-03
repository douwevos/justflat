package net.github.douwevos.justflat.contour.testui;

import net.github.douwevos.justflat.contour.ContourLayer;

public interface ContourLayerTestProducer {

	
	default String name() {
		return getClass().getSimpleName();
	}
	
	ContourLayer produceSourceLayer();

	ContourLayer produceResultLayer();

	int getThickness();
	
	default boolean doReduceFirst() {
		return true;
	}
}
