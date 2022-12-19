//package net.github.douwevos.justflat.contour.scaler;
//
//import net.github.douwevos.justflat.demo.ModelMouseEvent;
//import net.github.douwevos.justflat.demo.Selection;
//import net.github.douwevos.justflat.demo.ViewableModel;
//import net.github.douwevos.justflat.types.values.Bounds2D;
//
//public class DirectedLinesViewableModel implements ViewableModel {
//
//	public final DirectedLines directedLines;
//	
//	public DirectedLinesViewableModel(DirectedLines directedLines) {
//		this.directedLines = directedLines;
//	}
//	
//	
//	@Override
//	public Bounds2D bounds() {
//		return directedLines.bounds();
//	}
//	
//	@Override
//	public Selection<?> selectAt(ModelMouseEvent modelMouseEvent) {
//		return null;
//	}
//	
//}