package org.apache.isis.core.runtime.plugins.codegen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apache.isis.applib.internal.base._Casts;
import org.apache.isis.applib.internal.base._NullSafe;
import org.apache.isis.core.commons.exceptions.IsisException;

public class ProxyFactoryPluginUsingJavassist implements ProxyFactoryPlugin {

	@Override
	public <T> ProxyFactory<T> factory(
			final Class<T> base, 
			final Class<?>[] interfaces, 
			final Predicate<Method> methodFilter,
			final Class<?>[] constructorArgTypes) {
		
        final javassist.util.proxy.ProxyFactory pfDelegate = new javassist.util.proxy.ProxyFactory();

        pfDelegate.setSuperclass(base);
        pfDelegate.setInterfaces(interfaces);

        if(methodFilter!=null) {
        	pfDelegate.setFilter(methodFilter::test);	
        }
		
        final int constructorArgCount = _NullSafe.size(constructorArgTypes);
        
		return new ProxyFactory<T>() {

			@Override
			public T createInstance(final InvocationHandler handler, final Object[] constructorArgs) {
				
				final int constructorArgCountActual = _NullSafe.size(constructorArgTypes);
				
				if(constructorArgCount != constructorArgCountActual) {
					throw new IllegalArgumentException(String.format("Contructor args expected %d, got %d.", 
							constructorArgCount, constructorArgCountActual));
				}
				
				try {
					return _Casts.uncheckedCast( pfDelegate.create(
							constructorArgTypes, 
							constructorArgs, 
							(Object self, Method thisMethod, Method proceed, Object[] args)->{
								return handler.invoke(self, thisMethod, args);
							}));
				} catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | 
						IllegalAccessException | InvocationTargetException e) {
					throw new IsisException(e);
				}
			}
			
		};
	}

}
