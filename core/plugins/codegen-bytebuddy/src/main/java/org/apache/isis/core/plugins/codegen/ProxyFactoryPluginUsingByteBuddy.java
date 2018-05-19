package org.apache.isis.core.plugins.codegen;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.plugins.codegen.ProxyFactory;
import org.apache.isis.core.plugins.codegen.ProxyFactoryPlugin;

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
