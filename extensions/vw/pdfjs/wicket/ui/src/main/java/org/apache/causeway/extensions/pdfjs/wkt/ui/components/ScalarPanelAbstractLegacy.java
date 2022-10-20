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
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

abstract class ScalarPanelAbstractLegacy
extends PanelAbstract<ManagedObject, ScalarModel> {

    private static final long serialVersionUID = 1L;

    protected Component compactFrame;
    private Component regularFrame;

    public ScalarPanelAbstractLegacy(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    protected final ScalarModel scalarModel() {
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

        switch(scalarModel().getRenderingHint()) {
        case REGULAR:
            regularFrame = createRegularFrame();
            compactFrame = createShallowCompactFrame();
            regularFrame.setVisible(true);
            compactFrame.setVisible(false);
            break;
        default:
            regularFrame = createShallowRegularFrame();
            compactFrame = createCompactFrame();
            regularFrame.setVisible(false);
            compactFrame.setVisible(true);
            break;
        }

        addOrReplace(regularFrame, compactFrame);

        addCssFromMetaModel();
    }

    private void addCssFromMetaModel() {
        val scalarModel = scalarModel();

        Wkt.cssAppend(this, scalarModel.getCssClass());

        Facets.cssClass(scalarModel.getMetaModel(), scalarModel.getParentUiModel().getManagedObject())
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
