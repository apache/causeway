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

import java.util.stream.Stream;

import org.apache.isis.viewer.wicket.model.models.EntityModel;

/**
 * Represents a navigable chain of parent nodes starting at the current node.
 *
 * @since 2.0
 *
 */
public interface WhereAmIModel {

    public final static String CONFIG_KEY_IS_WHERE_AM_I_FEATURE_ENABLED = "isis.viewer.wicket.whereAmI.enabled";
    public final static String CONFIG_KEY_MAX_NAVIGABLE_PARENT_CHAIN_LENGTH = "isis.viewer.wicket.whereAmI.maxParentChainLength";

    public final static boolean IS_WHERE_AM_I_FEATURE_ENABLED_DEFAULT = true;
    public final static int MAX_NAVIGABLE_PARENT_CHAIN_LENGTH_DEFAULT = 64;


    public static WhereAmIModel of(EntityModel startOfChain) {
        return new WhereAmIModelDefault(startOfChain);
    }

    /**
     * The navigable parent chain requires a minimum length of 2 in order to be shown.
     * @return whether the where-am-I hint should be shown or hidden
     */
    public boolean isShowWhereAmI();

    /**
     * Streams the linked nodes of this model's navigable parent chain in reverse order.
     * @return reversed order stream of linked parent nodes, which does not include the start node
     */
    public Stream<EntityModel> streamParentChainReversed();

    /**
     *
     * @return the immutable start node of the navigable parent chain
     */
    public EntityModel getStartOfChain();

}
