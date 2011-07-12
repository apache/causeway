package org.apache.isis.viewer.json.viewer.util;
import static org.junit.Assert.assertEquals;

import org.apache.isis.viewer.json.viewer.representations.Representation;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;


public class ObjectMapperUtilsTest {

	ObjectMapper objectMapper = new ObjectMapper();
	
	@Test
	public void test() throws Exception {
		Representation link = new Representation();
		link.put("href", "http://localhost:8080/objects/OID:1");
		String writeValueAsString = objectMapper.writeValueAsString(link);
		Representation readValue = objectMapper.readValue(writeValueAsString, Representation.class);
		
		System.out.println(writeValueAsString);
		// {"href":"http://localhost:8080/objects/OID:4"}
		// {"href":"http://localhost:8080/objects/OID:4"}
		// {"href":"http://localhost:8080/objects/OID:1"}
		assertEquals(link.get("href"), readValue.get("href"));
		assertEquals(link, readValue);
	}

}
