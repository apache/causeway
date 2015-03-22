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

package org.apache.isis.viewer.wicket.ui;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;

/**
 * Creates {@link Component}s of a specified {@link ComponentType}, optionally
 * {@link #appliesTo(ComponentType, IModel) dependent on} the provided
 * {@link IModel model}.
 * 
 * <p>
 * This interface is at the very heart of the Wicket Objects' model, being an
 * usage of the chain-of-responsibility design pattern. The available
 * {@link ComponentFactory}s are registered through
 * {@link ComponentFactoryRegistry} (bootstrapped from the
 * <tt>IsisWicketApplication</tt>); various adapters make it easy to lookup
 * {@link Component}s from this registry.
 */
public interface ComponentFactory extends Serializable {

    /**
     * The {@link ComponentType} with which this component factory has been
     * registered.
     */
    ComponentType getComponentType();

    public enum ApplicationAdvice {
        APPLIES(true, false), 
        APPLIES_EXCLUSIVELY(true, true), 
        DOES_NOT_APPLY(false, false);
        private final boolean applies;
        private final boolean exclusively;

        private ApplicationAdvice(final boolean applies, final boolean exclusively) {
            this.applies = applies;
            this.exclusively = exclusively;
        }

        public boolean applies() {
            return applies;
        }

        /**
         * Whether no other {@link ComponentFactory}s should apply (ie stop
         * searching for other views).
         */
        public boolean exclusively() {
            return exclusively;
        }
        
        public static final ApplicationAdvice appliesIf(final boolean b) {
            return b ? ApplicationAdvice.APPLIES : ApplicationAdvice.DOES_NOT_APPLY;
        }
    }

    /**
     * Whether the {@link Component} created by this factory applies to the
     * specified {@link ComponentType} and {@link IModel}.
     */
    ApplicationAdvice appliesTo(ComponentType componentType, IModel<?> model);

    /**
     * Creates component, with id being derived from the
     * {@link #getComponentType() component type} for this factory.
     * 
     * @param model
     * @return
     */
    Component createComponent(IModel<?> model);

    /**
     * Creates component, with specified id.
     */
    Component createComponent(String id, IModel<?> model);

    /**
     * Used for rendering in drop-downs.
     */
    String getName();

    CssResourceReference getCssResourceReference();

}
