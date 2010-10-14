/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor;

import java.lang.reflect.Method;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.PropertyAccessException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.Setter;


/**
 * Access properties of an object in a Isis system, where that property is a BusinessValueHolder or
 * some other object which cannot be mapped using the standard Hibernate property accessors.
 */
public abstract class ObjectPropertyAccessorAbstract implements PropertyAccessor {

    public static final class [[NAME]]Setter implements Setter {
        private static final long serialVersionUID = 1L;
        private final Method getValueHolder;
        private final PropertyConverter converter;
        private final Class<?> clazz;
        private final String name;

        [[NAME]]Setter(final Method getValueHolder, final PropertyConverter converter, final Class<?> clazz, final String name) {
            this.getValueHolder = getValueHolder;
            this.converter = converter;
            this.clazz = clazz;
            this.name = name;
        }

        public Method getMethod() {
            return null;
        }

        public String getMethodName() {
            return null;
        }

        public void set(final Object target, final Object value, final SessionFactoryImplementor factory)
                throws HibernateException {
            try {
                final Object valueHolder = getValueHolder.invoke(target, (Object[]) null);
                converter.setValue(valueHolder, value);
            } catch (final Exception e) {
                throw new PropertyAccessException(e, "could not set a field value by reflection", true, clazz, name);
            }
        }
    }

    public static final class [[NAME]]Getter implements Getter {
        private static final long serialVersionUID = 1L;
        private final Method getValueHolder;
        private final PropertyConverter converter;
        private final boolean isNullable;
        private final Class<?> clazz;
        private final String name;

        [[NAME]]Getter(
                final Method getValueHolder,
                final PropertyConverter converter,
                final boolean isNullable,
                final Class<?> clazz,
                final String name) {
            this.getValueHolder = getValueHolder;
            this.converter = converter;
            this.isNullable = isNullable;
            this.clazz = clazz;
            this.name = name;
        }

        public Object get(final Object target) throws HibernateException {
            try {
                final Object valueHolder = getValueHolder.invoke(target, (Object[]) null);
                return converter.getPersistentValue(valueHolder, isNullable);
            } catch (final Exception e) {
                throw new PropertyAccessException(e, "could not get a field value by reflection", false, clazz, name);
            }
        }

        public Method getMethod() {
            return null;
        }

        public String getMethodName() {
            return null;
        }

        public Class<?> getReturnType() {
            return converter.getPersistentType();
        }

        @SuppressWarnings("unchecked")
        public Object getForInsert(final Object target, final Map mergeMap, final SessionImplementor session)
                throws HibernateException {
            return get(target);
        }
    }

    protected Method getValueHolderMethod(final Class<?> theClass, final String propertyName) throws PropertyNotFoundException {
        final String naturalName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

        Method getMethod = null;
        try {
            getMethod = theClass.getMethod("get" + naturalName, (Class[]) null);
        } catch (final SecurityException se) {
            throw new PropertyNotFoundException("Cannot access method get" + naturalName + " in " + theClass + " : "
                    + se.getMessage());
        } catch (final NoSuchMethodException nsme) {
            try {
                // handle .NET properties
                getMethod = theClass.getMethod("get_" + naturalName, (Class[]) null);
            } catch (final SecurityException se) {
                throw new PropertyNotFoundException("Cannot access method get_" + naturalName + " in " + theClass + " : "
                        + se.getMessage());
            } catch (final NoSuchMethodException nsme2) {
                throw new PropertyNotFoundException("Unknown property " + naturalName + " in " + theClass);
            }
        }
        return getMethod;
    }

    protected PropertyConverter getConverter(final Method getValueHolder) {
        return ConverterFactory.getInstance().getConverter(getValueHolder.getReturnType());
    }

    @SuppressWarnings("unchecked")
    public Setter getSetter(final Class theClass, final String propertyName) throws PropertyNotFoundException {
        final Method getValueHolder = getValueHolderMethod(theClass, propertyName);
        return new [[NAME]]Setter(getValueHolder, getConverter(getValueHolder), theClass, propertyName);
    }

    public Getter getGetter(final Class<?> theClass, final String propertyName, final boolean isNullable)
            throws PropertyNotFoundException {
        final Method getValueHolder = getValueHolderMethod(theClass, propertyName);
        return new [[NAME]]Getter(getValueHolder, getConverter(getValueHolder), isNullable, theClass, propertyName);
    }

}
