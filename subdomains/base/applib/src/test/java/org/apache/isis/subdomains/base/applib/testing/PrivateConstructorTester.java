package org.apache.isis.subdomains.base.applib.testing;

import java.lang.reflect.Constructor;

import org.apache.isis.core.commons.internal.reflection._Reflect;

public final class PrivateConstructorTester {

    private Class<?> cls;

	public PrivateConstructorTester(Class<?> cls) {
		this.cls = cls;
	}

	public void exercise() throws Exception {
        final Constructor<?> constructor = cls.getDeclaredConstructor();
        _Reflect.invokeConstructor(constructor);
    }
}
