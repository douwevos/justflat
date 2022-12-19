package net.github.douwevos.justflat.ttf.format;

import java.io.IOException;
import java.util.ArrayList;

import net.github.douwevos.justflat.ttf.format.types.FWord;
import net.github.douwevos.justflat.ttf.reader.TtfInputStream;

public class TtfControlValuesTable {

	private ArrayList<FWord> table = new ArrayList<FWord>();
	
	
	public void read(TtfInputStream in) throws IOException {
		while(in.available()>0) {
			table.add(in.readFWord());
		}
	}
	
}

