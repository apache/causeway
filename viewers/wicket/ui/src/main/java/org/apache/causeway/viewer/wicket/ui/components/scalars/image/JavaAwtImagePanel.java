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
package org.apache.causeway.viewer.wicket.ui.components.scalars.image;

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.decorators.FormLabelDecorator.FormLabelDecorationModel;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.scalars.AttributePanel;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktDecorators;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

class JavaAwtImagePanel
extends PanelAbstract<ManagedObject, UiAttributeWkt> {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE = "scalarValue";
    private static final String ID_FEEDBACK = "feedback";

    public JavaAwtImagePanel(final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel);
        buildGui();
    }

    private void buildGui() {

        Wkt.add(this, createScalarNameLabel(ID_SCALAR_NAME));

        var wicketImage = _WktImageUtil.asWicketImage(ID_SCALAR_VALUE, attributeModel())
                .orElse(null);
        if(wicketImage != null) {
            addOrReplace(wicketImage);

            addOrReplace(new NotificationPanel(
                    ID_FEEDBACK,
                    wicketImage,
                    new ComponentFeedbackMessageFilter(wicketImage)));

        } else {
            WktComponents.permanentlyHide(this, ID_SCALAR_VALUE, ID_FEEDBACK);
        }
    }

    @Override
    public String getVariation() {
        return Facets.labelAt(attributeModel().getMetaModel())
                .name();
    }

    /** see also {@link AttributePanel} */
    protected Label createScalarNameLabel(final String id) {

        var attributeModel = attributeModel();
        var scalarNameLabel = Wkt.label(id, attributeModel.getFriendlyName());

        WktDecorators.formLabel()
            .decorate(scalarNameLabel, FormLabelDecorationModel
                    .mandatory(attributeModel.isShowMandatoryIndicator()));

        attributeModel.getDescribedAs()
            .ifPresent(describedAs->WktTooltips.addTooltip(scalarNameLabel, describedAs));
        return scalarNameLabel;
    }

    protected final UiAttributeWkt attributeModel() {
        return getModel();
    }

}
