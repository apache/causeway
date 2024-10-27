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
package org.apache.causeway.viewer.wicket.ui.components.scalars.markup;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.value.Markup;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ComponentFactoryScalarTypeConstrainedAbstract;

/**
 * {@link ComponentFactory} for {@link ScalarMarkupPanel}.
 */
public class MarkupPanelFactories {

    // -- PUBLIC FACTORIES (FOR REGISTRATION)

    public static ComponentFactory parented() {
        return new Parented();
    }

    public static ComponentFactory standalone() {
        return new Standalone();
    }

    // -- PARENTED (ABSTRACT)

    public static abstract class ParentedAbstract<T extends Serializable>
    extends ComponentFactoryScalarTypeConstrainedAbstract {
        private final Class<T> valueType;

        protected ParentedAbstract(final Class<T> valueType) {
            super(ScalarMarkupPanel.class, valueType);
            this.valueType = valueType;
        }

        @Override
        protected Component createComponent(final String id, final ScalarModel scalarModel) {
            return new ScalarMarkupPanel<T>(id, scalarModel, valueType, this.key());
        }

        protected abstract MarkupComponent newMarkupComponent(String id, ScalarModel model);

    }

    // -- STANDALONE (ABSTRACT)

    public static abstract class StandaloneAbstract<T>
    extends ComponentFactoryAbstract {

        private final Class<T> valueType;

        public StandaloneAbstract(final Class<T> valueType) {
            super(UiComponentType.VALUE, StandaloneMarkupPanel.class);
            this.valueType = valueType;
        }

        @Override
        public ApplicationAdvice appliesTo(final IModel<?> model) {
            if (!(model instanceof ValueModel)) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            var valueModel = (ValueModel) model;
            var objectAdapter = valueModel.getObject();
            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            return appliesIf(valueType.isAssignableFrom(
                    objectAdapter.getSpecification().getCorrespondingClass()) );
        }

        @Override
        public final Component createComponent(final String id, final IModel<?> model) {
            // last arg is consumed by constructor, hence trivially serializable
            return new StandaloneMarkupPanel(id, (ValueModel) model, this::newMarkupComponent);
        }

        protected abstract MarkupComponent newMarkupComponent(String id, ValueModel model);
    }

    // -- CONCRETE COMPONENT FACTORY - PARENTED

    static class Parented extends ParentedAbstract<Markup> {

        public Parented() {
            super(Markup.class);
        }

        @Override
        protected MarkupComponent newMarkupComponent(final String id, final ScalarModel model) {
            var markupComponent = new MarkupComponent(id, model);
            markupComponent.setEnabled(false);
            return markupComponent;
        }

    }

    // -- CONCRETE COMPONENT FACTORY - STANDALONE

    static class Standalone extends StandaloneAbstract<Markup> {

        public Standalone() {
            super(Markup.class);
        }

        @Override
        protected MarkupComponent newMarkupComponent(final String id, final ValueModel model) {
            return new MarkupComponent(id, model);
        }
    }

}
