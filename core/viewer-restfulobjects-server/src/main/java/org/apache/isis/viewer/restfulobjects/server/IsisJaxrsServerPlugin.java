package org.apache.isis.viewer.restfulobjects.server;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.internal.context._Plugin;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;

public interface IsisJaxrsServerPlugin {

	// -- INTERFACE

	public Object newRestfulObjectsJaxbWriterForXml();

	public HttpStatusCode getFailureStatusCodeIfAny(Throwable ex);
	
	// -- LOOKUP

	public static IsisJaxrsServerPlugin get() {
		return _Plugin.getOrElse(IsisJaxrsServerPlugin.class, 
				ambigousPlugins->{
					throw new NonRecoverableException("Ambigous plugins implementing IsisJaxrsServerPlugin found on class path.");
				}, 
				()->{
					throw new NonRecoverableException("No plugin implementing IsisJaxrsServerPlugin found on class path.");
				}); 
	}

}
