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
package org.apache.isis.viewer.wicket.model.hints;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 *
 */
public class IsisPropertyEditCompletedEvent extends IsisEventLetterAbstract {

    private final ScalarModel scalarModel;
    private final Form<?> form;

    public IsisPropertyEditCompletedEvent(ScalarModel scalarModel, AjaxRequestTarget target, Form<?> form) {
        super(target);
        this.scalarModel = scalarModel;
        this.form = form;
    }

    public ScalarModel getScalarModel() {
        return scalarModel;
    }

    public Form<?> getForm() {
        return form;
    }
}

