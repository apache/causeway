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
package org.apache.isis.core.metamodel.facets.object.entity;

import java.lang.reflect.Method;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Indicates that this class is managed by a persistence context.
 * @since 2.0
 */
public interface EntityFacet extends Facet {

    String identifierFor(ObjectSpecification spec, Object pojo);

    ManagedObject fetchByIdentifier(ObjectSpecification spec, Bookmark bookmark);
    Can<ManagedObject> fetchByQuery(ObjectSpecification spec, Query<?> query);

    void persist(ObjectSpecification spec, Object pojo);

    void refresh(Object pojo);

    void delete(ObjectSpecification spec, Object pojo);

    EntityState getEntityState(Object pojo);

    /**
     * Whether given method originates from byte code mangling.
     * @param method
     */
    boolean isProxyEnhancement(Method method);

    <T> T detach(T pojo);

    PersistenceStandard getPersistenceStandard();

    // -- JUNIT SUPPORT

    static EntityFacet forTesting(
            final PersistenceStandard persistenceStandard,
            final FacetHolder facetHolder) {
        return new _EntityFacetForTesting(persistenceStandard, facetHolder);
    }

}
