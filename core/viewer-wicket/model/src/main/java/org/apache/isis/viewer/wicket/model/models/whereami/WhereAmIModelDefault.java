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

package org.apache.isis.viewer.wicket.model.models.whereami;

import java.util.LinkedList;
import java.util.stream.Stream;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.util.pchain.ParentChain;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;

import lombok.val;

/**
 *
 * @since 2.0
 *
 */
class WhereAmIModelDefault implements WhereAmIModel {

    private final LinkedList<Object> reversedChainOfParents = new LinkedList<>();
    private final EntityModel startOfChain;
    private final IsisWebAppCommonContext commonContext;

    private boolean isWhereAmIEnabled;
    private int maxChainLength;
    
    private static int configHash = 0;

    public WhereAmIModelDefault(EntityModel startOfChain) {
        this.startOfChain = startOfChain;
        this.commonContext = startOfChain.getCommonContext();

        overrideFromConfigIfNew(commonContext.getConfiguration());

        val adapter = startOfChain.getObject();
        final Object startNode = adapter.getPojo();

        ParentChain.of(commonContext.getSpecificationLoader()::loadSpecification)
        .streamParentChainOf(startNode, maxChainLength)
        .forEach(reversedChainOfParents::addFirst);
    }

    @Override
    public EntityModel getStartOfChain() {
        return startOfChain;
    }

    @Override
    public boolean isShowWhereAmI() {
        if(!isWhereAmIEnabled)
            return false; // this will prevent rendering

        return !reversedChainOfParents.isEmpty();
    }

    @Override
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

    private void overrideFromConfigIfNew(IsisConfiguration configuration) {

        //[ahuber] without evidence that this significantly improves performance,
        // (skipping 2 hash-table lookups) we use the smart update idiom here ...
        //
        // Note: Updates are expected to occur only once per application life-cycle,
        // however this class might be loaded by a class-loader, that endures multiple
        // application life-cycles. Chances of hash-collisions are simply neglected.

        // that's the hash of the object (we don't care about the actual config values)
        // assuming that, we get a new (immutable) config instance each app's life-cycle:
        final int newConfigHash = System.identityHashCode(configuration);
        if(newConfigHash == configHash) {
            return;
        }

        configHash = newConfigHash;

        isWhereAmIEnabled = configuration.getViewer().getWicket().getWhereAmI().isEnabled();
        maxChainLength = configuration.getViewer().getWicket().getWhereAmI().getMaxParentChainLength();
    }

}
