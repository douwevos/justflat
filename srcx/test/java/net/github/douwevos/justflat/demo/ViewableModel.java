package net.github.douwevos.justflat.demo;

import net.github.douwevos.justflat.types.values.Bounds2D;

public interface ViewableModel {

	Bounds2D bounds();

	Selection<?> selectAt(ModelMouseEvent modelMouseEvent);
	
}