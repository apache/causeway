package org.apache.isis.core.runtime.plugins.codegen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apache.isis.applib.internal.base._Casts;
import org.apache.isis.applib.internal.base._NullSafe;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import javassist.util.proxy.ProxyObject;

public class ProxyFactoryPluginUsingJavassist implements ProxyFactoryPlugin {
	
	@Override
	public <T> ProxyFactory<T> factory(
			final Class<T> base, 
			final Class<?>[] interfaces, 
			final Predicate<Method> methodFilter,
			final Class<?>[] constructorArgTypes) {
		
        final javassist.util.proxy.ProxyFactory pfDelegate = new javassist.util.proxy.ProxyFactory();
        final Objenesis objenesis = new ObjenesisStd();

        pfDelegate.setSuperclass(base);
        pfDelegate.setInterfaces(interfaces);

        if(methodFilter!=null) {
        	pfDelegate.setFilter(methodFilter::test);	
        }
        
		return new ProxyFactory<T>() {

			@Override
			public T createInstance(InvocationHandler handler, Object[] constructorArgs) {
				
				ensureSameSize(constructorArgTypes, constructorArgs);

				try {
					
					if(_NullSafe.isEmpty(constructorArgTypes)) {
						return _Casts.uncheckedCast( createNotUsingConstructor(handler) );	
					} else {
						return _Casts.uncheckedCast( createUsingConstructor(handler, constructorArgs) );
					}
					
				} catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | 
						IllegalAccessException | InvocationTargetException e) {
					throw new IsisException(e);
				}
			}

			private Object createNotUsingConstructor(InvocationHandler handler) {
				final Class<?> proxyClass = pfDelegate.createClass();
				
				final Object object = objenesis.newInstance(proxyClass);
				
				((ProxyObject)object).setHandler((Object self, Method thisMethod, Method proceed, Object[] args)->{
					return handler.invoke(self, thisMethod, args);
				});
				
				return object;
			}
			
			private Object createUsingConstructor(InvocationHandler handler, Object[] constructorArgs)
				throws NoSuchMethodException, IllegalArgumentException, InstantiationException, 
					IllegalAccessException, InvocationTargetException {
				
				return pfDelegate.create(
						constructorArgTypes, 
						constructorArgs, 
						(Object self, Method thisMethod, Method proceed, Object[] args)->{
							return handler.invoke(self, thisMethod, args);
						});
			}
			
		};
	}

	// -- HELPER
	
	private static void ensureSameSize(Class<?>[] a, Object[] b) {
		if(_NullSafe.size(a) != _NullSafe.size(b)) {
			throw new IllegalArgumentException(String.format("Contructor args expected %d, got %d.", 
					_NullSafe.size(a), _NullSafe.size(b) ));
		}
	}

}
