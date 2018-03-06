package org.apache.isis.viewer.restfulobjects.applib.client;

import javax.ws.rs.core.UriBuilder;

import org.apache.isis.applib.internal.context._Plugin;

public interface UriBuilderPlugin {

	// -- INTERFACE
	
	public UriBuilder uriTemplate(String uriTemplate);
	
	// -- LOOKUP
	
	public static UriBuilderPlugin get() {
		return _Plugin.getOrElse(UriBuilderPlugin.class, 
				ambigousPlugins->{
					throw _Plugin.ambiguityNonRecoverable(UriBuilderPlugin.class, ambigousPlugins); 
				}, 
				()->{
					throw _Plugin.absenceNonRecoverable(UriBuilderPlugin.class);
				});
	}
	
}
