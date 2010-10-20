package org.apache.isis.extensions.restful.applib.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

public interface HomePageResource {

	@GET
	@Produces( {"application/xhtml+xml", "text/html"} )
	public String resources();

}