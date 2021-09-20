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
package org.apache.isis.viewer.wicket.model.models.interaction;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;
import org.apache.isis.viewer.wicket.model.models.interaction.act.ActionInteractionModelWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.prop.PropertyInteractionModelWkt;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Implemented by a <i>UI Models</i> that are children to
 * a shared <i>Interaction Model</i>.
 * <p>
 * This sharing relation must survive a de-serialze/serialize cycle.
 *
 * @apiNote {@link Serializable}, but not an {@link IModel}, such that the
 * parent <i>Interaction Model</i> can take full control of serialization.
 * That is, it will inject the shared parent instance into this model
 * which is a <i>transient</i> field.
 *
 * @see ActionInteractionModelWkt
 * @see PropertyInteractionModelWkt
 */
@RequiredArgsConstructor
public abstract class InteractionHolderAbstract<I, T extends ModelAbstract<I>>
implements
    HasCommonContext,
    Serializable {

    private static final long serialVersionUID = 1L;

    private transient @NonNull T containerModel;

    @Override
    public final IsisAppCommonContext getCommonContext() {
        return containerModel().getCommonContext();
    }

    protected final T containerModel() {
        _Assert.assertNotNull(containerModel, "detached from container model");
        return containerModel;
    }

    public final I getObject() {
        _Assert.assertNotNull(this.containerModel, "already detached from container model");
        return containerModel.getObject();
    }

    public void attachToContainerModel(final @NonNull T containerModel) {
        _Assert.assertNull(this.containerModel, "cannot attach, already attached to container model");
        this.containerModel = containerModel;
    }

    public void detachFromContainerModel() {
        this.containerModel = null;
    }


}