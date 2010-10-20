package org.apache.isis.extensions.restful.viewer.util;

import java.util.ArrayList;
import java.util.List;

public final class ListUtils {
	
	private ListUtils(){}

	@SuppressWarnings("unchecked")
	public
	static <T> List<T> toList(final Object[] objects) {
	    final List<T> list = new ArrayList<T>();
	    for (final Object o : objects) {
	        list.add((T) o);
	    }
	    return list;
	}

}
