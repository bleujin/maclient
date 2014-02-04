package net.ion.framework.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {

	public static void closeQuietly(Closeable... clos) {
		for (Closeable clo : clos) {
			try {
				clo.close() ;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
