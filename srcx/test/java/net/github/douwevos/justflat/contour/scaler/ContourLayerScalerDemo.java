package net.github.douwevos.justflat.contour.scaler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
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
import net.github.douwevos.justflat.contour.ContourLayerOverlapCutter;
import net.github.douwevos.justflat.contour.testui.examples.ScalarTests;
import net.github.douwevos.justflat.contour.testui.examples.ScalarTests.ScalarTest;
import net.github.douwevos.justflat.demo.Camera;
import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.types.Layer;
import net.github.douwevos.justflat.types.values.Point2D;

@SuppressWarnings("serial")
public class ContourLayerScalerDemo extends JPanel implements Runnable, ComponentListener {

	private final static Log log = Log.instance(false);

	private static final boolean RUN_REPEATS = false;
	
	private JComboBox<String> cmbTestModels;
	
	private ContourLayer designedLayer;
	
	
	private ContourLayerViewer baseViewer;
	private ContourLayerViewer designViewer;
	private ContourLayerViewer onOffCutViewer;
	private ContourLayerViewer resultViewer;

	
	private ContourLayerViewer inputAndScaledViewer;
	private RouteListScaledDownCombinatorViewer routeListCombinatorViewer; 
//	private ScalerSegmentViewer scalerViewer2;
	private ScalerProjectionViewer scalerProjectionViewer;

//	private volatile int thickness = 3758;
	private volatile int thickness = 3225;

	private volatile boolean dirty = true;
	
	private volatile boolean doReduceLayer = false;
	
	ScalarTests scalarTests;
	
	public ContourLayerScalerDemo() {

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
		
		designViewer = new ContourLayerViewer() {
			{
				title = "Design";
			}
			
			@Override
			public void onModelChanged() {
				dirty = true;
			}
		};
		designViewer.setBounds(150,0, blockWidth, blockHeight);
		designViewer.drawDiscSize = true;
		add(designViewer);

		Camera camera = designViewer.getCamera();
		
		onOffCutViewer = new ContourLayerViewer() { { title = "On-Off overlap cutter"; } };
		onOffCutViewer.setCamera(camera, false);
		onOffCutViewer.setBounds(180+blockWidth, 0, blockWidth,blockHeight);
		onOffCutViewer.setFillWithAlpha(true);
		onOffCutViewer.setDrawDirections(true);
		add(onOffCutViewer);

		

		scalerProjectionViewer = new ScalerProjectionViewer() {{ title = "Scaler projection"; }};
		scalerProjectionViewer.setCamera(camera, false);
		scalerProjectionViewer.setBounds(210+blockWidth*2, 0, blockWidth, blockHeight);
		add(scalerProjectionViewer);

		
		
		blockWidth = 1050;
		
		
		inputAndScaledViewer = new ContourLayerViewer() {
			{ title = "Input & Scaled"; }
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				drawVisibleText(g, ""+thickness, 5, 30, Color.yellow, Color.black);
			}
		};
		inputAndScaledViewer.setCamera(camera, false);
		inputAndScaledViewer.setBounds(150,blockHeight+20, blockWidth, blockHeight);
		inputAndScaledViewer.setDrawDirections(true);
		add(inputAndScaledViewer);

		routeListCombinatorViewer = new RouteListScaledDownCombinatorViewer();
		routeListCombinatorViewer.setCamera(camera, false);
		routeListCombinatorViewer.setBounds(180+blockWidth, blockHeight+20, blockWidth,blockHeight);
		add(routeListCombinatorViewer);
//		scalerViewer2 = new ScalerSegmentViewer();
//		scalerViewer2.setCamera(camera, false);
//		scalerViewer2.setBounds(180+blockWidth, blockHeight+20, blockWidth,blockHeight);
//		add(scalerViewer2);

		resultViewer = new ContourLayerViewer();
		resultViewer.setCamera(camera, false);
		resultViewer.setBounds(210+blockWidth*2, blockHeight+20, blockWidth,blockHeight);
		add(resultViewer);

		
		JSlider jSlider = new JSlider(-500, 7000, thickness);
		jSlider.setOrientation(JSlider.VERTICAL);
		jSlider.setBounds(0, 200, 30, blockHeight);
		jSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				thickness = jSlider.getValue();
				log.debug("thickness="+thickness);
			}
		});
		add(jSlider);

		new Thread(this).start();

	}
	

	private void selectModel(ContourLayerTestProducer testProducer) {
		thickness = testProducer.getThickness();
		doReduceLayer = testProducer.doReduceFirst();
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
		jFrame.getContentPane().add(new ContourLayerScalerDemo());
		jFrame.setVisible(true);
		jFrame.setExtendedState(jFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}


	
	@Override
	public void run() {
		
		int dd = -1;
		
		while(true) {
			if (dd == this.thickness && !dirty) {
//				log.debug("dd="+dd);
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
			
			ContourLayer newLayer = discLayerFillContext.discLayer;
			if (doReduceLayer) {
				newLayer = discLayerFillContext.reduceResolution(discLayerFillContext.discLayer, discSizeSq, 1);
			}

			ContourLayerOverlapCutter overlapCutter = new ContourLayerOverlapCutter();
			newLayer = overlapCutter.scale(newLayer, false);

			
			discLayerFillContext.reduceResolution = newLayer;
			
			
			ContourLayer contourLayer = new ContourLayer(100000, 100000);
			int sequence=0;
			for(Contour c : newLayer) {
//				contourLayer.add(c);
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
			
			
			ContourLayerScaler discLayerScaler = new ContourLayerScaler();
			
			discLayerScaler.scale(discLayerFillContext.reduceResolution, dd, false);
			discLayerFillContext.scalerViewableModel = new ScalerViewableModel(discLayerScaler.allMutableContours);
			
			discLayerFillContext.routeListScaledDownCombinator = discLayerScaler.routeListScaledDownCombinator;
			
			if (RUN_REPEATS) {
				
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
	//				newLayer = discLayerFillContext.reduceResolution(newLayer, discSizeSq, 1);
				}
				
				resultViewer.setModel(multiResultLayer);
			}
			
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

		
		ContourLayer contourLayer = scalarTests.applyScaling(designedLayer, thickness, doReduceLayer);
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

			designViewer.setModel(designedLayer);
			baseViewer.setModel(designedLayer);
			
		}
		if (discLayerFillContext.reduceResolution!=null) {
			ContourLayer duplicate = discLayerFillContext.reduceResolution.duplicate();
			this.onOffCutViewer.setModel(duplicate);
		}

		if (discLayerFillContext.scaled != null) {
			ContourLayerViewableModel viewableModel = new ContourLayerViewableModel(discLayerFillContext.scaled, discLayerFillContext.reduceResolution);
			inputAndScaledViewer.setModel(viewableModel);
		}

		if (discLayerFillContext.scalerViewableModel != null) {
			
//			scalerViewer2.setModel(discLayerFillContext.scalerViewableModel);
			scalerProjectionViewer.setModel(discLayerFillContext.scalerViewableModel);
		}

		if (discLayerFillContext.routeListScaledDownCombinator != null) {
			RouteListScaledDownCombinatorViewableModel model = new RouteListScaledDownCombinatorViewableModel(discLayerFillContext.routeListScaledDownCombinator);
			routeListCombinatorViewer.setModel(model);
			
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
		
		designViewer.setBounds(col0, 0, blockWidth, blockHeight);
		onOffCutViewer.setBounds(col1, 0, blockWidth,blockHeight);
		scalerProjectionViewer.setBounds(col2, 0, blockWidth, blockHeight);

		int row1 = blockHeight+inset;

		inputAndScaledViewer.setBounds(col0, row1, blockWidth, blockHeight);
		routeListCombinatorViewer.setBounds(col1, row1, blockWidth, blockHeight);
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
