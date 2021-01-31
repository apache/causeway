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
package org.apache.isis.persistence.jpa.integration.metamodel;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.EntityType;

import org.springframework.data.jpa.repository.JpaContext;

import org.apache.isis.applib.exceptions.unrecoverable.ObjectNotFoundException;
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
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JpaEntityFacetFactory extends FacetFactoryAbstract {

    public JpaEntityFacetFactory() {
        super(ImmutableEnumSet.of(FeatureType.OBJECT));
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        
        val facetHolder = processClassContext.getFacetHolder();
        
        val entityAnnotation = processClassContext.synthesizeOnType(Entity.class);
        if(!entityAnnotation.isPresent()) {
            return;
        }
        
        val serviceRegistry = super.getMetaModelContext().getServiceRegistry();
        val jpaEntityFacet = new JpaEntityFacet(facetHolder, cls, serviceRegistry);
            
        addFacet(jpaEntityFacet);
    }
    
    // -- 
    
    public static class JpaEntityFacet
    extends FacetAbstract
    implements EntityFacet {

        private final Class<?> entityClass;
        private final ServiceRegistry serviceRegistry;
        
        protected JpaEntityFacet(
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

            val entityManager = getEntityManager();
            val persistenceUnitUtil = getPersistenceUnitUtil(entityManager);
            val primaryKey = persistenceUnitUtil.getIdentifier(pojo);
            
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
            val entityManager = getEntityManager();
            val entityPojo = entityManager.find(entityClass, primaryKey);
            
            if (entityPojo == null) {
                throw new ObjectNotFoundException(""+identifier);
            }
            
            return ManagedObject.of(entitySpec, entityPojo);
        }

        @Override
        public Can<ManagedObject> fetchByQuery(ObjectSpecification spec, Query<?> query) {
            
            val range = query.getRange();
            
            if(query instanceof AllInstancesQuery) {

                val queryFindAllInstances = (AllInstancesQuery<?>) query;
                val queryEntityType = queryFindAllInstances.getResultType();
                
                // guard against misuse
                if(!entityClass.isAssignableFrom(queryEntityType)) {
                    throw _Exceptions.unexpectedCodeReach();
                }

                val entityManager = getEntityManager();
                
                val cb = entityManager.getCriteriaBuilder();
                val cr = cb.createQuery(entityClass);

                cr.select(_Casts.uncheckedCast(cr.from(entityClass)));
                
                val typedQuery = entityManager
                        .createQuery(cr);
                
                if(range.hasOffset()) {
                    typedQuery.setFirstResult(range.getStartAsInt());
                }
                if(range.hasLimit()) {
                    typedQuery.setMaxResults(range.getLimitAsInt());
                }
                
                return Can.ofStream(
                    typedQuery.getResultStream()
                    .map(entity->ManagedObject.of(spec, entity)));
                
            } else if(query instanceof NamedQuery) {
                
                val applibNamedQuery = (NamedQuery<?>) query;
                val queryResultType = applibNamedQuery.getResultType();
                
                val entityManager = getEntityManager();
                
                val namedQuery = entityManager
                        .createNamedQuery(applibNamedQuery.getName(), queryResultType);
                
                if(range.hasOffset()) {
                    namedQuery.setFirstResult(range.getStartAsInt());
                }
                if(range.hasLimit()) {
                    namedQuery.setMaxResults(range.getLimitAsInt());
                }
                
                applibNamedQuery
                    .getParametersByName()
                    .forEach((paramName, paramValue)->
                        namedQuery.setParameter(paramName, paramValue));

                return Can.ofStream(
                        namedQuery.getResultStream()
                        .map(entity->ManagedObject.of(spec, entity)));
                
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
            
            val entityManager = getEntityManager();
            
            log.debug("about to persist entity {}", pojo);

            entityManager.persist(pojo);
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
            
            val entityManager = getEntityManager();
            entityManager.refresh(pojo);
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
            
            val entityManager = getEntityManager();
            entityManager.remove(pojo);
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
            
            val entityManager = getEntityManager();
            
            if(entityManager.contains(pojo)) {
                return EntityState.PERSISTABLE_ATTACHED;
            }

            //TODO[2033] how to determine whether deleted? (even relevant?)
//            if(isDeleted) {
//                return EntityState.PERSISTABLE_DESTROYED;
//            }
            return EntityState.PERSISTABLE_DETACHED;
        }

        @Override
        public boolean isProxyEnhancement(Method method) {
            return false;
        }

        @Override
        public <T> T detach(T pojo) {
            getEntityManager().detach(pojo);
            return pojo;
        }
        
        // -- JPA METAMODEL
        
        private final _Lazy<Optional<EntityType<?>>> jpaEntityTypeRef = _Lazy.threadSafe(this::queryJpaMetamodel);
        
        /** get the JPA meta-model associated with this (corresponding) entity*/
        private EntityType<?> getJpaEntityType() {
            return jpaEntityTypeRef.get().orElseThrow(_Exceptions::noSuchElement);
        }
        
        /** find the JPA meta-model associated with this (corresponding) entity*/ 
        private Optional<EntityType<?>> queryJpaMetamodel() {
            return getEntityManager().getMetamodel().getEntities()
            .stream()
            .filter(type->type.getJavaType().equals(entityClass))
            .findFirst();
        }
        
        // -- OBJECT ID SERIALIZATION
        
        private final _Lazy<JpaObjectIdSerializer<Object>> objectIdSerializerRef = _Lazy.threadSafe(this::createObjectIdSerializer);
        
        protected JpaObjectIdSerializer<Object> getObjectIdSerializer() {
            return objectIdSerializerRef.get();
        }
        
        protected JpaObjectIdSerializer<Object> createObjectIdSerializer() {
            val primaryKeyType = getJpaEntityType().getIdType().getJavaType();
            return _Casts.uncheckedCast(createJpaObjectIdSerializer(primaryKeyType, serviceRegistry));
        }
        
        // -- DEPENDENCIES
        
        protected JpaContext getJpaContext() {
            return serviceRegistry.lookupServiceElseFail(JpaContext.class);
        }
        
        protected EntityManager getEntityManager() {
            return getJpaContext().getEntityManagerByManagedType(entityClass);
        }
        
        protected PersistenceUnitUtil getPersistenceUnitUtil(EntityManager entityManager) {
            return entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        }
        
    }
    
    // -- HELPER - OBJECT ID SERIALIZATION
    

    @SuppressWarnings("rawtypes")
    private static JpaObjectIdSerializer createJpaObjectIdSerializer(
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
        return new JpaObjectIdSerializerUsingMementos<>(primaryKeyType, codec, serializer);
    }
    
    
    @RequiredArgsConstructor
    private static abstract class JpaObjectIdSerializer<T> {
        @SuppressWarnings("unused")
        final Class<T> primaryKeyType;
        abstract String stringify(T id);
        abstract T parse(String stringifiedPrimaryKey);
    }
    
    private static class LongIdSerializer extends JpaObjectIdSerializer<Long> {
        public LongIdSerializer() { super(Long.class); }
        @Override String stringify(Long id) { return id.toString(); }
        @Override Long parse(String stringifiedPrimaryKey) { return Long.parseLong(stringifiedPrimaryKey); }
    }
    private static class IntegerIdSerializer extends JpaObjectIdSerializer<Integer> {
        public IntegerIdSerializer() { super(Integer.class); }
        @Override String stringify(Integer id) { return id.toString(); }
        @Override Integer parse(String stringifiedPrimaryKey) { return Integer.parseInt(stringifiedPrimaryKey); }
    }
    private static class ShortIdSerializer extends JpaObjectIdSerializer<Short> {
        public ShortIdSerializer() { super(Short.class); }
        @Override String stringify(Short id) { return id.toString(); }
        @Override Short parse(String stringifiedPrimaryKey) { return Short.parseShort(stringifiedPrimaryKey); }
    }
    private static class ByteIdSerializer extends JpaObjectIdSerializer<Byte> {
        public ByteIdSerializer() { super(Byte.class); }
        @Override String stringify(Byte id) { return id.toString(); }
        @Override Byte parse(String stringifiedPrimaryKey) { return Byte.parseByte(stringifiedPrimaryKey); }
    }
    
    private static class JpaObjectIdSerializerUsingMementos<T> extends JpaObjectIdSerializer<T> {
        private final UrlEncodingService codec;
        private final SerializingAdapter serializer;
        
        public JpaObjectIdSerializerUsingMementos(
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
