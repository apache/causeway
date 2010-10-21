package org.apache.isis.extensions.restful.applib;

import static org.apache.isis.extensions.restful.applib.StringUtils.asString;
import static org.apache.isis.extensions.restful.applib.UrlConnectionUtils.createGetConnection;
import static org.apache.isis.extensions.restful.applib.UrlConnectionUtils.createPostConnection;
import static org.apache.isis.extensions.restful.applib.UrlConnectionUtils.readDocFromConnectionInputStream;
import static org.apache.isis.extensions.restful.applib.UrlConnectionUtils.writeMapToConnectionOutputStream;
import static org.apache.isis.extensions.restful.applib.UrlEncodeUtils.urlEncode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.Map;

import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;

public abstract class AbstractRestfulClient {
	
	private static Logger LOG = Logger.getLogger(AbstractRestfulClient.class); 

	static final String MIME_TYPE = "application/xhtml+xml";

	private final String hostUri;
	public String getHostUri() {
		return hostUri;
	}

	public AbstractRestfulClient(String hostUri) {
		this.hostUri = hostUri;
	}
	
	public Document get(String uri) throws RestfulClientException {
		if (LOG.isInfoEnabled()) {
			LOG.info("getting from '" + uri + "'");
		}
		try {
			HttpURLConnection connection = createGetConnection(uri);
			Document document = readDocFromConnectionInputStream(connection);
			if (LOG.isTraceEnabled()) {
				LOG.trace(document.toXML());
			}
			return document;
		} catch (ProtocolException e) {
			throw new RestfulClientException(e);
		} catch (IOException e) {
			throw new RestfulClientException(e);
		} catch (ValidityException e) {
			throw new RestfulClientException(e);
		} catch (ParsingException e) {
			throw new RestfulClientException(e);
		}
	}

	public Document post(String uri, String... paramArgs) {
		return post(uri, StringUtils.asMap(paramArgs));
	}

	private Document post(String uri, Map<String,String> formArgumentsByParameter) {
		if (LOG.isInfoEnabled()) {
			LOG.info("posting form arguments to '" + uri + "'");
			LOG.info(asString(formArgumentsByParameter));
		}
		try {
			Map<String, String> encodedMap = urlEncode(formArgumentsByParameter);
			if (LOG.isTraceEnabled()) {
				LOG.trace(asString(encodedMap));
			}
			HttpURLConnection connection = createPostConnection(uri);
			writeMapToConnectionOutputStream(encodedMap, connection);
			return readDocFromConnectionInputStream(connection);
		} catch (IOException e) {
			throw new RestfulClientException(e);
		} catch (ValidityException e) {
			throw new RestfulClientException(e);
		} catch (ParsingException e) {
			throw new RestfulClientException(e);
		}
	}

	
	////////////////////////////////////////////////////////////////////////
	// Helpers: string
	////////////////////////////////////////////////////////////////////////
	
	protected static String combine(String... pathParts ) {
		StringBuilder buf = new StringBuilder();
		for (String part : pathParts) {
			buf.append(part);
		}
		return buf.toString();
	}

}
