package net.ion.radon.aclient.util;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import net.ion.radon.aclient.AsyncHttpProvider;
import net.ion.radon.aclient.ByteArrayPart;
import net.ion.radon.aclient.Cookie;
import net.ion.radon.aclient.FilePart;
import net.ion.radon.aclient.FluentStringsMap;
import net.ion.radon.aclient.HttpResponseBodyPart;
import net.ion.radon.aclient.Part;
import net.ion.radon.aclient.StringPart;
import net.ion.radon.aclient.multipart.ByteArrayPartSource;
import net.ion.radon.aclient.multipart.MultipartRequestEntity;
import net.ion.radon.aclient.multipart.PartSource;

public class AsyncHttpProviderUtils {
	private final static byte[] NO_BYTES = new byte[0];

	public final static String DEFAULT_CHARSET = "ISO-8859-1";

	private final static String BODY_NOT_COMPUTED = "Response's body hasn't been computed by your AsyncHandler.";

	protected final static ThreadLocal<SimpleDateFormat[]> simpleDateFormat = new ThreadLocal<SimpleDateFormat[]>() {
		protected SimpleDateFormat[] initialValue() {

			return new SimpleDateFormat[] {
					new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US), // ASCTIME
					new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US), // RFC1036
					new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US), new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US), new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US), new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss Z", Locale.US),
					new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US) // RFC1123

			};
		}
	};

	public final static SimpleDateFormat[] get() {
		return simpleDateFormat.get();
	}

	// space ' '
	static final byte SP = 32;

	// tab ' '
	static final byte HT = 9;

	/**
	 * Carriage return
	 */
	static final byte CR = 13;

	/**
	 * Equals '='
	 */
	static final byte EQUALS = 61;

	static final byte LF = 10;

	/**
	 * carriage return line feed
	 */
	static final byte[] CRLF = new byte[] { CR, LF };

	/**
	 * Colon ':'
	 */
	static final byte COLON = 58;

	/**
	 * Semicolon ';'
	 */
	static final byte SEMICOLON = 59;

	/**
	 * comma ','
	 */
	static final byte COMMA = 44;

	static final byte DOUBLE_QUOTE = '"';

	static final String PATH = "Path";

	static final String EXPIRES = "Expires";

	static final String MAX_AGE = "Max-Age";

	static final String DOMAIN = "Domain";

	static final String SECURE = "Secure";

	static final String HTTPONLY = "HTTPOnly";

	static final String COMMENT = "Comment";

	static final String COMMENTURL = "CommentURL";

	static final String DISCARD = "Discard";

	static final String PORT = "Port";

	static final String VERSION = "Version";

	public final static URI createUri(String u) {
		URI uri = URI.create(u);
		final String scheme = uri.getScheme();
		if (scheme == null || !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("ws") && !scheme.equalsIgnoreCase("wss")) {
			throw new IllegalArgumentException("The URI scheme, of the URI " + u + ", must be equal (ignoring case) to 'http', 'https', 'ws', or 'wss'");
		}

		String path = uri.getPath();
		if (path == null) {
			throw new IllegalArgumentException("The URI path, of the URI " + uri + ", must be non-null");
		} else if (path.length() > 0 && path.charAt(0) != '/') {
			throw new IllegalArgumentException("The URI path, of the URI " + uri + ". must start with a '/'");
		} else if (path.length() == 0) {
			return URI.create(u + "/");
		}

		return uri;
	}

	public static String getBaseUrl(String url) {
		return getBaseUrl(createUri(url));
	}

	public final static String getBaseUrl(URI uri) {
		String url = uri.getScheme() + "://" + uri.getAuthority();
		int port = uri.getPort();
		if (port == -1) {
			port = getPort(uri);
			url += ":" + port;
		}
		return url;
	}

	public final static String getAuthority(URI uri) {
		String url = uri.getAuthority();
		int port = uri.getPort();
		if (port == -1) {
			port = getPort(uri);
			url += ":" + port;
		}
		return url;
	}

	public final static String contentToString(List<HttpResponseBodyPart> bodyParts, String charset) throws UnsupportedEncodingException {
		return new String(contentToBytes(bodyParts), charset);
	}

	public final static byte[] contentToBytes(List<HttpResponseBodyPart> bodyParts) throws UnsupportedEncodingException {
		final int partCount = bodyParts.size();
		if (partCount == 0) {
			return NO_BYTES;
		}
		if (partCount == 1) {
			return bodyParts.get(0).getBodyPartBytes();
		}
		int size = 0;
		List<byte[]> chunks = new ArrayList<byte[]>(partCount);
		for (HttpResponseBodyPart part : bodyParts) {
			byte[] chunk = part.getBodyPartBytes();
			size += chunk.length;
			chunks.add(chunk);
		}
		byte[] bytes = new byte[size];
		int offset = 0;
		for (byte[] chunk : chunks) {
			System.arraycopy(chunk, 0, bytes, offset, chunk.length);
			offset += chunk.length;
		}
		return bytes;
	}

	public final static byte[] contentToBytes(List<HttpResponseBodyPart> bodyParts, int maxLen) throws UnsupportedEncodingException {
		final int partCount = bodyParts.size();
		if (partCount == 0) {
			return NO_BYTES;
		}
		if (partCount == 1) {
			byte[] chunk = bodyParts.get(0).getBodyPartBytes();
			if (chunk.length <= maxLen) {
				return chunk;
			}
			byte[] result = new byte[maxLen];
			System.arraycopy(chunk, 0, result, 0, maxLen);
			return result;
		}
		int size = 0;
		byte[] result = new byte[maxLen];
		for (HttpResponseBodyPart part : bodyParts) {
			byte[] chunk = part.getBodyPartBytes();
			int amount = Math.min(maxLen - size, chunk.length);
			System.arraycopy(chunk, 0, result, size, amount);
			size += amount;
			if (size == maxLen) {
				return result;
			}
		}
		if (size < maxLen) {
			byte[] old = result;
			result = new byte[old.length];
			System.arraycopy(old, 0, result, 0, old.length);
		}
		return result;
	}

	public final static InputStream contentAsStream(List<HttpResponseBodyPart> bodyParts) {
		switch (bodyParts.size()) {
		case 0:
			return new ByteArrayInputStream(NO_BYTES);
		case 1:
			return bodyParts.get(0).readBodyPartBytes();
		}
		Vector<InputStream> streams = new Vector<InputStream>(bodyParts.size());
		for (HttpResponseBodyPart part : bodyParts) {
			streams.add(part.readBodyPartBytes());
		}
		return new SequenceInputStream(streams.elements());
	}

	public final static String getHost(URI uri) {
		String host = uri.getHost();
		if (host == null) {
			host = uri.getAuthority();
		}
		return host;
	}

	public final static URI getRedirectUri(URI uri, String location) {
		if (location == null)
			throw new IllegalArgumentException("URI " + uri + " was redirected to null location");
		URI newUri = uri.resolve(location);

		String scheme = newUri.getScheme();

		if (scheme == null || !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https") && !scheme.equals("ws") && !scheme.equals("wss")) {
			throw new IllegalArgumentException("The URI scheme, of the URI " + newUri + ", must be equal (ignoring case) to 'ws, 'wss', 'http', or 'https'");
		}

		return newUri;
	}

	public final static int getPort(URI uri) {
		int port = uri.getPort();
		if (port == -1)
			port = uri.getScheme().equals("http") ? 80 : 443;
		return port;
	}

	public final static MultipartRequestEntity createMultipartRequestEntity(List<Part> params, FluentStringsMap methodParams) throws FileNotFoundException {
		net.ion.radon.aclient.multipart.Part[] parts = new net.ion.radon.aclient.multipart.Part[params.size()];
		int i = 0;

		for (Part part : params) {
			if (part instanceof net.ion.radon.aclient.multipart.Part) {
				parts[i] = (net.ion.radon.aclient.multipart.Part) part;
			} else if (part instanceof StringPart) {
				parts[i] = new net.ion.radon.aclient.multipart.StringPart(part.getName(), ((StringPart) part).getValue(), ((StringPart) part).getCharset());
			} else if (part instanceof FilePart) {
				parts[i] = new net.ion.radon.aclient.multipart.FilePart(part.getName(), ((FilePart) part).getFile(), ((FilePart) part).getMimeType(), ((FilePart) part).getCharSet());

			} else if (part instanceof ByteArrayPart) {
				PartSource source = new ByteArrayPartSource(((ByteArrayPart) part).getFileName(), ((ByteArrayPart) part).getData());
				parts[i] = new net.ion.radon.aclient.multipart.FilePart(part.getName(), source, ((ByteArrayPart) part).getMimeType(), ((ByteArrayPart) part).getCharSet());

			} else if (part == null) {
				throw new NullPointerException("Part cannot be null");
			} else {
				throw new IllegalArgumentException(String.format("Unsupported part type for multipart parameter %s", part.getName()));
			}
			++i;
		}
		return new MultipartRequestEntity(parts, methodParams);
	}

	public final static byte[] readFully(InputStream in, int[] lengthWrapper) throws IOException {
		// just in case available() returns bogus (or -1), allocate non-trivial chunk
		byte[] b = new byte[Math.max(512, in.available())];
		int offset = 0;
		while (true) {
			int left = b.length - offset;
			int count = in.read(b, offset, left);
			if (count < 0) { // EOF
				break;
			}
			offset += count;
			if (count == left) { // full buffer, need to expand
				b = doubleUp(b);
			}
		}
		// wish Java had Tuple return type...
		lengthWrapper[0] = offset;
		return b;
	}

	private static byte[] doubleUp(byte[] b) {
		int len = b.length;
		byte[] b2 = new byte[len + len];
		System.arraycopy(b, 0, b2, 0, len);
		return b2;
	}

	public static String encodeCookies(Collection<Cookie> cookies) {
		StringBuilder sb = new StringBuilder();

		for (Cookie cookie : cookies) {
			if (cookie.getVersion() >= 1) {
				add(sb, '$' + VERSION, 1);
			}

			add(sb, cookie.getName(), cookie.getValue());

			if (cookie.getPath() != null) {
				add(sb, '$' + PATH, cookie.getPath());
			}

			if (cookie.getDomain() != null) {
				add(sb, '$' + DOMAIN, cookie.getDomain());
			}

			if (cookie.getVersion() >= 1) {
				if (!cookie.getPorts().isEmpty()) {
					sb.append('$');
					sb.append(PORT);
					sb.append((char) EQUALS);
					sb.append((char) DOUBLE_QUOTE);
					for (int port : cookie.getPorts()) {
						sb.append(port);
						sb.append((char) COMMA);
					}
					sb.setCharAt(sb.length() - 1, (char) DOUBLE_QUOTE);
					sb.append((char) SEMICOLON);
				}
			}
		}

		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	private static void add(StringBuilder sb, String name, String val) {
		if (val == null) {
			addQuoted(sb, name, "");
			return;
		}

		for (int i = 0; i < val.length(); i++) {
			char c = val.charAt(i);
			switch (c) {
			case '\t':
			case ' ':
			case '"':
			case '(':
			case ')':
			case ',':
			case '/':
			case ':':
			case ';':
			case '<':
			case '=':
			case '>':
			case '?':
			case '@':
			case '[':
			case '\\':
			case ']':
			case '{':
			case '}':
				addQuoted(sb, name, val);
				return;
			}
		}

		addUnquoted(sb, name, val);
	}

	private static void addUnquoted(StringBuilder sb, String name, String val) {
		sb.append(name);
		sb.append((char) EQUALS);
		sb.append(val);
		sb.append((char) SEMICOLON);
	}

	private static void addQuoted(StringBuilder sb, String name, String val) {
		if (val == null) {
			val = "";
		}

		sb.append(name);
		sb.append((char) EQUALS);
		sb.append((char) DOUBLE_QUOTE);
		sb.append(val.replace("\\", "\\\\").replace("\"", "\\\""));
		sb.append((char) DOUBLE_QUOTE);
		sb.append((char) SEMICOLON);
	}

	private static void add(StringBuilder sb, String name, int val) {
		sb.append(name);
		sb.append((char) EQUALS);
		sb.append(val);
		sb.append((char) SEMICOLON);
	}

	public static String constructUserAgent(Class<? extends AsyncHttpProvider> httpProvider) {
		StringBuffer b = new StringBuffer("AsyncHttpClient/1.0").append(" ").append("(").append(httpProvider.getSimpleName()).append(" - ").append(System.getProperty("os.name")).append(" - ").append(System.getProperty("os.version")).append(" - ").append(System.getProperty("java.version")).append(
				" - ").append(Runtime.getRuntime().availableProcessors()).append(" core(s))");
		return b.toString();
	}

	public static String parseCharset(String contentType) {
		for (String part : contentType.split(";")) {
			if (part.trim().startsWith("charset=")) {
				String[] val = part.split("=");
				if (val.length > 1) {
					String charset = val[1].trim();
					// Quite a lot of sites have charset="CHARSET",
					// e.g. charset="utf-8". Note the quotes. This is
					// not correct, but client should be able to handle
					// it (all browsers do, Apache HTTP Client and Grizzly
					// strip it by default)
					// This is a poor man's trim("\"").trim("'")
					return charset.replaceAll("\"", "").replaceAll("'", "");
				}
			}
		}
		return null;
	}

	public static Cookie parseCookie(String value) {
		String[] fields = value.split(";\\s*");
		String[] cookie = fields[0].split("=");
		String cookieName = cookie[0];
		String cookieValue = (cookie.length == 1) ? null : cookie[1];

		int maxAge = -1;
		String path = null;
		String domain = null;
		boolean secure = false;

		boolean maxAgeSet = false;
		boolean expiresSet = false;

		for (int j = 1; j < fields.length; j++) {
			if ("secure".equalsIgnoreCase(fields[j])) {
				secure = true;
			} else if (fields[j].indexOf('=') > 0) {
				String[] f = fields[j].split("=");
				if (f.length == 1)
					continue; // Add protection against null field values

				// favor 'max-age' field over 'expires'
				if (!maxAgeSet && "max-age".equalsIgnoreCase(f[0])) {
					try {
						maxAge = Math.max(Integer.valueOf(removeQuote(f[1])), 0);
					} catch (NumberFormatException e1) {
						// ignore failure to parse -> treat as session cookie
						// invalidate a previously parsed expires-field
						maxAge = -1;
					}
					maxAgeSet = true;
				} else if (!maxAgeSet && !expiresSet && "expires".equalsIgnoreCase(f[0])) {
					try {
						maxAge = Math.max(convertExpireField(f[1]), 0);
					} catch (Exception e) {
						// original behavior, is this correct at all (expires field with max-age semantics)?
						try {
							maxAge = Math.max(Integer.valueOf(f[1]), 0);
						} catch (NumberFormatException e1) {
							// ignore failure to parse -> treat as session cookie
						}
					}
					expiresSet = true;
				} else if ("domain".equalsIgnoreCase(f[0])) {
					domain = f[1];
				} else if ("path".equalsIgnoreCase(f[0])) {
					path = f[1];
				}
			}
		}

		return new Cookie(domain, cookieName, cookieValue, path, maxAge, secure);
	}

	private static int convertExpireField(String timestring) throws Exception {
		Exception exception = null;
		for (SimpleDateFormat sdf : simpleDateFormat.get()) {
			try {
				long expire = sdf.parse(removeQuote(timestring.trim())).getTime();
				return (int) ((expire - System.currentTimeMillis()) / 1000);
			} catch (ParseException e) {
				exception = e;
			} catch (NumberFormatException e) {
				exception = e;
			}
		}

		throw exception;
	}

	private final static String removeQuote(String s) {
		if (s.startsWith("\"")) {
			s = s.substring(1);
		}

		if (s.endsWith("\"")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	public static void checkBodyParts(int statusCode, Collection<HttpResponseBodyPart> bodyParts) {
		if (bodyParts == null || bodyParts.size() == 0) {

			// We allow empty body on 204
			if (statusCode == 204)
				return;

			throw new IllegalStateException(BODY_NOT_COMPUTED);
		}
	}
}
