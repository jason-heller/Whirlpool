package mapper.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
	public static InputStream getInputStream(Class<?> c, String path) {
		return c.getResourceAsStream("/" + path);
	}

	public static InputStream getInputStream(String path) {
		return getInputStream(Class.class, path);
	}

	public static BufferedReader getReader(Class<?> c, String path) {
		try {
			final InputStreamReader isr = new InputStreamReader(getInputStream(c, path));
			final BufferedReader reader = new BufferedReader(isr);
			return reader;
		} catch (final Exception e) {
			System.err.println("Failed to reader for " + path);
			throw e;
		}
	}

	public static BufferedReader getReader(String path) throws Exception {
		return getReader(Class.class, path);
	}
}
