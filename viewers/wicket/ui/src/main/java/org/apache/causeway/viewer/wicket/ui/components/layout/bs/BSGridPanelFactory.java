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
package org.apache.causeway.viewer.wicket.ui.components.layout.bs;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.entity.EntityComponentFactoryAbstract;

/**
 * {@link ComponentFactory} for {@link BSGridPanel}.
 */
public class BSGridPanelFactory extends EntityComponentFactoryAbstract {

    public BSGridPanelFactory() {
        super(UiComponentType.ENTITY, BSGridPanel.class);
    }

    @Override protected ApplicationAdvice appliesTo(final IModel<?> model) {
        final UiObjectWkt entityModel = (UiObjectWkt) model;

        var objectAdapter = entityModel.getObject();
        var objectSpec = entityModel.getTypeOfSpecification();

        return ApplicationAdvice.appliesIf(
                Facets.bootstrapGrid(objectSpec, objectAdapter)
                .isPresent());
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final UiObjectWkt entityModel = (UiObjectWkt) model;

        var objectAdapter = entityModel.getObject();
        var objectSpec = entityModel.getTypeOfSpecification();

        return Facets.bootstrapGrid(objectSpec, objectAdapter)
                .map(grid->new BSGridPanel(id, entityModel, grid))
                .orElseThrow(); // empty case guarded against by appliesTo(...) above
    }

    /**
     * refactoring hint: this is legacy code, just kept for reference - perhaps resurrect as feature
     */
    public static Optional<Component> extraContentForMixin(final String id, final ActionModel actionModel) {

        //[CAUSEWAY-3210] EntityModel.ofAdapter(commonContext, targetAdapterForMixin); not supported for mixins
        return Optional.empty();

//        var action = actionModel.getAction();
//        if(action.isMixedIn()) {
//
//            var mixinSpec = ((MixedInMember)action).getMixinType();
//            var targetAdapterForMixin = action.realTargetAdapter(actionModel.getActionOwner());
//
//            // if we can bootstrap a grid, use it
//            return Facets.bootstrapGrid(mixinSpec, targetAdapterForMixin)
//            .map(bsGrid->{
//                var commonContext = actionModel.getMetaModelContext();
//                var entityModelForMixin =
//                        EntityModel.ofAdapter(commonContext, targetAdapterForMixin);
//                return new BSGridPanel(id, entityModelForMixin, bsGrid);
//            });
//        }
//        return Optional.empty();
    }
}
