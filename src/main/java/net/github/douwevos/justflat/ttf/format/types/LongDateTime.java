package net.github.douwevos.justflat.ttf.format.types;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class LongDateTime {

	public final long value;
	
	public LongDateTime(final long value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(value);
		cal.add(Calendar.YEAR, -66);
		cal.add(Calendar.HOUR, 11);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		
		return ""+value+" :: "+ sdf.format(cal.getTime());
	}
}

