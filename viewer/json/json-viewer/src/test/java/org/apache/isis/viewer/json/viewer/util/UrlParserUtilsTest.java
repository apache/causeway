package org.apache.isis.viewer.json.viewer.util;
import static org.junit.Assert.assertEquals;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.junit.Test;


public class UrlParserUtilsTest {

	@Test
	public void test() throws Exception {
	    JsonRepresentation link = JsonRepresentation.newMap();
		link.mapPut("href", "http://localhost/objects/OID:1");
		String oidFromHref = UrlParserUtils.oidFromLink(link);
		assertEquals("OID:1", oidFromHref);
	}

}
