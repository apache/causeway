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
package org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.lang.Args;

import de.agilecoders.wicket.core.util.Attributes;

/**
 * A container around Bootstrap form component that sets
 * <a href="http://getbootstrap.com/css/#forms-control-validation">validation state</a>
 */
public class FormGroup extends WebMarkupContainer {

    private static final long serialVersionUID = 1L;

    private final FormComponent<?> formComponent;

    /**
     * Constructor
     *
     * @param id The component id
     * @param formComponent The form component that controls the validation state of the form group
     */
    public FormGroup(String id, FormComponent<?> formComponent) {
        super(id);

        this.formComponent = Args.notNull(formComponent, "formComponent");
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        Attributes.addClass(tag, "form-group");

        applyFeedbackClasses(tag, formComponent);
    }

    protected void applyFeedbackClasses(ComponentTag tag, FormComponent<?> formComponent) {
        FeedbackMessages feedbackMessages = formComponent.getFeedbackMessages();
        for (FeedbackMessage feedbackMessage : feedbackMessages) {
            if (feedbackMessage.getLevel() == FeedbackMessage.ERROR) {
                Attributes.addClass(tag, "has-error");
            } else if (feedbackMessage.getLevel() == FeedbackMessage.WARNING) {
                Attributes.addClass(tag, "has-warning");
            } else if (feedbackMessage.getLevel() == FeedbackMessage.SUCCESS) {
                Attributes.addClass(tag, "has-success");
            }
        }
    }
}
