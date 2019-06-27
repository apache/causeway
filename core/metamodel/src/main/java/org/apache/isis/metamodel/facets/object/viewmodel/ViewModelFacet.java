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

package org.apache.isis.metamodel.facets.object.viewmodel;

import java.util.function.Function;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.spec.ObjectSpecification;

/**
 * Indicates that this class is either a view model (for the UI/application layer) or a recreatable domain object of
 * some sort (for the domain layer, that is: an external or synthetic entity).
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to
 * applying either {@link org.apache.isis.applib.annotation.ViewModel} annotation or
 * {@link org.apache.isis.applib.ViewModel} interface (for a view model), or by annotating
 *
 * <p>
 *     Note: this facet is called &quot;ViewModelFacet&quot; for historical reasons; a better name would be
 *     &quot;RecreatableObjectFacet&quot;.  The old name has been retained only to avoid unnecessarily breaking
 *     some add-ons (eg Isis Addons Excel Module) that use this facet.
 * </p>
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
    		ObjectSpecification spec, 
    		String mementoStr, 
    		Function<ObjectSpecification, Object> factory) {
    	
    	final Object viewModelPojo;
		if(getRecreationMechanism().isInitializes()) {
			viewModelPojo = factory.apply(spec);
			initialize(viewModelPojo, mementoStr);
		} else {
			viewModelPojo = instantiate(spec.getCorrespondingClass(), mementoStr);
		}
		return viewModelPojo;
    }
    
    /**
     * Whether this implementation supports the recreation of objects by {@link RecreationMechanism#INSTANTIATES instantiating} (and implicitly also initializing) a new pojo, or by {@link RecreationMechanism#INITIALIZES initializing} a pojo created and passed to it by the framework.
     *
     * <p>
     *     Determines whether the framework then calls {@link #instantiate(Class, String)} or if it calls {@link #initialize(Object, String)}.
     * </p>
     */
    RecreationMechanism getRecreationMechanism();

    /**
     * Will be called if {@link #getRecreationMechanism()} is {@link RecreationMechanism#INITIALIZES}.
     */
    void initialize(Object pojo, String memento);

    /**
     * Will be called only if {@link #getRecreationMechanism()} is {@link RecreationMechanism#INSTANTIATES}.
     */
    Object instantiate(final Class<?> viewModelClass, String memento);
    
    /**
     * Obtain a memento of the pojo, which can then be used to reinstantiate (either by {@link #instantiate(Class, String)} or {@link #initialize(Object, String)}) subsequently.
     */
    String memento(Object pojo);

    /**
     * Whether {@link #clone(Object)} can be called.
     */
    boolean isCloneable(Object pojo);

    /**
     * Whether can infer the view model is immutable or not.
     *
     * <p>
     *     Equivalent to {@link #isCloneable(Object)}, but at the class rather than object level.
     * </p>
     */
    boolean isImplicitlyImmutable();

    /**
     * View models are implicitly immutable (their state is determined by their {@link #memento(Object)}), so this
     * method allows the framework to clone an existing view model to mutate it, thereby simulating editable
     * view models.
     */
    Object clone(Object pojo);

}
