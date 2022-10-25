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

import lombok.val;

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
    extends ComponentFactoryAbstract {

        private static final long serialVersionUID = 1L;

        private final Class<T> valueType;

        public ParentedAbstract(final Class<T> valueType) {
            super(UiComponentType.SCALAR_NAME_AND_VALUE, ScalarMarkupPanel.class);
            this.valueType = valueType;
        }

        @Override
        public ApplicationAdvice appliesTo(final IModel<?> model) {
            if (!(model instanceof ScalarModel)) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            val scalarModel = (ScalarModel) model;
            val scalarSpec = scalarModel.getScalarTypeSpec();
            val scalarType = scalarSpec.getCorrespondingClass();

            return appliesIf(scalarType.equals(valueType));

        }

        @Override
        public final Component createComponent(final String id, final IModel<?> model) {
            return new ScalarMarkupPanel<T>(id, (ScalarModel) model, valueType, this::newMarkupComponent);
        }

        protected abstract MarkupComponent newMarkupComponent(String id, ScalarModel model);

    }

    // -- STANDALONE (ABSTRACT)

    public static abstract class StandaloneAbstract<T> extends ComponentFactoryAbstract {
        private static final long serialVersionUID = 1L;

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

            val valueModel = (ValueModel) model;
            val objectAdapter = valueModel.getObject();
            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(objectAdapter)) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            return appliesIf(valueType.isAssignableFrom(
                    objectAdapter.getSpecification().getCorrespondingClass()) );
        }

        @Override
        public final Component createComponent(final String id, final IModel<?> model) {
            return new StandaloneMarkupPanel(id, (ValueModel) model, this::newMarkupComponent);
        }

        protected abstract MarkupComponent newMarkupComponent(String id, ValueModel model);
    }

    // -- CONCRETE COMPONENT FACTORY - PARENTED

    static class Parented extends ParentedAbstract<Markup> {
        private static final long serialVersionUID = 1L;

        public Parented() {
            super(Markup.class);
        }

        @Override
        protected MarkupComponent newMarkupComponent(final String id, final ScalarModel model) {
            val markupComponent = new MarkupComponent(id, model);
            markupComponent.setEnabled(false);
            return markupComponent;
        }

    }

    // -- CONCRETE COMPONENT FACTORY - STANDALONE

    static class Standalone extends StandaloneAbstract<Markup> {
        private static final long serialVersionUID = 1L;

        public Standalone() {
            super(Markup.class);
        }

        @Override
        protected MarkupComponent newMarkupComponent(final String id, final ValueModel model) {
            return new MarkupComponent(id, model);
        }
    }



}
