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
package org.apache.causeway.persistence.jdbc.metamodel;

import java.lang.reflect.Method;
import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.query.AllInstancesQuery;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData.PersistenceStack;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.idstringifier.IdStringifierLookupService;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
class JdbcEntityFacet
extends FacetAbstract
implements EntityFacet {

    // self managed injections via constructor
    @Inject private RelationalMappingContext mappingContext;
    @Inject private IdStringifierLookupService idStringifierLookupService;
    @Inject private JdbcAggregateTemplate jdbcAggregateTemplate;

    private final Class<?> entityClass;
    private PrimaryKeyType<?> primaryKeyType;
    private RelationalPersistentEntity<?> persistentEntity;

    @Getter
    private final EntityOrmMetadata ormMetadata;

    protected JdbcEntityFacet(
            final FacetHolder holder,
            final Class<?> entityClass) {
        super(EntityFacet.class, holder, Precedence.HIGH);
        getServiceInjector().injectServicesInto(this);

        this.entityClass = entityClass;
        this.persistentEntity = _MetadataUtil.jdbcEntityMetamodel(mappingContext, entityClass);
        this.ormMetadata = _MetadataUtil.ormMetadataFor(persistentEntity);
        this.primaryKeyType = idStringifierLookupService
            .primaryKeyTypeFor(entityClass, getPrimaryKeyType());
    }

    // -- ENTITY FACET

    @Override
    public PersistenceStack getPersistenceStack() {
        return PersistenceStack.JDBC;
    }

    @Override
    public Optional<String> identifierFor(final @Nullable Object pojo) {
        return primaryKey(pojo)
                .map(primaryKeyType::enstringWithCast);
    }

    @Override
    public Bookmark validateBookmark(final @NonNull Bookmark bookmark) {
        _Assert.assertNotNull(primaryKeyType.destring(bookmark.getIdentifier()));
        return bookmark;
    }

    @Override
    public Optional<Object> fetchByBookmark(final @NonNull Bookmark bookmark) {
        log.debug("fetchEntity; bookmark={}", bookmark);
        
        var primaryKey = primaryKeyType.destring(bookmark.getIdentifier());
        var entityPojo = jdbcAggregateTemplate.findById(primaryKey, entityClass);
        
        return Optional.ofNullable(entityPojo);
    }

    private Class<?> getPrimaryKeyType() {
        return getOrmMetadata().primaryKeyClass();
    }

    @Override
    public Can<ManagedObject> fetchByQuery(final Query<?> query) {

        var range = query.getRange();
        
        if (query instanceof AllInstancesQuery queryFindAllInstances) {

            var queryEntityType = queryFindAllInstances.getResultType();

            // guard against misuse
            _Assert.assertTypeIsInstanceOf(queryEntityType, entityClass);

            var springQuery = org.springframework.data.relational.core.query.Query.query(Criteria.empty());
            if (range.hasOffset()) {
                springQuery = springQuery.offset(range.getStartAsInt());
            }
            if (range.hasLimit()) {
                springQuery = springQuery.limit(range.getLimitAsInt());
            }
            
            var list = jdbcAggregateTemplate.findAll(springQuery, entityClass);

            var entitySpec = getEntitySpecification();
            return _NullSafe.stream(list)
                            .map(entity -> ManagedObject.adaptSingular(entitySpec, entity))
                            .collect(Can.toCan());

        }

        throw _Exceptions.unsupportedOperation(
                "Support for Query of type %s not implemented.", query.getClass());
    }

    @Override
    public void persist(final Object pojo) {
        if (pojo == null) {
            return; // nothing to do
        }

        // guard against misuse
        _Assert.assertNullableObjectIsInstanceOf(pojo, entityClass);

        log.debug("about to persist entity {}", pojo);

        jdbcAggregateTemplate.save(pojo);
    }

    @Override
    public void refresh(final Object pojo) {
        if(!isEntityPojo(pojo)) return; // nothing to do
        
        //TODO[causeway-persistence-jdbc-CAUSEWAY-3849] refresh / probably not implementable
        throw _Exceptions.notImplemented();
    }

    @Override
    public void delete(final Object pojo) {
        if(!isEntityPojo(pojo)) return; // nothing to do
        jdbcAggregateTemplate.delete(pojo);
    }

    @Override
    public EntityState getEntityState(final Object pojo) {
        if(!isEntityPojo(pojo)) return EntityState.NOT_PERSISTABLE;
        var primaryKey = primaryKey(pojo);
        
        return !primaryKey.isPresent()
            ? EntityState.SNAPSHOT_NO_OID
            : jdbcAggregateTemplate.existsById(primaryKey.get(), entityClass)
                ? EntityState.SNAPSHOT
                : EntityState.TRANSIENT_OR_REMOVED;
    }

    @Override
    public Object versionOf(final Object pojo) {
        //TODO[causeway-persistence-jdbc-CAUSEWAY-3849] versionOf
//        if (getEntityState(pojo).isAttached()) {
//            if (pojo instanceof HasVersion) {
//                return ((HasVersion<?>)pojo).getVersion();
//            }
//        }
        return null;
    }

    @Override
    public boolean isProxyEnhancement(final Method method) {
        return false;
    }

    @Override
    public <T> T detach(final T pojo) {
        // no-op / Spring Data JDBC has no session management
        return pojo;
    }
    
    // -- HELPER
    
    // simple guard
    private boolean isEntityPojo(final Object pojo) {
        return pojo != null
                && entityClass.isAssignableFrom(pojo.getClass());
    }
    
    private Optional<Object> primaryKey(final @Nullable Object pojo) {
        if(!isEntityPojo(pojo)) return Optional.empty();
        
        var idProperty = persistentEntity.getRequiredIdProperty();
        var propertyAccessor = persistentEntity.getPropertyAccessor(pojo);
        var primaryKeyIfAny = propertyAccessor.getProperty(idProperty);

        return Optional.ofNullable(primaryKeyIfAny);
    }

}
