package org.apache.isis.core.runtime.plugins.codegen;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apache.isis.applib.internal.exceptions._Exceptions;

public class ProxyFactoryPluginUsingByteBuddy implements ProxyFactoryPlugin {

	@Override
	public <T> ProxyFactory<T> factory(
			Class<T> base, 
			Class<?>[] interfaces, 
			Predicate<Method> methodFilter,
			Class<?>[] constructorArgTypes) {
		
		_Exceptions.throwNotImplemented();
		// TODO Auto-generated method stub
		return null;
	}

}
