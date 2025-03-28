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
package org.apache.causeway.viewer.wicket.ui.components.attributes.string;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeComponentFactoryWithTypeConstraint;
import org.apache.causeway.viewer.wicket.ui.panels.PanelUtil;

public class StringAttributePanelFactory extends AttributeComponentFactoryWithTypeConstraint {

    public StringAttributePanelFactory() {
        super(StringAttributePanel.class, String.class);
    }

    @Override
    public Component createComponent(final String id, final UiAttributeWkt attributeModel) {
        return Facets.multilineIsPresent(attributeModel.getMetaModel())
            ? new MultiLineAttributePanel(id, attributeModel)
            : new StringAttributePanel(id, attributeModel);
    }

    public static CssResourceReference cssResourceReferenceForMultiLineString() {
        return PanelUtil.cssResourceReferenceFor(MultiLineAttributePanel.class);
    }
}
