package net.github.douwevos.justflat.util;

import java.util.Objects;
import java.util.function.Predicate;

public class NoRepeats<T> implements Predicate<T> {

	
	public static <A> NoRepeats<A> filter() {
		return new NoRepeats<>();
	}
	
	T last;
	
	@Override
	public boolean test(T t) {
		boolean isSame = Objects.equals(t, last);
		last = t;
		return !isSame;
	}
	
}