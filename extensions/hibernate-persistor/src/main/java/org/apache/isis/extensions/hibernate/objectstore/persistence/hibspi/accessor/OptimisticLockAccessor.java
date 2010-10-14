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
import org.hibernate.mapping.MetaAttribute;
import org.hibernate.mapping.Property;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.extensions.hibernate.objectstore.metamodel.version.LongVersion;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;


/**
 * Accesses Isis version fields for use in optimistic locking. This accessor may wrap another
 * PropertyAccessor if version properties are also declared on the object (these properties must be updated in
 * the NOF adapter, but may also be in the object).
 */
public abstract class OptimisticLockAccessor implements PropertyAccessor {

    public interface LongVersionAccessor {
        Object get(LongVersion version);

        void set(LongVersion version, Object value);

        Class<?> getReturnType();
    }

    public static final class OptimisticLockSetter implements Setter {
        private static final long serialVersionUID = 1L;
        private final Class<?> clazz;
        private final String name;
        private final LongVersionAccessor versionAccessor;
        private final Setter wrappedSetter;

        OptimisticLockSetter(
                final Class<?> clazz,
                final String name,
                final LongVersionAccessor versionAccessor,
                final Setter wrappedSetter) {
            this.clazz = clazz;
            this.name = name;
            this.versionAccessor = versionAccessor;
            this.wrappedSetter = wrappedSetter;
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
                final ObjectAdapter targetAdapter = getAdapterManager().getAdapterFor(target);
                LongVersion version = (LongVersion) targetAdapter.getVersion();
                if (version == null) {
                    version = new LongVersion();
                    targetAdapter.setOptimisticLock(version);
                }
                versionAccessor.set(version, value);
                if (wrappedSetter != null) {
                    wrappedSetter.set(target, value, factory);
                }
            } catch (final Exception e) {
                throw new PropertyAccessException(e, "could not set a value by reflection", true, clazz, name);
            }
        }
    }

    public static final class OptimisticLockGetter implements Getter {
        private static final long serialVersionUID = 1L;
        private final Class<?> clazz;
        private final String name;
        private final LongVersionAccessor versionAccessor;

        OptimisticLockGetter(final Class<?> clazz, final String name, final LongVersionAccessor versionAccessor) {
            this.clazz = clazz;
            this.name = name;
            this.versionAccessor = versionAccessor;
        }

        public Object get(final Object target) throws HibernateException {
            try {
                final ObjectAdapter targetAdapter = getAdapterManager().getAdapterFor(target);
                // Hibernate might call to check what the unsaved value is - hence no adapter
                if (targetAdapter == null) {
                    return null;
                }
                final LongVersion version = (LongVersion) targetAdapter.getVersion();
                if (version == null) {
                    return null;
                }
                return versionAccessor.get(version);
            } catch (final Exception e) {
                throw new PropertyAccessException(e, "could not get a value by reflection", false, clazz, name);
            }
        }

        public Method getMethod() {
            return null;
        }

        public String getMethodName() {
            return null;
        }

        public Class<?> getReturnType() {
            return versionAccessor.getReturnType();
        }

        @SuppressWarnings("unchecked")
        public Object getForInsert(final Object target, final Map mergeMap, final SessionImplementor session)
                throws HibernateException {
            return get(target);
        }
    }

    protected abstract LongVersionAccessor getLongVersionAccessor();

    @SuppressWarnings("unchecked")
    public Setter getSetter(final Class theClass, final String propertyName) throws PropertyNotFoundException {
        Setter setter = null;
        final Property thisProperty = HibernateUtil.getConfiguration().getClassMapping(theClass.getName()).getProperty(
                propertyName);
        final MetaAttribute propertyMeta = thisProperty.getMetaAttribute(PropertyHelper.NAKED_PROPERTY);
        if (propertyMeta != null) {
            String access = null;
            final MetaAttribute accessMeta = thisProperty.getMetaAttribute(PropertyHelper.NAKED_ACCESS);
            if (accessMeta != null) {
                access = accessMeta.getValue();
            }
            final PropertyAccessor wrappedAccessor = PropertyAccessorFactory.getPropertyAccessor(access);
            if (wrappedAccessor != null) {
                setter = wrappedAccessor.getSetter(theClass, propertyMeta.getValue());
            }
        }
        return new OptimisticLockSetter(theClass, propertyName, getLongVersionAccessor(), setter);
    }

    @SuppressWarnings("unchecked")
    public Getter getGetter(final Class theClass, final String propertyName) throws PropertyNotFoundException {
        final Property thisProperty = HibernateUtil.getConfiguration().getClassMapping(theClass.getName()).getProperty(
                propertyName);
        final MetaAttribute propertyMeta = thisProperty.getMetaAttribute(PropertyHelper.NAKED_PROPERTY);
        if (propertyMeta != null) {
            String access = null;
            final MetaAttribute accessMeta = thisProperty.getMetaAttribute(PropertyHelper.NAKED_ACCESS);
            if (accessMeta != null) {
                access = accessMeta.getValue();
            }
            final PropertyAccessor wrappedAccessor = PropertyAccessorFactory.getPropertyAccessor(access);
            if (wrappedAccessor != null) {
                return wrappedAccessor.getGetter(theClass, propertyMeta.getValue());
            }
        }
        return new OptimisticLockGetter(theClass, propertyName, getLongVersionAccessor());
    }
    

    
    ///////////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////////

    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }


}
