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
package org.apache.causeway.core.metamodel.postprocessors;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectMemberAbstract;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class ObjectSpecificationPostProcessorAbstract
implements ObjectSpecificationPostProcessor {

    @Getter(onMethod_ = {@Override})
    private final @NonNull MetaModelContext metaModelContext;

    protected ObjectSpecificationPostProcessorAbstract(final MetaModelContext metaModelContext) {
        super();
        this.metaModelContext = metaModelContext;
    }

    /**
     * Use for domain-object-type agnostic facets only!
     * @see Facet#isObjectTypeSpecific()
     */
    protected static FacetedMethod facetedMethodFor(final ObjectMember objectMember) {
        // TODO: hacky, need to copy facet onto underlying peer, not to the action/association itself.
        val objectMemberImpl = (ObjectMemberAbstract) objectMember;
        return objectMemberImpl.getFacetedMethod();
    }

}
