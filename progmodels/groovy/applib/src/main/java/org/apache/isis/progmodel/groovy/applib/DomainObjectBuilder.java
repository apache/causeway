package org.apache.isis.progmodel.groovy.applib;

import groovy.util.ObjectGraphBuilder;

import java.util.Map;

import org.apache.isis.applib.DomainObjectContainer;

public class DomainObjectBuilder<T> extends ObjectGraphBuilder {

	private final DomainObjectContainer container;

	public DomainObjectBuilder(final DomainObjectContainer container, final Class<?>... classes) {
		this.container = container;
		ClassNameResolver classNameResolver = new ClassNameResolver() {
			@Override
			public String resolveClassname(String classname) {
				for(Class<?> cls: classes) {
					String packageName = cls.getPackage().getName();
					String fqcn = packageName + "." + upperFirst(classname);
					try {
						Thread.currentThread().getContextClassLoader().loadClass(fqcn);
						return fqcn;
					} catch(ClassNotFoundException ex) {
						// continue
					}
				}
				throw new RuntimeException("could not resolve " + classname + "'");
			}

		};
		this.setClassNameResolver(classNameResolver);
		final NewInstanceResolver instanceResolver = new DefaultNewInstanceResolver() {
			@SuppressWarnings("unchecked")
			@Override
			public Object newInstance(Class cls, Map attributes)
					throws InstantiationException, IllegalAccessException {
				return container.newTransientInstance(cls);
			}
		};
		this.setNewInstanceResolver(instanceResolver);
	}
	private static String upperFirst(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Object createNode(Object arg0, Map arg1, Object arg2) {
		Object domainObject = super.createNode(arg0, arg1, arg2);
		container.persistIfNotAlready(domainObject);
		return domainObject;
	}
}
