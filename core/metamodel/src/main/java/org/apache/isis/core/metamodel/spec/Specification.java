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
package org.apache.isis.core.metamodel.spec;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.HasFacetHolder;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Base interface for elements of the metamodel.
 *
 * <p>
 * The most significant sub-interfaces of this are {@link ObjectSpecification}
 * and {@link ObjectFeature} (which brings in {@link ObjectMember} and
 * {@link ObjectActionParameter}.
 *
 * <p>
 * Introduces so that viewers can deal with abstract Instances of said.
 *
 */
public interface Specification extends HasFacetHolder {

    FeatureType getFeatureType();

    /**
     * The element specification of the associated type.
     * <ul>
     * <li>for any {@link ObjectSpecification type}, will return itself,
     * unless a {@link TypeOfFacet} is present
     * </li>
     * <li>for a {@link OneToOneAssociation property}, will return the
     * {@link ObjectSpecification} of the type that the <i>getter</i> returns
     * </li><li>for a {@link OneToManyAssociation collection} it will be the type of
     * element the collection holds (not the type of collection)
     * </li><li>for an {@link ObjectAction action} will return {@link ObjectAction#getReturnType()}
     * </li><li>for an {@link ObjectActionParameter action parameter}, will return the element type of
     * the parameter (this is the parameter type itself if scalar, otherwise the type of
     * element the collection (non-scalar) parameter holds)
     * </li>
     * </ul>
     * @since 2.0
     */
    ObjectSpecification getElementType();

}
