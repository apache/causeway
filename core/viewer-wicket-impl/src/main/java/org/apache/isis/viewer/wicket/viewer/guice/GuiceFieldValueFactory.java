/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.isis.viewer.wicket.viewer.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Qualifier;

import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.proxy.LazyInitProxyFactory;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import org.apache.wicket.util.lang.Generics;

/**
 *
 */
public class GuiceFieldValueFactory implements IFieldValueFactory
{
	private final ConcurrentMap<GuiceProxyTargetLocator, Object> cache = Generics.newConcurrentHashMap();
	private static final Object NULL_SENTINEL = new Object();

	private final boolean wrapInProxies;

	/**
	 * Construct.
	 *
	 * @param wrapInProxies
	 */
	GuiceFieldValueFactory(final boolean wrapInProxies)
	{
		this.wrapInProxies = wrapInProxies;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getFieldValue(final Field field, final Object fieldOwner)
	{
		Object target = null;

		if (supportsField(field))
		{
			Inject injectAnnotation = field.getAnnotation(Inject.class);
			javax.inject.Inject javaxInjectAnnotation = field.getAnnotation(javax.inject.Inject.class);
			if (!Modifier.isStatic(field.getModifiers()) && (injectAnnotation != null || javaxInjectAnnotation != null))
			{
				try
				{
					boolean optional = injectAnnotation != null && injectAnnotation.optional();
					Annotation bindingAnnotation = findBindingAnnotation(field.getAnnotations());
					final GuiceProxyTargetLocator locator = new GuiceProxyTargetLocator(field, bindingAnnotation, optional);

					Object cachedValue = cache.get(locator);
					if (cachedValue != null)
					{
						return cachedValue == NULL_SENTINEL ? null : cachedValue;
					}

					target = locator.locateProxyTarget();
					if (target == null)
					{
						// Optional without a binding, return null
					}
					else
					{
						if (wrapInProxies)
						{
							target = LazyInitProxyFactory.createProxy(field.getType(), locator);
						}
					}

					if (locator.isSingletonScope())
					{
						Object tmpTarget = cache.putIfAbsent(locator, target == null ? NULL_SENTINEL : target);
						if (tmpTarget != null)
						{
							target = tmpTarget;
						}
					}

					if (!field.isAccessible())
					{
						field.setAccessible(true);
					}
				}
				catch (MoreThanOneBindingException e)
				{
					throw new RuntimeException(
							"Can't have more than one BindingAnnotation on field " + field.getName() +
									" of class " + fieldOwner.getClass().getName());
				}
			}
		}

		return target == NULL_SENTINEL ? null : target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supportsField(final Field field)
	{
		return field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(javax.inject.Inject.class);
	}

	/**
	 *
	 * @param annotations
	 * @return Annotation
	 * @throws MoreThanOneBindingException
	 */
	private Annotation findBindingAnnotation(final Annotation[] annotations)
			throws MoreThanOneBindingException
	{
		Annotation bindingAnnotation = null;

		// Work out if we have a BindingAnnotation on this parameter.
		for (Annotation annotation : annotations)
		{
			if (annotation.annotationType().getAnnotation(BindingAnnotation.class) != null ||
					annotation.annotationType().getAnnotation(Qualifier.class) != null)
			{
				if (bindingAnnotation != null)
				{
					throw new MoreThanOneBindingException();
				}
				bindingAnnotation = annotation;
			}
		}
		return bindingAnnotation;
	}

	/**
	 *
	 */
	public static class MoreThanOneBindingException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
}
