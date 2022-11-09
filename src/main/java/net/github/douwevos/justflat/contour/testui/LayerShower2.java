package net.github.douwevos.justflat.contour.testui;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.DiscLayerFillContext;
import net.github.douwevos.justflat.contour.DiscLayerOverlapCutter2;
import net.github.douwevos.justflat.contour.DiscLayerScaler;
import net.github.douwevos.justflat.contour.testui.examples.TestModelOne;
import net.github.douwevos.justflat.contour.testui.examples.TestModelOne2;
import net.github.douwevos.justflat.ttf.TextLayout;
import net.github.douwevos.justflat.ttf.TextLayoutToDiscLayer;
import net.github.douwevos.justflat.ttf.format.Ttf;
import net.github.douwevos.justflat.ttf.reader.TrueTypeFontParser;
import net.github.douwevos.justflat.types.Layer;
import net.github.douwevos.justflat.types.Point2D;

@SuppressWarnings("serial")
public class LayerShower2 extends JPanel implements Runnable, ComponentListener {

	private static Ttf ttf2;
	static {
		TrueTypeFontParser ttfParser = new TrueTypeFontParser();
		try {
			ttf2 = ttfParser.parse(new File("/usr/share/fonts/truetype/freefont/FreeSans.ttf"));
		} catch (IOException e) {
		}
	}

	
	private JComboBox<String> cmbTestModels;
	
	private ContourLayer designedLayer;
	
	private Layer cutLayer;
	
	
	private DiscLayerViewer baseViewer;
	private DiscLayerViewer moveLayerViewer;
	private DiscLayerViewer adaptiveLayerViewer;
	private DiscLayerViewer resultViewer;

	
	private DiscLayerViewer reachableLayerViewer1;
	private ScalerViewer2 scalerViewer2;
	private ScalerViewer3 scalerViewer3;

//	private volatile int thickness = 3758;
	private volatile int thickness = 3225;

	private volatile boolean dirty = true;
	
	List<ContourLayerTestProducer> testModelProducers = new ArrayList<>();
	
	public LayerShower2() {

		testModelProducers.add(new TestModelOne());
		testModelProducers.add(new TestModelOne2());
		
		List<String> testModelNames = testModelProducers.stream().map(s -> s.name()).collect(Collectors.toList());
		
//		List<String> testModelNames = new ArrayList<>(Arrays.asList("Fix 1", "Fix 2"));
		String[] array = testModelNames.toArray(new String[testModelNames.size()]);
		
		cmbTestModels = new JComboBox<>(array);
		cmbTestModels.setBounds(0,0, 100, 35);
		add(cmbTestModels);
		cmbTestModels.setSe
		cmbTestModels.addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
		          String item = (String) event.getItem();
		          ContourLayerTestProducer testProducer = testModelProducers.stream().filter(s -> s.name().equals(item)).findAny().orElse(null);
		          selectModel(testProducer);
		       }		
			System.exit(1);
			});
		
		designedLayer = createMainDiscLayer();
		
		setLayout(null);
		
		baseViewer = new DiscLayerViewer();
		baseViewer.setBounds(0,35, 100,100);
		
		add(baseViewer);

		int blockWidth = 1050;
		int blockHeight = 1000;
		
		moveLayerViewer = new DiscLayerViewer() {
			@Override
			public void onModelChanged() {
				dirty = true;
			}
		};
		moveLayerViewer.setBounds(150,0, blockWidth, blockHeight);
		moveLayerViewer.drawDiscSize = true;
		add(moveLayerViewer);

		Camera camera = moveLayerViewer.getCamera();
		
		adaptiveLayerViewer = new DiscLayerViewer();
		adaptiveLayerViewer.setCamera(camera, false);
		adaptiveLayerViewer.setBounds(180+blockWidth, 0, blockWidth,blockHeight);
		adaptiveLayerViewer.setFillWithAlpha(true);
		adaptiveLayerViewer.setDrawDirections(true);
		add(adaptiveLayerViewer);

		

		scalerViewer3 = new ScalerViewer3();
		scalerViewer3.setCamera(camera, false);
		scalerViewer3.setBounds(210+blockWidth*2, 0, blockWidth, blockHeight);
		add(scalerViewer3);

		
		
		blockWidth = 1050;
		
		
		reachableLayerViewer1 = new DiscLayerViewer();
		reachableLayerViewer1.setCamera(camera, false);
		reachableLayerViewer1.setBounds(150,blockHeight+20, blockWidth, blockHeight);
		reachableLayerViewer1.setDrawDirections(true);
		add(reachableLayerViewer1);

		scalerViewer2 = new ScalerViewer2();
		scalerViewer2.setCamera(camera, false);
		scalerViewer2.setBounds(180+blockWidth, blockHeight+20, blockWidth,blockHeight);
		add(scalerViewer2);

		resultViewer = new DiscLayerViewer();
		resultViewer.setCamera(camera, false);
		resultViewer.setBounds(210+blockWidth*2, blockHeight+20, blockWidth,blockHeight);
		add(resultViewer);

		
		JSlider jSlider = new JSlider(-10, 7000, thickness);
		jSlider.setOrientation(JSlider.VERTICAL);
		jSlider.setBounds(0, 200, 30, blockHeight);
		jSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				thickness = jSlider.getValue();
				System.out.println("thickness="+thickness);
			}
		});
		add(jSlider);

		new Thread(this).start();

	}
	
	private void selectModel(ContourLayerTestProducer testProducer) {
		designedLayer = testProducer.produceSourceLayer();
		dirty = true;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		addComponentListener(this);
	}
	
	public static void main(String[] args) {
		JFrame jFrame = new JFrame();
		jFrame.setSize(3000, 2000);
		jFrame.getContentPane().add(new LayerShower2());
		jFrame.setVisible(true);
		jFrame.setExtendedState(jFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	
	ContourLayer createMainDiscLayer() {
		TextLayout textLayout = new TextLayout(ttf2, "d");
		int textSize = 60000;

		TextLayoutToDiscLayer textLayoutToDiscLayer = new TextLayoutToDiscLayer(textLayout, textSize);
		ContourLayer discLayer = new ContourLayer(100000, 100000);
		textLayoutToDiscLayer.produceLayer(discLayer, 1000, 1000);
		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(32000, 39000));
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(33500, 22000));
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(18914, 9972));
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(22519, 10303));
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(23142, 15223));
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(14080, 14801));
		
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(31812, 14533));
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(29687, 15508));
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(29566, 14821));
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(14397, 14178));
		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(20944, 14946));
//		discLayer.moveDot(Point2D.of(28218, 40764), Point2D.of(22946, 14290));
		discLayer.contours.remove(1);

		
//		Contour boxContour = new Contour();
//		boxContour.add(Point2D.of(2000, 2000));
//		boxContour.add(Point2D.of(2000, 8000));
//		boxContour.add(Point2D.of(115024, 58174));
////		boxContour.add(Point2D.of(27438, 23144));
////		boxContour.add(Point2D.of(21627, 17798));
////		boxContour.add(Point2D.of(8000, 8000));
//		boxContour.add(Point2D.of(8000, 2000));
//		discLayer.add(boxContour);
		return discLayer;
	}

	
	@Override
	public void run() {
		
		int dd = -1;
		
		while(true) {
			if (dd == this.thickness && !dirty) {
//				System.out.println("dd="+dd);
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			dd = thickness;
			dirty = false;
			
			
			ContourLayer discLayer = designedLayer.duplicate();
			
	
			DiscLayerFillContext discLayerFillContext = new DiscLayerFillContext(discLayer);

			
			int discSize = 800;
			int discSizeSq = discSize*discSize;
			
			ContourLayer newLayer = discLayerFillContext.reduceResolution(discLayerFillContext.discLayer, discSizeSq, 1);

			DiscLayerOverlapCutter2 overlapCutter = new DiscLayerOverlapCutter2();
			newLayer = overlapCutter.scale(newLayer, false);

			
			discLayerFillContext.reduceResolution = newLayer;
			
			
			ContourLayer contourLayer = new ContourLayer(100000, 100000);
			int sequence=0;
			for(Contour c : newLayer) {
				contourLayer.add(c);
				c.setIndex(sequence++);
			}
			
			List<ContourLayer> layers = new ArrayList<>();
			for(int idx=0; idx<1; idx++) {
				ContourLayer scaledLayer = discLayerFillContext.scale(newLayer, dd, false);
				if (scaledLayer.isEmpty()) {
					break;
				}
				
				layers.add(scaledLayer);
				newLayer = scaledLayer;
				
				for(Contour c : newLayer) {
					contourLayer.add(c);
				}
			}
			
			discLayerFillContext.scaled = contourLayer;
			
			
			DiscLayerScaler discLayerScaler = new DiscLayerScaler();
			
			discLayerScaler.scale(discLayerFillContext.reduceResolution, dd, false);
			discLayerFillContext.scalerViewableModel = new ScalerViewableModel(discLayerScaler.allMutableContours);
			
			
			ContourLayer multiResultLayer = new ContourLayer(100000, 100000);
			newLayer = discLayerFillContext.reduceResolution;

			for(int idx=0; idx<6; idx++) {
				ContourLayer scaledLayer = discLayerFillContext.scale(newLayer, dd, false);
				if (scaledLayer.isEmpty()) {
					break;
				}
				
				layers.add(scaledLayer);
				newLayer = scaledLayer;
				
				for(Contour c : newLayer) {
					multiResultLayer.add(c);
				}
			}
			
			resultViewer.setModel(multiResultLayer);
			
			callPaint(discLayerFillContext);
			
		}
		
		
	}

	private void callPaint(DiscLayerFillContext discLayerFillContext) {
		if (discLayerFillContext.discLayer!=null) {
			ContourLayer duplicate = discLayerFillContext.discLayer.duplicate();

			moveLayerViewer.setModel(designedLayer);
			baseViewer.setModel(designedLayer);
			
		}
		if (discLayerFillContext.reduceResolution!=null) {
			ContourLayer duplicate = discLayerFillContext.reduceResolution.duplicate();
			this.adaptiveLayerViewer.setModel(duplicate);
		}

		if (discLayerFillContext.scaled != null) {
			reachableLayerViewer1.setModel(discLayerFillContext.scaled);
		}

		if (discLayerFillContext.scalerViewableModel != null) {
			scalerViewer2.setModel(discLayerFillContext.scalerViewableModel);
			scalerViewer3.setModel(discLayerFillContext.scalerViewableModel);
		}


		
		repaint();
		
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
		
		int baseX = 150;
		int inset = 20;

		int blockWidth = (dimension.width-baseX - inset*2)/3;
		int blockHeight = (dimension.height - inset)/2;
		
		int col0 = baseX;
		int col1 = baseX + blockWidth+inset;
		int col2 = baseX + (blockWidth+inset)*2;
		
		moveLayerViewer.setBounds(col0, 0, blockWidth, blockHeight);
		adaptiveLayerViewer.setBounds(col1, 0, blockWidth,blockHeight);
		scalerViewer3.setBounds(col2, 0, blockWidth, blockHeight);

		int row1 = blockHeight+inset;

		reachableLayerViewer1.setBounds(col0, row1, blockWidth, blockHeight);
		scalerViewer2.setBounds(col1, row1, blockWidth, blockHeight);
		resultViewer.setBounds(col2, row1, blockWidth, blockHeight);

	}

	
	public interface ScalerConfigProvider {
		int getThickness();
	}
	
}
