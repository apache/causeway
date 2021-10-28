/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;

import java.io.Serializable;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class LinkAndLabelFactoryAbstract
implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final String linkId;
    protected final ScalarModel scalarModelForAssociationIfAny;
    protected final EntityCollectionModel collectionModelForAssociationIfAny;

    /**
     * The actual factory method clients care about.
     */
    public abstract LinkAndLabel newActionLink(ObjectAction action);

    protected abstract ActionModel actionModel(
            ManagedAction managedAction,
            ScalarModel scalarModelForAssociationIfAny);

    // called exclusively by LinkAndLabel, which implements HasManagedAction
    protected final ActionLink newLinkComponent(final ManagedAction managedAction) {
        return ActionLink
                .create(linkId, actionModel(managedAction, scalarModelForAssociationIfAny));
    }

}
