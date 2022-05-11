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
package org.apache.isis.extensions.pdfjs.wkt.ui.components;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.WktComponents;

import lombok.val;

abstract class ScalarPanelAbstractLegacy
extends PanelAbstract<ManagedObject, ScalarModel> {

    private static final long serialVersionUID = 1L;

    protected static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    protected static final String ID_SCALAR_NAME = "scalarName";
    protected static final String ID_SCALAR_VALUE = "scalarValue";
    protected static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";
    protected static final String ID_FEEDBACK = "feedback";

    protected static final String ID_FILE_NAME_IF_COMPACT = "fileNameIfCompact";
    protected static final String ID_DOWNLOAD_IF_COMPACT = "scalarIfCompactDownload";

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
    protected MarkupContainer createShallowRegularFrame() {
        val shallowRegularFrame = new WebMarkupContainer(ID_SCALAR_IF_REGULAR);
        WktComponents.permanentlyHide(shallowRegularFrame,
                ID_SCALAR_NAME, ID_SCALAR_VALUE, ID_FEEDBACK);
        return shallowRegularFrame;
    }

    /**
     * Builds the hidden COMPACT component when in REGULAR format.
     */
    protected Component createShallowCompactFrame() {
        val shallowCompactFrame = new WebMarkupContainer(ID_SCALAR_IF_COMPACT);
        WktComponents.permanentlyHide(shallowCompactFrame,
                ID_DOWNLOAD_IF_COMPACT, ID_FILE_NAME_IF_COMPACT);
        return shallowCompactFrame;
    }

}
