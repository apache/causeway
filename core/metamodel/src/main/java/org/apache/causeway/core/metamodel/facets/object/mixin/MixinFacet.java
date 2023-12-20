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
package org.apache.causeway.core.metamodel.facets.object.mixin;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * Applies to {@link ObjectSpecification}s of classes that can act as a mix-in.
 * <p>
 * Mix-ins are annotated e.g. {@link DomainObject} with
 * {@link DomainObject#nature()} of {@link Nature#MIXIN}) and have a public 1-arg
 * constructor accepting an object (the mix-in's <i>holder</i>), being the object
 * this is a mix-in for.
 * <p>
 * Since 2.0 there are additional annotations, that when declared on a type, make
 * the type recognized as a mix-in. These are {@link Action}, {@link Property} and
 * {@link Collection}.
 */
public interface MixinFacet extends Facet {

    public enum Contributing {
        /**
         * Initial state early during introspection.
         */
        UNSPECIFIED,
        /**
         * Object associated with an <i>entity</i>, <i>viewmodel</i> or <i>domain-service</i>
         * to act as contributer of a single <i>domain-action</i>.
         */
        AS_ACTION,
        /**
         * Object associated with an <i>entity</i>, <i>viewmodel</i> or <i>domain-service</i>
         * to act as contributer of a single <i>domain-property</i>.
         */
        AS_PROPERTY,
        /**
         * Object associated with an <i>entity</i>, <i>viewmodel</i> or <i>domain-service</i>
         * to act as contributer of a single <i>domain-collection</i>.
         */
        AS_COLLECTION;

        public boolean isUnspecified() { return this==UNSPECIFIED; }
    }

    Contributing contributing();

    /**
     * The mixin's main method name.
     */
    String getMainMethodName();

    boolean isMixinFor(Class<?> candidateDomainType);

    enum Policy {
        FAIL_FAST,
        IGNORE_FAILURES
    }

    /**
     * Returns the mix-in around the provided domain object (<i>holder</i>),
     * also resolving any injection points.
     */
    Object instantiate(Object holderPojo);

    /**
     * Whether given method matches as a candidate for the mixin's main method.
     * @param method
     * @return whether has expected name and same declaring class
     */
    boolean isCandidateForMain(ResolvedMethod method);

}
