package net.github.douwevos.justflat.demo;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

public class ModelMouseEvent {

	public final MouseEvent event;
	public final Camera camera;
	public final double modelX;
	public final double modelY;
	
	public ModelMouseEvent(MouseEvent event, Camera camera, double modelX, double modelY) {
		this.event = event;
		this.camera = camera;
		this.modelX = modelX;
		this.modelY = modelY;
	}
	
	
	public static ModelMouseEvent create(MouseEvent awtEvent, Camera camera, Dimension viewDimension) {
		int mouseY = awtEvent.getY();
		int mouseX = awtEvent.getX();
		
		int bottom = viewDimension.height-1;
		int loc = bottom-mouseY;

		double cameraZoom = camera.getZoom();
		double ny = camera.getTranslateY() + loc * cameraZoom;
		double nx = camera.getTranslateX() + mouseX * cameraZoom;
		return new ModelMouseEvent(awtEvent, camera, nx, ny);
		
	}
}
