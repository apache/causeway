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
package org.apache.causeway.viewer.wicket.model.hints;

import java.util.Objects;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

public class CausewaySelectorEvent extends CausewayEventLetterAbstract {

    private final Component component;
    private final String hintKey;
    private final String hintValue;

    public CausewaySelectorEvent(
            final Component component,
            final String hintKey,
            final String hintValue,
            final AjaxRequestTarget target) {
        super(target);
        this.component = component;
        this.hintKey = hintKey;
        this.hintValue = hintValue;
    }

    public String hintFor(final Component component, final String hintKey) {
        return sameComponent(component)
                && sameKey(hintKey)
                ? hintValue
                : null;
    }

    private boolean sameComponent(final Component other) {
        return Objects.equals(this.component, other);
    }

    private boolean sameKey(final String other) {
        return Objects.equals(this.hintKey, other);
    }

}

