package org.apache.isis.viewer.restfulobjects.server;

import org.apache.isis.applib.internal.context._Plugin;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;

public interface IsisJaxrsServerPlugin {

	// -- INTERFACE

	public Object newRestfulObjectsJaxbWriterForXml();

	public HttpStatusCode getFailureStatusCodeIfAny(Throwable ex);
	
	// -- LOOKUP

	public static IsisJaxrsServerPlugin get() {
		return _Plugin.getOrElse(IsisJaxrsServerPlugin.class, 
				ambiguousPlugins->{
					throw _Plugin.ambiguityNonRecoverable(IsisJaxrsServerPlugin.class, ambiguousPlugins); 
				}, 
				()->{
					throw _Plugin.absenceNonRecoverable(IsisJaxrsServerPlugin.class);
				});
	}

}
