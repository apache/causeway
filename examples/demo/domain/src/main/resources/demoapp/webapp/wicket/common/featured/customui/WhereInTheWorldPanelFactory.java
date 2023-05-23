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
package demoapp.webapp.wicket.common.featured.customui;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;

import demoapp.dom.featured.customui.WhereInTheWorldPage;
import demoapp.dom.featured.customui.GeoapifyClient;

//tag::class[]
@org.springframework.stereotype.Component
@Priority(PriorityPrecedence.EARLY)                                             // <.>
public class WhereInTheWorldPanelFactory
        extends EntityComponentFactoryAbstract {
    private static final long serialVersionUID = 1L;

    public WhereInTheWorldPanelFactory() {
        super(
                UiComponentType.ENTITY                                          // <.>
                , WhereInTheWorldPanel.class
        );
    }

    @Override
    protected ApplicationAdvice doAppliesTo(final UiObjectWkt entityModel) {    // <.>
        final ManagedObject managedObject = entityModel.getObject();            // <.>
        final Object domainObject = managedObject.getPojo();                    // <.>
        return ApplicationAdvice.appliesIf(
                domainObject instanceof WhereInTheWorldPage);                   // <.>
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        UiObjectWkt entityModel = (UiObjectWkt) model;                          // <.>
        return new WhereInTheWorldPanel(id, entityModel, geoapifyClient);       // <.>
    }

    @Inject private GeoapifyClient geoapifyClient;                              // <.>
}
//end::class[]
