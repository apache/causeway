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

package org.apache.isis.viewer.wicket.ui.components.scalars.image;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

import java.awt.Graphics2D;

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.value.image.ImageValueFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class JavaAwtImagePanel extends PanelAbstract<ScalarModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_NAME = "scalarName";
    private static final String ID_SCALAR_VALUE = "scalarValue";
    private static final String ID_FEEDBACK = "feedback";

    public JavaAwtImagePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        buildGui();
    }

    private void buildGui() {
        final String name = getModel().getName();
        final Label scalarName = new Label(ID_SCALAR_NAME, name);
        addOrReplace(scalarName);

        final ImageValueFacet imageValueFacet = getModel().getTypeOfSpecification().getFacet(ImageValueFacet.class);
        final ObjectAdapter adapter = getModel().getObject();
        if (adapter != null) {
            final java.awt.Image imageValue = imageValueFacet.getImage(adapter);
            final RenderedDynamicImageResource imageResource = new RenderedDynamicImageResource(
                    imageValue.getWidth(null), imageValue.getHeight(null)) {

                private static final long serialVersionUID = 1L;

                @Override
                protected boolean render(final Graphics2D graphics, Attributes attributes) {
                    graphics.drawImage(imageValue, 0, 0, null);
                    return true;
                }

            };
            final Image image = new Image(ID_SCALAR_VALUE, imageResource);
            addOrReplace(image);
            addOrReplace(new NotificationPanel(ID_FEEDBACK, image, new ComponentFeedbackMessageFilter(image)));
        } else {
            permanentlyHide(ID_SCALAR_VALUE, ID_FEEDBACK);
        }
    }

}
