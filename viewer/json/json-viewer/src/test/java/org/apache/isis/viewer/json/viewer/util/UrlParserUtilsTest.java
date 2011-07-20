package org.apache.isis.viewer.json.viewer.util;
import static org.junit.Assert.assertEquals;

import org.apache.isis.viewer.json.viewer.representations.Representation;
import org.junit.Test;


public class UrlParserUtilsTest {

	@Test
	public void test() throws Exception {
		Representation link = new Representation();
		link.put("href", "http://localhost/objects/OID:1");
		String oidFromHref = UrlParserUtils.oidFromHref(link);
		assertEquals("OID:1", oidFromHref);
	}

}
