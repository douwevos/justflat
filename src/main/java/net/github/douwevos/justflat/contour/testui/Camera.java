package net.github.douwevos.justflat.contour.testui;

import java.util.ArrayList;
import java.util.List;

import net.github.douwevos.justflat.types.values.Point2D;

class Camera {
	
	private CameraLockType cameraLockType;
	private double zoom;
	private double translateX;
	private double translateY;
	private List<CameraListener> listeners = new ArrayList<>();
	

	public Camera() {
		cameraLockType = CameraLockType.FIT_MODEL;
		zoom = 1d;
	}
	
	
	public double getZoom() {
		return zoom;
	}
	
	public double getTranslateX() {
		return translateX;
	}
	
	public double getTranslateY() {
		return translateY;
	}
	
	public CameraLockType getLockType() {
		return cameraLockType;
	}
	
	public void setLockType(CameraLockType cameraLockType) {
		this.cameraLockType = cameraLockType;
		notifyChanged();
	}
	
	public void zoomIn(int x, int y) {
		zoomWith(x, y, zoom*0.9d);
	}

	public void zoomOut(int x, int y) {
		zoomWith(x, y, zoom*1.1d);
	}

	public void zoomWith(int x, int y, double newZoom) {
		if (newZoom == zoom) {
			return;
		}
		cameraLockType = CameraLockType.FREE;

		double nx = translateX + x*zoom;
		double ny = translateY + y*zoom;

		zoom = newZoom;
		nx = nx - x*zoom;
		ny = ny - y*zoom;
		translateX = nx;
		translateY = ny;
		notifyChanged();
	}


	public void setValues(double zoom, double x, double y) {
		if (this.zoom == zoom && translateX==x && translateY==y) {
			return;
		}
		this.zoom = zoom;
		this.translateX = x;
		this.translateY = y;
		notifyChanged();
	}

	public void setTranslate(double nx, double ny) {
		if (translateX == nx && translateY == ny) {
			return;
		}
		cameraLockType = CameraLockType.FREE;
		translateX = nx;
		translateY = ny;
		notifyChanged();
	}

	
	public void addListener(CameraListener cameraListener) {
		if (!listeners.contains(cameraListener)) {
			listeners.add(cameraListener);
		}
	}
	
	public void removeListener(CameraListener cameraListener) {
		listeners.remove(cameraListener);
	}

	private void notifyChanged() {
		for(CameraListener listener : listeners) {
			listener.onCameraChanged();
		}
	}
	
	public interface CameraListener {
		void onCameraChanged();
	}

	public Point2D toViewCoords(Point2D p, int viewHeight) {
		long xb = p.getX();
		long yb = p.getY();
		long ixb = Math.round((-translateX + xb)/zoom);
		long iyb = viewHeight + Math.round((translateY - yb)/zoom);
		return Point2D.of(ixb, iyb);
	}

	
}