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

package org.apache.isis.viewer.wicket.model.modelhelpers;

import java.util.LinkedList;
import java.util.stream.Stream;

import org.apache.isis.core.metamodel.util.pchain.ParentChain;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;

import lombok.val;

/**
 *
 * @implNote - previously this was called WhereAmIModel, but it isn't really a model in the Wicket sense (it's not serializable, for a start), hence renamed.
 * @since 2.0
 */
public class WhereAmIHelper {

    public static WhereAmIHelper of(EntityModel startOfChain) {
        return new WhereAmIHelper(startOfChain);
    }

    private final LinkedList<Object> reversedChainOfParents = new LinkedList<>();
    private final EntityModel startOfChain;
    private final IsisWebAppCommonContext commonContext;

    private final boolean isWhereAmIEnabled;

    public WhereAmIHelper(final EntityModel startOfChain) {
        this.startOfChain = startOfChain;
        this.commonContext = startOfChain.getCommonContext();

        val settings = commonContext.getConfiguration().getViewer().getWicket().getBreadcrumbs();
        this.isWhereAmIEnabled = settings.isEnabled();
        int maxChainLength = settings.getMaxParentChainLength();

        val adapter = startOfChain.getObject();
        final Object startNode = adapter.getPojo();

        ParentChain.of(commonContext.getSpecificationLoader()::loadSpecification)
        .streamParentChainOf(startNode, maxChainLength)
        .forEach(reversedChainOfParents::addFirst);
    }

    /**
     *
     * @return the immutable start node of the navigable parent chain
     */
    public EntityModel getStartOfChain() {
        return startOfChain;
    }

    /**
     * The navigable parent chain requires a minimum length of 2 in order to be shown.
     * @return whether the where-am-I hint should be shown or hidden
     */
    public boolean isShowWhereAmI() {
        if(!isWhereAmIEnabled) {
            return false; // this will prevent rendering
        }
        return !reversedChainOfParents.isEmpty();
    }

    /**
     * Streams the linked nodes of this model's navigable parent chain in reverse order.
     * @return reversed order stream of linked parent nodes, which does not include the start node
     */
    public Stream<EntityModel> streamParentChainReversed() {
        if(!isWhereAmIEnabled)
            return Stream.empty(); //[ahuber] unexpected call, we could log a warning

        return reversedChainOfParents.stream()
                .map(this::toEntityModel);
    }

    // -- HELPER

    private EntityModel toEntityModel(Object domainObject) {
        val objectAdapter = commonContext.getObjectManager().adapt(domainObject);
        return EntityModel.ofAdapter(commonContext, objectAdapter);
    }

}
