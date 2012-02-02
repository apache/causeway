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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * Base interface for elements of the metamodel.
 * 
 * <p>
 * The most significant subinterfaces of this are {@link ObjectSpecification}
 * and {@link ObjectFeature} (which brings in {@link ObjectMember} and
 * {@link ObjectActionParameter}.
 * 
 * <p>
 * Introduces so that viewers can deal with abstract Instances of said.
 * 
 */
public interface Specification extends IdentifiedHolder {

    FeatureType getFeatureType();

    /**
     * Returns a description of how the member is used - this complements the
     * help text.
     * 
     * @see #getHelp()
     */
    String getDescription();

    /**
     * Return an {@link Instance} of this {@link Specification} with respect to
     * the provided {@link ObjectAdapter}.
     * 
     * <p>
     * For example, if the {@link Specification} is a
     * {@link OneToOneAssociation}, then is an {@link Instance} implementation
     * representing the { {@link ObjectAdapter}/ {@link OneToOneAssociation}
     * tuple.
     * 
     * <p>
     * Implementations are expected to use a double-dispatch back to the
     * provided {@link ObjectAdapter} (passing themselves as a parameter), using
     * {@link ObjectAdapter#getInstance(Specification)}.
     * 
     * <p>
     * Note: this method may throw an {@link UnsupportedOperationException}; see
     * {@link ObjectAdapter#getInstance(Specification)} for details.
     */
    Instance getInstance(final ObjectAdapter adapter);

}
