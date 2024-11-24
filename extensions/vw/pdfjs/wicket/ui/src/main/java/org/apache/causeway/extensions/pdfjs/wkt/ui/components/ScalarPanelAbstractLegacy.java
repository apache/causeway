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
package org.apache.causeway.extensions.pdfjs.wkt.ui.components;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

abstract class ScalarPanelAbstractLegacy
extends PanelAbstract<ManagedObject, UiAttributeWkt> {

    private static final long serialVersionUID = 1L;

    protected Component compactFrame;
    private Component regularFrame;

    public ScalarPanelAbstractLegacy(final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel);
    }

    protected final UiAttributeWkt attributeModel() {
        return super.getModel();
    }

    @Override
    protected void onBeforeRender() {
        if (!hasBeenRendered()) {
            buildGui();
        }
        super.onBeforeRender();
    }

    private void buildGui() {

        if(attributeModel().getRenderingHint().isInTable()) {
            regularFrame = createShallowRegularFrame();
            compactFrame = createCompactFrame();
            regularFrame.setVisible(false);
            compactFrame.setVisible(true);
        } else {
            regularFrame = createRegularFrame();
            compactFrame = createShallowCompactFrame();
            regularFrame.setVisible(true);
            compactFrame.setVisible(false);
        }

        addOrReplace(regularFrame, compactFrame);

        addCssFromMetaModel();
    }

    private void addCssFromMetaModel() {
        var attributeModel = attributeModel();

        Wkt.cssAppend(this, attributeModel.getCssClass());

        Facets.cssClass(attributeModel.getMetaModel(), attributeModel.getParentUiModel().getManagedObject())
        .ifPresent(cssClass->
            Wkt.cssAppend(this, cssClass));
    }

    protected abstract MarkupContainer createRegularFrame();
    protected abstract Component createCompactFrame();

    /**
     * Builds the hidden REGULAR component when in COMPACT format.
     */
    protected abstract MarkupContainer createShallowRegularFrame();

    /**
     * Builds the hidden COMPACT component when in REGULAR format.
     */
    protected abstract Component createShallowCompactFrame();

}
