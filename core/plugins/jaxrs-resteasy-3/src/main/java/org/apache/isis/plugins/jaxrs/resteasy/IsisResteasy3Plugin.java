package org.apache.isis.plugins.jaxrs.resteasy;

import javax.ws.rs.core.UriBuilder;

import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.UriBuilderPlugin;
import org.apache.isis.viewer.restfulobjects.server.IsisJaxrsServerPlugin;
import org.apache.isis.viewer.restfulobjects.server.conneg.RestfulObjectsJaxbWriterForXml;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import org.jboss.resteasy.spi.Failure;

public class IsisResteasy3Plugin implements UriBuilderPlugin, IsisJaxrsServerPlugin {

	@Override
	public UriBuilder uriTemplate(String uriTemplate) {
		return new ResteasyUriBuilder().uriTemplate(uriTemplate);
	}

	@Override
	public Object newRestfulObjectsJaxbWriterForXml() {
		
		return new RestfulObjectsJaxbWriterForXml();
	}

	@Override
	public HttpStatusCode getFailureStatusCodeIfAny(Throwable ex) {
		
		return (ex instanceof Failure) 
				? RestfulResponse.HttpStatusCode.statusFor(((Failure)ex).getErrorCode())
				: null;
	}

}
