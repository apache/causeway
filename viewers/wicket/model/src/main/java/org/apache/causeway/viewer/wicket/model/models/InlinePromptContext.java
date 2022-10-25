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
package org.apache.causeway.viewer.wicket.model.models;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.functions._Functions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class InlinePromptContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ScalarModel scalarModel;

    @Getter
    private final MarkupContainer scalarTypeContainer;

    private final Component scalarIfRegular;
    private final WebMarkupContainer scalarIfRegularInlinePromptForm;

    public void onPrompt() {
        scalarIfRegular.setVisible(false);
        scalarIfRegularInlinePromptForm.setVisible(true);
    }

    public void onCancel(final Either<ActionModel, ScalarPropertyModel> memberModel) {

        memberModel
        .accept(_Functions.noopConsumer(), prop->{
            // reset the UI form input field to the untouched property value
            val untouchedPropertyValue = prop.getManagedProperty().getPropertyValue();
            scalarModel.setObject(untouchedPropertyValue);
        });

        scalarIfRegular.setVisible(true);
        scalarIfRegularInlinePromptForm.setVisible(false);

    }


}
