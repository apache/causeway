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

package org.apache.isis.viewer.wicket.ui.components.scalars.wizardpagedesc;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.metamodel.wizardpagedesc.WizardPageDescriptionFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.ComponentFactoryScalarAbstract;

/**
 * {@link ComponentFactory} for {@link WizardPageDescriptionPanel}.
 */
public class WizardPageDescriptionPanelFactory extends ComponentFactoryScalarAbstract {

    private static final long serialVersionUID = 1L;

    public WizardPageDescriptionPanelFactory() {
        super(String.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        final ApplicationAdvice applicationAdvice = super.appliesTo(model);
        if (!applicationAdvice.applies()) {
            return applicationAdvice;
        }
        final ScalarModel scalarModel = (ScalarModel) model;
        return appliesExclusivelyIf(scalarModel.getFacet(WizardPageDescriptionFacet.class) != null);
    }

    @Override
    public Component createComponent(final String id, final ScalarModel scalarModel) {
        return new WizardPageDescriptionPanel(id, scalarModel);
    }

}
