package net.github.douwevos.justflat.types.values;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.github.douwevos.justflat.demo.Camera;

public class Line2DDemo extends JPanel implements ComponentListener {

	
	Line2DViewer designViewer;
	
	public Line2DDemo() {
		
		
		designViewer = new Line2DViewer() {
			@Override
			public void onModelChanged() {
//				dirty = true;
			}
		};
		designViewer.setBounds(150,0, 1000, 800);
		add(designViewer);
		
		Line2D lineA = new Line2D(Point2D.of(100, 100), Point2D.of(1000, 110));
		Line2D lineB = new Line2D(Point2D.of(150, 160), Point2D.of(800, 10));
		designViewer.setModel(new Line2DViewableModel(lineA, lineB));

		Camera camera = designViewer.getCamera();
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
		designViewer.setBounds(0, 0, width, height);
	}
	
	
	
	
}
