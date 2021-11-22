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
package org.apache.isis.viewer.wicket.ui.components.tree;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.ParentedMarkupPanel;

import lombok.val;

/**
 * {@link ComponentFactory} for {@link ParentedMarkupPanel}.
 */
public class TreePanelFactories {

    // -- PARENTED

    static class Parented extends ComponentFactoryAbstract {
        private static final long serialVersionUID = 1L;

        public Parented() {
            super(ComponentType.SCALAR_NAME_AND_VALUE, ParentedTreePanel.class);
        }

        @Override
        public ApplicationAdvice appliesTo(final IModel<?> model) {
            if (!(model instanceof ScalarModel)) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            final ScalarModel scalarModel = (ScalarModel) model;

            if(!scalarModel.isScalarTypeSubtypeOf(org.apache.isis.applib.graph.tree.TreeNode.class)) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            return appliesIf( !scalarModel.hasChoices() );
        }

        @Override
        public final Component createComponent(final String id, final IModel<?> model) {

            return new ParentedTreePanel(id, (ScalarModel) model);
        }
    }

    // -- STANDALONE

    static class Standalone extends ComponentFactoryAbstract {
        private static final long serialVersionUID = 1L;

        public Standalone() {
            super(ComponentType.VALUE, StandaloneTreePanel.class);
        }

        @Override
        public ApplicationAdvice appliesTo(final IModel<?> model) {
            if (!(model instanceof ValueModel)) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            final ValueModel valueModel = (ValueModel) model;
            val adapter = valueModel.getObject();
            if(adapter==null || adapter.getPojo()==null) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            return appliesIf( adapter.getPojo() instanceof org.apache.isis.applib.graph.tree.TreeNode );
        }

        @Override
        public final Component createComponent(final String id, final IModel<?> model) {
            return new StandaloneTreePanel(id, (ValueModel) model);
        }
    }

    // -- CONSTRUCTION

    public static ComponentFactory parented() {
        return new Parented();
    }

    public static ComponentFactory standalone() {
        return new Standalone();
    }

}
