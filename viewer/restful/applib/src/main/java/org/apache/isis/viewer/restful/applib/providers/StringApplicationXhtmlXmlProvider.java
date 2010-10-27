package org.apache.isis.viewer.restful.applib.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.StringTextStar;

@Provider
@Produces("application/xhtml+xml")
@Consumes("application/xhtml+xml")
public class StringApplicationXhtmlXmlProvider extends StringTextStar {

	@Override
	public long getSize(Object o, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return o.toString().getBytes().length;
	}
}
