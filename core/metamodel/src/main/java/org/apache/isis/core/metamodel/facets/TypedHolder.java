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
package org.apache.isis.core.metamodel.facets;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * A {@link FacetHolder} that also has a {@link ObjectSpecification type}.
 *
 * <p>
 * Used to represent class members when building up the metamodel.
 */
public interface TypedHolder extends FacetHolder {

    public FeatureType getFeatureType();

    /**
     * The type of a {@link FeatureType#PROPERTY property}, the referenced (element) type
     * of a {@link FeatureType#COLLECTION collection}, the return type of an
     * {@link FeatureType#ACTION action}, the type of a
     * {@link FeatureType#ACTION_PARAMETER_SCALAR scalar action parameter}s, and the inferred
     * element type for a {@link FeatureType#ACTION_PARAMETER_COLLECTION collection action parameter}.
     */
    public Class<?> getType();

}
