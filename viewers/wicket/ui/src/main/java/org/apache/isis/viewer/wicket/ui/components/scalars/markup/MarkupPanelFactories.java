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

package org.apache.isis.viewer.wicket.ui.components.scalars.markup;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.value.Markup;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

import lombok.val;

/**
 * {@link ComponentFactory} for {@link ParentedMarkupPanel}.
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

    public static abstract class ParentedAbstract extends ComponentFactoryAbstract {
        private static final long serialVersionUID = 1L;

        private final Class<?> valueType;

        public ParentedAbstract(Class<?> valueType) {
            super(ComponentType.SCALAR_NAME_AND_VALUE, ParentedMarkupPanel.class);
            this.valueType = valueType;
        }

        @Override
        public ApplicationAdvice appliesTo(final IModel<?> model) {
            if (!(model instanceof ScalarModel)) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            val scalarModel = (ScalarModel) model;
            
            val scalarType = scalarModel.getTypeOfSpecification().getCorrespondingClass();
            
            if(scalarType.equals(valueType)) {
                return ApplicationAdvice.APPLIES_EXCLUSIVELY;
            }
            
            return appliesIf( valueType.isAssignableFrom(scalarType) );
            
        }

        @Override
        public final Component createComponent(final String id, final IModel<?> model) {
            return new ParentedMarkupPanel(id, (ScalarModel) model, getMarkupComponentFactory());        
        }

        protected abstract MarkupComponentFactory getMarkupComponentFactory();

    }

    // -- STANDALONE (ABSTRACT)

    public static abstract class StandaloneAbstract extends ComponentFactoryAbstract {
        private static final long serialVersionUID = 1L;

        private final Class<?> valueType;

        public StandaloneAbstract(Class<?> valueType) {
            super(ComponentType.VALUE, StandaloneMarkupPanel.class);
            this.valueType = valueType;
        }

        @Override
        public ApplicationAdvice appliesTo(final IModel<?> model) {
            if (!(model instanceof ValueModel))
                return ApplicationAdvice.DOES_NOT_APPLY;
            val valueModel = (ValueModel) model;
            val objectAdapter = valueModel.getObject();
            if(objectAdapter==null || objectAdapter.getPojo()==null) {
                return ApplicationAdvice.DOES_NOT_APPLY;
            }

            return appliesIf( valueType.isAssignableFrom(objectAdapter.getPojo().getClass()) );
        }

        @Override
        public final Component createComponent(final String id, final IModel<?> model) {
            return new StandaloneMarkupPanel(id, (ValueModel) model, getMarkupComponentFactory());
        }

        protected abstract MarkupComponentFactory getMarkupComponentFactory();
    }

    // -- CONCRETE COMPONENT FACTORY - PARENTED

    static class Parented extends ParentedAbstract {
        private static final long serialVersionUID = 1L;

        public Parented() {
            super(Markup.class);
        }

        @Override
        protected MarkupComponentFactory getMarkupComponentFactory() {
            return (id, model) -> {
                val markupComponent = new MarkupComponent(id, model);
                markupComponent.setEnabled(false);
                return markupComponent;    
            };
        }

    }

    // -- CONCRETE COMPONENT FACTORY - STANDALONE

    static class Standalone extends StandaloneAbstract {
        private static final long serialVersionUID = 1L;

        public Standalone() {
            super(Markup.class);
        }

        @Override
        protected MarkupComponentFactory getMarkupComponentFactory() {
            return MarkupComponent::new;
        }
    }



}
