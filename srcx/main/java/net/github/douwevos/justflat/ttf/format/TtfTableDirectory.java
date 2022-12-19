package net.github.douwevos.justflat.ttf.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

public class TtfTableDirectory {

	private ArrayList<TtfTableDirectoryItem> list = new ArrayList<TtfTableDirectoryItem>();
	
	public TtfTableDirectory(Collection<TtfTableDirectoryItem> items) {
		list.addAll(items);
	}
	
	
	public TtfTableDirectoryItem[] createOffsetSortedList() {
		TtfTableDirectoryItem result[] = new TtfTableDirectoryItem[list.size()];
		list.toArray(result);
		
		Arrays.sort(result, new Comparator<TtfTableDirectoryItem>() {
			public int compare(TtfTableDirectoryItem o1, TtfTableDirectoryItem o2) {
				final int off1 = o1.getOffset();
				final int off2 = o2.getOffset();
				return off1==off2 ? 0 : off1<off2 ? -1 : 1;
			}
		});
		return result;
	}
	
}

