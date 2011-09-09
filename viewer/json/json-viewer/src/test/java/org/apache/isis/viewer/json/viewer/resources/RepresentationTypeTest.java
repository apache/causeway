package org.apache.isis.viewer.json.viewer.resources;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.viewer.json.viewer.resources.RepresentationType;
import org.junit.Test;


public class RepresentationTypeTest {

	@Test
	public void getName() throws Exception {
	    assertThat(RepresentationType.DOMAIN_ACTION.getName(), is("domainAction"));
	    assertThat(RepresentationType.LIST.getName(), is("list"));
	}


}
