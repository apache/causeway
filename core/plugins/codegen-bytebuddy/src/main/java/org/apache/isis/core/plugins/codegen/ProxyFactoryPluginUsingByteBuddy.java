package org.apache.isis.core.plugins.codegen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.context._Context;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

public class ProxyFactoryPluginUsingByteBuddy implements ProxyFactoryPlugin {

	private static <T> ImplementationDefinition<T> nextProxyDef(
			Class<T> base, 
			Class<?>[] interfaces) {
		return new ByteBuddy()
			.with(new NamingStrategy.SuffixingRandom("bb"))
			.subclass(base)
			.implement(interfaces)
			.method(ElementMatchers.any());
	}
	
	@Override
	public <T> ProxyFactory<T> factory(
			Class<T> base, 
			Class<?>[] interfaces, 
			Class<?>[] constructorArgTypes) {
		
		final Objenesis objenesis = new ObjenesisStd();

		final Function<InvocationHandler, Class<? extends T>> proxyClassFactory = handler->
				nextProxyDef(base, interfaces)
				.intercept(InvocationHandlerAdapter.of(handler))
				.make()
				.load(_Context.getDefaultClassLoader())
				.getLoaded();

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
					throw new RuntimeException(e);
				}
				
			}

			@Override
			public T createInstance(InvocationHandler handler, Object[] constructorArgs) {
				
				ensureNonEmtpy(constructorArgs);
				ensureSameSize(constructorArgTypes, constructorArgs);

				try {
					return _Casts.uncheckedCast( createUsingConstructor(handler, constructorArgs) );
				} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | 
						IllegalArgumentException | InvocationTargetException | SecurityException  e) {
					throw new RuntimeException(e);
				}
			}

			// -- HELPER (create w/o initialize)
			
			private Object createNotUsingConstructor(InvocationHandler invocationHandler) {
				final Class<? extends T> proxyClass = proxyClassFactory.apply(invocationHandler);
				final Object object = objenesis.newInstance(proxyClass);
				return object;
			}
			
			// -- HELPER (create with initialize)
			
			private Object createUsingConstructor(InvocationHandler invocationHandler, @Nullable Object[] constructorArgs) 
					throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
				final Class<? extends T> proxyClass = proxyClassFactory.apply(invocationHandler);
				return proxyClass
				.getConstructor(constructorArgTypes==null ? _Constants.emptyClasses : constructorArgTypes)
				.newInstance(constructorArgs==null ? _Constants.emptyObjects : constructorArgs);
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
