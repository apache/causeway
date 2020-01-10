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
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

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
     * Returns a description of how the member is used.
     */
    String getDescription();


}
