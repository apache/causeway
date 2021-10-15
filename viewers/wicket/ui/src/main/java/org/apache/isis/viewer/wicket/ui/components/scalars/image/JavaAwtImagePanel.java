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

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

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

        Wkt.labelAdd(this, ID_SCALAR_NAME, ()->getModel().getFriendlyName());

        val wicketImage = WicketImageUtil.asWicketImage(ID_SCALAR_VALUE, getModel())
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

}
