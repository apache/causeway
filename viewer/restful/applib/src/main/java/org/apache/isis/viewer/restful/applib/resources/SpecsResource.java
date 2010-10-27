package org.apache.isis.viewer.restful.applib.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

public interface SpecsResource {

	@GET
	@Path("/")
	@Produces( { "application/xhtml+xml", "text/html" })
	public abstract String specs();

	@GET
	@Path("/{specFullName}")
	@Produces( { "application/xhtml+xml", "text/html" })
	public abstract String spec(
			@PathParam("specFullName") final String specFullName);

	@GET
	@Path("/{specFullName}/facet/{facetType}")
	@Produces( { "application/xhtml+xml", "text/html" })
	public abstract String specFacet(
			@PathParam("specFullName") final String specFullName,
			@PathParam("facetType") final String facetTypeName);

	@GET
	@Path("/{specFullName}/property/{propertyName}")
	@Produces( { "application/xhtml+xml", "text/html" })
	public abstract String specProperty(
			@PathParam("specFullName") final String specFullName,
			@PathParam("propertyName") final String propertyName);

	@GET
	@Path("/{specFullName}/collection/{collectionName}")
	@Produces( { "application/xhtml+xml", "text/html" })
	public abstract String specCollection(
			@PathParam("specFullName") final String specFullName,
			@PathParam("collectionName") final String collectionName);

	@GET
	@Path("/{specFullName}/action/{actionId}")
	@Produces( { "application/xhtml+xml", "text/html" })
	public abstract String specAction(
			@PathParam("specFullName") final String specFullName,
			@PathParam("actionId") final String actionId);

	@GET
	@Path("/{specFullName}/property/{propertyName}/facet/{facetType}")
	@Produces( { "application/xhtml+xml", "text/html" })
	public abstract String specPropertyFacet(
			@PathParam("specFullName") final String specFullName,
			@PathParam("propertyName") final String propertyName,
			@PathParam("facetType") final String facetTypeName);

	@GET
	@Path("/{specFullName}/collection/{collectionName}/facet/{facetType}")
	@Produces( { "application/xhtml+xml", "text/html" })
	public abstract String specCollectionFacet(
			@PathParam("specFullName") final String specFullName,
			@PathParam("collectionName") final String collectionName,
			@PathParam("facetType") final String facetTypeName);

	@GET
	@Path("/{specFullName}/action/{actionId}/facet/{facetType}")
	@Produces( { "application/xhtml+xml", "text/html" })
	public abstract String specActionFacet(
			@PathParam("specFullName") final String specFullName,
			@PathParam("actionId") final String actionId,
			@PathParam("facetType") final String facetTypeName);

}