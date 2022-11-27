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
package org.apache.causeway.core.metamodel.facets.object.viewmodel;

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * Indicates that this class is either a view model (for the UI/application layer) or a recreatable domain object of
 * some sort (for the domain layer, that is: an external or synthetic entity).
 *
 * <p>
 * In the standard Apache Causeway Programming Model, corresponds to
 * applying either {@link org.apache.causeway.applib.annotation.DomainObject} annotation with
 * {@link Nature} = {@link Nature#VIEW_MODEL}  or
 * {@link org.apache.causeway.applib.ViewModel} interface (for a view model), or by annotating
 *
 * <p>
 * Note: this facet is called &quot;ViewModelFacet&quot; for historical reasons; a better name would be
 * &quot;RecreatableObjectFacet&quot;.  The old name has been retained only to avoid unnecessarily breaking
 * some add-ons (eg Causeway Addons Excel Module) that use this facet.
 */
public interface ViewModelFacet extends Facet {

    /**
     * Creates a view-model instance from given bookmark if any.
     */
    ManagedObject instantiate(ObjectSpecification spec, final Optional<Bookmark> bookmark);

    /**
     * Obtain a {@link Bookmark} of the pojo, which can then be used to re-instantiate
     * by {@link #instantiate(ObjectSpecification, Optional)} subsequently.
     */
    Bookmark serializeToBookmark(ManagedObject managedObject);

    /**
     * For given view-model pojo resolves injection points, then calls post-construct method(s) if any.
     */
    void initialize(@Nullable Object pojo);

}
