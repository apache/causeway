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
package org.apache.isis.persistence.jdo.integration.metamodel.facets.entity;

import java.lang.reflect.Method;
import java.math.BigInteger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.query.AllInstancesQuery;
import org.apache.isis.applib.query.NamedQuery;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.commons.internal.primitives._Longs;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.persistence.jdo.applib.integration.JdoSupportService;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class JdoEntityFacetFactory extends FacetFactoryAbstract {

    public JdoEntityFacetFactory() {
        super(ImmutableEnumSet.of(FeatureType.OBJECT));
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();

        val entityAnnotation = Annotations.getAnnotation(cls, PersistenceCapable.class);
        if (entityAnnotation == null) {
            return;
        }
        
        val facetHolder = processClassContext.getFacetHolder();
        val serviceRegistry = super.getMetaModelContext().getServiceRegistry();
        val jdoEntityFacet = new JdoEntityFacet(facetHolder, cls, serviceRegistry);
            
        addFacet(jdoEntityFacet);
    }
    
    // -- 
    
    public static class JdoEntityFacet
    extends FacetAbstract
    implements EntityFacet {
        
        private final static _Longs.Range NON_NEGATIVE_INTS = _Longs.rangeClosed(0L, Integer.MAX_VALUE);

        private final Class<?> entityClass;
        private final ServiceRegistry serviceRegistry;
        
        protected JdoEntityFacet(
                final FacetHolder holder,
                final Class<?> entityClass, 
                final @NonNull ServiceRegistry serviceRegistry) {
            
            super(EntityFacet.class, holder);
            this.entityClass = entityClass;
            this.serviceRegistry = serviceRegistry;
        }
        
        @Override public boolean isDerived() { return false;}
        @Override public boolean isFallback() { return false;}
        @Override public boolean alwaysReplace() { return true;}
        
        // -- ENTITY FACET 

        @Override
        public String identifierFor(ObjectSpecification spec, Object pojo) {

            if(pojo==null) {
                throw _Exceptions.illegalArgument(
                        "The persistence layer cannot identify a pojo that is null (given type %s)",
                        spec.getCorrespondingClass().getName());
            }
            
            if(!spec.isEntity()) {
                throw _Exceptions.illegalArgument(
                        "The persistence layer does not recognize given type %s",
                        pojo.getClass().getName());
            }

            val persistenceManager = getPersistenceManager();
            val primaryKey = persistenceManager.getObjectId(pojo);
            
            if(primaryKey==null) {
                throw _Exceptions.illegalArgument(
                        "The persistence layer does not recognize given object of type %s, "
                        + "meaning the object has no identifier that associates it with the persistence layer. "
                        + "(most likely, because the object is detached, eg. was not persisted after being new-ed up)", 
                        pojo.getClass().getName());
            }
            
            return getObjectIdSerializer().stringify(primaryKey);

        }

        @Override
        public ManagedObject fetchByIdentifier(
                final @NonNull ObjectSpecification entitySpec, 
                final @NonNull String identifier) {
            
            val primaryKey = getObjectIdSerializer().parse(identifier);
            val persistenceManager = getPersistenceManager();
            val entity = persistenceManager.getObjectById(entityClass, primaryKey); 
            
            return ManagedObject.of(entitySpec, entity);
        }

        @Override
        public Can<ManagedObject> fetchByQuery(ObjectSpecification spec, Query<?> query) {
            
            final long rangeLower = query.getStart();
            final long rangeUpper = query.getCount() == Query.UNLIMITED_COUNT
                    ? (long) Integer.MAX_VALUE
                    : (long) NON_NEGATIVE_INTS.bounded(
                        BigInteger.valueOf(query.getStart())
                        .add(BigInteger.valueOf(query.getCount()))
                        .longValueExact());
            
            if(query instanceof AllInstancesQuery) {

                val queryFindAllInstances = (AllInstancesQuery<?>) query;
                val queryEntityType = queryFindAllInstances.getResultType();
                
                // guard against misuse
                if(!entityClass.isAssignableFrom(queryEntityType)) {
                    throw _Exceptions.unexpectedCodeReach();
                }

                val persistenceManager = getPersistenceManager();
                
                val typedQuery = persistenceManager.newJDOQLTypedQuery(entityClass)
                        .range(rangeLower, rangeUpper);
                
                return _NullSafe.stream(typedQuery.executeList())
                    .map(entity->ManagedObject.of(spec, entity))
                    .collect(Can.toCan());
                
            } else if(query instanceof NamedQuery) {
                
                val applibNamedQuery = (NamedQuery<?>) query;
                val queryResultType = applibNamedQuery.getResultType();
                
                val persistenceManager = getPersistenceManager();
                
                val namedParams = _Maps.<String, Object>newHashMap();
                val namedQuery = persistenceManager.newNamedQuery(queryResultType, applibNamedQuery.getName())
                        .setNamedParameters(namedParams)
                        .range(rangeLower, rangeUpper);
                
                applibNamedQuery
                    .getParametersByName()
                    .forEach(namedParams::put);

                return _NullSafe.stream(namedQuery.executeList())
                        .map(entity->ManagedObject.of(spec, entity))
                        .collect(Can.toCan());
                
            }
            
            throw _Exceptions.unsupportedOperation(
                    "Support for Query of type %s not implemented.", query.getClass());
        }

        @Override
        public void persist(ObjectSpecification spec, Object pojo) {
            if(pojo==null) {
                return; // nothing to do
            }
            
            // guard against misuse
            if(!entityClass.isAssignableFrom(pojo.getClass())) {
                throw _Exceptions.unexpectedCodeReach();
            }
            
            val persistenceManager = getPersistenceManager();
            persistenceManager.makePersistent(pojo);
        }

        @Override
        public void refresh(Object pojo) {
            if(pojo==null) {
                return; // nothing to do
            }
            
            // guard against misuse
            if(!entityClass.isAssignableFrom(pojo.getClass())) {
                throw _Exceptions.unexpectedCodeReach();
            }
            
            val persistenceManager = getPersistenceManager();
            persistenceManager.refresh(pojo);
        }

        @Override
        public void delete(ObjectSpecification spec, Object pojo) {
            
            if(pojo==null) {
                return; // nothing to do
            }
            
            // guard against misuse
            if(!entityClass.isAssignableFrom(pojo.getClass())) {
                throw _Exceptions.unexpectedCodeReach();
            }
            
            val persistenceManager = getPersistenceManager();
            persistenceManager.deletePersistent(pojo);
        }

        @Override
        public EntityState getEntityState(Object pojo) {
            
            if(pojo==null) {
                return EntityState.NOT_PERSISTABLE;
            }
            
            // guard against misuse
            if(!entityClass.isAssignableFrom(pojo.getClass())) {
                //throw _Exceptions.unexpectedCodeReach();
                return EntityState.NOT_PERSISTABLE;
            }
            
            return getJdoEntityStateProvider().getEntityState(pojo);
        }

        @Override
        public boolean isProxyEnhancement(Method method) {
            return false;
        }

        @Override
        public <T> T detach(T pojo) {
            val persistenceManager = getPersistenceManager();
            return persistenceManager.detachCopy(pojo);
        }
        
        // -- OBJECT ID SERIALIZATION
        
        private final _Lazy<JdoObjectIdSerializer<Object>> objectIdSerializerRef = _Lazy.threadSafe(this::createObjectIdSerializer);
        
        protected JdoObjectIdSerializer<Object> getObjectIdSerializer() {
            return objectIdSerializerRef.get();
        }
        
        protected JdoObjectIdSerializer<Object> createObjectIdSerializer() {
            final Class<?> primaryKeyType = getPersistenceManager().getObjectIdClass(entityClass);
            return _Casts.uncheckedCast(createJdoObjectIdSerializer(primaryKeyType, serviceRegistry));
        }
        
        // -- DEPENDENCIES
        
        protected JdoFacetContext getJdoEntityStateProvider() {
            return serviceRegistry
                    .lookupServiceElseFail(JdoFacetContext.class);
        }
        
        protected PersistenceManager getPersistenceManager() {
            return getPersistenceManagerFactory().getPersistenceManager();
        }
        
        protected PersistenceManagerFactory getPersistenceManagerFactory() {
            return serviceRegistry
                    .lookupServiceElseFail(JdoSupportService.class)
                    .getPersistenceManagerFactory();
        }
        
    }
    
    // -- HELPER - OBJECT ID SERIALIZATION
    

    @SuppressWarnings("rawtypes")
    private static JdoObjectIdSerializer createJdoObjectIdSerializer(
            final @NonNull Class<?> primaryKeyType,
            final @NonNull ServiceRegistry serviceRegistry) {
        
        
        // not strictly required, but to have simpler entity URLs for simple primary-key types
        {
            if(primaryKeyType.equals(Long.class)
                    || primaryKeyType.equals(long.class)) {
                return new LongIdSerializer();
            }
            if(primaryKeyType.equals(Integer.class)
                    || primaryKeyType.equals(int.class)) {
                return new IntegerIdSerializer();
            }
            if(primaryKeyType.equals(Short.class)
                    || primaryKeyType.equals(short.class)) {
                return new ShortIdSerializer();
            }
            if(primaryKeyType.equals(Byte.class)
                    || primaryKeyType.equals(byte.class)) {
                return new ByteIdSerializer();
            }
        }
        
        val codec = serviceRegistry.lookupServiceElseFail(UrlEncodingService.class);
        val serializer = serviceRegistry.lookupServiceElseFail(SerializingAdapter.class);
        return new JdoObjectIdSerializerUsingMementos<>(primaryKeyType, codec, serializer);
    }
    
    
    @RequiredArgsConstructor
    private static abstract class JdoObjectIdSerializer<T> {
        @SuppressWarnings("unused")
        final Class<T> primaryKeyType;
        abstract String stringify(T id);
        abstract T parse(String stringifiedPrimaryKey);
    }
    
    private static class LongIdSerializer extends JdoObjectIdSerializer<Long> {
        public LongIdSerializer() { super(Long.class); }
        @Override String stringify(Long id) { return id.toString(); }
        @Override Long parse(String stringifiedPrimaryKey) { return Long.parseLong(stringifiedPrimaryKey); }
    }
    private static class IntegerIdSerializer extends JdoObjectIdSerializer<Integer> {
        public IntegerIdSerializer() { super(Integer.class); }
        @Override String stringify(Integer id) { return id.toString(); }
        @Override Integer parse(String stringifiedPrimaryKey) { return Integer.parseInt(stringifiedPrimaryKey); }
    }
    private static class ShortIdSerializer extends JdoObjectIdSerializer<Short> {
        public ShortIdSerializer() { super(Short.class); }
        @Override String stringify(Short id) { return id.toString(); }
        @Override Short parse(String stringifiedPrimaryKey) { return Short.parseShort(stringifiedPrimaryKey); }
    }
    private static class ByteIdSerializer extends JdoObjectIdSerializer<Byte> {
        public ByteIdSerializer() { super(Byte.class); }
        @Override String stringify(Byte id) { return id.toString(); }
        @Override Byte parse(String stringifiedPrimaryKey) { return Byte.parseByte(stringifiedPrimaryKey); }
    }
    
    private static class JdoObjectIdSerializerUsingMementos<T> extends JdoObjectIdSerializer<T> {
        private final UrlEncodingService codec;
        private final SerializingAdapter serializer;
        
        public JdoObjectIdSerializerUsingMementos(
                final @NonNull Class<T> primaryKeyType, 
                final @NonNull UrlEncodingService codec,
                final @NonNull SerializingAdapter serializer) {
            super(primaryKeyType);
            this.codec = codec;
            this.serializer = serializer;
        }
       
        public String stringify(Object id) {
            return newMemento().put("id", id).asString();
        }
        
        public T parse(final String stringifiedPrimaryKey) {
            if(_Strings.isEmpty(stringifiedPrimaryKey)) {
                return null;
            }
            return _Casts.uncheckedCast(parseMemento(stringifiedPrimaryKey).get("id", Object.class));
        }
       
        // -- HELPER

        private _Mementos.Memento newMemento(){
            return _Mementos.create(codec, serializer);
        }

        private _Mementos.Memento parseMemento(String input){
            return _Mementos.parse(codec, serializer, input);
        }
        
    }
    


}
