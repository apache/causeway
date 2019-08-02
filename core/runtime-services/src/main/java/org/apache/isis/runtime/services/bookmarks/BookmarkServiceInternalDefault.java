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
package org.apache.isis.runtime.services.bookmarks;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkHolder;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.tree.TreeState;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.metamodel.adapter.oid.ObjectNotFoundException;
import org.apache.isis.metamodel.services.persistsession.PersistenceSessionServiceInternal;

/**
 * This service enables a serializable 'bookmark' to be created for an entity.
 *
 */
@Singleton
public class BookmarkServiceInternalDefault implements BookmarkService, SerializingAdapter {


    @Override
    @Programmatic
    public Object lookup(
            final BookmarkHolder bookmarkHolder,
            final FieldResetPolicy fieldResetPolicy) {
        Bookmark bookmark = bookmarkHolder.bookmark();
        return bookmark != null? lookup(bookmark, fieldResetPolicy): null;
    }

    private Object lookupInternal(
            final Bookmark bookmark,
            final FieldResetPolicy fieldResetPolicy) {
        if(bookmark == null) {
            return null;
        }
        try {
            return persistenceSessionServiceInternal.lookup(bookmark, fieldResetPolicy);
        } catch(ObjectNotFoundException ex) {
            return null;
        }
    }


    @Programmatic
    @Override
    public Object lookup(
            final Bookmark bookmark,
            final FieldResetPolicy fieldResetPolicy) {
        if(bookmark == null) {
            return null;
        }
        //FIXME[2112] why would we ever store Service Beans as Bookmarks?        
        //        final String objectType = bookmark.getObjectType();
        //        final Object service = lookupService(objectType);
        //        if(service != null) {
        //            return service;
        //        }
        return lookupInternal(bookmark, fieldResetPolicy);
    }

    @SuppressWarnings("unchecked")
    @Programmatic
    @Override
    public <T> T lookup(
            final Bookmark bookmark,
            final FieldResetPolicy fieldResetPolicy,
            Class<T> cls) {
        return (T) lookup(bookmark, fieldResetPolicy);
    }

    @Programmatic
    @Override
    public Bookmark bookmarkFor(final Object domainObject) {
        if(domainObject == null) {
            return null;
        }
        return persistenceSessionServiceInternal.bookmarkFor(unwrapped(domainObject));
    }

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }


    @Programmatic
    @Override
    public Bookmark bookmarkFor(Class<?> cls, String identifier) {
        return persistenceSessionServiceInternal.bookmarkFor(cls, identifier);
    }

    //FIXME[2112] why would we ever store Service Beans as Bookmarks?
    //    private Map<String,Object> servicesByClassName;
    //    private Object lookupService(final String className) {
    //        cacheServicesByClassNameIfNecessary();
    //        return servicesByClassName.get(className);
    //    }
    //
    //    private void cacheServicesByClassNameIfNecessary() {
    //        if (servicesByClassName == null) {
    //            final Map<String,Object> servicesByClassName = _Maps.newHashMap();
    //            final Stream<Object> registeredServices = serviceRegistry.streamServices();
    //            registeredServices.forEach(registeredService->{
    //                final String serviceClassName = registeredService.getClass().getName();
    //                servicesByClassName.put(serviceClassName, registeredService);
    //            });
    //            this.servicesByClassName = servicesByClassName;
    //        }
    //    }

    // -- SERIALIZING ADAPTER IMPLEMENTATION

    @Override
    public <T> T read(Class<T> cls, Serializable value) {

        if(Bookmark.class.equals(cls)) {
            return _Casts.uncheckedCast(value);
        }

        if(Bookmark.class.isAssignableFrom(value.getClass())) {
            final Bookmark valueBookmark = (Bookmark) value;
            return _Casts.uncheckedCast(lookup(valueBookmark, FieldResetPolicy.RESET));
        }

        return _Casts.uncheckedCast(value);
    }

    @Override
    public Serializable write(Object value) {
        if(isPredefinedSerializable(value.getClass())) {
            return (Serializable) value;
        } else {
            final Bookmark valueBookmark = bookmarkFor(value);
            return valueBookmark;
        }
    }

    // -- HELPER

    private final static Set<Class<? extends Serializable>> serializableFinalTypes = _Sets.of(
            String.class, String[].class,
            Class.class, Class[].class,
            Boolean.class, boolean.class, Boolean[].class, boolean[].class,
            Byte.class, byte.class, Byte[].class, byte[].class,
            Short.class, short.class, Short[].class, short[].class,
            Integer.class, int.class, Integer[].class, int[].class,
            Long.class, long.class, Long[].class, long[].class,
            Float.class, float.class, Float[].class, float[].class,
            Double.class, double.class, Double[].class, double[].class
            );

    private final static List<Class<? extends Serializable>> serializableTypes = _Lists.of(
            BigDecimal.class,
            BigInteger.class,
            java.util.Date.class,
            java.sql.Date.class,
            Enum.class,
            Bookmark.class,
            TreeState.class
            );

    private static boolean isPredefinedSerializable(final Class<?> cls) {
        if(!Serializable.class.isAssignableFrom(cls)) {
            return false;
        }
        //[ahuber] any non-scalar values could be problematic, so we are careful with wild-cards here
        if(cls.getName().startsWith("java.time.")) {
            return true;
        }
        if(cls.getName().startsWith("org.joda.time.")) {
            return true;
        }
        if(serializableFinalTypes.contains(cls)) {
            return true;
        }
        return serializableTypes.stream().anyMatch(t->t.isAssignableFrom(cls));
    }

    // -- INJECTION

    @Inject
    PersistenceSessionServiceInternal persistenceSessionServiceInternal;

    @Inject
    WrapperFactory wrapperFactory;

    @Inject
    ServiceRegistry serviceRegistry;

}
