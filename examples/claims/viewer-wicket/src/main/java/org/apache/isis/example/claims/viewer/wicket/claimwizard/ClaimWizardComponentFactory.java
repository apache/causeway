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

package org.apache.isis.example.claims.viewer.wicket.claimwizard;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.example.claims.dom.claim.ClaimWizard;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

public class ClaimWizardComponentFactory extends EntityComponentFactoryAbstract {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "wizard";

    public ClaimWizardComponentFactory() {
        super(ComponentType.ENTITY, NAME);
    }

    @Override
    protected ApplicationAdvice appliesTo(IModel<?> model) {
        return appliesExclusivelyIf(super.appliesTo(model).applies() && isModelForWizard((EntityModel) model));
    }

    private boolean isModelForWizard(EntityModel model) {
        final ObjectSpecification typeOfSpec = model.getTypeOfSpecification();
        final ObjectSpecification claimWizardSpec = getSpecificationLoader().loadSpecification(ClaimWizard.class);
        return typeOfSpec.isOfType(claimWizardSpec);
    }

    @Override
    public Component createComponent(String id, IModel<?> model) {
        final EntityModel entityModel = (EntityModel) model;
        return new ClaimWizardPanel(id, entityModel);
    }

}
