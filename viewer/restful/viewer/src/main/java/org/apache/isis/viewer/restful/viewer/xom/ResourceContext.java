package org.apache.isis.viewer.restful.viewer.xom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;


public class ResourceContext {

    private final HttpServletRequest httpServletRequest;

    private final HttpServletResponse httpServletResponse;

    private final Request request;

	private final HttpHeaders httpHeaders;

	private final UriInfo uriInfo;

	private final SecurityContext securityContext;

    public ResourceContext(
            final HttpHeaders httpHeaders,
            final UriInfo uriInfo,
            final Request request, 
            final HttpServletRequest httpServletRequest, 
            final HttpServletResponse httpServletResponse, 
            final SecurityContext securityContext) {
    	this.httpHeaders = httpHeaders;
    	this.uriInfo = uriInfo;
    	this.request = request;
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
        this.securityContext = securityContext;
    }

    public HttpHeaders getHttpHeaders() {
		return httpHeaders;
	}
    
    public UriInfo getUriInfo() {
		return uriInfo;
	}
    
    public Request getRequest() {
    	return request;
    }
    
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public HttpServletResponse getServletResponse() {
        return httpServletResponse;
    }

    public SecurityContext getSecurityContext() {
		return securityContext;
	}
}
