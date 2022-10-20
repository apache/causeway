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
package org.apache.causeway.core.metamodel.facets.members.publish.execution;

import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.services.publishing.ExecutionPublisher;

import lombok.NonNull;
import lombok.val;

/**
 * Indicates that details of the action invocation or property edit,
 * captured by an {@link Execution},
 * should be dispatched via {@link ExecutionPublisher} to all subscribed
 * {@link ExecutionSubscriber}s.
 * <p>
 * Corresponds to annotating the action method or property using
 * {@code @Action/@Property(executionPublishing=ENABLED)}
 *
 * @since 2.0
 *
 */
public interface ExecutionPublishingFacet extends Facet {

    public static boolean isPublishingEnabled(final @NonNull FacetHolder facetHolder) {
        val executionPublishingFacet = facetHolder.getFacet(ExecutionPublishingFacet.class);
        return executionPublishingFacet!=null;
    }

}
