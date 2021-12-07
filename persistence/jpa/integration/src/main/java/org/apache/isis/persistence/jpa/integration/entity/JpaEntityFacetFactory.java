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
package org.apache.isis.persistence.jpa.integration.entity;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.EntityType;

import org.springframework.data.jpa.repository.JpaContext;

import org.apache.isis.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.isis.applib.query.AllInstancesQuery;
import org.apache.isis.applib.query.NamedQuery;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.object.entity.PersistenceStandard;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JpaEntityFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public JpaEntityFacetFactory(final MetaModelContext mmc) {
        super(mmc, ImmutableEnumSet.of(FeatureType.OBJECT));
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();

        val facetHolder = processClassContext.getFacetHolder();

        val entityAnnotation = processClassContext.synthesizeOnType(Entity.class);
        if(!entityAnnotation.isPresent()) {
            return;
        }

        addFacet(
                new JpaEntityFacet(facetHolder, cls, getServiceRegistry()));
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

            super(EntityFacet.class, holder, Facet.Precedence.HIGH);
            this.entityClass = entityClass;
            this.serviceRegistry = serviceRegistry;
        }

        // -- ENTITY FACET

        @Override
        public PersistenceStandard getPersistenceStandard() {
            return PersistenceStandard.JPA;
        }

        @Override
        public String identifierFor(final ObjectSpecification spec, final Object pojo) {

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
                final @NonNull Bookmark bookmark) {

            _Assert.assertTrue(entitySpec.isEntity());

            log.debug("fetchEntity; bookmark={}", bookmark);

            val primaryKey = getObjectIdSerializer().parse(bookmark.getIdentifier());
            val entityManager = getEntityManager();
            val entityPojo = entityManager.find(entityClass, primaryKey);

            if (entityPojo == null) {
                throw new ObjectNotFoundException(""+bookmark);
            }

            return ManagedObject.bookmarked(entitySpec, entityPojo, bookmark);
        }

        @Override
        public Can<ManagedObject> fetchByQuery(final ObjectSpecification spec, final Query<?> query) {

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
        public void persist(final ObjectSpecification spec, final Object pojo) {
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
        public void refresh(final Object pojo) {
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
        public void delete(final ObjectSpecification spec, final Object pojo) {

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
        public EntityState getEntityState(final Object pojo) {

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

            val primaryKey = getPersistenceUnitUtil(entityManager).getIdentifier(pojo);
            if(primaryKey == null) {
                return EntityState.PERSISTABLE_DETACHED; // an optimization, not strictly required
            }

            //XXX whether DETACHED or REMOVED is currently undecidable (JPA)
            return EntityState.PERSISTABLE_DETACHED;
        }

        @Override
        public boolean isProxyEnhancement(final Method method) {
            return false;
        }

        @Override
        public <T> T detach(final T pojo) {
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

        protected PersistenceUnitUtil getPersistenceUnitUtil(final EntityManager entityManager) {
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
        @Override String stringify(final Long id) { return id.toString(); }
        @Override Long parse(final String stringifiedPrimaryKey) { return Long.parseLong(stringifiedPrimaryKey); }
    }
    private static class IntegerIdSerializer extends JpaObjectIdSerializer<Integer> {
        public IntegerIdSerializer() { super(Integer.class); }
        @Override String stringify(final Integer id) { return id.toString(); }
        @Override Integer parse(final String stringifiedPrimaryKey) { return Integer.parseInt(stringifiedPrimaryKey); }
    }
    private static class ShortIdSerializer extends JpaObjectIdSerializer<Short> {
        public ShortIdSerializer() { super(Short.class); }
        @Override String stringify(final Short id) { return id.toString(); }
        @Override Short parse(final String stringifiedPrimaryKey) { return Short.parseShort(stringifiedPrimaryKey); }
    }
    private static class ByteIdSerializer extends JpaObjectIdSerializer<Byte> {
        public ByteIdSerializer() { super(Byte.class); }
        @Override String stringify(final Byte id) { return id.toString(); }
        @Override Byte parse(final String stringifiedPrimaryKey) { return Byte.parseByte(stringifiedPrimaryKey); }
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

        @Override
        public String stringify(final Object id) {
            return newMemento().put("id", id).asString();
        }

        @Override
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

        private _Mementos.Memento parseMemento(final String input){
            return _Mementos.parse(codec, serializer, input);
        }

    }



}
