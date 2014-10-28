/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.spec;

import java.util.Collection;
import java.util.List;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;

public interface SpecificationLoader extends Injectable, InjectorMethodEvaluator {

    ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId);

    /**
     * Return the specification for the specified class of object.
     * 
     * <p>
     * It is possible for this method to return <tt>null</tt>, for example if
     * the configured {@link org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor}
     * has filtered out the class.
     */
    ObjectSpecification loadSpecification(String fullyQualifiedClassName);

    /**
     * @see #loadSpecification(String)
     */
    ObjectSpecification loadSpecification(Class<?> cls);

    /**
     * Loads the specifications of the specified types.
     */
    boolean loadSpecifications(List<Class<?>> typesToLoad);

    /**
     * Loads the specifications of the specified types except the one specified
     * (to prevent an infinite loop).
     */
    boolean loadSpecifications(List<Class<?>> typesToLoad, final Class<?> typeToIgnore);


    /**
     * Typically does not need to be called, but is available for {@link FacetFactory}s to force
     * early introspection of referenced specs in certain circumstances.
     * 
     * <p>
     * Originally introduced to support {@link AutoCompleteFacet}.
     */
    ObjectSpecification introspectIfRequired(final ObjectSpecification spec);
    
    
    Collection<ObjectSpecification> allSpecifications();


    List<Class<?>> getServiceClasses();
    
    /**
     * Whether this class has been loaded.
     */
    boolean loaded(Class<?> cls);

    /**
     * @see #loaded(Class).
     */
    boolean loaded(String fullyQualifiedClassName);


    void invalidateCache(Class<?> domainClass);



}
