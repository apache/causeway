package org.apache.isis.extensions.restful.viewer;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public abstract class AbstractJaxRsApplication extends Application {

	private Set<Object> singletons = new LinkedHashSet<Object>();
	private Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

	public AbstractJaxRsApplication() {
	}

	@Override
	public Set<Class<?>> getClasses() {
		return Collections.unmodifiableSet(classes);
	}

	@Override
	public Set<Object> getSingletons() {
		return Collections.unmodifiableSet(singletons);
	}

	
	protected boolean addClass(Class<?> cls) {
		return classes.add(cls);
	}
	
	protected boolean addSingleton(Object resource) {
		return singletons.add(resource);
	}

}