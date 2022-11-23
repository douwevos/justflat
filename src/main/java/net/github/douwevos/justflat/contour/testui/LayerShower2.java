package net.github.douwevos.justflat.contour.testui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.github.douwevos.justflat.contour.Contour;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.DiscLayerFillContext;
import net.github.douwevos.justflat.contour.DiscLayerOverlapCutter2;
import net.github.douwevos.justflat.contour.DiscLayerScaler;
import net.github.douwevos.justflat.contour.testui.examples.ScalarTests;
import net.github.douwevos.justflat.contour.testui.examples.ScalarTests.ScalarTest;
import net.github.douwevos.justflat.ttf.TextLayout;
import net.github.douwevos.justflat.ttf.TextLayoutToDiscLayer;
import net.github.douwevos.justflat.ttf.format.Ttf;
import net.github.douwevos.justflat.ttf.reader.TrueTypeFontParser;
import net.github.douwevos.justflat.types.Layer;
import net.github.douwevos.justflat.types.Point2D;

@SuppressWarnings("serial")
public class LayerShower2 extends JPanel implements Runnable, ComponentListener {

	private JComboBox<String> cmbTestModels;
	
	private ContourLayer designedLayer;
	
	private Layer cutLayer;
	
	
	private ContourLayerViewer baseViewer;
	private ContourLayerViewer moveLayerViewer;
	private ContourLayerViewer adaptiveLayerViewer;
	private ContourLayerViewer resultViewer;

	
	private ContourLayerViewer reachableLayerViewer1;
	private ScalerViewer2 scalerViewer2;
	private ScalerViewer3 scalerViewer3;

//	private volatile int thickness = 3758;
	private volatile int thickness = 3225;

	private volatile boolean dirty = true;
	
	ScalarTests scalarTests;
	
	public LayerShower2() {

		scalarTests = new ScalarTests();

		designedLayer = new ContourLayer(100, 100);
		
		List<String> testModelNames = scalarTests.streamNames().collect(Collectors.toList());
		testModelNames.add(0, "Please select");
		
		String[] array = testModelNames.toArray(new String[testModelNames.size()]);
		
		cmbTestModels = new JComboBox<>(array);
		cmbTestModels.setBounds(0,0, 140, 35);
		add(cmbTestModels);
		cmbTestModels.setRenderer(new ComboBoxRenderer(cmbTestModels));
		cmbTestModels.addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
		          String item = (String) event.getItem();
		          ScalarTest scalarTest = scalarTests.get(item);
		          ContourLayerTestProducer testProducer = scalarTest.producer;
		          selectModel(testProducer);
		       }		
//			System.exit(1);
			});
		
		
		Action actOutputScaled = new AbstractAction("out") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				writeScaled();
			}

		};
		JButton butOutputScaled = new JButton(actOutputScaled);
		add(butOutputScaled);
		butOutputScaled.setBounds(0,35, 140,35);
		
		setLayout(null);
		
		baseViewer = new ContourLayerViewer();
		baseViewer.setBounds(0,70, 140,100);
		
		add(baseViewer);

		int blockWidth = 1050;
		int blockHeight = 1000;
		
		moveLayerViewer = new ContourLayerViewer() {
			@Override
			public void onModelChanged() {
				dirty = true;
			}
		};
		moveLayerViewer.setBounds(150,0, blockWidth, blockHeight);
		moveLayerViewer.drawDiscSize = true;
		add(moveLayerViewer);

		Camera camera = moveLayerViewer.getCamera();
		
		adaptiveLayerViewer = new ContourLayerViewer();
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
		
		
		reachableLayerViewer1 = new ContourLayerViewer() {
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				drawVisibleText(g, ""+thickness, 5, 30, Color.yellow, Color.black);
			}
		};
		reachableLayerViewer1.setCamera(camera, false);
		reachableLayerViewer1.setBounds(150,blockHeight+20, blockWidth, blockHeight);
		reachableLayerViewer1.setDrawDirections(true);
		add(reachableLayerViewer1);

		scalerViewer2 = new ScalerViewer2();
		scalerViewer2.setCamera(camera, false);
		scalerViewer2.setBounds(180+blockWidth, blockHeight+20, blockWidth,blockHeight);
		add(scalerViewer2);

		resultViewer = new ContourLayerViewer();
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
		thickness = testProducer.getThickness();
		designedLayer = testProducer.produceSourceLayer();
		dirty = true;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		addComponentListener(this);
		
		ScalarTest failedTest = scalarTests.streamNames().map(s -> scalarTests.get(s)).filter(s -> !s.testOk).findFirst().orElse(null);
		if (failedTest != null) {
			cmbTestModels.setSelectedItem(failedTest.name());
		}

	}
	
	public static void main(String[] args) {
		JFrame jFrame = new JFrame();
		jFrame.setSize(3000, 2000);
		jFrame.getContentPane().add(new LayerShower2());
		jFrame.setVisible(true);
		jFrame.setExtendedState(jFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
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

	private void writeScaled() {
		PrintWriter out = new PrintWriter(System.out);

		out.println("	@Override");
		out.println("	public int getThickness() {");
		out.println("		return "+thickness+";");
		out.println("	}");
		out.println();

		out.println("	@Override");
		out.println("	public String name() {");
		out.println("		return getClass().getSimpleName();");
		out.println("	}");
		out.println();
		
		out.println("	public ContourLayer produceSourceLayer() {");
		out.println("		ContourLayer contourLayer = new ContourLayer("+designedLayer.getWidth()+", "+designedLayer.getHeight()+");");
		
		int contourOffset = 0;
		for(Contour contour : designedLayer) {
			String contourVarName = "contour"+(contourOffset++);
			out.println("		Contour "+contourVarName+" = new Contour();");
			for(Point2D p : contour) {
				out.println("		"+contourVarName+".add(Point2D.of("+p.x+", "+p.y+"));");
			}
			out.println("		"+contourVarName+".setClosed("+contour.isClosed()+");");
			out.println("		contourLayer.add("+contourVarName+");");
			out.println();
		}
		out.println("		return contourLayer;");
		out.println("	}");
		out.println();

		
		ContourLayer contourLayer = scalarTests.applyScaling(designedLayer, thickness);
		out.println("	public ContourLayer produceResultLayer() {");
		out.println("		ContourLayer contourLayer = new ContourLayer("+contourLayer.getWidth()+", "+contourLayer.getHeight()+");");
		
		for(Contour contour : contourLayer) {
			String contourVarName = "contour"+(contourOffset++);
			out.println("		Contour "+contourVarName+" = new Contour();");
			for(Point2D p : contour) {
				out.println("		"+contourVarName+".add(Point2D.of("+p.x+", "+p.y+"));");
			}
			out.println("		"+contourVarName+".setClosed("+contour.isClosed()+");");
			out.println("		contourLayer.add("+contourVarName+");");
			out.println();
		}
		out.println("		return contourLayer;");
		out.println("	}");
		out.flush();
		
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
	
	
	
	class ComboBoxRenderer extends JPanel implements ListCellRenderer {

	    private static final long serialVersionUID = -1L;

	    JPanel textPanel;
	    JLabel text;

	    public ComboBoxRenderer(JComboBox combo) {

	        textPanel = new JPanel();
	        textPanel.add(this);
	        text = new JLabel();
	        text.setOpaque(true);
	        text.setFont(combo.getFont());
	        textPanel.add(text);
	    }


	    @Override
	    public Component getListCellRendererComponent(JList list, Object value,
	            int index, boolean isSelected, boolean cellHasFocus) {

	        if (isSelected) {
	            setBackground(list.getSelectionBackground());
	        }
	        else {
	            setBackground(Color.WHITE);
	        }

	        String name = value.toString();
	        ScalarTest scalarTest = scalarTests.get(name);

	        text.setBackground(getBackground());
	        text.setText(value.toString());
	        
	        if (scalarTest== null || scalarTest.testOk) {
	        	text.setForeground(Color.BLACK);
	        } else {
	        	text.setForeground(Color.RED);
	        }
	        
	        return text;
	    }
	}
}
