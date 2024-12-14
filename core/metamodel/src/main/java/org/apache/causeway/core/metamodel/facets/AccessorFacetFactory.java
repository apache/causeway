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
package org.apache.causeway.core.metamodel.facets;

import java.util.function.Consumer;

import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * A {@link FacetFactory} implementation that is able to identify property or
 * collection accessors.
 * <p>
 * For example, a <i>getter</i> method is most commonly used to represent either
 * a property (value or reference) or a collection, with the return type
 * indicating which.
 */
public interface AccessorFacetFactory extends FacetFactory {

    /**
     * Whether is in support of {@link OneToOneAssociation}(s).
     */
    boolean supportsProperties();
    /**
     * Whether is in support of {@link OneToManyAssociation}(s).
     */
    boolean supportsCollections();

    /**
     * Whether (this facet is able to determine that) the supplied
     * {@link ResolvedMethod} represents an accessor to an {@link ObjectAssociation}.
     */
    boolean isAssociationAccessor(ResolvedMethod method);

    /**
     * Uses the provided {@link MethodRemover} to remove all matching accessors
     * and calls back the supplied consumer.
     */
    void findAndRemoveAccessors(MethodRemover methodRemover, Consumer<ResolvedMethod> onMatchingAccessor);

}
