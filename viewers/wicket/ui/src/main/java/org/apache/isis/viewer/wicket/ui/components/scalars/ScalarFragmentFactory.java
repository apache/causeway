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
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.WktComponents;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScalarFragmentFactory {

    @RequiredArgsConstructor
    public static enum FrameFragment {
        COMPACT("scalarIfCompact"),
        REGULAR("scalarIfRegular"),
        INLINE_PROMPT_FORM("scalarIfRegularInlinePromptForm")
        ;
        @Getter
        private final String containerId;
        public <T extends Component> T createComponent(final Function<String, T> factory) {
            return factory.apply(containerId);
        }
    }

    @RequiredArgsConstructor
    public static enum RegularFrame {

        SCALAR_VALUE_INLINE_PROMPT_LINK("scalarValueInlinePromptLink"),

        OUTPUT_FORMAT_CONTAINER("container-scalarValue-outputFormat"),
        INPUT_FORMAT_CONTAINER("container-scalarValue-inputFormat"),

        EDIT_PROPERTY("editProperty"),
        FEEDBACK("feedback"),
        ASSOCIATED_ACTION_LINKS_BELOW("associatedActionLinksBelow"),
        ASSOCIATED_ACTION_LINKS_RIGHT("associatedActionLinksRight"),

        ;
        @Getter
        private final String containerId;
        public <T extends Component> T createComponent(final Function<String, T> factory) {
            return factory.apply(containerId);
        }
        public void permanentlyHideIn(final MarkupContainer container) {
            container.streamChildren()
            .filter(comp->containerId.equals(comp.getId()))
            .findFirst()
            .ifPresentOrElse(comp->{
                comp.setVisibilityAllowed(false);
                comp.setVisible(false);
            },
            ()->{
                WktComponents.permanentlyHide(container, containerId);
            });
        }
    }

    // -- OUTPUT FRAGMENTS

    //TODO CompactFragment and OutputFragment should be unified
    @RequiredArgsConstructor
    public static enum CompactFragment {
        CHECKBOX("fragment-compact-checkbox"),
        LABEL("fragment-compact-label"),
        BADGE("fragment-compact-badge"),
        ;
        private final String fragmentId;
        /**
         * @param id - Where to 'put' the fragment
         * @param markupProvider - The component whose markup contains the fragment's markup
         * @param componentFactory - creates the scalarValue component to be added to the fragment
         */
        public <T extends Component> Fragment
        createFragment(final String id, final MarkupContainer markupProvider, final Function<String, T> componentFactory) {
            val fragment = Wkt.fragment(id, fragmentId, markupProvider);
            fragment.add(componentFactory.apply(ScalarPanelAbstract.ID_SCALAR_VALUE));
            return fragment;
        }
    }

    // INPUT FRAGMENTS

    @RequiredArgsConstructor
    public static enum InputFragment {
        TEXT("fragment-input-text"),
        TEXTAREA("fragment-input-textarea"),
        DATE("fragment-input-date"),
        CHECKBOX("fragment-input-checkbox"),
        FILE("fragment-input-file"),
        ;
        private final String fragmentId;
        /**
         * @param markupProvider - The component whose markup contains the fragment's markup
         */
        public Fragment createFragment(final MarkupContainer markupProvider, final FormComponent<?> inputComponent) {
            val fragment = Wkt.fragment(
                    RegularFrame.INPUT_FORMAT_CONTAINER.getContainerId(), fragmentId, markupProvider);
            fragment.add(inputComponent);
            return fragment;
        }
    }

    // PROMPT FRAGMENTS

    @RequiredArgsConstructor
    public static enum PromptFragment {
        LABEL("fragment-prompt-label", promptLabelModel->
            Wkt.label(ScalarPanelAbstract.ID_SCALAR_VALUE, promptLabelModel)),
        TEXTAREA("fragment-prompt-textarea", promptLabelModel->
            Wkt.textAreaNoTab(ScalarPanelAbstract.ID_SCALAR_VALUE, promptLabelModel)),
        ;
        private final String fragmentId;
        private final Function<IModel<String>, Component> componentFactory;

        /**
         * @param markupProvider - The component whose markup contains the fragment's markup
         */
        public Fragment createFragment(final MarkupContainer markupProvider,
                final IModel<String> promptLabelModel,
                final @Nullable Consumer<FormComponent<String>> onComponentCreated) {
            val fragment = Wkt.fragment(
                    RegularFrame.OUTPUT_FORMAT_CONTAINER.getContainerId(), fragmentId, markupProvider);
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