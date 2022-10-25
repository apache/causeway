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
package org.apache.causeway.core.metamodel.facets.object.introspection;


import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Introspection.EncapsulationPolicy;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facetapi.Facet;

/**
 *  Corresponds to the value of {@link DomainObject#introspection()},
 *  that specifies the {@link EncapsulationPolicy} of a domain object.
 *  @see Introspection
 */
public interface IntrospectionPolicyFacet extends Facet {

    Introspection getIntrospection();

    default IntrospectionPolicy getIntrospectionPolicy(final CausewayConfiguration causewayConfig) {
        switch(getIntrospection()) {
        case ENCAPSULATION_ENABLED:
            return IntrospectionPolicy.ENCAPSULATION_ENABLED;
        case ANNOTATION_OPTIONAL:
            return IntrospectionPolicy.ANNOTATION_OPTIONAL;
        case ANNOTATION_REQUIRED:
            return IntrospectionPolicy.ANNOTATION_REQUIRED;
        case AS_CONFIGURED:
            return causewayConfig.getCore().getMetaModel().getIntrospector().getPolicy();
        case NOT_SPECIFIED:
            throw _Exceptions.unexpectedCodeReach(); // there must be no such facet that returns such a case
        default:
            throw _Exceptions.unmatchedCase(getIntrospection());
        }

    }

}
