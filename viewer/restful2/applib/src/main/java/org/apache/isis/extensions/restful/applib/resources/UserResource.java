package org.apache.isis.extensions.restful.applib.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

public interface UserResource {

	@GET
	@Produces( { "application/xhtml+xml", "text/html" })
	public String user();

}