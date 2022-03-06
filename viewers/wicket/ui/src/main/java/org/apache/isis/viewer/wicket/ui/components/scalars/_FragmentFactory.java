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

import java.util.function.Consumer;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class _FragmentFactory {

    // -- REGULAR

    @RequiredArgsConstructor
    public static enum RegularFragment {
        TEXT_INPUT("fragmentValueAsTextInput"),
        TEXTAREA_INPUT("fragmentValueAsTextarea"),
        DATE_INPUT("fragmentValueAsDateInput"),
        TEXT_PROMPT("fragmentValueAsTextInlinePrompt"),
        TEXTAREA_PROMPT("fragmentValueAsTextareaInlinePrompt"),
        EDIT_ICON_PROMPT("fragmentValueAsEditIconInlinePrompt"),
        ;
        private final String fragmentId;
        public Fragment createFragment(final String id, final MarkupContainer container) {
            return Wkt.fragmentAdd(container, id, fragmentId);
        }
        public Fragment createFragmentNoTab(final String id, final MarkupContainer container) {
            return Wkt.fragmentAddNoTab(container, id, fragmentId);
        }
    }

    Fragment createRegularFragment(final RegularFragment type, final MarkupContainer container) {
        return type.createFragment(ScalarPanelAbstract.ID_SCALAR_VALUE_CONTAINER, container);
    }

    Fragment createInlinePromptFragment(final RegularFragment type, final MarkupContainer container) {
        return type.createFragmentNoTab(ScalarPanelAbstract.ID_SCALAR_VALUE_INLINE_PROMPT_LABEL, container);
    }

    // -- COMPACT

    @RequiredArgsConstructor
    public static enum CompactFragment {
        CHECKBOX("fragmentCompactAsCheckbox"),
        SPAN("fragmentCompactAsSpan");
        private final String fragmentId;
        public Fragment createFragment(final String id, final MarkupContainer container) {
            return Wkt.fragmentAdd(container, id, fragmentId);
        }
    }

    Fragment createCompactFragment(final CompactFragment type, final MarkupContainer container) {
        return type.createFragment(ScalarPanelAbstract.ID_SCALAR_IF_COMPACT, container);
    }

    // -- SHORTCUTS

    Fragment promptOnEditIcon(final MarkupContainer container, final IModel<String> promptLabelModel) {
        val fragment = createInlinePromptFragment(RegularFragment.EDIT_ICON_PROMPT, container);
        val editPromptLink = Wkt.add(fragment, new Button(ScalarPanelAbstract.ID_SCALAR_VALUE));
        Tooltips.addTooltip(editPromptLink, "edit");
        return fragment;
    }

    Fragment promptOnLabel(
            final MarkupContainer container,
            final IModel<String> promptLabelModel) {
        val fragment = createInlinePromptFragment(RegularFragment.TEXT_PROMPT, container);
        Wkt.labelAdd(fragment, ScalarPanelAbstract.ID_SCALAR_VALUE, promptLabelModel);
        return fragment;
    }

    Fragment promptOnTextarea(
            final MarkupContainer container,
            final IModel<String> promptLabelModel,
            final Consumer<FormComponent<String>> onComponentCreated) {
        val fragment = _FragmentFactory.createInlinePromptFragment(RegularFragment.TEXTAREA_PROMPT, container);
        val inlinePromptTextArea = Wkt.textAreaAddNoTab(fragment, ScalarPanelAbstract.ID_SCALAR_VALUE, promptLabelModel);
        onComponentCreated.accept(inlinePromptTextArea);
        return fragment;
    }
}