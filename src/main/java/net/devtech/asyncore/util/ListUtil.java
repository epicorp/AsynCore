package net.devtech.asyncore.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * I'm lazy, so here it is
 */
public class ListUtil {
	public static <A, B> List<B> mapToList(Collection<A> collection, Function<A, B> function) {
		List<B> newList = new ArrayList<>(collection.size());
		for (A a : collection) {
			newList.add(function.apply(a));
		}
		return newList;
	}
}
