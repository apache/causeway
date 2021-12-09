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
package org.apache.isis.core.metamodel.facets.object.viewmodel;

import java.util.function.Function;

import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Indicates that this class is either a view model (for the UI/application layer) or a recreatable domain object of
 * some sort (for the domain layer, that is: an external or synthetic entity).
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to
 * applying either {@link org.apache.isis.applib.annotation.DomainObject} annotation with
 * {@link Nature} = {@link Nature#VIEW_MODEL}  or
 * {@link org.apache.isis.applib.ViewModel} interface (for a view model), or by annotating
 *
 * <p>
 * Note: this facet is called &quot;ViewModelFacet&quot; for historical reasons; a better name would be
 * &quot;RecreatableObjectFacet&quot;.  The old name has been retained only to avoid unnecessarily breaking
 * some add-ons (eg Isis Addons Excel Module) that use this facet.
 */
public interface ViewModelFacet extends Facet {


    public enum RecreationMechanism {
        /**
         * Instantiates a new instance and then populates
         */
        INSTANTIATES,
        /**
         * Initializes an instance already created by the framework
         */
        INITIALIZES;

        public boolean isInstantiates() {
            return this == INSTANTIATES;
        }

        public boolean isInitializes() {
            return this == INITIALIZES;
        }

    }

    default Object createViewModelPojo(
            final ObjectSpecification spec,
            final Bookmark bookmark,
            final Function<ObjectSpecification, Object> viewModelPojoFactory) {

        final Object viewModelPojo;
        if(getRecreationMechanism().isInitializes()) {
            viewModelPojo = viewModelPojoFactory.apply(spec);
            initialize(viewModelPojo, bookmark);
        } else {
            viewModelPojo = instantiate(spec.getCorrespondingClass(), bookmark);
        }
        return viewModelPojo;
    }

    /**
     * Whether this implementation supports the recreation of objects by {@link RecreationMechanism#INSTANTIATES instantiating} (and implicitly also initializing) a new pojo, or by {@link RecreationMechanism#INITIALIZES initializing} a pojo created and passed to it by the framework.
     *
     * <p>
     *     Determines whether the framework then calls
     *     {@link #instantiate(Class, Bookmark)} or if it calls {@link #initialize(Object, Bookmark)}.
     * </p>
     */
    RecreationMechanism getRecreationMechanism();

    /**
     * Will be called if {@link #getRecreationMechanism()} is {@link RecreationMechanism#INITIALIZES}.
     */
    void initialize(Object viewModelPojo, Bookmark bookmark);

    /**
     * Will be called only if {@link #getRecreationMechanism()} is {@link RecreationMechanism#INSTANTIATES}.
     */
    Object instantiate(final Class<?> viewModelClass, Bookmark bookmark);

    /**
     * Obtain a memento of the pojo, which can then be used to reinstantiate
     * (either by {@link #instantiate(Class, Bookmark)} or {@link #initialize(Object, Bookmark)}) subsequently.
     */
    Bookmark serializeToBookmark(ManagedObject managedObject);

    /**
     * Governs whether on start of any AJAX request, the viewmodel needs to reload,
     * so any contained entities end up attached.
     */
    default boolean containsEntities() { return false; }

}
