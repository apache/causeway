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

package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLinkFactoryForWicket;

import lombok.val;

class ServiceActionLinkFactory extends ActionLinkFactoryForWicket {

    private static final long serialVersionUID = 1L;

    ServiceActionLinkFactory(
            final String linkId,
            final EntityModel serviceEntityModel) {
        super(linkId, serviceEntityModel, null, null);
    }

    @Override
    public LinkAndLabel newLink(
            final ObjectAction objectAction) {

        val objectAdapter = this.targetEntityModel.load();

        final AbstractLink link = super.newLinkComponent(objectAction, toggledMementosProviderIfAny);

        return newLinkAndLabel(objectAdapter, objectAction, link);
    }


}