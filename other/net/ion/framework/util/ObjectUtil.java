package net.ion.framework.util;

public class ObjectUtil {

	public final static <T> T coalesce(T... objs) {
		for (T object : objs) {
			if (object != null)
				return object;
		}
		return null;
	}

	public static String toString(Object obj) {
		return obj != null ? obj.toString() : "";
	}
}
