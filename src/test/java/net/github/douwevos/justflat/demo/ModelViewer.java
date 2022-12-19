package net.github.douwevos.justflat.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import net.github.douwevos.justflat.demo.Camera.CameraListener;
import net.github.douwevos.justflat.types.values.Bounds2D;
import net.github.douwevos.justflat.types.values.Point2D;


@SuppressWarnings("serial")
public abstract class ModelViewer<T extends ViewableModel> extends JPanel implements MouseListener, MouseWheelListener, ComponentListener, MouseMotionListener, CameraListener {

	protected Camera camera;
	protected boolean cameraOwner;
	
	protected T model;
	
	protected Image layerImage;
	
	protected Selection<?> highlighted;
	
	protected boolean fillWithAlpha = false;
	
	protected String title;
	
	public ModelViewer() {
		camera = new Camera();
		cameraOwner = true;
		camera.addListener(this);
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addComponentListener(this);
		updateCamera();
	}
	
	public void setFillWithAlpha(boolean fillWithAlpha) {
		this.fillWithAlpha = fillWithAlpha;
	}
	
	public void setCamera(Camera camera, boolean cameraOwner) {
		if (this.camera!=camera) {
			this.camera.removeListener(this);
			this.camera = camera;
			camera.addListener(this);
		}
		this.cameraOwner = cameraOwner;
		if (cameraOwner) {
			updateCamera();
		}
	}

	public Camera getCamera() {
		return camera;
	}

	
	@Override
	public void paint(Graphics g) {
		Graphics2D gfx = (Graphics2D) g.create();
		int width2 = getWidth();
		int height2 = getHeight();
		gfx.setColor(hasFocus() ? Color.BLUE.brighter() : Color.BLACK);
		gfx.drawRect(0, 0, width2-1, height2-1);
		
		if (layerImage == null) {
			layerImage = createLayerImage(model);
		}
		if (layerImage != null) {
			gfx.drawImage(layerImage, 1, 1,  null);
			paintOnTopLayer(gfx, model);
		}

		Selection<?> localSelected = highlighted;
		if (localSelected != null) {
			paintSelected(gfx, localSelected);
		}
		
		setFocusable(true);
		gfx.dispose();
	}

	public abstract void paintOnTopLayer(Graphics2D gfx, T model);
	
	
	protected void paintSelected(Graphics2D g, Selection<?> selected) {
		if (selected instanceof Point2D) {
			Point2D dot = (Point2D) selected;
			paintSelectedPointWithLocation(g, dot);
		}
	}

	protected void paintSelectedPointWithLocation(Graphics2D g, Point2D dot) {
		Dimension viewDimension = getViewDimension();
		g.setColor(Color.green);
		drawPointWithLocation(g, viewDimension, dot);
	}

	protected void drawPointWithLocation(Graphics g, Dimension viewDimension, Point2D dot) {
		double transX =  -camera.getTranslateX();
		double transY =  camera.getTranslateY();
		double cameraZoom = camera.getZoom();

		
		long xb = dot.x;
		long yb = dot.y;
		
		int ixa = (int) Math.round((transX + xb)/cameraZoom);
		int iya = viewDimension.height+(int) Math.round((transY - yb)/cameraZoom);

		g.drawArc(ixa-6, iya-6, 13,13,0,360);

		Color color = g.getColor();
		drawVisibleText(g, ""+dot, ixa+10, iya+4, color, new Color(0,0,0,160));
		g.setColor(color);
	}

	protected void drawPointNoLocation(Graphics g, Dimension viewDimension, Point2D dot) {
		double transX =  -camera.getTranslateX();
		double transY =  camera.getTranslateY();
		double cameraZoom = camera.getZoom();

		
		long xb = dot.x;
		long yb = dot.y;
		
		int ixa = (int) Math.round((transX + xb)/cameraZoom);
		int iya = viewDimension.height+(int) Math.round((transY - yb)/cameraZoom);

		g.drawArc(ixa-6, iya-6, 13,13,0,360);
	}


	protected void drawVisibleText(Graphics gfx, String text, int x, int y, Color colorText, Color colorRect) {
		if (colorRect != null) {
			FontMetrics fontMetrics = gfx.getFontMetrics();
			Rectangle2D textBounds = fontMetrics.getStringBounds(text, gfx);
			int textX = (int) Math.floor(textBounds.getX());
			int textY = (int) Math.floor(textBounds.getY());
			int textWidth = (int) Math.ceil(textBounds.getWidth());
			int textHeight = (int) Math.ceil(textBounds.getHeight());
			
			int insets = 3;
			gfx.setColor(colorRect);
			gfx.fillRect(x + textX - insets, y+textY-insets, 2*insets + textWidth, 2*insets + textHeight);
			
			gfx.setColor(colorText);
			gfx.drawRect(x + textX - insets, y+textY-insets, 2*insets + textWidth, 2*insets + textHeight);
		}
		gfx.setColor(colorText);
		gfx.drawString(text, x, y);
		
	}


	public void setModel(T model) {
//		if (this.modelLayer != null) {
//			return;
//		}
		
		this.model = model;
		layerImage = null;
		updateCamera();
		repaint();
	}
	
	public T getModel() {
		return model;
	}
	
	public Dimension getViewDimension() {
		return new Dimension(getWidth()-2, getHeight()-2);
	}
	

	private Image createLayerImage(T modelLayer) {
		if (modelLayer==null) {
			return null;
		}
		Dimension viewDimension = getViewDimension();
		if (viewDimension.width <=0 ||viewDimension.height<=0) {
			return null;
		}
		BufferedImage image = new BufferedImage(viewDimension.width, viewDimension.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2d = image.createGraphics();
		paintModel(image, graphics2d, modelLayer);
		
		if (title!=null) {
			Font font = graphics2d.getFont();
			Font font2 = new Font(font.getName(), Font.PLAIN, font.getSize()*2);
			FontMetrics fontMetrics = graphics2d.getFontMetrics(font2);
			Rectangle2D textBounds = fontMetrics.getStringBounds(title, graphics2d);
			int textWidth = (int) Math.round(textBounds.getWidth());
			int textHeight = (int) Math.round(textBounds.getHeight());
			
			int left = (getWidth()-textWidth)/2;
			
			graphics2d.setColor(new Color(0,0,0,120));
			graphics2d.fillRect(left, 0, textWidth, textHeight);
			graphics2d.setFont(font2);
			graphics2d.setColor(new Color(224,224,224,180));
			graphics2d.drawString(title, left, 20);
		}

		
		graphics2d.dispose();

		return image;
	}
	
	protected abstract void paintModel(BufferedImage image, Graphics2D gfx, T model);
	

	@Override
	public void mouseClicked(MouseEvent e) {
		int button = e.getButton();
//		if (button == 1) {
			grabFocus();
			if (e.getClickCount()==2) {
				camera.setLockType(CameraLockType.FIT_MODEL);
				updateCamera();
			} else {
				ModelMouseEvent modelEvent = ModelMouseEvent.create(e, camera, getViewDimension());
				onClicked(modelEvent);
			}
//		}
		
		
	}


	int dragX;
	int dragY;
	double dragTX;
	double dragTY;
	
	@Override
	public void mousePressed(MouseEvent e) {
		dragX = e.getX();
		dragY = e.getY();
		dragTX = camera.getTranslateX();
		dragTY = camera.getTranslateY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		ModelMouseEvent modelEvent = ModelMouseEvent.create(e, camera, getViewDimension());
		if (model!=null && highlighted!=null) {
			if (onDrag(modelEvent, highlighted)) {
				layerImage = null;
				onModelChanged();
				repaint();
				return;
			}
		}

		int mouseX = e.getX();
		int mouseY = e.getY();
		double cameraZoom = camera.getZoom();
		double nx = dragTX + (dragX - mouseX)*cameraZoom;
		double ny = dragTY + (mouseY - dragY)*cameraZoom;

		camera.setTranslate(nx,ny);
	}

	public abstract boolean onDrag(ModelMouseEvent event, Selection<?> selected);
	
	public void onModelChanged() {
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		ModelMouseEvent event = ModelMouseEvent.create(e, camera, getViewDimension());
//		int mouseY = e.getY();
//		int mouseX = e.getX();
//		
//		int bottom = getViewDimension().height-1;
//		int loc = bottom-mouseY;
//
//		double cameraZoom = camera.getZoom();
//		double ny = camera.getTranslateY() + loc * cameraZoom;
//		double nx = camera.getTranslateX() + mouseX * cameraZoom;
//		onMove(e, nx, ny);
		onMove(event);
	}
	
	protected void onMove(ModelMouseEvent modelEvent) {
		Selection<?> selectedOld = highlighted;
		highlighted = model==null ? null : model.selectAt(modelEvent);

		if (selectedOld == highlighted) {
			return;
		}
		repaint();
	}
	
	

	protected void onClicked(ModelMouseEvent modelEvent) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int mouseX = e.getX();
		int viewHeight = e.getComponent().getHeight();
		int mouseY = viewHeight-e.getY();
		
		int wheelRotation = e.getWheelRotation();
		
		if (wheelRotation<0) {
			camera.zoomIn(mouseX, mouseY);
		} else if (wheelRotation>0) {
			camera.zoomOut(mouseX, mouseY);
		}
	}

	private void updateCamera() {
		if (!cameraOwner) {
			return;
		}
		CameraLockType lockType = camera.getLockType();
		if (lockType == CameraLockType.FREE) {
			return;
		}
		T layer = model;
		int viewWidth = getWidth();
		int viewHeight = getHeight();
		if (layer==null || viewWidth==0 || viewHeight<0) {
			camera.setValues(1d, 0, 0);
			return;
		}
		int INSETS = 10;
		int mViewWidth = viewWidth - INSETS*2;
		int mViewHeight = viewHeight - INSETS*2;
		
		double ty = 0;
		double tx = 0;
		double zoom = 1d;
		Bounds2D bounds = layer.bounds();
		if (bounds != null) {

	//		if (camera.cameraLockType == CameraLockType.FIT_LAYER) {
	//		}
	
			int modelHeight = (int) (1l + bounds.top - bounds.bottom);
			int modelWidth = (int) (1l + bounds.right-bounds.left);
			
			double zoomY = (double) modelHeight/mViewHeight;
			double zoomX = (double) modelWidth/mViewWidth;
			zoom = zoomY;
	
			if (zoomX>zoomY) {
				zoom = zoomX;
				tx = -INSETS*zoom + bounds.left;
				ty = -INSETS*zoom + bounds.bottom + (modelHeight-viewHeight*zoom)/2;
			} else {
				zoom = zoomY;
				ty = -INSETS*zoom + bounds.bottom;
				tx = -INSETS*zoom + bounds.left + (modelWidth-viewWidth*zoom)/2;
			}
		}

		
		camera.setValues(zoom, tx, ty);
	}


	@Override
	public void componentResized(ComponentEvent e) {
		updateCamera();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		updateCamera();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		updateCamera();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void onCameraChanged() {
		layerImage = null;
		repaint();
	}
	
}
