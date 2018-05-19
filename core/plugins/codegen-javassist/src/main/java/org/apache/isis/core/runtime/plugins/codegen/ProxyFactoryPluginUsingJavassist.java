package org.apache.isis.core.runtime.plugins.codegen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.isis.applib.internal._Constants;
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
			public T createInstance(InvocationHandler handler, boolean initialize) {

				try {
					
					if(initialize) {
						ensureSameSize(constructorArgTypes, null);
						return _Casts.uncheckedCast( createUsingConstructor(handler, null) );
					} else {
						return _Casts.uncheckedCast( createNotUsingConstructor(handler) );
					}
					
				} catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | 
						IllegalAccessException | InvocationTargetException e) {
					throw new IsisException(e);
				}
				
			}

			@Override
			public T createInstance(InvocationHandler handler, Object[] constructorArgs) {
				
				ensureSameSize(constructorArgTypes, constructorArgs);
				ensureNonEmtpy(constructorArgs);

				try {
					return _Casts.uncheckedCast( createUsingConstructor(handler, constructorArgs) );
				} catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | 
						IllegalAccessException | InvocationTargetException e) {
					throw new IsisException(e);
				}
			}

			// -- HELPER (create w/o initialize)
			
			private Object createNotUsingConstructor(InvocationHandler handler) {
				final Class<?> proxyClass = pfDelegate.createClass();
				
				final Object object = objenesis.newInstance(proxyClass);
				
				((ProxyObject)object).setHandler((Object self, Method thisMethod, Method proceed, Object[] args)->{
					return handler.invoke(self, thisMethod, args);
				});
				
				return object;
			}
			
			// -- HELPER (create with initialize)
			
			private Object createUsingConstructor(InvocationHandler handler, @Nullable Object[] constructorArgs)
				throws NoSuchMethodException, IllegalArgumentException, InstantiationException, 
					IllegalAccessException, InvocationTargetException {
				
				return pfDelegate.create(
						constructorArgTypes==null ? _Constants.emptyClasses : constructorArgTypes, 
						constructorArgs==null ? _Constants.emptyObjects : constructorArgs,
						(Object self, Method thisMethod, Method proceed, Object[] args)->{
							return handler.invoke(self, thisMethod, args);
						});
			}
			
		};
	}

	// -- HELPER
	
	private static void ensureSameSize(Class<?>[] a, Object[] b) {
		if(_NullSafe.size(a) != _NullSafe.size(b)) {
			throw new IllegalArgumentException(String.format("Constructor arg count expected %d, got %d.", 
					_NullSafe.size(a), _NullSafe.size(b) ));
		}
	}
	
	private static void ensureNonEmtpy(Object[] a) {
		if(_NullSafe.isEmpty(a)) {
			throw new IllegalArgumentException(String.format("Contructor args count expected > 0, got %d.", 
					_NullSafe.size(a) ));
		}
	}

}
