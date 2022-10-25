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
import org.apache.wicket.model.Model;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.decorators.FormLabelDecorator.FormLabelDecorationModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktDecorators;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import lombok.val;

public class JavaAwtImagePanel
extends PanelAbstract<ManagedObject, ScalarModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE = "scalarValue";
    private static final String ID_FEEDBACK = "feedback";

    public JavaAwtImagePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        buildGui();
    }

    private void buildGui() {

        Wkt.add(this, createScalarNameLabel(ID_SCALAR_NAME));

        val wicketImage = WicketImageUtil.asWicketImage(ID_SCALAR_VALUE, scalarModel())
                .orElse(null);
        if(wicketImage != null) {
            addOrReplace(wicketImage);

            addOrReplace(new NotificationPanel(
                    ID_FEEDBACK,
                    wicketImage,
                    new ComponentFeedbackMessageFilter(wicketImage)));

        } else {
            permanentlyHide(ID_SCALAR_VALUE, ID_FEEDBACK);
        }

    }

    /** copied over from {@link ScalarPanelAbstract} */
    protected Label createScalarNameLabel(final String id) {

        val labelCaption = Model.of(scalarModel().getFriendlyName());

        final Label scalarNameLabel = Wkt.label(id, labelCaption);
        if(_Strings.isNullOrEmpty(labelCaption.getObject())) {
            return scalarNameLabel;
        }

        val scalarModel = scalarModel();

        WktDecorators.getFormLabel()
            .decorate(scalarNameLabel, FormLabelDecorationModel
                    .mandatory(scalarModel.isRequired()
                            && scalarModel.isEnabled()));

        scalarModel.getDescribedAs()
        .ifPresent(describedAs->WktTooltips.addTooltip(scalarNameLabel, describedAs));
        return scalarNameLabel;
    }

    protected final ScalarModel scalarModel() {
        return getModel();
    }

}
