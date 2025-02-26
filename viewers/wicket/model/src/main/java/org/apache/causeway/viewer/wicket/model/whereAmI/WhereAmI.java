/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.wicket.model.whereAmI;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.causeway.core.metamodel.util.pchain.ParentChain;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;

/**
 * @since 2.0
 */
public record WhereAmI(
        /** immutable start node of the navigable parent chain */
        UiObjectWkt startOfChain,
        List<Object> reversedChainOfParents,
        boolean isWhereAmIEnabled) {

    public static WhereAmI of(final UiObjectWkt startOfChain) {
        var mmc = startOfChain.getMetaModelContext();
        var settings = mmc.getConfiguration().getViewer().getWicket().getBreadcrumbs();
        var isWhereAmIEnabled = settings.isEnabled();
        int maxChainLength = settings.getMaxParentChainLength();

        var adapter = startOfChain.getObject();
        final Object startNode = adapter.getPojo();

        var reversedChainOfParents = new LinkedList<Object>();

        ParentChain.of(mmc.getSpecificationLoader())
            .streamParentChainOf(startNode, maxChainLength)
            .forEach(reversedChainOfParents::addFirst);

        return new WhereAmI(startOfChain, reversedChainOfParents, isWhereAmIEnabled);
    }

    /**
     * The navigable parent chain requires a minimum length of 2 in order to be shown.
     * @return whether the where-am-I hint should be shown or hidden
     */
    public boolean isShowWhereAmI() {
        if(!isWhereAmIEnabled) return false; // this will prevent rendering

        return !reversedChainOfParents.isEmpty();
    }

    /**
     * Streams the linked nodes of this model's navigable parent chain in reverse order.
     * @return reversed order stream of linked parent nodes, which does not include the start node
     */
    public Stream<UiObjectWkt> streamParentChainReversed() {
        if(!isWhereAmIEnabled) return Stream.empty(); //unexpected call, we could log a warning

        return reversedChainOfParents.stream()
                .map(this::toEntityModel);
    }

    // -- HELPER

    private UiObjectWkt toEntityModel(final Object domainObject) {
        var objectAdapter = startOfChain.getMetaModelContext().getObjectManager().adapt(domainObject);
        return UiObjectWkt.ofAdapter(objectAdapter);
    }

}
