package org.restlet.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Method implements Comparable<Method> {

	/** Map of registered methods. */
	private static final Map<String, Method> _methods = new ConcurrentHashMap<String, Method>();

	/**
	 * Pseudo-method use to match all methods.
	 */
	public static final Method ALL = new Method("*", "Pseudo-method use to match all methods.");

	private static final String BASE_HTTP = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html";

	private static final String BASE_WEBDAV = "http://www.webdav.org/specs/rfc2518.html";

	public static final Method CONNECT = new Method("CONNECT", "Used with a proxy that can dynamically switch to being a tunnel", BASE_HTTP + "#sec9.9", false, false);

	public static final Method COPY = new Method("COPY", "Creates a duplicate of the source resource, identified by the Request-URI, in the destination resource, identified by the URI in the Destination header", BASE_WEBDAV + "#METHOD_COPY", false, true);

	public static final Method DELETE = new Method("DELETE", "Requests that the origin server deletes the resource identified by the request URI", BASE_HTTP + "#sec9.7", false, true);

	public static final Method GET = new Method("GET", "Retrieves whatever information (in the form of an entity) that is identified by the request URI", BASE_HTTP + "#sec9.3", true, true);

	public static final Method HEAD = new Method("HEAD", "Identical to GET except that the server must not return a message body in the response", BASE_HTTP + "#sec9.4", true, true);

	public static final Method SEARCH = new Method("SEARCH", "Identical to Search except that the server must not return a message body in the response", BASE_HTTP + "#undefined", true, true);

	public static final Method LIST = new Method("LIST", "Identical to List except that the server must not return a message body in the response", BASE_HTTP + "#undefined", true, true);

	public static final Method EXECUTE = new Method("EXECUTE", "Identical to List except that the server must not return a message body in the response", BASE_HTTP + "#undefined", true, true);

	public static final Method LOCK = new Method("LOCK", "Used to take out a lock of any access type (WebDAV)", BASE_WEBDAV + "#METHOD_LOCK", true, false);

	public static final Method MKCOL = new Method("MKCOL", "Used to create a new collection (WebDAV)", BASE_WEBDAV + "#METHOD_MKCOL", false, true);

	public static final Method MOVE = new Method("MOVE", "Logical equivalent of a copy, followed by consistency maintenance processing, followed by a delete of the source (WebDAV)", BASE_WEBDAV + "#METHOD_MOVE", false, false);

	public static final Method OPTIONS = new Method("OPTIONS", "Requests for information about the communication options available on the request/response chain identified by the URI", BASE_HTTP + "#sec9.2", true, true);

	public static final Method POST = new Method("POST", "Requests that the origin server accepts the entity enclosed in the request as a new subordinate of the resource identified by the request URI", BASE_HTTP + "#sec9.5", false, false);

	public static final Method PROPFIND = new Method("PROPFIND", "Retrieves properties defined on the resource identified by the request URI", BASE_WEBDAV + "#METHOD_PROPFIND", true, true);

	public static final Method PROPPATCH = new Method("PROPPATCH", "Processes instructions specified in the request body to set and/or remove properties defined on the resource identified by the request URI", BASE_WEBDAV + "#METHOD_PROPPATCH", false, true);

	public static final Method PUT = new Method("PUT", "Requests that the enclosed entity be stored under the supplied request URI", BASE_HTTP + "#sec9.6", false, true);

	public static final Method TRACE = new Method("TRACE", "Used to invoke a remote, application-layer loop-back of the request message", BASE_HTTP + "#sec9.8", true, true);

	public static final Method UNLOCK = new Method("UNLOCK", "Removes the lock identified by the lock token from the request URI, and all other resources included in the lock", BASE_WEBDAV + "#METHOD_UNLOCK", true, false);

	public static void register(Method method) {
		String name = (method == null) ? null : method.getName().toLowerCase();
		if ((name != null) && !name.equals("")) {
			_methods.put(name, method);
		}
	}

	public static void sort(List<Method> methods) {
		Collections.sort(methods, new Comparator<Method>() {
			public int compare(Method m1, Method m2) {
				return m1.getName().compareTo(m2.getName());
			}
		});
	}

	public static Method valueOf(final String name) {
		Method result = null;

		if ((name != null) && !name.equals("")) {
			result = Method._methods.get(name.toLowerCase());
			if (result == null) {
				result = new Method(name);
			}
		}

		return result;
	}

	private final String description;

	private volatile boolean idempotent;

	private volatile String name;

	private final boolean replying;

	private final boolean safe;

	private volatile String uri;


	public Method(final String name) {
		this(name, null);
	}

	public Method(String name, String description) {
		this(name, description, null, false, false);
	}

	public Method(String name, String description, String uri) {
		this(name, description, uri, false, false);
	}

	public Method(String name, String description, String uri, boolean safe, boolean idempotent) {
		this(name, description, uri, safe, idempotent, true);
	}

	public Method(String name, String description, String uri, boolean safe, boolean idempotent, boolean replying) {
		this.name = name;
		this.description = description;
		this.uri = uri;
		this.safe = safe;
		this.idempotent = idempotent;
		this.replying = replying;
	}

	public int compareTo(Method o) {
		if (o != null) {
			return this.getName().compareTo(o.getName());
		}
		return 1;
	}

	@Override
	public boolean equals(final Object object) {
		return (object instanceof Method) && ((Method) object).getName().equals(getName());
	}

	public String getDescription() {
		return this.description;
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return this.uri;
	}

	@Override
	public int hashCode() {
		return (getName() == null) ? 0 : getName().hashCode();
	}

	public boolean isIdempotent() {
		return idempotent;
	}

	public boolean isReplying() {
		return replying;
	}

	public boolean isSafe() {
		return safe;
	}

	@Override
	public String toString() {
		return getName();
	}
}
