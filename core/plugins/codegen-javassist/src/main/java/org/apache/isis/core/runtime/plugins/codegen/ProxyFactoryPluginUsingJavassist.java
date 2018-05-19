package org.apache.isis.core.runtime.plugins.codegen;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apache.isis.applib.internal.exceptions._Exceptions;

public class ProxyFactoryPluginUsingJavassist implements ProxyFactoryPlugin {

	@Override
	public <T> ProxyFactory<T> factory(
			Class<T> base, 
			Class<?>[] interfaces, 
			Predicate<Method> methodFilter,
			Class<?>[] constructorArgTypes) {
		// TODO Auto-generated method stub
		_Exceptions.throwNotImplemented();
		return null;
	}

}
