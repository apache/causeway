package org.apache.isis.extensions.restful.applib.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public interface ServicesResource {

	@GET
	@Produces( {"application/xhtml+xml", "text/html"} )
	@Path("/")
	public String services();

}