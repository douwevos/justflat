package net.github.douwevos.justflat.types.values;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.github.douwevos.justflat.demo.Camera;

public class Line2DDemo extends JPanel implements ComponentListener {

	
	Line2DViewer designViewer;
	
	Line2DCrossPointViewer infoViewer;
	
	Line2DRelationViewer relationViewer;
	
	public Line2DDemo() {
		
		
		designViewer = new Line2DViewer() {
			@Override
			public void onModelChanged() {
				if (infoViewer!=null) {
					infoViewer.onModelChanged();
					infoViewer.onCameraChanged();
				}
				if (relationViewer!=null) {
					relationViewer.onModelChanged();
					relationViewer.onCameraChanged();
				}
//				dirty = true;
			}
		};
		designViewer.setBounds(150,0, 1000, 800);
		add(designViewer);
		
		Line2D lineA = new Line2D(Point2D.of(7000, 20), Point2D.of(100000, 11000));
		Line2D lineB = new Line2D(Point2D.of(150, 1600), Point2D.of(80000, 1000));
		Line2DViewableModel model = new Line2DViewableModel(lineA, lineB);
		designViewer.setModel(model);

		Camera camera = designViewer.getCamera();

		infoViewer = new Line2DCrossPointViewer();
		infoViewer.setModel(model);
		infoViewer.setCamera(camera, false);
		add(infoViewer);
		
		relationViewer = new Line2DRelationViewer();
		relationViewer.setModel(model);
		relationViewer.setCamera(camera, false);
		add(relationViewer);
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		addComponentListener(this);
	}

	
	public static void main(String[] args) {
		JFrame jFrame = new JFrame();
		jFrame.setSize(3000, 2000);
		jFrame.getContentPane().add(new Line2DDemo());
		jFrame.setVisible(true);
		jFrame.setExtendedState(jFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	
	@Override
	public void componentHidden(ComponentEvent e) {
	}
	
	@Override
	public void componentMoved(ComponentEvent e) {
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		updateLayout();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}
	
	
	private void updateLayout() {
		Dimension dimension = getSize();
		int width = dimension.width;
		int height = dimension.height;

		int blockWidth = width/2;
		int blockHeight = height/2;
		
		designViewer.setBounds(0, 0, blockWidth-5, blockHeight);
		
		infoViewer.setBounds(blockWidth,0,blockWidth-5, blockHeight);

		relationViewer.setBounds(0, blockHeight, blockWidth-5, blockHeight);
	}
	
	
	
	
}
