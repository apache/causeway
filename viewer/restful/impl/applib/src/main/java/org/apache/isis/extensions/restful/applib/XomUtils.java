package org.apache.isis.extensions.restful.applib;

import java.io.IOException;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Serializer;

public final class XomUtils {

	public static String getAttributeValueElseException(Document doc, String xpath, String msg) {
		Attribute attribute = getAttributeElseException(doc, xpath, msg);
		return attribute.getValue();
	}

	private static Attribute getAttributeElseException(Document document, String xpath, String msg) {
		Nodes query = document.getRootElement().query(xpath);
		if (query.size() != 1) {
			throw new IllegalArgumentException(msg);
		}
		Node node = query.get(0);
		if (!(node instanceof Attribute)) {
			throw new IllegalArgumentException(msg);
		}
		return (Attribute) node;
	}


	public static void prettyPrint(Document doc) throws IOException {
		Serializer serializer = new Serializer(System.out, Constants.URL_ENCODING_CHAR_SET);
		serializer.setIndent(4);
		serializer.setMaxLength(64);
		serializer.setPreserveBaseURI(true);
		serializer.write(doc);
		serializer.flush();
	}

	private XomUtils() {}

}
