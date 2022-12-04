package net.github.douwevos.justflat.contour.testui.examples;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import net.github.douwevos.justflat.contour.ContourComparator;
import net.github.douwevos.justflat.contour.ContourLayer;
import net.github.douwevos.justflat.contour.ContourLayerOverlapCutter;
import net.github.douwevos.justflat.contour.testui.ContourLayerTestProducer;
import net.github.douwevos.justflat.contour.testui.DiscLayerFillContext;

public class ScalarTests {

	Map<String, ScalarTest> testMap = new TreeMap<>();

	
	public ScalarTests() {
//		add(new TestModelOne());
//		add(new TestModelOne2());
//		add(new Triangle());
//		add(new Triangle2());
//		add(new TDBird());
//		add(new TDBird2());
//		add(new OLetter());
		add(new FullLetterR());
//		add(new AlmostStraight());
		runTests();
	}
	
	private void add(ContourLayerTestProducer producer) {
		ScalarTest scalarTest = new ScalarTest(producer);
		testMap.put(scalarTest.name(), scalarTest);
	}

	public ScalarTest get(String name) {
		return testMap.get(name);
	}


	public Stream<String> streamNames() {
		return testMap.keySet().stream();
	}

	private void runTests() {
		
		for(ScalarTest scalarTest : testMap.values()) {
			ContourLayer sourceLayer = scalarTest.producer.produceSourceLayer();
			int thickness = scalarTest.producer.getThickness();
			boolean doReduceFirst = scalarTest.producer.doReduceFirst();
			ContourLayer scaledLayer = applyScaling(sourceLayer, thickness, doReduceFirst);
			
			ContourLayer expectedLayer = scalarTest.producer.produceResultLayer();
			ContourComparator contourComparator = new ContourComparator();
			boolean same = contourComparator.equal(scaledLayer, expectedLayer);
			scalarTest.testOk = same;
			
		}
	}

	public ContourLayer applyScaling(ContourLayer input, int thickness, boolean doReduceFirst) {
		ContourLayer duplicate = input.duplicate();
		DiscLayerFillContext discLayerFillContext = new DiscLayerFillContext(duplicate);
		int discSize = 800;
		int discSizeSq = discSize*discSize;
		ContourLayer newLayer = discLayerFillContext.discLayer;
		if (doReduceFirst) {
			newLayer = discLayerFillContext.reduceResolution(discLayerFillContext.discLayer, discSizeSq, 1);
		}
		ContourLayerOverlapCutter overlapCutter = new ContourLayerOverlapCutter();
		newLayer = overlapCutter.scale(newLayer, false);
		ContourLayer scaledLayer = discLayerFillContext.scale(newLayer, thickness, false);
		return scaledLayer;
	}


	public static class ScalarTest {
		
		public final ContourLayerTestProducer producer;
		public boolean testOk;
		
		public ScalarTest(ContourLayerTestProducer producer) {
			this.producer = producer;
		}
		
		public String name() {
			return producer.name();
		}
	}





}
