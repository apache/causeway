package org.apache.isis.core.metamodel.services.swagger.internal;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.services.swagger.internal.ValuePropertyFactory.Factory;

public interface ValuePropertyPlugin {

	// -- CONTRACT
	
	public static interface ValuePropertyCollector {
	    public void addValueProperty(final Class<?> cls, final ValuePropertyFactory.Factory factory);
	    public void visitEntries(BiConsumer<Class<?>, ValuePropertyFactory.Factory> visitor);
	}
	
	public static ValuePropertyCollector collector() {
		
		return new ValuePropertyCollector() {
			
			final Map<Class<?>, Factory> entries = _Maps.newHashMap();
			
			@Override
			public void visitEntries(BiConsumer<Class<?>, Factory> visitor) {
				Objects.requireNonNull(visitor);
				entries.forEach(visitor);
			}
			
			@Override
			public void addValueProperty(Class<?> cls, Factory factory) {
				Objects.requireNonNull(cls);
				Objects.requireNonNull(factory);
				entries.put(cls, factory);
			}
		};
		
	}
	
	// -- INTERFACE
	
	public void plugin(ValuePropertyCollector collector);
	
}
