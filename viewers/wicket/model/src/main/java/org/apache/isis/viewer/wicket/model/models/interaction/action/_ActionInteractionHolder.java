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
package org.apache.isis.viewer.wicket.model.models.interaction.action;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Implemented by a <i>Parameter Interaction Model</i>,
 * which is a holder of a shared <i>Action Interaction Model</i>.
 * <p>
 * Multiple param models share a single action interaction.
 * This sharing relation must survive a de-serialze/serialize cycle.
 * @apiNote {@link Serializable}, but not an {@link IModel}, such that the
 * parent <i>Action Interaction Model</i> can take full control of serialization.
 * That is, it will inject the shared parent instance into this model
 * which is a <i>transient</i> field.
 */
@RequiredArgsConstructor
abstract class _ActionInteractionHolder
implements
    HasCommonContext,
    Serializable {

    private static final long serialVersionUID = 1L;

    private transient @NonNull ActionInteractionModelWkt actionInteractionModelWkt;

    @Override
    public final IsisAppCommonContext getCommonContext() {
        return actionInteractionModelWkt.getCommonContext();
    }

    protected final ActionInteractionModelWkt containerModel() {
        _Assert.assertNotNull(actionInteractionModelWkt, "detached from container model");
        return actionInteractionModelWkt;
    }

    public final ActionInteraction actionInteraction() {
        _Assert.assertNotNull(this.actionInteractionModelWkt, "already detached from container model");
        return actionInteractionModelWkt.actionInteraction();
    }

    public void attachToContainerModel(final @NonNull ActionInteractionModelWkt actionInteractionModelWkt) {
        _Assert.assertNull(this.actionInteractionModelWkt, "cannot attach, already attached to container model");
        this.actionInteractionModelWkt = actionInteractionModelWkt;
    }

    public void detachFromContainerModel() {
        this.actionInteractionModelWkt = null;
    }


}
