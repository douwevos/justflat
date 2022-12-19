/**
 * http://developer.apple.com/textfonts/TTRefMan/RM06/Chap6.html 
 * 
 */

package net.github.douwevos.justflat.ttf.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import net.github.douwevos.justflat.logging.Log;
import net.github.douwevos.justflat.ttf.format.LocationTable;
import net.github.douwevos.justflat.ttf.format.MaxProfile;
import net.github.douwevos.justflat.ttf.format.Ttf;
import net.github.douwevos.justflat.ttf.format.TtfCharacterMap;
import net.github.douwevos.justflat.ttf.format.TtfControlValuesTable;
import net.github.douwevos.justflat.ttf.format.TtfGlyphData;
import net.github.douwevos.justflat.ttf.format.TtfHead;
import net.github.douwevos.justflat.ttf.format.TtfHorizontalHeader;
import net.github.douwevos.justflat.ttf.format.TtfOffsetSubTable;
import net.github.douwevos.justflat.ttf.format.TtfTableDirectory;
import net.github.douwevos.justflat.ttf.format.TtfTableDirectoryItem;
import net.github.douwevos.justflat.ttf.format.TtfUtil;

public class TrueTypeFontParser {

	private static final Log log = Log.instance(false);
	
	public static void main(String[] args) {
		try {
			TrueTypeFontParser ttfParser = new TrueTypeFontParser();
			Ttf ttf = ttfParser.parse(new File("/opt/android-studio/jre/lib/fonts/DroidSansMono.ttf"));
			
			ttf.dump();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public Ttf parse(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		Ttf result = new Ttf();
		
		byte rawData[] = new byte[(int) file.length()];
		fis.read(rawData);
		
		TtfInputStream in = new TtfInputStream(rawData);
		TtfOffsetSubTable offSubTable = readOffsetSubTable(in);
		
		
		
		ArrayList<TtfTableDirectoryItem> tabDirList = new ArrayList<TtfTableDirectoryItem>();
		TtfTableDirectoryItem tableDirectory[] = new TtfTableDirectoryItem[offSubTable.getNumTables()];
		for(int idx=0; idx<tableDirectory.length; idx++) {
			tabDirList.add(readTableDirectory(in));
		}
		TtfTableDirectory ttfTableDirectory = new TtfTableDirectory(tabDirList);

		TtfTableDirectoryItem[] items = ttfTableDirectory.createOffsetSortedList();
		
		
		HashSet<TtfTableDirectoryItem> directory = new LinkedHashSet<TtfTableDirectoryItem>();
		for(int itemIdx=0; itemIdx<items.length; itemIdx++) {
//			log.log("seekOffset:{}", in.getSeekOffset());
			loadItem(result, in, items[itemIdx]);
			directory.add(items[itemIdx]);
			log.log(""+items[itemIdx]);
			
			TtfInputStream inputStream = items[itemIdx].createInputStream();
			byte[] t = new byte[256];
			int readCnt = inputStream.read(t);
			StringBuilder buf = new StringBuilder();
			for(int idx=0; idx<readCnt; idx++) {
				if ((idx%32)==0) {
					if (buf.length()>0) {
						log.log(buf.toString());
						buf.setLength(0);
					}
					buf.append(""+TtfUtil.toHex(idx)).append(":");
				}
				if ((idx%32)==16) {
					buf.append(" | ");
				}
				buf.append(" ").append(String.format("%02x", 0xff & t[idx]));
			}
			if (buf.length()>0) {
				log.log(buf.toString());
				buf.setLength(0);
			}
			
		}
		
		
		
		
		
		parse(result, directory);
		
		
		return result;
	}


	private void parse(Ttf ttf, HashSet<TtfTableDirectoryItem> directory) throws IOException {
		while(true) {
			int oldSize = directory.size();
			Iterator<TtfTableDirectoryItem> iter = directory.iterator();
			while(iter.hasNext()) {
				TtfTableDirectoryItem item = iter.next();
				int tag = item.getTag();
				log.log("tag:{}", tag);
				switch(tag) {
					case TtfTableDirectoryItem.TAG_HEAD : {
						TtfHead head = new TtfHead();
						head.read(item.createInputStream());
						ttf.setHead(head);
						head.dump("' ");
						iter.remove();
					} break;
					case TtfTableDirectoryItem.TAG_HHEA : {
						TtfHorizontalHeader t = new TtfHorizontalHeader();
						t.read(item.createInputStream());
						ttf.setHorizontalHeader(t);
						iter.remove();
					} break;
					case TtfTableDirectoryItem.TAG_CVT : {
						TtfControlValuesTable t = new TtfControlValuesTable();
						t.read(item.createInputStream());
						ttf.setControlValuesTable(t);
						iter.remove();
					} break;
					case TtfTableDirectoryItem.TAG_MAXP : {
						MaxProfile t = new MaxProfile();
						t.read(item.createInputStream());
						ttf.setMaxProfile(t);
						iter.remove();
					} break;
					case TtfTableDirectoryItem.TAG_CMAP : {
						TtfCharacterMap t = new TtfCharacterMap();
						t.read(item.createInputStream());
						ttf.setCharacterMap(t);
						iter.remove();
					} break;
					case TtfTableDirectoryItem.TAG_LOCA : {
						TtfHead head = ttf.getHead();
						MaxProfile maxProfile = ttf.getMaxProfile();
						if (head==null || maxProfile==null) {
							break;
						}
						LocationTable locationTable = new LocationTable(head, maxProfile);
						locationTable.read(item.createInputStream());
						locationTable.dump("# ");
						ttf.setLocationTable(locationTable);
						iter.remove();
					} break;
					case TtfTableDirectoryItem.TAG_GLYF : {
						TtfHead head = ttf.getHead();
						MaxProfile maxProfile = ttf.getMaxProfile();
						
						LocationTable locationTable = ttf.getLocationTable();
						if (head==null || maxProfile==null || locationTable==null) {
							break;
						}
						TtfGlyphData glyphData = new TtfGlyphData(maxProfile, head, locationTable);
						glyphData.read(item.createInputStream());
						ttf.setGlyphData(glyphData);
						iter.remove();
					} break;
				}
			}
			if (oldSize == directory.size()) {
				break;
			}
		}
		
	}


	private void loadItem(Ttf ttf, TtfInputStream in, TtfTableDirectoryItem item) throws IOException {
		int seekOffset = in.getSeekOffset();
		int allignedSeekOff = seekOffset+3;
		allignedSeekOff = allignedSeekOff - allignedSeekOff%4;
//		log.log("allignedSeekOff:{}", allignedSeekOff);
		in.setSeek(allignedSeekOff);
		
		if (allignedSeekOff != item.getOffset()) {
			throw new IOException("Invalid format. The current seekoffset does not comply with the entries offset. table-directory-item:"+item);
		}
		
		byte block[] = new byte[item.getLength()];
		in.read(block);
		
		item.setData(block);
		
		int tag = item.getTag();
		
		if (tag != TtfTableDirectoryItem.TAG_HEAD) {
			int chkSum = item.calculateCheckSum();
			if (chkSum!=item.getCheckSum()) {
				throw new IOException("Invalid checksum for table-entry. calculated="+chkSum+", stored in director:"+item.getCheckSum());
			}
		}
		
	}


	private TtfOffsetSubTable readOffsetSubTable(TtfInputStream in) throws IOException {
		int scalerType = in.readInt();
		short numTables = in.readShort();
		short searchRange = in.readShort();
		short entrySelector = in.readShort();
		short rangeShift = in.readShort();
		return new TtfOffsetSubTable(scalerType, numTables, searchRange, entrySelector, rangeShift);
	}

	private TtfTableDirectoryItem readTableDirectory(TtfInputStream in) throws IOException {
		int tag = in.readInt();
		int checkSum = in.readInt();
		int offset = in.readInt();
		int length = in.readInt();
		return new TtfTableDirectoryItem(tag, checkSum, offset, length);
	}
}

