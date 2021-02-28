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

package org.apache.isis.viewer.wicket.ui.components.scalars;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.xeditable.XEditableBehavior;

public class XEditableBehaviorWkt extends XEditableBehavior {

    private static final long serialVersionUID = 1L;
    private AjaxEventBehavior validateListener;

    /**
     * Fired when new value for validation.
     */
    protected void onValidate(AjaxRequestTarget target, String value) {

    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        validateListener = newSaveListener();
        component.add(validateListener);
    }

    @Override
    public void unbind(Component component) {
        component.remove(validateListener);
        validateListener = null;

        super.unbind(component);
    }

    protected AjaxEventBehavior newValidateListener() {
        return new AjaxEventBehavior("validate") {

            private static final long serialVersionUID = 1L;

            /**
             * what's bound to "validate" event in JavaScript, and sent to the server
             */
            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.getDynamicExtraParameters().add("return [{'name':'newValue', 'value': attrs.event.extraData.newValue}]");
            }

            /**
             * What's received at the server
             */
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                StringValue newValue = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("newValue");
                onValidate(target, newValue.toString());
            }

        };
    }



}
