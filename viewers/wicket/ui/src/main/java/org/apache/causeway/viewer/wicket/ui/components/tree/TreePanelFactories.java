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
package org.apache.causeway.viewer.wicket.ui.components.tree;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeComponentFactory;

/**
 * {@link ComponentFactory} for {@link TreeAttributePanel}.
 */
public class TreePanelFactories {

    // -- FACTORIES

    public static ComponentFactory parented() {
        return new Parented();
    }

    public static ComponentFactory standalone() {
        return new Standalone();
    }

    // -- PARENTED

    static class Parented extends AttributeComponentFactory {

        public Parented() {
            super(TreeAttributePanel.class);
        }

        @Override
        protected ApplicationAdvice appliesTo(final UiAttributeWkt attributeModel) {
            return appliesIf(attributeModel.isElementTypeSubtypeOf(org.apache.causeway.applib.graph.tree.TreeNode.class)
                    && !attributeModel.hasChoices());
        }

        @Override
        protected Component createComponent(final String id, final UiAttributeWkt attributeModel) {
            return new TreeAttributePanel(id, attributeModel);
        }

    }

    // -- STANDALONE

    static class Standalone extends ComponentFactoryAbstract {

        public Standalone() {
            super(UiComponentType.VALUE, StandaloneTreePanel.class);
        }

        @Override
        public ApplicationAdvice appliesTo(final IModel<?> model) {
            return model instanceof ValueModel valueModel
                ? appliesIf(ManagedObjects.correspondingClass(valueModel.getObject())
                    .map(org.apache.causeway.applib.graph.tree.TreeNode.class::isAssignableFrom)
                    .orElse(false))
                : ApplicationAdvice.DOES_NOT_APPLY;
        }

        @Override
        public final Component createComponent(final String id, final IModel<?> model) {
            return new StandaloneTreePanel(id, (ValueModel) model);
        }
    }

}
