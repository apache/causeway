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
package org.apache.causeway.viewer.wicket.ui.components.widgets.objectsimplelink;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

public class ObjectLinkSimplePanelFactory extends ComponentFactoryAbstract {

    public ObjectLinkSimplePanelFactory() {
        super(UiComponentType.OBJECT_LINK, ObjectLinkSimplePanel.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if (model instanceof UiObjectWkt) {
            return ApplicationAdvice.APPLIES;
        }
        if (model instanceof UiAttributeWkt) {
            return ApplicationAdvice.APPLIES;
        }
        return ApplicationAdvice.DOES_NOT_APPLY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        return new ObjectLinkSimplePanel(id, (IModel<ManagedObject>)model);
    }
}
