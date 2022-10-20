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
package org.apache.causeway.viewer.wicket.ui.test;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

class ComponentFactoryAbstractTest_init {

    private MetaModelContext metaModelContext;
    private MetaModelContext commonContext;

    @BeforeEach
    public void setUp() throws Exception {

        metaModelContext = MetaModelContext_forTesting.buildDefault();
        commonContext = metaModelContext;
    }

    @Test
    public void canInstantiateComponentFactoryWithNoComponentClass() {
        class ComponentFactoryWithNoComponentClass extends ComponentFactoryAbstract {

            private static final long serialVersionUID = 1L;

            public ComponentFactoryWithNoComponentClass() {
                super(null);
                setMetaModelContext(commonContext);
            }

            @Override
            protected ApplicationAdvice appliesTo(final IModel<?> model) {
                return null;
            }

            @Override
            public Component createComponent(final String id, final IModel<?> model) {
                return null;
            }

        }

        new ComponentFactoryWithNoComponentClass();
    }

    @Test
    public void canInstantiateComponentFactoryWithComponentClass() {
        class ComponentClass {}
        class ComponentFactoryWithComponentClass extends ComponentFactoryAbstract {

            private static final long serialVersionUID = 1L;

            public ComponentFactoryWithComponentClass() {
                super(null, ComponentClass.class);
                setMetaModelContext(commonContext);
            }

            @Override
            protected ApplicationAdvice appliesTo(final IModel<?> model) {
                return null;
            }

            @Override
            public Component createComponent(final String id, final IModel<?> model) {
                return null;
            }

        }

        new ComponentFactoryWithComponentClass();
    }

    @Test
    public void cannotInstantiateComponentFactoryWithIncorrectComponentClass() {


        class ComponentFactoryWithIncorrectComponentClass extends ComponentFactoryAbstract {

            private static final long serialVersionUID = 1L;

            public ComponentFactoryWithIncorrectComponentClass() {
                super(null, ComponentFactoryWithIncorrectComponentClass.class);
                setMetaModelContext(commonContext);
            }

            @Override
            protected ApplicationAdvice appliesTo(final IModel<?> model) {
                return null;
            }

            @Override
            public Component createComponent(final String id, final IModel<?> model) {
                return null;
            }

        }

        assertThrows(IllegalArgumentException.class, ()->{
            new ComponentFactoryWithIncorrectComponentClass();
        });

    }


}
