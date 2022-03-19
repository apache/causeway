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
import java.util.function.Function;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.WktTooltips;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScalarFragmentFactory {

    @RequiredArgsConstructor
    public static enum FragmentContainer {
        SCALAR_IF_OUTPUT("scalarIfCompact"),
        SCALAR_IF_INPUT("scalarIfRegular"),
        ;
        @Getter
        private final String containerId;
        public <T extends Component> T createComponent(final Function<String, T> factory) {
            return factory.apply(containerId);
        }
    }

    // -- OUTPUT FRAGMENTS

    @Deprecated
    private static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";

    @RequiredArgsConstructor
    public static enum CompactFragment {
        CHECKBOX("fragment-compact-checkbox"),
        LABEL("fragment-compact-label"),
        ;
        private final String fragmentId;
        public Fragment createFragment(final MarkupContainer container) {
            return Wkt.fragmentAdd(
                    container, ID_SCALAR_IF_COMPACT, fragmentId);
        }
    }

    // INPUT FRAGMENTS

    @Deprecated
    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";

    @RequiredArgsConstructor
    public static enum InputFragment {
        TEXT("fragment-input-text"),
        TEXTAREA("fragment-input-textarea"),
        DATE("fragment-input-date"),
        CHECKBOX("fragment-input-checkbox"),
        FILE("fragment-input-file"),
        ;
        private final String fragmentId;
        public Fragment createFragment(final MarkupContainer container, final FormComponent<?> inputComponent) {
            val fragment = Wkt.fragmentAdd(
                    container, ScalarPanelAbstract.ID_SCALAR_VALUE_CONTAINER, fragmentId);
            fragment.add(inputComponent);
            return fragment;
        }
    }

    // PROMPT FRAGMENTS

    @RequiredArgsConstructor
    public static enum PromptFragment {
        EDIT_ICON("fragment-prompt-editicon", promptLabelModel->
            WktTooltips.addTooltip(
                new Button(ScalarPanelAbstract.ID_SCALAR_VALUE), "Click to edit")),
        LABEL("fragment-prompt-label", promptLabelModel->
            Wkt.label(ScalarPanelAbstract.ID_SCALAR_VALUE, promptLabelModel)),
        TEXTAREA("fragment-prompt-textarea", promptLabelModel->
            Wkt.textAreaNoTab(ScalarPanelAbstract.ID_SCALAR_VALUE, promptLabelModel)),
        ;
        private final String fragmentId;
        private final Function<IModel<String>, Component> componentFactory;

        public Fragment createFragment(final MarkupContainer container,
                final IModel<String> promptLabelModel,
                final @Nullable Consumer<FormComponent<String>> onComponentCreated) {
            val fragment = Wkt.fragmentAdd(
                    container, ScalarPanelAbstract.ID_SCALAR_VALUE_INLINE_PROMPT_LABEL, fragmentId);
            val component = componentFactory.apply(promptLabelModel);
            fragment.add(component);
            if(onComponentCreated!=null
                    && component instanceof FormComponent) {
                onComponentCreated.accept(_Casts.uncheckedCast(component));
            }
            return fragment;
        }
    }

}