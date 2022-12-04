package net.github.douwevos.justflat.ttf.format;

import java.io.IOException;
import java.util.ArrayList;

import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.ttf.format.types.FWord;
import net.github.douwevos.justflat.ttf.format.types.Int16;
import net.github.douwevos.justflat.ttf.format.types.Uint16;
import net.github.douwevos.justflat.ttf.reader.TtfInputStream;

public class TtfGlyphData {

	private static final Log log = Log.instance(false);

	public ArrayList<GlypDescription> glypDescriptions = new ArrayList<GlypDescription>();
	
	
	private final MaxProfile maxProfile;
//	private final TtfHead head;
	private final LocationTable locationTable;
	
	public TtfGlyphData(MaxProfile maxProfile, TtfHead head, LocationTable locationTable) {
		this.maxProfile = maxProfile;
//		this.head = head;
		this.locationTable = locationTable;
	}
	
	public void read(TtfInputStream in) throws IOException {
		log.indent();
		try {
			int numOfGlyphs = maxProfile.getNumGlyphs().getValue();
			for(int idx=0; idx<numOfGlyphs; idx++) {
				int offset = locationTable.getOffset(idx);
				int length = locationTable.getOffset(idx+1)-offset;
				
				log.log("glyf["+idx+"]=seekoffset:"+in.getSeekOffset()+", offset="+offset+", length="+length);
				in.setSeek(offset);
				log.log("glyf["+idx+"]=seekoffset:"+in.getSeekOffset()+", offset="+offset+", length="+length);
				
				if (length==0) {
					glypDescriptions.add(new GlypDescription());
				} else {
					glypDescriptions.add(readNextDescription(in, idx));
				}
	//			glypDescriptions.add(readNextDescription(in, idx));
			}
		} finally {
			log.dedent();
		}
	}

	
	
	
	
	
	
	
	private GlypDescription readNextDescription(TtfInputStream in, int glyphIndex) throws IOException {
		GlypDescription result = new GlypDescription();
		
		int offset = locationTable.getOffset(glyphIndex);
		int length = locationTable.getOffset(glyphIndex+1)-offset;
		if (length > 0) {
			in.setSeek(offset);
			result.read(in);
		}
		
//		int contourCnt = result.getNumberOfContors().getValue();
//		for(int idx=0; idx<contourCnt; idx++) {
//				
//		}
		return result;
	}




	public static class GlypDescription {
		private Int16 numberOfContors;
		private FWord xMin, yMin, xMax, yMax;
		private GlyphDefinition glyphDefinition = new GlyphDefinition();

		public Int16 getNumberOfContors() {
			return numberOfContors;
		}

		public void setNumberOfContors(Int16 numberOfContors) {
			this.numberOfContors = numberOfContors;
		}

		public FWord getXMax() {
			return xMax;
		}

		public void setXMax(FWord max) {
			xMax = max;
		}

		public FWord getXMin() {
			return xMin;
		}

		public void setXMin(FWord min) {
			xMin = min;
		}

		public FWord getYMax() {
			return yMax;
		}

		public void setYMax(FWord max) {
			yMax = max;
		}

		public FWord getYMin() {
			return yMin;
		}

		public void setYMin(FWord min) {
			yMin = min;
		}
		
		public GlyphDefinition getGlyphDefinition() {
			return glyphDefinition;
		}

		public void read(TtfInputStream in) throws IOException {
			log.indent();
			try {
				numberOfContors = in.readInt16();
				xMin = in.readFWord();
				yMin = in.readFWord();
				xMax = in.readFWord();
				yMax = in.readFWord();
				log.log("numberOfContors="+numberOfContors);
				if (numberOfContors.getValue()==0) {
					
				} else  if (numberOfContors.getValue()>=0) {
					glyphDefinition.readSimpleGlyphs(in, this);
				} else {
					glyphDefinition.readCompoundGlyphs(in, this);
				}
			} finally {
				log.dedent();
			}
		}

		public void dump(String pre) {
			log.log(pre+"GlyphDescription");
			log.log(pre+" - numberOfContors:      "+numberOfContors);
			try {
				log.log(pre+" - bounds:                 "+xMin.getValue()+", "+yMin.getValue()+", "+xMax.getValue()+", "+yMax.getValue());
				glyphDefinition.dump(pre+"    ");
			} catch(NullPointerException ignore) {
				
			}
			
		}
	}

	
	public static class GlyphDefinition {
		private ArrayList<GlyphDot> dotList = new ArrayList<GlyphDot>();
		private int endPoints[];
		private byte program[];

		private ArrayList<Contour> contourList = new ArrayList<Contour>();
		
		
		public ArrayList<GlyphDot> getDotList() {
			return dotList;
		}
		
		
		public ArrayList<Contour> getContourList() {
			return contourList;
		}
		
		public void readSimpleGlyphs(TtfInputStream in, GlypDescription glypDescription) throws IOException {
			int indent = log.indent();
			try {
				int contourCnt = glypDescription.numberOfContors.getValue();
				int maxPntIndex = 0;
				endPoints = new int[contourCnt];
				for(int idx=0; idx<contourCnt; idx++) {
					endPoints[idx] = in.readUint16().getValue();
//					log.log("endPoints["+idx+"]="+TtfUtil.toHex(endPoints[idx]).substring(4));
					
					maxPntIndex = endPoints[idx]<maxPntIndex ? maxPntIndex : endPoints[idx];
				}
				maxPntIndex++;
				
				int programLength = in.readUint16().getValue();
				log.log("programLength="+TtfUtil.toHex(programLength).substring(4));
				program = new byte[programLength];
				in.read(program);
				
				byte flags[] = new byte[maxPntIndex];
				int flgIdx = 0;
				byte lastFlag = 0;
				int lastFlagRep = 0;
				log.indent();
				while(flgIdx<flags.length) {
					if (lastFlagRep>0) {
						flags[flgIdx++] = lastFlag;
						lastFlagRep--;
					} else {
						int k = in.read();
//						if (k<0) {
//							break;
//						}
						lastFlag = (byte) k;
//						log.log("flgIdx["+flgIdx+"]="+TtfUtil.toHex(lastFlag).substring(6));
						if ((lastFlag & 0x8)!=0) {
							lastFlagRep = in.read()+1;
//							log.log("lastFlagRep="+TtfUtil.toHex(lastFlagRep).substring(6));
						} else {
							lastFlagRep = 1;
						}
					}
				}
				log.dedent();


				int xcoords[] = parseCoords(in, maxPntIndex, flags, 0x2, 0x10);
				int ycoords[] = parseCoords(in, maxPntIndex, flags, 0x4, 0x20);

				log.log("in.offset="+TtfUtil.toHex((int) in.getSeekOffset()));
				for(int idx=0; idx<maxPntIndex; idx++) {
					boolean isCurve = (flags[idx]&1)==0;
					dotList.add(new GlyphDot(xcoords[idx], ycoords[idx], isCurve));
					log.log("dot["+idx+"]="+dotList.get(idx));
				}
				
				
				int start = 0;
				for(int contourIdx=0; contourIdx<endPoints.length; contourIdx++) {
					int end = endPoints[contourIdx];
					Contour contour = new Contour();
					while(start<=end) {
						contour.dotList.add(dotList.get(start));
						start++;
					}
					contourList.add(contour);
				}
			} finally {
				log.indentAt(indent);
			}
		}

		private int[] parseCoords(TtfInputStream in, int maxPntIndex, byte[] flags, int shortMask, int sameOrPositiveMask) throws IOException {
			int coords[] = new int[maxPntIndex];
			log.log("coords.len="+coords.length+", offset="+Long.toHexString(in.getSeekOffset()));
			log.indent();
			int lastX = 0;
			for(int idx=0; idx<maxPntIndex; idx++) {
				if ((flags[idx] & shortMask) == 0) {
					if ((flags[idx] & sameOrPositiveMask) == 0) {
						int deltaX = in.readInt16().getValue();
//						log.log("deltaX="+TtfUtil.toHex(deltaX).substring(4));
						lastX += deltaX;
					}
				} else {
					int delta = in.read();
//					log.log("deltaX="+TtfUtil.toHex(delta).substring(6));
					if ((flags[idx] & sameOrPositiveMask) == 0) {
						delta = -delta;	
					}
					lastX += delta;
				}
//				log.log("xcoords["+idx+"="+lastX);
				coords[idx] = lastX;
			}
			log.dedent();
			return coords;
		}


		public void readCompoundGlyphs(TtfInputStream in, GlypDescription glypDescription) throws IOException {
			while(true) {
				
				Uint16 flags = in.readUint16();
				int flgs = flags.getValue();
				int arg1=0, arg2=0;
				
				if ((flgs & 1)==0) {
					arg1 = in.read();
					arg2 = in.read();
				} else {
					arg1 = in.readInt16().getValue();
					arg2 = in.readInt16().getValue();
				}
				
				
				
				
				float a = 1f;
				float b = 0f;
				float c = 0f;
				float d = 1f;
				if ((flgs & 0x8)!=0) {
					a = in.readF2Dot14().getValue();
					d = a;
				} else if ((flgs & 0x40)!=0) {
					a = in.readF2Dot14().getValue();
					d = in.readF2Dot14().getValue();
				} else if ((flgs & 0x80)!=0) {
					a = in.readF2Dot14().getValue();
					b = in.readF2Dot14().getValue();
					c = in.readF2Dot14().getValue();
					d = in.readF2Dot14().getValue();
				}
				
				
//				log.log("a="+a+", b="+b+", c="+c+", d="+d);
				if ((flgs & 0x20) == 0) {
					break;
				}
			}
		}
		
		
		public void dump(String pre) {
			log.log(pre+"GlyphDefinition");
			StringBuilder buf = new StringBuilder();
			int cnt=0;
			for (GlyphDot dot : dotList) {
				if (buf.length()>0) {
					buf.append(", ");
				}
				buf.append(dot.toString());
				if (cnt>5) {
					log.log(pre+buf);
					buf.setLength(0);
					cnt = 0;
				}
			}
			if (buf.length()>0) {
				log.log(pre+buf);
			}
		}
		
	}

	
	
	public static class Contour {
		public ArrayList<GlyphDot> dotList = new ArrayList<GlyphDot>();
		
	}
	
	public static class GlyphDot {
		public final int x;
		public final int y;
		public final boolean isCurve;

		public GlyphDot(int nwX, int nwY, boolean isCurve) {
			x = nwX;
			y = nwY;
			this.isCurve = isCurve;
		}
		
		public String toString() {
			return "GDot[x="+x+", y="+y+"]";
		}

	}

	public void dump(String pre) {
		log.log("");
		log.log(pre+"Glyph Data");
		
		for (GlypDescription glypDescription : glypDescriptions) {
			glypDescription.dump(pre+"   ");
		}
	}
}

